package example;

/*

Actuate Client Example

This program demonstrates select file message with:
- search condition
- fetch handle
- name
- name list

java SelectFiles [options] ...

Common options are:
     -h hostname    SOAP endpoint, default 'http://localhost:8000'
     -u username    specify username, default 'Administrator'
     -p password    specify password, default ''
     -v volume      specify target volume
     -? print this usage
*/


public class SelectFiles
{
	public static final String usage =
		"Usage:\n"+
		"     java SelectFiles [options]\n";

	public static void main(String[] args)
	{
		// set command line usage
		Arguments.usage = usage;

		// get command line arguments
		Arguments arguments = new Arguments(args);

		try
		{

			// login to actuate server
			ActuateControl actuateControl = new ActuateControl(arguments.getURL());
			actuateControl.setUsername(arguments.getUsername());
			actuateControl.setPassword(arguments.getPassword());
			actuateControl.setTargetVolume(arguments.getTargetVolume());
			actuateControl.login();

			// Search based on file type
			com.actuate.schemas.FileCondition fileCondition =
				new com.actuate.schemas.FileCondition();
			fileCondition.setField(com.actuate.schemas.FileField.FileType);
			fileCondition.setMatch("R*");

			// This fileSearch demonstrate the use of FetchHandle by fetching 5 result at a time
			com.actuate.schemas.FileSearch fileSearch =
				new com.actuate.schemas.FileSearch();
			fileSearch.setCondition(fileCondition);
			fileSearch.setFetchSize(new Integer(5));


			String fetchHandle = null;

			while (true)
			{
				// set the fetchHandle
				fileSearch.setFetchHandle(fetchHandle);

				// selectFiles
				com.actuate.schemas.SelectFilesResponse selectFilesResponse =
					actuateControl.selectFiles(fileSearch, null, null);

				// get the fetch handle in the response
				fetchHandle = selectFilesResponse.getFetchHandle();

				// if the fetch handle is not returned that means we have fetched all the select
				// result
				if (null == fetchHandle)
					break;
			}

			// This example demonstrate select with name
			String name = "/report/SampleBIRTReport.rptdesign";
			actuateControl.selectFiles(null, name, null);

			// This example demonstrate select with name list
			com.actuate.schemas.ArrayOfString nameList =
				ActuateControl.newArrayOfString(
					new String[] {
						"/report/SampleBIRTReport.rptdesign",
						"/output/SampleBIRTReport.rptdocument" });
			actuateControl.selectFiles(null, null, nameList);

		}
		catch (Exception e)
		{
		}

	}
}
