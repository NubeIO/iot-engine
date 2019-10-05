package com.nubeiot.edge.core;

import java.util.function.Supplier;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.DefaultCatalog;
import com.nubeiot.edge.core.service.ModuleLoader;
import com.nubeiot.edge.core.service.PostDeploymentService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class InstallerVerticle extends ContainerVerticle {

    @Getter
    private ModuleTypeRule moduleRule;
    @Getter
    private InstallerEntityHandler entityHandler;

    @Override
    public void start() {
        super.start();
        final InstallerConfig installerConfig = IConfig.from(nubeConfig.getAppConfig(), InstallerConfig.class);
        installerConfig.getRepoConfig().recomputeLocal(nubeConfig.getDataDir());
        this.moduleRule = this.getModuleRuleProvider().get();
        this.addSharedData(InstallerEntityHandler.SHARED_INSTALLER_CFG, installerConfig)
            .addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, entityHandlerClass()), this::handler);
    }

    @Override
    public void registerEventbus(EventController eventClient) {
        eventClient.register(deploymentEvent(), new ModuleLoader(vertx, getSharedKey(), postDeploymentEvent()))
                   .register(postDeploymentEvent(), new PostDeploymentService(getEntityHandler()));
    }

    private void handler(SqlContext component) {
        this.entityHandler = (InstallerEntityHandler) component.getEntityHandler();
    }

    @NonNull
    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    @NonNull
    protected abstract Class<? extends InstallerEntityHandler> entityHandlerClass();

    @NonNull
    protected abstract EventModel deploymentEvent();

    @NonNull
    protected abstract EventModel postDeploymentEvent();

}
