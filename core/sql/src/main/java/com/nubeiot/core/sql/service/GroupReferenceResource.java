package com.nubeiot.core.sql.service;

import java.util.Set;

/**
 * Mark {@code EntityService} as representing {@code resource} contains one or more other resources. It makes {@code
 * EntityService} is holder one or more reference entity.
 */
public interface GroupReferenceResource extends HasReferenceResource {

    EntityReferences groupReferences();

    @Override
    default Set<String> ignoreFields() {
        return groupReferences().ignoreFields();
    }

}
