package io.github.zero.jooq.rql.criteria.comparision;

import io.github.zero.jooq.rql.criteria.CriteriaBuilder;
import io.github.zero.jooq.rql.parser.ast.ComparisonOperatorProxy;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import lombok.NonNull;

/**
 * The interface Comparison criteria builder.
 *
 * @param <T> Type of {@code ComparisonOperatorProxy}
 * @see ComparisonNode
 * @see ComparisonOperatorProxy
 * @see CriteriaBuilder
 * @since 1.0.0
 */
public interface ComparisonCriteriaBuilder<T extends ComparisonOperatorProxy> extends CriteriaBuilder<ComparisonNode> {

    /**
     * Create comparison criteria builder.
     *
     * @param node the node
     * @return the comparison criteria builder
     * @since 1.0.0
     */
    //TODO change to reflection solution
    static ComparisonCriteriaBuilder<ComparisonOperatorProxy> create(@NonNull ComparisonNode node) {
        final ComparisonOperator operator = node.getOperator();
        if (operator == RSQLOperators.EQUAL) {
            return new EqualBuilder(node);
        }
        if (operator == RSQLOperators.NOT_EQUAL) {
            return new NotEqualBuilder(node);
        }
        if (operator == RSQLOperators.GREATER_THAN) {
            return new GreaterThanBuilder(node);
        }
        if (operator == RSQLOperators.GREATER_THAN_OR_EQUAL) {
            return new GreaterThanOrEqualBuilder(node);
        }
        if (operator == RSQLOperators.LESS_THAN) {
            return new LessThanBuilder(node);
        }
        if (operator == RSQLOperators.LESS_THAN_OR_EQUAL) {
            return new LessThanOrEqualBuilder(node);
        }
        if (operator == RSQLOperators.IN) {
            return new InBuilder(node);
        }
        if (operator == RSQLOperators.NOT_IN) {
            return new NotInBuilder(node);
        }
        throw new UnsupportedOperationException("Unknown operator " + operator.getSymbol());
    }

    @NonNull ComparisonNode node();

    /**
     * Comparision operator.
     *
     * @return the comparision operator
     * @since 1.0.0
     */
    @NonNull T operator();

}
