package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.data.dto.staff.Staff;
import com.app.server.data.entity.*;
import com.app.server.database.repository.*;
import com.app.server.database.sql.PromoSQL;
import com.app.server.enums.*;
import com.app.server.service.PromoService;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import com.ygame.framework.utils.ConvertUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PromoDB extends BaseDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }

    private PromoSQL promoSQL;

    @Autowired
    public void setPromoSQL(PromoSQL promoSQL) {
        this.promoSQL = promoSQL;
    }

    private PromoRepository promoRepository;

    @Autowired
    public void setPromoRepository(PromoRepository promoRepository) {
        this.promoRepository = promoRepository;
    }

    private PromoItemGroupRepository promoItemGroupRepository;

    @Autowired
    public void setPromoItemGroupRepository(PromoItemGroupRepository promoItemGroupRepository) {
        this.promoItemGroupRepository = promoItemGroupRepository;
    }

    private PromoItemGroupDetailRepository promoItemGroupDetailRepository;

    @Autowired
    public void setPromoItemGroupDetailRepository(PromoItemGroupDetailRepository promoItemGroupDetailRepository) {
        this.promoItemGroupDetailRepository = promoItemGroupDetailRepository;
    }

    private PromoLimitRepository promoLimitRepository;

    @Autowired
    public void setPromoLimitRepository(PromoLimitRepository promoLimitRepository) {
        this.promoLimitRepository = promoLimitRepository;
    }

    private PromoLimitGroupRepository promoLimitGroupRepository;

    @Autowired
    public void setPromoLimitGroupRepository(PromoLimitGroupRepository promoLimitGroupRepository) {
        this.promoLimitGroupRepository = promoLimitGroupRepository;
    }

    private PromoOfferRepository promoOfferRepository;

    @Autowired
    public void setPromoOfferRepository(PromoOfferRepository promoOfferRepository) {
        this.promoOfferRepository = promoOfferRepository;
    }

    private PromoOfferProductRepository promoOfferProductRepository;

    @Autowired
    public void setPromoOfferProductRepository(PromoOfferProductRepository promoOfferProductRepository) {
        this.promoOfferProductRepository = promoOfferProductRepository;
    }

    private PromoOfferBonusRepository promoOfferBonusRepository;

    @Autowired
    public void setPromoOfferBonusRepository(PromoOfferBonusRepository promoOfferBonusRepository) {
        this.promoOfferBonusRepository = promoOfferBonusRepository;
    }

    private PromoHistoryRepository promoHistoryRepository;

    @Autowired
    public void setPromoHistoryRepository(PromoHistoryRepository promoHistoryRepository) {
        this.promoHistoryRepository = promoHistoryRepository;
    }

    private PromoRunningRepository promoRunningRepository;

    @Autowired
    public void setPromoRunningRepository(PromoRunningRepository promoRunningRepository) {
        this.promoRunningRepository = promoRunningRepository;
    }

    private PromoScheduleRepository promoScheduleRepository;

    @Autowired
    public void setPromoScheduleRepository(PromoScheduleRepository promoScheduleRepository) {
        this.promoScheduleRepository = promoScheduleRepository;
    }

    private PromoItemIgnoreRepository promoItemIgnoreRepository;

    @Autowired
    public void setPromoItemIgnoreRepository(PromoItemIgnoreRepository promoItemIgnoreRepository) {
        this.promoItemIgnoreRepository = promoItemIgnoreRepository;
    }

    private PromoAgencyIncludeRepository promoAgencyIncludeRepository;

    @Autowired
    public void setPromoAgencyIncludeRepository(PromoAgencyIncludeRepository promoAgencyIncludeRepository) {
        this.promoAgencyIncludeRepository = promoAgencyIncludeRepository;
    }

    private PromoAgencyIgnoreRepository promoAgencyIgnoreRepository;

    @Autowired
    public void setPromoAgencyIgnoreRepository(PromoAgencyIgnoreRepository promoAgencyIgnoreRepository) {
        this.promoAgencyIgnoreRepository = promoAgencyIgnoreRepository;
    }

    private PromoApplyObjectRepository promoApplyObjectRepository;

    @Autowired
    public void setPromoApplyObjectRepository(PromoApplyObjectRepository promoApplyObjectRepository) {
        this.promoApplyObjectRepository = promoApplyObjectRepository;
    }

    private PromoFilterRepository promoFilterRepository;

    @Autowired
    public void setPromoFilterRepository(PromoFilterRepository promoFilterRepository) {
        this.promoFilterRepository = promoFilterRepository;
    }

    public List<JSONObject> filerPromo(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotalPromo(String query) {
        return this.masterDB.getTotal(query);
    }

    public int getTotalPromoByPromoType(String key) {
        String sql = this.promoSQL.getTotalPromoByPromoType(key);
        return this.masterDB.getTotal(sql);
    }

    public PromoEntity createPromo(PromoEntity promoEntity) {
        return promoRepository.save(promoEntity);
    }

    public PromoItemGroupEntity createPromoItemGroup(PromoItemGroupEntity promoItemGroupEntity) {
        return this.promoItemGroupRepository.save(promoItemGroupEntity);
    }

    public PromoItemGroupDetailEntity createPromoItemGroupDetail(PromoItemGroupDetailEntity promoItemGroupDetailEntity) {
        return this.promoItemGroupDetailRepository.save(promoItemGroupDetailEntity);
    }

    public PromoLimitEntity createPromoLimit(PromoLimitEntity promoLimitEntity) {
        return this.promoLimitRepository.save(promoLimitEntity);
    }

    public PromoLimitGroupEntity createPromoLimitGroup(PromoLimitGroupEntity promoLimitGroupEntity) {
        return this.promoLimitGroupRepository.save(promoLimitGroupEntity);
    }

    public PromoOfferEntity createPromoOffer(PromoOfferEntity promoOfferEntity) {
        return this.promoOfferRepository.save(promoOfferEntity);
    }

    public PromoOfferProductEntity createPromoOfferProduct(PromoOfferProductEntity promoOfferProductEntity) {
        return this.promoOfferProductRepository.save(promoOfferProductEntity);
    }

    public PromoOfferBonusEntity createPromoOfferBonus(PromoOfferBonusEntity promoOfferBonusEntity) {
        return this.promoOfferBonusRepository.save(promoOfferBonusEntity);
    }

    public PromoHistoryEntity savePromoHistory(PromoHistoryEntity promoHistoryEntity) {
        return this.promoHistoryRepository.save(promoHistoryEntity);
    }

    public PromoEntity approvePromo(PromoEntity promoEntity) {
        return this.promoRepository.save(promoEntity);
    }

    public PromoHistoryEntity getLastPromoHistory(int promo_id) {
        String sql = this.promoSQL.getLastPromoHistory(promo_id);
        JSONObject rs = this.masterDB.getOne(sql);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), PromoHistoryEntity.class);
        }
        return null;
    }

    public PromoRunningEntity getPromoRunningById(int promo_id) {
        String sql = this.promoSQL.getPromoRunningById(promo_id);
        JSONObject rs = this.masterDB.getOne(sql);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), PromoRunningEntity.class);
        }
        return null;
    }

    public PromoRunningEntity getDamMeRunningById(int promo_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM dam_me_running WHERE promo_id=" + promo_id + ""
        );
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), PromoRunningEntity.class);
        }
        return null;
    }

    public PromoRunningEntity savePromoRunning(PromoRunningEntity promoRunningEntity) {
        return this.promoRunningRepository.save(promoRunningEntity);
    }

    public PromoEntity getPromo(int id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM promo WHERE id = " + id
        );

        if (rs != null) {
            return PromoEntity.from(rs);
        }
        return null;
    }

    public boolean stopPromo(int promo_id,
                             Date end_date,
                             int status) {
        return this.masterDB.update(
                "UPDATE promo SET end_date = " + parseDateToSql(end_date) + "," +
                        " status = " + status +
                        " WHERE id = " + promo_id
        );
    }

    public boolean removePromoRunning(int promo_id) {
        String sql = this.promoSQL.removePromoRunning(promo_id);
        return this.masterDB.update(sql);
    }

    public List<JSONObject> getPromoScheduleWaiting(int scheduleRunningLimit) {
        String sql = this.promoSQL.getPromoScheduleWaiting(scheduleRunningLimit);
        return this.masterDB.find(sql);
    }

    public JSONObject getPromoScheduleWaitingStartByPromo(int promo_id) {
        String sql = this.promoSQL.getPromoScheduleWaitingStartByPromo(promo_id);
        return this.masterDB.getOne(sql);
    }

    public JSONObject getPromoScheduleWaitingStopByPromo(int promo_id) {
        String sql = this.promoSQL.getPromoScheduleWaitingStopByPromo(promo_id);
        return this.masterDB.getOne(sql);
    }

    public PromoEntity getPromoInfo(int promo_id) {
        String sql = this.promoSQL.getPromoInfo(promo_id);
        JSONObject rs = this.masterDB.getOne(sql);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), PromoEntity.class);
        }
        return null;
    }

    public List<JSONObject> getPromoItemGroupList(int promo_id) {
        String sql = this.promoSQL.getPromoItemGroupList(promo_id);
        return this.masterDB.find(sql);
    }

    public List<JSONObject> getPromoItemGroupDetailList(int promo_item_group_id) {
        String sql = this.promoSQL.getPromoItemGroupDetailList(promo_item_group_id);
        return this.masterDB.find(sql);
    }

    public List<JSONObject> getPromoLimitList(int promo_id) {
        String sql = this.promoSQL.getPromoLimitList(promo_id);
        return this.masterDB.find(sql);
    }

    public List<JSONObject> getPromoLimitGroupList(int promo_limit_id) {
        String sql = this.promoSQL.getPromoLimitGroupList(promo_limit_id);
        return this.masterDB.find(sql);
    }

    public JSONObject getPromoOffer(int promo_limit_group_id) {
        String sql = this.promoSQL.getPromoOffer(promo_limit_group_id);
        return this.masterDB.getOne(sql);
    }

    public List<JSONObject> getPromoOfferProductList(int promo_offer_id) {
        String sql = this.promoSQL.getPromoOfferProductList(promo_offer_id);
        return this.masterDB.find(sql);
    }

    public List<JSONObject> getPromoOfferBonusList(int promo_offer_id) {
        String sql = this.promoSQL.getPromoOfferBonusList(promo_offer_id);
        return this.masterDB.find(sql);
    }

    public PromoEntity cancelPromo(PromoEntity promoEntity) {
        return promoRepository.save(promoEntity);
    }

    public boolean deletePromoItemGroupDetail(int promo_id) {
        return this.masterDB.update("DELETE FROM promo_item_group_detail WHERE promo_id=" + promo_id);
    }

    public boolean deletePromoItemGroup(int promo_id) {
        return this.masterDB.update("DELETE FROM promo_item_group WHERE promo_id=" + promo_id);
    }

    public boolean deletePromoOfferBonus(int promo_id) {
        return this.masterDB.update("DELETE FROM promo_offer_bonus WHERE promo_id=" + promo_id);
    }

    public boolean deletePromoOfferProduct(int promo_id) {
        return this.masterDB.update("DELETE FROM promo_offer_product WHERE promo_id=" + promo_id);
    }

    public boolean deletePromoOffer(int promo_id) {
        return this.masterDB.update(
                "DELETE FROM promo_offer WHERE promo_id=" + promo_id
        );
    }

    public boolean deletePromoLimitGroup(int promo_id) {
        return this.masterDB.update(
                "DELETE FROM promo_limit_group WHERE promo_id=" + promo_id);
    }

    public boolean deletePromoLimit(int promo_id) {
        return this.masterDB.update(
                "DELETE FROM promo_limit WHERE promo_id=" + promo_id);
    }

    public boolean cancelPromoSchedule(int id) {
        return this.masterDB.update(
                "UPDATE promo_schedule SET status = " + ProcessStatus.CANCEL.getValue() + " WHERE id=" + id);
    }

    public PromoScheduleEntity createPromoSchedule(PromoScheduleEntity promoScheduleEntity) {
        return this.promoScheduleRepository.save(promoScheduleEntity);
    }

    public boolean updatePromoScheduleStatus(int promo_schedule_id, int status, String note) {
        return this.masterDB.update(
                "UPDATE promo_schedule SET status = " + status +
                        ", note='" + note + "'" + " WHERE id=" + promo_schedule_id);
    }

    public PromoScheduleEntity getPromoScheduleWaitingStopByPromoId(int id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM promo_schedule WHERE promo_id=" + id +
                        " AND schedule_type='" + PromoScheduleType.STOP + "'" +
                        " AND status=" + ProcessStatus.WAITING.getValue() +
                        " LIMIT 1");
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), PromoScheduleEntity.class);
        }
        return null;
    }

    public boolean deletePromo(int promo_id) {
        return this.masterDB.update(
                "UPDATE promo SET status=" + PromoActiveStatus.DELETE.getId() + " WHERE id=" + promo_id);
    }

    public List<JSONObject> getPromoHistory(int promo_id) {
        return this.masterDB.find(
                "SELECT id,created_date,start_date,end_date,note,status,creator_id" +
                        " FROM promo_history" +
                        " WHERE promo_id=" + promo_id +
                        " ORDER BY id DESC"
        );
    }

    public PromoItemIgnoreEntity createPromoItemIgnore(PromoItemIgnoreEntity promoItemIgnoreEntity) {
        return promoItemIgnoreRepository.save(promoItemIgnoreEntity);
    }

    public List<JSONObject> getPromoItemIgnoreList(int promo_id) {
        return this.masterDB.find(
                "SELECT * FROM promo_item_ignore WHERE promo_id=" + promo_id
        );
    }

    public boolean deletePromoItemIgnore(int promo_id) {
        return this.masterDB.update(
                "DELETE FROM promo_item_ignore WHERE promo_id=" + promo_id);
    }

    public boolean updatePromoStatus(int promo_id, int status) {
        return this.masterDB.update(
                "UPDATE promo SET status = " + status + " WHERE id=" + promo_id);
    }

    public PromoAgencyIncludeEntity createPromoAgencyInclude(PromoAgencyIncludeEntity promoAgencyIncludeEntity) {
        return this.promoAgencyIncludeRepository.save(promoAgencyIncludeEntity);
    }

    public PromoAgencyIgnoreEntity createPromoAgencyIgnore(PromoAgencyIgnoreEntity promoAgencyIgnoreEntity) {
        return this.promoAgencyIgnoreRepository.save(promoAgencyIgnoreEntity);
    }

    public PromoApplyObjectEntity createPromoApplyObject(PromoApplyObjectEntity promoApplyObjectEntity) {
        return this.promoApplyObjectRepository.save(promoApplyObjectEntity);
    }


    public PromoFilterEntity createPromoFilter(PromoFilterEntity promoFilterEntity) {
        return this.promoFilterRepository.save(promoFilterEntity);
    }

    public List<JSONObject> getPromoFilterList(int promo_id) {
        return this.masterDB.find(
                "SELECT * FROM promo_filter WHERE promo_id=" + promo_id);
    }

    public PromoApplyObjectEntity getPromoApplyObject(int promo_id) {
        JSONObject jsonObject = this.masterDB.getOne(
                "SELECT * FROM promo_apply_object WHERE promo_id=" + promo_id);
        if (jsonObject != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), PromoApplyObjectEntity.class);
        }
        return null;
    }

    public boolean deletePromoApplyObject(int promo_id) {
        return this.masterDB.update("DELETE FROM promo_apply_object WHERE promo_id=" + promo_id);
    }

    public boolean deletePromoFilter(int promo_id) {
        return this.masterDB.update("DELETE FROM promo_filter WHERE promo_id=" + promo_id);
    }

    public boolean deletePromoStructure(int promo_id) {
        return this.masterDB.update("DELETE FROM promo_structure WHERE promo_id=" + promo_id);
    }

    public JSONObject getPromoHistoryDetail(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM promo_history WHERE id=" + id);
    }

    public boolean updatePromoPriority(int id, int priority, int modifier_id) {
        return this.masterDB.update(
                "UPDATE promo SET priority = " + priority +
                        ", modified_date = NOW()" +
                        ", modifier_id = " + modifier_id +
                        " WHERE id = " + id
        );
    }

    public JSONObject getLastPromoPriority() {
        return this.masterDB.getOne(
                "SELECT * FROM promo WHERE promo_type = '" + PromoType.PROMO.getKey() + "'" +
                        " ORDER BY priority DESC" +
                        " LIMIT 1"
        );
    }

    public List<JSONObject> getPromoCTKMPriorityList() {
        return this.masterDB.find(
                "SELECT * FROM promo WHERE promo_type = '" + PromoType.PROMO.getKey() + "'" +
                        " AND priority != 0" +
                        " AND status IN (" + PromoActiveStatus.canSort() + ")" +
                        " ORDER BY priority ASC"
        );
    }

    public List<JSONObject> filter(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotal(String query) {
        return this.masterDB.getTotal(query);
    }

    public List<JSONObject> getListPromoWaitingByProduct(Integer product_id) {
        return this.masterDB.find(
                "select t.id, t.code, t.name, t.description" +
                        " FROM promo t" +
                        " LEFT JOIN promo_item_group_detail t1 ON t1.promo_id = t.id" +
                        " WHERE" +
                        " t.status IN (" +
                        "" + PromoActiveStatus.WAITING.getId() +
                        "," + PromoActiveStatus.RUNNING.getId() +
                        "," + PromoActiveStatus.DRAFT.getId() +
                        ")" +
                        " AND t1.item_id = " + product_id +
                        " GROUP BY t.id"
        );
    }

    public List<JSONObject> getListPromoLikeProduct(String products) {
        return this.masterDB.find(
                "select t.id, t.code, t.promo_type, t.name, t.description, t.status, t.start_date, t.end_date," +
                        "t2.id as product_id," +
                        "t2.code as product_code," +
                        "t2.full_name as product_full_name" +
                        " FROM promo t" +
                        " LEFT JOIN promo_item_group_detail t1 ON t1.promo_id = t.id" +
                        " LEFT JOIN (SELECT id, code, full_name FROM product) t2 ON t2.id = t1.item_id" +
                        " WHERE" +
                        " t.promo_type IN ('" + PromoType.SALE_POLICY.getKey() + "'," +
                        "'" + PromoType.PROMO.getKey() + "'," +
                        "'" + PromoType.CTSS.getKey() + "')" +
                        " AND t.status IN (" + PromoActiveStatus.WAITING.getId() +
                        "," + PromoActiveStatus.RUNNING.getId() +
                        "," + PromoActiveStatus.DRAFT.getId() +
                        ")" +
                        " AND '" + products + "' LIKE " + "CONCAT('%\"',t1.item_id,'\"%')" +
                        " GROUP BY t.id, t2.id"
        );
    }

    public List<JSONObject> getListPromoDraft() {
        return this.masterDB.find(
                "SELECT * FROM promo WHERE status = " + PromoActiveStatus.DRAFT.getId()
        );
    }

    public boolean updatePromoApplyObject(
            int promo_id,
            String promo_agency_include,
            String promo_agency_ignore) {
        return this.masterDB.update(
                "UPDATE promo_apply_object SET" +
                        " promo_agency_include = '" + promo_agency_include + "'" +
                        ", promo_agency_ignore = '" + promo_agency_ignore + "'" +
                        " WHERE promo_id = " + promo_id
        );
    }

    public String getSearchProductHuntSaleSQL(String promo, String product, String combo) {
        return "SELECT a.promo_id, a.item_id as item_id, a.is_combo\n" +
                " FROM \n" +
                " (SELECT t1.promo_id, t.name, t.code, t1.combo_id as 'item_id', '1' as 'is_combo'\n" +
                " FROM (SELECT * FROM promo WHERE promo_type='CTSS' AND status = 2) as t\n" +
                " JOIN promo_item_group t1 ON t1.promo_id=t.id AND t1.type='COMBO'\n" +
                " GROUP BY t1.promo_id, t1.combo_id\n" +
                " UNION ALL \n" +
                " SELECT t1.promo_id, t.name, t.code, t2.item_id, '0' as 'is_combo'\n" +
                " FROM (SELECT * FROM promo WHERE promo_type='CTSS' AND status = 2) as t\n" +
                " JOIN promo_item_group t1 ON t1.promo_id=t.id\n" +
                " LEFT JOIN promo_item_group_detail t2 ON t2.promo_id=t.id\n" +
                " GROUP BY t2.item_id\n" +
                " ) as a\n" +
                " LEFT JOIN product t3 ON t3.id = a.item_id AND a.is_combo = 0" +
                " LEFT JOIN combo t4 ON t4.id = a.item_id AND a.is_combo = 1" +
                " WHERE " +
                "('" + promo + "'= '' OR a.name LIKE '%" + promo + "%'" +
                " OR a.code LIKE '%" + promo + "%')" +
                " AND ('" + product + "'= '' OR  t3.full_name LIKE '%" + product + "%'" +
                " OR t3.code LIKE '%" + product + "%')" +
                " AND ('" + combo + "'= '' OR  t4.full_name LIKE '%" + combo + "%'" +
                " OR t4.code LIKE '%" + combo + "%')" +
                " ORDER BY a.promo_id DESC \n" +
                " LIMIT 100";
    }

    public List<JSONObject> searchAllProductHuntSale(String promo, String product, String combo) {
        String query = this.getSearchProductHuntSaleSQL(
                promo,
                product,
                combo
        );
        return this.masterDB.find(
                query
        );
    }

    public List<JSONObject> searchProductHuntSale(String promo, String product) {
        return this.masterDB.find(
                "SELECT a.promo_id, a.item_id" +
                        " FROM \n" +
                        " (SELECT t1.promo_id, t.name, t.code, t2.item_id, '0' as 'is_combo'\n" +
                        " FROM (SELECT * FROM promo WHERE promo_type='CTSS' AND status = 2) as t\n" +
                        " JOIN promo_item_group t1 ON t1.promo_id=t.id\n" +
                        " LEFT JOIN promo_item_group_detail t2 ON t2.promo_id=t.id\n" +
                        " GROUP BY t1.promo_id,t2.item_id\n" +
                        " ORDER BY t1.promo_id DESC \n" +
                        " ) as a\n" +
                        " LEFT JOIN product t3 ON t3.id = a.item_id AND a.is_combo = 0" +
                        " WHERE " +
                        "('" + promo + "'= '' OR a.name LIKE '%" + promo + "%'" +
                        " OR a.code LIKE '%" + promo + "%')" +
                        " AND ('" + product + "'= '' OR  t3.full_name LIKE '%" + product + "%'" +
                        " OR t3.code LIKE '%" + product + "%')"
        );
    }

    public List<JSONObject> searchComboHuntSale(String promo, String combo) {
        return this.masterDB.find("SELECT a.promo_id,a.item_id" +
                " FROM \n" +
                " (SELECT t1.promo_id, t.name, t.code, t1.combo_id as 'item_id', '1' as 'is_combo'\n" +
                " FROM (SELECT * FROM promo WHERE promo_type='CTSS' AND status = 2) as t\n" +
                " JOIN promo_item_group t1 ON t1.promo_id=t.id AND t1.type='COMBO'\n" +
                " GROUP BY t1.promo_id, t1.combo_id\n" +
                " ORDER BY t1.promo_id DESC \n" +
                " ) as a\n" +
                " LEFT JOIN combo t4 ON t4.id = a.item_id AND a.is_combo = 1" +
                " WHERE " +
                "('" + promo + "'= '' OR a.name LIKE '%" + promo + "%'" +
                " OR a.code LIKE '%" + promo + "%')" +
                " AND ('" + combo + "'= '' OR  t4.full_name LIKE '%" + combo + "%'" +
                " OR t4.code LIKE '%" + combo + "%')"
        );
    }

    public JSONObject getComboInfo(int combo_id) {
        return this.masterDB.getOne(
                "SELECT * FROM combo WHERE id = " + combo_id
        );
    }

    public List<JSONObject> getListProductInCombo(int combo_id) {
        return this.masterDB.find(
                "SELECT * FROM combo_detail WHERE combo_id = " + combo_id
        );
    }

    public List<JSONObject> getPromoHuntSaleRunningByProduct(
            int item_id, int is_combo) {
        return this.masterDB.find("SELECT t.id, t.code, t.name, t.description, t1.combo_id" +
                " FROM (SELECT * FROM promo WHERE promo_type='CTSS' AND status = 2) as t" +
                " LEFT JOIN promo_item_group t1 ON t1.promo_id=t.id" +
                " LEFT JOIN promo_item_group_detail t2 ON t2.promo_id=t.id and t2.promo_item_group_id=t1.id" +
                " WHERE " +
                " (" + is_combo + " = 1 AND t1.combo_id = " + item_id + ")" +
                " OR (" + is_combo + " = 0 AND t2.item_id = " + item_id + ")" +
                " GROUP BY t.id, t.code, t.name, t.description, t1.combo_id" +
                " ORDER BY t.priority ASC, t.id DESC"
        );
    }

    public List<JSONObject> getPromoHuntSaleRunningByCombo(
            int combo_id) {
        return this.masterDB.find("SELECT t.id, t.code, t.name, t.description, t1.note, t.priority" +
                " FROM (SELECT * FROM promo WHERE promo_type='CTSS' AND status = 2) as t" +
                " JOIN promo_item_group t1 ON t1.promo_id=t.id" +
                " WHERE t1.type = 'COMBO' AND t1.combo_id = " + combo_id +
                " GROUP BY t.id, t.code, t.name, t.description, t1.note" +
                " ORDER BY t.priority ASC, t.id DESC"
        );
    }

    public List<JSONObject> getAllPromoHuntSaleRunningByCombo(
            int item_id, int status, String search
    ) {
        return this.masterDB.find(
                "SELECT t.id, t.code, t.name, t.description, t.status, t.start_date, t.end_date" +
                        " FROM (SELECT * FROM promo WHERE promo_type='CTSS') as t" +
                        " JOIN promo_item_group t1 ON t1.promo_id=t.id AND t1.type = 'COMBO'" +
                        " WHERE " +
                        " t1.combo_id = " + item_id +
                        " AND (" + status + " = 0 OR t.status = " + status + ")" +
                        " AND ('" + search + "' = ''" +
                        " OR t.code LIKE '%" + search + "%'" +
                        " OR t.name LIKE '%" + search + "%')" +
                        " GROUP BY t.id, t.code, t.name, t.description, t.status, t.start_date, t.end_date" +
                        " ORDER BY t.id DESC"
        );
    }

    public int insertPromoData(
            int promo_id,
            String data,
            String promo_type,
            int status) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query = "INSERT INTO promo_data(" +
                    "promo_id," +
                    "promo_data," +
                    "promo_type," +
                    "status)" +
                    " VALUES(" +
                    promo_id + "," +
                    "?" + "," +
                    "'" + promo_type + "'" + "," +
                    status +
                    ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, data);
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;
    }

    public boolean updatePromoData(
            int promo_id,
            String data,
            String promo_type,
            int promo_status) {
        boolean status = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "UPDATE promo_data SET " +
                    " promo_data = ?," +
                    " promo_type = " + "'" + promo_type + "'" + "," +
                    " status = " + promo_status +
                    " WHERE promo_id = " + promo_id;
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, data);
                int row = stmt.executeUpdate();
                if (row > 0) {
                    status = true;
                }
            }
        } catch (Exception ex) {
            status = false;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return status;
    }

    public JSONObject getPromoData(int promo_id) {
        return this.masterDB.getOne(
                "SELECT * FROM promo_data WHERE promo_id = " + promo_id
        );
    }

    public JSONObject getComboInfoByCode(String code) {
        return this.masterDB.getOne(
                "SELECT * FROM combo WHERE code = '" + code + "'"
        );
    }

    public int insertCombo(
            String name, String code, String description, String images, String hot_label, String data,
            int creator_id) {
        return this.masterDB.insert(
                "INSERT INTO combo(" +
                        "full_name," +
                        "code," +
                        "description," +
                        "images," +
                        "hot_label," +
                        "data," +
                        "creator_id" +
                        ") VALUES(" +
                        parseStringToSql(name) + "," +
                        parseStringToSql(code) + "," +
                        parseStringToSql(description) + "," +
                        parseStringToSql(images) + "," +
                        parseStringToSql(hot_label) + "," +
                        parseStringToSql(data) + "," +
                        parseIntegerToSql(creator_id) + "" +
                        ")"
        );
    }

    public int insertComboDetail(int combo_id, int product_id, int product_quantity) {
        return this.masterDB.insert(
                "INSERT INTO combo_detail(" +
                        "combo_id," +
                        "product_id," +
                        "product_quantity" +
                        ") VALUES(" +
                        parseIntegerToSql(combo_id) + "," +
                        parseIntegerToSql(product_id) + "," +
                        parseIntegerToSql(product_quantity) + "" +
                        ")"
        );
    }

    public JSONObject getLastPromoCTSS() {
        return this.masterDB.getOne(
                "SELECT * FROM promo WHERE promo_type = '" + PromoType.CTSS.getKey() + "' LIMIT 1"
        );
    }

    public List<JSONObject> getListPromoCTSS() {
        return this.masterDB.find(
                "SELECT * FROM promo WHERE promo_type = '" + PromoType.CTSS.getKey() + "' LIMIT 3"
        );
    }

    public List<JSONObject> getPromoCTSSPriorityList() {
        return this.masterDB.find(
                "SELECT * FROM promo WHERE promo_type = '" + PromoType.CTSS.getKey() + "'" +
                        " AND priority != 0" +
                        " AND status IN (" + PromoActiveStatus.canSort() + ")" +
                        " ORDER BY priority ASC"
        );
    }

    public boolean removePriorityWithPromoStopped() {
        return this.masterDB.update(
                "UPDATE promo SET priority = 0 WHERE status = " + PromoActiveStatus.STOPPED.getId()
        );
    }

    public int getLastPriorityPromoHuntSale() {
        JSONObject jsonObject = this.masterDB.getOne(
                "SELECT id,priority" +
                        " FROM promo WHERE promo_type = '" + PromoType.CTSS.getKey() + "'" +
                        " AND priority != 0" +
                        " ORDER BY priority DESC" +
                        " LIMIT 1"
        );
        if (jsonObject != null) {
            return ConvertUtils.toInt(jsonObject.get("priority"));
        }

        return 0;
    }

    public int getComboId(int promo_item_group_id) {
        JSONObject promoItemGroup = this.getPromoItemGroup(promo_item_group_id);
        if (promoItemGroup == null) {
            return 0;
        }

        return ConvertUtils.toInt(promoItemGroup.get("combo_id"));
    }

    private JSONObject getPromoItemGroup(int promo_item_group_id) {
        return this.masterDB.getOne(
                "SELECT * FROM promo_item_group WHERE id = " + promo_item_group_id
        );
    }

    public boolean updateCombo(int id, String hot_label, int status) {
        return this.masterDB.update(
                "UPDATE combo SET status = " + status + "," +
                        " hot_label = " + parseStringToSql(hot_label) +
                        " WHERE id = " + id
        );
    }

    public int countPromo(int combo_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT count(*) as total" +
                        " FROM (SELECT t.id, t.code, t.name, t.description, t.status" +
                        " FROM (SELECT * FROM promo WHERE promo_type='CTSS') as t" +
                        " JOIN promo_item_group t1 ON t1.promo_id=t.id AND t1.type = 'COMBO'" +
                        " WHERE t1.combo_id = " + combo_id +
                        " GROUP BY t.id, t.code, t.name, t.description, t.status) as a"
        );

        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public int checkHuntSale(int item_id, int is_combo) {
        if (is_combo == 0) {
            if (this.getPromoHuntSaleRunningByProduct(item_id, is_combo).isEmpty()) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (this.getPromoHuntSaleRunningByCombo(item_id).isEmpty()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    public JSONObject getPromoStructure(int promo_id) {
        return this.masterDB.getOne(
                "SELECT * FROM promo_structure WHERE promo_id = " + promo_id
        );
    }

    public List<JSONObject> getPromoFilterListInPromoIds(String data) {
        return this.masterDB.find(
                "SELECT id,code,name,promo_type,start_date,end_date,status,condition_type" +
                        " FROM promo" +
                        " WHERE status != -1" +
                        " AND " + "'" + data + "' LIKE " + "CONCAT('%\"',id,'\"%')"
        );
    }

    public List<JSONObject> getListPromoIgnorePromo(int promo_id) {
        return this.masterDB.find(
                "SELECT t1.id," +
                        "t1.code," +
                        "t1.name," +
                        "t1.promo_type," +
                        "t1.start_date," +
                        "t1.end_date," +
                        "t1.status," +
                        "t1.condition_type" +
                        " FROM (SELECT *" +
                        " FROM promo_structure" +
                        " WHERE promo_loai_tru LIKE " + "CONCAT('%\"'," + promo_id + ",'\"%')" +
                        ") as t" +
                        " LEFT JOIN promo t1 ON t1.id=t.promo_id"

        );
    }

    public List<JSONObject> getListPromoLienKetPromo(int promo_id) {
        return this.masterDB.find(
                "SELECT t1.id," +
                        "t1.code," +
                        "t1.name," +
                        "t1.promo_type," +
                        "t1.start_date," +
                        "t1.end_date," +
                        "t1.status," +
                        "t1.condition_type" +
                        " FROM (SELECT *" +
                        " FROM promo_structure" +
                        " WHERE promo_lien_ket LIKE " + "CONCAT('%\"'," + promo_id + ",'\"%')" +
                        ") as t" +
                        " LEFT JOIN promo t1 ON t1.id=t.promo_id"

        );
    }

    public List<JSONObject> getListPromoLink(String data) {
        return this.masterDB.find(
                "SELECT *" +
                        " FROM promo_link" +
                        " WHERE '" + data + "' LIKE " + "CONCAT('%\"',promo_id,'\"%')"
        );
    }

    public List<JSONObject> getListPromoDongThoiPromo(int promo_id) {
        return this.masterDB.find(
                "SELECT t1.id," +
                        "t1.code," +
                        "t1.name," +
                        "t1.promo_type," +
                        "t1.start_date," +
                        "t1.end_date," +
                        "t1.status," +
                        "t1.condition_type" +
                        " FROM (SELECT *" +
                        " FROM promo_structure" +
                        " WHERE promo_dong_thoi LIKE " + "CONCAT('%\"'," + promo_id + ",'\"%')" +
                        ") as t" +
                        " LEFT JOIN promo t1 ON t1.id=t.promo_id"

        );
    }

    public List<JSONObject> getListPromoDongThoiTruGTDTTPromo(int promo_id) {
        return this.masterDB.find(
                "SELECT t1.id," +
                        "t1.code," +
                        "t1.name," +
                        "t1.promo_type," +
                        "t1.start_date," +
                        "t1.end_date," +
                        "t1.status," +
                        "t1.condition_type" +
                        " FROM (SELECT *" +
                        " FROM promo_structure" +
                        " WHERE promo_lien_ket LIKE " + "CONCAT('%\"'," + promo_id + ",'\"%')" +
                        ") as t" +
                        " LEFT JOIN promo t1 ON t1.id=t.promo_id"

        );
    }

    public List<JSONObject> getPromoJSListInPromoIds(String data) {
        return this.masterDB.find(
                "SELECT id,promo_type,condition_type" +
                        " FROM promo" +
                        " WHERE status != -1" +
                        " AND " + "'" + data + "' LIKE " + "CONCAT('%\"',id,'\"%')"
        );
    }

    public void createPromoStructure(
            Integer promo_id,
            List<String> promo_lien_ket_list,
            List<String> promo_dong_thoi_list,
            List<String> promo_dong_thoi_tru_gtdtt_list,
            List<String> promo_loai_tru_list,
            int is_ignore_all_csbh,
            int is_ignore_all_ctkm) {
        this.masterDB.insert(
                "INSERT INTO promo_structure(" +
                        "promo_id," +
                        "promo_lien_ket," +
                        "promo_dong_thoi," +
                        "promo_dong_thoi_tru_gtdtt," +
                        "promo_loai_tru," +
                        "is_ignore_all_csbh," +
                        "is_ignore_all_ctkm" +
                        ")" +
                        " VALUES(" +
                        promo_id + "," +
                        "'" + JsonUtils.Serialize(promo_lien_ket_list) + "'," +
                        "'" + JsonUtils.Serialize(promo_dong_thoi_list) + "'," +
                        "'" + JsonUtils.Serialize(promo_dong_thoi_tru_gtdtt_list) + "'," +
                        "'" + JsonUtils.Serialize(promo_loai_tru_list) + "'," +
                        is_ignore_all_csbh + "," +
                        is_ignore_all_ctkm +
                        ")"
        );
    }

    public boolean setPromoWaitingPayment(int promo_id) {
        return this.masterDB.update(
                "UPDATE promo SET status = " + PromoCTTLStatus.WAITING_PAYMENT.getId() +
                        " WHERE id = " + promo_id
        );
    }

    public boolean setPromoWaitingReward(int promo_id) {
        return this.masterDB.update(
                "UPDATE promo SET status = " + PromoCTTLStatus.WAITING_REWARD.getId() +
                        " WHERE id = " + promo_id
        );
    }

    public boolean setPromoStop(int promo_id) {
        return this.masterDB.update(
                "UPDATE promo SET status = " + PromoCTTLStatus.STOPPED.getId() +
                        " WHERE id = " + promo_id
        );
    }

    public List<JSONObject> getListPromoCTTLRunning() {
        return this.masterDB.find(
                "SELECT id, order_date_data, payment_date_data, reward_date_data, status" +
                        " FROM promo" +
                        " WHERE promo_type = '" + PromoType.CTTL.getKey() + "'" +
                        " AND status IN (" +
                        PromoCTTLStatus.RUNNING.getId() + "," +
                        PromoCTTLStatus.WAITING_PAYMENT.getId() + "," +
                        PromoCTTLStatus.WAITING_REWARD.getId() + "" +
                        ")"
        );
    }

    public JSONObject getPromoJs(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM promo WHERE id = " + id
        );
    }

    public int getTotalLimit(int promo_id) {
        return this.getListPromoLimit(promo_id).size();
    }

    public List<JSONObject> getListPromoLimit(int promo_id) {
        return this.masterDB.find(
                "SELECT * FROM promo_limit WHERE promo_id = " + promo_id +
                        " AND condition_type != 'STEP'"
        );
    }

    public List<JSONObject> getListAgencyOfCTTLJs(int program_id, int require_confirm_join) {
        return this.masterDB.find(
                "SELECT t.*," +
                        "t1.code," +
                        "t1.shop_name," +
                        "t1.avatar," +
                        "t1.business_department_id," +
                        "t1.membership_id," +
                        "t1.avatar" +
                        " FROM agency_cttl_info t" +
                        " LEFT JOIN agency t1 ON t1.id=t.agency_id" +
                        " WHERE program_id = " + program_id +
                        " AND (" + require_confirm_join + " = 0 OR " +
                        " (" + require_confirm_join + "  = 1 AND confirm_join_quantity != 0))"
        );
    }

    public List<JSONObject> getListAgencyOfCSDMJs(int program_id) {
        return this.masterDB.find(
                "SELECT t.*," +
                        "t1.code," +
                        "t1.shop_name," +
                        "t1.avatar," +
                        "t1.business_department_id," +
                        "t1.membership_id," +
                        "t1.avatar" +
                        " FROM agency_csdm_info t" +
                        " LEFT JOIN agency t1 ON t1.id=t.agency_id" +
                        " WHERE program_id = " + program_id
        );
    }

    public JSONObject getAgencyCTTLInfoJs(
            int program_id,
            int agency_id) {
        return this.masterDB.getOne(
                "SELECT *" +
                        " FROM agency_cttl_info t" +
                        " WHERE program_id = " + program_id +
                        " AND agency_id = " + agency_id
        );
    }

    public JSONObject getOnePromoOfferBonus(int promo_id) {
        return this.masterDB.getOne(
                "SELECT t.*" +
                        " FROM promo_offer_bonus t" +
                        " WHERE t.promo_id = " + promo_id +
                        " LIMIT 1"
        );
    }

    public int countAgencyJoinCTTL(int promo_id) {
        return this.masterDB.getTotal(
                "SELECT id FROM agency_cttl_info WHERE program_id = " + promo_id
        );
    }

    public List<JSONObject> getListProductInPromo(int promo_id) {
        return this.masterDB.find(
                "SELECT * FROM promo_item_group_detail WHERE promo_id = " + promo_id
        );
    }

    public boolean clearPromoLinkGroup(int group_id) {
        return this.masterDB.update(
                "DELETE FROM promo_link WHERE group_id = " + group_id
        );
    }

    public int insertPromoLink(int promo_id, int group_id) {
        return this.masterDB.insert(
                "INSERT INTO promo_link(" +
                        "promo_id," +
                        "group_id)" +
                        " VALUES(" +
                        promo_id + "," +
                        group_id +
                        ")"
        );
    }

    public JSONObject getPromoLinkGroupByPromoId(int promo_id) {
        return this.masterDB.getOne(
                "SELECT * FROM promo_link WHERE promo_id = " + promo_id
        );
    }

    public List<JSONObject> getListPromoLinkByPromoId(int promo_id) {
        return this.masterDB.find(
                "SELECT t2.id," +
                        "t2.code," +
                        "t2.name," +
                        "t2.promo_type," +
                        "t2.start_date," +
                        "t2.end_date," +
                        "t2.status," +
                        "t2.condition_type" +
                        " FROM (SELECT * FROM promo_link WHERE promo_id = " + promo_id + ") as t" +
                        " LEFT JOIN promo_link t1 ON t1.group_id = t.group_id" +
                        " LEFT JOIN promo t2 ON t2.id = t1.promo_id" +
                        " WHERE t2.id != " + promo_id
        );
    }

    public List<JSONObject> getListCTTLOfAgency(int agency_id, int promo_id) {
        return this.masterDB.find(
                "SELECT t1.*, t.data, t.transaction" +
                        " FROM agency_cttl_info t" +
                        " LEFT JOIN promo t1 ON t1.id = t.program_id" +
                        " WHERE t.agency_id = " + agency_id +
                        " AND t.program_id != " + promo_id
        );
    }

    public List<JSONObject> getPromoCTTLInPromoIds(String data) {
        return this.masterDB.find(
                "SELECT id,code,name,promo_type,start_date,end_date,status,condition_type" +
                        " FROM promo" +
                        " WHERE status in (2,3,5,6)" +
                        " AND " + "'" + data + "' LIKE " + "CONCAT('%\"',id,'\"%')"
        );
    }

    public void saveCTTLTransactionTamTinh(int promo_id, int agency_id, String ltTLTransaction) {

    }

    public List<JSONObject> getListPromoLinkByGroup(int group_id) {
        return this.masterDB.find(
                "SELECT * FROM promo_link WHERE group_id = " + group_id
        );
    }

    public boolean removePromoLinkGroup(int promo_id) {
        return this.masterDB.update(
                "DELETE FROM promo_link WHERE promo_id = " + promo_id
        );
    }

    public int insertDamMeRunning(PromoRunningEntity promoRunningEntity) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query = "INSERT INTO dam_me_running(" +
                    "promo_id," +
                    "promo_data," +
                    "status" +
                    ")" +
                    " VALUES(" +
                    promoRunningEntity.getPromo_id() + "," +
                    "?," +
                    promoRunningEntity.getStatus() +
                    ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, promoRunningEntity.getPromo_data());
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;
    }

    public boolean removeDamMeRunning(int promo_id) {
        return this.masterDB.update(
                "DELETE FROM dam_me_running WHERE promo_id=" + promo_id
        );
    }

    public boolean updateDamMeRunning(PromoRunningEntity promoRunningEntity) {
        boolean status = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "UPDATE dam_me_running SET " +
                    "promo_data" + " = ?," +
                    "status" + " = " + parseIntegerToSql(promoRunningEntity.getStatus()) +
                    " WHERE id = " + promoRunningEntity.getId();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, promoRunningEntity.getPromo_data());
                int row = stmt.executeUpdate();
                if (row > 0) {
                    status = true;
                }
            }
        } catch (Exception ex) {
            status = false;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return status;
    }

    public JSONObject getAgencyCSDMInfoJs(
            int program_id,
            int agency_id) {
        return this.masterDB.getOne(
                "SELECT *" +
                        " FROM agency_csdm_info t" +
                        " WHERE program_id = " + program_id +
                        " AND agency_id = " + agency_id
        );
    }

    public List<JSONObject> getListCSDMLimit(int promo_id) {
        return this.masterDB.find(
                "SELECT *" +
                        " FROM promo_limit t" +
                        " WHERE promo_id = " + promo_id +
                        " AND condition_type != 'STEP'"
        );
    }

    public List<JSONObject> getListAgencyCSDMInfoJs(
            int agency_id) {
        return this.masterDB.find(
                "SELECT *" +
                        " FROM agency_csdm_info t" +
                        " WHERE agency_id = " + agency_id
        );
    }

    public int getTongSoLuongBanDau(int id) {
        JSONObject rs = this.masterDB.getOne("SELECT sum(confirm_join_quantity) as total" +
                " FROM agency_cttl_info WHERE program_id = " + id +
                " AND confirm_join_quantity > 0");
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public int getTongSoLuongThamGia(int id) {
        JSONObject rs = this.masterDB.getOne("SELECT sum(update_join_quantity) as total" +
                " FROM agency_cttl_info WHERE program_id = " + id +
                " AND update_join_quantity > 0");
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }

    public List<JSONObject> getListPromoAgencyIngore(String table) {
        return this.masterDB.find("SELECT * FROM " + table);
    }

    public JSONObject getVoucherReleasePeriod(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM voucher_release_period WHERE id = " + id
        );
    }

    public BXHRunningEntity getCTXHRunningById(int promo_id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM bxh_running WHERE promo_id=" + promo_id + ""
        );
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), BXHRunningEntity.class);
        }
        return null;
    }

    public int insertCTXHRunning(BXHRunningEntity promoRunningEntity) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query = "INSERT INTO bxh_running(" +
                    "promo_id," +
                    "promo_data," +
                    "status" +
                    ")" +
                    " VALUES(" +
                    promoRunningEntity.getPromo_id() + "," +
                    "?," +
                    promoRunningEntity.getStatus() +
                    ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, promoRunningEntity.getPromo_data());
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;
    }

    public boolean updateCTXHRunning(BXHRunningEntity promoRunningEntity) {
        boolean status = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "UPDATE bxh_running SET " +
                    "promo_data" + " = ?," +
                    "status" + " = " + parseIntegerToSql(promoRunningEntity.getStatus()) +
                    " WHERE id = " + promoRunningEntity.getId();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, promoRunningEntity.getPromo_data());
                int row = stmt.executeUpdate();
                if (row > 0) {
                    status = true;
                }
            }
        } catch (Exception ex) {
            status = false;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return status;
    }

    public List<JSONObject> getListAgencyOfCTXHJs(int program_id) {
        return this.masterDB.find(
                "SELECT t.*," +
                        "t1.code," +
                        "t1.shop_name," +
                        "t1.avatar," +
                        "t1.business_department_id," +
                        "t1.membership_id," +
                        "t1.avatar" +
                        " FROM agency_bxh_info t" +
                        " LEFT JOIN agency t1 ON t1.id=t.agency_id" +
                        " WHERE program_id = " + program_id
        );
    }

    public JSONObject getAgencyCTXHInfoJs(
            int program_id,
            int agency_id) {
        return this.masterDB.getOne(
                "SELECT *" +
                        " FROM agency_bxh_info t" +
                        " WHERE program_id = " + program_id +
                        " AND agency_id = " + agency_id
        );
    }

    public boolean updateAgencyDataForCTXH(int promo_id, String agency_data) {
        return this.masterDB.update("UPDATE bxh_running SET agency_data='" + agency_data + "' WHERE promo_id=" + promo_id);
    }

    public JSONObject getCTXHRunningJs(int promo_id) {
        return this.masterDB.getOne(
                "SELECT * FROM bxh_running WHERE promo_id=" + promo_id + ""
        );
    }

    public List<JSONObject> getListCTXHJoinByAgency(int agency_id) {
        return this.masterDB.find(
                "SELECT * FROM bxh_agency_join WHERE agency_id=" + agency_id + ""
        );
    }

    public List<JSONObject> getListCTXHRunningByAgency(int agency_id, String circle_type) {
        return this.masterDB.find(
                "SELECT t1.id FROM bxh_agency_join t" +
                        " LEFT JOIN promo t1 ON t1.id = t.promo_id" +
                        " WHERE t1.status = 2 AND t.agency_id=" + agency_id + "" +
                        " AND t1.circle_type = '" + circle_type + "'"
        );
    }

    public List<JSONObject> getListAgencyJoin(int promo_id) {
        return this.masterDB.find(
                "SELECT * FROM bxh_agency_join WHERE promo_id=" + promo_id + ""
        );
    }

    public List<JSONObject> getAllCTXHNotStopByCircle(String code) {
        return this.masterDB.find(
                "SELECT t.id, t.code, t.promo_type, t.name, t.description, t.status, t.start_date, t.end_date, t.circle_type" +
                        " FROM promo t WHERE t.promo_type = " + parseStringToSql(PromoType.BXH.getKey()) +
                        " AND t.circle_type = " + parseStringToSql(code) +
                        " AND t.status in (0,1,2)"
        );
    }

    public int insertBXHAgencyJoin(int agency_id, int promo_id) {
        return this.masterDB.insert("INSERT INTO bxh_agency_join(agency_id, promo_id)" +
                " VALUES(" +
                agency_id + ", " + promo_id + ")");
    }

    public int getTongKhachHangThamGia(int id, int require_confirm_join) {
        JSONObject rs = this.masterDB.getOne("SELECT count(*) as total" +
                " FROM agency_cttl_info WHERE program_id = " + id +
                " AND (" + require_confirm_join + " = 0 OR" +
                " (" + require_confirm_join + " = 1 AND confirm_join_quantity > 0))");
        if (rs != null) {
            return ConvertUtils.toInt(rs.get("total"));
        }
        return 0;
    }
}