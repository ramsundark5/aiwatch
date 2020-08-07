package com.aiwatch.postprocess;

import android.content.Context;
import com.aiwatch.Logger;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class FFMpegVideoMerger {

    private static final Logger LOGGER = new Logger();

    public void mergeVideos(Context context, String[] videoUris, String outputFilePath){
        String inputFilePath = outputFilePath.replace(".mp4", ".txt");
        writeToFile(videoUris, inputFilePath);
        String command = "-f concat -safe 0 -i " + inputFilePath +" -c copy "+outputFilePath;
        LOGGER.i("starting merge of video files "+videoUris.length);

        FFmpeg.executeAsync(command, new ExecuteCallback() {

            @Override
            public void apply(final long executionId, final int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    LOGGER.i("ffmpeg merge completed");
                } else {
                    LOGGER.e("ffmpeg extraction failed with response " + Config.getLastCommandOutput());
                }
                deleteInputFile(inputFilePath);
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
