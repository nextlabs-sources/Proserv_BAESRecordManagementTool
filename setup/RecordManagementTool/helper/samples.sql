INSERT INTO department(name, functionalMailbox) values ('Commercial', 'Record Officer.Commercial@baesystems.com');
INSERT INTO department(name, functionalMailbox) values ('Legal', '');

INSERT INTO activity (id, name, departmentOfRecord) values ('111111111111111111111111111111111111', 'Prime Contract End of Life', 'Commercial');
INSERT INTO activity (id, name, departmentOfRecord) values ('222222222222222222222222222222222222','Case Reference Closure', 'Legal');

INSERT INTO record_category (recordCategory, recordType, recordActivity, retentionPeriod, departmentOfRecord) values ('1.2.1', 'Complex','111111111111111111111111111111111111', '3', 'Commercial');
INSERT INTO record_category (recordCategory, recordType, recordActivity, retentionPeriod, departmentOfRecord) values ('1.3.1', 'Complex','111111111111111111111111111111111111', '12', 'Commercial');
INSERT INTO record_category (recordCategory, recordType, recordActivity, retentionPeriod, departmentOfRecord) values ('8.1', 'Complex','222222222222222222222222222222222222', '6', 'Legal');

INSERT INTO activity_detail (id, activity, titleValue, referenceValue) values ('9bcbe17a-9426-43bd-a6d3-17fbbf8405ba', '111111111111111111111111111111111111', 'title', 'reference');

commit;