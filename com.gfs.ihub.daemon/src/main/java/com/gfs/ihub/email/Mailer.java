package com.gfs.ihub.email;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.actuate.schemas.ArrayOfJobCondition;
import com.actuate.schemas.ArrayOfJobProperties;
import com.actuate.schemas.ArrayOfParameterValue;
import com.actuate.schemas.Attachment;
import com.actuate.schemas.DownloadFile;
import com.actuate.schemas.DownloadFileResponse;
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

	public static abstract class PropertiesBasedOptions {
		final String dirName;
		final String altDirName;
		final String fileName;
		final boolean propertiesFileExists;
		final Properties properties = new Properties();

		protected PropertiesBasedOptions(final String dirName,
				final String altDirName, final String fileName)
				throws IOException {
			this.dirName = dirName;
			this.altDirName = altDirName;
			this.fileName = fileName;
			boolean fileExists = true;
			java.io.File dir = new java.io.File(dirName);
			if (!dir.exists()) {
				dir = new java.io.File(altDirName);
				if (!dir.exists()) {
					fileExists = false;
				}
			}
			if (fileExists) {
				final java.io.File file = new java.io.File(dir, fileName);
				fileExists = file.exists();
				if (fileExists) {
					final FileInputStream fis = new FileInputStream(file);
					try {
						properties.load(fis);
					} finally {
						fis.close();
					}
				}
			}
			this.propertiesFileExists = fileExists;
		}

		abstract void setProperties();

		final void store() throws IOException {
			setProperties();
			java.io.File dir = new java.io.File(dirName);
			if (!dir.exists()) {
				dir = new java.io.File(altDirName);
				if (!dir.exists()) {
					throw new RuntimeException(
							"Can't create the properties file");
				}
			}
			final java.io.File file = new java.io.File(dir, fileName);
			final FileOutputStream fos = new FileOutputStream(file);
			try {
				properties.store(fos, "");
			} finally {
				fos.close();
			}
		}
	}

	public static class ActuateOptions extends PropertiesBasedOptions {
		final String urlString;
		final String volume;
		final String username;
		final String password;

		public ActuateOptions(final String dirName, final String altDirName,
				final String fileName, final String urlString,
				final String volume, final String username,
				final String password) throws IOException {
			super(dirName, altDirName, fileName);
			this.urlString = properties.getProperty("url", urlString);
			this.volume = properties.getProperty("volume", volume);
			this.username = properties.getProperty("username", username);
			this.password = properties.getProperty("password", password);
		}

		@Override
		void setProperties() {
			properties.setProperty("url", urlString);
			properties.setProperty("volume", volume);
			properties.setProperty("username", username);
			properties.setProperty("password", password);
		}
	}

	public static class SmtpOptions extends PropertiesBasedOptions {
		final String host;
		final int port;
		final String username;
		final String password;
		final boolean enableSSL;
		final boolean enableSTARTTLS;
		final boolean auth;
		final String defaultFrom;

		public SmtpOptions(final String dirName, final String altDirName,
				final String fileName, final String host, final int port,
				final String username, final String password,
				final boolean enableSSL, final boolean enableSTARTTLS,
				final boolean auth, final String defaultFrom)
				throws IOException {
			super(dirName, altDirName, fileName);
			this.host = properties.getProperty("mail.smtp.host", host);
			this.port = Integer.parseInt(properties.getProperty(
					"mail.smtp.port", String.valueOf(port)));
			this.username = properties.getProperty("username", username);
			this.password = properties.getProperty("password", password);
			this.enableSSL = "true".equalsIgnoreCase(properties.getProperty(
					"mail.smtp.ssl.enable", String.valueOf(enableSSL)));
			this.enableSTARTTLS = "true".equalsIgnoreCase(properties
					.getProperty("mail.smtp.starttls.enable",
							String.valueOf(enableSTARTTLS)));
			this.auth = "true".equalsIgnoreCase(properties.getProperty(
					"mail.smtp.auth", String.valueOf(auth)));
			this.defaultFrom = properties.getProperty("mail.user", defaultFrom);
		}

		@Override
		void setProperties() {
			properties.setProperty("mail.smtp.host", host);
			properties.setProperty("mail.smtp.port", String.valueOf(port));
			if (username != null)
				properties.setProperty("username", username);
			else
				properties.remove("username");
			if (password != null)
				properties.setProperty("password", password);
			else
				properties.remove("password");
			properties.setProperty("mail.smtp.ssl.enable",
					String.valueOf(enableSSL));
			properties.setProperty("mail.smtp.starttls.enable",
					String.valueOf(enableSTARTTLS));
			properties.setProperty("mail.smtp.auth", String.valueOf(auth));
			properties.setProperty("mail.user", defaultFrom);
		}
	}

	public static class SqlOptions extends PropertiesBasedOptions {
		final String urlString;
		final String username;
		final String password;

		public SqlOptions(final String dirName, final String altDirName,
				final String fileName, final String urlString,
				final String username, final String password)
				throws IOException {
			super(dirName, altDirName, fileName);
			this.urlString = properties.getProperty("url", urlString);
			this.username = properties.getProperty("username", username);
			this.password = properties.getProperty("password", password);
		}

		@Override
		void setProperties() {
			properties.setProperty("url", urlString);
			properties.setProperty("username", username);
			properties.setProperty("password", password);
		}
	}

	public static class FileOptions extends PropertiesBasedOptions {
		final String storeDirName;
		final String altStoreDirName;

		public FileOptions(final String dirName, final String altDirName,
				final String fileName, final String storeDirName,
				final String altStoreDirName) throws IOException {
			super(dirName, altDirName, fileName);
			this.storeDirName = properties.getProperty("storeDirName",
					storeDirName);
			this.altStoreDirName = properties.getProperty("altStoreDirName",
					altStoreDirName);
		}

		@Override
		void setProperties() {
			properties.setProperty("storeDirName", storeDirName);
			properties.setProperty("altStoreDirName", altStoreDirName);
		}
	}

	public Mailer(final ActuateOptions actuateOptions,
			final SmtpOptions smtpOptions, final SqlOptions sqlOptions,
			final FileOptions fileOptions) throws IdapiHelperException,
			IOException, SQLException {
		final URL serverURL = new URL(actuateOptions.urlString);
		helper = IdapiHelperImpl.getInstance(new URL[] { serverURL });
		helper.login(actuateOptions.volume, actuateOptions.username,
				actuateOptions.password, new byte[0], false);
		this.defaultFrom = smtpOptions.defaultFrom;
		smtpOptions.store();
		final Logger logger = new Logger();
		this.email = new Email(smtpOptions.properties, smtpOptions.username,
				smtpOptions.password, logger);
		this.logger = logger;
		final Connection newConnection = DriverManager.getConnection(
				sqlOptions.urlString, sqlOptions.username, sqlOptions.password);
		int sqlGrammar = ORACLE;
		if (sqlOptions.urlString.startsWith("jdbc:postgresql"))
			sqlGrammar = POSTGRESQL;
		this.sqlGrammar = sqlGrammar;
		// take the connection out of transaction mode so readOnly can be set
		newConnection.setAutoCommit(true);
		newConnection.setReadOnly(false);
		newConnection.setAutoCommit(false);
		connection = newConnection;
		java.io.File storeDir = new java.io.File(fileOptions.storeDirName);
		if (!storeDir.exists()) {
			storeDir = new java.io.File(fileOptions.altStoreDirName);
		}
		storeDir.mkdirs();
		this.storeDir = storeDir;
	}

	public void close() throws SQLException {
		connection.close();
	}

	public void processJobs() throws IOException, SQLException {
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
			SQLException {

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

		// get content bytes using DownloadFile
		final DownloadFile downloadFile = new DownloadFile();
		downloadFile.setFileId(outputFileId);
		downloadFile.setDownloadEmbedded(Boolean.TRUE); // needed to get bytes
		final DownloadFileResponse downloadFileResponse = helper
				.downloadFile(downloadFile);
		final Attachment attachment = downloadFileResponse.getContent();
		final byte[] contentData = attachment.getContentData();
		// final String contentType = attachment.getContentType(); // always
		// application/octet-stream

		saveFile(contentData, jobId, outputFileType);

		final int mimeTypePk = addMimeTypeToDB(contentType);

		final String from = emailFrom == null ? defaultFrom : emailFrom;
		final String[] to = emailTo.split(",[ ]*");
		final String[] cc = new String[0];
		final String[] bcc = new String[0];
		final String subject = emailSubject == null ? "BIRT report"
				: emailSubject;
		final String body = emailBody == null ? "BIRT report" : emailBody;

		email.sendMail(from, to, cc, bcc, subject, body, false, fileName,
				contentData, contentType);

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

	private void saveFile(final byte[] contentData, final String jobId,
			final String outputFileType) throws IOException {
		final String fileName = jobId + "." + outputFileType;
		final java.io.File file = new java.io.File(this.storeDir, fileName);
		final FileOutputStream fos = new FileOutputStream(file);
		try {
			fos.write(contentData);
		} finally {
			fos.close();
		}
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
