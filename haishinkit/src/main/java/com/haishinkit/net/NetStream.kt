package com.haishinkit.net

import com.haishinkit.codec.AudioCodec
import com.haishinkit.codec.RecordSetting
import com.haishinkit.codec.VideoCodec
import com.haishinkit.media.AudioSource
import com.haishinkit.media.VideoSource
import com.haishinkit.view.NetStreamView
import java.nio.ByteBuffer

/**
 * The `NetStream` class is the foundation of a RTMPStream.
 */
abstract class NetStream {
    interface Listener {
        /**
         * Tells the receiver to setUp.
         */
        fun onSetUp(stream: NetStream)

        /**
         * Tells the receiver to tearDown.
         */
        fun onTearDown(stream: NetStream)

        /**
         * Tells the receiver to captureOutput.
         */
        fun onCaptureOutput(stream: NetStream, type: Byte, buffer: ByteBuffer, timestamp: Long)
    }

    val videoSetting: VideoCodec.Setting by lazy {
        VideoCodec.Setting(videoCodec)
    }

    val audioSetting: AudioCodec.Setting by lazy {
        AudioCodec.Setting(audioCodec)
    }

    val recordSetting: RecordSetting by lazy {
        RecordSetting()
    }

    internal val audioCodec = AudioCodec()
    internal val videoCodec = VideoCodec()
    internal var renderer: NetStreamView? = null
    internal var audio: AudioSource? = null
    internal var video: VideoSource? = null

    /**
     * Attaches an audio stream to a NetStream.
     */
    open fun attachAudio(audio: AudioSource?) {
        if (audio == null) {
            this.audio?.tearDown()
            this.audio = null
            return
        }
        this.audio = audio
        this.audio?.stream = this
        this.audio?.setUp()
    }

    /**
     * Attaches a video stream to a NetStream.
     */
    open fun attachVideo(video: VideoSource?) {
        if (video == null) {
            this.video?.tearDown()
            this.video = null
            return
        }
        this.video = video
        this.video?.stream = this
        this.video?.setUp()
    }

    /**
     * Closes the stream from the server.
     */
    abstract fun close()
}
