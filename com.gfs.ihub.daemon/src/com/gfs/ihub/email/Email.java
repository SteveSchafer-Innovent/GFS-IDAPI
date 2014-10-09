package com.gfs.ihub.email;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class Email {
	private final Properties emailProperties;
	private final Logger logger;
	private final PasswordAuthentication pa;

	public Email(final Properties emailProperties, final String username,
			final String password, final Logger logger) throws IOException {
		this.logger = logger;
		this.emailProperties = emailProperties;
		this.pa = username == null ? null : new PasswordAuthentication(
				username, password);
	}

	public void sendMail(final String from, final String[] to,
			final String[] cc, final String[] bcc, final String subject,
			final String body, final boolean multiple, final String fileName,
			final byte[] contentData, final String contentType)
			throws IOException {
		logger.log("sending");
		final Session session = pa == null ? Session
				.getInstance(emailProperties) : Session.getInstance(
				emailProperties, new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return pa;
					}
				});
		session.setDebug(true);
		if (multiple) {
			final StringBuilder status = new StringBuilder();
			String sep = "";
			status.append("[");
			for (final String recipient : to) {
				final SendResult result = send(session, from,
						new String[] { recipient }, cc, bcc, subject, body,
						fileName, contentData, contentType);
				status.append(sep);
				sep = ", ";
				status.append(result.getJSON(recipient));
			}
			status.append("]");
			logger.log(status.toString());
		} else {
			final SendResult result = send(session, from, to, cc, bcc, subject,
					body, fileName, contentData, contentType);
			logger.log(result.getJSON(null));
		}
	}

	private SendResult send(final Session session, final String from,
			final String[] to, final String[] cc, final String[] bcc,
			final String subject, final String body, final String fileName,
			final byte[] contentData, final String contentType) {
		final MimeMessage mimeMessage = new MimeMessage(session);
		boolean success = false;
		String message = null;
		try {
			// The current context class loader doesn't include this jar for
			// some reason
			// according to threads in sun forums this is a bug in java 1.6
			// http://forums.sun.com/thread.jspa?threadID=5174556&start=0&tstart=0
			/*
			 * { final Thread thread = Thread.currentThread(); final ClassLoader
			 * threadClassLoader = thread.getContextClassLoader(); final
			 * ClassLoader thisClassLoader = this.getClass().getClassLoader();
			 * thread.setContextClassLoader(thisClassLoader);
			 * System.out.println(threadClassLoader);
			 * System.out.println(thisClassLoader); }
			 */
			mimeMessage.setFrom(new InternetAddress(from));
			for (final String address : to)
				mimeMessage.addRecipient(Message.RecipientType.TO,
						new InternetAddress(address));
			for (final String address : cc)
				mimeMessage.addRecipient(Message.RecipientType.CC,
						new InternetAddress(address));
			for (final String address : bcc)
				mimeMessage.addRecipient(Message.RecipientType.BCC,
						new InternetAddress(address));
			if (subject != null)
				mimeMessage.setSubject(subject);
			mimeMessage.setSentDate(new Date());

			final MimeMultipart multipart = new MimeMultipart("related");
			{
				final MimeBodyPart bodypart = new MimeBodyPart();
				final byte[] bytes = body.getBytes();
				final ByteArrayDataSource bads = new ByteArrayDataSource(bytes,
						"text/plain");
				final DataHandler dh = new DataHandler(bads);
				bodypart.setDataHandler(dh);
				bodypart.setDisposition("inline");
				multipart.addBodyPart(bodypart);
			}
			{
				final MimeBodyPart bodypart = new MimeBodyPart();
				final byte[] bytes = contentData;
				final ByteArrayDataSource bads = new ByteArrayDataSource(bytes,
						contentType);
				final DataHandler dh = new DataHandler(bads);
				bodypart.setDataHandler(dh);
				bodypart.setDisposition("attachment");
				bodypart.setFileName(fileName);
				multipart.addBodyPart(bodypart);
			}

			mimeMessage.setContent(multipart);
			Transport.send(mimeMessage);
			success = true;
			message = null;
		} catch (final MessagingException e) {
			e.printStackTrace();
			logger.log("Send failed", e);
			message = e.toString();
		}
		return new SendResult(success, message);
	}
}
