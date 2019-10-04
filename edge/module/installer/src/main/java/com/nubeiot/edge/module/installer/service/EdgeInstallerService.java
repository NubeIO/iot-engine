package com.nubeiot.edge.module.installer.service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.service.InstallerService;

public interface EdgeInstallerService extends InstallerService {

    static Set<? extends EdgeInstallerService> createServices(InstallerVerticle edgeVerticle) {
        final Map<Class, Object> inputs = Collections.singletonMap(InstallerVerticle.class, edgeVerticle);
        return ReflectionClass.stream(EdgeInstallerService.class.getPackage().getName(), EdgeInstallerService.class,
                                      ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    default String api() {
        return "bios.installer.service." + this.getClass().getSimpleName();
    }

    @Override
    default String rootPath() {
        return "/services";
    }

}
