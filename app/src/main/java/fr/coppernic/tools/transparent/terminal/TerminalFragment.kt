package fr.coppernic.tools.transparent.terminal

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import fr.coppernic.sdk.utils.core.CpcBytes
import fr.coppernic.tools.transparent.R
import fr.coppernic.tools.transparent.databinding.FragmentTerminalBinding
import fr.coppernic.tools.transparent.home.LogAdapter
import fr.coppernic.tools.transparent.settings.SettingsInteractor
import fr.coppernic.tools.transparent.transparent.SerialPortViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 *
 */
class TerminalFragment : Fragment(), TerminalView {
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var logs = ArrayList<String>()

    private val serialPortViewModel: SerialPortViewModel by viewModel()

    private val settings: SettingsInteractor by inject()

    private lateinit var binding: FragmentTerminalBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTerminalBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableSwOpen(false)

        initializeRecyclerView()
        enableSwOpen(true)

        updateSpinner(this.context)

        binding.swOpen.setOnCheckedChangeListener { _, checked ->
            when (checked) {
                false -> serialPortViewModel.closePorts()
                true -> serialPortViewModel.openTerminalPort(
                    binding.spPortName.selectedItem.toString(),
                    binding.spPortBaudrate.selectedItem.toString().toInt()
                )
            }
        }

        binding.fabSend.setOnClickListener {
            val dataString = binding.etDataToSend.text.toString()
            val dataToSend = serialPortViewModel.convertData(dataString)
            if (dataToSend != null) {
                serialPortViewModel.sendTerminalPort(dataToSend)
            } else {
                showError(TerminalView.Error.INCORRECT_DATA_TO_SEND)
            }
            addLog(">> $dataString")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            serialPortViewModel.portInData.collect { portData ->
                addLog("<< " + CpcBytes.byteArrayToString(portData))
            }
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

    private fun updateSpinner(context: Context?) {
        if (context == null) return

        viewLifecycleOwner.lifecycleScope.launch {
            serialPortViewModel.serialPortListFlow.collect { serialPortList ->
                if (serialPortList != null) {
                    val adapterPort =
                        ArrayAdapter<String>(context, android.R.layout.simple_spinner_item)
                    serialPortList.map { serialPort ->
                        adapterPort.add(serialPortViewModel.getSerialPortReference(serialPort))
                    }
                    binding.spPortName.adapter = adapterPort
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
            addItemDecoration(
                DividerItemDecoration(
                    this@TerminalFragment.context,
                    LinearLayoutManager.VERTICAL
                )
            )
            adapter = viewAdapter
        }
    }

    private fun enableSwOpen(enable: Boolean) {
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
        val message = when (error) {
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
