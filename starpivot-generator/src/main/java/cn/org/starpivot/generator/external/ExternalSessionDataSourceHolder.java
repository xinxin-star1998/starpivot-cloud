package cn.org.starpivot.generator.external;

import cn.org.starpivot.generator.config.GenExternalProperties;
import cn.org.starpivot.generator.domain.external.ExternalDbConnection;
import cn.org.starpivot.generator.domain.external.ExternalGenSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话级 JDBC 连接池复用（进程内，断开时销毁）
 */
@Component
@RequiredArgsConstructor
public class ExternalSessionDataSourceHolder {

    private final GenExternalProperties properties;
    private final ConcurrentHashMap<String, DataSource> dataSources = new ConcurrentHashMap<>();

    public DataSource getOrCreate(ExternalGenSession session) {
        return dataSources.computeIfAbsent(
                session.getSessionId(),
                id -> ExternalDataSourceFactory.create(session.getConnection(), properties));
    }

    public DataSource register(String sessionId, ExternalDbConnection connection) {
        remove(sessionId);
        DataSource ds = ExternalDataSourceFactory.create(connection, properties);
        dataSources.put(sessionId, ds);
        return ds;
    }

    public void remove(String sessionId) {
        DataSource ds = dataSources.remove(sessionId);
        ExternalDataSourceFactory.closeQuietly(ds);
    }
}

