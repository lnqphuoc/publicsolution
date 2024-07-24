
package com.ygame.framework.dbconn;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import org.apache.log4j.Logger;

public class PureClientManager implements ManagerIF {
	
	private static final Lock createLock_ = new ReentrantLock();
	private static Map<String,PureClientManager> INSTANCES = new HashMap<String,PureClientManager>();
	private static Logger logger_ = Logger.getLogger(PureClientManager.class);

	private String driver;
	private String url;
	private String user;
	private String password;

	/**
	 * Get a reference to a reusable pool.
	 *
	 * @return
	 */

	public static ManagerIF getInstance(String driver, String url,
								String user, String password) {
		String key = driver + ":" + url + ":" + user + ":" + password;
		if (!INSTANCES.containsKey(key)) {
			createLock_.lock();
			try {
				if (!INSTANCES.containsKey(key)) {
					INSTANCES.put(key, new PureClientManager(driver,url, user, password));
				}
			} finally {
				createLock_.unlock();
			}
		}
		return INSTANCES.get(key);
	}

	public PureClientManager(String driver, String url,
								String user, String password) {
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public Connection borrowClient() {		
		try {
			Class.forName(driver);
			Connection client = DriverManager.getConnection(url + "user=" + user + "&password=" + password);
			return client;
		}
		catch(Exception ex) {
			return null;
		}
	}

	public void returnClient(Connection client) {
		try {
			client.close();
		}
		catch(Exception ex) {
			
		}		
	}

	public void invalidClient(Connection client) {
		try {
			client.close();
		}
		catch(Exception ex) {

		}
	}

}
