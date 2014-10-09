package com.innoventsolutions.idapihelper;

import org.apache.axis.client.Call;

import com.actuate.schemas.ActuateAPI;

/**
 * @author Steve Schafer, Innovent Solutions
 * @version 1.0
 */
interface ActuateAPIEx extends ActuateAPI
{
	void setAuthId(String value);

	void setLocale(String value);

	void setTargetVolume(String value);

	void setConnectionHandle(byte[] value);

	void setDelayFlush(Boolean value);

	String getAuthId();

	String getLocale();

	String getTargetVolume();

	byte[] getConnectionHandle();

	Boolean getDelayFlush();

	Call getCall();
}
