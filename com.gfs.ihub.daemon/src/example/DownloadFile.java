package example;

/*


Actuate Client Example

This program demonstrates how to download a file from Actuate
Encylopedia Server using Apache AXIS library. A file can be
downloaded either with MIME attachment or embedded in the soap
response.

This program takes an encyclopedia filename to download. You
can specify where the file should be saved by supplying a
directory name. For compound files such as ROX, you can
specify whether to decompose the compound file into several
parts.

Usage:
     java DownloadFile [options] <filename> [<directory>] [decompose]
     directory    local directory where file are saved default='download/'
                  only used when the download embedded is turned on
     -e turn on download embedded, default is download mime
Example:
     java -e DownloadFile /report/SampleBIRTReport.rptdesign download 

Common options are:
     -h hostname    SOAP endpoint, default 'http://localhost:8000'
     -u username    specify username, default 'Administrator'
     -p password    specify password, default ''
     -v volume      specify target volume
     -? print this usage

*/
import java.io.File;
import java.util.Iterator;

import javax.xml.soap.SOAPException;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Service;

import com.actuate.schemas.ArrayOfAttachment;
import com.actuate.schemas.Attachment;


public class DownloadFile {
	
	public static final String QNAME_NAMESPACE = "http://schemas.actuate.com/actuate11";
	
	Service service = new Service();

	public static void main(String[] args) {
		// download settings
		String filename;
		Boolean decomposeCompoundDocument = Boolean.FALSE;
		Boolean downloadEmbedded = Boolean.FALSE;
		String downloadDirectory = "download";

		// set command line usage
		Arguments.usage =
			"Usage:\n"
				+ "     java DownloadFile [options] <filename> [<directory>] [decompose]\n"
				+ "     directory    local directory where file are saved default='download/'\n"
				+ "                  only used when the download embedded is turned on\n"
				+ "     -e turn on download embedded, default is download mime\n"
				+ "Example:\n"
				+ "     java -e DownloadFile /report/SampleBIRTReport.rptdesign download \n";

		// get command line arguments
		Arguments arguments = new Arguments(args);
		filename = arguments.getArgument();
		downloadDirectory = arguments.getOptionalArgument(downloadDirectory);
		downloadEmbedded =
			arguments.isEmbeddedDownload() ? Boolean.TRUE : Boolean.FALSE;
		if ("decompose".equalsIgnoreCase((arguments.getOptionalArgument(""))))
			decomposeCompoundDocument = Boolean.TRUE;

		// print
		System.out.println("Download : " + filename);
		System.out.println("Decompose: " + decomposeCompoundDocument);
		System.out.println("Embedded : " + downloadEmbedded);
		if (downloadEmbedded == Boolean.TRUE)
			System.out.println("Directory: " + downloadDirectory);

		// just create the download directory
		new File(downloadDirectory).mkdir();

		try 
		{
			// login to actuate server
			ActuateControl actuateControl =
				new ActuateControl(arguments.getURL());
			actuateControl.setUsername(arguments.getUsername());
			actuateControl.setPassword(arguments.getPassword());
			actuateControl.setTargetVolume(arguments.getTargetVolume());
			actuateControl.login();
			String downloadedFilePath = actuateControl.downloadFile(filename,decomposeCompoundDocument,downloadEmbedded,downloadDirectory);
			if( downloadedFilePath != null)
			{
				System.out.println( "File downloaded to : " + downloadedFilePath );
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
