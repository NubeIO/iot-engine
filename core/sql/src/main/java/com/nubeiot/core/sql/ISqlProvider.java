package com.nubeiot.core.sql;

import org.jooq.Catalog;

import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.component.IComponentProvider;

public interface ISqlProvider extends IComponentProvider {

    static <T extends EntityHandler> SQLWrapper<T> create(Vertx vertx, NubeConfig nubeConfig, Catalog catalog,
                                                          Class<T> entityHandlerCreator) {
        SqlConfig sqlConfig = IComponentProvider.computeConfig("sql.json", SqlConfig.class, nubeConfig);
        return new SQLWrapper<>(catalog, entityHandlerCreator);
    }

}
