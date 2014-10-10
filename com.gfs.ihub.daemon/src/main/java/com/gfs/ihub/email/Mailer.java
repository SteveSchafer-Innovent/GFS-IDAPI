package com.gfs.ihub.email;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.attachments.AttachmentPart;

import com.actuate.schemas.ArrayOfJobCondition;
import com.actuate.schemas.ArrayOfJobProperties;
import com.actuate.schemas.ArrayOfParameterValue;
import com.actuate.schemas.DownloadFile;
import com.actuate.schemas.File;
import com.actuate.schemas.GetFileDetails;
import com.actuate.schemas.GetFileDetailsResponse;
import com.actuate.schemas.GetJobDetails;
import com.actuate.schemas.GetJobDetailsResponse;
import com.actuate.schemas.JobCondition;
import com.actuate.schemas.JobField;
import com.actuate.schemas.JobProperties;
import com.actuate.schemas.JobSearch;
import com.actuate.schemas.ParameterValue;
import com.actuate.schemas.SelectJobs;
import com.actuate.schemas.SelectJobsResponse;
import com.gfs.ihub.options.ActuateOptions;
import com.gfs.ihub.options.FileOptions;
import com.gfs.ihub.options.SmtpOptions;
import com.gfs.ihub.options.SqlOptions;
import com.innoventsolutions.consts.ResultDefConsts;
import com.innoventsolutions.idapihelper.IdapiHelper;
import com.innoventsolutions.idapihelper.IdapiHelperException;
import com.innoventsolutions.idapihelper.IdapiHelperImpl;

public class Mailer implements AutoCloseable {
	public static final int ORACLE = 0;
	public static final int POSTGRESQL = 1;
	private final IdapiHelper helper;
	private final Email email;
	private Connection connection = null;
	private final int sqlGrammar;
	private final String defaultFrom;
	private final java.io.File storeDir;
	private final Logger logger;

	public Mailer(final ActuateOptions actuateOptions,
			final SmtpOptions smtpOptions, final SqlOptions sqlOptions,
			final FileOptions fileOptions) throws IdapiHelperException,
			IOException, SQLException {

		// Setup Actuate
		final URL serverURL = new URL(actuateOptions.getUrlString());
		helper = IdapiHelperImpl.getInstance(new URL[] { serverURL });
		helper.login(actuateOptions.getVolume(), actuateOptions.getUsername(),
				actuateOptions.getPassword(), new byte[0], false);

		// Setup SMTP Server
		this.defaultFrom = smtpOptions.getDefaultFrom();
		// smtpOptions.store();
		final Logger logger = new Logger();
		this.email = new Email(smtpOptions.getProperties(),
				smtpOptions.getUsername(), smtpOptions.getPassword(), logger);
		this.logger = logger;

		// Setup JDBC connection
		final Connection newConnection = DriverManager.getConnection(
				sqlOptions.getUrlString(), sqlOptions.getUsername(),
				sqlOptions.getPassword());
		int sqlGrammar = ORACLE;
		if (sqlOptions.getUrlString().startsWith("jdbc:postgresql"))
			sqlGrammar = POSTGRESQL;
		this.sqlGrammar = sqlGrammar;
		// take the connection out of transaction mode so readOnly can be set
		newConnection.setAutoCommit(true);
		newConnection.setReadOnly(false);
		newConnection.setAutoCommit(false);
		connection = newConnection;

		// Setup the location to store download files
		final java.io.File storeDir = new java.io.File(
				fileOptions.getStoreDirName());
		storeDir.mkdirs();
		this.storeDir = storeDir;
	}

	public void close() throws SQLException {
		connection.close();
	}

	public void processJobs() throws IOException, SQLException,
			MessagingException {
		logger.log("Processing jobs");

		final JobCondition jc1 = new JobCondition();
		jc1.setField(JobField.State);
		jc1.setMatch("Succeeded");

		final JobCondition[] jcArray = { jc1 };

		final ArrayOfJobCondition aoJC = new ArrayOfJobCondition();
		aoJC.setJobCondition(jcArray);

		final JobSearch jobSearch = new JobSearch();
		jobSearch.setFetchDirection(Boolean.FALSE);
		jobSearch.setFetchSize(Integer.valueOf(1000));
		jobSearch.setConditionArray(aoJC);

		final SelectJobs selectJobs = new SelectJobs();
		selectJobs.setSearch(jobSearch);
		selectJobs.setResultDef(ResultDefConsts.getJobResultDef());
		final SelectJobsResponse selectJobsResponse = helper
				.selectJobs(selectJobs);

		final ArrayOfJobProperties aoJobProps = selectJobsResponse.getJobs();
		final JobProperties[] jobPropsArray = aoJobProps.getJobProperties();
		final int jobCount = jobPropsArray.length;
		int sentCount = 0;
		for (int i = 0; i < jobCount; i++) {
			final JobProperties jobProps = jobPropsArray[i];
			final boolean sent = processJob(jobProps.getJobId());
			if (sent)
				sentCount++;
		}

		logger.log(jobCount + " job" + (jobCount == 1 ? "" : "s") + " examined");
		logger.log(sentCount + " email" + (sentCount == 1 ? "" : "s") + " sent");
	}

	private static final Map<String, String> CONTENT_TYPES;
	static {
		final Map<String, String> map = new HashMap<String, String>();
		map.put("ps", "application/postscript");
		map.put("pdf", "application/pdf");
		map.put("xls", "application/vnd.ms-excel");
		map.put("xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		map.put("ppt", "application/vnd.ms-powerpoint");
		map.put("pptx",
				"application/vnd.openxmlformats-officedocument.presentationml.presentation");
		map.put("doc", "application/msword");
		map.put("docx",
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		CONTENT_TYPES = map;
	}

	private boolean processJob(final String jobId) throws IOException,
			SQLException, MessagingException {

		if (notificationExistsInDB(Long.parseLong(jobId)))
			return false;

		final GetJobDetails getJobDetails = new GetJobDetails();
		getJobDetails.setResultDef(ResultDefConsts.getJobDetailsResultDef());
		getJobDetails.setJobId(jobId);
		GetJobDetailsResponse getJobDetailsResponse = new GetJobDetailsResponse();
		getJobDetailsResponse = helper.getJobDetails(getJobDetails);
		final JobProperties jobDetails = getJobDetailsResponse
				.getJobAttributes();
		if (jobDetails == null)
			return false;

		final String outputFileId = jobDetails.getActualOutputFileId();
		if (outputFileId == null)
			return false;

		final String outputFileName = jobDetails.getActualOutputFileName();
		if (outputFileName == null)
			return false;
		// /Home/el1zt/Parameter Value File/NJ.PDF;1
		final int indexOfSemiColon = outputFileName.lastIndexOf(';');
		final String rootOutputFileName = indexOfSemiColon < 0 ? outputFileName
				: outputFileName.substring(0, indexOfSemiColon);
		final int indexOfDot = rootOutputFileName.lastIndexOf('.');
		if (indexOfDot < 0)
			return false;
		final String outputFileType = rootOutputFileName.substring(
				indexOfDot + 1).toLowerCase();

		final String contentType = CONTENT_TYPES.get(outputFileType);
		if (contentType == null)
			return false;

		String emailFrom = null;
		String emailTo = null;
		String emailSubject = null;
		String emailBody = null;
		final ArrayOfParameterValue aoPV = getJobDetailsResponse
				.getReportParameters();
		if (aoPV == null)
			return false;
		final ParameterValue[] pvArray = aoPV.getParameterValue();
		for (int i = 0; i < pvArray.length; i++) {
			final ParameterValue pv = pvArray[i];
			final String name = pv.getName();
			if (name.startsWith("$$$"))
				continue;
			if ("pEMAIL_FROM".equals(name)) {
				emailFrom = pv.getValue();
			} else if ("pEMAIL_TO".equals(name)) {
				emailTo = pv.getValue();
			} else if ("pEMAIL_SUBJECT".equals(name)) {
				emailSubject = pv.getValue();
			} else if ("pEMAIL_BODY".equals(name)) {
				emailBody = pv.getValue();
			}
		}
		if (emailTo == null || emailTo.trim().length() == 0)
			return false;

		final GetFileDetails getFileDetails = new GetFileDetails();
		getFileDetails.setFileId(outputFileId);
		final GetFileDetailsResponse getFileDetailsResponse = helper
				.getFileDetails(getFileDetails);
		final File file = getFileDetailsResponse.getFile();
		final String fileName = file.getName();

		/*
		 * // get content bytes using GetContent final ObjectIdentifier
		 * objectIdentifier = new ObjectIdentifier();
		 * objectIdentifier.setId(outputFileId); final GetContent getContent =
		 * new GetContent(); getContent.setObject(objectIdentifier); final
		 * GetContentResponse getContentResponse = helper
		 * .getContent(getContent); final Attachment contentRef =
		 * getContentResponse.getContentRef(); // String connectionHandle = //
		 * getContentResponse.getConnectionHandle();
		 */

		// final String contentType = attachment.getContentType(); // always
		// application/octet-stream

		// get content bytes using DownloadFile
		final DownloadFile downloadFile = new DownloadFile();
		downloadFile.setFileId(outputFileId);
		downloadFile.setDecomposeCompoundDocument(new Boolean(false));
		downloadFile.setDownloadEmbedded(new Boolean(false));

		final String localFileName = jobId + "." + outputFileType;
		final java.io.File outputFile = new java.io.File(this.storeDir,
				localFileName);
		try {
			helper.downloadFile(downloadFile);
			BufferedOutputStream outStream = null;
			try {
				outStream = new BufferedOutputStream(new FileOutputStream(
						outputFile));
				final Object[] attachments = helper.getAttachments();
				for (int i = 0; i < attachments.length; i++) {
					final AttachmentPart attachmentPart = (AttachmentPart) attachments[i];
					if (attachmentPart == null)
						continue;
					final InputStream inStream = attachmentPart
							.getDataHandler().getInputStream();
					saveToStream(inStream, outStream);
				}
			} finally {
				helper.clearAttachments();
				if (outStream != null) {
					outStream.close();
				}
			}
		} catch (final SOAPException e) {
			throw AxisFault.makeFault(e);
		} catch (final RemoteException e) {
			throw e;
		} catch (final IOException e) {
			throw e;
		}

		final int mimeTypePk = addMimeTypeToDB(contentType);

		final String from = emailFrom == null ? defaultFrom : emailFrom;
		final String[] to = emailTo.split(",[ ]*");
		final String[] cc = new String[0];
		final String[] bcc = new String[0];
		final String subject = emailSubject == null ? "BIRT report"
				: emailSubject;
		final String body = emailBody == null ? "BIRT report" : emailBody;

		email.sendMail(from, to, cc, bcc, subject, body, false, fileName,
				outputFile, contentType);

		final int senderPk = addEmailAddressToDB(from);

		final int notificationId = addNotificationToDB(Long.parseLong(jobId),
				senderPk, mimeTypePk, subject, body, fileName);

		for (int i = 0; i < to.length; i++) {
			final String address = to[i];
			final int addressId = addEmailAddressToDB(address);
			addRecipientToDB(addressId, notificationId);
		}

		return true;
	}

	public boolean saveToStream(final InputStream inStream,
			final OutputStream out) throws IOException {
		boolean writeStatus = false;
		try {
			final byte[] buf = new byte[1024];
			int len = 0;
			while ((len = inStream.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			inStream.close();
			writeStatus = true;
		} catch (final IOException e) {
			System.out.println("Excepton while downloading file ");
			e.printStackTrace();
			throw e;
		}
		return writeStatus;
	}

	private int addEmailAddressToDB(final String address) throws SQLException {
		{
			final PreparedStatement stmt = connection
					.prepareStatement("select email_address_sk from email_account where email_address = ?");
			try {
				stmt.setString(1, address);
				final ResultSet rs = stmt.executeQuery();
				try {
					while (rs.next()) {
						return rs.getInt(1);
					}
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
		}
		int newId = 1;
		{
			final String sql;
			if (sqlGrammar == ORACLE)
				sql = "select actuate_notify_admin.email_account_seq.nextval from dual";
			else
				sql = "select nextval('email_account_seq')";
			final PreparedStatement stmt = connection.prepareStatement(sql);
			try {
				final ResultSet rs = stmt.executeQuery();
				try {
					if (rs.next()) {
						newId = rs.getInt(1);
					}
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
		}
		{
			final PreparedStatement stmt = connection
					.prepareStatement("insert into email_account "
							+ "(email_address_sk, email_address) values (?, ?)");
			try {
				stmt.setInt(1, newId);
				stmt.setString(2, address);
				stmt.execute();
			} finally {
				stmt.close();
			}
		}
		connection.commit();
		return newId;
	}

	int addMimeTypeToDB(final String formatType) throws SQLException {
		{
			final PreparedStatement stmt = connection
					.prepareStatement("select mime_type_sk from mime_type where mime_type_txt = ?");
			try {
				stmt.setString(1, formatType);
				final ResultSet rs = stmt.executeQuery();
				try {
					while (rs.next()) {
						return rs.getInt(1);
					}
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
		}
		int newId = 1;
		{
			final String sql;
			if (sqlGrammar == ORACLE)
				sql = "select actuate_notify_admin.mime_type_seq.nextval from dual";
			else
				sql = "select nextval('mime_type_seq')";
			final PreparedStatement stmt = connection.prepareStatement(sql);
			try {
				final ResultSet rs = stmt.executeQuery();
				try {
					if (rs.next()) {
						newId = rs.getInt(1);
					}
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
		}
		final PreparedStatement stmt3 = connection
				.prepareStatement("insert into mime_type "
						+ "(mime_type_sk, mime_type_txt) values (?, ?)");
		try {
			stmt3.setInt(1, newId);
			stmt3.setString(2, formatType);
			stmt3.execute();
		} finally {
			stmt3.close();
		}
		connection.commit();
		return newId;
	}

	boolean notificationExistsInDB(final long jobId) throws SQLException {
		final PreparedStatement stmt = connection
				.prepareStatement("select actuate_notification_sk from actuate_notification where job_iid = ?");
		try {
			stmt.setLong(1, jobId);
			final ResultSet rs = stmt.executeQuery();
			try {
				if (rs.next())
					return true;
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
		connection.commit();
		return false;
	}

	int addNotificationToDB(final long jobId, final int senderPk,
			final int mimeTypePk, final String subject, final String body,
			final String fileName) throws SQLException {
		int newId = 1;
		{
			final String sql;
			if (sqlGrammar == ORACLE)
				sql = "select actuate_notify_admin.actuate_notification_seq.nextval from dual";
			else
				sql = "select nextval('actuate_notification_seq')";
			final PreparedStatement stmt = connection.prepareStatement(sql);
			try {
				final ResultSet rs = stmt.executeQuery();
				try {
					if (rs.next()) {
						newId = rs.getInt(1);
					}
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
		}
		{
			final PreparedStatement stmt = connection
					.prepareStatement("insert into actuate_notification "
							+ "(actuate_notification_sk, job_iid, sender_email_address_sk, mime_type_sk, msg_sent_time, msg_subject_txt, msg_body_txt, rel_path_file_name) "
							+ "values (?, ?, ?, ?, ?, ?, ?, ?)");
			try {
				int i = 0;
				stmt.setInt(++i, newId);
				stmt.setLong(++i, jobId);
				stmt.setInt(++i, senderPk);
				stmt.setInt(++i, mimeTypePk);
				stmt.setDate(++i, new java.sql.Date(System.currentTimeMillis()));
				stmt.setString(++i, subject);
				stmt.setString(++i, body);
				stmt.setString(++i, fileName);
				stmt.execute();
			} finally {
				stmt.close();
			}
		}
		connection.commit();
		return newId;
	}

	void addRecipientToDB(final int addressId, final int notificationId)
			throws SQLException {
		final PreparedStatement stmt = connection
				.prepareStatement("insert into actuate_notify_recipient "
						+ "(rcpnt_email_address_sk, actuate_notification_sk) values (?, ?)");
		try {
			stmt.setInt(1, addressId);
			stmt.setInt(2, notificationId);
			stmt.execute();
		} finally {
			stmt.close();
		}
		connection.commit();
	}
}
