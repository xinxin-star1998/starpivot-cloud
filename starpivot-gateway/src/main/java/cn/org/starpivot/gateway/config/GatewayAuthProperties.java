package cn.org.starpivot.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "starpivot.gateway")
public class GatewayAuthProperties {

    /** 无需鉴权的路径（Ant 风格） */
    private List<String> whitelist = new ArrayList<>(List.of(
            "/auth/login",
            "/**/actuator/**",
            "/**/doc.html",
            "/**/swagger-ui/**",
            "/**/v3/api-docs/**",
            "/**/webjars/**"
    ));
}
