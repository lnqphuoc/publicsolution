/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.thriftutil;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 *
 * @author huynxt
 */
public class TNullTransport extends TTransport {

	@Override
	public boolean isOpen() {
		//throw new UnsupportedOperationException("Not supported yet.");
		return true;
	}

	@Override
	public void open() throws TTransportException {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void close() {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int read(byte[] buf, int off, int len) throws TTransportException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void write(byte[] buf, int off, int len) throws TTransportException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
