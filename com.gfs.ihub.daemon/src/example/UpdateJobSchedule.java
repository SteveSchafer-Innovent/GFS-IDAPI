package example;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.actuate.schemas.JobScheduleCondition;
import com.actuate.schemas.ArrayOfJobScheduleCondition;
import com.actuate.schemas.JobScheduleSearch;
import com.actuate.schemas.SelectJobSchedules;
import com.actuate.schemas.SelectJobSchedulesResponse;
import com.actuate.schemas.JobScheduleDetail;
import com.actuate.schemas.JobScheduleDetailScheduleType;
import com.actuate.schemas.AbsoluteDate;
import com.actuate.schemas.Daily;
import com.actuate.schemas.ArrayOfJobScheduleDetail;
import com.actuate.schemas.JobSchedule;
import com.actuate.schemas.UpdateJobScheduleOperation;
import com.actuate.schemas.UpdateJobScheduleOperationGroup;
import com.actuate.schemas.AdminOperation;
import com.actuate.schemas.GetJobDetails;
import com.actuate.schemas.GetJobDetailsResponse;
/*

Actuate Client Example

This program  demonstrates UpdateJobSchedule AdminOperation.
It updates scheduled monthly job "Monthly Report" submitted in SubmitJob  

Usage:
     java UpdateJobSchedule [options] <jobName>
Example:
     java UpdateJobSchedule "Monthly Report"

This program submit a scheduled job and run an immediate job with some report
parameters.

Common options are:  
     -h hostname    SOAP endpoint, default 'http://localhost:8000'
     -u username    specify username, default 'Administrator'
     -p password    specify password, default ''
     -v volume      specify target volume
     -? print this usage
*/

public class UpdateJobSchedule {
    public static ActuateControl actuateControl;
	public static String usage="Usage:\n java UpdateJobSchedule [options] <jobName>";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	public UpdateJobSchedule(){
//		dateFormat = new SimpleDateFormat("HH:mm:ss");//("MM-dd-yyyy HH:mm:ss");
//		("MM-dd-yyyy HH:nn:ss");	
	}
	public static void main(String[] args) {
		String jobName = "";
		String jobId = null;
		// set command line usage
		Arguments.usage = usage;

		// get command line arguments
		Arguments arguments = new Arguments(args);
		jobName = arguments.getOptionalArgument("Default Job Name");
		System.out.println("jobName: " + jobName);
		try {
		actuateControl = new ActuateControl(arguments.getURL());
		}catch (javax.xml.rpc.ServiceException e){
			e.printStackTrace();
		}catch (java.net.MalformedURLException e){
			e.printStackTrace();
		}
		// Login
		actuateControl.setUsername(arguments.getUsername());
		actuateControl.setPassword(arguments.getPassword());
		actuateControl.setTargetVolume(arguments.getTargetVolume());
		actuateControl.login();
		JobScheduleDetail scheduleDetail = new JobScheduleDetail();
		//=====Get jobId
		JobScheduleCondition[] jobScheduleConditionArray = new JobScheduleCondition[1];
		JobScheduleCondition jobScheduleCondition0 = new JobScheduleCondition();
		jobScheduleCondition0.setField(com.actuate.schemas.JobScheduleField.JobName);
		jobScheduleCondition0.setMatch(jobName);
		jobScheduleConditionArray[0]=jobScheduleCondition0;
		ArrayOfJobScheduleCondition arrayOfJobScheduleCondition = new  ArrayOfJobScheduleCondition();
		arrayOfJobScheduleCondition.setJobScheduleCondition(jobScheduleConditionArray);

		JobScheduleSearch jobScheduleSearch = new JobScheduleSearch();
		jobScheduleSearch.setConditionArray(arrayOfJobScheduleCondition);
		SelectJobSchedules selectJobSchedules = new SelectJobSchedules();
		selectJobSchedules.setSearch(jobScheduleSearch);
		selectJobSchedules.setResultDef( ActuateControl.newArrayOfString(new String[] {"JobID"} ));
		try {
			SelectJobSchedulesResponse selectJobSchedulesResponse= actuateControl.proxy.selectJobSchedules(selectJobSchedules);
			if (!(selectJobSchedulesResponse.getTotalCount()==0)){
				jobId = selectJobSchedulesResponse.getJobs().getJobProperties()[0].getJobId();
			} else {
				System.out.println("Not Found Scheduled Job jobName=<"+jobName+">");
				System.exit(0);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// one day offset
		Calendar tomorrow = new java.util.GregorianCalendar();
		tomorrow.set(Calendar.DATE,	tomorrow.get(Calendar.DATE) + 1);

		AbsoluteDate date = new AbsoluteDate();
		date.setRunOn(dateFormat.format(tomorrow.getTime()));
		date.setOnceADay(timeFormat.format(tomorrow.getTime()));

		Daily daily = new Daily();
		daily.setFrequencyInDays(1);
		daily.setOnceADay(dateFormat.format(tomorrow.getTime()));

		scheduleDetail.setAbsoluteDate(date);
		scheduleDetail.setScheduleType(JobScheduleDetailScheduleType.AbsoluteDate);

		ArrayOfJobScheduleDetail scheduleList = new ArrayOfJobScheduleDetail();
		scheduleList.setJobScheduleDetail(new JobScheduleDetail[] {scheduleDetail});

		JobSchedule jobSchedule = new JobSchedule();
		jobSchedule.setTimeZoneName("Pacific Standard Time"); // check $AC_SERVER_HOME/etc/timezonemap.xml for supported time zone
		jobSchedule.setScheduleDetails(scheduleList);
			// UpdateJobSchedule
			UpdateJobScheduleOperation updateJobScheduleOperation = new UpdateJobScheduleOperation();
			updateJobScheduleOperation.setSetSchedules(jobSchedule);
			UpdateJobScheduleOperationGroup updateJobScheduleOperationGroup = new UpdateJobScheduleOperationGroup();
			updateJobScheduleOperationGroup.setUpdateJobScheduleOperation(new UpdateJobScheduleOperation[] {updateJobScheduleOperation});
			com.actuate.schemas.UpdateJobSchedule updateJobSchedule = new com.actuate.schemas.UpdateJobSchedule();
			updateJobSchedule.setId(jobId);
			updateJobSchedule.setUpdateJobScheduleOperationGroup(updateJobScheduleOperationGroup);
			AdminOperation adminOperation = new AdminOperation();
			adminOperation.setUpdateJobSchedule(updateJobSchedule);
			actuateControl.runAdminOperation(adminOperation);
			// GetJobDetails
			GetJobDetails getJobDetails = new GetJobDetails();
			getJobDetails.setJobId(jobId);
			getJobDetails.setResultDef( ActuateControl.newArrayOfString(
			  new String[] {
			  	"InputDetail",
			  	"NotifyUsers",
			  	"NotifyGroups",
			  	"NotifyChannels",
			  	"DefaultOutputFileACL",
			  	"Status",
			  	"JobAttributes",
			  	"Schedules",
			  	"ReportParameters",
			  	"PrinterOptions"
			} ));
				try {
					GetJobDetailsResponse getJobDetailsResponse =
						actuateControl.proxy.getJobDetails(getJobDetails);
					ArrayOfJobScheduleDetail arrayOfJobScheduleDetails =
						getJobDetailsResponse.getSchedules().getScheduleDetails();
					JobScheduleDetail[] jobScheduleDetails =
						arrayOfJobScheduleDetails.getJobScheduleDetail();
					for (int i=0;i<jobScheduleDetails.length;i++)
					{
						System.out.println("Schedule");
						System.out.println("Type: " + jobScheduleDetails[i].getScheduleType() );
						AbsoluteDate absoluteDate = jobScheduleDetails[i].getAbsoluteDate();
						if ( null != absoluteDate ) {
							System.out.println("RunOn: " + absoluteDate.getRunOn() );
							System.out.println("Time : " + absoluteDate.getOnceADay() );
						}
					}
					}catch (java.rmi.RemoteException e){
						e.printStackTrace();
					}
	}
}
