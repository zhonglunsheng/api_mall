package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    //获取连接池实例
    private static JedisPool pool;

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

    private static String redisIp = PropertiesUtil.getProperty("redis.ip","127.0.0.1");
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port","6379"));

    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);

        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        //连接耗尽是否阻塞 true 阻塞直到超时 false 抛出异常
        config.setBlockWhenExhausted(true);

        pool = new JedisPool(config,redisIp,redisPort,1000*2);
    }

    static{
        initPool();
    }

    /**
     *
     * @return 获取单个实例
     */
    public static Jedis getJedis(){
        return pool.getResource();
    }

    /**
     * 将jedis实例放回连接池
     * @param jedis
     */
    public static void closeJedis(Jedis jedis){
        jedis.close();
    }



}
