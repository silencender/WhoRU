package com.silenceender.whoru;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import com.silenceender.whoru.model.RemoteDbManager;
import com.silenceender.whoru.preferences.MyPreferencesActivity;
import com.silenceender.whoru.utils.FaceUtil;
import com.wang.avi.AVLoadingIndicatorView;

import static com.silenceender.whoru.utils.ToolHelper.*;

/**
 * Created by Silen on 2017/8/18.
 */

public class MainActivity extends TakePhotoActivity {

    private static String picName;
    private AVLoadingIndicatorView avi;

    public static MainActivity mainActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        setContentView(R.layout.activity_main);
        avi= (AVLoadingIndicatorView) findViewById(R.id.avi);
        mainActivity = MainActivity.this;
        checkNetwork();
        RemoteDbManager.addNewDevice(this, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, MyPreferencesActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void uploadToGallary(View view){
        Intent intent = new Intent(MainActivity.this, ListActivity.class);
        startActivity(intent);
    }

    public void recognizeFace(View view){
        TakePhoto obj = getTakePhoto();
        obj.setImageUri(SAVEDIR + TEMPDIR + autoName());
        obj.picTakeCrop();
    }

    void startAnim(){
        //avi.show();
        avi.smoothToShow();
    }

    void stopAnim(){
        //avi.hide();
        avi.smoothToHide();
    }

    private void checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(!isConnected) {
            showNormalDialog();
        }
    }

    private void showNormalDialog(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mainActivity);
        normalDialog.setTitle("连接异常");
        normalDialog.setMessage("应用需要网络，请检查您的网络连接！");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        normalDialog.show();
    }

    @Override
    public void takeCancel() {
        super.takeCancel();
    }

    @Override
    public void takeFail(String msg) {
        super.takeFail(msg);
    }

    @Override
    public void takeSuccess(Uri uri) {
        super.takeSuccess(uri);
        //showImg(uri);
        startAnim();
        compressPic(uri.getPath());
    }

    @Override
    public void onCompressSuccessed(String imgPath) {
        super.onCompressSuccessed(imgPath);
        stopAnim();
        String[] splitedPath = imgPath.split(DIVIDE);
        picName = splitedPath[splitedPath.length-1];
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        intent.putExtra("imgPath", imgPath);
        startActivity(intent);
    }

    @Override
    public void onCompressFailed(String msg) {
        super.onCompressFailed(msg);
        Toast.makeText(mainActivity,"Failed in compress: "+msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFaceAlignSuccessed(String msg) {

    }

    @Override
    public void onFaceAlignFailed(String msg) {

    }

}
