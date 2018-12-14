package com.nubeiot.core.sql;

import java.util.function.Supplier;

import com.nubeiot.core.component.IComponentProvider;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface ISqlProvider extends IComponentProvider {

    static SQLWrapper create(Vertx vertx, JsonObject rootCfg, Supplier<Single<JsonObject>> initData) {
        JsonObject sqlCfg = IComponentProvider.computeConfig("sql.json", SQLWrapper.SQL_CFG_NAME, rootCfg);
        return new SQLWrapper(vertx, sqlCfg, initData);
    }

}
