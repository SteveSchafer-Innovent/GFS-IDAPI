package com.gfs.ihub.options;

import java.io.IOException;

public class ActuateOptions extends PropertiesBasedOptions {
	final String urlString;
	final String volume;
	final String username;
	final String password;

	public ActuateOptions(final String configDirName,
			final String configFileName) throws IOException {
		super(configDirName, configFileName);
		this.urlString = properties.getProperty("url");
		if (urlString == null)
			throw new RuntimeException("Actuate properties is missing url");
		this.volume = properties.getProperty("volume");
		this.username = properties.getProperty("username");
		if (username == null)
			throw new RuntimeException("Actuate properties is missing username");
		this.password = properties.getProperty("password");
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
