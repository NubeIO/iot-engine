package com.nubeiot.edge.connector.bacnet.converter;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import io.vertx.core.json.JsonObject;

import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyValues;

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

    public static JsonObject deviceObjectList(List<Pair<ObjectPropertyReference, Encodable>> list) {
        JsonObject data = new JsonObject();
        list.forEach(objectPropertyReferenceEncodablePair -> {
            String key = pointFormatBACnet(objectPropertyReferenceEncodablePair.getLeft().getObjectIdentifier());

            if (objectPropertyReferenceEncodablePair.getRight() instanceof ErrorClassAndCode) {
                return;
            }
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

    //probably won't be used anymore
    //    public static EdgeWriteRequest CovNotification(ObjectIdentifier initiatingDeviceIdentifier,
    //                                                   ObjectIdentifier monitoredObjectIdentifier,
    //                                                   SequenceOf<PropertyValue> listOfValues) {
    //        EdgeWriteRequest req;
    //        try {
    //            listOfValues.forEach(propertyValue -> {
    //                if (propertyValue.getPropertyIdentifier() == PropertyIdentifier.presentValue) {
    //                    Object value = encodableToPrimitive(propertyValue.getValue());
    //                    int priority = propertyValue.getPriority().intValue();
    //                    req = new EdgeWriteRequest()
    //                }
    //            });
    //        } catch (Exception e) {
    //            return null;
    //        }
    //        return req;
    //    }

    public static JsonObject readMultipleToJson(PropertyValues values) {
        JsonObject json = new JsonObject();
        values.forEach(objectPropertyReference -> {
            try {
                json.put(pointFormatBACnet(objectPropertyReference.getObjectIdentifier()),
                         encodableToPrimitive(values.get(objectPropertyReference)));
            } catch (Exception e) {
            }
        });
        return json;
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

    public static boolean isPrimitiveNull(Object obj) {
        if (obj instanceof String) {
            String str = (String) obj;
            return str.isEmpty() || str.equalsIgnoreCase("null");
        }
        return false;
    }

    public static BinaryPV primitiveToBinary(Object obj) {
        if (obj instanceof Integer || obj instanceof Long) {
            return BinaryPV.forId((Integer) obj);
        } else if (obj instanceof Float) {
            return BinaryPV.forId(((Float) obj).intValue());
        } else if (obj instanceof Double) {
            return BinaryPV.forId(((Double) obj).intValue());
        } else if (obj instanceof Boolean) {
            if ((Boolean) obj) {
                return BinaryPV.active;
            } else {
                return BinaryPV.inactive;
            }
        } else if (obj instanceof String) {
            String str = (String) obj;
            if (str.equalsIgnoreCase("true") || str.equals("1") || str.equalsIgnoreCase("on")) {
                return BinaryPV.active;
            } else {
                return BinaryPV.inactive;
            }
        }
        return BinaryPV.inactive;
    }

    public static Real primitiveToReal(Object obj) throws BACnetException {
        if (obj == null) {
            throw new BACnetException("Null value");
        }
        String str = obj.toString();
        if (str == null || str.equalsIgnoreCase("null")) {
            return new Real(0f);
        } else {
            try {
                return new Real(Float.parseFloat(str));
            } catch (NumberFormatException e) {
                throw new BACnetException("Invalid primitive for Real");
            }
        }
    }

    public static String pointFormatBACnet(ObjectIdentifier oid) {
        return oid.getObjectType().toString() + ":" + oid.getInstanceNumber();
    }

    public static String pointIDNubeToBACnet(String id) throws Exception {
        //TODO: problem with Relays and having more than 10 Binary Outputs... kill me
        String pointPrefix = null;
        int inst = 0;
        if (id.startsWith("UI") || id.startsWith("UO") || id.startsWith("DI") || id.startsWith("DO")) {
            pointPrefix = id.substring(0, 2);
        } else if (id.startsWith("R")) {
            pointPrefix = id.substring(0, 1);
        } else {
            throw new BACnetException("Invalid Nube point Id");
        }

        try {
            inst = Integer.parseInt(id.substring(pointPrefix.length()));
        } catch (Exception e) {
            throw new BACnetException("Invalid Nube point Id");
        }
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
                throw new BACnetException("Invalid Nube point Id");
            }
        }
    }

    public static String pointIDBACnetToNube(ObjectIdentifier o) throws BACnetException {
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
                throw new BACnetException("Unsupported Nube object type");
        }
    }

    public static ObjectIdentifier getObjectIdentifierFromNube(String id) throws Exception {
        return getObjectIdentifier(pointIDNubeToBACnet(id));
    }

    public static ObjectIdentifier getObjectIdentifier(String idString) throws BACnetRuntimeException {
        String[] arr = idString.split(":");
        if (arr.length != 2) {
            throw new BACnetRuntimeException("Illegal Object Identifier");
        }
        ObjectType type = ObjectType.forName(arr[0]);
        int objNum = Integer.parseInt(arr[1]);
        return new ObjectIdentifier(type, objNum);
    }

    public static PropertyValue nubeStringToPropertyValue(String str, Object val) throws Exception {
        if (str.equals("name")) {
            return new PropertyValue(PropertyIdentifier.objectName, new CharacterString(val.toString()));
        }
        if (str.equals("value")) {
            return new PropertyValue(PropertyIdentifier.presentValue, new Real((Float) val));
        }
        //TODO: support more properties (i.e historySettings, etc...)
        throw new BACnetException("Unsupported property: " + str);
    }

}
