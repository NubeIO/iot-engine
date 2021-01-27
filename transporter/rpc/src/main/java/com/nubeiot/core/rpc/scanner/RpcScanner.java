package com.nubeiot.core.rpc.scanner;

import com.nubeiot.core.rpc.RpcProtocolClient;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * Represents for a {@code RpcClient service} that is able to scan from a specific external source to extract an
 * appropriate {@code protocol} data object then using them to initialize itself service in startup.
 *
 * @param <P> Type of {@code IoTEntity}
 * @param <X> Type of {@code Protocol object}
 * @see ScannerSource
 * @since 1.0.0
 */
public interface RpcScanner<P extends IoTEntity, X> extends RpcProtocolClient<P> {

    @NonNull ScannerSource source();

    //    /**
    //     * Query data.
    //     *
    //     * @param requestData the request data
    //     * @return the single
    //     * @since 1.0.0
    //     */
    //    default Single<Map<String, X>> query(@NonNull RequestData requestData) {
    //        final String respKey = context().pluralKeyName();
    //        return execute(EventAction.GET_LIST, requestData).map(resp -> resp.getJsonArray(respKey, new JsonArray()))
    //                                                         .map(this::convert);
    //    }
    //
    //    /**
    //     * Declares a {@code protocol translator} that deserializes {@code data point} entity to corresponding object
    //     * depends on {@link #protocol()}
    //     *
    //     * @return protocol translator
    //     * @see IoTEntityConverter
    //     * @since 1.0.0
    //     */
    //    @NonNull IoTEntityConverter<P, X> translator();
    //
    //    /**
    //     * Parse key string.
    //     *
    //     * @param entity the entity
    //     * @return the string
    //     * @since 1.0.0
    //     */
    //    @NonNull String parseKey(@NonNull P entity);
    //
    //    /**
    //     * Converts response.
    //     *
    //     * @param array list response data
    //     * @return map of network data id and communication protocol
    //     * @since 1.0.0
    //     */
    //    @SuppressWarnings("unchecked")
    //    default Map<String, X> convert(@NonNull JsonArray array) {
    //        return array.stream()
    //                    .filter(JsonObject.class::isInstance)
    //                    .map(json -> (P) context().parseFromEntity((JsonObject) json))
    //                    .map(pojo -> new SimpleEntry<>(parseKey(pojo), translator().deserialize(pojo)))
    //                    .filter(entry -> Strings.isNotBlank(entry.getKey()))
    //                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    //    }
}
