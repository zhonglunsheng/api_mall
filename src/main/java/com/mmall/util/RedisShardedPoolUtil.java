package com.mmall.util;

import com.mmall.common.RedisShardedPool;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.StringValue;
import redis.clients.jedis.ShardedJedis;

@Slf4j
public class RedisShardedPoolUtil {
    public static Long expire(String key, int exTime){
        ShardedJedis shardedJedis = null;
        Long result = null;
        try {
            shardedJedis = RedisShardedPool.getShardedJedis();
            result = shardedJedis.expire(key,exTime);
        } catch (Exception e) {
            log.error("expire key:{} error",key,e);
        }
        RedisShardedPool.closeJedis(shardedJedis);
        return result;
    }

    public static String set(String key, String value){
        ShardedJedis shardedJedis = null;
        String result = null;
        try {
            shardedJedis = RedisShardedPool.getShardedJedis();
            result = shardedJedis.set(key,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,value,e);
        }
        RedisShardedPool.closeJedis(shardedJedis);
        return result;
    }

    public static Long setnx(String key, String value){
        ShardedJedis shardedJedis = null;
        Long result = null;
        try {
            shardedJedis = RedisShardedPool.getShardedJedis();
            result = shardedJedis.setnx(key,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} extime:{} error",key,value,e);
        }
        RedisShardedPool.closeJedis(shardedJedis);
        return result;
    }

    public static String setEx(String key, String value, int time){
        ShardedJedis shardedJedis = null;
        String result = null;
        try {
            shardedJedis = RedisShardedPool.getShardedJedis();
            result = shardedJedis.setex(key,time,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} extime:{} error",key,value,e);
        }
        RedisShardedPool.closeJedis(shardedJedis);
        return result;
    }

    public static String get(String key){
        ShardedJedis shardedJedis = null;
        String result = null;
        try {
            shardedJedis = RedisShardedPool.getShardedJedis();
            result = shardedJedis.get(key);
        } catch (Exception e) {
            log.error("get key:{} error",key,e);
        }
        RedisShardedPool.closeJedis(shardedJedis);
        return result;
    }

    public static Long del(String key){
        ShardedJedis shardedJedis = null;
        Long result = null;
        try {
            shardedJedis = RedisShardedPool.getShardedJedis();
            result = shardedJedis.del(key);
        } catch (Exception e) {
            log.error("del key:{} error",key,e);
        }
        RedisShardedPool.closeJedis(shardedJedis);
        return result;
    }

    public static String getSet(String key, String value){
        ShardedJedis shardedJedis = null;
        String result = null;
        try {
            shardedJedis = RedisShardedPool.getShardedJedis();
            result = shardedJedis.getSet(key,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} extime:{} error",key,value,e);
        }
        RedisShardedPool.closeJedis(shardedJedis);
        return result;
    }

    /**
     * 测试redis是否实现分布式
     * @param args
     */
    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {
            RedisShardedPoolUtil.set(String.valueOf(i),"value"+i);
            System.out.println(RedisShardedPoolUtil.get(String.valueOf(i)));
        }
    }
}
