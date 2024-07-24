/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.memcache;

import com.ygame.framework.common.Config;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

/**
 *
 * @author huynxt
 */
public class RedisClient {

    private static Logger logger = LogUtil.getLogger(RedisClient.class);
    private static final Lock createLock_ = new ReentrantLock();
    private static Map<String, RedisClient> instances = new NonBlockingHashMap();
    //private ArrayBlockingQueue<Jedis> pooledJedis;
    private JedisPool pooledJedis;
    private String host;
    private Integer port;
    private Integer maxPool;
    private String password;
    //private Integer minPool;
    private Integer timeout = 5000;

    public static RedisClient getInstance(String instanceName) {
        RedisClient instance = instances.get(instanceName);
        if (instance == null) {
            try {
                createLock_.lock();
                instance = instances.get(instanceName);
                if (instance == null) {
                    instance = new RedisClient(instanceName);
                    instances.put(instanceName, instance);
                }
            } finally {
                createLock_.unlock();
            }
        }
        return instance;
    }

    public RedisClient(String instanceName) {
        host = Config.getParam(instanceName, "host");
        port = ConvertUtils.toInt(Config.getParam(instanceName, "port"), 4730);
        password = ConvertUtils.toString(Config.getParam(instanceName, "password"), "");

        maxPool = ConvertUtils.toInt(Config.getParam(instanceName, "maxpool"), 1024);
        timeout = ConvertUtils.toInt(Config.getParam(instanceName, "timeout"), 5000);

//        GenericObjectPoolConfig jConfig = new GenericObjectPoolConfig();
//        jConfig.setMaxIdle(maxPool);
//        jConfig.setMinIdle(GenericObjectPool.DEFAULT_MIN_IDLE);
//        jConfig.setMaxTotal(-1); //unlimit
//        jConfig.setMaxWaitMillis(-1); //unlimit
//        //jConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
//        jConfig.setTestOnBorrow(true);
//        jConfig.setTestOnReturn(true);
//        jConfig.setTestWhileIdle(true);
//        jConfig.setTimeBetweenEvictionRunsMillis(5000); //5 secs
//        jConfig.setNumTestsPerEvictionRun(-1); //take size of pools
//        jConfig.setMinEvictableIdleTimeMillis(3600000); //1hour
//        jConfig.setSoftMinEvictableIdleTimeMillis(-1); //unlimit
//        jConfig.setLifo(false); //queue
        JedisPoolConfig jConfig = new JedisPoolConfig();
        jConfig.maxIdle = maxPool;
        jConfig.minIdle = GenericObjectPool.DEFAULT_MIN_IDLE;
        jConfig.maxActive = -1; //unlimit
        jConfig.maxWait = -1; //unlimit
        jConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
        jConfig.testOnBorrow = true;
        jConfig.testOnReturn = true;
        jConfig.testWhileIdle = true;
        jConfig.timeBetweenEvictionRunsMillis = 5000; //5 secs
        jConfig.numTestsPerEvictionRun = -1; //take size of pools
        jConfig.minEvictableIdleTimeMillis = 3600000; //1hour
        jConfig.softMinEvictableIdleTimeMillis = -1; //unlimit
        jConfig.lifo = false; //queue
        if (password.length() > 0) {
            pooledJedis = new JedisPool(jConfig, host, port, timeout, password);
        } else {
            pooledJedis = new JedisPool(jConfig, host, port, timeout);
        }
    }

    protected Jedis borrowClient() {
        Jedis client = pooledJedis.getResource();
        return client;
    }

    protected void returnClient(Jedis client) {
        pooledJedis.returnResource(client);
    }

    protected void invalidClient(Jedis client) {
        pooledJedis.returnBrokenResource(client);
    }

    public List<String> lrange(final String key, final long start, final long end) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            List<String> result = client.lrange(key, (int) start, (int) end);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long lpush(final String key, final String... strings) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.lpush(key, strings);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long linsert(final String key, LIST_POSITION where, String pivot, String value) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.linsert(key, where, pivot, value);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long llen(final String key) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.llen(key);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long lrem(final String key, int count, String value) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.lrem(key, count, value);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public boolean set(String key, String value) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            boolean result = "OK".equals(client.set(key, value));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public boolean mset(Map<String, String> keysvaules) throws Exception {

        Jedis client = null;
        try {
            String[] datas = new String[keysvaules.size() * 2];
            int index = 0;
            for (Entry<String, String> entry : keysvaules.entrySet()) {
                datas[index] = entry.getKey();
                datas[index + 1] = entry.getValue();
                index += 2;
            }
            client = this.borrowClient();
            //System.out.println(datas);
            boolean result = "OK".equals(client.mset(datas));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public long setnx(String key, String value) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            long result = client.setnx(key, value);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public boolean append(String key, String value) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            boolean result = "OK".equals(client.append(key, value));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public String get(String key) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            String result = client.get(key);
            this.returnClient(client);
            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public List<String> get(final String... keys) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            List<String> result = client.mget(keys);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Map<String, String> mget(List<String> keys) throws Exception {
        Jedis client = null;
        Map<String, String> result = new HashMap<>();
        try {
            client = borrowClient();
            List<String> data = client.mget(keys.toArray(new String[keys.size()]));
            for (int i = 0; i < keys.size(); i++) {
                result.put(keys.get(i), data.get(i));
            }
            returnClient(client);

        } catch (Exception ex) {
            invalidClient(client);
        }
        return result;
    }

    public Long incr(String key) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.incr(key);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long incrBy(String key, long by) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.incrBy(key, by);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }

    }

    public Long decr(String key) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.decr(key);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long decrBy(String key, long by) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.decrBy(key, by);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Double zincrby(String key, double score, String member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Double result = client.zincrby(key, score, member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<String> zrevrange(String key, long start, long end) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Set<String> result = client.zrevrange(key, start, end);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<String> zrangebyscore(final String key, final double min, final double max) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<String> result = client.zrangeByScore(key, min, max);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<String> zrangebyscore(final String key, final double max, final double min, final int offset, final int count) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<String> result = client.zrangeByScore(key, max, min, offset, count);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<String> zrevrangebyscore(final String key, final double max, final double min) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<String> result = client.zrevrangeByScore(key, max, min);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<String> zrevrangebyscore(final String key, final double max, final double min, final int offset, final int count) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<String> result = client.zrevrangeByScore(key, max, min, offset, count);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long zadd(String key, double score, String member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.zadd(key, score, member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long zadd(String key, Map<Double, String> scoreMembers) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.zadd(key, scoreMembers);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Double zscore(String key, String member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Double result = client.zscore(key, member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long zrem(String key, String... members) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.zrem(key, members);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public long zcount(final String key, final double min, final double max) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.zcount(key, min, max);
            this.returnClient(client);

            return result.longValue();
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="set">
    public Long sadd(String key, String member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.sadd(key, member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long srem(String key, String member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.srem(key, member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public String spop(String key) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            String result = client.spop(key);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public boolean sismembers(String key, String member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Boolean result = client.sismember(key, member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<String> smembers(String key) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<String> result = client.smembers(key);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public String srandmember(String key) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            String result = client.srandmember(key);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<String> sinter(String... keys) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<String> result = client.sinter(keys);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<String> sunion(String... keys) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<String> result = client.sunion(keys);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }
    //</editor-fold>

    public Long expire(String key, int seconds) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.expire(key, seconds);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long delete(String... keys) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.del(keys);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="transaction">
    public RedisTransactionClient beginTrans() throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Transaction trans = client.multi();
            return new RedisClient.RedisTransactionClient(this, client, trans);
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public static class RedisTransactionClient {

        RedisClient rsClient;
        Jedis client;
        Transaction trans;

        public RedisTransactionClient(RedisClient rsClient, Jedis client, Transaction trans) {
            this.rsClient = rsClient;
            this.client = client;
            this.trans = trans;
        }

        public boolean commit() {
            if (trans == null) {
                return false;
            }
            try {
                trans.exec();
                return true;
            } catch (Exception ex) {
                logger.error(LogUtil.stackTrace(ex));
            } finally {
                rsClient.returnClient(client);
            }
            return false;
        }

        public boolean rollback() {
            if (trans == null) {
                return false;
            }
            try {
                trans.discard();
                return true;
            } catch (Exception ex) {
                logger.error(LogUtil.stackTrace(ex));
            } finally {
                rsClient.returnClient(client);
            }
            return false;
        }

        public boolean set(String key, String value) throws Exception {
            if (trans == null) {
                return false;
            }
            try {
                trans.set(key, value);
                return true;
            } catch (Exception ex) {
                logger.error(LogUtil.stackTrace(ex));
                return false;
            }
        }

        public long setnx(String key, String value) throws Exception {
            if (trans == null) {
                return 0;
            }
            try {
                trans.setnx(key, value);
                return 1;
            } catch (Exception ex) {
                logger.error(LogUtil.stackTrace(ex));
                return 0;
            }
        }

        public long delete(String... keys) throws Exception {
            if (trans == null) {
                return 0L;
            }
            try {
                trans.del(keys);
                return 1;
            } catch (Exception ex) {
                logger.error(LogUtil.stackTrace(ex));
                return 0;
            }
        }

        public long lpush(final String key, final String value) throws Exception {
            if (trans == null) {
                return 0L;
            }
            try {
                trans.lpush(key, value);
                return 1L;
            } catch (Exception ex) {
                throw ex;
            }
        }

        public long linsert(final String key, LIST_POSITION where, String pivot, String value) throws Exception {
            if (trans == null) {
                return 0L;
            }
            try {
                trans.linsert(key, where, pivot, value);
                return 1L;
            } catch (Exception ex) {
                throw ex;
            }
        }

        public Long lrem(final String key, int count, String value) throws Exception {
            if (trans == null) {
                return 0L;
            }
            try {
                trans.lrem(key, count, value);
                return 1L;
            } catch (Exception ex) {
                throw ex;
            }
        }

        public long ltrim(final String key, int start, int end) throws Exception {
            if (trans == null) {
                return 0L;
            }
            try {
                trans.ltrim(key, start, end);
                return 1L;
            } catch (Exception ex) {
                throw ex;
            }
        }
    }

    public Transaction beginTransaction() throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Transaction trans = client.multi();
            return trans;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public boolean commit(Transaction trans) {
        if (trans == null) {
            return false;
        }
        try {
            trans.exec();
            return true;
        } catch (Exception ex) {
            logger.error(LogUtil.stackTrace(ex));
        }
        return false;
    }

    public boolean rollback(Transaction trans) {
        if (trans == null) {
            return false;
        }
        try {
            trans.discard();
            return true;
        } catch (Exception ex) {
            logger.error(LogUtil.stackTrace(ex));
        }
        return false;
    }

    public boolean set(Transaction trans, String key, String value) throws Exception {
        if (trans == null) {
            return false;
        }
        try {
            trans.set(key, value);
            return true;
        } catch (Exception ex) {
            logger.error(LogUtil.stackTrace(ex));
            return false;
        }
    }

    public long setnx(Transaction trans, String key, String value) throws Exception {
        if (trans == null) {
            return 0;
        }
        try {
            trans.setnx(key, value);
            return 1;
        } catch (Exception ex) {
            logger.error(LogUtil.stackTrace(ex));
            return 0;
        }
    }

    public long delete(Transaction trans, String... keys) throws Exception {
        if (trans == null) {
            return 0L;
        }
        try {
            trans.del(keys);
            return 1;
        } catch (Exception ex) {
            logger.error(LogUtil.stackTrace(ex));
            return 0;
        }
    }

    public long lpush(Transaction trans, final String key, final String value) throws Exception {
        if (trans == null) {
            return 0L;
        }
        try {
            trans.lpush(key, value);
            return 1L;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public long linsert(Transaction trans, final String key, LIST_POSITION where, String pivot, String value) throws Exception {
        if (trans == null) {
            return 0L;
        }
        try {
            trans.linsert(key, where, pivot, value);
            return 1L;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public Long lrem(Transaction trans, final String key, int count, String value) throws Exception {
        if (trans == null) {
            return 0L;
        }
        try {
            trans.lrem(key, count, value);
            return 1L;
        } catch (Exception ex) {
            throw ex;
        }
    }
//</editor-fold>
}
