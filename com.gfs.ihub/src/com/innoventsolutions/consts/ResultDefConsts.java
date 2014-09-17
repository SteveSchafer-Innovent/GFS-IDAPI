/*
 * Created on Feb 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.innoventsolutions.consts;
import com.actuate.schemas.ArrayOfString;

/**
 *  @author Scott Rosenbaum
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ResultDefConsts {
	public static final ArrayOfString getFolderItemsResultDef() {
		ArrayOfString resultDef = new ArrayOfString();
		String[] GET_FOLDER_ITEMS_RESULT_DEF = { "Description", "FileType", "Owner", "PageCount",
				"Size", "TimeStamp", "Version", "VersionName", "UserPermissions", "Name", "Id" };

		resultDef.setString(GET_FOLDER_ITEMS_RESULT_DEF);
		return resultDef;
	}

	public static final ArrayOfString getFilesSimpleResultDef() {
		ArrayOfString filesResultDef = new ArrayOfString();

		String[] GET_FILES_RESULT_DEF = { "Description", "FileType", "Id", "Name", "Owner",
				"PageCount", "Size", "TimeStamp", "UserPermissions", "Version", "VersionName" };

		filesResultDef.setString(GET_FILES_RESULT_DEF);
		return filesResultDef;
	}

	public static final ArrayOfString getFilesResultDef() {
		ArrayOfString filesResultDef = new ArrayOfString();

		String[] GET_FILES_RESULT_DEF = { "Description", "FileType", "Id", "Name", "Owner",
				"PageCount", "Size", "TimeStamp", "UserPermissions", "Version", "VersionName", "ACL",
				"ArchiveRules", "AccessType" };

		filesResultDef.setString(GET_FILES_RESULT_DEF);
		return filesResultDef;
	}

	public static final ArrayOfString getNameResultDef() {
		ArrayOfString filesResultDef = new ArrayOfString();

		String[] GET_FILES_RESULT_DEF = { "Name" };

		filesResultDef.setString(GET_FILES_RESULT_DEF);
		return filesResultDef;
	}
}
