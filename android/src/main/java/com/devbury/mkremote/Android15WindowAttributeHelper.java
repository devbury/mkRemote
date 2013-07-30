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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public class Android15WindowAttributeHelper implements IWindowAttributeHelper {

    @Override
    public void processNotificationBar(Window w, boolean on) {
        WindowManager.LayoutParams lp = w.getAttributes();
        if (on) {
            lp.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        w.setAttributes(lp);
    }

    @Override
    public void processWallpaper(Window w, boolean on) {
        if (on) {
            Context c = w.getContext();
            Drawable d = c.getWallpaper();
            try {
                if (d instanceof BitmapDrawable) {
                    Display dis = w.getWindowManager().getDefaultDisplay();
                    BitmapDrawable bd = (BitmapDrawable) d;
                    int scaledWidth = (int) (((float) dis.getWidth() / (float) dis.getHeight()) * bd.getMinimumHeight());
                    BitmapDrawable croped = new BitmapDrawable(Bitmap.createBitmap(bd.getBitmap(), (int) ((d
                            .getMinimumWidth() - scaledWidth) / 2), 0, scaledWidth, d.getMinimumHeight()));
                    w.setBackgroundDrawable(croped);
                    return;
                }
            } catch (Throwable t) {
                Logger.debug("Error resizing wallpaper");
                w.setBackgroundDrawable(d);
            }
        } else {
            w.setBackgroundDrawableResource(android.R.color.black);
        }
    }

}
