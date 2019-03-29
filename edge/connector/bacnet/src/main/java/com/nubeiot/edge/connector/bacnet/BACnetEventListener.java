package com.nubeiot.edge.connector.bacnet;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.connector.bacnet.Util.BACnetDataConversions;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.service.Service;
import com.serotonin.bacnet4j.service.confirmed.CreateObjectRequest;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class BACnetEventListener extends DeviceEventAdapter {

    private final String POINTS_API = "nubeio.edge.connector.pointsapi";
    private Vertx vertx;
    private EventController eventController;

    BACnetEventListener(Vertx vertx, EventController eventController) {
        this.vertx = vertx;
        this.eventController = eventController;
    }

    @Override
    public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier,
                                        ObjectIdentifier initiatingDeviceIdentifier,
                                        ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining,
                                        SequenceOf<PropertyValue> listOfValues) {
        String address = "edge.connector.bacnet.cov." + subscriberProcessIdentifier + "." + initiatingDeviceIdentifier +
                         "." + monitoredObjectIdentifier;
        JsonObject json = BACnetDataConversions.CovNotification(subscriberProcessIdentifier, monitoredObjectIdentifier,
                                                                listOfValues);
        //TODO: Check address, pattern and action
        EventMessage message = EventMessage.initial(EventAction.UPDATE, json);
        eventController.fire(POINTS_API + "points." + monitoredObjectIdentifier, EventPattern.POINT_2_POINT, message);
    }

    @Override
    public void requestReceived(Address from, Service service) {
        super.requestReceived(from, service);

        if (service instanceof CreateObjectRequest) {
            handleCreateObjectRequest((CreateObjectRequest) service);
        }
        if (service instanceof WritePropertyRequest) {
            handleWriteRequest((WritePropertyRequest) service);
        }
    }

    private void handleCreateObjectRequest(CreateObjectRequest req) {
        //TODO: send request to bonescript api for createObjectRequests
        //  resolve why this is a private method
        //  req.getListOfInitialValues();
    }

    private void handleWriteRequest(WritePropertyRequest req) {
        System.out.println(req.toString());
        //TODO: need to support more propertyId's??
        if (!req.getPropertyIdentifier().equals(PropertyIdentifier.presentValue)) {
            System.out.println("\n\nBAD PROP ID: " + req.getPropertyIdentifier().toString());
            return;
        }
        ObjectIdentifier oid = req.getObjectIdentifier();
        String id = "";
        switch (oid.getObjectType().toString()) {
            case "analog-input":
                id = "UI";
                break;
            case "analog-output":
                id = "UO";
                break;
            case "binary-input":
                id = "DI";
                break;
            case "binary-output":
                id = "DO";
                break;
            default:
                id = "";
                break;
        }
        if (id.equals("")) {
            System.err.println("ERROR GETING WRITE REQUEST ID FOR: " + oid + " @ " + req.getPriority());
            return;
        }
        int i = oid.getInstanceNumber();
        if (i >= 10) {
            i -= 10;
            id = "R";
        }
        id += Integer.toString(i);
        Object val;
        if (req.getPropertyValue() instanceof BinaryPV) {
            BinaryPV v = (BinaryPV) req.getPropertyValue();
            val = new Integer(v.intValue());
        } else if (req.getPropertyValue() instanceof Real) {
            Real v = (Real) req.getPropertyValue();
            val = new Float(v.floatValue());
        } else if (req.getPropertyValue() instanceof UnsignedInteger) {
            UnsignedInteger v = (UnsignedInteger) req.getPropertyValue();
            val = new Integer(v.intValue());
        } else if (req.getPropertyValue() instanceof CharacterString) {
            CharacterString s = (CharacterString) req.getPropertyValue();
            val = s.toString();
        } else if (req.getPropertyValue() instanceof Null) {
            Null n = (Null) req.getPropertyValue();
            val = "null";
        } else {
            val = new Integer(0);
        }

        System.out.println("REQUEST RECIEVED FOR POINT " + id + " value " + val + " @ " + req.getPriority().intValue());
        JsonObject reqBody = new JsonObject().put("value", val).put("priority", req.getPriority().intValue());

        vertx.createHttpClient().put(4000, "localhost", "/points/" + id + "/value", response -> {
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                response.bodyHandler(body -> {
                    System.err.println(body.toJsonObject());
                });
            }
        }).putHeader("content-type", "application/json").end(reqBody.toString());
    }

}
