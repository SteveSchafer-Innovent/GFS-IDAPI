/*

Actuate Client Example

This program  demonstrates SubmitJob. It submitted 2 jobs
for a given input file.  

Usage:
     java SubmitJob [options] [<inputFileName>] [<outputFileName>]
Example:
     java SubmitJob /report/SampleBIRTReport.rptdesign /output/SampleBIRTReport.rptdocument

This program submit a scheduled job and run an immediate job with some report
parameters.

Common options are:  
     -h hostname    SOAP endpoint, default 'http://localhost:8000'
     -u username    specify username, default 'Administrator'
     -p password    specify password, default ''
     -v volume      specify target volume
     -? print this usage

*/
package example;

import java.rmi.RemoteException;

import com.actuate.schemas.ArrayOfJobScheduleDetail;
import com.actuate.schemas.ArrayOfParameterValue;
import com.actuate.schemas.JobScheduleDetailScheduleType; 
import com.actuate.schemas.JobSchedule;
import com.actuate.schemas.JobScheduleDetail;
import com.actuate.schemas.Monthly;
import com.actuate.schemas.NewFile;
import com.actuate.schemas.SubmitJobOperation; 
import com.actuate.schemas.ParameterValue;
import com.actuate.schemas.SubmitJobResponse;

public class SubmitJob {

	public static ActuateControl actuateControl;

	public static String inputFileName;
	public static String outputFileName;

	public static void testSubmitJob() throws RemoteException {

		// Submit a scheduled job which run on the last week day of the month
		// at 1:00 PM localtime

		Monthly monthly = new Monthly();
		monthly.setFrequencyInMonths(1);
		monthly.setOnWeekDay(new Integer(0));
		monthly.setOnceADay("13:00:00");
		
		JobScheduleDetail[] jobScheduleDetail =
			new JobScheduleDetail[] { new JobScheduleDetail()};
		jobScheduleDetail[0].setMonthly(monthly);
		
		jobScheduleDetail[0].setScheduleType(JobScheduleDetailScheduleType.Monthly);

		ArrayOfJobScheduleDetail scheduleDetails =
			new ArrayOfJobScheduleDetail();
		scheduleDetails.setJobScheduleDetail(jobScheduleDetail);

		JobSchedule jobSchedule = new JobSchedule();
		jobSchedule.setScheduleDetails(scheduleDetails);

		NewFile requestedOutputFile = new NewFile();
		requestedOutputFile.setName(outputFileName);

		ArrayOfParameterValue parameterValues = new ArrayOfParameterValue();
		parameterValues
			.setParameterValue(new ParameterValue[] {
				ActuateControl.newParameterValue("NewParameter", "Oregon")});

		com.actuate.schemas.SubmitJob submitJob =
			new com.actuate.schemas.SubmitJob();
		submitJob.setJobName("Monthly Report");
		submitJob.setHeadline("June 2002");
		submitJob.setInputFileName(inputFileName);
		submitJob.setRequestedOutputFile(requestedOutputFile);
		submitJob.setSchedules(jobSchedule);
		submitJob.setOperation(SubmitJobOperation.RunReport);
		submitJob.setParameterValues(parameterValues);

		SubmitJobResponse submitJobResponse = null;
		submitJobResponse = actuateControl.proxy.submitJob(submitJob);

		// submit another job which runs immediately
		submitJob.setSchedules(null);
		submitJobResponse = actuateControl.proxy.submitJob(submitJob);
	}

	public static void main(String[] args) {
		// download settings
		inputFileName = "/report/SampleBIRTReport.rptdesign";
		outputFileName = "/output/SampleBIRTReport.rptdocument";

		// set command line usage
		Arguments.usage =
			"Usage:\n"
				+ "     java SubmitJob [options] [<inputFileName>] [<outputFileName>]\n"
				+ "Example:\n"
				+ "     java SubmitJob /report/SampleBIRTReport.rptdesign /output/SampleBIRTReport.rptdocument\n"
				+ "\n"
				+ "This program submit a scheduled job and run an immediate job with some report\n"
				+ "parameters\n";

		// get command line arguments
		Arguments arguments = new Arguments(args);
		inputFileName = arguments.getOptionalArgument(inputFileName);
		outputFileName = arguments.getOptionalArgument(outputFileName);

		System.out.println("Input  FileName: " + inputFileName);
		System.out.println("Output FileName: " + outputFileName);

		try {
			actuateControl = new ActuateControl(arguments.getURL());

			// Login
			actuateControl.setUsername(arguments.getUsername());
			actuateControl.setPassword(arguments.getPassword());
			actuateControl.setTargetVolume(arguments.getTargetVolume());
			actuateControl.login();

			// Test SubmitJob
			testSubmitJob();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
