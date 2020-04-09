package com.nubeiot.edge.installer.loader;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultApplicationRule implements ApplicationRule {

    @NonNull
    private final Function<IApplication, IApplication> validation;
    private ApplicationRule andThen;

    @Override
    public @NonNull IApplication validate(@NonNull IApplication application) throws InvalidModuleType {
        final IApplication app = validation.apply(application);
        return Optional.ofNullable(andThen).map(validator -> validator.validate(app)).orElse(app);
    }

    @Override
    public @NonNull ApplicationRule andThen(ApplicationRule andThen) {
        this.andThen = Objects.isNull(this.andThen) ? andThen : this.andThen.andThen(andThen);
        return this;
    }

}
