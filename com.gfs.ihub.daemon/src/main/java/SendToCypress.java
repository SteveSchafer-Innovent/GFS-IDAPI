import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.gfs.ihub.cypress.CypressDownloader;
import com.gfs.ihub.options.ActuateOptions;
import com.gfs.ihub.options.CypressOptions;

public class SendToCypress extends BaseDaemon {

	public static void main(String[] args) throws IOException {

		SendToCypress.init(args);

		final ActuateOptions actuateOptions = new ActuateOptions(CONFIG_DIR, ALT_CONFIG_DIR, "actuate.properties",
				"http://hippo.gfsprod.nt.gfs.com:8000", "GFS Operational Reporting", "vm7ji", "5Clocks!");

		final CypressOptions cypressOptions = new CypressOptions(CONFIG_DIR, ALT_CONFIG_DIR, "cypress.properties", "", "\\\\wolverine\\FTin\\Operational Reporting\\");

		try {
			System.out.println("Start time: " + new Date());
			// Connect to the iServer
			final CypressDownloader cypressDownloader = new CypressDownloader(actuateOptions, cypressOptions);

			System.out.println("Connected to server. Getting Files from the Encyc Volume...");
			List<String> messages = cypressDownloader.downloadFiles();
			for (String msg : messages) {
				System.out.println(msg);
			}

			System.out.println("End time: " + new Date());
			System.out.println("***********************************************************");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
