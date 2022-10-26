CREATE TABLE department_of_record (
	name varchar2(50) not null,
	functionalMailbox varchar2(255),
	CONSTRAINT departmentOfRecord_pk PRIMARY KEY (name)
);

CREATE TABLE activity (
	id varchar2(36) not null,
	name varchar2(255) not null,
	recordOfficersGuidance varchar2(2000),
	active number DEFAULT 1,
	CONSTRAINT activity_pk PRIMARY KEY (id)
);

CREATE TABLE record_category (
	recordCategory varchar2(50) not null,
	categoryName varchar2(255),
	recordType varchar2(50) not null,
	recordActivity varchar2(36),
	retentionPeriod number,
	departmentOfRecord varchar2(50),
	autoDelete number DEFAULT 0,
	comments varchar2(1000),
	active number DEFAULT 1,
	CONSTRAINT record_category_pk PRIMARY KEY (recordCategory),	
	CONSTRAINT record_category_activity_fk
			FOREIGN KEY (recordActivity) 
			REFERENCES activity(id),
	CONSTRAINT record_category_department_fk
			FOREIGN KEY (departmentOfRecord)
			REFERENCES department_of_record(name)
);

CREATE TABLE activity_detail (
	id varchar2(36) not null,
	activity varchar2(36) not null,
	titleValue varchar2(50),
	referenceValue  varchar2(50),
	dateValue timestamp,
	CONSTRAINT activity_detail_pk PRIMARY KEY (id),
	CONSTRAINT activity_fk
			FOREIGN KEY (activity)
			REFERENCES activity(id)
);

CREATE TABLE log (
	id varchar2(36) not null,
	time timestamp not null,
	admin varchar2(255) not null,
	target varchar2(255) not null,
	targetType varchar2(255) not null,
	action varchar2(255) not null,
	oldvalue varchar2(1000),
	newvalue varchar2(1000),
	extraDetails varchar2(4000),
	CONSTRAINT log_pk PRIMARY KEY (id)	
);

