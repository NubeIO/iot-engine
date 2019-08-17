package com.nubeiot.core.sql;

import org.jooq.Catalog;

import com.nubeiot.core.component.UnitProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SqlProvider<T extends AbstractEntityHandler> implements UnitProvider<SQLWrapper> {

    private final Catalog catalog;
    private final Class<T> entityHandlerClass;

    @Override
    public SQLWrapper<T> get() { return new SQLWrapper<>(catalog, entityHandlerClass); }

    @Override
    public Class<SQLWrapper> unitClass() { return SQLWrapper.class; }

}
