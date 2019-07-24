CREATE TABLE IF NOT EXISTS tbl_dashboard_connection (
	id                              TINYINT AUTO_INCREMENT NOT NULL,
	gateway_schema                  VARCHAR(5) NOT NULL,
	gateway_host                    VARCHAR(50) NOT NULL,
	gateway_port                    INTEGER NOT NULL,
	gateway_api                     VARCHAR(50),
	gateway_root_api                VARCHAR(50),
	nodered_schema                  VARCHAR(5) NOT NULL,
	nodered_host                    VARCHAR(50) NOT NULL,
	nodered_port                    INTEGER NOT NULL,
	created_at                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	modified_at                     TIMESTAMP NOT NULL DEFAULT 1,
	CONSTRAINT Pk_tbl_dashboard_connection PRIMARY KEY ( id )
 );
