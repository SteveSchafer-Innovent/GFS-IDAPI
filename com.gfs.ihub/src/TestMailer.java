import java.io.IOException;
import java.sql.SQLException;

import com.gfs.ihub.email.Mailer;
import com.innoventsolutions.idapihelper.IdapiHelperException;

public class TestMailer {
	public static final String CONFIG_DIR = "D:/Actuate3/BIRTiHubVisualization/modules/BIRTiHub/iHub/data/server/log/email.properties";
	public static final String ALT_CONFIG_DIR = "C:/Clients/GFS";

	/**
	 * @param args
	 * @throws IOException
	 * @throws IdapiHelperException
	 * @throws SQLException
	 */
	public static void main(final String[] args) throws IdapiHelperException,
			IOException, SQLException {
		final Mailer.ActuateOptions actuateOptions = new Mailer.ActuateOptions(
				CONFIG_DIR, ALT_CONFIG_DIR, "actuate.properties",
				"http://hippo.gfsprod.nt.gfs.com:8000",
				"GFS Operational Reporting", "vm7ji", "5Clocks!");
		final Mailer.SmtpOptions smtpOptions = new Mailer.SmtpOptions(
				CONFIG_DIR, ALT_CONFIG_DIR, "smtp.properties", "smtp.gfs.com",
				25, null, null, false, false, false, "noreply@gfs.com");
		final Mailer.SqlOptions sqlOptions = new Mailer.SqlOptions(CONFIG_DIR,
				ALT_CONFIG_DIR, "sql.properties",
				"jdbc:postgresql://localhost/gfs_email", "steve", null);
		final Mailer.FileOptions fileOptions = new Mailer.FileOptions(
				CONFIG_DIR, ALT_CONFIG_DIR, "file.properties", CONFIG_DIR
						+ "/email_attachments", ALT_CONFIG_DIR
						+ "/email_attachments");
		final Mailer mailer = new Mailer(actuateOptions, smtpOptions,
				sqlOptions, fileOptions);
		mailer.processJobs();
		mailer.close();
	}
}
