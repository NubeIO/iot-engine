package com.nubeiot.edge.connector.bacnet.handlers;

import static org.mockito.Matchers.any;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.connector.bacnet.BACnetInstance;
import com.serotonin.bacnet4j.exception.BACnetException;

public class RemotePointsInfoEventHandlerTest {

    String n = "testNet";
    String id = "1234";
    String p = "analogInput:1";
    String pr = "16";

    @Mock
    BACnetInstance bacnetInstance;
    Map<String, BACnetInstance> bacnetInstances = new HashMap<>();

    @InjectMocks
    RemotePointsInfoEventHandler eventHandler;

    @Before
    public void beforeEach() throws Exception {
        MockitoAnnotations.initMocks(this);
        bacnetInstances.put("testNet", bacnetInstance);
        eventHandler = new RemotePointsInfoEventHandler(bacnetInstances);
    }

    @Test
    public void noNetworkTest() throws Exception {
        eventHandler.getRemoteDevicePoints(
            RequestData.builder().body(new JsonObject().put("network", "testNetFalse").put("deviceId", "1234")).build())
                    .test()
                    .assertError(BACnetException.class);
        eventHandler.getRemoteDevicePointExtended(
            RequestData.builder().body(new JsonObject().put("network", "testNetFalse").put("deviceId", "1234")).build())
                    .test()
                    .assertError(BACnetException.class);
    }

}
