/*

Actuate Client Example

This program demonstrates how to upload a file to Actuate
Encylopedia Server using Apache AXIS library. 

This program takes a filename to upload. You can specify 
the server filename and location, if it's not specified 
then it uses the same name and place it in the root 
directory.

java UploadFile [options] localFileName [encyclopediaFileName]

     Upload a file to the server

Common options are:
   -h hostname    SOAP endpoint, default 'http://localhost:8000'
   -u username    specify username, default 'Administrator'
   -p password    specify password, default ''
   -v volume      specify target volume
   -? print this usage

 */
package example;

import java.io.File;

public class UploadFile {
	public static String usage =
		"java UploadFile [options] localFileName [encyclopediaFileName]\n"
			+ "\n"
			+ "	Upload a file to the server\n";

	public static void main(String[] args) {
		// default Upload settings
		String filename = "report/SampleBIRTReport.rptdesign";
		String encyclopediaFileName;

		// get command line arguments
		Arguments.usage = usage;
		Arguments arguments = new Arguments(args);
		filename = arguments.getArgument();

		// get an optional parameter
		encyclopediaFileName =
			arguments.getOptionalArgument(new File(filename).getName());

		if (!new File(filename).exists()) {
			System.out.println("File not found: " + filename);
			return;
		}

		System.out.println("Upload file: " + filename);
		System.out.println("As file    : " + encyclopediaFileName);

		try {
			// login to actuate server
			ActuateControl actuateControl =
				new ActuateControl(arguments.getURL());
			actuateControl.setUsername(arguments.getUsername());
			actuateControl.setPassword(arguments.getPassword());
			actuateControl.setTargetVolume(arguments.getTargetVolume());
			actuateControl.login();

			// set new file information        
			com.actuate.schemas.NewFile newFile =
				new com.actuate.schemas.NewFile();
			newFile.setName(encyclopediaFileName);

			// set the mime content id, must be the same with attachmentPart's
			com.actuate.schemas.Attachment content =
				new com.actuate.schemas.Attachment();
			content.setContentId("contentid");
			
			content.setContentType("application/octect.stream");

			// set Upload message
			com.actuate.schemas.UploadFile request =
				new com.actuate.schemas.UploadFile();
			request.setNewFile(newFile);
			request.setContent(content);

			// this use the input file as the data source
			javax.activation.DataHandler dhSource =
				new javax.activation.DataHandler(
					new javax.activation.FileDataSource(filename));

			// set attachment in the call
			org.apache.axis.attachments.AttachmentPart attachmentPart =
				new org.apache.axis.attachments.AttachmentPart();
			attachmentPart.setDataHandler(dhSource);
			attachmentPart.setContentId("contentid");

			// call upload file
			com.actuate.schemas.UploadFileResponse response =
				actuateControl.uploadFile(request, attachmentPart);

			// print out the file id of the encyclopedia file
			System.out.println("Uploaded with fileid: " + response.getFileId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
