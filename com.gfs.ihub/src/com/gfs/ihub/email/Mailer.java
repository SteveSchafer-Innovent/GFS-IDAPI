package com.gfs.ihub.email;

import java.net.MalformedURLException;
import java.net.URL;

import com.innoventsolutions.idapihelper.IdapiHelper;
import com.innoventsolutions.idapihelper.IdapiHelperException;
import com.innoventsolutions.idapihelper.IdapiHelperImpl;

public class Mailer {
	private final IdapiHelper helper;

	public Mailer(final String urlString, final String volume,
			final String username, final String password)
			throws MalformedURLException, IdapiHelperException {
		final URL serverURL = new URL(urlString);
		helper = IdapiHelperImpl.getInstance(new URL[] { serverURL });
		helper.login(volume, username, password, new byte[0], false);
	}
}
