package com.nubeiot.core.sql;

import java.util.Collections;

import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.pojos.CompositePojo;

import lombok.NonNull;

public interface CompositeMetadata<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>,
                                      C extends CompositePojo<P, C>>
    extends EntityMetadata<K, P, R, D> {

    @SuppressWarnings("unchecked")
    @Override
    @NonNull Class<C> modelClass();

    @NonNull Class<P> rawClass();

    @SuppressWarnings("unchecked")
    @Override
    default @NonNull C parse(@NonNull JsonObject request) throws IllegalArgumentException {
        return EntityHandler.parse(modelClass(), request);
    }

    default @NonNull C wrap(@NonNull JsonObject request, @NonNull EntityMetadata reference)
        throws IllegalArgumentException {
        final String refKey = reference.singularKeyName();
        return parse(request).wrap(Collections.singletonMap(refKey, reference.parse(request.getJsonObject(refKey))));
    }

    default @NonNull C convert(@NonNull P pojo) {
        return CompositePojo.create(pojo, rawClass(), modelClass());
    }

    default <REC extends Record> RecordMapper<REC, C> mapper(@NonNull EntityMetadata reference) {
        return r -> r.into(modelClass())
                     .wrap(Collections.singletonMap(reference.singularKeyName(),
                                                    (VertxPojo) r.into(reference.modelClass())));
    }

    abstract class AbstractCompositeMetadata<K, P extends VertxPojo, R extends UpdatableRecord<R>,
                                                D extends VertxDAO<R, P, K>, C extends CompositePojo<P, C>>
        implements CompositeMetadata<K, P, R, D, C> {

        @Override
        public abstract @NonNull Class<C> modelClass();

        @Override
        public C parse(@NonNull JsonObject request) throws IllegalArgumentException {
            return CompositeMetadata.super.parse(request);
        }

    }

}
