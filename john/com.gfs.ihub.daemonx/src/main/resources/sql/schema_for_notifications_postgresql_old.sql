drop table email_addresses;
drop table format_types;
drop table actu_notification_log;
drop table recipients;
drop table attachments;

DROP SEQUENCE public.actu_notification_log_seq;
DROP SEQUENCE public.attachments_seq;
DROP SEQUENCE public.email_addresses_seq;
DROP SEQUENCE public.format_types_seq;

create table email_addresses (
                email_addresses_pk integer not null,
                email_address varchar(100) not null,
                constraint email_addresses_pk primary key (email_addresses_pk)
);

create table format_types (
                format_type_pk integer not null,
                format_type varchar(100) not null,
                constraint format_types_pk primary key (format_type_pk)
);

create table actu_notification_log (
                actu_notification_log_pk integer not null,
                job_id varchar(100) not null,
                sender_email_address_pk integer not null,
                subject_param varchar(256) not null,
                sent_time timestamp not null,
                body_param varchar(1024) not null,
                constraint actu_notification_log_pk primary key (actu_notification_log_pk)
);

create table recipients (
                email_addresses_pk integer not null,
                actu_notification_log_pk integer not null,
                constraint recipients_pk primary key (email_addresses_pk, actu_notification_log_pk)
);

create table attachments (
                attachments_pk integer not null,
                actu_notification_log_pk integer not null,
                /* format_param integer not null, */
                format_type_pk integer not null,
                attachment bytea not null,
                constraint attachments_pk primary key (attachments_pk, actu_notification_log_pk, /* format_param, */ format_type_pk)
);

alter table recipients add constraint email_addresses_email_2_not57
foreign key (email_addresses_pk)
references email_addresses (email_addresses_pk)
not deferrable;

alter table actu_notification_log add constraint email_addresses_actu_notification_log_fk
foreign key (sender_email_address_pk)
references email_addresses (email_addresses_pk)
not deferrable;

alter table attachments add constraint format_types_attachments_fk
foreign key (format_type_pk)
references format_types (format_type_pk)
not deferrable;

alter table attachments add constraint actu_notification_log_attac63
foreign key (actu_notification_log_pk)
references actu_notification_log (actu_notification_log_pk)
not deferrable;

alter table recipients add constraint actu_notification_log_email566
foreign key (actu_notification_log_pk)
references actu_notification_log (actu_notification_log_pk)
not deferrable;

create sequence email_addresses_seq
start with 1
increment by 1
no maxvalue;

create sequence format_types_seq
start with 1
increment by 1
no maxvalue;

create sequence actu_notification_log_seq
start with 1
increment by 1
no maxvalue;

create sequence attachments_seq
start with 1
increment by 1
no maxvalue;
