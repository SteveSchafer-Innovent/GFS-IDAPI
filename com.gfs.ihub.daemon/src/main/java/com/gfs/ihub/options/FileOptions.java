package com.gfs.ihub.options;

import java.io.IOException;

public class FileOptions extends PropertiesBasedOptions {
	final String storeDirName;
	final String altStoreDirName;
	
	public FileOptions(final String dirName, final String altDirName, final String fileName, final String storeDirName,
			final String altStoreDirName) throws IOException {
		super(dirName,  fileName);
		this.storeDirName = properties.getProperty("storeDirName", storeDirName);
		this.altStoreDirName = properties.getProperty("altStoreDirName", altStoreDirName);
	}

	@Override
	void setProperties() {
		properties.setProperty("storeDirName", storeDirName);
		properties.setProperty("altStoreDirName", altStoreDirName);
	}

	public String getStoreDirName() {
		return storeDirName;
	}

	public String getAltStoreDirName() {
		return altStoreDirName;
	}
}