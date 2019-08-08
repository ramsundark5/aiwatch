package com.aiwatch.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.aiwatch.Logger;
import org.greenrobot.essentials.io.FileUtils;

import java.io.FileOutputStream;

public class RegionOfInterest {

    private static final Logger LOGGER = new Logger();

    private String saveImage(String inputFilePath){
        /*try {
            String outputFilePath = "outputImage.png";
            FileUtils.copyFile(inputFilePath, outputFilePath);
            RectF location = new RectF(float left, float top, float right, float bottom);
            if(location != null){
                FileOutputStream fos=new FileOutputStream(outputFilePath);
                Bitmap bitmapOutput = BitmapFactory.decodeFile(outputFilePath);
                //if bounding box needed, comment out the above line and uncomment the below ones
                drawBoundingBox(bitmapOutput, location);
                bitmapOutput.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
            LOGGER.d("image filepath is " + outputFilePath);
            return outputFilePath;
        }
        catch (Exception e) {
            LOGGER.e(e, e.getMessage());
        }*/
        return null;
    }

    private void drawBoundingBox(Bitmap bitmap, RectF location){
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        canvas.drawRect(location, paint);
    }
}
