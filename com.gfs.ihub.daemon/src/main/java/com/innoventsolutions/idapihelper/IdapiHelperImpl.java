package com.innoventsolutions.idapihelper;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Call;
import org.apache.axis.client.Stub;
import org.apache.axis.encoding.Base64;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.actuate.schemas.ActuateSoapBindingStub;
import com.actuate.schemas.ActuateSoapPort;
import com.actuate.schemas.ArrayOfAttachment;
import com.actuate.schemas.Attachment;
import com.actuate.schemas.DownloadFile;
import com.actuate.schemas.DownloadFileResponse;
import com.actuate.schemas.Login;
import com.actuate.schemas.LoginResponse;
import com.actuate.schemas.SystemLogin;
import com.actuate.schemas.SystemLoginResponse;
import com.actuate.schemas.UploadFile;
import com.actuate.schemas.UploadFileResponse;
import com.actuate.schemas.User;

/**
 * An invocation handler for dynamic proxy classes created to implement the
 * ActuateSoapPort and IdapiHelper interfaces. The IdapiHelper interfaces are
 * implemented here as well.
 * 
 * @author Steve Schafer / Innovent Solutions
 * @version 1.0
 */
public class IdapiHelperImpl implements InvocationHandler {
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

	private IdapiHelperImpl(final URL[] server) throws IdapiHelperException {
		setServerURL(server);
	}

	public Call getCall() {
		return api.getCall();
	}

	/**
	 * Sets the array of server URLs. Each URL points to the SOAP port of an
	 * Actuate report server. The first URL is normally used. Secondary URLs are
	 * only used in case of failure.
	 * 
	 * @param serverURL
	 * @throws IdapiHelperException
	 */
	public void setServerURL(final URL[] serverURL) throws IdapiHelperException {
		if (LOGGER.getEffectiveLevel().isGreaterOrEqual(Level.DEBUG)) {
			final StringBuffer buf = new StringBuffer();
			String sep = "";
			buf.append("setting server URL list to [");
			for (int i = 0; i < serverURL.length; i++) {
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
		try {
			rotatePort();
		} catch (final RemoteException e) {
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
	public void checkReady() throws NotReadyException {
		if (!isReady()) {
			throw new NotReadyException();
		}
	}

	/**
	 * Returns true if the server URLs have been set and the helper is ready to
	 * accept a login.
	 * 
	 * @return
	 */
	public boolean isReady() {
		if (port == null)
			return false;

		/*
		 * TODO Ping ping = new Ping(); ping.setNumBytes(Long.valueOf("128"));
		 * ping.setDestination(Destination.MDS); ping.setMode(Mode.Normal); try
		 * { PingResponse response = this.port.ping(ping); if
		 * (response.getReply() == null) return false; } catch (RemoteException
		 * e) { System.out.println("Failure to Ping Server ");
		 * e.printStackTrace(); return false; }
		 */

		return true;
	}

	/**
	 * Re-log in using the previous credentials.
	 * 
	 * @throws IdapiHelperException
	 */
	public void reconnect() throws IdapiHelperException {
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
	public void setAuthId(final String authId) throws NotReadyException {
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

	public String getAuthId() {
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
	public void login(final String volume, final String user,
			final String password, final byte[] extendedCredentials,
			final boolean getUserData) throws IdapiHelperException {
		checkReady();
		final Login login = new Login();
		login.setUser(user);
		if (extendedCredentials != null && extendedCredentials.length > 0) {
			login.setCredentials(extendedCredentials);
		}
		login.setPassword(password);
		if (volume != null && volume.length() > 0) {
			api.setTargetVolume(volume);
			login.setDomain(volume);
		}
		login.setUserSetting(new Boolean(getUserData));
		try {
			login(login);
		} catch (final RemoteException e) {
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
	public LoginResponse login(final Login login) throws RemoteException {
		LoginResponse loginResponse = null;
		RemoteException exception = null;
		for (int i = 0; i < serverURL.length && loginResponse == null
				&& exception == null; i++) {
			try {
				loginResponse = port.login(login);
			} catch (final RemoteException remoteException) {
				exception = remoteException;
				LOGGER.error("login - " + remoteException);
				rotatePort();
			}
		}
		if (exception != null) {
			throw exception;
		}
		api.setAuthId(loginResponse.getAuthId());
		final Boolean booleanObject = login.getUserSetting();
		this.getUserData = booleanObject == null ? false : booleanObject
				.booleanValue();
		if (getUserData) {
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
	public void systemLogin(final String password) throws IdapiHelperException {
		checkReady();
		final SystemLogin systemLogin = new SystemLogin();
		systemLogin.setSystemPassword(password);
		systemLogin.setSystemPasswordEncryptLevel(new Long(0));
		SystemLoginResponse loginResponse;
		try {
			loginResponse = port.systemLogin(systemLogin);
		} catch (final RemoteException e) {
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

	/**
	 * Create a new call object that can be used to send SOAP message to actuate
	 * server
	 * 
	 * @return Call
	 */
	/*
	 * private Call createCall() throws ServiceException { final Call call =
	 * (Call) api.createCall();
	 * call.setTargetEndpointAddress(serverURL[currentUrlIndex]); return call; }
	 */

	public void rotatePort() throws RemoteException {
		port = null;
		for (int i = 0; i < serverURL.length && port == null; i++) {
			if (currentUrlIndex >= 0) {
				LOGGER.warn("Rotating port");
			}
			currentUrlIndex++;
			if (currentUrlIndex >= serverURL.length) {
				currentUrlIndex = 0;
			}
			final URL url = serverURL[currentUrlIndex];
			try {
				port = api.getActuateSoapPort(url);
			} catch (final ServiceException serviceException) {
				LOGGER.warn("Failed to connect to URL " + currentUrlIndex
						+ ": " + url);
			}
		}
		if (port == null) {
			throw new RemoteException("Failed to connect");
		}
		setPortTimeout();
		LOGGER.debug("Using " + serverURL[currentUrlIndex]);
	}

	private void setPortTimeout() {
		if (port != null && port instanceof Stub) {
			final Stub stub = (Stub) port;
			stub.setTimeout(timeout);
			LOGGER.debug("Setting stub timeout to " + timeout);
		}
	}

	public String getUsername() {
		return userName;
	}

	public String getVolume() {
		return volume;
	}

	public byte[] getConnectionHandle() {
		return api.getConnectionHandle();
	}

	/**
	 * Set the connection handle. Connection handles are assigned by the report
	 * server when a report is viewed and are used to optimize access while
	 * viewing subsequent pages or performing other view operations.
	 * 
	 * @param value
	 */
	public void setConnectionHandle(final byte[] value) {
		api.setConnectionHandle(value);
	}

	public long getConnectTime() {
		return connectTime;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public User getUser() {
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		buf.append(serverURL);
		if (volume != null) {
			buf.append("/");
			buf.append(volume);
		}
		if (userName != null) {
			buf.append("/");
			buf.append(userName);
		}
		if (ec != null && ec.length > 0) {
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
	 * java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(final Object proxy, final Method method,
			final Object[] args) throws Throwable {
		final String methodName = method.getName();
		@SuppressWarnings("rawtypes")
		final Class[] methodParams = method.getParameterTypes();
		try {
			final Method thisMethod = getClass().getMethod(methodName,
					methodParams);
			return thisMethod.invoke(this, args);
		} catch (final NoSuchMethodException e) {
			// fall through to try an ActuateSoapPort method
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
		final Method portMethod = ActuateSoapPort.class.getMethod(methodName,
				methodParams);
		Throwable throwable = null;
		for (int i = 0; i < serverURL.length; i++) {
			try {
				return portMethod.invoke(port, args);
			} catch (final InvocationTargetException e) {
				final Throwable t = e.getTargetException();
				if (t instanceof ConnectException
						|| t instanceof ConnectIOException) {
					throwable = t;
					LOGGER.error(methodName + " - " + t);
					rotatePort();
				} else {
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
	public static IdapiHelper getInstance(final URL[] serverURL)
			throws IdapiHelperException {
		try {
			final InvocationHandler handler = new IdapiHelperImpl(serverURL);
			final Class proxyClass = Proxy.getProxyClass(
					IdapiHelper.class.getClassLoader(),
					new Class[] { IdapiHelper.class });
			final Constructor constructor = proxyClass
					.getConstructor(new Class[] { InvocationHandler.class });
			return (IdapiHelper) constructor
					.newInstance(new Object[] { handler });
		} catch (final Exception e) {
			throw new IdapiHelperException(e);
		}
	}

	public synchronized void setTimeout(final int timeout) {
		this.timeout = timeout;
		setPortTimeout();
	}

	public synchronized int getTimeout() {
		return timeout;
	}

	/**
	 * Download file from encyclopdia to the specified directory If document is
	 * decomposed ,multiple attachments of files will be discarded as its not in
	 * viewable format.But attachments ids will be shown to user.This example
	 * can be modified easily to save those attachments as different files.
	 * 
	 * @param fileId
	 * @param decomposeCompoundDocument
	 * @param downloadEmbedded
	 * @param downloadDirectory
	 * @return boolean
	 * 
	 */
	public String downloadFile(final String fileId,
			final boolean decomposeCompoundDocument,
			final boolean downloadEmbedded, final String downloadDirectory)
			throws Exception {
		final ActuateSoapBindingStub proxy = (ActuateSoapBindingStub) port;
		final DownloadFile downloadFile = new DownloadFile();
		downloadFile.setFileId(fileId);
		downloadFile.setDecomposeCompoundDocument(new Boolean(
				decomposeCompoundDocument));
		downloadFile.setDownloadEmbedded(new Boolean(downloadEmbedded));

		String downloadName = null;
		DownloadFileResponse downloadFileResponse = null;
		try {
			downloadFileResponse = proxy.downloadFile(downloadFile);

			final String serverFilePath = downloadFileResponse.getFile()
					.getName();
			final String localFileName = serverFilePath.substring(
					serverFilePath.lastIndexOf('/') + 1,
					serverFilePath.length());

			if (!downloadEmbedded) {
				downloadName = saveNonEmbededResponse(downloadDirectory,
						localFileName, decomposeCompoundDocument);
			} else {
				downloadName = saveEmbededResponse(downloadDirectory,
						localFileName, downloadFileResponse,
						decomposeCompoundDocument);
			}

		} catch (final SOAPException e) {
			throw AxisFault.makeFault(e);
		} catch (final RemoteException e) {
			throw e;
		} catch (final IOException e) {
			throw e;
		}
		return downloadName;
	}

	public String saveEmbededResponse(final String downloadDirectory,
			final String localFileName,
			final DownloadFileResponse downloadFileResponse,
			final boolean decomposed) throws IOException, RemoteException {

		String downloadName = null;
		BufferedOutputStream outStream = null;
		final String localFilePath = downloadDirectory + "/" + localFileName;

		try {
			if (!decomposed) {
				outStream = new BufferedOutputStream(new FileOutputStream(
						localFilePath));
			}

			final Attachment attachment = downloadFileResponse.getContent();
			if (attachment != null) {
				final byte[] b = attachment.getContentData();
				System.out.println("Attachment retrived as "
						+ attachment.getContentId());
				if (b != null && outStream != null) {
					outStream.write(b);
				}
			}

			final ArrayOfAttachment arrayOfAttachment = downloadFileResponse
					.getContainedFiles();

			if (arrayOfAttachment != null) {
				final Attachment[] attachments = arrayOfAttachment
						.getAttachment();
				for (int i = 0; i < attachments.length; i++) {
					if (attachments[i] != null) {
						final byte[] b = attachments[i].getContentData();
						System.out.println("Attachment retrived as "
								+ attachments[i].getContentId());
						if (b != null) {
							if (outStream != null) {
								outStream.write(b);
							} else {
								final String decomposedDocAttachment = downloadDirectory
										+ "/" + attachments[i].getContentId();
								final BufferedOutputStream tempOutStream = new BufferedOutputStream(
										new FileOutputStream(
												decomposedDocAttachment));
								tempOutStream.write(b);
								tempOutStream.close();
							}
						}

					}
				}
			}

		} catch (final RemoteException e) {
			throw e;
		} finally {
			if (outStream != null) {
				downloadName = localFilePath;
				outStream.close();
			}
		}
		return downloadName;
	}

	public String saveNonEmbededResponse(final String downloadDirectory,
			final String localFileName, final boolean decomposed)
			throws IOException, SOAPException, RemoteException {
		final ActuateSoapBindingStub proxy = (ActuateSoapBindingStub) port;
		String downloadName = null;
		BufferedOutputStream outStream = null;
		final String localFilePath = downloadDirectory + "/" + localFileName;
		try {
			if (!decomposed) {
				outStream = new BufferedOutputStream(new FileOutputStream(
						localFilePath));
			}

			final Object[] attachments = proxy.getAttachments();
			for (int i = 0; i < attachments.length; i++) {
				if (attachments[i] != null) {
					final AttachmentPart temp = (AttachmentPart) attachments[i];
					System.out.println("Attachment retrived as "
							+ temp.getContentId());
					final InputStream inStream = temp.getDataHandler()
							.getInputStream();
					if (!decomposed) {
						if (outStream != null) {
							saveToStream(inStream, outStream);
						}
					} else {
						final String decomposedDocAttachment = downloadDirectory
								+ "/" + temp.getContentId();
						final BufferedOutputStream tempOutStream = new BufferedOutputStream(
								new FileOutputStream(decomposedDocAttachment));
						saveToStream(inStream, tempOutStream);
						tempOutStream.close();
					}

				}
			}

		} catch (final SOAPException e) {
			throw AxisFault.makeFault(e);
		} catch (final RemoteException e) {
			throw e;
		} finally {
			proxy.clearAttachments();
			if (outStream != null) {
				downloadName = localFilePath;
				outStream.close();
			}
		}
		return downloadName;
	}

	public boolean saveToStream(final InputStream inStream,
			final OutputStream out) throws IOException {
		boolean writeStatus = false;
		try {
			final byte[] buf = new byte[1024];
			int len = 0;
			while ((len = inStream.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			inStream.close();
			writeStatus = true;
		} catch (final IOException e) {
			System.out.println("Excepton while downloading file ");
			e.printStackTrace();
			throw e;
		}
		return writeStatus;
	}

	public UploadFileResponse uploadFile(final UploadFile request,
			final org.apache.axis.attachments.AttachmentPart attachmentPart)
			throws RemoteException, RemoteException, ServiceException {
		final ActuateSoapBindingStub proxy = (ActuateSoapBindingStub) port;
		UploadFileResponse response;
		try {
			proxy.addAttachment(attachmentPart);
			response = proxy.uploadFile(request);
		} finally {
			proxy.clearAttachments();
		}
		return response;
	}

	/**
	 * Special re-implementation of UploadFile. The AXIS generated method cannot
	 * do this because of the way AXIS handles attachments.
	 * 
	 * @param request
	 * @param attachmentPart
	 * @return
	 */
	/*
	 * public UploadFileResponse uploadFile2(final UploadFile request, final
	 * AttachmentPart attachmentPart) throws RemoteException { Call call; try {
	 * call = createCall(); } catch (final ServiceException e) { throw new
	 * RemoteException("Failure to create call", e); } final QName requestQName
	 * = new QName( "http://schemas.actuate.com/actuate8", "UploadFile"); final
	 * QName responseQName = new QName( "http://schemas.actuate.com/actuate8",
	 * "UploadFileResponse"); call.addParameter(requestQName, requestQName,
	 * UploadFile.class, ParameterMode.IN); call.setReturnType(responseQName);
	 * call.setUseSOAPAction(true); call.setSOAPActionURI("");
	 * call.setEncodingStyle(null);
	 * call.setScopedProperty(AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
	 * call.setScopedProperty(Call.SEND_TYPE_ATTR, Boolean.FALSE);
	 * call.setOperationStyle("document"); call.setOperationName(requestQName);
	 * // set the actual MIME attachment //
	 * call.addAttachmentPart(attachmentPart); final Object resp =
	 * call.invoke(new Object[] { request }); if (resp instanceof
	 * RemoteException) { throw (RemoteException) resp; } try { return
	 * (UploadFileResponse) resp; } catch (final Exception e) { return
	 * (UploadFileResponse) JavaUtils.convert(resp, UploadFileResponse.class); }
	 * }
	 */

	public Object[] getAttachments() {
		final ActuateSoapBindingStub proxy = (ActuateSoapBindingStub) port;
		return proxy.getAttachments();
	}

	public void clearAttachments() {
		final ActuateSoapBindingStub proxy = (ActuateSoapBindingStub) port;
		proxy.clearAttachments();
	}
}
