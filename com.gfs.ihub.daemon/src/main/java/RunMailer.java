import java.io.IOException;
import java.sql.SQLException;

import com.gfs.ihub.email.Mailer;
import com.gfs.ihub.options.ActuateOptions;
import com.gfs.ihub.options.FileOptions;
import com.gfs.ihub.options.SmtpOptions;
import com.gfs.ihub.options.SqlOptions;
import com.innoventsolutions.idapihelper.IdapiHelperException;

public class RunMailer extends BaseDaemon {
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws IdapiHelperException
	 * @throws SQLException
	 */
	public static void main(final String[] args) throws IdapiHelperException,
			IOException, SQLException {
		
		RunMailer.init(args);
		
		final ActuateOptions actuateOptions = new ActuateOptions(
				CONFIG_DIR, ALT_CONFIG_DIR, "actuate.properties",
				"http://hippo.gfsprod.nt.gfs.com:8000",
				"GFS Operational Reporting", "vm7ji", "5Clocks!");

		final SmtpOptions smtpOptions = new SmtpOptions(
				CONFIG_DIR, ALT_CONFIG_DIR, "smtp.properties", "smtp.gfs.com",
				25, null, null, false, false, false, "noreply@gfs.com");

		final SqlOptions sqlOptions = new SqlOptions(CONFIG_DIR,
				ALT_CONFIG_DIR, "sql.properties",
				"jdbc:oracle:thin:@dtw01t.grhq.gfs.com:45064:DTW01T",
				"ACTUATE_NOTIFY_APPL", null);

		final FileOptions fileOptions = new FileOptions(
				CONFIG_DIR, ALT_CONFIG_DIR, "file.properties", CONFIG_DIR
						+ "/email_attachments", ALT_CONFIG_DIR
						+ "/email_attachments");

		final Mailer mailer = new Mailer(actuateOptions, smtpOptions,
				sqlOptions, fileOptions);

		mailer.processJobs();
		mailer.close();
	}
}
