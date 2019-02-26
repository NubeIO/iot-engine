CREATE SCHEMA IF NOT EXISTS "mock0";
SET SCHEMA "mock0";
CREATE TABLE IF NOT EXISTS tbl_sample_00 (
    id INT AUTO_INCREMENT,
    f_bool bit,
    f_date TIMESTAMP,
    f_str varchar(31),
    f_value_json text,
    CONSTRAINT Pk_tbl_sample PRIMARY KEY ( id )
);


CREATE SCHEMA IF NOT EXISTS "mock1";
SET SCHEMA "mock1";
CREATE TABLE IF NOT EXISTS tbl_sample_01 (
    id INT AUTO_INCREMENT,
    f_bool bit,
    f_date TIMESTAMP,
    f_str varchar(31),
    f_value_json text,
    f_timestamp timestamp,
    f_timestampz timestamp,
    f_date_1 date,
    f_time time,
    f_duration varchar(20),
    f_period varchar(20),
    CONSTRAINT Pk_tbl_sample_01 PRIMARY KEY ( id )
);
