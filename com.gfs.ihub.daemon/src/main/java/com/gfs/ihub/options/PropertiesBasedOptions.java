package com.gfs.ihub.options;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @param args
 */
public abstract class PropertiesBasedOptions {
	final String dirName;
	final String fileName;
	final boolean propertiesFileExists;
	final Properties properties = new Properties();

	protected PropertiesBasedOptions(final String dirName, final String fileName) throws IOException {
		this.dirName = dirName;
		this.fileName = fileName;
		boolean fileExists = true;
		java.io.File dir = new java.io.File(dirName);
		if (!dir.exists()) {
			throw new RuntimeException("Can't create the properties directory");
		}
		this.propertiesFileExists = fileExists;
	}

	abstract void setProperties();

	public void store() throws IOException {
		setProperties();
		java.io.File dir = new java.io.File(dirName);
		if (!dir.exists()) {
			throw new RuntimeException("Can't create the properties file");
		}
		final java.io.File file = new java.io.File(dir, fileName);
		final FileOutputStream fos = new FileOutputStream(file);
		try {
			properties.store(fos, "");
		} finally {
			fos.close();
		}
	}

	public Properties getProperties() {
		return properties;
	}
}
