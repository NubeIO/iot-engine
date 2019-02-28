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
                    println nativeHeaderSrcFolder
                    def proc = "javac -h $projectDir/$nativeHeaderSrcFolder $file".execute()
                    proc.consumeProcessOutput(sout, serr)
                    proc.waitForOrKill(1000)
                    println "out> $sout err > $serr"
                }
            }
        }

        srcFiles.each { srcFile ->
            srcFile.exists() && srcFile.eachFileRecurse(FileType.FILES) { file ->
                if (file.toString().endsWith(".class")) {
                    def sout = new StringBuilder(), serr = new StringBuilder()
                    def proc = "rm $file".execute()
                    proc.consumeProcessOutput(sout, serr)
                    proc.waitForOrKill(1000)
                    println "out> $sout err > $serr"
                }
            }

        }

    }
}
