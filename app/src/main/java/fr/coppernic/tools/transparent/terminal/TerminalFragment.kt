package fr.coppernic.tools.transparent.terminal

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import fr.coppernic.sdk.serial.SerialCom
import fr.coppernic.sdk.serial.SerialFactory
import fr.coppernic.sdk.utils.io.InstanceListener
import fr.coppernic.tools.transparent.R
import fr.coppernic.tools.transparent.home.LogAdapter
import kotlinx.android.synthetic.main.fragment_terminal.*

import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class TerminalFragment @Inject constructor() : Fragment(), TerminalView {
    @Inject
    lateinit var presenter:TerminalPresenter

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var logs = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_terminal, container, false)
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

        swOpen.setOnCheckedChangeListener { _, checked ->
            when(checked) {
                false -> presenter.closePort()
                true -> presenter.openPort(spPortName.selectedItem.toString(), spPortBaudrate.selectedItem.toString().toInt())
            }
        }

        fabSend.setOnClickListener {
            presenter.send(etDataToSend.text.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        activity.let {
            it?.menuInflater?.inflate(R.menu.menu_main, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val id = item?.itemId

        when(id) {
            R.id.action_clear_logs -> {
                logs.clear()
                viewAdapter.notifyDataSetChanged()
                tvEmptyLogs.visibility = View.VISIBLE
            }
        }

        return true
    }

    private fun initializeRecyclerView() {
        viewManager = LinearLayoutManager(activity)
        viewAdapter = LogAdapter(logs)

        rvLogs.apply {
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
        swOpen.isEnabled = enable
    }

    override fun addLog(log: String) {
        activity.let{
            it?.runOnUiThread {
                logs.add(0, log)
                viewAdapter.notifyDataSetChanged()
                tvEmptyLogs.visibility = View.INVISIBLE
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

        Snackbar.make(fabSend, message, Snackbar.LENGTH_SHORT).show()
    }
}