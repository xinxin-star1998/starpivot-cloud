package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrDTO;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrQueryDTO;
import cn.org.starpivot.mall.pms.domain.excel.PmsAttrExcel;
import cn.org.starpivot.mall.pms.domain.vo.PmsAttrVO;
import cn.org.starpivot.mall.pms.entity.PmsAttr;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Attrservice服务接口。
 * <p>
 * 封装商品属性相关业务逻辑。
 * </p>
 */

public interface PmsAttrService extends IService<PmsAttr>
{
    /**
     * 分页查询商品属性列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResponse<PmsAttrVO> selectPmsAttrPage(PmsAttrQueryDTO queryDTO);

    /**
     * 根据主键查询商品属性详细信息
     * 
     * @param attrId 商品属性主键
     * @return 商品属性信息
     */
    PmsAttrVO selectPmsAttrByAttrId(Long attrId);

    /**
     * 新增商品属性
     * 
     * @param pmsAttrDTO 商品属性信息
     * @return 是否成功
     */
    boolean insertPmsAttr(PmsAttrDTO pmsAttrDTO);

    /**
     * 修改商品属性
     * 
     * @param pmsAttrDTO 商品属性信息
     * @return 是否成功
     */
    boolean updatePmsAttr(PmsAttrDTO pmsAttrDTO);

    /**
     * 批量删除商品属性
     * 
     * @param attrIds 需要删除的商品属性主键数组
     * @return 是否成功
     */
    boolean deletePmsAttrByAttrIds(Long[] attrIds);

    /** 按查询条件导出 Excel 行（含关联分组信息） */
    /**
     * 查询列表。
     */
    List<PmsAttrExcel> listForExport(PmsAttrQueryDTO queryDTO);

    /**
     * 从 Excel 导入商品属性
     *
     * @param rows           解析后的行
     * @param attrType       0-销售属性 1-基本属性
     * @param updateSupport  为 true 且填写属性ID 时执行更新
     * @return 成功条数
     */
    int importFromExcel(List<PmsAttrExcel> rows, Integer attrType, boolean updateSupport);
}
