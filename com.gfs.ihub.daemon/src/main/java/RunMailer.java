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

		final ActuateOptions actuateOptions = new ActuateOptions(CONFIG_DIR,
				"actuate.properties");

		final SmtpOptions smtpOptions = new SmtpOptions(CONFIG_DIR,
				"smtp.properties");

		final SqlOptions sqlOptions = new SqlOptions(CONFIG_DIR,
				"sql.properties");

		final FileOptions fileOptions = new FileOptions(CONFIG_DIR,
				"file.properties");

		final Mailer mailer = new Mailer(actuateOptions, smtpOptions,
				sqlOptions, fileOptions);

		mailer.processJobs();
		mailer.close();
	}
}
