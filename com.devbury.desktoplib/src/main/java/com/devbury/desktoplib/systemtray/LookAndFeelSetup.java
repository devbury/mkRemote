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

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LookAndFeelSetup {
    private Logger logger = LoggerFactory.getLogger(LookAndFeelSetup.class);
    private String lookAndFeelClass;

    public LookAndFeelSetup() {
        try {
            lookAndFeelClass = UIManager.getSystemLookAndFeelClassName();
        } catch (Throwable t) {
            logger.warn("Could not find system LookAndFeel class name");
        }
    }

    public void init() {
        logger.debug("Installed LookAndFeels");
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            logger.debug("Name : {}, Class : {}", info.getName(), info.getClassName());
        }
        if (lookAndFeelClass != null) {
            try {
                UIManager.setLookAndFeel(lookAndFeelClass);
            } catch (Throwable t) {
                logger.error("Could not set LookAndFeel to {}", lookAndFeelClass, t);
            }
        }
        LookAndFeel current = UIManager.getLookAndFeel();
        logger.debug("Current LookAndFeel Class : {}, is native {}", current.getClass().getName(), current
                .isNativeLookAndFeel());

    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public String getLookAndFeelClass() {
        return lookAndFeelClass;
    }

    public void setLookAndFeelClass(String lookAndFeelClass) {
        this.lookAndFeelClass = lookAndFeelClass;
    }
}
