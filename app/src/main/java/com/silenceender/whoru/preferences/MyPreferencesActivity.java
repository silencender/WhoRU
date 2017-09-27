package com.silenceender.whoru.preferences;

import android.app.ActionBar;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.silenceender.whoru.MainActivity;
import com.silenceender.whoru.R;
import com.silenceender.whoru.model.RemoteDbManager;
import com.silenceender.whoru.utils.CompressImageUtil;
import com.silenceender.whoru.utils.JSONResponseHelper;
import com.silenceender.whoru.utils.ToolHelper;

import cz.msebera.android.httpclient.Header;

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
            Preference server = findPreference("server");
            server.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.i(TAG,newValue.toString());
                    return ToolHelper.setServer(newValue.toString());
                }
            });
            Preference getCode = findPreference("getCode");
            getCode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ClipboardManager clipboard = (ClipboardManager) MainActivity.mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("code", RemoteDbManager.getDeviceID());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.mainActivity,"引继码复制成功！",Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            final EditTextPreference inherit = (EditTextPreference)findPreference("inherit");
            final Preference cancelInherit = findPreference("cancelInherit");
            inherit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.i(TAG,newValue.toString());
                    final String account = newValue.toString();
                    if(account.equals("")) {
                        cancelInherit.getOnPreferenceClickListener().onPreferenceClick(cancelInherit);
                    } else {
                        RemoteDbManager.inherit(account, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                                JSONResponseHelper response = new JSONResponseHelper(responseBody);
                                if(response.getStatus() == 1) {
                                    MainActivity.setInherit(account);
                                    Toast.makeText(MainActivity.mainActivity,"引继成功！",Toast.LENGTH_SHORT).show();
                                } else{
                                    Toast.makeText(MainActivity.mainActivity,"引继失败！",Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(MainActivity.mainActivity,"引继失败！",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    return true;
                }
            });
            cancelInherit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    inherit.setText("");
                    MainActivity.setInherit("");
                    Toast.makeText(MainActivity.mainActivity,"成功取消引继！",Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}

