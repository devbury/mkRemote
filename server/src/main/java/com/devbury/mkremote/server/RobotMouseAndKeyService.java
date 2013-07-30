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

package com.devbury.mkremote.server;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.api.DataPacket;

public class RobotMouseAndKeyService implements MouseAndKeyService {

    private Logger logger = LoggerFactory
            .getLogger(RobotMouseAndKeyService.class);
    private MaximizeService maximizeService;
    private VirtualKeyCodeService virtualKeyCodeService;
    private Robot robot;
    private Dimension screenSize = null;
    private boolean mac = false;

    public RobotMouseAndKeyService() {
        if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
            mac = true;
        }

        try {
            robot = new Robot();
            if (mac) {
                screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                screenSize.width--;
                screenSize.height--;
            }
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void scrollDown() {
        if (mac) {
            robot.mouseWheel(-1);
        } else {
            robot.mouseWheel(1);
        }
    }

    @Override
    public void scrollUp() {
        if (mac) {
            robot.mouseWheel(1);
        } else {
            robot.mouseWheel(-1);
        }
    }

    @Override
    public void keySequence(int[] keyCodeSequence) {
        if (keyCodeSequence != null) {
            for (int i = 0; i < keyCodeSequence.length; i++) {
                keyPressed(keyCodeSequence[i]);
            }
            for (int i = keyCodeSequence.length - 1; i >= 0; i--) {
                keyReleased(keyCodeSequence[i]);
            }
        } else {
            logger.debug("Ignoring character.  It has no keyCodeSequence");
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        if (logger.isDebugEnabled()) {
            logger.debug("key pressed " + KeyEvent.getKeyText(keyCode));
        }
        robot.keyPress(keyCode);
    }

    @Override
    public void keyReleased(int keyCode) {
        if (logger.isDebugEnabled()) {
            logger.debug("key released " + KeyEvent.getKeyText(keyCode));
        }
        robot.keyRelease(keyCode);
    }

    @Override
    public void mouseDelta(int x, int y) {
        Point current = currentPoint();
        ddaMouseMove(current.x, current.y, current.x + x, current.y + y);
    }

    @Override
    public void leftButtonDown() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
    }

    @Override
    public void leftButtonUp() {
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    @Override
    public void middleButtonDown() {
        robot.mousePress(InputEvent.BUTTON2_MASK);
    }

    @Override
    public void middleButtonUp() {
        robot.mouseRelease(InputEvent.BUTTON2_MASK);
    }

    @Override
    public void rightButtonDown() {
        robot.mousePress(InputEvent.BUTTON3_MASK);
    }

    @Override
    public void rightButtonUp() {
        robot.mouseRelease(InputEvent.BUTTON3_MASK);
    }

    @Override
    public void maximize() {
        keySequence(maximizeService.getMaximizeSequence());
    }

    @Override
    public void keySequence(DataPacket vp) {
        keySequence(virtualKeyCodeService.getSequence(vp));
    }

    @Override
    public Point currentMouseLocation() {
        return currentPoint();
    }

    @Override
    public void mouseTo(int x, int y) {
        if (screenSize != null) {
            if (x > screenSize.width) {
                x = screenSize.width;
            } else if (x < 0) {
                x = 0;
            }
            if (y > screenSize.height) {
                y = screenSize.height;
            } else if (y < 0) {
                y = 0;
            }
        }
        robot.mouseMove(x, y);
    }

    protected Point currentPoint() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    protected void ddaMouseMove(int x0, int y0, int x1, int y1) {
        int dy = y1 - y0;
        int dx = x1 - x0;
        float t = (float) 0.5; // offset for rounding

        mouseTo(x0, y0);
        if (Math.abs(dx) > Math.abs(dy)) { // slope < 1
            float m = (float) dy / (float) dx; // compute slope
            t += y0;
            dx = (dx < 0) ? -1 : 1;
            m *= dx;
            while (x0 != x1) {
                x0 += dx; // step to next x value
                t += m; // add slope to y value
                mouseTo(x0, (int) t);
            }
        } else { // slope >= 1
            float m = (float) dx / (float) dy; // compute slope
            t += x0;
            dy = (dy < 0) ? -1 : 1;
            m *= dy;
            while (y0 != y1) {
                y0 += dy; // step to next y value
                t += m; // add slope to x value
                mouseTo((int) t, y0);
            }
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public MaximizeService getMaximizeService() {
        return maximizeService;
    }

    public void setMaximizeService(MaximizeService maximizeService) {
        this.maximizeService = maximizeService;
    }

    public VirtualKeyCodeService getVirtualKeyCodeService() {
        return virtualKeyCodeService;
    }

    public void setVirtualKeyCodeService(
            VirtualKeyCodeService virtualKeyCodeService) {
        this.virtualKeyCodeService = virtualKeyCodeService;
    }
}
