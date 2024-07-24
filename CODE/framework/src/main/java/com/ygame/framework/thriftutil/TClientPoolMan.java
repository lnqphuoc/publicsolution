/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.thriftutil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author huynxt
 */
public class TClientPoolMan {

	public static final TClientPoolMan Instance = new TClientPoolMan();
	protected final Map<String, TClientPoolBase> _map_AddrPortClass_Pool = new HashMap<String, TClientPoolBase>();
	protected final Map<String, List<TClientPoolBase>> _map_AddrPort_ListPool = new HashMap<String, List<TClientPoolBase>>();
	protected final Map<Class, List<TClientPoolBase>> _map_Class_ListPool = new HashMap<Class, List<TClientPoolBase>>();
	protected final Object _lock = new Object();

	protected TClientPoolMan() {
	}

	protected static String AddrPortToString(String addr, int port) {
		assert (addr != null);
		return "@" + addr + ":" + port;
	}

	protected static String AddrPortClassToString(String addr, int port, Class cls) {
		assert (addr != null && cls != null);
		String clsToString = (cls.isInterface() ? "interface_" : (cls.isPrimitive() ? "" : "class_"))
				+ cls.getName();
		return "@" + addr + ":" + port + "/" + clsToString;
	}

	protected boolean _addPool(TClientPoolBase pool) {
		assert (pool != null);
		TClientPoolConfig.ConnConfig connCfg = pool.getConnCfg();
		assert (connCfg != null);
		Class cls = pool.getClientClass();

		//check pool is already exist
		String key0 = AddrPortClassToString(connCfg.addr, connCfg.port, cls);
		if (_map_AddrPortClass_Pool.containsKey(key0)) {
			return false; //already exist
		}

		//add pool into map0
		_map_AddrPortClass_Pool.put(key0, pool);

		//add pool into map1
		String key1 = AddrPortToString(connCfg.addr, connCfg.port);
		List<TClientPoolBase> listPool1 = _map_AddrPort_ListPool.get(key1);
		if (listPool1 == null) {
			listPool1 = new LinkedList<TClientPoolBase>();
			_map_AddrPort_ListPool.put(key1, listPool1);
		}
		assert (!listPool1.contains(pool));
		listPool1.add(pool);

		//add pool into map2
		List<TClientPoolBase> listPool2 = _map_Class_ListPool.get(cls);
		if (listPool2 == null) {
			listPool2 = new LinkedList<TClientPoolBase>();
			_map_Class_ListPool.put(cls, listPool2);
		}
		assert (!listPool2.contains(pool));
		listPool2.add(pool);

		return true;
	}

	protected void _removePool(TClientPoolBase pool) {
		assert (pool != null);
		TClientPoolConfig.ConnConfig connCfg = pool.getConnCfg();
		assert (connCfg != null);
		Class cls = pool.getClientClass();

		//check exist pool to remove it from map0
		String key0 = AddrPortClassToString(connCfg.addr, connCfg.port, cls);
		TClientPoolBase foundPool0 = _map_AddrPortClass_Pool.remove(key0);
		if (foundPool0 == null) {
			return; //not exist
		}

		//remove pool from map1
		String key1 = AddrPortToString(connCfg.addr, connCfg.port);
		List<TClientPoolBase> foundListPool1 = _map_AddrPort_ListPool.remove(key1);
		assert (foundListPool1 != null);
		boolean foundPool1 = foundListPool1.remove(pool);
		assert (foundPool1 == true);

		//remove pool from map2
		List<TClientPoolBase> foundListPool2 = _map_Class_ListPool.remove(cls);
		assert (foundListPool2 != null);
		boolean foundPool2 = foundListPool2.remove(pool);
		assert (foundPool2 == true);
	}

	public boolean addPool(TClientPool<?> pool) {
		System.out.println("Add pool: " + pool);
		synchronized (_lock) {
			return _addPool(pool);
		}
	}

	public void removePool(TClientPool<?> pool) {
		System.out.println("Remove pool: " + pool);
		synchronized (_lock) {
			_removePool(pool);
		}
	}

	public TClientPoolBase getPool(String addr, int port, Class cls) {
		assert (addr != null && !addr.isEmpty() && port > 0 && cls != null);
		String key0 = AddrPortClassToString(addr, port, cls);
		synchronized (_lock) {
			return _map_AddrPortClass_Pool.get(key0);
		}
	}

	public List<TClientPoolBase> getListPool(String addr, int port) {
		assert (addr != null && !addr.isEmpty() && port > 0);
		String key1 = AddrPortToString(addr, port);
		List<TClientPoolBase> foundListPool1 = null;
		synchronized (_lock) {
			foundListPool1 = _map_AddrPort_ListPool.get(key1);
		}
		return foundListPool1;
	}

	public List<TClientPoolBase> getListPool(Class cls) {
		assert (cls != null);
		List<TClientPoolBase> foundListPool2 = null;
		synchronized (_lock) {
			foundListPool2 = _map_Class_ListPool.get(cls);
		}
		return foundListPool2;
	}

	public TClientPoolBase getFirstPool(Class cls) {
		List<TClientPoolBase> foundListPool2 = getListPool(cls);
		if (foundListPool2 == null) {
			return null;
		}
		Iterator<TClientPoolBase> poolIt = foundListPool2.iterator();
		if (!poolIt.hasNext()) {
			return null;
		}
		return poolIt.next();
	}
}
