package example;

/*

Actuate Client Example

This program demonstrates select page example.

Usage:
     java SelectPage [options] <filename> [<pageNumber>] [<directory>] [Reportlet|DHTML]
     pageNumber   default='1'
     directory    local directory where file are saved default='download/'

Example:
     java -e


Common options are:
     -h hostname    SOAP endpoint, default 'http://localhost:8000'
     -u username    specify username, default 'Administrator'
     -p password    specify password, default ''
     -v volume      specify target volume
     -? print this usage

*/

public class SelectPage
{

	public static ActuateControl actuateControl;

	public static void main(String[] args)
	{
		// download settings
		String filename;
		String downloadDirectory = "download";
		String format = "Reportlet";
		int pageNumber = 1;
		String fileType = "ROI";

		// set command line usage
		Arguments.usage =
				"Usage:\n"+
				"     java SelectPage [options] <filename> [<pageNumber>] [<directory>] [Reportlet|DHTML]\n"+
				"     pageNumber   default='1'\n"+
				"     directory    local directory where file are saved default='download/'\n"+
				"\n"+
				"Example:\n"+
				"     java -e \n"+
				"\n";

		// get command line arguments
		Arguments arguments = new Arguments(args);
		filename = arguments.getArgument();
		pageNumber = Integer.parseInt(arguments.getOptionalArgument("1"));
		downloadDirectory = arguments.getOptionalArgument(downloadDirectory);
		fileType = filename.split("\\.")[1];

		String argument;
		argument = arguments.getOptionalArgument("");
		if ("Reportlet".equalsIgnoreCase(argument))
			format = "Reportlet";
		else if ("DHTML".equalsIgnoreCase(argument))
			format = "DHTML";
		
		System.out.println("Filename : " + filename);
		System.out.println("Page     : " + pageNumber);
		System.out.println("Directory: " + downloadDirectory );
		System.out.println("Format   : " + format);

		try
		{
			actuateControl = new ActuateControl(arguments.getURL());

			// Login
			actuateControl.setUsername(arguments.getUsername());
			actuateControl.setPassword(arguments.getPassword());
			actuateControl.setTargetVolume(arguments.getTargetVolume());
			actuateControl.login();

			// Test Viewing operations
			if ("rptdocument".equalsIgnoreCase(fileType))
				actuateControl.selectJavaReportPage(filename,format,pageNumber,downloadDirectory);
			else actuateControl.selectPage(filename,format,pageNumber,downloadDirectory);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
