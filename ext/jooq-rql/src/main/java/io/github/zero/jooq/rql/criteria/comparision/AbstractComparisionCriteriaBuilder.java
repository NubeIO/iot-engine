package io.github.zero.jooq.rql.criteria.comparision;

import io.github.zero.jooq.rql.criteria.AbstractCriteriaBuilder;
import io.github.zero.jooq.rql.parser.ast.ComparisonOperatorProxy;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class AbstractComparisionCriteriaBuilder extends AbstractCriteriaBuilder<ComparisonNode>
    implements ComparisonCriteriaBuilder<ComparisonOperatorProxy> {

    @NonNull
    private final ComparisonOperatorProxy operator;

    protected AbstractComparisionCriteriaBuilder(@NonNull ComparisonNode node) {
        super(node);
        this.operator = ComparisonOperatorProxy.asProxy(node.getOperator());
    }

}
