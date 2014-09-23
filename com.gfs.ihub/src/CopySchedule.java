import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.actuate.schemas.ArrayOfJobProperties;
import com.actuate.schemas.ArrayOfJobScheduleCondition;
import com.actuate.schemas.ArrayOfString;
import com.actuate.schemas.GetJobDetails;
import com.actuate.schemas.GetJobDetailsResponse;
import com.actuate.schemas.JobInputDetail;
import com.actuate.schemas.JobProperties;
import com.actuate.schemas.JobScheduleCondition;
import com.actuate.schemas.JobScheduleField;
import com.actuate.schemas.JobScheduleSearch;
import com.actuate.schemas.NewFile;
import com.actuate.schemas.RetryOptionType;
import com.actuate.schemas.RetryOptions;
import com.actuate.schemas.SelectJobSchedules;
import com.actuate.schemas.SelectJobSchedulesResponse;
import com.actuate.schemas.SubmitJob;
import com.actuate.schemas.VersioningOption;
import com.innoventsolutions.idapihelper.IdapiHelper;
import com.innoventsolutions.idapihelper.IdapiHelperException;
import com.innoventsolutions.idapihelper.IdapiHelperImpl;

/**
 * This is close, but not entirely right. This program takes everything from one
 * server and puts it in another server. The goal would be to take all this
 * information and stick it in an XML file using an XSD.
 * 
 * @author Scott Rosenbaum
 * 
 */
public class CopySchedule {
	public static void main(final String[] args) throws Exception {
		final long startTime = System.currentTimeMillis();
		final FileOutputStream fso = new FileOutputStream("./schedule.log");
		final PrintStream out = new PrintStream(fso);

		final String urlSrc = "http://aixtd8:8000";
		final String volSrc = "aixTD8";
		final String urlDst = "http://aixdmz8:8000";
		final String volDst = "aixdmz8";
		final String username = "int_administrator";
		final String password = "12345678";

		CopySchedule scheduleControl;
		try {
			scheduleControl = new CopySchedule(urlSrc, volSrc, urlDst, volDst,
					username, password, out);
		} catch (final MalformedURLException e) {
			out.println("Malformed server URL");
			System.exit(2);
			return;
		} catch (final IdapiHelperException e) {
			out.println("Unable to connect to server");
			System.exit(2);
			return;
		}
		scheduleControl.execute();
		final long endTime = System.currentTimeMillis();
		out.println("end of schedule");
		out.println(" totalSize=" + scheduleControl.getDeleteCount());
		out.print("Elapsed time (sec): ");
		out.println((endTime - startTime) / 1000.0);
		out.flush();
		out.close();
	}

	private final IdapiHelper srcHelper;

	private final IdapiHelper dstHelper;

	private final int count = 0;

	private final int deleteCount = 0;

	private final Calendar deleteDate = new GregorianCalendar();

	private final int totalSize = 0;

	private final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private final PrintStream out;

	public CopySchedule(final String srcSrvr, final String srcVol,
			final String dstSrvr, final String dstVol, final String username,
			final String password, final PrintStream out)
			throws MalformedURLException, IdapiHelperException {
		this.out = out;
		final URL srcServerURL = new URL(srcSrvr);
		srcHelper = IdapiHelperImpl.getInstance(new URL[] { srcServerURL });
		srcHelper.login(srcVol, username, password, new byte[0], false);

		final URL dstServerURL = new URL(dstSrvr);
		dstHelper = IdapiHelperImpl.getInstance(new URL[] { dstServerURL });
		dstHelper.login(dstVol, username, password, new byte[0], false);

		out.println("Date String " + deleteDate.getTime());
	}

	public void execute() throws RemoteException {
		final JobScheduleCondition[] aJobScheduleCondition = {
				new JobScheduleCondition(), new JobScheduleCondition() };
		aJobScheduleCondition[0].setField(JobScheduleField.JobName);
		aJobScheduleCondition[0].setMatch("*");
		aJobScheduleCondition[1].setField(JobScheduleField.State);
		aJobScheduleCondition[1].setMatch("Scheduled");
		final ArrayOfJobScheduleCondition arrayOfJobScheduleCondtion = new ArrayOfJobScheduleCondition();
		arrayOfJobScheduleCondtion
				.setJobScheduleCondition(aJobScheduleCondition);

		final JobScheduleSearch jobScheduleSearch = new JobScheduleSearch();
		jobScheduleSearch.setFetchDirection(Boolean.FALSE);
		jobScheduleSearch.setFetchSize(new Integer(1000));
		jobScheduleSearch.setConditionArray(arrayOfJobScheduleCondtion);

		final SelectJobSchedules selectJobSchedules = new SelectJobSchedules();
		selectJobSchedules.setSearch(jobScheduleSearch);
		selectJobSchedules.setResultDef(getJobResultDef());

		SelectJobSchedulesResponse selectJobsResponse = new SelectJobSchedulesResponse();
		selectJobsResponse = srcHelper.selectJobSchedules(selectJobSchedules);

		final ArrayOfJobProperties arrayOfJobProperties = selectJobsResponse
				.getJobs();
		final JobProperties[] jobPropArray = arrayOfJobProperties
				.getJobProperties();
		for (int i = 0; i < jobPropArray.length; i++) {
			final JobProperties jobProp = jobPropArray[i];
			copyJob(jobProp);
		}

	}

	private void copyJob(final JobProperties job) {
		final GetJobDetails getJobDetails = new GetJobDetails();
		getJobDetails.setResultDef(getJobDetailsResultDef());
		getJobDetails.setJobId(job.getJobId());

		GetJobDetailsResponse response = new GetJobDetailsResponse();
		try {
			response = srcHelper.getJobDetails(getJobDetails);
		} catch (final RemoteException e) {
			out.println("Bad schedule item for " + job.getInputFileId());
		}

		out.println(job.getJobName());
		createJob(response);

		/*
		 * ArrayOfParameterValue params = response.getReportParameters(); if
		 * (params != null) { Object[] pValue = params.getParameterValue();
		 * System.out.println("params " + pValue.length); } else
		 * System.out.println("no params");
		 */
	}

	public void createJob(final GetJobDetailsResponse srcResponse) {

		final JobProperties jobProp = srcResponse.getJobAttributes();
		if (jobProp == null) {
			out.println("No job properties ");
			return;
		}
		final SubmitJob submitJob = new SubmitJob();
		submitJob.setHeadline("AUTO GEN " + jobProp.getActualHeadline());
		submitJob.setInputFileName(jobProp.getInputFileName());
		submitJob.setJobName("AUTO " + jobProp.getJobName());
		submitJob.setKeepOutputFile(Boolean.TRUE);
		submitJob.setNotifyUsersByName(srcResponse.getNotifyUsers());

		// submitJob.setOperation(Operation.RunReport);
		submitJob.setParameterValues(srcResponse.getReportParameters());
		submitJob.setPriority(jobProp.getPriority().intValue());
		submitJob.setQuery(srcResponse.getQuery());

		final NewFile newFile = new NewFile();
		// build a new file
		String outputName = jobProp.getRequestedOutputFileName();

		if ("INT-Unbilled Commercial Personal".equalsIgnoreCase(outputName))
			System.out.println("d");

		if (outputName.startsWith("/internal")
				|| jobProp.getJobName().startsWith("INT")) {
			outputName = outputName.substring("/internal/home/user".length());
			outputName = "/test/internal" + outputName;
		}

		if (outputName.startsWith("/external/home/user")
				|| jobProp.getJobName().startsWith("EXT")) {
			outputName = outputName.substring("/external/home/user".length());
			outputName = "/test/external" + outputName;
		}

		newFile.setName(outputName);
		newFile.setVersioning(VersioningOption.CreateNewVersion);

		newFile.setAccessType(srcResponse.getOutputFileAccessType());

		newFile.setACL(srcResponse.getDefaultOutputFileACL());
		submitJob.setRequestedOutputFile(newFile);

		submitJob.setRunLatestVersion(Boolean.TRUE);
		submitJob.setSchedules(srcResponse.getSchedules());

		final RetryOptions retryOption = new RetryOptions();
		retryOption.setRetryOption(RetryOptionType.Disabled);
		submitJob.setRetryOptions(retryOption);

		final JobInputDetail dtl = srcResponse.getInputDetail();
		submitJob.setSendEmailForFailure(dtl.getSendEmailForFailure());
		submitJob.setSendEmailForSuccess(dtl.getSendEmailForSuccess());
		submitJob.setSendFailureNotice(dtl.getRecordFailureStatus());
		submitJob.setSendSuccessNotice(dtl.getRecordSuccessStatus());

		try {
			dstHelper.submitJob(submitJob);
		} catch (final RemoteException e) {
			System.out.println("Failure to build new job for "
					+ srcResponse.getJobAttributes().getInputFileName());
			e.printStackTrace();
		}
	}

	private ArrayOfString getJobResultDef() {
		final ArrayOfString jobResultDef = new ArrayOfString();
		final String[] jobResults = { "JobId", "JobName", "JobType",
				"InputFileName", "NextStartTime", "RequestedOutputFileName",
				"State" };
		jobResultDef.setString(jobResults);
		return jobResultDef;
	}

	private ArrayOfString getJobDetailsResultDef() {
		final ArrayOfString jobResultDef = new ArrayOfString();
		final String[] jobResults = { "JobAttributes", "InputDetail",
				"Schedules", "PrinterOptions", "NotifyUsers",
				"DefaultOutputFileACL", "Status", "ReportParameters", "Query",
				"OutputFileAccessType" };
		jobResultDef.setString(jobResults);
		return jobResultDef;
	}

	public synchronized int getCount() {
		return count;
	}

	public synchronized int getDeleteCount() {
		return deleteCount;
	}

	public synchronized int getTotalSize() {
		return totalSize;
	}

}
