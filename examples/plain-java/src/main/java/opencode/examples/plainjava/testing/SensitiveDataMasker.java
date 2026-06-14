package opencode.examples.plainjava.testing;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

public class SensitiveDataMasker {

    private static final String MASK = "***";
    private static final String CREDENTIALS_SEPARATOR = ":";
    private static final String BEARER_SCHEME = "Bearer ";
    private static final String AUTHORIZATION_HEADER_PREFIX = "Authorization:";
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile("Bearer\\s+\\S+");
    private static final Pattern AUTHORIZATION_HEADER_PATTERN =
            Pattern.compile("Authorization:\\s*\\S+\\s+\\S+");

    public static String maskPassword(String text, String password) {
        if (text == null || password == null || password.isEmpty()) {
            return text;
        }
        return text.replace(password, MASK);
    }

    public static String maskApiKey(String text, String apiKey) {
        if (text == null || apiKey == null || apiKey.isEmpty()) {
            return text;
        }
        return text.replace(apiKey, MASK);
    }

    public static String maskBase64Credentials(String text, String username, String password) {
        if (text == null || username == null || username.isEmpty()
                || password == null || password.isEmpty()) {
            return text;
        }
        String credentials = username + CREDENTIALS_SEPARATOR + password;
        String encoded = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
        return text.replace(encoded, MASK);
    }

    public static String maskBearerToken(String text) {
        if (text == null) {
            return text;
        }
        return BEARER_TOKEN_PATTERN.matcher(text).replaceAll(BEARER_SCHEME + MASK);
    }

    public static String maskAuthorizationHeader(String text) {
        if (text == null) {
            return text;
        }
        return AUTHORIZATION_HEADER_PATTERN.matcher(text)
                .replaceAll(AUTHORIZATION_HEADER_PREFIX + " " + MASK);
    }

    public static String maskAllSensitiveData(String text, TestConfiguration config) {
        if (text == null || config == null) {
            return text;
        }

        String result = text;
        result = maskPassword(result, config.getPassword());
        result = maskApiKey(result, config.getProviderApiKey());
        result = maskBase64Credentials(result, config.getUsername(), config.getPassword());
        result = maskBearerToken(result);
        result = maskAuthorizationHeader(result);
        return result;
    }
}
