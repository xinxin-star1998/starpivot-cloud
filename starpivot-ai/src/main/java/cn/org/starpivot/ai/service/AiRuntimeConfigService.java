package cn.org.starpivot.ai.service;

import cn.org.starpivot.ai.config.AiRuntimeSnapshot;

public interface AiRuntimeConfigService {

    AiRuntimeSnapshot current();

    void refresh();
}
