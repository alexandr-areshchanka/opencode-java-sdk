# SDK Module Current API Surface

## Build Configuration (sdk/pom.xml)

- **Generator**: `openapi-generator-maven-plugin` (active, NOT commented out despite comment text)
- **Library**: `java` / `native` (uses Java HttpClient)
- **Generator version**: Managed by parent POM
- **API Package**: `opencode.sdk.api`
- **Model Package**: `opencode.sdk.model`
- **Invoker Package**: `opencode.sdk.invoker`
- **Config options**:
  - `useJakartaEe=true` (Jakarta EE 11, `jakarta.annotation-api` 3.0.0)
  - `dateLibrary=java8`
  - `openApiNullable=false`
  - `generateApiTests=false`, `generateModelTests=false`
  - `generateApiDocumentation=false`, `generateModelDocumentation=false`
  - `useSingleRequestParameter=false`
- **Post-processing**: `maven-antrun-plugin` fixes `Map<K,V>.class` and `getMap<K,V>()` syntax bugs
- **Dependencies**: Jackson 2.21.1, SLF4J 2.0.16, Jakarta Annotation API
- **Ignore file** (`src/main/resources/.openapi-generator-ignore`): Excludes `docs/` and `src/test/`

## Manual Source Files (sdk/src/main/java/)

Only **1 manual file** exists:

### `opencode/sdk/model/AnyOf.java`
- **Purpose**: Workaround for OpenAPI Generator bug — generator references `AnyOf` for schemas like `anyOf: [{}, {"type": "null"}]` but never creates the class
- **Extends**: `AbstractOpenApiSchema` (generated)
- **Constructors**: `AnyOf()`, `AnyOf(Object value)`
- **Methods**: `getActualInstance()`, `setActualInstance(Object)`, `getSchemas()`, `toUrlQueryString()`, `toString()`, `equals()`, `hashCode()`

## Generated API Classes (22 classes in `opencode.sdk.api`)

Each API class follows the same pattern:
- **Constructors**: `XxxApi()`, `XxxApi(ApiClient)`
- **Common methods**: `getApiException()`, `formatExceptionMessage()`, `downloadFileFromResponse()`, `prepareDownloadFile()`
- **Endpoint methods** come in 4 variants per endpoint:
  1. `methodName(params...)` — returns T
  2. `methodName(params..., Map<String, String> headers)` — returns T
  3. `methodNameWithHttpInfo(params...)` — returns `ApiResponse<T>`
  4. `methodNameWithHttpInfo(params..., Map<String, String> headers)` — returns `ApiResponse<T>`
  5. `methodNameRequestBuilder(params..., Map<String, String> headers)` — returns `HttpRequest.Builder`
- Parameters use `@jakarta.annotation.Nonnull` and `@jakarta.annotation.Nullable` annotations
- Common query parameters: `directory` (String, @Nullable), `workspace` (String, @Nullable)

### ConfigApi (581 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `configGet` | `String directory, String workspace` | `Config` |
| `configProviders` | `String directory, String workspace` | `ConfigProviders200Response` |
| `configUpdate` | `String directory, String workspace, Config body` | `Config` |

### ControlApi (558 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `appLog` | `String directory, String workspace, AppLogRequest body` | `Boolean` |
| `authRemove` | `String providerID` | `Boolean` |
| `authSet` | `String providerID, Auth body` | `Boolean` |

### EventApi (300 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `eventSubscribe` | `String directory, String workspace` | `Event` |

### ExperimentalApi (1751 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `experimentalConsoleGet` | `String directory, String workspace` | `ConsoleState` |
| `experimentalConsoleListOrgs` | `String directory, String workspace` | `ExperimentalConsoleListOrgs200Response` |
| `experimentalConsoleSwitchOrg` | `String directory, String workspace, ExperimentalConsoleSwitchOrgRequest body` | `Boolean` |
| `experimentalResourceList` | `String directory, String workspace` | `Map<String, McpResource>` |
| `experimentalSessionList` | `String directory, String workspace, ExperimentalSessionListRootsParameter roots, BigDecimal start, String search, BigDecimal limit, ...` | `List<GlobalSession>` |
| `toolIds` | `String directory, String workspace` | `List<String>` |
| `toolList` | `String directory, String workspace, String agent, String name` | `List<ToolListItem>` |
| `worktreeCreate` | `String directory, String workspace, WorktreeCreateInput body` | `Worktree` |
| `worktreeList` | `String directory, String workspace` | `List<String>` |
| `worktreeRemove` | `String directory, String workspace, WorktreeRemoveInput body` | `Boolean` |
| `worktreeReset` | `String directory, String workspace, WorktreeResetInput body` | `Boolean` |

### FileApi (1043 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `fileList` | `String directory, String workspace, String path` | `List<FileNode>` |
| `fileRead` | `String directory, String workspace, String path` | `FileContent` |
| `fileStatus` | `String directory, String workspace` | `List<ModelFile>` |
| `findFiles` | `String directory, String workspace, String pattern, String glob, String exclude, Integer limit` | `List<String>` |
| `findSymbols` | `String directory, String workspace, String query` | `List<Symbol>` |
| `findText` | `String directory, String workspace, String pattern` | `List<FindText200ResponseInner>` |

### GlobalApi (846 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `globalConfigGet` | _(none)_ | `Config` |
| `globalConfigUpdate` | `Config body` | `Config` |
| `globalDispose` | _(none)_ | `Boolean` |
| `globalEvent` | _(none)_ | `GlobalEvent` |
| `globalHealth` | _(none)_ | `GlobalHealth200Response` |
| `globalUpgrade` | `GlobalUpgradeRequest body` | `GlobalUpgrade200Response` |

### InstanceApi (1812 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `appAgents` | `String directory, String workspace` | `List<Agent>` |
| `appSkills` | `String directory, String workspace` | `List<AppSkills200ResponseInner>` |
| `commandList` | `String directory, String workspace` | `List<Command>` |
| `formatterStatus` | `String directory, String workspace` | `List<FormatterStatus>` |
| `instanceDispose` | `String directory, String workspace` | `Boolean` |
| `lspStatus` | `String directory, String workspace` | `List<LSPStatus>` |
| `pathGet` | `String directory, String workspace` | `Path` |
| `vcsApply` | `String directory, String workspace, VcsApplyRequest body` | `VcsApply200Response` |
| `vcsDiff` | `String directory, String workspace, String messageID, Integer context` | `List<VcsFileDiff>` |
| `vcsDiffRaw` | `String directory, String workspace` | `String` |
| `vcsGet` | `String directory, String workspace` | `VcsInfo` |
| `vcsStatus` | `String directory, String workspace` | `List<VcsFileStatus>` |

### McpApi (1320 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `mcpAdd` | `String directory, String workspace, McpAddRequest body` | `Map<String, MCPStatus>` |
| `mcpAuthAuthenticate` | `String directory, String workspace, String serverName` | `MCPStatus` |
| `mcpAuthCallback` | `String directory, String workspace, String serverName, McpAuthCallbackRequest body` | `MCPStatus` |
| `mcpAuthRemove` | `String directory, String workspace, String serverName` | `McpAuthRemove200Response` |
| `mcpAuthStart` | `String directory, String workspace, String serverName` | `McpAuthStart200Response` |
| `mcpConnect` | `String directory, String workspace, String serverName` | `Boolean` |
| `mcpDisconnect` | `String directory, String workspace, String serverName` | `Boolean` |
| `mcpStatus` | `String directory, String workspace` | `Map<String, MCPStatus>` |

### PermissionApi (457 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `permissionList` | `String directory, String workspace` | `List<PermissionRequest>` |
| `permissionReply` | `String directory, String workspace, String permissionID, PermissionReplyRequest body` | `Boolean` |

### ProjectApi (725 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `projectCurrent` | `String directory, String workspace` | `Project` |
| `projectInitGit` | `String directory, String workspace` | `Project` |
| `projectList` | `String directory, String workspace` | `List<Project>` |
| `projectUpdate` | `String directory, String workspace, String projectID, ProjectUpdateRequest body` | `Project` |

### ProviderApi (746 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `providerAuth` | `String directory, String workspace` | `Map<String, List<ProviderAuthMethod>>` |
| `providerList` | `String directory, String workspace` | `ProviderList200Response` |
| `providerOauthAuthorize` | `String directory, String workspace, String providerID, ProviderOauthAuthorizeRequest body` | `ProviderAuthAuthorization` |
| `providerOauthCallback` | `String directory, String workspace, String providerID, ProviderOauthCallbackRequest body` | `Boolean` |

### PtyApi (1168 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `ptyConnectToken` | `String directory, String workspace, String sessionID` | `PtyConnectToken200Response` |
| `ptyCreate` | `String directory, String workspace, PtyCreateRequest body` | `Pty` |
| `ptyGet` | `String directory, String workspace, String ptyID` | `Pty` |
| `ptyList` | `String directory, String workspace` | `List<Pty>` |
| `ptyRemove` | `String directory, String workspace, String ptyID` | `Boolean` |
| `ptyShells` | `String directory, String workspace` | `List<PtyShells200ResponseInner>` |
| `ptyUpdate` | `String directory, String workspace, String ptyID, PtyUpdateRequest body` | `Pty` |

### PtyWsApi (322 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `ptyConnect` | `String sessionID, String ptyID, String directory, String workspace, String token` | `Boolean` |

### QuestionApi (600 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `questionList` | `String directory, String workspace` | `List<QuestionRequest>` |
| `questionReject` | `String directory, String workspace, String questionID` | `Boolean` |
| `questionReply` | `String directory, String workspace, String questionID, QuestionReplyRequest body` | `Boolean` |

### SessionApi (4258 lines — largest API class)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `partDelete` | `String sessionID, String messageID, String partID, String directory, String workspace` | `Boolean` |
| `partUpdate` | `String sessionID, String messageID, String partID, String directory, String workspace, Part body` | `Part` |
| `permissionRespond` | `String sessionID, String permissionID, String directory, String workspace, PermissionRespondRequest body` | `Boolean` |
| `sessionAbort` | `String sessionID, String directory, String workspace` | `Boolean` |
| `sessionChildren` | `String sessionID, String directory, String workspace` | `List<Session>` |
| `sessionCommand` | `String sessionID, String directory, String workspace, SessionCommandRequest body` | `SessionPrompt200Response` |
| `sessionCreate` | `String directory, String workspace, SessionCreateRequest body` | `Session` |
| `sessionDelete` | `String sessionID, String directory, String workspace` | `Boolean` |
| `sessionDeleteMessage` | `String sessionID, String messageID, String directory, String workspace` | `Boolean` |
| `sessionDiff` | `String sessionID, String directory, String workspace, String messageID` | `List<SnapshotFileDiff>` |
| `sessionFork` | `String sessionID, String directory, String workspace, SessionForkRequest body` | `Session` |
| `sessionGet` | `String sessionID, String directory, String workspace` | `Session` |
| `sessionInit` | `String sessionID, String directory, String workspace, SessionInitRequest body` | `Boolean` |
| `sessionList` | `String directory, String workspace, String scope, String path, ExperimentalSessionListRootsParameter roots, BigDecimal start, String search, BigDecimal limit` | `List<Session>` |
| `sessionMessage` | `String sessionID, String messageID, String directory, String workspace` | `SessionMessage200Response` |
| `sessionMessages` | `String sessionID, String directory, String workspace, Integer limit, String before` | `List<SessionMessages200ResponseInner>` |
| `sessionPrompt` | `String sessionID, String directory, String workspace, SessionPromptRequest body` | `SessionPrompt200Response` |
| `sessionPromptAsync` | `String sessionID, String directory, String workspace, SessionPromptAsyncRequest body` | `void` |
| `sessionRevert` | `String sessionID, String directory, String workspace, SessionRevertRequest body` | `Session` |
| `sessionShare` | `String sessionID, String directory, String workspace` | `Session` |
| `sessionShell` | `String sessionID, String directory, String workspace, SessionShellRequest body` | `SessionShell200Response` |
| `sessionStatus` | `String directory, String workspace` | `Map<String, SessionStatus>` |
| `sessionSummarize` | `String sessionID, String directory, String workspace, SessionSummarizeRequest body` | `Boolean` |
| `sessionTodo` | `String sessionID, String directory, String workspace` | `List<Todo>` |
| `sessionUnrevert` | `String sessionID, String directory, String workspace` | `Session` |
| `sessionUnshare` | `String sessionID, String directory, String workspace` | `Session` |
| `sessionUpdate` | `String sessionID, String directory, String workspace, SessionUpdateRequest body` | `Session` |

### SyncApi (738 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `syncHistoryList` | `String directory, String workspace, Map<String, Integer> body` | `List<SyncHistoryList200ResponseInner>` |
| `syncReplay` | `String directory, String workspace, SyncReplayRequest body` | `SyncReplay200Response` |
| `syncStart` | `String directory, String workspace` | `Boolean` |
| `syncSteal` | `String directory, String workspace, SyncStealRequest body` | `SyncSteal200Response` |

### TuiApi (1976 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `tuiAppendPrompt` | `String directory, String workspace, FindText200ResponseInnerPath body` | `Boolean` |
| `tuiClearPrompt` | `String directory, String workspace` | `Boolean` |
| `tuiControlNext` | `String directory, String workspace` | `TuiControlNext200Response` |
| `tuiControlResponse` | `String directory, String workspace, Object body` | `Boolean` |
| `tuiExecuteCommand` | `String directory, String workspace, TuiExecuteCommandRequest body` | `Boolean` |
| `tuiOpenHelp` | `String directory, String workspace` | `Boolean` |
| `tuiOpenModels` | `String directory, String workspace` | `Boolean` |
| `tuiOpenSessions` | `String directory, String workspace` | `Boolean` |
| `tuiOpenThemes` | `String directory, String workspace` | `Boolean` |
| `tuiPublish` | `String directory, String workspace, TuiPublishRequest body` | `Boolean` |
| `tuiSelectSession` | `String directory, String workspace, TuiSelectSessionRequest body` | `Boolean` |
| `tuiShowToast` | `String directory, String workspace, TuiShowToastRequest body` | `Boolean` |

### V2Api (906 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `v2SessionCompact` | `String sessionID, String directory, String workspace` | `void` |
| `v2SessionContext` | `String sessionID, String directory, String workspace` | `List<SessionMessage>` |
| `v2SessionList` | `String directory, String workspace, BigDecimal limit, String cursor, String search, ExperimentalSessionListRootsParameter roots, BigDecimal start` | `V2SessionsResponse` |
| `v2SessionPrompt` | `String sessionID, String directory, String workspace, V2SessionPromptRequest body` | `SessionMessage` |
| `v2SessionWait` | `String sessionID, String directory, String workspace` | `void` |

### V2MessagesApi (332 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `v2SessionMessages` | `String sessionID, String directory, String workspace, BigDecimal limit, String cursor, String order` | `V2SessionMessagesResponse` |

### V2ModelsApi (303 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `v2ModelList` | `V2ModelListLocationParameter location` | `List<ModelV2Info>` |

### V2ProvidersApi (446 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `v2ProviderGet` | `String providerID, V2ProviderListLocationParameter location` | `ProviderV2Info` |
| `v2ProviderList` | `V2ProviderListLocationParameter location` | `List<ProviderV2Info>` |

### WorkspaceApi (1113 lines)
| Method | Parameters | Return Type |
|--------|-----------|-------------|
| `experimentalWorkspaceAdapterList` | `String directory, String workspace` | `List<ExperimentalWorkspaceAdapterList200ResponseInner>` |
| `experimentalWorkspaceCreate` | `String directory, String workspace, ExperimentalWorkspaceCreateRequest body` | `Workspace` |
| `experimentalWorkspaceList` | `String directory, String workspace` | `List<Workspace>` |
| `experimentalWorkspaceRemove` | `String directory, String workspace, String workspaceID` | `Workspace` |
| `experimentalWorkspaceStatus` | `String directory, String workspace` | `List<ExperimentalWorkspaceStatus200ResponseInner>` |
| `experimentalWorkspaceSyncList` | `String directory, String workspace` | `void` |
| `experimentalWorkspaceWarp` | `String directory, String workspace, ExperimentalWorkspaceWarpRequest body` | `void` |

## Generated Invoker Classes (11 classes in `opencode.sdk.invoker`)

| Class | Description |
|-------|-------------|
| `ApiClient` | Base HTTP client using Java `HttpClient`, handles serialization, auth, request building |
| `ApiException` | Exception class for API errors |
| `ApiResponse<T>` | Generic response wrapper with status code and data |
| `Configuration` | Global SDK configuration singleton |
| `JSON` | Jackson ObjectMapper wrapper for JSON serialization |
| `Pair` | Key-value pair for query parameters |
| `RFC3339DateFormat` | Date format for RFC 3339 |
| `RFC3339InstantDeserializer` | Custom deserializer for RFC 3339 instants |
| `RFC3339JavaTimeModule` | Java time module for RFC 3339 |
| `ServerConfiguration` | Server URL configuration |
| `ServerVariable` | Server variable for URL templates |

## Generated Model Classes (~300+ in `opencode/sdk.model`)

### Core Domain Models
- `Agent`, `AgentConfig`, `AgentConfigColor`, `AgentPart`, `AgentPartInput`, `AgentPartSource`
- `Command`
- `Config` (+ nested: `ConfigAgent`, `ConfigAutoupdate`, `ConfigCommandValue`, `ConfigCompaction`, `ConfigEnterprise`, `ConfigExperimental`, `ConfigFormatter`, `ConfigLsp`, `ConfigMcpValue`, `ConfigMode`, `ConfigPluginInner`, `ConfigSkills`, `ConfigToolOutput`, `ConfigWatcher`)
- `Model`, `ModelApi`, `ModelCapabilities`, `ModelCost`, `ModelFile`
- `Project`, `ProjectSummary`, `ProjectTime`, `ProjectUpdateRequest`
- `Provider`, `ProviderConfig`, `ProviderList200Response`
- `Session`, `SessionInfo`, `SessionStatus`, `SessionSummary`, `SessionTokens`, `SessionTime`

### Session Message Models
- `Message`, `AssistantMessage`, `UserMessage`
- `SessionMessage`, `SessionMessage200Response`
- `SessionMessageAssistant`, `SessionMessageAssistantContentInner`, `SessionMessageAssistantText`, `SessionMessageAssistantTool`, `SessionMessageAssistantToolState`, `SessionMessageAssistantSnapshot`, `SessionMessageAssistantReasoning`
- `SessionMessageCompaction`, `SessionMessageModelSwitched`, `SessionMessageAgentSwitched`, `SessionMessageShell`, `SessionMessageSynthetic`
- `SessionMessageUser`
- `SessionMessages200ResponseInner`
- `SessionMessageToolStateCompleted`, `SessionMessageToolStateError`, `SessionMessageToolStatePending`, `SessionMessageToolStateRunning`

### Session Request/Response Models
- `SessionCreateRequest`, `SessionCreateRequestModel`
- `SessionCommandRequest`, `SessionCommandRequestPartsInner`
- `SessionPromptRequest`, `SessionPromptRequestModel`, `SessionPromptRequestPartsInner`
- `SessionPromptAsyncRequest`, `SessionPromptAsyncRequestModel`
- `SessionPrompt200Response`
- `SessionForkRequest`, `SessionRevertRequest`, `SessionUpdateRequest`, `SessionUpdateRequestTime`
- `SessionInitRequest`, `SessionDelivery`, `SessionShellRequest`
- `SessionShell200Response`
- `SessionSummarizeRequest`
- `SessionErrorUnknown`, `SessionBusyError`, `SessionNotFoundError`, `SessionNextRetryError`

### Part Models
- `Part`, `TextPart`, `TextPartInput`, `TextPartTime`
- `ToolPart`, `ToolState`, `ToolStateCompleted`, `ToolStateError`, `ToolStatePending`, `ToolStateRunning`
- `FilePart`, `FilePartInput`, `FilePartSource`, `FilePartSourceText`
- `AgentPart`, `AgentPartInput`, `AgentPartSource`
- `PatchPart`, `SnapshotPart`, `StepStartPart`, `StepFinishPart`
- `SubtaskPart`, `SubtaskPartInput`
- `CompactionPart`, `RetryPart`, `ReasoningPart`

### Event Models (70+ classes)
- `Event`, `GlobalEvent`, `GlobalEventPayload`
- `EventSessionCreated`, `EventSessionDeleted`, `EventSessionUpdated`, `EventSessionCompacted`
- `EventSessionNext*` (TextDelta, TextStarted, TextEnded, ToolCalled, ToolFailed, ToolSuccess, ToolInputDelta, ToolInputStarted, ToolInputEnded, ToolProgress, StepStarted, StepEnded, StepFailed, ShellStarted, ShellEnded, ReasoningStarted, ReasoningDelta, ReasoningEnded, CompactionStarted, CompactionDelta, CompactionEnded, AgentSwitched, ModelSwitched, Prompted, Retried, Synthetic)
- `EventSessionStatus`, `EventSessionDiff`, `EventSessionError`, `EventSessionIdle`
- `EventMessagePartDelta`, `EventMessagePartUpdated`, `EventMessagePartRemoved`
- `EventMessageUpdated`, `EventMessageRemoved`
- `EventPermissionAsked`, `EventPermissionReplied`
- `EventQuestionAsked`, `EventQuestionReplied`, `EventQuestionRejected`
- `EventFileEdited`, `EventFileWatcherUpdated`
- `EventMcpToolsChanged`, `EventMcpBrowserOpenFailed`
- `EventAccountAdded`, `EventAccountRemoved`, `EventAccountSwitched`
- `EventPtyCreated`, `EventPtyDeleted`, `EventPtyUpdated`, `EventPtyExited`
- `EventCommandExecuted`, `EventTodoUpdated`, `EventVcsBranchUpdated`
- `EventWorkspaceReady`, `EventWorkspaceFailed`, `EventWorkspaceStatus`
- `EventWorktreeReady`, `EventWorktreeFailed`
- `EventLspUpdated`, `EventLspClientDiagnostics`
- `EventServerConnected`, `EventServerInstanceDisposed`, `EventGlobalDisposed`
- `EventInstallationUpdated`, `EventInstallationUpdateAvailable`
- `EventCatalogModelUpdated`, `EventModelsDevRefreshed`
- `EventTuiCommandExecute`, `EventTuiPromptAppend`, `EventTuiSessionSelect`, `EventTuiToastShow`

### Sync Event Models
- `SyncEventSessionCreated`, `SyncEventSessionDeleted`, `SyncEventSessionUpdated`
- `SyncEventSessionNext*` (TextDelta, TextStarted, TextEnded, ToolCalled, ToolFailed, ToolSuccess, ToolInputDelta, ToolInputStarted, ToolInputEnded, ToolProgress, StepStarted, StepEnded, StepFailed, ShellStarted, ShellEnded, ReasoningStarted, ReasoningDelta, ReasoningEnded, CompactionStarted, CompactionDelta, CompactionEnded, AgentSwitched, ModelSwitched, Prompted, Retried, Synthetic)
- `SyncEventMessageUpdated`, `SyncEventMessageRemoved`, `SyncEventMessagePartUpdated`, `SyncEventMessagePartRemoved`

### File/Search Models
- `FileContent`, `FileContentPatch`, `FileContentPatchHunksInner`, `FileNode`, `FileSource`
- `FindText200ResponseInner`, `FindText200ResponseInnerPath`, `FindText200ResponseInnerSubmatchesInner`
- `Symbol`, `SymbolLocation`, `SymbolSource`

### Auth/Permission Models
- `Auth`, `ApiAuth`, `WellKnownAuth`, `OAuth`
- `PermissionAction`, `PermissionActionConfig`, `PermissionConfig`, `PermissionRule`, `PermissionRuleConfig`
- `PermissionRequest`, `PermissionRequestTool`, `PermissionReplyRequest`, `PermissionRespondRequest`

### MCP Models
- `McpAddRequest`, `McpAddRequestConfig`, `McpAuthCallbackRequest`
- `McpLocalConfig`, `McpRemoteConfig`, `McpRemoteConfigOauth`, `McpOAuthConfig`, `McpResource`
- `MCPStatus`, `MCPStatusConnected`, `MCPStatusDisabled`, `MCPStatusFailed`, `MCPStatusNeedsAuth`, `MCPStatusNeedsClientRegistration`

### V2 API Models
- `V2SessionPromptRequest`, `V2SessionsResponse`, `V2SessionsResponseCursor`
- `V2SessionMessagesResponse`, `V2SessionList400Response`, `V2SessionMessages400Response`
- `ModelV2Info`, `ModelV2Info1`, `ModelV2InfoCapabilities`, `ModelV2InfoCostInner`, `ModelV2InfoEndpoint`, `ModelV2InfoLimit`, `ModelV2InfoOptions`, `ModelV2InfoTime`, `ModelV2InfoVariantsInner`
- `ProviderV2Info`, `ProviderV2InfoEnabled`, `ProviderV2InfoOptions`
- `V2ModelListLocationParameter`, `V2ProviderListLocationParameter`

### VCS Models
- `VcsApplyRequest`, `VcsApply200Response`, `VcsApply400Response`, `VcsApplyError`, `VcsFileDiff`, `VcsFileStatus`, `VcsInfo`

### Error Models
- `APIError`, `APIErrorData`
- `BadRequestError`, `BadRequestErrorData`
- `ContextOverflowError`, `ContextOverflowErrorData`
- `NotFoundError`, `NotFoundErrorData`
- `UnauthorizedError`, `ServiceUnavailableError`
- `UnknownError`, `UnknownError1`, `UnknownErrorData`
- `MessageAbortedError`, `MessageAbortedErrorData`, `MessageOutputLengthError`
- `StructuredOutputError`, `StructuredOutputErrorData`
- `InvalidCursorError`, `InvalidRequestError`
- Various `EffectHttpApiError*` classes

### Other Models
- `AbstractOpenApiSchema` — base class for anyOf/oneOf schemas
- `AnyOf` — manual workaround class
- `AppLogRequest`, `AppSkills200ResponseInner`
- `AttachmentConfig`, `ImageAttachmentConfig`
- `ConsoleState`, `FormatterStatus`, `LayoutConfig`, `LSPStatus`, `LogLevel`, `OutputFormat`
- `Prompt`, `PromptAgentAttachment`, `PromptFileAttachment`, `PromptReferenceAttachment`, `PromptSource`
- `Pty`, `PtyCreateRequest`, `PtyUpdateRequest`, `PtyUpdateRequestSize`, `PtyConnectToken200Response`, `PtyShells200ResponseInner`
- `QuestionInfo`, `QuestionOption`, `QuestionRejected`, `QuestionReplied`, `QuestionReplyRequest`, `QuestionRequest`, `QuestionTool`
- `Range`, `RangeStart`
- `ResourceSource`, `ReferenceConfigEntry`
- `ServerConfig`
- `SessionDelivery`, `SessionRevert`
- `SnapshotFileDiff`
- `Todo`
- `ToolFileContent`, `ToolListItem`, `ToolTextContent`
- `TuiControlNext200Response`, `TuiExecuteCommandRequest`, `TuiPublishRequest`, `TuiSelectSessionRequest`, `TuiShowToastRequest`
- `UserMessage`, `UserMessageModel`, `UserMessageSummary`, `UserMessageTime`
- `Workspace`, `WorkspaceTimeUsed`, `Worktree`, `WorktreeCreateInput`, `WorktreeError`, `WorktreeList400Response`, `WorktreeRemoveInput`, `WorktreeResetInput`
- Various `Config*AnyOf*` classes for polymorphic config values
- Various `Experimental*` classes for experimental endpoints
- `GlobalSession*` models for global session management
- `Provider*` models for provider configuration and auth
- `AccountV2*` models for account management

## Key API Design Notes

### 1. Library Type: `native` (Java HttpClient)
The SDK uses Java 21's built-in `HttpClient` — NOT Apache HttpClient, OkHttp, or Retrofit. This means:
- No external HTTP library dependencies
- Uses `java.net.http.HttpClient`, `HttpRequest`, `HttpResponse`
- Async operations use `CompletableFuture`
- Request builders return `HttpRequest.Builder`

### 2. Common Parameter Pattern
Most instance-scoped endpoints take `directory` and `workspace` as the first two query parameters (both `@Nullable`). Global endpoints (GlobalApi) don't have these parameters.

### 3. Return Type Patterns
- Simple operations: return domain model directly (e.g., `Session`, `Config`, `Boolean`)
- List operations: return `List<T>` or wrapper types (e.g., `V2SessionsResponse` with cursor pagination)
- Map operations: return `Map<String, T>` (e.g., `Map<String, MCPStatus>`)
- Void operations: return `void` or `Boolean`
- File download: return `File`

### 4. No DefaultApi
Unlike what AGENTS.md describes, the new OpenAPI spec generates **22 specific API classes** instead of a single `DefaultApi`. The old `DefaultApi` and `SessionApi` have been replaced by many specialized API classes.

### 5. Pagination in V2 APIs
V2 APIs use cursor-based pagination:
- `V2SessionsResponse` contains `V2SessionsResponseCursor`
- `v2SessionList` takes `BigDecimal limit, String cursor` parameters
- `V2SessionMessagesResponse` uses similar cursor pagination

### 6. No `client/` or `config/` Custom Packages
Unlike what AGENTS.md suggests ("Custom implementations should go in `client/`, `config/`, and `model/` packages"), there are NO custom `client/` or `config/` packages in the current source. The only manual addition is `AnyOf.java` in the `model/` package.

### 7. SessionPrompt200Response
The `sessionPrompt` and `sessionCommand` methods return `SessionPrompt200Response` (not `SessionMessage` directly). This wraps the response from synchronous prompt operations.
