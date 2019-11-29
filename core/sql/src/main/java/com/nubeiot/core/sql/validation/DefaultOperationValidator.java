package com.nubeiot.core.sql.validation;

import java.util.Optional;
import java.util.function.BiFunction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder(builderClassName = "Builder")
class DefaultOperationValidator implements OperationValidator {

    @NonNull
    private final BiFunction<RequestData, VertxPojo, Single<VertxPojo>> validate;
    @Setter
    private BiFunction<RequestData, VertxPojo, Single<VertxPojo>> andThen;

    @Override
    public Single<VertxPojo> validate(@NonNull RequestData reqData, VertxPojo dbEntity) {
        return validate.apply(reqData, dbEntity)
                       .flatMap(p -> Optional.ofNullable(andThen).map(f -> f.apply(reqData, p)).orElse(Single.just(p)));
    }

}
