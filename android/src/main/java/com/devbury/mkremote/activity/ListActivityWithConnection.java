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

import com.devbury.android.DialogUtils;
import com.devbury.logging.Logger;
import com.devbury.mkremote.MkRemoteApplication;
import com.devbury.mkremote.MkRemotePreferences;
import com.devbury.mkremote.R;
import com.devbury.mkremote.api.PasswordStorageService;
import com.devbury.mkremote.api.ServiceInfo;
import com.devbury.mkremote.connections.ServerConnection;
import com.devbury.mkremote.connections.ServerConnectionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.PowerManager;

public abstract class ListActivityWithConnection extends ListActivity implements OnSharedPreferenceChangeListener {

    public static final int CONNECTION_ERROR_DIALOG = 1;
    protected PasswordStorageService passwordStorageService;
    protected MkRemoteApplication app;
    protected ServerConnection serverConnection;
    protected SharedPreferences sharedPreferences;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(MkRemotePreferences.cs(R.string.c_notification_bar_on))) {
            app.windowAttributeHelper.processNotificationBar(getWindow(), sharedPreferences.getBoolean(key, false));
        }
    }

    protected void configureDependencies() {
        app = (MkRemoteApplication) getApplication();
        passwordStorageService = app.passwordStorageService;
        sharedPreferences = app.sharedPreferences;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.debug("Unregistering");
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureDependencies();
        onSharedPreferenceChanged(sharedPreferences, MkRemotePreferences.cs(R.string.c_notification_bar_on));
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectToServer();
        acquireWakeLock();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseWakeLock();
        app.serverConnectionFactory.close(serverConnection);
    }

    protected void freshShowDialog(int id) {
        DialogUtils.showDialog(this, id);
    }

    protected void acquireWakeLock() {
        if (sharedPreferences.getBoolean(MkRemotePreferences.cs(R.string.c_wake_lock_on), false)) {
            if (wakeLock == null) {
                try {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "mkRemote");
                } catch (Throwable t) {
                }
            }
            if (wakeLock != null) {
                wakeLock.acquire();
            }
        }
    }

    protected void releaseWakeLock() {
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d = null;
        if (id == CONNECTION_ERROR_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.connection_error);
            builder.setMessage(R.string.connection_error_message);
            final Activity hold = this;
            builder.setPositiveButton(R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    hold.finish();
                }
            });
            d = builder.create();
        }
        return d;
    }

    protected void serverConnectionErrorHandler() {
        freshShowDialog(CONNECTION_ERROR_DIALOG);
        serverConnection = null;
        app.serviceInfo = null;
    }

    protected void connectToServer() {
        // connect to ServiceInfo
        ServiceInfo service_info = app.serviceInfo;
        if (service_info != null) {
            serverConnection = null;
            try {
                serverConnection = app.serverConnectionFactory.getConnection(service_info, passwordStorageService
                        .getPassword(service_info.getId()));
            } catch (ServerConnectionException e) {
                serverConnectionErrorHandler();
            }
        }
    }
}
