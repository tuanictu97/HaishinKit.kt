package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RtmpChunk
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpStream
import java.nio.ByteBuffer

/**
 * 7.1.5. Video Message (9)
 */
internal class RtmpUserControlMessage : RtmpMessage(TYPE_USER) {
    enum class Event(val rawValue: Short) {
        STREAM_BEGIN(0x00),
        STREAM_EOF(0x01),
        STREAM_DRY(0x02),
        SET_BUFFER(0x03),
        RECORDED(0x04),
        PING(0x06),
        PONG(0x07),
        BUFFER_EMPTY(0x1F),
        BUFFER_FULL(0x20),
        UNKNOWN(Short.MAX_VALUE);
    }

    var event: Event = Event.UNKNOWN
        private set
    var value = 0
        private set
    override var length: Int = CAPACITY

    override fun encode(buffer: ByteBuffer): RtmpMessage {
        buffer.putShort(event.rawValue)
        buffer.putInt(value)
        return this
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        val e = buffer.short
        event = Event.values().first { n -> n.rawValue == e }
        value = buffer.int
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        when (event) {
            Event.PING -> {
                val message = connection.messageFactory.createRtmpUserControlMessage()
                message.event = Event.PONG
                message.chunkStreamID = RtmpChunk.CONTROL
                connection.doOutput(RtmpChunk.ZERO, message)
            }
            Event.BUFFER_FULL,
            Event.BUFFER_EMPTY -> {
                val stream = connection.streams[value]
                if (stream != null) {
                    val data = if (event == Event.BUFFER_FULL)
                        RtmpStream.Code.BUFFER_FLUSH.data("")
                    else
                        RtmpStream.Code.BUFFER_EMPTY.data("")
                    stream.dispatchEventWith(com.haishinkit.event.Event.RTMP_STATUS, false, data)
                }
            }
            else -> {
            }
        }
        return this
    }

    companion object {
        private const val CAPACITY = 6
    }
}
