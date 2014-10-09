import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import example.ActuateControl;


public class SendToCypress {
 
/**
* @param args
*/
public static ActuateControl actuateControl;
public String getFolderItemsFetchHandle;
public Integer getFolderItemsFetchSize = null;
 
//Downloads and Deletes pdf files from encyc volume
public static void getFiles(String folder) {
	
	List<String> fileList = new ArrayList<String>();
	actuateControl.setCurrentDirectory(folder);
	com.actuate.schemas.GetFolderItemsResponse response = actuateControl.getFolderItems();
	com.actuate.schemas.ArrayOfFile itemList = response.getItemList();

	//Parse through folder for files
	int totalCount = response.getTotalCount().intValue();
	System.out.println("There are " + totalCount + " files ready to send to Cypress");
	for (int i = 0; i <totalCount; i++){
		com.actuate.schemas.File file = itemList.getFile(i);
		
		//Only download PDF files to shared directory
		if(file.getFileType().equalsIgnoreCase("PDF")){
			try {
				downloadFile(folder + file.getName());
				deleteFile(folder + file.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
}

public static com.actuate.schemas.ArrayOfString newArrayOfString(
		String[] strings) {
		com.actuate.schemas.ArrayOfString arrayOfString =
			new com.actuate.schemas.ArrayOfString();
		arrayOfString.setString(strings);
		return arrayOfString;
	}
 
public static void downloadFile(String volFilePath) throws IOException{
	com.actuate.schemas.DownloadFile df = new com.actuate.schemas.DownloadFile();
	com.actuate.schemas.DownloadFileResponse dfr = null;
	com.actuate.schemas.Attachment att = null;
	byte[] content = null; 
	df.setDownloadEmbedded(Boolean.TRUE);
	String dlFullFilePath =  "\\\\wolverine\\FTin\\Operational Reporting\\";
	String fileName = volFilePath.substring(volFilePath.lastIndexOf("/") + 1, volFilePath.length());
	
	//Enter the path to your network folder. Download the file to the network directory.
	dlFullFilePath += fileName;
	df.setFileName(volFilePath);
	dfr = actuateControl.proxy.downloadFile(df); 
	att = dfr.getContent();
	content = att.getContentData();
	FileOutputStream fos = new FileOutputStream(dlFullFilePath);
	fos.write(content); 
	System.out.println("Downloaded " + dlFullFilePath);
	fos.close();
}
 
//Delete the output file from the iServer
public static void deleteFile(String delFileName){
	com.actuate.schemas.DeleteFile m_strDeleteFile = new com.actuate.schemas.DeleteFile ();
	m_strDeleteFile.setName(delFileName);
	com.actuate.schemas.AdminOperation adminOperation = new com.actuate.schemas.AdminOperation ();
	adminOperation.setDeleteFile(m_strDeleteFile);
	actuateControl.runAdminOperation(adminOperation);
	System.out.println (delFileName + " Deleted from the encyc volume");
}
 
public static void main(String[] args) {
	String actuateServerURL = "https://actuateService.gfs.com";
	String volume = "GFS Operational Reporting"; 
	String username = args[0];
	String password = args[1];
	String folderName = args[2];
	Date currTime = new Date(System.currentTimeMillis());
	
		try
		{
			System.out.println("Start time: " + currTime);
			// Connect to the iServer
			actuateControl = new ActuateControl(actuateServerURL);
			//Login 
			actuateControl.setUsername(username);
			actuateControl.setPassword(password);
			actuateControl.setTargetVolume(volume);
			actuateControl.login();
			System.out.println("Connected to server. Getting Files from the Encyc Volume...");
			getFiles(folderName);
			Date endTime = new Date(System.currentTimeMillis());
			System.out.println("End time: " + endTime);
			System.out.println("***********************************************************");
		}	
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
