package com.nubeiot.core.sql.query;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

/**
 * {@code Transitive reference} represents for a case:
 * <ul><li>Table A has <i>reference field</i> to Table B</li>
 * <li>Table B has <i>reference field</i> to Table C</li>
 * <li>Table A has <b>transitive reference</b> to Table C</li>
 * </ul>
 *
 * @param <P> Type of {@code VertxPojo}
 * @since 1.0.0
 */
public interface TransitiveReferenceQueryExecutor<P extends VertxPojo> extends ReferenceQueryExecutor<P> {}
