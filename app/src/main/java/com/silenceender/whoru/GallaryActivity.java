package com.silenceender.whoru;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.alexbbb.uploadservice.UploadService;
import com.alexbbb.uploadservice.UploadServiceBroadcastReceiver;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.silenceender.whoru.model.Person;
import com.silenceender.whoru.model.PersonDbManager;
import com.silenceender.whoru.model.RemoteDbManager;
import com.silenceender.whoru.utils.UploadReceiver;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.silenceender.whoru.utils.FaceUtil.PREFIX;
import static com.silenceender.whoru.utils.ToolHelper.*;

/**
 * Created by Silen on 2017/8/18.
 */

public class GallaryActivity extends TakePhotoActivity{

    public static GallaryActivity gallaryActivity;
    private AVLoadingIndicatorView avi;
    private static final String TITLE = "人物图库";
    private UploadServiceBroadcastReceiver uploadReceiver;
    private GridView gridview;
    private  Person person;
    private PersonDbManager db;
    private  List<String> listItems;
    private static String picName;
    private ProgressDialog dialog;
    private static boolean isLong = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(TITLE);
        setSupportActionBar(toolbar);
        gallaryActivity = GallaryActivity.this;
        db = new PersonDbManager(gallaryActivity.getApplicationContext());
        gridview = (GridView) findViewById(R.id.gridview);
        avi= (AVLoadingIndicatorView) findViewById(R.id.avi);
        this.dialog = new ProgressDialog(this);
        UploadService.NAMESPACE = "com.silenceender.whoru";
        uploadReceiver = new UploadReceiver(this,this.dialog);
        Intent intent = getIntent();
        this.person = new Person(intent.getStringExtra("name"));
        refreshList();
        FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.addfab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPic();
            }
        });
        FloatingActionButton deleteFab = (FloatingActionButton) findViewById(R.id.delfab);
        deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showClearDialog();
            }
        });
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
                isLong = false;
            }
        });
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                isLong = true;
                showDeleteDialog(position);
                return false;
            }
        });
        dialog.setMessage("");
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private void refreshList() {
        this.listItems = new ArrayList<String>();
        this.person= db.query(this.person.getName()).get(0);
        this.listItems = this.person.getPicnameList();
        gridview.setAdapter(new GallaryAdapter(this,this.person));
    }

    private void addPic() {
        TakePhoto obj = getTakePhoto();
        obj.setImageUri(this.person);
        obj.picSelectCrop();
    }

    private void showClearDialog(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(gallaryActivity);
        normalDialog.setTitle("确认清空");
        normalDialog.setMessage("确认清空图库吗？");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        person.setPicnames("");
                        RemoteDbManager.clearGal(person, new AsyncHttpResponseHandler() {
                            @Override
                            public void onStart() {
                                startAnim();
                            }
                            @Override
                            public void onFinish() {
                                stopAnim();
                            }
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                db.update(person);
                                deleteFolderFile(SAVEDIR + person.getName(),false);
                                refreshList();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(gallaryActivity,NETERR,Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        normalDialog.show();
    }

    private void showDeleteDialog(final int position){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(gallaryActivity);
        normalDialog.setTitle("确认删除");
        normalDialog.setMessage("确认删除图片吗？");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String picName = listItems.get(position);
                        RemoteDbManager.update(new Person(person.getName(),PREFIX + picName), new AsyncHttpResponseHandler() {
                            @Override
                            public void onStart() {
                                startAnim();
                            }
                            @Override
                            public void onFinish() {
                                stopAnim();
                            }
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                person.removePicname(position);
                                db.update(person);
                                deleteFolderFile(SAVEDIR + person.getName() + DIVIDE + picName,true);
                                deleteFolderFile(SAVEDIR + person.getName() + DIVIDE + PREFIX + picName,true);
                                refreshList();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(gallaryActivity,NETERR,Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        normalDialog.show();
    }

    void startAnim(){
        //avi.show();
        avi.smoothToShow();
    }

    void stopAnim(){
        //avi.hide();
        avi.smoothToHide();
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
        dialog.setMessage("压缩图片中...");
        dialog.show();
        compressPic(uri.getPath());
    }

    @Override
    public void onCompressSuccessed(String imgPath) {
        super.onCompressSuccessed(imgPath);
        String[] splitedPath = imgPath.split(DIVIDE);
        picName = splitedPath[splitedPath.length-1];
        dialog.setProgress(20);
        //uploadMultipart(this,this,imgPath,this.person,this.dialog);
        faceAlign(imgPath);
    }

    @Override
    public void onCompressFailed(String msg) {
        super.onCompressFailed(msg);
        try {
            dialog.dismiss();
        } catch (Exception e) {
        }
        Toast.makeText(gallaryActivity,"Failed in compress: "+msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFaceAlignInfo(String msg) {
        super.onFaceAlignInfo(msg);
        dialog.setMessage(msg);
    }

    @Override
    public void onFaceAlignSuccessed(String imgPath) {
        super.onFaceAlignSuccessed(imgPath);
        dialog.setMessage("上传人脸数据中...");
        dialog.setProgress(50);
        uploadMultipart(this,this,imgPath,this.person,this.dialog);
    }

    @Override
    public void onFaceAlignFailed(String msg) {
        super.onFaceAlignFailed(msg);
        try {
            dialog.dismiss();
        } catch (Exception e) {
        }
        deleteFolderFile(SAVEDIR + person.getName() + DIVIDE + picName,true);
        Toast.makeText(gallaryActivity,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUploadSuccess(String msg) {
        super.onUploadSuccess(msg);
        person.addPicname(picName);
        db.update(this.person);
        refreshList();
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUploadFailed(String msg) {
        super.onUploadFailed(msg);
        try {
            dialog.dismiss();
        } catch (Exception e) {
        }
        deleteFolderFile(SAVEDIR + person.getName() + DIVIDE + picName,true);
        deleteFolderFile(SAVEDIR + person.getName() + DIVIDE + PREFIX + picName,true);
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

}
