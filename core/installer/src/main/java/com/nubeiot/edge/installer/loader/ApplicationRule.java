package com.nubeiot.edge.installer.loader;

import java.util.function.Function;
import java.util.function.Predicate;

import com.nubeiot.edge.installer.loader.VertxModuleType.JVMModuleType;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;

import lombok.NonNull;

public interface ApplicationRule {

    @NonNull
    static ApplicationRule create(@NonNull Function<IApplication, IApplication> validation) {
        return new DefaultApplicationRule(validation);
    }

    static ApplicationRule jvmRule(@NonNull String... artifactGroups) {
        final Predicate<String> predicate = JVMModuleType.rulePredicate(artifactGroups);
        return create(app -> {
            if (!predicate.test(app.getAppId())) {
                throw new InvalidModuleType("Unqualified whitelist " + app.getServiceType().type() + " artifact");
            }
            return app;
        });
    }

    @NonNull IApplication validate(@NonNull IApplication application) throws InvalidModuleType;

    @NonNull ApplicationRule andThen(ApplicationRule andThen);

}
