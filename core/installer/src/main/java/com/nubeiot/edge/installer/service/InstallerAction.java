package com.nubeiot.edge.installer.service;

import java.util.Arrays;
import java.util.Collection;

import com.nubeiot.core.event.EventAction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InstallerAction {

    public static Collection<EventAction> install() {
        return Arrays.asList(EventAction.INIT, EventAction.CREATE);
    }

    public static Collection<EventAction> update() {
        return Arrays.asList(EventAction.MIGRATE, EventAction.UPDATE, EventAction.PATCH);
    }

    public static Collection<EventAction> uninstall() {
        return Arrays.asList(EventAction.REMOVE, EventAction.HALT);
    }

    public static Collection<EventAction> internal() {
        return Arrays.asList(EventAction.INIT, EventAction.MIGRATE);
    }

    public static boolean isInstall(EventAction action) {
        return install().contains(action);
    }

    public static boolean isUpdate(EventAction action) {
        return update().contains(action);
    }

    public static boolean isPatch(EventAction action) {
        return update().contains(action);
    }

    public static boolean isUninstall(EventAction action) {
        return uninstall().contains(action);
    }

    public static boolean isInternal(EventAction action) {
        return internal().contains(action);
    }

}
