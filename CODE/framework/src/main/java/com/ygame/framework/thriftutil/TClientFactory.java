/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.thriftutil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.*;

/**
 *
 * @author huynxt
 */
public class TClientFactory<_TServiceClient extends TServiceClient> extends TClientFactoryBase {

	protected final TServiceClientFactory<_TServiceClient> _serviceCliFactory;
	protected final Class _cliClass;
	protected final Method _pingMethod;

	public TClientFactory(TServiceClientFactory<_TServiceClient> serviceCliFactory, TClientPoolConfig.ConnConfig connCfg) {
		super(connCfg);
		_serviceCliFactory = serviceCliFactory;
		assert (_serviceCliFactory != null);

		_TServiceClient cli = makeTestObject();
		assert (cli != null);
		_cliClass = cli.getClass();
		Method pingMethod = null;
		try {
			pingMethod = _cliClass.getMethod("ping");
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		}
		_pingMethod = pingMethod;
	}

	@Override
	public String toString() {
		return "TClientFactory{" + "_cliClass=" + _cliClass + ", _connCfg=" + _connCfg + '}';
	}

	/**
	 * Create a client instance for testing, this instance will always throw a exception
	 * when a member method is invoked
	 * @return client object for retrieval Class info or any test..
	 */
	protected _TServiceClient makeTestObject() {
		TTransport transport = new TNullTransport();
		TProtocol protocol = new TNullProtocol(transport);
		try {
			transport.open();
		} catch (TTransportException ex) {
			ex.printStackTrace();
			return null;
		}
		return _serviceCliFactory.getClient(protocol);
	}

	/**
	 * Creates an instance that can be served by the pool.
	 * Instances returned from this method should be in the
	 * same state as if they had been
	 * {@link #activateObject activated}. They will not be
	 * activated before being served by the pool.
	 *
	 * @return an instance that can be served by the pool.
	 * @throws Exception if there is a problem creating a new instance,
	 *    this will be propagated to the code requesting an object.
	 */
    @Override
    public Object makeObject() throws Exception {
        TSocket socket = new TSocket(_connCfg.addr, _connCfg.port, _connCfg.timeout);
        TTransport transport = null;
        if (_connCfg.framed) {
            if (super._connCfg.useFastFrmTrans) {
                transport = new TFastFramedTransport(socket);
            } else {
                transport = new TFramedTransport(socket);
            }
        } else {
            throw new UnsupportedOperationException("No support for un-framed transport");
        }
        assert (transport != null);

        TProtocol protocol = new TBinaryProtocol(transport);
        transport.open();
        return _serviceCliFactory.getClient(protocol);
    }

	/**
	 * Destroys an instance no longer needed by the pool.
	 * <p>
	 * It is important for implementations of this method to be aware
	 * that there is no guarantee about what state <code>obj</code>
	 * will be in and the implementation should be prepared to handle
	 * unexpected errors.
	 * </p>
	 * <p>
	 * Also, an implementation must take in to consideration that
	 * instances lost to the garbage collector may never be destroyed.
	 * </p>
	 *
	 * @param obj the instance to be destroyed
	 * @throws Exception should be avoided as it may be swallowed by
	 *    the pool implementation.
	 * @see #validateObject
	 * @see ObjectPool#invalidateObject
	 */
	public void destroyObject(Object obj) throws Exception {
		if (obj == null) {
			return;
		}
		_TServiceClient client = (_TServiceClient) obj; //will throw a runtime exception if obj is not type of _TServiceClient
		TTransport itrans = client.getInputProtocol().getTransport();
		TTransport otrans = client.getOutputProtocol().getTransport();
		if (itrans != null) {
			itrans.close();
		}
		if (otrans != null && otrans != itrans) {
			otrans.close();
		}
	}

	/**
	 * Ensures that the instance is safe to be returned by the pool.
	 * Returns <code>false</code> if <code>obj</code> should be destroyed.
	 *
	 * @param obj the instance to be validated
	 * @return <code>false</code> if <code>obj</code> is not valid and should
	 *         be dropped from the pool, <code>true</code> otherwise.
	 */
	public boolean validateObject(Object obj) {
		if (obj == null || _pingMethod == null) {
			return false;
		}
		_TServiceClient client = null;
		try {
			client = (_TServiceClient) obj; //will throw a runtime exception if obj is not type of _TServiceClient
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

		try {
			_pingMethod.invoke(client);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
			return true; //??there is any better way
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			return true; //??there is any better way
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
			return true; //??there is any better way
		} catch (Exception ex) {
			// thrift's Exception: may be socket error
			return false;
		}

		return true;
	}

	/**
	 * Reinitialize an instance to be returned by the pool.
	 *
	 * @param obj the instance to be activated
	 * @throws Exception if there is a problem activating <code>obj</code>,
	 *    this exception may be swallowed by the pool.
	 * @see #destroyObject
	 */
	public void activateObject(Object obj) throws Exception {
		//usually dont care this
	}

	/**
	 * Uninitialize an instance to be returned to the idle object pool.
	 *
	 * @param obj the instance to be passivated
	 * @throws Exception if there is a problem passivating <code>obj</code>,
	 *    this exception may be swallowed by the pool.
	 * @see #destroyObject
	 */
	public void passivateObject(Object obj) throws Exception {
		//usually dont care this
	}

	@Override
	public Class getClientClass() {
		return _cliClass;
	}

	@Override
	public TServiceClientFactory<?> getServiceCliFactory() {
		return _serviceCliFactory;
	}
}
