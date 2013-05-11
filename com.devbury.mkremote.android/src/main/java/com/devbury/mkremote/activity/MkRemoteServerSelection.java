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

package com.devbury.mkremote.activity;

import java.util.ArrayList;

import com.devbury.android.DialogUtils;
import com.devbury.logging.Logger;
import com.devbury.mkremote.CustomServerDiscoveryService;
import com.devbury.mkremote.IMulticastLockService;
import com.devbury.mkremote.InvalidPasswordException;
import com.devbury.mkremote.MkRemoteApplication;
import com.devbury.mkremote.MkRemotePreferences;
import com.devbury.mkremote.MulticastLockService;
import com.devbury.mkremote.R;
import com.devbury.mkremote.api.Bluetooth;
import com.devbury.mkremote.api.PasswordStorageService;
import com.devbury.mkremote.api.ServiceInfo;
import com.devbury.mkremote.connections.DiscoveryService;
import com.devbury.mkremote.connections.HistoryDiscoveryService;
import com.devbury.mkremote.connections.ServerConnection;
import com.devbury.mkremote.connections.ServerConnectionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class MkRemoteServerSelection extends ListActivity implements OnSharedPreferenceChangeListener {

    private static final int NO_SERVICES_DIALOG = 100;
    private static final int PASSWORD_DIALOG = 102;
    private static final int INVALID_PASSWORD = 103;
    private static final int CUSTOM_SERVER_DIALOG = 104;
    private static final int INVALID_CUSTOM_SERVER_DIALOG = 105;
    private static final int CONNECTION_ERROR_DIALOG = 106;
    private static final String CUSTOM_HOST = "customHost";
    private static final String CUSTOM_PORT = "customPort";
    private static final String DEFAULT_SERVICE_NAME = "com.devbury.mkRemote";
    private PasswordStorageService passwordStorageService;
    private MkRemoteApplication app;
    private ServiceInfo serviceInfo;
    private ProgressDialog progressDialog;
    private DiscoveryService discoveryService;
    private IMulticastLockService multicastLockService;
    private EditText password;
    private EditText customHost;
    private EditText customPort;
    private HistoryDiscoveryService historyDiscoveryService;
    private boolean hasMulticastLock = false;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(MkRemotePreferences.cs(R.string.c_notification_bar_on))) {
            app.windowAttributeHelper.processNotificationBar(getWindow(), sharedPreferences.getBoolean(key, false));
        }
    }

    protected void configureDependencies() {
        app = (MkRemoteApplication) getApplication();
        progressDialog = new ProgressDialog(this) {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                return true;
            }

            @Override
            public boolean onKeyUp(int keyCode, KeyEvent event) {
                return true;
            }
        };
        progressDialog.setMessage(getResources().getText(R.string.looking_for_servers));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        CustomServerDiscoveryService ds = new CustomServerDiscoveryService();
        ds.setDiscoveryService(app.discoveryService);
        ds.setContext(this);
        discoveryService = ds;
        if (app.sdk > 3) {
            Logger.debug("Using MulticastLock");
            multicastLockService = new MulticastLockService(this);
            hasMulticastLock = true;
        } else {
            Logger.debug("Using no-op MulticastLock");
            multicastLockService = new IMulticastLockService() {
                @Override
                public void acquireLock() {
                }

                @Override
                public void releaseLock() {
                }
            };
        }

        passwordStorageService = app.passwordStorageService;

        historyDiscoveryService = app.historyDiscoveryService;
    }

    protected boolean showNoServicesDialog(ArrayList<ServiceInfo> services) {
        if (services.size() <= 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.debug("Unregistering");
        app.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureDependencies();
        onSharedPreferenceChanged(app.sharedPreferences, MkRemotePreferences.cs(R.string.c_notification_bar_on));
        app.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        multicastLockService.acquireLock();
        progressDialog.show();
        new SearchThread(this).start();
    }

    public void onPause() {
        super.onPause();
        progressDialog.dismiss();
        multicastLockService.releaseLock();
    }

    protected void serviceSelected(ServiceInfo serviceInfo) {
        if (serviceInfo.getVersionCode() >= app.minServerVersion) {
            this.serviceInfo = serviceInfo;
            if (getString(R.string.manual_address_id).equals(serviceInfo.getId())) {
                freshShowDialog(CUSTOM_SERVER_DIALOG);
            } else {
                try {
                    if (isValidPassword(passwordStorageService.getPassword(serviceInfo.getId()))) {
                        stopActivity();
                    } else {
                        freshShowDialog(PASSWORD_DIALOG);
                    }
                } catch (Throwable t) {
                    freshShowDialog(CONNECTION_ERROR_DIALOG);
                }
            }
        }
    }

    protected void stopActivity() {
        app.serviceInfo = serviceInfo;
        app.sharedPreferences.edit().putString(ServerConnectionActivity.LAST_HOST_ID, serviceInfo.getId()).putString(
                ServerConnectionActivity.LAST_HOST_IP, serviceInfo.getAddress()).putInt(
                ServerConnectionActivity.LAST_HOST_PORT, serviceInfo.getPort()).commit();

        finish();
    }

    protected void freshShowDialog(int id) {
        DialogUtils.showDialog(this, id);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog d = super.onCreateDialog(id);
        if (id == NO_SERVICES_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_servers_found);
            if (hasMulticastLock) {
                builder.setMessage(R.string.no_servers_found_summary);
            } else {
                builder.setMessage(R.string.no_servers_found_1_5_summary);
            }
            builder.setPositiveButton(R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    serviceInfo = null;
                    dialog.dismiss();
                }
            });
            d = builder.create();
        } else if (id == PASSWORD_DIALOG) {
            password = new EditText(this);
            password.setTransformationMethod(new PasswordTransformationMethod());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.password);
            builder.setView(password);
            builder.setPositiveButton(R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (isValidPassword(password.getText().toString())) {
                        addToHistory(serviceInfo);
                        stopActivity();
                    } else {
                        dialog.dismiss();
                        freshShowDialog(INVALID_PASSWORD);
                    }
                }
            });
            d = builder.create();
        } else if (id == INVALID_PASSWORD) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.invalid_password);
            builder.setMessage(R.string.invalid_password_message);
            builder.setPositiveButton(R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    freshShowDialog(PASSWORD_DIALOG);
                }
            });
            d = builder.create();
        } else if (id == CUSTOM_SERVER_DIALOG) {
            customHost = new EditText(this);
            customPort = new EditText(this);
            TableLayout l = new TableLayout(this);
            TableRow tr = new TableRow(this);
            TextView label = new TextView(this);
            label.setText(R.string.server_ip);
            tr.addView(label);
            tr.addView(customHost);
            l.addView(tr);
            tr = new TableRow(this);
            label = new TextView(this);
            label.setText(R.string.server_port);
            tr.addView(label);
            tr.addView(customPort);
            l.addView(tr);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.manual_address_id);
            builder.setView(l);
            builder.setPositiveButton(R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String host_string = customHost.getText().toString().trim();
                    String port_string = customPort.getText().toString().trim();
                    if (!host_string.contains(".")) {
                        host_string = Bluetooth.formatAddress(host_string);
                        customHost.setText(host_string);
                    }
                    Editor e = app.sharedPreferences.edit();
                    e.putString(CUSTOM_HOST, host_string);
                    e.putString(CUSTOM_PORT, port_string);
                    e.commit();
                    ServiceInfo si = new ServiceInfo();
                    si.setId(host_string);
                    si.setAddress(si.getId());
                    si.setName(DEFAULT_SERVICE_NAME);
                    serviceInfo = si;
                    dialog.dismiss();
                    try {
                        si.setPort(Integer.parseInt(port_string));
                    } catch (NumberFormatException ex2) {
                        freshShowDialog(INVALID_CUSTOM_SERVER_DIALOG);
                        return;
                    }
                    try {
                        if (isValidPassword(passwordStorageService.getPassword(si.getId()))) {
                            addToHistory(serviceInfo);
                            stopActivity();
                        } else {
                            freshShowDialog(PASSWORD_DIALOG);
                        }
                    } catch (ServerConnectionException ex) {
                        freshShowDialog(INVALID_CUSTOM_SERVER_DIALOG);
                    }
                }
            });
            d = builder.create();
        } else if (id == INVALID_CUSTOM_SERVER_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.invalid_custom_server);
            builder.setMessage(R.string.invalid_custom_server_summary);
            builder.setPositiveButton(R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    serviceInfo = null;
                    dialog.dismiss();
                }
            });
            d = builder.create();
        } else if (id == CONNECTION_ERROR_DIALOG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.connection_error);
            builder.setMessage(R.string.connection_error_message);
            builder.setPositiveButton(R.string.ok, null);
            d = builder.create();
        }
        return d;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == CUSTOM_SERVER_DIALOG) {
            customHost.setText(app.sharedPreferences.getString(CUSTOM_HOST, "192.168."));
            customPort.setText(app.sharedPreferences.getString(CUSTOM_PORT, "5555"));
        }
    }

    protected void addToHistory(ServiceInfo si) {
        if (historyDiscoveryService != null) {
            historyDiscoveryService.addToHistory(si);
        }
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        serviceSelected((ServiceInfo) l.getAdapter().getItem(position));
    }

    protected boolean isValidPassword(String password) {
        try {
            ServerConnection sc = app.serverConnectionFactory.getConnection(serviceInfo, password);
            app.serverConnectionFactory.close(sc);
            // password is valid.
            passwordStorageService.savePassword(serviceInfo.getId(), password);
            return true;
        } catch (InvalidPasswordException e) {
            return false;
        }
    }

    protected long now() {
        return System.currentTimeMillis();
    }

    public class SearchThread extends Thread {

        private Handler handler;
        private Context context;
        private ArrayList<ServiceInfo> services;

        public SearchThread(Context c) {
            context = c;
            handler = new Handler();
        }

        @Override
        public void run() {
            services = discoveryService.find(DEFAULT_SERVICE_NAME);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    if (showNoServicesDialog(services)) {
                        freshShowDialog(NO_SERVICES_DIALOG);
                    }
                    setListAdapter(new ArrayAdapter<ServiceInfo>(context, android.R.layout.simple_list_item_1, services) {
                        public View getView(int position, View convertView, ViewGroup parent) {
                            ServiceInfo si = services.get(position);
                            String os = si.getAttributes().getProperty("os.name");
                            LinearLayout ll = new LinearLayout(context);
                            ll.setOrientation(LinearLayout.VERTICAL);
                            View ret = ll;
                            int image_id = -1;
                            if (os != null) {
                                os = os.toLowerCase();
                                if (os.contains("mac")) {
                                    image_id = R.drawable.mac;
                                } else {
                                    if (os.contains("linux")) {
                                        image_id = R.drawable.linux;
                                    } else {
                                        if (os.contains("windows")) {
                                            image_id = R.drawable.windows;
                                        }
                                    }
                                }
                            }
                            if (image_id != -1) {
                                LinearLayout h = new LinearLayout(context);
                                h.setOrientation(LinearLayout.HORIZONTAL);
                                h.setGravity(Gravity.CENTER_VERTICAL);
                                ImageView iv = new ImageView(context);
                                iv.setImageResource(image_id);
                                h.addView(iv);
                                h.addView(ll);
                                ret = h;
                            }
                            TextView server_id = new TextView(context);
                            server_id.setTextSize((float) 20.0);
                            server_id.setTypeface(Typeface.DEFAULT_BOLD);
                            server_id.setText(si.getId());
                            ll.addView(server_id);
                            TextView ip = new TextView(context);
                            ip.setTextSize((float) 14.0);
                            ip.setTypeface(Typeface.DEFAULT_BOLD);
                            if (si.getVersionCode() >= app.minServerVersion) {
                                ip.setText(si.getAddress());
                            } else {
                                ip.setText(R.string.server_upgrade);
                            }
                            ll.addView(ip);
                            return ret;
                        }
                    });
                }
            });
        }
    }
}
