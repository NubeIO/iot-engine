CREATE SCHEMA IF NOT EXISTS PUBLIC;

CREATE TABLE IF NOT EXISTS DEVICE (
	ID                   uuid   NOT NULL,
	CODE                 varchar(63)   NOT NULL,
	CUSTOMER_CODE        varchar(31)   NOT NULL,
	SITE_CODE            varchar(63)   NOT NULL,
	POLICY_ID            varchar(127)  NOT NULL,
    DATA_VERSION         varchar(15)   DEFAULT '0.0.1' NOT NULL,
	METADATA_JSON        clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT PK_DEVICE_ID PRIMARY KEY ( ID )
 );

COMMENT ON COLUMN DEVICE.CODE IS 'Device Code';
COMMENT ON COLUMN DEVICE.DATA_VERSION IS 'Legacy version: startswith 0.0.x. 0.0.1: default lowdb - 0.0.2: migrate point - 0.0.3: migrate equipment. 1.0.0: production with fully support `alert` and `schedule`';
COMMENT ON COLUMN DEVICE.METADATA_JSON IS 'Extra information';

CREATE TABLE IF NOT EXISTS NETWORK (
	ID                   uuid   NOT NULL,
	CODE                 varchar(63)   NOT NULL,
	DEVICE               uuid   NOT NULL,
	LABEL                varchar(1000)   ,
	METADATA_JSON        clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT PK_NETWORK_ID PRIMARY KEY ( ID ),
	CONSTRAINT IDX_UQ_NETWORK UNIQUE ( CODE, DEVICE )
 );

CREATE INDEX IDX_NETWORK_DEVICE ON PUBLIC.NETWORK ( DEVICE );

COMMENT ON COLUMN NETWORK.CODE IS 'Network Code should be subnet name or Network card interface name';

CREATE TABLE IF NOT EXISTS EQUIPMENT (
	ID                   uuid   NOT NULL,
	CODE                 varchar(63)   NOT NULL,
	LABEL                varchar(1000)   ,
	EQUIP_TYPE           varchar(63)   NOT NULL,
	MANUFACTURER         varchar(500)   ,
	METADATA_JSON        clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT PK_EQUIPMENT PRIMARY KEY ( ID ),
	CONSTRAINT IDX_UQ_EQUIPMENT UNIQUE ( CODE, EQUIP_TYPE )
 );

COMMENT ON TABLE EQUIPMENT IS 'Equipment';
COMMENT ON COLUMN EQUIPMENT.CODE IS 'Equipment code from manufacturer';
COMMENT ON COLUMN EQUIPMENT.EQUIP_TYPE IS 'For example: HVAC | Fire Detection | Lightning | Water';
COMMENT ON COLUMN EQUIPMENT.MANUFACTURER IS 'Manufacturing company';
COMMENT ON COLUMN EQUIPMENT.METADATA_JSON IS 'Extra information';

CREATE TABLE IF NOT EXISTS MEASURE_UNIT (
	MEASURE_TYPE         varchar(15)  NOT NULL,
	MEASURE_CATEGORY     varchar(31)  DEFAULT 'ALL' NOT NULL,
	SYMBOL               varchar(15)   ,
	LABEL                varchar(1000)   ,
	POSSIBLE_VALUES_JSON varchar(500)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT PK_MEASURE_UNIT PRIMARY KEY ( MEASURE_TYPE )
 );

CREATE INDEX IDX_MEASURE_UNIT_CATEGORY ON PUBLIC.MEASURE_UNIT ( MEASURE_CATEGORY );

CREATE TABLE IF NOT EXISTS TRANSDUCERS (
	ID                   uuid   NOT NULL,
	CODE                 varchar(63)   NOT NULL,
	TRANSDUCER_TYPE      varchar(15)  DEFAULT 'SENSOR' NOT NULL,
	TRANSDUCER_CATEGORY  varchar(31)   NOT NULL,
	LABEL                varchar(1000)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT PK_TRANSDUCER PRIMARY KEY ( ID ),
	CONSTRAINT IDX_UQ_TRANSDUCER UNIQUE ( CODE, TRANSDUCER_TYPE, TRANSDUCER_CATEGORY )
 );

COMMENT ON TABLE TRANSDUCERS IS 'Represents for a set of actual transducers that plugged in device point. Mean that one or many transducers from one or many equipment, no duplicate and must be same type and category.\nIt can be differenced by type: `SENSOR | ACTUATOR` and categorized by SENSOR kinds or ACTUATOR kinds';
COMMENT ON COLUMN TRANSDUCERS.CODE IS 'User define code. Default it will be';
COMMENT ON COLUMN TRANSDUCERS.TRANSDUCER_TYPE IS 'SENSOR | ACTUATOR';
COMMENT ON COLUMN TRANSDUCERS.TRANSDUCER_CATEGORY IS 'Transducer Category. It can be `TEMP`, `HUMIDITY`, `MOTION`, `VELOCITY`, etc';

CREATE TABLE IF NOT EXISTS DEVICE_EQUIP (
	ID                   bigint GENERATED ALWAYS AS IDENTITY  NOT NULL,
	DEVICE               uuid   NOT NULL,
	EQUIP                uuid   NOT NULL,
	NETWORK              uuid   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT IDX_UQ_DEVICE_EQUIP UNIQUE ( DEVICE, EQUIP ) ,
	CONSTRAINT PK_DEVICE_EQUIP PRIMARY KEY ( ID )
 );

CREATE INDEX IDX_FK_DEVICE_EQUIPMENT_DEVICE ON DEVICE_EQUIP ( DEVICE );

CREATE INDEX IDX_FK_DEVICE_EQUIP_EQUIP ON DEVICE_EQUIP ( EQUIP );

CREATE INDEX IDX_FK_DEVICE_EQUIP_NETWORK ON DEVICE_EQUIP ( NETWORK );

CREATE TABLE THING (
	ID                   integer GENERATED ALWAYS AS IDENTITY  NOT NULL,
	EQUIP                uuid   NOT NULL,
	TRANSDUCER           uuid   NOT NULL,
	PRODUCT_CODE         varchar(127)   ,
	PRODUCT_LABEL        varchar(1000)   ,
	MEASURE_UNIT         varchar(15)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT IDX_UQ_EQUIP_THING_CODE UNIQUE ( EQUIP, PRODUCT_CODE ) ,
	CONSTRAINT IDX_UQ_THING UNIQUE ( EQUIP, TRANSDUCER ) ,
	CONSTRAINT PK_THING PRIMARY KEY ( ID )
 );

CREATE INDEX IDX_FK_THING_EQUIP ON THING ( EQUIP );

CREATE INDEX IDX_FK_THING_MEASURE ON THING ( MEASURE_UNIT );

CREATE INDEX IDX_FK_THING_TRANSDUCER ON THING ( TRANSDUCER );

COMMENT ON TABLE THING IS 'Real Thing that represents from Equipment and Transducer';
COMMENT ON COLUMN THING.PRODUCT_CODE IS 'Manufacturer product transducer code depends on equipment';
COMMENT ON COLUMN THING.PRODUCT_LABEL IS 'Manufacturer transducer label depends on equipment';
COMMENT ON COLUMN THING.MEASURE_UNIT IS 'Standard manufacturer transducer measure unit';

CREATE TABLE IF NOT EXISTS POINT ( 
	ID                   uuid   NOT NULL,
	CODE                 varchar(63)   NOT NULL,
	DEVICE               uuid   NOT NULL,
	TRANSDUCER           uuid   ,
	NETWORK              uuid   ,
	LABEL                varchar(1000)   ,
	ENABLED              boolean  DEFAULT TRUE NOT NULL,
	POINT_CATEGORY       varchar(31)  DEFAULT 'UNKNOWN' NOT NULL,
	POINT_KIND           varchar(15)  DEFAULT 'UNKNOWN' NOT NULL,
	POINT_TYPE           varchar(31)  DEFAULT 'UNKNOWN' NOT NULL,
	MEASURE_UNIT         varchar(15)  NOT NULL,
	MIN_SCALE            smallint  DEFAULT 0 NOT NULL,
	MAX_SCALE            smallint  DEFAULT 10 NOT NULL,
	PRECISION            smallint  DEFAULT 3 NOT NULL,
	OFFSET_VAL           smallint  DEFAULT 0 NOT NULL,
	VERSION              varchar(31)   ,
	METADATA_JSON        clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT PK_POINT PRIMARY KEY ( ID ),
	CONSTRAINT IDX_UQ_POINT UNIQUE ( CODE, DEVICE, NETWORK )
 );

CREATE INDEX IDX_FK_POINT_MEASURE_UNIT ON POINT ( MEASURE_UNIT );

CREATE INDEX IDX_FK_POINT_TRANSDUCER ON POINT ( TRANSDUCER );

CREATE INDEX IDX_FK_POINT_DEVICE ON POINT ( DEVICE );

CREATE INDEX IDX_FK_POINT_NETWORK ON POINT ( NETWORK );

COMMENT ON TABLE POINT IS 'Represents for:\n- Edge device pin if category is GPIO\n- Virtual point if category is not GPIO';
COMMENT ON COLUMN POINT.POINT_CATEGORY IS 'One of BACNET | GPIO | MODBUS | UNKNOWN';
COMMENT ON COLUMN POINT.POINT_KIND IS 'INPUT|OUTPUT|UNKNOWN';
COMMENT ON COLUMN POINT.POINT_TYPE IS 'ANALOG | DIGITAL | DC_10 | DC_12 | MA_20 | THERMISTOR_10K';

CREATE TABLE IF NOT EXISTS POINT_HISTORY_DATA (
	ID                   bigint GENERATED ALWAYS AS IDENTITY  NOT NULL,
	POINT                uuid   NOT NULL,
	TIME                 timestamp   NOT NULL,
	VALUE                double   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT IDX_UQ_POINT_HISTORY_DATA UNIQUE ( POINT, TIME ) ,
	CONSTRAINT PK_POINT_HISTORY_DATA PRIMARY KEY ( ID )
 );

CREATE INDEX IDX_FK_HISTORY_DATA_POINT ON POINT_HISTORY_DATA ( POINT );

CREATE INDEX IDX_POINT_HISTORY_DATA_TIME ON POINT_HISTORY_DATA ( TIME );

CREATE TABLE IF NOT EXISTS POINT_REALTIME_DATA ( 
	ID                   bigint GENERATED ALWAYS AS IDENTITY  NOT NULL,
	POINT                uuid   NOT NULL,
	TIME                 timestamp   NOT NULL,
	VALUE_JSON           clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT IDX_UQ_POINT_REALTIME_DATA UNIQUE ( POINT, TIME ) ,
	CONSTRAINT PK_POINT_REALTIME_DATA PRIMARY KEY ( ID )
 );

CREATE INDEX IDX_FK_POINT_REALTIME_DATA_POINT ON POINT_REALTIME_DATA ( POINT );

CREATE INDEX IDX_POINT_REALTIME_DATA_TIME ON POINT_REALTIME_DATA ( TIME );

CREATE TABLE IF NOT EXISTS POINT_TAG ( 
	ID                   bigint GENERATED ALWAYS AS IDENTITY  NOT NULL,
	TAG_NAME             varchar(63)   NOT NULL,
	POINT                uuid   NOT NULL,
	TAG_VALUE            varchar(255)   NOT NULL,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT IDX_UQ_POINT_TAG UNIQUE ( TAG_NAME, POINT ) ,
	CONSTRAINT PK_POINT_TAG PRIMARY KEY ( ID )
 );

CREATE INDEX IDX_FK_TAG_POINT ON POINT_TAG ( POINT );

CREATE INDEX IDX_TAG_BY_POINT ON POINT_TAG ( TAG_NAME );

CREATE TABLE IF NOT EXISTS POINT_VALUE_DATA ( 
	POINT                uuid   NOT NULL,
	VALUE                double   ,
	PRIORITY             smallint   NOT NULL,
	PRIORITY_VALUES_JSON clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT PK_POINT_VALUE_DATA PRIMARY KEY ( POINT )
 );

CREATE TABLE IF NOT EXISTS REALTIME_SETTING ( 
	POINT                uuid   NOT NULL,
	ENABLED              boolean  DEFAULT FALSE NOT NULL,
	TOLERANCE            double   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT PK_REALTIME_SETTING PRIMARY KEY ( POINT )
 );

CREATE TABLE IF NOT EXISTS SCHEDULE_SETTING ( 
	ID                   uuid   NOT NULL,
	POINT                uuid   NOT NULL,
	RECURRING            boolean  DEFAULT TRUE NOT NULL,
	NAME                 varchar(63)   NOT NULL,
	COLOR                varchar(15)  DEFAULT '#FFFFFF' NOT NULL,
	START_TIME           timestamp   NOT NULL,
	END_TIME             timestamp   NOT NULL,
	VALUE                double   NOT NULL,
	WEEKDAYS             array   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
	CONSTRAINT PK_SCHEDULE_SETTING PRIMARY KEY ( ID )
 );

CREATE INDEX IDX_FK_SCHEDULE_POINT ON SCHEDULE_SETTING ( POINT );

CREATE TABLE IF NOT EXISTS HISTORY_SETTING ( 
	POINT                uuid   NOT NULL,
	HISTORY_SETTING_TYPE varchar(15)   ,
	SCHEDULE             varchar(127)   ,
	TOLERANCE            double   ,
	SIZE                 integer   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           varchar(500)   ,
    CONSTRAINT PK_HISTORY_SETTING PRIMARY KEY ( POINT )
);

-- ALTER FK
ALTER TABLE DEVICE_EQUIP ADD CONSTRAINT FK_DEVICE_EQUIPMENT_DEVICE FOREIGN KEY ( DEVICE ) REFERENCES DEVICE( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE DEVICE_EQUIP ADD CONSTRAINT FK_DEVICE_EQUIP_EQUIP FOREIGN KEY ( EQUIP ) REFERENCES EQUIPMENT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE DEVICE_EQUIP ADD CONSTRAINT FK_DEVICE_EQUIP_NETWORK FOREIGN KEY ( NETWORK ) REFERENCES NETWORK( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE HISTORY_SETTING ADD CONSTRAINT FK_HISTORY_SETTING_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE NETWORK ADD CONSTRAINT FK_NETWORK_DEVICE FOREIGN KEY ( DEVICE ) REFERENCES DEVICE( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE POINT ADD CONSTRAINT FK_POINT_DEVICE FOREIGN KEY ( DEVICE ) REFERENCES DEVICE( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE POINT ADD CONSTRAINT FK_POINT_NETWORK FOREIGN KEY ( NETWORK ) REFERENCES NETWORK( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE POINT ADD CONSTRAINT FK_POINT_MEASURE_UNIT FOREIGN KEY ( MEASURE_UNIT ) REFERENCES MEASURE_UNIT( MEASURE_TYPE ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE POINT ADD CONSTRAINT FK_POINT_TRANSDUCER FOREIGN KEY ( TRANSDUCER ) REFERENCES TRANSDUCERS( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE POINT_HISTORY_DATA ADD CONSTRAINT FK_HISTORY_DATA_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE POINT_REALTIME_DATA ADD CONSTRAINT FK_POINT_REALTIME_DATA_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE POINT_TAG ADD CONSTRAINT FK_TAG_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE POINT_VALUE_DATA ADD CONSTRAINT FK_VALUE_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE REALTIME_SETTING ADD CONSTRAINT FK_REALTIME_SETTING_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE SCHEDULE_SETTING ADD CONSTRAINT FK_SCHEDULE_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE THING ADD CONSTRAINT FK_THING_EQUIP FOREIGN KEY ( EQUIP ) REFERENCES EQUIPMENT( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE THING ADD CONSTRAINT FK_THING_MEASURE FOREIGN KEY ( MEASURE_UNIT ) REFERENCES MEASURE_UNIT( MEASURE_TYPE ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE THING ADD CONSTRAINT FK_THING_TRANSDUCER FOREIGN KEY ( TRANSDUCER ) REFERENCES TRANSDUCERS( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;
