package com.nubeiot.edge.connector.bacnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.edge.connector.bacnet.converter.BACnetDataConversions;
import com.nubeiot.edge.connector.bacnet.converter.LocalPointObjectUtils;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.dto.TransportProvider;
import com.nubeiot.edge.connector.bacnet.handlers.BACnetEventListener;
import com.nubeiot.edge.connector.bacnet.listener.WhoIsListener;
import com.nubeiot.edge.connector.bacnet.objectModels.EdgePoint;
import com.nubeiot.edge.connector.bacnet.objectModels.EdgeWriteRequest;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.cache.RemoteEntityCachePolicy;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.AnalogValueObject;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.BinaryValueObject;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.ObjectPropertyTypeDefinition;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.DiscoveryUtils;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RemoteDeviceDiscoverer;
import com.serotonin.bacnet4j.util.RequestUtils;

import lombok.NonNull;

/**
 * Main BACnetInstance functionality - initialisation - local object adding - remote device request utils
 *
 * @implNote Use {@link DeviceEventListener} to listen event.
 *     <p>
 *     Must add more listener in {@link #addBACnetEvent(LocalDeviceMetadata, LocalDevice, Map)}
 */
public final class BACnetInstance extends AbstractSharedDataDelegate<BACnetInstance> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalDevice localDevice;
    //only mapping virtual points as other points can be calculated to get ObjectIdentifier so this saves memory
    //TODO: look into moving this higher up to save memory. need to check if each instance creates the exact same point
    // and instance number every time across all BACnetInstances
    private final Map<String, ObjectIdentifier> virtualPointsMap = new HashMap<>();
    private RemoteDeviceDiscoverer deviceDiscoverer;

    private BACnetInstance(@NonNull Vertx vertx, @NonNull String sharedKey, @NonNull LocalDevice localDevice) {
        super(vertx);
        registerSharedKey(sharedKey);
        this.localDevice = localDevice;
    }

    private BACnetInstance(@NonNull Vertx vertx, @NonNull String sharedKey, @NonNull TransportProvider provider) {
        super(vertx);
        registerSharedKey(sharedKey);
        this.localDevice = BACnetDevice.create(getSharedDataValue(BACnetDevice.EDGE_BACNET_METADATA), provider);
        logger.info("Init BACnet instance with network {}", provider.config().toJson());
    }

    public static BACnetInstance create(Vertx vertx, String sharedKey, LocalDevice localDevice) {
        return new BACnetInstance(vertx, sharedKey, localDevice);
    }

    public static Single<BACnetInstance> create(@NonNull Vertx vertx, @NonNull String sharedKey,
                                                @NonNull TransportProvider provider,
                                                @NonNull Map<String, BACnetInstance> bacnetInstances) {
        return new BACnetInstance(vertx, sharedKey, provider).init(bacnetInstances)
                                                             .doOnSuccess(instance -> bacnetInstances.put(
                                                                 provider.config().getName(), instance));
    }

    void terminate() {
        clearLocalObjects();
        localDevice.clearRemoteDevices();
        if (deviceDiscoverer != null) {
            deviceDiscoverer.stop();
        }
        localDevice.terminate();
    }

    private Single<BACnetInstance> init(@NonNull Map<String, BACnetInstance> instances) {
        final LocalDeviceMetadata metadata = getSharedDataValue(BACnetDevice.EDGE_BACNET_METADATA);
        //TODO should consider RemoteEntityCachePolicy.NEVER_EXPIRE
        return Single.fromCallable(localDevice::initialize)
                     .map(ld -> addBACnetEvent(metadata, localDevice, instances))
                     .map(ld -> ld.startRemoteDeviceDiscovery(rd -> ld.getCachePolicies()
                                                                      .putDevicePolicy(rd.getInstanceNumber(),
                                                                                       RemoteEntityCachePolicy.NEVER_EXPIRE)))
                     .delay(metadata.getMaxTimeoutInMS(), TimeUnit.MILLISECONDS)
                     .doOnEvent((discoverer, throwable) -> discoverer.stop())
                     .doOnSuccess(discover -> deviceDiscoverer = discover)
                     .map(discover -> this);
    }

    private LocalDevice addBACnetEvent(@NonNull LocalDeviceMetadata metadata, @NonNull LocalDevice localDevice,
                                       @NonNull Map<String, BACnetInstance> instances) {
        localDevice.getEventHandler().addListener(new WhoIsListener());
        //TODO: not meaningful, should remove
        if (metadata.isSlave()) {
            localDevice.getEventHandler()
                       .addListener(new BACnetEventListener(getVertx(), this, localDevice, instances));
        }
        return localDevice;
    }

    @Deprecated
    public void startRemoteDiscover() {
        final LocalDeviceMetadata metadata = getSharedDataValue(BACnetDevice.EDGE_BACNET_METADATA);
        startRemoteDiscover(metadata.getMaxTimeoutInMS());
    }

    @Deprecated
    public void startRemoteDiscover(long timeout) {
        if (deviceDiscoverer != null) {
            deviceDiscoverer.stop();
        }
        localDevice.clearRemoteDevices();
        deviceDiscoverer = localDevice.startRemoteDeviceDiscovery(remoteDevice -> localDevice.getCachePolicies()
                                                                                             .putDevicePolicy(
                                                                                                 remoteDevice.getInstanceNumber(),
                                                                                                 RemoteEntityCachePolicy.NEVER_EXPIRE));
        getVertx().setTimer(timeout, s -> deviceDiscoverer.stop());
    }

    @Deprecated
    public void initialiseLocalObjectsFromJson(JsonObject json) {
        //TODO IN FUTURE REMOVE ALL NON LOCAL/ SAME NETWORK POINTS
        clearLocalObjects();
        Observable.fromIterable(json.getMap().entrySet()).subscribe(entry -> {
            EdgePoint point = EdgePoint.fromJson(entry.getKey(), JsonObject.mapFrom(entry.getValue()));
            addLocalObject(point);
        }, error -> logger.error("Init local points error", error));
    }

    @Deprecated
    public ObjectIdentifier getLocalObjectId(String id) throws Exception {
        ObjectIdentifier oid;
        try {
            oid = BACnetDataConversions.getObjectIdentifierFromNube(id);
            if (localDevice.getObject(oid) != null) {
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

    @Deprecated
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

    @Deprecated
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

    @Deprecated
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

    @Deprecated
    public void removeLocalObject(String nubeId) throws Exception {
        ObjectIdentifier oid = getLocalObjectId(nubeId);
        localDevice.removeObject(oid);
        virtualPointsMap.remove(nubeId);
    }

    @Deprecated
    public void writeLocalObject(EdgeWriteRequest req) throws Exception {
        LocalPointObjectUtils.writeLocalObject(req, getLocalObjectId(req.getId()), localDevice);
    }

    @Deprecated
    public void updateLocalObject(String id, String property, Object value) throws Exception {
        LocalPointObjectUtils.updateLocalObjectProperty(localDevice, getLocalObjectId(id), property, value);
    }

    //REMOTE DEVICE FUNCTIONS
    @Deprecated
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

    @Deprecated
    public Single<JsonObject> getRemoteDeviceExtendedInfo(int instanceNumber) {
        RemoteDevice remoteDevice = localDevice.getCachedRemoteDevice(instanceNumber);
        if (remoteDevice == null) {
            return Single.error(new BACnetRuntimeException("Remote device not found"));
        } else {
            return getRemoteDeviceExtendedInfo(remoteDevice);
        }
    }

    @Deprecated
    private Single<JsonObject> getRemoteDeviceExtendedInfo(RemoteDevice remoteDevice) {
        return callAsyncBlocking(() -> {
            DiscoveryUtils.getExtendedDeviceInformation(localDevice, remoteDevice);
            return BACnetDataConversions.deviceExtended(remoteDevice);
        });
    }

    @Deprecated
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

    @Deprecated
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

    @Deprecated
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

    @Deprecated
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

    @Deprecated
    public Single<JsonObject> readRemoteObjectvalue(RemoteDevice remoteDevice, ObjectIdentifier oid) {
        return callAsyncBlocking(() -> {
            Encodable val = getPropery(remoteDevice, oid, PropertyIdentifier.presentValue);
            return new JsonObject().put("value", BACnetDataConversions.encodableToPrimitive(val));
        });
    }

    @Deprecated
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

    @Deprecated
    public Single<JsonObject> readMultipleRemoteObjectvalue(RemoteDevice remoteDevice, List<ObjectIdentifier> oids) {
        return callAsyncBlocking(() -> {
            PropertyValues values = RequestUtils.readOidPresentValues(localDevice, remoteDevice, oids, null);
            return new JsonObject().put("deviceId", remoteDevice.getInstanceNumber())
                                   .put("points", BACnetDataConversions.readMultipleToJson(values));
        });
    }

    @Deprecated
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

    @Deprecated
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

    @Deprecated
    private void writeAtPriority(RemoteDevice rd, ObjectIdentifier oid, Encodable val, int priority)
        throws BACnetException {
        RequestUtils.writeProperty(localDevice, rd, oid, PropertyIdentifier.presentValue, val, priority);
    }

    @Deprecated
    private Single<JsonObject> callAsyncBlocking(Callable callable) {
        return Single.create(source -> getVertx().executeBlocking(future -> {
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

    @Deprecated
    private String remoteObjectKey(@NonNull RemoteDevice remoteDevice, @NonNull ObjectIdentifier oid) {
        return remoteDevice.hashCode() + "::" + oid.hashCode();
    }

}
