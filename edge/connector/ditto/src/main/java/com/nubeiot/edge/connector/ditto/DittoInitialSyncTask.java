package com.nubeiot.edge.connector.ditto;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.workflow.task.EntityRuntimeContext;
import com.nubeiot.edge.connector.ditto.model.DittoEdge;
import com.nubeiot.edge.module.datapoint.sync.InitialSyncTask;
import com.nubeiot.iotdata.edge.model.tables.pojos.Edge;

import lombok.NonNull;

public final class DittoInitialSyncTask extends AbstractDittoTask<Edge>
    implements InitialSyncTask<DittoTaskContext, HttpClientDelegate> {

    public DittoInitialSyncTask(DittoTaskContext definitionContext) {
        super(definitionContext);
    }

    @Override
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityRuntimeContext<Edge> runtimeContext) {
        return Single.just(true);
    }

    @Override
    public @NonNull Maybe<JsonObject> execute(@NonNull EntityRuntimeContext<Edge> runtimeContext) {
        final DittoEdge ditto = new DittoEdge(runtimeContext.getData());
        return doSyncOnSuccess(runtimeContext.getMetadata(), ditto.creationEndpoint(thingId(definitionContext())),
                               createReqBody(ditto), runtimeContext.getData());
    }

    private JsonObject createReqBody(@NonNull DittoEdge pojo) {
        return new JsonObject().put("attributes", new JsonObject().put("extra", pojo.get().toJson()))
                               .put("features", new JsonObject().put("networks", new JsonObject())
                                                                .put("points", new JsonObject())
                                                                .put("histories", new JsonObject())
                                                                .put("realtime", new JsonObject())
                                                                .put("equipments", new JsonObject())
                                                                .put("transducers", new JsonObject()));
    }

}
