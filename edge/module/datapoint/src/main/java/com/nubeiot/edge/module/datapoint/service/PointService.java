package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.service.AbstractGroupEntityService;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.MeasureUnitMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.unit.DataType;
import com.nubeiot.iotdata.unit.UnitLabel;

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
    public RequestData recomputeRequestData(RequestData requestData, JsonObject extra) {
        DataPointIndex.NetworkMetadata.optimizeAlias(requestData.body());
        DataPointIndex.NetworkMetadata.optimizeAlias(requestData.getFilter());
        return super.recomputeRequestData(requestData, extra);
    }

    @Override
    public EntityReferences entityReferences() {
        return new EntityReferences().add(DeviceMetadata.INSTANCE, context().table().DEVICE.getName())
                                     .add(NetworkMetadata.INSTANCE, context().table().NETWORK.getName());
    }

    @Override
    public EntityReferences groupReferences() {
        return new EntityReferences().add(MeasureUnitMetadata.INSTANCE, context().table().MEASURE_UNIT.getName());
    }

    @Override
    protected Single<PointComposite> doGetOne(RequestData reqData) {
        return groupQuery().findOneByKey(reqData);
    }

    @Override
    public @NonNull JsonObject afterEachList(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(showGroupFields(requestData));
    }

    @Override
    public @NonNull JsonObject afterGet(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        PointComposite p = (PointComposite) pojo;
        final UnitLabel unitLabel = p.getMeasureUnitLabel();
        final JsonObject unit = p.safeGetOther(MeasureUnitMetadata.INSTANCE.singularKeyName(), MeasureUnit.class)
                                 .toJson();
        DataType dt = DataType.factory(unit, unitLabel);
        return JsonPojo.from(pojo)
                       .toJson(ignoreFields(requestData))
                       .put(MeasureUnitMetadata.INSTANCE.singularKeyName(), dt.toJson());
    }

    @Override
    public Set<String> ignoreFields(@NonNull RequestData requestData) {
        final Set<String> ignoreFields = super.ignoreFields(requestData);
        ignoreFields.add(context().table().getJsonField(context().table().MEASURE_UNIT_LABEL));
        return ignoreFields;
    }

    public interface PointExtension extends HasReferenceResource {

        @Override
        default EntityReferences entityReferences() {
            return new EntityReferences().add(PointMetadata.INSTANCE, "point");
        }

    }

}
