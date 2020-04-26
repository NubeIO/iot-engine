package com.nubeiot.edge.module.datapoint.verticle;

import java.util.UUID;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

public class PointTransducerVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_points_by_transducer_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":4,\"point\":{\"id\":\"" + PrimaryKey.P_BACNET_FAN + "\"," +
            "\"code\":\"HVAC_01_FAN\",\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.BACNET_NETWORK +
            "\",\"enabled\":true,\"protocol\":\"BACNET\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"revolutions_per_minute\",\"precision\":3,\"offset\":0}}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/transducer/" + PrimaryKey.TRANSDUCER_FAN_HVAC + "/point",
                           200, expected);
    }

    @Test
    public void test_get_points_by_transducer_n_device_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":4,\"point\":{\"id\":\"" + PrimaryKey.P_BACNET_FAN + "\",\"code\":\"HVAC_01_FAN\"," +
            "\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.BACNET_NETWORK + "\",\"enabled\":true," +
            "\"protocol\":\"BACNET\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"revolutions_per_minute\",\"precision\":3,\"offset\":0}}]}");
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/device/" + PrimaryKey.DEVICE_HVAC + "/transducer/" + PrimaryKey.TRANSDUCER_FAN_HVAC +
                           "/point", 200, expected);
    }

    @Test
    public void test_get_points_by_transducer_n_device_n_network_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":4,\"point\":{\"id\":\"" + PrimaryKey.P_BACNET_FAN + "\",\"code\":\"HVAC_01_FAN\"," +
            "\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.BACNET_NETWORK + "\",\"enabled\":true," +
            "\"protocol\":\"BACNET\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"revolutions_per_minute\",\"precision\":3,\"offset\":0}}]}");
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/network/" + PrimaryKey.BACNET_NETWORK + "/device/" + PrimaryKey.DEVICE_HVAC +
                           "/transducer/" + PrimaryKey.TRANSDUCER_FAN_HVAC + "/point", 200, expected);
    }

    @Test
    public void test_get_point_by_transducer_404(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message", "Not found resource with " + "transducer_id=" +
                                                                    PrimaryKey.TRANSDUCER_FAN_HVAC + " and point_id=" +
                                                                    PrimaryKey.P_GPIO_TEMP);
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/transducer/" + PrimaryKey.TRANSDUCER_FAN_HVAC + "/point/" + PrimaryKey.P_GPIO_TEMP,
                           404, expected);
    }

    @Test
    public void test_get_point_by_transducer_n_device_404(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message", "Not found resource with transducer_id=" +
                                                                    PrimaryKey.TRANSDUCER_FAN_HVAC + " and device_id=" +
                                                                    PrimaryKey.DEVICE_DROPLET + " and point_id=" +
                                                                    PrimaryKey.P_GPIO_TEMP);
        assertRestByClient(context, HttpMethod.GET, "/api/s/device/" + PrimaryKey.DEVICE_DROPLET + "/transducer/" +
                                                    PrimaryKey.TRANSDUCER_FAN_HVAC + "/point/" + PrimaryKey.P_GPIO_TEMP,
                           404, expected);
    }

    @Test
    public void test_create_point_by_transducer_n_device_201(TestContext context) {
        final UUID uuid = UUID.randomUUID();
        final Point newOne = MockData.search(PrimaryKey.P_BACNET_TEMP).setId(uuid).setCode("NEW_TEMP");
        final JsonObject expected = new JsonObject(
            "{\"resource\":{\"id\":6,\"point\":{\"id\":\"" + uuid + "\",\"code\":\"NEW_TEMP\",\"edge\":\"" +
            PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.BACNET_NETWORK + "\",\"label\":null,\"enabled\":true," +
            "\"protocol\":\"BACNET\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\",\"measure_unit\":\"celsius\"," +
            "\"unit_alias\":null,\"min_scale\":null,\"max_scale\":null,\"precision\":3,\"offset\":0,\"version\":null," +
            "\"metadata\":null}},\"action\":\"CREATE\",\"status\":\"SUCCESS\"}");
        final JsonObject reqBody = new JsonObject().put("point", JsonPojo.from(newOne).toJson());
        assertRestByClient(context, HttpMethod.POST, "/api/s/device/" + PrimaryKey.DEVICE_HVAC + "/transducer/" +
                                                     PrimaryKey.TRANSDUCER_SWITCH_HVAC + "/point",
                           RequestData.builder().body(reqBody).build(), 201, expected);
    }

    @Test
    public void test_create_point_by_transducer_201(TestContext context) {
        final UUID uuid = UUID.randomUUID();
        final Point newOne = MockData.search(PrimaryKey.P_BACNET_TEMP).setId(uuid).setCode("NEW_TEMP");
        final JsonObject expected = new JsonObject(
            "{\"resource\":{\"id\":6,\"point\":{\"id\":\"" + uuid + "\",\"code\":\"NEW_TEMP\",\"edge\":\"" +
            PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.BACNET_NETWORK + "\",\"label\":null,\"enabled\":true," +
            "\"protocol\":\"BACNET\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\",\"measure_unit\":\"celsius\"," +
            "\"unit_alias\":null,\"min_scale\":null,\"max_scale\":null,\"precision\":3,\"offset\":0,\"version\":null," +
            "\"metadata\":null}},\"action\":\"CREATE\",\"status\":\"SUCCESS\"}");
        final JsonObject reqBody = new JsonObject().put("point", JsonPojo.from(newOne).toJson());
        assertRestByClient(context, HttpMethod.POST,
                           "/api/s/transducer/" + PrimaryKey.TRANSDUCER_SWITCH_HVAC + "/point",
                           RequestData.builder().body(reqBody).build(), 201, expected);
    }

    @Test
    public void test_create_point_by_transducer_unmatched_device_201(TestContext context) {
        final UUID uuid = UUID.randomUUID();
        final Point newOne = MockData.search(PrimaryKey.P_BACNET_TEMP).setId(uuid).setCode("NEW_TEMP");
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message",
                                                         "Input device id " + PrimaryKey.DEVICE_DROPLET + " is " +
                                                         "unmatched with referenced device id " +
                                                         PrimaryKey.DEVICE_HVAC + " in transducer " +
                                                         PrimaryKey.TRANSDUCER_SWITCH_HVAC);
        final JsonObject reqBody = new JsonObject().put("point", JsonPojo.from(newOne).toJson());
        assertRestByClient(context, HttpMethod.POST, "/api/s/device/" + PrimaryKey.DEVICE_DROPLET + "/transducer/" +
                                                     PrimaryKey.TRANSDUCER_SWITCH_HVAC + "/point",
                           RequestData.builder().body(reqBody).build(), 400, expected);
    }

}
