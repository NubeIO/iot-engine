package com.nubeiot.edge.connector.bacnet.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.FileUtils;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.obj.AnalogInputObject;
import com.serotonin.bacnet4j.obj.AnalogOutputObject;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.BinaryInputObject;
import com.serotonin.bacnet4j.obj.BinaryOutputObject;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.PriorityArray;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.Polarity;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;

public class LocalPointObjectUtilsTest {

    private LocalDevice localDevice;
    private JsonObject points;

    @Before
    public void beforeEach() throws Exception {
        Transport transport = Mockito.mock(DefaultTransport.class);
        localDevice = new LocalDevice(1234, transport);

        final URL POINTS_RESOURCE = FileUtils.class.getClassLoader().getResource("points.json");
        points = new JsonObject(FileUtils.readFileToString(POINTS_RESOURCE.toString()));
    }

    @Test
    public void createLocalObjectUITest() throws Exception {
        JsonObject UI1 = points.getJsonObject("UI1");
        BACnetObject obj = LocalPointObjectUtils.createLocalObject("UI1", UI1, localDevice);
        assertNotNull(obj);
        assertTrue(obj instanceof AnalogInputObject);
        assertEquals("UI1", obj.getObjectName());
        assertEquals(new Real(UI1.getFloat("value")), obj.get(PropertyIdentifier.presentValue));
    }

    @Test
    public void createLocalObjectDITest() throws Exception {
        JsonObject DI1 = points.getJsonObject("DI1");
        BACnetObject obj = LocalPointObjectUtils.createLocalObject("DI1", DI1, localDevice);
        assertNotNull(obj);
        assertTrue(obj instanceof BinaryInputObject);
        assertEquals("DI1", obj.getObjectName());
        assertEquals(BinaryPV.forId(DI1.getInteger("value")), obj.get(PropertyIdentifier.presentValue));
    }

    @Test
    public void createLocalObjectUOTest() throws Exception {
        JsonObject UO1 = points.getJsonObject("UO1");
        BACnetObject obj = LocalPointObjectUtils.createLocalObject("UO1", UO1, localDevice);
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
        BACnetObject obj = LocalPointObjectUtils.createLocalObject("DO1", DO1, localDevice);
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

    @Test(expected = BACnetException.class)
    public void writeLocalObjectTest_NoValue() throws Exception {
        LocalPointObjectUtils.writeLocalObject("UI1", new JsonObject(), localDevice);
    }

    @Test(expected = BACnetException.class)
    public void writeLocalObjectTest_NoPriority() throws Exception {
        LocalPointObjectUtils.writeLocalObject("UO1", new JsonObject().put("value", 1), localDevice);
    }

    @Test(expected = BACnetException.class)
    public void writeLocalObjectTest_BadPriority() throws Exception {
        BACnetObject analogout = new AnalogOutputObject(localDevice, 1, "UO1", 0, EngineeringUnits.noUnits, false, 0);
        JsonObject data1 = new JsonObject().put("value", 1).put("priority", 17);
        JsonObject data2 = new JsonObject().put("value", 1).put("priority", 0);
        LocalPointObjectUtils.writeLocalObject("UO1", data1, localDevice);
        LocalPointObjectUtils.writeLocalObject("UO1", data2, localDevice);
    }

    @Test
    public void writeLocalObjectTest() throws Exception {
        BACnetObject analogIn = new AnalogInputObject(localDevice, 1, "UI1", 0, EngineeringUnits.noUnits, false);
        BACnetObject analogout = new AnalogOutputObject(localDevice, 1, "UO1", 0, EngineeringUnits.noUnits, false, 0);
        BACnetObject binaryOut = new BinaryOutputObject(localDevice, 1, "DO1", BinaryPV.inactive, false,
                                                        Polarity.normal, BinaryPV.inactive);
        BACnetObject binaryIn = new BinaryInputObject(localDevice, 1, "DI1", BinaryPV.inactive, false, Polarity.normal);

        JsonObject data = new JsonObject().put("value", 1).put("priority", 16);

        LocalPointObjectUtils.writeLocalObject("UI1", data, localDevice);
        Assert.assertEquals(new Real(1), localDevice.getObject("UI1").get(PropertyIdentifier.presentValue));

        LocalPointObjectUtils.writeLocalObject("UO1", data, localDevice);
        Assert.assertEquals(new Real(1), localDevice.getObject("UO1").get(PropertyIdentifier.presentValue));

        LocalPointObjectUtils.writeLocalObject("DO1", data, localDevice);
        Assert.assertEquals(BinaryPV.active, localDevice.getObject("DO1").get(PropertyIdentifier.presentValue));

        LocalPointObjectUtils.writeLocalObject("DI1", data, localDevice);
        Assert.assertEquals(BinaryPV.active, localDevice.getObject("DI1").get(PropertyIdentifier.presentValue));
    }

    @Test
    public void updatePointProperty_Test() throws Exception {
        BACnetObject analogIn = new AnalogInputObject(localDevice, 1, "UI1", 0, EngineeringUnits.noUnits, false);

        LocalPointObjectUtils.updateLocalObjectProperty(localDevice, "UI1", "name", "testName");
        Assert.assertEquals("testName", analogIn.getObjectName());
    }

    @Test(expected = BACnetException.class)
    public void updatePointProperty_InvalidProperty() throws Exception {
        LocalPointObjectUtils.updateLocalObjectProperty(localDevice, "UI1", "badName", "testName");
    }

    @Test(expected = BACnetException.class)
    public void updatePointProperty_InvalidObject() throws Exception {
        LocalPointObjectUtils.updateLocalObjectProperty(localDevice, "test", "name", "testName");
    }
}
