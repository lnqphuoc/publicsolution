/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.thriftutil;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;

/**
 *
 * @author huynxt
 */
public class TSerializerBase {

    private static final Logger _logger = Logger.getLogger(TSerializerBase.class);

    public static <T extends TBase> byte[] serialize(T value) {
        if(value == null) {
            return null;
        }
        try {
            TSerializer serializer = new TSerializer(new TCompactProtocol.Factory());

            byte[] valDtl = serializer.serialize(value);
            return valDtl;
        } catch (Exception ex) {
            _logger.error("Can't serialize object: " + ex.getMessage());
        }
        return null;
    }

    public static <T extends TBase> T deserialize(byte[] data, Class<T> type) {
        if(data == null) {
            return null;
        }
        try {
            TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
            T item = type.newInstance();
            deserializer.deserialize(item, data);
        return item;
        } catch (Exception ex) {
            _logger.error("Can't deserialize object: " + ex.getMessage());
        }
        return null;
    }
    
//    public static byte[] Serialize(TBase value) {
//        try {
//            TMemoryBuffer memBuf = new TMemoryBuffer(64);
//            Serialize(memBuf, value);
//            if (memBuf == null) {
//                return null;
//            }
//            return memBuf.getArray();
//        } catch (Exception ex) {
//            _logger.error("Can't serialize object: " + ex.getMessage());
//        }
//        return null;
//    }
//
//    public static short Serialize(TMemoryBuffer memBuf, TBase value) {
//        try {
//            if (memBuf == null) {
//                memBuf = new TMemoryBuffer(64);
//
//            }
//            TCompactProtocol binPro = new TCompactProtocol(memBuf);
//            value.write(binPro);
//            memBuf.flush();
//        } catch (Exception ex) {
//            _logger.error("Can't serialize object: " + ex.getMessage());
//            return -1;
//        }
//        return 0;
//    }
//
//    public static short Deserialize(TBase value, byte[] dataSerialize) {
//        try {
//            TMemoryBuffer memBuf = new TMemoryBuffer(dataSerialize.length);
//            memBuf.write(dataSerialize, 0, dataSerialize.length);
//            return Deserialize(value, memBuf);
//        } catch (Exception ex) {
//            _logger.error("Can't deserialize object: " + ex.getMessage());
//            return -1;
//        }
//        //return 0;
//    }
//
//    public static short Deserialize(TBase value, TMemoryBuffer memBuf) {
//        try {
//            if (memBuf == null) {
//                memBuf = new TMemoryBuffer(64);
//            }
//            TCompactProtocol binPro = new TCompactProtocol(memBuf);
//            value.read(binPro);
//
//        } catch (Exception ex) {
//            _logger.error("Can't deserialize object: " + ex.getMessage());
//            return -1;
//        }
//        return 0;
//    }
}
