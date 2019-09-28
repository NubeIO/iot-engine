package com.nubeiot.edge.connector.bacnet;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.nubeiot.core.exceptions.NetworkException;

public class TransportIPTest {

    @Test
    @Ignore
    public void test_by_subnet() {
        Assert.assertNotNull(TransportIP.bySubnet("192.168.1.6/30", -1));
    }

    @Test
    @Ignore
    public void test_by_network() {
        Assert.assertNotNull(TransportIP.byNetworkName("enp24s0", -1));
    }

    @Test(expected = NetworkException.class)
    public void test_by_subnet_invalid() {
        TransportIP.bySubnet("456.168.6.1/24", -1);
    }

    @Test(expected = NetworkException.class)
    public void test_by_network_invalid() {
        TransportIP.byNetworkName("not_found_xxx", -1);
    }

    @Test
    public void test_by_network_null() {
        Assert.assertNotNull(TransportIP.byNetworkName(null, -1));
    }

}
