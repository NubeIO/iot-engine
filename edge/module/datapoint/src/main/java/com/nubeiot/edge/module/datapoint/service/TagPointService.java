package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.BigSerialKeyEntity;
import com.nubeiot.core.sql.HasReferenceEntityService;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.PointTagDao;
import com.nubeiot.iotdata.model.tables.pojos.PointTag;
import com.nubeiot.iotdata.model.tables.records.PointTagRecord;

import lombok.NonNull;

public final class TagPointService extends AbstractDataPointService<Long, PointTag, PointTagRecord, PointTagDao>
    implements BigSerialKeyEntity<PointTag, PointTagRecord, PointTagDao>,
               HasReferenceEntityService<Long, PointTag, PointTagRecord, PointTagDao>, PointExtension {

    public TagPointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @NonNull
    public String listKey() {
        return "tags";
    }

    @Override
    public @NonNull Class<PointTag> modelClass() {
        return PointTag.class;
    }

    @Override
    public @NonNull Class<PointTagDao> daoClass() {
        return PointTagDao.class;
    }

    @Override
    public @NonNull JsonTable<PointTagRecord> table() {
        return Tables.POINT_TAG;
    }

    @Override
    public @NonNull String requestKeyName() {
        return "tag_" + jsonKeyName();
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Stream.concat(super.definitions().stream(),
                             Stream.of(EventMethodDefinition.createDefault("/point/:point_id/tags", "tag_id")))
                     .collect(Collectors.toSet());
    }

    @Override
    public String servicePath() {
        return "/tags";
    }

}
