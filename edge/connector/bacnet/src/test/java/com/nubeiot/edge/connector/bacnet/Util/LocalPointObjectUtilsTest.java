package com.nubeiot.edge.connector.bacnet.Util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.FileUtils;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.obj.AnalogInputObject;
import com.serotonin.bacnet4j.obj.AnalogOutputObject;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.BinaryInputObject;
import com.serotonin.bacnet4j.obj.BinaryOutputObject;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.PriorityArray;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;

//import com.nubeiot.core.utils.FileUtilsTest;

public class LocalPointObjectUtilsTest {

    private LocalDevice localDevice;
    private JsonObject points;

    @Before
    public void beforeAll() throws Exception {
        String broadcastAddress = NetworkUtils.getBroadcastAddress();
        int networkPrefixLength = NetworkUtils.getNetworkPrefixLength();
        IpNetwork network = new IpNetworkBuilder().withBroadcast(broadcastAddress, networkPrefixLength).build();
        Transport transport = new DefaultTransport(network);
        localDevice = new LocalDevice(1234, transport);

        final URL POINTS_RESOURCE = FileUtils.class.getClassLoader().getResource("points.json");
        points = new JsonObject(FileUtils.readFileToString(POINTS_RESOURCE.toString()));
    }

    @Test
    public void createLocalObjectUITest() throws Exception {

        JsonObject UI1 = points.getJsonObject("UI1");

        BACnetObject obj = LocalPointObjectUtils.createLocalObject(UI1, "UI1", localDevice);
        assertNotNull(obj);
        assertTrue(obj instanceof AnalogInputObject);
        assertEquals("UI1", obj.getObjectName());
        assertEquals(new Real(UI1.getFloat("value")), obj.get(PropertyIdentifier.presentValue));
    }

    @Test
    public void createLocalObjectDITest() throws Exception {

        JsonObject DI1 = points.getJsonObject("DI1");

        BACnetObject obj = LocalPointObjectUtils.createLocalObject(DI1, "DI1", localDevice);
        assertNotNull(obj);
        assertTrue(obj instanceof BinaryInputObject);
        assertEquals("DI1", obj.getObjectName());

        if (DI1.getInteger("value") == 1) {
            assertEquals(obj.get(PropertyIdentifier.presentValue), BinaryPV.active);
        } else {
            assertEquals(obj.get(PropertyIdentifier.presentValue), BinaryPV.inactive);
        }
    }

    @Test
    public void createLocalObjectUOTest() throws Exception {

        JsonObject UO1 = points.getJsonObject("UO1");

        BACnetObject obj = LocalPointObjectUtils.createLocalObject(UO1, "UO1", localDevice);
        assertNotNull(obj);
        assertTrue(obj instanceof AnalogOutputObject);
        assertEquals("UO1", obj.getObjectName());
        assertEquals(new Real(UO1.getFloat("value")), obj.get(PropertyIdentifier.presentValue));
        JsonObject pa1 = UO1.getJsonObject("priorityArray");
        PriorityArray pa2 = obj.get(PropertyIdentifier.priorityArray);
        for (int i = 0; i < 16; i++) {
            if (pa1.getValue(Integer.toString(i + 1)) instanceof String) {
                continue;
            }
            Real v1 = new Real(pa1.getFloat(Integer.toString(i + 1)));
            Real v2 = pa2.get(i).getRealValue();
            assertEquals(v1, v2);
        }
    }

    @Test
    public void createLocalObjectDOTest() throws Exception {

        JsonObject DO1 = points.getJsonObject("DO1");

        BACnetObject obj = LocalPointObjectUtils.createLocalObject(DO1, "DO1", localDevice);
        assertNotNull(obj);
        assertTrue(obj instanceof BinaryOutputObject);
        assertEquals("DO1", obj.getObjectName());

        if (DO1.getInteger("value") == 1) {
            assertEquals(obj.get(PropertyIdentifier.presentValue), BinaryPV.active);
        } else {
            assertEquals(obj.get(PropertyIdentifier.presentValue), BinaryPV.inactive);
        }

        JsonObject pa1 = DO1.getJsonObject("priorityArray");
        PriorityArray pa2 = obj.get(PropertyIdentifier.priorityArray);
        for (int i = 0; i < 16; i++) {
            if (pa1.getValue(Integer.toString(i + 1)) instanceof String) {
                continue;
            }
            Real v1 = new Real(pa1.getFloat(Integer.toString(i + 1)));
            Real v2 = pa2.get(i).getRealValue();
            assertEquals(v1, v2);
        }
    }

}
