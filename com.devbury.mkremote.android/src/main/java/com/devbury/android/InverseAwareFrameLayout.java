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

package com.devbury.android;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class InverseAwareFrameLayout extends FrameLayout {
    private Logger logger = LoggerFactory.getLogger(InverseAwareFrameLayout.class);
    private InverseManager inverseManager = new InverseManager() {
        @Override
        public boolean isInversed() {
            return false;
        }
    };

    public InverseAwareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InverseAwareFrameLayout(Context context) {
        super(context);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        flip(canvas);
        super.dispatchDraw(canvas);
        flip(canvas);
    }

    @Override
    public void draw(Canvas canvas) {
        flip(canvas);
        super.draw(canvas);
        flip(canvas);
    }

    public void modifyTrackballMotionEvent(MotionEvent event) {
        if (inverseManager.isInversed()) {
            event.setLocation(event.getX() * (-1), event.getY() * (-1));
        }
    }

    public void modifyTouchMotionEvent(MotionEvent event) {
        if (inverseManager.isInversed()) {
            event.setLocation(getMeasuredWidth() - event.getX(), getMeasuredHeight() - event.getY());
        }
    }

    private void flip(Canvas canvas) {
        if (inverseManager.isInversed()) {
            int px = getMeasuredWidth() / 2;
            int py = getMeasuredHeight() / 2;
            logger.debug("rotating canvas around " + px + ", " + py);
            canvas.rotate(180, px, py);
        }
    }

    public InverseManager getInverseManager() {
        return inverseManager;
    }

    public void setInverseManager(InverseManager inverseManager) {
        this.inverseManager = inverseManager;
    }
}
