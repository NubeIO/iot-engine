package com.nubeiot.edge.bios.service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.service.InstallerService;

public interface BiosInstallerService extends InstallerService {

    static Set<? extends BiosInstallerService> createServices(InstallerVerticle edgeVerticle) {
        final Map<Class, Object> inputs = Collections.singletonMap(InstallerVerticle.class, edgeVerticle);
        return ReflectionClass.stream(BiosInstallerService.class.getPackage().getName(), BiosInstallerService.class,
                                      ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    default String api() {
        return "bios.installer." + this.getClass().getSimpleName();
    }

    default String rootPath() {
        return "/modules";
    }

}
