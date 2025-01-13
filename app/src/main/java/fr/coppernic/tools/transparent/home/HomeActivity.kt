package fr.coppernic.tools.transparent.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import fr.coppernic.tools.transparent.App
import fr.coppernic.tools.transparent.R
import fr.coppernic.tools.transparent.databinding.ActivityMainBinding
import fr.coppernic.tools.transparent.settings.SettingsActivity
import fr.coppernic.tools.transparent.terminal.TerminalFragment
import fr.coppernic.tools.transparent.transparent.TransparentFragment

class HomeActivity : AppCompatActivity() {

    val transparentFragment:TransparentFragment = TransparentFragment()
    val terminalFragment: TerminalFragment = TerminalFragment()

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App.appComponents.inject(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.swMode.setOnCheckedChangeListener { it, checked ->
            when(checked) {
                false ->  {
                    it.text = getString(R.string.transparent)
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.container, transparentFragment, "Transparent")
                    fragmentTransaction.commit()
                }
                true -> {
                    it.text = getString(R.string.terminal)
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.container, terminalFragment, "terminal")
                    fragmentTransaction.commit()
                }
            }
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.container, transparentFragment, "Transparent")
        fragmentTransaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.action_clear_logs -> {
            }

            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }

        return false
    }
}
