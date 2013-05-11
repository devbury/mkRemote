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

package com.devbury.desktoplib.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class Tools {

    public static final Image getImageFromClasspath(String imageName) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            InputStream is = ClassLoader.getSystemResourceAsStream(imageName);
            BufferedInputStream bis = new BufferedInputStream(is);
            int b;
            while ((b = bis.read()) != -1) {
                os.write(b);
            }

            Image i = newToolkit().createImage(os.toByteArray());
            os.close();
            bis.close();
            is.close();
            return i;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Point centerComponent(Component c) {
        Dimension screen = newToolkit().getScreenSize();
        return new Point((screen.width / 2) - (c.getWidth() / 2), (screen.height / 2) - (c.getHeight() / 2));
    }

    private static Toolkit newToolkit() {
        return Toolkit.getDefaultToolkit();
    }
}
