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
    public Single<ValidationResult<List<?>>> validate(T s) {

        return Observable.fromIterable(validations)
                         .flatMapSingle(
                             validation -> validation.registerField(field).registerParentField(parentField).validate(s))
                         .toList()
                         .map(outputs -> {
                             List<Object> list = new ArrayList<>();
                             for (ValidationResult<?> output : outputs) {
                                 list.add(output.getData());
                             }
                             return new ValidationResult<List<?>>().success(list);
                         });
    }

    @Override
    protected boolean passNullCase() {
        return false;
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

}
