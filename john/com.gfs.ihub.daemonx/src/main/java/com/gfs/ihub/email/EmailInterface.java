package com.gfs.ihub.email;

import java.io.File;

public interface EmailInterface {

	void sendMail(String from, String[] to, String[] cc, String[] bcc,
			String subject, String body, boolean b, String fileName,
			File outputFile, String contentType);

}
