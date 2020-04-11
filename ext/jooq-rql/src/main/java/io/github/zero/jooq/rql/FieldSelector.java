package io.github.zero.jooq.rql;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.jooq.SelectFieldOrAsterisk;
import org.jooq.impl.DSL;

/**
 * The interface Field selector.
 *
 * @see SelectFieldOrAsterisk
 * @since 1.0.0
 */
public interface FieldSelector extends Supplier<Collection<? extends SelectFieldOrAsterisk>> {

    /**
     * The constant DEFAULT.
     */
    FieldSelector DEFAULT = new FieldSelector() {};

    @Override
    default Collection<? extends SelectFieldOrAsterisk> get() {
        return Collections.singleton(DSL.asterisk());
    }

}
