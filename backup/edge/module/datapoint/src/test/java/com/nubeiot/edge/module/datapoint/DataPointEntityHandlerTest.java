package com.nubeiot.edge.module.datapoint;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Table;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nubeiot.core.sql.ReferenceEntityMetadata;
import com.nubeiot.core.sql.decorator.EntityConstraintHolder;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.iotdata.edge.model.Keys;
import com.nubeiot.iotdata.edge.model.Tables;

public class DataPointEntityHandlerTest {

    private EntityConstraintHolder holder;

    @Before
    public void setup() {
        holder = () -> Keys.class;
    }

    @Test
    public void test_reference_table_to_device() {
        final List<ReferenceEntityMetadata> referenceEntityMetadata = holder.referenceTo(DeviceMetadata.INSTANCE);
        Assert.assertEquals(4, referenceEntityMetadata.size());
        Assert.assertTrue(referenceEntityMetadata.stream()
                                                 .allMatch(ref -> ref.getTable().equals(Tables.EDGE_DEVICE) ||
                                                                  ref.getTable().equals(Tables.TRANSDUCER) ||
                                                                  ref.getTable().equals(Tables.POINT_TRANSDUCER) ||
                                                                  ref.getTable().equals(Tables.FOLDER_GROUP)));
    }

    @Test
    public void test_reference_table_to_network() {
        final List<ReferenceEntityMetadata> referenceEntityMetadata = holder.referenceTo(NetworkMetadata.INSTANCE);
        Assert.assertEquals(3, referenceEntityMetadata.size());
        Assert.assertTrue(referenceEntityMetadata.stream()
                                                 .allMatch(ref -> ref.getTable().equals(Tables.POINT) ||
                                                                  ref.getTable().equals(Tables.EDGE_DEVICE) ||
                                                                  ref.getTable().equals(Tables.FOLDER_GROUP)));
    }

    @Test
    public void test_reference_table_to_point() {
        final List<ReferenceEntityMetadata> refs = holder.referenceTo(PointCompositeMetadata.INSTANCE);
        final Set<Table> tables = Stream.of(Tables.POINT_TAG, Tables.POINT_TRANSDUCER, Tables.POINT_HISTORY_DATA,
                                            Tables.POINT_REALTIME_DATA, Tables.POINT_VALUE_DATA,
                                            Tables.SCHEDULE_SETTING, Tables.HISTORY_SETTING, Tables.REALTIME_SETTING,
                                            Tables.FOLDER_GROUP).collect(Collectors.toSet());
        Assert.assertEquals(tables.size(), refs.size());
        Assert.assertTrue(refs.stream().allMatch(ref -> tables.contains(ref.getTable())));
    }

}
