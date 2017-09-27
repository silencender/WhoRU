package com.silenceender.whoru.utils;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alexbbb.uploadservice.UploadServiceBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Silen on 2017/8/22.
 */

public final class UploadReceiver extends UploadServiceBroadcastReceiver {

    private static final String TAG = "AndroidUploadService";
    private UploadResultListener listener;
    private ProgressDialog dialog;

    public UploadReceiver(UploadResultListener listener,@Nullable ProgressDialog dialog) {
        this.listener = listener;
        this.dialog = dialog;
    }

    @Override
    public void onProgress(String uploadId, int progress) {
        Log.i(TAG, "The progress of the upload with ID "
                + uploadId + " is: " + progress);
        try {
            this.dialog.setProgress(progress);
        } catch (Exception e) {
        }
    }

    @Override
    public void onProgress(final String uploadId,
                           final long uploadedBytes,
                           final long totalBytes) {
        Log.i(TAG, "Upload with ID " + uploadId +
                " uploaded bytes: " + uploadedBytes
                + ", total: " + totalBytes);
    }

    @Override
    public void onError(String uploadId, Exception exception) {
        Log.e(TAG, "Error in upload with ID: " + uploadId + ". "
                + exception.getLocalizedMessage(), exception);
        this.listener.onUploadFailed("Failed uploading!");
        try {
            dialog.dismiss();
        } catch (Exception e) {
        }
    }

    @Override
    public void onCompleted(String uploadId,
                            int serverResponseCode,
                            String serverResponseMessage) {
        Log.i(TAG, "Upload with ID " + uploadId
                + " has been completed with HTTP " + serverResponseCode
                + ". Response from server: " + serverResponseMessage);
        JSONResponseHelper response = new JSONResponseHelper(serverResponseMessage);
        if(response.getStatus() == 1) {
            this.listener.onUploadSuccess(response.getMsg());
        }
        else if(response.getStatus() == 0) {
            this.listener.onUploadFailed(response.getMsg());
        } else {
            this.listener.onUploadFailed("Server Error");
        }
        try {
            dialog.dismiss();
        } catch (Exception e) {
        }
        //If your server responds with a JSON, you can parse it
        //from serverResponseMessage string using a library
        //such as org.json (embedded in Android) or Google's gson
    }

    public interface UploadResultListener {
        void onUploadSuccess(String msg);
        void onUploadFailed(String msg);
    }
}
