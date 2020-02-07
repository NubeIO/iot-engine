package com.nubeiot.edge.connector.bacnet.translator;

import com.nubeiot.iotdata.dto.PointPropertyMetadata;

import lombok.NonNull;

public final class BACnetPointPropertyMetadataTranslator
    implements BACnetTranslator<PointPropertyMetadata, BACnetPointPropertyMetadata> {

    @Override
    public PointPropertyMetadata serialize(BACnetPointPropertyMetadata metadata) {
        return PointPropertyMetadata.builder()
                                    .pointKind(new BACnetPointKindTranslator().serialize(metadata.objectType()))
                                    .pointType(new BACnetPointTypeTranslator().serialize(metadata.objectType()))
                                    .transducerType(new BACnetThingTypeTranslator().serialize(metadata.objectType()))
                                    .build();
    }

    @Override
    public BACnetPointPropertyMetadata deserialize(PointPropertyMetadata concept) {
        return null;
    }

    @Override
    public @NonNull Class<PointPropertyMetadata> fromType() {
        return PointPropertyMetadata.class;
    }

    @Override
    public @NonNull Class<BACnetPointPropertyMetadata> toType() {
        return BACnetPointPropertyMetadata.class;
    }

}
