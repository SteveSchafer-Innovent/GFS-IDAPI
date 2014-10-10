package com.gfs.ihub.email;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

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
import com.innoventsolutions.consts.ResultDefConsts;
import com.innoventsolutions.idapihelper.IdapiHelper;
import com.innoventsolutions.idapihelper.IdapiHelperException;
import com.innoventsolutions.idapihelper.IdapiHelperImpl;

public class ActuateInterfaceImpl implements ActuateInterface {
	private final ActuateOptions options;
	private final IdapiHelper helper;
	private final Logger logger;

	public ActuateInterfaceImpl(final ActuateOptions options,
			final Logger logger) throws MalformedURLException,
			IdapiHelperException {
		this.options = options;
		this.logger = logger;
		final URL serverURL = new URL(options.getUrlString());
		helper = IdapiHelperImpl.getInstance(new URL[] { serverURL });
		helper.login(options.getVolume(), options.getUsername(),
				options.getPassword(), new byte[0], false);
		logger.log("Successfully logged in to Actuate");
	}

	public void processJobs(final JobProcessor processor) throws IOException {
		logger.log("processing jobs");

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
			final String jobIdString = jobProps.getJobId();
			final long jobId = Long.parseLong(jobIdString);
			if (processor.jobHasBeenProcessed(jobId))
				continue;
			final boolean sent = processJob(jobProps.getJobId(), processor);
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

	private boolean processJob(final String jobId, final JobProcessor processor)
			throws IOException {
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
		final java.io.File outputDir = options.getStoreDir();
		outputDir.mkdirs();
		final java.io.File outputFile = new java.io.File(outputDir,
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

		processor.processJob(contentType, emailFrom, emailTo, emailSubject,
				emailBody, fileName, outputFile, Long.parseLong(jobId));
		return true;
	}

	private boolean saveToStream(final InputStream inStream,
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
}
