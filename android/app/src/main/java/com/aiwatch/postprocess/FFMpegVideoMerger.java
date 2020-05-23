package com.aiwatch.postprocess;

import com.aiwatch.Logger;
import com.arthenica.mobileffmpeg.util.AsyncSingleFFmpegExecuteTask;
import com.arthenica.mobileffmpeg.util.SingleExecuteCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.Executors;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class FFMpegVideoMerger {

    private static final Logger LOGGER = new Logger();

    public void mergeVideos(String[] videoUris, String outputFilePath){

        String inputFilePath = outputFilePath.replace(".mp4", ".txt");
        writeToFile(videoUris, inputFilePath);
        String command = "-f concat -safe 0 -i " + inputFilePath +" -c copy "+outputFilePath;
        LOGGER.i("starting merge of video files "+videoUris.length);
        final AsyncSingleFFmpegExecuteTask asyncCommandTask = new AsyncSingleFFmpegExecuteTask(command, new SingleExecuteCallback() {
            @Override
            public void apply(int returnCode, String executeOutput) {
                if (returnCode == RETURN_CODE_SUCCESS) {

                } else if (returnCode == RETURN_CODE_CANCEL) {
                    LOGGER.i("Command execution cancelled by user.");
                } else {
                    LOGGER.i("ffmpeg concat failed with response " + executeOutput);
                }
                LOGGER.i("ffmpeg concat completed for camera");
            }
        });
        asyncCommandTask.executeOnExecutor(Executors.newSingleThreadExecutor());
        deleteInputFile(inputFilePath);
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
