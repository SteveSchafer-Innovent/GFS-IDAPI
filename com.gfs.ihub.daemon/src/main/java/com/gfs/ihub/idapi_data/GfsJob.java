package com.gfs.ihub.idapi_data;

import java.sql.Timestamp;
import java.util.Calendar;

import com.actuate.schemas.AbsoluteDate;
import com.actuate.schemas.ArrayOfJobScheduleDetail;
import com.actuate.schemas.ArrayOfParameterValue;
import com.actuate.schemas.Daily;
import com.actuate.schemas.EventType;
import com.actuate.schemas.GetJobDetailsResponse;
import com.actuate.schemas.JobProperties;
import com.actuate.schemas.JobPropertiesJobType;
import com.actuate.schemas.JobPropertiesState;
import com.actuate.schemas.JobSchedule;
import com.actuate.schemas.JobScheduleDetail;
import com.actuate.schemas.Monthly;
import com.actuate.schemas.ParameterValue;
import com.actuate.schemas.Weekly;

public class GfsJob extends JobProperties {

	private static final long serialVersionUID = 4226267394688019670L;
	private JobProperties jobProperties;
	private String emailFrom = "";
	private String emailTo = "";
	private String emailSubject = "";
	private String emailBody = "";
	private String parameterValues = "";
	private String entryLog = "";
	private String scheduleInfo = "";

	public GfsJob( GetJobDetailsResponse jobDetailsResponse, String entryLog) {
		this(jobDetailsResponse);
		this.entryLog = entryLog;
	}

	public GfsJob(GetJobDetailsResponse jobDetailsResponse) {

		if (jobDetailsResponse == null) {
			this.jobProperties = new JobProperties();
		}
		this.jobProperties = jobDetailsResponse.getJobAttributes();
		jobDetailsResponse.getInputDetail();

		setJobParameters(jobDetailsResponse.getReportParameters());
		setJobSchedule(jobDetailsResponse.getSchedules());


	}

	private void setJobSchedule(JobSchedule jobSched) {
		StringBuffer sb = new StringBuffer();
		ArrayOfJobScheduleDetail aosd = jobSched.getScheduleDetails();
		JobScheduleDetail[] aojsd = aosd.getJobScheduleDetail();
		for (int i = 0; i < aojsd.length; i++) {
			JobScheduleDetail jsd = aojsd[i];
			StringBuffer timeInfo = new StringBuffer();
			if(jsd.getDaily() != null){
				Daily daily = jsd.getDaily();
				timeInfo.append(daily.getOnceADay());
			} else if (jsd.getWeekly() != null){
				Weekly we = jsd.getWeekly();
				timeInfo.append(we.getOnceADay());
			} else if (jsd.getMonthly() != null) {
				Monthly mo = jsd.getMonthly();
				timeInfo.append(mo.getOnceADay());
			} else if (jsd.getAbsoluteDate() != null ){
				AbsoluteDate ab = jsd.getAbsoluteDate();
				timeInfo.append(ab.getOnceADay());
			} else {
				timeInfo.append(jsd.getScheduleStartDate()).append(" - ").append(jsd.getScheduleEndDate());
			}
			
			sb.append(jsd.getScheduleType()).append(" @ ").append(timeInfo).append("\n");
		}
		this.scheduleInfo = sb.toString();
	}

	private void setJobParameters(ArrayOfParameterValue aoPV) {

		final ParameterValue[] pva = aoPV.getParameterValue();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < pva.length; i++) {
			final String name = pva[i].getName();
			if (name.startsWith("$$$"))
				continue;
			if ("pEMAIL_FROM".equals(name)) {
				emailFrom = pva[i].getValue();
				continue;
			} else if ("pEMAIL_TO".equals(name)) {
				emailTo = pva[i].getValue();
				continue;
			} else if ("pEMAIL_SUBJECT".equals(name)) {
				emailSubject = pva[i].getValue();
				continue;
			} else if ("pEMAIL_BODY".equals(name)) {
				emailBody = pva[i].getValue();
				continue;
			}
			sb.append(pva[i].getDisplayName()).append("=").append(pva[i].getValue()).append("\n");
		}
		parameterValues = sb.toString();
	}

	//convert java objects to strings
	public Timestamp getNextStartDate() {
		if (jobProperties == null || jobProperties.getNextStartTime() == null)
			return null;

		return new Timestamp(jobProperties.getNextStartTime().getTime().getTime());
	}

	public String getStateString() {
		return jobProperties.getState().toString();
	}

	// Email Parameters
	public String getEmailFrom() {
		return emailFrom;
	}

	public String getEmailTo() {
		return emailTo;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	//all other parameters
	public String getParameterValues() {
		return parameterValues;
	}

	//Delegate methods follow
	public boolean equals(Object arg0) {
		return jobProperties.equals(arg0);
	}

	public String getActualHeadline() {
		return jobProperties.getActualHeadline();
	}

	public String getActualOutputFileId() {
		return jobProperties.getActualOutputFileId();
	}

	public String getActualOutputFileName() {
		return jobProperties.getActualOutputFileName();
	}

	public Calendar getCompletionTime() {
		return jobProperties.getCompletionTime();
	}

	public Long getDurationSeconds() {
		return jobProperties.getDurationSeconds();
	}

	public String getEventName() {
		return jobProperties.getEventName();
	}

	public String getEventParameter() {
		return jobProperties.getEventParameter();
	}

	public String getEventStatus() {
		return jobProperties.getEventStatus();
	}

	public EventType getEventType() {
		return jobProperties.getEventType();
	}

	public String getInputFileId() {
		return jobProperties.getInputFileId();
	}

	public String getInputFileName() {
		return jobProperties.getInputFileName();
	}

	public String getJobId() {
		return jobProperties.getJobId();
	}

	public String getJobName() {
		return jobProperties.getJobName();
	}

	public JobPropertiesJobType getJobType() {
		return jobProperties.getJobType();
	}

	public Calendar getNextStartTime() {
		return jobProperties.getNextStartTime();
	}

	public String getNotifyCount() {
		return jobProperties.getNotifyCount();
	}

	public Long getOutputFileSize() {
		return jobProperties.getOutputFileSize();
	}

	public String getOutputFileVersionName() {
		return jobProperties.getOutputFileVersionName();
	}

	public String getOwner() {
		return jobProperties.getOwner();
	}

	public Long getPageCount() {
		return jobProperties.getPageCount();
	}

	public String getParameterFileId() {
		return jobProperties.getParameterFileId();
	}

	public String getParameterFileName() {
		return jobProperties.getParameterFileName();
	}

	public Long getPriority() {
		return jobProperties.getPriority();
	}

	public String getRequestedHeadline() {
		return jobProperties.getRequestedHeadline();
	}

	public String getRequestedOutputFileName() {
		return jobProperties.getRequestedOutputFileName();
	}

	public String getResourceGroup() {
		return jobProperties.getResourceGroup();
	}

	public String getRoutedToNode() {
		return jobProperties.getRoutedToNode();
	}

	public Boolean getRunLatestVersion() {
		return jobProperties.getRunLatestVersion();
	}

	public Calendar getStartTime() {
		return jobProperties.getStartTime();
	}

	public JobPropertiesState getState() {
		return jobProperties.getState();
	}

	public Calendar getSubmissionTime() {
		return jobProperties.getSubmissionTime();
	}

	public String getEntryLog() {
		return entryLog;
	}

	public String getScheduleInfo() {
		return scheduleInfo;
	}

}
