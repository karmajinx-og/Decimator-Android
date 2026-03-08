package com.decimator.android.protocol

/**
 * Decimator control protocol: pin encoding and command/response conversion.
 * Faithful port from quentinmit/decimctl (protocol.py) — Apache 2.0.
 *
 * Decimator uses FTDI in synchronous bit-bang mode. Each byte written is the
 * state of 8 pins; bit 6 (0x40) = clock, bit 3 (0x08) = data to/from FPGA.
 */
object DecimatorProtocol {
    /** Total register space: 512 bytes (0x200). */
    const val REGISTER_SIZE = 0x200

    /** Preamble for read commands (from protocol.py). */
    val READ_PREAMBLE: ByteArray = byteArrayOf(
        0x00, 0x40, 0x00, 0x40, 0x48, 0x48, 0x40, 0x00
    )

    /** Preamble for write commands. */
    val WRITE_PREAMBLE: ByteArray = byteArrayOf(
        0x00, 0x40, 0x00, 0x40, 0x48, 0x48, 0x40, 0x00
    )

    /** Postamble for write commands. */
    val WRITE_POSTAMBLE: ByteArray = byteArrayOf(
        0x00, 0x40, 0x48
    )

    /**
     * Encode command bytes into raw pin sequences for the FTDI.
     * Three cycles per bit (not the inverse of rawResponseToBytes).
     */
    fun bytesToRawCommand(command: ByteArray): ByteArray {
        val out = ArrayList<Byte>(command.size * 8 * 3)
        for (c in command) {
            for (i in 7 downTo 0) {
                val bitSet = ((c.toInt() and 0xFF) and (1 shl i)) != 0
                if (bitSet) {
                    out.add(0x08)
                    out.add(0x48)
                    out.add(0x08)
                } else {
                    out.add(0x00)
                    out.add(0x40)
                    out.add(0x00)
                }
            }
        }
        return out.toByteArray()
    }

    /**
     * Decode raw FTDI read response into bytes.
     * Each bit is represented by four bytes; we use byte index 2 and bit 3.
     */
    fun rawResponseToBytes(raw: ByteArray): ByteArray {
        if (raw.size % 4 != 0) {
            throw IllegalArgumentException("Raw response length must be multiple of 4, got ${raw.size}")
        }
        val statusBits = ArrayList<Boolean>(raw.size / 4)
        var i = 0
        while (i < raw.size) {
            if (i + 3 < raw.size && raw[i + 2] != raw[i + 3]) {
                // Python logs "difference at bit"; treat as protocol error
                throw ProtocolException("Bit phase mismatch at bit ${statusBits.size}")
            }
            statusBits.add((raw[i + 2].toInt() and 0xFF and 0x08) != 0)
            i += 4
        }
        return bitListToBytes(statusBits)
    }

    /** Convert a sequence of bits (MSB first) into bytes. */
    fun bitListToBytes(bits: List<Boolean>): ByteArray {
        val byteCount = (bits.size + 7) / 8
        val out = ByteArray(byteCount)
        for (byteIndex in 0 until byteCount) {
            var value = 0
            for (bitIndex in 0 until 8) {
                val idx = byteIndex * 8 + bitIndex
                if (idx < bits.size && bits[idx]) {
                    value = (value shl 1) or 1
                } else {
                    value = value shl 1
                }
            }
            out[byteIndex] = value.toByte()
        }
        return out
    }
}

class ProtocolException(message: String) : Exception(message)
