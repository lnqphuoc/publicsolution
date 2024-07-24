/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.metrics;

import com.codahale.metrics.Meter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author huynxt
 */
public class CachedMetrics extends BasicMetrics {

    static final Lock _createLock = new ReentrantLock();
    static Map<String, CachedMetrics> _instances = new HashMap();
    final Meter _hitL1;
    final Meter _missL1;
    final Meter _missL2;

    public static CachedMetrics getInstance(String instanceName) {
        CachedMetrics instance = (CachedMetrics) _instances.get(instanceName);
        if (instance == null) {
            try {
                _createLock.lock();
                instance = (CachedMetrics) _instances.get(instanceName);
                if (instance == null) {
                    instance = new CachedMetrics(instanceName);
                    _instances.put(instanceName, instance);
                }
            } finally {
                _createLock.unlock();
            }
        }
        return instance;
    }

    protected CachedMetrics(String instanceName) {
        super(instanceName);
        
        _hitL1 = metrics.meter(getName("HitL1"));
        _missL1 = metrics.meter(getName("MissL1"));
        _missL2 = metrics.meter(getName("MissL2"));
    }
    
    public void release() {
        metrics.remove(getName("HitL1"));
        metrics.remove(getName("MissL1"));
        metrics.remove(getName("MissL2"));
    }
    
    public void incHitL1() {
        if(!haveConfig) {
            return;
        }
        _hitL1.mark();
    }

    public void incMissL1() {
        if(!haveConfig) {
            return;
        }
        _missL1.mark();
    }

    public void incMissL2() {
        if(!haveConfig) {
            return;
        }
        _missL2.mark();
    }
}
