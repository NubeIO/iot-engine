package com.nubeiot.edge.module.datapoint.rpc.query;

import java.util.Map;
import java.util.UUID;

import io.github.zero.utils.Strings;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointThingMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointThingComposite;

import lombok.NonNull;

/**
 * The interface Data protocol point scanner.
 *
 * @param <X> Type of {@code protocol object}
 * @param <T> Type of {@code DataProtocolPointScanner}
 * @since 1.0.0
 */
public interface DataProtocolPointScanner<X, T extends DataProtocolPointScanner>
    extends DataProtocolScanner<PointThingComposite, X, T> {

    @Override
    default @NonNull PointThingMetadata context() {
        return PointThingMetadata.INSTANCE;
    }

    @Override
    default @NonNull String parseKey(@NonNull PointThingComposite entity) {
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
        final PointThingComposite pojo = (PointThingComposite) new PointThingComposite().setNetworkId(networkId)
                                                                                        .setDeviceId(deviceId);
        return query(RequestData.builder().body(JsonPojo.from(pojo).toJson()).build());
    }

}
