package com.nubeiot.buildscript.nativelib

import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class NativeLibsGenerateTask extends DefaultTask {
    @Input
    public String projectDir
    @Input
    public String jniIncludeDir
    @Input
    public String systemIncludeDir
    @Input
    public String localIncludeDir
    @Input
    public String nativeLibsSrcFolder


    @TaskAction
    void generate() {
        def dir = new File(projectDir)
        String os = System.getProperty("os.name").toLowerCase()
        nativeLibsSrcFolder = nativeLibsSrcFolder.replaceFirst("^~", System.getProperty("user.home"))
        def outputDir = new File("$nativeLibsSrcFolder")
        if (!outputDir.exists()) {
            outputDir.mkdir()
        }

        dir.eachFileRecurse(FileType.FILES) { file ->
            String extension = ".cpp"
            if (file.toString().endsWith(extension)) {
                def sout = new StringBuilder(), serr = new StringBuilder(), proc
                if (os.startsWith("linux")) {
                    def dynamicLibName = "lib${file.toString().tokenize("/").last().replace(extension, "")}.so"
                    proc = """g++ -fPIC -std=c++0x -I$jniIncludeDir -I$jniIncludeDir/linux -I$systemIncludeDir 
-I$localIncludeDir -dynamiclib -o $nativeLibsSrcFolder/$dynamicLibName $file"""
                        .execute()
                } else if (os.startsWith("mac os x")) {
                    def dynamicLibName = "lib${file.toString().tokenize("/").last().replace(extension, "")}.dylib"
                    proc = """g++ -fPIC -std=c++0x -I$jniIncludeDir -I$jniIncludeDir/darwin -I$systemIncludeDir 
-I$localIncludeDir -dynamiclib -o $nativeLibsSrcFolder/$dynamicLibName $file"""
                        .execute()

                } else if (os.startsWith("win")) {
                    def dynamicLibName = "${file.toString().tokenize("/").last().replace(extension, "")}.dll"
                    "x86_64-w64-mingw32-g++ -I\"%JAVA_HOME%\\include\" -I\"%JAVA_HOME%\\include\\win32\" -shared -o hello.dll HelloJNICpp.c HelloJNICppImpl.cpp"

                    proc = """x86_64-w64-mingw32-g++ -std=c++0x -I$jniIncludeDir -I$jniIncludeDir/linux -I$systemIncludeDir 
-I$localIncludeDir -dynamiclib -o $nativeLibsSrcFolder/$dynamicLibName $file"""
                        .execute()
                } else {
                    println "err> OS must be Linux, MAC, or Windows"
                    return
                }
                proc.consumeProcessOutput(sout, serr)
                proc.waitForOrKill(1000)
                println "out> $sout"
                println "err> $serr"
            }
        }
    }
}
