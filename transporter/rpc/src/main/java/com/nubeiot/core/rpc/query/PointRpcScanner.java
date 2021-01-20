package com.nubeiot.core.rpc.query;

import java.util.Map;
import java.util.UUID;

import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.utils.Strings;
import io.reactivex.Single;

import lombok.NonNull;

/**
 * The interface Data protocol point scanner.
 *
 * @param <X> Type of {@code protocol object}
 * @param <T> Type of {@code DataProtocolPointScanner}
 * @since 1.0.0
 */
public interface PointRpcScanner<X, T extends PointRpcScanner> extends RpcScanner<PointTransducerComposite, X, T> {

    @Override
    default @NonNull PointTransducerMetadata context() {
        return PointTransducerMetadata.INSTANCE;
    }

    @Override
    default @NonNull String parseKey(@NonNull PointTransducerComposite entity) {
        return Strings.toString(entity.getPointId());
    }

    /**
     * Scan by network.
     *
     * @param networkId the network id
     * @param deviceId  the device id
     * @return map of network data id and protocol object
     * @since 1.0.0
     */
    default @NonNull Single<Map<String, X>> scan(@NonNull UUID networkId, @NonNull UUID deviceId) {
        final PointTransducerComposite pojo = (PointTransducerComposite) new PointTransducerComposite().setNetworkId(
            networkId).setDeviceId(deviceId);
        return query(RequestData.builder().body(JsonPojo.from(pojo).toJson()).build());
    }

}
