package io.github.zero.jooq.rql.criteria.logical;

import io.github.zero.jooq.rql.criteria.CriteriaBuilder;

import cz.jirutka.rsql.parser.ast.LogicalNode;
import lombok.NonNull;

public interface LogicalCriteriaBuilder<T extends LogicalNode> extends CriteriaBuilder<T> {

    @NonNull T node();

}
