package com.silenceender.whoru.utils;

import android.app.Activity;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;

import com.silenceender.whoru.R;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.silenceender.whoru.utils.ToolHelper.*;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.getAffineTransform;
import static org.opencv.imgproc.Imgproc.warpAffine;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * Created by Silen on 2017/8/29.
 */

public class FaceUtil {

    private String landMarkPath;
    private Handler mhHandler;
    private static FaceDet mFaceDet;
    private static String mode = "OUTER EYES AND NOSE";
    private static final int[] OUTER_EYES_AND_NOSE = new int[] {36, 45, 33};
    private static final int[] INNER_EYES_AND_BOTTOM_LIP = new int[] {39, 42, 57};
    private static int imgDim = 96;
    private static final double[][] OUTER_EYES_AND_NOSE_TEMPLATE = new double[][]{
            {0.194157004356,  0.169266924262},
            {0.788859128952,  0.158171147108},
            {0.494950890541,  0.514441370964}
    };
    private static final double[][] INNER_EYES_AND_BOTTOM_LIP_TEMPLATE = new double[][]{
            {0.363784939051,  0.177946865559},
            {0.618963181973,  0.172778129578},
            {0.500203967094,  0.750584423542}
    };
    private List<org.opencv.core.Point> transformTo = new ArrayList<>();
    public static final String PREFIX = "aligned_";

    public static boolean setFaceSize(int size) {
        if(size >= 48) {
            imgDim = size;
        }
        return true;
    }

    public static boolean setMode(String setMode) {
        mode = setMode;
        return true;
    }

    public FaceUtil(String landMarkPath,Activity activity,FaceAlignListener listener) {
        this.landMarkPath = landMarkPath;
        this.mhHandler = new MyHandler(activity,listener);
        if(mode.equals("OUTER EYES AND NOSE")) {
            for(double[] point : OUTER_EYES_AND_NOSE_TEMPLATE) {
                transformTo.add(new org.opencv.core.Point(imgDim * point[0], imgDim * point[1]));
            }
        } else {
            for(double[] point : INNER_EYES_AND_BOTTOM_LIP_TEMPLATE) {
                transformTo.add(new org.opencv.core.Point(imgDim * point[0], imgDim * point[1]));
            }
        }
    }

    public void align(final Activity activity, final String imgPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String targetPath = Constants.getFaceShapeModelPath();
                if (!new File(targetPath).exists()) {
                    sendMsg("初始化模型中...");
                    copyFileFromRawToOthers(activity.getApplicationContext(), R.raw.shape_predictor_68_face_landmarks, targetPath);
                }
                if(mFaceDet == null) {
                    sendMsg("模型加载中...");
                    mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
                }
                sendMsg("人脸数据提取中...");
                String[] splitedPath = imgPath.split(DIVIDE);
                String picName = splitedPath[splitedPath.length-1];
                String storePath = imgPath.substring(0,imgPath.length()-picName.length());
                String newPicPath = storePath + PREFIX + picName;
                List<VisionDetRet> results = mFaceDet.detect(imgPath);
                if(results != null && results.size() == 1){
                    try{
                        List<Point> landmarks = results.get(0).getFaceLandmarks();
                        List<Point> outerLandmarks = new ArrayList<Point>();
                        if(mode.equals("OUTER EYES AND NOSE")) {
                            for(int index : OUTER_EYES_AND_NOSE) {
                                outerLandmarks.add(landmarks.get(index));
                            }
                        } else {
                            for(int index : INNER_EYES_AND_BOTTOM_LIP) {
                                outerLandmarks.add(landmarks.get(index));
                            }
                        }
                        MatOfPoint2f src = new MatOfPoint2f();
                        src.fromList(convertPoints(outerLandmarks));
                        MatOfPoint2f dst = new MatOfPoint2f();
                        dst.fromList(transformTo);
                        Mat facePic = imread(imgPath);
                        //cvtColor(imread(imgPath),facePic,COLOR_BGR2RGB);
                        warpAffine(facePic,facePic,getAffineTransform(src,dst),new Size(imgDim, imgDim));
                        imwrite(newPicPath,facePic);
                        sendMsg(true,newPicPath);
                    } catch (Exception e){
                        e.printStackTrace();
                        sendMsg(false,e.toString());
                    }
                } else if(results == null || results.size() == 0) {
                    sendMsg(false,"好像没有人欸...");
                } else {
                    sendMsg(false,"人好像太多了⊙▽⊙");
                }
            }
        }).start();
    }
    private void sendMsg(boolean isSuccess,String obj){
        Message msg=new Message();
        msg.obj=obj;
        msg.what=isSuccess?1:0;
        mhHandler.sendMessage(msg);
    }

    private void sendMsg(String obj) {
        Message msg = new Message();
        msg.obj = obj;
        msg.what = -1;
        mhHandler.sendMessage(msg);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<Activity> mActivity;
        private final FaceAlignListener listener;

        MyHandler(Activity activity,FaceAlignListener l) {
            mActivity = new WeakReference<Activity>(activity);
            listener = l;
        }

        @Override
        public void handleMessage(Message msg) {
            Activity activity = mActivity.get();
            if (activity != null) {
                if(msg.what == 1) {
                    listener.onFaceAlignSuccessed(msg.obj.toString());
                } else if(msg.what == 0) {
                    listener.onFaceAlignFailed(msg.obj.toString());
                } else {
                    listener.onFaceAlignInfo(msg.obj.toString());
                }
            }
        }
    }

    public interface FaceAlignListener {
        void onFaceAlignInfo(String msg);
        void onFaceAlignSuccessed(String imgPath);
        void onFaceAlignFailed(String msg);
    }
}
