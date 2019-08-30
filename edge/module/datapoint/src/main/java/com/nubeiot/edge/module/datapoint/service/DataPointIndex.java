package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jooq.OrderField;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata.AbstractCompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.EntityMetadata.BigSerialKeyEntity;
import com.nubeiot.core.sql.EntityMetadata.SerialKeyEntity;
import com.nubeiot.core.sql.EntityMetadata.StringKeyEntity;
import com.nubeiot.core.sql.EntityMetadata.UUIDKeyEntity;
import com.nubeiot.core.sql.MetadataIndex;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.tables.JsonTable;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.datapoint.model.pojos.DeviceComposite;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;
import com.nubeiot.edge.module.datapoint.model.pojos.ThingComposite;
import com.nubeiot.iotdata.dto.PointPriorityValue;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.DeviceDao;
import com.nubeiot.iotdata.edge.model.tables.daos.DeviceEquipDao;
import com.nubeiot.iotdata.edge.model.tables.daos.EquipmentDao;
import com.nubeiot.iotdata.edge.model.tables.daos.HistorySettingDao;
import com.nubeiot.iotdata.edge.model.tables.daos.MeasureUnitDao;
import com.nubeiot.iotdata.edge.model.tables.daos.NetworkDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointHistoryDataDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointRealtimeDataDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointTagDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointValueDataDao;
import com.nubeiot.iotdata.edge.model.tables.daos.RealtimeSettingDao;
import com.nubeiot.iotdata.edge.model.tables.daos.ScheduleSettingDao;
import com.nubeiot.iotdata.edge.model.tables.daos.ThingDao;
import com.nubeiot.iotdata.edge.model.tables.daos.TransducerDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.nubeiot.iotdata.edge.model.tables.pojos.DeviceEquip;
import com.nubeiot.iotdata.edge.model.tables.pojos.Equipment;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTag;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;
import com.nubeiot.iotdata.edge.model.tables.pojos.RealtimeSetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.ScheduleSetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.Thing;
import com.nubeiot.iotdata.edge.model.tables.pojos.Transducer;
import com.nubeiot.iotdata.edge.model.tables.records.DeviceEquipRecord;
import com.nubeiot.iotdata.edge.model.tables.records.DeviceRecord;
import com.nubeiot.iotdata.edge.model.tables.records.EquipmentRecord;
import com.nubeiot.iotdata.edge.model.tables.records.HistorySettingRecord;
import com.nubeiot.iotdata.edge.model.tables.records.MeasureUnitRecord;
import com.nubeiot.iotdata.edge.model.tables.records.NetworkRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointHistoryDataRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointRealtimeDataRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointTagRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointValueDataRecord;
import com.nubeiot.iotdata.edge.model.tables.records.RealtimeSettingRecord;
import com.nubeiot.iotdata.edge.model.tables.records.ScheduleSettingRecord;
import com.nubeiot.iotdata.edge.model.tables.records.ThingRecord;
import com.nubeiot.iotdata.edge.model.tables.records.TransducerRecord;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public interface DataPointIndex extends MetadataIndex {

    List<EntityMetadata> INDEX = MetadataIndex.find(DataPointIndex.class);
    String BUILTIN_DATA = "BUILTIN_DATA";
    String DATA_SYNC_CFG = "DATA_SYNC_CFG";
    String DEVICE_ID = "DEVICE_ID";
    String NETWORK_ID = "NETWORK_ID";

    static Map<EntityMetadata, Integer> dependencies() {
        Map<EntityMetadata, Integer> map = new HashMap<>();
        map.put(MeasureUnitMetadata.INSTANCE, 10);
        map.put(DeviceMetadata.INSTANCE, 10);
        map.put(EquipmentMetadata.INSTANCE, 10);
        map.put(TransducerMetadata.INSTANCE, 10);
        map.put(NetworkMetadata.INSTANCE, 20);
        map.put(ThingMetadata.INSTANCE, 20);
        map.put(DeviceEquipMetadata.INSTANCE, 30);
        map.put(PointMetadata.INSTANCE, 40);
        return map;
    }

    @Override
    default List<EntityMetadata> index() {
        return INDEX;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class EquipmentMetadata implements UUIDKeyEntity<Equipment, EquipmentRecord, EquipmentDao> {

        public static final EquipmentMetadata INSTANCE = new EquipmentMetadata();

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
        public @NonNull Equipment onCreating(RequestData reqData) throws IllegalArgumentException {
            Equipment equip = parseFromRequest(reqData.body());
            return equip.setId(Optional.ofNullable(equip.getId()).orElseGet(UUID::randomUUID));
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class DeviceMetadata implements UUIDKeyEntity<Device, DeviceRecord, DeviceDao> {

        public static final DeviceMetadata INSTANCE = new DeviceMetadata();

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
        public @NonNull Device onCreating(RequestData reqData) throws IllegalArgumentException {
            Device device = parseFromRequest(reqData.body());
            device.setCustomerCode(
                Strings.requireNotBlank(device.getCustomerCode(), "Customer code cannot be blank").toUpperCase());
            device.setSiteCode(
                Strings.requireNotBlank(device.getSiteCode(), "Site code cannot be blank").toUpperCase());
            return device.setId(Optional.ofNullable(device.getId()).orElseGet(UUID::randomUUID))
                         .setCode(Optional.ofNullable(device.getCode()).orElse(device.getId().toString()));
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class HistoryDataMetadata
        implements BigSerialKeyEntity<PointHistoryData, PointHistoryDataRecord, PointHistoryDataDao> {

        public static final HistoryDataMetadata INSTANCE = new HistoryDataMetadata();

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
        public String singularKeyName() {
            return "history";
        }

        @Override
        public @NonNull String pluralKeyName() {
            return "histories";
        }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(Tables.POINT_HISTORY_DATA.TIME.desc(), Tables.POINT_HISTORY_DATA.POINT.asc());
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class HistorySettingMetadata
        implements UUIDKeyEntity<HistorySetting, HistorySettingRecord, HistorySettingDao> {

        public static final HistorySettingMetadata INSTANCE = new HistorySettingMetadata();

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
        public @NonNull String singularKeyName() {
            return "history_setting";
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "history_setting";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class MeasureUnitMetadata implements StringKeyEntity<MeasureUnit, MeasureUnitRecord, MeasureUnitDao> {

        public static final MeasureUnitMetadata INSTANCE = new MeasureUnitMetadata();

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
        public @NonNull String singularKeyName() {
            return "unit";
        }

        @Override
        public @NonNull String jsonKeyName() {
            return "type";
        }

        @Override
        @NonNull
        public MeasureUnit onCreating(RequestData reqData) throws IllegalArgumentException {
            final MeasureUnit measureUnit = StringKeyEntity.super.onCreating(reqData);
            Strings.requireNotBlank(measureUnit.getType(), "Missing unit type");
            Strings.requireNotBlank(measureUnit.getCategory(), "Missing unit category");
            return measureUnit;
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class NetworkMetadata implements UUIDKeyEntity<Network, NetworkRecord, NetworkDao> {

        public static final NetworkMetadata INSTANCE = new NetworkMetadata();

        private static Set<String> NULL_ALIASES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("default", "gpio")));

        static void optimizeAlias(JsonObject req, String cacheId) {
            Optional.ofNullable(req).ifPresent(r -> {
                if (NULL_ALIASES.contains(r.getString(INSTANCE.requestKeyName(), "").toLowerCase())) {
                    r.put(INSTANCE.requestKeyName(), cacheId);
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
        public @NonNull Network onCreating(RequestData reqData) throws IllegalArgumentException {
            Network network = parseFromRequest(reqData.body());
            return network.setId(Optional.ofNullable(network.getId()).orElseGet(UUID::randomUUID));
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
        public Point onCreating(RequestData reqData) throws IllegalArgumentException {
            final Point point = UUIDKeyEntity.super.onCreating(reqData);
            Objects.requireNonNull(point.getDevice(), "Missing device");
            Strings.requireNotBlank(point.getMeasureUnit(), "Missing point measure unit");
            return point.setId(Optional.ofNullable(point.getId()).orElseGet(UUID::randomUUID));
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class PointCompositeMetadata
        extends AbstractCompositeMetadata<UUID, Point, PointRecord, PointDao, PointComposite>
        implements UUIDKeyEntity<Point, PointRecord, PointDao> {

        public static final PointCompositeMetadata INSTANCE = new PointCompositeMetadata().addSubItem(
            MeasureUnitMetadata.INSTANCE);

        @Override
        public final @NonNull Class<PointComposite> modelClass() { return PointComposite.class; }

        @Override
        public final @NonNull JsonTable<PointRecord> table() { return Tables.POINT; }

        @Override
        public final @NonNull Class<PointDao> daoClass() { return PointDao.class; }

        @Override
        public @NonNull Class<Point> rawClass() {
            return Point.class;
        }

        @Override
        public PointComposite onCreating(RequestData reqData) throws IllegalArgumentException {
            PointComposite point = parseFromRequest(reqData.body());
            Objects.requireNonNull(point.getDevice(), "Missing device");
            MeasureUnit other = point.getOther(MeasureUnitMetadata.INSTANCE.singularKeyName());
            if (Objects.isNull(other)) {
                point.put(MeasureUnitMetadata.INSTANCE.singularKeyName(), new MeasureUnit().setType(
                    Strings.requireNotBlank(point.getMeasureUnit(), "Missing point measure unit")));
            } else {
                point.setMeasureUnit(other.getType());
            }
            point.setId(Optional.ofNullable(point.getId()).orElseGet(UUID::randomUUID));
            return point;
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class PointValueMetadata implements UUIDKeyEntity<PointValueData, PointValueDataRecord, PointValueDataDao> {

        public static final PointValueMetadata INSTANCE = new PointValueMetadata();

        @Override
        public final @NonNull JsonTable<PointValueDataRecord> table() { return Tables.POINT_VALUE_DATA; }

        @Override
        public final @NonNull Class<PointValueData> modelClass() { return PointValueData.class; }

        @Override
        public final @NonNull Class<PointValueDataDao> daoClass() { return PointValueDataDao.class; }

        @Override
        public @NonNull String requestKeyName() {
            return PointMetadata.INSTANCE.requestKeyName();
        }

        @Override
        public @NonNull String singularKeyName() {
            return "data";
        }

        @Override
        public @NonNull String pluralKeyName() {
            return "data";
        }

        @Override
        public @NonNull PointValueData onCreating(RequestData reqData) throws IllegalArgumentException {
            return optimizeValue(reqData);
        }

        @Override
        public @NonNull PointValueData onPatching(@NonNull PointValueData dbData, @NonNull RequestData reqData)
            throws IllegalArgumentException {
            return parseFromRequest(JsonPojo.merge(dbData, optimizeValue(reqData)));
        }

        private PointValueData optimizeValue(@NonNull RequestData reqData) {
            PointValueData pojo = parseFromRequest(reqData.body());
            pojo.setPriority(Optional.ofNullable(pojo.getPriority()).orElse(PointPriorityValue.DEFAULT_PRIORITY));
            pojo.setPriorityValues(Optional.ofNullable(pojo.getPriorityValues())
                                           .orElse(new PointPriorityValue())
                                           .add(pojo.getPriority(), pojo.getValue()));
            return pojo;
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class RealtimeDataMetadata
        implements BigSerialKeyEntity<PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao> {

        public static final RealtimeDataMetadata INSTANCE = new RealtimeDataMetadata();

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
        public @NonNull String singularKeyName() {
            return "realtime_data";
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "realtime_data";
        }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(Tables.POINT_REALTIME_DATA.TIME.desc(), Tables.POINT_REALTIME_DATA.POINT.asc());
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class RealtimeSettingMetadata
        implements UUIDKeyEntity<RealtimeSetting, RealtimeSettingRecord, RealtimeSettingDao> {

        public static final RealtimeSettingMetadata INSTANCE = new RealtimeSettingMetadata();

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
        public @NonNull String singularKeyName() {
            return "realtime_setting";
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

        public static final SchedulerSettingMetadata INSTANCE = new SchedulerSettingMetadata();

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
        public @NonNull String singularKeyName() {
            return "schedule";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class TagPointMetadata implements BigSerialKeyEntity<PointTag, PointTagRecord, PointTagDao> {

        public static final TagPointMetadata INSTANCE = new TagPointMetadata();

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
        public @NonNull String singularKeyName() {
            return "tag";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class ThingMetadata implements SerialKeyEntity<Thing, ThingRecord, ThingDao> {

        public static final ThingMetadata INSTANCE = new ThingMetadata();

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

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class TransducerMetadata implements UUIDKeyEntity<Transducer, TransducerRecord, TransducerDao> {

        public static final TransducerMetadata INSTANCE = new TransducerMetadata();

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
        public @NonNull Transducer onCreating(RequestData reqData) throws IllegalArgumentException {
            Transducer transducer = parseFromRequest(reqData.body());
            return transducer.setId(Optional.ofNullable(transducer.getId()).orElseGet(UUID::randomUUID));
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class DeviceEquipMetadata implements BigSerialKeyEntity<DeviceEquip, DeviceEquipRecord, DeviceEquipDao> {

        public static final DeviceEquipMetadata INSTANCE = new DeviceEquipMetadata();

        @Override
        public @NonNull JsonTable<DeviceEquipRecord> table() {
            return Tables.DEVICE_EQUIP;
        }

        @Override
        public @NonNull Class<DeviceEquip> modelClass() {
            return DeviceEquip.class;
        }

        @Override
        public @NonNull Class<DeviceEquipDao> daoClass() {
            return DeviceEquipDao.class;
        }

    }


    final class EquipThingMetadata
        extends AbstractCompositeMetadata<Integer, Thing, ThingRecord, ThingDao, ThingComposite>
        implements SerialKeyEntity<Thing, ThingRecord, ThingDao> {

        public static final EquipThingMetadata INSTANCE = new EquipThingMetadata();

        @Override
        public @NonNull Class<Thing> rawClass() {
            return Thing.class;
        }

        @Override
        public @NonNull Class<ThingComposite> modelClass() {
            return ThingComposite.class;
        }

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.Thing table() {
            return Tables.THING;
        }

        @Override
        public @NonNull Class<ThingDao> daoClass() {
            return ThingDao.class;
        }

    }


    final class DeviceEquipCompositeMetadata
        extends AbstractCompositeMetadata<Long, DeviceEquip, DeviceEquipRecord, DeviceEquipDao, DeviceComposite>
        implements BigSerialKeyEntity<DeviceEquip, DeviceEquipRecord, DeviceEquipDao> {

        public static final DeviceEquipCompositeMetadata INSTANCE = new DeviceEquipCompositeMetadata();

        @Override
        public @NonNull Class<DeviceEquip> rawClass() {
            return DeviceEquip.class;
        }

        @Override
        public @NonNull Class<DeviceComposite> modelClass() {
            return DeviceComposite.class;
        }

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.DeviceEquip table() {
            return Tables.DEVICE_EQUIP;
        }

        @Override
        public @NonNull Class<DeviceEquipDao> daoClass() {
            return DeviceEquipDao.class;
        }

    }

}
