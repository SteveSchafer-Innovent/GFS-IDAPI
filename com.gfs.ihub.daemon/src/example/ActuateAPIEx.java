package example;

/*

Interface class / ActuateAPI Extension

*/

public interface ActuateAPIEx extends com.actuate.schemas.ActuateAPI {

	public void setAuthId(java.lang.String authId);
	public void setLocale(java.lang.String locale);
	public void setTargetVolume(java.lang.String targetVolume);
	public void setTargetServer(java.lang.String targetServer);
	public void setConnectionHandle(java.lang.String connectionHandle);
	public void setDelayFlush(java.lang.Boolean delayFlush);
	public void setFileType(java.lang.String fileType);

	public java.lang.String getAuthId();
	public java.lang.String getLocale();
	public java.lang.String getTargetVolume();
	public java.lang.String getTargetServer();
	public java.lang.String getConnectionHandle();
	public java.lang.Boolean getDelayFlush();
	public java.lang.String getFileType();

	/**
	 * @return Call object with the SOAP Header element set
	 */
	public org.apache.axis.client.Call _getCall();

}
