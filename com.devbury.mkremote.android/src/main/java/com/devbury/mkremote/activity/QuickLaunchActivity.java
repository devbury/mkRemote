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

import java.io.File;

import com.devbury.mkremote.R;
import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.LaunchItem;
import com.devbury.mkremote.api.QuickLaunchListResponsePacket;
import com.devbury.mkremote.base64.Base64;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class QuickLaunchActivity extends ListActivityWithConnection {

    public static final int NO_FILES_DIALOG = 201;
    private String lastDir = "/";
    private ProgressDialog progressDialog;

    protected void configureDependencies() {
        super.configureDependencies();
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
        progressDialog.setMessage(getResources().getText(R.string.working));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateList("/");
    }

    protected void populateList(String dir) {
        if (progressDialog.isShowing()) {
            return;
        }
        progressDialog.show();
        new QuickLaunchThread(this, dir).start();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        LaunchItem li = (LaunchItem) l.getItemAtPosition(position);
        String file = li.getPath() + "/" + li.getName();
        if (li.getType() == LaunchItem.DIR_TYPE) {
            populateList(file);
        } else {
            DataPacket dp = new DataPacket(DataPacket.QUICK_LAUNCH);
            dp.setS(file);
            serverConnection.writeObject(dp);
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && !"/".equals(lastDir)) {
            File p = new File(lastDir);
            populateList(p.getParent());
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d = super.onCreateDialog(id);
        if (id == NO_FILES_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_files);
            builder.setMessage(R.string.no_files_message);
            final Activity hold = this;
            builder.setPositiveButton(R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if ("/".equals(lastDir)) {
                        hold.finish();
                    }
                }
            });
            d = builder.create();
        }
        return d;
    }

    public class QuickLaunchThread extends Thread {
        private Handler handler;
        private QuickLaunchListResponsePacket result;
        private String dir;
        private Context context;

        public QuickLaunchThread(Context c, String d) {
            handler = new Handler();
            dir = d;
            context = c;
        }

        @Override
        public void run() {
            lastDir = dir;
            DataPacket dp = new DataPacket(DataPacket.QUICK_LAUNCH_LIST);
            dp.setS(dir);
            result = serverConnection.writeObject(dp, QuickLaunchListResponsePacket.class);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (result.getItems().size() == 0) {
                        progressDialog.dismiss();
                        freshShowDialog(NO_FILES_DIALOG);
                    } else {
                        setListAdapter(new ArrayAdapter<LaunchItem>(context, android.R.layout.simple_list_item_1,
                                result.getItems()) {
                            private Base64 base64 = new Base64();

                            public View getView(int position, View convertView, ViewGroup parent) {
                                LaunchItem li = (LaunchItem) getItem(position);
                                LinearLayout ll = new LinearLayout(context);
                                ll.setOrientation(LinearLayout.HORIZONTAL);
                                ImageView iv = new ImageView(context);
                                byte[] image_array = base64.decode(li.getIcon());
                                iv.setImageBitmap(BitmapFactory.decodeByteArray(image_array, 0, image_array.length));
                                iv.setMinimumHeight(32);
                                iv.setMinimumWidth(32);
                                iv.setMaxHeight(32);
                                iv.setMinimumWidth(32);
                                iv.setMaxWidth(32);
                                ll.addView(iv);
                                TextView name = new TextView(context);
                                name.setTypeface(Typeface.DEFAULT_BOLD);
                                name.setTextSize((float) 20.0);
                                name.setText(li.getName());
                                ll.addView(name);
                                return ll;
                            }
                        });
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }
}
