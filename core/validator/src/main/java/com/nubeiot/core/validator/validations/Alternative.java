package com.nubeiot.core.validator.validations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Alternative<T> extends Validation<T, Object> {

    private final List<Validation<T, ?>> validations;

    @Override
    public Single<ValidationResult<Object>> validity(T s) {
        final List<NubeException> exception = new ArrayList<>();
        return Single.create(source -> {
            for (Validation<T, ?> validation : new HashSet<>(validations)) {

                validation.registerInput(this.input).validate(s)
                          .subscribe(validationResult -> source.onSuccess((ValidationResult) validationResult),
                                     error -> exception.add((NubeException) error));
            }

            if (exception.size() == validations.size()) {
                source.onError(exception.get(0));
            }
        });
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

}
