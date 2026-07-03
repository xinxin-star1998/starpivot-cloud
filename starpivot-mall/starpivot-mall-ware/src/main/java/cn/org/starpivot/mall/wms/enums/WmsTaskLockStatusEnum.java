package cn.org.starpivot.mall.wms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 工作单明细锁库存状态
 */
@Getter
@RequiredArgsConstructor
public enum WmsTaskLockStatusEnum {

    LOCKED(1, "锁定"),
    UNLOCKED(2, "解锁"),
    DEDUCTED(3, "扣减");

    private final int code;
    private final String label;
}
