import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.gfs.ihub.cypress.CypressDownloader;
import com.gfs.ihub.email.Mailer;
import com.gfs.ihub.options.ActuateOptions;
import com.gfs.ihub.options.CypressOptions;
import com.gfs.ihub.options.SmtpOptions;
import com.gfs.ihub.options.SqlOptions;
import com.innoventsolutions.idapihelper.IdapiHelperException;

public class RunIdapiProcess {

	/**
	 * @param args
	 * @throws IOException
	 * @throws IdapiHelperException
	 * @throws SQLException
	 * @throws MessagingException
	 */
	public static void main(final String[] args) throws IdapiHelperException,
			IOException, SQLException, MessagingException {

		final String defaultConfig = "D:/Actuate3/BIRTiHubVisualization/modules/BIRTiHub/iHub/data/server/log/mailer";
		final String defaultOperation = "both";

		final CommandLineParser parser = new PosixParser();
		final Options options = new Options();
		options.addOption("h", "help", false, "print this message");
		options.addOption("o", "operation", true,
				"what operation to perform (both is default)\n[both, email or cypress]");
		final String hMsg = "config directory default value [" + defaultConfig
				+ "]";
		options.addOption("d", "directory", true, hMsg);

		CommandLine line;
		try {
			line = parser.parse(options, args);
		} catch (final Exception ee) {
			System.out.println("General Error in command line "
					+ ee.getMessage());
			throw new IdapiHelperException("Error in command line: "
					+ ee.getMessage());
		}
		if (line.hasOption("h")) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(100, "IDAPI Daemon Command Line Options\n", "",
					options, "");
			return;
		}
		final String configDir = line.getOptionValue("d", defaultConfig);
		final String operation = line.getOptionValue("o", defaultOperation);

		final ActuateOptions actuateOptions = new ActuateOptions(configDir,
				"actuate.properties");
		final SmtpOptions smtpOptions = new SmtpOptions(configDir,
				"smtp.properties");
		final SqlOptions sqlOptions = new SqlOptions(configDir,
				"sql.properties");
		final CypressOptions cypressOptions = new CypressOptions(configDir,
				"cypress.properties");

		System.out.println("*** START DAEMON: " + new Date());
		if (defaultOperation.equals(operation) || "email".equals(operation)) {
			System.out.println("*** START EMAIL: " + new Date());
			try {
				final Mailer mailer = new Mailer(actuateOptions, smtpOptions,
						sqlOptions);
				mailer.processJobs();
				mailer.close();
			} catch (final Exception ex) {
				System.out.println("Failure while running email "
						+ ex.getMessage());
			}
			System.out.println("*** END EMAIL: " + new Date());
		}
		if (defaultOperation.equals(operation) || "cypress".equals(operation)) {
			System.out.println("*** START CYPRESS: " + new Date());
			try {
				final CypressDownloader cypressDownloader = new CypressDownloader(
						actuateOptions, cypressOptions);
				final List<String> messages = cypressDownloader.downloadFiles();
				for (final String msg : messages) {
					System.out.println(msg);
				}
			} catch (final Exception ex) {
				System.out.println("Failure while running Cypress "
						+ ex.getMessage());
			}
			System.out.println("*** END CYPRESS: " + new Date());
		}
		System.out.println("*** END DAEMON: " + new Date());

	}
}
