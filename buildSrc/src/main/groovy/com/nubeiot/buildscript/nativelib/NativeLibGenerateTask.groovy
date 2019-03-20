package com.nubeiot.buildscript.nativelib

import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class NativeLibsGenerateTask extends DefaultTask {
    @Input
    public String projectDir
    @Input
    public String nativeLibsSrcFolder


    @TaskAction
    void generate() {
        def javaHome = "${System.getenv("JAVA_HOME")}"
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
                    proc = """g++ -fPIC -std=c++0x -I$javaHome/include -I$javaHome/incldue/linux -dynamiclib -o 
$nativeLibsSrcFolder/$dynamicLibName $file""".execute()
                } else if (os.startsWith("mac os x")) {
                    def dynamicLibName = "lib${file.toString().tokenize("/").last().replace(extension, "")}.dylib"
                    proc = """g++ -fPIC -std=c++0x -I$javaHome/include -I$javaHome/include/darwin -dynamiclib -o 
$nativeLibsSrcFolder/$dynamicLibName $file""".execute()

                } else if (os.startsWith("win")) {
                    def dynamicLibName = "${file.toString().tokenize("/").last().replace(extension, "")}.dll"
                    proc = """x86_64-w64-mingw32-g++ -std=c++0x -I$javaHome\\include -I$javaHome\\include\\win32 -dynamiclib -o $nativeLibsSrcFolder/$dynamicLibName $file""".execute()
                } else {
                    println "Error > OS must be Linux, MAC, or Windows"
                    return
                }
                proc.consumeProcessOutput(sout, serr)
                proc.waitForOrKill(1000)
                if (sout) {
                    println "System Output > $sout"
                }
                if (serr) {
                    println "Error > $serr"
                }
            }
        }
    }
}
