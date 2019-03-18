package com.nubeiot.edge.connector.bacnet.Util;

import java.util.Map;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
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
                 remoteDevice.getCharacterStringProperty(PropertyIdentifier.maxApduLengthAccepted));
        data.put("segmentationSupported",
                 remoteDevice.getCharacterStringProperty(PropertyIdentifier.segmentationSupported));
        data.put("vendorId", remoteDevice.getCharacterStringProperty(PropertyIdentifier.vendorIdentifier));
        data.put("vendorName", remoteDevice.getCharacterStringProperty(PropertyIdentifier.vendorName));
        data.put("servicesSupported",
                 remoteDevice.getCharacterStringProperty(PropertyIdentifier.protocolServicesSupported));
        return data;
    }

    public static JsonObject deviceObjectList(SequenceOf<ObjectIdentifier> list) {
        JsonArray data = new JsonArray();
        list.forEach(item -> {
            data.add(item.toString());
        });
        return new JsonObject().put("objects", list);
    }

    public static JsonObject objectProperties(Map<PropertyIdentifier, Encodable> propValuesFinal) {
        JsonObject data = new JsonObject();
        propValuesFinal.forEach((propertyIdentifier, encodable) -> data.put(propertyIdentifier.toString(), encodable));
        return data;
    }

    public static JsonObject CovNotification(UnsignedInteger subscriberProcessIdentifier,
                                             ObjectIdentifier monitoredObjectIdentifier,
                                             SequenceOf<PropertyValue> listOfValues) {
        JsonObject data = new JsonObject();
        data.put("subscriberProcessID", subscriberProcessIdentifier);
        data.put("objectID", monitoredObjectIdentifier);
        data.put("value", listOfValues);
        return data;
    }

}
