package com.nubeiot.core.sql.validation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DefaultOperationValidator implements OperationValidator {

    @NonNull
    private final BiFunction<RequestData, VertxPojo, Single<? extends VertxPojo>> validation;
    private OperationValidator andThen;

    @Override
    public Single<VertxPojo> validate(@NonNull RequestData reqData, VertxPojo dbEntity) {
        return validation.apply(reqData, dbEntity)
                         .flatMap(p -> Optional.ofNullable(andThen)
                                               .map(validator -> validator.validate(reqData, p))
                                               .orElse(Single.just(p)));
    }

    @Override
    public @NonNull OperationValidator andThen(OperationValidator andThen) {
        this.andThen = Objects.isNull(this.andThen) ? andThen : this.andThen.andThen(andThen);
        return this;
    }

}
