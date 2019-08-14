package com.nubeiot.edge.connector.bacnet.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyValues;

//import com.nubeiot.core.utils.FileUtilsTest;


public class BACnetDataConversionsTest {

    @Before
    public void beforeEach() throws Exception {

    }

    @Test
    public void pointIDBACnetToNubeTest() throws Exception {
        ObjectIdentifier analogInput = new ObjectIdentifier(ObjectType.analogInput, 1);
        ObjectIdentifier analogOutput = new ObjectIdentifier(ObjectType.analogOutput, 1);
        ObjectIdentifier binaryInput = new ObjectIdentifier(ObjectType.binaryInput, 1);
        ObjectIdentifier binaryOutput = new ObjectIdentifier(ObjectType.binaryOutput, 1);
        ObjectIdentifier relay = new ObjectIdentifier(ObjectType.binaryOutput, 11);

        Assert.assertEquals("UI1", BACnetDataConversions.pointIDBACnetToNube(analogInput));
        Assert.assertEquals("UO1", BACnetDataConversions.pointIDBACnetToNube(analogOutput));
        Assert.assertEquals("DI1", BACnetDataConversions.pointIDBACnetToNube(binaryInput));
        Assert.assertEquals("DO1", BACnetDataConversions.pointIDBACnetToNube(binaryOutput));
        Assert.assertEquals("R1", BACnetDataConversions.pointIDBACnetToNube(relay));
    }

    @Test
    public void pointIDNubetoBACnetTest() throws Exception {

        Assert.assertEquals("analog-input:1", BACnetDataConversions.pointIDNubeToBACnet("UI1"));
        Assert.assertEquals("analog-output:1", BACnetDataConversions.pointIDNubeToBACnet("UO1"));
        Assert.assertEquals("binary-input:1", BACnetDataConversions.pointIDNubeToBACnet("DI1"));
        Assert.assertEquals("binary-output:1", BACnetDataConversions.pointIDNubeToBACnet("DO1"));
        Assert.assertEquals("binary-output:11", BACnetDataConversions.pointIDNubeToBACnet("R1"));


        Assert.assertEquals("analog-input:11", BACnetDataConversions.pointIDNubeToBACnet("UI11"));
        Assert.assertEquals("analog-input:111", BACnetDataConversions.pointIDNubeToBACnet("UI111"));
        Assert.assertEquals("analog-output:12", BACnetDataConversions.pointIDNubeToBACnet("UO12"));
        Assert.assertEquals("binary-input:13", BACnetDataConversions.pointIDNubeToBACnet("DI13"));
        Assert.assertEquals("binary-output:14", BACnetDataConversions.pointIDNubeToBACnet("DO14"));
        //TODO: the relay with more than 10 BI's problem.....

        //        Assert.assertEquals("binary-output:15", BACnetDataConversions.pointIDNubeToBACnet("R15"));
    }

//    @Test
//    public void CovNotificationTest() throws Exception {
//        ObjectIdentifier initObj = new ObjectIdentifier(ObjectType.device, 1);
//        ObjectIdentifier monObj = new ObjectIdentifier(ObjectType.analogOutput, 1);
//        SequenceOf<PropertyValue> listOfValues = new SequenceOf<>();
//
//        Assert.assertNull(BACnetDataConversions.CovNotification(initObj, monObj, listOfValues));
//
//        listOfValues.add(new PropertyValue(PropertyIdentifier.objectName, new CharacterString("yeet")));
//        Assert.assertNull(BACnetDataConversions.CovNotification(initObj, monObj, listOfValues));
//
//        listOfValues.add(new PropertyValue(PropertyIdentifier.presentValue, new Real(1)));
//        Assert.assertNull(BACnetDataConversions.CovNotification(initObj, monObj, listOfValues));
//
//        listOfValues = new SequenceOf<>();
//        listOfValues.add(new PropertyValue(PropertyIdentifier.presentValue, new UnsignedInteger(1), new Real(1),
//                                           new UnsignedInteger(16)));
//        Assert.assertNotNull(BACnetDataConversions.CovNotification(initObj, monObj, listOfValues));
//    }

    @Test
    public void getObjectIdentifierTest() throws Exception {
        String objStr = ObjectType.analogInput.toString() + ":1";
        ObjectIdentifier oid = BACnetDataConversions.getObjectIdentifier(objStr);
        assertEquals(oid, new ObjectIdentifier(ObjectType.analogInput, 1));

        objStr = ObjectType.binaryOutput + ":2";
        oid = BACnetDataConversions.getObjectIdentifier(objStr);
        assertEquals(oid, new ObjectIdentifier(ObjectType.binaryOutput, 2));

        objStr = "analog-output:1";
        oid = BACnetDataConversions.getObjectIdentifier(objStr);
        assertEquals(oid, new ObjectIdentifier(ObjectType.analogOutput, 1));

        objStr = "binary-input:2";
        oid = BACnetDataConversions.getObjectIdentifier(objStr);
        assertEquals(oid, new ObjectIdentifier(ObjectType.binaryInput, 2));
    }

    @Test(expected = BACnetRuntimeException.class)
    public void getObjectidentifierTest_BadInput() throws Exception {
        String objStr = "badInput:1";
        ObjectIdentifier oid = BACnetDataConversions.getObjectIdentifier(objStr);

        objStr = "badInput2";
        oid = BACnetDataConversions.getObjectIdentifier(objStr);
    }

    @Test
    public void encodableToPrimitive_Test() throws Exception {
        assertEquals(1f, BACnetDataConversions.encodableToPrimitive(new Real(1)));
        assertEquals(1, BACnetDataConversions.encodableToPrimitive(new UnsignedInteger(1)));
        assertEquals(1, BACnetDataConversions.encodableToPrimitive(BinaryPV.active));
        assertEquals("null", BACnetDataConversions.encodableToPrimitive(Null.instance));
        assertEquals("test", BACnetDataConversions.encodableToPrimitive(new CharacterString("test")));
    }

    @Test
    public void primitiveToBinary_Test() throws Exception {
        assertEquals(BinaryPV.active, BACnetDataConversions.primitiveToBinary(1));
        assertEquals(BinaryPV.active, BACnetDataConversions.primitiveToBinary(1f));
        assertEquals(BinaryPV.active, BACnetDataConversions.primitiveToBinary(1.5));
        assertEquals(BinaryPV.active, BACnetDataConversions.primitiveToBinary(1d));
        assertEquals(BinaryPV.active, BACnetDataConversions.primitiveToBinary(true));
        assertEquals(BinaryPV.active, BACnetDataConversions.primitiveToBinary("1"));
        assertEquals(BinaryPV.inactive, BACnetDataConversions.primitiveToBinary(false));
        assertEquals(BinaryPV.active, BACnetDataConversions.primitiveToBinary("true"));
        assertEquals(BinaryPV.inactive, BACnetDataConversions.primitiveToBinary("test"));
        assertEquals(BinaryPV.inactive, BACnetDataConversions.primitiveToBinary(null));
    }

    @Test
    public void primitiveToReal_Test() throws Exception {
        assertEquals(new Real(1.2f), BACnetDataConversions.primitiveToReal("1.2"));
        assertEquals(new Real(1.2f), BACnetDataConversions.primitiveToReal(1.2));
        assertEquals(new Real(1), BACnetDataConversions.primitiveToReal(1));
    }

    @Test(expected = BACnetException.class)
    public void primitiveToReal_Invalid() throws Exception {
        assertEquals(new Real(1.2f), BACnetDataConversions.primitiveToReal("test"));
    }

    @Test(expected = BACnetException.class)
    public void primitiveToReal_InvalidNull() throws Exception {
        assertEquals(new Real(1.2f), BACnetDataConversions.primitiveToReal(null));
    }

    @Test
    public void readMultipleToJson_Test() throws Exception {
        PropertyValues values = new PropertyValues();
        Assert.assertEquals(new JsonObject(), BACnetDataConversions.readMultipleToJson(values));
        values.add(new ObjectIdentifier(ObjectType.analogValue, 1), PropertyIdentifier.presentValue, new UnsignedInteger(0), new Real(0));
        values.add(new ObjectIdentifier(ObjectType.binaryValue, 1), PropertyIdentifier.presentValue, new UnsignedInteger(0), BinaryPV.active);

        JsonObject expected = new JsonObject().put("analog-value:1", 0.0).put("binary-value:1", 1);
        Assert.assertEquals(expected, BACnetDataConversions.readMultipleToJson(values));
    }
}
