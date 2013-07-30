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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.connections.ServerConnection;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.MotionEvent;

public class TrackBallClickHandler implements MotionEventHandler, OnSharedPreferenceChangeListener {

    private Logger logger = LoggerFactory.getLogger(TrackBallClickHandler.class);
    private String trackballKey = MkRemotePreferences.cs(R.string.c_trackball_key);
    private String trackballOnKey = MkRemotePreferences.cs(R.string.c_trackball_on);
    private String trackballWheelOnKey = MkRemotePreferences.cs(R.string.c_trackball_wheel_on);
    private String trackballUpDownOnKey = MkRemotePreferences.cs(R.string.c_trackball_scroll_up_down_on);
    private String trackballLeftRightOnKey = MkRemotePreferences.cs(R.string.c_trackball_scroll_left_right_on);
    private MotionEventHandler handlerDelegate;
    private KeyProcessor keyProcessor;
    private String trackballKeyValue;
    private SimpleMoveTrackballHandler simpleMoveTrackballHandler;
    private MouseWheelTrackballHandler mouseWheelTrackballHandler;
    private ScrollTrackballHandler scrollTrackballHandler;
    private boolean move = false;
    private boolean wheel = false;
    private boolean scrollUp = false;
    private boolean scrollLeft = false;

    public TrackBallClickHandler(SharedPreferences sp) {
        simpleMoveTrackballHandler = new SimpleMoveTrackballHandler(sp);
        mouseWheelTrackballHandler = new MouseWheelTrackballHandler();
        scrollTrackballHandler = new ScrollTrackballHandler();

        sp.registerOnSharedPreferenceChangeListener(this);
        init(sp);
    }

    public void init(SharedPreferences sp) {
        onSharedPreferenceChanged(sp, trackballKey);
        onSharedPreferenceChanged(sp, trackballOnKey);
        onSharedPreferenceChanged(sp, trackballWheelOnKey);
        onSharedPreferenceChanged(sp, trackballUpDownOnKey);
        onSharedPreferenceChanged(sp, trackballLeftRightOnKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(trackballKey)) {
            trackballKeyValue = sharedPreferences.getString(key, null);
            return;
        }
        if (key.equals(trackballOnKey)) {
            if (sharedPreferences.getBoolean(key, MkRemotePreferences.DEFAULT_TRACKBALL_ON)) {
                handlerDelegate = simpleMoveTrackballHandler;
                move = true;
            } else {
                move = false;
                verifyHandler();
            }
            return;
        }
        if (key.equals(trackballWheelOnKey)) {
            if (sharedPreferences.getBoolean(key, false)) {
                handlerDelegate = mouseWheelTrackballHandler;
                wheel = true;
            } else {
                wheel = false;
                verifyHandler();
            }
            return;
        }
        if (key.equals(trackballUpDownOnKey)) {
            if (sharedPreferences.getBoolean(key, false)) {
                handlerDelegate = scrollTrackballHandler;
                scrollUp = true;
                scrollTrackballHandler.setUpDown(true);
            } else {
                scrollUp = false;
                scrollTrackballHandler.setUpDown(false);
                verifyHandler();
            }
            return;
        }
        if (key.equals(trackballLeftRightOnKey)) {
            if (sharedPreferences.getBoolean(key, false)) {
                handlerDelegate = scrollTrackballHandler;
                scrollLeft = true;
                scrollTrackballHandler.setLeftRight(true);
            } else {
                scrollLeft = false;
                scrollTrackballHandler.setLeftRight(false);
                verifyHandler();
            }
            return;
        }
    }

    protected void verifyHandler() {
        if (move || wheel || scrollUp || scrollLeft) {
            return;
        }
        handlerDelegate = new MotionEventHandler() {

            @Override
            public boolean processEvent(MotionEvent event) {
                return true;
            }
        };
    }

    @Override
    public boolean processEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return keyProcessor.processKey(trackballKeyValue);
        }
        return handlerDelegate.processEvent(event);
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public MotionEventHandler getHandlerDelegate() {
        return handlerDelegate;
    }

    public void setHandlerDelegate(MotionEventHandler handlerDelegate) {
        this.handlerDelegate = handlerDelegate;
    }

    public KeyProcessor getKeyProcessor() {
        return keyProcessor;
    }

    public void setKeyProcessor(KeyProcessor keyProcessor) {
        this.keyProcessor = keyProcessor;
    }

    public void setServerConnection(ServerConnection sc) {
        simpleMoveTrackballHandler.setServerConnection(sc);
        mouseWheelTrackballHandler.setServerConnection(sc);
        scrollTrackballHandler.setServerConnection(sc);
    }
}
