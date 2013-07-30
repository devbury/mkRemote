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

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.Toast;

public class LockSlider extends SlidingDrawer {

    private View handle;
    private Rect lockFrame = new Rect();
    private ImageView lock;
    private boolean locked = false;
    private boolean justLocked = false;
    private Toast toast;

    public LockSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        newToast(context);
    }

    public LockSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        newToast(context);
    }

    protected void newToast(Context c) {
        toast = Toast.makeText(c, R.string.keyboard_locked, Toast.LENGTH_SHORT);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        handle = findViewById(R.id.statusbar);
        lock = (ImageView) findViewById(R.id.lock);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean ret = super.dispatchTouchEvent(ev);
        if (isOpened()) {
            justLocked = false;
            return ret;
        }
        if (locked && !justLocked) {
            // this allows the touch events through to control the mouse while the slider is locked 
            return false;
        } else {
            justLocked = false;
            return ret;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onInterceptTouchEvent(event);
        }

        int x = (int) event.getX();
        int y = (int) event.getY();
        handle.getHitRect(lockFrame);
        lockFrame.right = 30;
        if (lockFrame.contains(x, y)) {
            lockToggle();
            return false;
        } else if (locked) {
            return false;
        } else {
            return super.onInterceptTouchEvent(event);
        }
    }

    protected void lockToggle() {
        if (locked) {
            unlock();
        } else {
            lock();
        }
    }

    @Override
    public void lock() {
        locked = true;
        justLocked = true;
        lock.setImageResource(R.drawable.lock);
        toast.setText(R.string.keyboard_locked);
        toast.show();
        super.lock();
    }

    @Override
    public void unlock() {
        locked = false;
        justLocked = false;
        lock.setImageResource(R.drawable.unlock);
        toast.setText(R.string.keyboard_unlocked);
        toast.show();
        super.unlock();
    }
}
