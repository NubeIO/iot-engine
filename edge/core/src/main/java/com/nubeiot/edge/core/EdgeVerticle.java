package com.nubeiot.edge.core;

import java.util.function.Supplier;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.DefaultCatalog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EdgeVerticle extends ContainerVerticle {

    @Getter
    private ModuleTypeRule moduleRule;
    @Getter
    private EdgeEntityHandler entityHandler;

    @Override
    public void start() {
        super.start();
        this.moduleRule = this.getModuleRuleProvider().get();
        this.addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, entityHandlerClass()), this::handler);
    }

    private void handler(SqlContext component) {
        this.entityHandler = ((EdgeEntityHandler) component.getEntityHandler());
    }

    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    protected abstract Class<? extends EdgeEntityHandler> entityHandlerClass();

}
