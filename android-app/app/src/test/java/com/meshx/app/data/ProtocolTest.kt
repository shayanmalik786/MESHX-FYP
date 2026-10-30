package com.meshx.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProtocolTest {

    @Test
    fun encodesMessageWithEscapedNewline() {
        assertEquals("MSG|7|2|hi\\nthere\n", Protocol.encodeMessage(7, 2, "hi\nthere"))
    }

    @Test
    fun decodesReceivedTextContainingPipes() {
        val f = Protocol.decode("RX|42|2|3|-92|7.5|a|b\\nc")
        assertEquals(Frame.Rx(42, 2, 3, -92, 7.5f, "a|b\nc"), f)
    }

    @Test
    fun decodesNodeAndLink() {
        assertEquals(
            Frame.Node(2, "Fatima", -92, 7.5f, 64, 0.03f, 0.86f, 2, 5),
            Protocol.decode("NODE|2|Fatima|-92|7.5|64|0.03|0.86|2|5"),
        )
        assertEquals(Frame.Link(3, 2, 0.86f), Protocol.decode("LINK|3|2|0.86"))
    }

    @Test
    fun rejectsBrokenFrames() {
        assertNull(Protocol.decode("ACK|notanumber|2"))
        assertNull(Protocol.decode("NODE|1|short"))
        assertNull(Protocol.decode("WHAT|1"))
    }

    @Test
    fun lineBufferJoinsChunks() {
        val b = LineBuffer()
        assertEquals(emptyList<String>(), b.append("ACK|1"))
        assertEquals(listOf("ACK|1|2", "SENT|3"), b.append("|2\r\nSENT|3\nME|"))
        assertEquals(listOf("ME|3|80"), b.append("3|80\n"))
    }
}
