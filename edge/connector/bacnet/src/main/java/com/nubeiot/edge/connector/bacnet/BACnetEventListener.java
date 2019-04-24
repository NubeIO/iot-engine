package com.nubeiot.edge.connector.bacnet;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
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
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class BACnetEventListener extends DeviceEventAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
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
        logger.info(
            "COV Notification: " + monitoredObjectIdentifier.toString() + " from " + initiatingDeviceIdentifier);

        JsonObject json = BACnetDataConversions.CovNotification(initiatingDeviceIdentifier, monitoredObjectIdentifier,
                                                                listOfValues);
        if (json == null) {
            logger.warn("Invalid COV Notification from {} for {}", initiatingDeviceIdentifier,
                        monitoredObjectIdentifier);
        } else {
            EventMessage message = EventMessage.initial(EventAction.UPDATE, json);
            eventController.fire(POINTS_API, EventPattern.POINT_2_POINT, message);
        }
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
        //TODO: need to support more propertyId's??
        if (!req.getPropertyIdentifier().equals(PropertyIdentifier.presentValue)) {
            System.out.println("\n\nBAD PROP ID: " + req.getPropertyIdentifier().toString());
            return;
        }
        ObjectIdentifier oid = req.getObjectIdentifier();
        String id;
        Object val;
        try {
            id = BACnetDataConversions.pointIDBACnetToNube(oid);
            val = BACnetDataConversions.encodableToPrimitive(req.getPropertyValue());
        } catch (Exception e) {
            logger.warn("External BACnet write request error", e);
            return;
        }
        logger.info("REQUEST RECIEVED FOR POINT " + id + " value " + val + " @ " + req.getPriority().intValue());

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
