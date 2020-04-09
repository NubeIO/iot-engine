package com.nubeiot.edge.installer.loader;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.installer.InstallerCacheInitializer;
import com.nubeiot.edge.installer.model.dto.RequestedServiceData;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;

import lombok.NonNull;

final class DefaultApplicationParser extends AbstractSharedDataDelegate<ApplicationParser>
    implements ApplicationParser {

    DefaultApplicationParser(@NonNull Function<String, Object> sharedDataFunc) {
        super();
        this.registerSharedData(sharedDataFunc);
    }

    @Override
    public @NonNull IApplication parse(@NonNull RequestedServiceData serviceData) throws InvalidModuleType {
        final IApplication application = parse(dataDir(), serviceData.getMetadata(), serviceData.getAppConfig());
        final RuleRepository ruleRepository = getSharedDataValue(InstallerCacheInitializer.SHARED_RULE_REPOSITORY);
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
