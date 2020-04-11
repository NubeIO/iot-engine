package com.nubeiot.edge.installer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.zero.utils.FileUtils;
import io.github.zero.utils.Strings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.auth.Credential;
import com.nubeiot.auth.ExternalServer;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.edge.installer.loader.ModuleType;
import com.nubeiot.edge.installer.model.dto.RequestedServiceData;

import lombok.Getter;
import lombok.Setter;

@Getter
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class InstallerConfig implements IConfig {

    public static final String NAME = "__installer__";

    private boolean autoInstall = true;
    @JsonProperty(value = RepositoryConfig.NAME)
    @Setter
    private RepositoryConfig repoConfig = new RepositoryConfig();
    @JsonProperty(value = "builtin_app")
    private List<RequestedServiceData> builtinApps = new ArrayList<>();

    @Override
    public String key() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() {
        return AppConfig.class;
    }

    @Getter
    public static final class RepositoryConfig implements IConfig {

        public static final String DEFAULT_LOCAL = "repositories";
        static final String NAME = "repository";
        @JsonProperty(defaultValue = DEFAULT_LOCAL)
        private String local = DEFAULT_LOCAL;
        @JsonProperty(value = RemoteRepositoryConfig.NAME)
        private RemoteRepositoryConfig remoteConfig = new RemoteRepositoryConfig();

        public String getLocal() {
            return Strings.isBlank(local) ? DEFAULT_LOCAL : local;
        }

        String recomputeLocal(Path parent) {
            local = FileUtils.recomputeDataDir(NubeConfig.DEFAULT_DATADIR, parent, getLocal()).toString();
            return local;
        }

        @Override
        public String key() {
            return NAME;
        }

        @Override
        public Class<? extends IConfig> parent() {
            return InstallerConfig.class;
        }

        @Getter
        @JsonInclude(Include.NON_NULL)
        public static final class RemoteRepositoryConfig implements IConfig {

            static final String NAME = "remote";
            private final Map<ModuleType, List<ExternalServer>> urls = new HashMap<>();
            @Setter
            private Credential credential;

            @Override
            public String key() {
                return NAME;
            }

            @Override
            public Class<? extends IConfig> parent() {
                return RepositoryConfig.class;
            }

            RemoteRepositoryConfig addUrl(ModuleType moduleType, ExternalServer externalServer) {
                this.urls.computeIfAbsent(moduleType, t -> new ArrayList<>()).add(externalServer);
                return this;
            }

        }

    }

}
