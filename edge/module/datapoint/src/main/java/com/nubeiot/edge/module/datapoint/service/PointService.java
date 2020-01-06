package com.nubeiot.edge.module.datapoint.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.service.AbstractGroupEntityService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.core.sql.service.marker.ReferencingEntityMarker;
import com.nubeiot.core.sql.workflow.task.EntityTask;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.MeasureUnitMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;
import com.nubeiot.edge.module.datapoint.service.EdgeService.EdgeExtension;
import com.nubeiot.edge.module.datapoint.service.NetworkService.NetworkExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.unit.DataType;
import com.nubeiot.iotdata.unit.UnitAlias;

import lombok.NonNull;

public final class PointService
    extends AbstractGroupEntityService<PointMetadata, PointComposite, PointCompositeMetadata>
    implements DataPointService<PointComposite, PointCompositeMetadata> {

    public PointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public PointCompositeMetadata context() {
        return PointCompositeMetadata.INSTANCE;
    }

    @Override
    public PointMetadata rawContext() {
        return PointMetadata.INSTANCE;
    }

    @Override
    public EntityReferences referencedEntities() {
        final com.nubeiot.iotdata.edge.model.tables.Point table = context().table();
        return new EntityReferences().add(EdgeMetadata.INSTANCE, table.getJsonField(table.EDGE))
                                     .add(NetworkMetadata.INSTANCE, table.getJsonField(table.NETWORK));
    }

    @Override
    public EntityReferences groupReferences() {
        return new EntityReferences().add(MeasureUnitMetadata.INSTANCE,
                                          context().table().getJsonField(context().table().MEASURE_UNIT));
    }

    @Override
    public Set<String> ignoreFields() {
        return Stream.of(super.ignoreFields(),
                         Collections.singletonList(context().table().getJsonField(context().table().UNIT_ALIAS)))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    @Override
    public EntityTask postPersistTask() {
        return new PointReferencedService(entityHandler());
    }

    @Override
    protected RequestData recomputeRequestData(RequestData reqData, JsonObject extra) {
        EdgeExtension.optimizeReqData(entityHandler(), reqData, context().table().getJsonField(context().table().EDGE));
        NetworkExtension.optimizeAlias(entityHandler(), reqData);
        return super.recomputeRequestData(reqData, extra);
    }

    @Override
    public Single<JsonObject> afterEach(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return Single.just(JsonPojo.from(pojo).toJson(showGroupFields(requestData)));
    }

    @Override
    public Single<JsonObject> afterGet(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return Single.just(convertResource(pojo, requestData));
    }

    @Override
    public Single<JsonObject> afterCreate(Object key, @NonNull VertxPojo pojo, @NonNull RequestData reqData) {
        return Single.just(doTransform(EventAction.CREATE, key, pojo, reqData, this::convertResource));
    }

    @Override
    public @NonNull Single<JsonObject> afterUpdate(Object key, @NonNull VertxPojo pojo, @NonNull RequestData reqData) {
        return Single.just(doTransform(EventAction.UPDATE, key, pojo, reqData, this::convertResource));
    }

    @Override
    public Single<JsonObject> afterPatch(Object key, @NonNull VertxPojo pojo, @NonNull RequestData reqData) {
        return Single.just(doTransform(EventAction.PATCH, key, pojo, reqData, this::convertResource));
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Stream.of(DataPointService.super.definitions(),
                         EntityHttpService.createDefinitions(getAvailableEvents(), context(), EdgeMetadata.INSTANCE,
                                                             NetworkMetadata.INSTANCE))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    private JsonObject convertResource(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        final PointComposite p = (PointComposite) pojo;
        final UnitAlias unitAlias = p.getUnitAlias();
        final JsonObject unit = p.safeGetOther(MeasureUnitMetadata.INSTANCE.singularKeyName(), MeasureUnit.class)
                                 .toJson();
        return JsonPojo.from(pojo)
                       .toJson(ignoreFields(requestData))
                       .put(MeasureUnitMetadata.INSTANCE.singularKeyName(), DataType.factory(unit, unitAlias).toJson());
    }

    public interface PointExtension extends ReferencingEntityMarker {

        static Set<EventMethodDefinition> oneToOneDefinitions(@NonNull Collection<EventAction> availableEvents,
                                                              @NonNull Supplier<String> servicePath,
                                                              @NonNull Supplier<String> requestKeySupplier) {
            final ActionMethodMapping mapping = ActionMethodMapping.by(ActionMethodMapping.CRD_MAP, availableEvents);
            final EventMethodDefinition definition = EventMethodDefinition.create(
                Urls.combinePath(EntityHttpService.toCapturePath(PointMetadata.INSTANCE), servicePath.get()), mapping);
            return Stream.of(Collections.singleton(definition),
                             EntityHttpService.createDefinitions(mapping, servicePath, requestKeySupplier))
                         .flatMap(Collection::stream)
                         .collect(Collectors.toSet());
        }

        static RequestData createRequestData(@NonNull RequestData requestData, @NonNull JsonObject body) {
            final String pointId = requestData.body().getString(PointMetadata.INSTANCE.requestKeyName());
            return RequestData.builder()
                              .headers(requestData.headers())
                              .filter(requestData.filter())
                              .sort(requestData.sort())
                              .pagination(requestData.pagination())
                              .body(body.put(PointMetadata.INSTANCE.requestKeyName(), pointId))
                              .build();
        }

        @Override
        default EntityReferences referencedEntities() {
            return new EntityReferences().add(PointMetadata.INSTANCE, "point");
        }

    }

}
