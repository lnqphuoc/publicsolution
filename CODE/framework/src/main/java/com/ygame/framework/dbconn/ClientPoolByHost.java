
package com.ygame.framework.dbconn;

import java.sql.Connection;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;
import org.apache.log4j.Logger;

public class ClientPoolByHost {
	private static final Logger log = Logger.getLogger(ClientPoolByHost.class);
	public static final int DEFAULT_MAX_ACTIVE = 20;
	public static final long DEFAULT_MAX_WAITTIME_WHEN_EXHAUSTED = -1;
	public static final int DEFAULT_MAX_IDLE = 5;

	private ClientObjectFactory clientFactory;
	private int maxActive;
	private int maxIdle;
	private long maxWaitTimeWhenExhausted;
	private GenericObjectPool pool;

	public ClientPoolByHost(String driver, String url,
								String user, String password)  {
		this.clientFactory = new ClientObjectFactory(driver, url, user, password);
		this.maxActive = ClientPoolByHost.DEFAULT_MAX_ACTIVE;
		this.maxIdle = ClientPoolByHost.DEFAULT_MAX_ACTIVE;
		this.maxWaitTimeWhenExhausted = ClientPoolByHost.DEFAULT_MAX_WAITTIME_WHEN_EXHAUSTED;
		this.pool = this.createPool();
		//pool.setMaxIdle(m);
		pool.setMinEvictableIdleTimeMillis(50000);
		pool.setTimeBetweenEvictionRunsMillis(55000);

	}

	public void close() {
		try {
			pool.close();
		} catch (Exception e) {
			log.error("Unable to close pool", e);
		}
	}

	private GenericObjectPool createPool() {
		GenericObjectPoolFactory poolFactory = new GenericObjectPoolFactory(
				clientFactory, maxActive,
				GenericObjectPool.WHEN_EXHAUSTED_BLOCK,
				maxWaitTimeWhenExhausted, maxIdle);
		GenericObjectPool p = (GenericObjectPool) poolFactory.createPool();		
		p.setTestOnBorrow(true);
		p.setTestWhileIdle(true);
		p.setMaxIdle(-1);
		return p;
	}

	public Connection borrowClient() {
		Connection client = null;
		try {
			client = (Connection) pool.borrowObject();
		} catch (Exception e) {
			log.error("Uncaught exception", e);
		}
		return client;
	}
	public void returnObject(Connection client){
		try {
			pool.returnObject(client);
		} catch (Exception e) {
			log.error("Uncaught exception", e);
		}
	}
	public void invalidClient(Connection client){
		try {
			pool.invalidateObject(client);
		} catch (Exception e) {
			log.error("Uncaught exception", e);
		}
	}


}
