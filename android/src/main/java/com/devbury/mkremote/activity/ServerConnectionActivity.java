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
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.Toast;

public abstract class ServerConnectionActivity extends Activity implements ServerConnection {

    public static final String LAST_HOST_IP = "lastHostIp";
    public static final String LAST_HOST_PORT = "lastHostPort";
    public static final String LAST_HOST_ID = "lastHostId";
    public static final int CONNECTION_ERROR_DIALOG = 1;
    protected MkRemoteApplication app;
    protected SharedPreferences sharedPreferences;
    protected PasswordStorageService passwordStorageService;
    protected Handler uiHandler;
    private ServerConnection serverConnection;
    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;

    protected void configureDependencies() {
        uiHandler = new Handler();
        app = (MkRemoteApplication) getApplication();

        passwordStorageService = app.passwordStorageService;

        sharedPreferences = app.sharedPreferences;
        try {
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiLock = wm.createWifiLock("mkRemote");
            if (wifiLock != null) {
                wifiLock.acquire();
            }
        } catch (Throwable t) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureDependencies();
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

    @Override
    protected void onDestroy() {
        if (wifiLock != null) {
            wifiLock.release();
        }
        super.onDestroy();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d = null;
        if (id == CONNECTION_ERROR_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.connection_error);
            builder.setMessage(R.string.connection_error_message);
            builder.setPositiveButton(R.string.ok, null);
            d = builder.create();
        }
        return d;
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

    public void serverConnectionErrorHandler() {
        freshShowDialog(CONNECTION_ERROR_DIALOG);
        serverConnection = null;
        app.serviceInfo = null;
    }

    protected void connectToServer() {
        // connect to ServiceInfo
        ServiceInfo service_info = app.serviceInfo;
        boolean auto_connecting = false;
        if (service_info == null
                && sharedPreferences.getBoolean(MkRemotePreferences.cs(R.string.c_auto_connect_on),
                MkRemotePreferences.DEFAULT_AUTO_CONNECT_ON)) {
            String last_ip = sharedPreferences.getString(LAST_HOST_IP, null);
            int last_port = sharedPreferences.getInt(LAST_HOST_PORT, -1);
            String last_id = sharedPreferences.getString(LAST_HOST_ID, null);
            if (last_ip != null && last_port != (-1) && last_id != null) {
                auto_connecting = true;
                service_info = new ServiceInfo();
                service_info.setId(last_id);
                service_info.setAddress(last_ip);
                service_info.setName("com.devbury.mkRemote");
                service_info.setPort(last_port);
            }
        }

        if (service_info != null) {
            serverConnection = null;
            try {
                serverConnection = app.serverConnectionFactory.getConnection(service_info, passwordStorageService
                        .getPassword(service_info.getId()));
                app.serviceInfo = service_info;
                if (auto_connecting) {
                    Toast.makeText(this, R.string.auto_connected_to_host, Toast.LENGTH_LONG).show();
                }
            } catch (ServerConnectionException e) {
                if (auto_connecting) {
                    Toast.makeText(this, R.string.auto_connect_failed, Toast.LENGTH_LONG).show();
                    sharedPreferences.edit().remove(LAST_HOST_ID).remove(LAST_HOST_IP).remove(LAST_HOST_PORT).commit();
                } else {
                    serverConnectionErrorHandler();
                }
            }
        }
    }

    @Override
    public boolean isMacroMode() {
        if (serverConnection == null) {
            return false;
        }
        return serverConnection.isMacroMode();
    }

    @Override
    public void setMacroMode(boolean m) {
        serverConnection.setMacroMode(m);
    }

    @Override
    public boolean isConnected() {
        if (serverConnection == null) {
            return false;
        }
        return serverConnection.isConnected();
    }

    @Override
    public String getId() {
        if (serverConnection == null) {
            return "";
        }
        return serverConnection.getId();
    }

    @Override
    public <T> T writeObject(Object object, Class<T> clazz) {
        try {
            return serverConnection.writeObject(object, clazz);
        } catch (Throwable t) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    serverConnectionErrorHandler();
                }
            });
        }
        return null;
    }

    @Override
    public <T> T readObject(Class<T> clazz) {
        try {
            return serverConnection.readObject(clazz);
        } catch (Throwable t) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    serverConnectionErrorHandler();
                }
            });
        }
        return null;
    }

    @Override
    public void writeObject(Object object) {
        try {
            serverConnection.writeObject(object);
        } catch (Throwable t) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    serverConnectionErrorHandler();
                }
            });
        }
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }
}
