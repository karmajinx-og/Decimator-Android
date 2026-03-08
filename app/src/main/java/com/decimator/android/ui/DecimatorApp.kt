package com.decimator.android.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.decimator.android.connection.ConnectionState
import com.decimator.android.connection.DecimatorViewModel

@Composable
fun DecimatorApp(viewModel: DecimatorViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Deci-Droid",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        when (val s = state) {
            is ConnectionState.NoDevice -> {
                Text(
                    text = "Connect a Decimator device via USB OTG",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.refreshDeviceState(context)
                        Toast.makeText(context, "Checking for device…", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Check for device")
                }
            }
            is ConnectionState.PendingPermission -> {
                Text(
                    text = "Device found: ${s.device.productName ?: s.device.deviceName}. Grant USB permission.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.requestUsbPermission(context, s.device) }) {
                    Text("Grant USB permission")
                }
            }
            is ConnectionState.PermissionDenied -> Text(
                text = "USB permission denied for ${s.device.deviceName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            is ConnectionState.UnsupportedDevice -> Text(
                text = "Unsupported device: ${s.device.deviceName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            is ConnectionState.Connecting -> Text(
                text = "Connecting…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            is ConnectionState.Connected -> {
                Text(
                    text = "Connected: ${s.device.productName ?: s.device.deviceName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.disconnect() }) {
                    Text("Disconnect")
                }
            }
            is ConnectionState.Error -> Text(
                text = "Error: ${s.error.message}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
