/*
 * Copyright (c) 2009-2013 devBury LLC
 * This file is part of mkRemote.
 *
 *     mkRemote is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License Version 3
 *     as published by the Free Software Foundation.
 *
 *     mkRemote is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with mkRemote.  If not, see <http://www.gnu.org/licenses/gpl.txt/>.
 */

package com.devbury.mkremote.api;

public class DataPacket {
    public static final int KEY_PRESS = 0;
    public static final int MOUSE_MOVE = 1;
    public static final int DPAD_MOVE = 2;
    public static final int LEFT_BUTTON_DOWN = 3;
    public static final int LEFT_BUTTON_UP = 4;
    public static final int RIGHT_BUTTON_DOWN = 5;
    public static final int RIGHT_BUTTON_UP = 6;
    public static final int DEL_PRESS = 7;
    public static final int UP_ARROW = 8;
    public static final int DOWN_ARROW = 9;
    public static final int PAGE_UP = 10;
    public static final int PAGE_DOWN = 11;
    public static final int MIDDLE_BUTTON_DOWN = 12;
    public static final int MIDDLE_BUTTON_UP = 13;
    public static final int QUICK_LAUNCH_LIST = 14;
    public static final int QUICK_LAUNCH = 15;
    public static final int MAXIMIZE = 16;
    public static final int MOUSE_MOVE_SERVICE_INFO = 17;
    public static final int MACRO_START = 18;
    public static final int MACRO_SAVE = 19;
    public static final int MACRO_ABORT = 20;
    public static final int MACRO_LIST = 21;
    public static final int MACRO_DELETE = 22;
    public static final int MACRO_EXECUTE = 23;
    public static final int MACRO_END = 24;
    public static final int MACRO_DELAY = 25;
    public static final int MACRO_DELAY_RECORD = 26;
    public static final int CURRENT_MOUSE_LOCATION = 27;
    public static final int MOUSE_MOVE_TO = 28;
    public static final int LOGIN = 29;
    public static final int DOUBLE_CLICK = 30;
    public static final int LEFT_ARROW = 31;
    public static final int RIGHT_ARROW = 32;
    public static final int VIRTUAL_KEY_PRESS = 33;
    public static final int SCROLL_UP = 34;
    public static final int SCROLL_DOWN = 35;
    public static final int SERVICE_PING = 36;
    public static final int BOXEE_UP = 37;
    public static final int BOXEE_DOWN = 38;
    public static final int BOXEE_RIGHT = 39;
    public static final int BOXEE_LEFT = 40;
    public static final int BOXEE_BACK = 41;
    public static final int BOXEE_MUTE = 42;
    public static final int BOXEE_PLAY_NEXT = 43;
    public static final int BOXEE_PAUSE = 44;
    public static final int BOXEE_SELECT = 45;
    public static final int BOXEE_STOP = 47;
    public static final int BOXEE_VOLUME_UP = 48;
    public static final int BOXEE_VOLUME_DOWN = 49;
    public static final int BOXEE_PLAYED_PERCENTAGE = 50;
    public static final int BOXEE_SET_PLAYED_PERCENTAGE = 51;
    private int t;
    private int x;
    private int y;
    private String s;

    public DataPacket() {
        // no op
    }

    public DataPacket(int type) {
        t = type;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("t = ").append(t);
        return sb.toString();
    }

    public int getPercentage() {
        return x;
    }

    public void setPercentage(int p) {
        x = p;
    }

    public int getVirtualModifiers() {
        return y;
    }

    public void setVirtualModifiers(int m) {
        y = m;
    }

    public int getVirtualKeyCode() {
        return x;
    }

    public void setVirtualKeyCode(int code) {
        x = code;
    }

    public int getUnicode() {
        return x;
    }

    public void setUnicode(int unicode) {
        x = unicode;
    }

    public int getType() {
        return t;
    }

    public void setType(int type) {
        t = type;
    }

    public int getDx() {
        return x;
    }

    public void setDx(int dx) {
        x = dx;
    }

    public int getDy() {
        return y;
    }

    public void setDy(int dy) {
        y = dy;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }
}
