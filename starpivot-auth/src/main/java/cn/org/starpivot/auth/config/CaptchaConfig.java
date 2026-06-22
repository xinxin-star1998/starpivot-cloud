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
     * 配置无边框、140×52 像素、4 位字符、白底黑字等样式参数。
     * </p>
     *
     * @return 已配置属性的 {@link DefaultKaptcha} 实例
     */
    @Bean
    public DefaultKaptcha captchaProducer() {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.image.width", "140");
        properties.setProperty("kaptcha.image.height", "52");
        properties.setProperty("kaptcha.textproducer.font.size", "36");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.textproducer.char.space", "6");
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        properties.setProperty("kaptcha.background.clear.from", "white");
        properties.setProperty("kaptcha.background.clear.to", "white");
        properties.setProperty("kaptcha.noise.color", "gray");
        kaptcha.setConfig(new Config(properties));
        return kaptcha;
    }
}
