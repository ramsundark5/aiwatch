package com.aiwatch.media;
import com.aiwatch.Logger;
import com.aiwatch.common.RTSPTimeOutOption;
import com.aiwatch.media.db.CameraConfig;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

public class VideoFrameExtractor {

    private static final Logger LOGGER = new Logger();
    private CameraConfig cameraConfig;
    private FFmpegFrameGrabber grabber;

    public VideoFrameExtractor(CameraConfig cameraConfig) {
        try {
            this.cameraConfig = cameraConfig;
        } catch (Exception e) {
            LOGGER.e(e.getMessage());
        }
    }

    public Frame grabFrame(){
        Frame frame = null;
        try{
            if (grabber == null) {
                initGrabber(cameraConfig); // connect
            }
            frame = grabber.grabImage();
        }catch(Exception e){
            LOGGER.e(e, "Exception grabbing frame");
        }
        return frame;
    }


    public void initGrabber(CameraConfig cameraConfig) throws Exception {
        int TIMEOUT = 10; //10 secs
        grabber = new FFmpegFrameGrabber(cameraConfig.getVideoUrl()); // rtsp url
        //rtsp_transport flag is important. Otherwise grabbed image will be distorted
        grabber.setOption("rtsp_transport", "tcp");
        //grabber.setVideoCodec(cameraConfig.getVideoCodec());
        grabber.setOption(
                RTSPTimeOutOption.STIMEOUT.getKey(),
                String.valueOf(TIMEOUT * 1000000)
        ); // In microseconds.
        grabber.setOption("hwaccel", "h264_videotoolbox");
        grabber.start();
        LOGGER.i("connected to camera "+cameraConfig.getId());
    }

    public void stopGrabber(){
        try {
            if(grabber != null){
                grabber.stop();
            }
        } catch (Exception e) {
            LOGGER.e(e, e.getMessage());
        }finally{
            grabber = null;
        }
    }
}
