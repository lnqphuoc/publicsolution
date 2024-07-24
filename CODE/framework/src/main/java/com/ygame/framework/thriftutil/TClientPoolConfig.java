/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.thriftutil;

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 *
 * @author huynxt
 */
public class TClientPoolConfig {

	public static final GenericObjectPool.Config DefaultPoolConfig = new GenericObjectPool.Config();

	static {
		DefaultPoolConfig.maxIdle = 100;
		DefaultPoolConfig.minIdle = GenericObjectPool.DEFAULT_MIN_IDLE;
		DefaultPoolConfig.maxActive = -1; //unlimit
		DefaultPoolConfig.maxWait = -1; //unlimit
		DefaultPoolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
		DefaultPoolConfig.testOnBorrow = false;
		DefaultPoolConfig.testOnReturn = false;
		DefaultPoolConfig.testWhileIdle = true;
		DefaultPoolConfig.timeBetweenEvictionRunsMillis = 10000; //10 secs
		DefaultPoolConfig.numTestsPerEvictionRun = -1; //take size of pools
		DefaultPoolConfig.minEvictableIdleTimeMillis = 3600000; //1hour
		DefaultPoolConfig.softMinEvictableIdleTimeMillis = -1; //unlimit
		DefaultPoolConfig.lifo = false; //queue
	}

	public static class ConnConfig {

		public String addr;
		public int port;
		public boolean framed = true;
		public boolean useFastFrmTrans = true;
		public int timeout = 0;

		public ConnConfig() {
		}

		public ConnConfig(String addr_, int port_) {
			this.addr = addr_;
			this.port = port_;
			assertValid();
		}

		public ConnConfig(String addr_, int port_, boolean framed_, boolean useFastFrmTrans_, int timeout_) {
			this.addr = addr_;
			this.port = port_;
			this.framed = framed_;
			this.useFastFrmTrans = useFastFrmTrans_;
			this.timeout = timeout_;
			assertValid();
		}

		void assertValid() {
			assert (addr != null && !addr.isEmpty() && port > 0 && timeout >= 0);
		}

		@Override
		public String toString() {
			return "ConnConfig{" + "addr=" + addr + ", port=" + port + ", framed=" + framed + ", useFastFrmTrans=" + useFastFrmTrans + ", timeout=" + timeout + '}';
		}
	}

	public static GenericObjectPool.Config ClonePoolConfig(GenericObjectPool.Config poolCfg) {
		GenericObjectPool.Config ret = new GenericObjectPool.Config();
		ret.maxIdle = poolCfg.maxIdle;
		ret.minIdle = poolCfg.minIdle;
		ret.maxActive = poolCfg.maxActive;
		ret.maxWait = poolCfg.maxWait;
		ret.whenExhaustedAction = poolCfg.whenExhaustedAction;
		ret.testOnBorrow = poolCfg.testOnBorrow;
		ret.testOnReturn = poolCfg.testOnReturn;
		ret.testWhileIdle = poolCfg.testWhileIdle;
		ret.timeBetweenEvictionRunsMillis = poolCfg.timeBetweenEvictionRunsMillis;
		ret.numTestsPerEvictionRun = poolCfg.numTestsPerEvictionRun;
		ret.minEvictableIdleTimeMillis = poolCfg.minEvictableIdleTimeMillis;
		ret.softMinEvictableIdleTimeMillis = poolCfg.softMinEvictableIdleTimeMillis;
		ret.lifo = poolCfg.lifo;
		return ret;
	}

	public static ConnConfig CloneConnConfig(ConnConfig connCfg) {
		ConnConfig ret = new ConnConfig();
		ret.addr = connCfg.addr;
		ret.port = connCfg.port;
		ret.framed = connCfg.framed;
		ret.useFastFrmTrans = connCfg.useFastFrmTrans;
		ret.timeout = connCfg.timeout;
		return ret;
	}
}
