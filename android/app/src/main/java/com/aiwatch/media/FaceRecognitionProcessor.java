package com.aiwatch.media;

import com.aiwatch.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.FisherFaceRecognizer;

import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;

public class FaceRecognitionProcessor {

    private static final Logger LOGGER = new Logger();

    public void recognize(String trainingDir, String testImagePath) {
        try{
            LOGGER.d("starting face recognition");
            Mat testImage = imread(testImagePath, IMREAD_GRAYSCALE);

            File root = new File(trainingDir);

            FilenameFilter imgFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    name = name.toLowerCase();
                    return name.endsWith(".jpg") || name.endsWith(".png");
                }
            };

            File[] imageFiles = root.listFiles(imgFilter);

            MatVector images = new MatVector(imageFiles.length);

            Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
            IntBuffer labelsBuf = labels.createBuffer();

            int counter = 0;

            for (File image : imageFiles) {
                Mat img = imread(image.getAbsolutePath(), IMREAD_GRAYSCALE);

                int label = Integer.parseInt(image.getName().split("\\-")[0]);

                images.put(counter, img);

                labelsBuf.put(counter, label);

                LOGGER.d("detected label as "+ label);
                counter++;
            }

            FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
            // FaceRecognizer faceRecognizer = EigenFaceRecognizer.create();
            // FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();

            LOGGER.d("starting training");
            faceRecognizer.train(images, labels);
            LOGGER.d("completed training");

            LOGGER.d("starting recognition");
            IntPointer label = new IntPointer(1);
            DoublePointer confidence = new DoublePointer(1);
            faceRecognizer.predict(testImage, label, confidence);
            LOGGER.d("completed recognition");
            int predictedLabel = label.get(0);

            LOGGER.d("Predicted label: " + predictedLabel);
        }catch (Exception e){
            LOGGER.e(e, "Error running face recognition");
        }
    }
}
