package com.sample.operator.config.webSocket;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

// 채팅 기록 로그를 남길때, 서버엔드포인트가 빈으로 취급되지 않으므로 로깅 서비스 빈을 주입받지 못해 컨텍스트에서 추출하기 위해 세팅
@Component
public class SpringContext implements ApplicationContextAware
{
    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        ctx = appCtx;
    }

    public static <T> T getBean(Class<T> clazz)
    {
        return ctx.getBean(clazz);
    }
}
