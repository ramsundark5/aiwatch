package com.aiwatch.media;

import android.app.Service;
import android.content.Intent;
import android.os.FileObserver;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.otaliastudios.transcoder.MediaTranscoder;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CompressionService extends Service {

    private static final Logger LOGGER = new Logger();
    private FileObserver observer;
    private DefaultVideoStrategy videoStrategy = DefaultVideoStrategy.atMost(360, 480)
            .frameRate(15)
            .iFrameInterval(10F)
            .build();

    @Override
    public void onCreate() {
        LOGGER.i("Creating new monitoring service instance ");
        compressVideos();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void compressVideos()  {
        File appFolder = getApplicationContext().getFilesDir();
        File uncompressedVideoFolder = new File (appFolder, AppConstants.UNCOMPRESSED_VIDEO_FOLDER);
        LOGGER.i("compression service started");
        observer = new FileObserver(uncompressedVideoFolder.getAbsolutePath(), FileObserver.CLOSE_WRITE) {
            @Override
            public void onEvent(int event, final String rawFile) {
                if (event == FileObserver.CLOSE_WRITE  && !rawFile.equals(".probe")) { // check that it's not equal to .probe because thats created every time camera is launched
                    LOGGER.i("FileObserver created for path "+uncompressedVideoFolder.getAbsolutePath());
                    compressFile(rawFile);
                }
            }
        };
        observer.startWatching();
    }

    private synchronized void compressFile(String rawFileName){
        try{
            File appFolder = getApplicationContext().getFilesDir();
            File uncompressedVideoFolder = new File (appFolder, AppConstants.UNCOMPRESSED_VIDEO_FOLDER);
            File rawFile = new File(uncompressedVideoFolder, rawFileName);
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
                        public void onTranscodeProgress(double progress) {}
                        public void onTranscodeCompleted(int successCode) {
                            LOGGER.d("compression completed");
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
