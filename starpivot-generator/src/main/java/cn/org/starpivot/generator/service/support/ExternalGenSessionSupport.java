package cn.org.starpivot.generator.service.support;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.config.GenExternalProperties;
import cn.org.starpivot.generator.domain.dto.external.ExternalConnectResultVO;
import cn.org.starpivot.generator.domain.dto.external.ExternalSessionStatusVO;
import cn.org.starpivot.generator.domain.external.ExternalDbConnection;
import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import cn.org.starpivot.generator.domain.external.ExternalGenSession;
import cn.org.starpivot.generator.external.ExternalDataSourceFactory;
import cn.org.starpivot.generator.external.ExternalGenSessionStore;
import cn.org.starpivot.generator.external.ExternalSessionDataSourceHolder;
import cn.org.starpivot.generator.external.ExternalWritePathValidator;
import cn.org.starpivot.generator.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * 外部库代码生成会话生命周期与通用解析。
 */
@Component
@RequiredArgsConstructor
public class ExternalGenSessionSupport {

    private final GenExternalProperties externalProperties;
    private final ExternalGenSessionStore sessionStore;
    private final ExternalSessionDataSourceHolder dataSourceHolder;

    public void assertEnabled() {
        if (!externalProperties.isEnabled()) {
            throw new BizException("外部库代码生成功能未启用");
        }
    }

    public ExternalConnectResultVO connect(ExternalDbConnection connection) {
        assertEnabled();
        ExternalDataSourceFactory.assertAllowedHost(connection, externalProperties);
        String sessionId = "ext_" + UUID.randomUUID().toString().replace("-", "");
        ExternalGenSession session = new ExternalGenSession();
        session.setSessionId(sessionId);
        session.setConnection(connection);

        DataSource ds = dataSourceHolder.register(sessionId, connection);
        try {
            if (!ds.getConnection().isValid(5)) {
                throw new BizException("数据库连接无效");
            }
        } catch (Exception e) {
            dataSourceHolder.remove(sessionId);
            throw new BizException("数据库连接失败：" + e.getMessage());
        }
        String version = ExternalDataSourceFactory.queryDbVersion(ds, connection);
        session.setDbVersion(version);
        sessionStore.save(session);

        return ExternalConnectResultVO.builder()
                .sessionId(sessionId)
                .database(connection.getDatabase())
                .dbVersion(version)
                .expireMinutes(externalProperties.getSessionTtlMinutes())
                .build();
    }

    public void disconnect(String sessionId) {
        dataSourceHolder.remove(sessionId);
        sessionStore.remove(sessionId);
    }

    public ExternalGenSession requireSession(String sessionId) {
        assertEnabled();
        return sessionStore.getRequired(sessionId);
    }

    public ExternalSessionStatusVO getSessionStatus(String sessionId) {
        ExternalGenSession session = requireSession(sessionId);
        long remaining = 0;
        if (session.getExpireAt() != null) {
            remaining = Math.max(0, Duration.between(Instant.now(), session.getExpireAt()).getSeconds());
        }
        return ExternalSessionStatusVO.builder()
                .sessionId(sessionId)
                .database(session.getConnection().getDatabase())
                .dbVersion(session.getDbVersion())
                .remainingSeconds(remaining)
                .expireMinutes(externalProperties.getSessionTtlMinutes())
                .templateDir(session.getTemplateDir())
                .effectiveTemplateDir(resolveTemplateDir(session))
                .build();
    }

    public ExternalGenScope resolveScope(ExternalGenScope scope) {
        ExternalGenScope effective = scope != null ? scope : new ExternalGenScope();
        effective.validate();
        return effective;
    }

    public String resolveTemplateDir(ExternalGenSession session) {
        if (session != null && StringUtils.isNotEmpty(session.getTemplateDir())) {
            ExternalWritePathValidator.assertTemplateDir(session.getTemplateDir());
            return session.getTemplateDir().trim();
        }
        String dir = externalProperties.getTemplateDir();
        if (StringUtils.isEmpty(dir)) {
            return null;
        }
        ExternalWritePathValidator.assertTemplateDir(dir);
        return dir.trim();
    }

    public GenExternalProperties properties() {
        return externalProperties;
    }

    public ExternalGenSessionStore sessionStore() {
        return sessionStore;
    }
}
