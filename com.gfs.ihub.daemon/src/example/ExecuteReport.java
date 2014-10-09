package example;

/*

Actuate Client Example

This program demonstrates how to use ExecuteReport. It takes
an input filename and an output file name.

Usage:
     java ExecuteReport [options] <inputFileName> <outputFileName>
Example:
     java ExecuteReport /report/SampleBIRTReport.rptdesign /output/SampleBIRTReport.rptdocument

Common options are:
     -h hostname    SOAP endpoint, default 'http://localhost:8000'
     -u username    specify username, default 'Administrator'
     -p password    specify password, default ''
     -v volume      specify target volume
     -? print this usage
*/

public class ExecuteReport
{

	public static ActuateControl actuateControl;

	public static void main(String[] args)
	{
		// download settings
		String inputFileName;
		String outputFileName;

		// set command line usage
		Arguments.usage =
				"Usage:\n"+
				"     java ExecuteReport [options] <inputFileName> <outputFileName>\n"+
				"Example:\n"+
				"     java ExecuteReport /report/SampleBIRTReport.rptdesign /output/SampleBIRTReport.rptdocument\n";

		// get command line arguments
		Arguments arguments = new Arguments(args);
		inputFileName = arguments.getArgument();
		outputFileName = arguments.getArgument();

		System.out.println("Input  FileName: " + inputFileName);
		System.out.println("Output FileName: " + outputFileName);

		try
		{
			actuateControl = new ActuateControl(arguments.getURL());

			// Login
			actuateControl.setUsername(arguments.getUsername());
			actuateControl.setPassword(arguments.getPassword());
			actuateControl.setTargetVolume(arguments.getTargetVolume());
			actuateControl.login();

			// Test ExecuteReport
			actuateControl.setInputFileName(inputFileName);
			actuateControl.setOutputFileName(outputFileName);
			actuateControl.executeReport();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
