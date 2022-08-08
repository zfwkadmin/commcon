package com.zqazfl.common.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
/**
 * redis工具类 (字节)
 */
@Component
public class RedisUtils {
    private final Logger log = LoggerFactory.getLogger(RedisUtil.class);

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;


    public boolean expire(byte[] key, long time) {
        try {
            if (time > 0) {
                RedisSerializer<?> redisSerializer=  redisTemplate.getKeySerializer();
                redisTemplate.setKeySerializer(null);
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
                redisTemplate.setKeySerializer(redisSerializer);
            }
            return true;
        } catch (Exception e) {
            log.error("方法名称->expire异常", e);
            return false;
        }
    }



}
