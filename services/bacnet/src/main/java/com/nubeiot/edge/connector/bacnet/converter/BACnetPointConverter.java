package com.nubeiot.edge.connector.bacnet.converter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.edge.connector.bacnet.entity.BACnetPointEntity;
import com.nubeiot.edge.connector.bacnet.entity.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.iotdata.converter.IoTEntityConverter;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

import lombok.NonNull;

/**
 * Represents for Ba cnet point translator.
 *
 * @see <a href="http://www.bacnet.org/Bibliography/ES-7-96/ES-7-96.htm">BACnet-Objects, Properties and Services</a>
 *     <a href="https://www.controlscourse.com/bacnet-objects/">Bacnet objects</a>
 * @since 1.0.0
 */
public final class BACnetPointConverter
    implements BACnetProtocol, IoTEntityConverter<BACnetPointEntity, PropertyValuesMixin> {

    static final List<PropertyIdentifier> THING_PROPS = Collections.singletonList(PropertyIdentifier.deviceType);
    static final Set<PropertyIdentifier> POINT_PROPS = Stream.of(THING_PROPS, BACnetPointValueConverter.DATA_PROPS)
                                                             .flatMap(Collection::stream)
                                                             .collect(Collectors.toSet());

    @Override
    public BACnetPointEntity serialize(PropertyValuesMixin object) {
        return null;
    }

    @Override
    public PropertyValuesMixin deserialize(BACnetPointEntity concept) {
        return null;
    }

    @Override
    public @NonNull Class<BACnetPointEntity> fromType() {
        return BACnetPointEntity.class;
    }

    @Override
    public @NonNull Class<PropertyValuesMixin> toType() {
        return PropertyValuesMixin.class;
    }

    //    @Override
    //    public PointTransducerComposite serialize(PropertyValuesMixin mixin) {
    //        if (Objects.isNull(mixin) || Objects.isNull(mixin.getObjectId())) {
    //            throw new IllegalArgumentException("Invalid object properties. Cannot convert to persistence data");
    //        }
    //        if (ObjectTypeCategory.isPoint(mixin.getObjectId())) {
    //            throw new IllegalArgumentException(
    //                "Unable convert object type " + mixin.getObjectId().getObjectType() + " to persistence Point
    //                object");
    //        }
    //        final PointTransducerComposite pojo = new PointTransducerComposite();
    //        final Optional<ObjectType> objectType = mixin.getAndCast(PropertyIdentifier.objectType);
    //        final Optional<EngineeringUnits> units = mixin.getAndCast(PropertyIdentifier.units);
    //        final PointPropertyMetadata properties = new BACnetPointPropertyMetadataTranslator().serialize(
    //            BACnetPointPropertyMetadata.builder()
    //                                       .objectType(objectType.orElse(null))
    //                                       .units(units.orElse(null))
    //                                       .build());
    //        if (Objects.nonNull(properties.transducerType())) {
    //            final Transducer thing = new Transducer().setType(properties.transducerType())
    //                                                     .setCategory(properties.transducerCategory())
    //                                                     .setMeasureUnit(properties.unit().type())
    //                                                     .setMetadata(mixin.viewByProperties(THING_PROPS).toJson());
    //            pojo.put(TransducerMetadata.INSTANCE.singularKeyName(), thing);
    //        }
    //        final PointValueData data = new BACnetPointValueConverter().serialize(
    //            mixin.viewByProperties(BACnetPointValueConverter.DATA_PROPS));
    //        final Point point = new Point().setCode(ObjectIdentifierMixin.serialize(mixin.getObjectId()))
    //                                       .setProtocol(protocol())
    //                                       .setKind(properties.pointKind())
    //                                       .setType(properties.pointType())
    //                                       .setMeasureUnit(properties.unit().type())
    //                                       .setMetadata(mixin.viewWithoutProperties(POINT_PROPS).toJson());
    //        return pojo.put(PointMetadata.INSTANCE.singularKeyName(),
    //                        new PointComposite().wrap(point).put(PointValueMetadata.INSTANCE.singularKeyName(),
    //                        data));
    //    }
    //
    //    @Override
    //    public PropertyValuesMixin deserialize(PointTransducerComposite concept) {
    //        //TODO implement it
    //        return null;
    //    }
    //
    //    @Override
    //    public Class<PointTransducerComposite> fromType() {
    //        return PointTransducerComposite.class;
    //    }
    //
    //    @Override
    //    public Class<PropertyValuesMixin> toType() {
    //        return PropertyValuesMixin.class;
    //    }
}
