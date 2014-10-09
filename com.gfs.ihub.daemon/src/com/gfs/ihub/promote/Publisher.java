package com.gfs.ihub.promote;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.actuate.schemas.AdminOperation;
import com.actuate.schemas.Administrate;
import com.actuate.schemas.Attachment;
import com.actuate.schemas.DeleteFile;
import com.actuate.schemas.File;
import com.actuate.schemas.GetFolderItems;
import com.actuate.schemas.GetFolderItemsResponse;
import com.actuate.schemas.InstallApp;
import com.actuate.schemas.InstallAppResponse;
import com.actuate.schemas.NewFile;
import com.actuate.schemas.UploadFile;
import com.actuate.schemas.UploadFileResponse;
import com.actuate.schemas.VersioningOption;
import com.innoventsolutions.consts.ResultDefConsts;
import com.innoventsolutions.idapihelper.IdapiHelper;
import com.innoventsolutions.idapihelper.IdapiHelperException;
import com.innoventsolutions.idapihelper.IdapiHelperImpl;

public class Publisher {

	private final IdapiHelper helper;

	/**
	 * 
	 * @param urlString  	http://hippo.gfsprod.nt.gfs.com:8000
	 * @param volume		"GFS Operational Reporting"
	 * @param username		
	 * @param password
	 * 
	 * @throws MalformedURLException
	 * @throws IdapiHelperException
	 */
	public Publisher(String urlString, String volume, String username, String password) throws MalformedURLException, IdapiHelperException {

		URL serverURL = new URL(urlString);
		helper = IdapiHelperImpl.getInstance(new URL[] { serverURL });
		helper.login(volume, username, password, new byte[0], false);

	}

	/**
	 * Not required, quick test to see if your connection is working.
	 * 
	 */
	public List<String> getFiles(String folder) {
		List<String> fileList = new ArrayList<String>();

		GetFolderItems gfi = new GetFolderItems();
		gfi.setFolderName(folder);
		gfi.setResultDef(ResultDefConsts.getFilesSimpleResultDef());
		try {
			GetFolderItemsResponse gfir = helper.getFolderItems(gfi);
			File[] theFiles = gfir.getItemList().getFile();
			for (int i = 0; i < theFiles.length; i++) {
				fileList.add(theFiles[i].getName());
			}

		} catch (RemoteException e) {
			System.out.println("Failure To Execute GetFiles, probably a connection issue");
			e.printStackTrace();
		}

		return fileList;
	}

	/**
	 * Publish a zip file that has been uploaded to the server.
	 * 
	 * ZIP file should not contain a top level folder that corresponds to the project e.g. 
	 * the BIRTApplication.xml file is at the root of the zip file.
	 * NOTE: the .project file can not be in the zip folder structure
	 * 
	 * @param appName 
	 * @param appDescription - does not change unless you delete old file
	 * @param zipFilePath - location of the zip file on the server
	 * @param replaceExisting - delete original application first
	 * @return
	 */
	public String publish(String appName, String appDescription, String zipFilePath, Boolean replaceExisting) {
		if (replaceExisting == null) {
			replaceExisting = false;
		}
		if (replaceExisting) {
			deleteFile("/Applications/" + appName);
		}

		InstallApp installApp = new InstallApp();
		installApp.setDescription(appDescription);
		installApp.setName(appName);
		installApp.setZipFilePath(zipFilePath);

		try {
			InstallAppResponse response = helper.installApp(installApp);
			return response.getFolderId();
		} catch (RemoteException e) {
			e.printStackTrace();
			return "Failure to publish " + e.getMessage();
		}

	}

	/**
	 * Upload a file from the local file system to the connected iHub3
	 * 
	 * @param localZipFileName  
	 * @param serverFileName
	 * @return
	 * @throws Exception
	 */
	public Float upload(String localZipFileName, String serverFileName) throws Exception {

		// sanity test make sure file is there
		java.io.File fileToUpload = new java.io.File(localZipFileName);
		if (!fileToUpload.exists()) {
			throw new FileNotFoundException("File " + localZipFileName + " not found");
		}

		this.testForProjectFile(fileToUpload);

		UploadFile uploadFile = new UploadFile();
		//uploadFile needs a NewFile and an Attachment
		//Create a NewFile object
		NewFile newFile = new NewFile();
		newFile.setName(serverFileName);
		newFile.setReplaceExisting(new Boolean(true));
		newFile.setVersioning(VersioningOption.CreateNewVersion);
		uploadFile.setNewFile(newFile);

		// create an attachment
		Attachment attachment = new Attachment();
		attachment.setContentId(fileToUpload.getPath());
		uploadFile.setContent(attachment);
		Path path = Paths.get(fileToUpload.getPath());
		attachment.setContentData(Files.readAllBytes(path));

		try {
			UploadFileResponse response = helper.uploadFile(uploadFile);
			return new Float(response.getFileId());
		} catch (RemoteException e) {
			e.getMessage();
			e.printStackTrace();
			return new Float(-1);
		}

	}

	/*
	 * Quick and dirty delete file implementation, 
	 */
	public void deleteFile(String fileName) {

		DeleteFile deleteFile = new DeleteFile();
		deleteFile.setName(fileName);

		AdminOperation adminOperation = new AdminOperation();
		adminOperation.setDeleteFile(deleteFile);

		Administrate administrate = new Administrate();
		administrate.setAdminOperation(new AdminOperation[] { adminOperation });
		try {
			helper.administrate(administrate);
		} catch (RemoteException e) {
			// If the file does not exist, but no prob
		}
	}

	/**
	 * Make sure zip file does not contain a .project file
	 * 
	 * @param zipFile
	 * @throws Exception
	 */
	private void testForProjectFile(java.io.File zipFile) throws Exception {

		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				if (fileName.endsWith("/.project")) {
					throw new Exception("The zip file for the application can not contain a .project file, please remove and deploy again");
				}

				ze = zis.getNextEntry();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (zis != null) {
				zis.closeEntry();
				zis.close();
			}
		}

	}
}
