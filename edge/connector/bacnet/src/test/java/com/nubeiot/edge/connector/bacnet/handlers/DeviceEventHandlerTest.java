package com.nubeiot.edge.connector.bacnet.handlers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetInstance;
import com.serotonin.bacnet4j.exception.BACnetException;

public class DeviceEventHandlerTest {

    @Mock
    BACnetInstance bacnetInstance;
    Map<String, BACnetInstance> bacnetInstances = new HashMap<>();
    @InjectMocks
    DeviceEventHandler eventHandler;

    @Before
    public void beforeEach() throws Exception {
        MockitoAnnotations.initMocks(this);
        bacnetInstances.put("testNet", bacnetInstance);
        eventHandler = new DeviceEventHandler(bacnetInstances);
    }

    @Test
    public void getRemoteDeviceTest() throws Exception {
        when(bacnetInstance.getRemoteDevices()).thenReturn(Single.just(new JsonObject()));
        eventHandler.getCachedRemoteDevices("testNet").test().assertNoErrors().assertComplete();
    }

    @Test
    public void getRemoteDeviceExtendedInfo() throws Exception {
        when(bacnetInstance.getRemoteDeviceExtendedInfo(any(Integer.class))).thenReturn(Single.just(new JsonObject()));
        eventHandler.getRemoteDeviceExtendedInfo("testNet", 11).test().assertNoErrors().assertComplete();
    }

    @Test
    public void noNetworkTest() throws Exception {
        eventHandler.getCachedRemoteDevices("testNetFalse").test().assertError(BACnetException.class);
        eventHandler.getRemoteDeviceExtendedInfo("testNetFalse", 11).test().assertError(BACnetException.class);
    }

}
