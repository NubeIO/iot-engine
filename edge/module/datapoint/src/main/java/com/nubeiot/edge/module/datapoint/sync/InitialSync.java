package com.nubeiot.edge.module.datapoint.sync;

import io.reactivex.Maybe;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.transport.ProxyService;
import com.nubeiot.core.transport.Transporter;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

public interface InitialSync<T extends Transporter> extends ProxyService<T> {

    InitialSync EMPTY = new InitialSync() {
        @Override
        public Transporter transporter() { return null; }

        @Override
        public Maybe<JsonObject> sync(@NonNull Device device) { return Maybe.empty(); }
    };

    Maybe<JsonObject> sync(@NonNull Device device);

}
