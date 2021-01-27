package com.nubeiot.edge.connector.bacnet.service.discovery;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.ErrorMessage;
import io.github.zero88.qwe.dto.converter.ErrorMessageConverter;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.protocol.CommunicationProtocol;
import io.github.zero88.utils.Functions;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.rpc.BaseRpcProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetObjectCache;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryOptions;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryRequest;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryRequestWrapper;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.entity.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.iotdata.IoTEntity;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.ObjectPropertyTypeDefinition;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.RequestUtils;

import lombok.NonNull;

/**
 * Defines public service to expose HTTP API for end-user and/or nube-io service
 */
abstract class AbstractBACnetExplorer<P extends IoTEntity> extends BaseRpcProtocol<P> implements BACnetExplorer<P> {

    AbstractBACnetExplorer(@NonNull SharedDataLocalProxy sharedDataProxy) {
        super(sharedDataProxy);
    }

    @Override
    public Single<JsonObject> discoverThenWatch(@NonNull RequestData data) {
        return Single.just(new JsonObject());
    }

    @Override
    public Single<JsonObject> discoverManyThenWatch(@NonNull RequestData data) {
        return Single.just(new JsonObject());
    }

    final BACnetNetworkCache networkCache() {
        return sharedData().getData(BACnetCacheInitializer.EDGE_NETWORK_CACHE);
    }

    final BACnetDeviceCache deviceCache() {
        return sharedData().getData(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
    }

    final BACnetObjectCache objectCache() {
        return sharedData().getData(BACnetCacheInitializer.BACNET_OBJECT_CACHE);
    }

    final DiscoveryOptions parseDiscoverOptions(@NonNull RequestData reqData) {
        final LocalDeviceMetadata metadata = sharedData().getData(BACnetDevice.EDGE_BACNET_METADATA);
        return DiscoveryOptions.from(metadata.getMaxTimeoutInMS(), reqData);
    }

    final CommunicationProtocol parseNetworkProtocol(@NonNull DiscoveryRequest request) {
        return Optional.ofNullable(request.getNetwork())
                       .map(n -> BACnetNetwork.factory(n).toProtocol())
                       .map(p -> networkCache().add(p.identifier(), p).get(p.identifier()))
                       .orElseGet(() -> networkCache().get(request.getNetworkCode()));
    }

    final Single<PropertyValuesMixin> parseRemoteObject(@NonNull BACnetDevice device,
                                                        @NonNull RemoteDevice remoteDevice,
                                                        @NonNull ObjectIdentifier objId, boolean detail,
                                                        boolean includeError) {
        return Observable.fromIterable(getObjectTypes(detail, objId.getObjectType()))
                         .map(definition -> new ObjectPropertyReference(objId, definition.getPropertyTypeDefinition()
                                                                                         .getPropertyIdentifier()))
                         .collect(PropertyReferences::new,
                                  (refs, opr) -> refs.addIndex(objId, opr.getPropertyIdentifier(),
                                                               opr.getPropertyArrayIndex()))
                         .map(propertyRefs -> RequestUtils.readProperties(device.localDevice(), remoteDevice,
                                                                          propertyRefs, true, null))
                         .map(pvs -> PropertyValuesMixin.create(objId, pvs, includeError));
    }

    private List<ObjectPropertyTypeDefinition> getObjectTypes(boolean detail, @NonNull ObjectType objectType) {
        return detail
               ? ObjectProperties.getObjectPropertyTypeDefinitions(objectType)
               : ObjectProperties.getRequiredObjectPropertyTypeDefinitions(objectType);
    }

    protected String parsePersistResponse(@NonNull JsonObject output) {
        final ErrorMessage error = Functions.getIfThrow(() -> ErrorMessage.parse(output)).orElse(null);
        if (Objects.nonNull(error)) {
            throw ErrorMessageConverter.from(error);
        }
        return parseResourceId(output.getJsonObject("resource", new JsonObject()));
    }

    protected abstract String parseResourceId(@NonNull JsonObject resource);

    protected final @NonNull DiscoveryRequestWrapper createDiscoveryRequest(@NonNull RequestData reqData,
                                                                            @NonNull DiscoveryLevel level) {
        final DiscoveryRequest request = DiscoveryRequest.from(reqData, level);
        final DiscoveryOptions options = parseDiscoverOptions(reqData);
        final CommunicationProtocol protocol = parseNetworkProtocol(request);
        final BACnetDevice device = deviceCache().get(protocol);
        return new DiscoveryRequestWrapper(request, options, device);
    }

}
