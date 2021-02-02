package io.github.zero88.qwe.iot.connector;
import io.github.zero88.qwe.component.SharedDataLocalProxy;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public abstract class BaseService implements ConnectorService {

    @NonNull
    private final SharedDataLocalProxy sharedData;

}
