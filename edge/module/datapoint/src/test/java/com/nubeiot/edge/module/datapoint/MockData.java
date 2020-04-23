package com.nubeiot.edge.module.datapoint;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.github.zero.utils.UUID64;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.type.Label;
import com.nubeiot.edge.module.datapoint.DataPointConfig.BuiltinData;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeDeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderGroupMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.HistoryDataMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointTransducerMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ProtocolDispatcherMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.RealtimeSettingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.TagPointMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.TransducerMetadata;
import com.nubeiot.iotdata.dto.DeviceType;
import com.nubeiot.iotdata.dto.GroupLevel;
import com.nubeiot.iotdata.dto.HistorySettingType;
import com.nubeiot.iotdata.dto.PointKind;
import com.nubeiot.iotdata.dto.PointPriorityValue;
import com.nubeiot.iotdata.dto.PointType;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.dto.TransducerCategory;
import com.nubeiot.iotdata.dto.TransducerType;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.nubeiot.iotdata.edge.model.tables.pojos.Edge;
import com.nubeiot.iotdata.edge.model.tables.pojos.EdgeDevice;
import com.nubeiot.iotdata.edge.model.tables.pojos.Folder;
import com.nubeiot.iotdata.edge.model.tables.pojos.FolderGroup;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTag;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTransducer;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;
import com.nubeiot.iotdata.edge.model.tables.pojos.ProtocolDispatcher;
import com.nubeiot.iotdata.edge.model.tables.pojos.RealtimeSetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.Transducer;
import com.nubeiot.iotdata.unit.DataTypeCategory.AngularVelocity;
import com.nubeiot.iotdata.unit.DataTypeCategory.Base;
import com.nubeiot.iotdata.unit.DataTypeCategory.Temperature;

import lombok.NonNull;

public final class MockData {

    public static final Edge EDGE = new Edge().setId(PrimaryKey.EDGE)
                                              .setCode("NUBEIO_EDGE_28")
                                              .setCustomerCode("NUBEIO")
                                              .setSiteCode("SYDNEY-00001");
    public static final JsonObject MEASURE_UNITS = measures();
    public static final List<Network> NETWORKS = networks();
    public static final List<Device> DEVICES = devices();
    public static final List<Transducer> TRANSDUCERS = transducers();
    public static final List<EdgeDevice> EDGE_EQUIPS = edgeEquips();
    public static final List<PointTransducer> POINT_TRANSDUCERS = pointTransducers();
    public static final List<Point> POINTS = points();
    public static final List<PointTag> TAGS = tags();
    public static final List<PointValueData> POINT_DATA = pointData();
    public static final List<HistorySetting> HISTORY_SETTINGS = historySettings();
    public static final List<PointHistoryData> HISTORY_DATA = historyData();
    public static final List<RealtimeSetting> RT_SETTINGS = rtSettings();
    public static final List<ProtocolDispatcher> PROTOCOL_DISPATCHERS = protocolDispatchers();
    public static final List<Folder> FOLDERS = folders();
    public static final List<FolderGroup> FOLDER_GROUPS = folderGroups();

    private static List<Folder> folders() {
        return Arrays.asList(new Folder().setId(PrimaryKey.FOLDER_1).setEdgeId(PrimaryKey.EDGE).setName("folder-1"),
                             new Folder().setId(PrimaryKey.FOLDER_2).setEdgeId(PrimaryKey.EDGE).setName("folder-2"),
                             new Folder().setId(PrimaryKey.FOLDER_3).setEdgeId(PrimaryKey.EDGE).setName("folder-3"),
                             new Folder().setId(PrimaryKey.FOLDER_4).setEdgeId(PrimaryKey.EDGE).setName("folder-4"));
    }

    private static List<FolderGroup> folderGroups() {
        return Arrays.asList(new FolderGroup().setId(PrimaryKey.FOLDER_GROUP_1)
                                              .setFolderId(PrimaryKey.FOLDER_1)
                                              .setLevel(GroupLevel.NETWORK)
                                              .setDeviceId(PrimaryKey.DEVICE_DROPLET),
                             new FolderGroup().setId(PrimaryKey.FOLDER_GROUP_2)
                                              .setFolderId(PrimaryKey.FOLDER_1)
                                              .setLevel(GroupLevel.NETWORK)
                                              .setDeviceId(PrimaryKey.DEVICE_HVAC),
                             new FolderGroup().setId(PrimaryKey.FOLDER_GROUP_3)
                                              .setFolderId(PrimaryKey.FOLDER_2)
                                              .setLevel(GroupLevel.DEVICE)
                                              .setDeviceId(PrimaryKey.DEVICE_DROPLET),
                             new FolderGroup().setId(PrimaryKey.FOLDER_GROUP_4)
                                              .setFolderId(PrimaryKey.FOLDER_3)
                                              .setLevel(GroupLevel.DEVICE)
                                              .setDeviceId(PrimaryKey.DEVICE_DROPLET)
                                              .setPointId(PrimaryKey.P_GPIO_TEMP),
                             new FolderGroup().setId(PrimaryKey.FOLDER_GROUP_5)
                                              .setFolderId(PrimaryKey.FOLDER_3)
                                              .setLevel(GroupLevel.DEVICE)
                                              .setDeviceId(PrimaryKey.DEVICE_DROPLET)
                                              .setPointId(PrimaryKey.P_GPIO_HUMIDITY),
                             new FolderGroup().setId(PrimaryKey.FOLDER_GROUP_6)
                                              .setFolderId(PrimaryKey.FOLDER_4)
                                              .setLevel(GroupLevel.DEVICE)
                                              .setDeviceId(PrimaryKey.DEVICE_DROPLET)
                                              .setPointId(PrimaryKey.P_GPIO_TEMP));
    }

    private static List<ProtocolDispatcher> protocolDispatchers() {
        return Arrays.asList(new ProtocolDispatcher().setProtocol(Protocol.BACNET)
                                                     .setAction(EventAction.CREATE)
                                                     .setEntity(NetworkMetadata.INSTANCE.singularKeyName())
                                                     .setAddress(ProtocolDispatcherAddress.NETWORK)
                                                     .setState(State.ENABLED),
                             new ProtocolDispatcher().setProtocol(Protocol.BACNET)
                                                     .setAction(EventAction.REMOVE)
                                                     .setEntity(NetworkMetadata.INSTANCE.singularKeyName())
                                                     .setAddress(ProtocolDispatcherAddress.NETWORK)
                                                     .setState(State.ENABLED),
                             new ProtocolDispatcher().setProtocol(Protocol.BACNET)
                                                     .setAction(EventAction.CREATE)
                                                     .setEntity(DeviceMetadata.INSTANCE.singularKeyName())
                                                     .setAddress(ProtocolDispatcherAddress.DEVICE)
                                                     .setState(State.DISABLED),
                             new ProtocolDispatcher().setProtocol(Protocol.BACNET)
                                                     .setAction(EventAction.CREATE)
                                                     .setEntity(PointCompositeMetadata.INSTANCE.singularKeyName())
                                                     .setAddress(ProtocolDispatcherAddress.POINT)
                                                     .setState(State.ENABLED),
                             new ProtocolDispatcher().setProtocol(Protocol.BACNET)
                                                     .setAction(EventAction.CREATE)
                                                     .setEntity(TagPointMetadata.INSTANCE.singularKeyName())
                                                     .setAddress(ProtocolDispatcherAddress.TAG)
                                                     .setGlobal(true)
                                                     .setState(State.ENABLED));
    }

    private static List<RealtimeSetting> rtSettings() {
        return Collections.singletonList(new RealtimeSetting().setPoint(PrimaryKey.P_GPIO_TEMP).setEnabled(false));
    }

    private static List<PointValueData> pointData() {
        return Arrays.asList(new PointValueData().setPoint(PrimaryKey.P_GPIO_HUMIDITY)
                                                 .setPriority(5)
                                                 .setValue(10d)
                                                 .setPriorityValues(
                                                     new PointPriorityValue().add(5, 10).add(6, 9).add(8, 10)),
                             new PointValueData().setPoint(PrimaryKey.P_BACNET_TEMP)
                                                 .setPriority(3)
                                                 .setValue(24d)
                                                 .setPriorityValues(
                                                     new PointPriorityValue().add(3, 24d).add(9, 27.5).add(17, 25.5)),
                             new PointValueData().setPoint(PrimaryKey.P_BACNET_FAN)
                                                 .setPriority(2)
                                                 .setValue(240d)
                                                 .setPriorityValues(
                                                     new PointPriorityValue().add(2, 240d).add(7, 260d).add(16, 250)));
    }

    private static List<PointHistoryData> historyData() {
        return Arrays.asList(new PointHistoryData().setPoint(PrimaryKey.P_GPIO_HUMIDITY)
                                                   .setValue(30.0)
                                                   .setTime(OffsetDateTime.parse("2019-08-10T09:15:00.000Z")),
                             new PointHistoryData().setPoint(PrimaryKey.P_GPIO_HUMIDITY)
                                                   .setValue(35.0)
                                                   .setTime(OffsetDateTime.parse("2019-08-10T09:18:00.000Z")),
                             new PointHistoryData().setPoint(PrimaryKey.P_GPIO_HUMIDITY)
                                                   .setValue(32.0)
                                                   .setTime(OffsetDateTime.parse("2019-08-10T09:20:00.000Z")),
                             new PointHistoryData().setPoint(PrimaryKey.P_GPIO_HUMIDITY)
                                                   .setValue(42.0)
                                                   .setTime(OffsetDateTime.parse("2019-08-10T09:22:00.000Z")),
                             new PointHistoryData().setPoint(PrimaryKey.P_BACNET_TEMP)
                                                   .setValue(20.5)
                                                   .setTime(OffsetDateTime.parse("2019-08-10T09:15:15.000Z")),
                             new PointHistoryData().setPoint(PrimaryKey.P_BACNET_TEMP)
                                                   .setValue(20.8)
                                                   .setTime(OffsetDateTime.parse("2019-08-10T09:16:15.000Z")),
                             new PointHistoryData().setPoint(PrimaryKey.P_BACNET_TEMP)
                                                   .setValue(20.8)
                                                   .setTime(OffsetDateTime.parse("2019-08-10T09:17:15.000Z")),
                             new PointHistoryData().setPoint(PrimaryKey.P_BACNET_TEMP)
                                                   .setValue(20.6)
                                                   .setTime(OffsetDateTime.parse("2019-08-10T09:18:15.000Z")));
    }

    private static List<HistorySetting> historySettings() {
        return Arrays.asList(new HistorySetting().setPoint(PrimaryKey.P_GPIO_TEMP)
                                                 .setEnabled(false)
                                                 .setType(HistorySettingType.COV)
                                                 .setTolerance(1d),
                             new HistorySetting().setPoint(PrimaryKey.P_BACNET_FAN)
                                                 .setEnabled(false)
                                                 .setType(HistorySettingType.PERIOD)
                                                 .setSchedule("xyz"));
    }

    private static List<PointTag> tags() {
        return Arrays.asList(new PointTag().setPoint(PrimaryKey.P_GPIO_TEMP).setTagName("sensor").setTagValue("temp"),
                             new PointTag().setPoint(PrimaryKey.P_GPIO_TEMP)
                                           .setTagName("source")
                                           .setTagValue("droplet"),
                             new PointTag().setPoint(PrimaryKey.P_BACNET_TEMP).setTagName("sensor").setTagValue("temp"),
                             new PointTag().setPoint(PrimaryKey.P_BACNET_TEMP)
                                           .setTagName("source")
                                           .setTagValue("hvac"));
    }

    private static List<Network> networks() {
        final JsonObject metadata = new JsonObject(
            "{\"subnet_name\":\"subnet-A\",\"networkInterface\":\"docker0\",\"subnet\":\"172.17.0.1/16\"," +
            "\"broadcast\":\"172.17.255.255\",\"mac\":\"02:42:50:e1:cf:2b\",\"port\":47808}");
        return Arrays.asList(new Network().setId(PrimaryKey.BACNET_NETWORK)
                                          .setCode("DEMO-1")
                                          .setState(State.ENABLED)
                                          .setProtocol(Protocol.BACNET)
                                          .setEdge(EDGE.getId())
                                          .setMetadata(metadata), new Network().setId(PrimaryKey.DEFAULT_NETWORK)
                                                                               .setCode(NetworkMetadata.DEFAULT_CODE)
                                                                               .setState(State.ENABLED)
                                                                               .setProtocol(Protocol.WIRE)
                                                                               .setEdge(EDGE.getId()));
    }

    private static List<Device> devices() {
        return Arrays.asList(new Device().setId(PrimaryKey.DEVICE_DROPLET)
                                         .setCode("DROPLET_01")
                                         .setProtocol(Protocol.WIRE)
                                         .setManufacturer("NubeIO")
                                         .setType(DeviceType.DROPLET), new Device().setId(PrimaryKey.DEVICE_HVAC)
                                                                                   .setCode("HVAC_XYZ")
                                                                                   .setProtocol(Protocol.BACNET)
                                                                                   .setManufacturer("Lennox")
                                                                                   .setType(DeviceType.HVAC));
    }

    private static List<Point> points() {
        final Point p1 = new Point().setId(PrimaryKey.P_GPIO_HUMIDITY)
                                    .setCode("2CB2B763_HUMIDITY")
                                    .setProtocol(Protocol.WIRE)
                                    .setEdge(EDGE.getId())
                                    .setNetwork(PrimaryKey.DEFAULT_NETWORK)
                                    .setKind(PointKind.INPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(Base.PERCENTAGE.type())
                                    .setEnabled(true)
                                    .setMaxScale((short) 100)
                                    .setMinScale((short) 0)
                                    .setOffset((short) 0)
                                    .setPrecision((short) 3);
        final Point p2 = new Point().setId(PrimaryKey.P_GPIO_TEMP)
                                    .setCode("2CB2B763_TEMP")
                                    .setProtocol(Protocol.WIRE)
                                    .setEdge(EDGE.getId())
                                    .setNetwork(PrimaryKey.DEFAULT_NETWORK)
                                    .setKind(PointKind.INPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(Temperature.CELSIUS.type())
                                    .setEnabled(true)
                                    .setOffset((short) 0)
                                    .setPrecision((short) 3);
        final Point p3 = new Point().setId(PrimaryKey.P_BACNET_TEMP)
                                    .setCode("HVAC_01_TEMP")
                                    .setProtocol(Protocol.BACNET)
                                    .setEdge(EDGE.getId())
                                    .setNetwork(PrimaryKey.BACNET_NETWORK)
                                    .setKind(PointKind.INPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(Temperature.CELSIUS.type())
                                    .setEnabled(true)
                                    .setOffset((short) 0)
                                    .setPrecision((short) 3);
        final Point p4 = new Point().setId(PrimaryKey.P_BACNET_FAN)
                                    .setCode("HVAC_01_FAN")
                                    .setProtocol(Protocol.BACNET)
                                    .setEdge(EDGE.getId())
                                    .setNetwork(PrimaryKey.BACNET_NETWORK)
                                    .setKind(PointKind.INPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(AngularVelocity.RPM.type())
                                    .setEnabled(true)
                                    .setOffset((short) 0)
                                    .setPrecision((short) 3);
        final Point p5 = new Point().setId(PrimaryKey.P_BACNET_SWITCH)
                                    .setCode("HVAC_01_FAN_CONTROL")
                                    .setProtocol(Protocol.BACNET)
                                    .setEdge(EDGE.getId())
                                    .setNetwork(PrimaryKey.BACNET_NETWORK)
                                    .setKind(PointKind.OUTPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(Base.BOOLEAN.type())
                                    .setEnabled(true);
        return Arrays.asList(p1, p2, p3, p4, p5);
    }

    private static JsonObject measures() {
        return new JsonObject("{\"units\":[{\"type\":\"number\",\"category\":\"ALL\"},{\"type\":\"percentage\"," +
                              "\"category\":\"ALL\",\"symbol\":\"%\"},{\"type\":\"bool\",\"category\":\"ALL\"}," +
                              "{\"type\":\"revolutions_per_minute\",\"category\":\"ANGULAR_VELOCITY\"," +
                              "\"symbol\":\"rpm\"},{\"type\":\"radians_per_second\"," +
                              "\"category\":\"ANGULAR_VELOCITY\",\"symbol\":\"rad/s\"},{\"type\":\"volt\"," +
                              "\"category\":\"ELECTRIC_POTENTIAL\",\"symbol\":\"V\"},{\"type\":\"lux\"," +
                              "\"category\":\"ILLUMINATION\",\"symbol\":\"lx\"},{\"type\":\"kilowatt_hour\"," +
                              "\"category\":\"POWER\",\"symbol\":\"kWh\"},{\"type\":\"dBm\",\"category\":\"POWER\"," +
                              "\"symbol\":\"dBm\"},{\"type\":\"hectopascal\",\"category\":\"PRESSURE\"," +
                              "\"symbol\":\"hPa\"},{\"type\":\"fahrenheit\",\"category\":\"TEMPERATURE\"," +
                              "\"symbol\":\"°F\"},{\"type\":\"celsius\",\"category\":\"TEMPERATURE\"," +
                              "\"symbol\":\"°C\"},{\"type\":\"meters_per_second\",\"category\":\"VELOCITY\"," +
                              "\"symbol\":\"m/s\"},{\"type\":\"kilometers_per_hour\",\"category\":\"VELOCITY\"," +
                              "\"symbol\":\"km/h\"},{\"type\":\"miles_per_hour\",\"category\":\"VELOCITY\"," +
                              "\"symbol\":\"mph\"}]}");
    }

    private static List<EdgeDevice> edgeEquips() {
        return Arrays.asList(new EdgeDevice().setEdgeId(EDGE.getId())
                                             .setDeviceId(PrimaryKey.DEVICE_DROPLET)
                                             .setNetworkId(PrimaryKey.DEFAULT_NETWORK),
                             new EdgeDevice().setEdgeId(EDGE.getId())
                                             .setDeviceId(PrimaryKey.DEVICE_HVAC)
                                             .setNetworkId(PrimaryKey.BACNET_NETWORK));
    }

    private static List<Transducer> transducers() {
        final Transducer t1 = new Transducer().setId(PrimaryKey.TRANSDUCER_TEMP_DROPLET)
                                              .setDeviceId(PrimaryKey.DEVICE_DROPLET)
                                              .setCode("DROPLET-2CB2B763-T")
                                              .setType(TransducerType.SENSOR)
                                              .setCategory(TransducerCategory.TEMP)
                                              .setMeasureUnit(Temperature.CELSIUS.type())
                                              .setLabel(Label.builder().label("Droplet Temp").build());
        final Transducer t2 = new Transducer().setId(PrimaryKey.TRANSDUCER_HUMIDITY_DROPLET)
                                              .setDeviceId(PrimaryKey.DEVICE_DROPLET)
                                              .setCode("DROPLET-2CB2B763-H")
                                              .setType(TransducerType.SENSOR)
                                              .setCategory(TransducerCategory.HUMIDITY)
                                              .setMeasureUnit(Base.PERCENTAGE.type())
                                              .setDeviceId(PrimaryKey.DEVICE_DROPLET)
                                              .setLabel(Label.builder().label("Droplet Humidity").build());
        final Transducer t3 = new Transducer().setId(PrimaryKey.TRANSDUCER_SWITCH_HVAC)
                                              .setDeviceId(PrimaryKey.DEVICE_HVAC)
                                              .setCode("HVAC-XYZ-FAN-CONTROL")
                                              .setType(TransducerType.ACTUATOR)
                                              .setCategory(TransducerCategory.SWITCH)
                                              .setMeasureUnit(Base.BOOLEAN.type())
                                              .setLabel(Label.builder().label("HVAC Fan Control").build());
        final Transducer t4 = new Transducer().setId(PrimaryKey.TRANSDUCER_FAN_HVAC)
                                              .setDeviceId(PrimaryKey.DEVICE_HVAC)
                                              .setCode("HVAC-XYZ-FAN")
                                              .setType(TransducerType.SENSOR)
                                              .setCategory(TransducerCategory.VELOCITY)
                                              .setMeasureUnit(AngularVelocity.RPM.type())
                                              .setLabel(Label.builder().label("HVAC Fan").build());
        final Transducer t5 = new Transducer().setId(PrimaryKey.TRANSDUCER_TEMP_HVAC)
                                              .setDeviceId(PrimaryKey.DEVICE_HVAC)
                                              .setCode("HVAC-XYZ-TEMP-01")
                                              .setType(TransducerType.SENSOR)
                                              .setCategory(TransducerCategory.TEMP)
                                              .setMeasureUnit(Temperature.CELSIUS.type())
                                              .setLabel(Label.builder().label("HVAC Temp").build());
        return Arrays.asList(t1, t2, t3, t4, t5);
    }

    private static List<PointTransducer> pointTransducers() {
        final PointTransducer t1 = new PointTransducer().setPointId(PrimaryKey.P_GPIO_HUMIDITY)
                                                        .setTransducerId(PrimaryKey.TRANSDUCER_HUMIDITY_DROPLET)
                                                        .setDeviceId(PrimaryKey.DEVICE_DROPLET)
                                                        .setNetworkId(PrimaryKey.DEFAULT_NETWORK)
                                                        .setEdgeId(PrimaryKey.EDGE)
                                                        .setComputedTransducer(
                                                            PointTransducerMetadata.genComputedTransducer(
                                                                TransducerType.SENSOR,
                                                                PrimaryKey.TRANSDUCER_HUMIDITY_DROPLET));
        final PointTransducer t2 = new PointTransducer().setPointId(PrimaryKey.P_GPIO_TEMP)
                                                        .setTransducerId(PrimaryKey.TRANSDUCER_TEMP_DROPLET)
                                                        .setDeviceId(PrimaryKey.DEVICE_DROPLET)
                                                        .setNetworkId(PrimaryKey.DEFAULT_NETWORK)
                                                        .setEdgeId(PrimaryKey.EDGE)
                                                        .setComputedTransducer(
                                                            PointTransducerMetadata.genComputedTransducer(
                                                                TransducerType.SENSOR,
                                                                PrimaryKey.TRANSDUCER_TEMP_DROPLET));
        final PointTransducer t3 = new PointTransducer().setPointId(PrimaryKey.P_BACNET_TEMP)
                                                        .setTransducerId(PrimaryKey.TRANSDUCER_TEMP_HVAC)
                                                        .setDeviceId(PrimaryKey.DEVICE_HVAC)
                                                        .setNetworkId(PrimaryKey.BACNET_NETWORK)
                                                        .setEdgeId(PrimaryKey.EDGE)
                                                        .setComputedTransducer(
                                                            PointTransducerMetadata.genComputedTransducer(
                                                                TransducerType.SENSOR,
                                                                PrimaryKey.TRANSDUCER_TEMP_HVAC));
        final PointTransducer t4 = new PointTransducer().setPointId(PrimaryKey.P_BACNET_FAN)
                                                        .setTransducerId(PrimaryKey.TRANSDUCER_FAN_HVAC)
                                                        .setDeviceId(PrimaryKey.DEVICE_HVAC)
                                                        .setNetworkId(PrimaryKey.BACNET_NETWORK)
                                                        .setEdgeId(PrimaryKey.EDGE)
                                                        .setComputedTransducer(
                                                            PointTransducerMetadata.genComputedTransducer(
                                                                TransducerType.SENSOR, PrimaryKey.TRANSDUCER_FAN_HVAC));
        final PointTransducer t5 = new PointTransducer().setPointId(PrimaryKey.P_BACNET_SWITCH)
                                                        .setTransducerId(PrimaryKey.TRANSDUCER_SWITCH_HVAC)
                                                        .setDeviceId(PrimaryKey.DEVICE_HVAC)
                                                        .setDeviceId(PrimaryKey.DEVICE_HVAC)
                                                        .setNetworkId(PrimaryKey.BACNET_NETWORK)
                                                        .setEdgeId(PrimaryKey.EDGE)
                                                        .setComputedTransducer(
                                                            PointTransducerMetadata.genComputedTransducer(
                                                                TransducerType.ACTUATOR,
                                                                PrimaryKey.TRANSDUCER_SWITCH_HVAC));
        return Arrays.asList(t1, t2, t3, t4, t5);
    }

    public static Network searchNetwork(@NonNull UUID network) {
        return NETWORKS.stream().filter(p -> p.getId().equals(network)).findFirst().map(Network::new).orElse(null);
    }

    public static Point search(@NonNull UUID pointKey) {
        return POINTS.stream().filter(p -> p.getId().equals(pointKey)).findFirst().map(Point::new).orElse(null);
    }

    public static PointValueData searchData(@NonNull UUID pointKey) {
        return POINT_DATA.stream()
                         .filter(p -> p.getPoint().equals(pointKey))
                         .findFirst()
                         .map(PointValueData::new)
                         .orElse(null);
    }

    public static Device searchDevice(@NonNull UUID deviceKey) {
        return DEVICES.stream().filter(p -> p.getId().equals(deviceKey)).findFirst().map(Device::new).orElse(null);
    }

    public static JsonObject data_Edge_Network() {
        return BuiltinData.def()
                          .toJson()
                          .put(EdgeMetadata.INSTANCE.singularKeyName(), EDGE.toJson())
                          .put(NetworkMetadata.INSTANCE.singularKeyName(), data(NETWORKS));
    }

    public static JsonObject data_Device_Equip_Transducer() {
        return data_Edge_Network().put(DeviceMetadata.INSTANCE.singularKeyName(), data(DEVICES))
                                  .put(EdgeDeviceMetadata.INSTANCE.singularKeyName(), data(EDGE_EQUIPS))
                                  .put(TransducerMetadata.INSTANCE.singularKeyName(), data(TRANSDUCERS));
    }

    public static JsonObject data_Point_Setting_Tag() {
        return data_Point_Device().put(TagPointMetadata.INSTANCE.singularKeyName(), data(TAGS))
                                  .put(PointValueMetadata.INSTANCE.singularKeyName(), data(POINT_DATA))
                                  .put(HistorySettingMetadata.INSTANCE.singularKeyName(), data(HISTORY_SETTINGS))
                                  .put(HistoryDataMetadata.INSTANCE.singularKeyName(), data(HISTORY_DATA))
                                  .put(RealtimeSettingMetadata.INSTANCE.singularKeyName(), data(RT_SETTINGS));
    }

    public static JsonObject data_Point_Device() {
        return data_Device_Equip_Transducer().put(PointCompositeMetadata.INSTANCE.singularKeyName(), data(POINTS))
                                             .put(PointTransducerMetadata.INSTANCE.singularKeyName(),
                                                  data(POINT_TRANSDUCERS));
    }

    public static JsonObject data_Protocol_Dispatcher() {
        return data_Edge_Network().put(ProtocolDispatcherMetadata.INSTANCE.singularKeyName(),
                                       data(PROTOCOL_DISPATCHERS));
    }

    public static JsonObject data_Folder_Group() {
        return data_Point_Device().put(FolderMetadata.INSTANCE.singularKeyName(), data(FOLDERS))
                                  .put(FolderGroupMetadata.INSTANCE.singularKeyName(), data(FOLDER_GROUPS));
    }

    public static <T extends VertxPojo> List<JsonObject> data(List<T> list) {
        return list.stream().map(VertxPojo::toJson).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        IntStream.range(0, 10).forEach(i -> System.out.println(UUID.randomUUID()));
        System.out.println(OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
        System.out.println(JsonData.tryParse(data_Point_Setting_Tag()).toJson(JsonPojo.MAPPER).encodePrettily());
    }

    public static final class PrimaryKey {

        public static final UUID EDGE = UUID.fromString("d7cd3f57-a188-4462-b959-df7a23994c92");
        public static final UUID DEFAULT_NETWORK = UUID.fromString("e3eab951-932e-4fcc-a925-08b31e1014a0");
        public static final UUID BACNET_NETWORK = UUID.fromString("01fbb11e-45a6-479b-91a4-003534770c1c");

        public static final UUID P_GPIO_HUMIDITY = UUID.fromString("3bea3c91-850d-4409-b594-8ffb5aa6b8a0");
        public static final UUID P_GPIO_TEMP = UUID.fromString("1efaf662-1333-48d1-a60f-8fc60f259f0e");
        public static final UUID P_BACNET_TEMP = UUID.fromString("edbe3acf-5fca-4672-b633-72aa73004917");
        public static final UUID P_BACNET_FAN = UUID.fromString("6997056d-4c1b-4d30-b205-969432f72a93");
        public static final UUID P_BACNET_SWITCH = UUID.fromString("463fbdf0-388d-447e-baef-96dbb8232dd7");

        public static final UUID DEVICE_DROPLET = UUID.fromString("e43aa03a-4746-4fb5-815d-ee62f709b535");
        public static final UUID DEVICE_HVAC = UUID.fromString("28a4ba1b-154d-4bbf-8537-320be70e50e5");

        public static final UUID TRANSDUCER_TEMP_DROPLET = UUID.fromString("08d66e92-f15d-4fdb-9ed5-fd165b212591");
        public static final UUID TRANSDUCER_HUMIDITY_DROPLET = UUID.fromString("5eb7da66-8013-4cc4-9608-ead768eca665");
        public static final UUID TRANSDUCER_FAN_HVAC = UUID.fromString("388519ef-797f-49ca-a613-204b4587ef28");
        public static final UUID TRANSDUCER_TEMP_HVAC = UUID.fromString("960f5686-1dd6-48c0-bb5b-bec79c2b5788");
        public static final UUID TRANSDUCER_SWITCH_HVAC = UUID.fromString("76d34f4e-3b20-4776-99c7-d93d79d5b4a6");

        public static final String FOLDER_1 = UUID64.uuidToBase64(
            UUID.fromString("256b2c9a-04ce-4985-a561-a64651796c4b"));
        public static final String FOLDER_2 = UUID64.uuidToBase64(
            UUID.fromString("f0e0c118-784b-46c9-95cd-2277a5d65163"));
        public static final String FOLDER_3 = UUID64.uuidToBase64(
            UUID.fromString("a58667aa-cb3f-4bf4-a9e0-878aebb793e2"));
        public static final String FOLDER_4 = UUID64.uuidToBase64(
            UUID.fromString("7df8281c-1f7c-429b-af17-d9c6db3ffea8"));

        public static final String FOLDER_GROUP_1 = UUID64.uuidToBase64(
            UUID.fromString("fc8bb382-9b2e-4fbb-971d-9d0f797879c5"));
        public static final String FOLDER_GROUP_2 = UUID64.uuidToBase64(
            UUID.fromString("f0feaab1-74a6-40fc-9741-6c3abd76ab34"));
        public static final String FOLDER_GROUP_3 = UUID64.uuidToBase64(
            UUID.fromString("d74edfc3-78ee-45a1-848a-8f3680cb5151"));
        public static final String FOLDER_GROUP_4 = UUID64.uuidToBase64(
            UUID.fromString("3dd08f81-9f21-462c-b250-4feab2ca862e"));
        public static final String FOLDER_GROUP_5 = UUID64.uuidToBase64(
            UUID.fromString("60bb45aa-3a20-406a-af24-e3d31c08c86b"));
        public static final String FOLDER_GROUP_6 = UUID64.uuidToBase64(
            UUID.fromString("60400b6a-7ddd-42b6-94b3-793a5d4e6ff8"));
        public static final String FOLDER_GROUP_7 = UUID64.uuidToBase64(
            UUID.fromString("6d63f967-37af-4c16-acce-dd5e142d4133"));

    }


    public static final class ProtocolDispatcherAddress {

        public static final String NETWORK = "bacnet.dispatcher.network";
        public static final String DEVICE = "bacnet.dispatcher.device";
        public static final String POINT = "bacnet.dispatcher.point";
        public static final String TAG = "bacnet.dispatcher.tag";

    }

}
