package com.nubeiot.core.component;

import java.util.function.Supplier;

/**
 * Unit Provider
 *
 * @param <T> Unit type
 */
public interface UnitProvider<T extends Unit> extends Supplier<T> {

    Class<T> unitClass();

}
