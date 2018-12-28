CREATE TABLE IF NOT EXISTS tbl_ditto (
    id                   INT AUTO_INCREMENT,
	published_by         VARCHAR (255),
	created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	started_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	value                text,
	CONSTRAINT Pk_tbl_module PRIMARY KEY ( id )
 );
