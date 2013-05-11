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
import android.content.res.Configuration;
import android.os.Build;

public class Android3ConfigurationData implements AndroidConfigurationData {
    
    private Context context;

    public Android3ConfigurationData(Context c) {
        context = c;
    }

    @Override
    public void appendConfigurationData(StringBuffer sb) {
        try {
            sb.append("Display : ").append(Build.DISPLAY).append('\n');
        } catch (Throwable t) {
        }
        Configuration config = null;
        try {
            config = context.getResources().getConfiguration();
        } catch (Throwable t) {
        }
        try {
            sb.append("hardKeyboardHidden : ").append(config.hardKeyboardHidden).append('\n');
        } catch (Throwable t) {
        }
    }
}
