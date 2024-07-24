/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.memcache;

/**
 *
 * @author huynxt
 */
import com.ygame.framework.common.Config;
import com.ygame.framework.utils.ConvertUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.Transcoder;
import org.apache.commons.lang.StringUtils;

public class SpyMemcache {

    private static final Lock createLock_ = new ReentrantLock();
    static int timeout_get = 2500;
    static int timeout_set = 2500;
    //private static Logger logger_ = Logger.getLogger(SpyMemcache.class);
    MemcachedClient client;
    private static Map<String, SpyMemcache> _instances = new HashMap();

    public static void shutdown() {
        for (Map.Entry<String, SpyMemcache> entry : _instances.entrySet()) {
            //String string = entry.getKey();
            SpyMemcache spyMemcache = entry.getValue();
            spyMemcache.client.shutdown();
        }
        _instances.clear();
    }

//    public static boolean validateKey(String key) {
//        try {
//            //StringUtils.validateKey(key, false);
//            StringUtils.validateKey(key, false);
//            return true;
//        } catch (Exception ex) {
//            return false;
//        }
//    }

    public static SpyMemcache getInstance(String instanceName) throws Exception {
        SpyMemcache instance = _instances.get(instanceName);
        if (instance == null) {
            try {
                createLock_.lock();
                instance = _instances.get(instanceName);
                if (instance == null) {
                    instance = new SpyMemcache(instanceName);
                    _instances.put(instanceName, instance);
                }
            } finally {
                createLock_.unlock();
            }
        }
        return instance;
    }
// To-do : checking ser-der and connection factory

    public SpyMemcache(String instanceName) throws Exception {

        String hosts = Config.getParam(instanceName, "hosts");
        String useBinary = ConvertUtils.toString(Config.getParam(instanceName, "binary"), "yes");

        // use binary protocol
        if (useBinary != null && "yes".equals(useBinary.toLowerCase())) {
            this.client = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses(hosts));
        } else {
            this.client = new MemcachedClient(AddrUtil.getAddresses(hosts));
        }
    }

    public Future<Object> getAsync(String key) throws Exception {
        return this.client.asyncGet(key);
    }

    public Object get(String key) throws Exception {
        Future f = this.client.asyncGet(key);
        try {
            return f.get(timeout_get, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            f.cancel(true);
            throw e;
        }
    }

    public <T> T get(String key, Transcoder<T> tc) throws Exception {
        Future f = this.client.asyncGet(key, tc);
        try {
            return (T) f.get(timeout_get, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            f.cancel(true);
            throw e;
        }
    }

    public Future<Map<String, Object>> getMutlAsync(String[] keys) {
        return this.client.asyncGetBulk(keys);
    }

    public Map<String, Object> getMulti(String[] key) throws Exception {
        Future f = this.client.asyncGetBulk(key);
        try {
            return (Map) f.get(timeout_get, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            f.cancel(true);
            throw e;
        }
    }

    public Future<Map<String, Object>> getMutlAsync(Collection<String> keys) {
        return this.client.asyncGetBulk(keys);
    }

    public Map<String, Object> getMulti(Collection<String> keys) throws Exception {
        Future f = this.client.asyncGetBulk(keys);
        try {
            return (Map) f.get(timeout_get, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            f.cancel(true);
            throw e;
        }
    }

    public boolean add(String key, Object value) throws Exception {
        return add(key, value, 0);
    }

    public boolean add(String key, Object value, int exp) throws Exception {
        Future f = this.client.add(key, exp, value);
        try {
            return ((Boolean) f.get(timeout_set, TimeUnit.MILLISECONDS)).booleanValue();
        } catch (Exception e) {
            f.cancel(true);
            throw e;
        }
    }

    public boolean set(String key, Object value) throws Exception {
        return set(key, value, 0);
    }

    public boolean set(String key, Object value, int exp) throws Exception {
        Future f = this.client.set(key, exp, value);
        try {
            return ((Boolean) f.get(timeout_set, TimeUnit.MILLISECONDS)).booleanValue();
        } catch (Exception e) {
            f.cancel(true);
            throw e;
        }
    }

    public long incr(String key) {
        return this.incr(key, 1);
    }

    public long incr(String key, long value) {
        long ret = this.client.incr(key, value, value);
        // try again
        if (ret == -1L) {
            ret = this.client.incr(key, value, value);
        }
        return ret;
    }

    public long addCounter(String key, long value) {
        return this.client.incr(key, 0, value);
    }

    public boolean setCounter(String key, long value) throws Exception {
        return set(key, String.valueOf(value));
    }

    public long decr(String key) {
        return this.decr(key, 1);
    }

    public long decr(String key, long value) {
        return this.client.decr(key, value);
    }

    public boolean delete(String key) throws Exception {
        Future f = this.client.delete(key);
        try {
            return ((Boolean) f.get(timeout_set, TimeUnit.MILLISECONDS)).booleanValue();
        } catch (Exception e) {
            f.cancel(true);
            throw e;
        }
    }

    public boolean append(String key, Object value) throws Exception {
        Future f = this.client.append(0L, key, value);
        try {
            return ((Boolean) f.get(timeout_set, TimeUnit.MILLISECONDS)).booleanValue();
        } catch (Exception e) {
            f.cancel(true);
            throw e;
        }
    }

    public boolean prepend(String key, Object value) throws Exception {
        Future f = this.client.prepend(0L, key, value);
        try {
            return ((Boolean) f.get(timeout_set, TimeUnit.MILLISECONDS)).booleanValue();
        } catch (Exception e) {
            f.cancel(true);
            throw e;
        }
    }
}
