package com.nubeiot.edge.module.datapoint.model.ditto;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.TypeArgument;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.cache.ClassGraphCache;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.HasSyncAudit;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.tables.JsonTable;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Migrate/convert data between {@code Ditto/LowDB} and {@code Postgres/H2}
 *
 * @param <V> Pojo class that represents for data entity
 */
@SuppressWarnings("unchecked")
public interface IDittoModel<V extends VertxPojo> extends Supplier<V>, JsonData {

    static Class<IDittoModel> find(@NonNull EntityMetadata metadata) {
        final @NonNull JsonTable table = metadata.table();
        return ReflectionClass.stream(IDittoModel.class.getPackage().getName(), IDittoModel.class,
                                      ReflectionClass.publicClass().and(getClassInfoPredicate(metadata)))
                              .findFirst()
                              .orElseThrow(() -> new IllegalArgumentException("Not found sync model of " + table));
    }

    static IDittoModel<VertxPojo> create(ClassGraphCache<EntityMetadata, IDittoModel> cache,
                                         @NonNull EntityMetadata metadata, @NonNull VertxPojo data) {
        Class<IDittoModel> clazz = Objects.isNull(cache) ? find(metadata) : cache.get(metadata);
        return ReflectionClass.createObject(clazz, Collections.singletonMap(metadata.modelClass(), data));
    }

    static IDittoModel<VertxPojo> create(ClassGraphCache<EntityMetadata, IDittoModel> cache,
                                         @NonNull EntityMetadata metadata, @NonNull JsonObject data) {
        return create(cache, metadata, metadata.parseFromRequest(data));
    }

    static Predicate<ClassInfo> getClassInfoPredicate(@NonNull EntityMetadata metadata) {
        return clazz -> clazz.getTypeSignature()
                             .getSuperclassSignature()
                             .getTypeArguments()
                             .stream()
                             .map(TypeArgument::getTypeSignature)
                             .filter(ClassRefTypeSignature.class::isInstance)
                             .map(ClassRefTypeSignature.class::cast)
                             .anyMatch(s -> ReflectionClass.assertDataType(metadata.modelClass(), s.loadClass()));
    }

    /**
     * Ditto endpoint
     *
     * @param thingId thing id
     * @return Ditto URL endpoint
     */
    @NonNull String endpoint(String thingId);

    /**
     * Ditto Request data
     *
     * @return request data
     */
    JsonObject toJson();

    @RequiredArgsConstructor
    abstract class AbstractDittoModel<V extends VertxPojo> implements IDittoModel<V> {

        @NonNull
        private final V data;

        @Override
        public final V get() {
            return data;
        }

        @Override
        public final @NonNull String endpoint(String thingId) {
            return Strings.format("/api/2/" + endpointPattern(), Strings.requireNotBlank(thingId).toLowerCase());
        }

        @Override
        public JsonObject toJson() {
            if (get() instanceof HasSyncAudit) {
                return JsonPojo.from(((HasSyncAudit) get()).setSyncAudit(null)).toJson();
            }
            return JsonPojo.from(get()).toJson();
        }

        @NonNull
        abstract String endpointPattern();

    }

}
