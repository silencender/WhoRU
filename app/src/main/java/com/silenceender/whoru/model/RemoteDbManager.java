package com.silenceender.whoru.model;

import android.app.Activity;
import android.telephony.TelephonyManager;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static android.content.Context.TELEPHONY_SERVICE;

import static  com.silenceender.whoru.utils.ToolHelper.*;

/**
 * Created by Silen on 2017/8/22.
 */

public final class RemoteDbManager {

    private static String deviceID = "";

    private RemoteDbManager() {}

    public static void addNewDevice(Activity activity, AsyncHttpResponseHandler responseHandler) {
        if(deviceID.equals("")) {
            TelephonyManager tm = (TelephonyManager) activity.getSystemService(TELEPHONY_SERVICE);
            deviceID = tm.getDeviceId();
            post(setParams("addNewDevice",md5(SALT + deviceID)),responseHandler);
        }
    }

    public static void insert(Person person, AsyncHttpResponseHandler responseHandler) {
        post(setParams("insert",person.getName()),responseHandler);
    }

    public static void delete(Person person, AsyncHttpResponseHandler responseHandler) {
        post(setParams("delete",person.getName()),responseHandler);
    }

    public static void clear(AsyncHttpResponseHandler responseHandler) {
        post(setParams("clear",""),responseHandler);
    }

    public static void update(Person person, AsyncHttpResponseHandler responseHandler) {
        post(setParams("update",person.getJSONEncode()),responseHandler);
    }

    public static void ask(String picName, AsyncHttpResponseHandler responseHandler) {
        post(setParams("ask",picName),responseHandler);
    }

    private static RequestParams setParams(String method,String value) {
        RequestParams params = new RequestParams();
        params.put("deviceID",deviceID);
        params.put("method", method);
        params.put("value",	value);
        return params;
    }
}
