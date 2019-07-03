package com.nubeiot.edge.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.auth.Credential;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.NubeConfig.SecretConfig;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.loader.ModuleType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    public String name() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() {
        return AppConfig.class;
    }

    @Getter
    public static final class RepositoryConfig implements IConfig {

        static final String NAME = "repository";
        static final String DEFAULT_LOCAL = "repositories";

        @JsonProperty(defaultValue = DEFAULT_LOCAL)
        private String local = DEFAULT_LOCAL;
        @JsonProperty(value = RemoteRepositoryConfig.NAME)
        private RemoteRepositoryConfig remoteConfig = new RemoteRepositoryConfig();

        public String getLocal() {
            return Strings.isBlank(local) ? DEFAULT_LOCAL : local;
        }

        String recomputeLocal(Path parent) {
            local = FileUtils.recomputeDataDir(parent, getLocal()).toString();
            return local;
        }

        @Override
        public String name() {
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

            @Setter
            private Credential credential;
            private final Map<ModuleType, List<RemoteUrl>> urls = new HashMap<>();

            @Override
            public String name() {
                return NAME;
            }

            @Override
            public Class<? extends IConfig> parent() {
                return RepositoryConfig.class;
            }

            RemoteRepositoryConfig addUrl(ModuleType moduleType, RemoteUrl remoteUrl) {
                this.urls.computeIfAbsent(moduleType, t -> new ArrayList<>()).add(remoteUrl);
                return this;
            }

        }

    }


    @Getter
    @RequiredArgsConstructor
    @ToString
    public static class RemoteUrl {

        @Setter
        private Credential credential;
        private final String url;

        @JsonCreator
        public RemoteUrl(@JsonProperty(value = "credential") Credential credential,
                         @JsonProperty(value = "url", required = true) String url) {
            this.credential = credential;
            this.url = Strings.requireNotBlank(url);
        }

        public String computeUrl(SecretConfig secretConfig) {
            return Objects.isNull(this.credential) ? url : this.credential.computeUrl(this.url, secretConfig);
        }

    }

}
