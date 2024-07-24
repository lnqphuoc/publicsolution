/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.thriftutil;

import java.nio.ByteBuffer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.transport.TTransport;

/**
 *
 * @author huynxt
 */
public class TNullProtocol extends TProtocol {

	public TNullProtocol(TTransport trans) {
		super(trans);
	}

	@Override
	public void writeMessageBegin(TMessage message) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeMessageEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeStructBegin(TStruct struct) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeStructEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeFieldBegin(TField field) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeFieldEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeFieldStop() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeMapBegin(TMap map) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeMapEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeListBegin(TList list) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeListEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeSetBegin(TSet set) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeSetEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeBool(boolean b) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeByte(byte b) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeI16(short i16) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeI32(int i32) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeI64(long i64) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeDouble(double dub) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeString(String str) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void writeBinary(ByteBuffer buf) throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public TMessage readMessageBegin() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void readMessageEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public TStruct readStructBegin() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void readStructEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public TField readFieldBegin() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void readFieldEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public TMap readMapBegin() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void readMapEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public TList readListBegin() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void readListEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public TSet readSetBegin() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void readSetEnd() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean readBool() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public byte readByte() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public short readI16() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int readI32() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public long readI64() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public double readDouble() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String readString() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ByteBuffer readBinary() throws TException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
