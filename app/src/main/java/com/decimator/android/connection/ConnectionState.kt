package com.decimator.android.connection

import android.hardware.usb.UsbDevice
import com.decimator.android.ftdi.DecimatorConnection
import com.decimator.android.ftdi.DecimatorError

/**
 * UI-facing connection state. All [DecimatorConnection] operations run off the main thread.
 */
sealed class ConnectionState {
    data object NoDevice : ConnectionState()
    data class PendingPermission(val device: UsbDevice) : ConnectionState()
    data class PermissionDenied(val device: UsbDevice) : ConnectionState()
    data class UnsupportedDevice(val device: UsbDevice) : ConnectionState()
    data object Connecting : ConnectionState()
    data class Connected(val connection: DecimatorConnection, val device: UsbDevice) : ConnectionState()
    data class Error(val error: DecimatorError) : ConnectionState()
}
