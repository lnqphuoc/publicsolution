package com.ygame.framework.dbconn;

import java.sql.Connection;
import java.sql.DriverManager;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.log4j.Logger;

public class ClientObjectFactory implements PoolableObjectFactory {
    
    private static final Logger log = Logger.getLogger(ClientObjectFactory.class);
    private String driver;
    private String url;
    private String user;
    private String password;

    public ClientObjectFactory(String driver, String url,
            String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }
    
    @Override
    public void activateObject(Object arg0) throws Exception {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void destroyObject(Object obj) throws Exception {
        
        if (obj == null) {
            return;
        }
        Connection client = (Connection) obj;
        client.close();
    }
    
    @Override
    public Object makeObject() throws Exception {
        
        Class.forName(driver);
        Connection client = DriverManager.getConnection(url + "&user=" + user + "&password=" + password);
        return client;
    }
    
    @Override
    public void passivateObject(Object arg0) throws Exception {
        // TODO Auto-generated method stub
    }
    
    @Override
    public boolean validateObject(Object obj) {
        boolean result = true;
        try {
            if (obj == null) {
                return false;
            }
            
            Connection client = (Connection) obj;            
            result = result && client.isValid(1);
            result = result && (!client.isClosed());
            
        } catch (Exception ex) {
            result = false;
        }
        return result;
    }
}
