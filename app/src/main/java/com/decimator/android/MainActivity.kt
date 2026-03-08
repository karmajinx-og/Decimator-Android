package com.decimator.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.activity.viewModels
import com.decimator.android.connection.DecimatorViewModel
import com.decimator.android.ui.DecimatorApp
import com.decimator.android.ui.theme.DecimatorAndroidTheme
import com.decimator.android.usb.UsbPermissionHelper

class MainActivity : ComponentActivity() {

    private val viewModel: DecimatorViewModel by viewModels()

    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!UsbPermissionHelper.isPermissionIntent(intent?.action)) return
            val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, android.hardware.usb.UsbDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            }
            val granted = intent?.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) == true
            val vm = viewModel ?: return
            if (device != null) {
                if (granted) vm.onPermissionGranted(this@MainActivity, device)
                else vm.onPermissionDenied(device)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DecimatorAndroidTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    DecimatorApp(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Context.RECEIVER_NOT_EXPORTED
        } else {
            0
        }
        registerReceiver(usbPermissionReceiver, IntentFilter(UsbPermissionHelper.ACTION_USB_PERMISSION), flags)
        viewModel.refreshDeviceState(this)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(usbPermissionReceiver)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.refreshDeviceState(this)
    }

}
