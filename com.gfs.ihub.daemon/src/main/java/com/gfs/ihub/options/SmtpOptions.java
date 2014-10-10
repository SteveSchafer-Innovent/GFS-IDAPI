package com.gfs.ihub.options;

import java.io.IOException;

public class SmtpOptions extends PropertiesBasedOptions {
	final String host;
	final int port;
	final String username;
	final String password;
	final boolean enableSSL;
	final boolean enableSTARTTLS;
	final boolean auth;
	final String defaultFrom;

	public SmtpOptions(final String configDirName, final String configFileName)
			throws IOException {
		super(configDirName, configFileName);
		this.host = properties.getProperty("mail.smtp.host");
		if (host == null)
			throw new RuntimeException("SMTP properties is missing host");
		final String portString = properties.getProperty("mail.smtp.port",
				String.valueOf(25));
		this.port = Integer.parseInt(portString);
		this.username = properties.getProperty("username");
		this.password = properties.getProperty("password");
		this.enableSSL = "true".equalsIgnoreCase(properties.getProperty(
				"mail.smtp.ssl.enable", String.valueOf(false)));
		this.enableSTARTTLS = "true".equalsIgnoreCase(properties.getProperty(
				"mail.smtp.starttls.enable", String.valueOf(false)));
		this.auth = "true".equalsIgnoreCase(properties.getProperty(
				"mail.smtp.auth", String.valueOf(false)));
		this.defaultFrom = properties.getProperty("mail.user",
				"noreply@gfs.com");
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean isEnableSSL() {
		return enableSSL;
	}

	public boolean isEnableSTARTTLS() {
		return enableSTARTTLS;
	}

	public boolean isAuth() {
		return auth;
	}

	public String getDefaultFrom() {
		return defaultFrom;
	}
}
