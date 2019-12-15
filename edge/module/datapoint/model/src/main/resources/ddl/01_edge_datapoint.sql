CREATE SCHEMA IF NOT EXISTS PUBLIC;

CREATE TABLE IF NOT EXISTS EDGE (
	ID                   uuid          NOT NULL,
	CODE                 varchar(63)   NOT NULL,
	CUSTOMER_CODE        varchar(31)   NOT NULL,
	SITE_CODE            varchar(63)   NOT NULL,
	MODEL                varchar(255)  DEFAULT 'Nube EdgeIO-28' NOT NULL,
	FIRMWARE_VERSION     varchar(127)  DEFAULT 'v2' NOT NULL,
	OS_VERSION           varchar(127)   ,
	SOFTWARE_VERSION     varchar(127)   ,
	DATA_VERSION         varchar(15)  DEFAULT '0.0.2' NOT NULL,
	METADATA_JSON        clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT PK_EDGE_ID PRIMARY KEY ( ID )
 );

COMMENT ON COLUMN EDGE.CODE IS 'Edge Code';
COMMENT ON COLUMN EDGE.DATA_VERSION IS 'Legacy version: startswith 0.0.x. 0.0.1: default lowdb - 0.0.2: migrate point - 0.0.3: migrate equipment. 1.0.0: production with fully support `alert` and `schedule`';
COMMENT ON COLUMN EDGE.METADATA_JSON IS 'Extra information';

CREATE TABLE IF NOT EXISTS NETWORK (
	ID                   uuid           NOT NULL,
	CODE                 varchar(63)    NOT NULL,
	EDGE                 uuid           NOT NULL,
	PROTOCOL             varchar(31)  DEFAULT 'UNKNOWN' NOT NULL,
	STATE                varchar(31)  DEFAULT 'NONE' NOT NULL,
	LABEL                varchar(1000)   ,
	METADATA_JSON        clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT PK_NETWORK_ID PRIMARY KEY ( ID ),
	CONSTRAINT IDX_UQ_NETWORK UNIQUE ( CODE, EDGE, PROTOCOL )
 );

CREATE INDEX IDX_NETWORK_EDGE_PROTOCOL ON NETWORK ( EDGE, PROTOCOL );

COMMENT ON COLUMN NETWORK.CODE IS 'Network Code should be subnet name or Network card interface name';

CREATE TABLE IF NOT EXISTS DEVICE (
	ID                   uuid          NOT NULL,
	CODE                 varchar(63)   NOT NULL,
	DEVICE_TYPE          varchar(63)   NOT NULL,
	PROTOCOL             varchar(31)   DEFAULT 'UNKNOWN' NOT NULL,
	STATE                varchar(31)   DEFAULT 'NONE' NOT NULL,
	NAME                 varchar(127)   ,
	MANUFACTURER         varchar(500)   ,
	MODEL                varchar(255)   ,
	FIRMWARE_VERSION     varchar(127)   ,
	SOFTWARE_VERSION     varchar(127)   ,
	LABEL                varchar(1000)  ,
	METADATA_JSON        clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT PK_DEVICE PRIMARY KEY ( ID ),
	CONSTRAINT IDX_UQ_DEVICE UNIQUE ( CODE, DEVICE_TYPE )
 );

COMMENT ON TABLE DEVICE IS 'Remote Device is connected and managed by NubeIO Edge';
COMMENT ON COLUMN DEVICE.CODE IS 'Device code that is identified in communication protocol network';
COMMENT ON COLUMN DEVICE.DEVICE_TYPE IS 'For example: MACHINE | GATEWAY | EQUIPMENT';
COMMENT ON COLUMN DEVICE.MANUFACTURER IS 'Manufacturing company';
COMMENT ON COLUMN DEVICE.METADATA_JSON IS 'Extra information';

CREATE TABLE IF NOT EXISTS MEASURE_UNIT (
	MEASURE_TYPE         varchar(63)  NOT NULL,
	MEASURE_CATEGORY     varchar(63)  DEFAULT 'ALL' NOT NULL,
	SYMBOL               varchar(15)   ,
	LABEL                varchar(1000)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT PK_MEASURE_UNIT PRIMARY KEY ( MEASURE_TYPE )
 );

CREATE INDEX IDX_MEASURE_UNIT_CATEGORY ON MEASURE_UNIT ( MEASURE_CATEGORY );

CREATE TABLE IF NOT EXISTS THING (
	ID                   uuid   NOT NULL,
	CODE                 varchar(127)   ,
	DEVICE_ID            uuid   NOT NULL,
	THING_TYPE           varchar(15)  DEFAULT 'SENSOR' NOT NULL,
	THING_CATEGORY       varchar(63)   NOT NULL,
	LABEL                varchar(1000)   ,
	MEASURE_UNIT         varchar(63)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT PK_THING PRIMARY KEY ( ID ),
	CONSTRAINT IDX_UQ_THING UNIQUE ( CODE, DEVICE_ID )
 );

CREATE INDEX IDX_THING_DEVICE ON THING ( DEVICE_ID );

CREATE INDEX IDX_THING_TYPE ON THING ( THING_TYPE );

CREATE INDEX IDX_THING_CATEGORY ON THING ( THING_CATEGORY );

COMMENT ON TABLE THING IS 'Real physical Thing/Transducer in Device';
COMMENT ON COLUMN THING.CODE IS 'Manufacturer product transducer code depends on device';
COMMENT ON COLUMN THING.THING_TYPE IS 'SENSOR | ACTUATOR';
COMMENT ON COLUMN THING.THING_CATEGORY IS 'Thing Category depends on Thing Type. It can be `TEMP`, `HUMIDITY`, `MOTION`, `VELOCITY`, etc';
COMMENT ON COLUMN THING.LABEL IS 'Manufacturer transducer label depends on device';
COMMENT ON COLUMN THING.MEASURE_UNIT IS 'Standard manufacturer transducer measure unit';


CREATE TABLE IF NOT EXISTS EDGE_DEVICE (
	ID                   bigint GENERATED ALWAYS AS IDENTITY  NOT NULL,
	EDGE_ID              uuid   NOT NULL,
	DEVICE_ID            uuid   NOT NULL,
	NETWORK_ID           uuid   ,
	ADDRESS_JSON         clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT IDX_UQ_EDGE_DEVICE UNIQUE ( EDGE_ID, DEVICE_ID ) ,
	CONSTRAINT PK_EDGE_DEVICE PRIMARY KEY ( ID )
 );

CREATE INDEX IDX_FK_EDGE_DEVICE_EDGE ON EDGE_DEVICE ( EDGE_ID );

CREATE INDEX IDX_FK_EDGE_DEVICE_DEVICE ON EDGE_DEVICE ( DEVICE_ID );

CREATE INDEX IDX_FK_EDGE_DEVICE_NETWORK ON EDGE_DEVICE ( NETWORK_ID );

CREATE TABLE IF NOT EXISTS POINT ( 
	ID                   uuid   NOT NULL,
	CODE                 varchar(63)   NOT NULL,
	EDGE                 uuid   NOT NULL,
	NETWORK              uuid   ,
	LABEL                varchar(1000)   ,
	ENABLED              boolean  DEFAULT TRUE NOT NULL,
	PROTOCOL             varchar(31)  DEFAULT 'UNKNOWN' NOT NULL,
	POINT_KIND           varchar(15)  DEFAULT 'UNKNOWN' NOT NULL,
	POINT_TYPE           varchar(31)  DEFAULT 'UNKNOWN' NOT NULL,
	MEASURE_UNIT         varchar(63)  NOT NULL,
	MEASURE_UNIT_ALIAS   varchar(500)   ,
	MIN_SCALE            smallint,
	MAX_SCALE            smallint,
	PRECISION            smallint,
	OFFSET_VAL           smallint,
	VERSION              varchar(31)   ,
	METADATA_JSON        clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT PK_POINT PRIMARY KEY ( ID ),
	CONSTRAINT IDX_UQ_POINT UNIQUE ( CODE, EDGE, NETWORK )
 );

CREATE INDEX IDX_FK_POINT_MEASURE_UNIT ON POINT ( MEASURE_UNIT );

CREATE INDEX IDX_FK_POINT_EDGE ON POINT ( EDGE );

CREATE INDEX IDX_FK_POINT_NETWORK ON POINT ( NETWORK );

COMMENT ON TABLE POINT IS 'Represents for:\n- Edge PIN if category is GPIO\n- Virtual point if category is not GPIO';
COMMENT ON COLUMN POINT.PROTOCOL IS 'One of BACNET | GPIO | MODBUS | UNKNOWN';
COMMENT ON COLUMN POINT.POINT_KIND IS 'INPUT|OUTPUT|UNKNOWN';
COMMENT ON COLUMN POINT.POINT_TYPE IS 'ANALOG | DIGITAL | DC_10 | DC_12 | MA_20 | THERMISTOR_10K';

CREATE TABLE IF NOT EXISTS POINT_THING (
	ID                   bigint GENERATED ALWAYS AS IDENTITY  NOT NULL,
	POINT_ID             uuid   NOT NULL,
	THING_ID             uuid   NOT NULL,
	COMPUTED_THING       varchar(63)   ,
	DEVICE_ID            uuid,
	NETWORK_ID           uuid,
	EDGE_ID              uuid,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT PK_POINT_THING PRIMARY KEY ( ID ),
	CONSTRAINT IDX_UQ_POINT_THING UNIQUE ( POINT_ID, THING_ID ) ,
	CONSTRAINT IDX_UQ_POINT_THING_COMPUTED UNIQUE ( COMPUTED_THING )
 );

CREATE INDEX IDX_POINT_THING_POINT ON POINT_THING ( POINT_ID );

CREATE INDEX IDX_POINT_THING_THING ON POINT_THING ( THING_ID );

COMMENT ON TABLE POINT_THING IS 'Represents for Point is attached to one and more specific Things.\nThing with type is SENSOR is attached to only one Point. It will be validated by COMPUTED_THING field';
COMMENT ON COLUMN POINT_THING.COMPUTED_THING IS 'Computed Thing Id and Thing Type to ensure unique sensor thing';
COMMENT ON COLUMN POINT_THING.EDGE_ID IS 'Egde value is inherited from Point';
COMMENT ON COLUMN POINT_THING.COMPUTED_THING IS 'Network value is inherited from Point';
COMMENT ON COLUMN POINT_THING.NETWORK_ID IS 'Network value is inherited from Point';
COMMENT ON COLUMN POINT_THING.DEVICE_ID IS 'Device value is inherited from Thing';


CREATE TABLE IF NOT EXISTS POINT_HISTORY_DATA (
	ID                   bigint GENERATED ALWAYS AS IDENTITY  NOT NULL,
	POINT                uuid   NOT NULL,
	TIME                 timestamp   NOT NULL,
	VALUE                double   ,
	PRIORITY             integer  ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
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
	PRIORITY             integer  ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
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
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT IDX_UQ_POINT_TAG UNIQUE ( TAG_NAME, POINT ) ,
	CONSTRAINT PK_POINT_TAG PRIMARY KEY ( ID )
 );

CREATE INDEX IDX_FK_TAG_POINT ON POINT_TAG ( POINT );

CREATE INDEX IDX_TAG_BY_POINT ON POINT_TAG ( TAG_NAME );

CREATE TABLE IF NOT EXISTS POINT_VALUE_DATA ( 
	POINT                uuid   NOT NULL,
	VALUE                double   ,
	PRIORITY             integer   NOT NULL,
	PRIORITY_VALUES      varchar(500)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT PK_POINT_VALUE_DATA PRIMARY KEY ( POINT )
 );

CREATE TABLE IF NOT EXISTS REALTIME_SETTING ( 
	POINT                uuid   NOT NULL,
	ENABLED              boolean  DEFAULT FALSE NOT NULL,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT PK_REALTIME_SETTING PRIMARY KEY ( POINT )
 );

CREATE TABLE IF NOT EXISTS SCHEDULE_SETTING ( 
	ID                   uuid   NOT NULL,
	POINT                uuid   NOT NULL,
	RECURRING            boolean  DEFAULT TRUE NOT NULL,
	NAME                 varchar(63)  NOT NULL,
	COLOR                varchar(15)  DEFAULT '#FFFFFF' NOT NULL,
	START_TIME           timestamp   NOT NULL,
	END_TIME             timestamp   NOT NULL,
	VALUE                double   NOT NULL,
	WEEKDAYS             array   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT PK_SCHEDULE_SETTING PRIMARY KEY ( ID )
 );

CREATE INDEX IDX_FK_SCHEDULE_POINT ON SCHEDULE_SETTING ( POINT );

CREATE TABLE IF NOT EXISTS HISTORY_SETTING ( 
	POINT                uuid   NOT NULL,
	HISTORY_SETTING_TYPE varchar(15)   ,
	SCHEDULE             varchar(127)   ,
	TOLERANCE            double   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
    CONSTRAINT PK_HISTORY_SETTING PRIMARY KEY ( POINT )
);

CREATE TABLE IF NOT EXISTS PROTOCOL_DISPATCHER (
    ID                   int GENERATED ALWAYS AS IDENTITY  NOT NULL,
	ENTITY               varchar(127)   NOT NULL,
	ACTION               varchar(31)    NOT NULL,
	PROTOCOL             varchar(31)    DEFAULT 'UNKNOWN' NOT NULL,
	ADDRESS              varchar(255)   NOT NULL,
	GLOBAL               boolean        DEFAULT FALSE NOT NULL,
	STATE                varchar(31)    DEFAULT 'NONE' NOT NULL,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
    CONSTRAINT PK_PROTOCOL_DISPATCHER PRIMARY KEY ( ID ),
	CONSTRAINT IDX_UQ_PROTOCOL_DISPATCHER UNIQUE ( ENTITY, ACTION, PROTOCOL )
);

CREATE TABLE IF NOT EXISTS SYNC_DISPATCHER (
	ID                   integer GENERATED ALWAYS AS IDENTITY  NOT NULL,
	SYNC_TYPE            varchar(31)  NOT NULL,
	STATE                varchar(31)  DEFAULT 'NONE' NOT NULL,
	CONFIGURATION_JSON   clob(2147483647)   ,
	TIME_AUDIT           varchar(500)   ,
	SYNC_AUDIT           clob(2147483647)   ,
	CONSTRAINT PK_SYNC_DISPATCHER PRIMARY KEY ( ID ),
	CONSTRAINT IDX_UQ_SYNC_DISPATCHER UNIQUE ( SYNC_TYPE )
 );

-- ALTER FK
ALTER TABLE EDGE_DEVICE ADD CONSTRAINT FK_EDGE_DEVICE_EDGE FOREIGN KEY ( EDGE_ID ) REFERENCES EDGE( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE EDGE_DEVICE ADD CONSTRAINT FK_EDGE_DEVICE_DEVICE FOREIGN KEY ( DEVICE_ID ) REFERENCES DEVICE( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE EDGE_DEVICE ADD CONSTRAINT FK_EDGE_DEVICE_NETWORK FOREIGN KEY ( NETWORK_ID ) REFERENCES NETWORK( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE HISTORY_SETTING ADD CONSTRAINT FK_HISTORY_SETTING_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE NETWORK ADD CONSTRAINT FK_NETWORK_EDGE FOREIGN KEY ( EDGE ) REFERENCES EDGE( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE POINT ADD CONSTRAINT FK_POINT_EDGE FOREIGN KEY ( EDGE ) REFERENCES EDGE( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE POINT ADD CONSTRAINT FK_POINT_NETWORK FOREIGN KEY ( NETWORK ) REFERENCES NETWORK( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE POINT ADD CONSTRAINT FK_POINT_MEASURE_UNIT FOREIGN KEY ( MEASURE_UNIT ) REFERENCES MEASURE_UNIT( MEASURE_TYPE ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE POINT_HISTORY_DATA ADD CONSTRAINT FK_HISTORY_DATA_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE POINT_REALTIME_DATA ADD CONSTRAINT FK_POINT_REALTIME_DATA_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE POINT_TAG ADD CONSTRAINT FK_TAG_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE POINT_VALUE_DATA ADD CONSTRAINT FK_VALUE_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE REALTIME_SETTING ADD CONSTRAINT FK_REALTIME_SETTING_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE SCHEDULE_SETTING ADD CONSTRAINT FK_SCHEDULE_POINT FOREIGN KEY ( POINT ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE THING ADD CONSTRAINT FK_THING_DEVICE FOREIGN KEY ( DEVICE_ID ) REFERENCES DEVICE( ID ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE THING ADD CONSTRAINT FK_THING_MEASURE FOREIGN KEY ( MEASURE_UNIT ) REFERENCES MEASURE_UNIT( MEASURE_TYPE ) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE POINT_THING ADD CONSTRAINT FK_POINT_THING_POINT FOREIGN KEY ( POINT_ID ) REFERENCES POINT( ID ) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE POINT_THING ADD CONSTRAINT FK_POINT_THING_THING FOREIGN KEY ( THING_ID ) REFERENCES THING( ID ) ON DELETE CASCADE ON UPDATE CASCADE;
