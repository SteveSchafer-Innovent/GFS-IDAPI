package com.gfs.ihub.cypress;

/*

Actuate Client Example

This class controls operation between ActuateServer and user
application.
*/

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.actuate.schemas.AdminOperation;
import com.actuate.schemas.Administrate;
import com.actuate.schemas.ArrayOfFile;
import com.actuate.schemas.ArrayOfString;
import com.actuate.schemas.Attachment;
import com.actuate.schemas.DeleteFile;
import com.actuate.schemas.DownloadFile;
import com.actuate.schemas.DownloadFileResponse;
import com.actuate.schemas.File;
import com.actuate.schemas.FileCondition;
import com.actuate.schemas.FileField;
import com.actuate.schemas.FileSearch;
import com.actuate.schemas.GetFolderItems;
import com.actuate.schemas.GetFolderItemsResponse;
import com.gfs.ihub.options.ActuateOptions;
import com.gfs.ihub.options.CypressOptions;
import com.innoventsolutions.consts.ResultDefConsts;
import com.innoventsolutions.idapihelper.IdapiHelper;
import com.innoventsolutions.idapihelper.IdapiHelperException;
import com.innoventsolutions.idapihelper.IdapiHelperImpl;

public class CypressDownloader implements java.io.Serializable {

	private static final long serialVersionUID = 2215797365570454994L;
	private final IdapiHelper helper;
	private final String iHubFolderName;
	private final String osDestinationFolderName;

	/**
	 * Constructor
	 */
	public CypressDownloader(final ActuateOptions actuateOptions, final CypressOptions cypressOptions) throws MalformedURLException, IdapiHelperException {

		URL serverURL = new URL(actuateOptions.getUrlString());
		helper = IdapiHelperImpl.getInstance(new URL[] { serverURL });
		helper.login(actuateOptions.getVolume(), actuateOptions.getUsername(), actuateOptions.getPassword(), new byte[0], false);
		
		this.iHubFolderName = cypressOptions.getiHubCypressFolderName();
		this.osDestinationFolderName = cypressOptions.getOsDestinationFolder();

	}

	//Downloads and Deletes pdf files from encyc volume
	public List<String> downloadFiles() {

		List<String> messages = new ArrayList<String>();

		List<String> pdfFilesToProcess = getPDFInFolder();
		for (String iHubFileName : pdfFilesToProcess) {
			messages.add(downloadFile(iHubFileName));
			messages.add(deleteFile(iHubFileName));
		}

		return messages;
	}

	/**
	 * Returns a list of all the FileIds for any PDFs in a Given Folder
	 *
	 * @return GetFolderItemsResponse
	 */
	private List<String> getPDFInFolder() {

		List<String> filesToProcess = new ArrayList<String>();

		ArrayOfString resultDef = ResultDefConsts.getFolderItemsResultDef();

		FileSearch fileSearch = new FileSearch();
		FileCondition fileCondition = new FileCondition();
		fileCondition.setField(FileField.FileType);
		fileCondition.setMatch("PDF");

		fileSearch.setCondition(fileCondition);

		GetFolderItems request = new GetFolderItems();
		request.setSearch(fileSearch);
		request.setFolderName(this.iHubFolderName);
		request.setLatestVersionOnly(Boolean.FALSE);
		request.setResultDef(resultDef);

		FileSearch search = new FileSearch();
		search.setFetchDirection(Boolean.TRUE);

		request.setSearch(search);
		GetFolderItemsResponse response = null;
		try {
			response = helper.getFolderItems(request);

			ArrayOfFile itemList = response.getItemList();
			for (int i = 0; i < response.getTotalCount().intValue(); i++) {
				File file = itemList.getFile(i);
				filesToProcess.add(file.getName());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return filesToProcess;

	}

	private String downloadFile(final String iHubFileName) {

		StringBuffer sb = new StringBuffer();
		sb.append("download: ").append(iHubFileName);

		DownloadFile downloadFile = new DownloadFile();
		downloadFile.setDownloadEmbedded(Boolean.TRUE);
		downloadFile.setFileName(iHubFileName);

		try {
			DownloadFileResponse downloadFileResponse = helper.downloadFile(downloadFile);

			//Enter the path to your network folder. Download the file to the network directory.
			String fileName = iHubFileName.substring(iHubFileName.lastIndexOf("/") + 1, iHubFileName.length());
			String dlFullFilePath = this.osDestinationFolderName + fileName;
			FileOutputStream fos = new FileOutputStream(dlFullFilePath);
			Attachment attachment = downloadFileResponse.getContent();
			fos.write(attachment.getContentData());
			fos.close();
			sb.append(" success");

		} catch (RemoteException e) {
			sb.append(" ERROR REMOTE EXCEPTION").append(e.getMessage());
		} catch (FileNotFoundException e) {
			sb.append(" ERROR REMOTE EXCEPTION").append(e.getMessage());
		} catch (IOException e) {
			sb.append(" ERROR REMOTE EXCEPTION").append(e.getMessage());
		}

		return sb.toString();

	}

	//Delete the output file from the iServer
	private String deleteFile(String iHubFileName) {

		StringBuffer sb = new StringBuffer();
		sb.append("Delete: ").append(iHubFileName);
		final DeleteFile deleteFile = new DeleteFile();
		deleteFile.setName(iHubFileName);

		final AdminOperation adminOperation = new AdminOperation();
		adminOperation.setDeleteFile(deleteFile);

		final Administrate administrate = new Administrate();
		administrate.setAdminOperation(new AdminOperation[] { adminOperation });
		try {
			helper.administrate(administrate);
			sb.append(" success");
		} catch (final RemoteException e) {
			sb.append(" ERROR: ").append(e.getMessage());
		}

		return sb.toString();

	}

}
