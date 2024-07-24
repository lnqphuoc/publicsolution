/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ygame.framework.dbconn;

import java.sql.Connection;

public interface ManagerIF {

	public Connection borrowClient();
	public void returnClient(Connection conn);
	public void invalidClient(Connection client);
}
