package com.android.misael.pdi_ocr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    //log tag
    private static final String TAG = "OCVSample::Activity";

    private static final int       VIEW_MODE_RGBA     = 0;
    private static final int       VIEW_MODE_GRAY     = 1;
    private static final int       VIEW_MODE_CANNY    = 2;
    private static final int       VIEW_MODE_FEATURES = 5;

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    private int mViewMode;

    private MenuItem mItemPreviewRGBA;
    private MenuItem mItemPreviewGray;
    private MenuItem mItemPreviewCanny;
    private MenuItem mItemPreviewFeatures;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    Mat mIntermediateMat;
    Mat mGray;
    boolean isProcess = true;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
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
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.show_camera);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
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
        final int viewMode = mViewMode;
        switch (viewMode) {
            case VIEW_MODE_GRAY:
                // input frame has gray scale format
                Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_RGBA:
                // input frame has RBGA format
                mRgba = inputFrame.rgba();
                break;
            case VIEW_MODE_CANNY:
                // input frame has gray scale format
                mRgba = inputFrame.rgba();
                Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_FEATURES:
                // input frame has RGBA format
                mRgba = inputFrame.rgba();
                mGray = inputFrame.gray();

                Scalar CONTOUR_COLOR = new Scalar(255);
                MatOfKeyPoint keypoint = new MatOfKeyPoint();
                List<KeyPoint> listpoint = new ArrayList<KeyPoint>();
                KeyPoint kpoint = new KeyPoint();
                Mat mask = Mat.zeros(mGray.size(), CvType.CV_8UC1);
                int rectanx1;
                int rectany1;
                int rectanx2;
                int rectany2;

                //
                Scalar zeos = new Scalar(0, 0, 0);
                // List<MatOfPoint> contour1 = new ArrayList<MatOfPoint>();
                List<MatOfPoint> contour2 = new ArrayList<MatOfPoint>();
                Mat kernel = new Mat(1, 50, CvType.CV_8UC1, Scalar.all(255));
                Mat morbyte = new Mat();
                Mat hierarchy = new Mat();

                Rect rectan2 = new Rect();//
                Rect rectan3 = new Rect();//
                int imgsize = mRgba.height() * mRgba.width();
                //
                if (isProcess) {
                    isProcess = false;
                FeatureDetector detector = FeatureDetector
                        .create(FeatureDetector.MSER);
                detector.detect(mGray, keypoint);
                listpoint = keypoint.toList();
                //
                for (int ind = 0; ind < listpoint.size(); ind++) {
                    kpoint = listpoint.get(ind);
                    rectanx1 = (int) (kpoint.pt.x - 0.5 * kpoint.size);
                    rectany1 = (int) (kpoint.pt.y - 0.5 * kpoint.size);
                    // rectanx2 = (int) (kpoint.pt.x + 0.5 * kpoint.size);
                    // rectany2 = (int) (kpoint.pt.y + 0.5 * kpoint.size);
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
                for (int ind = 0; ind < contour2.size(); ind++) {
                    rectan3 = Imgproc.boundingRect(contour2.get(ind));
                    if (rectan3.area() > 0.5 * imgsize || rectan3.area() < 100
                            || rectan3.width / rectan3.height < 2) {
                        Mat roi = new Mat(morbyte, rectan3);
                        roi.setTo(zeos);

                    } else
                        Imgproc.rectangle(mRgba, rectan3.br(), rectan3.tl(),
                                CONTOUR_COLOR);
                }
                    isProcess = true;
                     return mRgba;
                 }



                // FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
                break;
        }

        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Preview RGBA");
        mItemPreviewGray = menu.add("Preview GRAY");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewFeatures = menu.add("Find features");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemPreviewRGBA) {
            mViewMode = VIEW_MODE_RGBA;
        } else if (item == mItemPreviewGray) {
            mViewMode = VIEW_MODE_GRAY;
        } else if (item == mItemPreviewCanny) {
            mViewMode = VIEW_MODE_CANNY;
        } else if (item == mItemPreviewFeatures) {
            mViewMode = VIEW_MODE_FEATURES;
        }

        return true;
    }
}
