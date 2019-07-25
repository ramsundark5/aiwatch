package com.aiwatch.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.aiwatch.Logger;
import com.aiwatch.media.db.CameraConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;

import nl.bravobit.ffmpeg.CustomFFmpeg;
import nl.bravobit.ffmpeg.CustomResponseHandler;

public class FFmpegConnectionTester {

    private static final Logger LOGGER = new Logger();

    public String getImageFromCamera(Context context, CameraConfig cameraConfig){
        String base64image = null;
        File outputFile = null;
        try{
            CustomFFmpeg ffmpeg = CustomFFmpeg.getInstance(context);
            boolean isffmpegSupported = ffmpeg.isSupported();
            LOGGER.i("ffmpeg supported "+isffmpegSupported);
            String fileName = "test" + System.currentTimeMillis()+".jpg";
            outputFile = new File(context.getFilesDir(), fileName);
            String frameExtractCommand =  " -s 300x300 -vframes 1 "+outputFile;
            String command = "-rtsp_transport tcp -i " + cameraConfig.getVideoUrlWithAuth() + frameExtractCommand;
            String[] ffmpegCommand = command.split("\\s+");
            ffmpeg.executeSync(ffmpegCommand, new CustomResponseHandler("connection tester"));
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
