package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceEquipCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EquipmentMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.DeviceComposite;

import lombok.NonNull;

public final class EquipmentByDevice
    extends AbstractManyToManyEntityService<DeviceComposite, DeviceEquipCompositeMetadata>
    implements DataPointService<DeviceComposite, DeviceEquipCompositeMetadata> {

    public EquipmentByDevice(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public DeviceEquipCompositeMetadata context() {
        return DeviceEquipCompositeMetadata.INSTANCE;
    }

    @Override
    public EntityReferences entityReferences() {
        final @NonNull com.nubeiot.iotdata.edge.model.tables.DeviceEquip table = context().table();
        return new EntityReferences().add(reference(), table.getJsonField(table.DEVICE))
                                     .add(resource(), table.getJsonField(table.EQUIP));
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return DeviceMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return EquipmentMetadata.INSTANCE;
    }

    @Override
    public Set<String> ignoreFields(@NonNull RequestData requestData) {
        final Set<String> ignores = super.ignoreFields(requestData);
        ignores.add(context().table().getJsonField(context().table().EQUIP));
        return ignores;
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        return DataPointService.definitionsForMany(getAvailableEvents(), reference(), resource());
    }

}
