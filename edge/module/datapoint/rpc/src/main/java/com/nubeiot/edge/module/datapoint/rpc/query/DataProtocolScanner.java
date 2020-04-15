package com.nubeiot.edge.module.datapoint.rpc.query;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.github.zero.utils.Strings;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.module.datapoint.rpc.DataProtocolRpcClient;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;

import lombok.NonNull;

/**
 * The interface Data protocol scanner for single protocol object.
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <X> Type of {@code Protocol object}
 * @param <T> Type of {@code DataProtocolQuery}
 * @since 1.0.0
 */
public interface DataProtocolScanner<P extends VertxPojo, X, T extends DataProtocolScanner>
    extends DataProtocolRpcClient<T> {

    /**
     * Query data.
     *
     * @param requestData the request data
     * @return the single
     * @since 1.0.0
     */
    default Single<Map<String, X>> query(@NonNull RequestData requestData) {
        final String respKey = context().pluralKeyName();
        return execute(EventAction.GET_LIST, requestData).map(resp -> resp.getJsonArray(respKey, new JsonArray()))
                                                         .map(this::convert);
    }

    /**
     * Declares a {@code protocol translator} that deserializes {@code data point} entity to corresponding object
     * depends on {@link #protocol()}
     *
     * @return protocol translator
     * @see IoTEntityTranslator
     * @since 1.0.0
     */
    @NonNull IoTEntityTranslator<P, X> translator();

    /**
     * Parse key string.
     *
     * @param entity the entity
     * @return the string
     * @since 1.0.0
     */
    @NonNull String parseKey(@NonNull P entity);

    /**
     * Converts response.
     *
     * @param array list response data
     * @return map of network data id and communication protocol
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default Map<String, X> convert(@NonNull JsonArray array) {
        return array.stream()
                    .filter(JsonObject.class::isInstance)
                    .map(json -> (P) context().parseFromEntity((JsonObject) json))
                    .map(pojo -> new SimpleEntry<>(parseKey(pojo), translator().deserialize(pojo)))
                    .filter(entry -> Strings.isNotBlank(entry.getKey()))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

}
