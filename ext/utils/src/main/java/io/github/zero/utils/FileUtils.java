package io.github.zero.utils;

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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero.exceptions.FileException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * File Utilities.
 *
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static Path defaultDatadir(@NonNull String root) {
        return Paths.get(System.getProperty("user.home"), root);
    }

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
                logger.debug("Invalid parse URL from " + urlString, e);
            }
        }
        return null;
    }

    /**
     * Read file to text.
     *
     * @param filePath Given file path
     * @return File content in text
     * @throws FileException if error when parsing file path or reading file
     */
    public static String readFileToString(String filePath) {
        Path path = toPath(filePath);
        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            return stream.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new FileException("Error when reading file: " + filePath, e);
        }
    }

    public static String getFileName(URL fileUrl) {
        try {
            return Paths.get(Objects.requireNonNull(fileUrl).toURI()).getFileName().toString();
        } catch (FileSystemNotFoundException | URISyntaxException e) {
            throw new FileException("File URL is wrong syntax", e);
        }
    }

    /**
     * Convert file path from String to {@link Path}.
     *
     * @param filePath      Given file path
     * @param classpathFile Classpath file name if filePath is blank
     * @return File path object
     * @throws IllegalArgumentException if {@code filePath} and {@code classpathFile} is blank
     * @throws FileException            if error when parsing file path or reading file
     */
    public static Path toPath(String filePath, String classpathFile) {
        return Strings.isBlank(filePath) ? getClasspathFile(classpathFile) : toPath(filePath);
    }

    private static Path getClasspathFile(String classpathFile) {
        final Path fileInWorkingDir = Paths.get(".", classpathFile);
        if (fileInWorkingDir.toFile().exists()) {
            return fileInWorkingDir;
        }
        logger.debug("Not found in working dir. Try to get file {} in classloader", classpathFile);
        final URL resource = FileUtils.class.getClassLoader().getResource(Strings.requireNotBlank(classpathFile));
        if (Objects.isNull(resource)) {
            logger.debug("File not found {}", classpathFile);
            throw new FileException("File not found " + classpathFile);
        }
        try {
            return Paths.get(resource.toURI());
        } catch (URISyntaxException ex) {
            throw new FileException(ex);
        }
    }

    /**
     * Convert file path from String to {@link Path}.
     *
     * @param filePath Given file path
     * @return File path object
     * @throws IllegalArgumentException if {@code filePath} is blank
     * @throws FileException            if error when parsing file path or reading file
     */
    public static Path toPath(String filePath) {
        try {
            return Paths.get(URI.create(filePath));
        } catch (IllegalArgumentException | FileSystemNotFoundException | SecurityException ex) {
            String strPath = Strings.requireNotBlank(filePath);
            strPath = strPath.replaceFirst("^(?:file:/)([^/])", "/".equals(File.separator) ? "/$1" : "$1");
            if (logger.isTraceEnabled()) {
                logger.trace("Invalid parse URI: " + strPath + ". Try to parse plain text", ex);
            }
            try {
                return Paths.get(strPath);
            } catch (InvalidPathException ex1) {
                ex1.addSuppressed(ex);
                throw new FileException("Cannot parse file path: " + filePath, ex1);
            }
        }
    }

    /**
     * Open stream from given URL.
     *
     * @param url URL
     * @return input stream
     * @throws NullPointerException if given {@code URL} is null
     * @throws FileException        if error when opening stream
     */
    public static InputStream toStream(URL url) {
        try {
            return Objects.requireNonNull(url).openStream();
        } catch (IOException e) {
            throw new FileException("Cannot open stream via url: " + url.toString(), e);
        }
    }

    /**
     * Open stream from given file.
     *
     * @param file Given file
     * @return input stream
     * @throws NullPointerException if given {@code URL} is null
     * @throws FileException        if file not found
     */
    public static InputStream toStream(File file) {
        try {
            return new FileInputStream(Objects.requireNonNull(file));
        } catch (FileNotFoundException e) {
            throw new FileException("File not found: " + file.toString(), e);
        }
    }

    /**
     * Convert {@link InputStream} to {@code byte array}.
     *
     * @param inputStream Given stream
     * @return Bytes array represents for data in input stream
     * @throws FileException if error when reading stream
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
     * @throws FileException if error when reading stream
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
            throw new FileException("Error when writing stream", e);
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
            if (logger.isTraceEnabled()) {
                logger.trace("Cannot close stream", e);
            }
        }
    }

    static ByteArrayOutputStream convertToByteArray(InputStream inputStream) {
        return writeToOutputStream(inputStream, new ByteArrayOutputStream());
    }

    /**
     * Create new folder inside parent folder
     *
     * @param defaultDataDir default data dir
     * @param parentDir      Given parent folder. Can be {@code blank} to fallback {@code default data dir}
     * @param paths          Given sub paths
     * @return new folder
     * @throws FileException if any error when creating folder
     * @see #resolveDataFolder(Path, String)
     */
    public static String createFolder(Path defaultDataDir, String parentDir, String... paths) {
        Path path = resolveDataFolder(defaultDataDir, parentDir);
        Path[] ps = new Path[] {path};
        Arrays.stream(paths).filter(Strings::isNotBlank).forEach(p -> ps[0] = ps[0].resolve(p));
        path = ps[0];
        File folder = path.toFile();
        if (!folder.exists() && !folder.mkdirs()) {
            throw new FileException("Cannot create folder with path: " + path.toString());
        }
        return path.toAbsolutePath().toString();
    }

    /**
     * Resolve dir to data dir.
     *
     * @param defaultDataDir Default data dir
     * @param dir            Given directory. Can be absolute path or relative
     * @return data dir. {@code $HOME/nubeio} if {@code dir} is {@code blank}.
     */
    public static Path resolveDataFolder(@NonNull Path defaultDataDir, String dir) {
        if (Strings.isNotBlank(dir)) {
            Path path = toPath(dir);
            return path.isAbsolute() ? path : defaultDataDir.resolve(dir);
        }
        return defaultDataDir;
    }

    public static boolean isChild(@NonNull Path parent, @NonNull Path child) {
        return child.toAbsolutePath().startsWith(parent);
    }

    /**
     * Escape all invalid character in file name to {@code underscore (_)}"
     *
     * @param fileName given file name
     * @return normalization file name
     */
    public static String normalize(@NonNull String fileName) {
        return fileName.replaceAll("[\\\\:*?\"<>|]", "_").replaceAll("_+", "_");
    }

    /**
     * Recompute data dir
     *
     * @param defaultDataDir
     * @param dataDir        Given data dir
     * @param resolvePath    Current folder might in absolute or relative to
     * @return new data dir path with normalize file name
     * @see #normalize(String)
     */
    public static Path recomputeDataDir(@NonNull Path defaultDataDir, @NonNull Path dataDir,
                                        @NonNull String resolvePath) {
        Path path = toPath(resolvePath);
        if (!path.isAbsolute()) {
            return dataDir.resolve(normalize(resolvePath));
        }
        if (isChild(defaultDataDir, path)) {
            if (defaultDataDir.equals(dataDir)) {
                return path;
            }
            return dataDir.resolve(defaultDataDir.relativize(path).toString().replaceAll("^/", ""));
        }
        return path;
    }

    public static String getExtension(String filename) {
        return Optional.ofNullable(filename)
                       .filter(f -> f.contains("."))
                       .map(f -> f.substring(filename.lastIndexOf(".") + 1))
                       .orElse("");
    }

}
