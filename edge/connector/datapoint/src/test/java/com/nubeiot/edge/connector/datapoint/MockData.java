package com.nubeiot.edge.connector.datapoint;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.type.Label;
import com.nubeiot.edge.connector.datapoint.DataPointConfig.BuiltinData;
import com.nubeiot.iotdata.dto.EquipType;
import com.nubeiot.iotdata.dto.PointCategory;
import com.nubeiot.iotdata.dto.PointKind;
import com.nubeiot.iotdata.dto.PointType;
import com.nubeiot.iotdata.dto.TransducerCategory;
import com.nubeiot.iotdata.dto.TransducerType;
import com.nubeiot.iotdata.model.tables.interfaces.IDeviceEquip;
import com.nubeiot.iotdata.model.tables.interfaces.IEquipment;
import com.nubeiot.iotdata.model.tables.interfaces.IPoint;
import com.nubeiot.iotdata.model.tables.interfaces.IPointTag;
import com.nubeiot.iotdata.model.tables.interfaces.IThing;
import com.nubeiot.iotdata.model.tables.interfaces.ITransducer;
import com.nubeiot.iotdata.model.tables.pojos.Device;
import com.nubeiot.iotdata.model.tables.pojos.DeviceEquip;
import com.nubeiot.iotdata.model.tables.pojos.Equipment;
import com.nubeiot.iotdata.model.tables.pojos.Network;
import com.nubeiot.iotdata.model.tables.pojos.Point;
import com.nubeiot.iotdata.model.tables.pojos.PointTag;
import com.nubeiot.iotdata.model.tables.pojos.Thing;
import com.nubeiot.iotdata.model.tables.pojos.Transducer;
import com.nubeiot.iotdata.unit.DataType;

public final class MockData {

    public static class PrimaryKey {

        public static final UUID DEVICE = UUID.fromString("d7cd3f57-a188-4462-b959-df7a23994c92");
        public static final UUID P_GPIO_HUM = UUID.fromString("3bea3c91-850d-4409-b594-8ffb5aa6b8a0");
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


    public static final Device DEVICE = new Device().setId(PrimaryKey.DEVICE)
                                                    .setCode("NUBEIO_EDGE_28")
                                                    .setCustomerCode("XXX")
                                                    .setSiteCode("XXX-00001")
                                                    .setPolicyId("com.nubeio.xxx:XXX-00001")
                                                    .setDataVersion("0.0.1");
    public static final JsonObject MEASURE_UNITS = measures();
    public static final Network NETWORK = network();
    public static final List<Point> POINTS = points();
    public static final List<Equipment> EQUIPS = equips();
    public static final List<Transducer> TRANSDUCERS = transducers();
    public static final List<PointTag> TAGS = tags();
    public static final List<DeviceEquip> DEVICE_EQUIPS = deviceEquips();
    public static final List<Thing> THINGS = things();

    private static List<PointTag> tags() {
        return Arrays.asList(new PointTag().setPoint(PrimaryKey.P_GPIO_TEMP).setTagName("sensor").setTagValue("temp"),
                             new PointTag().setPoint(PrimaryKey.P_GPIO_TEMP)
                                           .setTagName("source")
                                           .setTagValue("droplet"),
                             new PointTag().setPoint(PrimaryKey.P_BACNET_TEMP).setTagName("sensor").setTagValue("temp"),
                             new PointTag().setPoint(PrimaryKey.P_GPIO_TEMP).setTagName("source").setTagValue("hvac"));
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
        final Point p1 = new Point().setId(PrimaryKey.P_GPIO_HUM)
                                    .setCode("2CB2B763_HUMIDITY")
                                    .setCategory(PointCategory.GPIO)
                                    .setDevice(DEVICE.getId())
                                    .setKind(PointKind.INPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(DataType.PERCENTAGE.type())
                                    .setTransducer(PrimaryKey.TRANS_HUMIDITY)
                                    .setEnabled(true)
                                    .setMaxScale((short) 10)
                                    .setMinScale((short) 0)
                                    .setOffset((short) 0)
                                    .setPrecision((short) 3);
        final Point p2 = new Point().setId(PrimaryKey.P_GPIO_TEMP)
                                    .setCode("2CB2B763_TEMP")
                                    .setCategory(PointCategory.GPIO)
                                    .setDevice(DEVICE.getId())
                                    .setKind(PointKind.INPUT)
                                    .setType(PointType.DIGITAL)
                                    .setMeasureUnit(DataType.CELSIUS.type())
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
                                    .setMeasureUnit(DataType.CELSIUS.type())
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
                                    .setMeasureUnit(DataType.RPM.type())
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
                                    .setMeasureUnit(DataType.BOOLEAN.type())
                                    .setTransducer(PrimaryKey.TRANS_SWITCH)
                                    .setEnabled(true);
        return Arrays.asList(p1, p2, p3, p4, p5);
    }

    private static JsonObject measures() {
        return new JsonObject("{\"units\":[{\"type\":\"number\",\"category\":\"ALL\"}," +
                              "{\"type\":\"percentage\",\"category\":\"ALL\",\"symbol\":\"%\"}," +
                              "{\"type\":\"voltage\",\"category\":\"ELECTRIC_POTENTIAL\",\"symbol\":\"V\"}," +
                              "{\"type\":\"celsius\",\"category\":\"TEMPERATURE\",\"symbol\":\"U+2103\"}," +
                              "{\"type\":\"bool\",\"category\":\"ALL\",\"possible_values\":{\"0.5\":[\"true\",\"on\"," +
                              "\"start\",\"1\"],\"0.0\":[\"false\",\"off\",\"stop\",\"0\",\"null\"]}}," +
                              "{\"type\":\"dBm\",\"category\":\"POWER\",\"symbol\":\"dBm\"}," +
                              "{\"type\":\"hPa\",\"category\":\"PRESSURE\",\"symbol\":\"hPa\"}," +
                              "{\"type\":\"lux\",\"category\":\"ILLUMINATION\",\"symbol\":\"lx\"}," +
                              "{\"type\":\"kWh\",\"category\":\"POWER\",\"symbol\":\"kWh\"}," +
                              "{\"type\":\"rpm\",\"category\":\"VELOCITY\",\"symbol\":\"rpm\"}]}");
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
                                    .setMeasureUnit(DataType.PERCENTAGE.type())
                                    .setProductCode("DROPLET-2CB2B763-H")
                                    .setProductLabel(Label.builder().label("Droplet Humidity").build());
        final Thing t2 = new Thing().setEquip(PrimaryKey.EQUIP_DROPLET)
                                    .setTransducer(PrimaryKey.TRANS_TEMP)
                                    .setMeasureUnit(DataType.CELSIUS.type())
                                    .setProductCode("DROPLET-2CB2B763-T")
                                    .setProductLabel(Label.builder().label("Droplet Temp").build());
        final Thing t3 = new Thing().setEquip(PrimaryKey.EQUIP_HVAC)
                                    .setTransducer(PrimaryKey.TRANS_TEMP)
                                    .setMeasureUnit(DataType.CELSIUS.type())
                                    .setProductCode("HVAC-XYZ-TEMP")
                                    .setProductLabel(Label.builder().label("HVAC Temp").build());
        final Thing t4 = new Thing().setEquip(PrimaryKey.EQUIP_HVAC)
                                    .setTransducer(PrimaryKey.TRANS_FAN)
                                    .setMeasureUnit(DataType.RPM.type())
                                    .setProductCode("HVAC-XYZ-FAN")
                                    .setProductLabel(Label.builder().label("HVAC Fan").build());
        final Thing t5 = new Thing().setEquip(PrimaryKey.EQUIP_HVAC)
                                    .setTransducer(PrimaryKey.TRANS_SWITCH)
                                    .setMeasureUnit(DataType.BOOLEAN.type())
                                    .setProductCode("HVAC-XYZ-FAN-CONTROL")
                                    .setProductLabel(Label.builder().label("HVAC Fan Control").build());
        return Arrays.asList(t1, t2, t3, t4, t5);
    }

    public static JsonObject dataOnlyDevice() {
        return BuiltinData.def().toJson().put("device", DEVICE.toJson());
    }

    public static JsonObject fullData() {
        return dataOnlyDevice().put("network", NETWORK.toJson())
                               .put("equipment", EQUIPS.stream().map(IEquipment::toJson).collect(Collectors.toList()))
                               .put("device_equip",
                                    DEVICE_EQUIPS.stream().map(IDeviceEquip::toJson).collect(Collectors.toList()))
                               .put("transducer",
                                    TRANSDUCERS.stream().map(ITransducer::toJson).collect(Collectors.toList()))
                               .put("thing", THINGS.stream().map(IThing::toJson).collect(Collectors.toList()))
                               .put("point", POINTS.stream().map(IPoint::toJson).collect(Collectors.toList()))
                               .put("tag", TAGS.stream().map(IPointTag::toJson).collect(Collectors.toList()));
    }

    public static void main(String[] args) {
        IntStream.range(0, 10).forEach(i -> System.out.println(UUID.randomUUID()));
        THINGS.stream().map(IThing::toJson).findFirst().ifPresent(t -> System.out.println(t.encodePrettily()));
    }

}
