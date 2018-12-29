package com.nubeiot.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.exceptions.NubeException;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * File Utilities.
 *
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
    public static final Path DEFAULT_DATADIR = Paths.get(System.getProperty("user.home"), ".nubeio");

    /**
     * To URL.
     *
     * @param urlString url string
     * @return url Return {@code null} if given input is invalid {@code URI} syntax or {@code blank} value.
     */
    public static URL toUrl(String urlString) {
        if (Strings.isNotBlank(urlString)) {
            try {
                return new URL(urlString);
            } catch (MalformedURLException e) {
                logger.debug("Invalid parse URL from {}", e, urlString);
            }
        }
        return null;
    }

    /**
     * Read file to text.
     *
     * @param filePath Given file path
     * @return File content in text
     * @throws NubeException if error when parsing file path or reading file
     */
    public static String readFileToString(String filePath) {
        Path path = toPath(filePath);
        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            return stream.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new NubeException("Error when reading file: " + filePath, e);
        }
    }

    public static String getFileName(URL fileUrl) {
        try {
            return Paths.get(Objects.requireNonNull(fileUrl).toURI()).getFileName().toString();
        } catch (FileSystemNotFoundException | URISyntaxException e) {
            throw new NubeException("File URL is wrong syntax", e);
        }
    }

    /**
     * Convert file path from String to {@link Path}.
     *
     * @param filePath      Given file path
     * @param classpathFile Classpath file name if filePath is blank
     * @return File path object
     * @throws IllegalArgumentException if {@code filePath} and {@code classpathFile} is blank
     * @throws NubeException            if error when parsing file path or reading file
     */
    public static Path toPath(String filePath, String classpathFile) {
        return Strings.isBlank(filePath) ? getClasspathFile(classpathFile) : toPath(filePath);
    }

    private static Path getClasspathFile(String classpathFile) {
        final Path fileInWorkingDir = Paths.get(".", classpathFile);
        if (fileInWorkingDir.toFile().exists()) {
            return fileInWorkingDir;
        }
        logger.warn("Not found in working dir. Try to get file {} in classloader", classpathFile);
        final URL resource = FileUtils.class.getClassLoader().getResource(Strings.requireNotBlank(classpathFile));
        if (Objects.isNull(resource)) {
            logger.warn("File not found {}", classpathFile);
            throw new NubeException("File not found " + classpathFile);
        }
        try {
            return Paths.get(resource.toURI());
        } catch (URISyntaxException ex) {
            throw new NubeException(ex);
        }
    }

    /**
     * Convert file path from String to {@link Path}.
     *
     * @param filePath Given file path
     * @return File path object
     * @throws IllegalArgumentException if {@code filePath} is blank
     * @throws NubeException            if error when parsing file path or reading file
     */
    public static Path toPath(String filePath) {
        String strPath = Strings.requireNotBlank(filePath);
        strPath = strPath.replaceFirst("^(?:file:/)([^/])", "/".equals(File.separator) ? "/$1" : "$1");
        try {
            return Paths.get(URI.create(filePath));
        } catch (IllegalArgumentException | FileSystemNotFoundException | SecurityException ex) {
            logger.warn(ex, "Invalid parse URI: {0}. Try to parse plain text", strPath);
            try {
                return Paths.get(strPath);
            } catch (InvalidPathException ex1) {
                ex1.addSuppressed(ex);
                throw new NubeException("Cannot parse file path: " + filePath, ex1);
            }
        }
    }

    /**
     * Open stream from given URL.
     *
     * @param url URL
     * @return input stream
     * @throws NullPointerException if given {@code URL} is null
     * @throws NubeException        if error when opening stream
     */
    public static InputStream toStream(URL url) {
        try {
            return Objects.requireNonNull(url).openStream();
        } catch (IOException e) {
            throw new NubeException("Cannot open stream via url: " + url.toString(), e);
        }
    }

    /**
     * Open stream from given file.
     *
     * @param file Given file
     * @return input stream
     * @throws NullPointerException if given {@code URL} is null
     * @throws NubeException        if file not found
     */
    public static InputStream toStream(File file) {
        try {
            return new FileInputStream(Objects.requireNonNull(file));
        } catch (FileNotFoundException e) {
            throw new NubeException("File not found: " + file.toString(), e);
        }
    }

    /**
     * Convert {@link InputStream} to {@code byte array}.
     *
     * @param inputStream Given stream
     * @return Bytes array represents for data in input stream
     * @throws NubeException if error when reading stream
     */
    public static byte[] convertToBytes(InputStream inputStream) {
        return convertToByteArray(inputStream).toByteArray();
    }

    /**
     * Write data from {@link InputStream} to {@link OutputStream}.
     *
     * @param inputStream  Given input stream
     * @param outputStream Given output stream
     * @param <T>          Type of output stream
     * @return Given Output Stream
     * @throws NubeException if error when reading stream
     */
    public static <T extends OutputStream> T writeToOutputStream(InputStream inputStream, T outputStream) {
        Objects.requireNonNull(outputStream, "Output stream is null");
        Objects.requireNonNull(inputStream, "Input stream is null");
        try {
            int length;
            byte[] buffer = new byte[1024];
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            return outputStream;
        } catch (IOException e) {
            throw new NubeException("Error when writing stream", e);
        } finally {
            silentClose(inputStream);
        }
    }

    /**
     * Silent close stream.
     *
     * @param stream {@link Closeable}
     */
    public static void silentClose(Closeable stream) {
        if (Objects.isNull(stream)) {
            return;
        }
        try {
            stream.close();
        } catch (IOException e) {
            logger.trace(e, "Cannot close stream");
        }
    }

    static ByteArrayOutputStream convertToByteArray(InputStream inputStream) {
        return writeToOutputStream(inputStream, new ByteArrayOutputStream());
    }

    /**
     * Create new folder inside parent folder
     *
     * @param parentDir Given parent folder. Can be {@code blank} to fallback {@code default data dir}
     * @param paths     Given sub paths
     * @return new folder
     * @throws NubeException if any error when creating folder
     * @see #resolveDataFolder(String)
     */
    public static String createFolder(String parentDir, String... paths) {
        Path path = resolveDataFolder(parentDir);
        Arrays.stream(paths).filter(Strings::isNotBlank).forEach(path::resolve);
        File folder = path.toFile();
        if (!folder.exists() && !folder.mkdirs()) {
            throw new NubeException("Cannot create folder with path: " + path.toString());
        }
        return path.toAbsolutePath().toString();
    }

    /**
     * Resolve dir to data dir.
     *
     * @param dir Given directory. Can be absolute path or relative
     * @return data dir. {@code $HOME/nubeio} if {@code dir} is {@code blank}.
     */
    public static Path resolveDataFolder(String dir) {
        if (Strings.isNotBlank(dir)) {
            Path path = toPath(dir);
            return path.isAbsolute() ? path : DEFAULT_DATADIR.resolve(dir);
        }
        return DEFAULT_DATADIR;
    }

}
