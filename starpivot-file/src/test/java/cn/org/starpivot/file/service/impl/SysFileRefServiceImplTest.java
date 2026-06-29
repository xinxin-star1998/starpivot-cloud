package cn.org.starpivot.file.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.file.domain.entity.SysFile;
import cn.org.starpivot.file.domain.entity.SysFileRef;
import cn.org.starpivot.file.mapper.SysFileMapper;
import cn.org.starpivot.file.mapper.SysFileRefMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysFileRefServiceImplTest {

    @Mock
    private SysFileRefMapper sysFileRefMapper;

    @Mock
    private SysFileMapper sysFileMapper;

    @Spy
    @InjectMocks
    private SysFileRefServiceImpl sysFileRefService;

    @Test
    void bind_skipsWhenAlreadyExists() {
        ReflectionTestUtils.setField(sysFileRefService, "baseMapper", sysFileRefMapper);
        doReturn(1L).when(sysFileRefService).count(any(Wrapper.class));

        sysFileRefService.bind(1L, "goods", "100");

        verify(sysFileRefService, never()).save(any(SysFileRef.class));
    }

    @Test
    void assertNoReference_throwsWhenReferenced() {
        ReflectionTestUtils.setField(sysFileRefService, "baseMapper", sysFileRefMapper);
        when(sysFileRefMapper.countByFileId(1L)).thenReturn(2L);

        BizException ex = assertThrows(BizException.class, () -> sysFileRefService.assertNoReference(1L));
        assertEquals(ErrorCode.BIZ_ERROR, ex.getCode());
    }

    @Test
    void unbind_removesMatchingRef() {
        ReflectionTestUtils.setField(sysFileRefService, "baseMapper", sysFileRefMapper);
        doReturn(true).when(sysFileRefService).remove(any(Wrapper.class));

        sysFileRefService.unbind(1L, "goods", "100");

        verify(sysFileRefService).remove(any(Wrapper.class));
    }

    @Test
    void syncByObjectNames_bindsResolvedFiles() {
        ReflectionTestUtils.setField(sysFileRefService, "baseMapper", sysFileRefMapper);
        doReturn(true).when(sysFileRefService).remove(any(Wrapper.class));
        doReturn(0L).when(sysFileRefService).count(any(Wrapper.class));
        doReturn(true).when(sysFileRefService).save(any(SysFileRef.class));

        SysFile file = new SysFile();
        file.setFileId(10L);
        when(sysFileMapper.selectActiveByObjectName("file/goods/7/a.jpg")).thenReturn(file);

        sysFileRefService.syncByObjectNames("mall_spu", "100", List.of("file/goods/7/a.jpg"));

        verify(sysFileRefService).remove(any(Wrapper.class));
        verify(sysFileRefService).save(any(SysFileRef.class));
    }

    @Test
    void syncByObjectNames_skipsWhenMetadataMissing() {
        ReflectionTestUtils.setField(sysFileRefService, "baseMapper", sysFileRefMapper);
        doReturn(true).when(sysFileRefService).remove(any(Wrapper.class));
        when(sysFileMapper.selectActiveByObjectName("legacy/goods/a.jpg")).thenReturn(null);

        sysFileRefService.syncByObjectNames("mall_spu", "100", List.of("legacy/goods/a.jpg"));

        verify(sysFileRefService).remove(any(Wrapper.class));
        verify(sysFileRefService, never()).save(any(SysFileRef.class));
    }
}
