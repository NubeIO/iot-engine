package com.nubeiot.edge.module.datapoint.service.extension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.decorator.RequestDecorator;
import com.nubeiot.core.sql.decorator.RequestDecoratorExtension;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;

import lombok.NonNull;

public interface EdgeExtension extends RequestDecoratorExtension {

    static EdgeExtension create(@NonNull AbstractEntityService service) {
        return new EdgeExtension() {
            @Override
            public @NonNull RequestDecorator decorator() { return service; }

            @Override
            public <D> D getSharedDataValue(String dataKey) { return service.entityHandler().sharedData(dataKey); }

            @Override
            public RequestDecoratorExtension registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
                return this;
            }
        };
    }

    /**
     * Optimize json object.
     *
     * @param keys           the keys
     * @param optimizedValue the optimized value
     * @param data           the data
     * @return the json object
     * @since 1.0.0
     */
    @NonNull
    static JsonObject optimize(@NonNull List<String> keys, String optimizedValue, JsonObject data) {
        if (Strings.isBlank(optimizedValue) || Objects.isNull(data) || keys.isEmpty()) {
            return new JsonObject();
        }
        final String key = keys.stream().filter(data::containsKey).findFirst().orElse(keys.get(0));
        if (Objects.isNull(data.getValue(key))) {
            return data.put(key, optimizedValue);
        }
        return data;
    }

    @Override
    default @NonNull List<String> bodyKeys() {
        return Arrays.asList(EdgeMetadata.INSTANCE.requestKeyName(), EdgeMetadata.INSTANCE.singularKeyName());
    }

    @Override
    default @NonNull List<String> filterKeys() {
        return Collections.singletonList(EdgeMetadata.INSTANCE.singularKeyName());
    }

    @Override
    @NonNull
    default RequestData optimizeRequestBody(@NonNull RequestData reqData) {
        return reqData.setBody(optimize(bodyKeys(), getSharedDataValue(DataPointIndex.EDGE_ID), reqData.body()));
    }

    @Override
    @NonNull
    default RequestData optimizeRequestFilter(@NonNull RequestData reqData) {
        return reqData.filter(optimize(filterKeys(), getSharedDataValue(DataPointIndex.EDGE_ID), reqData.filter()));
    }

}
