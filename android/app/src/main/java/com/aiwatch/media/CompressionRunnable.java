package com.aiwatch.media;

import android.content.Context;

import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.otaliastudios.transcoder.MediaTranscoder;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CompressionRunnable implements Runnable {

    private static final Logger LOGGER = new Logger();
    private final Context context;
    private DefaultVideoStrategy videoStrategy = DefaultVideoStrategy.atMost(360, 480)
            .frameRate(15)
            .iFrameInterval(10F)
            .build();

    public CompressionRunnable(Context context){
        this.context = context;
    }

    @Override
    public void run() {
        try {
            long waitTime = 30*1000; //30 seconds
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    compressVideos();
                }
            }, 0, waitTime);
        } catch (Exception e) {
            LOGGER.e(e, "compression exception " + e.getMessage());
        }
    }

    public void compressVideos()  {
        File appFolder = context.getFilesDir();
        File uncompressedVideoFolder = new File (appFolder, AppConstants.UNCOMPRESSED_VIDEO_FOLDER);
        LOGGER.i("compression service started");

        for(File rawFile: uncompressedVideoFolder.listFiles()){
            if(rawFile.canWrite()){
                compressFile(rawFile);
            }
        }
    }

    private void compressFile(File rawFile){
        try{
            File appFolder = context.getFilesDir();
            File compressedFolder = new File(appFolder, AppConstants.COMPRESSED_VIDEO_FOLDER);
            if (!compressedFolder.exists()) {
                compressedFolder.mkdirs();
            }
            if(!rawFile.getName().endsWith(".mp4")){
                //we don't care about non-media files. ignore and return
                return;
            }
            long startTime = System.nanoTime();
            File outputFile = new File(compressedFolder, rawFile.getName());
            LOGGER.d("starting compression");
            Future compressFuture = MediaTranscoder.into(outputFile.getAbsolutePath())
                    //.setValidator(new WriteAlwaysValidator())
                    .setDataSource(rawFile.getAbsolutePath())
                    .setVideoOutputStrategy(videoStrategy)
                    .setListener(new MediaTranscoder.Listener() {
                        public void onTranscodeProgress(double progress) {
                            LOGGER.d("compression in progress "+ Thread.currentThread().getName());
                        }
                        public void onTranscodeCompleted(int successCode) {
                            LOGGER.d("compression completed");
                            compressVideos();
                        }
                        public void onTranscodeCanceled() {}
                        public void onTranscodeFailed(Throwable exception) {
                            LOGGER.e("error "+exception.getMessage());
                        }
                    }).transcode();
            compressFuture.get();
            rawFile.delete();
            long endTime = System.nanoTime();
            long time_ns = endTime - startTime;
            long time_s = TimeUnit.NANOSECONDS.toSeconds(time_ns);
            LOGGER.d("time taken "+time_s);
            LOGGER.d("compressed file created at path "+outputFile.getAbsolutePath());
        }catch(Exception e){
            LOGGER.e(e, "error compressing file "+ e.getMessage());
        }
    }
}
