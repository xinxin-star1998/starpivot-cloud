package cn.org.starpivot.auth.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置类。
 * <p>
 * 基于 Kaptcha 库配置图形验证码生成器，供 {@link cn.org.starpivot.auth.service.CaptchaService} 使用。
 * </p>
 * <ul>
 *   <li>{@link Configuration} — 声明为 Spring 配置类，参与容器初始化</li>
 * </ul>
 */
@Configuration
public class CaptchaConfig {

    /**
     * 注册 Kaptcha 验证码生成器 Bean。
     * <p>
     * 配置 4 位字符集与长度，图片由 {@link cn.org.starpivot.auth.captcha.CaptchaImageRenderer} 自绘。
     * </p>
     *
     * @return 已配置属性的 {@link DefaultKaptcha} 实例
     */
    @Bean
    public DefaultKaptcha captchaProducer() {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.textproducer.char.string", "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ");
        kaptcha.setConfig(new Config(properties));
        return kaptcha;
    }
}
