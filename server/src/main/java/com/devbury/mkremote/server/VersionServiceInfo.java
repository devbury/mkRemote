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

import com.devbury.mkremote.api.ServiceInfo;

public class VersionServiceInfo extends ServiceInfo {

    private static final long serialVersionUID = -2674446239794743259L;
    private transient VersionInfo version;

    public void init() {
        setVersionCode(version.getVersionCode());
        getAttributes().setProperty("os.name", osName());
    }

    protected String osName() {
        return System.getProperty("os.name");
    }

    public VersionInfo getVersion() {
        return version;
    }

    public void setVersion(VersionInfo version) {
        this.version = version;
    }
}
