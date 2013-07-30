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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;

public class Android1ConfigurationData implements AndroidConfigurationData {

    private Context context;

    public Android1ConfigurationData(Context c) {
        context = c;
    }

    @Override
    public void appendConfigurationData(StringBuffer sb) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        String version_name = "UNKNOWN";
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            version_name = pi.versionName;
        } catch (Throwable t) {
        }
        try {
            sb.append("Version : ").append(version_name).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Package : ").append(context.getPackageName()).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Phone Model :").append(Build.MODEL).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Android Version : ").append(Build.VERSION.RELEASE).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Android SDK : ").append(Build.VERSION.SDK).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Board : ").append(Build.BOARD).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Brand : ").append(Build.BRAND).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Device : ").append(Build.DEVICE).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Finger Print : ").append(Build.FINGERPRINT).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Host : ").append(Build.HOST).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("ID : ").append(Build.ID).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Model : ").append(Build.MODEL).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Product : ").append(Build.PRODUCT).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Tags : ").append(Build.TAGS).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Time : ").append(Build.TIME).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("Type : ").append(Build.TYPE).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("User : ").append(Build.USER).append('\n');
        } catch (Throwable t) {
        }

        Configuration config = null;
        try {
            config = context.getResources().getConfiguration();
        } catch (Throwable t) {
        }
        try {
            sb.append("fontScale : ").append(config.fontScale).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("keyboard : ").append(config.keyboard).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("keyboardHidden : ").append(config.keyboardHidden).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("locale : ").append(config.locale).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("mcc : ").append(config.mcc).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("mnc : ").append(config.mnc).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("navigation : ").append(config.navigation).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("orientation : ").append(config.orientation).append('\n');
        } catch (Throwable t) {
        }
        try {
            sb.append("touchScreen : ").append(config.touchscreen).append('\n');
        } catch (Throwable t) {
        }
    }
}
