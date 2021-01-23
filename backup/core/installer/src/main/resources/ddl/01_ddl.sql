CREATE TABLE IF NOT EXISTS tbl_module (
	service_id                      varchar(127) NOT NULL,
	service_name                    varchar(127) NOT NULL,
	service_type                    varchar(15) NOT NULL,
	version                         varchar(31) NOT NULL,
	published_by                    varchar(255),
	state                           varchar(15) NOT NULL,
	created_at                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	modified_at                     TIMESTAMP NOT NULL DEFAULT 1,
	deploy_id                       varchar(255),
	app_config_json                 text,
	system_config_json              text,
	deploy_location                 varchar(500),
	CONSTRAINT Pk_tbl_module PRIMARY KEY ( service_id ),
	CONSTRAINT Unique_tbl_module UNIQUE ( service_name, service_type )
 );

CREATE TABLE IF NOT EXISTS tbl_transaction (
	transaction_id                  varchar(63) NOT NULL,
	module_id                       varchar(127) NOT NULL,
	event                           varchar(15) NOT NULL,
	status                          varchar(15) NOT NULL,
	issued_at                       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	issued_by                       varchar(127),
	issued_from                     varchar(63),
	modified_at                     TIMESTAMP NOT NULL DEFAULT 1,
	prev_metadata_json              text,
	prev_app_config_json            text,
	prev_system_config_json         text,
	last_error_json                 text,
	retry                           integer NOT NULL DEFAULT 0,
	CONSTRAINT Pk_tbl_transaction PRIMARY KEY ( transaction_id ),
	FOREIGN KEY ( module_id ) REFERENCES tbl_module( service_id )
 );

CREATE INDEX IF NOT EXISTS Idx_tbl_transaction_module_id ON tbl_transaction ( module_id );

CREATE INDEX IF NOT EXISTS Idx_tbl_transaction_module_lifetime ON tbl_transaction ( module_id, issued_at );

CREATE TABLE IF NOT EXISTS tbl_remove_history (
	transaction_id                  varchar(63) NOT NULL,
	module_id                       varchar(127) NOT NULL,
	event                           varchar(15) NOT NULL,
	status                          varchar(15) NOT NULL,
	issued_at                       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	issued_by                       varchar(127),
	issued_from                     varchar(63),
	modified_at                     TIMESTAMP NOT NULL DEFAULT 1,
	prev_metadata_json              text,
	prev_app_config_json            text,
	prev_system_config_json         text,
	retry                           integer NOT NULL DEFAULT 0,
	CONSTRAINT Pk_tbl_remove_history PRIMARY KEY ( transaction_id )
 );
