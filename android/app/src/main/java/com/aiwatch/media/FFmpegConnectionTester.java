package com.aiwatch.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.aiwatch.Logger;
import com.aiwatch.models.CameraConfig;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class FFmpegConnectionTester {

    private static final Logger LOGGER = new Logger();

    public String getImageFromCamera(Context context, CameraConfig cameraConfig){
        String base64image = null;
        File outputFile = null;
        try{
            String fileName = "test" + System.currentTimeMillis()+".jpg";
            outputFile = new File(context.getFilesDir(), fileName);
            String frameExtractCommand =  " -s 300x300 -vframes 1 "+outputFile;
            String videoUrl = cameraConfig.getVideoUrlWithAuth();
            String rtspPrefix = "-rtsp_transport tcp ";
            if(videoUrl != null && !videoUrl.startsWith("rtsp")){
                rtspPrefix = "";
            }
            String command = rtspPrefix + "-i " + videoUrl + frameExtractCommand;
            //String[] ffmpegCommand = command.split("\\s+");
            int ffmpegResponse = FFmpeg.execute(command);

            if (ffmpegResponse == RETURN_CODE_SUCCESS) {
                Bitmap bitmapOutput = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
                base64image = imageToBase64(bitmapOutput);
            } else if (ffmpegResponse == RETURN_CODE_CANCEL) {
                LOGGER.i("Tester Command execution cancelled by user.");
            } else {
                LOGGER.i("ffmpeg testing failed with response" + ffmpegResponse);
            }

            Bitmap bitmapOutput = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
            base64image = imageToBase64(bitmapOutput);
        }catch(Exception e){
            LOGGER.e(e, "Error getting image from camera "+cameraConfig.getId());
        }finally {
            if(outputFile != null && outputFile.exists()){
                outputFile.delete();
            }
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
}
