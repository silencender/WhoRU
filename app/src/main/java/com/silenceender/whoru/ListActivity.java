package com.silenceender.whoru;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.silenceender.whoru.model.RemoteDbManager;
import com.silenceender.whoru.model.*;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.silenceender.whoru.utils.ToolHelper.*;

/**
 * Created by Silen on 2017/8/18.
 */

public class ListActivity extends AppCompatActivity {

    private ListView mListView;
    private AVLoadingIndicatorView avi;
    private static final String TITLE = "人物列表";
    private static PersonDbManager db;
    private static List<Person> personList;
    private static List<String> listItems;
    private static boolean isLong = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(TITLE);
        setSupportActionBar(toolbar);
        db = new PersonDbManager(ListActivity.this.getApplicationContext());

        FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.addfab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputDialog();
            }
        });

        FloatingActionButton deleteFab = (FloatingActionButton) findViewById(R.id.delfab);
        deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showClearDialog();
            }
        });

        avi= (AVLoadingIndicatorView) findViewById(R.id.avi);

        mListView = (ListView) findViewById(R.id.list_view);
        refreshList();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!isLong){
                    Intent intent = new Intent(ListActivity.this, GallaryActivity.class);
                    intent.putExtra("name", personList.get(position).getName());
                    startActivity(intent);
                }
                isLong = false;
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                isLong = true;
                final Person person = personList.get(position);
                showDeleteDialog(person);
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private void refreshList() {
        listItems = new ArrayList<String>();
        personList = db.query(null);
        if(personList != null) {
            for(Person person : personList){
                listItems.add(person.getName());
            }
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
        mListView.setAdapter(adapter);
    }

    private void showInputDialog() {
        final EditText editText = new EditText(ListActivity.this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(ListActivity.this);
        inputDialog.setTitle("输入姓名").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = editText.getText().toString().trim();
                        if(!input.equals("") && !isInList(listItems,input)){
                            final Person person = new Person(input);
                            RemoteDbManager.insert(person, new AsyncHttpResponseHandler() {
                                @Override
                                public void onStart() {
                                    startAnim();
                                }
                                @Override
                                public void onFinish() {
                                    stopAnim();
                                }
                                @Override
                                public void onRetry(int retryNo) {
                                    if(retryNo == 2){
                                        Toast.makeText(ListActivity.this,"请检查网络连接！",Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        Log.i("添加成功", new String(responseBody));
                                        db.insert(person);
                                        refreshList();
                                }
                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    String responseString = "";
                                    if(responseBody != null){
                                        responseString = new String(responseBody);
                                    }
                                    Log.e("服务器故障",responseString);
                                    Toast.makeText(ListActivity.this,"操作失败！",Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(ListActivity.this,"请输入合适的名字！",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        inputDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        inputDialog.show();
    }

    private void showClearDialog(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(ListActivity.this);
        normalDialog.setTitle("确认清空");
        normalDialog.setMessage("确认清空列表吗？");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RemoteDbManager.clear(new AsyncHttpResponseHandler() {
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
                                db.clear();
                                deleteFolderFile(SAVEDIR,false);
                                refreshList();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(ListActivity.this,"操作失败！",Toast.LENGTH_SHORT).show();
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

    private void showDeleteDialog(final Person person){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(ListActivity.this);
        normalDialog.setTitle("确认删除");
        normalDialog.setMessage("确认删除人物吗？");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RemoteDbManager.delete(person, new AsyncHttpResponseHandler() {
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
                                db.delete(person);
                                deleteFolderFile(SAVEDIR + person.getName(),true);
                                refreshList();
                            }
                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(ListActivity.this,"操作失败！",Toast.LENGTH_SHORT).show();
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
}
