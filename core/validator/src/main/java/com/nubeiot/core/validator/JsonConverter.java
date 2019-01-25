package com.nubeiot.core.validator;

import com.nubeiot.core.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class JsonConverter<T, R, I> extends Validation<T, R> {

    private final Validation<I, R> validator;
    @Setter
    private String parentField;
    @Setter
    private String field;
    private Object defaultValue = null;

    public static <E, F, G> Single<ValidationResult<F>> validate(Validation<G, F> validator, E s, String field) {
        return new JsonConverter<>(validator).validate(s, "", field);
    }

    public static <E, F, G> Single<ValidationResult<F>> validate(Validation<G, F> validator, E s, String parentField,
                                                                 String field) {
        return new JsonConverter<>(validator).validate(s, parentField, field);
    }

    public static <E, F, G> Single<ValidationResult<F>> validate(Validation<G, F> validator, E s, String parentField,
                                                                 String field, Object defaultValue) {
        return new JsonConverter<>(validator).registerDefaultValue(defaultValue).validate(s, parentField, field);
    }

    /**
     * @param defaultValue if value is null on the Json, then the default value will be inserted
     * @return own class object
     */
    public JsonConverter<T, R, I> registerDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public Single<ValidationResult<R>> validity(T s) {

        if (s instanceof JsonObject) {
            T value = null;
            // field is generally blank for Loop and KeyLoop
            if (Strings.isBlank(field)) {
                return validator.registerInput(buildInputField()).validate((I) s);
            }
            String[] fields = field.split("\\.");
            JsonObject jsonObject = (JsonObject) s;

            try {
                for (int i = 0; i < fields.length - 1; i++) {
                    if (jsonObject.getJsonObject(fields[i]) == null && this.defaultValue != null) {
                        jsonObject.put(fields[i], new JsonObject());
                    }
                    jsonObject = jsonObject.getJsonObject(fields[i]);
                }
                value = (T) jsonObject.getValue(fields[fields.length - 1]);
            } catch (NullPointerException ignored) {
            }

            if (value == null) {
                if (this.defaultValue != null) {
                    jsonObject.put(fields[fields.length - 1], defaultValue);
                    value = (T) defaultValue;
                }
            }

            return validator.registerInput(buildInputField()).validate((I) value);
        }

        if (this.validator.isNullable() && s == null) {
            return ValidationResult.valid();
        }

        // Case: JsonArray
        return validator.registerInput(buildInputField()).validate((I) s);
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

    @Override
    public Validation<T, R> registerInput(String input) {
        return this;
    }

    public Single<ValidationResult<R>> validate(T s, String parentField, String field) {
        this.parentField = parentField;
        this.field = field;
        return this.validate(s);
    }

    String buildInputField() {
        if (Strings.isNotBlank(parentField) && Strings.isNotBlank(this.field)) {
            return this.parentField + "." + this.field;
        } else {
            return this.parentField + this.field;
        }
    }

}
