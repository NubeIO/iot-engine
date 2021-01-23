package com.nubeiot.edge.module.datapoint.trigger;

import java.sql.Connection;
import java.sql.SQLException;

import org.h2.api.Trigger;
import org.jooq.DSLContext;
import org.jooq.exception.SQLStateClass;
import org.jooq.impl.DSL;

import io.github.zero88.utils.Strings;
import io.github.zero88.utils.UUID64;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderGroupMetadata;
import com.nubeiot.edge.module.datapoint.trigger.validator.GroupDeviceValidator;
import com.nubeiot.edge.module.datapoint.trigger.validator.GroupEdgeValidator;
import com.nubeiot.edge.module.datapoint.trigger.validator.GroupNetworkValidator;
import com.nubeiot.edge.module.datapoint.trigger.validator.GroupRecursiveFolderValidator;
import com.nubeiot.iotdata.dto.GroupLevel;
import com.nubeiot.iotdata.edge.model.tables.pojos.FolderGroup;

import lombok.NonNull;

public final class FolderGroupTrigger implements Trigger {

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type)
        throws SQLException { }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        try {
            final FolderGroup pojo = validateAndCompute(newRow, DSL.using(conn));
            newRow[1] = pojo.getLevel().type();
            newRow[2] = Strings.isBlank(pojo.getFolderId()) ? null : UUID64.uuid64ToUuid(pojo.getFolderId());
            newRow[3] = pojo.getNetworkId();
            newRow[4] = pojo.getDeviceId();
            newRow[5] = pojo.getPointId();
            newRow[6] = Strings.isBlank(pojo.getParentFolderId())
                        ? null
                        : UUID64.uuid64ToUuid(pojo.getParentFolderId());
        } catch (IllegalArgumentException cause) {
            throw new SQLException(SQLStateClass.C22_DATA_EXCEPTION.name(),
                                   SQLStateClass.C22_DATA_EXCEPTION.className(), cause);
        } catch (NubeException cause) {
            throw new SQLException(SQLStateClass.C23_INTEGRITY_CONSTRAINT_VIOLATION.name(),
                                   SQLStateClass.C23_INTEGRITY_CONSTRAINT_VIOLATION.className(), cause);
        }
    }

    @Override
    public void close() throws SQLException { }

    @Override
    public void remove() throws SQLException { }

    private FolderGroup validateAndCompute(@NonNull Object[] newRow, @NonNull DSLContext dsl) {
        final com.nubeiot.iotdata.edge.model.tables.FolderGroup table = FolderGroupMetadata.INSTANCE.table();
        final FolderGroup pojo = FolderGroupMetadata.INSTANCE.validate(
            new FolderGroup().setLevel(table.LEVEL.getDataType().convert(newRow[1]))
                             .setFolderId(table.FOLDER_ID.getDataType().convert(newRow[2]))
                             .setNetworkId(table.NETWORK_ID.getDataType().convert(newRow[3]))
                             .setDeviceId(table.DEVICE_ID.getDataType().convert(newRow[4]))
                             .setPointId(table.POINT_ID.getDataType().convert(newRow[5]))
                             .setParentFolderId(table.PARENT_FOLDER_ID.getDataType().convert(newRow[6])));
        if (pojo.getLevel() == GroupLevel.EDGE) {
            return new GroupEdgeValidator(dsl).compute(pojo);
        }
        if (pojo.getLevel() == GroupLevel.NETWORK) {
            return new GroupNetworkValidator(dsl).compute(pojo);
        }
        if (pojo.getLevel() == GroupLevel.DEVICE) {
            return new GroupDeviceValidator(dsl).compute(pojo);
        }
        if (pojo.getLevel() == GroupLevel.FOLDER) {
            return new GroupRecursiveFolderValidator(dsl).compute(pojo);
        }
        throw new IllegalArgumentException("Unsupported group level " + pojo.getLevel());
    }

}
