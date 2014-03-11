package fr.wayis.framework.test.runner.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * Utils to manage stream.
 */
public class StreamUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamUtils.class);

    /**
     * Closes a stream quietly.<br>
     * Tests if the stream is opened before to close it.<br>
     * If an error occurred, the exception is logged.
     *
     * @param closeable
     *            The stream to close.
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
        }
    }

}
