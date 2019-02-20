package fr.coppernic.tools.transparent.transparent


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
import kotlinx.android.synthetic.main.fragment_transparent.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
class TransparentFragment @Inject constructor() : Fragment(), TransparentView {
    @Inject
    lateinit var presenter:TransparentPresenter

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var logs = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transparent, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        initializeRecyclerView()

        spPortOutName.setSelection(1)

        enableSwOpen(false)

        presenter.setUp(this).subscribe {
            if(it) {
                // Switch open is accessible
                enableSwOpen(true)
            }
        }

        initializeSerialPorts()

        swOpen.setOnClickListener {
            if (swOpen.isChecked) {
                presenter.openPorts(spPortName.selectedItem.toString(),
                        spPortInBaudrate.selectedItem.toString().toInt(),
                        spPortOutName.selectedItem.toString(),
                        spPortOutBaudrate.selectedItem.toString().toInt())
            } else {
                presenter.closePorts()
            }
        }

        super.onViewCreated(view, savedInstanceState)
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

    private fun enableSwOpen(enable:Boolean) {
        swOpen.isEnabled = enable
    }

    override fun addLog(log: String) {
            activity.let {
                it?.runOnUiThread {
                    logs.add(log)
                    tvEmptyLogs.visibility = View.INVISIBLE
                    rvLogs.adapter.notifyDataSetChanged()
                }
            }
    }

    private fun initializeRecyclerView() {
        viewManager = LinearLayoutManager(activity)
        viewAdapter = LogAdapter(logs)

        rvLogs.apply {
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

        Snackbar.make(rvLogs, message, Snackbar.LENGTH_SHORT).show()
    }
}
