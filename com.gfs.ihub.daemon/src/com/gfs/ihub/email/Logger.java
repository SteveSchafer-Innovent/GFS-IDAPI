package com.gfs.ihub.email;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

public class Logger {
	private final File file;

	public Logger() {
		this.file = null;
	}

	public Logger(final String dirName, final String altDirName,
			final String fileName) {
		final File dir = new File(dirName);
		if (dir.exists())
			this.file = new File(dir, fileName);
		else {
			final File altDir = new File(altDirName);
			if (altDir.exists())
				this.file = new File(altDir, fileName);
			else
				this.file = null;
		}
	}

	private void log(final PrintStream ps, final String message,
			final Throwable t) {
		ps.print(new Date());
		ps.print(" ");
		ps.println(message);
		if (t != null)
			t.printStackTrace(ps);
	}

	public void log(final String message) {
		this.log(message, null);
	}

	public void log(final String message, final Throwable t) {
		try {
			final File file = this.file;
			if (file == null) {
				log(System.out, message, t);
			} else {
				final FileOutputStream fos = new FileOutputStream(file, true);
				final PrintStream ps = new PrintStream(fos);
				try {
					log(ps, message, t);
				} finally {
					ps.close();
				}
			}
		} catch (final IOException e) {
			System.out.println("unable to log " + message);
			e.printStackTrace();
		}
	}

}
