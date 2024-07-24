/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.gearman;

/**
 *
 * @author huynxt
 */
public class JobEnt {
    
    public Long JobId;
    public Long UserId;
    public Long ItemId;
    public String ItemKey;
    public short ActionType;
    public Long Timestamp;
    public String DataLog;
    public String Data;
    public String Note;
    public String ClientIP;
    public String ConfigParam;
    
    public JobEnt(){
        this.JobId = 0L;
        this.UserId = 0L;
        this.ItemId = 0L;
        this.ItemKey = "";
        this.ActionType = (short)0;
        this.Timestamp = System.currentTimeMillis();
        this.DataLog = "";
        this.Data = "";
        this.Note= "";
        this.ClientIP = "";
        this.ConfigParam = "";
    }
    
    public JobEnt(Long userId, Long itemId, String itemKey, short actionType,
            Long timestamp, String dataLog, String data, String note, String clientIP){        
        
        this.JobId = 0L; // auto-generate ID when insert to DB
        this.UserId = userId;
        this.ItemId = itemId;
        this.ItemKey = itemKey;
        this.ActionType = actionType;
        this.Timestamp = timestamp;
        this.DataLog = dataLog;
        this.Data = data;
        this.Note= note;
        this.ClientIP = clientIP;
        
    }
    
    public JobEnt(Long userId, Long itemId, String itemKey, short actionType,
            Long timestamp, String dataLog, String data, String note, String clientIP, String ConfigParam){        
        
        this.JobId = 0L; // auto-generate ID when insert to DB
        this.UserId = userId;
        this.ItemId = itemId;
        this.ItemKey = itemKey;
        this.ActionType = actionType;
        this.Timestamp = timestamp;
        this.DataLog = dataLog;
        this.Data = data;
        this.Note= note;
        this.ClientIP = clientIP;
        this.ConfigParam = ConfigParam;
        
    }
    
}
