package opencode.sdk.springboot.autoconfigure;

public class Auth {

    private String type = "basic";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
