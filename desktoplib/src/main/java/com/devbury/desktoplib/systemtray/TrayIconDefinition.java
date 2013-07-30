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

package com.devbury.desktoplib.systemtray;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.desktoplib.swing.Tools;

public class TrayIconDefinition {
    private Logger logger = LoggerFactory.getLogger(TrayIconDefinition.class);
    private String tooltip;
    private String imageName = "com/devbury/desktoplib/systemtray/devbury.png";
    private Collection<MenuItemDefinition> menuItems;

    public String toString() {
        StringBuffer sb = new StringBuffer("tooltip: ");
        sb.append(tooltip);
        sb.append(" imageName: ");
        sb.append(imageName);
        return sb.toString();
    }

    public TrayIcon buildTrayIcon() throws IOException {
        PopupMenu popup = new PopupMenu();
        for (MenuItemDefinition mid : menuItems) {
            if (mid instanceof SeperatorMenuItem) {
                popup.addSeparator();
            } else {
                MenuItem item = new MenuItem();
                item.setLabel(mid.getText());
                item.addActionListener(mid);
                popup.add(item);
            }
        }

        TrayIcon ti = new TrayIcon(getImage(imageName), tooltip, popup);
        ti.setImageAutoSize(true);
        return ti;
    }

    protected Image getImage(String image) {
        return Tools.getImageFromClasspath(image);
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Collection<MenuItemDefinition> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(Collection<MenuItemDefinition> menuItems) {
        this.menuItems = menuItems;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
