package cn.org.starpivot.mall.wms.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.wms.domain.dto.WmsWareSkuDTO;
import cn.org.starpivot.mall.wms.domain.dto.WmsWareSkuInboundDTO;
import cn.org.starpivot.mall.wms.domain.dto.WmsWareSkuQueryDTO;
import cn.org.starpivot.mall.wms.domain.vo.WmsWareSkuVO;
import cn.org.starpivot.mall.wms.service.WmsWareSkuService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商城-库存控制器。
 * <p>
 * 仓库 SKU 库存的增删改查等接口。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /mall/ware-sku}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Tag} — OpenAPI 分组「商城-库存」</li>
 * </ul>
 *
 * @see WmsWareSkuService
 */

@Slf4j
@RestController
@RequestMapping("/mall/ware-sku")
@RequiredArgsConstructor
@Tag(name = "商城-库存", description = "仓库 SKU 库存的增删改查等接口")
public class WmsWareSkuController
{
    private final WmsWareSkuService wmsWareSkuService;

    /**
     * 分页查询商品库存列表
     * 
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasAuthority('mall:sku:query')")
    @PostMapping("/list")
    public Result<PageResponse<WmsWareSkuVO>> list(@RequestBody WmsWareSkuQueryDTO queryDTO)
    {
        PageResponse<WmsWareSkuVO> page = wmsWareSkuService.selectWmsWareSkuPage(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取商品库存详细信息
     * 
     * @param id 商品库存主键
     * @return 商品库存信息
     */
    @PreAuthorize("hasAuthority('mall:sku:query')")
    @GetMapping(value = "/{id}")
    public Result<WmsWareSkuVO> getInfo(@PathVariable("id") Long id)
    {
        WmsWareSkuVO wmsWareSkuVO = wmsWareSkuService.selectWmsWareSkuById(id);
        return Result.success(wmsWareSkuVO);
    }

    /**
     * 新增商品库存
     * 
     * @param wmsWareSkuDTO 商品库存信息
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('mall:sku:add')")
    @PostMapping
    public Result<?> add(@Valid @RequestBody WmsWareSkuDTO wmsWareSkuDTO)
    {
        boolean success = wmsWareSkuService.insertWmsWareSku(wmsWareSkuDTO);
        return success ? Result.success("新增商品库存成功") : Result.error("新增商品库存失败");
    }

    /**
     * 快速入库（累加库存）
     */
    @PreAuthorize("hasAuthority('mall:sku:add')")
    @PostMapping("/inbound")
    public Result<?> inbound(@Valid @RequestBody WmsWareSkuInboundDTO inboundDTO) {
        wmsWareSkuService.inboundStock(inboundDTO);
        return Result.success("入库成功");
    }

    /**
     * 修改商品库存
     * 
     * @param wmsWareSkuDTO 商品库存信息
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('mall:sku:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody WmsWareSkuDTO wmsWareSkuDTO)
    {
        boolean success = wmsWareSkuService.updateWmsWareSku(wmsWareSkuDTO);
        return success ? Result.success("修改商品库存成功") : Result.error("修改商品库存失败");
    }

    /**
     * 删除商品库存（支持批量删除，请求体 ids）
     * 
     * @param deleteRequest 删除请求，包含 ids 数组
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('mall:sku:delete')")
    @DeleteMapping("/delete")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest)
    {
        List<Long> idList = deleteRequest.getIds();
        if (idList == null || idList.isEmpty()) {
            return Result.error("删除ID不能为空");
        }
        Long[] ids = idList.toArray(new Long[0]);
        boolean success = wmsWareSkuService.deleteWmsWareSkuByIds(ids);
        return success ? Result.success("删除商品库存成功") : Result.error("删除商品库存失败");
    }
}
