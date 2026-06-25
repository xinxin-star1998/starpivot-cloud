package cn.org.starpivot.mall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.org.starpivot.mall.wms.domain.bo.AddressVO;
import cn.org.starpivot.mall.wms.domain.dto.AddressDTO;
import cn.org.starpivot.mall.wms.domain.dto.AddressQueryDTO;
import cn.org.starpivot.mall.wms.entity.Address;

import java.util.List;

/**
 * Addressservice服务接口。
 * <p>
 * 封装地址相关业务逻辑。
 * </p>
 */

public interface AddressService extends IService<Address> {

    /** 懒加载：查询某父级下的直接子节点 */
    /**
     * 查询列表。
     */
    List<AddressVO> listChildren(String parentCode);

    /** 搜索（扁平列表，最多 200 条） */
    /**
     * searchAddress。
     */
    List<AddressVO> searchAddress(AddressQueryDTO queryDTO);

    /**
     * 查询AddressById。
     */
    AddressVO selectAddressById(Long id);

    /**
     * 新增记录。
     */
    void insertAddress(AddressDTO addressDTO);

    /**
     * 修改记录。
     */
    void updateAddress(AddressDTO addressDTO);

    /**
     * 删除记录。
     */
    void deleteAddressByIds(List<Long> ids);
}
