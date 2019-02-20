package fr.coppernic.tools.transparent.settings

interface SettingsInteractor {
    fun getPortRts():Boolean
    fun getPortXonXoff():Boolean
    fun getPortHardwareFlowControl():Boolean
    fun getCommunicationAscii():Boolean
    fun getCommunicationSuffix():String
}