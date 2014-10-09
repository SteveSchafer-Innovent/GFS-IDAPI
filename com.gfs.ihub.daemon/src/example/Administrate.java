/*

Actuate Client Example

This program demonstrates the following Administrate operations:
- create user
- update user
- delete user
- create folder
- create role
It also demonstrates how the administrate operation tag can be sent
as individual operation, grouped or combined as one transaction.

An optional argument ignoreDuplicate can be specified in the
command line, to ignore errors when an existing user or role already created.

Usage:
     java Administrate [options] [ignoreDuplicate]

Common options are:
     -h hostname    SOAP endpoint, default 'http://localhost:8000'
     -u username    specify username, default 'Administrator'
     -p password    specify password, default ''
     -v volume      specify target volume
     -? print this usage
*/
package example;

import java.rmi.RemoteException;
import com.actuate.schemas.*;

public class Administrate
{
	public static final String usage =
	"Usage:\n"+
	"     java Administrate [options] [ignoreDuplicate]\n";

	public static ActuateControl actuateControl;

	public static Boolean ignoreDup = Boolean.FALSE;

	/**
	 *
	 * This example send one Administrate operation at a time to create users and folders
	 *
	 * @throws RemoteException 
	 */
	public static void testCreateUser() throws RemoteException
	{
		int numberOfUsers = 20;

		// create 20 users
		for (int i = 0; i < numberOfUsers; i++)
		{

			// username are: User1 .. User100
			String username = "User" + i;
			String homeFolder = "/home/" + username;

			// create a normal user with password same as username
			User user = ActuateControl.newUser(username, username, homeFolder);

			// Set User0,3,6,...  view preference to DHTML
			// Set User1,4,7,... view preference to Default
			// Don't set User2,5,8,... view preference, the server will default it
			switch (i % 3)
			{
				case 0 :
					user.setViewPreference(UserViewPreference.DHTML);
					break;
				case 1 :
					user.setViewPreference(UserViewPreference.Default);
					break;
				case 2 :
					break;
			}

			// set notice option
			user.setSendNoticeForSuccess(new Boolean((i & 1) > 0));
			user.setSendNoticeForFailure(new Boolean((i & 2) > 0));

			// set email option
			user.setSendEmailForSuccess(new Boolean((i & 4) > 0));
			user.setSendEmailForFailure(new Boolean((i & 8) > 0));

			// create a fake email address User1@localhost
			user.setEmailAddress(username + "@" + "localhost");

			// assign different job priority
			user.setMaxJobPriority(new Long(1000-i));

			// create the user
			actuateControl.createUser(user);
		}

		// create folders for the user
		for (int i = 0; i < numberOfUsers; i++)
		{

			// username are: User1, User2, ...
			String username = "User" + i;
			String homeFolder = "/home/" + username;

			actuateControl.setUsername(username);
			actuateControl.setPassword(username);
			actuateControl.login();
			// login as that user and create a report directory in his home folder
			actuateControl.createFolder(homeFolder, "report", "My Reports");

		}

		// login as Administrator again
		actuateControl.setUsername("Administrator");
		actuateControl.setPassword("");
		actuateControl.login();

		// Create more subfolders
		actuateControl.createFolder("/", "report", "Main Report Folder");
		for (int i = 0; i < numberOfUsers; i++)
			actuateControl.createFolder(
				"/report",
				"subfolder" + i,
				"Sub Folder");

	}

	/**
	 * This example send multiple Administrate operation in a transaction tag
	 * @throws RemoteException
	 */
	public static void testCreateRole() throws RemoteException
	{
		int numberOfRoles = 10;

		TransactionOperation[] transactionOperations =
			new TransactionOperation[numberOfRoles];

		for (int i = 0; i < numberOfRoles; i++)
		{
			// Role name are Role1, Role2, Role3 ...
			String roleName = "Role" + i;
			System.out.println("Creating role " + roleName);

			Role role = new Role();
			role.setName(roleName);
			role.setDescription(roleName);

			CreateRole createRole = new CreateRole();
			createRole.setRole(role);
			createRole.setIgnoreDup(ignoreDup);

			AdminOperation adminOperation = new AdminOperation();
			adminOperation.setCreateRole(createRole);

			TransactionOperation transactionOperation =
				new TransactionOperation();
			transactionOperation.setCreateRole(createRole);

			transactionOperations[i] = transactionOperation;
		}

		Transaction transaction = new Transaction();
		transaction.setTransactionOperation(transactionOperations);

		AdminOperation adminOperation = new AdminOperation();
		adminOperation.setTransaction(transaction);

		if (null == actuateControl.runAdminOperation(adminOperation))
		{
			System.out.println("Test Create Role failed");
		}
	}

	/**
	 * Demonstrate giving roles to users
	 *
	 * @throws RemoteException
	 */
	public static void testUpdateUser() throws RemoteException
	{
		System.out.println("Updating User0");

		// do a search based on Name
		UserCondition userCondition = new UserCondition();
		userCondition.setField(UserField.Name);
		userCondition.setMatch("User0");

		UserSearch userSearch = new UserSearch();
		userSearch.setCondition(userCondition);

		// give User0 roles as Role0, Administrators
		UpdateUserOperation updateUserOperation = new UpdateUserOperation();
		updateUserOperation.setSetRolesByName(
			ActuateControl.newArrayOfString(
				new String[] { "Administrators", "Role0" }));

		UpdateUserOperationGroup updateUserOperations =
			new UpdateUserOperationGroup();
		updateUserOperations.setUpdateUserOperation(
			new UpdateUserOperation[] { updateUserOperation });

		// setup UpdateUser message
		UpdateUser updateUser = new UpdateUser();
		updateUser.setSearch(userSearch);
		updateUser.setUpdateUserOperationGroup(updateUserOperations);

		// setup Administrate message
		AdminOperation adminOperation = new AdminOperation();
		adminOperation.setUpdateUser(updateUser);

		actuateControl.runAdminOperation(adminOperation);
	}

	/**
	 * Demonstrate user condition search to delete user
	 *
	 * @throws RemoteException
	 */
	public static void testDeleteUser() throws RemoteException
	{
		System.out.println("Deleting User18 using search condition");

		UserCondition userCondition = new UserCondition();
		userCondition.setField(UserField.Name);
		userCondition.setMatch("User18");

		UserSearch userSearch = new UserSearch();
		userSearch.setCondition(userCondition);

		DeleteUser deleteUser = new DeleteUser();
		deleteUser.setSearch(userSearch);

		AdminOperation adminOperation = new AdminOperation();
		adminOperation.setDeleteUser(deleteUser);

		actuateControl.runAdminOperation(adminOperation);
	}

	public static void main(String[] args)
	{
		Arguments.usage = usage;

		// get command line arguments
		Arguments arguments = new Arguments(args);


		String argument = arguments.getOptionalArgument("");
		if ("ignoreDuplicate".equalsIgnoreCase(argument))
		{
			ignoreDup = Boolean.TRUE;
		}

		try
		{
			actuateControl = new ActuateControl(arguments.getURL());

			// Login
			actuateControl.setUsername(arguments.getUsername());
			actuateControl.setPassword(arguments.getPassword());
			actuateControl.setTargetVolume(arguments.getTargetVolume());
			actuateControl.login();

			// create users
			testCreateUser();

			// create roles
			testCreateRole();

			// assign roles to the user
			testUpdateUser();

			// delete a user
			testDeleteUser();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
