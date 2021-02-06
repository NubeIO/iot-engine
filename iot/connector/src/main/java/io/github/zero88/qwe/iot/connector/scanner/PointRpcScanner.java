package io.github.zero88.qwe.iot.connector.scanner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.zero88.qwe.iot.data.entity.IPoint;
import io.reactivex.Single;

import lombok.NonNull;

/**
 * The interface Data protocol point scanner.
 *
 * @param <X> Type of {@code protocol object}
 * @since 1.0.0
 */
public interface PointRpcScanner<P extends IPoint, X> extends RpcScanner<P, X> {

    //    @Override
    //    default @NonNull Class<P> context() {
    //        return PointTransducerMetadata.INSTANCE;
    //    }
    //
    //    @Override
    //    default @NonNull String parseKey(@NonNull PointTransducerComposite entity) {
    //        return Strings.toString(entity.getPointId());
    //    }
    //

    /**
     * Scan by network.
     *
     * @param networkId the network id
     * @param deviceId  the device id
     * @return map of network data id and protocol object
     * @since 1.0.0
     */
    default @NonNull Single<Map<String, X>> scan(@NonNull UUID networkId, @NonNull UUID deviceId) {
        //        final PointTransducerComposite pojo = (PointTransducerComposite) new PointTransducerComposite()
        //        .setNetworkId(
        //            networkId).setDeviceId(deviceId);
        //        return query(RequestData.builder().body(JsonPojo.from(pojo).toJson()).build());
        return Single.just(new HashMap<>());
    }

}
