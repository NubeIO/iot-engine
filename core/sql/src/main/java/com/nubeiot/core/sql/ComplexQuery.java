package com.nubeiot.core.sql;

import io.github.jklingsporn.vertx.jooq.rx.RXQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.jdbc.JDBCQueryExecutor;
import io.reactivex.Single;

/**
 * For complex query with {@code join}, {@code group by}
 */
public interface ComplexQuery extends JDBCQueryExecutor<Single<?>>, RXQueryExecutor {

}
