package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.edge.connector.bacnet.ObjectTypeCategory;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ThingMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;
import com.nubeiot.edge.module.datapoint.model.pojos.PointThingComposite;
import com.nubeiot.iotdata.dto.PointPropertyMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;
import com.nubeiot.iotdata.edge.model.tables.pojos.Thing;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

/**
 * Represents for Ba cnet point translator.
 *
 * @see <a href="http://www.bacnet.org/Bibliography/ES-7-96/ES-7-96.htm">BACnet-Objects, Properties and Services</a>
 *     <a href="https://www.controlscourse.com/bacnet-objects/">Bacnet objects</a>
 * @since 1.0.0
 */
public final class BACnetPointTranslator implements BACnetTranslator<PointThingComposite, PropertyValuesMixin>,
                                                    IoTEntityTranslator<PointThingComposite, PropertyValuesMixin> {

    static final List<PropertyIdentifier> THING_PROPS = Collections.singletonList(PropertyIdentifier.deviceType);
    static final Set<PropertyIdentifier> POINT_PROPS = Stream.of(THING_PROPS, BACnetPointValueTranslator.DATA_PROPS)
                                                             .flatMap(Collection::stream)
                                                             .collect(Collectors.toSet());

    @Override
    public PointThingComposite serialize(PropertyValuesMixin mixin) {
        if (Objects.isNull(mixin) || Objects.isNull(mixin.getObjectId())) {
            throw new IllegalArgumentException("Invalid object properties. Cannot convert to persistence data");
        }
        if (ObjectTypeCategory.isPoint(mixin.getObjectId())) {
            throw new IllegalArgumentException(
                "Unable convert object type " + mixin.getObjectId().getObjectType() + " to persistence Point object");
        }
        final PointThingComposite pojo = new PointThingComposite();
        final Optional<ObjectType> objectType = mixin.getAndCast(PropertyIdentifier.objectType);
        final Optional<EngineeringUnits> units = mixin.getAndCast(PropertyIdentifier.units);
        final PointPropertyMetadata properties = new BACnetPointPropertyMetadataTranslator().serialize(
            BACnetPointPropertyMetadata.builder()
                                       .objectType(objectType.orElse(null))
                                       .units(units.orElse(null))
                                       .build());
        if (Objects.nonNull(properties.thingType())) {
            final Thing thing = new Thing().setType(properties.thingType())
                                           .setCategory(properties.thingCategory())
                                           .setMeasureUnit(properties.unit().type())
                                           .setMetadata(mixin.viewByProperties(THING_PROPS).toJson());
            pojo.put(ThingMetadata.INSTANCE.singularKeyName(), thing);
        }
        final PointValueData data = new BACnetPointValueTranslator().serialize(
            mixin.viewByProperties(BACnetPointValueTranslator.DATA_PROPS));
        final Point point = new Point().setCode(ObjectIdentifierMixin.serialize(mixin.getObjectId()))
                                       .setProtocol(protocol())
                                       .setKind(properties.pointKind())
                                       .setType(properties.pointType())
                                       .setMeasureUnit(properties.unit().type())
                                       .setMetadata(mixin.viewWithoutProperties(POINT_PROPS).toJson());
        return pojo.put(PointMetadata.INSTANCE.singularKeyName(),
                        new PointComposite().wrap(point).put(PointValueMetadata.INSTANCE.singularKeyName(), data));
    }

    @Override
    public PropertyValuesMixin deserialize(PointThingComposite concept) {
        //TODO implement it
        return null;
    }

    @Override
    public Class<PointThingComposite> fromType() {
        return PointThingComposite.class;
    }

    @Override
    public Class<PropertyValuesMixin> toType() {
        return PropertyValuesMixin.class;
    }

}
