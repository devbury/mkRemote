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

public class LoginResponsePacket {
    public static final int GOOD_PASSWORD = 0;
    public static final int BAD_PASSWORD = 1;
    private int s;
    private boolean macroMode;

    public int getStatus() {
        return s;
    }

    public void setStatus(int s) {
        this.s = s;
    }

    public int getS() {
        return s;
    }

    public void setS(int s) {
        this.s = s;
    }

    public boolean isMacroMode() {
        return macroMode;
    }

    public void setMacroMode(boolean macroMode) {
        this.macroMode = macroMode;
    }
}
