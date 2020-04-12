package io.github.zero.jooq.rql;

import org.jooq.Table;

import lombok.NonNull;

/**
 * The interface Jooq rql delegate visitor.
 *
 * @param <R> Type of {@code parameter}
 * @since 1.0.0
 */
public interface JooqRqlDelegateVisitor<R> extends JooqRqlVisitor<R> {

    /**
     * Create condition visitor jooq rql condition visitor.
     *
     * @param table the table
     * @return the jooq rql condition visitor
     * @since 1.0.0
     */
    default @NonNull JooqRqlConditionVisitor createConditionVisitor(@NonNull Table table) {
        return JooqRqlConditionVisitor.builder()
                                      .table(table)
                                      .queryContext(queryContext())
                                      .criteriaBuilderFactory(criteriaBuilderFactory())
                                      .build();
    }

}
