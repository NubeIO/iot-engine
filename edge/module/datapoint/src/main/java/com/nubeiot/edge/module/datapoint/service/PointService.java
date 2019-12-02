package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.service.AbstractGroupEntityService;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.MeasureUnitMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;
import com.nubeiot.edge.module.datapoint.service.EdgeService.EdgeExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.unit.DataType;
import com.nubeiot.iotdata.unit.UnitAlias;

import lombok.NonNull;

public final class PointService
    extends AbstractGroupEntityService<Point, PointMetadata, PointComposite, PointCompositeMetadata>
    implements DataPointService<Point, PointMetadata> {

    public PointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public PointMetadata context() {
        return PointMetadata.INSTANCE;
    }

    @Override
    public PointCompositeMetadata contextGroup() {
        return PointCompositeMetadata.INSTANCE;
    }

    @Override
    public EntityReferences entityReferences() {
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
    public Single<JsonObject> onEach(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
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
    public Set<String> ignoreFields(@NonNull RequestData requestData) {
        final Set<String> ignoreFields = super.ignoreFields(requestData);
        ignoreFields.add(context().table().getJsonField(context().table().UNIT_ALIAS));
        return ignoreFields;
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Stream.concat(DataPointService.super.definitions().stream(),
                             EntityHttpService.createDefinitions(getAvailableEvents(), PointMetadata.INSTANCE,
                                                                 EdgeMetadata.INSTANCE, NetworkMetadata.INSTANCE)
                                              .stream()).collect(Collectors.toSet());
    }

    @Override
    protected RequestData recomputeRequestData(RequestData reqData, JsonObject extra) {
        EdgeExtension.optimizeReqData(entityHandler(), reqData, context().table().getJsonField(context().table().EDGE));
        DataPointIndex.NetworkMetadata.optimizeAlias(reqData.body());
        DataPointIndex.NetworkMetadata.optimizeAlias(reqData.getFilter());
        return super.recomputeRequestData(reqData, extra);
    }

    private JsonObject convertResource(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        PointComposite p = (PointComposite) pojo;
        final UnitAlias unitAlias = p.getUnitAlias();
        final JsonObject unit = p.safeGetOther(MeasureUnitMetadata.INSTANCE.singularKeyName(), MeasureUnit.class)
                                 .toJson();
        return JsonPojo.from(pojo)
                       .toJson(ignoreFields(requestData))
                       .put(MeasureUnitMetadata.INSTANCE.singularKeyName(), DataType.factory(unit, unitAlias).toJson());
    }

    public interface PointExtension extends HasReferenceResource {

        @Override
        default EntityReferences entityReferences() {
            return new EntityReferences().add(PointMetadata.INSTANCE, "point");
        }

    }

}
