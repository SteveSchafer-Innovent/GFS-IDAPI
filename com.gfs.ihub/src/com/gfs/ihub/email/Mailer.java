package com.gfs.ihub.email;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import com.actuate.schemas.ArrayOfJobProperties;
import com.actuate.schemas.ArrayOfJobScheduleCondition;
import com.actuate.schemas.ArrayOfParameterValue;
import com.actuate.schemas.Attachment;
import com.actuate.schemas.DownloadFile;
import com.actuate.schemas.DownloadFileResponse;
import com.actuate.schemas.File;
import com.actuate.schemas.GetContent;
import com.actuate.schemas.GetContentResponse;
import com.actuate.schemas.GetFileDetails;
import com.actuate.schemas.GetFileDetailsResponse;
import com.actuate.schemas.GetJobDetails;
import com.actuate.schemas.GetJobDetailsResponse;
import com.actuate.schemas.JobProperties;
import com.actuate.schemas.JobScheduleCondition;
import com.actuate.schemas.JobScheduleField;
import com.actuate.schemas.JobScheduleSearch;
import com.actuate.schemas.ObjectIdentifier;
import com.actuate.schemas.ParameterValue;
import com.actuate.schemas.SelectJobSchedules;
import com.actuate.schemas.SelectJobSchedulesResponse;
import com.innoventsolutions.consts.ResultDefConsts;
import com.innoventsolutions.idapihelper.IdapiHelper;
import com.innoventsolutions.idapihelper.IdapiHelperException;
import com.innoventsolutions.idapihelper.IdapiHelperImpl;

public class Mailer {
	private final IdapiHelper helper;

	public Mailer(final String urlString, final String volume,
			final String username, final String password)
			throws MalformedURLException, IdapiHelperException {
		final URL serverURL = new URL(urlString);
		helper = IdapiHelperImpl.getInstance(new URL[] { serverURL });
		helper.login(volume, username, password, new byte[0], false);
	}

	public void processJobs() throws RemoteException {
		final JobScheduleCondition[] jscArray = { new JobScheduleCondition(),
				new JobScheduleCondition() };
		JobScheduleCondition jsc = jscArray[0];
		jsc.setField(JobScheduleField.JobName);
		jsc.setMatch("*");
		jsc = jscArray[1];
		jsc.setField(JobScheduleField.State);
		jsc.setMatch("Scheduled");
		ArrayOfJobScheduleCondition aoJSC = new ArrayOfJobScheduleCondition();
		aoJSC.setJobScheduleCondition(jscArray);

		JobScheduleSearch jobScheduleSearch = new JobScheduleSearch();
		jobScheduleSearch.setFetchDirection(Boolean.FALSE);
		jobScheduleSearch.setFetchSize(new Integer(1000));
		jobScheduleSearch.setConditionArray(aoJSC);

		SelectJobSchedules selectJobSchedules = new SelectJobSchedules();
		selectJobSchedules.setSearch(jobScheduleSearch);
		selectJobSchedules.setResultDef(ResultDefConsts.getJobResultDef());

		SelectJobSchedulesResponse response = helper
				.selectJobSchedules(selectJobSchedules);

		ArrayOfJobProperties aoJobProps = response.getJobs();
		JobProperties[] jobPropsArray = aoJobProps.getJobProperties();
		for (int i = 0; i < jobPropsArray.length; i++) {
			JobProperties jobProps = jobPropsArray[i];
			processJob(jobProps.getJobId());
		}
	}

	public void processJob(final String jobId) throws RemoteException {
		GetJobDetails getJobDetails = new GetJobDetails();
		getJobDetails.setResultDef(ResultDefConsts.getJobDetailsResultDef());
		getJobDetails.setJobId(jobId);
		GetJobDetailsResponse getJobDetailsResponse = new GetJobDetailsResponse();
		getJobDetailsResponse = helper.getJobDetails(getJobDetails);
		JobProperties jobDetails = getJobDetailsResponse.getJobAttributes();
		if (jobDetails == null) {
			return;
		}
		// String inputFileName = jobProps.getInputFileName();
		// String jobName = jobProps.getJobName();
		String emailFrom = null;
		String emailTo = null;
		String emailSubject = null;
		String emailBody = null;
		ArrayOfParameterValue aoPV = getJobDetailsResponse
				.getReportParameters();
		ParameterValue[] pvArray = aoPV.getParameterValue();
		for (int i = 0; i < pvArray.length; i++) {
			ParameterValue pv = pvArray[i];
			String name = pv.getName();
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
		if (emailTo != null) {
			// String outputName = jobProps.getRequestedOutputFileName();
			// FileAccess accessType = response.getOutputFileAccessType();
			final String outputFileId = jobDetails.getActualOutputFileId();
			// String outputFileName = jobProps.getActualOutputFileName();
			GetFileDetails getFileDetails = new GetFileDetails();
			getFileDetails.setFileId(outputFileId);
			GetFileDetailsResponse getFileDetailsResponse = helper
					.getFileDetails(getFileDetails);
			File file = getFileDetailsResponse.getFile();
			ObjectIdentifier objectIdentifier = new ObjectIdentifier();
			objectIdentifier.setId(outputFileId);
			GetContent getContent = new GetContent();
			getContent.setObject(objectIdentifier);
			GetContentResponse getContentResponse = helper
					.getContent(getContent);
			DownloadFile downloadFile = new DownloadFile();
			downloadFile.setFileId(outputFileId);
			DownloadFileResponse downloadFileResponse = helper
					.downloadFile(downloadFile);
			Attachment contentRef = getContentResponse.getContentRef();
			byte[] contentData = contentRef.getContentData();
			sendMail(emailFrom, emailTo, emailSubject, emailBody);
		}
	}

	private void sendMail(String emailFrom, String emailTo,
			String emailSubject, String emailBody) {
		// TODO Auto-generated method stub

	}
}
