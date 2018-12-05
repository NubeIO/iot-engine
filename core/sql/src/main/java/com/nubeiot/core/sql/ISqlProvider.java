package com.nubeiot.core.sql;

import java.util.function.Supplier;

import com.nubeiot.core.component.IComponentProvider;
import com.nubeiot.core.utils.Configs;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface ISqlProvider extends IComponentProvider<SQLWrapper> {

    static SQLWrapper initConfig(Vertx vertx, JsonObject allConfig, Supplier<Single<JsonObject>> initData) {
        JsonObject defSqlCfg = Configs.getApplicationCfg(Configs.loadDefaultConfig("sql.json"))
                                      .getJsonObject(SQLWrapper.SQL_CFG_NAME, new JsonObject());
        JsonObject inputSqlCfg = Configs.getApplicationCfg(allConfig)
                                        .getJsonObject(SQLWrapper.SQL_CFG_NAME, new JsonObject());
        return new SQLWrapper(vertx, defSqlCfg.mergeIn(inputSqlCfg, true), initData);
    }

}
