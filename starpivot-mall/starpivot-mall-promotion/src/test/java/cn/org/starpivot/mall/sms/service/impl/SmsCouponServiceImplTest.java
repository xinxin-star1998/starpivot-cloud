package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.sms.domain.bo.CouponSaveBo;
import cn.org.starpivot.mall.sms.domain.bo.CouponSpuBo;
import cn.org.starpivot.mall.sms.domain.vo.CouponVo;
import cn.org.starpivot.mall.sms.entity.SmsCoupon;
import cn.org.starpivot.mall.sms.mapper.SmsCouponHistoryMapper;
import cn.org.starpivot.mall.sms.mapper.SmsCouponMapper;
import cn.org.starpivot.mall.sms.mapper.SmsCouponSpuCategoryRelationMapper;
import cn.org.starpivot.mall.sms.mapper.SmsCouponSpuRelationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link SmsCouponServiceImpl} 单元测试。
 * <p>覆盖优惠券查询、新增、修改、删除及发布状态变更等核心场景。</p>
 */
@ExtendWith(MockitoExtension.class)
class SmsCouponServiceImplTest {

    @Mock
    private SmsCouponMapper smsCouponMapper;
    @Mock
    private SmsCouponHistoryMapper smsCouponHistoryMapper;
    @Mock
    private SmsCouponSpuRelationMapper smsCouponSpuRelationMapper;
    @Mock
    private SmsCouponSpuCategoryRelationMapper smsCouponSpuCategoryRelationMapper;
    @Mock
    private ProductFeignSupport productFeignSupport;

    @InjectMocks
    private SmsCouponServiceImpl couponService;

    // ── 辅助 ──────────────────────────────────────────────────────────

    private CouponSaveBo buildValidSaveBo() {
        CouponSaveBo bo = new CouponSaveBo();
        bo.setCouponType(0);
        bo.setCouponName("满100减10");
        bo.setAmount(new BigDecimal("10"));
        bo.setMinPoint(new BigDecimal("100"));
        bo.setPerLimit(1);
        bo.setStartTime(LocalDateTime.now().plusDays(1));
        bo.setEndTime(LocalDateTime.now().plusDays(30));
        bo.setEnableStartTime(LocalDateTime.now());
        bo.setEnableEndTime(LocalDateTime.now().plusDays(7));
        bo.setUseType(0);
        bo.setPublishCount(100);
        bo.setPublish(0);
        return bo;
    }

    private SmsCoupon buildCoupon(Long id, String name, int receiveCount) {
        SmsCoupon c = new SmsCoupon();
        c.setId(id);
        c.setCouponName(name);
        c.setReceiveCount(receiveCount);
        c.setAmount(new BigDecimal("10"));
        c.setMinPoint(new BigDecimal("100"));
        c.setUseType(0);
        c.setPublish(0);
        return c;
    }

    // ── getById ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getById 查询优惠券详情")
    class GetByIdTests {

        @Test
        @DisplayName("id 为 null 时抛出 BizException")
        void nullId_throwsException() {
            assertThrows(BizException.class, () -> couponService.getById(null));
        }

        @Test
        @DisplayName("优惠券不存在时抛出 BizException")
        void notFound_throwsException() {
            when(smsCouponMapper.selectById(999L)).thenReturn(null);
            assertThrows(BizException.class, () -> couponService.getById(999L));
        }

        @Test
        @DisplayName("正常返回优惠券详情（含关联列表）")
        void found_returnsVo() {
            SmsCoupon c = buildCoupon(1L, "满100减10", 0);
            when(smsCouponMapper.selectById(1L)).thenReturn(c);
            when(smsCouponSpuRelationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());
            when(smsCouponSpuCategoryRelationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            CouponVo vo = couponService.getById(1L);

            assertNotNull(vo);
            assertEquals("满100减10", vo.getCouponName());
        }
    }

    // ── add ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("add 新增优惠券")
    class AddTests {

        @Test
        @DisplayName("使用门槛小于面额时抛出异常")
        void minPointLessThanAmount_throwsException() {
            CouponSaveBo bo = buildValidSaveBo();
            bo.setMinPoint(new BigDecimal("5"));
            bo.setAmount(new BigDecimal("10"));

            BizException ex = assertThrows(BizException.class, () -> couponService.add(bo));
            assertTrue(ex.getMessage().contains("门槛"));
        }

        @Test
        @DisplayName("结束时间不晚于开始时间时抛出异常")
        void endTimeBeforeStartTime_throwsException() {
            CouponSaveBo bo = buildValidSaveBo();
            bo.setStartTime(LocalDateTime.now().plusDays(30));
            bo.setEndTime(LocalDateTime.now().plusDays(1));

            assertThrows(BizException.class, () -> couponService.add(bo));
        }

        @Test
        @DisplayName("领取结束时间不晚于开始时间时抛出异常")
        void enableEndTimeBeforeStart_throwsException() {
            CouponSaveBo bo = buildValidSaveBo();
            bo.setEnableStartTime(LocalDateTime.now().plusDays(10));
            bo.setEnableEndTime(LocalDateTime.now().plusDays(5));

            assertThrows(BizException.class, () -> couponService.add(bo));
        }

        @Test
        @DisplayName("会员赠券未选会员等级时抛出异常")
        void memberCouponWithoutLevel_throwsException() {
            CouponSaveBo bo = buildValidSaveBo();
            bo.setCouponType(1);
            bo.setMemberLevel(null);

            assertThrows(BizException.class, () -> couponService.add(bo));
        }

        @Test
        @DisplayName("指定商品但未选商品时抛出异常")
        void spuTypeWithoutSpuList_throwsException() {
            CouponSaveBo bo = buildValidSaveBo();
            bo.setUseType(2);
            bo.setSpuList(null);

            assertThrows(BizException.class, () -> couponService.add(bo));
        }

        @Test
        @DisplayName("指定分类但未选分类时抛出异常")
        void categoryTypeWithoutCategoryList_throwsException() {
            CouponSaveBo bo = buildValidSaveBo();
            bo.setUseType(1);
            bo.setCategoryList(null);

            assertThrows(BizException.class, () -> couponService.add(bo));
        }

        @Test
        @DisplayName("正常新增优惠券成功")
        void validBo_succeeds() {
            CouponSaveBo bo = buildValidSaveBo();
            doReturn(1).when(smsCouponMapper).insert((SmsCoupon) any());

            couponService.add(bo);

            verify(smsCouponMapper).insert((SmsCoupon) any());
        }
    }

    // ── update ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("update 修改优惠券")
    class UpdateTests {

        @Test
        @DisplayName("id 为 null 时抛出异常")
        void nullId_throwsException() {
            CouponSaveBo bo = buildValidSaveBo();
            bo.setId(null);
            assertThrows(BizException.class, () -> couponService.update(bo));
        }

        @Test
        @DisplayName("优惠券不存在时抛出异常")
        void notFound_throwsException() {
            CouponSaveBo bo = buildValidSaveBo();
            bo.setId(999L);
            when(smsCouponMapper.selectById(999L)).thenReturn(null);

            assertThrows(BizException.class, () -> couponService.update(bo));
        }

        @Test
        @DisplayName("已发布优惠券不允许修改面额")
        void publishedCannotChangeAmount_throwsException() {
            SmsCoupon existing = buildCoupon(1L, "满100减10", 0);
            existing.setPublish(1);
            when(smsCouponMapper.selectById(1L)).thenReturn(existing);

            CouponSaveBo bo = buildValidSaveBo();
            bo.setId(1L);
            bo.setAmount(new BigDecimal("20"));

            BizException ex = assertThrows(BizException.class, () -> couponService.update(bo));
            assertTrue(ex.getMessage().contains("面额"));
        }

        @Test
        @DisplayName("已发布优惠券不允许修改适用范围")
        void publishedCannotChangeUseType_throwsException() {
            SmsCoupon existing = buildCoupon(1L, "满100减10", 0);
            existing.setPublish(1);
            when(smsCouponMapper.selectById(1L)).thenReturn(existing);

            CouponSaveBo bo = buildValidSaveBo();
            bo.setId(1L);
            bo.setUseType(2);
            bo.setSpuList(List.of(new CouponSpuBo()));

            BizException ex = assertThrows(BizException.class, () -> couponService.update(bo));
            assertTrue(ex.getMessage().contains("适用范围"));
        }

        @Test
        @DisplayName("发行数量不能小于已领取数量")
        void publishCountLessThanReceived_throwsException() {
            SmsCoupon existing = buildCoupon(1L, "满100减10", 50);
            existing.setPublish(0);
            when(smsCouponMapper.selectById(1L)).thenReturn(existing);

            CouponSaveBo bo = buildValidSaveBo();
            bo.setId(1L);
            bo.setPublishCount(30);

            BizException ex = assertThrows(BizException.class, () -> couponService.update(bo));
            assertTrue(ex.getMessage().contains("发行数量"));
        }
    }

    // ── removeByIds ──────────────────────────────────────────────────

    @Nested
    @DisplayName("removeByIds 删除优惠券")
    class RemoveByIdsTests {

        @Test
        @DisplayName("空列表抛出 BizException")
        void emptyIds_throwsException() {
            assertThrows(BizException.class, () -> couponService.removeByIds(Collections.emptyList()));
        }

        @Test
        @DisplayName("null 列表抛出 BizException")
        void nullIds_throwsException() {
            assertThrows(BizException.class, () -> couponService.removeByIds(null));
        }

        @Test
        @DisplayName("已有用户领取的优惠券不能删除")
        void hasReceived_throwsException() {
            SmsCoupon c = buildCoupon(1L, "满100减10", 5);
            when(smsCouponMapper.selectById(1L)).thenReturn(c);

            BizException ex = assertThrows(BizException.class,
                    () -> couponService.removeByIds(List.of(1L)));
            assertTrue(ex.getMessage().contains("领取"));
        }

        @Test
        @DisplayName("有领取记录的优惠券不能删除")
        void hasHistory_throwsException() {
            SmsCoupon c = buildCoupon(1L, "满100减10", 0);
            when(smsCouponMapper.selectById(1L)).thenReturn(c);
            when(smsCouponHistoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            BizException ex = assertThrows(BizException.class,
                    () -> couponService.removeByIds(List.of(1L)));
            assertTrue(ex.getMessage().contains("领取记录"));
        }

        @Test
        @DisplayName("正常删除未被领取的优惠券")
        void removeUnused_succeeds() {
            SmsCoupon c = buildCoupon(1L, "满100减10", 0);
            when(smsCouponMapper.selectById(1L)).thenReturn(c);
            when(smsCouponHistoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(smsCouponSpuRelationMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
            when(smsCouponSpuCategoryRelationMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);

            couponService.removeByIds(List.of(1L));

            verify(smsCouponMapper).delete(any(LambdaQueryWrapper.class));
        }
    }

    // ── updatePublishStatus ──────────────────────────────────────────

    @Nested
    @DisplayName("updatePublishStatus 修改发布状态")
    class UpdatePublishStatusTests {

        @Test
        @DisplayName("参数为 null 时抛出异常")
        void nullParams_throwsException() {
            assertThrows(BizException.class, () -> couponService.updatePublishStatus(null, 0));
            assertThrows(BizException.class, () -> couponService.updatePublishStatus(1L, null));
        }

        @Test
        @DisplayName("无效的发布状态值抛出异常")
        void invalidPublishValue_throwsException() {
            assertThrows(BizException.class, () -> couponService.updatePublishStatus(1L, 5));
        }

        @Test
        @DisplayName("优惠券不存在时抛出异常")
        void notFound_throwsException() {
            when(smsCouponMapper.selectById(999L)).thenReturn(null);
            assertThrows(BizException.class, () -> couponService.updatePublishStatus(999L, 0));
        }

        @Test
        @DisplayName("直接上架需先提交审批，抛出异常")
        void directPublish_throwsException() {
            SmsCoupon c = buildCoupon(1L, "满100减10", 0);
            c.setPublish(0);
            when(smsCouponMapper.selectById(1L)).thenReturn(c);

            BizException ex = assertThrows(BizException.class,
                    () -> couponService.updatePublishStatus(1L, 1));
            assertTrue(ex.getMessage().contains("审批"));
        }

        @Test
        @DisplayName("状态相同时不执行更新")
        void sameStatus_noUpdate() {
            SmsCoupon c = buildCoupon(1L, "满100减10", 0);
            c.setPublish(0);
            when(smsCouponMapper.selectById(1L)).thenReturn(c);

            couponService.updatePublishStatus(1L, 0);

            verify(smsCouponMapper, never()).updateById(any(SmsCoupon.class));
        }

        @Test
        @DisplayName("下架操作成功执行")
        void unpublish_succeeds() {
            SmsCoupon c = buildCoupon(1L, "满100减10", 0);
            c.setPublish(1);
            when(smsCouponMapper.selectById(1L)).thenReturn(c);
            when(smsCouponMapper.updateById(any(SmsCoupon.class))).thenReturn(1);

            couponService.updatePublishStatus(1L, 0);

            verify(smsCouponMapper).updateById(any(SmsCoupon.class));
        }
    }
}
