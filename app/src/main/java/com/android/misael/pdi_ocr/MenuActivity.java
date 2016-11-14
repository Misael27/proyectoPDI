package com.android.misael.pdi_ocr;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.misael.pdi_ocr.videoActivity.mTessOCR;

public class MenuActivity extends AppCompatActivity {

    private static final int DIR_PICK = 1234;
    private Mat mGray;
    BroadcastReceiver receiverText;
    String resultImage = "";
    int tot = 0;
    int cont = 0;
    public static List<Bitmap> imgBmps;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OPENCV", "OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        register(R.id.videoButton);
        register(R.id.imagenButton);

        receiverText = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String text = intent.getStringExtra("text");
                resultImage = resultImage + "\r\n\r\n" + text;
                cont += 1;
                if(cont == tot){
                    if(resultImage.equals("")){
                        Toast toast;
                        toast = Toast.makeText(getApplicationContext(),"No hay resultados en "+String.valueOf(cont)+" regiones", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER| Gravity.CENTER_HORIZONTAL,0,0);
                        toast.show();
                    }
                    else{
                       callTextAreaActivity();
                    }
                }
            }
        };

        IntentFilter intentFilterLoad = new IntentFilter();
        intentFilterLoad.addAction("image");
        try {
           // unregisterReceiver(receiverText);
            registerReceiver(receiverText, intentFilterLoad);
        }
        catch(Exception e){

        }

        mTessOCR = new MyTessOCR(MenuActivity.this);

        imgBmps = new ArrayList<>();

    }

    private void register(int buttonResourceId){
        findViewById(buttonResourceId).setOnClickListener(buttonClickListener);
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v){
                switch(v.getId()){
                    case R.id.videoButton:
                        Intent VideoActivity = new Intent(getApplicationContext(),videoActivity.class);
                        startActivity(VideoActivity);
                        break;
                    case R.id.imagenButton:
                        setDirPickIntent();
                        break;
                }
            }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DIR_PICK && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String route = uri.getPath();
            getBmp(route);
        }
    }

    private void setDirPickIntent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
        intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
        intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        startActivityForResult(intent, DIR_PICK);
    }

    public void getBmp(String route){
        try {
            mGray = Imgcodecs.imread(route, 0);
            if(!mGray.empty()){
                detectText();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void detectText(){ //MSER
      //  Mat mGray = new Mat();

        Scalar CONTOUR_COLOR = new Scalar(138,226,52);
        MatOfKeyPoint keypoint = new MatOfKeyPoint();
        List<KeyPoint> listpoint = new ArrayList<KeyPoint>();
        KeyPoint kpoint = new KeyPoint();
        Mat mask = Mat.zeros(mGray.size(), CvType.CV_8UC1);
        int rectanx1;
        int rectany1;
        int rectanx2;
        int rectany2;

        Scalar zeos = new Scalar(0, 0, 0);
        // List<MatOfPoint> contour1 = new ArrayList<MatOfPoint>();
        List<MatOfPoint> contour2 = new ArrayList<>();
        Mat kernel = new Mat(1, 50, CvType.CV_8UC1, Scalar.all(255));
        Mat morbyte = new Mat();
        Mat hierarchy = new Mat();

        Rect rectan2 = new Rect();//
        Rect rectan3;//
        int imgsize = mGray.height() * mGray.width();
        //

        FeatureDetector detector = FeatureDetector
                .create(FeatureDetector.MSER);
        detector.detect(mGray, keypoint);
        listpoint = keypoint.toList();
        //
        for (int ind = 0; ind < listpoint.size(); ind++) {
            kpoint = listpoint.get(ind);
            rectanx1 = (int) (kpoint.pt.x - 0.5 * kpoint.size);
            rectany1 = (int) (kpoint.pt.y - 0.5 * kpoint.size);
            rectanx2 = (int) (kpoint.size);
            rectany2 = (int) (kpoint.size);
            if (rectanx1 <= 0)
                rectanx1 = 1;
            if (rectany1 <= 0)
                rectany1 = 1;
            if ((rectanx1 + rectanx2) > mGray.width())
                rectanx2 = mGray.width() - rectanx1;
            if ((rectany1 + rectany2) > mGray.height())
                rectany2 = mGray.height() - rectany1;
            Rect rectant = new Rect(rectanx1, rectany1, rectanx2, rectany2);
            Mat roi = new Mat(mask, rectant);
            roi.setTo(CONTOUR_COLOR);

        }

        Imgproc.morphologyEx(mask, morbyte, Imgproc.MORPH_DILATE, kernel);
        Imgproc.findContours(morbyte, contour2, hierarchy,
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        tot = contour2.size();
        resultImage = "";
        cont = 0;
        imgBmps.clear();
        for (int ind = contour2.size()-1; ind >= 0; ind--) {
            rectan3 = Imgproc.boundingRect(contour2.get(ind));
            if (rectan3.area() > 0.5 * imgsize || rectan3.area() < 100
                    || rectan3.width / rectan3.height < 2) {
                Mat roi = new Mat(morbyte, rectan3);
                roi.setTo(zeos);
                cont += 1;
                if(cont == tot){
                    if(!resultImage.equals("")){
                        Toast toast;
                        toast = Toast.makeText(getApplicationContext(),"No hay resultados en "+String.valueOf(cont)+" regiones", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER| Gravity.CENTER_HORIZONTAL,0,0);
                        toast.show();
                    }
                    else{
                        callTextAreaActivity();
                    }
                }
            } else {
                Mat croppedPart;
                croppedPart = mGray.submat(rectan3);
                Mat result = new Mat();
                Imgproc.GaussianBlur(croppedPart,croppedPart, new Size(3, 3), 0);
                Imgproc.threshold(croppedPart,result,0,255,Imgproc.THRESH_OTSU);
                Bitmap bmp = Bitmap.createBitmap(result.width(), result.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(result, bmp);
                doOCR(bmp);
            }
        }
        showInterfaz(false);
    }

    private void doOCR(Bitmap bitmap) { //CALL TO TESSERACT
        imgBmps.add(bitmap);
        Map<String,String> params = new HashMap<>();
        params.put("idx",String.valueOf(imgBmps.size()-1));
        handleOCR.startService(this,"image",params);
    }

    public void showInterfaz(boolean show){
        TextView textView2 = (TextView)findViewById(R.id.textView2);
        TextView textView3 = (TextView)findViewById(R.id.textView3);
        Button videoButton = (Button)findViewById(R.id.videoButton);
        Button imagenButton = (Button)findViewById(R.id.imagenButton);
        if(show){
            textView2.setVisibility(View.VISIBLE);
            videoButton.setVisibility(View.VISIBLE);
            imagenButton.setVisibility(View.VISIBLE);
            textView3.setVisibility(View.GONE);
        }
        else {
            textView2.setVisibility(View.GONE);
            videoButton.setVisibility(View.GONE);
            imagenButton.setVisibility(View.GONE);
            textView3.setVisibility(View.VISIBLE);
        }
    }

    public void callTextAreaActivity(){
        showInterfaz(true);
        Intent TextAreaActivity = new Intent(getApplicationContext(),TextAreaActivity.class);
        TextAreaActivity.putExtra("text",resultImage);
        startActivity(TextAreaActivity);
    }

}
