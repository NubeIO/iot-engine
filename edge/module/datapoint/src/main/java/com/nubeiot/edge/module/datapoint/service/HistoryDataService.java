package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.Sort;
import com.nubeiot.core.dto.Sort.SortType;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.DesiredException;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.HistoryDataMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.dto.HistorySettingType;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;

import lombok.NonNull;

public final class HistoryDataService extends AbstractOneToManyEntityService<PointHistoryData, HistoryDataMetadata>
    implements PointExtension, DataPointService<PointHistoryData, HistoryDataMetadata> {

    public HistoryDataService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public HistoryDataMetadata context() {
        return HistoryDataMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE, EventAction.CREATE);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        final EventMethodDefinition definition = EventMethodDefinition.create("/point/:point_id/histories",
                                                                              "/:" + context().requestKeyName(),
                                                                              ActionMethodMapping.READ_MAP);
        return Stream.of(definition).collect(Collectors.toSet());
    }

    @Override
    public String servicePath() {
        return "/histories";
    }

    @Override
    protected Single<?> doInsert(@NonNull RequestData reqData) {
        final PointHistoryData his = (PointHistoryData) validation().onCreating(reqData);
        return validateReferenceEntity(reqData).flatMap(b -> isAbleToInsertByCov(his))
                                               .filter(b -> b)
                                               .switchIfEmpty(Single.error(new DesiredException(
                                                   "COV of point " + his.getPoint() +
                                                   " doesn't meet setting requirement")))
                                               .flatMap(b -> queryExecutor().insertReturningPrimary(his, reqData));
    }

    private Maybe<Boolean> isAbleToInsertByCov(PointHistoryData his) {
        final UUID point = his.getPoint();
        final JsonObject filter = new JsonObject().put(context().table().POINT.getName(),
                                                       JsonData.checkAndConvert(point));
        final Sort sort = Sort.builder().item(context().table().TIME.getName(), SortType.DESC).build();
        return getHistorySetting(point).filter(this::isTolerance)
                                       .flatMap(s -> getLastHistory(filter, sort).map(p -> validateCOV(his, p, s)))
                                       .defaultIfEmpty(true);
    }

    private Maybe<PointHistoryData> getLastHistory(JsonObject filter, Sort sort) {
        return queryExecutor().executeAny(queryExecutor().queryBuilder().viewOne(filter, sort))
                              .map(rr -> rr.fetchOptionalInto(PointHistoryData.class))
                              .filter(Optional::isPresent)
                              .map(Optional::get);
    }

    private boolean validateCOV(PointHistoryData his, PointHistoryData p, HistorySetting s) {
        return s.getTolerance().compareTo(Math.abs(his.getValue() - p.getValue())) < 0;
    }

    private boolean isTolerance(HistorySetting s) {
        return s.getType() == HistorySettingType.COV && Objects.nonNull(s.getTolerance());
    }

    private Maybe<HistorySetting> getHistorySetting(UUID pointId) {
        return entityHandler().dao(HistorySettingMetadata.INSTANCE.daoClass())
                              .findOneById(pointId)
                              .filter(Optional::isPresent)
                              .map(Optional::get);
    }

}
