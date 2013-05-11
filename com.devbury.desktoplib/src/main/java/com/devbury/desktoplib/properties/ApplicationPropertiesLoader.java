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

package com.devbury.desktoplib.properties;

import java.util.Collection;

public class ApplicationPropertiesLoader {

    public static final String APPLICATION_PROPERTIES_CLASS = "com.devbury.desktoplib.properties.ApplicationProperties";
    public static final String LOGGING_PROPERTIES_METHOD = "getLoggingPropertiesFiles";
    public static final String APPLICATION_PROPERTIES_METHOD = "getApplicationPropertiesFiles";

    public static Collection<String> getLoggingPropertiesFiles() {
        return getPropertiesFiles(LOGGING_PROPERTIES_METHOD);
    }

    public static Collection<String> getApplicationPropertiesFiles() {
        return getPropertiesFiles(APPLICATION_PROPERTIES_METHOD);
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> getPropertiesFiles(String methodName) {
        try {
            Class<?> ap = Class.forName(APPLICATION_PROPERTIES_CLASS);
            return (Collection<String>) ap
                    .getMethod(methodName, (Class[]) null).invoke(null,
                            (Object[]) null);
        } catch (Throwable t) {
            throw new ApplicationPropertiesException(
                    "Could not load properties", t);
        }
    }
}
