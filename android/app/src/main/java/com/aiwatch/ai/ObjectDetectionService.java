package com.aiwatch.ai;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.google.firebase.perf.metrics.AddTrace;

import java.io.IOException;
import java.util.List;

public class ObjectDetectionService {

    private static final Logger LOGGER = new Logger();
    private Classifier objectDetector;

    // Configuration values for the prepackaged SSD model.
    private static final float DETECTION_CONFIDENCE_SENSITIVITY = 0.6f;
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;

    //model loaded from https://storage.googleapis.com/download.tensorflow.org/models/tflite/coco_ssd_mobilenet_v1_1.0_quant_2018_06_29.zip
    //https://www.tensorflow.org/lite/guide/hosted_models
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";

    public ObjectDetectionService(final AssetManager assets){
        objectDetector = getObjectDetector(assets);
    }

    @AddTrace(name = "objdectDetectionTrace")
    public ObjectDetectionResult detectObjects(final Bitmap croppedImage) {
        ObjectDetectionResult result = new ObjectDetectionResult();
        long startTime = System.currentTimeMillis();
        final List<Classifier.Recognition> detectionResults = objectDetector.recognizeImage(croppedImage);
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        LOGGER.d("inference time "+timeElapsed);
        for (Classifier.Recognition detection: detectionResults) {
            switch(detection.getTitle())
            {
                case "person":
                    if(detection.getConfidence() > DETECTION_CONFIDENCE_SENSITIVITY){
                        result.setName(Events.PERSON_DETECTED_EVENT.getName());
                        result.setMessage(AppConstants.PERSON_DETECTED_MESSAGE);
                        result.setConfidence(detection.getConfidence());
                        result.setLocation(detection.getLocation());
                        LOGGER.i("person detected with confidence "+detection.getConfidence());
                    }
                    break;
                case "bicycle":
                case "boat":
                case "car":
                case "motorcycle":
                case "truck":
                    if(detection.getConfidence() > DETECTION_CONFIDENCE_SENSITIVITY){
                        result.setName(Events.VEHICLE_DETECTED_EVENT.getName());
                        result.setMessage(AppConstants.VEHICLE_DETECTED_MESSAGE);
                        result.setConfidence(detection.getConfidence());
                        result.setLocation(detection.getLocation());
                    }
                    break;
                case "bear":
                case "bird":
                case "cat":
                case "cow":
                case "dog":
                case "horse":
                case "mouse":
                case "sheep":
                    if(detection.getConfidence() > DETECTION_CONFIDENCE_SENSITIVITY){
                        result.setName(Events.ANIMAL_DETECTED_EVENT.getName());
                        result.setMessage(AppConstants.ANIMAL_DETECTED_MESSAGE);
                        result.setConfidence(detection.getConfidence());
                        result.setLocation(detection.getLocation());
                    }
                    break;
                default:
                    //result.setCameraName(Events.OTHER_DETECTED_EVENT.getCameraName());
                    //result.setConfidence(detection.getConfidence());
                    LOGGER.v("detected " + detection.getTitle() + " with confidence "+detection.getConfidence());
            }
            if(result.getName() != null && result.getConfidence() > DETECTION_CONFIDENCE_SENSITIVITY){
                //break the for loop
                return result;
            }
        }
        //TODO if we reached here its just other events. comment these out before PROD
        result.setName(Events.OTHER_DETECTED_EVENT.getName());
        result.setMessage(AppConstants.OTHER_DETECTED_MESSAGE);
        result.setConfidence(0);
        return result;
    }

    private Classifier getObjectDetector(final AssetManager assets){
        if (this.objectDetector == null) {
            try {
                this.objectDetector =
                        TFLiteObjectDetectionAPIModel.create(
                                assets,
                                TF_OD_API_MODEL_FILE,
                                TF_OD_API_LABELS_FILE,
                                TF_OD_API_INPUT_SIZE,
                                TF_OD_API_IS_QUANTIZED);
            } catch (final IOException e) {
                LOGGER.e("Exception initializing classifier! " + e.getMessage());
            }
        }
        return this.objectDetector;
    }
}
