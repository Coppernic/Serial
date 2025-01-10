package fr.coppernic.tools.transparent.transparent

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import fr.coppernic.sdk.utils.core.CpcBytes
import fr.coppernic.tools.transparent.R
import fr.coppernic.tools.transparent.databinding.FragmentTransparentBinding
import fr.coppernic.tools.transparent.home.LogAdapter
import fr.coppernic.tools.transparent.settings.SettingsInteractor
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class TransparentFragment : androidx.fragment.app.Fragment(), TransparentView {
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var logs = ArrayList<String>()

    private val serialPortViewModel: SerialPortViewModel by viewModel()

    private val settings: SettingsInteractor by inject()

    private lateinit var binding: FragmentTransparentBinding

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

//        presenter.setUp(this).subscribe {
//            if(it) {
                // Switch open is accessible
                enableSwOpen(true)
//            }
//        }

//        initializeSerialPorts()

        updateSpinner(this.context)

        binding.swOpen.setOnClickListener {
            if (binding.swOpen.isChecked) {
                    serialPortViewModel.launchTransparentPortsMode(binding.spPortName.selectedItem.toString(),
                        binding.spPortInBaudrate.selectedItem.toString().toInt(),
                        binding.spPortOutName.selectedItem.toString(),
                        binding.spPortOutBaudrate.selectedItem.toString().toInt())

//                presenter.openPorts(binding.spPortName.selectedItem.toString(),
//                        binding.spPortInBaudrate.selectedItem.toString().toInt(),
//                        binding.spPortOutName.selectedItem.toString(),
//                        binding.spPortOutBaudrate.selectedItem.toString().toInt())
            } else {
                    serialPortViewModel.closePorts()
//                presenter.closePorts()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            serialPortViewModel.portInData.collect { portData ->
                addLog(">> " + CpcBytes.byteArrayToString(portData))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            serialPortViewModel.portOutData.collect { portData ->
                addLog("<< " + CpcBytes.byteArrayToString(portData))
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity.let {
            it?.menuInflater?.inflate(fr.coppernic.tools.transparent.R.menu.menu_main, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        when (id) {
            fr.coppernic.tools.transparent.R.id.action_clear_logs -> {
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

    private fun updateSpinner(context: Context?) {
        if (context == null) return

        viewLifecycleOwner.lifecycleScope.launch {
            serialPortViewModel.serialPortListFlow.collect { serialPortList ->
                if (serialPortList != null) {
                    val adapterPortIn = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item )
                    val adapterPortOut = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item )
                    serialPortList.map { serialPort ->
                        adapterPortIn.add(serialPortViewModel.getSerialPortReference(serialPort))
                        adapterPortOut.add(serialPortViewModel.getSerialPortReference(serialPort))
                    }
                    binding.spPortName.adapter = adapterPortIn
                    binding.spPortOutName.adapter = adapterPortIn
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            serialPortViewModel.updateSerialPortList()
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

    override fun showError(error: TransparentView.Error) {
        val message =  when (error) {
            TransparentView.Error.OPEN_ERROR_PORT_IN -> R.string.error_open_port_in
            TransparentView.Error.OPEN_ERROR_PORT_OUT -> R.string.error_open_port_out
            TransparentView.Error.OK -> R.string.error_ok
        }
        Snackbar.make(binding.rvLogs, message, Snackbar.LENGTH_SHORT).show()
    }
}
