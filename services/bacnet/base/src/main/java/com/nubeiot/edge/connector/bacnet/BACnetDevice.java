package com.nubeiot.edge.connector.bacnet;

import java.util.Arrays;
import java.util.List;

import io.github.zero88.qwe.protocol.CommunicationProtocol;
import io.reactivex.Single;

import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.RemoteDeviceScanner;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;

/**
 * The interface BACnet Device.
 *
 * @since 1.0.0
 */
public interface BACnetDevice {

    /**
     * The constant EDGE_BACNET_METADATA in cache.
     */
    String EDGE_BACNET_METADATA = "EDGE_BACNET_METADATA";

    /**
     * Gets local device metadata.
     *
     * @return the local device metadata
     * @see LocalDeviceMetadata
     * @since 1.0.0
     */
    @NonNull LocalDeviceMetadata metadata();

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
     * @see DiscoverOptions
     * @see RemoteDeviceScanner
     * @since 1.0.0
     */
    @NonNull Single<RemoteDeviceScanner> scanRemoteDevices(@NonNull DiscoverOptions options);

    /**
     * Discover remote device by {@code device code}.
     *
     * @param deviceCode the device code
     * @param options    the options
     * @return the remote device
     * @see DiscoverOptions
     * @see RemoteDevice
     * @since 1.0.0
     */
    @NonNull Single<RemoteDevice> discoverRemoteDevice(@NonNull ObjectIdentifier deviceCode,
                                                       @NonNull DiscoverOptions options);

}
