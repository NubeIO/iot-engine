package com.nubeiot.dashboard.utils;

import java.io.File;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.FileUpload;

/**
 * @deprecated
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static String appendRealFileNameWithExtension(FileUpload fileUpload) {
        File tempFile = new File(fileUpload.uploadedFileName());
        String renamedFile = fileUpload.uploadedFileName() + fileUpload.fileName().replace(" ", "+");
        boolean success = tempFile.renameTo(new File(renamedFile));
        if (!success) {
            logger.error("File renaming Got failed...");
            return fileUpload.uploadedFileName();
        } else {
            return renamedFile;
        }
    }

}
