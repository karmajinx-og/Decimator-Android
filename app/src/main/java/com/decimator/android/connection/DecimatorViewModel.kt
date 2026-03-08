package com.decimator.android.connection

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decimator.android.ftdi.DecimatorError
import com.decimator.android.ftdi.DecimatorFtdiDriver
import com.decimator.android.usb.findDecimatorDevice
import com.decimator.android.usb.UsbPermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Holds connection state and performs open/close on a background thread (viewModelScope + IO).
 */
class DecimatorViewModel : ViewModel() {

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.NoDevice)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    /**
     * Call when the activity has UsbManager and context (e.g. onResume).
     * If a Decimator device is attached, updates state to PendingPermission, PermissionDenied, or triggers open.
     */
    fun refreshDeviceState(context: Context) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = usbManager.findDecimatorDevice()
        when {
            device == null -> _state.value = ConnectionState.NoDevice
            !UsbPermissionHelper.hasPermission(usbManager, device) -> _state.value = ConnectionState.PendingPermission(device)
            else -> openDevice(context, device)
        }
    }

    /**
     * Call when user should be prompted for USB permission (e.g. tap "Grant permission").
     */
    fun requestUsbPermission(context: Context, device: UsbDevice) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        UsbPermissionHelper.requestPermission(context, usbManager, device)
        _state.value = ConnectionState.PendingPermission(device)
    }

    /**
     * Call when we know permission was granted (e.g. from BroadcastReceiver).
     * Opens the device on IO dispatcher.
     */
    fun onPermissionGranted(context: Context, device: UsbDevice) {
        openDevice(context, device)
    }

    /**
     * Call when permission was denied.
     */
    fun onPermissionDenied(device: UsbDevice) {
        _state.value = ConnectionState.PermissionDenied(device)
    }

    private fun openDevice(context: Context, device: UsbDevice) {
        _state.value = ConnectionState.Connecting
        viewModelScope.launch {
            DecimatorFtdiDriver.open(context, device)
                .onSuccess { connection ->
                    _state.value = ConnectionState.Connected(connection, device)
                }
                .onFailure { err ->
                    _state.value = when (err) {
                        is DecimatorError -> ConnectionState.Error(err)
                        else -> ConnectionState.Error(DecimatorError.FtdiError(err?.message))
                    }
                }
        }
    }

    fun disconnect() {
        val current = _state.value
        if (current is ConnectionState.Connected) {
            current.connection.close()
            _state.value = ConnectionState.NoDevice
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    /**
     * Call when device was disconnected (e.g. USB unplugged). Clear connection if we were connected.
     */
    fun onDeviceDetached(device: UsbDevice?) {
        val current = _state.value
        if (current is ConnectionState.Connected && current.device == device) {
            current.connection.close()
            _state.value = ConnectionState.NoDevice
        } else if (device == null && current is ConnectionState.Connected) {
            current.connection.close()
            _state.value = ConnectionState.NoDevice
        }
    }
}
