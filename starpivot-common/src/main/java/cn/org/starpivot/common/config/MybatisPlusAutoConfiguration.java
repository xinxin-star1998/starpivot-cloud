package cn.org.starpivot.common.config;

import cn.org.starpivot.common.datascope.DataPermissionInnerInterceptor;
import cn.org.starpivot.common.datascope.DataScopeProvider;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Plus 公共自动配置：注册 MySQL 分页插件与数据权限拦截器。
 * <p>
 * 数据权限拦截器依赖 {@link DataScopeProvider} Bean；若上下文中无此 Bean
 * （如非 {@code starpivot-system} 模块且未自行提供实现），则跳过注册。
 * </p>
 */
@AutoConfiguration
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class MybatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(ObjectProvider<DataScopeProvider> dataScopeProviderProvider) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 数据权限拦截器（需在分页之前）
        // 延迟解析 DataScopeProvider：传递 ObjectProvider 而非立即调用 getIfAvailable()，
        // 避免 MybatisPlusInterceptor → DataScopeProvider → Mapper → sqlSessionFactory 循环依赖
        interceptor.addInnerInterceptor(new DataPermissionInnerInterceptor(dataScopeProviderProvider));

        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
