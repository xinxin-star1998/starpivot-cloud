package cn.org.starpivot.mall.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.wms.domain.bo.AddressVO;
import cn.org.starpivot.mall.wms.domain.dto.AddressDTO;
import cn.org.starpivot.mall.wms.domain.dto.AddressQueryDTO;
import cn.org.starpivot.mall.wms.entity.Address;
import cn.org.starpivot.mall.wms.mapper.AddressMapper;
import cn.org.starpivot.mall.wms.service.AddressService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 地址服务实现类。
 * <p>
 * 实现 {@link AddressService}，处理地址相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 * </ul>
 *
 * @see AddressService
 */

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    @Transactional(readOnly = true)
    public List<AddressVO> listChildren(String parentCode) {
        String parent = normalizeParentParam(parentCode);
        List<Address> list = baseMapper.selectByParentCode(parent);
        return list.stream().map(this::toVoWithHasChildren).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressVO> searchAddress(AddressQueryDTO queryDTO) {
        if (queryDTO == null || !hasSearchCondition(queryDTO)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请至少填写一项搜索条件");
        }
        List<Address> list = baseMapper.searchList(queryDTO);
        return list.stream().map(this::toVoWithHasChildren).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressVO selectAddressById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "地址ID不能为空");
        }
        Address address = this.getById(id);
        if (address == null) {
            throw new BizException("地址不存在");
        }
        return toVoWithHasChildren(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertAddress(AddressDTO addressDTO) {
        Address address = new Address();
        BeanUtils.copyProperties(addressDTO, address);
        address.setId(null);
        normalizeParentCode(address);
        this.save(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(AddressDTO addressDTO) {
        if (addressDTO.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "地址ID不能为空");
        }
        Address existing = this.getById(addressDTO.getId());
        if (existing == null) {
            throw new BizException("地址不存在");
        }
        Address address = new Address();
        BeanUtils.copyProperties(addressDTO, address);
        normalizeParentCode(address);
        this.updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddressByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            Address address = this.getById(id);
            if (address == null) {
                continue;
            }
            long childCount =
                    count(Wrappers.<Address>lambdaQuery().eq(Address::getParentCode, address.getCode()));
            if (childCount > 0) {
                throw new BizException("地区「" + address.getName() + "」存在下级，请先删除子级");
            }
        }
        super.removeByIds(ids);
    }

    private AddressVO toVoWithHasChildren(Address address) {
        AddressVO vo = new AddressVO();
        BeanUtils.copyProperties(address, vo);
        // 0-省 1-市 2-区县 3-乡镇（乡镇为最末级）
        Long level = address.getLevel();
        vo.setHasChildren(level != null && level < 3L);
        return vo;
    }

    private static String normalizeParentParam(String parentCode) {
        if (!StringUtils.hasText(parentCode)) {
            return "0";
        }
        return parentCode.trim();
    }

    private void normalizeParentCode(Address address) {
        if (!StringUtils.hasText(address.getParentCode())) {
            address.setParentCode("0");
        }
    }

    private static boolean hasSearchCondition(AddressQueryDTO queryDTO) {
        return StringUtils.hasText(queryDTO.getName())
                || StringUtils.hasText(queryDTO.getCode())
                || StringUtils.hasText(queryDTO.getParentCode())
                || queryDTO.getLevel() != null;
    }
}
