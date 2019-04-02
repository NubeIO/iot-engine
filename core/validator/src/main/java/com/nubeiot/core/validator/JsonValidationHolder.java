package com.nubeiot.core.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

public class JsonValidationHolder<T> implements JsonValidation, ValidationHolder<T> {

    protected Map<String, List<Validation<Object>>> validations = new HashMap<>();

    @Override
    public Map<String, List<Validation<Object>>> validations() {
        return this.validations;
    }

    @Override
    public ValidationHolder add(String field, @NonNull Validation<Object> validation) {
        List<Validation<Object>> validationList = this.validations.get(field);
        if (validationList == null) {
            validationList = new ArrayList<>();
        }
        validationList.add(validation);
        this.validations.put(field, validationList);
        return this;
    }

    @Override
    public Object get(Object data, String key) {
        return JsonValidation.super.get(data, key);
    }

}

