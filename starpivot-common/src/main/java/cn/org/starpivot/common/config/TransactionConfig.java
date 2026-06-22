package cn.org.starpivot.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 全局事务配置（仅在有数据源/事务管理器的服务中生效，如 system、file）。
 */
@Configuration
@EnableTransactionManagement
@ConditionalOnBean(PlatformTransactionManager.class)
public class TransactionConfig {

    /**
     * 创建事务模板，用于编程式事务管理
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        // 设置默认超时时间为30秒
        template.setTimeout(30);
        // 设置默认传播行为为 REQUIRED
        template.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);
        return template;
    }

    // 如需自定义事务管理器或事务属性，可在此添加
}