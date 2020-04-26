package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Maybe;
import io.reactivex.Single;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.decorator.RequestDecorator;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractReferencingEntityService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.utils.JsonUtils;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderMetadata;
import com.nubeiot.edge.module.datapoint.service.extension.EdgeExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.Folder;

import lombok.NonNull;

public final class FolderService extends AbstractReferencingEntityService<Folder, FolderMetadata>
    implements DataPointService<Folder, FolderMetadata> {

    public FolderService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public FolderMetadata context() {
        return FolderMetadata.INSTANCE;
    }

    @Override
    public boolean supportForceDeletion() {
        return true;
    }

    @Override
    public @NonNull EntityReferences referencedEntities() {
        return new EntityReferences().add(EdgeMetadata.INSTANCE);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Stream.concat(DataPointService.super.definitions().stream(),
                             EntityHttpService.createDefinitions(getAvailableEvents(), context(), EdgeMetadata.INSTANCE)
                                              .stream()).collect(Collectors.toSet());
    }

    @Override
    public @NonNull RequestDecorator requestDecorator() {
        return EdgeExtension.create(this);
    }

    @Override
    protected OperationValidator initCreationValidator() {
        return super.initCreationValidator().andThen(OperationValidator.create((reqData, pojo) -> {
            final String folderName = ((Folder) pojo).getName();
            final com.nubeiot.iotdata.edge.model.tables.@NonNull Folder table = context().table();
            return queryExecutor().fetchExists(table, table.NAME.eq(folderName))
                                  .flatMap(b -> Maybe.error(context().alreadyExisted(
                                      JsonUtils.kvMsg(table.getJsonField(table.NAME), folderName))))
                                  .switchIfEmpty(Single.just(false))
                                  .map(ignore -> pojo);
        }));
    }

}
