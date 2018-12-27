package com.nubeiot.core.sql;

import java.util.function.Supplier;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.component.IComponentProvider;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface ISqlProvider extends IComponentProvider {

    static SQLWrapper create(Vertx vertx, NubeConfig nubeConfig, Supplier<Single<JsonObject>> initData) {
        SqlConfig sqlConfig = IComponentProvider.computeConfig("sql.json", SqlConfig.class, nubeConfig);
        return new SQLWrapper(vertx.getDelegate(), sqlConfig, initData);
    }

}
