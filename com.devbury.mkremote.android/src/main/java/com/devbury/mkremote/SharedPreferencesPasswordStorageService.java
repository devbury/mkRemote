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

import com.devbury.mkremote.api.PasswordStorageService;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class SharedPreferencesPasswordStorageService implements PasswordStorageService {

    private static final String PASSWORD_PREFIX = "PASSWORD_FOR_SERVER_";
    SharedPreferences sharedPreferences;

    @Override
    public String getPassword(String key) {
        return sharedPreferences.getString(PASSWORD_PREFIX + key.toUpperCase(), "");
    }

    @Override
    public void savePassword(String key, String password) {
        Editor e = sharedPreferences.edit();
        e.putString(PASSWORD_PREFIX + key.toUpperCase(), password);
        e.commit();
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }
}
