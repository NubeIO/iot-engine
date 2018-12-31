package com.nubeiot.buildscript.jooq

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.ForcedType

class JooqGenerateTask extends DefaultTask {

    @Input
    String packageName
    @Input
    JDBCType jdbcType = JDBCType.PLAIN_JDBC
    @Input
    APIType apiType = APIType.RX
    @Input
    public Set<ForcedType> dbTypes = []
    @Input
    public Set<String> enumTypes = []
    @Input
    public Set<CustomDataType> javaTypes = []

    String ddlDir = "src/main/resources/ddl/*.sql"
    String targetDir = project.genSrc.javaSrcFolder

    @TaskAction
    void generate() {
        def input = project.projectDir.toPath().resolve(ddlDir).toString()
        def output = project.projectDir.toPath().resolve(targetDir).toString()
        enumTypes += ["com.nubeiot.core.enums.State", "com.nubeiot.core.enums.Status",
                      "com.nubeiot.core.event.EventAction", "com.nubeiot.core.event.EventPattern",
                      "com.nubeiot.core.exceptions.NubeException.ErrorCode", "com.nubeiot.core.cluster.ClusterType"]
        CacheDataType.instance().addEnumClasses(enumTypes).addDataType(javaTypes)
        enumTypes.each { s ->
            def expression = s.substring(s.lastIndexOf(".") + 1)
            if (expression == "EventAction") {
                expression = "event|action|event_action"
            }
            dbTypes.add(new ForcedType(types: DB.TYPES.varchar, userType: s, enumConverter: true,
                                       expression: Utils.toSnakeCase(expression)))
        }
        def config = JooqProvider.createConfiguration(JooqProvider.createDatabase(input, dbTypes),
                                                      JooqProvider.createTarget(output, packageName),
                                                      JooqProvider.getVertxGenerator(apiType, jdbcType))
        try {
            GenerationTool.generate(config)
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

    static class CustomDataType {
        String className
        String parser
        String converter
    }
}
