package cn.org.starpivot.file.service.impl;

import cn.org.starpivot.api.file.FileBizConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.storage.FileCenterUploadHelper;
import cn.org.starpivot.common.storage.FileStorageService;
import cn.org.starpivot.file.config.FileCenterProperties;
import cn.org.starpivot.file.config.FileCenterPurgeProperties;
import cn.org.starpivot.file.domain.entity.SysFile;
import cn.org.starpivot.file.domain.entity.SysFileFolder;
import cn.org.starpivot.file.mapper.SysFileFolderMapper;
import cn.org.starpivot.file.mapper.SysFileMapper;
import cn.org.starpivot.file.service.FileCategoryAccessService;
import cn.org.starpivot.file.service.ISysFileRefService;
import cn.org.starpivot.file.support.FileMediaTypeResolver;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysFileServiceImplTest {

    @Mock
    private SysFileFolderMapper sysFileFolderMapper;
    @Mock
    private FileCenterUploadHelper fileCenterUploadHelper;
    @Mock
    private FileMediaTypeResolver fileMediaTypeResolver;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private FileCenterProperties fileCenterProperties;
    @Mock
    private FileCenterPurgeProperties purgeProperties;
    @Mock
    private FileCategoryAccessService fileCategoryAccessService;
    @Mock
    private ISysFileRefService sysFileRefService;
    @Mock
    private SysFileMapper sysFileMapper;

    @Spy
    @InjectMocks
    private SysFileServiceImpl sysFileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sysFileService, "baseMapper", sysFileMapper);
        ReflectionTestUtils.setField(sysFileService, "ossEnabled", true);
        lenient().doNothing().when(fileCategoryAccessService).requireAccess(org.mockito.ArgumentMatchers.anyString());
        lenient().when(fileCategoryAccessService.resolveAccessibleCategories())
                .thenReturn(Arrays.asList("SYSTEM", "HR", "FINANCE"));
        lenient().doNothing().when(sysFileRefService).assertNoReference(org.mockito.ArgumentMatchers.anyLong());
        lenient().doNothing().when(sysFileRefService).deleteByFileIds(anyCollection());
        LoginUser loginUser = LoginUser.builder().userId(1L).username("admin").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void logicDelete_throwsWhenReferenced() {
        SysFile file = new SysFile();
        file.setFileId(1L);
        file.setCategory("SYSTEM");
        file.setDelFlag(FileBizConstants.DEL_FLAG_NORMAL);
        doReturn(List.of(file)).when(sysFileService).list(org.mockito.ArgumentMatchers.<Wrapper<SysFile>>any());
        org.mockito.Mockito.doThrow(new BizException(ErrorCode.BIZ_ERROR, "文件仍被引用"))
                .when(sysFileRefService).assertNoReference(1L);

        BizException ex = assertThrows(BizException.class,
                () -> sysFileService.logicDelete(List.of(1L)));
        assertEquals(ErrorCode.BIZ_ERROR, ex.getCode());
    }

    @Test
    void logicDelete_throwsWhenFileNotFound() {
        doReturn(List.of()).when(sysFileService).list(org.mockito.ArgumentMatchers.<Wrapper<SysFile>>any());

        BizException ex = assertThrows(BizException.class,
                () -> sysFileService.logicDelete(List.of(999L)));
        assertEquals(ErrorCode.NOT_FOUND, ex.getCode());
    }

    @Test
    void restore_throwsWhenFolderMissing() {
        SysFile recycleFile = new SysFile();
        recycleFile.setFileId(1L);
        recycleFile.setFolderId(10L);
        recycleFile.setCategory("SYSTEM");
        recycleFile.setDelFlag(FileBizConstants.DEL_FLAG_RECYCLE);

        when(sysFileMapper.selectRecycleByIds(anyCollection())).thenReturn(List.of(recycleFile));
        when(sysFileFolderMapper.selectBatchIds(anyCollection())).thenReturn(List.of());

        BizException ex = assertThrows(BizException.class,
                () -> sysFileService.restore(List.of(1L)));
        assertEquals(ErrorCode.NOT_FOUND, ex.getCode());
        assertEquals("目标文件夹不存在，无法恢复文件", ex.getMessage());
    }

    @Test
    void restore_throwsWhenNotInRecycle() {
        when(sysFileMapper.selectRecycleByIds(anyCollection())).thenReturn(List.of());

        BizException ex = assertThrows(BizException.class,
                () -> sysFileService.restore(List.of(999L)));
        assertEquals(ErrorCode.NOT_FOUND, ex.getCode());
        assertEquals("部分文件不在回收站或不存在", ex.getMessage());
    }

    @Test
    void restore_succeedsWhenFolderExists() {
        SysFile recycleFile = new SysFile();
        recycleFile.setFileId(1L);
        recycleFile.setFolderId(10L);
        recycleFile.setCategory("SYSTEM");
        recycleFile.setDelFlag(FileBizConstants.DEL_FLAG_RECYCLE);

        SysFileFolder folder = new SysFileFolder();
        folder.setFolderId(10L);

        when(sysFileMapper.selectRecycleByIds(anyCollection())).thenReturn(List.of(recycleFile));
        when(sysFileFolderMapper.selectBatchIds(anyCollection())).thenReturn(List.of(folder));
        when(sysFileMapper.restoreByIds(List.of(1L), "admin")).thenReturn(1);

        sysFileService.restore(List.of(1L));

        verify(sysFileMapper).restoreByIds(List.of(1L), "admin");
    }

    @Test
    void moveToFolder_throwsOnCrossCategory() {
        SysFileFolder targetFolder = new SysFileFolder();
        targetFolder.setFolderId(2L);
        targetFolder.setCategory("HR");
        when(sysFileFolderMapper.selectById(2L)).thenReturn(targetFolder);

        SysFile file = new SysFile();
        file.setFileId(1L);
        file.setFolderId(1L);
        file.setCategory("FINANCE");
        file.setDelFlag(FileBizConstants.DEL_FLAG_NORMAL);
        doReturn(List.of(file)).when(sysFileService).list(org.mockito.ArgumentMatchers.<Wrapper<SysFile>>any());

        BizException ex = assertThrows(BizException.class,
                () -> sysFileService.moveToFolder(List.of(1L), 2L));
        assertEquals(ErrorCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void purgeExpiredRecycleFiles_deletesStorageAndDb() throws Exception {
        when(purgeProperties.getRecycleRetentionDays()).thenReturn(90);
        when(purgeProperties.getBatchSize()).thenReturn(100);

        SysFile expired = new SysFile();
        expired.setFileId(100L);
        expired.setObjectName("file/hr/1/2024/01/01/uuid.jpg");
        expired.setDeleteTime(LocalDateTime.now().minusDays(100));

        when(sysFileMapper.selectExpiredRecycleFiles(any(), eq(100))).thenReturn(List.of(expired));
        when(sysFileMapper.countReferencesByObjectName(expired.getObjectName(), List.of(100L))).thenReturn(0L);
        when(sysFileMapper.deletePhysicallyByIds(List.of(100L))).thenReturn(1);

        int purged = sysFileService.purgeExpiredRecycleFiles();

        assertEquals(1, purged);
        verify(fileStorageService).deleteObject(expired.getObjectName());
        verify(sysFileMapper).deletePhysicallyByIds(List.of(100L));
    }

    @Test
    void purgeExpiredRecycleFiles_skipsDbWhenOssDeleteFails() throws Exception {
        when(purgeProperties.getRecycleRetentionDays()).thenReturn(90);
        when(purgeProperties.getBatchSize()).thenReturn(100);

        SysFile expired = new SysFile();
        expired.setFileId(100L);
        expired.setObjectName("file/hr/1/2024/01/01/uuid.jpg");

        when(sysFileMapper.selectExpiredRecycleFiles(any(), anyInt())).thenReturn(List.of(expired));
        when(sysFileMapper.countReferencesByObjectName(expired.getObjectName(), List.of(100L))).thenReturn(0L);
        org.mockito.Mockito.doThrow(new RuntimeException("oss error"))
                .when(fileStorageService).deleteObject(expired.getObjectName());

        int purged = sysFileService.purgeExpiredRecycleFiles();

        assertEquals(0, purged);
        verify(sysFileMapper, never()).deletePhysicallyByIds(any());
    }

    @Test
    void permanentDelete_removesRecycleFiles() throws Exception {
        SysFile recycleFile = new SysFile();
        recycleFile.setFileId(5L);
        recycleFile.setCategory("SYSTEM");
        recycleFile.setObjectName("file/system/1/obj.pdf");
        recycleFile.setDelFlag(FileBizConstants.DEL_FLAG_RECYCLE);

        when(sysFileMapper.selectRecycleByIds(anyCollection())).thenReturn(List.of(recycleFile));
        when(sysFileMapper.countReferencesByObjectName(recycleFile.getObjectName(), List.of(5L))).thenReturn(0L);
        when(sysFileMapper.deletePhysicallyByIds(List.of(5L))).thenReturn(1);

        sysFileService.permanentDelete(List.of(5L));

        verify(fileStorageService).deleteObject(recycleFile.getObjectName());
        verify(sysFileMapper).deletePhysicallyByIds(List.of(5L));
    }
}
