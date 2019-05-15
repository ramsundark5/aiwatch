package com.aiwatch.common;

/**
 * There is no universal option for streaming timeout. Each of protocols has
 * its own list of options.
 */
public enum RTSPTimeOutOption {
    /**
     * Depends on protocol (FTP, HTTP, RTMP, SMB, SSH, TCP, UDP, or UNIX).
     *
     * http://ffmpeg.org/ffmpeg-all.html
     */
    TIMEOUT,
    /**
     * Protocols
     *
     * Maximum time to wait for (network) read/write operations to complete,
     * in microseconds.
     *
     * http://ffmpeg.org/ffmpeg-all.html#Protocols
     */
    RW_TIMEOUT,
    /**
     * Protocols -> RTSP
     *
     * Set socket TCP I/O timeout in microseconds.
     *
     * http://ffmpeg.org/ffmpeg-all.html#rtsp
     */
    STIMEOUT;

    public String getKey() {
        return toString().toLowerCase();
    }

}
