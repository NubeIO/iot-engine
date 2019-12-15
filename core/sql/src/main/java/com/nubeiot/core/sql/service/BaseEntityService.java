package com.nubeiot.core.sql.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

interface BaseEntityService<M extends EntityMetadata> {

    /**
     * Entity handler
     *
     * @return entity handler
     */
    @NonNull EntityHandler entityHandler();

    /**
     * Context entity metadata
     *
     * @return entity metadata
     * @see EntityMetadata
     */
    @NonNull M context();

}
