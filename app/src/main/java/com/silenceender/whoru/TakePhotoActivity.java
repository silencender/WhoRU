package com.silenceender.whoru;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.silenceender.whoru.utils.*;


public class TakePhotoActivity extends AppCompatActivity implements TakePhoto.TakeResultListener,CompressImageUtil.CompressListener,UploadReceiver.UploadResultListener {
    private TakePhoto takePhoto;

    protected ProgressDialog wailLoadDialog;
    protected TakePhoto getTakePhoto(){
        if (takePhoto==null){
            takePhoto=new TakePhoto(this,this);
        }
        return takePhoto;
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

    @Override
    public void onCompressSuccessed(String imgPath) {

    }
    @Override
    public void onCompressFailed(String msg) {
        if (wailLoadDialog!=null)wailLoadDialog.dismiss();
    }

    @Override
    public void onUploadSuccess(String msg) {

    }

    @Override
    public void onUploadFailed(String msg) {

    }
}
