package com.nubeiot.edge.installer.loader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;
import com.nubeiot.edge.installer.model.tables.pojos.Application;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModuleTypeRule implements Shareable {

    private final Map<ModuleType, ModuleTypePredicate> rules;

    public ModuleTypeRule() {
        rules = new HashMap<>();
    }

    public IApplication parse(@NonNull Path dataDir, @NonNull JsonObject metadata, AppConfig appConfig) {
        IApplication application = parse(metadata);
        application = application.setAppConfig(appConfig.toJson());
        application = application.setSystemConfig(computeAppSystemConfig(dataDir, application.getAppId()));
        return application;
    }

    public IApplication parse(JsonObject metadata) {
        ModuleType moduleType = ModuleType.factory(metadata.getString("service_type"));
        String serviceId = metadata.getString("app_id");
        JsonObject module = Objects.isNull(serviceId) ? moduleType.serialize(metadata, this) : metadata;
        return new Application().fromJson(module);
    }

    public IApplication parse(@NonNull Path dataDir, @NonNull IApplication application, AppConfig appConfig) {
        application = application.setAppConfig(appConfig.toJson());
        application = application.setSystemConfig(computeAppSystemConfig(dataDir, application.getAppId()));
        return application;
    }

    private JsonObject computeAppSystemConfig(@NonNull Path parentDataDir, String serviceId) {
        return NubeConfig.blank(FileUtils.recomputeDataDir(parentDataDir, FileUtils.normalize(serviceId))).toJson();
    }

    public ModuleTypeRule registerRule(ModuleType moduleType, List<String> searchPattern) {
        rules.put(moduleType, ModuleTypePredicateFactory.factory(moduleType, searchPattern));
        return this;
    }

    public Predicate<String> getRule(ModuleType moduleType) {
        final ModuleTypePredicate ruleMetadata = this.rules.get(moduleType);
        return Objects.isNull(ruleMetadata) ? any -> false : ruleMetadata.getRule();
    }

    public List<String> getSearchPattern(ModuleType moduleType) {
        final ModuleTypePredicate ruleMetadata = this.rules.get(moduleType);
        return Objects.isNull(ruleMetadata) ? new ArrayList<>() : ruleMetadata.getSearchPattern();
    }

    @Override
    public Shareable copy() {
        return new ModuleTypeRule(Collections.unmodifiableMap(this.rules));
    }

}
