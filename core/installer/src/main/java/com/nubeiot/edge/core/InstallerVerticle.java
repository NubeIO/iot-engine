package com.nubeiot.edge.core;

import java.util.function.Supplier;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.DefaultCatalog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private void handler(SqlContext component) {
        this.entityHandler = (InstallerEntityHandler) component.getEntityHandler();
    }

    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    protected abstract Class<? extends InstallerEntityHandler> entityHandlerClass();

}
