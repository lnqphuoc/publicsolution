/*
 * Copyright (C) 2009 by Eric Lambert <Eric.Lambert@sun.com>
 * Use and distribution licensed under the BSD license.  See
 * the COPYING file in the parent directory for full text.
 */
package com.ygame.framework.jobqueue.gearman;

import com.ygame.framework.gearman.JobEnt;
import com.ygame.framework.jobqueue.AbstractJobQueueFunction;
import com.ygame.framework.jobqueue.JobQueueResult;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.JSONUtil;
import java.util.HashSet;
import java.util.Set;

import org.gearman.client.GearmanIOEventListener;
import org.gearman.client.GearmanJobResult;
import org.gearman.client.GearmanJobResultImpl;
import org.gearman.common.GearmanPacket;
import org.gearman.common.GearmanPacketImpl;
import org.gearman.common.GearmanPacketMagic;
import org.gearman.common.GearmanPacketType;
import org.gearman.util.ByteUtils;
import org.gearman.worker.GearmanFunction;

public abstract class AbstractGearmanFunction extends AbstractJobQueueFunction implements GearmanFunction {
    byte[] uniqueId = null;
    
    protected final String name;
    protected Object data;
    protected byte[] jobHandle;
    protected Set<GearmanIOEventListener> listeners;

    public AbstractGearmanFunction() {
        this(null);
    }

    public AbstractGearmanFunction(String name) {
        listeners = new HashSet<>();
        jobHandle = new byte[0];
        if (name == null) {
            this.name = this.getClass().getCanonicalName();
        } else {
            this.name = name;
        }
    }

    @Override
    public String getName() {                                                   
        return name;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public void setJobHandle(byte[] handle) throws IllegalArgumentException {
        if (handle == null) {
            throw new IllegalArgumentException("handle can not be null");
        }
        if (handle.length == 0) {
            throw new IllegalArgumentException("handle can not be empty");
        }
        jobHandle = new byte[handle.length];
        System.arraycopy(handle, 0, jobHandle, 0, handle.length);
    }

    @Override
    public byte[] getJobHandle() {                                              
        byte[] rt = new byte[jobHandle.length];
        System.arraycopy(jobHandle, 0, rt, 0, jobHandle.length);
        return rt;
    }

    @Override
    public void registerEventListener(GearmanIOEventListener listener)
            throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("listener can not be null");
        }
        listeners.add(listener);
    }

    @Override
    public void fireEvent(GearmanPacket event)
            throws IllegalArgumentException {
        if (event == null) {
            throw new IllegalArgumentException("event can not be null");
        }
        for (GearmanIOEventListener listener : listeners) {
            listener.handleGearmanIOEvent(event);
        }
    }

    public void sendData(byte[] data) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_DATA,
                GearmanPacketImpl.generatePacketData(jobHandle, data)));

    }

    public void sendWarning(byte[] warning) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_WARNING,
                GearmanPacketImpl.generatePacketData(jobHandle, warning)));
    }

    public void sendException(byte[] exception) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_EXCEPTION,
                GearmanPacketImpl.generatePacketData(jobHandle, exception)));
    }

    public void sendStatus(int denominator, int numerator) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_STATUS,
                GearmanPacketImpl.generatePacketData(jobHandle,
                ByteUtils.toUTF8Bytes(String.valueOf(numerator)),
                ByteUtils.toUTF8Bytes(String.valueOf(denominator)))));
    }

    public abstract JobQueueResult executeFunction(JobEnt job);
    
    GearmanJobResult toGearmanJobResult(JobQueueResult item) {
        if(item == null) {
            return null;
        }
        GearmanJobResult result = new GearmanJobResultImpl(item.getJobHandle(), item.jobSucceeded(), item.getResults(), item.getWarnings(), item.getExceptions(), item.getNumerator(), item.getDenominator());
        return result;
    }

    @Override
    public GearmanJobResult call() {
        GearmanPacket event;
        GearmanJobResult result = null;
        Exception thrown = null;
        try {
            String jsondata = ConvertUtils.decodeString((byte[]) this.data);
            JobEnt job = JSONUtil.DeSerialize(jsondata, JobEnt.class);
            result = toGearmanJobResult(executeFunction(job));
        } catch (Exception e) {
            thrown = e;
        }
        if (result == null) {
            String message = thrown == null ? "function returned null result" : thrown.getMessage();
            if (message == null) {
                message = thrown.toString();
            }
            fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                    GearmanPacketType.WORK_EXCEPTION,
                    GearmanPacketImpl.generatePacketData(jobHandle,
                    message.getBytes())));
            result = new GearmanJobResultImpl(jobHandle, false, new byte[0],
                    new byte[0], new byte[0], -1, -1);
        }

        if (result.jobSucceeded()) {
            event = new GearmanPacketImpl(GearmanPacketMagic.REQ,
                    GearmanPacketType.WORK_COMPLETE,
                    GearmanPacketImpl.generatePacketData(jobHandle,
                    result.getResults()));
        } else {
            event = new GearmanPacketImpl(GearmanPacketMagic.REQ,
                    GearmanPacketType.WORK_FAIL, jobHandle);

        }
        fireEvent(event);
        return result;
    }
    
    @Override
    public void setUniqueId(byte[] bytes) {
        this.uniqueId = bytes;
    }

    @Override
    public byte[] getUniqueId() {
        return this.uniqueId;
    }
}
