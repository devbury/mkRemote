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

import java.awt.SplashScreen;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SystemTrayApplication {

    public static final String BASE_CONTEXT = "classpath:com/devbury/desktoplib/systemtray/SystemTrayApplicationBaseContext.xml";
    public static final String APPLICATION_CONTEXT = "classpath:SystemTrayApplicationContext.xml";
    private Logger logger = LoggerFactory
            .getLogger(SystemTrayApplication.class);
    private String[] args;
    private ClassPathXmlApplicationContext applicationContext;
    private String[] configLocations = {BASE_CONTEXT, APPLICATION_CONTEXT};

    public static void main(String[] args) {
        SystemTrayApplication sta = newSystemTrayApplication();
        sta.setArgs(args);
        sta.configureLogging();
        sta.start();
    }

    protected static SystemTrayApplication newSystemTrayApplication() {
        return new SystemTrayApplication();
    }

    public void configureLogging() {
        Log4jSystemTrayConfigurer configure = new Log4jSystemTrayConfigurer();
        configure.configureLog4j();
    }

    public void start() {
        logSystemInfo();
        try {
            if (!isSystemTraySupported()) {
                JOptionPane
                        .showMessageDialog(
                                null,
                                "Could not start application.  This application requires system tray support and your platform "
                                        + "does not provide this.",
                                "System Tray Not Supported",
                                JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException("SystemTray is not supported");
            } else {
                logger.debug("SystemTray is supported");
            }
            if (applicationContext == null) {
                String[] locations = buildConfigLocations();
                logger.debug("Creating context from "
                        + configLocationsToString(locations));
                applicationContext = new ClassPathXmlApplicationContext(
                        locations);
                logger.debug("Context finished building");
            }
            // get the TrayIconDefinition instances
            Map defs = applicationContext
                    .getBeansOfType(TrayIconDefinition.class);
            if (defs == null || defs.isEmpty()) {
                throw new RuntimeException(
                        "No TrayIconDefinition instances exist in the context");
            }
            SystemTray tray = newSystemTray();
            Iterator<TrayIconDefinition> it = defs.values().iterator();
            LinkedList<TrayIcon> installedIcons = new LinkedList<TrayIcon>();
            while (it.hasNext()) {
                TrayIconDefinition def = (TrayIconDefinition) it.next();
                try {
                    TrayIcon ti = def.buildTrayIcon();
                    tray.add(ti);
                    installedIcons.add(ti);
                } catch (Throwable t) {
                    logger.error("Could not add TrayIconDefinition " + def);
                }
            }
            // get the monitor object out of the context to block on
            Object monitor = applicationContext
                    .getBean("applicationShutdownService");

            // if there was a splash screen shut it down
            SplashScreen splash = newSplashScreen();
            if (splash != null) {
                logger.debug("Shutting down splash screen");
                splash.close();
            }

            synchronized (monitor) {
                monitor.wait();
            }
            logger.debug("Application shutting down");
            Iterator<TrayIcon> trayIconIt = installedIcons.iterator();
            while (trayIconIt.hasNext()) {
                TrayIcon ti = (TrayIcon) trayIconIt.next();
                tray.remove(ti);
            }
            applicationContext.close();
            logger.debug("Application stopped");
        } catch (Throwable t) {
            logger.error("Unresolved exception", t);
            logger.error("Application shutting down");
            if (applicationContext != null) {
                applicationContext.close();
            }
            logger.error("Application stopped");
        }
    }

    protected void logSystemInfo() {
        logger.debug("os.name = {}", System.getProperty("os.name"));
        logger.debug("os.arch = {}", System.getProperty("os.arch"));
        logger.debug("os.version = {}", System.getProperty("os.version"));
        logger.debug("java.version = {}", System.getProperty("java.version"));
        logger.debug("java.vendor = {}", System.getProperty("java.vendor"));
    }

    protected boolean isSystemTraySupported() {
        return SystemTray.isSupported();
    }

    protected SystemTray newSystemTray() {
        return SystemTray.getSystemTray();
    }

    protected SplashScreen newSplashScreen() {
        return SplashScreen.getSplashScreen();
    }

    protected String[] buildConfigLocations() {
        return getConfigLocations();
    }

    protected String configLocationsToString(String[] locations) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < locations.length; i++) {
            sb.append(locations[i]);
            if (i != (locations.length - 1)) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public ClassPathXmlApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(
            ClassPathXmlApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public String[] getConfigLocations() {
        return configLocations;
    }

    public void setConfigLocations(String[] configLocations) {
        this.configLocations = configLocations;
    }
}
