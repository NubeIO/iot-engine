package com.nubeiot.buildscript.jooq

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class JooqGenerateTaskTest {

    Project project

    @Before
    void setup() {
        def genFolder = "src/generated"
        def genSrc = [srcFolder: genFolder, javaSrcFolder: "$genFolder/java", resourceFolder: "$genFolder/resources"]
        project = ProjectBuilder.builder().build()
        project.extensions.add("genProps", genSrc)
    }

    @Test
    void canAddTaskToProject() {
        def task = project.task('greeting', type: JooqGenerateTask)
        Assert.assertTrue(task instanceof JooqGenerateTask)
    }
}
