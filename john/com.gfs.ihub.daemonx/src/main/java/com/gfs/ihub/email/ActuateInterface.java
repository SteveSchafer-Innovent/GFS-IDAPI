package com.gfs.ihub.email;

import java.io.IOException;
import java.rmi.RemoteException;

public interface ActuateInterface {
	interface JobProcessor {
		boolean jobHasBeenProcessed(long jobId);

		void processJob(String contentType, String emailFrom, String emailTo,
				String emailSubject, String emailBody, String fileName,
				java.io.File outputFile, long jobId);
	}

	void processJobs(JobProcessor processor) throws RemoteException,
			IOException;
}
