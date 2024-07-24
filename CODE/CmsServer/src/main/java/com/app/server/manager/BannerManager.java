package com.app.server.manager;

import com.app.server.data.entity.BannerEntity;
import com.app.server.database.BannerDB;
import com.app.server.database.MasterDB;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Getter
public class BannerManager {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }

    private Map<Integer, BannerEntity> mpBannerPriority = new HashMap<>();
    private List<BannerEntity> bannerEntityList = new ArrayList<>();

    public void loadData() {
        loadBannerPriority();
    }

    private void loadBannerPriority() {
        try {
            mpBannerPriority.clear();
            bannerEntityList.clear();
            List<JSONObject> jsonObjectList = this.
                    masterDB.find(
                            "SELECT * FROM banner WHERE priority != 0" +
                                    " ORDER BY priority ASC"
                    );
            for (JSONObject jsonObject : jsonObjectList) {
                bannerEntityList.add(JsonUtils.DeSerialize(JsonUtils.Serialize(
                        jsonObject
                ), BannerEntity.class));
            }

            for (int i = 0; i < bannerEntityList.size(); i++) {
                BannerEntity bannerEntity = bannerEntityList.get(i);
                bannerEntity.setPriority(i + 1);
                mpBannerPriority.put(bannerEntity.getId(), bannerEntity);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    public int getPriority(int id) {
        BannerEntity bannerEntity = this.getMpBannerPriority().get(id);
        if (bannerEntity != null) {
            return bannerEntity.getPriority();
        }

        return 0;
    }

    public int getLastPriority() {
        if (this.bannerEntityList.size() > 0) {
            return this.bannerEntityList.get(
                    bannerEntityList.size() - 1
            ).getPriority();
        }

        return 0;
    }
}