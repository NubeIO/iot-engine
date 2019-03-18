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
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.edge.connector.bacnet.BACnet;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.type.Encodable;

public class PointsEventHandlerTest {

    @Mock
    BACnet bacnetInstance;
    @Mock
    Vertx vertx;

    @InjectMocks
    PointsEventHandler eventHandler;

    @Before
    public void beforeAll() throws Exception {
        MockitoAnnotations.initMocks(this);
        eventHandler = new PointsEventHandler(vertx, bacnetInstance);
    }

    @Test
    public void writeRemoteDevicePointValueTest() throws Exception {
        when(bacnetInstance.writeAtPriority(any(Integer.class), any(String.class), any(Encodable.class),
                                            any(Integer.class))).thenReturn(Single.just(new JsonObject()));

        RequestData data = writeMessage(1234, "analogInput:1", 16, "1");
        Single<EventMessage> reply = eventHandler.writeRemoteDevicePointValue(data);
        reply.test().assertNoErrors().assertComplete();

        data = writeMessage(1234, "analogInput:1", 16, "1.2");
        reply = eventHandler.writeRemoteDevicePointValue(data);
        reply.test().assertNoErrors().assertComplete();

        data = writeMessage(1234, "analogInput:1", 16, "null");
        reply = eventHandler.writeRemoteDevicePointValue(data);
        reply.test().assertNoErrors().assertComplete();
    }

    @Test
    public void writeRemoteDevicePointValueTest_invalidPriority() throws Exception {
        RequestData data = writeMessage(1234, "analogInput:1", 0, "1");
        Single<EventMessage> reply = eventHandler.writeRemoteDevicePointValue(data);
        reply.test().assertError(BACnetException.class);

        data = writeMessage(1234, "analogInput:1", 17, "1");
        reply = eventHandler.writeRemoteDevicePointValue(data);
        reply.test().assertError(BACnetException.class);
    }

    @Test
    public void writeRemoteDevicePointValueTest_invalidValue() throws Exception {
        RequestData data = writeMessage(1234, "analogInput:1", 16, "bad_value");
        Single<EventMessage> reply = eventHandler.writeRemoteDevicePointValue(data);
        reply.test().assertError(NumberFormatException.class);
    }

    private RequestData writeMessage(int id, String obj, int priority, String value) {
        JsonObject data = new JsonObject();
        data.put("deviceID", id);
        data.put("objectID", obj);
        data.put("priority", priority);
        data.put("value", value);
        RequestData d = RequestData.builder().body(data).build();
        return d;
    }

}
