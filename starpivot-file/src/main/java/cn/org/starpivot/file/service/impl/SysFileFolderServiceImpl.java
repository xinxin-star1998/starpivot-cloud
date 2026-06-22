package cn.org.starpivot.file.service.impl;

import cn.org.starpivot.api.file.FileBizConstants;
import cn.org.starpivot.api.file.FileCategory;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.util.AssertUtils;
import cn.org.starpivot.file.domain.bo.FileCategoryNodeVO;
import cn.org.starpivot.file.domain.bo.SysFileFolderVO;
import cn.org.starpivot.file.domain.dto.SysFileFolderDTO;
import cn.org.starpivot.file.domain.entity.SysFileFolder;
import cn.org.starpivot.file.mapper.SysFileFolderMapper;
import cn.org.starpivot.file.mapper.SysFileMapper;
import cn.org.starpivot.file.service.ISysFileFolderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件夹服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysFileFolderServiceImpl extends ServiceImpl<SysFileFolderMapper, SysFileFolder>
        implements ISysFileFolderService {

    private final SysFileMapper sysFileMapper;

    @Override
    public List<FileCategoryNodeVO> listTree(String category) {
        List<FileCategory> categories = StringUtils.hasText(category)
                ? List.of(FileCategory.of(category))
                : Arrays.asList(FileCategory.values());

        List<FileCategoryNodeVO> nodes = new ArrayList<>();
        for (FileCategory cat : categories) {
            FileCategoryNodeVO node = new FileCategoryNodeVO();
            node.setCategory(cat.getCode());
            node.setCategoryLabel(cat.getLabel());
            node.setDefaultFolderId(cat.getDefaultFolderId());

            List<SysFileFolder> folders = list(new LambdaQueryWrapper<SysFileFolder>()
                    .eq(SysFileFolder::getCategory, cat.getCode())
                    .orderByAsc(SysFileFolder::getOrderNum)
                    .orderByAsc(SysFileFolder::getFolderId));

            List<SysFileFolderVO> children = folders.stream()
                    .map(this::toFolderVO)
                    .sorted(Comparator.comparing(SysFileFolderVO::getOrderNum, Comparator.nullsLast(Integer::compareTo)))
                    .collect(Collectors.toList());
            node.setChildren(children);
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SysFileFolderDTO dto) {
        FileCategory.of(dto.getCategory());
        if (dto.getFolderName() == null || dto.getFolderName().isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "文件夹名称不能为空");
        }

        long exists = count(new LambdaQueryWrapper<SysFileFolder>()
                .eq(SysFileFolder::getCategory, dto.getCategory())
                .eq(SysFileFolder::getFolderName, dto.getFolderName()));
        if (exists > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "同分类下文件夹名称已存在");
        }

        SysFileFolder folder = new SysFileFolder();
        folder.setCategory(dto.getCategory());
        folder.setFolderName(dto.getFolderName());
        folder.setParentId(0L);
        folder.setOrderNum(dto.getOrderNum() != null ? dto.getOrderNum() : 0);
        folder.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "0");
        folder.setRemark(dto.getRemark());
        folder.setCreateBy(SecurityContextUtils.getUsername());
        folder.setCreateTime(LocalDateTime.now());
        save(folder);
        return folder.getFolderId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysFileFolderDTO dto) {
        AssertUtils.notNull(dto.getFolderId(), ErrorCode.PARAM_INVALID, "文件夹ID不能为空");
        SysFileFolder folder = getById(dto.getFolderId());
        AssertUtils.notNull(folder, ErrorCode.NOT_FOUND, "文件夹不存在");

        if (StringUtils.hasText(dto.getFolderName()) && !dto.getFolderName().equals(folder.getFolderName())) {
            long exists = count(new LambdaQueryWrapper<SysFileFolder>()
                    .eq(SysFileFolder::getCategory, folder.getCategory())
                    .eq(SysFileFolder::getFolderName, dto.getFolderName())
                    .ne(SysFileFolder::getFolderId, dto.getFolderId()));
            if (exists > 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "同分类下文件夹名称已存在");
            }
            folder.setFolderName(dto.getFolderName());
        }
        if (dto.getOrderNum() != null) {
            folder.setOrderNum(dto.getOrderNum());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            folder.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            folder.setRemark(dto.getRemark());
        }
        folder.setUpdateBy(SecurityContextUtils.getUsername());
        folder.setUpdateTime(LocalDateTime.now());
        updateById(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long folderId) {
        SysFileFolder folder = getById(folderId);
        AssertUtils.notNull(folder, ErrorCode.NOT_FOUND, "文件夹不存在");
        if (FileBizConstants.DEFAULT_FOLDER_NAME.equals(folder.getFolderName())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "默认文件夹不可删除");
        }
        long fileCount = sysFileMapper.countActiveByFolderId(folderId);
        if (fileCount > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "文件夹下存在文件，无法删除");
        }
        removeById(folderId);
    }

    private SysFileFolderVO toFolderVO(SysFileFolder folder) {
        SysFileFolderVO vo = new SysFileFolderVO();
        BeanUtils.copyProperties(folder, vo);
        vo.setFileCount(sysFileMapper.countActiveByFolderId(folder.getFolderId()));
        return vo;
    }
}
