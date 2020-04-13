package io.github.zero.jooq.rql;

import org.jooq.DSLContext;

import lombok.NonNull;

/**
 * The interface Jooq rql query.
 *
 * @param <R> Type of {@code Query Result}
 * @param <T> Type of {@code Visitor result}
 * @see JooqRqlFacade
 * @since 1.0.0
 */
public interface JooqRqlQuery<R, T> extends JooqRqlFacade, HasLog {

    /**
     * Defines dsl context.
     *
     * @return the dsl context
     * @since 1.0.0
     */
    @NonNull DSLContext dsl();

    /**
     * Defines Jooq RQL Parser.
     *
     * @return the Jooq rql parser
     * @see JooqRqlParser
     * @since 1.0.0
     */
    @NonNull JooqRqlParser parser();

    /**
     * Defines Jooq RQL visitor.
     *
     * @return the Jooq rql visitor
     * @see JooqRqlVisitor
     * @since 1.0.0
     */
    @NonNull JooqRqlVisitor<T> visitor();

    /**
     * Execute.
     *
     * @param query the query
     * @return the r
     * @since 1.0.0
     */
    @NonNull R execute(@NonNull String query);

}
