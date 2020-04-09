package com.nubeiot.edge.installer;

import io.vertx.core.Vertx;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.cache.CacheInitializer;

import lombok.NonNull;

public final class InstallerCacheInitializer implements CacheInitializer<InstallerCacheInitializer, InstallerVerticle> {

    public static final String INSTALLER_CFG = "INSTALLER_CFG";
    public static final String APP_DEPLOYER_CFG = "APP_DEPLOYER_CFG";
    public static final String RULE_REPOSITORY = "APPLICATION_RULE_REPOSITORY";

    @Override
    public InstallerCacheInitializer init(@NonNull InstallerVerticle context) {
        final Vertx vertx = context.getVertx();
        addBlockingCache(vertx, INSTALLER_CFG, () -> getInstallerConfig(context), context::addSharedData);
        addBlockingCache(vertx, APP_DEPLOYER_CFG, context::appDeployerDefinition, context::addSharedData);
        addBlockingCache(vertx, RULE_REPOSITORY, context::ruleRepository, context::addSharedData);
        return this;
    }

    private InstallerConfig getInstallerConfig(@NonNull InstallerVerticle context) {
        final InstallerConfig installerConfig = IConfig.from(context.getNubeConfig().getAppConfig(),
                                                             InstallerConfig.class);
        installerConfig.getRepoConfig().recomputeLocal(context.getNubeConfig().getDataDir());
        return installerConfig;
    }

}
