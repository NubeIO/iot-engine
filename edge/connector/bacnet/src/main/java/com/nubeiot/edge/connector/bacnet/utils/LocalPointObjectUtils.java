package com.nubeiot.edge.connector.bacnet.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.edge.connector.bacnet.objectModels.EdgePoint;
import com.nubeiot.edge.connector.bacnet.objectModels.EdgeWriteRequest;
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

    public static BACnetObject createLocalObject(EdgePoint point, LocalDevice localDevice) {
        BACnetObject obj;

        try {
            ObjectIdentifier oid = BACnetDataConversions.getObjectIdentifierFromNube(point.getId());
            ObjectType t = oid.getObjectType();
            if (t == ObjectType.analogInput) {
                obj = new AnalogInputObject(localDevice, oid.getInstanceNumber(), point.getName(),
                                            BACnetDataConversions.primitiveToReal(point.getValue()).floatValue(),
                                            EngineeringUnits.noUnits, false).supportCovReporting(
                    point.getCovTolerance());
            } else if (t == ObjectType.analogOutput) {
                obj = new AnalogOutputObject(localDevice, oid.getInstanceNumber(), point.getName(),
                                             BACnetDataConversions.primitiveToReal(point.getValue()).floatValue(),
                                             EngineeringUnits.noUnits, false, 0).supportCovReporting(
                    point.getCovTolerance());
            } else if (t == ObjectType.binaryInput) {
                BinaryPV binaryVal1 = BACnetDataConversions.primitiveToBinary(point.getValue());
                obj = new BinaryInputObject(localDevice, oid.getInstanceNumber(), point.getName(), binaryVal1, false,
                                            Polarity.normal).supportCovReporting();
            } else if (t == ObjectType.binaryOutput) {
                BinaryPV binaryVal2 = BACnetDataConversions.primitiveToBinary(point.getValue());
                obj = new BinaryOutputObject(localDevice, oid.getInstanceNumber(), point.getName(), binaryVal2, false,
                                             Polarity.normal, BinaryPV.inactive).supportCovReporting();
            }
            //TODO: what to do for virtual points?
            else {
                return null;
            }
        } catch (Exception ex) {
            logger.error("Failure creating point {}", ex, point.getId());
            return null;
        }

        writePriorityArray(obj, point.getPriorityArray());
        return obj;
    }

    private static void writePriorityArray(BACnetObject obj, Object[] priorityArray) {
        if (priorityArray == null || priorityArray.length < 16) {
            return;
        }
        Encodable val;
        for (int i = 0; i < 16; i++) {
            Object o = priorityArray[i];
            try {
                if (o == null) {
                    continue;
                }
                if (isBinary(obj) && !(o instanceof String)) {
                    val = BACnetDataConversions.primitiveToBinary(o);
                } else if (o instanceof String) {
                    if (((String) o).equalsIgnoreCase("null")) {
                        continue;
                    } else {
                        val = new CharacterString((String) o);
                    }
                } else {
                    val = BACnetDataConversions.primitiveToReal(priorityArray[i]);
                }
                obj.writeProperty(new ValueSource(), new PropertyValue(PropertyIdentifier.presentValue, null, val,
                                                                       new UnsignedInteger(i + 1)));
            } catch (Exception ex) {
                logger.warn(
                    "Error creating priority array -> Object: " + obj.getInstanceId() + "  - Issue writing value " + o +
                    " of type " + o.getClass() + " @ priority " + (i+1), ex);
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

    public static void writeLocalObject(EdgeWriteRequest req, LocalDevice localDevice) throws Exception {
        if (isInputFromNube(req.getId())) {
            writeToLocalInput(localDevice.getObject(BACnetDataConversions.getObjectIdentifierFromNube(req.getId())),
                              req.getValue());
        } else {
            writeToLocalOutput(localDevice.getObject(BACnetDataConversions.getObjectIdentifierFromNube(req.getId())),
                               req.getValue(), req.getPriority());
        }
    }

    private static void writeToLocalInput(BACnetObject obj, Object value) throws Exception {
        Encodable val;
        if (isBinary(obj)) {
            val = BACnetDataConversions.primitiveToBinary(value);
        } else {
            val = BACnetDataConversions.primitiveToReal(value);
        }
        obj.writePropertyInternal(PropertyIdentifier.presentValue, val);
    }

    private static void writeToLocalOutput(BACnetObject obj, Object value, int priority)
        throws BACnetException, BACnetServiceException {
        if (priority > 16 || priority < 1) {
            throw new BACnetException("Invalid priority: " + priority);
        }
        Encodable val;
        if (isBinary(obj)) {
            val = BACnetDataConversions.primitiveToBinary(value);
        } else {
            val = BACnetDataConversions.primitiveToReal(value);
        }
        obj.writeProperty(new ValueSource(),
                          new PropertyValue(PropertyIdentifier.presentValue, null, val, new UnsignedInteger(priority)));
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
            throw new BACnetException("Use write to point instead");
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
