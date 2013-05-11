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

import com.devbury.mkremote.api.BoxeePercentageResponsePacket;
import com.devbury.mkremote.api.DataPacket;
import com.devbury.mkremote.api.VirtualKeyCode;
import com.devbury.mkremote.connections.ServerConnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.Toast;

public class CustomKeyProcessor implements KeyProcessor {

    private ServerConnection serverConnection;
    private MouseButtonStatus mouseButtonStatus = new MouseButtonStatus();
    private DataPacket leftMouseDown = new DataPacket(DataPacket.LEFT_BUTTON_DOWN);
    private DataPacket leftMouseUp = new DataPacket(DataPacket.LEFT_BUTTON_UP);
    private DataPacket doubleClick = new DataPacket(DataPacket.DOUBLE_CLICK);
    private DataPacket rightMouseDown = new DataPacket(DataPacket.RIGHT_BUTTON_DOWN);
    private DataPacket rightMouseUp = new DataPacket(DataPacket.RIGHT_BUTTON_UP);
    private DataPacket middleMouseDown = new DataPacket(DataPacket.MIDDLE_BUTTON_DOWN);
    private DataPacket middleMouseUp = new DataPacket(DataPacket.MIDDLE_BUTTON_UP);
    private ClickFeedbackService clickFeedbackService;
    private DoubleClickThread doubleClickThread = new DoubleClickThread();
    private VirtualKeyboardManager virtualKeyboardManager;
    private SharedPreferences sharedPreferences;
    private Context context;
    private Toast boxeeVolume = null;
    private MkRemoteApplication app;

    @Override
    public boolean processKey(String setting) {
        if (setting.equals(MkRemotePreferences.cs(R.string.c_left_mouse_click))) {
            if (doubleClickThread.isAlive()) {
                doubleClickThread.abortClick();
                doubleClick();
            } else {
                doubleClickThread = new DoubleClickThread();
                doubleClickThread.start();
            }
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_middle_mouse_click))) {
            middleMouseClick();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_right_mouse_click))) {
            rightMouseClick();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_left_mouse_hold))) {
            leftMouseHold();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_middle_mouse_hold))) {
            middleMouseHold();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_right_mouse_hold))) {
            rightMouseHold();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_up_arrow))) {
            upArrow();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_down_arrow))) {
            downArrow();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_right_arrow))) {
            rightArrow();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_left_arrow))) {
            leftArrow();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_page_up))) {
            pageUp();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_page_down))) {
            pageDown();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_maximize))) {
            maximize();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_show_virtual_keyboard))) {
            virtualKeyboardManager.show();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_f5))) {
            f5();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_esc))) {
            esc();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_enter))) {
            enter();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_scroll_up))) {
            scrollUp();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_scroll_down))) {
            scrollDown();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_toggle_boxee_mode))) {
            toggleBoxee();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_boxee_volume_up))) {
            boxeeVolumeUp();
            return true;
        }
        if (setting.equals(MkRemotePreferences.cs(R.string.c_boxee_volume_down))) {
            boxeeVolumeDown();
            return true;
        }
        return false;
    }

    private void showBoxeeVolume(int volumePercent) {
        if (boxeeVolume == null) {
            boxeeVolume = new Toast(context);
            boxeeVolume.setDuration(1000);
            boxeeVolume.setGravity(Gravity.TOP, 0, 0);
            boxeeVolume.setView(LayoutInflater.from(context).inflate(R.layout.boxee_volume, null));
        }
        ((ProgressBar) (boxeeVolume.getView().findViewById(R.id.boxee_volume_level))).setProgress(volumePercent);
        boxeeVolume.show();
    }

    private void boxeeVolumeUp() {
        BoxeePercentageResponsePacket p = serverConnection.writeObject(new DataPacket(DataPacket.BOXEE_VOLUME_UP),
                BoxeePercentageResponsePacket.class);
        showBoxeeVolume(p.getPercentage());
    }

    private void boxeeVolumeDown() {
        BoxeePercentageResponsePacket p = serverConnection.writeObject(new DataPacket(DataPacket.BOXEE_VOLUME_DOWN),
                BoxeePercentageResponsePacket.class);
        showBoxeeVolume(p.getPercentage());
    }

    private void toggleBoxee() {
        if (app.isLite()) {
            Toast.makeText(context, R.string.only_in_full_version, Toast.LENGTH_LONG).show();
        } else {
            boolean t = sharedPreferences.getBoolean(MkRemotePreferences.cs(R.string.c_boxee_mode_on), false);
            sharedPreferences.edit().putBoolean(MkRemotePreferences.cs(R.string.c_boxee_mode_on), !t).commit();
        }
    }

    private void scrollDown() {
        serverConnection.writeObject(new DataPacket(DataPacket.SCROLL_DOWN));
    }

    private void scrollUp() {
        serverConnection.writeObject(new DataPacket(DataPacket.SCROLL_UP));
    }

    private void enter() {
        DataPacket enter = new DataPacket(DataPacket.VIRTUAL_KEY_PRESS);
        enter.setVirtualKeyCode(VirtualKeyCode.ENTER);
        serverConnection.writeObject(enter);
    }

    private void f5() {
        DataPacket f5 = new DataPacket(DataPacket.VIRTUAL_KEY_PRESS);
        f5.setVirtualKeyCode(VirtualKeyCode.F5);
        serverConnection.writeObject(f5);
    }

    private void esc() {
        DataPacket esc = new DataPacket(DataPacket.VIRTUAL_KEY_PRESS);
        esc.setVirtualKeyCode(VirtualKeyCode.ESC);
        serverConnection.writeObject(esc);
    }

    private void maximize() {
        serverConnection.writeObject(new DataPacket(DataPacket.MAXIMIZE));
    }

    private void upArrow() {
        serverConnection.writeObject(new DataPacket(DataPacket.UP_ARROW));
    }

    private void downArrow() {
        serverConnection.writeObject(new DataPacket(DataPacket.DOWN_ARROW));
    }

    private void rightArrow() {
        serverConnection.writeObject(new DataPacket(DataPacket.RIGHT_ARROW));
    }

    private void leftArrow() {
        serverConnection.writeObject(new DataPacket(DataPacket.LEFT_ARROW));
    }

    private void pageUp() {
        serverConnection.writeObject(new DataPacket(DataPacket.PAGE_UP));
    }

    private void pageDown() {
        serverConnection.writeObject(new DataPacket(DataPacket.PAGE_DOWN));
    }

    private void doubleClick() {
        clickFeedbackService.provideFeedback();
        if (mouseButtonStatus.isLeftMouseDown()) {
            mouseButtonStatus.setLeftMouseDown(false);
            serverConnection.writeObject(leftMouseUp);
        }
        serverConnection.writeObject(doubleClick);
    }

    private void leftMouseClick() {
        clickFeedbackService.provideFeedback();
        if (mouseButtonStatus.isLeftMouseDown()) {
            mouseButtonStatus.setLeftMouseDown(false);
            serverConnection.writeObject(leftMouseUp);
        } else {
            serverConnection.writeObject(leftMouseDown);
            serverConnection.writeObject(leftMouseUp);
        }
    }

    private void leftMouseHold() {
        clickFeedbackService.provideFeedback();
        if (mouseButtonStatus.isLeftMouseDown()) {
            serverConnection.writeObject(leftMouseUp);
            mouseButtonStatus.setLeftMouseDown(false);
        } else {
            serverConnection.writeObject(leftMouseDown);
            mouseButtonStatus.setLeftMouseDown(true);
        }
    }

    private void middleMouseClick() {
        clickFeedbackService.provideFeedback();
        if (mouseButtonStatus.isMiddleMouseDown()) {
            mouseButtonStatus.setMiddleMouseDown(false);
            serverConnection.writeObject(middleMouseUp);
        } else {
            serverConnection.writeObject(middleMouseDown);
            serverConnection.writeObject(middleMouseUp);
        }
    }

    private void middleMouseHold() {
        clickFeedbackService.provideFeedback();
        if (mouseButtonStatus.isMiddleMouseDown()) {
            serverConnection.writeObject(middleMouseUp);
            mouseButtonStatus.setMiddleMouseDown(false);
        } else {
            serverConnection.writeObject(middleMouseDown);
            mouseButtonStatus.setMiddleMouseDown(true);
        }
    }

    private void rightMouseClick() {
        clickFeedbackService.provideFeedback();
        if (mouseButtonStatus.isRightMouseDown()) {
            serverConnection.writeObject(rightMouseUp);
            mouseButtonStatus.setRightMouseDown(false);
        } else {
            serverConnection.writeObject(rightMouseDown);
            serverConnection.writeObject(rightMouseUp);
        }
    }

    private void rightMouseHold() {
        clickFeedbackService.provideFeedback();
        if (mouseButtonStatus.isRightMouseDown()) {
            serverConnection.writeObject(rightMouseUp);
            mouseButtonStatus.setRightMouseDown(false);
        } else {
            serverConnection.writeObject(rightMouseDown);
            mouseButtonStatus.setRightMouseDown(true);
        }
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public MouseButtonStatus getMouseButtonStatus() {
        return mouseButtonStatus;
    }

    public void setMouseButtonStatus(MouseButtonStatus mouseButtonStatus) {
        this.mouseButtonStatus = mouseButtonStatus;
    }

    public ClickFeedbackService getClickFeedbackService() {
        return clickFeedbackService;
    }

    public void setClickFeedbackService(ClickFeedbackService clickFeedbackService) {
        this.clickFeedbackService = clickFeedbackService;
    }

    public VirtualKeyboardManager getVirtualKeyboardManager() {
        return virtualKeyboardManager;
    }

    public void setVirtualKeyboardManager(VirtualKeyboardManager virtualKeyboardManager) {
        this.virtualKeyboardManager = virtualKeyboardManager;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public MkRemoteApplication getApp() {
        return app;
    }

    public void setApp(MkRemoteApplication app) {
        this.app = app;
    }

    private class DoubleClickThread extends Thread {
        private boolean abortClick = false;

        @Override
        public void run() {
            try {
                Thread.sleep(175);
                if (!abortClick) {
                    leftMouseClick();
                }
            } catch (Throwable t) {
            }
        }

        public void abortClick() {
            abortClick = true;
        }
    }
}
