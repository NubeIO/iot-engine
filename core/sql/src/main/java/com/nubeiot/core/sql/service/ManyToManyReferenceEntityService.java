package com.nubeiot.core.sql.service;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.ManyToManyEntityTransformer;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.sql.validation.CompositeValidation;

import lombok.NonNull;

/**
 * Represents service that holds a {@code resource} has one or more {@code reference} to other resources. It presents
 * many-to-one relationship.
 *
 * @param <P> Composite pojo
 * @param <M> Composite Metadata Type
 */
public interface ManyToManyReferenceEntityService<P extends CompositePojo, M extends CompositeMetadata>
    extends OneToManyReferenceEntityService<P, M>, ManyToManyResource {

    /**
     * Represents physical database entity
     *
     * @return physical entity metadata
     * @apiNote It represents for a joining table in many-to-many relationship
     */
    @Override
    @NonNull M context();

    /**
     * Composite validation
     *
     * @return composite validation
     */
    @Override
    @NonNull CompositeValidation validation();

    /**
     * Represents logical database entity
     *
     * @return logical entity metadata
     * @apiNote Represents one reference table in {@code many-to-many} relationship that is actual {@code service
     *     resource context}
     */
    @NonNull EntityMetadata reference();

    /**
     * Represents presentation resource of service
     *
     * @return presentation entity metadata
     * @apiNote Represents one reference table in {@code many-to-many} relationship that is {@code service resource
     *     presentation}
     */
    @NonNull EntityMetadata resource();

    @Override
    @SuppressWarnings("unchecked")
    default @NonNull ComplexQueryExecutor<P> queryExecutor() {
        return entityHandler().complexQuery()
                              .from(context())
                              .context(reference())
                              .with(resource())
                              .references(transformer().ref().entityReferences());
    }

    @Override
    @NonNull ManyToManyEntityTransformer transformer();

}
