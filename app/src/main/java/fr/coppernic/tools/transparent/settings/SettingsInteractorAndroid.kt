package fr.coppernic.tools.transparent.settings

import android.content.Context
import android.content.SharedPreferences
import fr.coppernic.tools.transparent.R
import javax.inject.Inject

class SettingsInteractorAndroid @Inject constructor():SettingsInteractor {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var sharedPreferences:SharedPreferences

    override fun getPortRts(): Boolean {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_port_rts_key), false)
    }

    override fun getPortXonXoff(): Boolean {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_port_xon_xoff_key), false)
    }

    override fun getPortHardwareFlowControl(): Boolean {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_port_hardware_flow_control_key), false)
    }

    override fun getCommunicationAscii(): Boolean {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_communication_ascii_key), false)
    }

    override fun getCommunicationSuffix(): String? {
        return sharedPreferences.getString(context.getString(R.string.pref_communication_suffix_key), context.resources.getStringArray(R.array.pref_communication_suffix_list_values)[0])
    }
}