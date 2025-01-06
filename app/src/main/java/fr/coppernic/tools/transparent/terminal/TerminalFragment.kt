package fr.coppernic.tools.transparent.terminal

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import fr.coppernic.sdk.serial.SerialCom
import fr.coppernic.sdk.serial.SerialFactory
import fr.coppernic.sdk.utils.io.InstanceListener
import fr.coppernic.tools.transparent.R
import fr.coppernic.tools.transparent.databinding.FragmentTerminalBinding
import fr.coppernic.tools.transparent.home.LogAdapter
import fr.coppernic.tools.transparent.settings.SettingsInteractor

import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class TerminalFragment @Inject constructor() : Fragment(), TerminalView {
    @Inject
    lateinit var presenter: TerminalPresenter

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var logs = ArrayList<String>()

    @Inject
    lateinit var settings: SettingsInteractor

    private lateinit var binding: FragmentTerminalBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentTerminalBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableSwOpen(false)

        initializeRecyclerView()
        initializeSerialPort()

        presenter.setUp(this).subscribe {
            if (it) {
                enableSwOpen(true)
            }
        }

        binding.swOpen.setOnCheckedChangeListener { _, checked ->
            when(checked) {
                false -> presenter.closePort()
                true -> presenter.openPort(binding.spPortName.selectedItem.toString(), binding.spPortBaudrate.selectedItem.toString().toInt())
            }
        }

        binding.fabSend.setOnClickListener {
            presenter.send(binding.etDataToSend.text.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_logs -> {
                logs.clear()
                viewAdapter.notifyDataSetChanged()
                binding.tvEmptyLogs.visibility = View.VISIBLE
            }
        }
        return true
    }

    private fun initializeRecyclerView() {
        viewManager = LinearLayoutManager(activity)
        viewAdapter = LogAdapter(logs)

        binding.rvLogs.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(DividerItemDecoration(this@TerminalFragment.context, LinearLayoutManager.VERTICAL))
            adapter = viewAdapter
        }
    }

    private fun initializeSerialPort() {
        // In port instantiation
        context?.let {
            SerialFactory.getDirectInstance(it, object : InstanceListener<SerialCom> {
                override fun onDisposed(p0: SerialCom) {

                }

                override fun onCreated(p0: SerialCom) {
                    presenter.setPort(p0)
                }
            })
        }
    }

    private fun enableSwOpen(enable:Boolean) {
        binding.swOpen.isEnabled = enable
    }

    override fun addLog(log: String) {
        if (settings.getLogEnable()) {
            activity.let {
                it?.runOnUiThread {
                    logs.add(0, log)
                    viewAdapter.notifyDataSetChanged()
                    binding.tvEmptyLogs.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun showError(error: TerminalView.Error) {
        val message = when(error) {
            TerminalView.Error.OPEN_ERROR -> R.string.error_open
            TerminalView.Error.INCORRECT_DATA_TO_SEND -> R.string.error_incorrect_data_to_send
            TerminalView.Error.PORT_NOT_OPENED -> R.string.error_port_not_opened
            else -> {
                R.string.error_ok
            }
        }

        Snackbar.make(binding.fabSend, message, Snackbar.LENGTH_SHORT).show()
    }
}
