package com.nubeiot.edge.core;

import java.util.Set;
import java.util.function.Supplier;

import io.reactivex.Observable;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.register.EventHttpServiceRegister;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.DefaultCatalog;
import com.nubeiot.edge.core.service.AppDeployer;
import com.nubeiot.edge.core.service.InstallerService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class InstallerVerticle extends ContainerVerticle {

    public static final String SHARED_MODULE_RULE = "MODULE_RULE";
    static final String SHARED_INSTALLER_CFG = "INSTALLER_CFG";

    @Getter
    private InstallerEntityHandler entityHandler;
    private MicroContext microCtx;

    @Override
    public void start() {
        super.start();
        final InstallerConfig installerConfig = IConfig.from(nubeConfig.getAppConfig(), InstallerConfig.class);
        installerConfig.getRepoConfig().recomputeLocal(nubeConfig.getDataDir());
        final ModuleTypeRule moduleRule = getModuleRuleProvider().get();
        this.addSharedData(SHARED_INSTALLER_CFG, installerConfig)
            .addSharedData(SHARED_MODULE_RULE, moduleRule)
            .addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, entityHandlerClass()), this::handler)
            .addProvider(new MicroserviceProvider(), ctx -> microCtx = (MicroContext) ctx)
            .registerSuccessHandler(v -> publishApis(microCtx).flatMapSingle(r -> entityHandler.startAppModules())
                                                              .subscribe(logger::info, logger::error));
    }

    @NonNull
    protected abstract Class<? extends InstallerEntityHandler> entityHandlerClass();

    @NonNull
    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    @NonNull
    protected abstract Supplier<Set<? extends InstallerService>> services(@NonNull InstallerEntityHandler handler);

    @NonNull
    protected abstract AppDeployer appDeployer(@NonNull InstallerEntityHandler entityHandler);

    private void handler(SqlContext sqlContext) {
        this.entityHandler = (InstallerEntityHandler) sqlContext.getEntityHandler();
        appDeployer(entityHandler).register(entityHandler.eventClient());
    }

    private Observable<Record> publishApis(MicroContext c) {
        return new EventHttpServiceRegister<>(vertx.getDelegate(), getSharedKey(),
                                              services(entityHandler).get()).publish(c.getLocalController());
    }

}
