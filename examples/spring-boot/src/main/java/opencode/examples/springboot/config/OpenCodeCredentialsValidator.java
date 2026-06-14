package opencode.examples.springboot.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class OpenCodeCredentialsValidator {

    @Value("${opencode.auth.type:basic}")
    private String authType;

    @Value("${opencode.username:#{null}}")
    private String username;

    @Value("${opencode.password:#{null}}")
    private String password;

    @Value("${opencode.bearer-token:#{null}}")
    private String bearerToken;

    @PostConstruct
    public void validate() {
        String type = (authType == null ? "basic" : authType.trim().toLowerCase(Locale.ROOT));

        switch (type) {
            case "none":
                return;
            case "bearer":
                if (bearerToken == null || bearerToken.isBlank()) {
                    throw new IllegalStateException(
                            "OpenCode bearer authentication is enabled (opencode.auth.type=bearer) "
                                    + "but 'opencode.bearer-token' is not set. Set it in your configuration "
                                    + "(e.g. application.properties, environment variable OPENCODE_BEARER_TOKEN, "
                                    + "or command-line argument).");
                }
                return;
            case "basic":
            default:
                if (isBlank(username)) {
                    throw new IllegalStateException(
                            "OpenCode basic authentication is enabled (opencode.auth.type=basic, the default) "
                                    + "but 'opencode.username' is not set. Set it in your configuration "
                                    + "(e.g. application.properties or environment variable OPENCODE_USERNAME).");
                }
                if (isBlank(password)) {
                    throw new IllegalStateException(
                            "OpenCode basic authentication is enabled (opencode.auth.type=basic, the default) "
                                    + "but 'opencode.password' is not set. Set it in your configuration "
                                    + "(e.g. application.properties, environment variable OPENCODE_PASSWORD, "
                                    + "or command-line argument).");
                }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
