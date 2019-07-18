package com.nubeiot.edge.connector.datapoint.model;

import com.nubeiot.iotdata.model.tables.interfaces.IPointRealtimeData;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DittoRealtimeData implements IDittoModel<IPointRealtimeData> {

    private final IPointRealtimeData realtimeData;

    @Override
    public String jqExpr() {
        return null;
    }

    @Override
    public IPointRealtimeData get() {
        return null;
    }

}
