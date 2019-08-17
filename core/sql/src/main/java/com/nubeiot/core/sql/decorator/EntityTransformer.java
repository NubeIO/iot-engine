package com.nubeiot.core.sql.decorator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.sql.pojos.JsonPojo;

import lombok.NonNull;

/**
 * Transformer entity resource before response to client
 */
public interface EntityTransformer {

    /**
     * Represents set of audit fields. Use {@link Filters#AUDIT} to expose these fields
     */
    Set<String> AUDIT_FIELDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("time_audit", "sync_audit")));

    /**
     * Enable {@code CUD} response includes full resource instead of simple resource with only response status and
     * {@code primary key} of resource.
     *
     * @return {@code true} if enable full resource in response
     */
    default boolean enableFullResourceInCUDResponse() {
        return true;
    }

    /**
     * Get ignore fields by request data. By default, it excludes {@link #AUDIT_FIELDS}.
     * <p>
     * If {@code request data} has {@link Filters#AUDIT}, it will expose these {@code audit fields}
     *
     * @param requestData Request data
     * @return set of ignore fields when do customizing response data
     * @see #afterCreate(VertxPojo, RequestData)
     * @see #afterDelete(VertxPojo, RequestData)
     * @see #afterModify(VertxPojo, RequestData)
     * @see #afterGet(VertxPojo, RequestData)
     * @see #afterEachList(VertxPojo, RequestData)
     */
    default Set<String> ignoreFields(@NonNull RequestData requestData) {
        return requestData.hasAudit() ? Collections.emptySet() : AUDIT_FIELDS;
    }

    /**
     * Do any transform each resource in list item
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     * @apiNote By default, result omits {@code null} fields
     */
    @NonNull
    default JsonObject afterEachList(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return afterGet(pojo, requestData);
    }

    /**
     * Do any transform for single resource
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     * @apiNote By default, result omits {@code null} fields
     */
    @NonNull
    default JsonObject afterGet(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(ignoreFields(requestData));
    }

    /**
     * Do any transform resource after {@code CREATE} action successfully if {@link #enableFullResourceInCUDResponse()}
     * is {@code true}
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     * @apiNote By default, result doesn't omits {@code null} fields
     */
    @NonNull
    default JsonObject afterCreate(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(JsonData.MAPPER, ignoreFields(requestData));
    }

    /**
     * Do any transform resource after {@code UPDATE} or {@code PATCH} action successfully if {@link
     * #enableFullResourceInCUDResponse()} is {@code true}
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     * @apiNote By default, result omits {@code null} fields and {@link #AUDIT_FIELDS}
     */
    @NonNull
    default JsonObject afterModify(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(ignoreFields(requestData));
    }

    /**
     * Do any transform resource after {@code DELETE} action successfully if {@link #enableFullResourceInCUDResponse()}
     * is {@code true}
     *
     * @param pojo        item
     * @param requestData request data
     * @return transformer item
     * @apiNote By default, result doesn't omits {@code null} fields
     */
    @NonNull
    default JsonObject afterDelete(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(JsonData.MAPPER, ignoreFields(requestData));
    }

}
