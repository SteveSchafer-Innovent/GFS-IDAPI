package com.gfs.ihub.options;

import java.io.IOException;

public class SqlOptions extends PropertiesBasedOptions {
	final String urlString;
	final String username;
	final String password;

	public SqlOptions(final String configDirName, final String configFileName)
			throws IOException {
		super(configDirName, configFileName);
		this.urlString = properties.getProperty("url");
		if (urlString == null)
			throw new RuntimeException("SQL properties is missing url");
		this.username = properties.getProperty("username");
		if (username == null)
			throw new RuntimeException("SQL properties is missing username");
		this.password = properties.getProperty("password");
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
