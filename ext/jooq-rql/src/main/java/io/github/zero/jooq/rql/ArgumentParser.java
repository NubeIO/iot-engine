package io.github.zero.jooq.rql;

import java.util.List;

import org.jooq.Field;

import lombok.NonNull;

/**
 * The interface Argument parser.
 *
 * @since 1.0.0
 */
public interface ArgumentParser {

    /**
     * The constant DEFAULT.
     */
    ArgumentParser DEFAULT = new ArgumentParser() {};

    /**
     * Parse one argument value.
     *
     * @param field the database field
     * @param value the argument value
     * @return the database value
     * @see Field
     * @since 1.0.0
     */
    default Object parse(@NonNull Field field, String value) {
        return value;
    }

    /**
     * Parse list argument values.
     *
     * @param field  the database field
     * @param values the argument values
     * @return the database values
     * @see Field
     * @since 1.0.0
     */
    default List<?> parse(@NonNull Field field, List<String> values) {
        return values;
    }

}
