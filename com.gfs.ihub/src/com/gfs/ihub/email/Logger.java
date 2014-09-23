package com.gfs.ihub.email;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;

public class Logger {
	private final String logFilename;

	public Logger(final String logFilename) {
		this.logFilename = logFilename;
	}

	public void log(final String message) {
		try {
			final String filename = logFilename;
			if (logFilename == null) {
				final PrintStream out = System.out;
				out.print(new Date());
				out.print(" ");
				out.println(message);
			}
			else {
				final File file = new File(filename);
				final FileWriter fw = new FileWriter(file, true);
				final PrintWriter pw = new PrintWriter(fw);
				try {
					pw.print(new Date());
					pw.print(" ");
					pw.println(message);
				}
				finally {
					pw.close();
				}
			}
		}
		catch (final IOException e) {
			System.out.println("unable to log " + message);
			e.printStackTrace();
		}
	}

	void log(final String message, final Throwable t) {
		try {
			final String filename = logFilename;
			if (filename == null) {
				final PrintStream out = System.out;
				out.print(new Date());
				out.print(" ");
				out.println(message);
				t.printStackTrace(out);
			}
			else {
				final File file = new File(filename);
				final FileWriter fw = new FileWriter(file, true);
				try {
					final PrintWriter pw = new PrintWriter(fw);
					pw.print(new Date());
					pw.print(" ");
					pw.println(message);
					t.printStackTrace(pw);
				}
				finally {
					fw.close();
				}
			}
		}
		catch (final IOException e) {
			System.out.println(message);
			t.printStackTrace();
			log("Log failure:");
			e.printStackTrace();
		}
	}
}
