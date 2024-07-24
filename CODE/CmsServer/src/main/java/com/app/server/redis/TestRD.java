package com.app.server.redis;

import com.app.server.config.ConfigInfo;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.memcache.RedisFactory;

import redis.clients.jedis.Jedis;

public class TestRD {

	public static String getData(String token) {
        Jedis jedis = null;
        RedisFactory client = null;
        try {
            client = RedisFactory.getInstance(ConfigInfo.REDIS);
            jedis = client.getClient();
            return jedis.get(token);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (jedis != null && client != null) {
                client.returnClient(jedis);
            }
        }
        return "";
    }

}
