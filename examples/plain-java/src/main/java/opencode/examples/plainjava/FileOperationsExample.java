package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResourceTracker;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.DefaultApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;

public class FileOperationsExample {

    private static final Logger logger = LoggerFactory.getLogger(FileOperationsExample.class);

    private final DefaultApi api;
    private final ResponseValidator validator;
    private final ResourceTracker tracker;

    public FileOperationsExample(DefaultApi api) {
        this.api = api;
        this.validator = null;
        this.tracker = null;
    }

    public FileOperationsExample(ExampleContext context) {
        this.api = context.getDefaultApi();
        this.validator = context.getValidator();
        this.tracker = context.getResourceTracker();
    }

    public void demonstrateFileOperations() {
        try {
            logger.info("=== File Operations Example ===");

            // List files in root directory
            listFiles(".");

            // Read a file
            readFile("pom.xml");

            // Get git status
            getFileStatus();

            // Find files
            findFiles("*.java");

            // Search text in files
            findText("class");

            // Find symbols
            findSymbols("File");

            logger.info("=== File Operations Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during file operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during file operations: {}", e.getMessage(), e);
        }
    }

    private void listFiles(String path) throws ApiException {
        logger.info("\n--- Listing Files in Directory: {} ---", path);

        List<FileNode> files = api.fileList(
                path,
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(files, "files");
        }

        logger.info("Found {} entries:", files.size());
        int fileCount = 0;
        int dirCount = 0;
        for (FileNode file : files) {
            if (validator != null) {
                validator.validateNonNull(file.getName(), "file name");
                validator.validateNonNull(file.getPath(), "file path");
                validator.validateNonNull(file.getType(), "file type");
            }

            String type = file.getType() == FileNode.TypeEnum.DIRECTORY ? "[DIR]" : "[FILE]";
            logger.info("  {} {} - {}", type, file.getName(), file.getPath());
            if (file.getType() == FileNode.TypeEnum.DIRECTORY) {
                dirCount++;
            } else {
                fileCount++;
            }
        }
        logger.info("Summary: {} files, {} directories", fileCount, dirCount);
    }

    private void readFile(String path) throws ApiException {
        logger.info("\n--- Reading File: {} ---", path);

        FileContent content = api.fileRead(
                path,
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateNonNull(content, "file content");
            validator.validateNonNull(content.getType(), "content type");
            validator.validateNonNull(content.getContent(), "content");
        }

        logger.info("File Type: {}", content.getType());
        logger.info("MIME Type: {}", content.getMimeType());
        logger.info("Content Length: {} characters", content.getContent().length());
        logger.info("First 200 characters:");
        String preview = content.getContent().substring(0, Math.min(200, content.getContent().length()));
        for (String line : preview.split("\n")) {
            logger.info("  {}", line);
        }
    }

    private void getFileStatus() throws ApiException {
        logger.info("\n--- Getting Git File Status ---");

        List<ModelFile> files = api.fileStatus(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(files, "file status");
        }

        if (files.isEmpty()) {
            logger.info("No modified files in git");
        } else {
            logger.info("Found {} files with status changes:", files.size());
            for (ModelFile file : files) {
                logger.info("  {}: {} (added: {}, removed: {})",
                        file.getStatus(),
                        file.getPath(),
                        file.getAdded(),
                        file.getRemoved());
            }
        }
    }

    private void findFiles(String pattern) throws ApiException {
        logger.info("\n--- Finding Files: {} ---", pattern);

        List<String> files = api.findFiles(
                pattern,
                null,  // directory
                null,  // workspace
                null,  // dirs
                null,  // type
                10     // limit
        );

        if (validator != null) {
            validator.validateCollection(files, "found files");
        }

        logger.info("Found {} files matching '{}'", files.size(), pattern);
        for (String file : files) {
            logger.info("  - {}", file);
        }
    }

    private void findText(String searchPattern) throws ApiException {
        logger.info("\n--- Searching Text: {} ---", searchPattern);

        List<FindText200ResponseInner> results = api.findText(
                searchPattern,
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(results, "search results");
        }

        logger.info("Found {} matches for '{}'", results.size(), searchPattern);
        int limit = Math.min(5, results.size());
        for (int i = 0; i < limit; i++) {
            FindText200ResponseInner result = results.get(i);
            logger.info("  Match {}: Line {} in {}",
                    i + 1,
                    result.getLineNumber(),
                    result.getPath().getText());
        }
        if (results.size() > 5) {
            logger.info("  ... and {} more matches", results.size() - 5);
        }
    }

    private void findSymbols(String query) throws ApiException {
        logger.info("\n--- Finding Symbols: {} ---", query);

        List<Symbol> symbols = api.findSymbols(
                query,
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(symbols, "symbols");
        }

        logger.info("Found {} symbols matching '{}'", symbols.size(), query);
        int limit = Math.min(10, symbols.size());
        for (int i = 0; i < limit; i++) {
            Symbol symbol = symbols.get(i);
            if (validator != null) {
                validator.validateNonNull(symbol.getName(), "symbol name");
                validator.validateNonNull(symbol.getKind(), "symbol kind");
            }

            logger.info("  Symbol: {} (kind: {}) at {} - range: {}",
                    symbol.getName(),
                    symbol.getKind(),
                    symbol.getLocation().getUri(),
                    symbol.getLocation().getRange());
        }
        if (symbols.size() > 10) {
            logger.info("  ... and {} more symbols", symbols.size() - 10);
        }
    }

    public static void main(String[] args) {
        logger.info("Starting File Operations Example");

        // Configure the client with Basic Auth
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:4096");
        String credentials = "opencode:opencode123";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));
        DefaultApi api = new DefaultApi(apiClient);

        try {
            // Verify connection with health check
            var health = api.globalHealth();
            logger.info("Connected to OpenCode server (version: {})", health.getVersion());

            // Run the example
            FileOperationsExample example = new FileOperationsExample(api);
            example.demonstrateFileOperations();

        } catch (ApiException e) {
            System.err.println("Failed to connect to OpenCode server: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
