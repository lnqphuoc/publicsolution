
package com.ygame.framework.dbconn;

import com.ygame.framework.common.Config;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;



public class ClientManager implements ManagerIF {

	private ClientPoolByHost commentClientPoolByHost;
	private static final Lock createLock_ = new ReentrantLock();
	private static Map<String,ClientManager> INSTANCES = new HashMap<String,ClientManager>();
	private static Logger logger_ = Logger.getLogger(ClientManager.class);

	/**
	 * Get a reference to a reusable pool.
	 *
	 * @return
	 */

	public static ManagerIF getInstance(String instanceName) {		
		if (!INSTANCES.containsKey(instanceName)) {
			ClientManager.createLock_.lock();
			try {
				if (!INSTANCES.containsKey(instanceName)) {
					INSTANCES.put(instanceName, new ClientManager(instanceName));
				}
			} finally {
				createLock_.unlock();
			}
		}
		return INSTANCES.get(instanceName);
	}

	public ClientManager(String instanceName) {
            String driver = Config.getParam(instanceName,"driver");
            String url = Config.getParam(instanceName,"url");
            String user = Config.getParam(instanceName,"user");
            String password = Config.getParam(instanceName,"password");
            commentClientPoolByHost = new ClientPoolByHost(driver, url, user, password);
	}

	public Connection borrowClient() {
		Connection client = commentClientPoolByHost.borrowClient();
		
//		try {
//			if(!client.isValid(2)) {
//				commentClientPoolByHost.invalidClient(client);
//				client = commentClientPoolByHost.borrowClient();
//			}
//		}
//		catch(Exception ex) {
//			commentClientPoolByHost.invalidClient(client);
//			client = commentClientPoolByHost.borrowClient();
//		}
		return client;
	}

	public void returnClient(Connection client) {
		commentClientPoolByHost.returnObject(client);
	}

	public void invalidClient(Connection client) {
		commentClientPoolByHost.invalidClient(client);
	}

	

}
