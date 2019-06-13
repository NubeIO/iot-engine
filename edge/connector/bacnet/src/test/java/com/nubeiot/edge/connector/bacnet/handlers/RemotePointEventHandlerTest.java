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
import com.serotonin.bacnet4j.type.Encodable;

public class RemotePointEventHandlerTest {

    String n = "testNet";
    int id = 1234;
    String p = "analogInput:1";
    int pr = 16;

    @Mock
    BACnetInstance bacnetInstance;
    Map<String, BACnetInstance> bacnetInstances = new HashMap<>();

    @InjectMocks
    RemotePointEventHandler eventHandler;

    @Before
    public void beforeEach() throws Exception {
        MockitoAnnotations.initMocks(this);
        bacnetInstances.put(n, bacnetInstance);
        eventHandler = new RemotePointEventHandler(bacnetInstances);
    }

    @Test
    public void writeRemoteDevicePointValueTest() throws Exception {
        when(bacnetInstance.writeAtPriority(any(Integer.class), any(String.class), any(Encodable.class),
                                            any(Integer.class))).thenReturn(Single.just(new JsonObject()));

        eventHandler.writeRemoteDevicePointValue(n, id, p, pr, "1").test().assertNoErrors().assertComplete();
    }

    @Test
    public void noNetworkTest() throws Exception {
        eventHandler.readRemoteDevicePointValue("testNetFalse", id, p)
                    .test()
                    .assertError(BACnetException.class);
        eventHandler.writeRemoteDevicePointValue("testNetFalse", id, p, pr, 1)
                    .test()
                    .assertError(BACnetException.class);
    }

}
