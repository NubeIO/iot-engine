package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.ErrorMessage;
import io.github.zero88.qwe.dto.converter.ErrorMessageConverter;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.micro.metadata.ActionMethodMapping;
import io.github.zero88.utils.Functions;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.rpc.BaseRpcProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetObjectCache;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverLevel;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
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
abstract class AbstractBACnetRpcDiscoveryService<P extends IoTEntity> extends BaseRpcProtocol<P>
    implements BACnetRpcDiscoveryService<P> {

    AbstractBACnetRpcDiscoveryService(@NonNull SharedDataLocalProxy sharedDataProxy) {
        super(sharedDataProxy);
    }

    @Override
    public @NonNull ActionMethodMapping eventMethodMap() {
        Map<EventAction, HttpMethod> methods = new HashMap<>();
        methods.put(EventAction.GET_LIST, HttpMethod.GET);
        methods.put(EventAction.GET_ONE, HttpMethod.GET);
        methods.put(EventAction.BATCH_CREATE, HttpMethod.POST);
        methods.put(EventAction.CREATE, HttpMethod.PUT);
        return ActionMethodMapping.create(methods);
    }

    @EventContractor(action = "GET_LIST", returnType = Single.class)
    public abstract Single<JsonObject> list(RequestData reqData);

    @EventContractor(action = "GET_ONE", returnType = Single.class)
    public abstract Single<JsonObject> get(RequestData reqData);

    @EventContractor(action = "BATCH_CREATE", returnType = Single.class)
    public abstract Single<JsonObject> discoverThenDoBatch(RequestData reqData);

    @EventContractor(action = "CREATE", returnType = Single.class)
    public abstract Single<JsonObject> discoverThenDoPersist(RequestData reqData);

    final BACnetNetworkCache networkCache() {
        return sharedData().getData(BACnetCacheInitializer.EDGE_NETWORK_CACHE);
    }

    final BACnetDeviceCache deviceCache() {
        return sharedData().getData(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
    }

    final BACnetObjectCache objectCache() {
        return sharedData().getData(BACnetCacheInitializer.BACNET_OBJECT_CACHE);
    }

    final DiscoverOptions parseDiscoverOptions(@NonNull RequestData reqData) {
        final LocalDeviceMetadata metadata = sharedData().getData(BACnetDevice.EDGE_BACNET_METADATA);
        return DiscoverOptions.from(metadata.getMaxTimeoutInMS(), reqData);
    }

    final CommunicationProtocol parseNetworkProtocol(@NonNull DiscoverRequest request) {
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
                                                                            @NonNull DiscoverLevel level) {
        final DiscoverRequest request = DiscoverRequest.from(reqData, level);
        final DiscoverOptions options = parseDiscoverOptions(reqData);
        final CommunicationProtocol protocol = parseNetworkProtocol(request);
        final BACnetDevice device = deviceCache().get(protocol);
        return new DiscoveryRequestWrapper(request, options, device);
    }

}
