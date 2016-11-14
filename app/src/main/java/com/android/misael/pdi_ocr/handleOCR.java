package com.android.misael.pdi_ocr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.Gravity;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.android.misael.pdi_ocr.videoActivity.imgBmp;
import static com.android.misael.pdi_ocr.MenuActivity.imgBmps;


public class handleOCR extends IntentService {

    String action;
    int idx = -1;

    public handleOCR() {
        super("handleOCR");
    }

    public static void startService(Context context, String action,  Map<String,String> params) {
        Intent intent = new Intent(context, handleOCR.class);
        intent.setAction(action);
        if(params != null){
            for(Map.Entry<String, String> entry : params.entrySet()){
                intent.putExtra(entry.getKey(),entry.getValue());
            }
        }
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            action = intent.getAction();
            if(action.equals("image")){
                idx = Integer.valueOf(intent.getStringExtra("idx"));
            }
            storeImage();
            doOCR();
        }
    }


    private void storeImage() {
        //almacena la imagen bmp en sdcard
        String datapath = Environment.getExternalStorageDirectory() + "/sampleBMP/img.png";
        FileOutputStream out = null;
        try{
            out = new FileOutputStream(datapath);
            imgBmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        }catch(Exception e){
            e.printStackTrace();
        } finally {
            try {
                if(out!=null){
                    out.close();
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }

    }

    private void doOCR() {
        //aplica ocr a un bmp
        Bitmap bmp = imgBmp;
        if(action.equals("image")){
            if(idx != -1 && idx < imgBmps.size()) {
                bmp = imgBmps.get(idx);
            }
        }

        String text = videoActivity.mTessOCR.getOCRResult(bmp);

        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("text",text);
        sendBroadcast(intent);
    }


}
