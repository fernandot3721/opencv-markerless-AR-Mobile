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
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
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
    private Button mBtnTest;
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

        mBtnTest = (Button) findViewById(R.id.buttonTest);
        mBtnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testDrawPolylines();
            }
        });

        mTrainimgView = (ImageView) findViewById(R.id.imageTrain);
        mQueryimgView = (ImageView) findViewById(R.id.imageQuery);

        mTrainImgPath = "/sdcard/b.jpg";
        mQueryImgPath = "/sdcard/a.jpg";
        mTrainImg = Highgui.imread(mTrainImgPath, Highgui.IMREAD_GRAYSCALE);
        mQueryImg = Highgui.imread(mQueryImgPath, Highgui.IMREAD_GRAYSCALE);
//        mTrainImg = Highgui.imread(mTrainImgPath);
//        mQueryImg = Highgui.imread(mQueryImgPath);

        if (LogEnable) {
            Log.d(LogTAG,  "Train Image width: " + mTrainImg.width() + " height: " + mTrainImg.height());
            Log.d(LogTAG,  "Test Image width: " + mQueryImg.width() + " height: " + mQueryImg.height());
        }

        showImg(mTrainImg, mTrainimgView);
        showImg(mQueryImg, mQueryimgView);
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

        Mat ret2 = new Mat();
        ImageView retView2 = (ImageView) findViewById(R.id.imageResult2);
        Features2d.drawMatches(mTrainImg, keyPointsTrain, mQueryImg, keyPointsQuery, goodMatches, ret2);
        showImg(ret2, retView2);
    }

    private void drawMatchResult2(List<MatOfDMatch> matchesList,
                                 MatOfKeyPoint keyPointsTrain, MatOfKeyPoint keyPointsQuery) {

        Log.e(LogTAG, "match list size : " + matchesList.size());

        List<MatOfDMatch> good = new ArrayList<MatOfDMatch>();
        for (MatOfDMatch match : matchesList) {
            if (match.get(0, 0)[3] < 0.45*match.get(1, 0)[3]) { // distance
                good.add(match);
            }
        }
        Log.e(LogTAG, "good match size : " + good.size());

        Mat ret = new Mat();
        ImageView retView = (ImageView) findViewById(R.id.imageResult);
        Features2d.drawMatches2(mQueryImg, keyPointsQuery, mTrainImg, keyPointsTrain, good, ret);
        showImg(ret, retView);


    }

    private void drawMatchResult3(List<MatOfDMatch> matchesList,
                                  MatOfKeyPoint keyPointsTrain, MatOfKeyPoint keyPointsQuery) {

        List<MatOfDMatch> good = new ArrayList<MatOfDMatch>();
        for (MatOfDMatch match : matchesList) {
            if (match.get(0, 0)[3] < 0.45*match.get(1, 0)[3]) { // distance
                good.add(match);
            }
        }
        Log.e(LogTAG, "good match size : " + good.size());

        int size = good.size();
        if (size < 10)
            return;
        Point[] srcPoints = new Point[size];
        Point[] dstPoints = new Point[size];
        if (size > 0) {
            double[] data = null;
            for (int i = 0; i < size; i++) {
                //MatOfDMatch
                MatOfDMatch match = good.get(i);

                // trainIdx : match.get(0, 0)[1]
                // keypoint : keyPointsTrain.get(trainIdx, 0)
                data = keyPointsTrain.get((int)match.get(0, 0)[1], 0);
                srcPoints[i] = new Point(data[0], data[1]);

                // queryIdx = match.get(0, 0)[0]
                data = keyPointsQuery.get((int)match.get(0, 0)[0], 0);
                dstPoints[i] = new Point(data[0], data[1]);
            }
        }

        MatOfDMatch mask = new MatOfDMatch();
        Mat matrix = Calib3d.findHomography(new MatOfPoint2f(srcPoints), new MatOfPoint2f(dstPoints), Calib3d.RANSAC, 4, mask);
        Log.e(LogTAG, matrix.dump());

//        int height = mQueryImg.rows();
//        int width = mQueryImg.cols();
//
//        Point[] array = {
//                new Point(1,1),
//                new Point(width+1,1),
//                new Point(width+1,height+1),
//                new Point(1,height+1),
//        };

        ArrayList<MatOfPoint> list = new ArrayList<MatOfPoint>();

//        MatOfPoint srcRect = new MatOfPoint(array);
//        Log.e(LogTAG, srcRect.dump());
//        srcRect.convertTo(srcRect, CvType.CV_32F);
        MatOfPoint dstRect = new MatOfPoint();
//        dstRect.convertTo(dstRect, CvType.CV_32F);
//        Core.perspectiveTransform(srcRect, dstRect, matrix);
//        dstRect.convertTo(dstRect, CvType.CV_32S);

        dstRect = calcAffineTransformRect(mQueryImg.size(), matrix);

//        int y = mTrainImg.rows()/2;
//        int x = mTrainImg.cols()/2;
//        array = dstRect.toArray();
//        for (int i=0; i<array.length; i++) {
//            array[i].x += x;
//            array[i].y += y;
//        }
//        dstRect = new MatOfPoint(array);

        Log.e(LogTAG, dstRect.dump());
        list.add(dstRect);

        Core.polylines(mTrainImg, list, true, new Scalar(200, 0, 200, 200), 3, Core.LINE_AA, 0);

        Mat ret = new Mat();
        ImageView retView = (ImageView) findViewById(R.id.imageResult);
        Features2d.drawMatches2(mQueryImg, keyPointsQuery, mTrainImg, keyPointsTrain, good, ret);
        showImg(ret, retView);
    }

    private void drawMatchResult4(MatOfDMatch matches,
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


        int size = goodMatchesList.size();
        Point[] srcPoints = new Point[size];
        Point[] dstPoints = new Point[size];
        if (size > 0) {
            double[] data = null;
            for (int i = 0; i < size; i++) {
                DMatch match = goodMatchesList.get(i);
                data = keyPointsTrain.get((match.trainIdx), 0);
                srcPoints[i] = new Point(data[0], data[1]);

                data = keyPointsQuery.get((match.queryIdx), 0);
                dstPoints[i] = new Point(data[0], data[1]);
            }
        }

        MatOfDMatch mask = new MatOfDMatch();
        Mat matrix = Calib3d.findHomography(new MatOfPoint2f(srcPoints), new MatOfPoint2f(dstPoints), Calib3d.RANSAC, 4, mask);

//        int width = mQueryImg.rows();
//        int height = mQueryImg.cols();
//
//        Point[] array = {
//                new Point(0,0),
//                new Point(width,0),
//                new Point(width,height),
//                new Point(0,height),
//        };


//        MatOfPoint srcRect = new MatOfPoint(array);
//        Log.e(LogTAG, srcRect.dump());
//        srcRect.convertTo(srcRect, CvType.CV_32F);
        MatOfPoint dstRect = new MatOfPoint();
//        dstRect.convertTo(dstRect, CvType.CV_32F);
//        Core.perspectiveTransform(srcRect, dstRect, matrix);


        dstRect = calcAffineTransformRect(mQueryImg.size(), matrix);

        dstRect.convertTo(dstRect, CvType.CV_32S);
        Log.e(LogTAG, dstRect.dump());
        ArrayList<MatOfPoint> list = new ArrayList<MatOfPoint>();
        list.add(dstRect);

        Core.polylines(mTrainImg, list, true, new Scalar(200, 0, 200, 200), 3, Core.LINE_AA, 0);

        Mat ret = new Mat();
        ImageView retView = (ImageView) findViewById(R.id.imageResult);
        Features2d.drawMatches(mQueryImg, keyPointsQuery, mTrainImg, keyPointsTrain, goodMatches, ret);
        showImg(ret, retView);

    }

    // Order of output is: Top Left, Bottom Left, Bottom Right, Top Right
    MatOfPoint calcAffineTransformRect(Size imgSize, Mat transMat)
    {
        MatOfPoint dstRect = null;
        float width = (float)(imgSize.width) - 1;
        float height = (float)(imgSize.height) - 1;

        double[] src = {0,0,width,width,0,height,height,0,1,1,1,1};
        Mat srcMat = new MatOfDouble(src).reshape(0, 3);
//        Mat srcMat = new Mat(Matd); //(Mat_<double>(3,4) << 0,0,width,width,0,height,height,0,1,1,1,1);

        Mat dstMat = transMat.setTo(srcMat);
        Point[] dst = new Point[4];

        Point pt = new Point();
        for(int i=0; i<4; i++){
            pt.x = dstMat.get(0,i)[0] / dstMat.get(2,i)[0];
            pt.y = dstMat.get(1,i)[0] / dstMat.get(2,i)[0];
            dst[i] = pt;
        }
        dstRect = new MatOfPoint(dst);

        return dstRect;
    }

    private void doMatch() {
        if (LogEnable) {
            Log.d(LogTAG, "======run doMatch");
        }

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
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

//        PerformanceAnalyzer.log();
//        matcher.match(descriptorsQuery, descriptorsTrain, matches); // BRUTEFORCE matcher
//        PerformanceAnalyzer.count("Match COSTs");
//        drawMatchResult4(matches, keyPointsTrain, keyPointsQuery);

        PerformanceAnalyzer.log();
        matcher.knnMatch(descriptorsQuery, descriptorsTrain, matchesList, 2); // FLANNBASED matcher
        PerformanceAnalyzer.count("knnMatch COSTs");

        drawMatchResult3(matchesList, keyPointsTrain, keyPointsQuery);
    }

    public void testDrawPolylines() {
        Point[] array = {
            new Point(100,100),
            new Point(400,100),
            new Point(400,400),
            new Point(100,400),
        };

        MatOfPoint mop = new MatOfPoint(array);
        ArrayList<MatOfPoint> list = new ArrayList<MatOfPoint>();
        list.add(mop);

        Mat ret = Mat.zeros(512, 512, CvType.CV_8UC1);
        ImageView retView = (ImageView) findViewById(R.id.imageResult);
        Core.polylines(ret, list, false, new Scalar(200, 0, 200, 200), 3, Core.LINE_AA, 0);
        showImg(ret, retView);


        float width = (float)(mTrainImg.size().width) - 1;
        float height = (float)(mTrainImg.size().height) - 1;


        double[] src = {0,0,width,width,0,height,height,0,1,1,1,1};
        Mat srcMat = new MatOfDouble(src).reshape(0, 3);
//        Mat srcMat = new Mat(Matd); //(Mat_<double>(3,4) << 0,0,width,width,0,height,height,0,1,1,1,1);

//        Mat dstMat = transMat.mul(srcMat);

        Point[] dst = new Point[4];

        Point pt = new Point();
        for(int i=0; i<4; i++){
            pt.x = srcMat.get(0,i)[0] / srcMat.get(2,i)[0];
            pt.y = srcMat.get(1,i)[0] / srcMat.get(2,i)[0];
            dst[i] = pt;
        }
        MatOfPoint dstRect = new MatOfPoint(dst);
        Log.e(LogTAG, dstRect.dump());
    }

}
