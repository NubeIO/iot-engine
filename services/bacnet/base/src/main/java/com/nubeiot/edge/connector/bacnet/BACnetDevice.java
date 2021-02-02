package com.nubeiot.edge.connector.bacnet;

import java.util.Arrays;
import java.util.List;

import io.github.zero88.qwe.component.HasSharedData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventMessage;
import io.github.zero88.qwe.protocol.CommunicationProtocol;
import io.reactivex.Single;

import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryOptions;
import com.nubeiot.edge.connector.bacnet.internal.request.ConfirmedRequestFactory;
import com.nubeiot.edge.connector.bacnet.internal.request.RemoteDeviceScanner;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.service.confirmed.ConfirmedRequestService;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;

/**
 * The interface BACnet Device.
 *
 * @since 1.0.0
 */
public interface BACnetDevice extends HasSharedData {

    /**
     * The constant CONFIG_KEY in cache.
     */
    String CONFIG_KEY = "BACNET_CONFIG";

    /**
     * Gets local device config.
     *
     * @return the local device config
     * @see BACnetConfig
     * @since 1.0.0
     */
    @NonNull BACnetConfig config();

    /**
     * Gets communication protocol.
     *
     * @return the communication protocol
     * @see CommunicationProtocol
     * @since 1.0.0
     */
    @NonNull CommunicationProtocol protocol();

    /**
     * Gets local device.
     *
     * @return the local device
     * @see LocalDevice
     * @since 1.0.0
     */
    @NonNull LocalDevice localDevice();

    /**
     * Add listeners to BACnet Device.
     *
     * @param listeners the listeners
     * @return a reference to this, so the API can be used fluently
     * @since 1.0.0
     */
    @NonNull BACnetDevice addListeners(@NonNull List<DeviceEventListener> listeners);

    /**
     * Add listener to BACnet Device.
     *
     * @param listeners the listeners
     * @return a reference to this, so the API can be used fluently
     * @since 1.0.0
     */
    default @NonNull BACnetDevice addListeners(DeviceEventListener... listeners) {
        return addListeners(Arrays.asList(listeners));
    }

    /**
     * Async start BACnet device.
     *
     * @return a reference to this, so the API can be used fluently
     * @since 1.0.0
     */
    @NonNull BACnetDevice asyncStart();

    /**
     * Stop BACnet device.
     *
     * @return a reference to this, so the API can be used fluently
     * @since 1.0.0
     */
    @NonNull Single<BACnetDevice> stop();

    /**
     * Scan remote devices in {@code BACnet} network
     *
     * @param options the options
     * @return the remote devices
     * @see DiscoveryOptions
     * @see RemoteDeviceScanner
     * @since 1.0.0
     */
    @NonNull Single<RemoteDeviceScanner> scanRemoteDevices(@NonNull DiscoveryOptions options);

    /**
     * Discover remote device.
     *
     * @param arguments the discovery arguments
     * @return the remote device
     * @see DiscoveryArguments
     * @see RemoteDevice
     * @since 1.0.0
     */
    @NonNull Single<RemoteDevice> discoverRemoteDevice(@NonNull DiscoveryArguments arguments);

    /**
     * Discover remote object
     *
     * @param arguments the discovery arguments
     * @return the property values of object
     * @see DiscoveryArguments
     * @see PropertyValuesMixin
     * @since 1.0.0
     */
    @NonNull Single<PropertyValuesMixin> discoverRemoteObject(@NonNull DiscoveryArguments arguments);

    /**
     * Parse remote object
     *
     * @param remoteDevice remote device
     * @param objId        object id
     * @param detail       should be detail
     * @param includeError should include error
     * @return the property values of object
     * @see PropertyValuesMixin
     * @since 1.0.0
     */
    @NonNull Single<PropertyValuesMixin> parseRemoteObject(@NonNull RemoteDevice remoteDevice,
                                                           @NonNull ObjectIdentifier objId, boolean detail,
                                                           boolean includeError);

    /**
     * Send BACnet request
     *
     * @param action      Event action
     * @param args        Discovery request arguments
     * @param requestData Request data
     * @param factory     BACnet request
     * @return json result
     * @see DiscoveryArguments
     * @see ConfirmedRequestFactory
     */
    @NonNull <T extends ConfirmedRequestService, D> Single<EventMessage> send(@NonNull EventAction action,
                                                                              @NonNull DiscoveryArguments args,
                                                                              @NonNull RequestData requestData,
                                                                              @NonNull ConfirmedRequestFactory<T, D> factory);

}
