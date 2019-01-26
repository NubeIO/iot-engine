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

    /**
     * Enum Types that using {@code valueOf} function to parse data
     */
    @Input
    public Set<String> enumTypes = []

    /**
     * Converter between Java data type and database type
     */
    @Input
    public Set<ForcedType> dbTypes = []

    /**
     * Converter between JsonObject and Java data type
     */
    @Input
    public Set<JsonDataType> javaTypes = []

    @Input
    public String ddlDir = "src/main/resources/ddl"
    @Input
    public String targetDir = project.genProps.javaSrcDir

    @TaskAction
    void generate() {
        def ddl = project.projectDir.toPath().resolve(ddlDir).toString()
        def input = ddl.endsWith(".sql") ? ddl : ddl + "/*.sql"
        def output = project.projectDir.toPath().resolve(targetDir).toString()
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
            logger.info("-" * 58)
            logger.info("Generating DDL based on: {}", project.deps.database.h2)
            logger.info("=" * 58)
            logger.info("")
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

    /**
     * Converter between JsonObject and Java data type
     */
    static class JsonDataType {
        /**
         * Java full qualified class name
         */
        String className
        /**
         * Serialize function
         */
        String converter
        /**
         * Deserialize function
         */
        String parser
        /**
         * Default value if {@code null} on deserialize
         */
        String defVal = "null"

        @Override
        String toString() {
            return "${className} - Converter: ${converter} - Parser: ${parser} - Default: ${defVal}"
        }
    }
}
