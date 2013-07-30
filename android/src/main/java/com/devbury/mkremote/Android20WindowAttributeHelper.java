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

import android.view.Window;
import android.view.WindowManager;

public class Android20WindowAttributeHelper extends Android16WindowAttributeHelper implements IWindowAttributeHelper {

    @Override
    public void processWallpaper(Window w, boolean on) {
        w.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = w.getAttributes();
        if (on) {
            lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
        } else {
            lp.flags &= ~WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
        }
        w.setAttributes(lp);
    }
}
