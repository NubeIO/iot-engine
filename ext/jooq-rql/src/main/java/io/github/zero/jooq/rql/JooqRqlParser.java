package io.github.zero.jooq.rql;

import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.TableLike;

import io.github.zero.jooq.rql.parser.ast.ComparisonOperatorProxy;
import io.github.zero.jooq.rql.visitor.JooqRqlConditionVisitor;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import lombok.NonNull;

/**
 * Represents for {@code Jooq RQL} parser.
 *
 * @since 1.0.0
 */
public final class JooqRqlParser {

    /**
     * The constant DEFAULT.
     */
    public static JooqRqlParser DEFAULT = new JooqRqlParser();
    @NonNull
    private final RSQLParser parser;

    /**
     * Instantiates a new {@code Jooq RQL} parser.
     *
     * @since 1.0.0
     */
    private JooqRqlParser() {
        this(ComparisonOperatorProxy.operators());
    }

    /**
     * Instantiates a new {@code Jooq RQL} parser.
     *
     * @param comparisons the comparisons
     * @see ComparisonOperatorProxy
     * @since 1.0.0
     */
    public JooqRqlParser(@NonNull Set<ComparisonOperatorProxy> comparisons) {
        parser = new RSQLParser(
            comparisons.stream().map(ComparisonOperatorProxy::operator).collect(Collectors.toSet()));
    }

    /**
     * Parse query to select condition step.
     *
     * @param query the query
     * @param dsl   the dsl
     * @param table the table
     * @return the select condition step
     * @throws RSQLParserException the RSQL parser exception
     * @see Record
     * @see Table
     * @see DSLContext
     * @see SelectConditionStep
     * @since 1.0.0
     */
    public Condition criteria(@NonNull String query, @NonNull DSLContext dsl, @NonNull TableLike table)
        throws RSQLParserException {
        return parse(query, dsl, JooqRqlConditionVisitor.builder().table(table).build());
    }

    /**
     * Parse query to appropriate output.
     *
     * @param <R>     Type of {@code result}
     * @param query   the query
     * @param dsl     the dsl
     * @param visitor the visitor
     * @return the select condition step
     * @throws RSQLParserException the RSQL parser exception
     * @see Record
     * @see Table
     * @see DSLContext
     * @see SelectConditionStep
     * @since 1.0.0
     */
    public <R> R parse(@NonNull String query, @NonNull DSLContext dsl, @NonNull JooqRqlVisitor<R> visitor)
        throws RSQLParserException {
        return parser.parse(query).accept(visitor, dsl);
    }

}
