package com.nubeiot.core.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.validation.CompositeValidation;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;

public interface CompositeMetadata<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>,
                                      C extends CompositePojo<P, C>>
    extends EntityMetadata<K, P, R, D>, CompositeValidation<P, C> {

    @Override
    @SuppressWarnings("unchecked")
    @NonNull Class<C> modelClass();

    @NonNull Class<P> rawClass();

    @NonNull List<EntityMetadata> subItems();

    @Override
    default CompositeMetadata context() {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    default @NonNull C parseFromEntity(@NonNull JsonObject entity) throws IllegalArgumentException {
        Map<String, VertxPojo> sub = subItems().stream()
                                               .filter(m -> entity.getValue(m.singularKeyName()) instanceof JsonObject)
                                               .collect(Collectors.toMap(EntityMetadata::singularKeyName,
                                                                         m -> m.parseFromEntity(entity.getJsonObject(
                                                                             m.singularKeyName()))));
        return ((C) ReflectionClass.createObject(modelClass()).fromJson(entity)).wrap(sub);
    }

    @Override
    @SuppressWarnings("unchecked")
    default @NonNull C parseFromRequest(@NonNull JsonObject request) throws IllegalArgumentException {
        Map<String, VertxPojo> sub = subItems().stream()
                                               .filter(m -> request.getValue(m.singularKeyName()) instanceof JsonObject)
                                               .collect(Collectors.toMap(EntityMetadata::singularKeyName,
                                                                         m -> m.parseFromRequest(request.getJsonObject(
                                                                             m.singularKeyName()))));
        return ((C) ReflectionClass.createObject(modelClass()).fromJson(request)).wrap(sub);
    }

    @Override
    @SuppressWarnings("unchecked")
    default C onCreating(RequestData reqData) throws IllegalArgumentException {
        return CompositeValidation.super.onCreating(reqData);
    }

    default <REC extends Record> RecordMapper<REC, C> mapper() {
        return mapper(subItems().toArray(new EntityMetadata[] {}));
    }

    //TODO hack way due to jooq bug. Must guard the order of given references
    default <REC extends Record> RecordMapper<REC, C> mapper(EntityMetadata... references) {
        return r -> {
            final C into = r.into(table().fields()).into(modelClass());
            final Iterator<EntityMetadata> iterator = Stream.of(references).iterator();
            Map<String, List<Field>> group = Stream.of(
                r.fieldsRow().fields(IntStream.range(table().fields().length, r.fields().length).toArray()))
                                                   .collect(LinkedHashMap::new, (map, f) -> map.computeIfAbsent(
                                                       f.getQualifiedName().first(), k -> new ArrayList<>()).add(f),
                                                            Map::putAll);
            final Map<String, VertxPojo> other = new HashMap<>();
            for (List<Field> fields : group.values()) {
                if (!iterator.hasNext()) {
                    break;
                }
                final EntityMetadata ref = iterator.next();
                other.put(ref.singularKeyName(),
                          (VertxPojo) r.into(fields.toArray(new Field[0])).into(ref.modelClass()));
            }
            return (C) into.wrap(other);
        };
    }

    abstract class AbstractCompositeMetadata<K, P extends VertxPojo, R extends UpdatableRecord<R>,
                                                D extends VertxDAO<R, P, K>, C extends CompositePojo<P, C>>
        implements CompositeMetadata<K, P, R, D, C> {

        private final List<EntityMetadata> sub = new ArrayList<>();

        @Override
        public abstract @NonNull Class<C> modelClass();

        @Override
        public @NonNull List<EntityMetadata> subItems() {
            return sub;
        }

        @Override
        public C parseFromEntity(@NonNull JsonObject entity) throws IllegalArgumentException {
            return CompositeMetadata.super.parseFromEntity(entity);
        }

        @Override
        public C parseFromRequest(@NonNull JsonObject request) throws IllegalArgumentException {
            return CompositeMetadata.super.parseFromRequest(request);
        }

        @SuppressWarnings("unchecked")
        protected <T extends CompositeMetadata> T addSubItem(EntityMetadata... metadata) {
            sub.addAll(Stream.of(metadata).filter(Objects::nonNull).collect(Collectors.toList()));
            return (T) this;
        }

    }

}
