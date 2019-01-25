package com.nubeiot.core.validator.validations;

import java.util.ArrayList;
import java.util.List;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Composition<T> extends Validation<T, List<?>> {

    private final List<Validation<T, ?>> validations;

    @Override
    public Single<ValidationResult<List<?>>> validity(T s) {

        return Observable.fromIterable(validations)
                         .flatMapSingle(validation -> validation.registerInput(this.input).validate(s))
                         .toList()
                         .flatMap(outputs -> {
                             List<Object> list = new ArrayList<>();
                             for (ValidationResult<?> output : outputs) {
                                 list.add(output.getData());
                             }
                             return ValidationResult.valid(list);
                         });
    }

    @Override
    protected boolean isNullable() {
        return false;
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

}
