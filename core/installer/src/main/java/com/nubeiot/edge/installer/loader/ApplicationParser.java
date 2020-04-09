package com.nubeiot.edge.installer.loader;

import java.util.function.Function;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.edge.installer.model.dto.RequestedServiceData;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;

import lombok.NonNull;

public interface ApplicationParser extends SharedDataDelegate<ApplicationParser> {

    static @NonNull ApplicationParser create(@NonNull Function<String, Object> sharedDataFunc) {
        return new DefaultApplicationParser(sharedDataFunc);
    }

    @NonNull IApplication parse(@NonNull RequestedServiceData serviceData) throws InvalidModuleType;

}
