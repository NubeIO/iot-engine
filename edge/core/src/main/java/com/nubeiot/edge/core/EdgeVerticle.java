package com.nubeiot.edge.core;

import java.util.function.Supplier;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.sql.SQLWrapper;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.DefaultCatalog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EdgeVerticle extends ContainerVerticle {

    static final String SHARED_EVENTBUS = "module_loader";
    @Getter
    private ModuleTypeRule moduleRule;
    @Getter
    private EdgeEntityHandler entityHandler;
    @Getter
    private EventController eventController;

    @Override
    public void start() {
        super.start();
        this.moduleRule = this.getModuleRuleProvider().get();
        this.eventController = registerEventBus(new EventController(vertx));
        this.addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, entityHandlerClass()), this::handler);
        this.addSharedData(SHARED_EVENTBUS, this.eventController);
    }

    private void handler(SQLWrapper component) {
        this.entityHandler = ((EdgeEntityHandler) component.getEntityHandler());
    }

    protected abstract EventController registerEventBus(EventController controller);

    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    protected abstract Class<? extends EdgeEntityHandler> entityHandlerClass();

}
