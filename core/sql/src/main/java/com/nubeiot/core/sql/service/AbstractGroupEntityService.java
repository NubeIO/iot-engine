package com.nubeiot.core.sql.service;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.GroupEntityTransformer;
import com.nubeiot.core.sql.decorator.GroupRequestDecorator;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.GroupQueryExecutor;
import com.nubeiot.core.sql.service.marker.GroupReferencingEntityMarker;

import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for the {@code Group entity}.
 *
 * @param <M>  Type of {@code EntityMetadata}
 * @param <CP> Type of {@code CompositePojo}
 * @param <CM> Type of {@code CompositeMetadata}
 * @see EntityMetadata
 * @see CompositePojo
 * @see CompositeMetadata
 * @see GroupEntityService
 * @see GroupRequestDecorator
 * @see GroupEntityTransformer
 * @since 1.0.0
 */
public abstract class AbstractGroupEntityService<M extends EntityMetadata, CP extends CompositePojo,
                                                    CM extends CompositeMetadata>
    extends AbstractReferencingEntityService<CP, CM>
    implements GroupEntityService<M, CP, CM>, GroupRequestDecorator, GroupEntityTransformer {

    /**
     * Instantiates a new Abstract group entity service.
     *
     * @param entityHandler the entity handler
     * @since 1.0.0
     */
    public AbstractGroupEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull GroupQueryExecutor<CP> queryExecutor() {
        return GroupEntityService.super.queryExecutor();
    }

    @Override
    public @NonNull GroupEntityTransformer transformer() {
        return this;
    }

    @Override
    public GroupReferencingEntityMarker marker() {
        return this;
    }

}
