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
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.type.Label;
import com.nubeiot.edge.module.datapoint.DataPointConfig.BuiltinData;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.DeviceEquipMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.EquipmentMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.HistoryDataMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.RealtimeSettingMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.TagPointMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.ThingMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.TransducerMetadata;
import com.nubeiot.iotdata.dto.EquipType;
import com.nubeiot.iotdata.dto.HistorySettingType;
import com.nubeiot.iotdata.dto.PointCategory;
import com.nubeiot.iotdata.dto.PointKind;
import com.nubeiot.iotdata.dto.PointPriorityValue;
import com.nubeiot.iotdata.dto.PointType;
import com.nubeiot.iotdata.dto.TransducerCategory;
import com.nubeiot.iotdata.dto.TransducerType;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.nubeiot.iotdata.edge.model.tables.pojos.DeviceEquip;
import com.nubeiot.iotdata.edge.model.tables.pojos.Equipment;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTag;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;
import com.nubeiot.iotdata.edge.model.tables.pojos.RealtimeSetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.Thing;
import com.nubeiot.iotdata.edge.model.tables.pojos.Transducer;
import com.nubeiot.iotdata.unit.DataTypeCategory.AngularVelocity;
import com.nubeiot.iotdata.unit.DataTypeCategory.Base;
import com.nubeiot.iotdata.unit.DataTypeCategory.Temperature;

public final class MockData {

    public static final Device DEVICE = new Device().setId(PrimaryKey.DEVICE)
                                                    .setCode("NUBEIO_EDGE_28")
                                                    .setCustomerCode("NUBEIO")
                                                    .setSiteCode("SYDNEY-00001");
    public static final JsonObject MEASURE_UNITS = measures();
    public static final Network NETWORK = network();
    public static final List<Equipment> EQUIPS = equips();
    public static final List<Transducer> TRANSDUCERS = transducers();
    public static final List<DeviceEquip> DEVICE_EQUIPS = deviceEquips();
    public static final List<Thing> THINGS = things();
    public static final List<Point> POINTS = points();
    public static final List<PointTag> TAGS = tags();
    public static final List<PointValueData> POINT_DATA = pointData();
    public static final List<HistorySetting> HISTORY_SETTINGS = historySettings();
    public static final List<PointHistoryData> HISTORY_DATA = historyData();
    public static final List<RealtimeSetting> RT_SETTINGS = rtSettings();

    private static List<RealtimeSetting> rtSettings() {
        return Collections.singletonList(new RealtimeSetting().setPoint(PrimaryKey.P_GPIO_TEMP).setEnabled(true));
    }

    private static List<PointValueData> pointData() {
        return Arrays.asList(new PointValueData().setPoint(PrimaryKey.P_GPIO_HUMIDITY)
                                                 .setPriority(8)
                                                 .setValue(10d)
                                                 .setPriorityValues(
                                                     new PointPriorityValue().add(5, 10).add(6, 9).add(8, 10)),
                             new PointValueData().setPoint(PrimaryKey.P_BACNET_TEMP)
                                                 .setPriority(3)
                                                 .setValue(24d)
                                                 .setPriorityValues(
                                                     new PointPriorityValue().add(3, 24d).add(9, 27.5).add(17, 25.5)),
                             new PointValueData().setPoint(PrimaryKey.P_BACNET_FAN)
                                                 .setPriority(7)
                                                 .setValue(260d)
                                                 .setPriorityValues(
                                                     new PointPriorityValue().add(5, 240d).add(7, 260d).add(17, 250)));
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
        return Arrays.asList(
            new HistorySetting().setPoint(PrimaryKey.P_GPIO_TEMP).setType(HistorySettingType.COV).setTolerance(1d),
            new HistorySetting().setPoint(PrimaryKey.P_BACNET_FAN)
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

    private static Network network() {
        final JsonObject metadata = new JsonObject(
            "{\"subnet_name\":\"subnet-A\",\"networkInterface\":\"docker0\",\"subnet\":\"172.17.0.1/16\"," +
            "\"broadcast\":\"172.17.255.255\",\"mac\":\"02:42:50:e1:cf:2b\",\"port\":47808}");
        return new Network().setId(PrimaryKey.NETWORK)
                            .setCode("network-1")
                            .setDevice(DEVICE.getId())
                            .setMetadata(metadata);
    }

    private static List<Equipment> equips() {
        return Arrays.asList(new Equipment().setId(PrimaryKey.EQUIP_DROPLET)
                                            .setCode("DROPLET_01")
                                            .setManufacturer("NubeIO")
                                            .setType(EquipType.DROPLET), new Equipment().setId(PrimaryKey.EQUIP_HVAC)
                                                                                        .setCode("HVAC_XYZ")
                                                                                        .setManufacturer("Lennox")
                                                                                        .setType(EquipType.HVAC));
    }

    private static List<Point> points() {
        final Point p1 = new Point().setId(PrimaryKey.P_GPIO_HUMIDITY)
                                    .setCode("2CB2B763_HUMIDITY")
                                    .setCategory(PointCategory.GPIO)
                                    .setDevice(DEVICE.getId())
                                    .setKind(PointKind.INPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(Base.PERCENTAGE.type())
                                    .setTransducer(PrimaryKey.TRANS_HUMIDITY)
                                    .setEnabled(true)
                                    .setMaxScale((short) 100)
                                    .setMinScale((short) 0)
                                    .setOffset((short) 0)
                                    .setPrecision((short) 3);
        final Point p2 = new Point().setId(PrimaryKey.P_GPIO_TEMP)
                                    .setCode("2CB2B763_TEMP")
                                    .setCategory(PointCategory.GPIO)
                                    .setDevice(DEVICE.getId())
                                    .setKind(PointKind.INPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(Temperature.CELSIUS.type())
                                    .setTransducer(PrimaryKey.TRANS_TEMP)
                                    .setEnabled(true)
                                    .setOffset((short) 0)
                                    .setPrecision((short) 3);
        final Point p3 = new Point().setId(PrimaryKey.P_BACNET_TEMP)
                                    .setCode("HVAC_01_TEMP")
                                    .setCategory(PointCategory.BACNET)
                                    .setDevice(DEVICE.getId())
                                    .setNetwork(NETWORK.getId())
                                    .setKind(PointKind.INPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(Temperature.CELSIUS.type())
                                    .setTransducer(PrimaryKey.TRANS_TEMP)
                                    .setEnabled(true)
                                    .setOffset((short) 0)
                                    .setPrecision((short) 3);
        final Point p4 = new Point().setId(PrimaryKey.P_BACNET_FAN)
                                    .setCode("HVAC_01_FAN")
                                    .setCategory(PointCategory.BACNET)
                                    .setDevice(DEVICE.getId())
                                    .setNetwork(NETWORK.getId())
                                    .setKind(PointKind.INPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(AngularVelocity.RPM.type())
                                    .setEnabled(true)
                                    .setOffset((short) 0)
                                    .setPrecision((short) 3);
        final Point p5 = new Point().setId(PrimaryKey.P_BACNET_SWITCH)
                                    .setCode("HVAC_01_FAN_CONTROL")
                                    .setCategory(PointCategory.BACNET)
                                    .setDevice(DEVICE.getId())
                                    .setNetwork(NETWORK.getId())
                                    .setKind(PointKind.OUTPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(Base.BOOLEAN.type())
                                    .setTransducer(PrimaryKey.TRANS_SWITCH)
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

    private static List<DeviceEquip> deviceEquips() {
        return Arrays.asList(new DeviceEquip().setDevice(DEVICE.getId()).setEquip(PrimaryKey.EQUIP_DROPLET),
                             new DeviceEquip().setDevice(DEVICE.getId())
                                              .setEquip(PrimaryKey.EQUIP_HVAC)
                                              .setNetwork(NETWORK.getId()));
    }

    private static List<Transducer> transducers() {
        final Transducer t1 = new Transducer().setId(PrimaryKey.TRANS_TEMP)
                                              .setCode("TEMP_01")
                                              .setType(TransducerType.SENSOR)
                                              .setCategory(TransducerCategory.TEMP);
        final Transducer t2 = new Transducer().setId(PrimaryKey.TRANS_HUMIDITY)
                                              .setCode("HUMIDITY_01")
                                              .setType(TransducerType.SENSOR)
                                              .setCategory(TransducerCategory.HUMIDITY);
        final Transducer t3 = new Transducer().setId(PrimaryKey.TRANS_SWITCH)
                                              .setCode("FAN_SWITCH_01")
                                              .setType(TransducerType.ACTUATOR)
                                              .setCategory(TransducerCategory.SWITCH);
        final Transducer t4 = new Transducer().setId(PrimaryKey.TRANS_FAN)
                                              .setCode("FAN_SWITCH_01")
                                              .setType(TransducerType.SENSOR)
                                              .setCategory(TransducerCategory.SWITCH);
        return Arrays.asList(t1, t2, t3, t4);
    }

    private static List<Thing> things() {
        final Thing t1 = new Thing().setEquip(PrimaryKey.EQUIP_DROPLET)
                                    .setTransducer(PrimaryKey.TRANS_HUMIDITY)
                                    .setMeasureUnit(Base.PERCENTAGE.type())
                                    .setProductCode("DROPLET-2CB2B763-H")
                                    .setProductLabel(Label.builder().label("Droplet Humidity").build());
        final Thing t2 = new Thing().setEquip(PrimaryKey.EQUIP_DROPLET)
                                    .setTransducer(PrimaryKey.TRANS_TEMP)
                                    .setMeasureUnit(Temperature.CELSIUS.type())
                                    .setProductCode("DROPLET-2CB2B763-T")
                                    .setProductLabel(Label.builder().label("Droplet Temp").build());
        final Thing t3 = new Thing().setEquip(PrimaryKey.EQUIP_HVAC)
                                    .setTransducer(PrimaryKey.TRANS_TEMP)
                                    .setMeasureUnit(Temperature.CELSIUS.type())
                                    .setProductCode("HVAC-XYZ-TEMP")
                                    .setProductLabel(Label.builder().label("HVAC Temp").build());
        final Thing t4 = new Thing().setEquip(PrimaryKey.EQUIP_HVAC)
                                    .setTransducer(PrimaryKey.TRANS_FAN)
                                    .setMeasureUnit(AngularVelocity.RPM.type())
                                    .setProductCode("HVAC-XYZ-FAN")
                                    .setProductLabel(Label.builder().label("HVAC Fan").build());
        final Thing t5 = new Thing().setEquip(PrimaryKey.EQUIP_HVAC)
                                    .setTransducer(PrimaryKey.TRANS_SWITCH)
                                    .setMeasureUnit(Base.BOOLEAN.type())
                                    .setProductCode("HVAC-XYZ-FAN-CONTROL")
                                    .setProductLabel(Label.builder().label("HVAC Fan Control").build());
        return Arrays.asList(t1, t2, t3, t4, t5);
    }

    public static Point search(UUID pointKey) {
        return POINTS.stream().filter(p -> p.getId().equals(pointKey)).findFirst().orElse(null);
    }

    public static PointValueData searchData(UUID pointKey) {
        return POINT_DATA.stream().filter(p -> p.getPoint().equals(pointKey)).findFirst().orElse(null);
    }

    public static JsonObject data_Device_Network() {
        return BuiltinData.def()
                          .toJson()
                          .put(DeviceMetadata.INSTANCE.singularKeyName(), DEVICE.toJson())
                          .put(NetworkMetadata.INSTANCE.singularKeyName(), NETWORK.toJson());
    }

    public static JsonObject data_Equip_Thing() {
        return data_Device_Network().put(EquipmentMetadata.INSTANCE.singularKeyName(), data(EQUIPS))
                                    .put(DeviceEquipMetadata.INSTANCE.singularKeyName(), data(DEVICE_EQUIPS))
                                    .put(TransducerMetadata.INSTANCE.singularKeyName(), data(TRANSDUCERS))
                                    .put(ThingMetadata.INSTANCE.singularKeyName(), data(THINGS));
    }

    public static JsonObject data_Point_Setting_Tag() {
        return data_Equip_Thing().put(PointMetadata.INSTANCE.singularKeyName(), data(POINTS))
                                 .put(TagPointMetadata.INSTANCE.singularKeyName(), data(TAGS))
                                 .put(PointValueMetadata.INSTANCE.singularKeyName(), data(POINT_DATA))
                                 .put(HistorySettingMetadata.INSTANCE.singularKeyName(), data(HISTORY_SETTINGS))
                                 .put(HistoryDataMetadata.INSTANCE.singularKeyName(), data(HISTORY_DATA))
                                 .put(RealtimeSettingMetadata.INSTANCE.singularKeyName(), data(RT_SETTINGS));
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

        public static final UUID DEVICE = UUID.fromString("d7cd3f57-a188-4462-b959-df7a23994c92");
        public static final UUID P_GPIO_HUMIDITY = UUID.fromString("3bea3c91-850d-4409-b594-8ffb5aa6b8a0");
        public static final UUID P_GPIO_TEMP = UUID.fromString("1efaf662-1333-48d1-a60f-8fc60f259f0e");
        public static final UUID P_BACNET_TEMP = UUID.fromString("edbe3acf-5fca-4672-b633-72aa73004917");
        public static final UUID P_BACNET_FAN = UUID.fromString("6997056d-4c1b-4d30-b205-969432f72a93");
        public static final UUID P_BACNET_SWITCH = UUID.fromString("463fbdf0-388d-447e-baef-96dbb8232dd7");
        public static final UUID TRANS_TEMP = UUID.fromString("08d66e92-f15d-4fdb-9ed5-fd165b212591");
        public static final UUID TRANS_SWITCH = UUID.fromString("76d34f4e-3b20-4776-99c7-d93d79d5b4a6");
        public static final UUID TRANS_HUMIDITY = UUID.fromString("5eb7da66-8013-4cc4-9608-ead768eca665");
        public static final UUID TRANS_FAN = UUID.fromString("388519ef-797f-49ca-a613-204b4587ef28");
        public static final UUID EQUIP_DROPLET = UUID.fromString("e43aa03a-4746-4fb5-815d-ee62f709b535");
        public static final UUID EQUIP_HVAC = UUID.fromString("28a4ba1b-154d-4bbf-8537-320be70e50e5");
        public static final UUID NETWORK = UUID.fromString("01fbb11e-45a6-479b-91a4-003534770c1c");

    }

}
