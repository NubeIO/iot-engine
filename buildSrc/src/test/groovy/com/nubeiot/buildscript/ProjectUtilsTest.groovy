package com.nubeiot.buildscript

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before

class ProjectUtilsTest {

    Project project

    @Before
    void setup() {
        def genFolder = "src/generated"
        def genSrc = [srcFolder: genFolder, javaSrcFolder: "$genFolder/java", resourceFolder: "$genFolder/resources"]
        project = ProjectBuilder.builder().build()
        project.extensions.add("genProps", genSrc)
    }
}
