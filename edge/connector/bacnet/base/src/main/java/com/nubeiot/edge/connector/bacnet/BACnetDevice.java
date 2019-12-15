package com.nubeiot.edge.connector.bacnet;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.utils.ExecutorHelpers;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.connector.bacnet.converter.BACnetDataConversions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverResponse;
import com.nubeiot.edge.connector.bacnet.discover.RemoteDeviceScanner;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.dto.TransportProvider;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import lombok.Getter;
import lombok.NonNull;

public final class BACnetDevice extends AbstractSharedDataDelegate<BACnetDevice> {

    public static final String EDGE_BACNET_METADATA = "EDGE_BACNET_METADATA";

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Getter
    private final LocalDeviceMetadata metadata;
    private final TransportProvider transportProvider;
    @Getter
    private final LocalDevice localDevice;

    public BACnetDevice(@NonNull Vertx vertx, @NonNull String sharedKey, @NonNull CommunicationProtocol protocol) {
        this(vertx, sharedKey, TransportProvider.byProtocol(protocol));
    }

    private BACnetDevice(@NonNull Vertx vertx, @NonNull String sharedKey, @NonNull TransportProvider provider) {
        super(vertx);
        this.metadata = registerSharedKey(sharedKey).getSharedDataValue(EDGE_BACNET_METADATA);
        this.transportProvider = provider;
        this.localDevice = create(metadata, transportProvider);
    }

    static LocalDevice create(@NonNull LocalDeviceMetadata metadata, @NonNull TransportProvider transportProvider) {
        final Transport transport = transportProvider.get();
        transport.setTimeout((int) metadata.getMaxTimeoutInMS());
        final LocalDevice device = new LocalDevice(metadata.getDeviceNumber(), transport);
        return device.writePropertyInternal(PropertyIdentifier.vendorIdentifier,
                                            new UnsignedInteger(metadata.getVendorId()))
                     .writePropertyInternal(PropertyIdentifier.vendorName,
                                            new CharacterString(metadata.getVendorName()))
                     .writePropertyInternal(PropertyIdentifier.modelName, new CharacterString(metadata.getModelName()))
                     .writePropertyInternal(PropertyIdentifier.objectType, ObjectType.device)
                     .writePropertyInternal(PropertyIdentifier.objectName,
                                            new CharacterString(metadata.getObjectName()));
    }

    BACnetDevice asyncStart() {
        final EventController client = getSharedDataValue(SHARED_EVENTBUS);
        final DiscoverOptions options = DiscoverOptions.builder()
                                                       .force(true)
                                                       .timeout(metadata.getMaxTimeoutInMS())
                                                       .timeUnit(TimeUnit.MILLISECONDS)
                                                       .build();
        scanRemoteDevices(options).subscribe(discoverer -> client.request(publishDiscoverCompletion(discoverer)),
                                             logger::error);
        return this;
    }

    public void stop() {
        localDevice.terminate();
    }

    public Single<RemoteDevice> discoverRemoteDevice(int deviceCode, DiscoverOptions options) {
        long timeout = TimeUnit.MILLISECONDS.convert(options.getTimeout(), options.getTimeUnit());
        return init(options.isForce()).map(
            ld -> Functions.getOrThrow(t -> new NotFoundException("Not found device id " + deviceCode, t),
                                       () -> ld.getRemoteDeviceBlocking(deviceCode, timeout)));
    }

    public BACnetDevice addListener(DeviceEventListener... listeners) {
        Stream.of(listeners)
              .filter(Objects::nonNull)
              .forEachOrdered(listener -> this.localDevice.getEventHandler().addListener(listener));
        return this;
    }

    public Single<RemoteDeviceScanner> scanRemoteDevices(@NonNull DiscoverOptions options) {
        return this.init(options.isForce())
                   .map(ld -> RemoteDeviceScanner.create(ld, options))
                   .map(RemoteDeviceScanner::start)
                   .delay(options.getTimeout(), options.getTimeUnit())
                   .doAfterSuccess(RemoteDeviceScanner::stop);
    }

    private Single<LocalDevice> init(boolean force) {
        return ExecutorHelpers.blocking(getVertx(), () -> {
            if (force || !localDevice.isInitialized()) {
                return localDevice.initialize();
            }
            return localDevice;
        });
    }

    private DeliveryEvent publishDiscoverCompletion(RemoteDeviceScanner scanner) {
        final JsonArray remotes = scanner.getRemoteDevices()
                                         .stream()
                                         .map(BACnetDataConversions::deviceMinimal)
                                         .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
        final JsonObject body = DiscoverResponse.builder()
                                                .network(transportProvider.protocol())
                                                .localDevice(metadata)
                                                .remoteDevices(remotes)
                                                .build()
                                                .toJson();
        return DeliveryEvent.builder()
                            .action(EventAction.NOTIFY)
                            .address(metadata.getDiscoverCompletionAddress())
                            .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                            .addPayload(RequestData.builder().body(body).build())
                            .build();
    }

}
