package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

public class RedisShardedPool {
    //获取连接池实例
    private static ShardedJedisPool pool;

    //最大连接数
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total","20"));
    //空闲状态最大jedis实例个数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","10"));;
    //空闲状态最小jedis实例个数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","2"));;

    //在borrow一个jedis实例是否进行验证 如果为true 实例是肯定可以用的
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow","true"));
    //在return一个jedis实例是否进行验证 如果为true 实例是肯定可以用的
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow","true"));;

    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip","127.0.0.1");
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port","6379"));
    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip","127.0.0.1");
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port","6379"));

    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);

        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        //连接耗尽是否阻塞 true 阻塞直到超时 false 抛出异常
        config.setBlockWhenExhausted(true);

        JedisShardInfo info1 = new JedisShardInfo(redis1Ip,redis1Port,1000 * 2);
        JedisShardInfo info2 = new JedisShardInfo(redis2Ip,redis2Port,1000 * 2);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<>();
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        pool = new ShardedJedisPool(config,jedisShardInfoList, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    static{
        initPool();
    }

    /**
     *
     * @return 获取单个实例
     */
    public static ShardedJedis getShardedJedis(){
        return pool.getResource();
    }

    /**
     * 将jedis实例放回连接池
     * @param shardedJedis
     */
    public static void closeJedis(ShardedJedis shardedJedis){
        shardedJedis.close();
    }
}
