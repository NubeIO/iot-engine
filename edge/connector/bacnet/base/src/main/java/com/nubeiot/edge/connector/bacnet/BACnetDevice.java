package com.nubeiot.edge.connector.bacnet;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.utils.ExecutorHelpers;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.dto.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.dto.TransportProvider;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.cache.RemoteEntityCachePolicy;
import com.serotonin.bacnet4j.cache.RemoteEntityCachePolicy.TimedExpiry;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.RemoteDeviceDiscoverer;

import lombok.Getter;
import lombok.NonNull;

public final class BACnetDevice extends AbstractSharedDataDelegate<BACnetDevice> {

    public static final String EDGE_BACNET_METADATA = "EDGE_BACNET_METADATA";

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Getter
    private final BACnetNetwork network;
    @Getter
    private final LocalDeviceMetadata metadata;
    @Getter
    private final LocalDevice localDevice;

    public BACnetDevice(@NonNull Vertx vertx, @NonNull String sharedKey, BACnetNetwork network) {
        super(vertx);
        this.metadata = registerSharedKey(sharedKey).getSharedDataValue(EDGE_BACNET_METADATA);
        this.network = network;
        this.localDevice = create(metadata, TransportProvider.byConfig(network));
    }

    static LocalDevice create(@NonNull LocalDeviceMetadata metadata, @NonNull TransportProvider transportProvider) {
        final LocalDevice device = new LocalDevice(metadata.getDeviceNumber(), transportProvider.get());
        return device.writePropertyInternal(PropertyIdentifier.vendorIdentifier,
                                            new UnsignedInteger(metadata.getVendorId()))
                     .writePropertyInternal(PropertyIdentifier.vendorName,
                                            new CharacterString(metadata.getVendorName()))
                     .writePropertyInternal(PropertyIdentifier.modelName, new CharacterString(metadata.getModelName()))
                     .writePropertyInternal(PropertyIdentifier.objectType, ObjectType.device)
                     .writePropertyInternal(PropertyIdentifier.objectName,
                                            new CharacterString(metadata.getObjectName()));
    }

    public void start() {
        EventController client = getSharedDataValue(SHARED_EVENTBUS);
        final DeliveryEvent event = DeliveryEvent.builder()
                                                 .action(EventAction.NOTIFY)
                                                 .address(metadata.getCompleteDiscoverAddress())
                                                 .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                 .addPayload(RequestData.builder().body(network.toJson()).build())
                                                 .build();
        init(DiscoverOptions.builder()
                            .timeout(metadata.getMaxTimeoutInMS())
                            .timeUnit(TimeUnit.MILLISECONDS)
                            .build()).subscribe(discoverer -> client.request(event), logger::error);
    }

    public void stop() {
        localDevice.terminate();
    }

    public Observable<RemoteDevice> discoverRemoteDevices(DiscoverOptions options) {
        return init(options).map(RemoteDeviceDiscoverer::getRemoteDevices).flattenAsObservable(r -> r);
    }

    public BACnetDevice addListener(DeviceEventListener... listeners) {
        Stream.of(listeners)
              .filter(Objects::nonNull)
              .forEachOrdered(listener -> this.localDevice.getEventHandler().addListener(listener));
        return this;
    }

    private Single<RemoteDeviceDiscoverer> init(DiscoverOptions options) {
        final RemoteEntityCachePolicy policy = Optional.ofNullable(options.getDuration())
                                                       .map(TimedExpiry::new)
                                                       .map(RemoteEntityCachePolicy.class::cast)
                                                       .orElse(RemoteEntityCachePolicy.NEVER_EXPIRE);
        return ExecutorHelpers.blocking(getVertx(), localDevice::initialize).map(ld -> {
            logger.info("BACNET::START DISCOVER - Thread: {}", Thread.currentThread().getName());
            return ld.startRemoteDeviceDiscovery(rd -> {
                logger.info("BACNET::INSIDE DISCOVER - Thread: {}", Thread.currentThread().getName());
                ld.getCachePolicies().putDevicePolicy(rd.getInstanceNumber(), policy);
            });
        }).delay(options.getTimeout(), options.getTimeUnit()).doAfterSuccess(RemoteDeviceDiscoverer::stop);
    }

}
