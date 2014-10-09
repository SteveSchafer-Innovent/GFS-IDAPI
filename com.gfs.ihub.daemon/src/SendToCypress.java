import java.util.Date;
import java.util.List;

import com.gfs.ihub.cypress.DownloadDirectory;

public class SendToCypress {

	/**
	* @param args
	*/
	public static DownloadDirectory downloadDir;
	public String getFolderItemsFetchHandle;
	public Integer getFolderItemsFetchSize = null;

	public static void main(String[] args) {
		String actuateServerURL = "https://actuateService.gfs.com";
		String volume = "GFS Operational Reporting";
		String username = args[0];
		String password = args[1];
		String iHubFolderName = args[2];
		String localFileLocation = args[3];
		String debug = args[4];

		try {
			System.out.println("Start time: " + new Date());
			// Connect to the iServer
			downloadDir = new DownloadDirectory(actuateServerURL, volume, username, password);
			//Login 

			System.out.println("Connected to server. Getting Files from the Encyc Volume...");
			List<String> messages = downloadDir.downloadFiles(iHubFolderName, localFileLocation);
			if ("true".equalsIgnoreCase(debug)){
				for (String msg : messages) {
					System.out.println(msg);
				}
			}
			
			System.out.println("End time: " + new Date());
			System.out.println("***********************************************************");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

}
