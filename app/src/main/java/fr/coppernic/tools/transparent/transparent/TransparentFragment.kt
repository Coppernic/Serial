package fr.coppernic.tools.transparent.transparent


import android.annotation.SuppressLint
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import fr.coppernic.sdk.serial.SerialCom
import fr.coppernic.sdk.serial.SerialFactory
import fr.coppernic.sdk.utils.io.InstanceListener
import fr.coppernic.tools.transparent.R
import fr.coppernic.tools.transparent.databinding.FragmentTransparentBinding
import fr.coppernic.tools.transparent.home.LogAdapter
import fr.coppernic.tools.transparent.settings.SettingsInteractor
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
class TransparentFragment @Inject constructor() : androidx.fragment.app.Fragment(), TransparentView {
    @Inject
    lateinit var presenter: TransparentPresenter

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var logs = ArrayList<String>()

    @Inject
    lateinit var settings: SettingsInteractor

    lateinit var binding: FragmentTransparentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentTransparentBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        initializeRecyclerView()

        binding.spPortOutName.setSelection(1)

        enableSwOpen(false)

        presenter.setUp(this).subscribe {
            if(it) {
                // Switch open is accessible
                enableSwOpen(true)
            }
        }

        initializeSerialPorts()

        binding.swOpen.setOnClickListener {
            if (binding.swOpen.isChecked) {
                presenter.openPorts(binding.spPortName.selectedItem.toString(),
                        binding.spPortInBaudrate.selectedItem.toString().toInt(),
                        binding.spPortOutName.selectedItem.toString(),
                        binding.spPortOutBaudrate.selectedItem.toString().toInt())
            } else {
                presenter.closePorts()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity.let {
            it?.menuInflater?.inflate(R.menu.menu_main, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        when (id) {
            R.id.action_clear_logs -> {
                logs.clear()
                viewAdapter.notifyDataSetChanged()
                binding.tvEmptyLogs.visibility = View.VISIBLE
            }
        }

        return true
    }

    private fun enableSwOpen(enable:Boolean) {
        binding.swOpen.isEnabled = enable
    }

    override fun addLog(log: String) {
        if (settings.getLogEnable()) {
            activity.let {
                it?.runOnUiThread {
                    logs.add(log)
                    binding.tvEmptyLogs.visibility = View.INVISIBLE
                    binding.rvLogs.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun initializeRecyclerView() {
        viewManager = LinearLayoutManager(activity)
        viewAdapter = LogAdapter(logs)

        binding.rvLogs.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(DividerItemDecoration(this@TransparentFragment.context, LinearLayoutManager.VERTICAL))
            adapter = viewAdapter
        }
    }

    private fun initializeSerialPorts() {
        // In port instantiation
        context?.let {
            SerialFactory.getDirectInstance(it, object : InstanceListener<SerialCom> {
                override fun onDisposed(p0: SerialCom?) {

                }

                override fun onCreated(p0: SerialCom?) {
                    presenter.setPort(TransparentView.Port.IN, p0)
                }
            })
        }

        // Out port instantiation
        context?.let {
            SerialFactory.getDirectInstance(it, object : InstanceListener<SerialCom> {
                override fun onDisposed(p0: SerialCom?) {

                }

                override fun onCreated(p0: SerialCom?) {
                    presenter.setPort(TransparentView.Port.OUT, p0)
                }
            })
        }
    }

    override fun showError(error: TransparentView.Error) {
        val message =  when (error) {
            TransparentView.Error.OPEN_ERROR_PORT_IN -> R.string.error_open_port_in
            TransparentView.Error.OPEN_ERROR_PORT_OUT -> R.string.error_open_port_out
            TransparentView.Error.OK -> R.string.error_ok
        }
        Snackbar.make(binding.rvLogs, message, Snackbar.LENGTH_SHORT).show()
    }
}
