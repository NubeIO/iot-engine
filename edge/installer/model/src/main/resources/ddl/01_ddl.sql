CREATE TABLE IF NOT EXISTS application (
	app_id                          varchar(127) NOT NULL,
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
	CONSTRAINT Pk_application PRIMARY KEY ( app_id ),
	CONSTRAINT Unique_application UNIQUE ( service_name, service_type )
 );

CREATE TABLE IF NOT EXISTS deploy_transaction (
	transaction_id                  varchar(63) NOT NULL,
	app_id                          varchar(127) NOT NULL,
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
	CONSTRAINT Pk_deploy_transaction PRIMARY KEY ( transaction_id ),
	FOREIGN KEY ( app_id ) REFERENCES application( app_id )
 );

CREATE INDEX IF NOT EXISTS Idx_deploy_transaction_app_id ON deploy_transaction ( app_id );

CREATE INDEX IF NOT EXISTS Idx_deploy_transaction_module_lifetime ON deploy_transaction ( app_id, issued_at );

CREATE TABLE IF NOT EXISTS application_history (
	transaction_id                  varchar(63) NOT NULL,
	app_id                          varchar(127) NOT NULL,
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
	CONSTRAINT Pk_application_history PRIMARY KEY ( transaction_id )
 );

 CREATE TABLE IF NOT EXISTS APPLICATION_BACKUP (
	ID                      uuid   NOT NULL,
	APP_ID                  varchar(127)   ,
	STATUS                  varchar(15) NOT NULL,
	DATA_DIR_JSON           text,
	INSTALLATION_DIR_JSON   text,
	ERROR_JSON              text,
	TIME_AUDIT              varchar(500)   ,
	SYNC_AUDIT              clob(2147483647)   ,
	CONSTRAINT PK_APPLICATION_BACKUP PRIMARY KEY ( ID )
 );
