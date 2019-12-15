package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class PointThingVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_points_by_thing_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":4,\"point\":{\"id\":\"" + PrimaryKey.P_BACNET_FAN + "\"," +
            "\"code\":\"HVAC_01_FAN\",\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.NETWORK +
            "\",\"enabled\":true,\"protocol\":\"BACNET\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"revolutions_per_minute\",\"precision\":3,\"offset\":0}}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/thing/" + PrimaryKey.THING_FAN_HVAC + "/point", 200,
                           expected);
    }

    @Test
    public void test_get_points_by_thing_n_device_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":4,\"point\":{\"id\":\"" + PrimaryKey.P_BACNET_FAN + "\",\"code\":\"HVAC_01_FAN\"," +
            "\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.NETWORK + "\",\"enabled\":true," +
            "\"protocol\":\"BACNET\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"revolutions_per_minute\",\"precision\":3,\"offset\":0}}]}");
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/device/" + PrimaryKey.DEVICE_HVAC + "/thing/" + PrimaryKey.THING_FAN_HVAC + "/point",
                           200, expected);
    }

    @Test
    public void test_get_points_by_thing_n_device_n_network_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":4,\"point\":{\"id\":\"" + PrimaryKey.P_BACNET_FAN + "\",\"code\":\"HVAC_01_FAN\"," +
            "\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.NETWORK + "\",\"enabled\":true," +
            "\"protocol\":\"BACNET\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"revolutions_per_minute\",\"precision\":3,\"offset\":0}}]}");
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/network/" + PrimaryKey.NETWORK + "/device/" + PrimaryKey.DEVICE_HVAC + "/thing/" +
                           PrimaryKey.THING_FAN_HVAC + "/point", 200, expected);
    }

    @Test
    public void test_get_points_by_device_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":1,\"point\":{\"id\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\"," +
            "\"code\":\"2CB2B763_HUMIDITY\",\"edge\":\"" + PrimaryKey.EDGE + "\",\"enabled\":true," +
            "\"protocol\":\"GPIO\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\",\"measure_unit\":\"percentage\"," +
            "\"min_scale\":0,\"max_scale\":100,\"precision\":3,\"offset\":0}},{\"id\":2,\"point\":{\"id\":\"" +
            PrimaryKey.P_GPIO_TEMP + "\",\"code\":\"2CB2B763_TEMP\",\"edge\":\"" + PrimaryKey.EDGE +
            "\",\"enabled\":true,\"protocol\":\"GPIO\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"celsius\",\"precision\":3,\"offset\":0}}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/device/" + PrimaryKey.DEVICE_DROPLET + "/point", 200,
                           expected);
    }

    @Test
    public void test_get_points_by_device_n_network_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":1,\"point\":{\"id\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\"," +
            "\"code\":\"2CB2B763_HUMIDITY\",\"edge\":\"" + PrimaryKey.EDGE + "\",\"enabled\":true," +
            "\"protocol\":\"GPIO\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\",\"measure_unit\":\"percentage\"," +
            "\"min_scale\":0,\"max_scale\":100,\"precision\":3,\"offset\":0}},{\"id\":2,\"point\":{\"id\":\"" +
            PrimaryKey.P_GPIO_TEMP + "\",\"code\":\"2CB2B763_TEMP\",\"edge\":\"" + PrimaryKey.EDGE +
            "\",\"enabled\":true,\"protocol\":\"GPIO\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"celsius\",\"precision\":3,\"offset\":0}}]}");
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/network/default/device/" + PrimaryKey.DEVICE_DROPLET + "/point", 200, expected);
    }

    @Test
    public void test_get_point_by_device_n_network_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"id\":2,\"point\":{\"id\":\"" + PrimaryKey.P_GPIO_TEMP + "\",\"code\":\"2CB2B763_TEMP\",\"edge\":\"" +
            PrimaryKey.EDGE + "\",\"enabled\":true,\"protocol\":\"GPIO\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"celsius\",\"precision\":3,\"offset\":0}}");
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/network/default/device/" + PrimaryKey.DEVICE_DROPLET + "/point/" +
                           PrimaryKey.P_GPIO_TEMP, 200, expected);
    }

    @Test
    public void test_unable_create_point_by_device_n_network_404(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message",
                                                         "Unsupported HTTP method POST in '/network/default/device/" +
                                                         PrimaryKey.DEVICE_DROPLET + "/point'");
        assertRestByClient(context, HttpMethod.POST,
                           "/api/s/network/default/device/" + PrimaryKey.DEVICE_DROPLET + "/point", 410, expected);
    }

    @Test
    public void test_get_point_by_thing_404(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message", "Not found resource with " + "thing_id=" +
                                                                    PrimaryKey.THING_FAN_HVAC + " and point_id=" +
                                                                    PrimaryKey.P_GPIO_TEMP);
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/thing/" + PrimaryKey.THING_FAN_HVAC + "/point/" + PrimaryKey.P_GPIO_TEMP, 404,
                           expected);
    }

    @Test
    public void test_get_point_by_thing_n_device_404(TestContext context) {

        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message", "Not found resource with thing_id=" +
                                                                    PrimaryKey.THING_FAN_HVAC + " and device_id=" +
                                                                    PrimaryKey.DEVICE_DROPLET + " and point_id=" +
                                                                    PrimaryKey.P_GPIO_TEMP);
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/device/" + PrimaryKey.DEVICE_DROPLET + "/thing/" + PrimaryKey.THING_FAN_HVAC +
                           "/point/" + PrimaryKey.P_GPIO_TEMP, 404, expected);
    }

}
