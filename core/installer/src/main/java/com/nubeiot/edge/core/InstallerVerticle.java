package com.nubeiot.edge.core;

import java.util.Set;
import java.util.function.Supplier;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.register.EventHttpServiceRegister;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.DefaultCatalog;
import com.nubeiot.edge.core.service.DeployerDefinition;
import com.nubeiot.edge.core.service.InstallerService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class InstallerVerticle extends ContainerVerticle {

    public static final String SHARED_MODULE_RULE = "MODULE_RULE";

    @Getter
    private ModuleTypeRule moduleRule;
    @Getter
    private InstallerEntityHandler entityHandler;

    @Override
    public void start() {
        super.start();
        final InstallerConfig installerConfig = IConfig.from(nubeConfig.getAppConfig(), InstallerConfig.class);
        installerConfig.getRepoConfig().recomputeLocal(nubeConfig.getDataDir());
        this.moduleRule = getModuleRuleProvider().get();
        this.addSharedData(InstallerEntityHandler.SHARED_INSTALLER_CFG, installerConfig)
            .addSharedData(SHARED_MODULE_RULE, moduleRule)
            .addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, entityHandlerClass()), this::handler)
            .addProvider(new MicroserviceProvider(), this::publishService);
    }

    @Override
    public final void registerEventbus(EventController eventClient) {
        deploymentService().register(eventClient);
    }

    private void publishService(MicroContext c) {
        new EventHttpServiceRegister<>(vertx.getDelegate(), getSharedKey(), services().get()).publish(
            c.getLocalController());
    }

    private void handler(SqlContext sqlContext) {
        this.entityHandler = (InstallerEntityHandler) sqlContext.getEntityHandler();
    }

    @NonNull
    protected abstract Class<? extends InstallerEntityHandler> entityHandlerClass();

    @NonNull
    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    @NonNull
    protected abstract Supplier<Set<? extends InstallerService>> services();

    @NonNull
    protected abstract DeployerDefinition deploymentService();

}
