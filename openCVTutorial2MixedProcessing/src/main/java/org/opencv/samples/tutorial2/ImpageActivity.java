package org.opencv.samples.tutorial2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import java.util.List;

public class ImpageActivity extends Activity {

    private boolean LogEnable = true;
    private String LogTAG = "TJPDEBUG";

    private Button mBtnBack;
    private Button mBtnMatch;
    private ImageView mTrainimgView;
    private ImageView mTestimgView;
    private String mTrainImgPath;
    private String mTestImgPath;
    private Mat mTrainImg;
    private Mat mTestImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impage);
        init();
    }

    private void init() {
        mBtnBack = (Button) findViewById(R.id.buttonBack);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImpageActivity.this, Tutorial2Activity.class);
                startActivity(intent);
                finish();
            }
        });

        mBtnMatch = (Button) findViewById(R.id.buttonMatch);
        mBtnMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doMatch();
            }
        });

        mTrainimgView = (ImageView) findViewById(R.id.imageTrain);
        mTestimgView = (ImageView) findViewById(R.id.imageTest);

        mTestImgPath = "/sdcard/uc-browser-android2.jpg";
        mTrainImgPath = "/sdcard/uc.png";
        mTrainImg = Highgui.imread(mTrainImgPath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        mTestImg = Highgui.imread(mTestImgPath);

        if (LogEnable) {
            Log.d(LogTAG,  "Train Image width: " + mTrainImg.width() + " height: " + mTrainImg.height());
            Log.d(LogTAG,  "Test Image width: " + mTestImg.width() + " height: " + mTestImg.height());
        }

        showImg(mTrainImg, mTrainimgView);
        showImg(mTestImg, mTestimgView);
    }

    private void showImg(Mat img, ImageView content) {
        Bitmap tmp = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, tmp);
        content.setImageBitmap(tmp);
    }

    private void drawKeyPoints(Mat img, MatOfKeyPoint keyPoints) {
        List<KeyPoint> keyList = keyPoints.toList();
        int i = 0;

        if (LogEnable) {
            Log.d(LogTAG, "match size: " + keyList.size());
        }

        for (KeyPoint point : keyList) {
            if (i++ > 500) {
                break;
            }
            Core.circle(img, point.pt, 10, new Scalar(255,0,0,255));
        }

    }

    private void doMatch() {
        if (LogEnable) {
            Log.d(LogTAG, "======run doMatch");
        }

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();

        // for train img
        if (LogEnable) {
            Log.d(LogTAG, "find key point for Train Image");
        }
        detector.detect(mTrainImg, keyPoints);
        drawKeyPoints(mTrainImg, keyPoints);
        showImg(mTrainImg, mTrainimgView);

        // for test img
        if (LogEnable) {
            Log.d(LogTAG, "find key point for Test Image");
        }
        detector.detect(mTestImg, keyPoints);
        drawKeyPoints(mTestImg, keyPoints);
        showImg(mTestImg, mTestimgView);
    }

}
