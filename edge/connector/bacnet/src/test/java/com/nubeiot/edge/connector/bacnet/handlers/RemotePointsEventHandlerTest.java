package com.nubeiot.edge.connector.bacnet.handlers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.connector.bacnet.BACnetInstance;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.type.Encodable;


public class RemotePointsEventHandlerTest {

    String n = "testNet";
    Integer id = 1234;
    String p = "analogInput:1";
    String pr = "16";

    @Mock
    BACnetInstance bacnetInstance;
    Map<String, BACnetInstance> bacnetInstances = new HashMap<>();

    @InjectMocks
    RemotePointsEventHandler eventHandler;

    @Before
    public void beforeEach() throws Exception {
        MockitoAnnotations.initMocks(this);
        bacnetInstances.put(n, bacnetInstance);
        eventHandler = new RemotePointsEventHandler(bacnetInstances);
    }

    @Test
    public void writeRemoteDevicePointValueTest() throws Exception {
        when(bacnetInstance.writeAtPriority(any(Integer.class), any(String.class), any(Encodable.class),
                                            any(Integer.class))).thenReturn(Single.just(new JsonObject()));

        eventHandler.writeRemoteDevicePointValue(buildRequest(n, id, "1", 1, 16))
                    .test()
                    .assertNoErrors()
                    .assertComplete();
        eventHandler.writeRemoteDevicePointValue(buildRequest(n, id, "1", 1, ""))
                    .test()
                    .assertNoErrors()
                    .assertComplete();

    }

    @Test
    public void noNetworkTest() throws Exception {
        eventHandler.readRemoteDevicePointValue(buildRequest("testNetFalse", id, p, null, 0))
                    .test()
                    .assertError(BACnetException.class);
        eventHandler.writeRemoteDevicePointValue(buildRequest("testNetFalse", id, p, pr, 1))
                    .test()
                    .assertError(BACnetException.class);
    }

    private RequestData buildRequest(String net, Integer dev, String obj, Object val, Integer prior) {
        return RequestData.builder()
                          .body(new JsonObject().put("network", net)
                                                .put("deviceId", dev.toString())
                                                .put("objectId", obj)
                                                .put("value", "1")
                                                .put("priority", prior.toString()))
                          .build();
    }

    private RequestData buildRequest(String net, Integer dev, String obj, Object val, String prior) {
        return RequestData.builder()
                          .body(new JsonObject().put("network", net)
                                                .put("deviceId", dev.toString())
                                                .put("objectId", obj)
                                                .put("value", "1")
                                                .put("priority", prior))
                          .build();
    }

}
