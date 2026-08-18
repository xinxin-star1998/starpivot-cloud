package cn.org.starpivot.ai.provider;

import org.springframework.context.ApplicationEvent;

public class AiProviderChangedEvent extends ApplicationEvent {

    public AiProviderChangedEvent(Object source) {
        super(source);
    }
}
