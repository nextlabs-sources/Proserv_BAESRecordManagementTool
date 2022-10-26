CREATE TABLESPACE tbs_rmt_per
  DATAFILE 'tbs_rmt_per.dat' 
    SIZE 40M AUTOEXTEND ON
  ONLINE;  

CREATE TEMPORARY TABLESPACE tbs_rmt_temp
  TEMPFILE 'tbs_rmt_temp.dbf'
    SIZE 20M
    AUTOEXTEND ON;                        

CREATE USER rmtadmin
  IDENTIFIED BY Nextlabs123
  DEFAULT TABLESPACE tbs_rmt_per
  TEMPORARY TABLESPACE tbs_rmt_temp
  QUOTA unlimited on tbs_rmt_per;
  

GRANT create session TO rmtadmin;
GRANT create table TO rmtadmin;
GRANT create view TO rmtadmin;
GRANT create any trigger TO rmtadmin;
GRANT create any procedure TO rmtadmin;
GRANT create sequence TO rmtadmin;
GRANT create synonym TO rmtadmin;