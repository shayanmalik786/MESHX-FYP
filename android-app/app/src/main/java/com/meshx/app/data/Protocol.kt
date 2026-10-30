package com.meshx.app.data

import com.meshx.app.model.RoutingMode

/**
 * Text protocol between the phone and its MeshX node over BLE (Nordic UART Service).
 * One frame per line, fields separated by '|'. The free-text field is always last,
 * so a message may itself contain '|'. Newlines inside text are sent as "\n".
 * The same format is parsed by the ESP32 firmware (see docs/protocol.md).
 *
 * Phone -> node
 *   MSG|<msgId>|<toNode>|<text>
 *   MODE|<ADAPTIVE|SHORTEST_HOP|FLOODING>
 *   HELLO
 *
 * Node -> phone
 *   ME|<nodeId>|<battery%>
 *   RX|<msgId>|<fromNode>|<hops>|<rssi>|<snr>|<text>
 *   ACK|<msgId>|<hops>
 *   FAIL|<msgId>
 *   SENT|<msgId>
 *   NODE|<id>|<name>|<rssi>|<snr>|<battery>|<loss>|<score>|<hops>|<lastSeenSec>
 *   LINK|<a>|<b>|<score>
 */
sealed interface Frame {
    data class Me(val nodeId: Int, val battery: Int) : Frame
    data class Rx(val msgId: Long, val from: Int, val hops: Int, val rssi: Int, val snr: Float, val text: String) : Frame
    data class Ack(val msgId: Long, val hops: Int) : Frame
    data class Sent(val msgId: Long) : Frame
    data class Fail(val msgId: Long) : Frame
    data class Node(
        val id: Int, val name: String, val rssi: Int, val snr: Float, val battery: Int,
        val loss: Float, val score: Float, val hops: Int, val lastSeenSec: Int,
    ) : Frame
    data class Link(val a: Int, val b: Int, val score: Float) : Frame
}

object Protocol {
    const val MAX_TEXT = 200

    fun encodeMessage(msgId: Long, to: Int, text: String): String =
        "MSG|$msgId|$to|${escape(text.take(MAX_TEXT))}\n"

    fun encodeMode(mode: RoutingMode): String = "MODE|${mode.name}\n"

    fun encodeHello(): String = "HELLO\n"

    /** Parses one line (without the trailing newline). Returns null for unknown or broken frames. */
    fun decode(line: String): Frame? {
        val type = line.substringBefore('|')
        return try {
            when (type) {
                "ME" -> line.fields(3).let { Frame.Me(it[1].toInt(), it[2].toInt()) }
                "RX" -> line.fields(7).let {
                    Frame.Rx(it[1].toLong(), it[2].toInt(), it[3].toInt(), it[4].toInt(), it[5].toFloat(), unescape(it[6]))
                }
                "ACK" -> line.fields(3).let { Frame.Ack(it[1].toLong(), it[2].toInt()) }
                "SENT" -> line.fields(2).let { Frame.Sent(it[1].toLong()) }
                "FAIL" -> line.fields(2).let { Frame.Fail(it[1].toLong()) }
                "NODE" -> line.fields(10).let {
                    Frame.Node(
                        it[1].toInt(), it[2], it[3].toInt(), it[4].toFloat(), it[5].toInt(),
                        it[6].toFloat(), it[7].toFloat(), it[8].toInt(), it[9].toInt(),
                    )
                }
                "LINK" -> line.fields(4).let { Frame.Link(it[1].toInt(), it[2].toInt(), it[3].toFloat()) }
                else -> null
            }
        } catch (e: RuntimeException) {
            null
        }
    }

    private fun String.fields(count: Int): List<String> {
        val parts = split('|', limit = count)
        require(parts.size == count) { "expected $count fields" }
        return parts
    }

    fun escape(text: String) = text.replace("\\", "\\\\").replace("\n", "\\n")

    fun unescape(text: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '\\' && i + 1 < text.length) {
                out.append(if (text[i + 1] == 'n') '\n' else text[i + 1])
                i += 2
            } else {
                out.append(c)
                i++
            }
        }
        return out.toString()
    }
}

/** Collects BLE notification chunks and hands back complete lines. */
class LineBuffer {
    private val buf = StringBuilder()

    fun append(chunk: String): List<String> {
        buf.append(chunk)
        val lines = mutableListOf<String>()
        while (true) {
            val nl = buf.indexOf("\n")
            if (nl < 0) break
            lines += buf.substring(0, nl).trimEnd('\r')
            buf.delete(0, nl + 1)
        }
        return lines
    }
}
