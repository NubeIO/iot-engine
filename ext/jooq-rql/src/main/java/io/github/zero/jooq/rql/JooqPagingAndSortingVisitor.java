package io.github.zero.jooq.rql;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectLimitStep;
import org.jooq.SelectOptionStep;
import org.jooq.SelectSeekStepN;
import org.jooq.Table;

import io.github.zero.jpa.Pageable;
import io.github.zero.jpa.Sortable;

import cz.jirutka.rsql.parser.ast.Node;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class JooqPagingAndSortingVisitor extends AbstractJooqVisitor<SelectOptionStep<Record>>
    implements JooqRqlDelegateVisitor<SelectOptionStep<Record>> {

    @NonNull
    private final Table table;
    private final Pageable pageable;
    private final Sortable sortable;

    @Override
    protected SelectOptionStep<Record> build(@NonNull Node node, @NonNull DSLContext dsl) {
        final Collection<? extends SelectFieldOrAsterisk> selector = queryContext().fieldSelector().by(table);
        final Condition condition = createConditionVisitor(table).build(node, dsl);
        return paging(orderBy(dsl.select(selector).from(table).where(condition), sortable), pageable);
    }

    private SelectSeekStepN<Record> orderBy(@NonNull SelectConditionStep<Record> sql, Sortable sort) {
        if (Objects.isNull(sort) || sort.isEmpty()) {
            return sql.orderBy(Collections.emptyList());
        }
        return sql.orderBy(sort.orders()
                               .stream()
                               .filter(order -> !order.getProperty().contains("."))
                               .map(this::sortField)
                               .filter(Objects::nonNull)
                               .toArray(OrderField[]::new));
    }

    private OrderField<?> sortField(@NonNull Sortable.Order order) {
        return queryContext().fieldMapper()
                             .get(table, order.getProperty())
                             .map(f -> order.getDirection().isAsc() ? f.asc() : f.desc())
                             .orElse(null);
    }

    /**
     * Do query paging
     *
     * @param sql        SQL select command
     * @param pagination Given pagination
     * @return Database Select DSL
     */
    private SelectOptionStep<Record> paging(@NonNull SelectLimitStep<Record> sql, @NonNull Pageable pagination) {
        return sql.limit(pagination.getPerPage()).offset((pagination.getPage() - 1) * pagination.getPerPage());
    }

}
