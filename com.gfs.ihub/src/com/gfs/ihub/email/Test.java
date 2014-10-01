package com.gfs.ihub.email;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import com.actuate.schemas.ArrayOfString;
import com.actuate.schemas.SelectUsers;
import com.innoventsolutions.idapihelper.IdapiHelper;
import com.innoventsolutions.idapihelper.IdapiHelperException;
import com.innoventsolutions.idapihelper.IdapiHelperImpl;

public class Test {

	/**
	 * @param args
	 * @throws IdapiHelperException
	 * @throws MalformedURLException
	 * @throws RemoteException
	 */
	public static void main(final String[] args) throws MalformedURLException,
			IdapiHelperException, RemoteException {
		// TODO Auto-generated method stub
		final IdapiHelper helper = IdapiHelperImpl
				.getInstance(new URL[] { new URL("") });
		final SelectUsers selectUsers = new SelectUsers();
		selectUsers.setId("");
		final ArrayOfString aosResultDef = new ArrayOfString();
		aosResultDef.setString(new String[] {});
		selectUsers.setResultDef(aosResultDef);
		if (true) {
			class A {
			}
			final A a = new A();
		}
		helper.selectUsers(selectUsers);
	}
}
