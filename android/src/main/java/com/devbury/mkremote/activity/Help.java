/*
 * Copyright (c) 2009-2013 devBury LLC
 *
 *   This file is part of mkRemote.
 *
 *   mkRemote is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License Version 3
 *   as published by the Free Software Foundation.
 *
 *   mkRemote is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with mkRemote.  If not, see <http://www.gnu.org/licenses/gpl.txt/>.
 */

package com.devbury.mkremote.activity;

import com.devbury.logging.Logger;
import com.devbury.mkremote.MkRemoteApplication;
import com.devbury.mkremote.MkRemotePreferences;
import com.devbury.mkremote.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Help extends Activity implements OnSharedPreferenceChangeListener {
    public static final String CHANGELOG = "http://download.devbury.com/mkRemote/CHANGELOG.html";
    public static final String HELP_VIDEOS = "http://download.devbury.com/mkRemote/DOCS.html";
    private static String url = CHANGELOG;
    private boolean showChangelog = true;
    private MkRemoteApplication app;
    private ProgressDialog progressDialog;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(MkRemotePreferences.cs(R.string.c_notification_bar_on))) {
            app.windowAttributeHelper.processNotificationBar(getWindow(), sharedPreferences.getBoolean(key, false));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.debug("Unregistering");
        app.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MkRemoteApplication) getApplication();
        setContentView(R.layout.help);
        onSharedPreferenceChanged(app.sharedPreferences, MkRemotePreferences.cs(R.string.c_notification_bar_on));
        app.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        TextView tv = (TextView) findViewById(R.id.version);
        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
            tv.setText("Version " + info.versionName);
        } catch (NameNotFoundException e) {
            tv.setText("Version Unknown");
        }
        Button b = (Button) findViewById(R.id.toggle);
        b.setText(getToggleText());
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
                ((TextView) v).setText(getToggleText());
            }
        });
        if (app.isLite()) {
            ImageView iv = (ImageView) findViewById(R.id.help_logo);
            iv.setImageResource(R.drawable.logo_lite);
        }

        progressDialog = new ProgressDialog(this) {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                return true;
            }

            @Override
            public boolean onKeyUp(int keyCode, KeyEvent event) {
                return true;
            }
        };
        progressDialog.setMessage(getResources().getText(R.string.loading));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        WebView wv = (WebView) findViewById(R.id.web);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressDialog.dismiss();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressDialog.show();
            }
        });
        OnClickListener c = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                String subject = "mkRemote Help";
                sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@devbury.com"});
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                sendIntent.setType("message/rfc822");
                startActivity(Intent.createChooser(sendIntent, "Title:"));
            }
        };
        findViewById(R.id.devbury).setOnClickListener(c);
        findViewById(R.id.version).setOnClickListener(c);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showUrl();
    }

    protected void showUrl() {
        WebView wv = (WebView) findViewById(R.id.web);
        wv.loadUrl(url);
    }

    protected void toggle() {
        showChangelog = !showChangelog;
        if (showChangelog) {
            url = CHANGELOG;
        } else {
            url = HELP_VIDEOS;
        }
        showUrl();
    }

    protected int getToggleText() {
        if (showChangelog) {
            return R.string.show_videos;
        }
        return R.string.show_changelog;
    }
}
