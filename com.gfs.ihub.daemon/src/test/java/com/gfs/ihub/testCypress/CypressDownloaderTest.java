package com.gfs.ihub.testCypress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.gfs.ihub.cypress.CypressDownloader;
import com.gfs.ihub.options.ActuateOptions;
import com.gfs.ihub.options.CypressOptions;
import com.innoventsolutions.idapihelper.IdapiHelper;
import com.innoventsolutions.idapihelper.IdapiHelperException;

@SuppressWarnings("nls")
@RunWith(MockitoJUnitRunner.class)
public class CypressDownloaderTest {
    static final String CONFIG_DIR_NAME = "test_local";
    static final String ACTUATE_CONFIG_FILE_NAME = "actuate.properties";
    static final String CYPRESS_CONFIG_FILE_NAME = "cypress.properties";


    @Mock
    IdapiHelper helper;
    
    private ActuateOptions actuateOptions;
    private CypressOptions cypressOptions;

    private static CypressDownloader cypDownloader;
    

    @Before
    public void setUp() throws Exception {
    
    	
    	actuateOptions = new ActuateOptions(CONFIG_DIR_NAME, ACTUATE_CONFIG_FILE_NAME);
    	cypressOptions = new CypressOptions(CONFIG_DIR_NAME, CYPRESS_CONFIG_FILE_NAME);
    	cypDownloader = new CypressDownloader(actuateOptions, cypressOptions);
    }

    @Test
    public void cannotLogin() throws Exception {
        verifyLoginRequest();
    }


    private void verifyLoginRequest() throws IdapiHelperException {
//        verify(helper).login("Test Volume", "user", "password", new byte[0], false);
    }
}
