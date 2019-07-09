package com.nubeiot.edge.core;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.vertx.maven.MavenVerticleFactory;
import io.vertx.maven.ResolverOptions;

import com.nubeiot.auth.Credential;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.core.InstallerConfig.RemoteUrl;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig.RemoteRepositoryConfig;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.DefaultCatalog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EdgeVerticle extends ContainerVerticle {

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
        this.addSharedData(SHARED_INSTALLER_CFG, this.getInstallerConfig().toJson());
        this.moduleRule = this.getModuleRuleProvider().get();
        this.addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, entityHandlerClass()), this::handler);

        InstallerConfig installerCfg = Credential.recomputeReferenceCredentials(
            IConfig.from(nubeConfig.getAppConfig(), InstallerConfig.class), nubeConfig.getSecretConfig());
        setupServiceRepository(installerCfg.getRepoConfig());
    }

    private void handler(SqlContext component) {
        this.entityHandler = (EdgeEntityHandler) component.getEntityHandler();
    }

    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    protected abstract Class<? extends EdgeEntityHandler> entityHandlerClass();

    private void setupServiceRepository(RepositoryConfig repositoryCfg) {
        logger.info("Setting up service local and remote repository");
        RemoteRepositoryConfig remoteConfig = repositoryCfg.getRemoteConfig();
        logger.info("URLs: " + remoteConfig.getUrls());
        remoteConfig.getUrls()
                    .entrySet()
                    .stream()
                    .parallel()
                    .forEach(entry -> handleVerticleFactory(repositoryCfg.getLocal(), entry));
    }

    private void handleVerticleFactory(String local, Entry<ModuleType, List<RemoteUrl>> entry) {
        final ModuleType type = entry.getKey();
        if (ModuleType.JAVA == type) {
            List<RemoteUrl> remoteUrls = entry.getValue();
            String javaLocal = FileUtils.createFolder(local, type.name().toLowerCase(Locale.ENGLISH));
            logger.info("{} local repositories: {}", type, javaLocal);
            logger.info("{} remote repositories: {}", type, remoteUrls);
            ResolverOptions resolver = new ResolverOptions().setRemoteRepositories(
                remoteUrls.stream().map(RemoteUrl::computeUrl).collect(Collectors.toList()))
                                                            .setLocalRepository(javaLocal);
            vertx.registerVerticleFactory(new MavenVerticleFactory(resolver));
        }
    }

}
