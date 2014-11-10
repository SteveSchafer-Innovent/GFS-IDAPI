package com.gfs.ihub.email;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gfs.ihub.options.SqlOptions;

public class DatabaseInterfaceImpl implements DatabaseInterface {
	private final Connection connection;
	private final SQLGrammar sqlGrammar;
	private final Logger logger;

	public DatabaseInterfaceImpl(final SqlOptions options, final Logger logger) {
		this.logger = logger;
		// Setup JDBC connection
		try {
			final Connection connection = DriverManager.getConnection(
					options.getUrlString(), options.getUsername(),
					options.getPassword());
			SQLGrammar sqlGrammar = SQLGrammar.ORACLE;
			if (options.getUrlString().startsWith("jdbc:postgresql"))
				sqlGrammar = SQLGrammar.POSTGRESQL;
			logger.log("SQL grammar is " + sqlGrammar);
			this.sqlGrammar = sqlGrammar;
			// take the connection out of transaction mode so readOnly can be
			// set
			connection.setAutoCommit(true);
			connection.setReadOnly(false);
			connection.setAutoCommit(false);
			this.connection = connection;
			logger.log("Successfully connected to database");
		} catch (final SQLException e) {
			throw new RuntimeException("Unable to connect to database", e);
		}
	}

	public int addEmailAddressToDB(final String address) {
		try {
			{
				final PreparedStatement stmt = connection
						.prepareStatement("select email_address_sk from email_account where email_address = ?");
				try {
					stmt.setString(1, address);
					final ResultSet rs = stmt.executeQuery();
					try {
						while (rs.next()) {
							return rs.getInt(1);
						}
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			}
			int newId = 1;
			{
				final String sql;
				if (sqlGrammar == SQLGrammar.ORACLE)
					sql = "select actuate_notify_admin.email_account_seq.nextval from dual";
				else
					sql = "select nextval('email_account_seq')";
				final PreparedStatement stmt = connection.prepareStatement(sql);
				try {
					final ResultSet rs = stmt.executeQuery();
					try {
						if (rs.next()) {
							newId = rs.getInt(1);
						}
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			}
			logger.log("Adding " + address + " to database");
			{
				final PreparedStatement stmt = connection
						.prepareStatement("insert into email_account "
								+ "(email_address_sk, email_address) values (?, ?)");
				try {
					stmt.setInt(1, newId);
					stmt.setString(2, address);
					stmt.execute();
				} finally {
					stmt.close();
				}
			}
			connection.commit();
			return newId;
		} catch (final SQLException e) {
			throw new RuntimeException(
					"Unable to add email address to database", e);
		}
	}

	public int addMimeTypeToDB(final String mimeType) {
		try {
			{
				final PreparedStatement stmt = connection
						.prepareStatement("select mime_type_sk from mime_type where mime_type_txt = ?");
				try {
					stmt.setString(1, mimeType);
					final ResultSet rs = stmt.executeQuery();
					try {
						while (rs.next()) {
							return rs.getInt(1);
						}
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			}
			int newId = 1;
			{
				final String sql;
				if (sqlGrammar == SQLGrammar.ORACLE)
					sql = "select actuate_notify_admin.mime_type_seq.nextval from dual";
				else
					sql = "select nextval('mime_type_seq')";
				final PreparedStatement stmt = connection.prepareStatement(sql);
				try {
					final ResultSet rs = stmt.executeQuery();
					try {
						if (rs.next()) {
							newId = rs.getInt(1);
						}
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			}
			logger.log("Adding " + mimeType + " to database");
			final PreparedStatement stmt3 = connection
					.prepareStatement("insert into mime_type "
							+ "(mime_type_sk, mime_type_txt) values (?, ?)");
			try {
				stmt3.setInt(1, newId);
				stmt3.setString(2, mimeType);
				stmt3.execute();
			} finally {
				stmt3.close();
			}
			connection.commit();
			return newId;
		} catch (final SQLException e) {
			throw new RuntimeException("Unable to add mime type to database", e);
		}
	}

	public boolean notificationExistsInDB(final long jobId) {
		try {
			final PreparedStatement stmt = connection
					.prepareStatement("select actuate_notification_sk from actuate_notification where job_iid = ?");
			try {
				stmt.setLong(1, jobId);
				final ResultSet rs = stmt.executeQuery();
				try {
					if (rs.next())
						return true;
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
			connection.commit();
			return false;
		} catch (final SQLException e) {
			throw new RuntimeException(
					"Unable to check if notifications exist in the database", e);
		}
	}

	public int addNotificationToDB(final long jobId, final int senderPk,
			final int mimeTypePk, final String subject, final String body,
			final String fileName) {
		try {
			int newId = 1;
			{
				final String sql;
				if (sqlGrammar == SQLGrammar.ORACLE)
					sql = "select actuate_notify_admin.actuate_notification_seq.nextval from dual";
				else
					sql = "select nextval('actuate_notification_seq')";
				final PreparedStatement stmt = connection.prepareStatement(sql);
				try {
					final ResultSet rs = stmt.executeQuery();
					try {
						if (rs.next()) {
							newId = rs.getInt(1);
						}
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			}
			{
				final PreparedStatement stmt = connection
						.prepareStatement("insert into actuate_notification "
								+ "(actuate_notification_sk, job_iid, sender_email_address_sk, mime_type_sk, msg_sent_time, msg_subject_txt, msg_body_txt, rel_path_file_name) "
								+ "values (?, ?, ?, ?, ?, ?, ?, ?)");
				try {
					int i = 0;
					stmt.setInt(++i, newId);
					stmt.setLong(++i, jobId);
					stmt.setInt(++i, senderPk);
					stmt.setInt(++i, mimeTypePk);
					stmt.setDate(++i,
							new java.sql.Date(System.currentTimeMillis()));
					stmt.setString(++i, subject);
					stmt.setString(++i, body);
					stmt.setString(++i, fileName);
					stmt.execute();
				} finally {
					stmt.close();
				}
			}
			connection.commit();
			return newId;
		} catch (final SQLException e) {
			throw new RuntimeException(
					"Unable to add notification to database", e);
		}
	}

	public void addRecipientToDB(final int addressId, final int notificationId) {
		try {
			final PreparedStatement stmt = connection
					.prepareStatement("insert into actuate_notify_recipient "
							+ "(rcpnt_email_address_sk, actuate_notification_sk) values (?, ?)");
			try {
				stmt.setInt(1, addressId);
				stmt.setInt(2, notificationId);
				stmt.execute();
			} finally {
				stmt.close();
			}
			connection.commit();
		} catch (final SQLException e) {
			throw new RuntimeException("Unable to add recipient to database", e);
		}
	}

	public void close() {
		try {
			connection.close();
		} catch (final SQLException e) {
			throw new RuntimeException("Unable to close database", e);
		}
	}
}
