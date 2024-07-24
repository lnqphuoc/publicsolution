/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.thriftutil;

import java.util.NoSuchElementException;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;

/**
 *
 * @author huynxt
 */
public class TClientPool<_TServiceClient extends TServiceClient> extends TClientPoolBase {

	protected final GenericObjectPool _genericPool;
	protected final TClientFactory<_TServiceClient> _cliFactory;
	protected volatile long _borrowCount = 0;
	protected volatile long _returnCount = 0;
	protected volatile long _invalidCount = 0;

	public TClientPool(TServiceClientFactory<_TServiceClient> serviceCliFactory, TClientPoolConfig.ConnConfig connCfg, GenericObjectPool.Config poolCfg) {
		super(poolCfg);    
		_cliFactory = new TClientFactory<_TServiceClient>(serviceCliFactory, connCfg);
		_genericPool = new GenericObjectPool(_cliFactory, _poolCfg);
	}

	@Override
	public String toString() {
		return "TClientPool{" + "_cliFactory=" + _cliFactory + '}';
	}

	@Override
	public Class getClientClass() {
		return _cliFactory.getClientClass();
	}

	@Override
	public TClientFactory<?> getClientFactory() {
		return _cliFactory;
	}

	@Override
	public long getBorrowCount() {
		return _borrowCount;
	}

	@Override
	public long getInvalidCount() {
		return _invalidCount;
	}

	@Override
	public long getReturnCount() {
		return _returnCount;
	}

	@Deprecated
	public Object borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
		Object ret = _genericPool.borrowObject();
		if (ret != null) {
			++_borrowCount;
		}
		return ret;
	}

	@Deprecated
	public Object borrowObjExless() {
		try {
			return borrowObject();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public _TServiceClient borrowClient() throws Exception, NoSuchElementException, IllegalStateException {
		return (_TServiceClient) borrowObject();
	}

	public _TServiceClient borrowClientExless() {
		return (_TServiceClient) borrowObjExless();
	}

	public void returnObject(Object obj) throws Exception {
		if (obj == null) {
			return;
		}
		_genericPool.returnObject(obj);
		++_returnCount;
	}

	public boolean returnObjExless(Object obj) {
		try {
			returnObject(obj);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public void invalidateObject(Object obj) throws Exception {
		if (obj == null) {
			return;
		}
		System.out.println("invalidateObject " + obj + " in pool " + this.toString());
		_genericPool.invalidateObject(obj);
		++_invalidCount;
	}

	public boolean invalidObjExless(Object obj) {
		try {
			invalidateObject(obj);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
		_genericPool.addObject();
	}

	public int getNumIdle() throws UnsupportedOperationException {
		return _genericPool.getNumIdle();
	}

	public int getNumActive() throws UnsupportedOperationException {
		return _genericPool.getNumActive();
	}

	public void clear() throws Exception, UnsupportedOperationException {
		_genericPool.clear();
	}

	public void close() throws Exception {
		_genericPool.close();
	}

	@Deprecated
	public void setFactory(PoolableObjectFactory factory) throws IllegalStateException, UnsupportedOperationException {
		_genericPool.setFactory(factory);
	}
}
