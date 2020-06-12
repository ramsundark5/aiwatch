package com.aiwatch.postprocess;

import android.content.Context;

import com.aiwatch.Logger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import nl.bravobit.ffmpeg.CustomFFmpeg;
import nl.bravobit.ffmpeg.FFcommandExecuteResponseHandler;

public class FFMpegVideoMerger {

    private static final Logger LOGGER = new Logger();

    public void mergeVideos(Context context, String[] videoUris, String outputFilePath){

        String inputFilePath = outputFilePath.replace(".mp4", ".txt");
        writeToFile(videoUris, inputFilePath);

        CustomFFmpeg ffmpeg = CustomFFmpeg.getInstance(context);
        String command = "-f concat -safe 0 -i " + inputFilePath +" -c copy "+outputFilePath;
        LOGGER.i("starting merge of video files "+videoUris.length);
        String[] ffmpegCommand = command.split("\\s+");

        ffmpeg.execute(ffmpegCommand, new FFcommandExecuteResponseHandler() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(String message) {
                LOGGER.d("ffmpeg concat success");
            }

            @Override
            public void onProgress(String message) {

            }

            @Override
            public void onFailure(String message) {
                LOGGER.e("ffmpeg concat failed with response " + message);
            }
        });
    }

    private void writeToFile(String[] videoUris, String filePath) {
        try {
            FileOutputStream stream = new FileOutputStream(filePath);
            OutputStreamWriter outWriter = new OutputStreamWriter(stream);
            for (String videoUri : videoUris) {
                outWriter.append("file '"+videoUri+"'");
                outWriter.append("\r");
            }
            outWriter.close();
            stream.close();
        }
        catch (Exception e) {
            LOGGER.e("Exception creating merge input file "+e);
        }
    }

    private void deleteInputFile(String filePath){
        try{
            File inputFile = new File(filePath);
            inputFile.delete();
        }catch (Exception e){
            LOGGER.e("Error cleaning up merge text file "+e);
        }
    }
}
