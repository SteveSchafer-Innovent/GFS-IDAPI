import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.gfs.ihub.cypress.CypressDownloader;
import com.gfs.ihub.options.ActuateOptions;
import com.gfs.ihub.options.CypressOptions;

public class SendToCypress extends BaseDaemon {

	public static void main(final String[] args) throws IOException {

		SendToCypress.init(args);

		final ActuateOptions actuateOptions = new ActuateOptions(CONFIG_DIR,
				"actuate.properties");

		final CypressOptions cypressOptions = new CypressOptions(CONFIG_DIR,
				"cypress.properties");

		try {
			System.out.println("Start time: " + new Date());
			// Connect to the iServer
			final CypressDownloader cypressDownloader = new CypressDownloader(
					actuateOptions, cypressOptions);

			System.out
					.println("Connected to server. Getting Files from the Encyc Volume...");
			final List<String> messages = cypressDownloader.downloadFiles();
			for (final String msg : messages) {
				System.out.println(msg);
			}

			System.out.println("End time: " + new Date());
			System.out
					.println("***********************************************************");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
