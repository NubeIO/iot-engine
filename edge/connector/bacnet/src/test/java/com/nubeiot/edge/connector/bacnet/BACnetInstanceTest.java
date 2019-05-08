package com.nubeiot.edge.connector.bacnet;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.connector.bacnet.utils.BACnetDataConversions;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.cache.RemoteEntityCachePolicy;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.constructed.Address;

@RunWith(MockitoJUnitRunner.class)
public class BACnetInstanceTest {

    private LocalDevice localDevice;
    private DefaultTransport transport;
    private BACnetInstance bacnetInstance;
    private Map<String, Integer> remoteSubscriptions;

    @Before
    public void beforeEach() throws Exception {
        Vertx vertx = Mockito.mock(Vertx.class);
        transport = Mockito.mock(DefaultTransport.class);
        localDevice = new LocalDevice(1234, transport);
        remoteSubscriptions = new HashMap<>();
        bacnetInstance = BACnetInstance.createBACnet(localDevice, vertx, remoteSubscriptions);
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
}
