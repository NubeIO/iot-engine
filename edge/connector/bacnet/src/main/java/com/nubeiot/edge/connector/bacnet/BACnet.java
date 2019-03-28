package com.nubeiot.edge.connector.bacnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.connector.bacnet.Util.BACnetDataConversions;
import com.nubeiot.edge.connector.bacnet.Util.LocalPointObjectUtils;
import com.nubeiot.edge.connector.bacnet.Util.NetworkUtils;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.ObjectPropertyTypeDefinition;
import com.serotonin.bacnet4j.service.Service;
import com.serotonin.bacnet4j.service.confirmed.CreateObjectRequest;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVRequest;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ErrorClass;
import com.serotonin.bacnet4j.type.enumerated.ErrorCode;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.DiscoveryUtils;
import com.serotonin.bacnet4j.util.RequestUtils;

/*
 * Main BACnet functionality
 *  - initialisation
 *  - local object adding
 *  - remote device request utils
 */
public class BACnet {

    //TODO: be able to search for single remote device? doubts cuz dat not how de bacnetty work brethren
    //TODO: implement MTSP support
    //TODO execute blocking will throw warnings after 10 seconds. is it possible for this to happen on local network
    // requests?

    private Vertx vertx;
    private EventController eventController;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String POINTS_API = "nubeio.edge.connector.pointsapi";

    private LocalDevice localDevice;
    private HashMap<RemoteDevice, Vector<ObjectIdentifier>> points;

    private BACnet(String name, int id, EventController eventController, Vertx vertx, LocalDevice localDevice) {
        this.eventController = eventController;
        this.vertx = vertx;
        this.points = new HashMap<>();
        this.localDevice = localDevice;
    }

    private BACnet(String name, int id, Future<Void> future, EventController eventController, Vertx vertx) {
        this.eventController = eventController;
        this.vertx = vertx;
        this.points = new HashMap<>();

        String broadcastAddress = null;
        int networkPrefixLength = 24;
        try {
            broadcastAddress = NetworkUtils.getBroadcastAddress();
            networkPrefixLength = NetworkUtils.getNetworkPrefixLength();
        } catch (Exception ex) {
            ex.printStackTrace();
            future.fail(ex);
            return;
        }

        IpNetwork network = new IpNetworkBuilder().withBroadcast(broadcastAddress, networkPrefixLength).build();
        Transport transport = new DefaultTransport(network);
        localDevice = new LocalDevice(id, transport);
        localDevice.writePropertyInternal(PropertyIdentifier.modelName, new CharacterString(name));
        try {
            localDevice.initialize();
            localDevice.startRemoteDeviceDiscovery();
            //TODO: should this be stopped? does it stop handling IAM req if stopped?

            if (!localDevice.isInitialized()) {
                throw new BACnetServiceException(ErrorClass.device, ErrorCode.internalError);
            }
        } catch (Exception ex) {
            logger.error("\n\nBACNET FAILURE\n\n", ex);
            future.fail(ex);
            return;
        }
    }

    public static BACnet createBACnet(String name, int id, Future<Void> future, EventController eventController,
                                      Vertx vertx) {
        return new BACnet(name, id, future, eventController, vertx);
    }

    public static BACnet createBACnet(String name, int id, EventController eventController, Vertx vertx,
                                      LocalDevice localDevice) {
        return new BACnet(name, id, eventController, vertx, localDevice);
    }

    public void terminate() {
        localDevice.terminate();
    }

    public void BEGIN_TEST() {
        //        logger.info("\n\n\nBEGINING TEST\n\n");
        //        logger.info(getRemoteDevices());
        //        RemoteDevice rd = localDevice.getCachedRemoteDevice(2769127);
        //        if(rd == null) {System.out.println("NO DEVICE");return;}
        //        getRemoteDeviceObjectList(rd).subscribe(data -> {
        //            logger.info(data);
        //        });
    }

    //TODO: test new RxJava
    public void initialiseLocalObjectsFromJson(JsonObject json) {
        Observable.fromIterable(json.getMap().entrySet()).subscribe(entry -> {
            JsonObject pointJson = JsonObject.mapFrom(entry.getValue());
            LocalPointObjectUtils.createLocalObject(pointJson, entry.getKey(), localDevice);
        });
    }

    public void addLocalObjectFromJson(JsonObject json) {
        //TODO: get id from json
        //        LocalPointObjectUtils.createLocalObject(json, id, localDevice);
    }

    //REMOTE DEVICE FUNCTIONS

    public Single<JsonObject> getRemoteDevices() {
        if (localDevice.getRemoteDeviceCache().getEntities().size() == 0) {
            return Single.just(new JsonObject());
        } else {
            JsonObject data = new JsonObject();
            return Observable.fromIterable(localDevice.getRemoteDeviceCache().getEntities())
                             .map(remoteDevice -> BACnetDataConversions.deviceMinimal(remoteDevice))
                             .flatMapSingle(entries -> Single.just(entries))
                             .collect(() -> data, (d, deviceJson) -> d.put(
                                 Integer.toString(deviceJson.getInteger("instanceNumber")), deviceJson));
        }
    }

    public Single<JsonObject> getRemoteDeviceExtendedInfo(int instanceNumber) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        } else {
            return getRemoteDeviceExtendedInfo(remoteDevice);
        }
    }

    public Single<JsonObject> getRemoteDeviceExtendedInfo(RemoteDevice remoteDevice) {
        return callAsyncBlocking(() -> {
            DiscoveryUtils.getExtendedDeviceInformation(localDevice, remoteDevice);
            return BACnetDataConversions.deviceExtended(remoteDevice);
        });
    }

    public Single<JsonObject> getRemoteDeviceObjectList(int instanceNumber) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        } else {
            return getRemoteDeviceObjectList(remoteDevice);
        }
    }

    public Single<JsonObject> getRemoteDeviceObjectList(RemoteDevice remoteDevice) {
        return callAsyncBlocking(() -> {
            SequenceOf<ObjectIdentifier> list = RequestUtils.getObjectList(localDevice, remoteDevice);
            remoteDevice.setDeviceProperty(PropertyIdentifier.objectList, list);
            return BACnetDataConversions.deviceObjectList(list);
        });
    }

    public Single<JsonObject> getRemoteObjectProperties(int instanceNumber, String objectID) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        }
        try {
            return getRemoteObjectProperties(remoteDevice, getObjectIdentifier(objectID));
        } catch (BACnetRuntimeException e) {
            return Single.error(e);
        }
    }

    public Single<JsonObject> getRemoteObjectProperties(RemoteDevice d, ObjectIdentifier oid) {
        return callAsyncBlocking(() -> {
            List<ObjectPropertyTypeDefinition> propsDefs = ObjectProperties.getObjectPropertyTypeDefinitions(
                oid.getObjectType());
            ArrayList<PropertyIdentifier> props = new ArrayList<PropertyIdentifier>(propsDefs.size());
            Map<PropertyIdentifier, Encodable> propValuesFinal = new HashMap<>();

            for (ObjectPropertyTypeDefinition prop : propsDefs) {
                props.add(prop.getPropertyTypeDefinition().getPropertyIdentifier());
            }

            Map<PropertyIdentifier, Encodable> propValues = RequestUtils.getProperties(localDevice, d, oid, null,
                                                                                       props.toArray(
                                                                                           new PropertyIdentifier[0]));
            propValues.forEach((pid, val) -> {
                if (val instanceof ErrorClassAndCode) {
                    return;
                }
                propValuesFinal.put(pid, val);
                d.setObjectProperty(oid, pid, val);
            });

            return BACnetDataConversions.objectProperties(propValuesFinal);
        });
    }

    public Encodable getPropery(RemoteDevice rd, ObjectIdentifier oid, PropertyIdentifier pid) {
        try {
            if (oid == null) {
                return RequestUtils.getProperty(localDevice, rd, pid);
            } else {
                return RequestUtils.getProperty(localDevice, rd, oid, pid);
            }
        } catch (BACnetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    //TODO: check if confirmed or unconfirmed request - need async or not
    public void remoteObjectSubscribeCOV(RemoteDevice rd, ObjectIdentifier obj) {
        boolean correctID = false;
        while (!correctID) {
            try {
                int subID = (int) Math.floor((Math.random() * 100) + 1);
                UnsignedInteger subProcessID = new UnsignedInteger(subID);
                UnsignedInteger lifetime = new UnsignedInteger(0);
                SubscribeCOVRequest request = new SubscribeCOVRequest(subProcessID, obj, Boolean.TRUE, lifetime);
                localDevice.send(rd, request);
                //TODO: handle property not subscribable -> setup interval polling
                correctID = true;
            } catch (BACnetRuntimeException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Single<JsonObject> writeAtPriority(int instanceNumber, String obj, Encodable val, int priority) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetException("Remote device not found"));
        }
        try {
            ObjectIdentifier oid = getObjectIdentifier(obj);
            writeAtPriority(remoteDevice, oid, val, priority);
        } catch (BACnetRuntimeException e) {
            return Single.error(e);
        }
        return Single.just(new JsonObject());
    }

    //TODO: check if throws error or just nothing when not writable
    public void writeAtPriority(RemoteDevice rd, ObjectIdentifier obj, Encodable val, int priority) {
        WritePropertyRequest req = new WritePropertyRequest(obj, PropertyIdentifier.presentValue, null, val,
                                                            new UnsignedInteger(priority));
        localDevice.send(rd, req);
    }

    public ObjectIdentifier getObjectIdentifier(String idString) throws BACnetRuntimeException {
        String[] arr = idString.split(":");
        if (arr.length <= 1) {
            throw new BACnetRuntimeException("Illegal Object Identifier");
        }
        ObjectType type = ObjectType.forName(arr[0]);
        int objNum = Integer.parseInt(arr[1]);
        return new ObjectIdentifier(type, objNum);
    }

    public Single<JsonObject> callAsyncBlocking(Callable callable) {
        return Single.create(source -> {
            vertx.executeBlocking(future -> {
                try {
                    JsonObject result = (JsonObject) callable.call();
                    future.complete(result);
                    source.onSuccess(result);
                } catch (Exception e) {
                    future.fail(e);
                    source.onError(e);
                }
            }, res -> {});
        });
    }

    private class listener extends DeviceEventAdapter {

        @Override
        public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier,
                                            ObjectIdentifier initiatingDeviceIdentifier,
                                            ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining,
                                            SequenceOf<PropertyValue> listOfValues) {
            String address = "edge.connector.bacnet.cov." + subscriberProcessIdentifier + "." +
                             initiatingDeviceIdentifier + "." + monitoredObjectIdentifier;
            JsonObject json = BACnetDataConversions.CovNotification(subscriberProcessIdentifier,
                                                                    monitoredObjectIdentifier, listOfValues);
            //TODO: Check address, pattern and action
            EventMessage message = EventMessage.initial(EventAction.UPDATE, json);
            eventController.fire(POINTS_API + "points." + monitoredObjectIdentifier, EventPattern.POINT_2_POINT,
                                 message);
        }

        @Override
        public void requestReceived(Address from, Service service) {
            super.requestReceived(from, service);
            if (service instanceof CreateObjectRequest) {
                CreateObjectRequest req = (CreateObjectRequest) service;
                //TODO: send request to bonescript api for createObjectRequests
                //  resolve why this is a private method
                //  req.getListOfInitialValues();
            }
        }

    }

}
