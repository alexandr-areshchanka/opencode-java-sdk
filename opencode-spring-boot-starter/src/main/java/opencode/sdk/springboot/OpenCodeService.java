package opencode.sdk.springboot;

import opencode.sdk.api.ConfigApi;
import opencode.sdk.api.ControlApi;
import opencode.sdk.api.EventApi;
import opencode.sdk.api.ExperimentalApi;
import opencode.sdk.api.FileApi;
import opencode.sdk.api.GlobalApi;
import opencode.sdk.api.InstanceApi;
import opencode.sdk.api.McpApi;
import opencode.sdk.api.PermissionApi;
import opencode.sdk.api.ProjectApi;
import opencode.sdk.api.ProviderApi;
import opencode.sdk.api.PtyApi;
import opencode.sdk.api.PtyWsApi;
import opencode.sdk.api.QuestionApi;
import opencode.sdk.api.SessionApi;
import opencode.sdk.api.SyncApi;
import opencode.sdk.api.TuiApi;
import opencode.sdk.api.V2Api;
import opencode.sdk.api.V2MessagesApi;
import opencode.sdk.api.V2ModelsApi;
import opencode.sdk.api.V2ProvidersApi;
import opencode.sdk.api.WorkspaceApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.GlobalHealth200Response;
import org.springframework.stereotype.Service;

@Service
public class OpenCodeService {

    private final ApiClient apiClient;

    public OpenCodeService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public GlobalHealth200Response getHealth() throws ApiException {
        return new GlobalApi(apiClient).globalHealth();
    }

    public ConfigApi configApi() {
        return new ConfigApi(apiClient);
    }

    public ControlApi controlApi() {
        return new ControlApi(apiClient);
    }

    public EventApi eventApi() {
        return new EventApi(apiClient);
    }

    public ExperimentalApi experimentalApi() {
        return new ExperimentalApi(apiClient);
    }

    public FileApi fileApi() {
        return new FileApi(apiClient);
    }

    public GlobalApi globalApi() {
        return new GlobalApi(apiClient);
    }

    public InstanceApi instanceApi() {
        return new InstanceApi(apiClient);
    }

    public McpApi mcpApi() {
        return new McpApi(apiClient);
    }

    public PermissionApi permissionApi() {
        return new PermissionApi(apiClient);
    }

    public ProjectApi projectApi() {
        return new ProjectApi(apiClient);
    }

    public ProviderApi providerApi() {
        return new ProviderApi(apiClient);
    }

    public PtyApi ptyApi() {
        return new PtyApi(apiClient);
    }

    public PtyWsApi ptyWsApi() {
        return new PtyWsApi(apiClient);
    }

    public QuestionApi questionApi() {
        return new QuestionApi(apiClient);
    }

    public SessionApi sessionApi() {
        return new SessionApi(apiClient);
    }

    public SyncApi syncApi() {
        return new SyncApi(apiClient);
    }

    public TuiApi tuiApi() {
        return new TuiApi(apiClient);
    }

    public V2Api v2Api() {
        return new V2Api(apiClient);
    }

    public V2MessagesApi v2MessagesApi() {
        return new V2MessagesApi(apiClient);
    }

    public V2ModelsApi v2ModelsApi() {
        return new V2ModelsApi(apiClient);
    }

    public V2ProvidersApi v2ProvidersApi() {
        return new V2ProvidersApi(apiClient);
    }

    public WorkspaceApi workspaceApi() {
        return new WorkspaceApi(apiClient);
    }
}
