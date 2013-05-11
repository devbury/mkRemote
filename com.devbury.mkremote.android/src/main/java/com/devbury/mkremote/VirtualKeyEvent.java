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

import android.view.KeyEvent;

public class VirtualKeyEvent extends KeyEvent {

    private int virtualCode;
    private boolean virtualAltPressed;
    private boolean virtualCtrlPressed;
    private boolean virtualWinPressed;
    private boolean virtualCommandPressed;

    public VirtualKeyEvent(int action, int code) {
        super(action, code);
        virtualCode = code;
    }

    public VirtualKeyEvent(KeyEvent origEvent, long eventTime, int newRepeat) {
        super(origEvent, eventTime, newRepeat);
    }

    public VirtualKeyEvent(KeyEvent origEvent) {
        super(origEvent);
    }

    public VirtualKeyEvent(long downTime, long eventTime, int action, int code, int repeat, int metaState, int device,
                           int scancode, int flags) {
        super(downTime, eventTime, action, code, repeat, metaState, device, scancode, flags);
    }

    public VirtualKeyEvent(long downTime, long eventTime, int action, int code, int repeat, int metaState, int device,
                           int scancode) {
        super(downTime, eventTime, action, code, repeat, metaState, device, scancode);
    }

    public VirtualKeyEvent(long downTime, long eventTime, int action, int code, int repeat, int metaState) {
        super(downTime, eventTime, action, code, repeat, metaState);
    }

    public VirtualKeyEvent(long downTime, long eventTime, int action, int code, int repeat) {
        super(downTime, eventTime, action, code, repeat);
    }

    public VirtualKeyEvent(long time, String characters, int device, int flags) {
        super(time, characters, device, flags);
    }

    @Override
    public int getUnicodeChar(int meta) {
        if (virtualCode > 0) {
            return virtualCode;
        } else {
            return 0;
        }
    }

    public int getVirtualCode() {
        return virtualCode;
    }

    public void setVirtualCode(int virtualCode) {
        this.virtualCode = virtualCode;
    }

    public boolean isVirtualAltPressed() {
        return virtualAltPressed;
    }

    public void setVirtualAltPressed(boolean virtualAltPressed) {
        this.virtualAltPressed = virtualAltPressed;
    }

    public boolean isVirtualCtrlPressed() {
        return virtualCtrlPressed;
    }

    public void setVirtualCtrlPressed(boolean virtualCtrlPressed) {
        this.virtualCtrlPressed = virtualCtrlPressed;
    }

    public boolean isVirtualWinPressed() {
        return virtualWinPressed;
    }

    public void setVirtualWinPressed(boolean virtualWinPressed) {
        this.virtualWinPressed = virtualWinPressed;
    }

    public boolean isVirtualCommandPressed() {
        return virtualCommandPressed;
    }

    public void setVirtualCommandPressed(boolean virtualCommandPressed) {
        this.virtualCommandPressed = virtualCommandPressed;
    }
}
