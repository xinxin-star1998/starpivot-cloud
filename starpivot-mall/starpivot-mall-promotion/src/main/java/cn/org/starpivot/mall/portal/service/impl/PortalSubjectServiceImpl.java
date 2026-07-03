package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.api.product.dto.PortalProductListDto;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.portal.domain.vo.PortalSubjectDetailVo;
import cn.org.starpivot.mall.portal.service.PortalSubjectService;
import cn.org.starpivot.mall.sms.entity.SmsHomeSubject;
import cn.org.starpivot.mall.sms.entity.SmsHomeSubjectSpu;
import cn.org.starpivot.mall.sms.mapper.SmsHomeSubjectMapper;
import cn.org.starpivot.mall.sms.mapper.SmsHomeSubjectSpuMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortalSubjectServiceImpl implements PortalSubjectService {

    private final SmsHomeSubjectMapper smsHomeSubjectMapper;
    private final SmsHomeSubjectSpuMapper smsHomeSubjectSpuMapper;
    private final ProductFeignSupport productFeignSupport;

    @Override
    @Transactional(readOnly = true)
    public PortalSubjectDetailVo getDetail(Long subjectId, int pageNum, int pageSize) {
        if (subjectId == null) {
            throw new BizException("专题 ID 不能为空");
        }
        SmsHomeSubject subject = smsHomeSubjectMapper.selectOne(
                Wrappers.<SmsHomeSubject>lambdaQuery()
                        .eq(SmsHomeSubject::getId, subjectId)
                        .eq(SmsHomeSubject::getStatus, 1));
        if (subject == null) {
            throw new BizException("专题不存在或已下线");
        }

        List<Long> orderedSpuIds = loadOrderedSpuIds(subjectId);
        PageResponse<PortalProductListDto> productPage =
                productFeignSupport.listPortalProductsByOrderedSpuIds(orderedSpuIds, pageNum, pageSize);

        PortalSubjectDetailVo vo = new PortalSubjectDetailVo();
        vo.setId(subject.getId());
        vo.setName(subject.getName());
        vo.setTitle(StringUtils.hasText(subject.getTitle()) ? subject.getTitle() : subject.getName());
        vo.setSubTitle(subject.getSubTitle());
        vo.setCoverImg(normalizeImg(subject.getImg()));
        vo.setUrl(StringUtils.hasText(subject.getUrl()) ? subject.getUrl() : "/portal/subject/" + subject.getId());
        vo.setProducts(productPage);
        return vo;
    }

    private List<Long> loadOrderedSpuIds(Long subjectId) {
        List<SmsHomeSubjectSpu> relations = smsHomeSubjectSpuMapper.selectList(
                Wrappers.<SmsHomeSubjectSpu>lambdaQuery()
                        .eq(SmsHomeSubjectSpu::getSubjectId, subjectId)
                        .orderByAsc(SmsHomeSubjectSpu::getSort)
                        .orderByAsc(SmsHomeSubjectSpu::getId));
        if (CollectionUtils.isEmpty(relations)) {
            return List.of();
        }
        List<Long> spuIds = new ArrayList<>();
        for (SmsHomeSubjectSpu relation : relations) {
            if (relation.getSpuId() != null && !spuIds.contains(relation.getSpuId())) {
                spuIds.add(relation.getSpuId());
            }
        }
        return spuIds;
    }

    private String normalizeImg(String img) {
        if (!StringUtils.hasText(img)) {
            return img;
        }
        return StorageObjectPathUtils.normalizeToObjectName(img.trim());
    }
}
