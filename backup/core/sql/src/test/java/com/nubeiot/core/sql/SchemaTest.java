package com.nubeiot.core.sql;

import org.jooq.Catalog;

import com.nubeiot.core.sql.mock.manyschema.mock0.tables.TblSample_00;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.TblSample_01;

public interface SchemaTest {

    interface OneSchema {

        Catalog CATALOG = com.nubeiot.core.sql.mock.oneschema.DefaultCatalog.DEFAULT_CATALOG;

    }


    interface ManySchema {

        Catalog CATALOG = com.nubeiot.core.sql.mock.manyschema.DefaultCatalog.DEFAULT_CATALOG;
        TblSample_00 TBL_SAMPLE_00 = TblSample_00.TBL_SAMPLE_00;
        TblSample_01 TBL_SAMPLE_01 = TblSample_01.TBL_SAMPLE_01;

    }

}
