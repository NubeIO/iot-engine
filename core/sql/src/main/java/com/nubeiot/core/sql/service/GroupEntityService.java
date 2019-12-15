package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.GroupEntityTransformer;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.GroupQueryExecutor;

import lombok.NonNull;

/**
 * Represents for {@code Group Entity} service based on eventbus listener.
 *
 * @param <P>  Type of {@code VertxPojo}
 * @param <M>  Type of {@code EntityMetadata}
 * @param <CP> Type of {@code CompositePojo}
 * @param <CM> Type of {@code CompositeMetadata}
 * @see CompositePojo
 * @see CompositeMetadata
 * @see EntityService
 * @see GroupReferenceMarker
 * @since 1.0.0
 */
public interface GroupEntityService<P extends VertxPojo, M extends EntityMetadata, CP extends CompositePojo<P, CP>,
                                       CM extends CompositeMetadata>
    extends BaseEntityService<M>, GroupReferenceMarker {

    /**
     * Declares context group pojo.
     *
     * @return context group pojo
     * @see CompositePojo
     * @since 1.0.0
     */
    @NonNull CM contextGroup();

    /**
     * Declares group query executor.
     *
     * @return group query executor
     * @see GroupQueryExecutor
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default GroupQueryExecutor<P, CP> groupQuery() {
        return GroupQueryExecutor.create(entityHandler(), context(), contextGroup(), this);
    }

    /**
     * Declares group entity transformer.
     *
     * @return group entity transformer
     * @see GroupEntityTransformer
     * @since 1.0.0
     */
    @NonNull GroupEntityTransformer transformer();

}
