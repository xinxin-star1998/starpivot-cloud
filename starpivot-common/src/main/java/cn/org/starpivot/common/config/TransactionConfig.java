package cn.org.starpivot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 全局事务配置
 *
 * 统一配置 Spring 事务管理，启用声明式事务支持
 *
 * 配置说明：
 * - 启用声明式事务管理（默认使用代理模式）
 * - 支持 @Transactional 注解
 * - 默认回滚策略：仅对 RuntimeException 回滚
 *
 * 使用建议：
 * - 只读方法添加 @Transactional(readOnly = true) 以优化性能
 * - 需要回滚所有异常时使用 @Transactional(rollbackFor = Exception.class)
 * - Service 层方法默认不需要 @Transactional，框架自动管理
 */
@Configuration
@EnableTransactionManagement
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