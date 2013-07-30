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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import com.devbury.logging.Logger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class EmailUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static final String EXCEPTION_ERROR_MAIL_BODY = "exception_error_mail_body";
    private Thread.UncaughtExceptionHandler delegateHandler;
    private Context context;
    private SharedPreferences sharedPreferences;

    public EmailUncaughtExceptionHandler(Context c, Thread.UncaughtExceptionHandler dh) {
        context = c;
        delegateHandler = dh;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private String info() {
        ArrayList<AndroidConfigurationData> configData = new ArrayList<AndroidConfigurationData>();
        try {
            configData.add(new Android1ConfigurationData(context));
        } catch (Throwable t) {
        }
        try {
            configData.add(new Android3ConfigurationData(context));
        } catch (Throwable t) {
        }
        try {
            configData.add(new Android4ConfigurationData(context));
        } catch (Throwable t) {
        }
        try {
            configData.add(new Android5ConfigurationData(context));
        } catch (Throwable t) {
        }
        StringBuffer data = new StringBuffer();
        for (AndroidConfigurationData acd : configData) {
            try {
                acd.appendConfigurationData(data);
            } catch (Throwable t) {
            }
        }
        return data.toString();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        StringBuffer sb = new StringBuffer();
        Date cur_date = new Date();
        sb.append("Comments :\n\n\n");
        sb.append("Error Report collected on : ").append(cur_date).append("\n\n");
        sb.append("Information").append('\n');
        sb.append("----------------------------------------------------------------------------\n\n");
        sb.append(info());
        sb.append('\n');
        sb.append("Stack Trace\n");
        sb.append("----------------------------------------------------------------------------\n\n");
        StringWriter stack_trace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stack_trace));
        sb.append(stack_trace);
        sb.append('\n');
        sb.append("Cause\n");
        sb.append("----------------------------------------------------------------------------\n\n");

        Throwable cause = throwable.getCause();
        while (cause != null) {
            StringWriter cause_writer = new StringWriter();
            cause.printStackTrace(new PrintWriter(cause_writer));
            sb.append(cause_writer);
            cause = cause.getCause();
        }
        sharedPreferences.edit().putString(EXCEPTION_ERROR_MAIL_BODY, sb.toString()).commit();
        if (delegateHandler != null) {
            delegateHandler.uncaughtException(thread, throwable);
        }
    }

    public boolean hasError() {
        if (sharedPreferences.contains(EXCEPTION_ERROR_MAIL_BODY)) {
            Logger.debug("Previous error found");
            return true;
        } else {
            Logger.debug("Previous error not found");
            return false;
        }
    }

    public void sendErrorMail() {
        sendErrorMail(context);
    }

    public void clearError() {
        sharedPreferences.edit().remove(EXCEPTION_ERROR_MAIL_BODY).commit();
    }

    public void sendErrorMail(Context c) {
        String mail_body = sharedPreferences.getString(EXCEPTION_ERROR_MAIL_BODY, null);
        if (mail_body != null) {
            Logger.debug("Found error from previous run");
            clearError();
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            String subject = context.getPackageName();
            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "error_report@devbury.com" });
            sendIntent.putExtra(Intent.EXTRA_TEXT, mail_body);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            sendIntent.setType("message/rfc822");
            c.startActivity(Intent.createChooser(sendIntent, "Title:"));
        } else {
            Logger.debug("No error found from previous run");
        }
    }
}
