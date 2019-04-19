package com.nubeiot.edge.connector.bacnet.Util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import io.vertx.core.json.JsonObject;

import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class BACnetDataConversions {

    public static JsonObject deviceMinimal(RemoteDevice remoteDevice) {
        JsonObject data = new JsonObject();
        data.put("instanceNumber", remoteDevice.getInstanceNumber());
        data.put("name", remoteDevice.getName());
        data.put("address", remoteDevice.getAddress().toString());
        return data;
    }

    public static JsonObject deviceExtended(RemoteDevice remoteDevice) {
        JsonObject data = new JsonObject();
        data.put("instanceNumber", remoteDevice.getInstanceNumber());
        data.put("name", remoteDevice.getName());
        data.put("address", remoteDevice.getAddress().toString());
        data.put("maxAPDULengthAccepted",
                 encodableToPrimitive(remoteDevice.getDeviceProperty(PropertyIdentifier.maxApduLengthAccepted)));
        data.put("segmentationSupported",
                 encodableToPrimitive(remoteDevice.getDeviceProperty(PropertyIdentifier.segmentationSupported)));
        data.put("vendorId", encodableToPrimitive(remoteDevice.getDeviceProperty(PropertyIdentifier.vendorIdentifier)));
        data.put("vendorName", remoteDevice.getDeviceProperty(PropertyIdentifier.vendorName));
        data.put("servicesSupported",
                 encodableToPrimitive(remoteDevice.getDeviceProperty(PropertyIdentifier.protocolServicesSupported)));
        return data;
    }

    //TODO: create method to map Encodables to primitives
    public static JsonObject deviceObjectList(List<Pair<ObjectPropertyReference, Encodable>> list) {
        JsonObject data = new JsonObject();
        list.forEach(objectPropertyReferenceEncodablePair -> {
            String key = pointFormatBACnet(objectPropertyReferenceEncodablePair.getLeft().getObjectIdentifier());
            if (!data.containsKey(key)) {
                data.put(key, new JsonObject());
            }
            data.getJsonObject(key)
                .put(objectPropertyReferenceEncodablePair.getLeft().getPropertyIdentifier().toString(),
                     objectPropertyReferenceEncodablePair.getRight().toString());
        });
        return data;
    }

    public static JsonObject objectProperties(Map<PropertyIdentifier, Encodable> propValuesFinal) {
        JsonObject data = new JsonObject();
        propValuesFinal.forEach((propertyIdentifier, encodable) -> data.put(propertyIdentifier.toString(),
                                                                            encodableToPrimitive(encodable)));
        return data;
    }

    public static JsonObject CovNotification(ObjectIdentifier initiatingDeviceIdentifier,
                                             ObjectIdentifier monitoredObjectIdentifier,
                                             SequenceOf<PropertyValue> listOfValues) {
        JsonObject data = new JsonObject();
        try {
            listOfValues.forEach(propertyValue -> {
                if (propertyValue.getPropertyIdentifier() == PropertyIdentifier.presentValue) {
                    data.put("value", encodableToPrimitive(propertyValue.getValue()));
                    data.put("priority", encodableToPrimitive(propertyValue.getPriority()));
                }
            });
        } catch (Exception e) {
            return null;
        }
        if (data.isEmpty()) {
            return null;
        }
        return data;
    }

    public static String pointFormatBACnet(ObjectIdentifier oid) {
        return oid.getObjectType().toString() + ":" + oid.getInstanceNumber();
    }

    public static Object encodableToPrimitive(Encodable val) {
        if (val instanceof Real) {
            return ((Real) val).floatValue();
        } else if (val instanceof UnsignedInteger) {
            return ((UnsignedInteger) val).intValue();
        } else if (val instanceof BinaryPV) {
            return ((BinaryPV) val).intValue();
        } else if (val instanceof Null) {
            return "null";
        } else {
            return val.toString();
        }
    }

    public static String pointIDNubeToBACnet(String id) {
        String pointPrefix = id.substring(0, id.length() - 1);
        int inst = Integer.parseInt(id.substring(id.length() - 1));
        switch (pointPrefix) {
            case "UI":
                return ObjectType.analogInput.toString() + ":" + inst;
            case "UO":
                return ObjectType.analogOutput.toString() + ":" + inst;
            case "DI":
                return ObjectType.binaryInput.toString() + ":" + inst;
            case "DO": {
                return ObjectType.binaryOutput.toString() + ":" + inst;
            }
            case "R": {
                inst += 10;
                return ObjectType.binaryOutput.toString() + ":" + inst;
            }
            default: {
                return null;
            }
        }
    }

    public static String pointIDBACnetToNube(ObjectIdentifier o) {
        switch (o.getObjectType().toString()) {
            case "analog-input":
                return "UI" + o.getInstanceNumber();
            case "analog-output":
                return "UO" + o.getInstanceNumber();
            case "binary-input":
                return "DI" + o.getInstanceNumber();
            case "binary-output":
                if (o.getInstanceNumber() > 10) {
                    return "R" + (o.getInstanceNumber() - 10);
                } else {
                    return "DO" + o.getInstanceNumber();
                }
            default:
                return "";
        }
    }

}
