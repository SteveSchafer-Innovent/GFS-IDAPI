package com.innoventsolutions.idapihelper;

import java.net.URL;
import java.rmi.RemoteException;

import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Call;

import com.actuate.schemas.ActuateSoapPort;
import com.actuate.schemas.UploadFile;
import com.actuate.schemas.UploadFileResponse;
import com.actuate.schemas.User;

/**
 * @author Steve Schafer, Innovent Solutions
 * @version 1.0
 */
public interface IdapiHelper extends ActuateSoapPort {
	void setServerURL(URL[] serverURL) throws IdapiHelperException;

	void checkReady() throws NotReadyException;

	boolean isReady();

	Call getCall();

	void reconnect() throws IdapiHelperException;

	void setAuthId(String authId) throws NotReadyException;

	String getAuthId();

	void setTimeout(int timeout);

	int getTimeout();

	void login(String volume, String user, String password,
			byte[] extendedCredentials, boolean getUserData)
			throws IdapiHelperException;

	void systemLogin(String password) throws IdapiHelperException;

	UploadFileResponse uploadFile(UploadFile request,
			AttachmentPart attachmentPart) throws Exception;

	String downloadFile(String fileId, boolean decomposeCompoundDocument,
			boolean downloadEmbedded, String downloadDirectory)
			throws Exception;

	void rotatePort() throws RemoteException;

	String getUsername();

	String getVolume();

	byte[] getConnectionHandle();

	void setConnectionHandle(byte[] value);

	long getConnectTime();

	long getLoginTime();

	User getUser();

	String toString();

	Object[] getAttachments();

	void clearAttachments();

}