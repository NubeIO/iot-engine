package com.nubeiot.edge.connector.bacnet.internal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.ErrorData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventMessage;
import io.github.zero88.qwe.event.EventbusClient;
import io.github.zero88.qwe.exceptions.NotFoundException;
import io.github.zero88.qwe.protocol.CommunicationProtocol;
import io.github.zero88.qwe.utils.ExecutorHelpers;
import io.github.zero88.utils.Functions;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.SingleHelper;

import com.nubeiot.edge.connector.bacnet.BACnetConfig;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryOptions;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryResponse;
import com.nubeiot.edge.connector.bacnet.entity.BACnetDeviceEntity;
import com.nubeiot.edge.connector.bacnet.internal.listener.BACnetResponseListener;
import com.nubeiot.edge.connector.bacnet.internal.request.ConfirmedRequestFactory;
import com.nubeiot.edge.connector.bacnet.internal.request.RemoteDeviceScanner;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.service.confirmed.ConfirmedRequestService;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.RequestUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(fluent = true)
final class DefaultBACnetDevice implements BACnetDevice {

    @Getter
    private final SharedDataLocalProxy sharedData;
    @Getter
    private final BACnetConfig config;
    @Getter
    private final LocalDevice localDevice;
    private final TransportProvider transportProvider;

    DefaultBACnetDevice(@NonNull SharedDataLocalProxy sharedData, @NonNull CommunicationProtocol protocol) {
        this(sharedData, TransportProvider.byProtocol(protocol));
    }

    private DefaultBACnetDevice(@NonNull SharedDataLocalProxy sharedData, @NonNull TransportProvider provider) {
        this.sharedData = sharedData;
        this.config = sharedData.getData(BACnetDevice.CONFIG_KEY);
        this.transportProvider = provider;
        this.localDevice = create(config, transportProvider);
    }

    static LocalDevice create(@NonNull BACnetConfig config, @NonNull TransportProvider transportProvider) {
        final Transport transport = transportProvider.get();
        transport.setTimeout((int) config.getMaxTimeoutInMS());
        final LocalDevice device = new LocalDevice(config.getDeviceId(), transport);
        return device.writePropertyInternal(PropertyIdentifier.vendorIdentifier,
                                            new UnsignedInteger(config.getVendorId()))
                     .writePropertyInternal(PropertyIdentifier.vendorName, new CharacterString(config.getVendorName()))
                     .writePropertyInternal(PropertyIdentifier.modelName, new CharacterString(config.getModelName()))
                     .writePropertyInternal(PropertyIdentifier.objectType, ObjectType.device)
                     .writePropertyInternal(PropertyIdentifier.objectName, new CharacterString(config.getDeviceName()));
    }

    @Override
    public CommunicationProtocol protocol() {
        return transportProvider.protocol();
    }

    @Override
    public BACnetDevice addListeners(@NonNull List<DeviceEventListener> listeners) {
        listeners.stream()
                 .filter(Objects::nonNull)
                 .forEachOrdered(listener -> this.localDevice.getEventHandler().addListener(listener));
        return this;
    }

    public BACnetDevice asyncStart() {
        final DiscoveryOptions options = DiscoveryOptions.builder()
                                                         .force(true)
                                                         .timeout(config.getMaxDiscoverTimeout())
                                                         .timeUnit(config.getMaxDiscoverTimeoutUnit())
                                                         .build();
        scanRemoteDevices(options).subscribe(this::handleAfterScan);
        return this;
    }

    public Single<BACnetDevice> stop() {
        return ExecutorHelpers.blocking(this.sharedData.getVertx(), () -> {
            localDevice.terminate();
            return this;
        });
    }

    public Single<RemoteDeviceScanner> scanRemoteDevices(@NonNull DiscoveryOptions options) {
        return this.init(options.isForce())
                   .map(ld -> RemoteDeviceScanner.create(ld, options))
                   .map(RemoteDeviceScanner::start)
                   .delay(options.getTimeout(), options.getTimeUnit())
                   .doAfterSuccess(RemoteDeviceScanner::stop);
    }

    public Single<RemoteDevice> discoverRemoteDevice(@NonNull DiscoveryArguments args) {
        final long timeout = TimeUnit.MILLISECONDS.convert(args.options().getTimeout(), args.options().getTimeUnit());
        final Integer id = args.params().getDeviceInstance();
        log.info("Start discovering device {} with force={} in timeout {}ms ", id, args.options().isForce(), timeout);
        return this.init(args.options().isForce())
                   .map(ld -> Functions.getOrThrow(t -> new NotFoundException("Not found device id " + id, t),
                                                   () -> ld.getRemoteDevice(id).get(timeout)));
    }

    @Override
    public @NonNull Single<PropertyValuesMixin> discoverRemoteObject(@NonNull DiscoveryArguments args) {
        log.info("Discovering object '{}' in device '{}' in network {}...",
                 ObjectIdentifierMixin.serialize(args.params().objectCode()),
                 ObjectIdentifierMixin.serialize(args.params().remoteDeviceId()), protocol().identifier());
        return this.discoverRemoteDevice(args)
                   .flatMap(rd -> parseRemoteObject(rd, args.params().objectCode(), true, args.options().isDetail()));
    }

    public Single<PropertyValuesMixin> parseRemoteObject(@NonNull RemoteDevice remoteDevice,
                                                         @NonNull ObjectIdentifier objId, boolean detail,
                                                         boolean includeError) {
        final ObjectType objType = objId.getObjectType();
        return Observable.fromIterable(detail
                                       ? ObjectProperties.getObjectPropertyTypeDefinitions(objType)
                                       : ObjectProperties.getRequiredObjectPropertyTypeDefinitions(objType))
                         .map(definition -> new ObjectPropertyReference(objId, definition.getPropertyTypeDefinition()
                                                                                         .getPropertyIdentifier()))
                         .collect(PropertyReferences::new,
                                  (refs, opr) -> refs.addIndex(objId, opr.getPropertyIdentifier(),
                                                               opr.getPropertyArrayIndex()))
                         .map(propertyRefs -> RequestUtils.readProperties(localDevice, remoteDevice, propertyRefs, true,
                                                                          null))
                         .map(pvs -> PropertyValuesMixin.create(objId, pvs, includeError));
    }

    @Override
    public @NonNull <T extends ConfirmedRequestService, D> Single<EventMessage> send(@NonNull EventAction action,
                                                                                     @NonNull DiscoveryArguments args,
                                                                                     @NonNull RequestData reqData,
                                                                                     @NonNull ConfirmedRequestFactory<T, D> factory) {
        return Single.just(factory.convertData(args, reqData))
                     .map(data -> factory.factory(args, data))
                     .flatMap(req -> this.discoverRemoteDevice(args.params().remoteDeviceId(), args.options())
                                         .flatMap(rd -> {
                                             final Vertx vertx = sharedData().getVertx();
                                             return SingleHelper.toSingle(handler -> vertx.executeBlocking(
                                                 p -> localDevice.send(rd, req, new BACnetResponseListener(action, p)),
                                                 handler));
                                         }));
    }

    private Single<LocalDevice> init(boolean force) {
        return ExecutorHelpers.blocking(this.sharedData.getVertx(), () -> {
            if (force || !localDevice.isInitialized()) {
                return localDevice.initialize();
            }
            return localDevice;
        });
    }

    private void handleAfterScan(RemoteDeviceScanner scanner, Throwable t) {
        final EventbusClient client = EventbusClient.create(this.sharedData);
        final EventMessage msg = Objects.isNull(t) ? createSuccessDiscoverMsg(scanner) : createErrorDiscoverMsg(t);
        client.publish(config.getCompleteDiscoverAddress(), msg);
    }

    private EventMessage createSuccessDiscoverMsg(@NonNull RemoteDeviceScanner scanner) {
        final List<BACnetDeviceEntity> remotes = scanner.getRemoteDevices()
                                                        .stream()
                                                        .map(RemoteDeviceMixin::create)
                                                        .map(rdm -> BACnetDeviceEntity.builder().mixin(rdm).build())
                                                        .collect(Collectors.toList());
        final JsonObject body = DiscoveryResponse.builder()
                                                 .network(protocol())
                                                 .localDevice(LocalDeviceMetadata.from(config()))
                                                 .remoteDevices(remotes)
                                                 .build()
                                                 .toJson();
        return EventMessage.initial(EventAction.NOTIFY, RequestData.builder().body(body).build().toJson());
    }

    private EventMessage createErrorDiscoverMsg(@NonNull Throwable t) {
        final JsonObject extraInfo = DiscoveryResponse.builder()
                                                      .network(protocol())
                                                      .localDevice(LocalDeviceMetadata.from(config()))
                                                      .build()
                                                      .toJson();
        return EventMessage.initial(EventAction.NOTIFY_ERROR,
                                    ErrorData.builder().throwable(t).extraInfo(extraInfo).build());
    }

}
