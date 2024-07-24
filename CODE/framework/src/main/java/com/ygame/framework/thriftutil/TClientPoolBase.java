/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.thriftutil;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 *
 * @author huynxt
 */
public abstract class TClientPoolBase implements ObjectPool {

	protected final GenericObjectPool.Config _poolCfg;

	protected TClientPoolBase(GenericObjectPool.Config poolCfg) {
		assert (poolCfg != null);
		_poolCfg = TClientPoolConfig.ClonePoolConfig(poolCfg);
	}

	public TClientPoolConfig.ConnConfig getConnCfg() {
		return getClientFactory().getConnCfg();
	}

	public GenericObjectPool.Config getPoolCfg() {
		return TClientPoolConfig.ClonePoolConfig(_poolCfg);
	}

	abstract public Class getClientClass();

	abstract public TClientFactory<?> getClientFactory();

	abstract public long getBorrowCount();

	abstract public long getReturnCount();

	abstract public long getInvalidCount();
}
