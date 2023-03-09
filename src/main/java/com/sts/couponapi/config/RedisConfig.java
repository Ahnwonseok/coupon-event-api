package com.sts.couponapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@EnableRedisRepositories //redis를 사용할 수 있게 해줌
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    //RedisConnectionFactory를 이용해 내장 / 외부의 Redis와 연결
    //지금은 embedded redis와 연결되어 있는 상태
    //추후 외부 uri를 사용하여 외부 redis와 연결이 가능함
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    //RedisConnection Factory에서 넘겨주는 값을 RedisTemplate을 통해 직렬화
    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    //캐시에서 Redis를 사용하기 위해 설정
    //RedisCacheManager를 Bean으로 등록하면 기본 CacheManager를 RedisCacheManager로 사용함.
    @Bean(name = "cacheManager")
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        //cache 사용을 위해 redisCacheConfiguration 초기화
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                // null value 캐시안함
                .disableCachingNullValues()
                // 캐시의 기본 유효시간 설정 -> CacheKey의 DEFAULT_EXPIRE_SEC로 설정
                .entryTtl(Duration.ofSeconds(CacheKey.DEFAULT_EXPIRE_SEC))
                //키 앞에 붙는 prefix 형식 지정. 여기서는 '이름::' 이렇게 되도록 설정되어 있음
                .computePrefixWith(CacheKeyPrefix.simple())
                // redis 캐시 데이터 저장방식을 StringSeriallizer로 지정
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                //value 직렬화 기본 옵션이 jdk 직렬화임. 우리 엔티티 클래스들을 json형식으로 직렬화 하려면 이 설정을 꼭 넣어줘야 함.
                //아니면 이 에러가 나옴. product 엔티티 클래스를 예시로 들어봄
                // "errorMessage": "DefaultSerializer requires a Serializable payload but received an object of type [com.project.stress_traffic_system.product.model.Product]"
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 캐시키별 유효시간 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(CacheKey.USERNAME, RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(CacheKey.POST_EXPIRE_SEC)));

        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory).cacheDefaults(configuration)
                .withInitialCacheConfigurations(cacheConfigurations).build();
    }


}