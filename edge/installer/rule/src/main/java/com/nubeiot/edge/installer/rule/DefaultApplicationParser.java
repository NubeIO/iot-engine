package com.nubeiot.edge.installer.rule;

import java.nio.file.Path;
import java.util.Optional;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.installer.dto.RequestedServiceData;
import com.nubeiot.edge.installer.model.InvalidModuleType;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;
import com.nubeiot.edge.installer.model.type.ModuleType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultApplicationParser implements ApplicationParser {

    private final Path dataDir;

    @Override
    public @NonNull IApplication parse(@NonNull RuleRepository ruleRepository,
                                       @NonNull RequestedServiceData serviceData) throws InvalidModuleType {
        final IApplication application = parse(dataDir(), serviceData.getMetadata(), serviceData.getAppConfig());
        return Optional.ofNullable(ruleRepository.get(application.getServiceType()))
                       .map(r -> r.validate(application))
                       .orElse(application);
    }

    private IApplication parse(@NonNull Path dataDir, @NonNull JsonObject metadata, AppConfig appConfig) {
        final IApplication application = ModuleType.factory(metadata.getString("service_type")).serialize(metadata);
        return application.setAppConfig(appConfig.toJson())
                          .setSystemConfig(computeAppSystemConfig(dataDir, application.getAppId()));
    }

    private JsonObject computeAppSystemConfig(@NonNull Path parentDataDir, String serviceId) {
        return NubeConfig.blank(FileUtils.recomputeDataDir(parentDataDir, FileUtils.normalize(serviceId))).toJson();
    }

}
