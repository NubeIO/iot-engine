package com.nubeiot.edge.connector.bacnet.discover;

import org.json.JSONException;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;

public class DiscoverResponseTest {

    @Test
    public void test_serialize() throws JSONException {
        final LocalDeviceMetadata metadata = LocalDeviceMetadata.builder().deviceNumber(111).modelName("xyz").build();
        final Ipv4Network firstActiveIp = Ipv4Network.getFirstActiveIp();
        final DiscoverResponse response = DiscoverResponse.builder()
                                                          .network(firstActiveIp)
                                                          .localDevice(metadata)
                                                          .build();
        final JsonObject expected = new JsonObject().put("network", firstActiveIp.toJson())
                                                    .put("localDevice", metadata.toJson());
        System.out.println(response.toJson());
        JsonHelper.assertJson(expected, response.toJson());
    }

    @Test
    public void test_deserialize() throws JSONException {
        final Ipv4Network firstActiveIp = Ipv4Network.getFirstActiveIp();
        final LocalDeviceMetadata metadata = LocalDeviceMetadata.builder().deviceNumber(222).modelName("abc").build();
        final JsonObject expected = new JsonObject().put("network", firstActiveIp.toJson())
                                                    .put("localDevice", metadata.toJson());
        final DiscoverResponse xyz = JsonData.from(expected, DiscoverResponse.class);
        System.out.println(xyz.toJson());
        JsonHelper.assertJson(expected, xyz.toJson());
    }

}
