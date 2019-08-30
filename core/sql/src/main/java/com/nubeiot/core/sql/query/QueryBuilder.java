package com.nubeiot.core.sql.query;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectJoinStep;
import org.jooq.SelectLimitStep;
import org.jooq.SelectOptionStep;
import org.jooq.SelectSeekStepN;
import org.jooq.Table;
import org.jooq.impl.DSL;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.Sort;
import com.nubeiot.core.dto.Sort.SortType;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.tables.JsonTable;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public final class QueryBuilder {

    private final EntityMetadata base;
    private List<OrderField<?>> orderFields;
    private Collection<EntityMetadata> references;
    private Predicate<EntityMetadata> predicate = metadata -> true;
    private JoinType joinType = JoinType.JOIN;
    private Supplier<List<SelectFieldOrAsterisk>> fields = () -> Collections.singletonList(DSL.asterisk());

    @SuppressWarnings("unchecked")
    QueryBuilder(@NonNull EntityMetadata base) {
        this.base = base;
        this.orderFields = base.orderFields();
    }

    public QueryBuilder references(@NonNull Collection<EntityMetadata> references) {
        this.references = references;
        return this;
    }

    public QueryBuilder predicate(@NonNull Predicate<EntityMetadata> predicate) {
        this.predicate = predicate;
        return this;
    }

    public QueryBuilder joinType(@NonNull JoinType type) {
        this.joinType = type;
        return this;
    }

    public QueryBuilder joinFields(@NonNull Supplier<List<SelectFieldOrAsterisk>> selectFields) {
        this.fields = selectFields;
        return this;
    }

    /**
     * Create view query
     *
     * @param filter     Request filter
     * @param sort       Sort
     * @param pagination pagination
     * @return query function
     */
    @SuppressWarnings("unchecked")
    Function<DSLContext, ? extends ResultQuery<? extends Record>> view(JsonObject filter, Sort sort,
                                                                       Pagination pagination) {
        final @NonNull JsonTable<? extends Record> table = base.table();
        return context -> {
            final SelectJoinStep<Record> query = context.select(fields.get()).from(table);
            if (Objects.nonNull(references)) {
                references.stream()
                          .filter(predicate)
                          .forEach(meta -> doJoin(query, meta, QueryParser.fromReference(meta, filter), joinType));
            }
            return (ResultQuery<? extends Record>) paging(orderBy(query.where(condition(table, filter, false)), sort),
                                                          pagination);
        };
    }

    /**
     * Create view query for one resource
     *
     * @param filter Request filter
     * @return query function
     */
    Function<DSLContext, ? extends ResultQuery<? extends Record>> viewOne(JsonObject filter) {
        return view(filter, null, Pagination.oneValue());
    }

    Function<DSLContext, Boolean> exist(@NonNull EntityMetadata metadata, @NonNull Object key) {
        return exist(metadata.table(), conditionByPrimary(metadata, key));
    }

    public Function<DSLContext, Boolean> exist(@NonNull Table table, @NonNull Condition condition) {
        return dsl -> dsl.fetchExists(table, condition);
    }

    @SuppressWarnings("unchecked")
    Function<DSLContext, ? extends ResultQuery<? extends Record>> existQueryByJoin(JsonObject filter) {
        final @NonNull JsonTable<? extends Record> table = base.table();
        final JsonObject nullable = new JsonObject();
        return context -> {
            final SelectJoinStep<Record> query = context.select(onlyPrimaryKeys()).from(table);
            if (Objects.nonNull(references)) {
                references.stream()
                          .peek(meta -> {
                              if (!predicate.test(meta)) {
                                  nullable.put(meta.requestKeyName(), filter.getValue(meta.requestKeyName()));
                              }
                          })
                          .filter(predicate)
                          .forEach(meta -> doJoin(query, meta, new JsonObject().put(meta.jsonKeyName(), filter.getValue(
                              meta.requestKeyName())), JoinType.RIGHT_OUTER_JOIN));
            }
            return (ResultQuery<? extends Record>) query.where(condition(table, nullable, true)).limit(1);
        };
    }

    /**
     * Create database condition by request filter
     * <p>
     * It is simple filter function by equal comparision. Any complex query should be override by each service.
     *
     * @param table  Resource table
     * @param filter Filter request
     * @return Database Select DSL
     * @see Condition
     */
    //TODO Rich query depends on RQL in future https://github.com/NubeIO/iot-engine/issues/128
    public Condition condition(@NonNull JsonTable<? extends Record> table, JsonObject filter) {
        return condition(table, filter, false);
    }

    @SuppressWarnings("unchecked")
    Condition conditionByPrimary(@NonNull EntityMetadata metadata, @NonNull Object key) {
        return metadata.table().getField(metadata.jsonKeyName()).eq(key);
    }

    @SuppressWarnings("unchecked")
    private Condition condition(@NonNull JsonTable<? extends Record> table, JsonObject filter, boolean allowNullable) {
        if (Objects.isNull(filter)) {
            return DSL.trueCondition();
        }
        Condition[] c = new Condition[] {DSL.trueCondition()};
        filter.stream().map(entry -> {
            final Field field = table.getField(entry.getKey());
            return Optional.ofNullable(field)
                           .map(f -> Optional.ofNullable(entry.getValue())
                                             .map(v -> allowNullable ? f.eq(v).or(f.isNull()) : f.eq(v))
                                             .orElseGet(f::isNull))
                           .orElse(null);
        }).filter(Objects::nonNull).forEach(condition -> c[0] = c[0].and(condition));
        return c[0];
    }

    private SelectSeekStepN<? extends Record> orderBy(@NonNull SelectConditionStep<? extends Record> sql, Sort sort) {
        if (Objects.isNull(sort) || sort.isEmpty()) {
            return sql.orderBy(orderFields);
        }
        final JsonObject jsonSort = sort.toJson();
        final Stream<OrderField<?>> sortFields = Stream.concat(
            jsonSort.stream().filter(entry -> !entry.getKey().contains(".")).map(entry -> sortField(base, entry)),
            Optional.ofNullable(references)
                    .map(refs -> refs.stream()
                                     .flatMap(
                                         meta -> QueryParser.streamRefs(meta, jsonSort).map(e -> sortField(meta, e))))
                    .orElse(Stream.empty()));
        return sql.orderBy(sortFields.filter(Objects::nonNull).toArray(OrderField[]::new));
    }

    private OrderField<?> sortField(EntityMetadata meta, Entry<String, Object> entry) {
        final SortType type = SortType.parse(Strings.toString(entry.getValue()));
        if (type == null) {
            return null;
        }
        final Field<?> field = meta.table().getField(entry.getKey());
        return Optional.ofNullable(field).map(f -> SortType.DESC == type ? f.desc() : f.asc()).orElse(null);
    }

    /**
     * Do query paging
     *
     * @param sql        SQL select command
     * @param pagination Given pagination
     * @return Database Select DSL
     */
    private SelectOptionStep<? extends Record> paging(@NonNull SelectLimitStep<? extends Record> sql,
                                                      Pagination pagination) {
        Pagination paging = Optional.ofNullable(pagination).orElseGet(() -> Pagination.builder().build());
        return sql.limit(paging.getPerPage()).offset((paging.getPage() - 1) * paging.getPerPage());
    }

    private List<SelectFieldOrAsterisk> onlyPrimaryKeys() {
        return Stream.concat(Stream.of(base.table().asterisk()), Optional.ofNullable(references)
                                                                         .map(s -> s.stream()
                                                                                    .filter(predicate)
                                                                                    .map(entry -> entry.table()
                                                                                                       .getPrimaryKey()
                                                                                                       .getFieldsArray())
                                                                                    .flatMap(Stream::of))
                                                                         .orElse(Stream.empty()))
                     .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private void doJoin(SelectJoinStep<Record> query, EntityMetadata meta, JsonObject filter, JoinType joinType) {
        query.join(meta.table(), joinType).onKey().where(condition(meta.table(), filter));
    }

}
