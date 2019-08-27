package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.datapoint.model.pojos.ThingComposite;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.EquipThingMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.EquipmentMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.TransducerMetadata;
import com.nubeiot.iotdata.edge.model.tables.Thing;

import lombok.NonNull;

public final class TransducerByEquipment extends AbstractManyToManyEntityService<ThingComposite, EquipThingMetadata>
    implements DataPointService<ThingComposite, EquipThingMetadata> {

    public TransducerByEquipment(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public EquipThingMetadata context() {
        return EquipThingMetadata.INSTANCE;
    }

    @Override
    public EntityReferences entityReferences() {
        final @NonNull Thing table = context().table();
        return new EntityReferences().add(reference(), table.getJsonField(table.EQUIP))
                                     .add(resource(), table.getJsonField(table.TRANSDUCER));
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return EquipmentMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return TransducerMetadata.INSTANCE;
    }

}
