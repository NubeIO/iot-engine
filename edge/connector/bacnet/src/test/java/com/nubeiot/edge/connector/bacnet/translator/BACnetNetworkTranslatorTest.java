package com.nubeiot.edge.connector.bacnet.translator;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.core.protocol.network.UdpProtocol;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

public class BACnetNetworkTranslatorTest {

    @Test
    public void test_serialize() throws JSONException {
        final UdpProtocol protocol = UdpProtocol.builder().ip(Ipv4Network.getFirstActiveIp()).port(47808).build();
        Network network = new Network().setCode(protocol.identifier())
                                       .setProtocol(Protocol.BACNET)
                                       .setState(State.ENABLED)
                                       .setMetadata(protocol.toJson());
        JsonHelper.assertJson(network.toJson(), new BACnetNetworkTranslator().serialize(protocol).toJson());
    }

    @Test
    public void test_deserialize_with_metadata() throws JSONException {
        final UdpProtocol protocol = UdpProtocol.builder().ip(Ipv4Network.getFirstActiveIp()).port(47808).build();
        final Network input = new Network().setCode(protocol.identifier())
                                           .setProtocol(Protocol.BACNET)
                                           .setState(State.ENABLED)
                                           .setMetadata(protocol.toJson());
        final CommunicationProtocol udp = new BACnetNetworkTranslator().deserialize(input);
        System.out.println(input.toJson());
        System.out.println(udp.toJson());
        Assert.assertEquals(protocol, udp);
        JsonHelper.assertJson(protocol.toJson(), udp.toJson());
    }

    @Test(expected = NotFoundException.class)
    public void test_deserialize_without_metadata() {
        final Network input = new Network().setCode("udp4-xxx+abc=44444-47808")
                                           .setProtocol(Protocol.BACNET)
                                           .setState(State.ENABLED);
        new BACnetNetworkTranslator().deserialize(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_deserialize_with_metadata_has_invalid_type() {
        final Network input = new Network().setCode("udp4-xxx+abc=44444-47808")
                                           .setProtocol(Protocol.BACNET)
                                           .setState(State.ENABLED)
                                           .setMetadata(new JsonObject().put("type", "xxx"));
        new BACnetNetworkTranslator().deserialize(input);
    }

}
