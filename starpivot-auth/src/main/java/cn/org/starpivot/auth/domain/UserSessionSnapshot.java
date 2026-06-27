package cn.org.starpivot.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * 用户单个设备会话快照。
 */
@Getter
@AllArgsConstructor
public class UserSessionSnapshot {

    private final String sessionId;
    private final Map<String, String> attributes;
}
