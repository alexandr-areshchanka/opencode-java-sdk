package opencode.sdk.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Objects;

import opencode.sdk.invoker.ApiClient;

/**
 * Represents an anyOf schema that accepts any value (including null).
 * Workaround for OpenAPI Generator bug: the generator references this class
 * for schemas like {@code anyOf: [{}, {"type": "null"}]} but never creates it.
 */
public class AnyOf extends AbstractOpenApiSchema {

    private static final String SCHEMA_TYPE = "anyOf";

    public AnyOf() {
        super(SCHEMA_TYPE, Boolean.TRUE);
    }

    public AnyOf(Object value) {
        super(SCHEMA_TYPE, Boolean.TRUE);
        setActualInstance(value);
    }

    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    @Override
    public Map<String, Class<?>> getSchemas() {
        return Collections.emptyMap();
    }

    @Override
    public void setActualInstance(Object instance) {
        super.setActualInstance(instance);
    }

    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    public String toUrlQueryString(String prefix) {
        Object instance = getActualInstance();
        if (instance == null) {
            return null;
        }

        String suffix = "";
        if (prefix == null) {
            prefix = "";
        } else {
            prefix = prefix + "[";
            suffix = "]";
        }

        StringJoiner joiner = new StringJoiner("&");
        joiner.add(String.format(java.util.Locale.ROOT, "%sextra%s=%s",
                prefix, suffix,
                ApiClient.urlEncode(ApiClient.valueToString(instance))));
        return joiner.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AnyOf {\n");
        sb.append("    instance: ").append(toIndentedString(getActualInstance())).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnyOf other = (AnyOf) o;
        return Objects.equals(getActualInstance(), other.getActualInstance());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getActualInstance());
    }
}
