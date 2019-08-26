package com.nubeiot.core.sql.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.GroupEntityTransformer;
import com.nubeiot.core.sql.pojos.CompositePojo;

import lombok.NonNull;

public abstract class AbstractGroupEntityService<P extends VertxPojo, M extends EntityMetadata,
                                                    CP extends CompositePojo<P, CP>, CM extends CompositeMetadata>
    extends AbstractOneToManyEntityService<P, M>
    implements GroupEntityService<P, M, CP, CM>, GroupEntityTransformer, GroupReferenceResource {

    public AbstractGroupEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull GroupEntityTransformer transformer() {
        return this;
    }

    @Override
    public GroupReferenceResource ref() {
        return this;
    }

    protected Maybe<Boolean> validate(@NonNull RequestData reqData) {
        final Set<EntityMetadata> refs = Stream.concat(ref().entityReferences().refMetadata().stream(),
                                                       ref().groupReferences().refMetadata().stream())
                                               .collect(Collectors.toSet());
        return queryExecutor().mustExists(reqData, refs);
    }

}
