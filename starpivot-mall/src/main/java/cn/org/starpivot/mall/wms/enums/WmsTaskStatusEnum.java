package cn.org.starpivot.mall.wms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 库存工作单状态
 */
@Getter
@RequiredArgsConstructor
public enum WmsTaskStatusEnum {

    NEW(0, "待处理"),
    PROCESSING(1, "处理中"),
    FINISHED(2, "已完成"),
    INVALID(3, "无效");

    private final int code;
    private final String label;
}
