/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.metrics;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Metrics about latencies
 */
public class LatencyMetrics extends BasicMetrics {

    /**
     * Latency
     */
    public final Timer latency;
    /**
     * Total latency in micro sec
     */
    public final Counter totalLatency;
    /**
     * Error count
     */
    protected final Meter error;
    
    static final Lock _createLock = new ReentrantLock();
    static Map<String, LatencyMetrics> _instances = new HashMap();

    public static LatencyMetrics getInstance(String instanceName) {
        LatencyMetrics instance = (LatencyMetrics) _instances.get(instanceName);
        if (instance == null) {
            try {
                _createLock.lock();
                instance = (LatencyMetrics) _instances.get(instanceName);
                if (instance == null) {
                    instance = new LatencyMetrics(instanceName);
                    _instances.put(instanceName, instance);
                }
            } finally {
                _createLock.unlock();
            }
        }
        return instance;
    }
    
    protected LatencyMetrics(String instanceName) {
        super(instanceName);

        latency = metrics.timer(getName("Latency"));
        totalLatency = metrics.counter(getName("TotalLatency"));
        error = metrics.meter(getName("Error"));
    }
    
    public void release() {
        metrics.remove(getName("Latency"));
        metrics.remove(getName("TotalLatency"));
    }

    /**
     * takes nanoseconds *
     */
    public void addNano(long nanos) {
        if(!haveConfig) {
            return;
        }
        // convert to microseconds. 1 millionth
        addMicro(nanos / 1000);
    }

    void addMicro(long micros) {
        if(!haveConfig) {
            return;
        }
        latency.update(micros, TimeUnit.MICROSECONDS);
        totalLatency.inc(micros);
    }
    
    public void incError() {
        if(!haveConfig) {
            return;
        }
        error.mark();
    }    
}
