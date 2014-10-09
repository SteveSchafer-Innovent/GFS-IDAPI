package example;

/*

This class supplement SOAP Header access to the generated code
for use with Actuate server.

*/

import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;

import org.apache.axis.message.SOAPHeaderElement;

public class ActuateAPILocatorEx
	extends com.actuate.schemas.ActuateAPILocator
	implements ActuateAPIEx {

	private java.lang.String authId = "";
	private java.lang.String locale = "en_US";
	private java.lang.String targetVolume;
	private java.lang.String targetServer;
	private java.lang.String connectionHandle;
	private java.lang.Boolean delayFlush;
	org.apache.axis.client.Call call = null;
	private java.lang.String fileType;


	/**
	 * Constructor for ActuateAPILocatorEx
	 */
	public ActuateAPILocatorEx() {
		super();
	}

	/**
	 * Create call object and add Actuate's SOAP Header
	 */
	public Call createCall() throws ServiceException {
		call = (org.apache.axis.client.Call) super.createCall();
		if (null != authId)
			call.addHeader(new SOAPHeaderElement(null, "AuthId", authId));
		if (null != locale)
			call.addHeader(new SOAPHeaderElement(null, "Locale", locale));
		if (null != targetVolume)
			call.addHeader(
				new SOAPHeaderElement(null, "TargetVolume", targetVolume));
		if (null != fileType)
            	call.addHeader(new SOAPHeaderElement(null, "FileType", fileType));		
		if (null != connectionHandle)
			call.addHeader(
				new SOAPHeaderElement(
					null,
					"ConnectionHandle",
					connectionHandle));
		if (null != targetServer)
			call.addHeader(
				new SOAPHeaderElement(
				null,
				"TargetServer",
				targetServer));
		if (null != targetVolume)
			call.addHeader(
				new SOAPHeaderElement(null, "DelayFlush", delayFlush));
		return call;
	}

	/**
	 * Returns the authId.
	 * @return java.lang.String
	 */
	public java.lang.String getAuthId() {
		return authId;
	}

	/**
	 * Get previously created call object
	 */
	public org.apache.axis.client.Call _getCall() {
		if (null == call) {
			try {
				createCall();
			} catch (ServiceException e) {
			}
		}
		return call;
	}

	/**
	 * Returns the connectionHandle.
	 * @return byte[]
	 */
	public String getConnectionHandle() {
		return connectionHandle;
	}

	/**
	 * Returns the delayFlush.
	 * @return java.lang.Boolean
	 */
	public java.lang.Boolean getDelayFlush() {
		return delayFlush;
	}

	/**
	 * Returns the locale.
	 * @return java.lang.String
	 */
	public java.lang.String getLocale() {
		return locale;
	}

	/**
	 * Returns the targetVolume.
	 * @return java.lang.String
	 */
	public java.lang.String getTargetVolume() {
		return targetVolume;
	}

	/**
	 * Returns the targetServer
	 * @return java.lang.String
	 */
	public java.lang.String getTargetServer() 
	{
		return targetServer;
	}

	/**
	 * Sets the authId.
	 * @param authId The authId to set
	 */
	public void setAuthId(java.lang.String authId) {
		this.authId = authId;
	}

	/**
	 * Sets the connectionHandle.
	 * @param connectionHandle The connectionHandle to set
	 */
	public void setConnectionHandle(java.lang.String connectionHandle) {
		this.connectionHandle = connectionHandle;
	}

	/**
	 * Sets the delayFlush.
	 * @param delayFlush The delayFlush to set
	 */
	public void setDelayFlush(java.lang.Boolean delayFlush) {
		this.delayFlush = delayFlush;
	}

	/**
	 * Sets the locale.
	 * @param locale The locale to set
	 */
	public void setLocale(java.lang.String locale) {
		this.locale = locale;
	}

	/**
	 * Sets the targetVolume.
	 * @param targetVolume The targetVolume to set
	 */
	public void setTargetVolume(java.lang.String targetVolume) {
		this.targetVolume = targetVolume;
	}

	/**
	 * Sets the targetServer
	 * @param targetServer The targetServer to set
	 */
	public void setTargetServer(java.lang.String targetServer) 
	{
		this.targetServer = targetServer;
	}
	
	public java.lang.String getFileType() 
	{
		return fileType;
	}

	public void setFileType(java.lang.String fileType) 
	{
		this.fileType = fileType;
	}

}
