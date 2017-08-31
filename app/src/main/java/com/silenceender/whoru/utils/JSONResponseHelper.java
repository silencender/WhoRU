package com.silenceender.whoru.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Silen on 2017/8/23.
 */

public final class JSONResponseHelper {
    private JSONObject json = null;
    private int status = -1;
    private String message = "";

    public JSONResponseHelper(String string) {
        try{
            Log.i("JSON Response",string);
            this.json = new JSONObject(string);
            parseJSON();
        }catch (JSONException e) {
            Log.w("JSON Parse Error",e.getMessage());
            e.printStackTrace();
        }
    }

    public JSONResponseHelper(byte[] bytes) {
        try{
            String string = new String(bytes, "utf-8");
            Log.i("JSON Response",string);
            try{
                this.json = new JSONObject(string);
                parseJSON();
            }catch (JSONException e) {
                Log.w("JSON Parse Error",e.getMessage());
                e.printStackTrace();
            }
        }catch (UnsupportedEncodingException e) {
            Log.w("Response Encode Error",e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseJSON() {
        try{
            this.status = this.json.getInt("status");
            this.message = this.json.getString("message");
        }catch (JSONException e) {
            Log.w("JSON Structure Error",e.getMessage());
            e.printStackTrace();
        }
    }

    public JSONObject getJSON() {
        return this.json;
    }

    public int getStatus() {
        return this.status;
    }

    public String getMsg() {
        return this.message;
    }
}
