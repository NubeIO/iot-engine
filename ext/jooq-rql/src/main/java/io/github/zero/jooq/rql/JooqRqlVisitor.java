package io.github.zero.jooq.rql;

import org.jooq.DSLContext;

import cz.jirutka.rsql.parser.ast.RSQLVisitor;

/**
 * The interface Jooq RQL visitor.
 *
 * @param <R> Type of {@code Result}
 * @see DSLContext
 * @see RSQLVisitor
 * @see JooqRqlFacade
 * @since 1.0.0
 */
public interface JooqRqlVisitor<R> extends RSQLVisitor<R, DSLContext>, JooqRqlFacade, HasLog {

}
