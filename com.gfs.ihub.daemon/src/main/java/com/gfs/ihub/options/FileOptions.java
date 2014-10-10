package com.gfs.ihub.options;

import java.io.IOException;

public class FileOptions extends PropertiesBasedOptions {
	final String storeDirName;

	public FileOptions(final String configDirName, final String configFileName)
			throws IOException {
		super(configDirName, configFileName);
		this.storeDirName = properties.getProperty("storeDirName");
		if (storeDirName == null)
			throw new RuntimeException(
					"File properties is missing storeDirName");
	}

	public String getStoreDirName() {
		return storeDirName;
	}

}