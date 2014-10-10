package com.gfs.ihub.options;

import java.io.IOException;

public class SqlOptions extends PropertiesBasedOptions {
	final String urlString;
	final String username;
	final String password;

	public SqlOptions(final String configDirName, final String configFileName, final String urlString, final String username,
			final String password) throws IOException {
		super(configDirName,  configFileName);
		this.urlString = properties.getProperty("url", urlString);
		this.username = properties.getProperty("username", username);
		this.password = properties.getProperty("password", password);
	}

	@Override
	void setProperties() {
		properties.setProperty("url", urlString);
		properties.setProperty("username", username);
		properties.setProperty("password", password);
	}

	public String getUrlString() {
		return urlString;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
