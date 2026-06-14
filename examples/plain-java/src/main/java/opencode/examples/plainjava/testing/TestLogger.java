package opencode.examples.plainjava.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TestLogger implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(TestLogger.class);

    private final String logFilePath;
    private PrintWriter fileWriter;

    public TestLogger(String logFilePath) {
        this.logFilePath = logFilePath;
        if (logFilePath != null) {
            try {
                fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)));
            } catch (IOException e) {
                logger.error("Failed to open log file: {}", logFilePath, e);
            }
        }
    }

    public void logInfo(String message) {
        logger.info(message);
        if (fileWriter != null) {
            fileWriter.println("[INFO] " + message);
        }
    }

    public void logError(String message, Throwable t) {
        logger.error(message, t);
        if (fileWriter != null) {
            fileWriter.println("[ERROR] " + message);
            if (t != null) {
                t.printStackTrace(fileWriter);
            }
        }
    }

    public void logValidation(ValidationResult result) {
        String message = result.isValid()
                ? "Validation passed: " + result.getMessage()
                : "Validation failed: " + result.getFieldName() + " - " + result.getMessage();

        if (result.isValid()) {
            logger.debug(message);
        } else {
            logger.warn(message);
        }

        if (fileWriter != null) {
            fileWriter.println("[VALIDATION] " + message);
        }
    }

    public void flush() {
        if (fileWriter != null) {
            fileWriter.flush();
        }
    }

    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
}
