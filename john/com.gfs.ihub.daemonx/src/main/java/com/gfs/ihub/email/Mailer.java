package com.gfs.ihub.email;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gfs.ihub.options.ActuateOptions;
import com.gfs.ihub.options.SmtpOptions;
import com.gfs.ihub.options.SqlOptions;

public class Mailer implements AutoCloseable, ActuateInterface.JobProcessor {
	private static final Pattern FILENAME_PATTERN = Pattern
			.compile("/(?:[^/]*/)+([^;]+).*");
	private final String defaultFrom;
	private final Logger logger;
	private final ActuateInterface actuateInterface;
	private final DatabaseInterface databaseInterface;
	private final EmailInterface emailInterface;

	public Mailer(final ActuateOptions actuateOptions,
			final SmtpOptions smtpOptions, final SqlOptions sqlOptions)
			throws IOException {
		final Logger logger = new Logger();
		this.logger = logger;

		actuateInterface = new ActuateInterfaceImpl(actuateOptions, logger);

		databaseInterface = new DatabaseInterfaceImpl(sqlOptions, logger);

		this.defaultFrom = smtpOptions.getDefaultFrom();
		this.emailInterface = new EmailInterfaceImpl(smtpOptions, logger);
	}

	public void close() {
		databaseInterface.close();
	}

	public void processJobs() throws IOException {
		actuateInterface.processJobs(this);
	}

	public void processJob(final String contentType, final String emailFrom,
			final String emailTo, final String emailSubject,
			final String emailBody, final String fileName,
			final java.io.File outputFile, final long jobId) {
		try {
			logger.log("Sending job " + jobId);
			final int mimeTypePk = databaseInterface
					.addMimeTypeToDB(contentType);

			final String from = emailFrom == null ? defaultFrom : emailFrom;
			final String[] to = emailTo.split("[ ]*[,;][ ]*");
			final String[] cc = new String[0];
			final String[] bcc = new String[0];
			final String subject = (emailSubject == null || emailSubject.equals("")) ? "BIRT report"
					: emailSubject;
			final String body = (emailBody == null | emailBody.equals("")) ? "BIRT report" : emailBody;

			final Matcher matcher = FILENAME_PATTERN.matcher(fileName);

			final String trimmedFilename = matcher.find() ? matcher.group(1)
					: fileName;

			emailInterface.sendMail(from, to, cc, bcc, subject, body, false,
					trimmedFilename, outputFile, contentType);

			final int senderPk = databaseInterface.addEmailAddressToDB(from);

			final int notificationId = databaseInterface.addNotificationToDB(
					jobId, senderPk, mimeTypePk, subject, body, fileName);

			for (int i = 0; i < to.length; i++) {
				final String address = to[i];
				final int addressId = databaseInterface
						.addEmailAddressToDB(address);
				databaseInterface.addRecipientToDB(addressId, notificationId);
			}
		} catch (final Exception e) {
			throw new RuntimeException("Failed to process job " + jobId, e);
		}
	}

	public boolean jobHasBeenProcessed(final long jobId) {
		try {
			return databaseInterface.notificationExistsInDB(jobId);
		} catch (final Exception e) {
			throw new RuntimeException("Failed to check if job " + jobId
					+ " has been processed", e);
		}
	}
}
