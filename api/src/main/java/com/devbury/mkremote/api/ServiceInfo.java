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

package com.devbury.mkremote.api;

import java.io.Serializable;
import java.util.Properties;

public class ServiceInfo implements Serializable {

    private static final long serialVersionUID = 6953791178592186675L;
    private String id;
    private String name;
    private int versionCode;
    private String address;
    private int port;
    private Properties attributes = new Properties();

    public String toString() {
        StringBuffer sb = new StringBuffer("name: ");
        sb.append(getName());
        sb.append(", versionCode: ");
        sb.append(getVersionCode());
        sb.append(", id: ");
        sb.append(getId());
        sb.append(", address: ");
        sb.append(getAddress());
        sb.append(", port: ");
        sb.append(getPort());
        sb.append(", attributes: ");
        sb.append(attributes);
        return sb.toString();
    }

    public ServiceInfo clone() {
        ServiceInfo si = new ServiceInfo();
        setValues(si);
        return si;
    }

    protected void setValues(ServiceInfo si) {
        si.setAddress(address);
        si.setAttributes((Properties) attributes.clone());
        si.setId(id);
        si.setName(name);
        si.setPort(port);
        si.setVersionCode(versionCode);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ServiceInfo) {
            ServiceInfo other = (ServiceInfo) o;
            if (getName().equals(other.getName()) && getVersionCode() == other.getVersionCode()
                    && getAddress().equals(other.getAddress()) && getPort() == other.getPort()) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String serviceName) {
        this.name = serviceName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Properties getAttributes() {
        return attributes;
    }

    public void setAttributes(Properties attributes) {
        this.attributes = attributes;
    }
}
