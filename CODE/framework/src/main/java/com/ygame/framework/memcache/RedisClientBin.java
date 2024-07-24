/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.memcache;

import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import java.util.*;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 *
 * @author huynxt
 */
public class RedisClientBin {

    static final Logger logger = LogUtil.getLogger(RedisClientBin.class);

    private final RedisClient redisClient;

    public static RedisClientBin getInstance(String instanceName) {
        return new RedisClientBin(instanceName);
    }

    private RedisClientBin(String instanceName) {
        redisClient = RedisClient.getInstance(instanceName);
    }

    private Jedis borrowClient() {
        return redisClient.borrowClient();
    }

    private void returnClient(Jedis client) {
        redisClient.returnClient(client);
    }

    private void invalidClient(Jedis client) {
        redisClient.invalidClient(client);
    }

    private byte[] string2ByteArray(String key) {
        return key.getBytes();
    }

    private byte[][] string2ByteArray(String... keys) {
        List<byte[]> ls = new ArrayList<byte[]>();
        for (int i = 0; i < keys.length; ++i) {
            ls.add(keys[i].getBytes());
        }
        return (byte[][]) ls.toArray();
    }

    private String byteArray2String(byte[] b) {
        return new String(b);
    }

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

    public boolean set(String key, byte[] value) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            boolean result = "OK".equals(client.set(string2ByteArray(key), value));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public byte[] getListItemByIndex(String key, int index) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            byte[] result = client.lindex(string2ByteArray(key), index);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long del(String... keys) throws Exception {
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

    public boolean setnx(String key, byte[] value) throws Exception {
        Jedis client = null;
        try {
            client = borrowClient();
            byte[] keyBin = ConvertUtils.encodeString(key);
            long result = client.setnx(keyBin, value).longValue();
            returnClient(client);

            return result == 1;
        } catch (Exception ex) {
            invalidClient(client);
            throw ex;
        }
    }

    public boolean append(String key, byte[] value) throws Exception {
        Jedis client = null;
        try {
            client = borrowClient();
            byte[] keyBin = ConvertUtils.encodeString(key);
            long result = client.append(keyBin, value).longValue();
            returnClient(client);

            return result > 0;
        } catch (Exception ex) {
            invalidClient(client);
            throw ex;
        }
    }

    public byte[] get(String key) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            byte[] result = client.get(string2ByteArray(key));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public List<byte[]> get(final String... keys) throws Exception {
        Jedis client = null;
        try {
            client = borrowClient();
            byte[][] arrKeys = new byte[keys.length][];
            for (int i = 0; i < keys.length; i++) {
                arrKeys[i] = keys[i].getBytes();
            }
            List<byte[]> result = client.mget(arrKeys);
            returnClient(client);
            return result;

        } catch (Exception ex) {
            invalidClient(client);
            throw ex;
        }
    }

    public Map<String, byte[]> mget(List<String> keys) throws Exception {
        Jedis client = null;
        Map<String, byte[]> result = new HashMap<>();
        try {
            client = borrowClient();
            byte[][] arrKeys = new byte[keys.size()][];
            for (int i = 0; i < keys.size(); i++) {
                arrKeys[i] = keys.get(i).getBytes();
            }
            List<byte[]> data = client.mget(arrKeys);
            for (int i = 0; i < keys.size(); i++) {
                result.put(keys.get(i), data.get(i));
            }
            returnClient(client);

        } catch (Exception ex) {
            invalidClient(client);
        }
        return result;
    }

    public Long zadd(String key, double score, byte[] member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.zadd(string2ByteArray(key), score, member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long zadd(String key, Map<Double, byte[]> scoreMembers) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.zadd(string2ByteArray(key), scoreMembers);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long zrem(String key, byte[]... members) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.zrem(string2ByteArray(key), members);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Double zincrby(String key, double score, byte[] member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Double result = client.zincrby(string2ByteArray(key), score, member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Double zscore(String key, byte[] member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Double result = client.zscore(string2ByteArray(key), member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<byte[]> zrevrange(final String key, final long start, final long end) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<byte[]> result = client.zrevrange(string2ByteArray(key), (int) start, (int) end);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<byte[]> zrevrangebyscore(final String key, final double max, final double min) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<byte[]> result = client.zrevrangeByScore(string2ByteArray(key), max, min);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<byte[]> zrevrangebyscore(final String key, final double max, final double min, final int offset, final int count) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<byte[]> result = client.zrevrangeByScore(string2ByteArray(key), max, min, offset, count);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<byte[]> zrange(final String key, final long start, final long end) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<byte[]> result = client.zrange(string2ByteArray(key), (int) start, (int) end);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<byte[]> zrangebyscore(final String key, final double min, final double max) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<byte[]> result = client.zrangeByScore(string2ByteArray(key), min, max);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<byte[]> zrangebyscore(final String key, final double max, final double min, final int offset, final int count) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<byte[]> result = client.zrangeByScore(string2ByteArray(key), max, min, offset, count);
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
            Long result = client.zcount(string2ByteArray(key), min, max);
            this.returnClient(client);

            return result.longValue();
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long zrank(String key, byte[] member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.zrank(string2ByteArray(key), member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long zcard(String key) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.zcard(string2ByteArray(key));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public List<byte[]> lrange(final String key, final long start, final long end) throws Exception {

        Jedis client = null;
        try {

            client = this.borrowClient();
            List<byte[]> result = client.lrange(string2ByteArray(key), (int) start, (int) end);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long lpush(final String key, final byte[]... strings) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.lpush(string2ByteArray(key), strings);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long rpush(final String key, final byte[]... strings) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.rpush(string2ByteArray(key), strings);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long linsert(final String key, LIST_POSITION where, byte[] pivot, byte[] value) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.linsert(string2ByteArray(key), where, pivot, value);
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
            Long result = client.llen(string2ByteArray(key));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long lrem(final String key, int count, byte[] value) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.lrem(string2ByteArray(key), count, value);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long hset(final String key, String field, byte[] value) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.hset(string2ByteArray(key), string2ByteArray(field), value);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long hsetnx(final String key, String field, byte[] value) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.hsetnx(string2ByteArray(key), string2ByteArray(field), value);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long hdel(final String key, String... field) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Long result = client.hdel(string2ByteArray(key), string2ByteArray(field));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public byte[] hget(final String key, String field) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            byte[] result = client.hget(string2ByteArray(key), string2ByteArray(field));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Map<String, byte[]> hgetAll(final String key) throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Map<String, byte[]> result = new HashMap<String, byte[]>();
            Map<byte[], byte[]> map = client.hgetAll(string2ByteArray(key));
            Set<Entry<byte[], byte[]>> set = map.entrySet();
            for (Entry<byte[], byte[]> entry : set) {
                result.put(byteArray2String(entry.getKey()), entry.getValue());
            }
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="set">
    public Long sadd(String key, byte[] member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.sadd(string2ByteArray(key), member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Long srem(String key, byte[] member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Long result = client.srem(string2ByteArray(key), member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public byte[] spop(String key) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            byte[] result = client.spop(string2ByteArray(key));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public boolean sismembers(String key, byte[] member) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Boolean result = client.sismember(string2ByteArray(key), member);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<byte[]> smembers(String key) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            Set<byte[]> result = client.smembers(string2ByteArray(key));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public byte[] srandmember(String key) throws Exception {
        Jedis client = null;
        try {

            client = this.borrowClient();
            byte[] result = client.srandmember(string2ByteArray(key));
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<byte[]> sinter(String... keys) throws Exception {
        Jedis client = null;
        try {
            byte[][] bkeys = new byte[keys.length][];
            for (int i = 0; i < keys.length; ++i) {
                bkeys[i] = string2ByteArray(keys[i]);
            }
            client = this.borrowClient();
            Set<byte[]> result = client.sinter(bkeys);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public Set<byte[]> sunion(String... keys) throws Exception {
        Jedis client = null;
        try {
            byte[][] bkeys = new byte[keys.length][];
            for (int i = 0; i < keys.length; ++i) {
                bkeys[i] = string2ByteArray(keys[i]);
            }
            client = this.borrowClient();
            Set<byte[]> result = client.sunion(bkeys);
            this.returnClient(client);

            return result;
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="transaction">
    public RedisTransactionClient beginTrans() throws Exception {
        Jedis client = null;
        try {
            client = this.borrowClient();
            Transaction trans = client.multi();
            return new RedisTransactionClient(this, client, trans);
        } catch (Exception ex) {
            this.invalidClient(client);
            throw ex;
        }
    }

    public static class RedisTransactionClient {

        RedisClientBin rsClient;
        Jedis client;
        Transaction trans;

        public RedisTransactionClient(RedisClientBin rsClient, Jedis client, Transaction trans) {
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

        public boolean set(String key, byte[] value) throws Exception {
            if (trans == null) {
                return false;
            }
            try {
                trans.set(rsClient.string2ByteArray(key), value);
                return true;
            } catch (Exception ex) {
                logger.error(LogUtil.stackTrace(ex));
                return false;
            }
        }

        public long setnx(String key, byte[] value) throws Exception {
            if (trans == null) {
                return 0;
            }
            try {
                trans.setnx(rsClient.string2ByteArray(key), value);
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
                byte[][] arr = new byte[keys.length][];
                for (int i = 0; i < keys.length; ++i) {
                    arr[i] = rsClient.string2ByteArray(keys[i]);
                }
                trans.del(arr);
                return 1;
            } catch (Exception ex) {
                logger.error(LogUtil.stackTrace(ex));
                return 0;
            }
        }

        public long lpush(final String key, final byte[] value) throws Exception {
            if (trans == null) {
                return 0L;
            }
            try {
                trans.lpush(rsClient.string2ByteArray(key), value);
                return 1L;
            } catch (Exception ex) {
                throw ex;
            }
        }

        public long rlpush(final String key, final byte[] value) throws Exception {
            if (trans == null) {
                return 0L;
            }
            try {
                trans.rpush(rsClient.string2ByteArray(key), value);
                return 1L;
            } catch (Exception ex) {
                throw ex;
            }
        }

        public long linsert(final String key, LIST_POSITION where, byte[] pivot, byte[] value) throws Exception {
            if (trans == null) {
                return 0L;
            }
            try {
                trans.linsert(rsClient.string2ByteArray(key), where, pivot, value);
                return 1L;
            } catch (Exception ex) {
                throw ex;
            }
        }

        public Long lrem(final String key, int count, byte[] value) throws Exception {
            if (trans == null) {
                return 0L;
            }
            try {
                trans.lrem(rsClient.string2ByteArray(key), count, value);
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

    public boolean set(Transaction trans, String key, byte[] value) throws Exception {
        if (trans == null) {
            return false;
        }
        try {
            trans.set(string2ByteArray(key), value);
            return true;
        } catch (Exception ex) {
            logger.error(LogUtil.stackTrace(ex));
            return false;
        }
    }

    public long setnx(Transaction trans, String key, byte[] value) throws Exception {
        if (trans == null) {
            return 0;
        }
        try {
            trans.setnx(string2ByteArray(key), value);
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
            byte[][] arr = new byte[keys.length][];
            for (int i = 0; i < keys.length; ++i) {
                arr[i] = string2ByteArray(keys[i]);
            }
            trans.del(arr);
            return 1;
        } catch (Exception ex) {
            logger.error(LogUtil.stackTrace(ex));
            return 0;
        }
    }

    public long lpush(Transaction trans, final String key, final byte[] value) throws Exception {
        if (trans == null) {
            return 0L;
        }
        try {
            trans.lpush(string2ByteArray(key), value);
            return 1L;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public long linsert(Transaction trans, final String key, LIST_POSITION where, byte[] pivot, byte[] value) throws Exception {
        if (trans == null) {
            return 0L;
        }
        try {
            trans.linsert(string2ByteArray(key), where, pivot, value);
            return 1L;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public Long lrem(Transaction trans, final String key, int count, byte[] value) throws Exception {
        if (trans == null) {
            return 0L;
        }
        try {
            trans.lrem(string2ByteArray(key), count, value);
            return 1L;
        } catch (Exception ex) {
            throw ex;
        }
    }
//</editor-fold>
}
