package com.app.server.database;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ReloadCacheDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }

    public int insertReloadCache(String type, int cache_id, int status) {
        return this.masterDB.insert(
                "INSERT INTO reload_cache_app(" +
                        "type," +
                        "cache_id," +
                        "status)" +
                        " VALUES(" +
                        "'" + type + "'," +
                        "'" + cache_id + "'," +
                        "'" + status + "'" +
                        ")"
        );
    }

    public boolean updateReloadCache(int id, String type, int cache_id, int status) {
        return this.masterDB.update(
                "UPDATE reload_cache_app SET" +
                        " type='" + type + "'," +
                        " cache_id=" + cache_id + "," +
                        "status=" + status + "" +
                        " WHERE id=" + id
        );
    }

    public List<JSONObject> getListCacheFailed(int limit) {
        return this.masterDB.find(
                "SELECT * FROM reload_cache_app WHERE status = 0 LIMIT " + limit
        );
    }
}