package cn.org.starpivot.generator.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.generator.domain.dto.external.*;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import cn.org.starpivot.generator.domain.external.ExternalDbConnection;

import java.util.List;
import java.util.Map;

/**
 * 外部库代码生成服务
 */
public interface GenExternalService {

    ExternalConnectResultVO connect(ExternalDbConnection connection);

    void disconnect(String sessionId);

    ExternalSessionStatusVO getSessionStatus(String sessionId);

    PageResponse<GenTable> listTables(ExternalTableQueryDTO query);

    List<GenTableColumn> listColumns(ExternalColumnsRequest request);

    void savePathProfile(ExternalPathProfileRequest request);

    void saveDraft(ExternalDraftRequest request);

    void saveGenConfig(ExternalGenConfigRequest request);

    void saveTemplateDir(ExternalTemplateDirRequest request);

    Map<String, String> preview(ExternalPreviewRequest request);

    byte[] download(ExternalDownloadRequest request);

    ExternalImportResultVO importToGenTable(ExternalImportRequest request);

    ExternalWriteResultVO writeToDisk(ExternalWriteRequest request);

    ExternalWriteDiffResultVO previewWriteDiff(ExternalWriteDiffRequest request);

    ExternalWriteDiffItemVO previewWriteDiffFile(ExternalWriteDiffFileRequest request);

    Map<String, Object> listTemplates();

    ExternalWriteRollbackResultVO rollbackWrite(ExternalWriteRollbackRequest request);

    ExternalWriteDirListVO listWriteDirs(String path);

    Map<String, Object> listCapabilities();
}

