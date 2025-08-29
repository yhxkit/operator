package com.sample.operator.config.cache;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.springframework.stereotype.Component;

@Component
public class EhCacheEventLogger implements CacheEventListener<Object, Object>
{
    @Override
    public void onEvent(CacheEvent<?, ?> event) {
        String msg = "Cache event = " + event.getType()
                + ": " + event.getKey();
//                + ": " + event.getOldValue()
//                + ": " + event.getNewValue();

        System.out.println("캐시 발생 " + msg);
    }
}
