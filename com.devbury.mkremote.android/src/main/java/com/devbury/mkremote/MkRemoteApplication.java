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

package com.devbury.mkremote;

import com.devbury.android.EmailUncaughtExceptionHandler;
import com.devbury.logging.Logger;
import com.devbury.mkremote.api.GsonJsonService;
import com.devbury.mkremote.api.JsonService;
import com.devbury.mkremote.api.MacroListResponsePacket;
import com.devbury.mkremote.api.MacroListResponsePacketDeserializer;
import com.devbury.mkremote.api.PasswordStorageService;
import com.devbury.mkremote.api.QuickLaunchListResponsePacket;
import com.devbury.mkremote.api.QuickLaunchListResponsePacketDeserializer;
import com.devbury.mkremote.api.ServiceInfo;
import com.devbury.mkremote.connections.ChainServerConnectionFactory;
import com.devbury.mkremote.connections.DiscoveryService;
import com.devbury.mkremote.connections.HistoryDiscoveryService;
import com.devbury.mkremote.connections.MergeDiscoveryService;
import com.devbury.mkremote.connections.MulticastDiscoveryService;
import com.devbury.mkremote.connections.NativeBluetoothDiscoveryService;
import com.devbury.mkremote.connections.NativeBluetoothServerConnectionFactory;
import com.devbury.mkremote.connections.ServerConnectionFactory;
import com.devbury.mkremote.connections.SocketServerConnectionFactory;
import com.google.gson.GsonBuilder;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.MotionEvent;

public class MkRemoteApplication extends Application {

    public static final int BLUETOOTH_NONE = 0;
    public static final int BLUETOOTH_EXPERIMENTAL = 1;
    public static final int BLUETOOTH_NATIVE = 2;
    public static final String SERVICE_INFO = "service_info";
    public SharedPreferences sharedPreferences;
    public PasswordStorageService passwordStorageService;
    public JsonService jsonService;
    public ServiceInfo serviceInfo;
    public ServerConnectionFactory serverConnectionFactory;
    public HistoryDiscoveryService historyDiscoveryService;
    public DiscoveryService discoveryService;
    public int bluetoothType = BLUETOOTH_NONE;
    public EmailUncaughtExceptionHandler handler;
    public IMultiTouchHandler multiTouchHandler;
    public IWindowAttributeHelper windowAttributeHelper;
    public boolean multiTouchSupported = false;
    public int minServerVersion = 0;
    public int sdk;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init("mkRemote");
        handler = new EmailUncaughtExceptionHandler(this, Thread.getDefaultUncaughtExceptionHandler());
        Thread.setDefaultUncaughtExceptionHandler(handler);

        sdk = Integer.parseInt(Build.VERSION.SDK);
        Logger.debug("Android SDK " + sdk);

        try {
            minServerVersion = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getInt("MIN_SERVER_VERSION");
        } catch (NameNotFoundException e) {
        }
        MkRemotePreferences.setContext(this);
        MkRemotePreferences.initDefaults();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferencesPasswordStorageService pass = new SharedPreferencesPasswordStorageService();
        pass.setSharedPreferences(sharedPreferences);
        passwordStorageService = pass;

        GsonBuilder b = new GsonBuilder();
        b.registerTypeAdapter(MacroListResponsePacket.class, new MacroListResponsePacketDeserializer());
        b.registerTypeAdapter(QuickLaunchListResponsePacket.class, new QuickLaunchListResponsePacketDeserializer());
        jsonService = new GsonJsonService(b.create());
        ChainServerConnectionFactory chain = new ChainServerConnectionFactory();
        SocketServerConnectionFactory scf = new SocketServerConnectionFactory();
        scf.setJsonService(jsonService);
        chain.addServerConnectionFactory(scf);
        if (!isLite()) {
            NativeBluetoothServerConnectionFactory nf = new NativeBluetoothServerConnectionFactory();
            nf.setJsonService(jsonService);
            chain.addServerConnectionFactory(nf);
        }
        serverConnectionFactory = chain;
        serverConnectionFactory.init();

        MergeDiscoveryService mds = new MergeDiscoveryService();
        if (sdk == 3) {
            historyDiscoveryService = new HistoryDiscoveryService();
            historyDiscoveryService.setApp(this);
            historyDiscoveryService.setSharedPreferences(sharedPreferences);
            mds.addDiscoveryService(historyDiscoveryService);
        } else {
            MulticastDiscoveryService multi = new MulticastDiscoveryService();
            multi.setJsonService(jsonService);
            mds.addDiscoveryService(multi);
        }
        NativeBluetoothDiscoveryService native_service = new NativeBluetoothDiscoveryService();
        native_service.setSharedPreferences(sharedPreferences);
        native_service.setJsonService(jsonService);
        mds.addDiscoveryService(native_service);
        bluetoothType = BLUETOOTH_NATIVE;
        Logger.debug("Using native bluetooth");

        discoveryService = mds;

        try {
            MultiTouchHandler h = new MultiTouchHandler();
            multiTouchHandler = h;
            Logger.debug("Using multi-touch");
        } catch (Throwable t) {
            multiTouchHandler = new IMultiTouchHandler() {
                @Override
                public boolean processEvent(MotionEvent event) {
                    return false;
                }
            };
            Logger.debug("No multi-touch");
        }

        if (sdk >= 5) {
            windowAttributeHelper = new Android20WindowAttributeHelper();
            Logger.debug("Android 2.0+ Window Helper");
        } else if (sdk == 4) {
            windowAttributeHelper = new Android16WindowAttributeHelper();
            Logger.debug("Android 1.6 Window Helper");
        } else {
            windowAttributeHelper = new Android15WindowAttributeHelper();
            Logger.debug("Android 1.5 Window Helper");
        }
    }

    @Override
    public void onTerminate() {
        serverConnectionFactory.destroy();
    }

    public boolean hasError() {
        return handler.hasError();
    }

    public void clearError() {
        handler.clearError();
    }

    public void sendErrorEmail(Context c) {
        handler.sendErrorMail(c);
    }

    public boolean isLite() {
        return !"com.devbury.mkremote".equals(getPackageName());
    }

    public int getDefaultOrientation() {
        if (sharedPreferences.getString(MkRemotePreferences.cs(R.string.c_default_orientation), null).equals(
                MkRemotePreferences.cs(R.string.c_portrait))) {
            return Configuration.ORIENTATION_PORTRAIT;
        }
        return Configuration.ORIENTATION_LANDSCAPE;
    }

    public int getDefaultScreenOrientation() {
        if (getDefaultOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }
}
