package cn.org.starpivot.ai.security;

import cn.org.starpivot.ai.config.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AiPermissionLoader {

    private final JdbcTemplate jdbcTemplate;
    private final AiProperties aiProperties;

    public List<String> selectPermissionsByUserId(Long userId) {
        String schema = aiProperties.validatedSystemDbSchema();
        return jdbcTemplate.queryForList(
                """
                SELECT DISTINCT m.perms
                FROM %s.sys_menu m
                         INNER JOIN %s.sys_role_menu rm ON m.menu_id = rm.menu_id
                         INNER JOIN %s.sys_user_role ur ON rm.role_id = ur.role_id
                WHERE ur.user_id = ?
                  AND m.status = '0'
                  AND m.perms IS NOT NULL
                  AND m.perms <> ''
                """.formatted(schema, schema, schema),
                String.class,
                userId);
    }

    public List<String> selectAllPermissionStrings() {
        String schema = aiProperties.validatedSystemDbSchema();
        return jdbcTemplate.queryForList(
                """
                SELECT DISTINCT perms
                FROM %s.sys_menu
                WHERE status = '0'
                  AND perms IS NOT NULL
                  AND perms <> ''
                """.formatted(schema),
                String.class);
    }

    public List<String> selectRoleKeysByUserId(Long userId) {
        String schema = aiProperties.validatedSystemDbSchema();
        return jdbcTemplate.queryForList(
                """
                SELECT DISTINCT r.role_key
                FROM %s.sys_role r
                         INNER JOIN %s.sys_user_role ur ON r.role_id = ur.role_id
                WHERE ur.user_id = ?
                  AND r.status = '0'
                  AND r.del_flag = '0'
                """.formatted(schema, schema),
                String.class,
                userId);
    }

    public boolean hasAllDataScopeRole(Long userId) {
        String schema = aiProperties.validatedSystemDbSchema();
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM %s.sys_role r
                         INNER JOIN %s.sys_user_role ur ON r.role_id = ur.role_id
                WHERE ur.user_id = ?
                  AND r.status = '0'
                  AND r.del_flag = '0'
                  AND r.data_scope = '1'
                """.formatted(schema, schema),
                Integer.class,
                userId);
        return count != null && count > 0;
    }
}
