package com.nubeiot.edge.module.datapoint.sync;

import com.nubeiot.core.transport.Transporter;
import com.nubeiot.iotdata.edge.model.tables.pojos.Edge;

public interface InitialSyncTask<DC extends SyncDefinitionContext, T extends Transporter>
    extends SyncTask<DC, Edge, T> {}
