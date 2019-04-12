package com.nubeiot.edge.connector.bacnet;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.connector.bacnet.Util.BACnetDataConversions;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.cache.RemoteEntityCachePolicy;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class BACnetTest {

    EventController eventController;
    Vertx vertx;
    LocalDevice localDevice;
    DefaultTransport transport;
    BACnetInstance bacnetInstance;

    @Before
    public void beforeEach() throws Exception {
        vertx = Mockito.mock(Vertx.class);
        transport = Mockito.mock(DefaultTransport.class);
        localDevice = new LocalDevice(1234, transport);
        bacnetInstance = BACnetInstance.createBACnet(localDevice, vertx);
    }

    @After
    public void afterEach() throws Exception {
        localDevice.terminate();
    }

    @Test
    public void initPointsFromJsonTest() {
        final URL POINTS_RESOURCE = FileUtils.class.getClassLoader().getResource("points.json");
        JsonObject points = new JsonObject(FileUtils.readFileToString(POINTS_RESOURCE.toString()));

        bacnetInstance.initialiseLocalObjectsFromJson(points);
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

    @Test
    public void getObjectIdentifierTest() throws Exception {
        String objStr = ObjectType.analogInput.toString() + ":1";
        ObjectIdentifier oid = bacnetInstance.getObjectIdentifier(objStr);
        assertEquals(oid, new ObjectIdentifier(ObjectType.analogInput, 1));

        objStr = ObjectType.binaryOutput + ":2";
        oid = bacnetInstance.getObjectIdentifier(objStr);
        assertEquals(oid, new ObjectIdentifier(ObjectType.binaryOutput, 2));

        objStr = "analog-output:1";
        oid = bacnetInstance.getObjectIdentifier(objStr);
        assertEquals(oid, new ObjectIdentifier(ObjectType.analogOutput, 1));

        objStr = "binary-input:2";
        oid = bacnetInstance.getObjectIdentifier(objStr);
        assertEquals(oid, new ObjectIdentifier(ObjectType.binaryInput, 2));
    }

    @Test(expected = BACnetRuntimeException.class)
    public void getObjectidentifierTest_NoObjectException() throws Exception {
        String objStr = "badInput:1";
        ObjectIdentifier oid = bacnetInstance.getObjectIdentifier(objStr);
    }

    @Test(expected = BACnetRuntimeException.class)
    public void getObjectidentifierTest_BadInputException() throws Exception {
        String objStr = "badInput2";
        ObjectIdentifier oid = bacnetInstance.getObjectIdentifier(objStr);
    }

}
