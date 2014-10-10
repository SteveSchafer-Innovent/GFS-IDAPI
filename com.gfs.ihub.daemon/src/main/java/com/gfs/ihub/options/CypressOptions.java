package com.gfs.ihub.options;

import java.io.IOException;

public class CypressOptions extends PropertiesBasedOptions {
	final String iHubCypressFolderName;
	final String osDestinationFolder;

	public CypressOptions(final String configDirName,
			final String configFileName) throws IOException {
		super(configDirName, configFileName);
		this.iHubCypressFolderName = properties
				.getProperty("iHubCypressFolderName");
		if (iHubCypressFolderName == null)
			throw new RuntimeException(
					"Cypress properties is missing iHubCypressFolderName");
		this.osDestinationFolder = properties
				.getProperty("osDestinationFolder");
		if (osDestinationFolder == null)
			throw new RuntimeException(
					"Cypress properties is missing osDestinationFolder");
	}

	public String getiHubCypressFolderName() {
		return iHubCypressFolderName;
	}

	public String getOsDestinationFolder() {
		return osDestinationFolder;
	}

}