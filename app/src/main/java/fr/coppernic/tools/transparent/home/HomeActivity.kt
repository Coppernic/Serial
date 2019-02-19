package fr.coppernic.tools.transparent.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ArrayAdapter
import fr.coppernic.sdk.serial.SerialCom
import fr.coppernic.sdk.serial.SerialFactory
import fr.coppernic.sdk.utils.io.InstanceListener
import fr.coppernic.tools.transparent.App
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import android.support.v7.widget.DividerItemDecoration
import android.view.Menu
import android.view.MenuItem
import fr.coppernic.tools.transparent.R

class HomeActivity : AppCompatActivity(), HomeView {

    private val inPortNames = ArrayList<String>()
    private val outPortNames = ArrayList<String>()
    private val inPortBaudrates = ArrayList<String>()
    private val outPortBaudrates = ArrayList<String>()

    private lateinit var inNamesAdapter:ArrayAdapter<String>
    private lateinit var outNamesAdapter:ArrayAdapter<String>
    private lateinit var inBaudratesAdapter:ArrayAdapter<String>
    private lateinit var outBaudratesAdapter:ArrayAdapter<String>

    @Inject
    lateinit var presenter:HomePresenter

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var logs = ArrayList<String>()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(fr.coppernic.tools.transparent.R.layout.activity_main)

        App.appComponents.inject(this)

        inNamesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, inPortNames)
        spPortInName.adapter = inNamesAdapter
        outNamesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, outPortNames)
        spPortOutName.adapter = outNamesAdapter
        inBaudratesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, inPortBaudrates)
        spPortInBaudrate.adapter = inBaudratesAdapter
        outBaudratesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, outPortBaudrates)
        spPortOutBaudrate.adapter = outBaudratesAdapter

        viewManager = LinearLayoutManager(this)
        viewAdapter = LogAdapter(logs)

        rvLogs.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(DividerItemDecoration(this@HomeActivity, LinearLayoutManager.VERTICAL))
            adapter = viewAdapter
        }

        enableSwOpen(false)

        presenter.setUp(this).subscribe {
            if(it) {
                // Switch open is accessible
                enableSwOpen(true)
            }
        }

        // In port instantiation
        SerialFactory.getDirectInstance(this, object : InstanceListener<SerialCom> {
            override fun onDisposed(p0: SerialCom?) {

            }

            override fun onCreated(p0: SerialCom?) {
                presenter.setPort(HomeView.Port.IN, p0)
            }
        })

        // Out port instantiation
        SerialFactory.getDirectInstance(this, object : InstanceListener<SerialCom> {
            override fun onDisposed(p0: SerialCom?) {

            }

            override fun onCreated(p0: SerialCom?) {
                presenter.setPort(HomeView.Port.OUT, p0)
            }
        })

        swOpen.setOnClickListener {
            if (swOpen.isChecked) {
                presenter.openPorts(spPortInName.selectedItem.toString(),
                        spPortInBaudrate.selectedItem.toString().toInt(),
                        spPortOutName.selectedItem.toString(),
                        spPortOutBaudrate.selectedItem.toString().toInt())
            } else {
                presenter.closePorts()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            val id = item.itemId

            when(id) {
                R.id.action_clear_logs -> {
                    logs.clear()
                    viewAdapter.notifyDataSetChanged()
                }
            }
        }

        return false
    }

    override fun addPort(port: HomeView.Port, name: String) {
        when(port) {
            HomeView.Port.IN -> {
                inPortNames.add(name)
                inNamesAdapter.notifyDataSetChanged()
            }
            HomeView.Port.OUT -> {
                outPortNames.add(name)
                outNamesAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun addBaudrate(port: HomeView.Port, baudrate: Int) {
        when(port) {
            HomeView.Port.IN -> {
                inPortBaudrates.add(baudrate.toString())
                inBaudratesAdapter.notifyDataSetChanged()
            }
            HomeView.Port.OUT -> {
                outPortBaudrates.add(baudrate.toString())
                outBaudratesAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun enableSwOpen(enable:Boolean) {
        swOpen.isEnabled = enable
    }

    override fun addLog(log: String) {
        runOnUiThread {
            logs.add(log)
            rvLogs.adapter.notifyDataSetChanged()
        }
    }
}
