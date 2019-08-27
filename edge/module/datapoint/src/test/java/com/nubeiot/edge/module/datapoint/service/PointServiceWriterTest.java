package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.dto.PointCategory;
import com.nubeiot.iotdata.dto.PointKind;
import com.nubeiot.iotdata.dto.PointType;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.unit.DataTypeCategory.AngularVelocity;
import com.nubeiot.iotdata.unit.DataTypeCategory.Base;
import com.nubeiot.iotdata.unit.DataTypeCategory.Temperature;
import com.nubeiot.iotdata.unit.DataTypeCategory.Velocity;
import com.nubeiot.iotdata.unit.UnitLabel;

public class PointServiceWriterTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_create_without_device(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT).put("message", "Missing device");
        final UUID id = UUID.randomUUID();
        final Point p1 = new Point().setId(id).setCode("TET_01");
        RequestData req = RequestData.builder().body(JsonPojo.from(p1).toJson()).build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_without_unit(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                              .put("message", "Missing point measure unit");
        final UUID id = UUID.randomUUID();
        final Point p1 = new Point().setId(id).setCode("TET_01").setDevice(id);
        RequestData req = RequestData.builder().body(JsonPojo.from(p1).toJson()).build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_with_non_exist_device(TestContext context) {
        final UUID id = UUID.randomUUID();
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message", "Not found resource with device_id=" + id);
        final Point p1 = new Point().setId(id).setCode("TET_01").setDevice(id);
        RequestData req = RequestData.builder()
                                     .body(JsonPojo.from(p1)
                                                   .toJson()
                                                   .put("unit", new JsonObject().put("type", Base.NUMBER.type())))
                                     .build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_by_device_and_non_exist_network(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message",
                                                   "Not found resource with network_id=" + PrimaryKey.DEVICE);
        Point p1 = new Point().setCode("TET_01").setMeasureUnit(Base.BOOLEAN.type());
        RequestData req = RequestData.builder()
                                     .body(JsonPojo.from(p1)
                                                   .toJson()
                                                   .put("device_id", PrimaryKey.DEVICE.toString())
                                                   .put("network_id", PrimaryKey.DEVICE.toString()))
                                     .build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_directly(TestContext context) {
        final UUID id = UUID.randomUUID();
        JsonObject data = new JsonObject(
            "{\"id\":\"" + id + "\",\"code\":\"TET_01\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\",\"category" +
            "\":\"GPIO\",\"unit\":{\"type\":\"meters_per_second\",\"symbol\":\"m/s\",\"category\":\"VELOCITY\"," +
            "\"label\":{\"= 10.0\":\"hah\",\"> 10.0\":\"xyz\"}},\"device\":\"" + PrimaryKey.DEVICE +
            "\",\"enabled\":true}");
        JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", data);
        Point p1 = new Point().setId(id)
                              .setCode("TET_01")
                              .setDevice(PrimaryKey.DEVICE)
                              .setKind(PointKind.INPUT)
                              .setType(PointType.DIGITAL)
                              .setCategory(PointCategory.GPIO)
                              .setMeasureUnit(Velocity.M_PER_SECOND.type())
                              .setMeasureUnitLabel(new UnitLabel().add("10", "hah").add(">10", "xyz"));
        RequestData req = RequestData.builder().body(JsonPojo.from(p1).toJson()).build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_by_device(TestContext context) {
        final UUID id = UUID.randomUUID();
        JsonObject data = new JsonObject(
            "{\"id\":\"" + id + "\",\"code\":\"TET_01\",\"kind\":\"OUTPUT\",\"type\":\"DIGITAL\",\"category" +
            "\":\"BACNET\",\"unit\":{\"type\":\"fahrenheit\",\"symbol\":\"°F\",\"category\":\"TEMPERATURE\"}," +
            "\"device\":\"" + PrimaryKey.DEVICE + "\",\"enabled\":true}");
        JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", data);
        Point p1 = new Point().setId(id)
                              .setCode("TET_01")
                              .setKind(PointKind.OUTPUT)
                              .setType(PointType.DIGITAL)
                              .setCategory(PointCategory.BACNET)
                              .setMeasureUnit(Temperature.FAHRENHEIT.type());
        RequestData req = RequestData.builder()
                                     .body(JsonPojo.from(p1).toJson().put("device_id", PrimaryKey.DEVICE.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_by_device_and_network(TestContext context) {
        final UUID id = UUID.randomUUID();
        JsonObject data = new JsonObject(
            "{\"id\":\"" + id + "\",\"code\":\"TET_01\",\"kind\":\"OUTPUT\",\"type\":\"DIGITAL\",\"category" +
            "\":\"BACNET\",\"unit\":{\"type\":\"fahrenheit\",\"symbol\":\"°F\",\"category\":\"TEMPERATURE\"}," +
            "\"device\":\"" + PrimaryKey.DEVICE + "\",\"network\":\"" + PrimaryKey.NETWORK + "\",\"enabled\":true}");
        JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", data);
        Point p1 = new Point().setId(id)
                              .setCode("TET_01")
                              .setKind(PointKind.OUTPUT)
                              .setType(PointType.DIGITAL)
                              .setCategory(PointCategory.BACNET)
                              .setMeasureUnit(Temperature.FAHRENHEIT.type());
        RequestData req = RequestData.builder()
                                     .body(JsonPojo.from(p1)
                                                   .toJson()
                                                   .put("device_id", PrimaryKey.DEVICE.toString())
                                                   .put("network_id", PrimaryKey.NETWORK.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_update_directly(TestContext context) {
        JsonObject body = new JsonObject(
            "{\"id\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\",\"code\":\"NUBE_HUMIDITY\",\"device\":\"" +
            PrimaryKey.DEVICE + "\",\"network\":\"" + PrimaryKey.NETWORK +
            "\",\"enabled\":false,\"category\":\"BACNET\"," + "\"kind\":\"OUTPUT\",\"type\":\"10K-THERMISTOR\"," +
            "\"unit\":{\"type\":\"bool\",\"category\":\"ALL\",\"label\":{\"= 0.0\":\"OFF\",\"= 1.0\":\"ON\"}}}");
        JsonObject expected = new JsonObject().put("action", EventAction.UPDATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", body);
        final Point p1 = new Point().setCode("NUBE_HUMIDITY")
                                    .setCategory(PointCategory.BACNET)
                                    .setDevice(PrimaryKey.DEVICE)
                                    .setNetwork(PrimaryKey.NETWORK)
                                    .setKind(PointKind.OUTPUT)
                                    .setType(PointType.THERMISTOR_10K)
                                    .setMeasureUnit(Base.BOOLEAN.type())
                                    .setMeasureUnitLabel(new UnitLabel().add("=1", "ON").add("=0", "OFF"))
                                    .setTransducer(null)
                                    .setEnabled(false);
        RequestData req = RequestData.builder()
                                     .body(p1.toJson().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.UPDATE, req);
    }

    @Test
    public void test_update_by_device(TestContext context) {
        JsonObject body = new JsonObject(
            "{\"id\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\",\"code\":\"NUBE_VELOCITY\",\"device\":\"" +
            PrimaryKey.DEVICE + "\",\"enabled\":false,\"category\":\"BACNET\",\"kind\":\"INPUT\",\"type\":\"UNKNOWN\"" +
            ",\"unit\":{\"type\":\"kilometers_per_hour\",\"category\":\"VELOCITY\",\"symbol\":\"km/h\"}}");
        JsonObject expected = new JsonObject().put("action", EventAction.UPDATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", body);
        final Point p1 = new Point().setCode("NUBE_VELOCITY")
                                    .setCategory(PointCategory.BACNET)
                                    .setKind(PointKind.INPUT)
                                    .setMeasureUnit(Velocity.KM_PER_HOUR.type())
                                    .setEnabled(false);
        RequestData req = RequestData.builder()
                                     .body(p1.toJson()
                                             .put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString())
                                             .put("device_id", PrimaryKey.DEVICE.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.UPDATE, req);
    }

    @Test
    public void test_update_by_device_network(TestContext context) {
        JsonObject body = new JsonObject(
            "{\"id\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\",\"code\":\"NUBE_VELOCITY\",\"device\":\"" +
            PrimaryKey.DEVICE + "\",\"enabled\":false,\"category\":\"BACNET\",\"kind\":\"INPUT\"," +
            "\"type\":\"UNKNOWN\",\"unit\":{\"type\":\"kilometers_per_hour\",\"category\":\"VELOCITY\"," +
            "\"symbol\":\"km/h\"}}");
        JsonObject expected = new JsonObject().put("action", EventAction.UPDATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", body);
        final Point p1 = new Point().setCode("NUBE_VELOCITY")
                                    .setCategory(PointCategory.BACNET)
                                    .setKind(PointKind.INPUT)
                                    .setMeasureUnit(Velocity.KM_PER_HOUR.type())
                                    .setEnabled(false);
        RequestData req = RequestData.builder()
                                     .body(p1.toJson()
                                             .put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString())
                                             .put("device_id", PrimaryKey.DEVICE.toString())
                                             .put("network_id", "GPIO"))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.UPDATE, req);
    }

    @Test
    public void test_patch_directly(TestContext context) {
        final JsonObject body = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_SWITCH))
                                        .toJson()
                                        .put("code", "NUBE_XX");
        body.remove("measure_unit");
        body.put("unit", Base.BOOLEAN.toJson());
        JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", body);
        final JsonObject p1 = JsonPojo.from(new Point().setCode("NUBE_XX")).toJson();
        RequestData req = RequestData.builder().body(p1.put("point_id", PrimaryKey.P_BACNET_SWITCH.toString())).build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.PATCH, req);
    }

    @Test
    public void test_patch_by_device(TestContext context) {
        final JsonObject body = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_SWITCH))
                                        .toJson()
                                        .put("code", "NUBE_XX");
        body.remove("measure_unit");
        body.remove("network");
        body.put("unit", Base.BOOLEAN.toJson());
        JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", body);
        final JsonObject p1 = JsonPojo.from(new Point().setCode("NUBE_XX")).toJson().put("network", (String) null);
        RequestData req = RequestData.builder()
                                     .body(p1.put("point_id", PrimaryKey.P_BACNET_SWITCH.toString())
                                             .put("device_id", PrimaryKey.DEVICE.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.PATCH, req);
    }

    @Test
    public void test_patch_by_device_network(TestContext context) {
        final JsonObject body = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_FAN))
                                        .toJson()
                                        .put("code", "NUBE_FAN");
        body.remove("measure_unit");
        body.put("unit", AngularVelocity.RAD_PER_SECOND.toJson());
        JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", body);
        Point p1 = new Point().setCode("NUBE_FAN")
                              .setCategory(PointCategory.BACNET)
                              .setMeasureUnit(AngularVelocity.RAD_PER_SECOND.type());
        RequestData req = RequestData.builder()
                                     .body(JsonPojo.from(p1)
                                                   .toJson()
                                                   .put("point_id", PrimaryKey.P_BACNET_FAN.toString())
                                                   .put("device_id", PrimaryKey.DEVICE.toString())
                                                   .put("network_id", PrimaryKey.NETWORK.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.PATCH, req);
    }

}
