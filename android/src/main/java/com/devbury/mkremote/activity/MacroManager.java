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

import com.devbury.mkremote.R;
import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.MacroListResponsePacket;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MacroManager extends ListActivityWithConnection {

    private static final int CONFIRM_DELETE = 201;
    private ProgressDialog progressDialog;
    private boolean delete = false;
    private String currentName;

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
        populateList();
        delete = false;
    }

    protected void populateList() {
        progressDialog.show();
        new MacroThread(this).start();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String macro_name = (String) l.getItemAtPosition(position);
        currentName = macro_name;
        if (!macro_name.equals(getText(R.string.no_macros))) {
            if (delete) {
                freshShowDialog(CONFIRM_DELETE);
            } else {
                DataPacket dp = new DataPacket(DataPacket.MACRO_EXECUTE);
                dp.setS(macro_name);
                serverConnection.writeObject(dp);
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.macromanager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_macro:
                serverConnection.writeObject(new DataPacket(DataPacket.MACRO_START));
                serverConnection.setMacroMode(true);
                finish();
                break;
            case R.id.delete_macro:
                delete = true;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d = super.onCreateDialog(id);
        if (id == CONFIRM_DELETE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.confirm_delete);
            builder.setMessage(R.string.confirm_delete_message);
            builder.setPositiveButton(R.string.yes, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    delete = false;
                    DataPacket dp = new DataPacket(DataPacket.MACRO_DELETE);
                    dp.setS(currentName);
                    serverConnection.writeObject(dp);
                    dialog.dismiss();
                    populateList();
                }
            });
            builder.setNegativeButton(R.string.no, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    delete = false;
                    dialog.dismiss();
                }
            });
            d = builder.create();
        }
        return d;
    }

    public class MacroThread extends Thread {
        private Handler handler;
        private MacroListResponsePacket result;
        private Context context;

        public MacroThread(Context c) {
            handler = new Handler();
            context = c;
        }

        @Override
        public void run() {
            result = serverConnection.writeObject(new DataPacket(DataPacket.MACRO_LIST), MacroListResponsePacket.class);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (result.getMacroNames().size() == 0) {
                        result.getMacroNames().add(getText(R.string.no_macros).toString());
                    }
                    setListAdapter(new ArrayAdapter(context, android.R.layout.simple_list_item_1, result
                            .getMacroNames()) {
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView name = new TextView(context);
                            name.setTypeface(Typeface.DEFAULT_BOLD);
                            name.setTextSize((float) 20.0);
                            name.setText(result.getMacroNames().get(position));
                            return name;
                        }
                    });
                    progressDialog.dismiss();
                }
            });
        }
    }
}
