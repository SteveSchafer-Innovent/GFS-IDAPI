package example;

/*

Actuate Client Example

This class controls operation between ActuateServer and user
application.
*/

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Calendar;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.attachments.AttachmentPart;

import com.actuate.schemas.ActuateSoapBindingStub;
import com.actuate.schemas.ArrayOfString;
import com.actuate.schemas.GetAllCounterValues;
import com.actuate.schemas.GetAllCounterValuesResponse;
import com.actuate.schemas.GetFolderItemsResponse;
import com.actuate.schemas.GetPageCount;
import com.actuate.schemas.GetPageCountResponse;
import com.actuate.schemas.ObjectIdentifier;
import com.actuate.schemas.SelectUsersResponse;

public class ActuateControl implements java.io.Serializable {

	// control variables
	public String authenticationId = null;
	public String username = "Administrator";
	private String password = "";
	private String targetVolume = null;
	private String targetServer = null;
	public String inputFileName = "SampleBIRTReport.rptdesign";
	public String outputFileName = "SampleBIRTReport.rptdocument";
	public String jobName = "Default JobName";
	private String currentDirectory = "/";

	// server settings
	public String actuateServerURL = "http://localhost:8000/";

	// proxy operation
	public ActuateSoapBindingStub proxy;
	public ActuateAPIEx actuateAPI;

	/**
	 * Constructor
	 */
	public ActuateControl() throws MalformedURLException, ServiceException {
		actuateAPI = new ActuateAPILocatorEx();
		setActuateServerURL(actuateServerURL);
	}

	/**
	 * Constructor
	 */
	public ActuateControl(String serverURL)
		throws MalformedURLException, ServiceException {
		actuateAPI = new ActuateAPILocatorEx();
		setActuateServerURL(serverURL);
	}

	/**
	 * Create a new call object that can be used to send SOAP message
	 * to actuate server
	 * @return Call
	 * @throws ServiceException
	 */
	public org.apache.axis.client.Call createCall() throws ServiceException {
		org.apache.axis.client.Call call =
			(org.apache.axis.client.Call) actuateAPI.createCall();
		call.setTargetEndpointAddress(this.actuateServerURL);
		return call;
	}

	/**
	 * Login to actuate server with username and password to targetVolume
	 * Return true if login success
	 */
	public boolean login() {
		boolean success = true;

		com.actuate.schemas.Login request = new com.actuate.schemas.Login();

		request.setPassword(password);
		request.setUser(username);

		try {
			actuateAPI.setAuthId(null);
			System.out.println("Setting TargetVolume to " + targetVolume);			
			actuateAPI.setTargetVolume(targetVolume);
			com.actuate.schemas.LoginResponse response = proxy.login(request);
			authenticationId = response.getAuthId();
			actuateAPI.setAuthId(authenticationId);
		} catch (java.rmi.RemoteException e) {
			// login failed
			success = false;
		}
		return success;
	}

	/**
	 * Select a single page and save the result in the specified directory
	 * @param FileName
	 * @param format
	 * @param pageNumber
	 * @param downloadDirectory
	 * @return String	the first file attachment name
	 * @throws RemoteException
	 */
	public String selectPage(
		String FileName,
		String format,
		int pageNumber,
		String downloadDirectory)
		throws RemoteException {
		// Set view parameter
		com.actuate.schemas.ViewParameter viewParameter =
			new com.actuate.schemas.ViewParameter();
		viewParameter.setFormat(format);
		viewParameter.setUserAgent(
			"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; Q312461; .NET CLR 1.0.3705)");

		// Set file name to view
		com.actuate.schemas.ObjectIdentifier objectIdentifier =
			new com.actuate.schemas.ObjectIdentifier();
		objectIdentifier.setName(FileName);

		// Set the pages to view
		com.actuate.schemas.PageIdentifier pageIdentifier =
			new com.actuate.schemas.PageIdentifier();
		pageIdentifier.setPageNum(new Long(pageNumber));

		// Set the select page request
		com.actuate.schemas.SelectPage selectPage =
			new com.actuate.schemas.SelectPage();
		selectPage.setObject(objectIdentifier);
		selectPage.setViewParameter(viewParameter);
		selectPage.setPage(pageIdentifier);
		selectPage.setDownloadEmbedded(new Boolean(true));

		//Set FileType
		actuateAPI.setFileType(FileName.split("\\.")[1]);

		// SelectPage
		com.actuate.schemas.SelectPageResponse selectPageResponse =
			proxy.selectPage(selectPage);

		// Save the result in download directory
		new File(downloadDirectory).mkdir();

		String firstAttachmentName =
			saveAttachment(selectPageResponse.getPageRef(), downloadDirectory);
		saveAttachment(
			selectPageResponse.getPostResponseRef(),
			downloadDirectory);

		return firstAttachmentName;
	}

	/**
	 * Select a single page and save the result in the specified directory
	 * @param FileName
	 * @param format
	 * @param pageNumber
	 * @param downloadDirectory
	 * @return String	the first file attachment name
	 * @throws RemoteException
	 */
	public String selectJavaReportPage(
		String FileName,
		String format,
		int pageNumber,
		String downloadDirectory)
		throws RemoteException {
		// Set file name to view
		com.actuate.schemas.ObjectIdentifier objectIdentifier =
			new com.actuate.schemas.ObjectIdentifier();
		objectIdentifier.setName(FileName);

		// Set the pages to view
		com.actuate.schemas.PageIdentifier pageIdentifier =
			new com.actuate.schemas.PageIdentifier();
		pageIdentifier.setPageNum(new Long(pageNumber));

		// Set the select page request
		com.actuate.schemas.SelectJavaReportPage selectJavaReportPage =
			new com.actuate.schemas.SelectJavaReportPage();
		selectJavaReportPage.setObject(objectIdentifier);
		//selectJavaReportPage.setViewParameter(viewParameter);
		selectJavaReportPage.setPage(pageIdentifier);
		selectJavaReportPage.setDownloadEmbedded(new Boolean(true));

		//Set FileType
		actuateAPI.setFileType(FileName.split("\\.")[1]);

		// SelectPage
		com.actuate.schemas.SelectJavaReportPageResponse selectJavaReportPageResponse =
			proxy.selectJavaReportPage(selectJavaReportPage);

		// Save the result in download directory
		new File(downloadDirectory).mkdir();

		String firstAttachmentName =
			saveAttachment(selectJavaReportPageResponse.getPageRef(), downloadDirectory);

		return firstAttachmentName;
	}
	
	public GetPageCountResponse getPageCount(String name,String id)
	{
		ObjectIdentifier objectIdentifier = new ObjectIdentifier();
		objectIdentifier.setName(name);
		objectIdentifier.setId(id);
		
		GetPageCount getPageCount = new GetPageCount();
		getPageCount.setObject(objectIdentifier);
		GetPageCountResponse getPageCountResponse = null;
		try {
			getPageCountResponse = proxy.getPageCount(getPageCount);
		} catch (RemoteException e) {			
			e.printStackTrace();
		}		
		return getPageCountResponse;
	}

	public GetAllCounterValuesResponse getAllCounterValues()
	{
		System.out.println("Setting TargetServer to " + targetServer);			
		actuateAPI.setTargetServer(targetServer);
		GetAllCounterValues getAllCounterValues = new GetAllCounterValues();
		GetAllCounterValuesResponse getAllCounterValuesResponse = null;
		try {
			getAllCounterValuesResponse = proxy.getAllCounterValues(getAllCounterValues);
		} catch (RemoteException e) {			
			e.printStackTrace();
		}		
		return getAllCounterValuesResponse;

	}


	public String getFolderItemsFetchHandle;
	public Integer getFolderItemsFetchSize = null;
	/**
	 * Method getFolderItems. Can be called repeatedly to use a fetch handle.
	 *
	 * @return GetFolderItemsResponse
	 */
	public GetFolderItemsResponse getFolderItems() {

		com.actuate.schemas.ArrayOfString resultDef =
			newArrayOfString(
				new String[] {
					"UserPermissions",
					"FileType",
					"Version",
					"Description",
					"VersionName",
					"Size",
					"PageCount",
					"TimeStamp",
					"Owner" });

		com.actuate.schemas.GetFolderItems request =
			new com.actuate.schemas.GetFolderItems();
		request.setFolderName(currentDirectory);
		request.setLatestVersionOnly(Boolean.FALSE);
		request.setResultDef(resultDef);

		com.actuate.schemas.FileSearch search =
			new com.actuate.schemas.FileSearch();
		search.setFetchDirection(Boolean.TRUE);
		search.setFetchSize(getFolderItemsFetchSize);
		search.setFetchHandle(getFolderItemsFetchHandle);

		request.setSearch(search);
		com.actuate.schemas.GetFolderItemsResponse response = null;
		try {
			response = proxy.getFolderItems(request);
			getFolderItemsFetchHandle = response.getFetchHandle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;

	}

	/**
	 * Send a single administrate message to create a user
	 * @param user
	 * @return AdministrateResponse
	 * @throws RemoteException
	 */
	public com.actuate.schemas.AdministrateResponse createUser(
		com.actuate.schemas.User user)
		throws RemoteException {
		System.out.println("Creating user " + user.getName());

		com.actuate.schemas.CreateUser createUser =
			new com.actuate.schemas.CreateUser();
		createUser.setUser(user);
		createUser.setIgnoreDup(Boolean.TRUE);

		com.actuate.schemas.AdminOperation adminOperation =
			new com.actuate.schemas.AdminOperation();
		adminOperation.setCreateUser(createUser);

		return runAdminOperation(adminOperation);
	}

	public com.actuate.schemas.AdministrateResponse createFolder(
		String workingFolderName,
		String folderName,
		String folderDescription)
		throws RemoteException {
		System.out.println(
			"Creating folder " + folderName + " in " + workingFolderName);

		com.actuate.schemas.CreateFolder createFolder =
			new com.actuate.schemas.CreateFolder();
		createFolder.setWorkingFolderName(workingFolderName);
		createFolder.setFolderName(folderName);
		createFolder.setDescription(folderDescription);
		createFolder.setIgnoreDup(Boolean.TRUE);

		com.actuate.schemas.AdminOperation adminOperation =
			new com.actuate.schemas.AdminOperation();
		adminOperation.setCreateFolder(createFolder);

		return runAdminOperation(adminOperation);
	}

	/**
	 * Convenient function to run the one administrate operation and handle the RemoteException
	 *
	 * @param adminOperation
	 * @return boolean TRUE if there is no error
	 */
	public com.actuate.schemas.AdministrateResponse runAdminOperation(
		com.actuate.schemas.AdminOperation adminOperation) {
		com.actuate.schemas.Administrate administrate =
			new com.actuate.schemas.Administrate();
		administrate.setAdminOperation(
			new com.actuate.schemas.AdminOperation[] { adminOperation });

		com.actuate.schemas.AdministrateResponse administrateResponse = null;
		try {
			administrateResponse = proxy.administrate(administrate);
		} catch (java.rmi.RemoteException e) {
			org.apache.axis.AxisFault l_fault =
				org.apache.axis.AxisFault.makeFault(e);
			System.out.println(l_fault.getFaultString());
			System.out.println(l_fault.getFaultCode().toString());
			org.w3c.dom.Element[] l_details = l_fault.getFaultDetails();
		}
		return administrateResponse;
	}

	/**
	 * Convenient function to run an array of administrate operation and handle the RemoteException
	 *
	 * @param adminOperation
	 * @return boolean TRUE if there is no error
	 */
	public boolean runAdminOperation(
		com.actuate.schemas.AdminOperation[] adminOperations) {
		com.actuate.schemas.Administrate administrate =
			new com.actuate.schemas.Administrate();
		administrate.setAdminOperation(adminOperations);
		try {
			com.actuate.schemas.AdministrateResponse administrateResponse =
				proxy.administrate(administrate);
		} catch (java.rmi.RemoteException e) {
			org.apache.axis.AxisFault l_fault =
				org.apache.axis.AxisFault.makeFault(e);
			System.out.println(l_fault.getFaultString());
			System.out.println(l_fault.getFaultCode().toString());
			org.w3c.dom.Element[] l_details = l_fault.getFaultDetails();
			return false;
		}
		return true;
	}

	/**
	 * Delete user by name or a list of name
	 * @param name			please only set one of name or nameList
	 * @param nameList
	 * @throws RemoteException
	 */
	public void deleteUsers(String name, String[] nameList)
		throws RemoteException {
		com.actuate.schemas.DeleteUser deleteUser =
			new com.actuate.schemas.DeleteUser();
		deleteUser.setName(name);
		deleteUser.setNameList(newArrayOfString(nameList));

		com.actuate.schemas.AdminOperation adminOperation =
			new com.actuate.schemas.AdminOperation();
		adminOperation.setDeleteUser(deleteUser);

		runAdminOperation(adminOperation);
	}

	/**
	 * Download file from encyclopdia to the specified directory
	 * If document is decomposed ,multiple attachments of files will be discarded as its not in viewable format.But attachments ids will be 
	 * shown to user.This example can be modified easily to save those attachments as different files.
	 * @param FileName
	 * @param decomposeCompoundDocument
	 * @param downloadEmbedded
	 * @param downloadDirectory
	 * @return boolean
	 *
	 */
	public String downloadFile(
		String FileName,
		boolean decomposeCompoundDocument,
		boolean downloadEmbedded,
		String downloadDirectory) throws Exception {
		System.out.println("Download " + FileName);
		com.actuate.schemas.DownloadFile downloadFile =
			new com.actuate.schemas.DownloadFile();
		downloadFile.setFileName(FileName);
		downloadFile.setDecomposeCompoundDocument(
			new Boolean(decomposeCompoundDocument));
		downloadFile.setDownloadEmbedded(new Boolean(downloadEmbedded));

		String downloadName = null;
		com.actuate.schemas.DownloadFileResponse downloadFileResponse = null;
		try
		{
			downloadFileResponse = proxy.downloadFile(downloadFile);
			
			String serverFilePath = downloadFileResponse.getFile().getName();
			String localFileName  = serverFilePath.substring( serverFilePath.lastIndexOf('/') + 1 ,serverFilePath.length()) ;
			
			
			
			if( ! downloadEmbedded )
			{
				downloadName =	saveNonEmbededResponse( downloadDirectory, localFileName, decomposeCompoundDocument );
			}
			else
			{
				downloadName = saveEmbededResponse( downloadDirectory, localFileName, downloadFileResponse, decomposeCompoundDocument);
			}
			
		}
		catch ( SOAPException e )
		{
			throw AxisFault.makeFault( e );
		}
		catch (RemoteException e) 
		{
			throw e ;
		}
		catch (IOException e) 
		{
			throw e ;
		}	
		return downloadName;
	}

	public String  saveEmbededResponse(String downloadDirectory, String localFileName ,com.actuate.schemas.DownloadFileResponse downloadFileResponse,boolean decomposed ) throws  IOException,RemoteException
	{

		String downloadName = null;
		BufferedOutputStream outStream = null;
		String localFilePath  =	downloadDirectory + "/" + localFileName;
	
		try
		{
			if( ! decomposed )
			{
				outStream =  new BufferedOutputStream( new FileOutputStream( localFilePath ) );
			}

			com.actuate.schemas.Attachment attachment = downloadFileResponse.getContent();
			if(attachment != null )
			{
				byte[] b = attachment.getContentData();
				System.out.println("Attachment retrived as " + attachment.getContentId());
				if(b != null  && outStream !=  null )
				{
					outStream.write(b);
				}
			}
	
			com.actuate.schemas.ArrayOfAttachment arrayOfAttachment =
				downloadFileResponse.getContainedFiles();

			if ( arrayOfAttachment != null) 
			{
				com.actuate.schemas.Attachment[] attachments =
					arrayOfAttachment.getAttachment();
				for (int i = 0; i < attachments.length; i++) 
				{
					if(attachments[i] != null )
					{
						byte[] b = attachments[i].getContentData();
						System.out.println("Attachment retrived as " + attachments[i].getContentId());
						if(b != null )
						{
						 	if(outStream !=  null )
							{
								outStream.write(b);
							}
							else
							{
								String decomposedDocAttachment = downloadDirectory + "/" + attachments[i].getContentId();
								BufferedOutputStream tempOutStream = new BufferedOutputStream( new FileOutputStream( decomposedDocAttachment  ) );
								tempOutStream.write(b);
								tempOutStream.close();
							}
						}
					
					}
				}
			}
			
		}
		catch (RemoteException e) 
		{
			throw e ;
		}
		finally
		{
			if( outStream !=  null )
			{
				downloadName = localFilePath;
				outStream.close();
			}
		}
	return downloadName;	
	}

	public String saveNonEmbededResponse(String downloadDirectory, String localFileName, boolean decomposed) throws  IOException,SOAPException,RemoteException
	{
		String downloadName = null;
		BufferedOutputStream outStream = null;
		String localFilePath  =	downloadDirectory + "/" + localFileName;
		try
		{
			if( ! decomposed )
			{
				outStream =  new BufferedOutputStream( new FileOutputStream( localFilePath ) );
			}
		
			Object[] attachments =   proxy.getAttachments( );
			for( int i =0 ;i < attachments.length ; i++)
			{
				if(attachments[i] !=  null)
				{ 
					AttachmentPart temp = ( AttachmentPart )attachments[i] ;
					System.out.println("Attachment retrived as " + temp.getContentId());
					InputStream inStream = temp.getDataHandler( ).getInputStream( );
					if( ! decomposed )
					{	if( outStream !=  null )
						{
							saveToStream(inStream,outStream);
						}
					}
					else
					{
							String decomposedDocAttachment = downloadDirectory + "/" + temp.getContentId();
							BufferedOutputStream tempOutStream = new BufferedOutputStream( new FileOutputStream( decomposedDocAttachment  ) );
							saveToStream(inStream,tempOutStream);
							tempOutStream.close();
					}
						
				
				}
			}
	
		}
		catch ( SOAPException e )
		{
			throw AxisFault.makeFault( e );
		}
		catch (RemoteException e) 
		{
			throw e ;
		}
		finally
		{
			proxy.clearAttachments();
			if( outStream !=  null )
			{
				downloadName = localFilePath;
				outStream.close();
			}
		}
	return downloadName;
	}
	
	public boolean saveToStream(InputStream inStream , OutputStream out ) throws IOException
	{
		boolean writeStatus = false;
		try
		{
			byte[] buf = new byte[1024];
			int len = 0;
			while ( ( len = inStream.read( buf ) ) > 0 )
			{
				out.write( buf, 0, len );
			}
	
			inStream.close( );
			writeStatus = true;
		}
		catch( IOException e )
		{
			System.out.println("Excepton while downloading file ");
			e.printStackTrace();
			throw e;
		}
		return writeStatus;
	}


	public com.actuate.schemas.UploadFileResponse uploadFile(
		com.actuate.schemas.UploadFile request,
		org.apache.axis.attachments.AttachmentPart attachmentPart)
		throws RemoteException, RemoteException, ServiceException {

		com.actuate.schemas.UploadFileResponse response ;
		try
		{
			proxy.addAttachment( attachmentPart );
			response =  proxy.uploadFile( request );
		}
		finally 
		{
			proxy.clearAttachments();
		}
		return response ;
	}

	/**
	 * ExecuteReport using the information stored in this class:
	 * - jobName
	 * - inputFileName
	 * - outputFileName
	 * @throws RemoteException
	 */
	public void executeReport() throws RemoteException {
		com.actuate.schemas.ExecuteReport executeReport =
			new com.actuate.schemas.ExecuteReport();
		executeReport.setJobName(jobName);
		executeReport.setInputFileName(inputFileName);

		boolean bSaveOutputFile = (!outputFileName.equals(""));
		executeReport.setSaveOutputFile(new Boolean(bSaveOutputFile));
		if (bSaveOutputFile) {
			com.actuate.schemas.NewFile requestedOutputFile =
				new com.actuate.schemas.NewFile();
			requestedOutputFile.setName(outputFileName);

			executeReport.setRequestedOutputFile(requestedOutputFile);
		}

		com.actuate.schemas.ExecuteReportResponse executeReportResponse =
			proxy.executeReport(executeReport);

		System.out.println("Status " + executeReportResponse.getStatus());
	}

	/**
	 * Select all users.
	 * @return SelectUsersResponse
	 */
	public SelectUsersResponse selectUsers() {
		com.actuate.schemas.ArrayOfString resultDef =
			newArrayOfString(
				new String[] {
					"Id",
					"Name",
					"Description",
					"IsLoginDisabled",
					"EmailAddress",
					"HomeFolder",
					"ViewPreference",
					"MaxJobPriority",
					"SuccessNoticeExpiration",
					"FailureNoticeExpiration",
					"SendNoticeForSuccess",
					"SendNoticeForFailure",
					"SendEmailForFailure",
					"SendEmailForSuccess",
					"AttachReportInEmail" });

		com.actuate.schemas.UserSearch userSearch =
			new com.actuate.schemas.UserSearch();

		com.actuate.schemas.SelectUsers selectUsers =
			new com.actuate.schemas.SelectUsers();
		selectUsers.setResultDef(resultDef);
		selectUsers.setSearch(userSearch);

		com.actuate.schemas.SelectUsersResponse selectUsersResponse = null;
		try {
			selectUsersResponse = proxy.selectUsers(selectUsers);

			com.actuate.schemas.User[] users =
				selectUsersResponse.getUsers().getUser();
			for (int i = 0;
				i < selectUsersResponse.getTotalCount().intValue();
				i++) {
				System.out.println(users[i].getName());
			}

		} catch (RemoteException e) {
			System.out.println("error !!!");
			e.printStackTrace();
		}

		return selectUsersResponse;

	}

	/**
	 * Select files by fileSearch or name or nameList, exclusively.
	 * @param fileSearch
	 * @param name
	 * @param nameList
	 * @return SelectFilesResponse
	 */
	public com.actuate.schemas.SelectFilesResponse selectFiles(
		com.actuate.schemas.FileSearch fileSearch,
		String name,
		ArrayOfString nameList) {
		if (null == authenticationId)
			login();

		com.actuate.schemas.ArrayOfString resultDef =
			newArrayOfString(
				new String[] {
					"Description",
					"FileType",
					"Id",
					"Name",
					"Owner",
					"PageCount",
					"Size",
					"TimeStamp",
					"UserPermissions",
					"Version",
					"VersionName" });

		com.actuate.schemas.SelectFiles selectFiles =
			new com.actuate.schemas.SelectFiles();
		selectFiles.setResultDef(resultDef);

		if (fileSearch != null)
			selectFiles.setSearch(fileSearch);
		else if (name != null)
			selectFiles.setName(name);
		else if (nameList != null)
			selectFiles.setNameList(nameList);

		com.actuate.schemas.SelectFilesResponse selectFilesResponse = null;
		try {
			selectFilesResponse = proxy.selectFiles(selectFiles);

			com.actuate.schemas.ArrayOfFile itemList =
				selectFilesResponse.getItemList();
			com.actuate.schemas.File[] files = itemList.getFile();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					printFile(System.out, files[i]);
				}
			}
		} catch (RemoteException e) {
			System.out.println("error !!!");
			e.printStackTrace();
		}
		return selectFilesResponse;
	}

	/**
	 * Utility function to print properties of com.actuate.schemas.File to an output stream
	 * @param out
	 * @param file
	 */
	public static void printFile(
		PrintStream out,
		com.actuate.schemas.File file) {
		Calendar calendar = file.getTimeStamp();
		String xmlDate =
			""
				+ calendar.get(Calendar.YEAR)
				+ "-"
				+ (calendar.get(Calendar.MONTH) + 1)
				+ "-"
				+ calendar.get(Calendar.DAY_OF_MONTH)
				+ "T"
				+ calendar.get(Calendar.HOUR_OF_DAY)
				+ ":"
				+ calendar.get(Calendar.MINUTE)
				+ ":"
				+ calendar.get(Calendar.SECOND);
		;
		/*
		(1900+date.getYear())+"-"+
							date.getMonth()+"-"+
							date.getDate()+"T"+
							date.getHours()+":"+
							date.getMinutes()+":"+
							date.getSeconds()
							*/;

		System.out.println(
			file.getId()
				+ "\t"
				+ file.getName()
				+ "\t"
				+ file.getDescription()
				+ "\t"
				+ file.getFileType()
				+ "\t"
				+ file.getOwner()
				+ "\t"
				+ file.getPageCount()
				+ "\t"
				+ xmlDate
				+ "\t"
				+ file.getUserPermissions()
				+ "\t"
				+ file.getVersion()
				+ "\t"
				+ file.getVersionName()
				+ "\t"
				+ file.getSize());
	}

	/**
	 * Utility function to save an embedded attachment to a directory
	 * @param attachment
	 * @param downloadDirectory
	 * @return String	the file name saved
	 */
	public static String saveAttachment(
		com.actuate.schemas.Attachment attachment,
		String downloadDirectory) {
		if (null == attachment)
			return null;
		if (null == downloadDirectory)
			downloadDirectory = ".";

		// an embedded attachment is stored in the ContentData
		byte[] contentData = attachment.getContentData();

		if (contentData != null) {

			String FileName = attachment.getContentId();
			String saveName = downloadDirectory + "/" + FileName;

			try {
				FileOutputStream os = new FileOutputStream(saveName);
				os.write(contentData);
			} catch (Exception e) {
				System.out.println("Failed to save file");
			}

			return FileName;

		}
		return null;
	}

	/**
	 * Utility function that computes output FileName for a give input file.
	 * A ROX input has ROI output, a JOD input has ROW output
	 *
	 * @param inputFileName
	 * @return String
	 */
	public static String getDefaultOutputName(String inputFileName) {
		int semiColonIndex = inputFileName.lastIndexOf(';');
		if (-1 != semiColonIndex)
			inputFileName = inputFileName.substring(0, semiColonIndex);
		String FileName = "output.roi";
		int dotIndex = inputFileName.lastIndexOf('.');
		if (-1 != dotIndex) {
			String extension =
				inputFileName.substring(dotIndex + 1).toLowerCase();
			if (extension.equals("rox")) {
				FileName = inputFileName.substring(0, dotIndex + 1) + "roi";
			} else if (extension.equals("jod")) {
				FileName = inputFileName.substring(0, dotIndex + 1) + "row";
			}
		}
		return FileName;
	}

	/**
	 * Convenient function to create User.
	 *
	 * @param username
	 * @param password
	 * @param homeFolder
	 * @return User
	 */
	public static com.actuate.schemas.User newUser(
		String username,
		String password,
		String homeFolder) {

		com.actuate.schemas.User user = new com.actuate.schemas.User();
		user.setName(username);
		user.setPassword(password);
		user.setHomeFolder(homeFolder);

		return user;
	}

	/**
	 * Convenient function to create ParameterValue.
	 *
	 * @param name 	Parameter Name
	 * @param value	Parameter Value
	 * @return ParameterValue
	 */
	public static com.actuate.schemas.ParameterValue newParameterValue(
		String name,
		String value) {

		com.actuate.schemas.ParameterValue parameterValue =
			new com.actuate.schemas.ParameterValue();
		parameterValue.setName(name);
		parameterValue.setValue(value);
		return parameterValue;
	}

	/**
	 * Convenient function to create ArrayOfString.
	 *
	 * @param strings	Java's String[]
	 * @return ArrayOfString
	 */
	public static com.actuate.schemas.ArrayOfString newArrayOfString(
		String[] strings) {
		com.actuate.schemas.ArrayOfString arrayOfString =
			new com.actuate.schemas.ArrayOfString();
		arrayOfString.setString(strings);
		return arrayOfString;
	}

	/**
	 * Utility function that returns ArrayOfString in HTML like this:
	 * Title<BR>
	 * <LI>string1
	 * <LI>string2 ...
	 *
	 * @param title
	 * @param arrayOfString
	 * @return String
	 */
	public static String htmlList(String title, ArrayOfString arrayOfString) {
		StringBuffer stringBuffer = new StringBuffer();
		if (arrayOfString != null) {
			String[] strings = arrayOfString.getString();
			stringBuffer.append(title + "<BR>");
			for (int i = 0; i < strings.length; i++) {
				stringBuffer.append("<LI>" + strings[i]);
			}
			stringBuffer.append("<BR>");
		}
		return stringBuffer.toString();
	}

	/* Get/Set methods
	 */

	/**
	 * Sets the ActuateServerURL
	 * @param serverURL The actuate server url to set
	 */
	public void setActuateServerURL(String serverURL)
		throws MalformedURLException, ServiceException {
		if ((proxy == null) || !serverURL.equals(actuateServerURL)) {
			if (serverURL != null)
				actuateServerURL = serverURL;
			System.out.println("Setting server to " + actuateServerURL);			
			proxy = (ActuateSoapBindingStub)
				actuateAPI.getActuateSoapPort(
					new java.net.URL(actuateServerURL));
		}
	}

	/**
	 * Sets the current directory for getFolderItems operation.
	 * @param directory
	 */
	public void setCurrentDirectory(String directory) {
		if (directory != null) {
			getFolderItemsFetchHandle = null;
			currentDirectory = directory;
		}
		if (!currentDirectory.endsWith("/"))
			currentDirectory = currentDirectory + "/";
	}

	/**
	 * Returns the authenticationId. Set username and password, then use login()
	 * to get authentication from the server.
	 *
	 * @return String	The authentication id
	 */
	public String getAuthenticationId() {
		return authenticationId;
	}

	/**
	 * Returns the currentDirectory for getFolderItems operation.
	 * @return String
	 */
	public String getCurrentDirectory() {
		return currentDirectory;
	}

	/**
	 * Returns the inputFileName. This is used for test SubmitJob, ExecuteReport
	 * and DownloadFile
	 *
	 * @return String
	 */
	public String getInputFileName() {
		return inputFileName;
	}

	/**
	 * Returns the outputFileName.
	 * @return String
	 */
	public String getOutputFileName() {
		return outputFileName;
	}

	/**
	 * Returns the password.
	 * @return String
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the username.
	 * @return String
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the inputFileName.
	 * @param inputFileName The inputFileName to set
	 */
	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	/**
	 * Sets the outputFileName.
	 * @param outputFileName The outputFileName to set
	 */
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	/**
	 * Sets the password.
	 * @param password The password to set
	 */
	public void setPassword(String password) {
		if (password == null)
			password = "";
		this.password = password;
	}

	public void setTargetVolume(String targetVolume) {
		if (targetVolume== null)
			targetVolume= "";
		this.targetVolume = targetVolume;
	}

	public void setTargetServer(String targetServer) {
		if (targetServer== null)
			targetServer= "";
		this.targetServer = targetServer;
	}

	/**
	 * Sets the username.
	 * @param username The username to set
	 */
	public void setUsername(String username) {
		if (username == null)
			return;
		this.username = username;
	}

	/**
	 * @return
	 */
	public String getConnectionHandle() {
		return actuateAPI.getConnectionHandle();
	}

	/**
	 * @param string
	 */
	public void setConnectionHandle(String string) {
		actuateAPI.setConnectionHandle(string);
	}

	private final static String[] hex = {
			"00", "01", "02", "03", "04", "05", "06", "07",
			"08", "09", "0a", "0b", "0c", "0d", "0e", "0f",
			"10", "11", "12", "13", "14", "15", "16", "17",
			"18", "19", "1a", "1b", "1c", "1d", "1e", "1f",
			"20", "21", "22", "23", "24", "25", "26", "27",
			"28", "29", "2a", "2b", "2c", "2d", "2e", "2f",
			"30", "31", "32", "33", "34", "35", "36", "37",
			"38", "39", "3a", "3b", "3c", "3d", "3e", "3f",
			"40", "41", "42", "43", "44", "45", "46", "47",
			"48", "49", "4a", "4b", "4c", "4d", "4e", "4f",
			"50", "51", "52", "53", "54", "55", "56", "57",
			"58", "59", "5a", "5b", "5c", "5d", "5e", "5f",
			"60", "61", "62", "63", "64", "65", "66", "67",
			"68", "69", "6a", "6b", "6c", "6d", "6e", "6f",
			"70", "71", "72", "73", "74", "75", "76", "77",
			"78", "79", "7a", "7b", "7c", "7d", "7e", "7f",
			"80", "81", "82", "83", "84", "85", "86", "87",
			"88", "89", "8a", "8b", "8c", "8d", "8e", "8f",
			"90", "91", "92", "93", "94", "95", "96", "97",
			"98", "99", "9a", "9b", "9c", "9d", "9e", "9f",
			"a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7",
			"a8", "a9", "aa", "ab", "ac", "ad", "ae", "af",
			"b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7",
			"b8", "b9", "ba", "bb", "bc", "bd", "be", "bf",
			"c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7",
			"c8", "c9", "ca", "cb", "cc", "cd", "ce", "cf",
			"d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7",
			"d8", "d9", "da", "db", "dc", "dd", "de", "df",
			"e0", "e1", "e2", "e3", "e4", "e5", "e6", "e7",
			"e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef",
			"f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7",
			"f8", "f9", "fa", "fb", "fc", "fd", "fe", "ff"
		};
	
	public static final String encode(String s, char charEnc)
		{
			if (s == null)
			{
				return null;
			}

			StringBuffer sbuf = new StringBuffer();
			final char chrarry[] = s.toCharArray();

			for (int i = 0; i < chrarry.length; i++)
			{
				int ch = chrarry[i];
				if ('A' <= ch && ch <= 'Z')
				{		// 'A'..'Z'
					sbuf.append(chrarry[i]);
				}
				else if ('a' <= ch && ch <= 'z')	// 'a'..'z'
				{
					sbuf.append(chrarry[i]);
				}
				else if ('0' <= ch && ch <= '9')	// '0'..'9'
				{
					sbuf.append(chrarry[i]);
				}
				else if (ch <= 0x007f)	// other ASCII
				{
					sbuf.append(charEnc + hex[ch]);
				}
				else if (ch <= 0x07FF)		// non-ASCII <= 0x7FF
				{
					sbuf.append(charEnc + hex[0xc0 | (ch >> 6)]);
					sbuf.append(charEnc + hex[0x80 | (ch & 0x3F)]);
				}
				else					// 0x7FF < ch <= 0xFFFF
				{
					sbuf.append(charEnc + hex[0xe0 | (ch >> 12)]);
					sbuf.append(charEnc + hex[0x80 | ((ch >> 6) & 0x3F)]);
					sbuf.append(charEnc + hex[0x80 | (ch & 0x3F)]);
				}
			}
			return sbuf.toString();
		}
}
