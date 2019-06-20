package com.aiwatch.media;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.aiwatch.common.RTSPTimeOutOption;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.aiwatch.postprocess.NotificationManager;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import java.io.ByteArrayOutputStream;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGBA;

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
        Trace frameGrabTrace = FirebasePerformance.getInstance().newTrace("frameGrabTrace");
        frameGrabTrace.start();
        try{
            long startTime = System.currentTimeMillis();
            frame = grabber.grabImage();
            long endTime = System.currentTimeMillis();
            long timeElapsed = endTime - startTime;
            if(frame.keyFrame){
                frameGrabTrace.putMetric("keyFrameGrabTime", timeElapsed);
                LOGGER.d("KeyFrame grab time for camera "+cameraConfig.getId() + ":  "+timeElapsed);
            }else{
                frameGrabTrace.putMetric("nonKeyFrameGrabTime", timeElapsed);
                LOGGER.d("NokeyFrame grab time for camera "+cameraConfig.getId() + ":  "+timeElapsed);
            }
        }catch(Exception e){
            LOGGER.e(e, "Exception grabbing frame");
        }
        frameGrabTrace.stop();
        return frame;
    }

    public String getImageFromCamera(){
        String base64image = null;
        AndroidFrameConverter frameConverter = new AndroidFrameConverter();
        try{
            Frame frame = grabFrame();
            if(frame != null){
                Bitmap bitmap = frameConverter.convert(frame);
                 base64image = imageToBase64(bitmap);
            }
        }catch(Exception e){
            LOGGER.e(e, "error getting test image");
        }finally{
            stopGrabber();
        }
        return base64image;
    }

    private String imageToBase64(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] imageBytes = baos.toByteArray();

        String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        return base64String;
    }

    public synchronized void initGrabber(CameraConfig cameraConfig) {
        int TIMEOUT = 10; //10 secs
        try{
            grabber = new FFmpegFrameGrabber(cameraConfig.getVideoUrlWithAuth()); // rtsp url
            //rtsp_transport flag is important. Otherwise grabbed image will be distorted
            grabber.setOption("rtsp_transport", "tcp");
            grabber.setImageHeight(AppConstants.TF_OD_API_INPUT_SIZE);
            grabber.setImageWidth(AppConstants.TF_OD_API_INPUT_SIZE);
            grabber.setOption(
                    RTSPTimeOutOption.STIMEOUT.getKey(),
                    String.valueOf(TIMEOUT * 1000000)
            ); // In microseconds.
            grabber.setPixelFormat(AV_PIX_FMT_RGBA);
            grabber.setVideoOption("threads", "0");
            grabber.setAudioOption("threads", "0");
            //grabber.setOption("hwaccel", "auto");
            grabber.start();
            notifyAndUpdateCameraStatus(false);
            LOGGER.i("connected to camera "+cameraConfig.getId());
        }catch(Exception e){
            LOGGER.e(e, "Error connecting to camera " + cameraConfig.getId());
            notifyAndUpdateCameraStatus(true);
        }
    }

    public synchronized void stopGrabber(){
        try {
            if(grabber != null){
                grabber.stop();
            }
            //notifyAndUpdateCameraStatus(true);
        } catch (Exception e) {
            LOGGER.e(e, "Error stopping grabber");
        }finally{
            grabber = null;
        }
    }

    public void notifyAndUpdateCameraStatus(boolean disconnected){
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
