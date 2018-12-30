package com.nubeiot.buildscript.jooq

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.ForcedType

class JooqGenerateTask extends DefaultTask {

    public APIType apiType = APIType.RX
    public JDBCType jdbcType = JDBCType.PLAIN_JDBC
    public String ddlDir = "src/main/resources/ddl/*.sql"
    public String targetDir = "src/generated"
    public String packageName
    public List<ForcedType> forcedTypes
    public Set<String> enumTypes
    public Set<CustomDataType> dataTypes

    @TaskAction
    void generate() {
        if (enumTypes == null) {
            enumTypes = new HashSet<>()
        }
        enumTypes += ["com.nubeiot.core.enums.State", "com.nubeiot.core.enums.Status",
                      "com.nubeiot.core.event.EventAction", "com.nubeiot.core.event.EventPattern",
                      "com.nubeiot.core.exceptions.NubeException.ErrorCode", "com.nubeiot.core.cluster.ClusterType"]
        CacheDataType.instance().addEnumClasses(enumTypes).addDataType(dataTypes)
        if (forcedTypes == null) {
            forcedTypes = new ArrayList<>()
        }
        enumTypes.each { s ->
            forcedTypes.add(new ForcedType().withTypes("(?i:VARCHAR)").withUserType(s).withEnumConverter(true))
        }
        def configuration = NubeJooqProvider.createConfiguration(
                NubeJooqProvider.createDatabase(ddlDir, forcedTypes),
                NubeJooqProvider.createTarget(targetDir, packageName),
                NubeJooqProvider.getVertxGenerator(apiType, jdbcType))

        try {
            GenerationTool.generate(configuration)
        } catch (Exception e) {
            throw new GradleException("Cannot generate jooq", e)
        }
    }

    static enum APIType {
        CLASSIC, COMPLETABLE_FUTURE, RX
    }

    static enum JDBCType {
        REACTIVE_POSTGRES, PLAIN_JDBC
    }

}
