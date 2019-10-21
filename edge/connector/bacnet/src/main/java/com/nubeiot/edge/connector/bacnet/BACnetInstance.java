package com.nubeiot.edge.connector.bacnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.connector.bacnet.BACnetConfig.BACnetNetworkConfig;
import com.nubeiot.edge.connector.bacnet.handlers.BACnetEventListener;
import com.nubeiot.edge.connector.bacnet.objectModels.EdgePoint;
import com.nubeiot.edge.connector.bacnet.objectModels.EdgeWriteRequest;
import com.nubeiot.edge.connector.bacnet.utils.BACnetDataConversions;
import com.nubeiot.edge.connector.bacnet.utils.LocalPointObjectUtils;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.cache.RemoteEntityCachePolicy;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.AnalogValueObject;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.BinaryValueObject;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.ObjectPropertyTypeDefinition;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ErrorClass;
import com.serotonin.bacnet4j.type.enumerated.ErrorCode;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.DiscoveryUtils;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RemoteDeviceDiscoverer;
import com.serotonin.bacnet4j.util.RequestUtils;

import lombok.NonNull;

/*
 * Main BACnetInstance functionality
 *  - initialisation
 *  - local object adding
 *  - remote device request utils
 */


public class BACnetInstance {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Vertx vertx;
    private LocalDevice localDevice;
    private BACnetConfig config;
    private RemoteDeviceDiscoverer deviceDiscoverer;
    //only mapping virtual points as other points can be calculated to get ObjectIdentifier so this saves memory
    //TODO: look into moving this higher up to save memory. need to check if each instance creates the exact same point
    // and instance number every time across all BACnetInstances
    private Map<String, ObjectIdentifier> virtualPointsMap;

    private BACnetInstance(LocalDevice localDevice, Vertx vertx) {
        this.vertx = vertx;
        this.localDevice = localDevice;
        this.config = new BACnetConfig();
        virtualPointsMap = new HashMap<>();
    }

    private BACnetInstance(BACnetConfig config, BACnetNetworkConfig netConfig, EventController eventController,
                           ServiceDiscoveryController localController, Map bacnetInstances, Vertx vertx) {
        this.vertx = vertx;
        this.config = config;
        virtualPointsMap = new HashMap<>();
        Transport transport = TransportProvider.byConfig(netConfig).get();
        logger.info("BACnet instance started on network {}",
                    transport.getNetwork().getNetworkIdentifier().getIdString());

        localDevice = new LocalDevice(config.getDeviceId(), transport);
        localDevice.writePropertyInternal(PropertyIdentifier.modelName, new CharacterString(config.getModelName()));
        localDevice.writePropertyInternal(PropertyIdentifier.vendorName, new CharacterString("Nube-iO"));
        localDevice.writePropertyInternal(PropertyIdentifier.objectName, new CharacterString(config.getDeviceName()));
        try {
            localDevice.initialize();
            if (config.isAllowSlave()) {
                localDevice.getEventHandler()
                           .addListener(
                               new BACnetEventListener(config, this, localDevice, eventController, localController,
                                                       bacnetInstances, vertx));
            }

            startRemoteDiscover();
            if (!localDevice.isInitialized()) {
                throw new NubeException(new BACnetServiceException(ErrorClass.device, ErrorCode.internalError));
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new NubeException(e);
        }
    }

    public static BACnetInstance createBACnet(LocalDevice localDevice, Vertx vertx) {
        return new BACnetInstance(localDevice, vertx);
    }

    public static BACnetInstance createBACnet(BACnetConfig bacnetConfig, BACnetNetworkConfig networkConfig,
                                              EventController eventController,
                                              ServiceDiscoveryController localController, Map bacnetInstances,
                                              Vertx vertx) {
        return new BACnetInstance(bacnetConfig, networkConfig, eventController, localController, bacnetInstances,
                                  vertx);
    }

    public void terminate() {
        clearLocalObjects();
        localDevice.clearRemoteDevices();
        if (deviceDiscoverer != null) {
            deviceDiscoverer.stop();
        }
        localDevice.terminate();
    }

    public void startRemoteDiscover() {
        startRemoteDiscover(config.getDiscoveryTimeout());
    }

    public void startRemoteDiscover(long timeout) {
        if (deviceDiscoverer != null) {
            deviceDiscoverer.stop();
        }
        localDevice.clearRemoteDevices();
        deviceDiscoverer = localDevice.startRemoteDeviceDiscovery(remoteDevice -> {
            localDevice.getCachePolicies()
                       .putDevicePolicy(remoteDevice.getInstanceNumber(), RemoteEntityCachePolicy.NEVER_EXPIRE);
        });
        vertx.setTimer(timeout, s -> {
            deviceDiscoverer.stop();
        });
    }

    public void initialiseLocalObjectsFromJson(JsonObject json) {
        //TODO IN FUTURE REMOVE ALL NON LOCAL/ SAME NETWORK POINTS
        clearLocalObjects();
        Observable.fromIterable(json.getMap().entrySet()).subscribe(entry -> {
            EdgePoint point = EdgePoint.fromJson(entry.getKey(), JsonObject.mapFrom(entry.getValue()));
            addLocalObject(point);
        }, error -> logger.error("Init local points error", error));
    }

    public ObjectIdentifier getLocalObjectId(String id) throws Exception {
        ObjectIdentifier oid;
        try {
            oid = BACnetDataConversions.getObjectIdentifierFromNube(id);
            if (oid != null && localDevice.getObject(oid) != null) {
                return oid;
            }
        } catch (Exception e) {
        }

        oid = virtualPointsMap.get(id);
        if (oid != null) {
            return oid;
        }
        throw new BACnetException("Local point " + id + " doesnt exists");
    }

    public String getLocalObjectNubeId(ObjectIdentifier oid) {
        if (virtualPointsMap.containsValue(oid)) {
            for (Entry<String, ObjectIdentifier> e : virtualPointsMap.entrySet()) {
                if (e.getValue().equals(oid)) {
                    return e.getKey();
                }
            }
        }
        try {
            return BACnetDataConversions.pointIDBACnetToNube(oid);
        } catch (Exception e) {
            return null;
        }
    }

    public void clearLocalObjects() {
        for (BACnetObject obj : localDevice.getLocalObjects()) {
            try {
                if (!obj.getId().equals(localDevice.getId())) {
                    localDevice.removeObject(obj.getId());
                }
            } catch (BACnetServiceException e) {
                logger.error("Failed to remove object {}", e, obj.getId());
            }
        }
    }

    public BACnetObject addLocalObject(EdgePoint point) throws Exception {
        try {
            getLocalObjectId(point.getId());
            throw new BACnetException("Local point " + point.getId() + " already exists");
        } catch (BACnetException e) {
            BACnetObject obj = LocalPointObjectUtils.createLocalObject(point, localDevice);
            if (obj instanceof BinaryValueObject || obj instanceof AnalogValueObject) {
                virtualPointsMap.put(point.getId(), obj.getId());
            }
            return obj;
        }
    }

    public void removeLocalObject(String nubeId) throws Exception {
        ObjectIdentifier oid = getLocalObjectId(nubeId);
        localDevice.removeObject(oid);
        if (virtualPointsMap.containsKey(nubeId)) {
            virtualPointsMap.remove(nubeId);
        }
    }

    public void writeLocalObject(EdgeWriteRequest req) throws Exception {
        LocalPointObjectUtils.writeLocalObject(req, getLocalObjectId(req.getId()), localDevice);
    }

    public void updateLocalObject(String id, String property, Object value) throws Exception {
        LocalPointObjectUtils.updateLocalObjectProperty(localDevice, getLocalObjectId(id), property, value);
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

    public Single<JsonObject> getRemoteDeviceObjectList(int instanceNumber, boolean allData) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        } else {
            return getRemoteDeviceObjectList(remoteDevice, allData);
        }
    }

    private Single<JsonObject> getRemoteDeviceObjectList(RemoteDevice remoteDevice, boolean allData) {
        return callAsyncBlocking(() -> {
            SequenceOf<ObjectIdentifier> list = RequestUtils.getObjectList(localDevice, remoteDevice);
            remoteDevice.setDeviceProperty(PropertyIdentifier.objectList, list);

            List<ObjectPropertyReference> refs = new ArrayList<>();
            list.forEach(objectIdentifier -> {
                if (allData) {
                    List<ObjectPropertyTypeDefinition> propsDefs = ObjectProperties.getObjectPropertyTypeDefinitions(
                        objectIdentifier.getObjectType());

                    for (ObjectPropertyTypeDefinition prop : propsDefs) {
                        refs.add(new ObjectPropertyReference(objectIdentifier,
                                                             prop.getPropertyTypeDefinition().getPropertyIdentifier()));
                    }
                } else {
                    if (!objectIdentifier.getObjectType().equals(ObjectType.device)) {
                        refs.add(new ObjectPropertyReference(objectIdentifier, PropertyIdentifier.objectName));
                        if (ObjectProperties.getObjectPropertyTypeDefinition(objectIdentifier.getObjectType(),
                                                                             PropertyIdentifier.presentValue) != null) {
                            refs.add(new ObjectPropertyReference(objectIdentifier, PropertyIdentifier.presentValue));
                        }
                    }
                }
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
        } catch (Exception e) {
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
            if (propValuesFinal.isEmpty()) {
                throw new BACnetException("Object not found");
            }
            return BACnetDataConversions.objectProperties(propValuesFinal);
        });
    }

    public Single<JsonObject> readRemoteObjectvalue(int instanceNumber, String objectID) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        }
        try {
            return readRemoteObjectvalue(remoteDevice, BACnetDataConversions.getObjectIdentifier(objectID));
        } catch (BACnetRuntimeException e) {
            return Single.error(e);
        }
    }

    public Single<JsonObject> readRemoteObjectvalue(RemoteDevice remoteDevice, ObjectIdentifier oid) {
        return callAsyncBlocking(() -> {
            Encodable val = getPropery(remoteDevice, oid, PropertyIdentifier.presentValue);
            return new JsonObject().put("value", BACnetDataConversions.encodableToPrimitive(val));
        });
    }

    public Single<JsonObject> readMultipleRemoteObjectvalue(int instanceNumber, List<String> oids) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        }
        try {
            ArrayList<ObjectIdentifier> list = new ArrayList<>();
            for (String s : oids) {
                list.add(BACnetDataConversions.getObjectIdentifier(s));
            }
            return readMultipleRemoteObjectvalue(remoteDevice, list);
        } catch (BACnetRuntimeException e) {
            return Single.error(e);
        }
    }

    public Single<JsonObject> readMultipleRemoteObjectvalue(RemoteDevice remoteDevice, List<ObjectIdentifier> oids) {
        return callAsyncBlocking(() -> {
            PropertyValues values = RequestUtils.readOidPresentValues(localDevice, remoteDevice, oids, null);
            return new JsonObject().put("deviceId", remoteDevice.getInstanceNumber())
                                   .put("points", BACnetDataConversions.readMultipleToJson(values));
        });
    }

    private Encodable getPropery(RemoteDevice rd, ObjectIdentifier oid, PropertyIdentifier pid) {
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

    //    public Single<JsonObject> remoteObjectSubscribeCOV(int instanceNumber, String objectID) {
    //        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
    //        if (remoteDevice == null) {
    //            return Single.error(new BACnetRuntimeException("Remote device not found"));
    //        }
    //        try {
    //            return remoteObjectSubscribeCOV(remoteDevice, BACnetDataConversions.getObjectIdentifier
    //            (objectID));
    //        } catch (BACnetRuntimeException e) {
    //            return Single.error(e);
    //        }
    //    }
    //
    //    public Single<JsonObject> remoteObjectSubscribeCOV(RemoteDevice rd, ObjectIdentifier oid) {
    //        return callAsyncBlocking(() -> sendSubscribeCOVRequestBlocking(rd, oid));
    //    }
    //
    //    public JsonObject sendSubscribeCOVRequestBlocking(RemoteDevice rd, ObjectIdentifier oid) throws
    //    Exception {
    //        boolean correctID = false;
    //        while (!correctID) {
    //            int subID = (int) Math.floor((Math.random() * 100) + 1);
    //            UnsignedInteger subProcessID = new UnsignedInteger(subID);
    //            UnsignedInteger lifetime = new UnsignedInteger(0);
    //            SubscribeCOVRequest request = new SubscribeCOVRequest(subProcessID, oid, Boolean.TRUE, lifetime);
    //            localDevice.send(rd, request).get();
    //            correctID = true;
    //            remoteSubscriptions.put(remoteObjectKey(rd, oid), subProcessID.intValue());
    //        }
    //        return new JsonObject();
    //    }
    //
    //    public Single<JsonObject> removeRemoteObjectSubscription(int instanceNumber, String objectId) {
    //        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
    //        if (remoteDevice == null) {
    //            return Single.error(new BACnetRuntimeException("Remote device not found"));
    //        }
    //        try {
    //            return removeRemoteObjectSubscription(remoteDevice, BACnetDataConversions.getObjectIdentifier
    //            (objectId));
    //        } catch (BACnetRuntimeException e) {
    //            return Single.error(e);
    //        }
    //    }
    //
    //    public Single<JsonObject> removeRemoteObjectSubscription(RemoteDevice remoteDevice, ObjectIdentifier
    //    oid) {
    //        try {
    //            return Single.create(source -> removeRemoteObjectSubscriptionBlocking(remoteDevice, oid));
    //        } catch (Exception e) {
    //            return Single.error(e);
    //        }
    //    }
    //
    //    //CHECK LocalDevice COV_Contexts for memory overuse
    //    public JsonObject removeRemoteObjectSubscriptionBlocking(RemoteDevice remoteDevice, ObjectIdentifier oid)
    //        throws Exception {
    //        if (!remoteSubscriptions.containsKey(remoteObjectKey(remoteDevice, oid))) {
    //            throw new BACnetException("Subscription doesn't exist");
    //        }
    //        UnsignedInteger subId = new UnsignedInteger(remoteSubscriptions.get(remoteObjectKey(remoteDevice,
    //        oid)));
    //        SubscribeCOVRequest request = new SubscribeCOVRequest(subId, oid, null, null);
    //        //check don't need to confirm
    //        localDevice.send(remoteDevice, request);
    //        remoteSubscriptions.remove(remoteObjectKey(remoteDevice, oid));
    //        return new JsonObject();
    //    }

    public Single<JsonObject> writeAtPriority(int instanceNumber, String obj, Object v, int priority) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        }
        try {
            if (priority < 1 || priority > 16) {
                throw new BACnetException("Invalid priority");
            }
            Encodable val;
            ObjectIdentifier oid = BACnetDataConversions.getObjectIdentifier(obj);
            if (BACnetDataConversions.isPrimitiveNull(v)) {
                val = Null.instance;
            } else if (oid.getObjectType()
                          .isOneOf(ObjectType.binaryOutput, ObjectType.binaryInput, ObjectType.binaryValue)) {
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
