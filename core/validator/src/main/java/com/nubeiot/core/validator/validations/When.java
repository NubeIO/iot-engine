package com.nubeiot.core.validator.validations;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class When<T extends Single<ValidationResult<Object>>> extends Validation<T, Object> {

    private Single<ValidationResult<Object>> is;
    private Single<ValidationResult<Object>> then;
    private T otherwise;

    public Single<ValidationResult<Object>> validate() {
        return this.validate(null);
    }

    @Override
    public Single<ValidationResult<Object>> validate(T s) {
        return Single.create(
            source -> this.is.subscribe(ignored -> this.then.subscribe(source::onSuccess, source::onError),
                                        error -> otherwise.subscribe(source::onSuccess, source::onError)));
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

    public When<T> registerIs(Single<ValidationResult<Object>> is) {
        this.is = is;
        return this;
    }

    public When<T> registerThen(Single<ValidationResult<Object>> then) {
        this.then = then;
        return this;
    }

    public When<T> registerOtherwise(T otherwise) {
        this.otherwise = otherwise;
        return this;
    }

}
