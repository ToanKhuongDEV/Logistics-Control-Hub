package com.logistics.hub.feature.redis.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.redis.constant.CacheConstant;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
public class RedisConfig {

        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.activateDefaultTyping(
                                LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(CacheConstant.OSRM_TTL_HOURS))
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                                .disableCachingNullValues();

                Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                                CacheConstant.OSRM_MATRIX, defaultConfig.entryTtl(Duration.ofHours(24)),
                                CacheConstant.OSRM_ROUTE, defaultConfig.entryTtl(Duration.ofHours(24)),
                                CacheConstant.DASHBOARD_STATS, defaultConfig.entryTtl(Duration.ofMinutes(5)),
                                CacheConstant.DEPOTS, defaultConfig.entryTtl(Duration.ofHours(1)),
                                CacheConstant.DRIVERS, defaultConfig.entryTtl(Duration.ofHours(1)),
                                CacheConstant.VEHICLES, defaultConfig.entryTtl(Duration.ofHours(1)),
                                CacheConstant.DEPOT_STATS, defaultConfig.entryTtl(Duration.ofMinutes(10)),
                                CacheConstant.DRIVER_STATS, defaultConfig.entryTtl(Duration.ofMinutes(10)),
                                CacheConstant.VEHICLE_STATS, defaultConfig.entryTtl(Duration.ofMinutes(10)));

                return RedisCacheManager.builder(factory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigs)
                                .build();
        }

        @Bean("osrmMatrixKeyGenerator")
        public KeyGenerator osrmMatrixKeyGenerator() {
                return (target, method, params) -> {
                        if (params.length > 0 && params[0] instanceof List<?> locations) {
                                return locations.stream()
                                                .filter(loc -> loc instanceof LocationEntity)
                                                .map(loc -> String.valueOf(((LocationEntity) loc).getId()))
                                                .collect(Collectors.joining(","));
                        }
                        return "osrm-matrix-default";
                };
        }
}
