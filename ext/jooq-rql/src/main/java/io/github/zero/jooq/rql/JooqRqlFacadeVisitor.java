package io.github.zero.jooq.rql;

import org.jooq.TableLike;

import lombok.NonNull;

/**
 * The interface Jooq rql delegate visitor.
 *
 * @param <R> Type of {@code parameter}
 * @since 1.0.0
 */
public interface JooqRqlFacadeVisitor<R> extends JooqRqlVisitor<R> {

    /**
     * Defines table
     *
     * @return the table
     * @see TableLike
     * @since 1.0.0
     */
    @NonNull TableLike table();

    /**
     * Create condition visitor jooq rql condition visitor.
     *
     * @return the jooq rql condition visitor
     * @since 1.0.0
     */
    default @NonNull JooqRqlConditionVisitor conditionVisitor() {
        return JooqRqlConditionVisitor.builder()
                                      .table(table())
                                      .queryContext(queryContext())
                                      .criteriaBuilderFactory(criteriaBuilderFactory())
                                      .build();
    }

}
