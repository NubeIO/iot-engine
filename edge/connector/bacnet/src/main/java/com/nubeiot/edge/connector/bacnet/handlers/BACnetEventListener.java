package com.nubeiot.edge.connector.bacnet.handlers;

import java.util.Map;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.edge.connector.bacnet.BACnetInstance;
import com.nubeiot.edge.connector.bacnet.converter.BACnetDataConversions;
import com.nubeiot.edge.connector.bacnet.converter.LocalPointObjectUtils;
import com.nubeiot.edge.connector.bacnet.listener.WhoIsListener;
import com.nubeiot.edge.connector.bacnet.objectModels.EdgeWriteRequest;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.service.Service;
import com.serotonin.bacnet4j.service.confirmed.CreateObjectRequest;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PriorityValue;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

/**
 * @see WhoIsListener
 * @deprecated split to small part for single responsibility
 */
@Deprecated
public class BACnetEventListener extends DeviceEventAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private BACnetInstance bacnetInstance;
    private LocalDevice localDevice;
    private Map<String, BACnetInstance> bacnetInstances;
    private Vertx vertx;

    public BACnetEventListener(Vertx vertx, BACnetInstance bacnetInstance, LocalDevice localDevice,
                               Map<String, BACnetInstance> bacnetInstances) {
        this.bacnetInstance = bacnetInstance;
        this.localDevice = localDevice;
        this.bacnetInstances = bacnetInstances;
        this.vertx = vertx;
    }

    @Override
    public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier, ObjectIdentifier initiatingDeviceIdentifier,
                                        ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining,
                                        SequenceOf<PropertyValue> listOfValues) {
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
        logger.info(
            "Write request received for point " + req.getObjectIdentifier() + " value " + req.getPropertyValue() +
            " @ " + req.getPriority().intValue());

        if (!req.getPropertyIdentifier().equals(PropertyIdentifier.presentValue)) {
            logger.warn("Unsupported external write request for property Id: " + req.getPropertyIdentifier().toString());
            return;
        }

        ObjectIdentifier oid = req.getObjectIdentifier();
        if (localDevice.getObject(oid) == null) {
            logger.error("Error in write request for {}. Object does not exist.", oid.toString());
            return;
        }

        String id;
        Object val;
        int priority = 16;
        try {
            id = bacnetInstance.getLocalObjectNubeId(oid);
            if (id == null) {
                throw new BACnetException();
            }
            val = BACnetDataConversions.encodableToPrimitive(req.getPropertyValue());
            priority = req.getPriority() != null ? req.getPriority().intValue() : 16;
        } catch (Exception e) {
            logger.warn("External BACnet write request error ", e);
            return;
        }
        sendWriteRequest(new EdgeWriteRequest(id, val, priority), localDevice.getObject(oid));
    }

    private void sendWriteRequest(EdgeWriteRequest writeRequest, BACnetObject point) {

        try {
            Encodable oldVal = ((PriorityValue) point.readProperty(PropertyIdentifier.priorityArray, new UnsignedInteger(
                writeRequest.getPriority()))).getConstructedValue();

            //TODO: write point framework problem...
            // the framework will write to the BACnetObject straight after this method and if not connected to the
            // edge-api, the http is too fast to allow a revert unless called with setTimer / executeBlocking or
            // something like that

            //            localController.executeHttpService(r -> r.getName().equals("edge-api"), "/edge-api/points",
            //            HttpMethod.GET,
            //                                               RequestData.builder().build()).subscribe(responseData -> {
            //                logger.info("Successfully wrote to point {}", writeRequest.getId());
            writeToOtherNetworks(writeRequest);
            //            }, error -> {
            //                logger.error("Failed writing point to database", error);
            //                revertWriteRequest(point, oldVal, writeRequest.getPriority());
            //            });
        } catch (Exception e) {
            logger.error("error writing to point ", e);
        }
    }

    private void writeToOtherNetworks(EdgeWriteRequest writeRequest) {
        bacnetInstances.forEach((s, baCnetInstance) -> {
            try {
                baCnetInstance.writeLocalObject(writeRequest);
            } catch (Exception e) {
                logger.error("Failed writing point update from external BACnet request to network {}", e, s);
            }
        });
    }

    private void revertWriteRequest(BACnetObject point, Encodable oldVal, int priority) {
        vertx.setTimer(500, f -> {
            try {
                LocalPointObjectUtils.writeToLocalOutput(point, oldVal, new UnsignedInteger(priority));
            } catch (Exception e) {
                logger.error("Cannot revert point write request on failure for point {}", e, point.getId());
            }
        });
    }

}
