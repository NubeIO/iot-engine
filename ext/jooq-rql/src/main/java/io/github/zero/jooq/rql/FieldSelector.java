package io.github.zero.jooq.rql;

import java.util.Collection;
import java.util.Collections;

import org.jooq.SelectFieldOrAsterisk;
import org.jooq.Table;
import org.jooq.impl.DSL;

import lombok.NonNull;

/**
 * The interface Field selector.
 *
 * @see SelectFieldOrAsterisk
 * @see Table
 * @since 1.0.0
 */
@FunctionalInterface
public interface FieldSelector {

    /**
     * The constant DEFAULT.
     */
    FieldSelector DEFAULT = table -> Collections.singleton(DSL.asterisk());

    /**
     * Get fields by table
     *
     * @param table the table
     * @return collection fields
     */
    Collection<? extends SelectFieldOrAsterisk> by(@NonNull Table table);

}
