package com.nubeiot.edge.connector.device;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;

public class NetworkInfoTest {

    @Test
    public void deserialization_test() {
        String ipAddress = "192.168.0.10";
        String gateway = "192.168.0.1";
        String subnetMask = "255.255.255.255";
        JsonObject data = new JsonObject().put("ip_address", ipAddress)
                                          .put("gateway", gateway)
                                          .put("subnet_mask", "255.255.255.255");

        NetworkInfo networkInfo = JsonData.from(data, NetworkInfo.class);
        Assert.assertEquals(networkInfo.getIpAddress(), ipAddress);
        Assert.assertEquals(networkInfo.getGateway(), gateway);
        Assert.assertEquals(networkInfo.getSubnetMask(), subnetMask);
    }

}
