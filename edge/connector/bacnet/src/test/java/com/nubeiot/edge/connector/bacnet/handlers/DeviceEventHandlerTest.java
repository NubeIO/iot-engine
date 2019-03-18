package com.nubeiot.edge.connector.bacnet.handlers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.connector.bacnet.BACnet;

public class DeviceEventHandlerTest {

    @Mock
    BACnet bacnetInstance;
    @Mock
    Vertx vertx;
    @InjectMocks
    DeviceEventHandler eventHandler;

    @Before
    public void beforeAll() throws Exception {
        MockitoAnnotations.initMocks(this);
        eventHandler = new DeviceEventHandler(vertx, bacnetInstance);
    }

    @Test
    public void getRemoteDeviceExtendedTest() throws Exception {
        when(bacnetInstance.getRemoteDevices()).thenReturn(Single.just(new JsonObject()));
        eventHandler.getCachedRemoteDevices(null).test().assertNoErrors().assertComplete();
    }

    @Test
    public void getRemoteDeviceExtendedInfo() throws Exception {
        when(bacnetInstance.getRemoteDeviceExtendedInfo(any(Integer.class))).thenReturn(Single.just(new JsonObject()));

        RequestData req = RequestData.builder().body(new JsonObject().put("deviceID", 11)).build();
        eventHandler.getCachedRemoteDevices(req).test().assertNoErrors().assertComplete();
    }

}
