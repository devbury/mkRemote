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

public class MouseButtonStatus {

    private boolean leftMouseDown = false;
    private boolean middleMouseDown = false;
    private boolean rightMouseDown = false;

    public boolean isLeftMouseDown() {
        return leftMouseDown;
    }

    public void setLeftMouseDown(boolean leftMouseDown) {
        this.leftMouseDown = leftMouseDown;
    }

    public boolean isMiddleMouseDown() {
        return middleMouseDown;
    }

    public void setMiddleMouseDown(boolean middleMouseDown) {
        this.middleMouseDown = middleMouseDown;
    }

    public boolean isRightMouseDown() {
        return rightMouseDown;
    }

    public void setRightMouseDown(boolean rightMouseDown) {
        this.rightMouseDown = rightMouseDown;
    }
}
