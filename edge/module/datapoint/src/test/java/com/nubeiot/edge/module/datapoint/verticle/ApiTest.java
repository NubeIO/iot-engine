package com.nubeiot.edge.module.datapoint.verticle;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;

public class ApiTest {

    @Test
    public void test() {
        final Set<EventAction> events = ActionMethodMapping.CRUD_MAP.get().keySet();
        final EntityMetadata resource = FolderMetadata.INSTANCE;
        final EntityMetadata ref1 = PointCompositeMetadata.INSTANCE;
        final EntityMetadata ref2 = DeviceMetadata.INSTANCE;
        Stream.of(EntityHttpService.createDefinitions(events, resource, ref1),
                  EntityHttpService.createDefinitions(events, resource, ref2),
                  EntityHttpService.createDefinitions(events, resource, true, ref2, ref1))
              .flatMap(Collection::stream)
              .map(JsonData::toJson)
              .forEach(s -> System.out.println(s.encodePrettily()));
    }

}
