package com.nubeiot.core.sql.service;

import java.util.Map.Entry;

import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.service.marker.OneToOneEntityMarker;

import lombok.NonNull;

public interface OneToOneEntityService<CP extends CompositePojo, CM extends CompositeMetadata>
    extends ReferencedEntityService<CP, CM>, OneToOneEntityMarker {

    @SuppressWarnings("unchecked")
    default MaybeSource<CP> doGet(@NonNull CP pojo, @NonNull Object key, Entry<EntityMetadata, String> entry) {
        final EntityMetadata m = entry.getKey();
        final String rk = entry.getValue();
        return (MaybeSource<CP>) referencedQuery(m).findOneByKey(createRequestData(key, rk))
                                                   .map(r -> pojo.put(m.singularKeyName(), r))
                                                   .onErrorReturn(t -> pojo)
                                                   .toMaybe();
    }

    default RequestData createRequestData(@NonNull Object key, @NonNull String referenceKey) {
        return RequestData.builder().body(new JsonObject().put(referenceKey, JsonData.checkAndConvert(key))).build();
    }

    default Maybe<CP> onCreate(@NonNull RequestData requestData, @NonNull CP pojo) {
        return Maybe.just(pojo);
    }

}
