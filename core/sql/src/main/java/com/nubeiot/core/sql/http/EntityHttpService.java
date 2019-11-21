package com.nubeiot.core.sql.http;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface EntityHttpService {

    static Set<EventMethodDefinition> createCRUDDefinitions(@NonNull EntityMetadata resource,
                                                            EntityMetadata... references) {
        return createDefinitions(ActionMethodMapping.CRUD_MAP, resource, references);
    }

    static Set<EventMethodDefinition> createDefinitions(@NonNull Collection<EventAction> events,
                                                        @NonNull EntityMetadata resource,
                                                        EntityMetadata... references) {
        return createDefinitions(ActionMethodMapping.CRUD_MAP, events, resource, references);
    }

    static Set<EventMethodDefinition> createDefinitions(@NonNull ActionMethodMapping base,
                                                        @NonNull Collection<EventAction> availableEvents,
                                                        @NonNull EntityMetadata resource,
                                                        EntityMetadata... references) {
        return createDefinitions(ActionMethodMapping.by(base, availableEvents), resource, references);
    }

    static Set<EventMethodDefinition> createDefinitions(@NonNull ActionMethodMapping available,
                                                        @NonNull EntityMetadata resource,
                                                        EntityMetadata... references) {
        return createDefinitions(available, resource::singularKeyName, resource::requestKeyName, references);
    }

    static Set<EventMethodDefinition> createDefinitions(@NonNull Collection<EventAction> availableEvents,
                                                        @NonNull Supplier<String> resourceKeyName,
                                                        @NonNull Supplier<String> resourceRequestKeyName,
                                                        EntityMetadata... references) {
        return createDefinitions(ActionMethodMapping.by(ActionMethodMapping.CRUD_MAP, availableEvents), resourceKeyName,
                                 resourceRequestKeyName, references);
    }

    static Set<EventMethodDefinition> createDefinitions(@NonNull ActionMethodMapping available,
                                                        @NonNull Supplier<String> resourceKeyName,
                                                        @NonNull Supplier<String> resourceRequestKeyName,
                                                        EntityMetadata... references) {
        final List<String> servicePaths = Stream.of(references)
                                                .filter(Objects::nonNull)
                                                .map(EntityHttpService::toCapturePath)
                                                .collect(Collectors.toList());
        return Stream.concat(servicePaths.stream().map(path -> Urls.combinePath(path, resourceKeyName.get())),
                             Stream.of(Urls.combinePath(String.join(Urls.PATH_SEP_CHAR, servicePaths),
                                                        resourceKeyName.get())))
                     .map(path -> EventMethodDefinition.create(path, resourceRequestKeyName.get(), available))
                     .collect(Collectors.toSet());
    }

    static String toCapturePath(@NonNull EntityMetadata metadata) {
        return Urls.capturePath(metadata.singularKeyName(), metadata.requestKeyName());
    }

}
