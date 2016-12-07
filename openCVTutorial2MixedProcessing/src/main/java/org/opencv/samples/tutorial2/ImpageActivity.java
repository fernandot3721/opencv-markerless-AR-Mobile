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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import java.util.ArrayList;
import java.util.List;

public class ImpageActivity extends Activity {

    private boolean LogEnable = true;
    private String LogTAG = "TJPDEBUG";

    private Button mBtnBack;
    private Button mBtnMatch;
    private ImageView mTrainimgView;
    private ImageView mQueryimgView;
    private String mTrainImgPath;
    private String mQueryImgPath;
    private Mat mTrainImg;
    private Mat mQueryImg;

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
        mQueryimgView = (ImageView) findViewById(R.id.imageQuery);

        mQueryImgPath = "/sdcard/uc-browser-android2.jpg";
        mTrainImgPath = "/sdcard/uc.png";
//        mTrainImg = Highgui.imread(mTrainImgPath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
//        mQueryImg = Highgui.imread(mQueryImgPath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        mTrainImg = Highgui.imread(mTrainImgPath);
        mQueryImg = Highgui.imread(mQueryImgPath);

        if (LogEnable) {
            Log.d(LogTAG,  "Train Image width: " + mTrainImg.width() + " height: " + mTrainImg.height());
            Log.d(LogTAG,  "Test Image width: " + mQueryImg.width() + " height: " + mQueryImg.height());
        }

        showImg(mTrainImg, mTrainimgView);
        showImg(mQueryImg, mQueryimgView);
    }

    private void showImg(Mat img, ImageView content) {
        Bitmap tmp = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(img, tmp);
        content.setImageBitmap(tmp);
    }

    private void drawKeyPoints(Mat img, MatOfKeyPoint keyPoints) {
        List<KeyPoint> keyList = keyPoints.toList();
        int i = 0;

        if (LogEnable) {
            Log.d(LogTAG, "key point size: " + keyList.size());
        }

        if (null == img) {
            return;
        }

        Features2d.drawKeypoints(img, keyPoints, img);

    }

    private void detectAndCompute(Mat img, FeatureDetector detector, DescriptorExtractor extractor,
                                  MatOfKeyPoint keyPoints, Mat descriptors) {
        PerformanceAnalyzer.log();
        detector.detect(img, keyPoints);
        PerformanceAnalyzer.logAndCount("Find key points COSTs");
        extractor.compute(img, keyPoints, descriptors);
        PerformanceAnalyzer.logAndCount("Find descriptors COSTs");
//        drawKeyPoints(null, keyPoints);
        drawKeyPoints(img, keyPoints);
//        PerformanceAnalyzer.count("Draw key pointns COSTs");
    }

    private void drawMatchResult(MatOfDMatch matches,
                                 MatOfKeyPoint keyPointsTrain, MatOfKeyPoint keyPointsQuery) {
        List<DMatch> matchList = matches.toList();
        if (LogEnable) {
            Log.e(LogTAG, "MATCH size: " + matchList.size());
        }
        if (matchList.size() <= 0) {
            return;
        }

        double maxDistance = 0;
        double minDistance = 1000;

        int rowCount = matchList.size();
        for (int i = 0; i < rowCount; i++) {
            double dist = matchList.get(i).distance;
            if (dist < minDistance) minDistance = dist;
            if (dist > maxDistance) maxDistance = dist;
        }

        List<DMatch> goodMatchesList = new ArrayList<DMatch>();
        double upperBound = 6 * minDistance;
        for (int i = 0; i < rowCount; i++) {
            if (matchList.get(i).distance < upperBound) {
                goodMatchesList.add(matchList.get(i));
            }
        }

        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromList(goodMatchesList);
        if (LogEnable) {
            Log.e(LogTAG, "good MATCH size: " + goodMatchesList.size());
        }

        Mat ret = new Mat();
        ImageView retView = (ImageView) findViewById(R.id.imageResult);
        Features2d.drawMatches(mQueryImg, keyPointsQuery, mTrainImg, keyPointsTrain, goodMatches, ret);
        showImg(ret, retView);

        ImageView retView2 = (ImageView) findViewById(R.id.imageResult2);
        Features2d.drawMatches(mTrainImg, keyPointsTrain, mQueryImg, keyPointsQuery, goodMatches, ret);
        showImg(ret, retView2);
    }

    private void drawMatchResult2(List<MatOfDMatch> matchesList,
                                 MatOfKeyPoint keyPointsTrain, MatOfKeyPoint keyPointsQuery) {

        Log.e(LogTAG, "match list size : " + matchesList.size());

        DMatch[] test;
        List<MatOfDMatch> good = new ArrayList<MatOfDMatch>();
        List<MatOfDMatch> good2 = new ArrayList<MatOfDMatch>();
        for (MatOfDMatch match : matchesList) {
            test = match.toArray();
            if (test[0].distance < 0.75*test[1].distance) {
                good.add(match);
            }
        }
        Log.e(LogTAG, "good match size : " + good.size());

        Mat ret = new Mat();
        ImageView retView = (ImageView) findViewById(R.id.imageResult);
//        PerformanceAnalyzer.log();
        Features2d.drawMatches2(mQueryImg, keyPointsQuery, mTrainImg, keyPointsTrain, good, ret);
//        PerformanceAnalyzer.count("drawMatches2 COSTs");
        showImg(ret, retView);
    }

    private void doMatch() {
        if (LogEnable) {
            Log.d(LogTAG, "======run doMatch");
        }

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        MatOfKeyPoint keyPointsTrain = new MatOfKeyPoint();
        MatOfKeyPoint keyPointsQuery = new MatOfKeyPoint();
        Mat descriptorsTrain = new Mat();
        Mat descriptorsQuery = new Mat();
        MatOfDMatch matches = new MatOfDMatch();
        List<MatOfDMatch> matchesList = new ArrayList<MatOfDMatch>();

        // for train img
        if (LogEnable) {
            Log.d(LogTAG, "--find key point for Train Image");
        }
        detectAndCompute(mTrainImg, detector, extractor, keyPointsTrain, descriptorsTrain);
        showImg(mTrainImg, mTrainimgView);

        // for test img
        if (LogEnable) {
            Log.d(LogTAG, "--find key point for Query Image");
        }
        detectAndCompute(mQueryImg, detector, extractor, keyPointsQuery, descriptorsQuery);
        showImg(mQueryImg, mQueryimgView);

        // start to match
        if (LogEnable) {
            Log.d(LogTAG, "--match two Image");
        }

        // NOTE: ORB only takes CV_32 format
        descriptorsQuery.convertTo(descriptorsQuery, CvType.CV_32F);
        descriptorsTrain.convertTo(descriptorsTrain, CvType.CV_32F);
        // test end

//        matcher.match(descriptorsQuery, descriptorsTrain, matches); // BRUTEFORCE matcher
//        drawMatchResult(matches, keyPointsTrain, keyPointsQuery);

        PerformanceAnalyzer.log();
        matcher.knnMatch(descriptorsQuery, descriptorsTrain, matchesList, 2); // FLANNBASED matcher
        PerformanceAnalyzer.count("knnMatch COSTs");

        drawMatchResult2(matchesList, keyPointsTrain, keyPointsQuery);
    }

}
