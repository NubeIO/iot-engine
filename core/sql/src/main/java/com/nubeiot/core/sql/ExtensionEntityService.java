package com.nubeiot.core.sql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.ResultQuery;
import org.jooq.UpdatableRecord;
import org.jooq.exception.TooManyRowsException;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.StateException;

import lombok.NonNull;

public abstract class ExtensionEntityService<K, M extends VertxPojo, R extends UpdatableRecord<R>,
                                                D extends VertxDAO<R, M, K>>
    extends AbstractEntityService<K, M, R, D> implements ExtensionResource {

    public ExtensionEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    protected @NonNull RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        if (action == EventAction.GET_LIST) {
            return recompute(requestData, null);
        }
        if (action != EventAction.CREATE) {
            return recompute(requestData, Collections.singletonMap(jsonKeyName(), parsePrimaryKey(requestData)));
        }
        return super.recompute(action, requestData);
    }

    @Override
    protected ResultQuery<R> query(@NonNull DSLContext ctx, @NonNull RequestData requestData) {
        return super.query(ctx, requestData);
    }

    @Override
    protected Single<M> doGetOne(RequestData requestData) {
        K pk = parsePrimaryKey(requestData);
        return get().queryExecutor()
                    .findOne(ctx -> query(ctx, requestData))
                    .map(o -> o.orElseThrow(() -> notFound(pk)))
                    .onErrorResumeNext(t -> Single.error(t instanceof TooManyRowsException ? new StateException(
                        "Query is not correct, the result contains more than one record", t) : t));
    }

    @Override
    protected @NonNull JsonObject customizeGetItem(@NonNull M pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(computeIgnoreFields(requestData));
    }

    @Override
    protected @NonNull JsonObject customizeModifiedItem(@NonNull M pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(JsonData.MAPPER, computeIgnoreFields(requestData));
    }

    protected Set<String> computeIgnoreFields(@NonNull RequestData requestData) {
        JsonObject filter = Optional.ofNullable(requestData.getFilter()).orElseGet(JsonObject::new);
        final Set<String> ignores = new HashSet<>();
        ignores.addAll(IGNORE_FIELDS);
        ignores.addAll(
            extensions().keySet().stream().filter(s -> filter.fieldNames().contains(s)).collect(Collectors.toSet()));
        return ignores;
    }

    protected RequestData recompute(RequestData requestData, Map<String, ?> extra) {
        JsonObject filter = Optional.ofNullable(requestData.getFilter()).orElseGet(JsonObject::new);
        Optional.ofNullable(requestData.body())
                .ifPresent(body -> body.stream()
                                       .filter(entry -> this.extensions().containsKey(entry.getKey()))
                                       .forEach(entry -> filter.put(entry.getKey(), this.extensions()
                                                                                        .get(entry.getKey())
                                                                                        .apply(entry.getValue()
                                                                                                    .toString()))));
        Optional.ofNullable(extra).ifPresent(m -> filter.getMap().putAll(m));
        return RequestData.builder()
                          .body(requestData.body())
                          .headers(requestData.headers())
                          .filter(filter)
                          .pagination(requestData.getPagination())
                          .build();
    }

}
