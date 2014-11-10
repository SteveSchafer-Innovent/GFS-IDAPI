package com.gfs.ihub.email;

public interface DatabaseInterface {
	int addEmailAddressToDB(String address);

	int addMimeTypeToDB(final String formatType);

	int addNotificationToDB(long jobId, int senderPk, int mimeTypePk,
			String subject, String body, String fileName);

	void addRecipientToDB(int addressId, int notificationId);

	boolean notificationExistsInDB(long jobId);

	void close();

	enum SQLGrammar {
		ORACLE, POSTGRESQL
	}
}
