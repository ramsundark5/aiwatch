package com.aiwatch.media;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.TimingLogger;
import com.aiwatch.Logger;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.ai.ObjectDetectionService;
import com.aiwatch.common.AppConstants;
import com.google.firebase.perf.metrics.AddTrace;

import org.bytedeco.javacv.AndroidFrameConverter;

public class ImageProcessor {

    private static final Logger LOGGER = new Logger();
    private ObjectDetectionService objectDetectionService;
    private AndroidFrameConverter frameConverter;
    private static int imagesProcessed = 0;
    private static int imagesSkipped = 0;

    public ImageProcessor(AssetManager assetManager){
        frameConverter = new AndroidFrameConverter();
        objectDetectionService = new ObjectDetectionService(assetManager);
    }

    @AddTrace(name = "imageProcessTrace")
    public ObjectDetectionResult processImage(final FrameEvent frameEvent){
        TimingLogger timings = new TimingLogger(LOGGER.DEFAULT_TAG, "ImageProcessor performance");
        Bitmap bitmapOutput = frameConverter.convert(frameEvent.getFrame());
        timings.addSplit("Bitmap conversion time");
        if(bitmapOutput == null){
            imagesSkipped++;
            return null;
        }
        Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmapOutput, AppConstants.TF_OD_API_INPUT_SIZE, AppConstants.TF_OD_API_INPUT_SIZE, false);
        timings.addSplit("Bitmap resize time");
        //conditionally call based on camera config
        final ObjectDetectionResult objectDetectionResult = objectDetectionService.detectObjects(croppedBitmap);
        timings.addSplit("Inference time");
        imagesProcessed++;
        LOGGER.d("completed detection service. Thread is "+ Thread.currentThread().getName());
        LOGGER.d("images processed "+ imagesProcessed);
        LOGGER.d("images skippped "+ imagesSkipped);
        timings.addSplit("Image processing time");
        timings.dumpToLog();
        return objectDetectionResult;
    }
}

