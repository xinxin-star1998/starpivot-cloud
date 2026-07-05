package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.system.SysMessageClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class SysMessageClientFallbackFactory implements FallbackFactory<SysMessageClient> {

    private static final String ACTION = "站内消息";

    @Override
    public SysMessageClient create(Throwable cause) {
        return new SysMessageClient() {
            @Override
            public cn.org.starpivot.common.domain.Result<Void> send(
                    cn.org.starpivot.api.system.dto.MessageSendRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public cn.org.starpivot.common.domain.Result<Void> sendToRoles(
                    cn.org.starpivot.api.system.dto.MessageSendToRolesRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
