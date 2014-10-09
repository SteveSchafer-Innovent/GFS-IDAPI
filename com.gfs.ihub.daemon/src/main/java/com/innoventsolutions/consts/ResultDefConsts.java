/*
 * Created on Feb 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.innoventsolutions.consts;

import com.actuate.schemas.ArrayOfString;

/**
 * @author Scott Rosenbaum
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ResultDefConsts {
	public static final ArrayOfString getFolderItemsResultDef() {
		final ArrayOfString resultDef = new ArrayOfString();
		final String[] results = { "Description", "FileType",
				"Owner", "PageCount", "Size", "TimeStamp", "Version",
				"VersionName", "UserPermissions", "Name", "Id" };
		resultDef.setString(results);
		return resultDef;
	}

	public static final ArrayOfString getFilesSimpleResultDef() {
		final ArrayOfString resultDef = new ArrayOfString();
		final String[] results = { "Description", "FileType", "Id", "Name",
				"Owner", "PageCount", "Size", "TimeStamp", "UserPermissions",
				"Version", "VersionName" };
		resultDef.setString(results);
		return resultDef;
	}

	public static final ArrayOfString getFilesResultDef() {
		final ArrayOfString resultDef = new ArrayOfString();
		final String[] results = { "Description", "FileType", "Id", "Name",
				"Owner", "PageCount", "Size", "TimeStamp", "UserPermissions",
				"Version", "VersionName", "ACL", "ArchiveRules", "AccessType" };
		resultDef.setString(results);
		return resultDef;
	}

	public static final ArrayOfString getNameResultDef() {
		final ArrayOfString resultDef = new ArrayOfString();
		final String[] results = { "Name" };
		resultDef.setString(results);
		return resultDef;
	}

	public static final ArrayOfString getJobResultDef() {
		final ArrayOfString resultDef = new ArrayOfString();
		final String[] results = { "JobId", "JobName", "JobType",
				"InputFileName", "NextStartTime", "RequestedOutputFileName",
				"State" };
		resultDef.setString(results);
		return resultDef;
	}

	public static final ArrayOfString getJobDetailsResultDef() {
		final ArrayOfString resultDef = new ArrayOfString();
		final String[] results = { "JobAttributes", "InputDetail", "Schedules",
				"PrinterOptions", "NotifyUsers", "DefaultOutputFileACL",
				"Status", "ReportParameters", "Query", "OutputFileAccessType" };
		resultDef.setString(results);
		return resultDef;
	}

}
