package com.nubeiot.core.sql.query;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface ReferenceQueryExecutor<P extends VertxPojo> extends SimpleQueryExecutor<P> {

    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> ReferenceQueryExecutor create(
        EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
        return new DaoReferenceQueryExecutor<>(handler, metadata);
    }

    interface ReferenceFilterCreation {

        static JsonObject createFilter(@NonNull Map<String, EntityMetadata> references, JsonObject filter) {
            if (Objects.isNull(filter)) {
                return new JsonObject();
            }
            filter.getMap()
                  .putAll(filter.fieldNames()
                                .stream()
                                .filter(s -> s.contains(".") && references.containsKey(s.substring(0, s.indexOf("."))))
                                .map(ReferenceFilterCreation::refKeyEntryWithRoot)
                                .map(entry -> referenceFilterEntry(filter, entry))
                                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
            return filter;
        }

        static Entry<String, String> refKeyEntryWithRoot(String s) {
            return new SimpleEntry<>(s.substring(0, s.indexOf(".")), s);
        }

        static Entry<String, Map<String, Object>> referenceFilterEntry(JsonObject rootFilter,
                                                                       Entry<String, String> entry) {
            return new SimpleEntry<>(entry.getKey(), findRefFilterFromRoot(rootFilter, entry));
        }

        static Map<String, Object> findRefFilterFromRoot(JsonObject rootFilter, Entry<String, String> entry) {
            return Collections.singletonMap(entry.getValue().replaceAll("^" + entry.getKey() + "\\.", ""),
                                            rootFilter.getValue(entry.getValue()));
        }

    }


    class DaoReferenceQueryExecutor<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>>
        extends DaoQueryExecutor<K, P, R, D> implements ReferenceQueryExecutor<P> {

        DaoReferenceQueryExecutor(EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata) {
            super(handler, metadata);
        }

        @Override
        public Single<P> findOneByKey(RequestData requestData) {
            K pk = metadata.parseKey(requestData);
            return handler.dao(metadata.daoClass())
                          .queryExecutor()
                          .findOne(ctx -> query(ctx, requestData))
                          .flatMap(o -> o.map(Single::just).orElse(Single.error(metadata.notFound(pk))))
                          .onErrorResumeNext(EntityQueryExecutor::wrapDatabaseError);
        }

    }

}
