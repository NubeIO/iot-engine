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
public abstract class EdgeVerticle extends ContainerVerticle {

    public static final String SHARED_DATA_DIR = "DATA_DIR";
    public static final String SHARED_INSTALLER_CFG = "INSTALLER_CFG";
    @Getter
    private ModuleTypeRule moduleRule;
    @Getter
    private EdgeEntityHandler entityHandler;
    @Getter
    private InstallerConfig installerConfig;

    @Override
    public void start() {
        super.start();
        this.installerConfig = IConfig.from(nubeConfig.getAppConfig(), InstallerConfig.class);
        this.installerConfig.getRepoConfig().recomputeLocal(nubeConfig.getDataDir());
        this.addSharedData(SHARED_DATA_DIR, this.nubeConfig.getDataDir().toString())
            .addSharedData(SHARED_INSTALLER_CFG, this.getInstallerConfig().toJson());
        this.moduleRule = this.getModuleRuleProvider().get();
        this.addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, entityHandlerClass()), this::handler);
    }

    private void handler(SqlContext component) {
        this.entityHandler = (EdgeEntityHandler) component.getEntityHandler();
    }

    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    protected abstract Class<? extends EdgeEntityHandler> entityHandlerClass();

}
