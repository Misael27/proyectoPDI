package com.android.misael.pdi_ocr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;



public class videoActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    //log tag
    private static final String TAG = "OCVSample::Activity";
    int top, left, width, height;
    public static MyTessOCR mTessOCR;
    public static boolean DoOCR = false, DoingOcr = false;
    public static Bitmap imgBmp = null;
    private CameraBridgeViewBase mOpenCvCameraView;


    Bitmap bmp;

    private double x;
    private double y;

    Rect rectan = new Rect();

    Mat mRgba;
    Mat mIntermediateMat;
    Mat mGray;

    BroadcastReceiver receiverText;



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(videoActivity.this);
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Log.i(TAG, "called onCreate");


        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        ArrayList<View> views = new ArrayList<>();
        views.add(findViewById(R.id.btnOK));
        mOpenCvCameraView.addTouchables(views);
        if(mTessOCR == null) {
            mTessOCR = new MyTessOCR(videoActivity.this);
        }

        receiverText = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String text = intent.getStringExtra("text");
                DoingOcr = false;
                if(DoOCR) {
                    if(!text.equals("")) {
                        TextView disp = (TextView) findViewById(R.id.txtDisp);
                        disp.setText(text);
                    }
                }
            }
        };

        IntentFilter intentFilterLoad = new IntentFilter();
        intentFilterLoad.addAction("Broadcast");
        try {
           // unregisterReceiver(receiverText);
            registerReceiver(receiverText, intentFilterLoad);
        }
        catch(Exception e){

        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat croppedPart = null;
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        Size sizeRgba = mRgba.size();

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        left = cols / 8;
        top = rows / 8;

        width = cols * 3 / 4;
        height = rows * 3 / 4;

        if(rectan != null) {
            try {

                croppedPart = mGray.submat(rectan);
                Mat result = new Mat();
               // Imgproc.GaussianBlur(croppedPart, croppedPart, new Size(3, 3), 0);
               // Imgproc.threshold(croppedPart, result, 0, 255, Imgproc.THRESH_OTSU);
                Imgproc.adaptiveThreshold(croppedPart, result, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 4);

                bmp = Bitmap.createBitmap(result.width(), result.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(result, bmp);
                doOCR(bmp);


            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        if(rectan != null) {
            try {
                Mat rgbaInnerWindow = mRgba.submat(rectan);

                Imgproc.cvtColor(rgbaInnerWindow, mIntermediateMat, Imgproc.COLOR_BGRA2RGBA, 4);

                mRgba.convertTo(mRgba,-1,0.4,0.6);

                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_RGBA2BGRA, 4);
                rgbaInnerWindow.release();

                Imgproc.rectangle(mRgba, rectan.br(), rectan.tl(), new Scalar(0, 255, 0));
            }catch(Exception e){
                //rectan = null;
            }

        }
        else{
            //rectan = new Rect(0,0,mRgba.width(),mRgba.height());
        }

        return mRgba;
    }


    @Override
    public boolean onTouch(View arg0,MotionEvent event) {

        double cols = mRgba.cols();// mRgba is your image frame
        double rows = mRgba.rows();

        double xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        double yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        x = (event).getX() - xOffset;
        y = (event).getY() - yOffset;

        if((rectan.x == 0 && rectan.y==0) || (rectan.width!=0 && rectan.height!=0)){
            rectan.x = (int)x;
            rectan.y = (int)y;
            rectan.width = rectan.height = 0;
        }
        else{
            rectan.width = (int)x - rectan.x;
            rectan.height = (int)y - rectan.y;
            if(rectan.width <= 0 || rectan.height <= 0){
                rectan.x = rectan.y = 0;
                rectan.width = mRgba.width();
                rectan.height= mRgba.height();
                return false;
            }
        }
        return false;
    }


    private void doOCR(final Bitmap bitmap) { //CALL TO TESSERACT
        if(DoOCR&&!DoingOcr) {
            DoingOcr = true;
            imgBmp = bitmap;
            handleOCR.startService(this,"Broadcast",null);
        }

    }

    public void OKClicked(View view){
        String text = getText();
        Intent TextAreaActivity = new Intent(getApplicationContext(),TextAreaActivity.class);
        TextAreaActivity.putExtra("text",text);
        startActivity(TextAreaActivity);

    }

    public void ReconocerClicked(View view){
        Button reconocer = (Button)findViewById(R.id.btnReconocer);
        if(!DoOCR){
            DoOCR = true;
            reconocer.setText(R.string.btnReconocerActive);
            reconocer.setBackgroundColor(getResources().getColor(R.color.colorSecondary));

        }
        else{
            DoOCR = false;
            mTessOCR.stopRecognition();
            reconocer.setText(R.string.btnReconocer);
            reconocer.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

    }

    public String getText(){
        TextView disp = (TextView)findViewById(R.id.txtDisp);
        return disp.getText().toString();
    }

}
