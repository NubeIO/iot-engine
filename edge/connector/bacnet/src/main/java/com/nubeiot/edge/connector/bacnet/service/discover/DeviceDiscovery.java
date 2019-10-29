package com.nubeiot.edge.connector.bacnet.service.discover;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.converter.BACnetDataConversions;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.dto.DiscoverOptions;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.RequestUtils;

import lombok.NonNull;

public final class DeviceDiscovery extends AbstractBACnetDiscoveryService implements BACnetDiscoveryService {

    DeviceDiscovery(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull String servicePath() {
        return "/discovery/bacnet/network/:network_code/device";
    }

    @Override
    public String paramPath() {
        return "/:device_id";
    }

    @EventContractor(action = EventAction.DISCOVER, returnType = Single.class)
    public Single<JsonObject> discover(RequestData reqData) {
        final BACnetNetwork network = BACnetNetwork.factory(reqData.body().getJsonObject("network"));
        logger.info("Request network {}", network.toJson());
        final int deviceId = reqData.body().getJsonObject("device", new JsonObject()).getInteger("device", -1);
        final BACnetDevice device = new BACnetDevice(getVertx(), getSharedKey(), network);
        final DiscoverOptions options = parseDiscoverOptions(reqData);
        return device.discoverRemoteDevices(options)
                     .filter(remoteDevice -> remoteDevice.getInstanceNumber() == deviceId)
                     .switchIfEmpty(Observable.error(new NotFoundException("Not found device id")))
                     .flatMapSingle(remote -> collectRemoteDevice(device.getLocalDevice(), remote).map(
                         r -> new JsonObject().put(toId(remote.getObjectIdentifier()), r)))
                     .collectInto(new JsonObject(), JsonObject::mergeIn)
                     .doFinally(device::stop);
    }

    private Single<JsonObject> collectRemoteDevice(@NonNull LocalDevice localDevice,
                                                   @NonNull RemoteDevice remoteDevice) {
        return Observable.fromIterable(getObjectList(localDevice, remoteDevice))
                         .filter(objId -> objId.getObjectType() != ObjectType.device)
                         .flatMap(objId -> eachObject(localDevice, remoteDevice, objId).map(
                             r -> new JsonObject().put(toId(objId), r)).toObservable())
                         .collectInto(new JsonObject(), JsonObject::mergeIn);
    }

    private String toId(ObjectIdentifier objId) {
        return BACnetDataConversions.pointFormatBACnet(objId);
    }

    private Single<JsonObject> eachObject(@NonNull LocalDevice localDevice, @NonNull RemoteDevice remoteDevice,
                                          @NonNull ObjectIdentifier objId) {
        return Observable.fromIterable(ObjectProperties.getRequiredObjectPropertyTypeDefinitions(objId.getObjectType()))
                         .map(definition -> new ObjectPropertyReference(objId, definition.getPropertyTypeDefinition()
                                                                                         .getPropertyIdentifier()))
                         .collect(PropertyReferences::new,
                                  (refs, opr) -> refs.addIndex(objId, opr.getPropertyIdentifier(),
                                                               opr.getPropertyArrayIndex()))
                         .map(propRefs -> RequestUtils.readProperties(localDevice, remoteDevice, propRefs, true, null))
                         .map(pvs -> {
                             JsonObject json = new JsonObject();
                             pvs.iterator()
                                .forEachRemaining(opr -> json.put(opr.getPropertyIdentifier().toString(),
                                                                  pvs.getNoErrorCheck(opr).toString()));
                             return json;
                         });
    }

    private SequenceOf<ObjectIdentifier> getObjectList(@NonNull LocalDevice localDevice,
                                                       @NonNull RemoteDevice remoteDevice) {
        try {
            return RequestUtils.getObjectList(localDevice, remoteDevice);
        } catch (BACnetException e) {
            throw new NubeException(ErrorCode.ENGINE_ERROR, e);
        }
    }

}
