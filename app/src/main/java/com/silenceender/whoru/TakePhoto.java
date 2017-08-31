package com.silenceender.whoru;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telecom.DisconnectCause;
import android.text.TextUtils;
import android.util.Log;

import com.silenceender.whoru.model.Person;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.silenceender.whoru.utils.ToolHelper.*;

public class TakePhoto {

    public final static int PIC_SELECT_CROP = 123;

    public final static int PIC_TAKE_CROP = 124;

    public final static int PIC_CROP = 125;

    private final int outputX=1200;//裁剪默认宽度
    private final int outputY=1200;//裁剪默认高度
    private int cropHeight;
    private int cropWidth;
    private Activity activity;
    private TakeResultListener l;
    private Uri imageUri;

    public TakePhoto(Activity activity, TakeResultListener l) {
        this.activity = activity;
        this.l = l;
        mkDir(SAVEDIR);
        mkDir(SAVEDIR + TEMPDIR);
    }

    /**
     * 处理拍照或裁剪结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onResult(int requestCode,int resultCode,Intent data) {
        StringBuffer sb = new StringBuffer();
        sb.append("requestCode:").append(requestCode).append("--resultCode:").append(resultCode).append("--data:").append(data).append("--imageUri:").append(imageUri);
        Log.w("info", sb.toString());
        switch (requestCode) {
            case PIC_SELECT_CROP:
                if (resultCode == Activity.RESULT_OK&&data!=null) {//从相册选择照片并裁切
                    cropImageUri(data.getData(), PIC_CROP);
                } else {
                    l.takeCancel();
                }
                break;
            case PIC_TAKE_CROP://拍取照片,并裁切
                if (resultCode == Activity.RESULT_OK) {
                    cropImageUri(imageUri, PIC_CROP);
                }
                break;
            case PIC_CROP://裁剪照片
                if (resultCode == Activity.RESULT_OK) {
                    l.takeSuccess(imageUri);
                } else if (resultCode == Activity.RESULT_CANCELED) {//裁切的照片没有保存
                    if (data != null) {
                        Bitmap bitmap = data.getParcelableExtra("data");//获取裁切的结果数据
                        //将裁切的结果写入到文件
                        writeToFile(bitmap);
                        l.takeSuccess(imageUri);
                        Log.w("info", bitmap == null ? "null" : "not null");
                    } else {
                        l.takeFail("没有获取到裁剪结果");
                    }
                } else {
                    l.takeCancel();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 从相册选择照片进行裁剪
     *
     */
    public void picSelectCrop() {
        picSelectCrop(outputX,outputY);
    }
    /**
     * 从相册选择照片进行裁剪
     *
     * @param cropWidth 裁切宽度
     * @param cropHeight 裁切高度
     */
    public void picSelectCrop(int cropWidth, int cropHeight) {
        this.cropWidth=cropWidth;
        this.cropHeight=cropHeight;
        Intent intent = new Intent(Intent.ACTION_PICK,null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(intent, PIC_SELECT_CROP);
    }

    public void picCrop(Bundle imgdata, Uri data) {
        picCrop(imgdata,data,outputX,outputY);
    }

    public void picCrop(Bundle imgdata, Uri data, int cropWidth, int cropHeight) {
        this.cropWidth=cropWidth;
        this.cropHeight=cropHeight;
        Uri uri = Uri.fromFile(new File(imgdata.getString("imgurl")));
        imageUri = uri;
        cropImageUri(data, PIC_CROP);
    }

    /**
     * 拍取照片不裁切
     *
     * @param uri 图片保存的路径
     */
    /*
    public void picTakeOriginal(Uri uri) {
        imageUri = uri;
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//将拍取的照片保存到指定URI
        activity.startActivityForResult(intent, PIC_TAKE_ORIGINAL);
    }
*/
    /**
     * 从相机拍取照片进行裁剪
     */
    public void picTakeCrop() {
      picTakeCrop(outputX,outputY);
    }
    /**
     * 从相机拍取照片进行裁剪
     * @param cropWidth 裁切宽度
     * @param cropHeight 裁切高度
     */
    public void picTakeCrop(int cropWidth, int cropHeight) {
        this.cropWidth=cropWidth;
        this.cropHeight=cropHeight;
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        //intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照
        intent.putExtra(MediaStore.EXTRA_OUTPUT, this.imageUri);//将拍取的照片保存到指定URI
        activity.startActivityForResult(intent,PIC_TAKE_CROP);
    }


    /**
     * 裁剪指定uri对应的照片
     *
     * @param imageUri：uri对应的照片
     * @param requestCode：请求码
     */
    private void cropImageUri(Uri imageUri, int requestCode) {
        boolean isReturnData = isReturnData();
        Log.w("ksdinf","isReturnData:"+( isReturnData ? "true" : "false"));
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("crop", "true");
        //intent.putExtra("aspectX",1);
        //intent.putExtra("aspectY",1);
        //intent.putExtra("outputX",cropWidth);
        //intent.putExtra("outputY",cropHeight);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, this.imageUri);
        intent.putExtra("return-data", isReturnData);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 是否裁剪之后返回数据
     **/
    private boolean isReturnData() {
        String release= Build.VERSION.RELEASE;
        int sdk= Build.VERSION.SDK_INT;
        Log.i("ksdinf","release:"+release+"sdk:"+sdk);
        String manufacturer = Build.MANUFACTURER;
        if (!TextUtils.isEmpty(manufacturer)) {
            if (manufacturer.toLowerCase().contains("lenovo")) {//对于联想的手机返回数据
                return true;
            }
        }
//        if (sdk>=21){//5.0或以上版本要求返回数据
//            return  true;
//        }
        return false;
    }

    /**
     * 将bitmap写入到文件
     *
     * @param bitmap
     */
    private void writeToFile(Bitmap bitmap) {
        if (bitmap == null) return;
        File file = new File(imageUri.getPath());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bos.toByteArray());
            bos.flush();
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) try {
                fos.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public Uri getImageUri() {
        return imageUri;
    }
    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
    public void setImageUri(String picname) {
        setImageUri( Uri.fromFile(new File(picname)));
    }
    public void setImageUri(Person person) {
        mkDir(SAVEDIR + person.getName() + DIVIDE);
        setImageUri(Uri.fromFile(new File(SAVEDIR  + person.getName() + DIVIDE + autoName())));
        Log.w("info", "####################    " + this.imageUri.getPath());
    }

    /**
     * 拍照结果监听接口
     */
    public interface TakeResultListener {
        void takeSuccess(Uri uri);

        void takeFail(String msg);

        void takeCancel();
    }
}