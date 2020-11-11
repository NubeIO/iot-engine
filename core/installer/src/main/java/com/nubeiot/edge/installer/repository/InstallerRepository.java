package com.nubeiot.edge.installer.repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.github.zero88.utils.FileUtils;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.maven.MavenVerticleFactory;
import io.vertx.maven.ResolverOptions;

import com.nubeiot.auth.ExternalServer;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.edge.installer.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.installer.InstallerConfig.RepositoryConfig.RemoteRepositoryConfig;
import com.nubeiot.edge.installer.loader.ModuleType;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class InstallerRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstallerRepository.class);

    private final Vertx vertx;

    public static InstallerRepository create(@NonNull Vertx vertx) {
        return new InstallerRepository(vertx);
    }

    public void setup(@NonNull RepositoryConfig repositoryCfg, @NonNull Path dataDir) {
        LOGGER.info("Setting up service local and remote repository");
        RemoteRepositoryConfig remoteConfig = repositoryCfg.getRemoteConfig();
        LOGGER.info("URLs" + remoteConfig.getUrls());
        remoteConfig.getUrls()
                    .entrySet()
                    .stream()
                    .parallel()
                    .forEach(entry -> handleVerticleFactory(dataDir, repositoryCfg.getLocal(), entry));
    }

    private void handleVerticleFactory(@NonNull Path dataDir, String local,
                                       Entry<ModuleType, List<ExternalServer>> entry) {
        final ModuleType type = entry.getKey();
        final String localDir = RepositoryConfig.DEFAULT_LOCAL.equals(local)
                                ? dataDir.resolve(local).toString()
                                : local;
        if (ModuleType.JAVA == type) {
            List<ExternalServer> externalServers = entry.getValue();
            String javaLocal = FileUtils.createFolder(NubeConfig.DEFAULT_DATADIR, localDir,
                                                      type.name().toLowerCase(Locale.ENGLISH));
            LOGGER.info("{} local repositories: {}", type, javaLocal);
            LOGGER.info("{} remote repositories: {}", type, externalServers);
            ResolverOptions resolver = new ResolverOptions().setRemoteRepositories(
                externalServers.stream().map(ExternalServer::getUrl).collect(Collectors.toList()))
                                                            .setLocalRepository(javaLocal);
            vertx.registerVerticleFactory(new MavenVerticleFactory(resolver));
        }
    }

}
