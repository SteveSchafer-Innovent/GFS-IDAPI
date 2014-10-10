package com.gfs.ihub.options;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @param args
 */
public abstract class PropertiesBasedOptions {
	final String configDirName;
	final String configFileName;
	final boolean propertiesFileExists;
	final Properties properties = new Properties();

	protected PropertiesBasedOptions(final String configDirName,
			final String configFileName) throws IOException {
		this.configDirName = configDirName;
		this.configFileName = configFileName;
		final java.io.File dir = new java.io.File(configDirName);
		if (!dir.exists()) {
			throw new FileNotFoundException(
					"Can't find the properties directory " + dir);
		}
		final java.io.File file = new java.io.File(dir, configFileName);
		final boolean fileExists = file.exists();
		if (!fileExists)
			throw new FileNotFoundException("Can't find the properties file "
					+ file);
		final FileInputStream fis = new FileInputStream(file);
		properties.load(fis);
		this.propertiesFileExists = fileExists;
	}

	public Properties getProperties() {
		return properties;
	}
}
