package com.nubeiot.edge.connector.datapoint;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.iotdata.model.DefaultCatalog;

import lombok.Getter;

public class DataPointVerticle extends ContainerVerticle {

    @Getter
    private DataPointEntityHandler entityHandler;

    @Override
    public void start() {
        super.start();
        this.addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, DataPointEntityHandler.class),
                         this::handler);
    }

    @Override
    public void registerEventbus(EventController controller) {
        super.registerEventbus(controller);
    }

    private void handler(SqlContext component) {
        this.entityHandler = (DataPointEntityHandler) component.getEntityHandler();
    }

}
