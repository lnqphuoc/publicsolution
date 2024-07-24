/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.memcache;

import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author huynxt
 */
public class SpyMemcachedTransaction {

    static Logger _logger = Logger.getLogger(SpyMemcachedTransaction.class);
    SpyMemcache _client = null;
    Map<String, TaskEnt> _tasks = new HashMap<String, TaskEnt>();

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
        public Object value;

        public TaskEnt(TaskDef action, String key, Object value) {
            this.action = action;
            this.key = key;
            this.value = value;
        }
    }

    public SpyMemcachedTransaction(SpyMemcache client) throws Exception {
        if (client == null) {
            throw new Exception("Client can not be null.");
        }
        _client = client;
    }

    public long incr(String key) throws Exception {
        return incr(key, 1);
    }

    public long incr(String key, long value) throws Exception {
        return _client.incr(key, value);
    }

    public long decr(String key) throws Exception {
        return decr(key, 1);
    }

    public long decr(String key, long value) throws Exception {
        return _client.decr(key, value);
    }

    public long addCounter(String key, long value) {
        return _client.addCounter(key, value);
    }

    public boolean add(String key, Object value) throws Exception {
        return add(key, value, 0);
    }

    public boolean add(String key, Object value, int exp) throws Exception {
        if (_client.add(key, value, exp)) {
            if (!_tasks.containsKey(key)) {
                TaskEnt item = new TaskEnt(TaskDef.DEL, key, value);
                _tasks.put(key, item);
            }
            return true;
        }
        return false;
    }

    public boolean set(String key, Object value) throws Exception {
        return set(key, value, 0);
    }

    public boolean set(String key, Object value, int exp) throws Exception {
        Object oldValue = _client.get(key);
        TaskDef action = TaskDef.SET;
        if (oldValue == null) {
            action = TaskDef.DEL;
        }
        if (_client.set(key, value, exp)) {
            if (!_tasks.containsKey(key)) {
                TaskEnt item = new TaskEnt(action, key, oldValue);
                _tasks.put(key, item);
            }
            return true;
        }
        return false;
    }

    public boolean delete(String key) throws Exception {
        Object oldValue = _client.get(key);
        if (oldValue == null) {
            return true;
        }
        if (_client.delete(key)) {
            if (!_tasks.containsKey(key)) {
                TaskEnt item = new TaskEnt(TaskDef.ADD, key, oldValue);
                _tasks.put(key, item);
            }
            return true;
        }
        return false;
    }

    public boolean append(String key, Object value) throws Exception {
        Object oldValue = _client.get(key);
        if (oldValue == null) {
            return false;
        }
        if (_client.append(key, value)) {
            if (!_tasks.containsKey(key)) {
                TaskEnt item = new TaskEnt(TaskDef.SET, key, oldValue);
                _tasks.put(key, item);
            }
            return true;
        }
        return false;
    }

    public boolean prepend(String key, Object value) throws Exception {
        Object oldValue = _client.get(key);
        if (oldValue == null) {
            return false;
        }
        if (_client.prepend(key, value)) {
            if (!_tasks.containsKey(key)) {
                TaskEnt item = new TaskEnt(TaskDef.SET, key, oldValue);
                _tasks.put(key, item);
            }
            return true;
        }
        return false;
    }

    public boolean setCounter(String key, long value) throws Exception {
        return set(key, String.valueOf(value));
    }

    void executeTask(TaskEnt item) {
        try {
            switch (item.action) {
                case ADD:
                    _client.add(item.key, item.value);
                    break;
                case APPEND:
                    _client.append(item.key, item.value);
                    break;
                case DECR:
                    _client.decr(item.key, ConvertUtils.toLong(item.value, 1));
                    break;
                case DEL:
                    _client.delete(item.key);
                    break;
                case INCR:
                    _client.incr(item.key, ConvertUtils.toLong(item.value, 1));
                    break;
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
