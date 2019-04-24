package com.nubeiot.edge.connector.bacnet.Util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.AnalogInputObject;
import com.serotonin.bacnet4j.obj.AnalogOutputObject;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.BinaryInputObject;
import com.serotonin.bacnet4j.obj.BinaryOutputObject;
import com.serotonin.bacnet4j.obj.BinaryValueObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.ValueSource;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.Polarity;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

/*
 * Static methods to create and add objects/points on the local BACnetInstance device instance
 *
 * essentially a JSON parser to create local objects
 */
public class LocalPointObjectUtils {

    public static Logger logger = LoggerFactory.getLogger(LocalPointObjectUtils.class);

    public static BACnetObject createLocalObject(String pointID, JsonObject json, LocalDevice localDevice) {
        BACnetObject obj;
        String name = "NAME_ERROR";
        float presentValue = 0;
        float covIncrement = 0;

        if (json.containsKey("name")) {
            name = json.getString("name");
        }
        if (json.containsKey("value")) {
            presentValue = json.getFloat("value");
        }
        if (json.containsKey("historySettings") && json.getJsonObject("historySettings").containsKey("tolerance")) {
            covIncrement = json.getJsonObject("historySettings").getFloat("tolerance");
        }

        try {
            obj = addBACnetObject(localDevice, BACnetDataConversions.getObjectIdentifierFromNube(pointID), name,
                                  presentValue, covIncrement);
        } catch (Exception ex) {
            logger.warn(ex);
            return null;
        }

        writePriorityArray(obj, json.getJsonObject("priorityArray"));
        return obj;
    }

    public static BACnetObject addBACnetObject(LocalDevice localDevice, ObjectIdentifier oid, String name,
                                               float presentValue, float covIncrement)
        throws BACnetException, BACnetServiceException {
        int instanceNumber = oid.getInstanceNumber();
        ObjectType t = oid.getObjectType();
        if (t == ObjectType.analogInput) {
            return new AnalogInputObject(localDevice, instanceNumber, name, presentValue, EngineeringUnits.noUnits,
                                         false).supportCovReporting(covIncrement);
        }
        if (t == ObjectType.analogOutput) {
            return new AnalogOutputObject(localDevice, instanceNumber, name, presentValue, EngineeringUnits.noUnits,
                                          false, 0).supportCovReporting(covIncrement);
        }
        if (t == ObjectType.binaryInput) {
            BinaryPV binaryVal1 = BACnetDataConversions.primitiveToBinary(presentValue);
            return new BinaryInputObject(localDevice, instanceNumber, name, binaryVal1, false,
                                         Polarity.normal).supportCovReporting();
        }
        if (t == ObjectType.binaryOutput) {
            BinaryPV binaryVal2 = BACnetDataConversions.primitiveToBinary(presentValue);
            return new BinaryOutputObject(localDevice, instanceNumber, name, binaryVal2, false, Polarity.normal,
                                          BinaryPV.inactive).supportCovReporting();
        }
        //TODO: what to do for virtual points?
        return null;
    }

    private static void writePriorityArray(BACnetObject obj, JsonObject priorityArray) {
        if (priorityArray == null) {
            return;
        }
        Encodable val;
        for (int i = 1; i <= 16; i++) {
            Object o = priorityArray.getValue(Integer.toString(i));
            try {
                if (isBinary(obj) && !(o instanceof String)) {
                    val = BACnetDataConversions.primitiveToBinary(o);
                } else if (o instanceof String) {
                    if (((String) o).equalsIgnoreCase("null")) {
                        continue;
                    } else {
                        val = new CharacterString((String) o);
                    }
                } else {
                    val = new Real(priorityArray.getFloat(Integer.toString(i)));
                }
                obj.writeProperty(new ValueSource(), new PropertyValue(PropertyIdentifier.presentValue, null, val,
                                                                       new UnsignedInteger(i)));
            } catch (Exception ex) {
                System.err.println(
                    "Object: " + obj.getInstanceId() + "  - Issue writing value " + o + " of type " + o.getClass() +
                    " @ priority " + i);
                ex.printStackTrace();
            }
        }
    }

    private static CharacterString[] getTags(Object val) {
        JsonArray tagsTmp = ((JsonArray) val);
        CharacterString[] tags = new CharacterString[tagsTmp.size()];
        for (int i = 0; i < tagsTmp.size(); i++) {
            tags[i] = new CharacterString(tagsTmp.getString(i));
        }
        return tags;
    }

    public static void writeLocalObject(String nubeId, JsonObject json, LocalDevice localDevice) throws Exception {
        if (!json.containsKey("value")) {
            throw new BACnetException("No value suplied");
        }
        if (isInputFromNube(nubeId)) {
            writeToLocalInput(localDevice.getObject(BACnetDataConversions.getObjectIdentifierFromNube(nubeId)), json);
        } else {
            writeToLocalOutput(localDevice.getObject(BACnetDataConversions.getObjectIdentifierFromNube(nubeId)), json);
        }
    }

    private static void writeToLocalInput(BACnetObject obj, JsonObject json) {
        Encodable val;
        if (isBinary(obj)) {
            val = BACnetDataConversions.primitiveToBinary(json.getValue("value"));
        } else {
            val = new Real(json.getFloat("value"));
        }
        try {
            obj.writePropertyInternal(PropertyIdentifier.presentValue, val);
        } catch (Exception e) {
            logger.warn("Error writing to local input object", e);
        }
    }

    private static void writeToLocalOutput(BACnetObject obj, JsonObject json)
        throws BACnetException, BACnetServiceException {
        if (!json.containsKey("priority")) {
            throw new BACnetException("No priority supplied for writing Output type");
        }
        if (json.getInteger("priority") > 16 || json.getInteger("priority") < 1) {
            throw new BACnetException("Invalid priority: " + json.getInteger("priority"));
        }
        Encodable val;
        if (isBinary(obj)) {
            val = BACnetDataConversions.primitiveToBinary(json.getValue("value"));
        } else {
            val = new Real(json.getFloat("value"));
        }
        obj.writeProperty(new ValueSource(), new PropertyValue(PropertyIdentifier.presentValue, null, val,
                                                               new UnsignedInteger(json.getInteger("priority"))));
    }

    public static void updateLocalObjectProperty(LocalDevice localDevice, String nubeId, String property, Object val)
        throws Exception {
        PropertyValue propertyValue = BACnetDataConversions.nubeStringToPropertyValue(property, val);
        BACnetObject obj = localDevice.getObject(BACnetDataConversions.getObjectIdentifierFromNube(nubeId));
        updateLocalObjectProperty(obj, propertyValue.getPropertyIdentifier(), propertyValue.getValue());
    }

    public static void updateLocalObjectProperty(BACnetObject obj, PropertyIdentifier pid, Encodable val)
        throws Exception {
        if (pid == PropertyIdentifier.presentValue) {
            //TODO: support updating present value from "update" instead of "write"
            throw new BACnetException("Not yet supported updating value instead of writing");
        }
        if (obj == null) {
            throw new BACnetException("Object does not exist");
        }
        obj.writePropertyInternal(pid, val);
    }

    private static boolean isBinary(BACnetObject obj) {
        return (obj instanceof BinaryInputObject || obj instanceof BinaryOutputObject ||
                obj instanceof BinaryValueObject);
    }

    private static boolean isInputFromNube(String id) {
        try {
            return isInput(BACnetDataConversions.getObjectIdentifierFromNube(id));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isInput(ObjectIdentifier oid) {
        return oid.getObjectType().isOneOf(ObjectType.binaryInput, ObjectType.analogInput, ObjectType.multiStateInput);
    }

}
