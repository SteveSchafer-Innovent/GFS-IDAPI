/*

Actuate Client Example

This program demonstrates how to create a folder. It takes a folder
name, homeFolder where the folder is located and the folder
description. The homeFolder must be an absolute path.

Usage: java CreateFolder [options] folderName [homeFolder] [descrip
        Default home folder is '/'
        Default description is 'default description'

Common options are:
     -h hostname    SOAP endpoint, default 'http://localhost:8000'
     -u username    specify username, default 'Administrator'
     -p password    specify password, default ''
     -v volume      specify target volume
     -? print this usage

*/
package example;

public class CreateFolder
{

	public static void main(String[] args)
	{

		// set command line usage
		Arguments.usage =
			"Usage: java CreateFolder [options] folderName [homeFolder] [description]\n"
				+ "	Default home folder is '/'\n"
				+ "	Default description is 'default description'\n";

		// get command line arguments
		Arguments arguments = new Arguments(args);

		String folderName = arguments.getArgument();
		String homeFolder = arguments.getOptionalArgument("/");
		String description =
			arguments.getOptionalArgument("default description");

		try
		{

			// login to actuate server
			ActuateControl actuateControl =
				new ActuateControl(arguments.getURL());
			actuateControl.setUsername(arguments.getUsername());
			actuateControl.setPassword(arguments.getPassword());
			actuateControl.setTargetVolume(arguments.getTargetVolume());
			actuateControl.login();

			// create folder
			actuateControl.createFolder(homeFolder, folderName, description);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}
