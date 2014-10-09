package com.innoventsolutions.idapihelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Stub;
import org.apache.axis.encoding.Base64;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.actuate.schemas.ActuateSoapPort;
import com.actuate.schemas.Login;
import com.actuate.schemas.LoginResponse;
import com.actuate.schemas.SystemLogin;
import com.actuate.schemas.SystemLoginResponse;
import com.actuate.schemas.User;

/**
 * An invocation handler for dynamic proxy classes created to implement the
 * ActuateSoapPort and IdapiHelper interfaces. The IdapiHelper interfaces are
 * implemented here as well.
 * 
 * @author Steve Schafer / Innovent Solutions
 * @version 1.0
 */
public class IdapiHelperImpl implements InvocationHandler
{
	private static final Logger LOGGER = Logger
		.getLogger(IdapiHelperImpl.class);
	private ActuateAPIEx api = null;
	private ActuateSoapPort port = null;
	private URL[] serverURL = null;
	private String userName = null;
	private String volume = null;
	private String password = null;
	private byte[] ec = null;
	private long loginTime = 0;
	private long connectTime = 0;
	private boolean getUserData;
	private User user = null;
	private int currentUrlIndex;
	private int timeout = 0;

	private IdapiHelperImpl(URL[] server) throws IdapiHelperException
	{
		setServerURL(server);
	}

	/**
	 * Sets the array of server URLs. Each URL points to the SOAP port of an
	 * Actuate report server. The first URL is normally used. Secondary URLs are
	 * only used in case of failure.
	 * 
	 * @param serverURL
	 * @throws IdapiHelperException
	 */
	public void setServerURL(URL[] serverURL) throws IdapiHelperException
	{
		if (LOGGER.getEffectiveLevel().isGreaterOrEqual(Level.DEBUG))
		{
			StringBuffer buf = new StringBuffer();
			String sep = "";
			buf.append("setting server URL list to [");
			for (int i = 0; i < serverURL.length; i++)
			{
				buf.append(sep);
				sep = ", ";
				buf.append(serverURL[i]);
			}
			buf.append("]");
			LOGGER.debug(buf);
		}
		this.serverURL = serverURL;
		currentUrlIndex = -1;
		api = new ActuateAPILocatorEx();
		try
		{
			rotatePort();
		}
		catch (RemoteException e)
		{
			throw new IdapiHelperException("Failed to rotate port", e);
		}
		connectTime = System.currentTimeMillis();
		LOGGER.debug("Connected to URL " + currentUrlIndex + ": "
			+ serverURL[currentUrlIndex]);
	}

	/**
	 * Throws a NotReadyException if the serverURLs have not been set.
	 * 
	 * @throws NotReadyException
	 */
	public void checkReady() throws NotReadyException
	{
		if (!isReady())
		{
			throw new NotReadyException();
		}
	}

	/**
	 * Returns true if the server URLs have been set and the helper is ready to
	 * accept a login.
	 * 
	 * @return
	 */
	public boolean isReady()
	{
		return port != null;
	}

	/**
	 * Re-log in using the previous credentials.
	 * 
	 * @throws IdapiHelperException
	 */
	public void reconnect() throws IdapiHelperException
	{
		login(volume, userName, password, ec, getUserData);
	}

	/**
	 * Sets the authentation ID. This can be used instead of loggin in if a
	 * valid authId is available. AuthIds are assigned by the server upon login
	 * and last for 24 hours.
	 * 
	 * @param authId
	 * @throws NotReadyException
	 */
	public void setAuthId(String authId) throws NotReadyException
	{
		checkReady();
		api.setAuthId(authId);
		this.getUserData = false;
		this.user = null;
		this.userName = null;
		this.volume = null;
		this.password = null;
		this.ec = null;
		loginTime = System.currentTimeMillis();
	}

	public String getAuthId()
	{
		return api == null ? null : api.getAuthId();
	}
	
	/**
	 * A login method that is easier to use than the one provided in
	 * ActuateSoapPort
	 * 
	 * @param volume
	 * @param user
	 * @param password
	 * @param extendedCredentials
	 * @param getUserData
	 * @throws IdapiHelperException
	 */
	public void login(String volume, String user, String password,
		byte[] extendedCredentials, boolean getUserData)
		throws IdapiHelperException
	{
		checkReady();
		Login login = new Login();
		login.setUser(user);
		if (extendedCredentials != null && extendedCredentials.length > 0)
		{
			login.setCredentials(extendedCredentials);
		}
		login.setPassword(password);
		if (volume != null && volume.length() > 0)
		{
			api.setTargetVolume(volume);
			login.setDomain(volume);
		}
		login.setUserSetting(new Boolean(getUserData));
		try
		{
			login(login);
		}
		catch (RemoteException e)
		{
			throw new IdapiHelperException("Failed to log in", e);
		}
	}

	/**
	 * This replaces the login method of ActuateSoapPort in order to capture
	 * certain information from the response.
	 * 
	 * @param login
	 * @return
	 * @throws RemoteException
	 */
	public LoginResponse login(Login login) throws RemoteException
	{
		LoginResponse loginResponse = null;
		RemoteException exception = null;
		for (int i = 0; i < serverURL.length && loginResponse == null
			&& exception == null; i++)
		{
			try
			{
				loginResponse = port.login(login);
			}
			catch (RemoteException remoteException)
			{
				exception = remoteException;
				LOGGER.error("login - " + remoteException);
				rotatePort();
			}
		}
		if (exception != null)
		{
			throw exception;
		}
		api.setAuthId(loginResponse.getAuthId());
		Boolean booleanObject = login.getUserSetting();
		this.getUserData = booleanObject == null ? false : booleanObject
			.booleanValue();
		if (getUserData)
		{
			this.user = loginResponse.getUser();
		}
		this.userName = login.getUser();
		this.volume = login.getDomain();
		this.password = login.getPassword();
		this.ec = login.getCredentials();
		this.loginTime = System.currentTimeMillis();
		return loginResponse;
	}

	/**
	 * Log in to system administration
	 * 
	 * @param password
	 * @throws NotReadyException
	 */
	public void systemLogin(String password) throws IdapiHelperException
	{
		checkReady();
		SystemLogin systemLogin = new SystemLogin();
		systemLogin.setSystemPassword(password);
		systemLogin.setSystemPasswordEncryptLevel(new Long(0));
		SystemLoginResponse loginResponse;
		try
		{
			loginResponse = port.systemLogin(systemLogin);
		}
		catch (RemoteException e)
		{
			throw new IdapiHelperException("Failed to log in", e);
		}
		api.setAuthId(loginResponse.getAuthId());
		this.getUserData = false;
		this.user = null;
		this.userName = "Administrator";
		this.volume = null;
		this.password = password;
		this.ec = null;
		loginTime = System.currentTimeMillis();
	}


	public void rotatePort() throws RemoteException
	{
		port = null;
		for (int i = 0; i < serverURL.length && port == null; i++)
		{
			if (currentUrlIndex >= 0)
			{
				LOGGER.warn("Rotating port");
			}
			currentUrlIndex++;
			if (currentUrlIndex >= serverURL.length)
			{
				currentUrlIndex = 0;
			}
			URL url = serverURL[currentUrlIndex];
			try
			{
				port = api.getActuateSoapPort(url);
			}
			catch (ServiceException serviceException)
			{
				LOGGER.warn("Failed to connect to URL " + currentUrlIndex
					+ ": " + url);
			}
		}
		if (port == null)
		{
			throw new RemoteException("Failed to connect");
		}
		setPortTimeout();
		LOGGER.debug("Using " + serverURL[currentUrlIndex]);
	}

	private void setPortTimeout()
	{
		if (port != null && port instanceof Stub)
		{
			Stub stub = (Stub) port;
			stub.setTimeout(timeout);
			LOGGER.debug("Setting stub timeout to " + timeout);
		}
	}

	public String getUsername()
	{
		return userName;
	}

	public String getVolume()
	{
		return volume;
	}

	public byte[] getConnectionHandle()
	{
		return api.getConnectionHandle();
	}

	/**
	 * Set the connection handle. Connection handles are assigned by the report
	 * server when a report is viewed and are used to optimize access while
	 * viewing subsequent pages or performing other view operations.
	 * 
	 * @param value
	 */
	public void setConnectionHandle(byte[] value)
	{
		api.setConnectionHandle(value);
	}

	public long getConnectTime()
	{
		return connectTime;
	}

	public long getLoginTime()
	{
		return loginTime;
	}

	public User getUser()
	{
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(serverURL);
		if (volume != null)
		{
			buf.append("/");
			buf.append(volume);
		}
		if (userName != null)
		{
			buf.append("/");
			buf.append(userName);
		}
		if (ec != null && ec.length > 0)
		{
			buf.append("/");
			buf.append(Base64.encode(ec));
		}
		return buf.toString();
	}

	/*
	 * (non-Javadoc) For each method invocation, first check IdapiHelperImpl
	 * (this). If the method is not implemented here then check the contained
	 * ActuateSoapPort variable, port. For methods implemented by
	 * ActuateSoapPort, if they throw an UnknownHostException, try alternate
	 * ports.
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 *      java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args)
		throws Throwable
	{
		String methodName = method.getName();
		@SuppressWarnings("rawtypes")
		Class[] methodParams = method.getParameterTypes();
		try
		{
			Method thisMethod = getClass().getMethod(methodName, methodParams);
			return thisMethod.invoke(this, args);
		}
		catch (NoSuchMethodException e)
		{
			// fall through to try an ActuateSoapPort method
		}
		catch (InvocationTargetException e)
		{
			throw e.getTargetException();
		}
		Method portMethod = ActuateSoapPort.class.getMethod(methodName,
			methodParams);
		Throwable throwable = null;
		for (int i = 0; i < serverURL.length; i++)
		{
			try
			{
				return portMethod.invoke(port, args);
			}
			catch (InvocationTargetException e)
			{
				Throwable t = e.getTargetException();
				if (t instanceof ConnectException
					|| t instanceof ConnectIOException)
				{
					throwable = t;
					LOGGER.error(methodName + " - " + t);
					rotatePort();
				}
				else
				{
					throw t;
				}
			}
		}
		throw throwable;
	}

	/**
	 * Creates a new instance of IdapiHelper (and its super-implementation,
	 * ActuateSoapPort) from the supplied array of URLs.
	 * 
	 * @param serverURL
	 *            An array of URLs pointing to Actuate report server SOAP ports.
	 * @return The new instance.
	 * @throws IdapiHelperException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static IdapiHelper getInstance(URL[] serverURL)
		throws IdapiHelperException
	{
		try
		{
			InvocationHandler handler = new IdapiHelperImpl(serverURL);
			Class proxyClass = Proxy.getProxyClass(IdapiHelper.class
				.getClassLoader(), new Class[] { IdapiHelper.class });
			Constructor constructor = proxyClass
				.getConstructor(new Class[] { InvocationHandler.class });
			return (IdapiHelper) constructor
				.newInstance(new Object[] { handler });
		}
		catch (Exception e)
		{
			throw new IdapiHelperException(e);
		}
	}

	public synchronized void setTimeout(int timeout)
	{
		this.timeout = timeout;
		setPortTimeout();
	}

	public synchronized int getTimeout()
	{
		return timeout;
	}
}
