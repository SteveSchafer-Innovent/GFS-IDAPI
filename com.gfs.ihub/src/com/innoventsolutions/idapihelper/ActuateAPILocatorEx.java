package com.innoventsolutions.idapihelper;

import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.log4j.Logger;

import com.actuate.schemas.ActuateAPILocator;
import com.actuate.schemas.ActuateSoapPort;
/**
 * @author Steve Schafer, Innovent Solutions
 * @version 1.0
 */
public class ActuateAPILocatorEx extends ActuateAPILocator implements
	ActuateAPIEx
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4203087338065934073L;
	private static final Logger LOGGER = Logger
		.getLogger(ActuateAPILocatorEx.class);
	private String authId = "";
	private String locale = "en_US";
	private String targetVolume = null;
	private byte[] connectionHandle = null;
	private Boolean delayFlush = null;
	private Call call = null;

	public ActuateAPILocatorEx()
	{
		super();
	}

	public ActuateSoapPort getActuateSoapPort(URL portAddress)
		throws ServiceException
	{
		LOGGER.debug("getActuateSoapPort " + portAddress);
		ActuateSoapPort result = super.getActuateSoapPort(portAddress);
		LOGGER.debug("getActuateSoapPort done");
		return result;
	}

	public javax.xml.rpc.Call createCall() throws ServiceException
	{
		call = (Call) super.createCall();
		addHeader("AuthId", authId);
		addHeader("Locale", locale);
		addHeader("TargetVolume", targetVolume);
		addHeader("ConnectionHandle", connectionHandle);
		addHeader("DelayFlush", delayFlush);
		return call;
	}

	private void addHeader(String name, Object value)
	{
		if (value != null)
			call.addHeader(new SOAPHeaderElement(null, name, value));
	}

	public String getAuthId()
	{
		return authId;
	}

	public Call getCall()
	{
		if (null == call)
		{
			try
			{
				createCall();
			}
			catch (ServiceException e)
			{
			}
		}
		return call;
	}

	public byte[] getConnectionHandle()
	{
		return connectionHandle;
	}

	public Boolean getDelayFlush()
	{
		return delayFlush;
	}

	public String getLocale()
	{
		return locale;
	}

	public String getTargetVolume()
	{
		return targetVolume;
	}

	public void setAuthId(String authId)
	{
		this.authId = authId;
	}

	public void setConnectionHandle(byte[] connectionHandle)
	{
		this.connectionHandle = connectionHandle;
	}

	public void setDelayFlush(Boolean delayFlush)
	{
		this.delayFlush = delayFlush;
	}

	public void setLocale(String locale)
	{
		this.locale = locale;
	}

	public void setTargetVolume(String targetVolume)
	{
		this.targetVolume = targetVolume;
	}
}
