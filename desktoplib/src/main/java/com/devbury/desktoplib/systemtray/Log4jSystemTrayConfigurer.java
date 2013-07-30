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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.desktoplib.properties.ApplicationPropertiesLoader;

public class Log4jSystemTrayConfigurer {

    public static final String DEFAULT_BASE_APPLICATION_PROPERTIES = "com/devbury/desktoplib/systemtray/SystemTrayApplication.properties";
    public static final String DEFAULT_APPLICATION_PROPERTIES = "SystemTrayApplication.properties";

    public Log4jSystemTrayConfigurer() {

    }

    public void configureLog4j() {
        Properties p = newProperties();
        loadProperties(p, getClass().getClassLoader().getResourceAsStream(DEFAULT_BASE_APPLICATION_PROPERTIES));
        loadProperties(p, getClass().getClassLoader().getResourceAsStream(DEFAULT_APPLICATION_PROPERTIES));
        for (String file : ApplicationPropertiesLoader.getLoggingPropertiesFiles()) {
            loadProperties(p, file);
        }
        initLog4j(p);
    }

    protected void initLog4j(Properties p) {
        PropertyConfigurator.configure(p);
        // get a logger
        Logger logger = LoggerFactory.getLogger(Log4jSystemTrayConfigurer.class);
        logger.info("Log4j configured with...");
        for (Map.Entry e : p.entrySet()) {
            if (((String) e.getKey()).startsWith("log4j")) {
                logger.info("   " + e.getKey() + "=" + e.getValue());
            }
        }
    }

    protected void loadProperties(Properties p, String file_name) {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(file_name));
            loadProperties(p, is);
            is.close();
        } catch (Exception e) {
        }
    }

    protected void loadProperties(Properties p, InputStream resource) {
        if (resource != null) {
            try {
                p.load(resource);
            } catch (IOException e) {
            }
        }
    }

    protected Properties newProperties() {
        return new Properties();
    }

    protected String getSystemProperty(String s) {
        return System.getProperty(s);
    }
}
