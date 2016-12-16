package meiroo.cvgl;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


public class CVGLActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private static final int VIEW_MODE_RGBA = 0;
    private static final int VIEW_MODE_GRAY = 1;
    private static final int VIEW_MODE_CANNY = 2;
    private static final int VIEW_MODE_FEATURES = 5;

    private int mViewMode;
    private Mat mRgba;
    private Mat mIntermediateMat;
    private Mat mGray;
    private Mat mRgba2;
    private Mat mGray2;

    private MenuItem mItemPreviewRGBA;
    private MenuItem mItemPreviewGray;
    private MenuItem mItemPreviewCanny;
    private MenuItem mItemPreviewFeatures;

    private CameraBridgeViewBase mOpenCvCameraView;

    public long time = 0;
    public static int result = 0;

    private WorkerThread mWorkerThread;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("nonfree");
                    System.loadLibrary("cvgl");

                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    public CVGLActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial2_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mWorkerThread = new WorkerThread();
        //Utils.CopyAssets(getResources().getAssets(), Environment.getExternalStorageDirectory().getPath(), "CVGL");
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

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        mWorkerThread.exit();
        mWorkerThread = null;
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgba2 = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mGray2 = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mRgba2.release();
        mGray.release();
        mGray2.release();
        mIntermediateMat.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
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

                if (SystemClock.uptimeMillis() - time >= 1500) {
                    time = SystemClock.uptimeMillis();
//                    PerformanceAnalyzer.log();
                    mGray2 = mGray.clone();
                    mRgba2 = mRgba.clone();
//                    PerformanceAnalyzer.count("COPY Costs");
                    mWorkerThread.executeTask(mGray2, mRgba2);
                }

                break;
        }

        return mRgba;
    }

    private class WorkerThread extends Thread {
        protected static final String TAG = "WorkerThread";
        private Handler mHandler;
        private Looper mLooper;

        public WorkerThread() {
            start();
        }

        public void run() {
            Looper.prepare();
            mLooper = Looper.myLooper();
            mHandler = new Handler(mLooper) {
                @Override
                public void handleMessage(Message msg) {
                    Mat[] params = (Mat[]) msg.obj;
                    detectObject(params[0], params[1]);
                }
            };
            Looper.loop();
        }

        public void exit() {
            if (mLooper != null) {
                mLooper.quit();
                mLooper = null;
            }
        }

        public void executeTask(Mat... params) {
            mHandler.removeCallbacksAndMessages(null);
            Message msg = Message.obtain();
            msg.obj = params;
            mHandler.sendMessage(msg);
        }
    }

    synchronized private void detectObject(Mat gray, Mat rgb) {
        PerformanceAnalyzer.log();
        result = native_FindFeatures(gray.getNativeObjAddr(), rgb.getNativeObjAddr(), time);
//        Log.i("GLAndroid","recog result = " + result);
        if (result > 0) {
            PerformanceAnalyzer.logAndCount("SUCESS JCOST");
        } else {
            PerformanceAnalyzer.logAndCount("FAILED JCOST");
        }
//        Highgui.imwrite("/sdcard/test/" + time + ".jpg", gray);
//        PerformanceAnalyzer.count("imwrite COST");

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemPreviewRGBA) {
            mViewMode = VIEW_MODE_RGBA;
        } else if (item == mItemPreviewGray) {
            mViewMode = VIEW_MODE_GRAY;
        } else if (item == mItemPreviewCanny) {
            mViewMode = VIEW_MODE_CANNY;
            domakeTrain();
        } else if (item == mItemPreviewFeatures) {
            mViewMode = VIEW_MODE_FEATURES;
        }

        return true;
    }

    private void domakeTrain() {
        native_makeTrain();
    }

    static native void native_makeTrain();

    static native int native_FindFeatures(long matAddrGr, long matAddrRgba, long id);

    static native void native_start();

    static native void native_gl_resize(int w, int h);

    static native void native_gl_render();

    static native void native_key_event(int key, int status);

    static native void native_touch_event(float x, float y, int status);
}


