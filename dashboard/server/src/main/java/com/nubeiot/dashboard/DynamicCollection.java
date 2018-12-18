package com.nubeiot.dashboard;

import com.nubeiot.dashboard.impl.handlers.BaseCollectionHandler;
import com.nubeiot.dashboard.impl.handlers.MenuCollectionHandler;

public enum DynamicCollection {
    MENU("menu", new MenuCollectionHandler());

    private final String menu;
    private final BaseCollectionHandler menuCollection;

    DynamicCollection(String menu, BaseCollectionHandler menuCollection) {
        this.menu = menu;
        this.menuCollection = menuCollection;
    }

    public static BaseCollectionHandler getCollection(String collection) {
        for (DynamicCollection dynamicCollection : DynamicCollection.values()) {
            if (collection.equals(dynamicCollection.menu)) {
                return dynamicCollection.menuCollection;
            }
        }
        return new BaseCollectionHandler();
    }
}
