package com.silenceender.whoru;


import android.net.Uri;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.silenceender.whoru.model.Person;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.silenceender.whoru.utils.ToolHelper.*;

/**
 * Created by Silen on 2017/8/18.
 */

public class GallaryAdapter extends BaseAdapter {
    private Context mContext;
    private Person person;
    private List<Uri> mThumbIds = new ArrayList<Uri>();

    public GallaryAdapter(Context c,Person person) {
        mContext = c;
        this.person = person;
        setPhotos();
    }

    public int getCount() {
        return mThumbIds.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 400));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageURI(mThumbIds.get(position));
        return imageView;
    }

    private void setPhotos() {
        List<String> picnames = stringToList(this.person.getPicnames());
        for(String picname : picnames) {
            if(!picname.equals("")) {
                mThumbIds.add(Uri.fromFile(new File(SAVEDIR + DIVIDE + this.person.getName() + DIVIDE + picname)));
            }
        }
    }
}
