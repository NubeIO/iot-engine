package com.nubeiot.edge.module.datapoint.service;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.HistoryDataMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.dto.HistorySettingType;
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
    public String servicePath() {
        return "/histories";
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

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        RequestData reqData = onCreatingOneResource(requestData);
        return doInsert(reqData).flatMap(pk -> doLookupByPrimaryKey(pk).map(pojo -> new SimpleEntry<>(pk, pojo)))
                                .doOnSuccess(
                                    j -> asyncPostService().onSuccess(this, EventAction.CREATE, j.getValue(), reqData))
                                .doOnError(t -> asyncPostService().onError(this, EventAction.CREATE, t))
                                .flatMap(resp -> transformer().afterCreate(resp.getKey(), resp.getValue(), reqData));
    }

    @Override
    protected Single<?> doInsert(@NonNull RequestData reqData) {
        final PointHistoryData p = (PointHistoryData) validation().onCreating(reqData);
        return validateReferenceEntity(reqData).flatMapSingle(b -> findSetting(p))
                                               .flatMap(history -> queryExecutor().insertReturningPrimary(p, reqData));
    }

    private Single<Boolean> findSetting(PointHistoryData historyData) {
        final JsonObject filter = new JsonObject().put(context().table().POINT.getName(),
                                                       JsonData.checkAndConvert(historyData.getPoint()));
        final Sort sort = Sort.builder().item(context().table().TIME.getName(), SortType.DESC).build();
        return queryExecutor().executeAny(queryExecutor().queryBuilder().viewOne(filter, sort))
                              .map(PointHistoryData.class::cast)
                              .onErrorReturnItem(new PointHistoryData())
                              .filter(p -> Objects.nonNull(p.getPoint()))
                              .flatMap(p -> entityHandler().dao(HistorySettingMetadata.INSTANCE.daoClass())
                                                           .findOneById(historyData.getPoint())
                                                           .filter(Optional::isPresent)
                                                           .map(Optional::get)
                                                           .filter(s -> s.getType() == HistorySettingType.COV &&
                                                                        Objects.nonNull(s.getTolerance()))
                                                           .map(s -> historyData.getValue()
                                                                                .compareTo(s.getTolerance()) >= 0))
                              .switchIfEmpty(Maybe.just(true))
                              .filter(b -> b)
                              .switchIfEmpty(Single.error(new IllegalArgumentException(
                                  "COV of point " + historyData.getPoint() + " doesn't meet requirement")));
    }

}
