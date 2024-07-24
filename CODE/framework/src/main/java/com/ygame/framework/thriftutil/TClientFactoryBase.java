/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.thriftutil;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.thrift.TServiceClientFactory;

/**
 *
 * @author huynxt
 */
public abstract class TClientFactoryBase implements PoolableObjectFactory {

	protected final TClientPoolConfig.ConnConfig _connCfg;

	protected TClientFactoryBase(TClientPoolConfig.ConnConfig connCfg) {
		assert (connCfg != null);
		_connCfg = TClientPoolConfig.CloneConnConfig(connCfg);
		assert (_connCfg.addr != null && !_connCfg.addr.isEmpty()
				&& _connCfg.port > 0
				&& _connCfg.framed == true //only support for framed transport
				&& _connCfg.timeout >= 0);
	}

	public TClientPoolConfig.ConnConfig getConnCfg() {
		return TClientPoolConfig.CloneConnConfig(_connCfg);
	}

	abstract public Class getClientClass();

	abstract public TServiceClientFactory<?> getServiceCliFactory();

	@Override
	public String toString() {
		return "TClientFactoryBase{" + "_connCfg=" + _connCfg + '}';
	}
}
