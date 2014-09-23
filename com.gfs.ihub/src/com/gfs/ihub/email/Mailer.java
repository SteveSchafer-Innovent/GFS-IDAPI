package com.gfs.ihub.email;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

public class Mailer {
	private final IdapiHelper helper;
	private final Email email;
	private final Connection connection;

	public static class ActuateOptions {
		final String urlString;
		final String volume;
		final String username;
		final String password;

		public ActuateOptions(final String urlString, final String volume,
				final String username, final String password) {
			this.urlString = urlString;
			this.volume = volume;
			this.username = username;
			this.password = password;
		}
	}

	public static class SmtpOptions {
		final String host;
		final int port;
		final String username;
		final String password;

		public SmtpOptions(final String host, final int port,
				final String username, final String password) {
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
		}
	}

	public static class SqlOptions {
		final String urlString;
		final String username;
		final String password;

		public SqlOptions(final String urlString, final String username,
				final String password) {
			this.urlString = urlString;
			this.username = username;
			this.password = password;
		}
	}

	public Mailer(final ActuateOptions actuateOptions,
			final SmtpOptions smtpOptions, final SqlOptions sqlOptions)
			throws IdapiHelperException, IOException, SQLException {
		final URL serverURL = new URL(actuateOptions.urlString);
		helper = IdapiHelperImpl.getInstance(new URL[] { serverURL });
		helper.login(actuateOptions.volume, actuateOptions.username,
				actuateOptions.password, new byte[0], false);
		final Properties emailProperties = new Properties();
		String emailPropertiesFileName = System
				.getProperty("emailPropertiesFileName");
		if (emailPropertiesFileName == null)
			emailPropertiesFileName = "D:/Actuate3/BIRTiHubVisualization/modules/BIRTiHub/iHub/data/server/log/email.properties";
		final java.io.File propertiesFile = new java.io.File(
				emailPropertiesFileName);
		final String username;
		final String password;
		final String logFileName;
		final String defaultLogFileName = "D:/Actuate3/BIRTiHubVisualization/modules/BIRTiHub/iHub/data/server/log/email.log";
		String defaultFrom = "noreply@gfs.com";
		if (propertiesFile.exists()) {
			final FileInputStream fis = new FileInputStream(propertiesFile);
			try {
				emailProperties.load(fis);
			} finally {
				fis.close();
			}
			username = emailProperties.getProperty("username", defaultFrom);
			password = emailProperties.getProperty("password", "24Proxy61!");
			defaultFrom = emailProperties.getProperty("from", defaultFrom);
			logFileName = emailProperties
					.getProperty("log", defaultLogFileName);
		} else {
			emailProperties.setProperty("mail.smtp.host", "mail.gfs.com");
			emailProperties.setProperty("mail.smtp.port", "2525");
			emailProperties.setProperty("mail.user", defaultFrom);
			emailProperties.setProperty("mail.smtp.ssl.enable", "false");
			emailProperties.setProperty("mail.smtp.starttls.enable", "false");
			emailProperties.setProperty("mail.smtp.auth", "true");
			username = defaultFrom;
			password = "24Proxy61!";
			logFileName = defaultLogFileName;
		}
		this.email = new Email(emailProperties, username, password,
				logFileName, defaultFrom);
		final Connection newConnection = DriverManager.getConnection(
				sqlOptions.urlString, sqlOptions.username, sqlOptions.password);
		// take the connection out of transaction mode so readOnly can be set
		newConnection.setAutoCommit(true);
		newConnection.setReadOnly(false);
		newConnection.setAutoCommit(false);
		this.connection = newConnection;
	}

	public void processJobs() throws IOException {
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
		for (int i = 0; i < jobPropsArray.length; i++) {
			final JobProperties jobProps = jobPropsArray[i];
			processJob(jobProps.getJobId());
		}
	}

	private void processJob(final String jobId) throws IOException {
		final GetJobDetails getJobDetails = new GetJobDetails();
		getJobDetails.setResultDef(ResultDefConsts.getJobDetailsResultDef());
		getJobDetails.setJobId(jobId);
		GetJobDetailsResponse getJobDetailsResponse = new GetJobDetailsResponse();
		getJobDetailsResponse = helper.getJobDetails(getJobDetails);
		final JobProperties jobDetails = getJobDetailsResponse
				.getJobAttributes();
		if (jobDetails == null)
			return;

		final String outputFileId = jobDetails.getActualOutputFileId();
		if (outputFileId == null)
			return;

		final String outputFileName = jobDetails.getActualOutputFileName();
		if (outputFileName == null)
			return;
		// /Home/el1zt/Parameter Value File/NJ.PDF;1
		final int indexOfSemiColon = outputFileName.lastIndexOf(';');
		final String rootOutputFileName = indexOfSemiColon < 0 ? outputFileName
				: outputFileName.substring(0, indexOfSemiColon);
		final int indexOfDot = rootOutputFileName.lastIndexOf('.');
		if (indexOfDot < 0)
			return;
		final String outputFileType = rootOutputFileName
				.substring(indexOfDot + 1);
		if ("html".equalsIgnoreCase(outputFileType)
				|| "rptdocument".equalsIgnoreCase(outputFileType)
				|| "data".equalsIgnoreCase(outputFileType))
			return;

		/*
		 * String contentType; if ("pdf".equalsIgnoreCase(outputFileType)) {
		 * contentType = "application/pdf"; } else if
		 * ("xls".equalsIgnoreCase(outputFileType)) { contentType =
		 * "application/vnd.ms-excel"; } else if
		 * ("xlsx".equalsIgnoreCase(outputFileType)) { contentType =
		 * "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		 * } else { contentType = "application/octet-stream"; }
		 */

		String emailFrom = null;
		String emailTo = null;
		String emailSubject = null;
		String emailBody = null;
		final ArrayOfParameterValue aoPV = getJobDetailsResponse
				.getReportParameters();
		if (aoPV == null)
			return;
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
			return;

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
		downloadFile.setDownloadEmbedded(Boolean.TRUE);
		final DownloadFileResponse downloadFileResponse = helper
				.downloadFile(downloadFile);
		final Attachment attachment = downloadFileResponse.getContent();
		// final ArrayOfAttachment aoAttachment = downloadFileResponse
		// .getContainedFiles(); // is null
		// final File attachmentFile = downloadFileResponse.getFile();
		final byte[] contentData = attachment.getContentData(); // TODO is null
		final String contentType = attachment.getContentType();

		email.sendMail(emailFrom, emailTo, null, null, emailSubject, emailBody,
				false, fileName, contentData, contentType);
	}
}
