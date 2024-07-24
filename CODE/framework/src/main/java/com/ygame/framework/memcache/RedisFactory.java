package com.ygame.framework.memcache;

import com.ygame.framework.common.Config;
import com.ygame.framework.utils.ConvertUtils;
import ga.log4j.GA;
import java.util.HashMap;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class RedisFactory {

    private static RedisFactory sInstance;
    private static final Map<String, RedisFactory> mPoolJedis = new HashMap<>();
    private JedisPool jedisPool;

    public static synchronized RedisFactory getInstance(String instance) {
        RedisFactory sInstace = mPoolJedis.get(instance);
        if (sInstance == null) {
            sInstance = new RedisFactory(instance);
            mPoolJedis.put(instance, sInstace);
        }
        return sInstance;
    }

    public RedisFactory(String instance) {
        initPool(instance);
    }

    private void initPool(String poolName) {
        try {
            GA.app.info("trying to init pool " + poolName);
            String host = ConvertUtils.toString(Config.getParam(poolName, "default_host"), "127.0.0.1");
            String password = ConvertUtils.toString(Config.getParam(poolName, "default_password"), null);

            int port = ConvertUtils.toInt(Config.getParam(poolName, "default_port"), 6379);
            int timeout = ConvertUtils.toInt(Config.getParam(poolName, "default_timeout"), Protocol.DEFAULT_TIMEOUT);
            int maxActive = ConvertUtils.toInt(Config.getParam(poolName, "default_max_active"), 8);
            int maxIdle = ConvertUtils.toInt(Config.getParam(poolName, "default_max_idle"), 8);
            int minIdle = ConvertUtils.toInt(Config.getParam(poolName, "default_min_idle"), 8);

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxActive(maxActive);
            config.setMaxIdle(maxIdle);
            config.setMinIdle(minIdle);
            GA.app.info("{instance:" + poolName + ", host: " + host + ",port: " + port + ", pass: " + password + "}");

            jedisPool = new JedisPool(config, host, port, timeout, password);

            GA.app.info("initial " + poolName + " pool successfully!");
        } catch (Exception e) {
            GA.app.error("initPool Redis exception", e);
            System.exit(1);
        }
    }

    public Jedis getClient() {
        return jedisPool.getResource();
    }

    public void returnClient(Jedis jedis) {
        jedisPool.returnResource(jedis);
    }
}
