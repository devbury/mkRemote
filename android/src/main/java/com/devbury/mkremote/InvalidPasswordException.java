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

package com.devbury.mkremote;

import com.devbury.mkremote.connections.ServerConnectionException;

public class InvalidPasswordException extends ServerConnectionException {

    private static final long serialVersionUID = 3939179207892031208L;

    public InvalidPasswordException() {
        super();
    }

    public InvalidPasswordException(String arg0) {
        super(arg0);
    }

    public InvalidPasswordException(Throwable arg0) {
        super(arg0);
    }

    public InvalidPasswordException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
