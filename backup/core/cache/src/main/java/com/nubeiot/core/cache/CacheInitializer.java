package com.nubeiot.core.cache;

import lombok.NonNull;

public interface CacheInitializer<R extends CacheInitializer, C> {

    @NonNull R init(@NonNull C context);

}
