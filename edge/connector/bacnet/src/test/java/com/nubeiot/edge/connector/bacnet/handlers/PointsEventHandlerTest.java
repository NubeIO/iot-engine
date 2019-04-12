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

public class PointsEventHandlerTest {

    String n = "testNet";
    int id = 1234;
    String p = "analogInput:1";
    int pr = 16;

    @Mock
    BACnetInstance bacnetInstance;
    Map<String, BACnetInstance> bacnetInstances = new HashMap<>();

    @InjectMocks
    PointsEventHandler eventHandler;

    @Before
    public void beforeEach() throws Exception {
        MockitoAnnotations.initMocks(this);
        bacnetInstances.put("testNet", bacnetInstance);
        eventHandler = new PointsEventHandler(bacnetInstances);
    }

    //TODO: test sending 1 / 0 to binary-output

    @Test
    public void writeRemoteDevicePointValueTest() throws Exception {
        when(bacnetInstance.writeAtPriority(any(Integer.class), any(String.class), any(Encodable.class),
                                            any(Integer.class))).thenReturn(Single.just(new JsonObject()));

        Single<JsonObject> reply = eventHandler.writeRemoteDevicePointValue(n, id, p, pr, "1");
        reply.test().assertNoErrors().assertComplete();

        reply = eventHandler.writeRemoteDevicePointValue(n, id, p, pr, "1.2");
        reply.test().assertNoErrors().assertComplete();

        reply = eventHandler.writeRemoteDevicePointValue(n, id, p, pr, "null");
        reply.test().assertNoErrors().assertComplete();

        reply = eventHandler.writeRemoteDevicePointValue(n, id, p, pr, "true");
        reply.test().assertNoErrors().assertComplete();

        reply = eventHandler.writeRemoteDevicePointValue(n, id, p, pr, "false");
        reply.test().assertNoErrors().assertComplete();

        reply = eventHandler.writeRemoteDevicePointValue(n, id, p, pr, 1);
        reply.test().assertNoErrors().assertComplete();

        reply = eventHandler.writeRemoteDevicePointValue(n, id, p, pr, 1.1);
        reply.test().assertNoErrors().assertComplete();

        //        reply = eventHandler.writeRemoteDevicePointValue(n, id, p, pr, null);
        //        reply.test().assertNoErrors().assertComplete();
    }

    @Test
    public void writeRemoteDevicePointValueTest_invalidPriority() throws Exception {
        Single<JsonObject> reply = eventHandler.writeRemoteDevicePointValue(n, id, p, 0, "1");
        reply.test().assertError(BACnetException.class);

        reply = eventHandler.writeRemoteDevicePointValue(n, id, p, 17, "1");
        reply.test().assertError(BACnetException.class);
    }

    @Test
    public void writeRemoteDevicePointValueTest_invalidValue() throws Exception {
        Single<JsonObject> reply = eventHandler.writeRemoteDevicePointValue(n, id, p, pr, "bad_value");
        reply.test().assertError(NumberFormatException.class);
    }

    @Test
    public void noNetworkTest() throws Exception {
        eventHandler.getRemoteDevicePoints("testNetFalse", id).test().assertError(BACnetException.class);
        eventHandler.getRemoteDevicePointExtended("testNetFalse", id, p).test().assertError(BACnetException.class);
        eventHandler.writeRemoteDevicePointValue("testNetFalse", id, p, pr, 1)
                    .test()
                    .assertError(BACnetException.class);
    }

}
