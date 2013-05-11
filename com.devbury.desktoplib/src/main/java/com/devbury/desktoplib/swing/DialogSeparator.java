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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JLabel;

public class DialogSeparator extends JLabel {

    private static final long serialVersionUID = 2601142562901134334L;
    public static final int OFFSET = 15;

    public DialogSeparator() {
    }

    public DialogSeparator(String text) {
        super(text);
    }

    public Dimension getPreferredSize() {
        return new Dimension(getParent().getWidth(), 20);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public void paint(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        Dimension d = getSize();
        int y = (d.height - 3) / 2;
        g.setColor(Color.white);
        g.drawLine(1, y, d.width - 1, y);
        y++;
        g.drawLine(0, y, 1, y);
        g.setColor(Color.gray);
        g.drawLine(d.width - 1, y, d.width, y);
        y++;
        g.drawLine(1, y, d.width - 1, y);

        String text = getText();
        if (text.length() == 0)
            return;

        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        y = (d.height + fm.getAscent()) / 2;
        int l = fm.stringWidth(text);

        g.setColor(getBackground());
        g.fillRect(OFFSET - 5, 0, OFFSET + l, d.height);

        g.setColor(getForeground());
        g.drawString(text, OFFSET, y);
    }
}
