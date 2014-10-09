package example;

/*

Actuate Client Example

This program demonstrates a simple example for select user.

*/

public class SelectUsers {

	public static void main(String[] args) {
				
		// get command line arguments
		Arguments arguments = new Arguments(args);

		try {

			// login to actuate server
			ActuateControl actuateControl = new ActuateControl(arguments.getURL());
			actuateControl.setUsername(arguments.getUsername());
			actuateControl.setPassword(arguments.getPassword());
			actuateControl.setTargetVolume(arguments.getTargetVolume());
			actuateControl.login();

			// try select operation
			actuateControl.selectUsers();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
			
	}
}
