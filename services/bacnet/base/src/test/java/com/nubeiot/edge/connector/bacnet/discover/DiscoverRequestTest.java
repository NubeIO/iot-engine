package com.nubeiot.edge.connector.bacnet.discover;

import org.json.JSONException;
import org.junit.Test;

import io.github.zero88.qwe.TestHelper.JsonHelper;
import io.github.zero88.qwe.protocol.network.Ipv4Network;
import io.vertx.core.json.JsonObject;

public class DiscoverRequestTest {

    @Test
    public void test_serialize() throws JSONException {
        final Ipv4Network firstActiveIp = Ipv4Network.getFirstActiveIp();
        final DiscoverRequest request = DiscoverRequest.builder()
                                                       .networkCode(firstActiveIp.identifier())
                                                       .deviceInstance(1)
                                                       .objectCode("analog-value:2")
                                                       .build();
        final JsonObject expected = new JsonObject().put("networkCode", firstActiveIp.identifier())
                                                    .put("deviceInstance", 1)
                                                    .put("objectCode", "analog-value:2");
        System.out.println(request.toJson());
        JsonHelper.assertJson(expected, request.toJson());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_validate_network() {
        final JsonObject request = new JsonObject().put("networkCode", "");
        DiscoverRequest.from(request, DiscoverLevel.NETWORK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_validate_device_missing_network() {
        final JsonObject request = new JsonObject().put("networkCode", "").put("deviceInstance", 1);
        DiscoverRequest.from(request, DiscoverLevel.DEVICE);
    }

    @Test(expected = NullPointerException.class)
    public void test_validate_device_missing_device() {
        final JsonObject request = new JsonObject().put("networkCode", "xyz");
        DiscoverRequest.from(request, DiscoverLevel.DEVICE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_validate_object_missing_network() {
        final JsonObject request = new JsonObject().put("deviceInstance", 1).put("objectCode", "1");
        DiscoverRequest.from(request, DiscoverLevel.OBJECT);
    }

    @Test(expected = NullPointerException.class)
    public void test_validate_object_missing_device() {
        final JsonObject request = new JsonObject().put("networkCode", "xyz").put("objectCode", "1");
        DiscoverRequest.from(request, DiscoverLevel.OBJECT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_validate_object_missing_obj() {
        final JsonObject request = new JsonObject().put("networkCode", "xyz").put("deviceInstance", 1);
        DiscoverRequest.from(request, DiscoverLevel.OBJECT);
    }

    @Test
    public void test_deserialize_full() throws JSONException {
        final JsonObject body = new JsonObject().put("networkCode", "xx")
                                                .put("deviceInstance", 1)
                                                .put("objectCode", "analog-value:2");
        final DiscoverRequest request = DiscoverRequest.from(body, DiscoverLevel.OBJECT);
        JsonHelper.assertJson(body, request.toJson());
    }

}
