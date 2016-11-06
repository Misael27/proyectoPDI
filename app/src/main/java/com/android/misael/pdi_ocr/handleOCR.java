package com.android.misael.pdi_ocr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.android.misael.pdi_ocr.MainActivity.imgBmp;


public class handleOCR extends IntentService {

    public handleOCR() {
        super("handleOCR");
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, handleOCR.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
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
        String text = MainActivity.mTessOCR.getOCRResult(imgBmp);
        if (!text.equals("")) {
            Intent intent = new Intent();
            intent.setAction("Broadcast");
            intent.putExtra("text",text);
            sendBroadcast(intent);
        }

    }
}
