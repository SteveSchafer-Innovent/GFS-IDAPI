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

	public SmtpOptions(final String configDirName,  final String configFileName, final String host, final int port,
			final String username, final String password, final boolean enableSSL, final boolean enableSTARTTLS, final boolean auth,
			final String defaultFrom) throws IOException {
		super(configDirName, configFileName);
		this.host = properties.getProperty("mail.smtp.host", host);
		this.port = Integer.parseInt(properties.getProperty("mail.smtp.port", String.valueOf(port)));
		this.username = properties.getProperty("username", username);
		this.password = properties.getProperty("password", password);
		this.enableSSL = "true".equalsIgnoreCase(properties.getProperty("mail.smtp.ssl.enable", String.valueOf(enableSSL)));
		this.enableSTARTTLS = "true".equalsIgnoreCase(properties.getProperty("mail.smtp.starttls.enable", String.valueOf(enableSTARTTLS)));
		this.auth = "true".equalsIgnoreCase(properties.getProperty("mail.smtp.auth", String.valueOf(auth)));
		this.defaultFrom = properties.getProperty("mail.user", defaultFrom);
	}

	@Override
	void setProperties() {
		properties.setProperty("mail.smtp.host", host);
		properties.setProperty("mail.smtp.port", String.valueOf(port));
		if (username != null)
			properties.setProperty("username", username);
		else
			properties.remove("username");
		if (password != null)
			properties.setProperty("password", password);
		else
			properties.remove("password");
		properties.setProperty("mail.smtp.ssl.enable", String.valueOf(enableSSL));
		properties.setProperty("mail.smtp.starttls.enable", String.valueOf(enableSTARTTLS));
		properties.setProperty("mail.smtp.auth", String.valueOf(auth));
		properties.setProperty("mail.user", defaultFrom);
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
