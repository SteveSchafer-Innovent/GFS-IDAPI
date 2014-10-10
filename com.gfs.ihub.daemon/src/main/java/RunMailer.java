import java.io.IOException;
import java.sql.SQLException;

import javax.mail.MessagingException;

import com.gfs.ihub.email.Mailer;
import com.gfs.ihub.options.ActuateOptions;
import com.gfs.ihub.options.SmtpOptions;
import com.gfs.ihub.options.SqlOptions;
import com.innoventsolutions.idapihelper.IdapiHelperException;

public class RunMailer extends BaseDaemon {

	/**
	 * @param args
	 * @throws IOException
	 * @throws IdapiHelperException
	 * @throws SQLException
	 * @throws MessagingException
	 */
	public static void main(final String[] args) throws IdapiHelperException,
			IOException, SQLException, MessagingException {

		RunMailer.init(args);

		final ActuateOptions actuateOptions = new ActuateOptions(CONFIG_DIR,
				"actuate.properties");

		final SmtpOptions smtpOptions = new SmtpOptions(CONFIG_DIR,
				"smtp.properties");

		final SqlOptions sqlOptions = new SqlOptions(CONFIG_DIR,
				"sql.properties");

		final Mailer mailer = new Mailer(actuateOptions, smtpOptions,
				sqlOptions);

		mailer.processJobs();
		mailer.close();
	}
}
