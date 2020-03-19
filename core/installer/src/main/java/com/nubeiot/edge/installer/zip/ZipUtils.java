package com.nubeiot.edge.installer.zip;

import java.io.File;

import io.vertx.core.Vertx;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

public class ZipUtils {

    void zipFolder(Vertx vertx, String folder) {
        try {
            final ZipFile zipFile = new ZipFile("filename.zip");
            zipFile.addFolder(new File(folder), new ZipParameters());
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

}
