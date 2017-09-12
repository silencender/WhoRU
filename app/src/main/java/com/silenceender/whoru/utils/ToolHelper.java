package com.silenceender.whoru.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alexbbb.uploadservice.MultipartUploadRequest;
import com.alexbbb.uploadservice.UploadNotificationConfig;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.silenceender.whoru.R;
import com.silenceender.whoru.model.Person;
import com.silenceender.whoru.model.RemoteDbManager;

import org.opencv.core.Point;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by Silen on 2017/8/20.
 */

public final class ToolHelper {

    public static final String SEPARATOR = ",";
    public static  final String DIVIDE = "/";
    public static final String TITLESPLIT = " - ";
    public static final String SAVEDIR = Environment.getExternalStorageDirectory().getPath() + "/WhoRU/photos/";
    public static final String TEMPDIR = "../temp/";
    public static String BASEURL = "http://182.254.214.148/prp/";
    public static String UPLOADURL = BASEURL + "upload";
    public static String OPERATEURL = BASEURL + "control";
    public static final String SALT = "UrW9CL1APb";
    public static final String SERVERERR = "服务器错误！";
    public static final String NETERR = "请检查网络连接！";
    private static AsyncHttpClient client = new AsyncHttpClient();

    private ToolHelper() {}

    public static boolean setServer(String url) {
        BASEURL = url;
        UPLOADURL = BASEURL + "upload.php";
        OPERATEURL = BASEURL + "control.php";
        return true;
    }

    public static final String stringJoin(List<String> strs) {
        StringBuilder strBuilder = new StringBuilder();

        if(strs.size() != 0){
            for(String str : strs){
                strBuilder.append(str);
                strBuilder.append(SEPARATOR);
            }

            String joinedString = strBuilder.toString();
            joinedString = joinedString.substring(0, joinedString.length() - SEPARATOR.length());
            return joinedString;
        }
        else {
            return "";
        }
    }

    public static final List<String> stringToList(String stringList) {
        List<String> myList = new ArrayList<String>();
        if(stringList != ""){
            myList = new ArrayList<String>(Arrays.asList(stringList.split(SEPARATOR)));
        }
        return  myList;
    }

    public static final boolean mkDir(String path){
        try {
            File destDir = new File(path);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Dir Create Failed",e.getMessage());
            return false;
        }
    }

    public static String autoName() {
        return new String(String.valueOf(System.currentTimeMillis())) + ".jpg";
    }

    public static String hash(String string) {
        byte[] digest = null;
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            digest = md.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String DICT = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuffer hexString = new StringBuffer();
        for(byte by : digest){
            int index = (0xff & by)%DICT.length();
            hexString.append(DICT.charAt(index));
        }
        return hexString.toString();
    }

    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) { //目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {
                        file.delete();
                    } else { //目录
                        if (file.listFiles().length == 0) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static boolean isInList(List<String> list,String string) {
        boolean isIn = false;
        for(String item : list){
            if(string.equals(item)) {
                isIn = true;
            }
        }
        return isIn;
    }

    public static void uploadMultipart(final Context context, Activity activity, String path, Person person,@Nullable ProgressDialog dialog) {

        final String uploadID = UUID.randomUUID().toString();
        final String IMGPARAM = "photo";
        String[] splitedPath = path.split(DIVIDE);
        String picName = splitedPath[splitedPath.length-1];

        try {
            new MultipartUploadRequest(context, uploadID, UPLOADURL)
                    .addFileToUpload(path,IMGPARAM,picName,"image/jpeg")
                    .addParameter("device", RemoteDbManager.getDeviceID())
                    .addParameter("name", person.getName())
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload();
        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
    }

    @NonNull
    public static void copyFileFromRawToOthers(@NonNull final Context context, @RawRes int id, @NonNull final String targetPath) {
        InputStream in = context.getResources().openRawResource(id);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(targetPath);
            byte[] buff = new byte[1024];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static org.opencv.core.Point convertPoint(android.graphics.Point point) {
        return new org.opencv.core.Point(point.x,point.y);
    }

    public static List<org.opencv.core.Point> convertPoints(List<android.graphics.Point> points) {
        List<org.opencv.core.Point> cvPoints = new ArrayList<>();
        for(android.graphics.Point point : points) {
            cvPoints.add(convertPoint(point));
        }
        return cvPoints;
    }

    public static void get(RequestParams params, @NonNull AsyncHttpResponseHandler responseHandler) {
        Log.i("Get",params.toString());
        client.get(OPERATEURL, params, responseHandler);
    }

    public static void post(RequestParams params, @NonNull AsyncHttpResponseHandler responseHandler) {
        Log.i("Post",params.toString());
        client.post(OPERATEURL, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASEURL + relativeUrl;
    }
}
