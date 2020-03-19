package com.nubeiot.edge.installer.service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.edge.installer.InstallerEntityHandler;

/**
 * Represents Installer service.
 *
 * @since 1.0.0
 */
public interface InstallerService extends EventHttpService {

    /**
     * Create services.
     *
     * @param <T>           Type of {@code InstallerService}
     * @param entityHandler the entity handler
     * @param serviceClazz  the service clazz
     * @return set of {@code InstallerService}
     * @since 1.0.0
     */
    static <T extends InstallerService> Set<T> createServices(InstallerEntityHandler entityHandler,
                                                              Class<T> serviceClazz) {
        final Map<Class, Object> inputs = Collections.singletonMap(InstallerEntityHandler.class, entityHandler);
        return ReflectionClass.stream(serviceClazz.getPackage().getName(), serviceClazz, ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    @Override
    default Set<EventMethodDefinition> definitions() {
        final String fullPath = Urls.combinePath(rootPath(), appPath(), servicePath());
        return Collections.singleton(
            EventMethodDefinition.create(fullPath, paramPath(), ActionMethodMapping.byCRUD(getAvailableEvents())));
    }

    /**
     * Defines Root path.
     *
     * @return Root path
     * @since 1.0.0
     */
    default String rootPath() {
        return "/installer";
    }

    /**
     * Defines Application path.
     *
     * @return Application path
     * @since 1.0.0
     */
    String appPath();

    /**
     * Defines Service path.
     *
     * @return Service path
     * @since 1.0.0
     */
    String servicePath();

    /**
     * Defines Param path.
     *
     * @return Param path
     * @since 1.0.0
     */
    String paramPath();

}
