package com.nubeiot.core.sql.decorator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
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
     * Construct {@code CUD Response} that includes full resource
     *
     * @param action Event action
     * @param result Result data
     * @return response
     */
    static JsonObject fullResponse(@NonNull EventAction action, @NonNull JsonObject result) {
        return new JsonObject().put("resource", result).put("action", action).put("status", Status.SUCCESS);
    }

    static JsonObject keyResponse(@NonNull String keyName, @NonNull Object keyValue) {
        return new JsonObject().put(keyName, JsonData.checkAndConvert(keyValue));
    }

    static JsonObject getData(@NonNull JsonObject responseData) {
        return responseData.getJsonObject("resource");
    }

    /**
     * @return resource metadata
     */
    @NonNull EntityMetadata resourceMetadata();

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
     * @see #afterCreate(Object, VertxPojo, RequestData)
     * @see #afterDelete(VertxPojo, RequestData)
     * @see #afterUpdate(Object, VertxPojo, RequestData)
     * @see #afterPatch(Object, VertxPojo, RequestData)
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
    default Single<JsonObject> afterEachList(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return Single.just(JsonPojo.from(pojo).toJson(ignoreFields(requestData)));
    }

    /**
     * Wrap list data to json object
     *
     * @param results given results
     * @return json object of list data
     */
    @NonNull
    default JsonObject wrapListData(@NonNull JsonArray results) {
        return new JsonObject().put(resourceMetadata().pluralKeyName(), results);
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
    default Single<JsonObject> afterGet(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return Single.just(JsonPojo.from(pojo).toJson(ignoreFields(requestData)));
    }

    /**
     * Do any transform resource after {@code CREATE} action successfully if {@link #enableFullResourceInCUDResponse()}
     * is {@code true}
     *
     * @param key     pojo key
     * @param pojo    item
     * @param reqData request data
     * @return transformer item
     * @apiNote By default, result doesn't omits {@code null} fields
     */
    @NonNull
    default Single<JsonObject> afterCreate(@NonNull Object key, @NonNull VertxPojo pojo, @NonNull RequestData reqData) {
        return Single.just(doTransform(EventAction.CREATE, key, pojo, reqData,
                                       (p, r) -> JsonPojo.from(pojo).toJson(JsonData.MAPPER, ignoreFields(reqData))));
    }

    /**
     * Do any transform resource after {@code UPDATE} action successfully if {@link #enableFullResourceInCUDResponse()}
     * is {@code true}
     *
     * @param key     pojo key
     * @param pojo    item
     * @param reqData request data
     * @return transformer item
     * @apiNote By default, result omits {@code null} fields and {@link #AUDIT_FIELDS}
     */
    default @NonNull Single<JsonObject> afterUpdate(@NonNull Object key, @NonNull VertxPojo pojo,
                                                    @NonNull RequestData reqData) {
        return Single.just(doTransform(EventAction.UPDATE, key, pojo, reqData,
                                       (p, r) -> JsonPojo.from(pojo).toJson(JsonPojo.MAPPER, ignoreFields(reqData))));
    }

    /**
     * Do any transform resource after {@code PATCH} action successfully if {@link #enableFullResourceInCUDResponse()}
     * is {@code true}
     *
     * @param key     pojo key
     * @param pojo    item
     * @param reqData request data
     * @return transformer item
     * @apiNote By default, result omits {@code null} fields and {@link #AUDIT_FIELDS}
     */
    @NonNull
    default Single<JsonObject> afterPatch(@NonNull Object key, @NonNull VertxPojo pojo, @NonNull RequestData reqData) {
        return Single.just(doTransform(EventAction.PATCH, key, pojo, reqData,
                                       (p, r) -> JsonPojo.from(pojo).toJson(JsonPojo.MAPPER, ignoreFields(reqData))));
    }

    /**
     * Do any transform resource after {@code DELETE} action successfully if {@link #enableFullResourceInCUDResponse()}
     * is {@code true}
     *
     * @param pojo    item
     * @param reqData request data
     * @return transformer item
     * @apiNote By default, result doesn't omits {@code null} fields
     */
    @NonNull
    default Single<JsonObject> afterDelete(@NonNull VertxPojo pojo, @NonNull RequestData reqData) {
        return Single.just(doTransform(EventAction.REMOVE, pojo, reqData,
                                       (p, r) -> JsonPojo.from(p).toJson(JsonData.MAPPER, ignoreFields(reqData)),
                                       JsonObject::new));
    }

    default JsonObject doTransform(EventAction action, Object key, VertxPojo pojo, RequestData reqData,
                                   BiFunction<VertxPojo, RequestData, JsonObject> converter) {
        return doTransform(action, pojo, reqData, converter,
                           () -> keyResponse(resourceMetadata().requestKeyName(), key));
    }

    default JsonObject doTransform(EventAction action, VertxPojo pojo, RequestData reqData,
                                   BiFunction<VertxPojo, RequestData, JsonObject> converter,
                                   Supplier<JsonObject> ifNotFull) {
        if (enableFullResourceInCUDResponse()) {
            return fullResponse(action, converter.apply(pojo, reqData));
        } else {
            return ifNotFull.get();
        }
    }

}
