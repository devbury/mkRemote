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

import java.awt.Point;

import com.devbury.mkremote.api.DataPacket;

public interface MouseAndKeyService {
    void keyPressed(int keyCode);

    void keyReleased(int keyCode);

    void keySequence(int[] keyCodeSequence);

    void mouseDelta(int x, int y);

    void leftButtonDown();

    void leftButtonUp();

    void middleButtonDown();

    void middleButtonUp();

    void rightButtonDown();

    void rightButtonUp();

    void maximize();

    void keySequence(DataPacket vp);

    Point currentMouseLocation();

    void mouseTo(int x, int y);

    void scrollUp();

    void scrollDown();
}
