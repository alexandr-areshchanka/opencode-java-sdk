package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.*;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileOperationsController {

    private final OpenCodeService openCodeService;

    @GetMapping("/tree")
    public List<FileNode> getFileTree(
            @RequestParam String path,
            @RequestParam(required = false) String directory,
            @RequestParam(required = false) String workspace) throws ApiException {
        return openCodeService.fileApi().fileList(path, directory, workspace);
    }

    @GetMapping("/content")
    public FileContent getFileContent(
            @RequestParam String path,
            @RequestParam(required = false) String directory,
            @RequestParam(required = false) String workspace) throws ApiException {
        return openCodeService.fileApi().fileRead(path, directory, workspace);
    }

    @GetMapping("/search")
    public List<FindText200ResponseInner> searchFiles(
            @RequestParam String pattern,
            @RequestParam(required = false) String directory,
            @RequestParam(required = false) String workspace) throws ApiException {
        return openCodeService.fileApi().findText(pattern, directory, workspace);
    }

    @GetMapping("/find")
    public List<String> findFiles(
            @RequestParam String query,
            @RequestParam(required = false) String directory,
            @RequestParam(required = false) String workspace,
            @RequestParam(required = false) String dirs,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer limit) throws ApiException {
        return openCodeService.fileApi().findFiles(query, directory, workspace, dirs, type, limit);
    }

    @GetMapping("/symbols")
    public List<Symbol> getSymbols(
            @RequestParam String query,
            @RequestParam(required = false) String directory,
            @RequestParam(required = false) String workspace) throws ApiException {
        return openCodeService.fileApi().findSymbols(query, directory, workspace);
    }

    @GetMapping("/diff")
    public List<SnapshotFileDiff> getFileDiff(
            @RequestParam String sessionId,
            @RequestParam(required = false) String directory,
            @RequestParam(required = false) String workspace,
            @RequestParam(required = false) String messageId) throws ApiException {
        return openCodeService.sessionApi().sessionDiff(sessionId, directory, workspace, messageId);
    }

    @GetMapping("/status")
    public List<opencode.sdk.model.ModelFile> getFileStatus(
            @RequestParam(required = false) String directory,
            @RequestParam(required = false) String workspace) throws ApiException {
        return openCodeService.fileApi().fileStatus(directory, workspace);
    }
}
