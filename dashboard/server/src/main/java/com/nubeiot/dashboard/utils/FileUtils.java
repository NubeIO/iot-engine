package com.nubeiot.dashboard.utils;

import java.io.File;

import io.vertx.reactivex.ext.web.FileUpload;

public class FileUtils {
    public static String appendRealFileNameWithExtension(FileUpload fileUpload) {
        File tempFile = new File(fileUpload.uploadedFileName());
        String renamedFile = fileUpload.uploadedFileName() + fileUpload.fileName().replace(" ", "+");
        boolean success = tempFile.renameTo(new File(renamedFile));
        if (!success) {
            System.out.println("File renaming Got failed...");
            return fileUpload.uploadedFileName();
        } else {
            return renamedFile;
        }
    }
}
