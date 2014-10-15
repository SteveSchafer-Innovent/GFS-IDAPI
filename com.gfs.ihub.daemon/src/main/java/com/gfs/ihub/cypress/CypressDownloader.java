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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	 * @throws IdapiHelperException 
	 * @throws MalformedURLException 
	 */
	public CypressDownloader(final ActuateOptions actuateOptions, final CypressOptions cypressOptions) throws MalformedURLException,
			IdapiHelperException {
		this(actuateOptions, cypressOptions, null);
	}

	/**
	 * Constructor
	 */
	public CypressDownloader(final ActuateOptions actuateOptions, final CypressOptions cypressOptions, IdapiHelper optionalIdapiHelper)
			throws MalformedURLException, IdapiHelperException {

		if (optionalIdapiHelper == null) {
			URL serverURL = new URL(actuateOptions.getUrlString());
			this.helper = IdapiHelperImpl.getInstance(new URL[] { serverURL });
		} else {
			this.helper = optionalIdapiHelper;
		}

		helper.login(actuateOptions.getVolume(), actuateOptions.getUsername(), actuateOptions.getPassword(), new byte[0], false);

		this.iHubFolderName = cypressOptions.getiHubCypressFolderName();
		String tmpFolderName = cypressOptions.getOsDestinationFolder();
		if (!tmpFolderName.endsWith(java.io.File.separator)) {
			tmpFolderName += java.io.File.separator;
		}
		this.osDestinationFolderName = tmpFolderName;

	}

	//Downloads and Deletes pdf files from encyc volume
	public List<String> downloadFiles() {

		List<String> messages = new ArrayList<String>();

		Map<String, String> pdfFilesToProcess = getPDFInFolder();
		for (Entry<String, String> iHubFile : pdfFilesToProcess.entrySet()) {
			messages.add(downloadFile(iHubFile));
			messages.add(deleteFile(iHubFile));
		}

		return messages;
	}

	/**
	 * Returns a list of all the FileIds for any PDFs in a Given Folder
	 *
	 * @return GetFolderItemsResponse
	 */
	private Map<String, String> getPDFInFolder() {

		Map<String, String> filesToProcess = new HashMap<String, String>();

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
				File iHubFile = itemList.getFile(i);
				filesToProcess.put(iHubFile.getId(), createUniqueFileName(iHubFile));

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return filesToProcess;

	}

	/*
	 * Only works with PDF file names
	 */
	private String createUniqueFileName(final File iHubFile) {
		StringBuffer sb = new StringBuffer();
		String fName = iHubFile.getName();
		sb.append(fName.subSequence(0, fName.toLowerCase().indexOf(".pdf")));
		sb.append("_").append(iHubFile.getVersion());
		sb.append(".pdf");

		return sb.toString();
	}

	private String downloadFile(final Entry<String, String> iHubFile) {

		String iHubFileNm = iHubFile.getValue();
		StringBuffer sb = new StringBuffer();
		sb.append("download: ").append(iHubFileNm);

		DownloadFile downloadFile = new DownloadFile();
		downloadFile.setDownloadEmbedded(Boolean.TRUE);
		downloadFile.setFileId(iHubFile.getKey());

		try {
			DownloadFileResponse downloadFileResponse = helper.downloadFile(downloadFile);

			//Enter the path to your network folder. Download the file to the network directory.
			String fileName = iHubFileNm.substring(iHubFileNm.lastIndexOf("/") + 1, iHubFileNm.length());
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
	private String deleteFile(Entry<String, String> iHubFile) {

		StringBuffer sb = new StringBuffer();
		sb.append("Delete: ").append(iHubFile.getValue());
		final DeleteFile deleteFile = new DeleteFile();
		deleteFile.setId(iHubFile.getKey());

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
