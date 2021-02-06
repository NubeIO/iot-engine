package io.github.zero88.qwe.iot.connector.scanner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.zero88.qwe.iot.data.entity.IDevice;
import io.github.zero88.utils.Strings;
import io.reactivex.Single;

import lombok.NonNull;

/**
 * The interface Data protocol device scanner.
 *
 * @param <X> Type of {@code protocol object}
 * @since 1.0.0
 */
public interface DeviceRpcScanner<P extends IDevice, X> extends RpcScanner<P, X> {

    //    @Override
    default @NonNull String parseKey(@NonNull P entity) {
        return Strings.toString(entity.key());
    }

    /**
     * Scan by network.
     *
     * @param networkId the network id
     * @return map of network data id and protocol object
     * @since 1.0.0
     */
    default @NonNull Single<Map<String, X>> scan(@NonNull UUID networkId) {
        //        final P pojo = (P) new EdgeDeviceComposite().setDevice(
        //            new Device().setProtocol(protocol()).setState(State.ENABLED)).setNetworkId(networkId);
        //        return query(RequestData.builder().body(JsonPojo.from(pojo).toJson()).build());
        return Single.just(new HashMap<>());
    }

}
