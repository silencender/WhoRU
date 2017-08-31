package com.silenceender.whoru;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import org.opencv.android.OpenCVLoader;

import com.silenceender.whoru.utils.*;
import com.tzutalin.dlib.Constants;


public class TakePhotoActivity extends AppCompatActivity implements TakePhoto.TakeResultListener,CompressImageUtil.CompressListener,FaceUtil.FaceAlignListener,UploadReceiver.UploadResultListener {
    private TakePhoto takePhoto;
    private static boolean isOpencvLoaded = false;

    protected ProgressDialog wailLoadDialog;
    protected TakePhoto getTakePhoto(){
        if (takePhoto==null){
            takePhoto=new TakePhoto(this,this);
        }
        return takePhoto;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isOpencvLoaded) {
            if (OpenCVLoader.initDebug()) {
                isOpencvLoaded = true;
            } else {
                Toast.makeText(this,"Opencv库加载失败！",Toast.LENGTH_SHORT).show();
            }
        }
        /*AsyncServiceHelper.initOpenCV(OpenCVLoader.OPENCV_VERSION_3_1_0, this, new LoaderCallbackInterface() {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.i("OPENCV", "OpenCV loaded successfully");
                    }
                    break;
                    default: {
                        Log.e("OPENCV", "OpenCV loaded failed");
                    }
                    break;
                }
            }

            @Override
            public void onPackageInstall(int operation, InstallCallbackInterface callback) {
            }
        });*/
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
        getTakePhoto().onResult(requestCode,resultCode, data);
        super.onActivityResult(requestCode,resultCode, data);
    }
    @Override
    public void takeSuccess(Uri uri) {
        Log.i("info", "takeSuccess：" + uri);
    }
    @Override
    public void takeFail(String msg) {
        Log.w("info", "takeFail:" + msg);
    }
    @Override
    public void takeCancel() {
        Log.w("info", "用户取消");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (takePhoto!=null)outState.putParcelable("imageUri", takePhoto.getImageUri());
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        getTakePhoto().setImageUri((Uri)savedInstanceState.getParcelable("imageUri"));
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected void compressPic(String path) {
        new CompressImageUtil().compressImageByPixel(path,this,this);
    }

    protected void faceAlign(String imgPath) {
        new FaceUtil(Constants.getFaceShapeModelPath(),this,this).align(this,imgPath);
    }
    @Override
    public void onCompressSuccessed(String imgPath) {

    }
    @Override
    public void onCompressFailed(String msg) {
        if (wailLoadDialog!=null)wailLoadDialog.dismiss();
    }

    @Override
    public void onFaceAlignInfo(String msg) {
        //Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onFaceAlignSuccessed(String imgPath) {

    }
    @Override
    public void onFaceAlignFailed(String msg) {

    }

    @Override
    public void onUploadSuccess(String msg) {

    }

    @Override
    public void onUploadFailed(String msg) {

    }
}
