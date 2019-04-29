package com.nubeiot.edge.connector.bacnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.edge.connector.bacnet.BACnetConfig.BACnetNetworkConfig;
import com.nubeiot.edge.connector.bacnet.utils.BACnetDataConversions;
import com.nubeiot.edge.connector.bacnet.utils.LocalPointObjectUtils;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.ObjectPropertyTypeDefinition;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVRequest;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
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

import lombok.NonNull;

/*
 * Main BACnetInstance functionality
 *  - initialisation
 *  - local object adding
 *  - remote device request utils
 */

//TODO: CHECK WHAT HAPPENS WHEN OBJECT DOES'T EXIST ON REMOTE DEVICE


public class BACnetInstance {

    //TODO execute blocking will throw warnings after 10 seconds. is it possible for this to happen on local network
    // requests?

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //    private Map<RemoteDevice, ArrayList<ObjectIdentifier>> cachedRemotePoints = new HashMap<>();
    private final Map<String, Integer> remoteSubscriptions = new HashMap<>();
    private Vertx vertx;
    private LocalDevice localDevice;
    private BACnetConfig config;
    private PollingTimers pollingTimers;

    private BACnetInstance(LocalDevice localDevice, Vertx vertx, PollingTimers timers) {
        this.vertx = vertx;
        this.localDevice = localDevice;
        this.pollingTimers = timers;
        this.config = new BACnetConfig();
    }

    private BACnetInstance(LocalDevice localDevice, Vertx vertx, PollingTimers timers,
                           Map<String, Integer> remoteSubscriptions) {
        this(localDevice, vertx, timers);
        this.remoteSubscriptions.putAll(remoteSubscriptions);
    }

    private BACnetInstance(BACnetConfig config, BACnetNetworkConfig netConfig, EventController eventController,
                           PollingTimers pollingTimers, Vertx vertx) {
        this.vertx = vertx;
        this.config = config;
        this.pollingTimers = pollingTimers;
        Transport transport = TransportProvider.byConfig(netConfig).get();

        //TODO: set name and model name
        localDevice = new LocalDevice(config.getDeviceId(), transport);
        localDevice.writePropertyInternal(PropertyIdentifier.modelName, new CharacterString(config.getDeviceName()));
        try {
            localDevice.initialize();
            localDevice.getEventHandler().addListener(new BACnetEventListener(vertx, eventController));
            localDevice.startRemoteDeviceDiscovery();
            //TODO: should this be stopped? does it stop handling IAM req if stopped?
            if (!localDevice.isInitialized()) {
                throw new NubeException(new BACnetServiceException(ErrorClass.device, ErrorCode.internalError));
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new NubeException(e);
        }
    }

    public static BACnetInstance createBACnet(LocalDevice localDevice, Vertx vertx, PollingTimers pollingTimers) {
        return new BACnetInstance(localDevice, vertx, pollingTimers);
    }

    public static BACnetInstance createBACnet(LocalDevice localDevice, Vertx vertx, PollingTimers pollingTimers,
                                              Map<String, Integer> remoteSubscriptions) {
        return new BACnetInstance(localDevice, vertx, pollingTimers, remoteSubscriptions);
    }

    public static BACnetInstance createBACnet(BACnetConfig bacnetConfig, BACnetNetworkConfig networkConfig,
                                              EventController eventController, PollingTimers pollingTimers,
                                              Vertx vertx) {
        return new BACnetInstance(bacnetConfig, networkConfig, eventController, pollingTimers, vertx);
    }

    public void terminate() {
        localDevice.terminate();
    }

    public void initialiseLocalObjectsFromJson(JsonObject json) {
        localDevice.getLocalObjects().clear();//TODO: make sure no memory leaks in BACnetObjects
        Observable.fromIterable(json.getMap().entrySet()).subscribe(entry -> {
            JsonObject pointJson = JsonObject.mapFrom(entry.getValue());
            LocalPointObjectUtils.createLocalObject(entry.getKey(), pointJson, localDevice);
        });
    }

    public void addLocalObjectFromJson(String id, JsonObject json) {
        try {
            if (localDevice.getObject(BACnetDataConversions.getObjectIdentifierFromNube(id)) == null) {
                LocalPointObjectUtils.createLocalObject(id, json, localDevice);
            } else {
                logger.warn("Local object {} already exists", id);
            }
        } catch (Exception e) {
        }
    }

    public void removeLocalObject(String id) {
        try {
            localDevice.removeObject(
                BACnetDataConversions.getObjectIdentifier(BACnetDataConversions.pointIDNubeToBACnet(id)));
        } catch (Exception e) {
            logger.warn("Invalid NubeIO Point Id to remove: {}", e, id);
        }
    }

    public void writeLocalObject(String id, JsonObject json) {
        try {
            LocalPointObjectUtils.writeLocalObject(id, json, localDevice);
        } catch (Exception e) {
            logger.warn("Failed writing to point {}", e, id);
        }
    }

    public void updateLocalObject(String id, String property, Object value) {
        try {
            LocalPointObjectUtils.updateLocalObjectProperty(localDevice, id, property, value);
        } catch (Exception e) {
            logger.warn("Error updating point {} property {}", e, id, property);
        }
    }

    //REMOTE DEVICE FUNCTIONS

    public Single<JsonObject> getRemoteDevices() {
        if (localDevice.getRemoteDeviceCache().getEntities().size() == 0) {
            return Single.just(new JsonObject());
        } else {
            JsonObject data = new JsonObject();
            return Observable.fromIterable(localDevice.getRemoteDeviceCache().getEntities())
                             .map(BACnetDataConversions::deviceMinimal)
                             .flatMapSingle(Single::just)
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

    private Single<JsonObject> getRemoteDeviceExtendedInfo(RemoteDevice remoteDevice) {
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

    //TODO: refactor
    private Single<JsonObject> getRemoteDeviceObjectList(RemoteDevice remoteDevice) {
        return callAsyncBlocking(() -> {
            SequenceOf<ObjectIdentifier> list = RequestUtils.getObjectList(localDevice, remoteDevice);
            remoteDevice.setDeviceProperty(PropertyIdentifier.objectList, list);

            List<ObjectPropertyReference> refs = new ArrayList<>();
            list.forEach(objectIdentifier -> {
                if (objectIdentifier.getObjectType().toString().equals(ObjectType.device.toString())) {
                    return;
                }
                refs.add(new ObjectPropertyReference(objectIdentifier, PropertyIdentifier.objectName));

                //TODO: work out why some can't request presentValue
                //
                //                if (ObjectProperties.getObjectPropertyTypeDefinition(objectIdentifier.getObjectType(),
                //                                                                     PropertyIdentifier
                //                                                                     .presentValue) != null) {
                //                    refs.add(new ObjectPropertyReference(objectIdentifier, PropertyIdentifier
                //                    .presentValue));
                //                }
            });
            return BACnetDataConversions.deviceObjectList(
                RequestUtils.readProperties(localDevice, remoteDevice, refs, true, null));
        });
    }

    public Single<JsonObject> getRemoteObjectProperties(int instanceNumber, String objectID) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        }
        try {
            return getRemoteObjectProperties(remoteDevice, BACnetDataConversions.getObjectIdentifier(objectID));
        } catch (BACnetRuntimeException e) {
            return Single.error(e);
        }
    }

    private Single<JsonObject> getRemoteObjectProperties(RemoteDevice d, ObjectIdentifier oid) {
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

    public Single<JsonObject> remoteObjectSubscribeCOV(int instanceNumber, String objectID) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        }
        try {
            return remoteObjectSubscribeCOV(remoteDevice, BACnetDataConversions.getObjectIdentifier(objectID));
        } catch (BACnetRuntimeException e) {
            return Single.error(e);
        }
    }

    public Single<JsonObject> remoteObjectSubscribeCOV(RemoteDevice rd, ObjectIdentifier oid) {
        return callAsyncBlocking(() -> sendSubscribeCOVRequestBlocking(rd, oid));
    }

    public JsonObject sendSubscribeCOVRequestBlocking(RemoteDevice rd, ObjectIdentifier oid) throws Exception {
        boolean correctID = false;
        while (!correctID) {
            int subID = (int) Math.floor((Math.random() * 100) + 1);
            UnsignedInteger subProcessID = new UnsignedInteger(subID);
            UnsignedInteger lifetime = new UnsignedInteger(0);
            SubscribeCOVRequest request = new SubscribeCOVRequest(subProcessID, oid, Boolean.TRUE, lifetime);
            localDevice.send(rd, request).get();
            correctID = true;
            remoteSubscriptions.put(remoteObjectKey(rd, oid), subProcessID.intValue());
        }
        initRemoteObjectPolling(rd, oid);
        return new JsonObject();
    }

    public Single<JsonObject> removeRemoteObjectSubscription(int instanceNumber, String objectId) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        }
        try {
            return removeRemoteObjectSubscription(remoteDevice, BACnetDataConversions.getObjectIdentifier(objectId));
        } catch (BACnetRuntimeException e) {
            return Single.error(e);
        }
    }

    public Single<JsonObject> removeRemoteObjectSubscription(RemoteDevice remoteDevice, ObjectIdentifier oid) {
        try {
            return Single.create(source -> removeRemoteObjectSubscriptionBlocking(remoteDevice, oid));
        } catch (Exception e) {
            return Single.error(e);
        }
    }

    //TODO: CHECK LocalDevice COV_Contexts for memory overuse
    public JsonObject removeRemoteObjectSubscriptionBlocking(RemoteDevice remoteDevice, ObjectIdentifier oid)
        throws Exception {
        if (!remoteSubscriptions.containsKey(remoteObjectKey(remoteDevice, oid))) {
            throw new BACnetException("Subscription doesn't exist");
        }
        UnsignedInteger subId = new UnsignedInteger(remoteSubscriptions.get(remoteObjectKey(remoteDevice, oid)));
        SubscribeCOVRequest request = new SubscribeCOVRequest(subId, oid, null, null);
        //TODO: check don't need to confirm
        localDevice.send(remoteDevice, request);
        remoteSubscriptions.remove(remoteObjectKey(remoteDevice, oid));
        pollingTimers.removePoint(remoteDevice, oid);
        return new JsonObject();
    }

    public Single<JsonObject> initRemoteObjectPolling(int instanceNumber, String oid, long pollTime) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        }
        try {
            return initRemoteObjectPolling(remoteDevice, BACnetDataConversions.getObjectIdentifier(oid),
                                           config.getDefaultPollingTime());
        } catch (BACnetRuntimeException e) {
            return Single.error(e);
        }
    }

    public Single<JsonObject> initRemoteObjectPolling(RemoteDevice rd, ObjectIdentifier oid) {
        return initRemoteObjectPolling(rd, oid, config.getDefaultPollingTime());
    }

    public Single<JsonObject> initRemoteObjectPolling(RemoteDevice rd, ObjectIdentifier oid, long pollTime) {
        try {
            pollingTimers.addPoint(this, rd, oid, pollTime);
            return Single.just(new JsonObject());
        } catch (BACnetException e) {
            return Single.error(e);
        }
    }

    public Single<JsonObject> removeRemoteObjectPolling(RemoteDevice rd, ObjectIdentifier oid) {
        try {
            pollingTimers.removePoint(rd, oid);
            return Single.just(new JsonObject());
        } catch (BACnetException e) {
            return Single.error(e);
        }
    }

    public Single<JsonObject> writeAtPriority(int instanceNumber, String obj, Object v, int priority) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        }
        try {
            Encodable val;
            ObjectIdentifier oid = BACnetDataConversions.getObjectIdentifier(obj);
            if (oid.getObjectType().isOneOf(ObjectType.binaryOutput, ObjectType.binaryInput, ObjectType.binaryValue)) {
                val = BACnetDataConversions.primitiveToBinary(v);
            } else {
                val = BACnetDataConversions.primitiveToReal(v);
            }
            writeAtPriority(remoteDevice, oid, val, priority);
        } catch (BACnetRuntimeException | BACnetException e) {
            return Single.error(e);
        }
        return Single.just(new JsonObject());
    }

    private void writeAtPriority(RemoteDevice rd, ObjectIdentifier oid, Encodable val, int priority)
        throws BACnetException {
        RequestUtils.writeProperty(localDevice, rd, oid, PropertyIdentifier.presentValue, val, priority);
    }

    private Single<JsonObject> callAsyncBlocking(Callable callable) {
        return Single.create(source -> vertx.executeBlocking(future -> {
            try {
                JsonObject result = (JsonObject) callable.call();
                future.complete(result);
                source.onSuccess(result);
            } catch (Exception e) {
                future.fail(e);
                source.onError(e);
            }
        }, res -> {}));
    }

    private String remoteObjectKey(@NonNull RemoteDevice remoteDevice, @NonNull ObjectIdentifier oid) {
        return remoteDevice.hashCode() + "::" + oid.hashCode();
    }

}
