package com.nubeiot.core.sql;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.utils.Functions;

import lombok.NonNull;

public interface CompositeMetadata<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>,
                                      C extends CompositePojo<P, C>>
    extends EntityMetadata<K, P, R, D> {

    @SuppressWarnings("unchecked")
    @Override
    @NonNull Class<C> modelClass();

    @SuppressWarnings("unchecked")
    @Override
    default @NonNull C parse(@NonNull JsonObject request) throws IllegalArgumentException {
        return EntityHandler.parse(modelClass(), request);
    }

    @NonNull Class<P> rawClass();

    default @NonNull C wrap(@NonNull JsonObject request, @NonNull EntityMetadata reference)
        throws IllegalArgumentException {
        final String refKey = reference.singularKeyName();
        return parse(request).wrap(Collections.singletonMap(refKey, reference.parse(request.getJsonObject(refKey))));
    }

    default NotFoundException notFound(@NonNull RequestData reqData, EntityMetadata... references) {
        return notFound(reqData, Stream.of(references).collect(Collectors.toList()));
    }

    default NotFoundException notFound(@NonNull RequestData reqData, Collection<EntityMetadata> references) {
        if (Objects.isNull(references)) {
            return notFound(reqData);
        }
        String msg = references.stream()
                               .filter(Objects::nonNull)
                               .filter(m -> !requestKeyName().equals(m.requestKeyName()))
                               .map(ref -> new SimpleEntry<>(ref.requestKeyName(),
                                                             Functions.getIfThrow(() -> ref.parseKey(reqData))
                                                                      .orElse("")))
                               .map(entry -> entry.getKey() + "=" + entry.getValue())
                               .collect(Collectors.joining(" and ", " and ", ""));
        return new NotFoundException(notFound(reqData).getMessage() + msg);
    }

    default @NonNull C convert(@NonNull P pojo) {
        return CompositePojo.create(pojo, rawClass(), modelClass());
    }

    default <REC extends Record> RecordMapper<REC, C> mapper(EntityMetadata... references) {
        return r -> (C) r.into(modelClass())
                         .wrap(Stream.of(references)
                                     .filter(Objects::nonNull)
                                     .collect(Collectors.toMap(EntityMetadata::singularKeyName,
                                                               ref -> (VertxPojo) r.into(ref.modelClass()))));
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
