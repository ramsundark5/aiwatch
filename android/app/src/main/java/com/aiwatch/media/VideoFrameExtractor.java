package com.aiwatch.media;
import android.content.Context;

import com.aiwatch.Logger;
import com.aiwatch.common.RTSPTimeOutOption;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.aiwatch.postprocess.NotificationManager;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

public class VideoFrameExtractor {

    private static final Logger LOGGER = new Logger();
    private CameraConfig cameraConfig;
    private FFmpegFrameGrabber grabber;
    private Context context;

    public VideoFrameExtractor(CameraConfig cameraConfig, Context context) {
        try {
            this.cameraConfig = cameraConfig;
            this.context = context;
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


    public void initGrabber(CameraConfig cameraConfig) {
        int TIMEOUT = 10; //10 secs
        try{
            grabber = new FFmpegFrameGrabber(cameraConfig.getVideoUrl()); // rtsp url
            //rtsp_transport flag is important. Otherwise grabbed image will be distorted
            grabber.setOption("rtsp_transport", "tcp");
            //grabber.setVideoCodec(cameraConfig.getVideoCodec());
            grabber.setOption(
                    RTSPTimeOutOption.STIMEOUT.getKey(),
                    String.valueOf(TIMEOUT * 1000000)
            ); // In microseconds.
            //grabber.setOption("hwaccel", "h264_videotoolbox");
            grabber.start();
            notifyAndUpdateCameraStatus(false);
            LOGGER.i("connected to camera "+cameraConfig.getId());
        }catch(Exception e){
            LOGGER.e(e, "Error connecting to camera " + cameraConfig.getId());
            notifyAndUpdateCameraStatus(true);
        }
    }

    public void stopGrabber(){
        try {
            if(grabber != null){
                grabber.stop();
            }
            notifyAndUpdateCameraStatus(true);
        } catch (Exception e) {
            LOGGER.e(e, e.getMessage());
        }finally{
            grabber = null;
        }
    }

    private void notifyAndUpdateCameraStatus(boolean disconnected){
        try{
            if(cameraConfig.isDisconnected() == disconnected){
                //nothing new to notify
                return;
            }
            String status = disconnected ? "disconnected" : "connected";
            cameraConfig.setDisconnected(disconnected);
            NotificationManager.sendStringNotification(context, "Camera "+ cameraConfig.getName() + " "+ status);
            NotificationManager.sendUINotification(context, cameraConfig);
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            cameraConfigDao.updateCameraStatus(cameraConfig.getId(), disconnected);
        }catch (Exception e){
            LOGGER.e(e, "error notifying camera status ");
        }
    }

}
