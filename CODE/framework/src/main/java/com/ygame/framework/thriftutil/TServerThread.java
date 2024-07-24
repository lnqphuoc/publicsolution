/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.thriftutil;

import com.ygame.framework.common.LogUtil;
import java.net.InetSocketAddress;
import org.apache.log4j.Logger;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

/**
 *
 * @author huynxt
 */
public class TServerThread extends Thread {

    private static final Logger logger = LogUtil.getLogger(TServerThread.class);
    private String mode;
    private String host;
    private Integer port;
    private Integer minThread;
    private Integer maxThread;
    private TProcessor processor;
    private TServer server = null;

    public TServerThread(String mode, String host, Integer port, Integer minThread, Integer maxThread, TProcessor processor) {
        this.mode = mode;
        this.host = host;
        this.port = port;
        this.minThread = minThread;
        this.maxThread = maxThread;
        this.processor = processor;
    }
    
    @Override
    public void run() {
        if (mode.equals("threadpool")) {
            runThreadPool();
        } else {
            runNonblocking();
        }
    }
    
    public void runThreadPool() {
        try {
            if (host == null || host.isEmpty()) {
                host = "0.0.0.0";
            }
            InetSocketAddress inetSock = new InetSocketAddress(host, port);
            TServerTransport serverTransport = new TServerSocket(inetSock);
            
            TThreadPoolServer.Args args2 = new TThreadPoolServer.Args(serverTransport).processor(processor);
            args2.transportFactory(new TFramedTransport.Factory());
            args2.protocolFactory(new TBinaryProtocol.Factory());
            args2.minWorkerThreads(minThread);
            args2.maxWorkerThreads(maxThread);
            TServer threadPoolServer = new TThreadPoolServer(args2);
            
            threadPoolServer.serve();            
        } catch (Exception ex) {
            logger.error("Can't start server");
            logger.error(LogUtil.stackTrace(ex));
        }
    }

    
    public void runNonblocking() {
        try {
            if (host == null || host.isEmpty()) {
                host = "0.0.0.0";
            }
            InetSocketAddress inetSock = new InetSocketAddress(host, port);
            TNonblockingServerSocket transport = new TNonblockingServerSocket(inetSock);

            THsHaServer.Args options = new THsHaServer.Args(transport);
            options.minWorkerThreads(minThread);
            options.maxWorkerThreads(maxThread);
            
            options.processor((TProcessor) this.processor);

            this.server = new THsHaServer(options);

            this.server.serve();
        } catch (Exception ex) {
            logger.error("Can't start server");
            logger.error(LogUtil.stackTrace(ex));
        }
    }
}
