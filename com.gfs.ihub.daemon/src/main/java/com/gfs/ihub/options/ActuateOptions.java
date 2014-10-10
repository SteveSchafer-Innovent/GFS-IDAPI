package com.gfs.ihub.options;

import java.io.IOException;

public class ActuateOptions extends PropertiesBasedOptions {
	final String urlString;
	final String volume;
	final String username;
	final String password;

	public ActuateOptions(final String configDirName, final String configFileName, final String urlString,
			final String volume, final String username, final String password) throws IOException {
		super(configDirName, configFileName);
		this.urlString = properties.getProperty("url", urlString);
		this.volume = properties.getProperty("volume", volume);
		this.username = properties.getProperty("username", username);
		this.password = properties.getProperty("password", password);
	}

	@Override
	void setProperties() {
		properties.setProperty("url", urlString);
		properties.setProperty("volume", volume);
		properties.setProperty("username", username);
		properties.setProperty("password", password);
	}

	public String getUrlString() {
		return urlString;
	}

	public String getVolume() {
		return volume;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
