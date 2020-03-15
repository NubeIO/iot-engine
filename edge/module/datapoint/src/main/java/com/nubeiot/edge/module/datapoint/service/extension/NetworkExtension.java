package com.nubeiot.edge.module.datapoint.service.extension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestFilter;
import com.nubeiot.core.sql.decorator.RequestDecorator;
import com.nubeiot.core.sql.decorator.RequestDecoratorExtension;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;

import lombok.NonNull;

public interface NetworkExtension extends RequestDecoratorExtension {

    Set<String> DEFAULT_ALIASES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(NetworkMetadata.DEFAULT_CODE, "LOCAL")));

    static JsonObject optimizeAlias(@NonNull List<String> keys, String optimizedValue, JsonObject data,
                                    boolean forceAdd) {
        if (Strings.isBlank(optimizedValue) || Objects.isNull(data) || keys.isEmpty()) {
            return data;
        }
        final String key = keys.stream().filter(data::containsKey).findFirst().orElse(forceAdd ? keys.get(0) : null);
        if (Strings.isBlank(key)) {
            return data;
        }
        final Object value = data.getValue(key);
        if (forceAdd && Objects.isNull(value) || DEFAULT_ALIASES.contains(Strings.toString(value).toUpperCase())) {
            return data.put(key, optimizedValue);
        }
        return data;
    }

    static NetworkExtension create(@NonNull RequestDecoratorExtension extension) {
        return new NetworkExtension() {
            @Override
            public @NonNull RequestDecorator decorator() { return extension; }

            @Override
            public <D> D getSharedDataValue(String dataKey) { return extension.getSharedDataValue(dataKey); }

            @Override
            public RequestDecoratorExtension registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
                return this;
            }
        };
    }

    static NetworkExtension create(@NonNull AbstractEntityService service) {
        return new NetworkExtension() {
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

    @Override
    default @NonNull List<String> bodyKeys() {
        return Arrays.asList(NetworkMetadata.INSTANCE.requestKeyName(), NetworkMetadata.INSTANCE.singularKeyName());
    }

    @Override
    default @NonNull List<String> filterKeys() {
        return Collections.singletonList(NetworkMetadata.INSTANCE.singularKeyName());
    }

    @Override
    @NonNull
    default RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return decorator().onCreatingOneResource(requestData.setBody(
            optimizeAlias(bodyKeys(), getSharedDataValue(DataPointIndex.DEFAULT_NETWORK_ID), requestData.body(),
                          true)));
    }

    @Override
    @NonNull
    default RequestData optimizeRequestBody(@NonNull RequestData reqData) {
        return reqData.setBody(
            optimizeAlias(bodyKeys(), getSharedDataValue(DataPointIndex.DEFAULT_NETWORK_ID), reqData.body(), false));
    }

    @Override
    @NonNull
    default RequestData optimizeRequestFilter(@NonNull RequestData reqData) {
        return reqData.filter(new RequestFilter(
            optimizeAlias(filterKeys(), getSharedDataValue(DataPointIndex.DEFAULT_NETWORK_ID), reqData.filter(),
                          false)));
    }

}
