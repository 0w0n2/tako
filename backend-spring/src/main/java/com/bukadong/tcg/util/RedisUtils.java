package com.bukadong.tcg.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> values;
    private final SetOperations<String, Object> setOps;

    RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.values = redisTemplate.opsForValue();
        this.setOps = redisTemplate.opsForSet();
    }

    public void setValue(String key, Object value) {
        values.set(key, value);
    }

    public void setValue(String key, Object value, Duration duration) {
        values.set(key, value, duration);
    }

    public boolean keyExists(String key) {
        return redisTemplate.hasKey(key);
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    public void addSetValue(String key, Object value) {
        setOps.add(key, value);
    }

    public void deleteSetValue(String key, Object value) {
        setOps.remove(key, value);
    }

    public Set<Object> getSetValue(String key) {
        return setOps.members(key);
    }

    public boolean isSetMember(String key, Object value) {
        return Boolean.TRUE.equals(setOps.isMember(key, value));
    }

}
