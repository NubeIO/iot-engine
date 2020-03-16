package com.nubeiot.edge.installer;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.register.EventHttpServiceRegister;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.installer.loader.ModuleTypeRule;
import com.nubeiot.edge.installer.service.AppDeployer;
import com.nubeiot.edge.installer.service.AppDeploymentWorkflow;
import com.nubeiot.edge.installer.service.InstallerService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class InstallerVerticle<T extends InstallerService> extends ContainerVerticle {

    @Getter
    private InstallerEntityHandler entityHandler;
    private MicroContext microContext;

    @Override
    public void start() {
        super.start();
        final InstallerConfig installerConfig = IConfig.from(nubeConfig.getAppConfig(), InstallerConfig.class);
        installerConfig.getRepoConfig().recomputeLocal(nubeConfig.getDataDir());
        final ModuleTypeRule moduleRule = getModuleRuleProvider().get();
        this.addSharedData(InstallerEntityHandler.SHARED_INSTALLER_CFG, installerConfig)
            .addSharedData(InstallerEntityHandler.SHARED_MODULE_RULE, moduleRule)
            .addSharedData(InstallerEntityHandler.SHARED_APP_DEPLOYER_CFG, appDeployer())
            .addProvider(new SqlProvider<>(entityHandlerClass()), this::sqlHandler)
            .addProvider(new MicroserviceProvider(), ctx -> microContext = (MicroContext) ctx)
            .registerSuccessHandler(v -> publishApis(microContext).flatMap(r -> deployAppModules()).subscribe(r -> {
                logger.info("Trigger deploying {} app modules successfully", r.size());
                if (logger.isDebugEnabled()) {
                    logger.debug(r);
                }
            }, logger::error));
    }

    @NonNull
    protected abstract Class<? extends InstallerEntityHandler> entityHandlerClass();

    @NonNull
    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    @NonNull
    protected abstract AppDeployer appDeployer();

    @NonNull
    protected abstract Supplier<Set<T>> services(@NonNull InstallerEntityHandler handler);

    private Single<JsonArray> deployAppModules() {
        final AppDeploymentWorkflow workflow = new AppDeploymentWorkflow(entityHandler);
        return entityHandler.getModulesWhenBootstrap()
                            .flatMapObservable(modules -> workflow.process(modules, entityHandler.getBootstrap()))
                            .collectInto(new JsonArray(), JsonArray::add);
    }

    private void sqlHandler(SqlContext sqlContext) {
        entityHandler = ((InstallerEntityHandler) sqlContext.getEntityHandler()).initDeployer();
    }

    private Single<List<Record>> publishApis(MicroContext microContext) {
        return EventHttpServiceRegister.<T>builder().vertx(vertx)
                                                    .sharedKey(getSharedKey())
                                                    .eventServices(services(entityHandler))
                                                    .build()
                                                    .publish(microContext.getLocalController());
    }

}
