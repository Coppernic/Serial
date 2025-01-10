package fr.coppernic.tools.transparent.core.helper


import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

/**
 * action to identify our requests
 */
const val ACTION = "fr.coppernic.tools.transparent.USB_PERMISSION"

object UsbPermission {

    // buffer size of one so the first send() queues instead of blocks
    private val queue = Channel<CompletableDeferred<Boolean>>(1)

    /**
     * request permission for this device from the [UsbManager]
     */
    private fun sendPermissionRequest(context: Context, device: UsbDevice) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager?

        usbManager?.requestPermission(
            device,
            PendingIntent.getBroadcast(context, 0, Intent(ACTION), 0)
        )
    }

    /**
     * receive responses to permission requests
     */
    private val receiver = object : BroadcastReceiver() {
        var isRegistered = false
            private set

        fun register(context: Context): Intent? = synchronized(this) {
            return try {
                if (!isRegistered)
                    context.registerReceiver(this, IntentFilter(ACTION))
                else
                    null
            } finally {
                isRegistered = true
            }

        }

        fun unregister(context: Context) = synchronized(this) {
            if (isRegistered)
                try {
                    context.unregisterReceiver(this)
                } finally {
                    isRegistered = false
                }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                runBlocking {
                    // the queue should never be empty at this point...
                    val result = queue.tryReceive().getOrNull() ?: return@runBlocking

                    val usbManager = context?.getSystemService(Context.USB_SERVICE) as UsbManager?

                    // send result
                    result.complete(device != null && usbManager!!.hasPermission(device))
                }
            }
        }
    }

    /**
     * register to receive responses to permission requests
     */
    fun registerForUsbPermissions(context: Context) = receiver.register(context)

    /**
     * unregister to no longer receive responses to permission requests
     */
    fun unregisterForUsbPermissions(context: Context) = receiver.unregister(context)

    /**
     * request permission for a device.  returns true if permission is granted
     */
    suspend fun requestPermission(context: Context, device: UsbDevice, autoRegister: Boolean = true) {
        val usbManager = (context.getSystemService(Context.USB_SERVICE) as UsbManager?) ?: return
        when {
            usbManager.hasPermission(device) -> true
            !receiver.isRegistered && !autoRegister -> false // throw if not registered?
            else -> {
                val result = CompletableDeferred<Boolean>()

                // post a request in the queue so we receive the result.  this will suspend if there's
                // already a request in the queue (keeps the order correct)
                queue.send(result)

                // auto-register if not already registered
                if (!receiver.isRegistered) registerForUsbPermissions(context)

                sendPermissionRequest(context, device)

                // wait for result
                result.await()
            }
        }
    }


}