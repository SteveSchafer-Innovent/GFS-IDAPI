package example;

/*

Actuate Client Example

This program demonstrates GetFolderItems. It takes an optional
parameter a folder name in encyclopedia. The default value is
the root folder.

Usage:
     java GetFolderItems [options] [<directory>]
Example:
     java GetFolderItems /

Common options are:
     -h hostname    SOAP endpoint, default 'http://localhost:8000'
     -u username    specify username, default 'Administrator'
     -p password    specify password, default ''
     -v volume      specify target volume
     -? print this usage

*/

public class GetFolderItems {

	public static ActuateControl actuateControl;

	public static void main(String[] args) {
		// download settings
		String directory = "/";

		// set command line usage
		Arguments.usage =
			"Usage:\n"
				+ "     java GetFolderItems [options] [<directory>]\n"
				+ "Example:\n"
				+ "     java GetFolderItems /\n";

		// get command line arguments
		Arguments arguments = new Arguments(args);
		directory = arguments.getOptionalArgument(directory);

		System.out.println("Directory: " + directory);

		try {
			actuateControl = new ActuateControl(arguments.getURL());

			// Login
			actuateControl.setUsername(arguments.getUsername());
			actuateControl.setPassword(arguments.getPassword());
			actuateControl.setTargetVolume(arguments.getTargetVolume());
			actuateControl.login();

			// Test File/Folder operations
			actuateControl.setCurrentDirectory(directory);
			com.actuate.schemas.GetFolderItemsResponse response =
				actuateControl.getFolderItems();

			// displaying result
			com.actuate.schemas.ArrayOfFile itemList = response.getItemList();

			int totalCount = response.getTotalCount().intValue();
			for (int i = 0; i < totalCount; i++) {
				com.actuate.schemas.File file = itemList.getFile(i);
				ActuateControl.printFile(System.out, file);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
