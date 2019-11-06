package com.atguigu.gmall.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Jay
 * @create 2019-11-01 17:58
 */
public class RedisUtil {

    private JedisPool jedisPool;

    public void initJedisUtil(String host, int port, int timeOut) {
        // 配置连接池的参数类
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        // 设置连接池最大核心数
        jedisPoolConfig.setMaxTotal(200);
        // 最少剩余数
        jedisPoolConfig.setMinIdle(10);
        // 等待时间
        jedisPoolConfig.setMaxWaitMillis(10*1000);
        // 等待队列
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 设置当用户获取到jedis 时，做自检看当前获取到的jedis 是否可以使用
        jedisPoolConfig.setTestOnBorrow(true);

        jedisPool = new JedisPool(jedisPoolConfig, host, port, timeOut);

    }

    /**
     * 获取Jedis
     * @return
     */
    public Jedis getJedis() {
        return jedisPool.getResource();
    }


}
