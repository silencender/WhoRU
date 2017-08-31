package com.silenceender.whoru.preferences;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;

import com.silenceender.whoru.R;
import com.silenceender.whoru.utils.CompressImageUtil;
import com.silenceender.whoru.utils.FaceUtil;
import com.silenceender.whoru.utils.ToolHelper;

/**
 * Created by Silen on 2017/8/31.
 */

public class MyPreferencesActivity extends PreferenceActivity {

    private static final String TAG = "Preference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ac = getActionBar();
        ac.setDisplayHomeAsUpEnabled(true);  //激活返回键
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            Preference compress = findPreference("compressSize");
            compress.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.i(TAG,newValue.toString());
                    return CompressImageUtil.setCompressSize(Integer.parseInt(newValue.toString()));
                }
            });
            Preference face = findPreference("faceSize");
            face.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.i(TAG,newValue.toString());
                    return FaceUtil.setFaceSize(Integer.parseInt(newValue.toString()));
                }
            });
            final ListPreference faceMode = (ListPreference)findPreference("faceMode");
            faceMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String mode = faceMode.getEntry().toString();
                    Log.i(TAG,mode);
                    return FaceUtil.setMode(mode);
                }
            });
            Preference server = findPreference("server");
            server.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.i(TAG,newValue.toString());
                    return ToolHelper.setServer(newValue.toString());
                }
            });
        }
    }
}

