package com.nubeiot.core.validator;

import com.nubeiot.core.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public abstract class Validation<T, R> {

    protected String parentField = "";
    protected String field = "";
    protected Object defaultValue = null;
    protected String errorType = "ValidationError";

    public abstract Single<ValidationResult<R>> validate(T s);

    protected abstract String getErrorMessage();

    public Validation<T, R> registerField(String field) {
        this.field = field;
        return this;
    }

    public Validation<T, R> registerParentField(String parentField) {
        this.parentField = parentField;
        return this;
    }

    public Validation<T, R> registerDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * @param jsonObject : JSON object to be passed to extract value
     * @param field      : JSON field on String format like "thing.features.points.UI1.value"
     * @return : Resulted value on ValidationResult form
     *         <p>
     *         Basically this will works as a mediator for parsing JSON value and sending it to actual business logic of
     *         validation
     */
    public final Single<ValidationResult<R>> validate(JsonObject jsonObject, String field) {
        this.field = field;
        if (Strings.isBlank(field)) {
            return validate((T) jsonObject);
        }

        T value;
        String[] fields = field.split("\\.");

        JsonObject object = jsonObject;
        try {
            for (int i = 0; i < fields.length - 1; i++) {
                if (object.getJsonObject(fields[i]) == null && this.defaultValue != null) {
                    object.put(fields[i], new JsonObject());
                }
                object = object.getJsonObject(fields[i]);
            }
        } catch (NullPointerException ignored) {
        }
        value = (T) object.getValue(fields[fields.length - 1]);

        if (value == null) {
            if (this.defaultValue != null) {
                object.put(fields[fields.length - 1], defaultValue);
            }

            if (this.passNullCase()) {
                return new ValidationResult<R>().asyncSuccess();
            }
        }

        return validate(value);
    }

    protected boolean passNullCase() {
        return true;
    }

    public final Single<ValidationResult<R>> validate(Object s, String parentField, String field) {
        this.field = field;
        this.parentField = parentField;

        if (s instanceof JsonObject) {
            return validate((JsonObject) s, field);
        }

        if (this.passNullCase() && s == null) {
            return new ValidationResult<R>().asyncSuccess();
        }

        return validate((T) s);
    }

    protected final String getAbsoluteField() {
        if (Strings.isNotBlank(parentField) && Strings.isNotBlank(this.field)) {
            return parentField + "." + this.field;
        } else {
            return this.parentField + this.field;
        }
    }

}
