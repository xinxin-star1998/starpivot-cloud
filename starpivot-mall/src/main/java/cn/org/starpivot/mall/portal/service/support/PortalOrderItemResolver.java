package cn.org.starpivot.mall.portal.service.support;

import cn.org.starpivot.mall.portal.domain.bo.PortalOrderItemBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderPriceTrialBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartVo;
import cn.org.starpivot.mall.portal.service.PortalCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 从购物车或直接明细解析下单行项目。
 */
@Component
@RequiredArgsConstructor
public class PortalOrderItemResolver {

    private final PortalCartService portalCartService;

    public List<PortalOrderItemBo> resolveTrialItems(Long memberId, PortalOrderPriceTrialBo bo) {
        if (Boolean.TRUE.equals(bo.getUseCart())) {
            return fromCheckedCart(memberId, bo.getCartSkuIds());
        }
        return bo.getItems() == null ? List.of() : bo.getItems();
    }

    public List<PortalOrderItemBo> resolveOrderItems(Long memberId, PortalOrderSubmitBo bo) {
        if (Boolean.TRUE.equals(bo.getUseCart())) {
            return fromCheckedCart(memberId, bo.getCartSkuIds());
        }
        return bo.getItems() == null ? List.of() : bo.getItems();
    }

    private List<PortalOrderItemBo> fromCheckedCart(Long memberId, List<Long> cartSkuIds) {
        PortalCartVo cart = portalCartService.listCart(memberId);
        if (cart.getItems() == null) {
            return List.of();
        }
        return cart.getItems().stream()
                .filter(item -> Boolean.TRUE.equals(item.getChecked()))
                .filter(item -> Boolean.TRUE.equals(item.getValid()))
                .filter(item -> cartSkuIds == null || cartSkuIds.isEmpty() || cartSkuIds.contains(item.getSkuId()))
                .map(item -> {
                    PortalOrderItemBo orderItem = new PortalOrderItemBo();
                    orderItem.setSkuId(item.getSkuId());
                    orderItem.setQuantity(item.getQuantity());
                    return orderItem;
                })
                .collect(Collectors.toList());
    }
}
