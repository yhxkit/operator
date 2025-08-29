package com.sample.operator.config.cache;
import lombok.RequiredArgsConstructor;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.EventType;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.config.JCacheConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.management.CacheStatisticsMXBean;
import javax.cache.spi.CachingProvider;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Set;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class EhCacheConfig implements JCacheConfigurer {

    private final EhCacheEventLogger ehCacheEventLogger;

    // 어떤 순간에 캐시 발생할지 설정
    public CacheEventListenerConfigurationBuilder getCacheEventListenerConfigurationBuilder()
    {
        CacheEventListenerConfigurationBuilder cacheEventbuilder = CacheEventListenerConfigurationBuilder
                .newEventListenerConfiguration(ehCacheEventLogger, EventType.CREATED, EventType.UPDATED, EventType.REMOVED, EventType.EVICTED, EventType.EXPIRED)
                .unordered().asynchronous();

        return cacheEventbuilder;
    }

    public CacheConfiguration<String, Object> getCacheConfiguration() {
        ExpiryPolicy<Object, Object> timeToIdle = ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(60*60*6));
        CacheConfiguration<String, Object> cacheConf = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, Object.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder().heap(2, EntryUnit.ENTRIES).offheap(10, MemoryUnit.MB))
                .withService(getCacheEventListenerConfigurationBuilder())
                .withExpiry(timeToIdle).build();

        return cacheConf;
    }

    @Bean
    public CacheManager getCacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cm = provider.getCacheManager();

        javax.cache.configuration.Configuration cc = Eh107Configuration.fromEhcacheCacheConfiguration(this::getCacheConfiguration);

        cm.createCache("cacheA", cc);
        cm.createCache("cacheB", cc);
        cm.createCache("cacheC", cc);

        // 통계 활성화
        cm.enableStatistics("cacheA", true);
        cm.enableStatistics("cacheB", true);
        cm.enableStatistics("cacheC", true);

        return cm;
    }

    public void getCacheStatistics() {
        try
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectInstance> cb = mbs.queryMBeans(ObjectName.getInstance("org.ehcache:type=CacheStatistics,name=*"),null);

            for(ObjectInstance o : cb)
            {
                System.out.println("오브젝트명 " + o.getObjectName());

                CacheStatisticsMXBean smb = MBeanServerInvocationHandler.newProxyInstance(mbs, o.getObjectName(), CacheStatisticsMXBean.class, false);
                System.out.println(smb.getCacheGets());
                System.out.println(smb.getCacheHits());
                System.out.println(smb.getCacheMisses());
                System.out.println(smb.getCachePuts());
                System.out.println(smb.getCacheEvictions());
                System.out.println(smb.getCacheRemovals());
                System.out.println(smb.getAverageGetTime());
                System.out.println(smb.getAveragePutTime());
                System.out.println(smb.getAverageRemoveTime());
                System.out.println(smb.getCacheHitPercentage());
            }

        }
        catch (Exception e)
        {
            System.out.println("통계 확인 오류");
        }
    }
}
