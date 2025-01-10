package fr.coppernic.tools.transparent.data.sources

import android.content.Context
import fr.coppernic.sdk.serial.SerialCom
import fr.coppernic.sdk.serial.SerialFactory
import fr.coppernic.sdk.utils.helpers.OsHelper
import fr.coppernic.sdk.utils.io.InstanceListener
import fr.coppernic.tools.transparent.domain.models.SerialPortComm
import fr.coppernic.tools.transparent.domain.models.SerialPortPhysical
import fr.coppernic.tools.transparent.domain.sources.PhysicalSerialPortDataSource
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

const val RECEIVE_TIMEOUT = 100
const val RECEIVE_LENGTH = 100

class PhysicalSerialPortDataSourceImpl(
    val context: Context
) : PhysicalSerialPortDataSource {

    suspend fun createSerialCom(): SerialCom = suspendCoroutine { continuation ->
        SerialFactory.getDirectInstance(context, object : InstanceListener<SerialCom> {
            override fun onDisposed(serialCom: SerialCom) {
            }
            override fun onCreated(serialCom: SerialCom) {
                continuation.resume(serialCom)
            }
        })
    }

    override fun getSerialPortList(): List<SerialPortPhysical> {
        return when {
            OsHelper.isAccess() -> listOf(
                SerialPortPhysical("/dev/ttyHS0"),
                SerialPortPhysical("/dev/ttyMSM0"),
                SerialPortPhysical("/dev/ttyMSM1"),
            )
            OsHelper.isIdPlatformV2() -> listOf(
                SerialPortPhysical("/dev/ttyHS0"),
                SerialPortPhysical("/dev/ttyMSM0"),
            )
            else -> listOf<SerialPortPhysical>()
        }
    }

    override suspend fun openSerialPort(
        serialPort: SerialPortPhysical,
        baudrate: Int,
        rts: Boolean,
        xonXoff: Boolean,
        hardwareFlowControl: Boolean
    ): SerialPortComm {
        val serialCom = createSerialCom()
        serialCom?.apply {
            setRts(rts)
            setXonXoff(xonXoff)
            setHardwareFlowControl(hardwareFlowControl)
            open(serialPort.physicalReference, baudrate)
            flush()
        }
        return SerialPortComm(
            serialPort,
            serialCom
        )
    }

    override fun sendData(serialPortComm: SerialPortComm, data: ByteArray) {
        serialPortComm.serialCom?.send(data, data.size)
    }

    override fun receiveData(serialPortComm: SerialPortComm): ByteArray {
        val availableData = serialPortComm.serialCom?.queueStatus
        val response = availableData?.let { ByteArray(it) } ?: return byteArrayOf()
        serialPortComm.serialCom.receive(RECEIVE_TIMEOUT, availableData, response)
        return response
    }

    override fun closeSerialPort(serialPortComm: SerialPortComm) {
        serialPortComm.serialCom?.flush()
        serialPortComm.serialCom?.close()
    }
}
