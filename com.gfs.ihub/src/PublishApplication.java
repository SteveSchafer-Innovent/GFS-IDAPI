import com.gfs.ihub.promote.Publisher;

public class PublishApplication {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String volume = "testvol";
		String urlString = "http://deephaven:8000"; //GFS DEV = http://hippo.gfsprod.nt.gfs.com:8000
		String username = "administrator";
		String password = "";

		Publisher publisher = new Publisher(urlString, volume, username, password);

		System.out.println(publisher.getFiles("/").toString());

		String serverFileName = "/upload/test.zip";
		publisher.deleteFile(serverFileName);

		Float fileId = publisher.upload("C:\\workspace\\Birt_ihub\\com.gfs.ihub\\test.zip", serverFileName);
		String appId = null;
		if (fileId > 0) {
			String appName = "test";
			String appDescription = "Application Description";
			Boolean replaceExisting = true;
			appId = publisher.publish(appName, appDescription, serverFileName, replaceExisting);
		}
		
		System.out.println("App Id: " + appId);

	}

}
