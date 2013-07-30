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

import com.devbury.logging.Logger;

import android.view.MotionEvent;

public class MultiTouchHandler implements IMultiTouchHandler {
    
    public MultiTouchHandler() {
        int a = MotionEvent.ACTION_POINTER_1_DOWN;
    }

    @Override
    public boolean processEvent(MotionEvent event) {
        String event_name = null;
        switch (event.getAction()) {
        case MotionEvent.ACTION_POINTER_1_DOWN:
            event_name = "POINTER_1_DOWN";
            break;
        case MotionEvent.ACTION_POINTER_1_UP:
            event_name = "POINTER_1_UP";
            break;
        case MotionEvent.ACTION_POINTER_2_DOWN:
            event_name = "POINTER_2_DOWN";
            break;
        case MotionEvent.ACTION_POINTER_2_UP:
            event_name = "POINTER_2_UP";
            break;
        case MotionEvent.ACTION_POINTER_3_DOWN:
            event_name = "POINTER_3_DOWN";
            break;
        case MotionEvent.ACTION_POINTER_3_UP:
            event_name = "POINTER_3_UP";
            break;
        }
        if (event_name != null) {
            Logger.debug("Motion event is " + event_name);
        }
        return true;
    }
}
