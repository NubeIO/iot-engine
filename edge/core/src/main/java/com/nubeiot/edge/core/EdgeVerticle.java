package com.nubeiot.edge.core;

import java.util.function.Supplier;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.sql.ISqlProvider;
import com.nubeiot.core.sql.SQLWrapper;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.DefaultCatalog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EdgeVerticle extends AbstractVerticle implements ISqlProvider {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ModuleLoader moduleLoader;
    private ModuleTypeRule moduleRule;
    private SQLWrapper<? extends EdgeEntityHandler> sqlWrapper;
    private EdgeEntityHandler entityHandler;
    private EventController eventController;
    private NubeConfig nubeConfig;

    @Override
    public final void start() throws Exception {
        this.nubeConfig = IConfig.from(config(), NubeConfig.class);
        this.moduleLoader = new ModuleLoader(vertx);
        this.moduleRule = this.getModuleRuleProvider().get();
        this.eventController = registerEventBus(new EventController(vertx));
        this.sqlWrapper = ISqlProvider.create(this.vertx, nubeConfig, DefaultCatalog.DEFAULT_CATALOG,
                                              entityHandlerClass());
        this.sqlWrapper.start();
        this.entityHandler = this.sqlWrapper.getEntityHandler().registerVerticle(this);
        super.start();
    }

    @Override
    public final void stop() {
        this.sqlWrapper.stop();
    }

    protected abstract EventController registerEventBus(EventController controller);

    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    protected abstract Class<? extends EdgeEntityHandler> entityHandlerClass();

}
