package com.gfs.ihub.testCypress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.gfs.ihub.cypress.CypressDownloader;
import com.gfs.ihub.options.ActuateOptions;
import com.gfs.ihub.options.CypressOptions;
import com.innoventsolutions.idapihelper.IdapiHelperException;

public class TestCypress {

	private String CONFIG_DIR ;

	@Before
	public void setup(){
		CONFIG_DIR = "test_gfs_sit";
		
	}
	
	@Test
	public void test() {
		assertTrue(true);
	}

	@Test
	public void testSuccess() {
		assertTrue(true);
	}

	@Test
	public void testActuateConfigWithDefaults() throws IOException {

		String actu_url = "http://hippo.gfsprod.nt.gfs.com:8000";
		String actu_vol = "GFS Operational Reporting";
		String actu_user = "Administrator";
		String actu_pass = "3actBIRT#";
		final ActuateOptions actuateOptions = new ActuateOptions(CONFIG_DIR, "actuate.properties");

		assertEquals(actuateOptions.getPassword(), actu_pass);
		assertEquals(actuateOptions.getUrlString(), actu_url);
		assertEquals(actuateOptions.getUsername(), actu_user);
		assertEquals(actuateOptions.getVolume(), actu_vol);
		
	}

	
	public void testCypressConfig() throws IOException {
		String iHubCypressFolderName = "/send_to_cypress";
		String osDestinationFolder = "\\\\wolverine\\FTin\\Operational Reporting\\";
		final CypressOptions cypressOptions = new CypressOptions(CONFIG_DIR, "cypress.properties");
		assertEquals(cypressOptions.getiHubCypressFolderName(), iHubCypressFolderName);
		assertEquals(cypressOptions.getOsDestinationFolder(), osDestinationFolder);
	}
	
	
	public void testLogin() throws IOException, IdapiHelperException {

		final ActuateOptions actuateOptions = new ActuateOptions(CONFIG_DIR, "actuate.properties");
		final CypressOptions cypressOptions = new CypressOptions(CONFIG_DIR, "cypress.properties");
		CypressDownloader cypressDownloader = new CypressDownloader(actuateOptions, cypressOptions);
		
		cypressDownloader.downloadFiles();
		
	} 

}
