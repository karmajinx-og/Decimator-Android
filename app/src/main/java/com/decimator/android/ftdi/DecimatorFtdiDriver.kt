package com.decimator.android.ftdi

import android.content.Context
import android.hardware.usb.UsbDevice
import com.decimator.android.protocol.DecimatorProtocol
import com.ftdi.j2xx.D2xxManager
import com.ftdi.j2xx.FT_Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * FTDI D2XX-based driver for Decimator devices.
 *
 * - All USB I/O runs on [Dispatchers.IO] (never on main thread).
 * - Init sequence matches decimctl: baud 3,000,000, latency 1 ms, RTS/CTS, bit-bang mode 0x48.
 * - [open] returns a [DecimatorConnection] or [DecimatorError].
 */
object DecimatorFtdiDriver {

    private const val DECIMATOR_BAUD_RATE = 3_000_000
    private const val LATENCY_MS = 1
    private const val CHUNK_SIZE = 256

    /** Open a Decimator device. Must be called from a coroutine; runs on IO dispatcher. */
    suspend fun open(context: Context, usbDevice: UsbDevice): Result<DecimatorConnection> =
        withContext(Dispatchers.IO) {
            try {
                val manager = D2xxManager.getInstance(context)
                manager.setVIDPID(DecimatorUsbConstants.VENDOR_ID, DecimatorUsbConstants.PRODUCT_ID)
                if (!manager.isFtDevice(usbDevice)) {
                    return@withContext Result.failure(DecimatorError.UnsupportedDevice(usbDevice))
                }
                val params = D2xxManager.DriverParameters().apply {
                    readTimeout = 5000
                    writeTimeout = 5000
                }
                val ftDevice = manager.openByUsbDevice(context, usbDevice, params)
                    ?: return@withContext Result.failure(DecimatorError.OpenFailed("openByUsbDevice returned null"))
                if (!ftDevice.isOpen) {
                    return@withContext Result.failure(DecimatorError.OpenFailed("device not open after openByUsbDevice"))
                }
                initDecimatorBitBang(ftDevice)
                Result.success(DecimatorConnection(ftDevice))
            } catch (e: Exception) {
                Result.failure(
                    when {
                        e is D2xxManager.D2xxException -> DecimatorError.FtdiError(e.message ?: "D2xxException")
                        else -> DecimatorError.FtdiError(e.message ?: e.toString())
                    }
                )
            }
        }

    /**
     * Init sequence from decimctl Device.open():
     * Reset, baud 3M, USB params, setChars(0,0,0,0), latency 1ms, RTS/CTS,
     * setBitMode(0,0) then setBitMode(0, SYNC_BITBANG), clock 0x48, setBitMode(0x48, SYNC_BITBANG).
     */
    private fun initDecimatorBitBang(ft: FT_Device) {
        ft.resetDevice()
        check(ft.setBaudRate(DECIMATOR_BAUD_RATE)) { "setBaudRate($DECIMATOR_BAUD_RATE) failed" }
        ft.setFlowControl(D2xxManager.FT_FLOW_RTS_CTS, 0, 0)
        ft.setChars(0, 0, 0, 0)
        check(ft.setLatencyTimer(LATENCY_MS.toByte())) { "setLatencyTimer($LATENCY_MS) failed" }
        ft.setBitMode(0, D2xxManager.FT_BITMODE_RESET)
        ft.setBitMode(0, D2xxManager.FT_BITMODE_SYNC_BITBANG)
        clockRawBytes(ft, byteArrayOf(0x48))
        check(ft.setBitMode(0x48, D2xxManager.FT_BITMODE_SYNC_BITBANG)) {
            "setBitMode(0x48, SYNC_BITBANG) failed"
        }
    }

    /** Raw clock in/out; used by [DecimatorConnection]. All I/O on caller's thread (use from IO dispatcher). */
    internal fun clockRawBytes(ft: FT_Device, dataIn: ByteArray): ByteArray {
        val out = ByteArray(dataIn.size)
        var outOffset = 0
        var inOffset = 0
        while (inOffset < dataIn.size) {
            val chunk = (dataIn.size - inOffset).coerceAtMost(CHUNK_SIZE)
            val toSend = dataIn.copyOfRange(inOffset, inOffset + chunk)
            val written = ft.write(toSend, chunk)
            if (written < 0) throw DecimatorError.IOError("write failed: $written")
            val chunkBuf = ByteArray(chunk)
            val read = ft.read(chunkBuf, chunk)
            if (read < 0) throw DecimatorError.IOError("read failed: $read")
            chunkBuf.copyInto(out, outOffset, 0, read)
            inOffset += chunk
            outOffset += read
        }
        return out
    }
}

/**
 * Open connection to a Decimator device. All methods must be called from a background thread
 * (use from a coroutine on Dispatchers.IO).
 */
class DecimatorConnection(private val ft: FT_Device) {

    fun isOpen(): Boolean = ft.isOpen

    /** Clock raw bytes in/out; returns response bytes. */
    suspend fun clockRawBytes(dataIn: ByteArray): ByteArray = kotlinx.coroutines.withContext(Dispatchers.IO) {
        if (!ft.isOpen) throw DecimatorError.DeviceDisconnected
        DecimatorFtdiDriver.clockRawBytes(ft, dataIn)
    }

    /**
     * Write to FPGA register. Port of decimctl fpga_write_bytes.
     * address = (register shl 1) and 0xfffe; then send preamble, address (big-endian short), value, postamble.
     */
    suspend fun fpgaWriteBytes(register: Int, value: ByteArray): ByteArray =
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            if (!ft.isOpen) throw DecimatorError.DeviceDisconnected
            val address = (register shl 1) and 0xFFFE
            val addressBytes = byteArrayOf(
                (address shr 8).toByte(),
                (address and 0xFF).toByte()
            )
            val rawAddress = DecimatorProtocol.bytesToRawCommand(addressBytes)
            val rawValue = DecimatorProtocol.bytesToRawCommand(value)
            val buf = DecimatorProtocol.WRITE_PREAMBLE + rawAddress + rawValue + DecimatorProtocol.WRITE_POSTAMBLE
            DecimatorFtdiDriver.clockRawBytes(ft, buf)
        }

    /**
     * Read from FPGA. Port of decimctl fpga_read_bytes.
     * Send read preamble + address (start shl 1 | 1), purge, setBitMode(0x40), clock 4 bytes per bit, then restore 0x48.
     */
    suspend fun fpgaReadBytes(start: Int, length: Int): ByteArray =
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            if (!ft.isOpen) throw DecimatorError.DeviceDisconnected
            val address = (start shl 1) or 1
            val addressBytes = byteArrayOf(
                (address shr 8).toByte(),
                (address and 0xFF).toByte()
            )
            val buf = DecimatorProtocol.READ_PREAMBLE + DecimatorProtocol.bytesToRawCommand(addressBytes)
            DecimatorFtdiDriver.clockRawBytes(ft, buf)
            ft.purge((D2xxManager.FT_PURGE_RX.toInt() or D2xxManager.FT_PURGE_TX.toInt()).toByte())
            ft.setBitMode(0x40, D2xxManager.FT_BITMODE_SYNC_BITBANG)
            val bitCycles = length * 8
            val readClock = ByteArray(bitCycles * 4)
            var idx = 0
            val pattern = byteArrayOf(0x00, 0x40, 0x40, 0x00)
            repeat(bitCycles) {
                pattern.copyInto(readClock, idx)
                idx += 4
            }
            val raw = DecimatorFtdiDriver.clockRawBytes(ft, readClock)
            DecimatorFtdiDriver.clockRawBytes(ft, byteArrayOf(0))
            ft.setBitMode(0x48, D2xxManager.FT_BITMODE_SYNC_BITBANG)
            DecimatorFtdiDriver.clockRawBytes(ft, byteArrayOf(0x00, 0x40, 0x48))
            DecimatorProtocol.rawResponseToBytes(raw)
        }

    /** Read full 0x200 register block. */
    suspend fun readRawRegisters(): ByteArray = fpgaReadBytes(0, DecimatorProtocol.REGISTER_SIZE)

    fun close() {
        try {
            ft.setBitMode(0, D2xxManager.FT_BITMODE_RESET)
            ft.close()
        } catch (_: Exception) { }
    }
}

sealed class DecimatorError : Exception() {
    override val message: String get() = when (this) {
        is UnsupportedDevice -> "Not a supported Decimator/FTDI device: ${device.deviceName}"
        is OpenFailed -> reason
        is FtdiError -> msg ?: "FTDI error"
        is IOError -> msg ?: "I/O error"
        is DeviceDisconnected -> "Device disconnected"
    }
    data class UnsupportedDevice(val device: UsbDevice) : DecimatorError()
    data class OpenFailed(val reason: String) : DecimatorError()
    data class FtdiError(override val message: String?) : DecimatorError()
    data class IOError(override val message: String?) : DecimatorError()
    object DeviceDisconnected : DecimatorError()
}

private object DecimatorUsbConstants {
    const val VENDOR_ID = 0x215F
    const val PRODUCT_ID = 0x6000
}
