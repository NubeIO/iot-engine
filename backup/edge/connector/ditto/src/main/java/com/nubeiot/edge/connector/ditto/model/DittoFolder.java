package com.nubeiot.edge.connector.ditto.model;

import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IFolder;

import lombok.NonNull;

public final class DittoFolder extends AbstractDittoModel<IFolder> {

    public DittoFolder(@NonNull IFolder data) {
        super(data);
    }

    @Override
    @NonNull String endpointPattern() {
        return null;
    }

}
