package cn.org.starpivot.ai.service.impl;

import cn.org.starpivot.ai.domain.dto.AiKnowledgeDocumentQueryDto;
import cn.org.starpivot.ai.domain.dto.AiKnowledgeDocumentSaveDto;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeBase;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeChunk;
import cn.org.starpivot.ai.domain.entity.AiKnowledgeDocument;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeDocumentVo;
import cn.org.starpivot.ai.mapper.AiKnowledgeBaseMapper;
import cn.org.starpivot.ai.mapper.AiKnowledgeChunkMapper;
import cn.org.starpivot.ai.mapper.AiKnowledgeDocumentMapper;
import cn.org.starpivot.ai.rag.loader.DocumentLoaderService;
import cn.org.starpivot.ai.rag.loader.ParseResult;
import cn.org.starpivot.ai.service.AiIndexService;
import cn.org.starpivot.ai.service.AiKnowledgeDocumentService;
import cn.org.starpivot.api.file.FileCategory;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.storage.FileCenterUploadHelper;
import cn.org.starpivot.common.storage.FileStorageService;
import cn.org.starpivot.common.storage.UploadResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeDocumentServiceImpl implements AiKnowledgeDocumentService {

    private static final String STATUS_NORMAL = "0";
    private static final String SOURCE_TEXT = "TEXT";
    private static final String SOURCE_FILE = "FILE";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "md", "markdown", "txt");
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    private final AiKnowledgeBaseMapper aiKnowledgeBaseMapper;
    private final AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;
    private final AiKnowledgeChunkMapper aiKnowledgeChunkMapper;
    private final AiIndexService aiIndexService;
    private final DocumentLoaderService documentLoaderService;
    private final ObjectProvider<FileStorageService> fileStorageServiceProvider;
    private final ObjectProvider<FileCenterUploadHelper> fileCenterUploadHelperProvider;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiKnowledgeDocumentVo> pageList(AiKnowledgeDocumentQueryDto query) {
        Page<AiKnowledgeDocument> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiKnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledgeDocument::getKbId, query.getKbId())
                .like(StringUtils.hasText(query.getTitle()), AiKnowledgeDocument::getTitle, query.getTitle())
                .eq(StringUtils.hasText(query.getStatus()), AiKnowledgeDocument::getStatus, query.getStatus())
                .orderByDesc(AiKnowledgeDocument::getUpdateTime);
        Page<AiKnowledgeDocument> result = aiKnowledgeDocumentMapper.selectPage(page, wrapper);
        PageResponse<AiKnowledgeDocumentVo> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setRows(result.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AiKnowledgeDocumentVo getById(Long docId) {
        return toVo(requireDoc(docId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(AiKnowledgeDocumentSaveDto dto) {
        AiKnowledgeBase kb = requireKb(dto.getKbId());
        LocalDateTime now = LocalDateTime.now();
        String operator = currentOperator();
        AiKnowledgeDocument entity;
        if (dto.getDocId() != null) {
            entity = requireDoc(dto.getDocId());
            if (!kb.getKbId().equals(entity.getKbId())) {
                throw new BizException("文档不属于该知识库");
            }
        } else {
            entity = new AiKnowledgeDocument();
            entity.setKbId(kb.getKbId());
            entity.setCreateBy(operator);
            entity.setCreateTime(now);
            entity.setSourceType(SOURCE_TEXT);
            entity.setDocVersion(1);
        }
        entity.setTitle(dto.getTitle().trim());
        entity.setContent(dto.getContent().trim());
        entity.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : STATUS_NORMAL);
        entity.setUpdateBy(operator);
        entity.setUpdateTime(now);
        if (dto.getDocId() != null) {
            aiKnowledgeDocumentMapper.updateById(entity);
        } else {
            entity.setIndexStatus(AiIndexServiceImpl.INDEX_PENDING);
            aiKnowledgeDocumentMapper.insert(entity);
        }
        aiIndexService.submitTextIndex(entity.getDocId());
        return entity.getDocId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadFile(Long kbId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BizException("文件大小不能超过 20MB");
        }
        String originalName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalName) || !originalName.contains(".")) {
            throw new BizException("无法识别文件类型");
        }
        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BizException("不支持的文件类型，仅支持 PDF、DOCX、MD、TXT");
        }

        byte[] bytes;
        ParseResult parseResult;
        try {
            bytes = file.getBytes();
            parseResult = documentLoaderService.load(new ByteArrayInputStream(bytes), originalName);
        } catch (Exception ex) {
            throw new BizException("文件解析失败：" + ex.getMessage());
        }
        if (!parseResult.isSuccess()) {
            throw new BizException(parseResult.getErrorMsg());
        }

        AiKnowledgeBase kb = requireKb(kbId);
        LocalDateTime now = LocalDateTime.now();
        String operator = currentOperator();
        String fileType = DocumentLoaderService.detectFileType(originalName);
        String title = StringUtils.hasText(parseResult.getTitle())
                ? parseResult.getTitle().trim()
                : stripExtension(originalName);

        AiKnowledgeDocument entity = new AiKnowledgeDocument();
        entity.setKbId(kb.getKbId());
        entity.setTitle(title);
        entity.setContent(parseResult.getFullText());
        entity.setSourceType(SOURCE_FILE);
        entity.setOriginalFileName(originalName);
        entity.setFileType(fileType);
        entity.setFileSize(file.getSize());
        entity.setStatus(STATUS_NORMAL);
        entity.setIndexStatus(AiIndexServiceImpl.INDEX_PENDING);
        entity.setDocVersion(1);
        entity.setCreateBy(operator);
        entity.setCreateTime(now);
        entity.setUpdateBy(operator);
        entity.setUpdateTime(now);

        FileCenterUploadHelper fileCenterHelper = fileCenterUploadHelperProvider.getIfAvailable();
        if (fileCenterHelper != null) {
            try {
                UploadResult uploadResult = fileCenterHelper.upload(
                        file,
                        FileCategory.AI_KNOWLEDGE.getObjectPathSegment(),
                        FileCategory.AI_KNOWLEDGE.getDefaultFolderId());
                entity.setObjectName(uploadResult.getObjectName());
            } catch (Exception ex) {
                log.warn("文件中心上传失败，将仅使用解析文本索引：{}", ex.getMessage());
            }
        } else {
            FileStorageService storage = fileStorageServiceProvider.getIfAvailable();
            if (storage != null) {
                try {
                    UploadResult uploadResult = storage.uploadFile(
                            file,
                            "ai-knowledge",
                            String.valueOf(kbId),
                            null,
                            MAX_FILE_SIZE);
                    entity.setObjectName(uploadResult.getObjectName());
                } catch (Exception ex) {
                    log.warn("文件上传 OSS 失败，将仅使用解析文本索引：{}", ex.getMessage());
                }
            }
        }

        aiKnowledgeDocumentMapper.insert(entity);
        aiIndexService.submitFileIndex(entity.getDocId());
        return entity.getDocId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long docId) {
        AiKnowledgeDocument doc = requireDoc(docId);
        aiKnowledgeChunkMapper.delete(new LambdaQueryWrapper<AiKnowledgeChunk>()
                .eq(AiKnowledgeChunk::getDocId, doc.getDocId()));
        aiKnowledgeDocumentMapper.deleteById(docId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reindex(Long docId) {
        AiKnowledgeDocument doc = requireDoc(docId);
        if (SOURCE_FILE.equals(doc.getSourceType()) && StringUtils.hasText(doc.getObjectName())) {
            aiIndexService.submitFileIndex(docId);
        } else {
            aiIndexService.submitTextIndex(docId);
        }
    }

    private AiKnowledgeBase requireKb(Long kbId) {
        AiKnowledgeBase kb = aiKnowledgeBaseMapper.selectById(kbId);
        if (kb == null) {
            throw new BizException("知识库不存在");
        }
        return kb;
    }

    private AiKnowledgeDocument requireDoc(Long docId) {
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(docId);
        if (doc == null) {
            throw new BizException("文档不存在");
        }
        return doc;
    }

    private AiKnowledgeDocumentVo toVo(AiKnowledgeDocument entity) {
        AiKnowledgeDocumentVo vo = new AiKnowledgeDocumentVo();
        vo.setDocId(entity.getDocId());
        vo.setKbId(entity.getKbId());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setChunkCount(entity.getChunkCount());
        vo.setStatus(entity.getStatus());
        vo.setSourceType(entity.getSourceType());
        vo.setOriginalFileName(entity.getOriginalFileName());
        vo.setFileType(entity.getFileType());
        vo.setFileSize(entity.getFileSize());
        vo.setIndexStatus(entity.getIndexStatus());
        vo.setErrorMsg(entity.getErrorMsg());
        vo.setIndexedAt(entity.getIndexedAt());
        vo.setUpdateBy(entity.getUpdateBy());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private String currentOperator() {
        String username = SecurityContextUtils.getUsername();
        return StringUtils.hasText(username) ? username : "system";
    }

    private String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
