package com.nubeiot.core.validator.validations;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class When<T1, T2 extends Single<ValidationResult<Object>>> extends Validation<T1, Object> {

    private Single<ValidationResult<Object>> is;
    private Single<ValidationResult<Object>> then;
    private T2 otherwise;

    @Override
    public Single<ValidationResult<Object>> validity(T1 s) {
        return Single.create(
            source -> this.is.subscribe(ignored -> this.then.subscribe(source::onSuccess, source::onError),
                                        error -> otherwise.subscribe(source::onSuccess, source::onError)));
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

    public When<T1, T2> registerIs(Single<ValidationResult<Object>> is) {
        this.is = is;
        return this;
    }

    public When<T1, T2> registerThen(Single<ValidationResult<Object>> then) {
        this.then = then;
        return this;
    }

    public When<T1, T2> registerOtherwise(T2 otherwise) {
        this.otherwise = otherwise;
        return this;
    }

}
