package com.nubeiot.edge.installer;

import io.vertx.core.Vertx;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.cache.CacheInitializer;

import lombok.NonNull;

public final class InstallerCacheInitializer implements CacheInitializer<InstallerCacheInitializer, InstallerVerticle> {

    public static final String SHARED_MODULE_RULE = "MODULE_RULE";
    public static final String SHARED_INSTALLER_CFG = "INSTALLER_CFG";
    public static final String SHARED_APP_DEPLOYER_CFG = "APP_DEPLOYER_CFG";
    public static final String SHARED_RULE_REPOSITORY = "APPLICATION_RULE_REPOSITORY";

    @Override
    public InstallerCacheInitializer init(@NonNull InstallerVerticle context) {
        final Vertx vertx = context.getVertx();
        addBlockingCache(vertx, SHARED_INSTALLER_CFG, () -> getInstallerConfig(context), context::addSharedData);
        addBlockingCache(vertx, SHARED_APP_DEPLOYER_CFG, context::appDeployerDefinition, context::addSharedData);
        addBlockingCache(vertx, SHARED_RULE_REPOSITORY, context::ruleRepository, context::addSharedData);
        return this;
    }

    private InstallerConfig getInstallerConfig(@NonNull InstallerVerticle context) {
        final InstallerConfig installerConfig = IConfig.from(context.getNubeConfig().getAppConfig(),
                                                             InstallerConfig.class);
        installerConfig.getRepoConfig().recomputeLocal(context.getNubeConfig().getDataDir());
        return installerConfig;
    }

}
