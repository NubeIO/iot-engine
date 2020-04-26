package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestFilter.Filters;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.dto.PointKind;
import com.nubeiot.iotdata.dto.PointType;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.unit.DataType;
import com.nubeiot.iotdata.unit.DataTypeCategory.AngularVelocity;
import com.nubeiot.iotdata.unit.DataTypeCategory.Base;
import com.nubeiot.iotdata.unit.DataTypeCategory.Temperature;
import com.nubeiot.iotdata.unit.DataTypeCategory.Velocity;
import com.nubeiot.iotdata.unit.UnitAlias;

public class PointServiceWriterTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_create_with_new_unit(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message", "Not found resource with unit_type=xx");
        final UUID id = UUID.randomUUID();
        final DataType dt = DataType.factory("xx", "ab");
        final Point p1 = new Point().setId(id).setCode("TET_01");
        RequestData req = RequestData.builder().body(JsonPojo.from(p1).toJson().put("unit", dt.toJson())).build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_without_unit(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "Point measure unit is mandatory");
        final UUID id = UUID.randomUUID();
        final Point p1 = new Point().setId(id).setCode("TET_01");
        final RequestData req = RequestData.builder().body(JsonPojo.from(p1).toJson()).build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_with_non_exist_edge(TestContext context) {
        final UUID id = UUID.randomUUID();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message", "Not found resource with edge_id=" + id);
        final Point p1 = new Point().setId(id).setCode("TET_01").setEdge(id);
        RequestData req = RequestData.builder()
                                     .body(JsonPojo.from(p1)
                                                   .toJson()
                                                   .put("unit", new JsonObject().put("type", Base.NUMBER.type())))
                                     .build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_by_edge_and_non_exist_network(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message", "Not found resource with network_id=" + PrimaryKey.EDGE);
        Point p1 = new Point().setCode("TET_01").setMeasureUnit(Base.BOOLEAN.type());
        RequestData req = RequestData.builder()
                                     .body(JsonPojo.from(p1)
                                                   .toJson()
                                                   .put("edge_id", PrimaryKey.EDGE.toString())
                                                   .put("network_id", PrimaryKey.EDGE.toString()))
                                     .build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_directly(TestContext context) {
        final UUID id = UUID.randomUUID();
        final JsonObject data = new JsonObject(
            "{\"id\":\"" + id + "\",\"code\":\"TET_01\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\",\"protocol" +
            "\":\"WIRE\",\"unit\":{\"type\":\"meters_per_second\",\"symbol\":\"m/s\",\"category\":\"VELOCITY\"," +
            "\"alias\":{\"= 10.0\":\"hah\",\"> 10.0\":\"xyz\"}},\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" +
            PrimaryKey.DEFAULT_NETWORK + "\",\"enabled\":true" + "}");
        JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", data);
        Point p1 = new Point().setId(id)
                              .setCode("TET_01")
                              .setEdge(PrimaryKey.EDGE)
                              .setKind(PointKind.INPUT)
                              .setType(PointType.DIGITAL)
                              .setProtocol(Protocol.WIRE)
                              .setMeasureUnit(Velocity.M_PER_SECOND.type())
                              .setUnitAlias(new UnitAlias().add("10", "hah").add(">10", "xyz"));
        RequestData req = RequestData.builder().body(JsonPojo.from(p1).toJson()).build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_by_edge(TestContext context) {
        final UUID id = UUID.randomUUID();
        JsonObject data = new JsonObject(
            "{\"id\":\"" + id + "\",\"code\":\"TET_01\",\"kind\":\"OUTPUT\",\"type\":\"DIGITAL\",\"protocol" +
            "\":\"BACNET\",\"unit\":{\"type\":\"fahrenheit\",\"symbol\":\"°F\",\"category\":\"TEMPERATURE\"}," +
            "\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.DEFAULT_NETWORK +
            "\",\"enabled\":true}");
        JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", data);
        Point p1 = new Point().setId(id)
                              .setCode("TET_01")
                              .setKind(PointKind.OUTPUT)
                              .setType(PointType.DIGITAL)
                              .setProtocol(Protocol.BACNET)
                              .setMeasureUnit(Temperature.FAHRENHEIT.type());
        RequestData req = RequestData.builder()
                                     .body(JsonPojo.from(p1).toJson().put("edge_id", PrimaryKey.EDGE.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_by_edge_and_network(TestContext context) {
        final UUID id = UUID.randomUUID();
        JsonObject data = new JsonObject(
            "{\"id\":\"" + id + "\",\"code\":\"TET_01\",\"kind\":\"OUTPUT\",\"type\":\"DIGITAL\",\"protocol" +
            "\":\"BACNET\",\"unit\":{\"type\":\"fahrenheit\",\"symbol\":\"°F\",\"category\":\"TEMPERATURE\"}," +
            "\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.BACNET_NETWORK + "\",\"enabled\":true}");
        JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", data);
        Point p1 = new Point().setId(id)
                              .setCode("TET_01")
                              .setKind(PointKind.OUTPUT)
                              .setType(PointType.DIGITAL)
                              .setProtocol(Protocol.BACNET)
                              .setMeasureUnit(Temperature.FAHRENHEIT.type());
        RequestData req = RequestData.builder()
                                     .body(JsonPojo.from(p1)
                                                   .toJson()
                                                   .put("edge_id", PrimaryKey.EDGE.toString())
                                                   .put("network_id", PrimaryKey.BACNET_NETWORK.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_update_directly(TestContext context) {
        final JsonObject body = new JsonObject(
            "{\"id\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\",\"code\":\"NUBE_HUMIDITY\",\"edge\":\"" + PrimaryKey.EDGE +
            "\",\"network\":\"" + PrimaryKey.BACNET_NETWORK + "\",\"enabled\":false,\"protocol\":\"BACNET\"," +
            "\"kind\":\"OUTPUT\",\"type\":\"10K-THERMISTOR\",\"unit\":{\"type\":\"bool\",\"category\":\"ALL\"," +
            "\"alias\":{\"= 0.0\":\"OFF\",\"= 1.0\":\"ON\"}}}");
        final JsonObject expected = new JsonObject().put("action", EventAction.UPDATE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", body);
        final Point p1 = new Point().setCode("NUBE_HUMIDITY")
                                    .setProtocol(Protocol.BACNET)
                                    .setEdge(PrimaryKey.EDGE)
                                    .setNetwork(PrimaryKey.BACNET_NETWORK)
                                    .setKind(PointKind.OUTPUT)
                                    .setType(PointType.THERMISTOR_10K)
                                    .setMeasureUnit(Base.BOOLEAN.type())
                                    .setUnitAlias(new UnitAlias().add("=1", "ON").add("=0", "OFF"))
                                    .setEnabled(false);
        final RequestData req = RequestData.builder()
                                           .body(p1.toJson().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                           .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.UPDATE, req);
    }

    @Test
    public void test_update_by_edge_and_network(TestContext context) {
        final JsonObject body = new JsonObject(
            "{\"id\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\",\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" +
            PrimaryKey.DEFAULT_NETWORK + "\",\"code\":\"NUBE_VELOCITY\",\"enabled\":false," +
            "\"protocol\":\"BACNET\",\"kind\":\"INPUT\",\"type\":\"UNKNOWN\"," +
            "\"unit\":{\"type\":\"kilometers_per_hour\",\"category\":\"VELOCITY\",\"symbol\":\"km/h\"}}");
        final JsonObject expected = new JsonObject().put("action", EventAction.UPDATE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", body);
        final Point p1 = new Point().setCode("NUBE_VELOCITY")
                                    .setProtocol(Protocol.BACNET)
                                    .setKind(PointKind.INPUT)
                                    .setMeasureUnit(Velocity.KM_PER_HOUR.type())
                                    .setEnabled(false);
        final RequestData req = RequestData.builder()
                                           .body(p1.toJson()
                                                   .put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString())
                                                   .put("edge_id", PrimaryKey.EDGE.toString())
                                                   .put("network_id", "local"))
                                           .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.UPDATE, req);
    }

    @Test
    public void test_update_by_edge_missing_network(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "Point must be assigned to Network");
        final Point p1 = new Point().setCode("NUBE_VELOCITY")
                                    .setProtocol(Protocol.BACNET)
                                    .setKind(PointKind.INPUT)
                                    .setMeasureUnit(Velocity.KM_PER_HOUR.type())
                                    .setEnabled(false);
        final RequestData req = RequestData.builder()
                                           .body(p1.toJson()
                                                   .put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString())
                                                   .put("edge_id", PrimaryKey.EDGE.toString()))
                                           .build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.UPDATE, req);
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
    public void test_patch_by_edge(TestContext context) {
        final JsonObject body = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_SWITCH))
                                        .toJson()
                                        .put("code", "NUBE_XX");
        body.remove("measure_unit");
        body.put("unit", Base.BOOLEAN.toJson());
        JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", body);
        final JsonObject p1 = JsonPojo.from(new Point().setCode("NUBE_XX")).toJson();
        RequestData req = RequestData.builder()
                                     .body(p1.put("point_id", PrimaryKey.P_BACNET_SWITCH.toString())
                                             .put("edge_id", PrimaryKey.EDGE.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.PATCH, req);
    }

    @Test
    public void test_patch_by_edge_network(TestContext context) {
        final JsonObject body = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_FAN))
                                        .toJson()
                                        .put("code", "NUBE_FAN");
        body.remove("measure_unit");
        body.put("unit", AngularVelocity.RAD_PER_SECOND.toJson());
        JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", body);
        Point p1 = new Point().setCode("NUBE_FAN")
                              .setProtocol(Protocol.BACNET)
                              .setMeasureUnit(AngularVelocity.RAD_PER_SECOND.type());
        RequestData req = RequestData.builder()
                                     .body(JsonPojo.from(p1)
                                                   .toJson()
                                                   .put("point_id", PrimaryKey.P_BACNET_FAN.toString())
                                                   .put("edge_id", PrimaryKey.EDGE.toString())
                                                   .put("network_id", PrimaryKey.BACNET_NETWORK.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.PATCH, req);
    }

    @Test
    public void test_delete_point_without_force(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.BEING_USED)
                                                    .put("message", "Resource with point_id=" + PrimaryKey.P_GPIO_TEMP +
                                                                    " is using by another resource");
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_TEMP.toString()))
                                           .build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.REMOVE, req);
    }

    @Test
    public void test_delete_point_with_force(TestContext context) {
        final JsonObject body = new JsonObject(
            "{\"id\":\"1efaf662-1333-48d1-a60f-8fc60f259f0e\",\"code\":\"2CB2B763_TEMP\"," +
            "\"edge\":\"d7cd3f57-a188-4462-b959-df7a23994c92\"," +
            "\"network\":\"e3eab951-932e-4fcc-a925-08b31e1014a0\",\"label\":null," +
            "\"enabled\":true,\"protocol\":\"WIRE\",\"kind\":\"INPUT\"," +
            "\"type\":\"DIGITAL\",\"min_scale\":null,\"max_scale\":null," +
            "\"precision\":3,\"offset\":0,\"version\":null,\"metadata\":null," +
            "\"unit\":{\"type\":\"celsius\",\"category\":\"TEMPERATURE\",\"symbol\":\"°C\",\"label\":null}}");
        final JsonObject expected = new JsonObject().put("action", EventAction.REMOVE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", body);
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_TEMP.toString()))
                                           .filter(new JsonObject().put(Filters.FORCE, true))
                                           .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.REMOVE, req);
    }

    @Test
    public void test_create_batch_point(TestContext context) {
        final UUID p1Id = UUID.randomUUID();
        final UUID p2Id = UUID.randomUUID();
        final JsonObject body = new JsonObject(
            "{\"points\":[{\"resource\":{\"id\":\"" + p1Id + "\",\"code\":\"TET_01\"," + "\"edge\":\"" +
            PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.DEFAULT_NETWORK + "\"," +
            "\"enabled\":true,\"protocol\":\"UNKNOWN\",\"kind\":\"UNKNOWN\",\"type\":\"UNKNOWN\"," +
            "\"unit\":{\"type\":\"bool\",\"category\":\"ALL\"}},\"action\":\"CREATE\",\"status\":\"SUCCESS\"}," +
            "{\"resource\":{\"id\":\"" + p2Id + "\",\"code\":\"TET02\"," + "\"edge\":\"" + PrimaryKey.EDGE +
            "\",\"network\":\"" + PrimaryKey.DEFAULT_NETWORK + "\"," +
            "\"enabled\":true,\"protocol\":\"UNKNOWN\",\"kind\":\"UNKNOWN\",\"type\":\"UNKNOWN\"," +
            "\"unit\":{\"type\":\"number\",\"category\":\"ALL\"}},\"action\":\"CREATE\",\"status\":\"SUCCESS\"}]}");
        final Point p1 = new Point().setId(p1Id)
                                    .setCode("TET_01")
                                    .setMeasureUnit(Base.BOOLEAN.type())
                                    .setEdge(PrimaryKey.EDGE)
                                    .setNetwork(PrimaryKey.DEFAULT_NETWORK);
        final Point p2 = new Point().setId(p2Id)
                                    .setCode("TET02")
                                    .setMeasureUnit(Base.NUMBER.type())
                                    .setEdge(PrimaryKey.EDGE)
                                    .setNetwork(PrimaryKey.DEFAULT_NETWORK);
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("points",
                                                                      new JsonArray().add(JsonPojo.from(p1).toJson())
                                                                                     .add(JsonPojo.from(p2).toJson())))
                                           .build();
        asserter(context, true, body, PointService.class.getName(), EventAction.BATCH_CREATE, req,
                 JSONCompareMode.LENIENT);
    }

    @Test
    public void test_create_batch_point_invalid(TestContext context) {
        final RequestData req = RequestData.builder().body(new JsonObject()).build();
        asserter(context, false, new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                 .put("message", "Must provide json array under 'points' key"),
                 PointService.class.getName(), EventAction.BATCH_CREATE, req);
    }

    @Test
    public void test_create_batch_point_one_error(TestContext context) {
        final UUID p1Id = UUID.randomUUID();
        final JsonObject body = new JsonObject(
            "{\"points\":[{\"resource\":{\"edge\":\"" + PrimaryKey.EDGE + "\"," + "\"network\":\"" +
            PrimaryKey.DEFAULT_NETWORK + "\",\"measure_unit\":\"number\",\"unit\":{\"type\":\"number\"}}," +
            "\"error\":{\"code\":\"INVALID_ARGUMENT\",\"message\":\"code is mandatory\"}}," +
            "{\"resource\":{\"id\":\"" + p1Id + "\",\"code\":\"TET_01\",\"edge\":\"" + PrimaryKey.EDGE +
            "\",\"network\":\"" + PrimaryKey.DEFAULT_NETWORK + "\"," +
            "\"enabled\":true,\"protocol\":\"UNKNOWN\",\"kind\":\"UNKNOWN\",\"type\":\"UNKNOWN\"," +
            "\"unit\":{\"type\":\"bool\",\"category\":\"ALL\"}},\"action\":\"CREATE\",\"status\":\"SUCCESS\"}]}");
        final Point p1 = new Point().setId(p1Id)
                                    .setCode("TET_01")
                                    .setMeasureUnit(Base.BOOLEAN.type())
                                    .setEdge(PrimaryKey.EDGE)
                                    .setNetwork(PrimaryKey.DEFAULT_NETWORK);
        final Point p2 = new Point().setMeasureUnit(Base.NUMBER.type())
                                    .setEdge(PrimaryKey.EDGE)
                                    .setNetwork(PrimaryKey.DEFAULT_NETWORK);
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("points",
                                                                      new JsonArray().add(JsonPojo.from(p1).toJson())
                                                                                     .add(JsonPojo.from(p2).toJson())))
                                           .build();
        asserter(context, true, body, PointService.class.getName(), EventAction.BATCH_CREATE, req,
                 JSONCompareMode.LENIENT);
    }

}
