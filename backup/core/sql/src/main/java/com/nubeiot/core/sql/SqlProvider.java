package com.nubeiot.core.sql;

import com.nubeiot.core.component.UnitProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SqlProvider<T extends EntityHandler> implements UnitProvider<SQLWrapper> {

    private final Class<T> entityHandlerClass;

    @Override
    public SQLWrapper<T> get() { return new SQLWrapper<>(entityHandlerClass); }

    @Override
    public Class<SQLWrapper> unitClass() { return SQLWrapper.class; }

}
