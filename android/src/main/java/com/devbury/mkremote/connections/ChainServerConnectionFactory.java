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

package com.devbury.mkremote.connections;

import java.util.ArrayList;

import com.devbury.mkremote.api.ServiceInfo;

public class ChainServerConnectionFactory implements ServerConnectionFactory {

    private ArrayList<ServerConnectionFactory> factories = new ArrayList<ServerConnectionFactory>();

    @Override
    public boolean close(ServerConnection serverConnection) {
        for (ServerConnectionFactory f : factories) {
            if (f.close(serverConnection)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        for (ServerConnectionFactory f : factories) {
            f.destroy();
        }
    }

    @Override
    public ServerConnection getConnection(ServiceInfo serviceInfo, String password) {
        for (ServerConnectionFactory f : factories) {
            ServerConnection ret = f.getConnection(serviceInfo, password);
            if (ret != null) {
                return ret;
            }
        }
        throw new ServerConnectionException("Not a valid server");
    }

    @Override
    public void init() {
        for (ServerConnectionFactory f : factories) {
            f.init();
        }
    }

    public void addServerConnectionFactory(ServerConnectionFactory f) {
        factories.add(f);
    }
}
