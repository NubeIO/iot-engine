package com.nubeiot.buildscript.nativelib

import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class NativeHeadersGenerateTask extends DefaultTask {
    @Input
    public String projectDir
    @Input
    public Set<File> srcFiles
    @Input
    public String nativeHeaderSrcFolder

    @TaskAction
    void generate() {
        srcFiles.each { srcFile ->
            srcFile.exists() && srcFile.eachFileRecurse(FileType.FILES) { file ->
                if (file.toString().endsWith(".java") && file.toString().contains("/nativeclass/")) {
                    def sout = new StringBuilder(), serr = new StringBuilder()
                    def classpathSplitter = "src/main/java/"
                    def values = file.toString().split(classpathSplitter)
                    println values
                    def classpath = values[0] + classpathSplitter
                    def javaClass = values[1].replace(".java", "").replaceAll("/", ".")
                    def proc = "javah -d $projectDir/$nativeHeaderSrcFolder -classpath $classpath $javaClass".execute()
                    proc.consumeProcessOutput(sout, serr)
                    proc.waitForOrKill(1000)
                    println "out> $sout err > $serr"
                }
            }
        }
    }
}
