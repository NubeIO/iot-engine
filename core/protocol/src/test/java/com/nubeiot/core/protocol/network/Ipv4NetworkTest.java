package com.nubeiot.core.protocol.network;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;

public class Ipv4NetworkTest {

    @Test
    public void test_serialize() throws JSONException {
        final Ipv4Network eth0 = Ipv4Network.builder()
                                            .ifIndex(1)
                                            .ifName("eth0")
                                            .cidrAddress("172.1.0.1/16")
                                            .hostAddress("172.1.0.2")
                                            .broadcastAddress("172.1.0.255")
                                            .build();
        System.out.println(eth0.toJson());
        JsonHelper.assertJson(new JsonObject("{\"ifIndex\":1,\"ifName\":\"eth0\",\"cidrAddress\":\"172.1.0.1/16\"," +
                                             "\"hostAddress\":\"172.1.0.2\",\"broadcastAddress\":\"172.1.0.255\"," +
                                             "\"type\":\"ipv4\"}"), eth0.toJson());
    }

    @Test
    public void test_deserialize() {
        final Ipv4Network ipv4 = JsonData.from("{\"ifIndex\":1,\"ifName\":\"eth0\",\"cidrAddress\":\"172.1.0.1/16\"," +
                                               "\"hostAddress\":\"172.1.0.2\",\"broadcastAddress\":\"172.1.0.255\"}",
                                               Ipv4Network.class);
        Assert.assertEquals("ipv4", ipv4.type());
        Assert.assertEquals(1, ipv4.getIfIndex().intValue());
        Assert.assertEquals("eth0", ipv4.getIfName());
        Assert.assertNull(ipv4.getDisplayName());
        Assert.assertEquals("172.1.0.2", ipv4.getHostAddress());
        Assert.assertEquals("172.1.0.255", ipv4.getBroadcastAddress());
        Assert.assertEquals("172.1.0.1/16", ipv4.getCidrAddress());
        Assert.assertEquals("ipv4-eth0", ipv4.identifier());
    }

    @Test
    public void test_equals_ipv4() {
        final Ipv4Network ip1 = JsonData.from(
            "{\"type\":\"ipv4\",\"ifIndex\":1,\"ifName\":\"eth0\",\"cidrAddress\":\"172.1.0.1/16\"," +
            "\"hostAddress\":\"172.1.0.2\",\"broadcastAddress\":\"172.1.0.255\"}", Ipv4Network.class);
        final Ipv4Network ip2 = Ipv4Network.builder()
                                           .ifIndex(2)
                                           .ifName("eth0")
                                           .cidrAddress("172.1.0.1/16")
                                           .hostAddress("172.1.0.2")
                                           .broadcastAddress("172.1.0.255")
                                           .build();
        Assert.assertEquals(ip1, ip2);
        Assert.assertEquals(ip1.identifier(), ip2.identifier());
    }

    @Test
    public void test_valid_ipv4() {
        Assert.assertTrue(Ipv4Network.isValidIPv4("255.255.255.255"));
        Assert.assertTrue(Ipv4Network.isValidIPv4("0.0.0.0"));
        Assert.assertTrue(Ipv4Network.isValidIPv4("192.168.1.1"));
        Assert.assertTrue(Ipv4Network.isValidIPv4("10.0.1.1"));
    }

    @Test
    public void test_invalid_ipv4() {
        Assert.assertFalse(Ipv4Network.isValidIPv4("256.255.255.255"));
        Assert.assertFalse(Ipv4Network.isValidIPv4("0.0.0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_invalid_ipv4_catch() {
        Assert.assertFalse(Ipv4Network.isValidIPv4(""));
    }

}
