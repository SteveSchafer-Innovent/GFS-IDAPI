package com.gfs.ihub.testEmail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternTest {
	private static final Pattern FILENAME_PATTERN = Pattern
			.compile("/(?:[a-zA-Z0-9_]*/)+([^;]+).*");

	public static void main(final String[] args) {
		final String fileName = args[0];
		System.out.println(fileName);

		final Matcher matcher = FILENAME_PATTERN.matcher(fileName);

		final String trimmedFilename = matcher.find() ? matcher.group(1)
				: fileName;

		System.out.println(trimmedFilename);
	}
}
