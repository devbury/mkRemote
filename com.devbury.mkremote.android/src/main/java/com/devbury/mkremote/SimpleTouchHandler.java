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

import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.connections.ServerConnection;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.RelativeLayout.LayoutParams;

public class SimpleTouchHandler implements OnSharedPreferenceChangeListener, OnTouchListener, OnGestureListener {

    private static final int CLICK_DELAY = 175;
    private static String leftMouseClick = MkRemotePreferences.cs(R.string.c_left_mouse_click);
    private static String touchScreenOn = MkRemotePreferences.cs(R.string.c_touchscreen_on);
    private static String mouseSensitivity = MkRemotePreferences.cs(R.string.c_mouse_sensitivity);
    private static String touchClickOn = MkRemotePreferences.cs(R.string.c_touch_click_on);
    private static String longPressScrollOn = MkRemotePreferences.cs(R.string.c_long_press_scroll_on);
    private ServerConnection serverConnection;
    private KeyProcessor keyProcessor;
    private IMultiTouchHandler multiTouchHandler;
    private WindowManager windowManager;
    private DataPacket touchPacket = new DataPacket(DataPacket.MOUSE_MOVE);
    private int lastX;
    private int lastY;
    private int multiplier = 1;
    private long downTime;
    private boolean clickEnabled = true;
    private boolean touchScreenActive = true;
    private boolean scrollActive = false;
    private boolean longPressEnabled = false;
    private GestureDetector gestureDetector;
    private float startY;
    private View view = null;

    public SimpleTouchHandler(SharedPreferences sp) {
        sp.registerOnSharedPreferenceChangeListener(this);
        init(sp);
    }

    public void init(SharedPreferences sp) {
        onSharedPreferenceChanged(sp, mouseSensitivity);
        onSharedPreferenceChanged(sp, touchScreenOn);
        onSharedPreferenceChanged(sp, touchClickOn);
        onSharedPreferenceChanged(sp, longPressScrollOn);
        gestureDetector = new GestureDetector(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals(mouseSensitivity)) {
            multiplier = Integer.valueOf(sp.getString(key, "1"));
            return;
        }
        if (key.equals(touchScreenOn)) {
            touchScreenActive = sp.getBoolean(key, true);
            return;
        }
        if (key.equals(touchClickOn)) {
            clickEnabled = sp.getBoolean(key, true);
            return;
        }
        if (key.equals(longPressScrollOn)) {
            longPressEnabled = sp.getBoolean(key, false);
            return;
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {
        scrollActive = true;
        startY = e.getY();
        View ball = view.findViewById(R.id.scroll_ball);
        int center_x = ball.getWidth() / 2;
        int center_y = ball.getHeight() / 2;
        LayoutParams lp = (LayoutParams) ball.getLayoutParams();
        lp.topMargin = (int) e.getY() - center_y;
        lp.leftMargin = ((int) (windowManager.getDefaultDisplay().getWidth() / 2) - center_x);
        ball.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        view = v;
        if (serverConnection.isConnected()) {
            //multiTouchHandler.processEvent(event);
            if (longPressEnabled && !gestureDetector.onTouchEvent(event)) {
                if (scrollActive) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        int diff = (int) (event.getY() - startY);
                        if (diff < 0) {
                            serverConnection.writeObject(new DataPacket(DataPacket.SCROLL_UP));
                        } else if (diff > 0) {
                            serverConnection.writeObject(new DataPacket(DataPacket.SCROLL_DOWN));
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        scrollActive = false;
                        view.findViewById(R.id.scroll_ball).setVisibility(View.GONE);
                    }
                } else {
                    return processEvent(event);
                }
            } else {
                return processEvent(event);
            }
        }
        return true;
    }

    public boolean processEvent(MotionEvent event) {
        if (touchScreenActive) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    int hsize = event.getHistorySize();
                    int new_x;
                    int new_y;
                    for (int i = 0; i < hsize; i++) {
                        new_x = (int) event.getHistoricalX(i);
                        new_y = (int) event.getHistoricalY(i);
                        move(new_x - lastX, new_y - lastY);
                        lastX = new_x;
                        lastY = new_y;
                    }
                    new_x = (int) event.getX();
                    new_y = (int) event.getY();
                    move(new_x - lastX, new_y - lastY);
                    lastX = (int) event.getX();
                    lastY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getX();
                    lastY = (int) event.getY();
                    downTime = now();
                    break;
                case MotionEvent.ACTION_UP:
                    if (clickEnabled && ((now() - downTime) <= CLICK_DELAY)) {
                        keyProcessor.processKey(leftMouseClick);
                    }
                    break;
            }
        }
        return true;
    }

    private void move(int x, int y) {
        touchPacket.setDx(x * multiplier);
        touchPacket.setDy(y * multiplier);
        serverConnection.writeObject(touchPacket);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    protected long now() {
        return System.currentTimeMillis();
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public KeyProcessor getKeyProcessor() {
        return keyProcessor;
    }

    public void setKeyProcessor(KeyProcessor keyProcessor) {
        this.keyProcessor = keyProcessor;
    }

    public IMultiTouchHandler getMultiTouchHandler() {
        return multiTouchHandler;
    }

    public void setMultiTouchHandler(IMultiTouchHandler multiTouchHandler) {
        this.multiTouchHandler = multiTouchHandler;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }
}