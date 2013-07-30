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

package com.devbury.mkremote.server.boxee;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devbury.mkremote.server.NetworkAddressService;
import com.devbury.mkremote.server.OptionsStorageService;

public class BoxeeService implements IBoxeeService {

	private Logger logger = LoggerFactory.getLogger(BoxeeService.class);
	private String prefix;
	private OptionsStorageService optionsStorageService;
	private NetworkAddressService networkAddressService;

	protected class BoxeeThread extends Thread {
		public String command;
		public String response;

		@Override
		public void run() {
			try {
				URL u = new URL(prefix + command);
				logger.debug("Command is {}", u.toString());
				URLConnection uc = u.openConnection();
				uc.setConnectTimeout(1000);
				InputStream is = uc.getInputStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int i = -1;
				while ((i = is.read()) != -1) {
					if (i != '\n') {
						bos.write(i);
					}
				}
				response = bos.toString();
				response = response.replace("<html><li>", "");
				response = response.replace("</html>", "");
				logger.debug("Response is {}", response);
			} catch (Throwable t) {
				logger.debug("Error connecting.  Error was {}", t.toString());
			}
		}

	}

	public void init() {
		// load port and address
		StringBuffer sb = new StringBuffer("http://");
		try {
			sb.append(networkAddressService.getNetworkAddress()
					.getHostAddress());
		} catch (Throwable t) {
			logger.debug("Could not determine network address.  Setting to 127.0.0.1");
			sb.append("127.0.0.1");
		}
		sb.append(':');
		sb.append(optionsStorageService.getBoxeePort());
		sb.append("/xbmcCmds/xbmcHttp?command=");
		prefix = sb.toString();
	}

	protected void sendCommand(String c) {
		BoxeeThread bt = new BoxeeThread();
		bt.command = c;
		bt.start();
	}

	protected Object sendCommandForData(String c) {
		BoxeeThread bt = new BoxeeThread();
		bt.command = c;
		bt.run();
		return bt.response;
	}

	public void select() {
		sendCommand("SendKey(256)");
	}

	public void back() {
		sendCommand("SendKey(275)");
	}

	public void down() {
		sendCommand("SendKey(271)");
	}

	public int getPercentage() {
		try {
			return Integer.parseInt(sendCommandForData("GetPercentage()")
					.toString());
		} catch (Throwable e) {
			return -1;
		}
	}

	public int getVolume() {
		try {
			return Integer.parseInt(sendCommandForData("GetVolume()")
					.toString());
		} catch (Throwable t) {
			return 0;
		}
	}

	public void left() {
		sendCommand("SendKey(272)");
	}

	public void mute() {
		sendCommand("Mute()");
	}

	public void pause() {
		sendCommand("Pause()");
	}

	public void playNext() {
		sendCommand("PlayNext()");
	}

	public void playPrev() {
		sendCommand("PlayPrev()");
	}

	public void right() {
		sendCommand("SendKey(273)");
	}

	public void seekPercentage(int percent) {
		sendCommand("SeekPercentage(" + percent + ")");
	}

	public void seekPercentageRealtive(int percent) {
		sendCommand("SeekPercentageRelative(" + percent + ")");
	}

	public void setVolume(int percent) {
		sendCommand("SetVolume(" + percent + ")");
	}

	public void stop() {
		sendCommand("Stop()");
	}

	public void up() {
		sendCommand("SendKey(270)");
	}

	public OptionsStorageService getOptionsStorageService() {
		return optionsStorageService;
	}

	public void setOptionsStorageService(
			OptionsStorageService optionsStorageService) {
		this.optionsStorageService = optionsStorageService;
	}

	public NetworkAddressService getNetworkAddressService() {
		return networkAddressService;
	}

	public void setNetworkAddressService(
			NetworkAddressService networkAddressService) {
		this.networkAddressService = networkAddressService;
	}
}
