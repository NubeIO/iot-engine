package com.nubeiot.edge.connector.bacnet.Util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

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
        String UI = "UI1";
        String UO = "UO1";
        String DI = "DI1";
        String DO = "DO1";
        String R = "R1";

        Assert.assertEquals("analog-input:1", BACnetDataConversions.pointIDNubeToBACnet(UI));
        Assert.assertEquals("analog-output:1", BACnetDataConversions.pointIDNubeToBACnet(UO));
        Assert.assertEquals("binary-input:1", BACnetDataConversions.pointIDNubeToBACnet(DI));
        Assert.assertEquals("binary-output:1", BACnetDataConversions.pointIDNubeToBACnet(DO));
        Assert.assertEquals("binary-output:11", BACnetDataConversions.pointIDNubeToBACnet(R));
    }

    @Test
    public void CovNotificationTest() throws Exception {
        ObjectIdentifier initObj = new ObjectIdentifier(ObjectType.device, 1);
        ObjectIdentifier monObj = new ObjectIdentifier(ObjectType.analogOutput, 1);
        SequenceOf<PropertyValue> listOfValues = new SequenceOf<>();

        Assert.assertNull(BACnetDataConversions.CovNotification(initObj, monObj, listOfValues));

        listOfValues.add(new PropertyValue(PropertyIdentifier.objectName, new CharacterString("yeet")));
        Assert.assertNull(BACnetDataConversions.CovNotification(initObj, monObj, listOfValues));

        listOfValues.add(new PropertyValue(PropertyIdentifier.presentValue, new Real(1)));
        Assert.assertNull(BACnetDataConversions.CovNotification(initObj, monObj, listOfValues));

        listOfValues = new SequenceOf<>();
        listOfValues.add(new PropertyValue(PropertyIdentifier.presentValue, new UnsignedInteger(1), new Real(1),
                                           new UnsignedInteger(16)));
        Assert.assertNotNull(BACnetDataConversions.CovNotification(initObj, monObj, listOfValues));
    }

}
