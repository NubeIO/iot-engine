package com.nubeiot.edge.installer.rule;

import java.nio.file.Path;

import com.nubeiot.edge.installer.dto.RequestedServiceData;
import com.nubeiot.edge.installer.model.InvalidModuleType;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;

import lombok.NonNull;

public interface ApplicationParser {

    static @NonNull ApplicationParser create(@NonNull Path dataDir) {
        return new DefaultApplicationParser(dataDir);
    }

    @NonNull Path dataDir();

    @NonNull IApplication parse(@NonNull RuleRepository ruleRepository, @NonNull RequestedServiceData serviceData)
        throws InvalidModuleType;

}
