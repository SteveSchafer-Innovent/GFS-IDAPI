package com.gfs.ihub.options;

import java.io.IOException;

public class FileOptions extends PropertiesBasedOptions {
	final String storeDirName;
	
	public FileOptions(final String configDirName, final String configFileName, final String storeDirName) throws IOException {
		super(configDirName,  configFileName);
		this.storeDirName = properties.getProperty("storeDirName", storeDirName);
	}

	@Override
	void setProperties() {
		properties.setProperty("storeDirName", storeDirName);
	}

	public String getStoreDirName() {
		return storeDirName;
	}

}