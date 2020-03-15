package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Maybe;
import io.reactivex.Single;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestFilter;
import com.nubeiot.core.dto.Sort;
import com.nubeiot.core.dto.Sort.SortType;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.DesiredException;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractReferencingEntityService;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.step.CreationStep;
import com.nubeiot.core.sql.workflow.step.ModificationStep;
import com.nubeiot.edge.module.datapoint.DataPointIndex.HistoryDataMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.cache.DataCacheInitializer;
import com.nubeiot.edge.module.datapoint.cache.PointHistoryCache;
import com.nubeiot.edge.module.datapoint.service.extension.PointExtension;
import com.nubeiot.iotdata.dto.HistorySettingType;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;

import lombok.NonNull;

public final class HistoryDataService extends AbstractReferencingEntityService<PointHistoryData, HistoryDataMetadata>
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
        return EntityHttpService.createDefinitions(ActionMethodMapping.DQL_MAP, context(), PointMetadata.INSTANCE);
    }

    @Override
    public String servicePath() {
        return context().pluralKeyName();
    }

    @Override
    protected CreationStep initCreationStep() {
        return super.initCreationStep()
                    .onSuccess((action, keyPojo) -> putIntoCache((PointHistoryData) keyPojo.dbEntity()));
    }

    @Override
    protected ModificationStep initModificationStep(EventAction action) {
        return super.initModificationStep(action)
                    .onSuccess((reqData, event, keyPojo) -> putIntoCache((PointHistoryData) keyPojo.dbEntity()));
    }

    @Override
    protected OperationValidator initCreationValidator() {
        return super.initCreationValidator()
                    .andThen(OperationValidator.create((req, pojo) -> isAbleToInsertByCov((PointHistoryData) pojo)));
    }

    private Single<PointHistoryData> isAbleToInsertByCov(@NonNull PointHistoryData his) {
        final UUID point = his.getPoint();
        return getHistorySetting(point).filter(HistorySetting::getEnabled)
                                       .filter(this::isTolerance)
                                       .switchIfEmpty(Maybe.error(new DesiredException(
                                           "History setting point '" + point + "' is disabled or not COV type")))
                                       .flatMap(s -> getLastHistory(point).map(last -> checkCovExcess(his, last, s)))
                                       .switchIfEmpty(Single.just(his));
    }

    private Maybe<PointHistoryData> getLastHistory(@NonNull UUID point) {
        final PointHistoryCache cache = entityHandler().sharedData(DataCacheInitializer.HISTORIES_DATA_CACHE);
        final PointHistoryData his = cache.get(point);
        if (Objects.nonNull(his)) {
            logger.info("Last history of point {} from cache", point);
            return Maybe.just(his);
        }
        final RequestFilter filter = (RequestFilter) new RequestFilter().put(context().table().POINT.getName(),
                                                                             JsonData.checkAndConvert(point));
        final Sort sort = Sort.builder().item(context().table().TIME.getName(), SortType.DESC).build();
        return queryExecutor().executeAny(queryExecutor().queryBuilder().viewOne(filter, sort))
                              .map(rr -> rr.fetchOptionalInto(PointHistoryData.class))
                              .filter(Optional::isPresent)
                              .map(Optional::get)
                              .doOnSuccess(history -> cache.add(point, history));
    }

    private PointHistoryData checkCovExcess(@NonNull PointHistoryData his, @NonNull PointHistoryData last,
                                            @NonNull HistorySetting s) {
        if (Double.compare(Math.abs(his.getValue() - last.getValue()), s.getTolerance()) > 0) {
            return his;
        }
        throw new DesiredException(
            "COV of point " + his.getPoint() + " is " + his.getValue() + " that doesn't exceed setting " +
            s.getTolerance());
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

    private void putIntoCache(PointHistoryData his) {
        final PointHistoryCache cache = entityHandler().sharedData(DataCacheInitializer.HISTORIES_DATA_CACHE);
        cache.add(his.getPoint(), his);
    }

}
