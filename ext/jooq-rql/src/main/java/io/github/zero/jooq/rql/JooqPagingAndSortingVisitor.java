package io.github.zero.jooq.rql;

import java.util.Collections;
import java.util.Objects;

import org.jooq.DSLContext;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectLimitStep;
import org.jooq.SelectOptionStep;
import org.jooq.SelectSeekStepN;
import org.jooq.TableLike;

import io.github.zero.jpa.Pageable;
import io.github.zero.jpa.Sortable;

import cz.jirutka.rsql.parser.ast.Node;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public final class JooqPagingAndSortingVisitor extends AbstractJooqVisitor<SelectOptionStep<Record>>
    implements JooqRqlFacadeVisitor<SelectOptionStep<Record>> {

    @NonNull
    private final TableLike table;
    private final Pageable pageable;
    private final Sortable sortable;

    @Override
    protected SelectOptionStep<Record> build(@NonNull Node node, @NonNull DSLContext dsl) {
        return paging(orderBy(
            dsl.select(queryContext().fieldSelector().get()).from(table).where(conditionVisitor().build(node, dsl)),
            sortable), pageable);
    }

    private SelectSeekStepN<Record> orderBy(@NonNull SelectConditionStep<Record> sql, Sortable sort) {
        if (Objects.isNull(sort) || sort.isEmpty()) {
            return sql.orderBy(Collections.emptyList());
        }
        return sql.orderBy(sort.orders()
                               .stream()
                               .filter(order -> !order.property().contains("."))
                               .map(this::sortField)
                               .filter(Objects::nonNull)
                               .toArray(OrderField[]::new));
    }

    private OrderField<?> sortField(@NonNull Sortable.Order order) {
        return queryContext().fieldMapper()
                             .get(table, order.property())
                             .map(f -> order.direction().isASC() ? f.asc() : f.desc())
                             .orElse(null);
    }

    private SelectOptionStep<Record> paging(@NonNull SelectLimitStep<Record> sql, @NonNull Pageable pagination) {
        return sql.limit(pagination.getPerPage()).offset((pagination.getPage() - 1) * pagination.getPerPage());
    }

}
