package com.nubeiot.edge.connector.bacnet;

import java.net.URL;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.connector.bacnet.objectModels.EdgePoint;
import com.nubeiot.edge.connector.bacnet.utils.BACnetDataConversions;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.cache.RemoteEntityCachePolicy;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.RemoteDeviceDiscoverer;

@RunWith(MockitoJUnitRunner.class)
public class BACnetInstanceTest {

    private LocalDevice localDevice;
    private DefaultTransport transport;
    private BACnetInstance bacnetInstance;
    private JsonObject points;
    private Vertx vertx;

    @Before
    public void beforeEach() throws Exception {
        vertx = Mockito.mock(Vertx.class);
        transport = Mockito.mock(DefaultTransport.class);
        localDevice = new LocalDevice(1234, transport);
        bacnetInstance = BACnetInstance.create(vertx, BACnetInstanceTest.class.getName(), localDevice);
        final URL POINTS_RESOURCE = FileUtils.class.getClassLoader().getResource("points.json");
        points = new JsonObject(FileUtils.readFileToString(POINTS_RESOURCE.toString()));
    }

    @After
    public void afterEach() throws Exception {
        localDevice.terminate();
    }

    private void initPointsFromJson() throws Exception {
        bacnetInstance.initialiseLocalObjectsFromJson(points);
    }

    @Test
    public void initPointsFromJsonTest() throws Exception {
        initPointsFromJson();
        points.getMap().keySet().forEach(s -> {
            try {
                Assert.assertNotNull(bacnetInstance.getLocalObjectId(s));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        });
    }

    @Test
    public void discoveryTest() throws Exception {
        LocalDevice ld = Mockito.mock(LocalDevice.class);
        BACnetInstance inst = BACnetInstance.create(vertx, BACnetInstanceTest.class.getName(), ld);
        Mockito.when(ld.startRemoteDeviceDiscovery()).thenReturn(Mockito.mock(RemoteDeviceDiscoverer.class));
        inst.startRemoteDiscover();
        Mockito.verify(ld).clearRemoteDevices();
        Mockito.verify(ld).startRemoteDeviceDiscovery(Mockito.any(Consumer.class));
        Mockito.verify(vertx).setTimer(Mockito.anyLong(), Mockito.any(Handler.class));
    }

    @Test
    public void getLocalObjectId() throws Exception {
        initPointsFromJson();

        points.getMap().keySet().forEach(s -> {
            try {
                Assert.assertNotNull(bacnetInstance.getLocalObjectId(s));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        });
    }

    @Test(expected = BACnetException.class)
    public void getLocalObjectIdError() throws Exception {
        bacnetInstance.getLocalObjectId("no_point");
    }

    @Test
    public void getLocalObjectNubeId() throws Exception {

        points.getMap().keySet().forEach(s -> {
            try {
                EdgePoint p = EdgePoint.fromJson(s, points.getJsonObject(s));
                ObjectIdentifier oid = bacnetInstance.addLocalObject(p).getId();
                Assert.assertEquals(s, bacnetInstance.getLocalObjectNubeId(oid));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        });
    }

    @Test
    public void getLocalObjectNubeIdError() throws Exception {
        Assert.assertNull(bacnetInstance.getLocalObjectNubeId(new ObjectIdentifier(ObjectType.binaryValue, 11)));
    }

    @Test
    public void addLocalPoint() throws Exception {
        points.getMap().forEach((s, o) -> {
            EdgePoint p = EdgePoint.fromJson(s, JsonObject.mapFrom(o));
            try {
                bacnetInstance.addLocalObject(p);
                Assert.assertNotNull(bacnetInstance.getLocalObjectId(s));
                Assert.assertNotNull(localDevice.getObject(bacnetInstance.getLocalObjectId(s)));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        });
    }

    @Test
    public void removeLocalPoint() throws Exception {
        initPointsFromJson();

        points.getMap().keySet().forEach(s -> {
            try {
                ObjectIdentifier oid = bacnetInstance.getLocalObjectId(s);
                bacnetInstance.removeLocalObject(s);
                Assert.assertNull(localDevice.getObject(oid));
                try {
                    bacnetInstance.getLocalObjectId(s);
                    Assert.fail();
                } catch (BACnetException ex) {
                }
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        });
    }

    @Test
    public void getLocalObjectIdTest() throws Exception {
        initPointsFromJsonTest();
        Assert.assertNotNull(bacnetInstance.getLocalObjectId("UI1"));
        Assert.assertNotNull(bacnetInstance.getLocalObjectId("DI1"));
        Assert.assertNotNull(bacnetInstance.getLocalObjectId("UO1"));
        Assert.assertNotNull(bacnetInstance.getLocalObjectId("DO1"));
        Assert.assertNotNull(bacnetInstance.getLocalObjectId("4AB28169_MOVEMENT"));
        Assert.assertNotNull(bacnetInstance.getLocalObjectId("4AB28169_TEMP"));
    }

    @Test(expected = BACnetException.class)
    public void getLocalObjectIdTest_invalidPoint() throws Exception {
        Assert.assertNull(bacnetInstance.getLocalObjectId("UI9"));
        Assert.assertNull(bacnetInstance.getLocalObjectId("no_point"));
    }

    @Test
    public void getRemoteDevicesTest() throws Exception {

        bacnetInstance.getRemoteDevices().test().assertNoErrors().assertValue(new JsonObject());

        RemoteDevice remoteDevice1 = new RemoteDevice(localDevice, 1111, new Address(1, "yote".getBytes()));
        RemoteDevice remoteDevice2 = new RemoteDevice(localDevice, 2222, new Address(2, "yeet".getBytes()));
        localDevice.getRemoteDeviceCache().putEntity(0, remoteDevice1, RemoteEntityCachePolicy.EXPIRE_1_DAY);
        localDevice.getRemoteDeviceCache().putEntity(1, remoteDevice2, RemoteEntityCachePolicy.EXPIRE_1_DAY);

        JsonObject rdJson = new JsonObject();
        rdJson.put(Integer.toString(remoteDevice1.getInstanceNumber()),
                   BACnetDataConversions.deviceMinimal(remoteDevice1));
        rdJson.put(Integer.toString(remoteDevice2.getInstanceNumber()),
                   BACnetDataConversions.deviceMinimal(remoteDevice2));
        bacnetInstance.getRemoteDevices().test().assertNoErrors().assertValue(rdJson);
    }

    @Test
    public void getRemoteDeviceExtendedInfoTest_InvalidID() throws Exception {
        RemoteDevice remoteDevice1 = new RemoteDevice(localDevice, 1111, new Address(1, "yote".getBytes()));
        localDevice.getRemoteDeviceCache().putEntity(0, remoteDevice1, RemoteEntityCachePolicy.EXPIRE_1_DAY);

        bacnetInstance.getRemoteDeviceExtendedInfo(1).test().assertError(BACnetRuntimeException.class);
    }

    @Test
    public void getRemoteObjectPropertiesTest_InvalidID() throws Exception {
        RemoteDevice remoteDevice1 = new RemoteDevice(localDevice, 1111, new Address(1, "yote".getBytes()));
        localDevice.getRemoteDeviceCache().putEntity(0, remoteDevice1, RemoteEntityCachePolicy.EXPIRE_1_DAY);

        bacnetInstance.getRemoteObjectProperties(1111, "Bad_Object:ID")
                      .test()
                      .assertError(BACnetRuntimeException.class);
    }

}
