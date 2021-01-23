package com.nubeiot.edge.module.datapoint;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.jooq.OrderField;

import io.github.zero88.utils.DateTimes;
import io.github.zero88.utils.Strings;
import io.github.zero88.utils.UUID64;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata.AbstractCompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.EntityMetadata.BigSerialKeyEntity;
import com.nubeiot.core.sql.EntityMetadata.SerialKeyEntity;
import com.nubeiot.core.sql.EntityMetadata.StringKeyEntity;
import com.nubeiot.core.sql.EntityMetadata.UUID64KeyEntity;
import com.nubeiot.core.sql.EntityMetadata.UUIDKeyEntity;
import com.nubeiot.core.sql.MetadataIndex;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.tables.JsonTable;
import com.nubeiot.edge.module.datapoint.model.pojos.EdgeDeviceComposite;
import com.nubeiot.edge.module.datapoint.model.pojos.FolderGroupComposite;
import com.nubeiot.edge.module.datapoint.model.pojos.HasProtocol;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;
import com.nubeiot.edge.module.datapoint.model.pojos.PointTransducerComposite;
import com.nubeiot.iotdata.dto.GroupLevel;
import com.nubeiot.iotdata.dto.HistorySettingType;
import com.nubeiot.iotdata.dto.PointPriorityValue;
import com.nubeiot.iotdata.dto.PointPriorityValue.PointValue;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.dto.TransducerType;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.DeviceDao;
import com.nubeiot.iotdata.edge.model.tables.daos.EdgeDao;
import com.nubeiot.iotdata.edge.model.tables.daos.EdgeDeviceDao;
import com.nubeiot.iotdata.edge.model.tables.daos.FolderDao;
import com.nubeiot.iotdata.edge.model.tables.daos.FolderGroupDao;
import com.nubeiot.iotdata.edge.model.tables.daos.HistorySettingDao;
import com.nubeiot.iotdata.edge.model.tables.daos.MeasureUnitDao;
import com.nubeiot.iotdata.edge.model.tables.daos.NetworkDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointHistoryDataDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointRealtimeDataDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointTagDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointTransducerDao;
import com.nubeiot.iotdata.edge.model.tables.daos.PointValueDataDao;
import com.nubeiot.iotdata.edge.model.tables.daos.ProtocolDispatcherDao;
import com.nubeiot.iotdata.edge.model.tables.daos.RealtimeSettingDao;
import com.nubeiot.iotdata.edge.model.tables.daos.ScheduleSettingDao;
import com.nubeiot.iotdata.edge.model.tables.daos.SyncDispatcherDao;
import com.nubeiot.iotdata.edge.model.tables.daos.TransducerDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.nubeiot.iotdata.edge.model.tables.pojos.Edge;
import com.nubeiot.iotdata.edge.model.tables.pojos.EdgeDevice;
import com.nubeiot.iotdata.edge.model.tables.pojos.Folder;
import com.nubeiot.iotdata.edge.model.tables.pojos.FolderGroup;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTag;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTransducer;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;
import com.nubeiot.iotdata.edge.model.tables.pojos.ProtocolDispatcher;
import com.nubeiot.iotdata.edge.model.tables.pojos.RealtimeSetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.ScheduleSetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.SyncDispatcher;
import com.nubeiot.iotdata.edge.model.tables.pojos.Transducer;
import com.nubeiot.iotdata.edge.model.tables.records.DeviceRecord;
import com.nubeiot.iotdata.edge.model.tables.records.EdgeDeviceRecord;
import com.nubeiot.iotdata.edge.model.tables.records.EdgeRecord;
import com.nubeiot.iotdata.edge.model.tables.records.FolderGroupRecord;
import com.nubeiot.iotdata.edge.model.tables.records.FolderRecord;
import com.nubeiot.iotdata.edge.model.tables.records.HistorySettingRecord;
import com.nubeiot.iotdata.edge.model.tables.records.MeasureUnitRecord;
import com.nubeiot.iotdata.edge.model.tables.records.NetworkRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointHistoryDataRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointRealtimeDataRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointTagRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointTransducerRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointValueDataRecord;
import com.nubeiot.iotdata.edge.model.tables.records.ProtocolDispatcherRecord;
import com.nubeiot.iotdata.edge.model.tables.records.RealtimeSettingRecord;
import com.nubeiot.iotdata.edge.model.tables.records.ScheduleSettingRecord;
import com.nubeiot.iotdata.edge.model.tables.records.SyncDispatcherRecord;
import com.nubeiot.iotdata.edge.model.tables.records.TransducerRecord;
import com.nubeiot.iotdata.unit.DataType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public interface DataPointIndex extends MetadataIndex {

    List<EntityMetadata> INDEX = Collections.unmodifiableList(MetadataIndex.find(DataPointIndex.class));
    String BUILTIN_DATA = "BUILTIN_DATA";
    String DATA_SYNC_CFG = "DATA_SYNC_CFG";
    String CUSTOMER_CODE = "CUSTOMER_CODE";
    String SITE_CODE = "SITE_CODE";
    String EDGE_ID = "EDGE_ID";
    String DEFAULT_NETWORK_ID = "DEFAULT_NETWORK_ID";

    static Map<EntityMetadata, Integer> dependencies() {
        Map<EntityMetadata, Integer> map = new HashMap<>();
        map.put(MeasureUnitMetadata.INSTANCE, 10);
        map.put(EdgeMetadata.INSTANCE, 10);
        map.put(DeviceMetadata.INSTANCE, 10);
        map.put(NetworkMetadata.INSTANCE, 20);
        map.put(TransducerMetadata.INSTANCE, 20);
        map.put(EdgeDeviceMetadata.INSTANCE, 30);
        map.put(PointCompositeMetadata.INSTANCE, 40);
        map.put(PointTransducerMetadata.INSTANCE, 50);
        map.put(FolderMetadata.INSTANCE, 60);
        map.put(FolderGroupMetadata.INSTANCE, 70);
        return map;
    }

    @Override
    default List<EntityMetadata> index() {
        return INDEX;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class DeviceMetadata implements UUIDKeyEntity<Device, DeviceRecord, DeviceDao>, HasProtocol<Device> {

        public static final DeviceMetadata INSTANCE = new DeviceMetadata();

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.Device table() {
            return Tables.DEVICE;
        }

        @Override
        public @NonNull Class<Device> modelClass() {
            return Device.class;
        }

        @Override
        public @NonNull Class<DeviceDao> daoClass() {
            return DeviceDao.class;
        }

        @Override
        public @NonNull Device onCreating(RequestData reqData) throws IllegalArgumentException {
            Device device = UUIDKeyEntity.super.onCreating(reqData);
            Strings.requireNotBlank(device.getCode(), "Device code is mandatory");
            Strings.requireNotBlank(device.getType(), "Device type is mandatory");
            return device.setId(Optional.ofNullable(device.getId()).orElseGet(UUID::randomUUID));
        }

        @Override
        public Protocol getProtocol(@NonNull Device pojo) {
            return pojo.getProtocol();
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class EdgeMetadata implements UUIDKeyEntity<Edge, EdgeRecord, EdgeDao> {

        public static final EdgeMetadata INSTANCE = new EdgeMetadata();

        @Override
        public @NonNull JsonTable<EdgeRecord> table() {
            return Tables.EDGE;
        }

        @Override
        public @NonNull Class<Edge> modelClass() {
            return Edge.class;
        }

        @Override
        public @NonNull Class<EdgeDao> daoClass() {
            return EdgeDao.class;
        }

        @Override
        public @NonNull Edge onCreating(RequestData reqData) throws IllegalArgumentException {
            Edge edge = UUIDKeyEntity.super.onCreating(reqData);
            edge.setCustomerCode(
                Strings.requireNotBlank(edge.getCustomerCode(), "Customer code cannot be blank").toUpperCase());
            edge.setSiteCode(Strings.requireNotBlank(edge.getSiteCode(), "Site code cannot be blank").toUpperCase());
            return edge.setId(Optional.ofNullable(edge.getId()).orElseGet(UUID::randomUUID))
                       .setCode(Optional.ofNullable(edge.getCode()).orElse(edge.getId().toString()));
        }

        @Override
        public @NonNull Edge onPatching(@NonNull Edge dbData, RequestData reqData) throws IllegalArgumentException {
            final JsonObject body = reqData.body().copy();
            body.put(context().jsonKeyName(),
                     JsonData.checkAndConvert(context().parseKey(body.remove(context().requestKeyName()).toString())));
            Edge edge = parseFromRequest(JsonPojo.merge(dbData, body));
            if (!edge.getCustomerCode().equals(dbData.getCustomerCode())) {
                throw new IllegalArgumentException("Customer code is read-only");
            }
            if (!edge.getSiteCode().equals(dbData.getSiteCode())) {
                throw new IllegalArgumentException("Site code is read-only");
            }
            return edge;
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class HistoryDataMetadata
        implements BigSerialKeyEntity<PointHistoryData, PointHistoryDataRecord, PointHistoryDataDao> {

        public static final HistoryDataMetadata INSTANCE = new HistoryDataMetadata();

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.PointHistoryData table() {
            return Tables.POINT_HISTORY_DATA;
        }

        @Override
        public @NonNull Class<PointHistoryData> modelClass() {
            return PointHistoryData.class;
        }

        @Override
        public @NonNull Class<PointHistoryDataDao> daoClass() {
            return PointHistoryDataDao.class;
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

        @Override
        public @NonNull PointHistoryData onCreating(RequestData reqData) throws IllegalArgumentException {
            final PointHistoryData historyData = BigSerialKeyEntity.super.onCreating(reqData);
            Objects.requireNonNull(historyData.getPoint(), "History data point is mandatory");
            Objects.requireNonNull(historyData.getValue(), "History data value is mandatory");
            return historyData.setTime(Optional.ofNullable(historyData.getTime()).orElse(DateTimes.now()))
                              .setPriority(Optional.ofNullable(historyData.getPriority())
                                                   .orElse(PointPriorityValue.DEFAULT_PRIORITY));
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class HistorySettingMetadata
        implements UUIDKeyEntity<HistorySetting, HistorySettingRecord, HistorySettingDao> {

        public static final HistorySettingMetadata INSTANCE = new HistorySettingMetadata();

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.HistorySetting table() {
            return Tables.HISTORY_SETTING;
        }

        @Override
        public @NonNull Class<HistorySetting> modelClass() {
            return HistorySetting.class;
        }

        @Override
        public @NonNull Class<HistorySettingDao> daoClass() {
            return HistorySettingDao.class;
        }

        @Override
        public @NonNull String requestKeyName() {
            return PointCompositeMetadata.INSTANCE.requestKeyName();
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

        @Override
        public @NonNull HistorySetting onCreating(@NonNull RequestData reqData) throws IllegalArgumentException {
            return validate(UUIDKeyEntity.super.onCreating(reqData));
        }

        @Override
        public @NonNull HistorySetting onPatching(@NonNull HistorySetting dbData, @NonNull RequestData reqData)
            throws IllegalArgumentException {
            return validate(UUIDKeyEntity.super.onPatching(dbData, reqData));
        }

        @NonNull
        private HistorySetting validate(@NonNull HistorySetting setting) {
            Objects.requireNonNull(setting.getType(), "History setting type is mandatory. One of: " +
                                                      Arrays.asList(HistorySettingType.COV.type(),
                                                                    HistorySettingType.PERIOD.type()));
            if (HistorySettingType.COV.equals(setting.getType())) {
                Objects.requireNonNull(setting.getTolerance(), "History setting tolerance is mandatory");
                if (setting.getTolerance() < 0) {
                    throw new IllegalArgumentException("History setting tolerance must be positive number");
                }
                setting.setSchedule(null);
            }
            if (HistorySettingType.PERIOD.equals(setting.getType())) {
                Objects.requireNonNull(setting.getSchedule(), "History setting schedule is mandatory");
                setting.setTolerance(null);
            }
            return setting;
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class MeasureUnitMetadata implements StringKeyEntity<MeasureUnit, MeasureUnitRecord, MeasureUnitDao> {

        public static final MeasureUnitMetadata INSTANCE = new MeasureUnitMetadata();

        @Override
        public @NonNull JsonTable<MeasureUnitRecord> table() {
            return Tables.MEASURE_UNIT;
        }

        @Override
        public @NonNull Class<MeasureUnit> modelClass() {
            return MeasureUnit.class;
        }

        @Override
        public @NonNull Class<MeasureUnitDao> daoClass() {
            return MeasureUnitDao.class;
        }

        @Override
        public @NonNull String requestKeyName() {
            return "unit_type";
        }

        @Override
        public @NonNull String jsonKeyName() {
            return "type";
        }

        @Override
        public @NonNull String singularKeyName() {
            return "unit";
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
    final class NetworkMetadata implements UUIDKeyEntity<Network, NetworkRecord, NetworkDao>, HasProtocol<Network> {

        public static final NetworkMetadata INSTANCE = new NetworkMetadata();
        public static final String DEFAULT_CODE = "DEFAULT";

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.Network table() {
            return Tables.NETWORK;
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
        public @NonNull Network onCreating(RequestData reqData) throws IllegalArgumentException {
            Network network = UUIDKeyEntity.super.onCreating(reqData);
            Objects.requireNonNull(network.getEdge(), "Edge is mandatory");
            Strings.requireNotBlank(network.getCode(), "Network code is mandatory");
            return network.setId(Optional.ofNullable(network.getId()).orElseGet(UUID::randomUUID));
        }

        @Override
        public Protocol getProtocol(@NonNull Network pojo) {
            return pojo.getProtocol();
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class PointMetadata implements UUIDKeyEntity<Point, PointRecord, PointDao>, HasProtocol<Point> {

        public static final PointMetadata INSTANCE = new PointMetadata();

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.Point table() {
            return Tables.POINT;
        }

        @Override
        public @NonNull Class<Point> modelClass() {
            return Point.class;
        }

        @Override
        public @NonNull Class<PointDao> daoClass() {
            return PointDao.class;
        }

        @Override
        public @NonNull Point onCreating(RequestData reqData) throws IllegalArgumentException {
            final Point point = UUIDKeyEntity.super.onCreating(reqData);
            Objects.requireNonNull(point.getEdge(), "Point must be assigned to Edge");
            Objects.requireNonNull(point.getNetwork(), "Point must be assigned to Network");
            Strings.requireNotBlank(point.getMeasureUnit(), "Point measure unit is mandatory");
            return point.setId(Optional.ofNullable(point.getId()).orElseGet(UUID::randomUUID));
        }

        @Override
        public Protocol getProtocol(@NonNull Point pojo) {
            return pojo.getProtocol();
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class PointCompositeMetadata
        extends AbstractCompositeMetadata<UUID, Point, PointRecord, PointDao, PointComposite>
        implements UUIDKeyEntity<Point, PointRecord, PointDao>, HasProtocol<Point> {

        public static final PointCompositeMetadata INSTANCE = new PointCompositeMetadata().addSubItem(
            MeasureUnitMetadata.INSTANCE);

        @Override
        public final @NonNull Class<PointComposite> modelClass() { return PointComposite.class; }

        @Override
        public final @NonNull com.nubeiot.iotdata.edge.model.tables.Point table() { return Tables.POINT; }

        @Override
        public final @NonNull Class<PointDao> daoClass() { return PointDao.class; }

        @Override
        public @NonNull String requestKeyName() {
            return EntityMetadata.createRequestKeyName(rawClass(), jsonKeyName());
        }

        @Override
        public @NonNull Class<Point> rawClass() {
            return Point.class;
        }

        @Override
        public @NonNull PointComposite onCreating(RequestData reqData) throws IllegalArgumentException {
            PointComposite point = validate(parseFromRequest(reqData.body()));
            point.setId(Optional.ofNullable(point.getId()).orElseGet(UUID::randomUUID));
            return point;
        }

        @Override
        public @NonNull PointComposite onUpdating(@NonNull Point dbData, RequestData reqData)
            throws IllegalArgumentException {
            return validate(super.onUpdating(dbData, reqData));
        }

        @Override
        public @NonNull PointComposite onPatching(@NonNull Point dbData, RequestData reqData)
            throws IllegalArgumentException {
            return validate(super.onPatching(dbData, reqData));
        }

        @Override
        public Protocol getProtocol(@NonNull Point pojo) {
            return pojo.getProtocol();
        }

        private PointComposite validate(@NonNull PointComposite point) {
            Objects.requireNonNull(point.getEdge(), "Point must be assigned to Edge");
            Objects.requireNonNull(point.getNetwork(), "Point must be assigned to Network");
            point.setCode(Strings.requireNotBlank(point.getCode(), () -> mandatoryField(table().CODE)));
            final MeasureUnit other = point.getOther(MeasureUnitMetadata.INSTANCE.singularKeyName());
            if (Objects.isNull(other)) {
                point.addMeasureUnit(new MeasureUnit().setType(
                    Strings.requireNotBlank(point.getMeasureUnit(), "Point measure unit is mandatory")));
            } else {
                point.setMeasureUnit(other.getType());
            }
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
            return PointCompositeMetadata.INSTANCE.requestKeyName();
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
        public @NonNull PointValueData parseFromRequest(@NonNull JsonObject request) throws IllegalArgumentException {
            final PointValueData pv = UUIDKeyEntity.super.parseFromRequest(request);
            return pv.setPriority(Optional.ofNullable(pv.getPriority()).orElse(PointPriorityValue.DEFAULT_PRIORITY));
        }

        @Override
        public @NonNull PointValueData onCreating(@NonNull RequestData reqData) throws IllegalArgumentException {
            final PointValueData pojo = parseFromRequest(reqData.body());
            return pojo.setPriorityValues(Optional.ofNullable(pojo.getPriorityValues())
                                                  .orElse(new PointPriorityValue())
                                                  .add(pojo.getPriority(), pojo.getValue()));
        }

        @Override
        public @NonNull PointValueData onPatching(@NonNull PointValueData dbData, @NonNull RequestData reqData)
            throws IllegalArgumentException {
            final PointValueData pvData = parseFromRequest(reqData.body());
            final JsonObject priorities = Optional.ofNullable(pvData.getPriorityValues())
                                                  .map(PointPriorityValue::toJson)
                                                  .orElse(new JsonObject())
                                                  .put(pvData.getPriority().toString(), pvData.getValue());
            final PointPriorityValue merged = JsonData.merge(dbData.getPriorityValues().toJson(), priorities,
                                                             PointPriorityValue.class);
            final PointValue highestValue = merged.findHighestValue();
            return new PointValueData().setPoint(dbData.getPoint())
                                       .setPriority(highestValue.getPriority())
                                       .setValue(highestValue.getValue())
                                       .setPriorityValues(merged)
                                       .setTimeAudit(dbData.getTimeAudit())
                                       .setSyncAudit(dbData.getSyncAudit());
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class RealtimeDataMetadata
        implements BigSerialKeyEntity<PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao> {

        public static final RealtimeDataMetadata INSTANCE = new RealtimeDataMetadata();

        public static JsonObject simpleValue(Double val, Integer priority) {
            return new JsonObject().put("val", val)
                                   .put("priority",
                                        Optional.ofNullable(priority).orElse(PointPriorityValue.DEFAULT_PRIORITY));
        }

        public static JsonObject fullValue(@NonNull JsonObject value, @NonNull DataType dataType) {
            return value.put("display", dataType.display(dataType.parse(value.getValue("val"))));
        }

        @Override
        public @NonNull JsonTable<PointRealtimeDataRecord> table() {
            return Tables.POINT_REALTIME_DATA;
        }

        @Override
        public @NonNull Class<PointRealtimeData> modelClass() {
            return PointRealtimeData.class;
        }

        @Override
        public @NonNull Class<PointRealtimeDataDao> daoClass() {
            return PointRealtimeDataDao.class;
        }

        @Override
        public @NonNull String requestKeyName() {
            return "rt_id";
        }

        @Override
        public @NonNull String singularKeyName() {
            return "rt_data";
        }

        @Override
        @NonNull
        public String pluralKeyName() {
            return "rt_data";
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
        public @NonNull JsonTable<RealtimeSettingRecord> table() {
            return Tables.REALTIME_SETTING;
        }

        @Override
        public @NonNull Class<RealtimeSetting> modelClass() {
            return RealtimeSetting.class;
        }

        @Override
        public @NonNull Class<RealtimeSettingDao> daoClass() {
            return RealtimeSettingDao.class;
        }

        @Override
        public @NonNull String requestKeyName() {
            return PointCompositeMetadata.INSTANCE.requestKeyName();
        }

        @Override
        public @NonNull String singularKeyName() {
            return "rt_setting";
        }

        @Override
        public @NonNull String pluralKeyName() {
            return "rt_setting";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class SchedulerSettingMetadata
        implements UUIDKeyEntity<ScheduleSetting, ScheduleSettingRecord, ScheduleSettingDao> {

        public static final SchedulerSettingMetadata INSTANCE = new SchedulerSettingMetadata();

        @Override
        public @NonNull JsonTable<ScheduleSettingRecord> table() {
            return Tables.SCHEDULE_SETTING;
        }

        @Override
        public @NonNull Class<ScheduleSetting> modelClass() {
            return ScheduleSetting.class;
        }

        @Override
        public @NonNull Class<ScheduleSettingDao> daoClass() {
            return ScheduleSettingDao.class;
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
        public @NonNull JsonTable<PointTagRecord> table() {
            return Tables.POINT_TAG;
        }

        @Override
        public @NonNull Class<PointTag> modelClass() {
            return PointTag.class;
        }

        @Override
        public @NonNull Class<PointTagDao> daoClass() {
            return PointTagDao.class;
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
    final class TransducerMetadata implements UUIDKeyEntity<Transducer, TransducerRecord, TransducerDao> {

        public static final TransducerMetadata INSTANCE = new TransducerMetadata();

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.Transducer table() {
            return Tables.TRANSDUCER;
        }

        @Override
        public @NonNull Class<Transducer> modelClass() {
            return Transducer.class;
        }

        @Override
        public @NonNull Class<TransducerDao> daoClass() {
            return TransducerDao.class;
        }

        @Override
        public @NonNull Transducer onCreating(RequestData reqData) throws IllegalArgumentException {
            Transducer transducer = UUIDKeyEntity.super.onCreating(reqData);
            Strings.requireNotBlank(transducer.getCode(), "Transducer code is mandatory");
            Strings.requireNotBlank(transducer.getType(), "Transducer type is mandatory");
            Strings.requireNotBlank(transducer.getCategory(), "Transducer category is mandatory");
            return transducer.setId(Optional.ofNullable(transducer.getId()).orElseGet(UUID::randomUUID));
        }

    }


    final class PointTransducerMetadata extends
                                        AbstractCompositeMetadata<Long, PointTransducer, PointTransducerRecord,
                                                                     PointTransducerDao, PointTransducerComposite>
        implements BigSerialKeyEntity<PointTransducer, PointTransducerRecord, PointTransducerDao> {

        public static final PointTransducerMetadata INSTANCE = new PointTransducerMetadata().addSubItem(
            PointCompositeMetadata.INSTANCE, TransducerMetadata.INSTANCE);

        public static String genComputedTransducer(@NonNull TransducerType type, @NonNull UUID transducerId) {
            if (type != TransducerType.SENSOR) {
                return null;
            }
            return UUID64.uuidToBase64(transducerId) + "-" + type.type();
        }

        @Override
        public @NonNull Class<PointTransducer> rawClass() {
            return PointTransducer.class;
        }

        @Override
        public @NonNull Class<PointTransducerComposite> modelClass() {
            return PointTransducerComposite.class;
        }

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.PointTransducer table() {
            return Tables.POINT_TRANSDUCER;
        }

        @Override
        public @NonNull Class<PointTransducerDao> daoClass() {
            return PointTransducerDao.class;
        }

    }


    final class EdgeDeviceMetadata
        extends AbstractCompositeMetadata<Long, EdgeDevice, EdgeDeviceRecord, EdgeDeviceDao, EdgeDeviceComposite>
        implements BigSerialKeyEntity<EdgeDevice, EdgeDeviceRecord, EdgeDeviceDao> {

        public static final EdgeDeviceMetadata INSTANCE = new EdgeDeviceMetadata().addSubItem(DeviceMetadata.INSTANCE,
                                                                                              EdgeMetadata.INSTANCE);

        @Override
        public @NonNull Class<EdgeDevice> rawClass() {
            return EdgeDevice.class;
        }

        @Override
        public @NonNull Class<EdgeDeviceComposite> modelClass() {
            return EdgeDeviceComposite.class;
        }

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.EdgeDevice table() {
            return Tables.EDGE_DEVICE;
        }

        @Override
        public @NonNull Class<EdgeDeviceDao> daoClass() {
            return EdgeDeviceDao.class;
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class FolderMetadata implements UUID64KeyEntity<Folder, FolderRecord, FolderDao> {

        public static final FolderMetadata INSTANCE = new FolderMetadata();

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.Folder table() {
            return Tables.FOLDER;
        }

        @Override
        public @NonNull Class<Folder> modelClass() {
            return Folder.class;
        }

        @Override
        public @NonNull Class<FolderDao> daoClass() {
            return FolderDao.class;
        }

        @Override
        public @NonNull String jsonKeyName() {
            return "id";
        }

        @Override
        public @NonNull Folder onCreating(@NonNull RequestData reqData) throws IllegalArgumentException {
            final Folder folder = UUID64KeyEntity.super.onCreating(reqData);
            Strings.requireNotBlank(folder.getName(), () -> mandatoryField(table().NAME));
            return folder.setId(Strings.fallback(folder.getId(), UUID64::random));
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class FolderGroupMetadata
        extends AbstractCompositeMetadata<String, FolderGroup, FolderGroupRecord, FolderGroupDao, FolderGroupComposite>
        implements UUID64KeyEntity<FolderGroup, FolderGroupRecord, FolderGroupDao> {

        public static final FolderGroupMetadata INSTANCE = new FolderGroupMetadata().addSubItem(FolderMetadata.INSTANCE,
                                                                                                DeviceMetadata.INSTANCE,
                                                                                                NetworkMetadata.INSTANCE,
                                                                                                PointCompositeMetadata.INSTANCE);

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.FolderGroup table() {
            return Tables.FOLDER_GROUP;
        }

        @Override
        public @NonNull Class<FolderGroupDao> daoClass() {
            return FolderGroupDao.class;
        }

        @Override
        public @NonNull String jsonKeyName() {
            return "id";
        }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(table().LEVEL.asc(), table().FOLDER_ID.asc());
        }

        @Override
        public @NonNull Class<FolderGroupComposite> modelClass() {
            return FolderGroupComposite.class;
        }

        @Override
        public @NonNull Class<FolderGroup> rawClass() {
            return FolderGroup.class;
        }

        @Override
        public @NonNull FolderGroupComposite onCreating(@NonNull RequestData reqData) throws IllegalArgumentException {
            final FolderGroupComposite pojo = super.onCreating(reqData);
            final Folder folder = pojo.getOther(FolderMetadata.INSTANCE.singularKeyName());
            final Device device = pojo.getOther(DeviceMetadata.INSTANCE.singularKeyName());
            final Point point = pojo.getOther(PointCompositeMetadata.INSTANCE.singularKeyName());
            pojo.setFolderId(Optional.ofNullable(folder).map(Folder::getId).orElseGet(pojo::getFolderId));
            pojo.setDeviceId(Optional.ofNullable(device).map(Device::getId).orElseGet(pojo::getDeviceId));
            pojo.setPointId(Optional.ofNullable(point).map(Point::getId).orElseGet(pojo::getPointId));
            return (FolderGroupComposite) validate(pojo.setId(Strings.fallback(pojo.getId(), UUID64::random)));
        }

        @Override
        public @NonNull FolderGroupComposite onUpdating(@NonNull FolderGroup dbData, @NonNull RequestData reqData)
            throws IllegalArgumentException {
            return validate(super.onUpdating(dbData, reqData));
        }

        @Override
        public @NonNull FolderGroupComposite onPatching(@NonNull FolderGroup dbData, @NonNull RequestData reqData)
            throws IllegalArgumentException {
            return validate(super.onPatching(dbData, reqData));
        }

        public <PP extends FolderGroup> PP validate(@NonNull PP pojo) {
            pojo.setFolderId(Strings.requireNotBlank(pojo.getFolderId(), () -> mandatoryField(table().FOLDER_ID)));
            if (pojo.getLevel() == GroupLevel.EDGE) {
                final UUID networkId = Strings.requireNotBlank(pojo.getNetworkId(),
                                                               () -> mandatoryField(table().NETWORK_ID));
                return (PP) pojo.setNetworkId(networkId).setDeviceId(null).setPointId(null).setParentFolderId(null);
            }
            if (pojo.getLevel() == GroupLevel.NETWORK) {
                if (Objects.isNull(pojo.getNetworkId()) && Objects.isNull(pojo.getDeviceId())) {
                    throw mandatoryField(table().NETWORK_ID);
                }
                return (PP) pojo.setPointId(null).setParentFolderId(null);
            }
            if (pojo.getLevel() == GroupLevel.DEVICE) {
                if (Objects.isNull(pojo.getDeviceId())/* && Objects.isNull(pojo.getPointId())*/) {
                    throw mandatoryField(table().DEVICE_ID);
                }
                return (PP) pojo.setParentFolderId(null);
            }
            if (pojo.getLevel() == GroupLevel.FOLDER) {
                pojo.setParentFolderId(
                    Strings.requireNotBlank(pojo.getParentFolderId(), () -> mandatoryField(table().PARENT_FOLDER_ID)));
                if (pojo.getParentFolderId().equals(pojo.getFolderId())) {
                    throw new IllegalArgumentException("Parent folder cannot be same as itself");
                }
                return pojo;
            }
            throw new IllegalArgumentException("Unknown group level " + pojo.getLevel());
        }

    }


    final class ProtocolDispatcherMetadata
        implements SerialKeyEntity<ProtocolDispatcher, ProtocolDispatcherRecord, ProtocolDispatcherDao> {

        public static final ProtocolDispatcherMetadata INSTANCE = new ProtocolDispatcherMetadata();

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.ProtocolDispatcher table() {
            return Tables.PROTOCOL_DISPATCHER;
        }

        @Override
        public @NonNull Class<ProtocolDispatcher> modelClass() {
            return ProtocolDispatcher.class;
        }

        @Override
        public @NonNull Class<ProtocolDispatcherDao> daoClass() {
            return ProtocolDispatcherDao.class;
        }

        @Override
        public @NonNull String requestKeyName() {
            return "dispatcher_id";
        }

        @Override
        public @NonNull String singularKeyName() {
            return "dispatcher";
        }

        @Override
        public @NonNull ProtocolDispatcher onCreating(@NonNull RequestData reqData) throws IllegalArgumentException {
            final ProtocolDispatcher dispatcher = SerialKeyEntity.super.onCreating(reqData);
            Objects.requireNonNull(dispatcher.getProtocol(), "Missing dispatcher protocol");
            Objects.requireNonNull(dispatcher.getAction(), "Missing dispatcher action");
            Objects.requireNonNull(dispatcher.getEntity(), "Missing dispatcher entity");
            Objects.requireNonNull(dispatcher.getAddress(), "Missing dispatcher address");
            return dispatcher;
        }

    }


    final class SyncDispatcherMetadata
        implements SerialKeyEntity<SyncDispatcher, SyncDispatcherRecord, SyncDispatcherDao> {

        public static final SyncDispatcherMetadata INSTANCE = new SyncDispatcherMetadata();

        @Override
        public @NonNull com.nubeiot.iotdata.edge.model.tables.SyncDispatcher table() {
            return Tables.SYNC_DISPATCHER;
        }

        @Override
        public @NonNull Class<SyncDispatcher> modelClass() {
            return SyncDispatcher.class;
        }

        @Override
        public @NonNull Class<SyncDispatcherDao> daoClass() {
            return SyncDispatcherDao.class;
        }

    }

}
