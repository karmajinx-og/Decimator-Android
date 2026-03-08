package com.decimator.android.usb

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build

/**
 * Request runtime permission for a Decimator USB device.
 * Call [requestPermission] then handle [Intent] in onNewIntent/onActivityResult with [hasPermission].
 */
object UsbPermissionHelper {

    const val ACTION_USB_PERMISSION = "com.decimator.android.USB_PERMISSION"

    fun hasPermission(usbManager: UsbManager, device: UsbDevice?): Boolean {
        if (device == null) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            usbManager.hasPermission(device)
        } else {
            @Suppress("DEPRECATION")
            usbManager.hasPermission(device)
        }
    }

    /**
     * Request permission for [device]. Result is delivered via the [PendingIntent].
     * Use [createPermissionIntent] to build the intent that will be sent when user grants/denies.
     */
    fun requestPermission(context: Context, usbManager: UsbManager, device: UsbDevice) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        val pi = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            flags
        )
        usbManager.requestPermission(device, pi)
    }

    fun isPermissionIntent(action: String?): Boolean = action == ACTION_USB_PERMISSION
}
