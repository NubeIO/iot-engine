package com.nubeiot.core.sql.service;

import java.util.Set;
import java.util.stream.Stream;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.sql.validation.CompositeValidation;

import lombok.NonNull;

/**
 * Represents service that holds a {@code resource} has one or more {@code reference} to other resources. It presents
 * many-to-one relationship.
 *
 * @param <M> Composite Metadata Type
 * @param <V> Composite Entity Validation
 */
public interface ManyToOneReferenceEntityService<P extends CompositePojo, M extends CompositeMetadata,
                                                    V extends CompositeValidation>
    extends OneToManyReferenceEntityService<P, M, V> {

    <K, X extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, X, K>> EntityMetadata<K, X, R, D> reference();

    @Override
    @NonNull M metadata();

    @Override
    @NonNull V validation();

    @Override
    @NonNull ManyToOneEntityTransformer transformer();

    @Override
    @SuppressWarnings("unchecked")
    default @NonNull ComplexQueryExecutor<P> queryExecutor() {
        return entityHandler().complexQuery().from(metadata()).with(reference());
    }

    @Override
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        return recompute(requestData, new JsonObject().put(metadata().jsonKeyName(),
                                                           JsonData.checkAndConvert(metadata().parseKey(requestData))));
    }

    interface ManyToOneEntityTransformer extends ReferenceEntityTransformer {

        CompositeMetadata metadata();

        EntityMetadata reference();

        @Override
        default Set<String> ignoreFields(@NonNull RequestData requestData) {
            return Stream.of(metadata().requestKeyName(), reference().requestKeyName())
                         .collect(() -> ReferenceEntityTransformer.super.ignoreFields(requestData), Set::add,
                                  Set::addAll);
        }

    }

}
