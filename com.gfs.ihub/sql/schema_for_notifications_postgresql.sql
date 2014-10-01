/* EMAIL_ACCOUNT */
CREATE TABLE EMAIL_ACCOUNT 
(
   EMAIL_ADDRESS_SK INTEGER NOT NULL,
   EMAIL_ADDRESS VARCHAR(160) NOT NULL
);

ALTER TABLE EMAIL_ACCOUNT ADD 
  CONSTRAINT EMAIL_ACCOUNT_PK
  PRIMARY KEY (EMAIL_ADDRESS_SK);
  
CREATE UNIQUE INDEX EMAIL_ACCOUNT_UK1 ON EMAIL_ACCOUNT
(EMAIL_ADDRESS);
 
CREATE SEQUENCE EMAIL_ACCOUNT_SEQ
START WITH 1
MAXVALUE 9999999999
MINVALUE 0
CACHE 20;


/* MIME_TYPE */
CREATE TABLE MIME_TYPE
(
   MIME_TYPE_SK INTEGER NOT NULL,
   MIME_TYPE_TXT VARCHAR(200) NOT NULL
);

ALTER TABLE MIME_TYPE ADD
  CONSTRAINT MIME_TYPE_PK
  PRIMARY KEY (MIME_TYPE_SK);

CREATE UNIQUE INDEX MIME_TYPE_UK1 ON MIME_TYPE
(MIME_TYPE_TXT);

CREATE SEQUENCE MIME_TYPE_SEQ
START WITH 1
MAXVALUE 9999999999
MINVALUE 0
CACHE 20;


/* ACTUATE_NOTIFICATION */
CREATE TABLE ACTUATE_NOTIFICATION
(
   ACTUATE_NOTIFICATION_SK INTEGER NOT NULL,
   JOB_IID INTEGER NOT NULL,
   SENDER_EMAIL_ADDRESS_SK INTEGER NOT NULL,
   MIME_TYPE_SK INTEGER NOT NULL,
   MSG_SENT_TIME TIMESTAMP(6) WITH TIME ZONE NOT NULL,
   MSG_SUBJECT_TXT VARCHAR(4000) NOT NULL,
   MSG_BODY_TXT VARCHAR(4000) NOT NULL,
   REL_PATH_FILE_NAME VARCHAR(4000) NOT NULL
);

ALTER TABLE ACTUATE_NOTIFICATION ADD
  CONSTRAINT ACTUATE_NOTIFICATION_PK
  PRIMARY KEY (ACTUATE_NOTIFICATION_SK);
  
CREATE UNIQUE INDEX ACTUATE_NOTIFICATION_UK1 ON ACTUATE_NOTIFICATION
(JOB_IID);

ALTER TABLE ACTUATE_NOTIFICATION ADD
  CONSTRAINT FK_EMAIL_ACCOUNT_01
  FOREIGN KEY (SENDER_EMAIL_ADDRESS_SK)
  REFERENCES EMAIL_ACCOUNT(EMAIL_ADDRESS_SK);

CREATE INDEX ACTUATE_NOTIFICATION_IX02
  ON ACTUATE_NOTIFICATION(SENDER_EMAIL_ADDRESS_SK);
  
ALTER TABLE ACTUATE_NOTIFICATION ADD
  CONSTRAINT FK_MIME_TYPE_01
  FOREIGN KEY (MIME_TYPE_SK)
  REFERENCES MIME_TYPE(MIME_TYPE_SK);
  
CREATE INDEX MIME_TYPE_IX03
  ON ACTUATE_NOTIFICATION(MIME_TYPE_SK);

CREATE SEQUENCE ACTUATE_NOTIFICATION_SEQ
START WITH 1
MAXVALUE 9999999999
MINVALUE 0
CACHE 20;


/* ACTUATE_NOTIFY_RECIPIENT */
CREATE TABLE ACTUATE_NOTIFY_RECIPIENT 
(
   RCPNT_EMAIL_ADDRESS_SK INTEGER NOT NULL,
   ACTUATE_NOTIFICATION_SK INTEGER NOT NULL
);

ALTER TABLE ACTUATE_NOTIFY_RECIPIENT ADD
  CONSTRAINT ACTUATE_NOTIFY_RECIPIENT_PK
  PRIMARY KEY (RCPNT_EMAIL_ADDRESS_SK, ACTUATE_NOTIFICATION_SK);

ALTER TABLE ACTUATE_NOTIFY_RECIPIENT ADD
  CONSTRAINT FK_EMAIL_ACCOUNT_02
  FOREIGN KEY (RCPNT_EMAIL_ADDRESS_SK)
  REFERENCES EMAIL_ACCOUNT(EMAIL_ADDRESS_SK);

CREATE INDEX ACTUATE_NOTIFY_RECIPIENT_IX01
  ON ACTUATE_NOTIFY_RECIPIENT(RCPNT_EMAIL_ADDRESS_SK);

ALTER TABLE ACTUATE_NOTIFY_RECIPIENT ADD
  CONSTRAINT FK_ACTUATE_NOTIFICATION_02
  FOREIGN KEY (ACTUATE_NOTIFICATION_SK)
  REFERENCES ACTUATE_NOTIFICATION(ACTUATE_NOTIFICATION_SK);

CREATE INDEX ACTUATE_NOTIFY_RECIPIENT_IX02
  ON ACTUATE_NOTIFY_RECIPIENT(ACTUATE_NOTIFICATION_SK);