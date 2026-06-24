package cn.org.starpivot.mall.wms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 采购明细 / 采购需求状态
 */
@Getter
@RequiredArgsConstructor
public enum WmsPurchaseDetailStatusEnum {

    CREATED(0, "新建"),
    ASSIGNED(1, "已分配"),
    BUYING(2, "采购中"),
    FINISH(3, "完成"),
    HAS_ERROR(4, "失败");

    private final int code;
    private final String label;
}
