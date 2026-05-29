package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.GlobalApi;
import opencode.sdk.api.InstanceApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.Agent;
import opencode.sdk.model.AppSkills200ResponseInner;
import opencode.sdk.model.Command;
import opencode.sdk.model.GlobalHealth200Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;

public class SystemInfoExample {

    private static final Logger logger = LoggerFactory.getLogger(SystemInfoExample.class);

    private final GlobalApi globalApi;
    private final InstanceApi instanceApi;
    private final ResponseValidator validator;

    public SystemInfoExample(ApiClient apiClient) {
        this.globalApi = new GlobalApi(apiClient);
        this.instanceApi = new InstanceApi(apiClient);
        this.validator = null;
    }

    public SystemInfoExample(ExampleContext context) {
        this.globalApi = new GlobalApi(context.getApiClient());
        this.instanceApi = new InstanceApi(context.getApiClient());
        this.validator = context.getValidator();
    }

    public void demonstrateSystemInfo() {
        try {
            logger.info("=== System Info Example ===");

            // Check health
            checkHealth();

            // List agents
            listAgents();

            // List skills
            listSkills();

            // List commands
            listCommands();

            logger.info("=== System Info Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during system info operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during system info operations: {}", e.getMessage(), e);
        }
    }

    private void checkHealth() throws ApiException {
        logger.info("\n--- Checking Server Health ---");

        GlobalHealth200Response health = globalApi.globalHealth();

        if (validator != null) {
            validator.validateNonNull(health, "health response");
            validator.validateNonNull(health.getHealthy(), "healthy");
            validator.validateNonNull(health.getVersion(), "version");
        }

        logger.info("Health check successful!");
        logger.info("  Healthy: {}", health.getHealthy());
        logger.info("  Version: {}", health.getVersion());
    }

    private void listAgents() throws ApiException {
        logger.info("\n--- Listing Agents ---");

        List<Agent> agents = instanceApi.appAgents(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(agents, "agents");
        }

        logger.info("Found {} agents:", agents.size());
        for (Agent agent : agents) {
            if (validator != null) {
                validator.validateNonNull(agent.getName(), "agent name");
                validator.validateNonNull(agent.getMode(), "agent mode");
            }

            logger.info("  - Name: {}", agent.getName());
            if (agent.getDescription() != null) {
                logger.info("    Description: {}", agent.getDescription());
            }
            logger.info("    Mode: {}", agent.getMode());
            if (agent.getModel() != null) {
                logger.info("    Model: {}", agent.getModel());
            }
        }
    }

    private void listSkills() throws ApiException {
        logger.info("\n--- Listing Skills ---");

        List<AppSkills200ResponseInner> skills = instanceApi.appSkills(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(skills, "skills");
        }

        logger.info("Found {} skills:", skills.size());
        for (AppSkills200ResponseInner skill : skills) {
            if (validator != null) {
                validator.validateNonNull(skill.getName(), "skill name");
            }

            logger.info("  - Name: {}", skill.getName());
            logger.info("    Description: {}", skill.getDescription());
            logger.info("    Location: {}", skill.getLocation());
        }
    }

    private void listCommands() throws ApiException {
        logger.info("\n--- Listing Commands ---");

        List<Command> commands = instanceApi.commandList(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(commands, "commands");
        }

        logger.info("Found {} commands:", commands.size());
        for (Command command : commands) {
            if (validator != null) {
                validator.validateNonNull(command.getName(), "command name");
            }

            logger.info("  - Name: {}", command.getName());
            if (command.getDescription() != null) {
                logger.info("    Description: {}", command.getDescription());
            }
            if (command.getAgent() != null) {
                logger.info("    Agent: {}", command.getAgent());
            }
            if (command.getSource() != null) {
                logger.info("    Source: {}", command.getSource());
            }
        }
    }

    public static void main(String[] args) {
        logger.info("Starting System Info Example");
        logger.info("============================");

        // Configure the client with Basic Auth
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:4096");
        String credentials = "opencode:opencode123";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));

        try {
            // Run the example
            SystemInfoExample example = new SystemInfoExample(apiClient);
            example.demonstrateSystemInfo();

            logger.info("\n");
            logger.info("============================");
            logger.info("Example completed successfully!");

        } catch (Exception e) {
            logger.error("Error running example: {}", e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}
