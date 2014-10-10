package com.gfs.ihub.email;

import java.io.IOException;
import java.sql.SQLException;

import javax.mail.MessagingException;

import com.gfs.ihub.options.ActuateOptions;
import com.gfs.ihub.options.SmtpOptions;
import com.gfs.ihub.options.SqlOptions;
import com.innoventsolutions.idapihelper.IdapiHelperException;

public class Mailer implements AutoCloseable, ActuateInterface.JobProcessor {
	private final String defaultFrom;
	private final Logger logger;
	private final ActuateInterface actuateInterface;
	private final DatabaseInterface databaseInterface;
	private final EmailInterface emailInterface;

	public Mailer(final ActuateOptions actuateOptions,
			final SmtpOptions smtpOptions, final SqlOptions sqlOptions)
			throws IdapiHelperException, IOException, SQLException {
		final Logger logger = new Logger();
		this.logger = logger;

		actuateInterface = new ActuateInterfaceImpl(actuateOptions, logger);

		databaseInterface = new DatabaseInterfaceImpl(sqlOptions, logger);

		this.defaultFrom = smtpOptions.getDefaultFrom();
		this.emailInterface = new EmailInterface(smtpOptions, logger);
	}

	public void close() throws SQLException {
		databaseInterface.close();
	}

	public void processJobs() throws IOException, SQLException,
			MessagingException {
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
			final String[] to = emailTo.split(",[ ]*");
			final String[] cc = new String[0];
			final String[] bcc = new String[0];
			final String subject = emailSubject == null ? "BIRT report"
					: emailSubject;
			final String body = emailBody == null ? "BIRT report" : emailBody;

			emailInterface.sendMail(from, to, cc, bcc, subject, body, false,
					fileName, outputFile, contentType);

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
