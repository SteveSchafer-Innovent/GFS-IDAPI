package com.gfs.ihub.options;

import java.io.IOException;

public class CypressOptions extends PropertiesBasedOptions {
	final String iHubCypressFolderName;
	final String osDestinationFolder;
	
	public CypressOptions(final String dirName, final String altDirName, final String fileName, final String iHubCypressFolderName,
			final String osDestinationFolder) throws IOException {
		super(dirName, fileName);
		this.iHubCypressFolderName = properties.getProperty("iHubCypressFolderName", iHubCypressFolderName);
		this.osDestinationFolder = properties.getProperty("osDestinationFolder", osDestinationFolder);
	}

	@Override
	void setProperties() {
		properties.setProperty("iHubCypressFolderName", iHubCypressFolderName);
		properties.setProperty("osDestinationFolder", osDestinationFolder);
	}

	public String getiHubCypressFolderName() {
		return iHubCypressFolderName;
	}

	public String getOsDestinationFolder() {
		return osDestinationFolder;
	}


}