package cn.org.starpivot.tms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Data
@ConfigurationProperties(prefix = "starpivot.tms")
public class TmsProperties {

    private String systemDbSchema = "star_pivot";
    private Kuaidi100 kuaidi100 = new Kuaidi100();
    /** 轨迹定时刷新间隔（毫秒），0 表示关闭 */
    private long trackRefreshScanMs = 300000L;
    private int trackRefreshBatchSize = 20;
    /** 快递100 识别签收后是否自动确认收货（默认关闭） */
    private boolean autoConfirmOnDelivered = false;
    /** 无重量时使用默认单件重量 kg */
    private BigDecimal defaultItemWeightKg = new BigDecimal("0.500");

    public String validatedSystemDbSchema() {
        if (!StringUtils.hasText(systemDbSchema)) {
            return "star_pivot";
        }
        return systemDbSchema.trim();
    }

    @Data
    public static class Kuaidi100 {
        private boolean enabled;
        private String customer;
        private String key;
        private String queryUrl = "https://poll.kuaidi100.com/poll/query.do";
    }
}
