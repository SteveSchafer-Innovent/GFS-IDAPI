import java.io.IOException;

import com.gfs.ihub.email.Mailer;
import com.innoventsolutions.idapihelper.IdapiHelperException;

public class TestMailer {

	/**
	 * @param args
	 * @throws IOException
	 * @throws IdapiHelperException
	 */
	public static void main(final String[] args) throws IdapiHelperException,
			IOException {
		final String serverURL = "http://hippo.gfsprod.nt.gfs.com:8000";
		final String volume = "GFS Operational Reporting";
		final String username = "vm7ji";
		final String password = "5Clocks!";
		final Mailer mailer = new Mailer(serverURL, volume, username, password);
		mailer.processJobs();
	}

}
