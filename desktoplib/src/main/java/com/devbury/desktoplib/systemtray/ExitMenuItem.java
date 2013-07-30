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

import java.awt.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExitMenuItem implements MenuItemDefinition {

    private Logger logger = LoggerFactory.getLogger(ExitMenuItem.class);
    private ApplicationShutdownService applicationShutdownService;

    public String getText() {
        return "Exit";
    }

    public void actionPerformed(ActionEvent e) {
        logger.debug("Exit selected");
        applicationShutdownService.shutdown();
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public ApplicationShutdownService getApplicationShutdownService() {
        return applicationShutdownService;
    }

    public void setApplicationShutdownService(ApplicationShutdownService applicationShutdownService) {
        this.applicationShutdownService = applicationShutdownService;
    }
}
