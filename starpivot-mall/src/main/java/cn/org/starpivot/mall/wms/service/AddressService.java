package cn.org.starpivot.mall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.org.starpivot.mall.wms.domain.bo.AddressVO;
import cn.org.starpivot.mall.wms.domain.dto.AddressDTO;
import cn.org.starpivot.mall.wms.domain.dto.AddressQueryDTO;
import cn.org.starpivot.mall.wms.entity.Address;

import java.util.List;

/**
 * 省市区地址服务
 */
public interface AddressService extends IService<Address> {

    /** 懒加载：查询某父级下的直接子节点 */
    List<AddressVO> listChildren(String parentCode);

    /** 搜索（扁平列表，最多 200 条） */
    List<AddressVO> searchAddress(AddressQueryDTO queryDTO);

    AddressVO selectAddressById(Long id);

    void insertAddress(AddressDTO addressDTO);

    void updateAddress(AddressDTO addressDTO);

    void deleteAddressByIds(List<Long> ids);
}
