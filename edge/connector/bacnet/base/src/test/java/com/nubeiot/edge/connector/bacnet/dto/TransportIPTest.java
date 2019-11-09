package com.nubeiot.edge.connector.bacnet.dto;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.NetworkException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;

public class TransportIPTest {

    @Test
    public void test_by_subnet() {
        final Ipv4Network firstActiveIp = Ipv4Network.getFirstActiveIp();
        final IpNetwork ipNetwork = TransportIP.bySubnet(firstActiveIp.getCidrAddress(), -1);
        Assert.assertNotNull(ipNetwork);
        Assert.assertEquals(47808, ipNetwork.getPort());
        Assert.assertEquals(firstActiveIp.getBroadcastAddress(), ipNetwork.getBroadcastAddresss());
    }

    @Test
    public void test_by_network() {
        final Ipv4Network firstActiveIp = Ipv4Network.getFirstActiveIp();
        final IpNetwork ipNetwork = TransportIP.byNetworkName(firstActiveIp.getIfName(), 9999);
        Assert.assertNotNull(ipNetwork);
        Assert.assertEquals(9999, ipNetwork.getPort());
        Assert.assertEquals(firstActiveIp.getBroadcastAddress(), ipNetwork.getBroadcastAddresss());
    }

    @Test(expected = NetworkException.class)
    public void test_by_subnet_invalid() {
        TransportIP.bySubnet("456.168.6.1/24", -1);
    }

    @Test(expected = NotFoundException.class)
    public void test_by_network_invalid() {
        TransportIP.byNetworkName("not_found_xxx", -1);
    }

    @Test
    public void test_by_network_null() {
        Assert.assertNotNull(TransportIP.byNetworkName(null, -1));
    }

}
