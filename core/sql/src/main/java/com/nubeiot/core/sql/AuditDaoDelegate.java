package com.nubeiot.core.sql;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.shared.internal.AbstractVertxDAO;

import lombok.NonNull;

public class AuditDaoDelegate<R extends UpdatableRecord<R>, P, T, FIND_MANY, FIND_ONE, EXECUTE, INSERT_RETURNING,
                                 D extends AbstractVertxDAO<R, P, T, FIND_MANY, FIND_ONE, EXECUTE, INSERT_RETURNING>>
    extends AbstractVertxDAO<R, P, T, FIND_MANY, FIND_ONE, EXECUTE, INSERT_RETURNING> {

    private final D dao;
    private final Class<P> pojoClass;

    public AuditDaoDelegate(@NonNull D dao, @NonNull Class<P> pojoClass) {
        super(dao.getTable(), pojoClass, dao.queryExecutor());
        this.dao = dao;
        this.pojoClass = pojoClass;
    }

    @Override
    protected T getId(P object) {
        return null;
    }

    @Override
    public EXECUTE update(P object) {
        return super.update(object);
    }

}
