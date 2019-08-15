package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.EntityMetadata.BigSerialKeyEntity;
import com.nubeiot.core.sql.EntityMetadata.SerialKeyEntity;
import com.nubeiot.core.sql.EntityMetadata.StringKeyEntity;
import com.nubeiot.core.sql.EntityMetadata.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.DeviceDao;
import com.nubeiot.iotdata.edge.model.tables.daos.EquipmentDao;
import com.nubeiot.iotdata.edge.model.tables.daos.HistorySettingDao;
import com.nubeiot.iotdata.edge.model.tables.daos.MeasureUnitDao;
import com.nubeiot.iotdata.edge.model.tables.daos.NetworkDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointHistoryDataDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointRealtimeDataDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointTagDao;
import com.nubeiot.iotdata.edge.model.tables.daos.RealtimeSettingDao;
import com.nubeiot.iotdata.edge.model.tables.daos.ScheduleSettingDao;
import com.nubeiot.iotdata.edge.model.tables.daos.ThingDao;
import com.nubeiot.iotdata.edge.model.tables.daos.TransducerDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.nubeiot.iotdata.edge.model.tables.pojos.Equipment;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTag;
import com.nubeiot.iotdata.edge.model.tables.pojos.RealtimeSetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.ScheduleSetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.Thing;
import com.nubeiot.iotdata.edge.model.tables.pojos.Transducer;
import com.nubeiot.iotdata.edge.model.tables.records.DeviceRecord;
import com.nubeiot.iotdata.edge.model.tables.records.EquipmentRecord;
import com.nubeiot.iotdata.edge.model.tables.records.HistorySettingRecord;
import com.nubeiot.iotdata.edge.model.tables.records.MeasureUnitRecord;
import com.nubeiot.iotdata.edge.model.tables.records.NetworkRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointHistoryDataRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointRealtimeDataRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointTagRecord;
import com.nubeiot.iotdata.edge.model.tables.records.RealtimeSettingRecord;
import com.nubeiot.iotdata.edge.model.tables.records.ScheduleSettingRecord;
import com.nubeiot.iotdata.edge.model.tables.records.ThingRecord;
import com.nubeiot.iotdata.edge.model.tables.records.TransducerRecord;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public interface Metadata {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class EquipmentMetadata implements UUIDKeyEntity<Equipment, EquipmentRecord, EquipmentDao> {

        static final EquipmentMetadata INSTANCE = new EquipmentMetadata();

        @Override
        public @NonNull Class<Equipment> modelClass() {
            return Equipment.class;
        }

        @Override
        public @NonNull Class<EquipmentDao> daoClass() {
            return EquipmentDao.class;
        }

        @Override
        public @NonNull JsonTable<EquipmentRecord> table() {
            return Tables.EQUIPMENT;
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "equipments";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class DeviceMetadata implements UUIDKeyEntity<Device, DeviceRecord, DeviceDao> {

        static final DeviceMetadata INSTANCE = new DeviceMetadata();

        @Override
        public @NonNull Class<Device> modelClass() {
            return Device.class;
        }

        @Override
        public @NonNull Class<DeviceDao> daoClass() {
            return DeviceDao.class;
        }

        @Override
        public @NonNull JsonTable<DeviceRecord> table() {
            return Tables.DEVICE;
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "devices";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class HistoryDataMetadata
        implements BigSerialKeyEntity<PointHistoryData, PointHistoryDataRecord, PointHistoryDataDao> {

        static final HistoryDataMetadata INSTANCE = new HistoryDataMetadata();

        @Override
        public @NonNull Class<PointHistoryData> modelClass() {
            return PointHistoryData.class;
        }

        @Override
        public @NonNull Class<PointHistoryDataDao> daoClass() {
            return PointHistoryDataDao.class;
        }

        @Override
        public @NonNull JsonTable<PointHistoryDataRecord> table() {
            return Tables.POINT_HISTORY_DATA;
        }

        @Override
        public @NonNull String requestKeyName() {
            return "history_id";
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "histories";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class HistorySettingMetadata
        implements UUIDKeyEntity<HistorySetting, HistorySettingRecord, HistorySettingDao> {

        static final HistorySettingMetadata INSTANCE = new HistorySettingMetadata();

        @Override
        public @NonNull Class<HistorySetting> modelClass() {
            return HistorySetting.class;
        }

        @Override
        public @NonNull Class<HistorySettingDao> daoClass() {
            return HistorySettingDao.class;
        }

        @Override
        public @NonNull JsonTable<HistorySettingRecord> table() {
            return Tables.HISTORY_SETTING;
        }

        @Override
        public @NonNull String requestKeyName() {
            return PointMetadata.INSTANCE.requestKeyName();
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "history_setting";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class MeasureUnitMetadata implements StringKeyEntity<MeasureUnit, MeasureUnitRecord, MeasureUnitDao> {

        static final MeasureUnitMetadata INSTANCE = new MeasureUnitMetadata();

        @Override
        public @NonNull Class<MeasureUnit> modelClass() {
            return MeasureUnit.class;
        }

        @Override
        public @NonNull Class<MeasureUnitDao> daoClass() {
            return MeasureUnitDao.class;
        }

        @Override
        public @NonNull JsonTable<MeasureUnitRecord> table() {
            return Tables.MEASURE_UNIT;
        }

        @Override
        public @NonNull String requestKeyName() {
            return jsonKeyName();
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "units";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class NetworkMetadata implements UUIDKeyEntity<Network, NetworkRecord, NetworkDao> {

        static final NetworkMetadata INSTANCE = new NetworkMetadata();

        private static Set<String> NULL_ALIASES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("default", "gpio")));

        static void optimizeAlias(JsonObject req) {
            Optional.ofNullable(req).ifPresent(r -> {
                if (NULL_ALIASES.contains(r.getString(INSTANCE.requestKeyName(), "").toLowerCase())) {
                    r.put(INSTANCE.requestKeyName(), (String) null);
                }
            });
        }

        @Override
        public @NonNull Class<Network> modelClass() {
            return Network.class;
        }

        @Override
        public @NonNull Class<NetworkDao> daoClass() {
            return NetworkDao.class;
        }

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.Network table() {
            return Tables.NETWORK;
        }

        @Override
        public @NonNull String pluralKeyName() {
            return "networks";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class PointMetadata implements UUIDKeyEntity<Point, PointRecord, PointDao> {

        public static final PointMetadata INSTANCE = new PointMetadata();

        @Override
        public @NonNull Class<Point> modelClass() {
            return Point.class;
        }

        @Override
        public @NonNull Class<PointDao> daoClass() {
            return PointDao.class;
        }

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.Point table() {
            return Tables.POINT;
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "points";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class RealtimeDataMetadata
        implements BigSerialKeyEntity<PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao> {

        static final RealtimeDataMetadata INSTANCE = new RealtimeDataMetadata();

        @Override
        public @NonNull Class<PointRealtimeData> modelClass() {
            return PointRealtimeData.class;
        }

        @Override
        public @NonNull Class<PointRealtimeDataDao> daoClass() {
            return PointRealtimeDataDao.class;
        }

        @Override
        public @NonNull JsonTable<PointRealtimeDataRecord> table() {
            return Tables.POINT_REALTIME_DATA;
        }

        @Override
        public @NonNull String requestKeyName() {
            return "realtime_id";
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "realtime_data";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class RealtimeSettingMetadata
        implements UUIDKeyEntity<RealtimeSetting, RealtimeSettingRecord, RealtimeSettingDao> {

        static final RealtimeSettingMetadata INSTANCE = new RealtimeSettingMetadata();

        @Override
        public @NonNull Class<RealtimeSetting> modelClass() {
            return RealtimeSetting.class;
        }

        @Override
        public @NonNull Class<RealtimeSettingDao> daoClass() {
            return RealtimeSettingDao.class;
        }

        @Override
        public @NonNull JsonTable<RealtimeSettingRecord> table() {
            return Tables.REALTIME_SETTING;
        }

        @Override
        @NonNull
        public String requestKeyName() {
            return PointMetadata.INSTANCE.requestKeyName();
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "realtime_setting";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class SchedulerSettingMetadata
        implements UUIDKeyEntity<ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao> {

        static final SchedulerSettingMetadata INSTANCE = new SchedulerSettingMetadata();

        @Override
        public @NonNull Class<ScheduleSetting> modelClass() {
            return ScheduleSetting.class;
        }

        @Override
        public @NonNull Class<ScheduleSettingDao> daoClass() {
            return ScheduleSettingDao.class;
        }

        @Override
        public @NonNull JsonTable<ScheduleSettingRecord> table() {
            return Tables.SCHEDULE_SETTING;
        }

        @Override
        public @NonNull String requestKeyName() {
            return "setting_id";
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "schedules";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class TagPointMetadata implements BigSerialKeyEntity<PointTag, PointTagRecord, PointTagDao> {

        static final TagPointMetadata INSTANCE = new TagPointMetadata();

        @Override
        public @NonNull Class<PointTag> modelClass() {
            return PointTag.class;
        }

        @Override
        public @NonNull Class<PointTagDao> daoClass() {
            return PointTagDao.class;
        }

        @Override
        public @NonNull JsonTable<PointTagRecord> table() {
            return Tables.POINT_TAG;
        }

        @Override
        public @NonNull String requestKeyName() {
            return "tag_" + jsonKeyName();
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "tags";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class ThingMetadata implements SerialKeyEntity<Thing, ThingRecord, ThingDao> {

        static final ThingMetadata INSTANCE = new ThingMetadata();

        @Override
        public @NonNull Class<Thing> modelClass() {
            return Thing.class;
        }

        @Override
        public @NonNull Class<ThingDao> daoClass() {
            return ThingDao.class;
        }

        @Override
        public @NonNull JsonTable<ThingRecord> table() {
            return Tables.THING;
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "things";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class TransducerMetadata implements UUIDKeyEntity<Transducer, TransducerRecord, TransducerDao> {

        static final TransducerMetadata INSTANCE = new TransducerMetadata();

        @Override
        public @NonNull Class<Transducer> modelClass() {
            return Transducer.class;
        }

        @Override
        public @NonNull Class<TransducerDao> daoClass() {
            return TransducerDao.class;
        }

        @Override
        public @NonNull JsonTable<TransducerRecord> table() {
            return Tables.TRANSDUCER;
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "transducers";
        }

    }

}
