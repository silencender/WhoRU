package com.silenceender.whoru.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;


public class CompressImageUtil {

	private String imgPath;
	private Handler mhHandler;
	private static int picSize = 1000;

	public static boolean setCompressSize(int size) {
		if(size > 100) {
			picSize = size;
		}
		return true;
	}

	private void compressImageByQuality(final Bitmap bitmap, final String imgPath) {
		if (bitmap == null) {
			sendMsg(false, "像素压缩失败");
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int options = 100;
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
				while (baos.toByteArray().length / 1024 > picSize) {
					baos.reset();
					options -= 5;
					if (options < 0) options = 0;
					bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
					if (options == 0) break;
				}
//				if(bitmap!=null&&!bitmap.isRecycled()){
//					bitmap.recycle();
//				}
				try {
					FileOutputStream fos = new FileOutputStream(new File(imgPath));
					fos.write(baos.toByteArray());
					fos.flush();
					fos.close();
					sendMsg(true, imgPath);
				} catch (Exception e) {
					sendMsg(false, "质量压缩失败");
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void compressImageByPixel(String imgPath) {
		Bitmap bitmap=null;
		if(imgPath==null){
			sendMsg(false,"要压缩的文件不存在");
			return;
		}
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
		newOpts.inJustDecodeBounds = false;
		int width = newOpts.outWidth;
		int height = newOpts.outHeight;
		float maxSize =1200f;//默认1200px
		int be = 1;
		if (width > height && width > maxSize) {
			be = (int) (newOpts.outWidth / maxSize);
			be++;
		} else if (width < height && height > maxSize) {
			be = (int) (newOpts.outHeight / maxSize);
			be++;
		}
		newOpts.inSampleSize =be;//设置采样率
		newOpts.inPreferredConfig = Config.ARGB_8888;
		newOpts.inPurgeable = true;
		newOpts.inInputShareable = true;
		bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
		compressImageByQuality(bitmap,imgPath);
	}

	public void compressImageByPixel(String imgPath , Activity activity, CompressListener listener) {
		this.imgPath=imgPath;
		this.mhHandler = new MyHandler(activity,listener);
		File file=new File(imgPath);
		if (file==null||!file.exists()||!file.isFile()){
			sendMsg(false,"要压缩的文件不存在");
			return;
		}
		this.compressImageByPixel(imgPath);
	}

	private void sendMsg(boolean isSuccess,String obj){
		Message msg=new Message();
		msg.obj=obj;
		msg.what=isSuccess?1:0;
		mhHandler.sendMessage(msg);
	}

	private static class MyHandler extends Handler {
		private final WeakReference<Activity> mActivity;
		private final CompressListener listener;

		MyHandler(Activity activity,CompressListener l) {
			mActivity = new WeakReference<Activity>(activity);
			listener = l;
		}

		@Override
		public void handleMessage(Message msg) {
			Activity activity = mActivity.get();
			if (activity != null) {
				if (msg.what == 1) {//压缩成功
					listener.onCompressSuccessed((String) msg.obj);
				} else if (msg.what == 0) {//压缩失败
					listener.onCompressFailed((String) msg.obj);
				}
			}
		}
	}


	public interface CompressListener{
		void onCompressSuccessed(String imgPath);
		void onCompressFailed(String msg);
	}
}
