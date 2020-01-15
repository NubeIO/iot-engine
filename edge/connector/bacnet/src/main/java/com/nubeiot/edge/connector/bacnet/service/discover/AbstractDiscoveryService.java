package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.ErrorMessageConverter;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetObjectCache;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
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
abstract class AbstractDiscoveryService extends AbstractSharedDataDelegate<AbstractDiscoveryService>
    implements BACnetDiscoveryService {

    AbstractDiscoveryService(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx);
        registerSharedKey(sharedKey);
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

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public abstract Single<JsonObject> list(RequestData reqData);

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public abstract Single<JsonObject> get(RequestData reqData);

    @EventContractor(action = EventAction.BATCH_CREATE, returnType = Single.class)
    public abstract Single<JsonObject> discoverThenDoBatch(RequestData reqData);

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public abstract Single<JsonObject> discoverThenDoPersist(RequestData reqData);

    final BACnetNetworkCache networkCache() {
        return getSharedDataValue(BACnetCacheInitializer.EDGE_NETWORK_CACHE);
    }

    final BACnetDeviceCache deviceCache() {
        return getSharedDataValue(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
    }

    final BACnetObjectCache objectCache() {
        return getSharedDataValue(BACnetCacheInitializer.BACNET_OBJECT_CACHE);
    }

    final DiscoverOptions parseDiscoverOptions(@NonNull RequestData reqData) {
        final LocalDeviceMetadata metadata = getSharedDataValue(BACnetDevice.EDGE_BACNET_METADATA);
        return DiscoverOptions.from(metadata.getMaxTimeoutInMS(), reqData);
    }

    final CommunicationProtocol parseNetworkProtocol(@NonNull DiscoverRequest request) {
        final CommunicationProtocol cacheProtocol = networkCache().get(request.getNetworkCode());
        final CommunicationProtocol reqBodyProtocol = BACnetNetwork.factory(
            Optional.ofNullable(request.getNetwork()).orElse(new JsonObject())).toProtocol();
        return JsonData.from(cacheProtocol.toJson().mergeIn(reqBodyProtocol.toJson()), CommunicationProtocol.class);
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

    protected final @NonNull DiscoveryRequestWrapper toRequest(@NonNull RequestData reqData,
                                                               @NonNull DiscoverRequest.DiscoverLevel level) {
        final DiscoverRequest request = DiscoverRequest.from(reqData, level);
        final DiscoverOptions options = parseDiscoverOptions(reqData);
        final CommunicationProtocol protocol = parseNetworkProtocol(request);
        final BACnetDevice device = deviceCache().get(protocol);
        return new DiscoveryRequestWrapper(request, options, device);
    }


}
