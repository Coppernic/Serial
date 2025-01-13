package fr.coppernic.tools.transparent.transparent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.coppernic.sdk.utils.core.CpcBytes
import fr.coppernic.tools.transparent.domain.models.SerialPort
import fr.coppernic.tools.transparent.domain.models.SerialPortComm
import fr.coppernic.tools.transparent.domain.repositories.SerialPortRepo
import fr.coppernic.tools.transparent.settings.SettingsInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class SerialPortViewModel(
    val serialPortRepo: SerialPortRepo,
    val settingsInteractor: SettingsInteractor,
) : ViewModel() {

    private val _serialPortListFlow = MutableStateFlow<List<SerialPort>?>(null)
    val serialPortListFlow = _serialPortListFlow.asStateFlow()

    fun updateSerialPortList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _serialPortListFlow.value = serialPortRepo.getSerialPortList()
            }
        }
    }

    fun getSerialPortReference(serialPort: SerialPort): String {
        return serialPortRepo.getSerialPortReference(serialPort)
    }

    private var serialPortCommIn: SerialPortComm? = null
    private var serialPortCommOut: SerialPortComm? = null

    private var backgroundJobTerminal: Job? = null

    private var openTransparentPortsJob: Job? = null

    private var backgroundJobReadIn: Job? = null
    private var backgroundJobReadOut: Job? = null

    private var backgroundJobSendIn: Job? = null
    private var backgroundJobSendOut: Job? = null

    private val _portInData = MutableStateFlow<ByteArray>(byteArrayOf())
    val portInData: StateFlow<ByteArray> = _portInData.asStateFlow()

    private val _portOutData = MutableStateFlow<ByteArray>(byteArrayOf())
    val portOutData: StateFlow<ByteArray> = _portOutData.asStateFlow()

    val portOutChannel = Channel<ByteArray>(Channel.RENDEZVOUS)

    val portInChannel = Channel<ByteArray>(Channel.RENDEZVOUS)


    fun launchTransparentPortsMode(
        nameIn: String,
        baudrateIn: Int,
        nameOut: String,
        baudrateOut: Int
    ) {
        openTransparentPortsJob = openTransparentPorts(nameIn, baudrateIn, nameOut, baudrateOut)
        backgroundJobReadIn = launchTransparentReadJob(true, portOutChannel, _portInData)
        backgroundJobReadOut = launchTransparentReadJob(false, portInChannel, _portOutData)
        backgroundJobSendIn = launchTransparentSendJob(true, portInChannel)
        backgroundJobSendOut = launchTransparentSendJob(false, portOutChannel)
    }

    fun openTransparentPorts(
        nameIn: String,
        baudrateIn: Int,
        nameOut: String,
        baudrateOut: Int
    ): Job {
        return viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val serialPortIn = serialPortRepo.getSerialPortFromReference(nameIn)
                serialPortCommIn = serialPortIn?.let {
                    serialPortRepo.openSerialPort(
                        it, baudrateIn, settingsInteractor.getPortRts(),
                        settingsInteractor.getPortXonXoff(),
                        settingsInteractor.getPortHardwareFlowControl()
                    )
                }
                val serialPortOut = serialPortRepo.getSerialPortFromReference(nameOut)
                serialPortCommOut = serialPortOut?.let {
                    serialPortRepo.openSerialPort(
                        it, baudrateOut, settingsInteractor.getPortRts(),
                        settingsInteractor.getPortXonXoff(),
                        settingsInteractor.getPortHardwareFlowControl()
                    )
                }
            }
        }
    }

    fun launchTransparentReadJob(
        inOutPort: Boolean,
        portChannel: Channel<ByteArray>,
        portDataMutableStateFlow: MutableStateFlow<ByteArray>
    ): Job {
        return viewModelScope.launch {
            withContext(Dispatchers.IO) {
                openTransparentPortsJob?.join()

                val serialPortComm = if (inOutPort) {
                    serialPortCommIn
                } else {
                    serialPortCommOut
                }

                while (true) {
                    delay(200)
                    serialPortComm?.let {
                        val data = serialPortRepo.receiveData(serialPortComm)

                        if (data.isNotEmpty()) {
                            Timber.d("After read serialPortComm $serialPortComm (inOutPort = $inOutPort) : size=${data.size} $data")
                            portDataMutableStateFlow.update { data }
                            portChannel.send(data)
                        }
                    }
                }
            }
        }
    }

    fun launchTransparentSendJob(
        inOutPort: Boolean,
        portChannel: Channel<ByteArray>,
    ): Job {
        return viewModelScope.launch {
            withContext(Dispatchers.IO) {
                openTransparentPortsJob?.join()

                val serialPortComm = if (inOutPort) {
                    serialPortCommIn
                } else {
                    serialPortCommOut
                }

                while (true) {
                    serialPortComm?.let {
                        Timber.d("in launchTransparentSendJob for serialPortComm = $serialPortComm")
                        val dataToSend = portChannel.receive()
                        Timber.d("Send on serialPortComm $serialPortComm (inOutPort = $inOutPort) : size=${dataToSend.size} $dataToSend")
                        serialPortRepo.sendData(serialPortComm, dataToSend)
                    }
                }
            }
        }
    }

    fun openTerminalPort(name: String, baudrate: Int) {
        backgroundJobTerminal = viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val serialPort = serialPortRepo.getSerialPortFromReference(name)

                serialPort?.let {
                    serialPortCommIn = serialPortRepo.openSerialPort(
                        it, baudrate, settingsInteractor.getPortRts(),
                        settingsInteractor.getPortXonXoff(),
                        settingsInteractor.getPortHardwareFlowControl()
                    )
                }

                serialPortCommOut = null

                while (true) {
                    delay(200)

                    serialPortCommIn?.let { serialPortComm ->
                        val data = serialPortRepo.receiveData(serialPortComm)
                        if (data.isNotEmpty()) {
                            _portInData.update { data }
                        }
                    }
                }
            }
        }
    }

    fun convertData(data: String): ByteArray? {
        try {
            val dataBytes: ByteArray?
            dataBytes = when (settingsInteractor.getCommunicationAscii()) {
                true -> {
                    var dataStr = data
                    when (settingsInteractor.getCommunicationSuffix()) {
                        "[CR]" -> dataStr += "\r"
                        "[LF]" -> dataStr += "\n"
                        "[CR][LF]" -> dataStr += "\r\n"
                    }
                    dataStr.toByteArray(Charsets.UTF_8)
                }

                false -> {
                    CpcBytes.parseHexStringToArray(data)
                }
            }
            return dataBytes
        } catch (ex: Exception) {
            return null
        }
    }

    fun sendTerminalPort(data: ByteArray) {
        viewModelScope.launch {
            serialPortCommIn?.let { serialPortRepo.sendData(it, data) }
        }
    }

    fun closePorts() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                backgroundJobTerminal?.cancelAndJoin()
                backgroundJobTerminal = null
                backgroundJobReadIn?.cancelAndJoin()
                backgroundJobReadIn = null
                backgroundJobReadOut?.cancelAndJoin()
                backgroundJobReadOut = null
                backgroundJobSendIn?.cancelAndJoin()
                backgroundJobSendIn = null
                backgroundJobSendOut?.cancelAndJoin()
                backgroundJobSendOut = null

                serialPortCommIn?.let {
                    serialPortRepo.closeSerialPort(it)
                    serialPortCommIn = null
                }
                serialPortCommOut?.let {
                    serialPortRepo.closeSerialPort(it)
                    serialPortCommOut = null
                }
            }
        }
    }
}