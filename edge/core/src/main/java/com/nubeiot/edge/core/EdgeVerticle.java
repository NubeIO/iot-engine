package com.nubeiot.edge.core;

import java.util.function.Supplier;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.sql.SQLWrapper;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.DefaultCatalog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EdgeVerticle extends ContainerVerticle {

    private ModuleLoader moduleLoader;
    private ModuleTypeRule moduleRule;
    private EdgeEntityHandler entityHandler;
    private EventController eventController;

    @Override
    public final void start() {
        super.start();
        this.moduleLoader = new ModuleLoader(vertx);
        this.moduleRule = this.getModuleRuleProvider().get();
        this.eventController = registerEventBus(new EventController(vertx));
        this.addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, entityHandlerClass()), this::handler);
    }

    private void handler(SQLWrapper component) {
        this.entityHandler = ((EdgeEntityHandler) component.getEntityHandler()).registerVerticle(this);
    }

    protected abstract EventController registerEventBus(EventController controller);

    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    protected abstract Class<? extends EdgeEntityHandler> entityHandlerClass();

}
