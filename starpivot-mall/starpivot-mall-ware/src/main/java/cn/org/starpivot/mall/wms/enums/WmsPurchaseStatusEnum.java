package cn.org.starpivot.mall.wms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 采购单状态
 */
@Getter
@RequiredArgsConstructor
public enum WmsPurchaseStatusEnum {

    CREATED(0, "新建"),
    ASSIGNED(1, "已分配"),
    RECEIVED(2, "已领取"),
    FINISH(3, "已完成"),
    HAS_ERROR(4, "有异常");

    private final int code;
    private final String label;

    public static boolean canMerge(int status) {
        return status == CREATED.code || status == ASSIGNED.code;
    }

    public static boolean canReceive(int status) {
        return status == CREATED.code || status == ASSIGNED.code;
    }
}
