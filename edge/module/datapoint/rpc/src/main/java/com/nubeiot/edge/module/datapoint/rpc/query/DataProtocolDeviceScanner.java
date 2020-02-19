package com.nubeiot.edge.module.datapoint.rpc.query;

import java.util.Map;
import java.util.UUID;

import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeDeviceMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.EdgeDeviceComposite;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

/**
 * The interface Data protocol device scanner.
 *
 * @param <X> Type of {@code protocol object}
 * @param <T> Type of {@code DataProtocolDeviceScanner}
 * @since 1.0.0
 */
public interface DataProtocolDeviceScanner<X, T extends DataProtocolDeviceScanner>
    extends DataProtocolScanner<EdgeDeviceComposite, X, T> {

    @Override
    default @NonNull EdgeDeviceMetadata context() {
        return EdgeDeviceMetadata.INSTANCE;
    }

    @Override
    default @NonNull String parseKey(@NonNull EdgeDeviceComposite entity) {
        return Strings.toString(entity.getDeviceId());
    }

    /**
     * Scan by network.
     *
     * @param networkId the network id
     * @return map of network data id and protocol object
     * @since 1.0.0
     */
    default @NonNull Single<Map<String, X>> scan(@NonNull UUID networkId) {
        final EdgeDeviceComposite pojo = (EdgeDeviceComposite) new EdgeDeviceComposite().setDevice(
            new Device().setProtocol(protocol()).setState(State.ENABLED)).setNetworkId(networkId);
        return query(RequestData.builder().body(JsonPojo.from(pojo).toJson()).build());
    }

}
