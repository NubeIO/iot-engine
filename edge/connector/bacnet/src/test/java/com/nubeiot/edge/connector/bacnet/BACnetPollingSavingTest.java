package com.nubeiot.edge.connector.bacnet;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.apdu.Error;
import com.serotonin.bacnet4j.cache.RemoteEntityCachePolicy;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.ErrorAPDUException;
import com.serotonin.bacnet4j.service.confirmed.ConfirmedRequestService;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.ServiceFutureImpl;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.Segmentation;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class BACnetPollingSavingTest {

    private LocalDevice localDevice;
    private DefaultTransport transport;
    private BACnetInstance bacnetInstance;
    private PollingTimers pollingTimers;
    private Map<String, Integer> remoteSubscriptions;

    @Before
    public void beforeEach() throws Exception {
        Vertx vertx = Mockito.mock(Vertx.class);
        transport = Mockito.mock(DefaultTransport.class);
        pollingTimers = Mockito.mock(PollingTimers.class);
        localDevice = new LocalDevice(1234, transport);
        remoteSubscriptions = new HashMap<>();
        bacnetInstance = BACnetInstance.createBACnet(localDevice, vertx, pollingTimers, remoteSubscriptions);
    }

    @After
    public void afterEach() throws Exception {
        localDevice.terminate();
    }

    @Test
    public void initPollingTest() throws Exception {
        RemoteDevice rd = Mockito.mock(RemoteDevice.class);
        ObjectIdentifier oid = Mockito.mock(ObjectIdentifier.class);
        bacnetInstance.initRemoteObjectPolling(rd, oid, 1000).test().assertNoErrors();
        Mockito.verify(pollingTimers)
               .addPoint(Mockito.any(BACnetInstance.class), Mockito.any(RemoteDevice.class),
                         Mockito.any(ObjectIdentifier.class), Mockito.anyLong());
        bacnetInstance.initRemoteObjectPolling(rd, oid).test().assertNoErrors();
    }

    @Test
    public void initPollingTest_Exception() throws Exception {
        Mockito.doThrow(BACnetException.class)
               .when(pollingTimers)
               .addPoint(Mockito.any(BACnetInstance.class), Mockito.any(RemoteDevice.class),
                         Mockito.any(ObjectIdentifier.class), Mockito.anyLong());
        RemoteDevice rd = Mockito.mock(RemoteDevice.class);
        ObjectIdentifier oid = Mockito.mock(ObjectIdentifier.class);
        bacnetInstance.initRemoteObjectPolling(rd, oid, 1000).test().assertError(BACnetException.class);
        Mockito.verify(pollingTimers)
               .addPoint(Mockito.any(BACnetInstance.class), Mockito.any(RemoteDevice.class),
                         Mockito.any(ObjectIdentifier.class), Mockito.anyLong());
    }

    @Test
    public void removePollingTest() throws Exception {
        RemoteDevice rd = Mockito.mock(RemoteDevice.class);
        ObjectIdentifier oid = Mockito.mock(ObjectIdentifier.class);
        bacnetInstance.removeRemoteObjectPolling(rd, oid).test().assertNoErrors();
        Mockito.verify(pollingTimers).removePoint(Mockito.any(RemoteDevice.class), Mockito.any(ObjectIdentifier.class));
    }

    @Test
    public void removePollingTest_Exception() throws Exception {
        Mockito.doThrow(BACnetException.class)
               .when(pollingTimers)
               .removePoint(Mockito.any(RemoteDevice.class), Mockito.any(ObjectIdentifier.class));
        RemoteDevice rd = Mockito.mock(RemoteDevice.class);
        ObjectIdentifier oid = Mockito.mock(ObjectIdentifier.class);
        bacnetInstance.removeRemoteObjectPolling(rd, oid).test().assertError(BACnetException.class);
        Mockito.verify(pollingTimers).removePoint(Mockito.any(RemoteDevice.class), Mockito.any(ObjectIdentifier.class));
    }

    @Test
    public void subscribeCOVTest() throws Exception {
        ServiceFutureImpl sf = new ServiceFutureImpl();
        sf.success(null);
        Mockito.when(transport.send(Mockito.any(Address.class), Mockito.anyInt(), Mockito.any(Segmentation.class),
                                    Mockito.any(ConfirmedRequestService.class))).thenReturn(sf);
        RemoteDevice remoteDevice1 = new RemoteDevice(localDevice, 1111, new Address(1, "yote".getBytes()));
        ObjectIdentifier oid = Mockito.mock(ObjectIdentifier.class);
        localDevice.getRemoteDeviceCache().putEntity(0, remoteDevice1, RemoteEntityCachePolicy.EXPIRE_1_DAY);

        JsonObject data = bacnetInstance.sendSubscribeCOVRequestBlocking(remoteDevice1, oid);
        assertEquals(new JsonObject(), data);
        assertEquals(1, remoteSubscriptions.size());
        Mockito.verify(pollingTimers)
               .addPoint(Mockito.any(BACnetInstance.class), Mockito.any(RemoteDevice.class),
                         Mockito.any(ObjectIdentifier.class), Mockito.anyLong());

        data = bacnetInstance.removeRemoteObjectSubscriptionBlocking(remoteDevice1, oid);
        assertEquals(new JsonObject(), data);
        assertEquals(0, remoteSubscriptions.size());
        Mockito.verify(pollingTimers).removePoint(Mockito.any(RemoteDevice.class), Mockito.any(ObjectIdentifier.class));
    }

    @Test(expected = ErrorAPDUException.class)
    public void subscribeCOBTest_NonSubscribe() throws Exception {
        ServiceFutureImpl sf = new ServiceFutureImpl();
        sf.fail(Mockito.mock(Error.class));
        Mockito.when(transport.send(Mockito.any(Address.class), Mockito.anyInt(), Mockito.any(Segmentation.class),
                                    Mockito.any(ConfirmedRequestService.class))).thenReturn(sf);
        RemoteDevice remoteDevice1 = new RemoteDevice(localDevice, 1111, new Address(1, "yote".getBytes()));
        localDevice.getRemoteDeviceCache().putEntity(0, remoteDevice1, RemoteEntityCachePolicy.EXPIRE_1_DAY);

        bacnetInstance.sendSubscribeCOVRequestBlocking(remoteDevice1, Mockito.mock(ObjectIdentifier.class));
    }

}
