package com.silenceender.whoru;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.alexbbb.uploadservice.UploadService;
import com.alexbbb.uploadservice.UploadServiceBroadcastReceiver;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.silenceender.whoru.model.Person;
import com.silenceender.whoru.model.RemoteDbManager;
import com.silenceender.whoru.utils.JSONResponseHelper;
import com.silenceender.whoru.utils.UploadReceiver;

import cz.msebera.android.httpclient.Header;

import static com.silenceender.whoru.utils.ToolHelper.DIVIDE;
import static com.silenceender.whoru.utils.ToolHelper.SAVEDIR;
import static com.silenceender.whoru.utils.ToolHelper.TEMPDIR;
import static com.silenceender.whoru.utils.ToolHelper.deleteFolderFile;
import static com.silenceender.whoru.utils.ToolHelper.uploadMultipart;

public class ResultActivity extends Activity implements UploadReceiver.UploadResultListener{

    private ImageView imgShow;
    private EditText mEditText;
    private UploadServiceBroadcastReceiver uploadReceiver;
    private static String imgPath;
    private static String picName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_result);
        WindowManager m = this.getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        try {
            params.width = (int) ((d.getWidth()) * 0.8);
            params.height = (int) ((d.getHeight()) * 0.6);
        }
        catch (Exception e){}
        this.getWindow().setAttributes(params);

        UploadService.NAMESPACE = "com.silenceender.whoru";
        uploadReceiver = new UploadReceiver(this,null);

        Intent intent = getIntent();
        imgPath = intent.getStringExtra("imgPath");
        String[] splitedPath = imgPath.split(DIVIDE);
        picName = splitedPath[splitedPath.length-1];
        mEditText = (EditText)findViewById(R.id.editTextResult);
        mEditText.setText("");
        mEditText.setKeyListener(null);
        imgShow= (ImageView) findViewById(R.id.imgShow);
        showImg();
        getResult();
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

    private void showImg(){
        BitmapFactory.Options option=new BitmapFactory.Options();
        option.inSampleSize=2;
        Bitmap bitmap=BitmapFactory.decodeFile(imgPath,option);
        Bitmap b;
        try{
            b = toRoundCorner(bitmap, 10);
        }
        catch (Exception e) {
            b =bitmap;
        }
        imgShow.setImageBitmap(b);
    }

    private void getResult() {
        mEditText.setText("上传图片中...");
        uploadMultipart(this,this,imgPath,new Person("UNKNOWN"),null);
    }

    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    @Override
    public void onUploadSuccess(String msg) {
        RemoteDbManager.ask(picName, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                mEditText.setText("让我想想你是谁(￣▽￣)...");
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                JSONResponseHelper response = new JSONResponseHelper(responseBody);
                if(response.getStatus() == 1){
                    mEditText.setText("认出来了！你是" + response.getMsg().split(",")[0] + ",确信度" + response.getMsg().split(",")[1] + "(๑•̀ㅂ•́)و✧");
                } else if(response.getStatus() == 0) {
                    mEditText.setText("我好像不认识你欸(＞﹏＜)");
                } else {
                    mEditText.setText("服务器响应出错： "+ new String(responseBody));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                mEditText.setText("请检查网络连接！");
            }
        });
        deleteFolderFile(SAVEDIR + TEMPDIR + DIVIDE + picName,true);
        //Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUploadFailed(String msg) {
        deleteFolderFile(SAVEDIR + TEMPDIR + DIVIDE + picName,true);
        mEditText.setText("请检查网络连接！");
    }
}
