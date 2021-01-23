package com.nubeiot.edge.module.datapoint.service.extension;

import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.core.sql.service.marker.OneToOneEntityMarker;
import com.nubeiot.core.sql.service.marker.ReferencingEntityMarker;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;

import lombok.NonNull;

public interface PointExtension extends ReferencingEntityMarker {

    @Override
    default EntityReferences referencedEntities() {
        return new EntityReferences().add(PointCompositeMetadata.INSTANCE,
                                          PointCompositeMetadata.INSTANCE.singularKeyName());
    }

    interface PointOneToOneExtension extends OneToOneEntityMarker, PointExtension {

        @Override
        @NonNull
        default EntityReferences dependantEntities() {
            return PointExtension.super.referencedEntities();
        }

        @Override
        @NonNull
        default EntityReferences referencedEntities() {
            return PointExtension.super.referencedEntities();
        }

    }

}
