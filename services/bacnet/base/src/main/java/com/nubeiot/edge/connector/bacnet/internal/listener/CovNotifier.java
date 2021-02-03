package com.nubeiot.edge.connector.bacnet.internal.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventMessage;
import io.github.zero88.qwe.event.EventbusClient;
import io.github.zero88.qwe.event.Waybill;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryParams;
import com.nubeiot.edge.connector.bacnet.mixin.BACnetJsonMixin;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @see <a href="https://store.chipkin.com/articles/bacnet-what-is-the-bacnet-change-of-value-cov">COV</a>
 */
@Slf4j
@RequiredArgsConstructor
public final class CovNotifier extends DeviceEventAdapter implements DeviceEventListener {

    private final BACnetDevice device;
    private final Map<String, Waybill> dispatchers = new HashMap<>();

    @Override
    public void covNotificationReceived(final UnsignedInteger subscriberProcessIdentifier,
                                        final ObjectIdentifier initiatingDevice,
                                        final ObjectIdentifier monitoredObjectIdentifier,
                                        final UnsignedInteger timeRemaining,
                                        final SequenceOf<PropertyValue> listOfValues) {
        final String key = DiscoveryParams.builder()
                                          .networkId(device.protocol().identifier())
                                          .deviceInstance(initiatingDevice.getInstanceNumber())
                                          .objectCode(ObjectIdentifierMixin.serialize(monitoredObjectIdentifier))
                                          .build()
                                          .buildKey(DiscoveryLevel.OBJECT);
        log.info("COV of '{}' notification received, time remaining: '{}s'", key, timeRemaining.longValue());
        final Waybill dispatcher = dispatchers.get(key);
        if (Objects.isNull(dispatcher)) {
            return;
        }
        final JsonObject convertValue = BACnetJsonMixin.MAPPER.convertValue(listOfValues, JsonObject.class);
        EventbusClient.create(device.sharedData())
                      .publish(dispatcher.getAddress(), EventMessage.initial(dispatcher.getAction(), convertValue));
    }

    public void addDispatcher(@NonNull EventMessage subscribeCOVResult, @NonNull DiscoveryArguments args,
                              @NonNull RequestData requestData) {
        if (subscribeCOVResult.isError()) {
            return;
        }
        dispatchers.put(args.key(), Waybill.from(requestData.body()));
    }

    public void removeDispatcher(@NonNull EventMessage subscribeCOVResult, @NonNull DiscoveryArguments args,
                                 @NonNull RequestData requestData) {
        if (subscribeCOVResult.isError()) {
            return;
        }
        dispatchers.remove(args.key());
    }

}
