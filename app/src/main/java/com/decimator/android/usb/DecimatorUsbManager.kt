package com.decimator.android.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

/**
 * Decimator devices use FTDI chip: VID 0x215F, PID 0x6000.
 * See Decimator_Design_Android_Control_App.md and quentinmit/decimctl.
 */
object DecimatorUsbConstants {
    const val VENDOR_ID = 0x215F
    const val PRODUCT_ID = 0x6000
}

/**
 * Helper to detect and manage Decimator USB devices.
 * Next step: integrate FTDI Android D2XX library for bit-bang mode (not standard USB serial).
 */
fun UsbManager.findDecimatorDevice(): UsbDevice? =
    deviceList.values.find { device ->
        device.vendorId == DecimatorUsbConstants.VENDOR_ID &&
            device.productId == DecimatorUsbConstants.PRODUCT_ID
    }
