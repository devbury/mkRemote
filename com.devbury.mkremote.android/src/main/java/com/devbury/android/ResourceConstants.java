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

package com.devbury.android;

import com.devbury.android.exceptions.ResourceConstantsConfigurationException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ResourceConstants {

    private static Context context = null;

    public static String cs(int resourceId) {
        try {
            return context.getString(resourceId);
        } catch (NullPointerException e) {
            throw new ResourceConstantsConfigurationException("Application object has not been configured");
        }
    }

    public static void setDefault(int resourceId, String d) {
        setDefault(cs(resourceId), d);
    }

    public static void setDefault(String key, String d) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        if (!p.contains(key)) {
            p.edit().putString(key, d).commit();
        }
    }

    public static void setDefault(int resourceId, boolean b) {
        setDefault(cs(resourceId), b);
    }

    public static void setDefault(String key, boolean b) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        if (!p.contains(key)) {
            p.edit().putBoolean(key, b).commit();
        }
    }

    public static void setDefault(int resourceId, int i) {
        setDefault(cs(resourceId), i);
    }

    public static void setDefault(String key, int i) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        if (!p.contains(key)) {
            p.edit().putInt(key, i).commit();
        }
    }

    public static void setDefault(int resourceId, long l) {
        setDefault(cs(resourceId), l);
    }

    public static void setDefault(String key, long l) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        if (!p.contains(key)) {
            p.edit().putLong(key, l).commit();
        }
    }

    public static void setDefault(int resourceId, float f) {
        setDefault(cs(resourceId), f);
    }

    public static void setDefault(String key, float f) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        if (!p.contains(key)) {
            p.edit().putFloat(key, f).commit();
        }
    }

    public static void setContext(Context c) {
        context = c;
    }
}
