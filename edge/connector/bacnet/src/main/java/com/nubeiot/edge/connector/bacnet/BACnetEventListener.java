package com.nubeiot.edge.connector.bacnet;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventController;
import com.nubeiot.edge.connector.bacnet.objectModels.EdgeWriteRequest;
import com.nubeiot.edge.connector.bacnet.utils.BACnetDataConversions;
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
    private final String POINTS_API;
    private EventController eventController;

    BACnetEventListener(EventController eventController, BACnetConfig config) {
        this.eventController = eventController;
        this.POINTS_API = config.getLocalPointsApiAddress();
    }

    @Override
    public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier,
                                        ObjectIdentifier initiatingDeviceIdentifier,
                                        ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining,
                                        SequenceOf<PropertyValue> listOfValues) {
        logger.warn("COV notification unsupported", new UnsupportedOperationException("COV notification unsupported"));
        //        logger.info(
        //            "COV Notification: " + monitoredObjectIdentifier.toString() + " from " +
        //            initiatingDeviceIdentifier);
        //
        //        JsonObject json = BACnetDataConversions.CovNotification(initiatingDeviceIdentifier,
        //        monitoredObjectIdentifier,
        //                                                                listOfValues);
        //        if (json == null) {
        //            logger.warn("Invalid COV Notification from {} for {}", initiatingDeviceIdentifier,
        //                        monitoredObjectIdentifier);
        //        } else {
        //            EventMessage message = EventMessage.initial(EventAction.UPDATE, json);
        //            eventController.fire(POINTS_API, EventPattern.POINT_2_POINT, message);
        //        }
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
        logger.warn("Create new point request unsupported",
                    new UnsupportedOperationException("Create new point request unsupported"));
    }

    private void handleWriteRequest(WritePropertyRequest req) {
        if (!req.getPropertyIdentifier().equals(PropertyIdentifier.presentValue)) {
            logger.warn("Unsupported Property Id: " + req.getPropertyIdentifier().toString());
            return;
        }
        ObjectIdentifier oid = req.getObjectIdentifier();
        String id;
        Object val;
        try {
            id = BACnetDataConversions.pointIDBACnetToNube(oid);
            val = BACnetDataConversions.encodableToPrimitive(req.getPropertyValue());
        } catch (Exception e) {
            logger.warn("External BACnet write request error ", e);
            return;
        }

        EdgeWriteRequest edgeReq = new EdgeWriteRequest(id, val, req.getPriority().intValue());
        logger.info("REQUEST RECIEVED FOR POINT " + id + " value " + val + " @ " + req.getPriority().intValue());

        //TODO: send write request to edge-api
//        EventMessage message = EventMessage.initial(EventAction., edgeReq.toJson());
//        eventController.fire(POINTS_API+"."+id+".value", EventPattern.POINT_2_POINT, message);
    }

}
