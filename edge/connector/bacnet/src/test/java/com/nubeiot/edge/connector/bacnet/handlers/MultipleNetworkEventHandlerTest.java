package com.nubeiot.edge.connector.bacnet.handlers;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.connector.bacnet.BACnetInstance;

public class MultipleNetworkEventHandlerTest {

    Map<String, BACnetInstance> bacnetInstances = new HashMap<>();
    MultipleNetworkEventHandler eventHandler;

    @Before
    public void beforeEach() throws Exception {
        eventHandler = new MultipleNetworkEventHandler(bacnetInstances);
    }

    @Test
    public void getRemoteDeviceTest() throws Exception {
        BACnetInstance inst1 = Mockito.mock(BACnetInstance.class);
        BACnetInstance inst2 = Mockito.mock(BACnetInstance.class);
        bacnetInstances.put("net1", inst1);
        bacnetInstances.put("net2", inst2);
        when(inst1.getRemoteDevices()).thenReturn(Single.just(new JsonObject().put("1", 1)));
        when(inst2.getRemoteDevices()).thenReturn(Single.just(new JsonObject().put("2", 2)));

        JsonObject expected = new JsonObject().put("net1", new JsonObject().put("1", 1))
                                              .put("net2", new JsonObject().put("2", 2));

        eventHandler.getCachedRemoteDevices().test().assertNoErrors().assertComplete().assertValue(expected);
    }

    @Test
    public void startDiscoveryTest() throws Exception {
        BACnetInstance inst1 = Mockito.mock(BACnetInstance.class);
        bacnetInstances.put("net1", inst1);

        RequestData requestData = RequestData.builder().build();
        requestData.getFilter().put("timeout","1000");
        eventHandler.startDiscovery(requestData);
        Mockito.verify(inst1).startRemoteDiscover(Mockito.anyLong());
        requestData = RequestData.builder().build();
        requestData.getFilter().put("timeout","0");
        eventHandler.startDiscovery(requestData);
        Mockito.verify(inst1).startRemoteDiscover();
    }
}
