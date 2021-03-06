/*
 * Copyright (C) 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fathom.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class Util {

    /**
     * Resource path prefix that specifies to load from a classpath location, value is <b>{@code classpath:}</b>
     */
    public static final String CLASSPATH_PREFIX = "classpath:";
    /**
     * Resource path prefix that specifies to load from a url location, value is <b>{@code url:}</b>
     */
    public static final String URL_PREFIX = "url:";
    /**
     * Resource path prefix that specifies to load from a file location, value is <b>{@code file:}</b>
     */
    public static final String FILE_PREFIX = "file:";
    /**
     * Private internal log instance.
     */
    private static final Logger log = LoggerFactory.getLogger(Util.class);

    public static String toString(Method method) {
        return toString(method.getDeclaringClass(), method.getName());
    }

    public static String toString(Class<?> aClass, String method) {
        return aClass.getName() + "::" + method;
    }

    public static String toString(Class<? extends Collection> collectionType, Class<?> objectType) {
        if (collectionType == null) {
            return objectType.getSimpleName();
        }
        return collectionType.getSimpleName() + "<" + objectType.getSimpleName() + ">";
    }


    /**
     * Returns {@code true} if the resource path is not null and starts with one of the recognized
     * resource prefixes ({@link #CLASSPATH_PREFIX CLASSPATH_PREFIX},
     * {@link #URL_PREFIX URL_PREFIX}, or {@link #FILE_PREFIX FILE_PREFIX}), {@code false} otherwise.
     *
     * @param resourcePath the resource path to check
     * @return {@code true} if the resource path is not null and starts with one of the recognized
     * resource prefixes, {@code false} otherwise.
     * @since 0.9
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static boolean hasResourcePrefix(String resourcePath) {
        return resourcePath != null &&
                (resourcePath.startsWith(CLASSPATH_PREFIX) ||
                        resourcePath.startsWith(URL_PREFIX) ||
                        resourcePath.startsWith(FILE_PREFIX));
    }

    /**
     * Returns {@code true} if the resource at the specified path exists, {@code false} otherwise.  This
     * method supports scheme prefixes on the path as defined in {@link #getInputStreamForPath(String)}.
     *
     * @param resourcePath the path of the resource to check.
     * @return {@code true} if the resource at the specified path exists, {@code false} otherwise.
     * @since 0.9
     */
    public static boolean resourceExists(String resourcePath) {
        InputStream stream = null;
        boolean exists = false;

        try {
            stream = getInputStreamForPath(resourcePath);
            exists = true;
        } catch (IOException e) {
            stream = null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }

        return exists;
    }


    /**
     * Returns the InputStream for the resource represented by the specified path, supporting scheme
     * prefixes that direct how to acquire the input stream
     * ({@link #CLASSPATH_PREFIX CLASSPATH_PREFIX},
     * {@link #URL_PREFIX URL_PREFIX}, or {@link #FILE_PREFIX FILE_PREFIX}).  If the path is not prefixed by one
     * of these schemes, the path is assumed to be a file-based path that can be loaded with a
     * {@link java.io.FileInputStream FileInputStream}.
     *
     * @param resourcePath the String path representing the resource to obtain.
     * @return the InputStream for the specified resource.
     * @throws IOException if there is a problem acquiring the resource at the specified path.
     */
    public static InputStream getInputStreamForPath(String resourcePath) throws IOException {

        InputStream is;
        if (resourcePath.startsWith(CLASSPATH_PREFIX)) {
            is = loadFromClassPath(stripPrefix(resourcePath));

        } else if (resourcePath.startsWith(URL_PREFIX)) {
            is = loadFromUrl(stripPrefix(resourcePath));

        } else if (resourcePath.startsWith(FILE_PREFIX)) {
            is = loadFromFile(stripPrefix(resourcePath));

        } else {
            is = loadFromFile(resourcePath);
        }

        if (is == null) {
            throw new IOException("Resource [" + resourcePath + "] could not be found.");
        }

        return is;
    }

    private static InputStream loadFromFile(String path) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Opening file [" + path + "]...");
        }
        return new FileInputStream(path);
    }

    private static InputStream loadFromUrl(String urlPath) throws IOException {
        log.debug("Opening url {}", urlPath);
        URL url = new URL(urlPath);
        return url.openStream();
    }

    private static InputStream loadFromClassPath(String path) throws IOException {
        log.debug("Opening resource from classpath '{}'", path);
        return ClassUtil.getResource(path).openStream();
    }

    private static String stripPrefix(String resourcePath) {
        return resourcePath.substring(resourcePath.indexOf(":") + 1);
    }

    /**
     * Convenience method that closes the specified {@link InputStream InputStream}, logging any
     * {@link IOException IOException} that might occur. If the {@code InputStream}
     * argument is {@code null}, this method does nothing.  It returns quietly in all cases.
     *
     * @param is the {@code InputStream} to close, logging any {@code IOException} that might occur.
     */
    public static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                log.warn("Error closing input stream.", e);
            }
        }
    }

    public static Set<String> splitToSet(String value, String delimiter) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String chunk : value.split(delimiter)) {
            set.add(chunk.trim());
        }
        return set;
    }

    public static void logSetting(Logger log, Enum<?> name, Object value) {
        logSetting(log, name.toString(), value);
    }

    public static void logSetting(Logger log, String name, Object value) {
        log.info(Strings.padEnd(name, 32, '.') + (value == null ? "" : (name.toLowerCase().contains("password") ? "<masked>" : value.toString())));
    }

    public static String getPreSubstring(String input, char c) {
        int i = input.indexOf(c);
        if (i > -1) {
            return input.substring(0, i);
        }
        return input;
    }

    public static String getPostSubstring(String input, char c) {
        int i = input.indexOf(c);
        if (i > -1) {
            return input.substring(i + 1);
        }
        return input;
    }

}
