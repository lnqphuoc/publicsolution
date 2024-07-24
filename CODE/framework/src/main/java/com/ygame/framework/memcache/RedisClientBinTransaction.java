/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.memcache;

import com.ygame.framework.common.LogUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author huynxt
 */
public class RedisClientBinTransaction {

    public enum TaskDef {

        ADD(1), SET(2), DEL(3), APPEND(4), INCR(5), DECR(6);
        private int code;

        private TaskDef(int c) {
            code = c;
        }

        public int TaskDef() {
            return code;
        }
    }

    class TaskEnt {

        public TaskDef action;
        public String key;
        public byte[] value;

        public TaskEnt(TaskDef action, String key, byte[] value) {
            this.action = action;
            this.key = key;
            this.value = value;
        }
    }
    static Logger _logger = Logger.getLogger(RedisClientBinTransaction.class);
    RedisClientBin _client = null;
    Map<String, TaskEnt> _tasks = new HashMap<String, TaskEnt>();

    public RedisClientBinTransaction(RedisClientBin client) throws Exception {
        if (client == null) {
            throw new Exception("Client can not be null.");
        }
        _client = client;
    }

    public boolean add(String key, byte[] value) throws Exception {
        if (_client.setnx(key, value)) {
            if (!_tasks.containsKey(key)) {
                TaskEnt item = new TaskEnt(TaskDef.DEL, key, value);
                _tasks.put(key, item);
            }
            return true;
        }
        return false;
    }

    public boolean set(String key, byte[] value) throws Exception {
        if (!_tasks.containsKey(key)) {
            byte[] oldValue = _client.get(key);
            TaskDef action = TaskDef.SET;
            if (oldValue == null) {
                action = TaskDef.DEL;
            }
            if (_client.set(key, value)) {
                TaskEnt item = new TaskEnt(action, key, oldValue);
                _tasks.put(key, item);
                return true;
            }
        }
        else {
            return _client.set(key, value);
        }
        return false;
    }

    public boolean delete(String key) throws Exception {
        if (!_tasks.containsKey(key)) {
            byte[] oldValue = _client.get(key);
            if (oldValue == null) {
                return false;
            }
            if (_client.del(key) > 0) {
                TaskEnt item = new TaskEnt(TaskDef.ADD, key, oldValue);
                _tasks.put(key, item);
                return true;
            }
        }
        else {
            return (_client.del(key) > 0);
        }
//        
        return false;
    }

    public boolean append(String key, byte[] value) throws Exception {
        if (!_tasks.containsKey(key)) {
            byte[] oldValue = _client.get(key);
            TaskDef action = TaskDef.SET;
            if (oldValue == null) {
                action = TaskDef.DEL;
            }
            if (_client.append(key, value)) {
                TaskEnt item = new TaskEnt(action, key, oldValue);
                _tasks.put(key, item);
                return true;
            }
        }
        else {
            return _client.append(key, value);
        }
        return false;
    }

//    public boolean prepend(String key, Object value) throws Exception {
//        Object oldValue = _client.get(key);
//        if (oldValue == null) {
//            return false;
//        }
//        if (_client.prepend(key, value)) {
//            if (!_tasks.containsKey(key)) {
//                SpyMemcachedTransaction.TaskEnt item = new SpyMemcachedTransaction.TaskEnt(SpyMemcachedTransaction.TaskDef.SET, key, oldValue);
//                _tasks.put(key, item);
//            }
//            return true;
//        }
//        return false;
//    }
    
    void executeTask(TaskEnt item) {
        try {
            switch (item.action) {
                case ADD:
                    _client.setnx(item.key, item.value);
                    break;
                case APPEND:
                    _client.append(item.key, item.value);
                    break;
//                case DECR:
//                    _client.(item.key, ConvertUtils.toLong(item.value, 1));
//                    break;
                case DEL:
                    _client.del(item.key);
                    break;
//                case INCR:
//                    _client.incr(item.key, ConvertUtils.toLong(item.value, 1));
//                    break;
                case SET:
                    _client.set(item.key, item.value);
                    break;
            }
        } catch (Exception ex) {
            _logger.error(LogUtil.stackTrace(ex));
        }
    }

    public void rollback() {
        try {
            Iterator iter = _tasks.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                executeTask((TaskEnt) entry.getValue());
                iter.remove();
            }
        } catch (Exception ex) {
        }
    }
}
