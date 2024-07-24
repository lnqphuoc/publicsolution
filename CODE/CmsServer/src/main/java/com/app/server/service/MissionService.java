package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.MissionConstants;
import com.app.server.constants.ProductConstants;
import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.csdm.CSDMTransactionData;
import com.app.server.data.dto.cttl.CTTLAgencyResult;
import com.app.server.data.dto.ctxh.CTXHAgencyResult;
import com.app.server.data.dto.mission.*;
import com.app.server.data.dto.mission.ApplyFilterRequest;
import com.app.server.data.dto.product.Category;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.product.ProductGroup;
import com.app.server.data.dto.program.DeptInfo;
import com.app.server.data.dto.program.Program;
import com.app.server.data.dto.program.Source;
import com.app.server.data.dto.program.agency.Agency;
import com.app.server.data.dto.program.filter.ProgramFilter;
import com.app.server.data.dto.program.filter.ProgramFilterType;
import com.app.server.data.entity.AgencyOrderDetailEntity;
import com.app.server.data.entity.PromoHistoryEntity;
import com.app.server.data.entity.VoucherReleasePeriodEntity;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.*;
import com.app.server.data.request.cttl.GetResultMissionBXHInfoRequest;
import com.app.server.data.request.ctxh.VRPDataRequest;
import com.app.server.data.request.damme.GetOrderInfoByCodeRequest;
import com.app.server.data.request.damme.ProductGiamThemRequest;
import com.app.server.data.request.mission.*;
import com.app.server.data.request.promo.*;
import com.app.server.data.response.product.ItemResponse;
import com.app.server.database.MissionDB;
import com.app.server.database.MissionLogDB;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.google.common.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import io.swagger.models.auth.In;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MissionService extends BaseService {
    private MissionDB missionDB;

    @Autowired
    public void setMissionDB(MissionDB missionDB) {
        this.missionDB = missionDB;
    }

    private MissionLogDB missionLogDB;

    @Autowired
    public void setMissionLogDB(MissionLogDB missionLogDB) {
        this.missionLogDB = missionLogDB;
    }

    private AssignMissionService assignMissionService;

    @Autowired
    public void setAssignMissionService(AssignMissionService assignMissionService) {
        this.assignMissionService = assignMissionService;
    }

    private AccumulateMissionService accumulateMissionService;

    @Autowired
    public void setAccumulateMissionService(AccumulateMissionService accumulateMissionService) {
        this.accumulateMissionService = accumulateMissionService;
    }

    public ClientResponse filterMissionGroup(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_GROUP, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionDB.getTotal(query);
            for (JSONObject js : records) {
                js.put("creator_info", this.dataManager.getStaffManager().getStaffProfile(ConvertUtils.toInt(js.get("creator_id"))));
                js.put("quantity", this.missionDB.getTotalMissionByGroup(ConvertUtils.toInt(js.get("id"))));
            }
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterMissionByGroup(FilterListByIdRequest request) {
        try {
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("mission_group_id");
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(request.getId()));
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_BY_GROUP, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionDB.getTotal(query);

            for (JSONObject js : records) {
                this.parseItemDataOfMission(js);
                js.put("creator_info", this.dataManager.getStaffManager().getStaffProfile(ConvertUtils.toInt(js
                        .get("id"))));

                js.put("mission_name", js.get("name"));
                js.put("mission_required_value", js.get("required_value"));
                js.put("mission_action_number", js.get("action_number"));
                js.put("description", this.convertMissionName(js));
            }
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void parseItemDataOfMission(JSONObject js) {
        try {
            if (js == null || js
                    .get("item_data") == null) {
                return;
            }
            JSONObject jsItemData = JsonUtils.DeSerialize(js
                    .get("item_data").toString(), JSONObject.class);
            if (jsItemData == null) {
                return;
            }

            if (ProductConstants.ALL.equals(jsItemData.get("type").toString())) {
                return;
            }

            List<JSONObject> itemDataList = new ArrayList<>();
            List<JSONObject> items = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(jsItemData.get("items")),
                    new TypeToken<List<JSONObject>>() {
                    }.getType());

            for (JSONObject jsItem : items) {
                ItemInfo itemInfo = JsonUtils.DeSerialize(JsonUtils.Serialize(jsItem), ItemInfo.class);
                parseItemInfo(itemInfo);
                jsItem.put("id", itemInfo.getItem_id());
                jsItem.put("name", itemInfo.getItem_name());
                jsItem.put("code", itemInfo.getItem_code());
                itemDataList.add(jsItem);
            }
            jsItemData.put("items", itemDataList);
            js.put("item_data", JsonUtils.Serialize(jsItemData));
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    private void parseItemInfo(ItemInfo itemInfo) {
        try {
            MissionProductType productType = MissionProductType.fromKey(
                    itemInfo.getItem_type());
            if (productType == null) {
                return;
            }

            switch (productType) {
                case PRODUCT:
                    ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(itemInfo.getItem_id());
                    if (productCache == null) {
                        return;
                    }
                    itemInfo.setItem_code(productCache.getCode());
                    itemInfo.setItem_name(productCache.getFull_name());
                    return;
                case CATEGORY:
                    Category category = this.dataManager.getProductManager().getCategoryById(itemInfo.getItem_id());
                    if (category == null) {
                        return;
                    }
                    itemInfo.setItem_name(category.getName());
                    return;
                case ITEM_TYPE:
                    if (ItemType.MAY_MOC.getKey() == itemInfo.getItem_id()) {
                        itemInfo.setItem_name(ItemType.MAY_MOC.getValue());
                    } else if (ItemType.PHU_TUNG.getKey() == itemInfo.getItem_id()) {
                        itemInfo.setItem_name(ItemType.PHU_TUNG.getValue());
                    }
                    return;
                case PRODUCT_GROUP:
                    ProductGroup productGroup = this.dataManager.getProductManager().getMpProductGroup().get(itemInfo.getItem_id());
                    if (productGroup == null) {
                        return;
                    }
                    itemInfo.setItem_name(productGroup.getName());
                    itemInfo.setItem_code(productGroup.getCode());
                    return;
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    public ClientResponse getMissionGroupInfo(BasicRequest request) {
        try {
            JSONObject missionGroup = this.missionDB.getMissionGroupInfo(request.getId());
            if (missionGroup == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            missionGroup.put("creator_info", this.dataManager.getStaffManager().getStaffProfile(
                    ConvertUtils.toInt(missionGroup.get("creator_id"))));

            missionGroup.put("quantity", this.missionDB.getTotalMissionByGroup(request.getId()));

            JSONObject data = new JSONObject();
            data.put("mission_group_info", missionGroup);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getMissionSettingInfo(BasicRequest request) {
        try {
            JSONObject missionSettingInfo = this.missionDB.getMissionSettingInfo(request.getId());
            if (missionSettingInfo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int mission_group_id = ConvertUtils.toInt(missionSettingInfo.get("mission_group_id"));
            JSONObject missionGroupInfo = this.missionDB.getMissionGroupInfo(mission_group_id);


            JSONObject data = new JSONObject();
            data.put("mission_group_info", missionGroupInfo);
            /**
             * Đối tượng
             */
            ApplyObjectRequest apply_object =
                    JsonUtils.DeSerialize(
                            missionSettingInfo.get("apply_object_data").toString(),
                            ApplyObjectRequest.class);
            apply_object.getAgency_includes().forEach(
                    aic -> aic.initInfo(this.dataManager.getAgencyManager().getAgencyBasicData(aic.getId()))
            );
            apply_object.getAgency_ignores().forEach(
                    aic -> aic.initInfo(this.dataManager.getAgencyManager().getAgencyBasicData(aic.getId()))
            );

            data.put("apply_object", apply_object);

            /**
             * Tỷ lệ
             */
            GenerateRateData generate_rate_data = JsonUtils.DeSerialize(missionSettingInfo.get("generate_rate_data").toString(),
                    GenerateRateData.class);
            data.put("generate_rate_data", generate_rate_data);


            /**
             * Mức ưu đãi
             */
            List<JSONObject> vrps = new ArrayList<>();
            MissionOfferRequest offer_data =
                    JsonUtils.DeSerialize(missionSettingInfo.get("offer_data").toString(),
                            MissionOfferRequest.class);
            offer_data.getLimits().forEach(l -> {
                l.getOffers().forEach(o -> {
                    JSONObject jsVRP = this.missionDB.getVRP(o.getVrp_id());
                    if (jsVRP != null) {
                        vrps.add(jsVRP);
                    }
                });
            });

            List<JSONObject> nhiem_vu_uu_tien_tuan = new ArrayList<>();
            if (generate_rate_data.getTuan() != null) {
                generate_rate_data.getTuan().getMua_hang().getNhiem_vu_uu_tien().getNhiem_vu().forEach(
                        nv_id -> {
                            JSONObject nhiem_vu = this.missionDB.getMission(nv_id);
                            if (nhiem_vu != null) {
                                this.parseItemDataOfMission(nhiem_vu);
                                nhiem_vu.put("mission_name", nhiem_vu.get("name"));
                                nhiem_vu.put("mission_required_value", nhiem_vu.get("required_value"));
                                nhiem_vu.put("mission_action_number", nhiem_vu.get("action_number"));
                                nhiem_vu.put("description", this.convertMissionName(nhiem_vu));
                            }
                            nhiem_vu_uu_tien_tuan.add(nhiem_vu);
                        }
                );
            }
            List<JSONObject> nhiem_vu_uu_tien_thang = new ArrayList<>();
            if (generate_rate_data.getThang() != null) {
                generate_rate_data.getThang().getMua_hang().getNhiem_vu_uu_tien().getNhiem_vu().forEach(
                        nv_id -> {
                            JSONObject nhiem_vu = this.missionDB.getMission(nv_id);
                            if (nhiem_vu != null) {
                                this.parseItemDataOfMission(nhiem_vu);
                                nhiem_vu.put("mission_name", nhiem_vu.get("name"));
                                nhiem_vu.put("mission_required_value", nhiem_vu.get("required_value"));
                                nhiem_vu.put("mission_action_number", nhiem_vu.get("action_number"));
                                nhiem_vu.put("description", this.convertMissionName(nhiem_vu));
                            }
                            nhiem_vu_uu_tien_thang.add(nhiem_vu);
                        }
                );
            }

            List<JSONObject> nhiem_vu_uu_tien_quy = new ArrayList<>();
            if (generate_rate_data.getQuy() != null) {
                generate_rate_data.getQuy().getMua_hang().getNhiem_vu_uu_tien().getNhiem_vu().forEach(
                        nv_id -> {
                            JSONObject nhiem_vu = this.missionDB.getMission(nv_id);
                            if (nhiem_vu != null) {
                                this.parseItemDataOfMission(nhiem_vu);
                                nhiem_vu.put("mission_name", nhiem_vu.get("name"));
                                nhiem_vu.put("mission_required_value", nhiem_vu.get("required_value"));
                                nhiem_vu.put("mission_action_number", nhiem_vu.get("action_number"));
                                nhiem_vu.put("description", this.convertMissionName(nhiem_vu));
                            }
                            nhiem_vu_uu_tien_quy.add(nhiem_vu);
                        }
                );
            }

            missionSettingInfo.put("start_date_millisecond",
                    DateTimeUtils.getDateTime(missionSettingInfo.get("start_date").toString(), "yyyy-MM-dd HH:mm:ss").getTime());

            data.put("offer_data", offer_data);
            data.put("vrps", vrps);
            data.put("mission_setting_info", missionSettingInfo);
            data.put("nhiem_vu_uu_tien_tuan", nhiem_vu_uu_tien_tuan);
            data.put("nhiem_vu_uu_tien_thang", nhiem_vu_uu_tien_thang);
            data.put("nhiem_vu_uu_tien_quy", nhiem_vu_uu_tien_quy);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterMissionBXH(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_BXH, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query,
                    this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getMissionBXHInfo(BasicRequest request) {
        try {
            JSONObject missionBXH = this.missionDB.getMissionBXH(request.getId());
            if (missionBXH == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            MissionBXHData missionBXHData = this.convertMissionBXHData(missionBXH);

            JSONObject data = new JSONObject();
            data.put("mission_bxh", missionBXHData);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private MissionBXHData convertMissionBXHData(JSONObject missionBXH) {
        try {
            MissionBXHData missionBXHData = new MissionBXHData();
            /**
             * Mission BXH Info
             */
            missionBXHData.setInfo(JsonUtils.DeSerialize(JsonUtils.Serialize(missionBXH), MissionBXHInfo.class));

            ApplyObjectRequest applyObjectRequest = JsonUtils.DeSerialize(
                    missionBXH.get("apply_object_data").toString(), ApplyObjectRequest.class);
            /**
             * Chỉ định
             */
            applyObjectRequest.getAgency_includes().stream().forEach(
                    agencyBasicData ->
                            agencyBasicData.initInfo(
                                    this.dataManager.getAgencyManager().getAgencyBasicData(
                                            agencyBasicData.getId()))
            );

            /**
             * Loại trừ
             */
            applyObjectRequest.getAgency_ignores().stream().forEach(
                    agencyBasicData ->
                            agencyBasicData.initInfo(this.dataManager.getAgencyManager().getAgencyBasicData(agencyBasicData.getId()))
            );

            missionBXHData.setApply_object(applyObjectRequest);

            /**
             * Hạng mức ưu đãi
             */
            if (missionBXH.get("offer_data") != null) {
                List<MissionBXHLimitRequest> missionBXHLimitRequestList = JsonUtils.DeSerialize(
                        missionBXH.get("offer_data").toString(),
                        new TypeToken<List<MissionBXHLimitRequest>>() {
                        }.getType());
                missionBXHLimitRequestList.stream().forEach(
                        e -> e.getGroups().forEach(
                                g -> {
                                    List<JSONObject> vouchers = new ArrayList<>();
                                    g.getOffer().getOffer_voucher_info().stream().forEach(v -> {
                                        JSONObject voucher = this.missionDB.getVRP(ConvertUtils.toInt(v.get("id")));
                                        if (voucher != null) {
                                            voucher.put("expire_day", v.get("expire_day"));
                                            voucher.put("offer_value", v.get("offer_value"));
                                            vouchers.add(voucher);
                                        }
                                    });
                                    g.getOffer().setVoucher_data(JsonUtils.Serialize(vouchers));
                                }
                        ));
                missionBXHData.setLimits(missionBXHLimitRequestList);
            }
            return missionBXHData;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    private MissionBXHData convertMissionBXHToPromoData(JSONObject missionBXH) {
        try {
            MissionBXHData missionBXHData = new MissionBXHData();
            /**
             * Mission BXH Info
             */
            missionBXHData.setInfo(JsonUtils.DeSerialize(JsonUtils.Serialize(missionBXH), MissionBXHInfo.class));

            ApplyObjectRequest applyObjectRequest = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(missionBXH.get("apply_object_data")), ApplyObjectRequest.class);
            /**
             * Chỉ định
             */
            applyObjectRequest.getAgency_includes().stream().forEach(
                    agencyBasicData ->
                            agencyBasicData.initInfo(
                                    this.dataManager.getAgencyManager().getAgencyBasicData(
                                            agencyBasicData.getId()))
            );

            /**
             * Loại trừ
             */
            applyObjectRequest.getAgency_ignores().stream().forEach(
                    agencyBasicData ->
                            agencyBasicData.initInfo(this.dataManager.getAgencyManager().getAgencyBasicData(agencyBasicData.getId()))
            );

            missionBXHData.setApply_object(applyObjectRequest);

            /**
             * Hạng mức ưu đãi
             */
            if (missionBXH.get("offer_data") != null) {
                List<MissionBXHLimitRequest> missionBXHLimitRequestList = JsonUtils.DeSerialize(
                        missionBXH.get("offer_data").toString(),
                        new TypeToken<List<MissionBXHLimitRequest>>() {
                        }.getType());
                missionBXHLimitRequestList.stream().forEach(
                        e -> e.getGroups().forEach(
                                g -> {
                                    List<JSONObject> vouchers = new ArrayList<>();
                                    g.getOffer().getOffer_voucher_info().stream().forEach(v -> {
                                        JSONObject voucher = this.missionDB.getVRP(ConvertUtils.toInt(v.get("id")));
                                        if (voucher != null) {
                                            voucher.put("expire_day", v.get("expire_day"));
                                            voucher.put("offer_value", v.get("offer_value"));
                                            vouchers.add(voucher);
                                        }
                                    });
                                    g.getOffer().setVoucher_data(JsonUtils.Serialize(vouchers));
                                }
                        ));
                missionBXHData.setLimits(missionBXHLimitRequestList);
            }
            return missionBXHData;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    public ClientResponse createMissionBXH(SessionData sessionData, CreateMissionBXHRequest request) {
        try {
            ClientResponse crValidateCreateMissionBXH = this.validateCreateMissionBXH(request);
            if (crValidateCreateMissionBXH.failed()) {
                return crValidateCreateMissionBXH;
            }

            request.getLimits().forEach(
                    limit -> limit.getGroups().forEach(
                            group -> {
                                group.getOffer().setOffer_voucher_info(
                                        JsonUtils.DeSerialize(
                                                group.getOffer().getVoucher_data(),
                                                new TypeToken<List<JSONObject>>() {
                                                }.getType()));
                                group.getOffer().setVoucher_data("[]");
                            }
                    )
            );

            Date now = new Date();

            int month = now.getMonth() + 1;
            int year = now.getYear() + 1900;

            if (request.getInfo().getMission_period_id() == 3) {
                int quy = month / 3;
                if (month % 3 > 0) {
                    quy += 1;
                }

                if (quy == 1) {
                    request.getInfo().setStart_date_millisecond(
                            DateTimeUtils.getDateTime(year + "-" + "01" + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss").getTime());
                    request.getInfo().setEnd_date_millisecond(
                            DateTimeUtils.getDateTime(year + "-" + "03" + "-31 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime());
                } else if (quy == 2) {
                    request.getInfo().setStart_date_millisecond(
                            DateTimeUtils.getDateTime(year + "-" + "04" + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss").getTime());
                    request.getInfo().setEnd_date_millisecond(
                            DateTimeUtils.getDateTime(year + "-" + "06" + "-30 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime());
                } else if (quy == 3) {
                    request.getInfo().setStart_date_millisecond(
                            DateTimeUtils.getDateTime(year + "-" + "07" + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss").getTime());
                    request.getInfo().setEnd_date_millisecond(
                            DateTimeUtils.getDateTime(year + "-" + "09" + "-30 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime());
                } else if (quy == 4) {
                    request.getInfo().setStart_date_millisecond(
                            DateTimeUtils.getDateTime(year + "-" + "10" + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss").getTime());
                    request.getInfo().setEnd_date_millisecond(
                            DateTimeUtils.getDateTime(year + "-" + "12" + "-31 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime());
                }
            } else if (request.getInfo().getMission_period_id() == 2) {

            } else {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int rsCreateMissionBXH = this.missionDB.createMissionBXH(
                    generatePromoCode(),
                    request.getInfo(),
                    request.getApply_object(),
                    request.getLimits()
            );

            if (rsCreateMissionBXH <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            JSONObject jsMissionBXH = this.missionDB.getMissionBXH(rsCreateMissionBXH);

//            this.saveAgencyDataForMissionBXH(
//                    rsCreateMissionBXH,
//                    request.getInfo().getType(),
//                    this.createPromoDataFilter(
//                            jsMissionBXH,
//                            JsonUtils.DeSerialize(
//                                    jsMissionBXH.get("apply_object_data").toString(),
//                                    ApplyObjectRequest.class))
//            );

            this.saveMissionBXHHistory(
                    rsCreateMissionBXH,
                    jsMissionBXH,
                    "",
                    sessionData.getId(),
                    MissionBXHStatus.DRAFT.getId());


            JSONObject data = new JSONObject();
            data.put("id", rsCreateMissionBXH);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void saveMissionBXHHistory(int rsCreateMissionBXH, JSONObject data, String note, int staff_id, int status) {
        try {
            this.missionDB.insertMissionBXHHistory(rsCreateMissionBXH, data, note, staff_id, status);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    private ClientResponse validateCreateMissionBXH(CreateMissionBXHRequest request) {
        if (request.getInfo() == null || request.getInfo().validate().failed()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }

        if (request.getApply_object() == null || request.getApply_object().validate().failed()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }

        /**
         * Check đại lý chỉ định
         */
        for (AgencyBasicData agencyBasicData : request.getApply_object().getAgency_includes()) {
            if (this.dataManager.getAgencyManager().getAgencyBasicData(agencyBasicData.getId()) == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }
        }

        /**
         * Check đại lý loại trừ
         */
        for (AgencyBasicData agencyBasicData : request.getApply_object().getAgency_ignores()) {
            if (this.dataManager.getAgencyManager().getAgencyBasicData(agencyBasicData.getId()) == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }
        }

        /**
         * Đại lý chỉ định không trùng với đại lý loại trừ
         */
        for (int iDaiLyChiDinh = 0; iDaiLyChiDinh < request.getApply_object().getAgency_includes().size(); iDaiLyChiDinh++) {
            for (int iDaiLyLoaiTru = 0; iDaiLyLoaiTru < request.getApply_object().getAgency_ignores().size(); iDaiLyLoaiTru++) {
                if (request.getApply_object().getAgency_includes().get(iDaiLyChiDinh).getId()
                        == request.getApply_object().getAgency_ignores().get(iDaiLyLoaiTru).getId()) {
                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_AGENCY_INCLUDE_CONTAIN_AGENCY_IGNORE);
                    clientResponse.setMessage("[Đại lý chỉ định thứ " + (iDaiLyChiDinh + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }
            }
        }

        for (ApplyFilterRequest applyFilterRequest : request.getApply_object().getFilters()) {
            for (ApplyFilterDetailRequest applyFilterDetailRequest : applyFilterRequest.getFilter_types()) {
                if (!this.checkFilterType(applyFilterDetailRequest.getFilter_type())) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_TYPE_INVALID);
                }
            }
        }

        if (request.getLimits() == null || request.getLimits().isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }

        /**
         * Kiểm tra guyên tắc phát hành
         * - không được rỗng
         * - expire_day > 0
         * - nguyên tắc phát hành đã kích hoạt
         */
        for (int iLimit = 0; iLimit < request.getLimits().size(); iLimit++) {
            MissionBXHLimitRequest promoLimitRequest = request.getLimits().get(iLimit);
            if (promoLimitRequest.getLevel() <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_LIMIT_EMPTY);
            }
            boolean hasMoneyDiscount = false;
            for (int iGroup = 0; iGroup < promoLimitRequest.getGroups().size(); iGroup++) {
                int countMoneyDiscount = 0;
                MissionBXHLimitGroupRequest promoLimitGroupRequest = promoLimitRequest.getGroups().get(iGroup);
                if (promoLimitGroupRequest.getOffer().validate().failed()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_NOT_EMPTY);
                }

                String voucher_data = promoLimitGroupRequest.getOffer().getVoucher_data();
                if (voucher_data == null || voucher_data.isEmpty()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_NOT_EMPTY);
                }

                List<JSONObject> vouchers = JsonUtils.DeSerialize(voucher_data, new TypeToken<List<JSONObject>>() {
                }.getType());
                if (vouchers == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_NOT_EMPTY);
                }

                for (JSONObject vrp : vouchers) {
                    VRPDataRequest vrpDataRequest = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(vrp), VRPDataRequest.class);
                    if (vrpDataRequest.getId() == 0 || vrpDataRequest.getExpire_day() <= 0) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    JSONObject jsVRP = this.promoDB.getVoucherReleasePeriod(vrpDataRequest.getId());
                    if (jsVRP == null) {
                        ClientResponse crVRP = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
                        crVRP.setMessage("[VRPDataRequest] " + crVRP.getMessage());
                        return crVRP;
                    }

                    VoucherReleasePeriodEntity voucherReleasePeriodEntity = VoucherReleasePeriodEntity.from(jsVRP);
                    if (VoucherReleasePeriodStatus.ACTIVATED.getId() != voucherReleasePeriodEntity.getStatus()) {
                        ClientResponse crVRP = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
                        crVRP.setMessage("[" + voucherReleasePeriodEntity.getCode() + "] " + crVRP.getMessage());
                        return crVRP;
                    }

                    if (VoucherOfferType.MONEY_DISCOUNT.getKey().equals(voucherReleasePeriodEntity.getOffer_type())) {
                        countMoneyDiscount++;
                        if (countMoneyDiscount > 1) {
                            ClientResponse crVRP = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_TYPE_INVALID);
                            crVRP.setMessage("Chỉ cho phép 1 nguyên tắc phiếu giảm tiền / 1 hạn mức");
                            return crVRP;
                        }

                        if ((promoLimitGroupRequest.getOffer().getOffer_value() == null ||
                                promoLimitGroupRequest.getOffer().getOffer_value() == 0) &&
                                (promoLimitGroupRequest.getOffer().getOffer_dtt_value() == null ||
                                        promoLimitGroupRequest.getOffer().getOffer_dtt_value() == 0)) {
                            ClientResponse crVRP = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_TYPE_INVALID);
                            crVRP.setMessage("[" + voucherReleasePeriodEntity.getCode() + "] " + crVRP.getMessage());
                            return crVRP;
                        }

                        hasMoneyDiscount = true;
                    }

                    vrp.put("key", DigestUtils.md5Hex(ConvertUtils.toString(vrpDataRequest.getId())));
                }

                if ((promoLimitGroupRequest.getOffer().getOffer_value() > 0 ||
                        promoLimitGroupRequest.getOffer().getOffer_dtt_value() > 0) &&
                        hasMoneyDiscount == false

                ) {
                    return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Tỷ lệ ưu đãi");
                }

                if ((promoLimitGroupRequest.getOffer().getOffer_value() == 0 &&
                        promoLimitGroupRequest.getOffer().getOffer_dtt_value() == 0) &&
                        hasMoneyDiscount == true

                ) {
                    return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Voucher giảm tiền");
                }

                promoLimitGroupRequest.getOffer().setVoucher_data(JsonUtils.Serialize(vouchers));
            }
        }

        /*Tổng ưu đãi*/
        if (request.getInfo().getTotal_offer_value() < 0) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Tổng ưu đãi");
        }

        if (request.getInfo().getTotal_offer_value() == 0 && request.getInfo().getShow_total_offer_value_in_app() == 1) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Tổng ưu đãi: " + request.getInfo().getTotal_offer_value() + ", Hiển thị trên App: " + request.getInfo().getShow_total_offer_value_in_app());
        }

        if (request.getInfo().getTotal_offer_value() != 0) {
            long sum_offer_value = 0;
            for (int iLimit = 0; iLimit < request.getLimits().size(); iLimit++) {
                MissionBXHLimitRequest promoLimitRequest = request.getLimits().get(iLimit);
                for (int iGroup = 0; iGroup < promoLimitRequest.getGroups().size(); iGroup++) {
                    sum_offer_value += promoLimitRequest.getGroups().get(iGroup).getOffer().getOffer_value();
                }
            }
//            if (request.getInfo().getTotal_offer_value() != sum_offer_value) {
//                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Tỷ lệ ưu đãi/Tổng ưu đãi");
//            }
        }

        return ClientResponse.success(null);
    }

    private boolean checkFilterType(String filter_type) {
        if (ProgramFilterType.CAP_BAC.getKey().equals(filter_type) ||
                ProgramFilterType.DOANH_THU_THUAN_NAM_TRUOC.getKey().equals(filter_type) ||
                ProgramFilterType.DOANH_THU_THUAN_TU_DEN.getKey().equals(filter_type) ||
                ProgramFilterType.TINH_THANH.getKey().equals(filter_type) ||
                ProgramFilterType.VUNG_DIA_LY.getKey().equals(filter_type)
        ) {
            return true;
        }
        return false;
    }

    private String generatePromoCode() {
        try {
            PromoType promoType = PromoType.NHIEM_VU_BXH;

            /**
             * yyMMdd + loại chính sách + stt của loại chính sách
             */
            int count = this.missionDB.getTotalMissionBXH();
            if (count < 0) {
                return "";
            }
            count = count + 1;
            String tmp = StringUtils.leftPad(String.valueOf(count), 4, '0');
            DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
            String date = dateFormat.format(new Date());

            return (date + promoType.getCode() + "_" + tmp);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return "";
    }

    private List<JSONObject> getPromoOfferList(int id) {
        return new ArrayList<>();
    }

    private List<MissionBXHAgencyRankData> getListAgencyRank(int mission_bxh_id, List<Integer> agencyList, Date start_date, Date end_date) {
        try {
            List<MissionBXHAgencyRankData> agencyPointList = new ArrayList<>();
            agencyList.forEach(
                    agency -> {
                        MissionBXHAgencyRankData jsAgency = new MissionBXHAgencyRankData();
                        jsAgency.setAgency_id(agency);
                        jsAgency.setPoint(
                                this.missionDB.getTotalMissionBXHPoint(agency,
                                        this.appUtils.convertDateToString(start_date, "yyyy-MM-dd"),
                                        this.appUtils.convertDateToString(end_date, "yyyy-MM-dd")));
                        JSONObject jsLastData = this.missionDB.getLastData(agency, start_date, end_date);
                        if (jsLastData != null) {
                            jsAgency.setTime(DateTimeUtils.getDateTime(jsLastData.get("created_date").toString(),
                                    "yyyy-MM-dd HH:mm:ss"));
                            jsAgency.setRow_id(ConvertUtils.toInt(jsLastData.get("id")));
                        }
                        agencyPointList.add(jsAgency);
                    }
            );

            Collections.sort(
                    agencyPointList, (a, b) -> {
                        int aPoint = a.getPoint();
                        int bPoint = b.getPoint();
                        if (aPoint > bPoint) {
                            return -1;
                        } else if (aPoint < bPoint) {
                            return 1;
                        } else if (a.getRow_id() < b.getRow_id()) {
                            return -1;
                        } else if (a.getRow_id() > b.getRow_id()) {
                            return 1;
                        } else if (a.getAgency_id() < b.getAgency_id()) {
                            return -1;
                        } else return 0;
                    }
            );
            return agencyPointList;
        } catch (Exception e) {
            LogUtil.printDebug(Module.MISSION.name(), e);
        }
        return new ArrayList<>();
    }

    private ClientResponse filterResultAgencyTemp(SessionData sessionData, FilterListByIdRequest request, JSONObject missionBXH) {
        try {
            /**
             * ds hạn mức
             */
            List<JSONObject> hamMucList = new ArrayList<>();

            missionBXH.put("promo_limit_info", hamMucList);

            JSONObject promo_offer_bonus
                    = this.promoDB.getOnePromoOfferBonus(request.getId());
            if (promo_offer_bonus != null &&
                    ConvertUtils.toInt(promo_offer_bonus.get("product_id")) != 0) {
                missionBXH.put("gift_info", this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(promo_offer_bonus.get("product_id"))
                ));
            }

            PromoHistoryEntity jsPromoHistory = this.promoDB.getLastPromoHistory(request.getId());
            if (jsPromoHistory == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            Program program = this.dataManager.getProgramManager().importProgram(jsPromoHistory.getPromo_data());
            if (program == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            missionBXH.put("total_limit", this.promoDB.getTotalLimit(request.getId()));

            JSONObject data = new JSONObject();
            data.put("info", missionBXH);

            List<CTXHAgencyResult> cttlAgencyList = this.dataManager.getProgramManager().getAllAgencyReadyJoinCTXH();
            cttlAgencyList = cttlAgencyList.stream().filter(
                    x -> this.checkProgramFilter(
                            x.getAgency(),
                            program,
                            null,
                            null)).collect(Collectors.toList());

            int business_department_id = ConvertUtils.toInt(
                    this.filterUtils.getValueByKey(
                            request.getFilters(),
                            "business_department_id"));
            String search = this.filterUtils.getValueByType(
                    request.getFilters(),
                    "search");
            cttlAgencyList = cttlAgencyList.stream().filter(
                    x -> filterCTXHAgencyTemp(
                            x,
                            search,
                            0,
                            business_department_id)
            ).collect(Collectors.toList());

            int from = this.appUtils.getOffset(request.getPage());
            int to = from + ConfigInfo.PAGE_SIZE;
            int total = cttlAgencyList.size();
            if (to > total) {
                to = total;
            }
            List<CTXHAgencyResult> subList = new ArrayList<>();
            if (from < total) {
                subList = cttlAgencyList.subList(
                        from,
                        to);
            }

            data.put("records", subList);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkProgramFilter(Agency agency, Program program, Source source, DeptInfo deptInfo) {
        try {
            Date now = DateTimeUtils.getNow();
            // Loại trừ đại lý
            if (program.getLtIgnoreAgencyId().contains(agency.getId()))
                return false;
            // Bao gồm đại lý
            if (program.getLtIncludeAgencyId().contains(agency.getId()))
                return true;
            if (program.getLtIncludeAgencyId().isEmpty() && program.getLtProgramFilter().isEmpty())
                return true;
            if (!program.getLtIncludeAgencyId().isEmpty() && program.getLtProgramFilter().isEmpty())
                return false;
            // Bộ lọc
            for (ProgramFilter programFilter : program.getLtProgramFilter()) {
                // Kiểm tra cấp bậc
                boolean isMatchedMembership = true;
                if (!programFilter.getLtAgencyMembershipId().isEmpty())
                    isMatchedMembership = programFilter.getLtAgencyMembershipId().contains(agency.getMembershipId());
                if (!isMatchedMembership)
                    continue;
                // Kiểm tra phòng kinh doanh
                boolean isMatchedAgencyBusinessDepartment = true;
                if (!programFilter.getLtAgencyBusinessDepartmentId().isEmpty())
                    isMatchedAgencyBusinessDepartment = programFilter.getLtAgencyBusinessDepartmentId().contains(agency.getBusinessDepartmentId());
                if (!isMatchedAgencyBusinessDepartment)
                    continue;
                // Kiểm tra tỉnh - tp
                boolean isMatchedAgencyCity = true;
                if (!programFilter.getLtAgencyCityId().isEmpty())
                    isMatchedAgencyCity = programFilter.getLtAgencyCityId().contains(agency.getCityId());
                if (!isMatchedAgencyCity)
                    continue;
                // kiểm tra doanh thu thuần trong khoảng
                if (programFilter.getFromDttLastYear() > 0 ||
                        programFilter.getEndDttLastYear() > 0) {
                    int year = ConvertUtils.toInt(
                            this.appUtils.getYear(now));
                    long dtt_last_year = this.missionDB.getDttLastYear(
                            agency.getId(),
                            year - 1);
                    boolean isDttRange = true;
                    if (programFilter.getFromDttLastYear() > 0 && programFilter.getEndDttLastYear() > 0) {
                        isDttRange = (dtt_last_year >= programFilter.getFromDttLastYear() && dtt_last_year <= programFilter.getEndDttLastYear());
                    } else if (programFilter.getFromDttLastYear() > 0) {
                        isDttRange = (dtt_last_year >= programFilter.getFromDttLastYear());
                    } else if (programFilter.getEndDttLastYear() > 0) {
                        isDttRange = (dtt_last_year <= programFilter.getEndDttLastYear());
                    }
                    if (!isDttRange)
                        continue;
                }

                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return false;
    }

    private boolean filterCTXHAgencyTemp(
            CTXHAgencyResult x,
            String search,
            int limit,
            int business_department_id) {
        if (
                (search.isEmpty() ||
                        x.getAgency_info().getCode().contains(search) ||
                        x.getAgency_info().getCode().toLowerCase().contains(search.toLowerCase()) ||
                        x.getAgency_info().getShop_name().contains(search) ||
                        x.getAgency_info().getShop_name().toLowerCase().contains(search.toLowerCase())) &&
                        (business_department_id == 0 ||
                                x.getAgency_info().getBusiness_department_id() == business_department_id)
        ) {
            return true;
        }
        return false;
    }

    private String getNickName(JSONObject jsAgency) {
        if (jsAgency != null) {
            return ConvertUtils.toString(jsAgency.get("nick_name"));
        }

        return "";
    }

    private Integer getAgencyRank(int agencyId, List<MissionBXHAgencyRankData> agencyRankList) {
        for (int iRank = 0; iRank < agencyRankList.size(); iRank++) {
            if (agencyRankList.get(iRank).getAgency_id() == agencyId) {
                return iRank + 1;
            }
        }
        return null;
    }

    private List<JSONObject> getListVRPByRank(int rank, List<MissionBXHLimitRequest> limits) {
        if (rank > limits.size()) {
            return null;
        }
        MissionBXHLimitRequest offer = limits.get(rank - 1);
        List<JSONObject> vrpDataRequestList = JsonUtils.DeSerialize(
                offer.getGroups().get(0).getOffer().getVoucher_data(),
                new TypeToken<List<JSONObject>>() {
                }.getType()
        );

        List<JSONObject> vrps = new ArrayList<>();
        for (JSONObject js : vrpDataRequestList) {
            vrps.add(this.promoDB.getVoucherReleasePeriod(ConvertUtils.toInt(js.get("id"))));
        }
        return vrps;
    }

    public ClientResponse editMissionBXH(SessionData sessionData, EditMissionBXHRequest request) {
        try {
            JSONObject missionBXH = this.missionDB.getMissionBXH(request.getId());
            if (missionBXH == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            if (MissionBXHStatus.DRAFT.getId() != ConvertUtils.toInt(missionBXH.get("status"))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            ClientResponse crValidateCreateMissionBXH = this.validateCreateMissionBXH(
                    JsonUtils.DeSerialize(JsonUtils.Serialize(request), CreateMissionBXHRequest.class));
            if (crValidateCreateMissionBXH.failed()) {
                return crValidateCreateMissionBXH;
            }

            request.getLimits().forEach(
                    limit -> limit.getGroups().forEach(
                            group -> {
                                group.getOffer().setOffer_voucher_info(
                                        JsonUtils.DeSerialize(
                                                group.getOffer().getVoucher_data(),
                                                new TypeToken<List<JSONObject>>() {
                                                }.getType()));
                                group.getOffer().setVoucher_data("[]");
                            }
                    )
            );

            boolean rs = this.missionDB.updateMissionBXH(
                    request.getId(),
                    request.getInfo(),
                    request.getApply_object(),
                    request.getLimits()
            );

            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.saveMissionBXHHistory(
                    request.getId(),
                    this.missionDB.getMissionBXH(request.getId()),
                    request.getNote(),
                    sessionData.getId(),
                    MissionBXHStatus.DRAFT.getId());

            JSONObject data = new JSONObject();
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse activeMissionBXH(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject missionBXH = this.missionDB.getMissionBXH(request.getId());
            if (missionBXH == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            if (MissionBXHStatus.DRAFT.getId() != ConvertUtils.toInt(missionBXH.get("status"))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            MissionBXHInfo missionBXHInfo = MissionBXHInfo.from(missionBXH);
            if (missionBXHInfo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            Date now = DateTimeUtils.getNow();

            boolean rsStartMissionBXH = this.missionDB.startMissionBXH(request.getId());

            /**
             * Lưu đại lý tham gia xếp hạng
             */
            CreatePromoRequest promo_data = this.createPromoDataFilter(
                    missionBXH,
                    JsonUtils.DeSerialize(
                            missionBXH.get("apply_object_data").toString(),
                            ApplyObjectRequest.class));
            this.saveAgencyDataForMissionBXH(request.getId(), missionBXHInfo.getType(), promo_data);

            this.saveMissionBXHHistory(
                    request.getId(),
                    this.missionDB.getMissionBXH(request.getId()),
                    "",
                    sessionData.getId(),
                    MissionBXHStatus.RUNNING.getId()
            );

            JSONObject data = new JSONObject();
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void saveAgencyDataForMissionBXH(int mission_bxh_id, int type, CreatePromoRequest promo_data) {
        List<Agency> agencyList = this.getListAgencyByFilter(JsonUtils.Serialize(promo_data));
        List<Agency> agency_join_list = new ArrayList<>();
        agencyList.forEach(
                agency -> {
                    if (this.missionDB.getMissionBXHAgencyJoin(type, agency.getId()) == null) {
                        agency_join_list.add(agency);
                    }
                }
        );

        this.missionDB.updateAgencyDataForMissionBXH(
                mission_bxh_id,
                JsonUtils.Serialize(agency_join_list.stream().map(
                        e -> e.getId()
                ).collect(Collectors.toList())));

        for (Agency agency : agency_join_list) {
            this.missionDB.insertMissionBXHAgencyJoin(agency.getId(), mission_bxh_id, type);
        }
    }

    private CreatePromoRequest createPromoDataFilter(
            JSONObject missionBXH,
            ApplyObjectRequest applyObjectRequest) {
        try {
            CreatePromoRequest promo_data = new CreatePromoRequest();
            promo_data.setPromo_info(JsonUtils.DeSerialize(JsonUtils.Serialize(missionBXH), PromoInfoRequest.class));
            promo_data.getPromo_info().setPromo_type(PromoType.NHIEM_VU_BXH.getKey());
            promo_data.getPromo_info().setCondition_type(PromoConditionType.DTT.getKey());
            PromoApplyObjectRequest promoApplyObjectRequest = new PromoApplyObjectRequest();
            promoApplyObjectRequest.setPromo_agency_ignores(applyObjectRequest.getAgency_ignores());
            promoApplyObjectRequest.setPromo_agency_includes(applyObjectRequest.getAgency_includes());
            promoApplyObjectRequest.setPromo_filters(
                    JsonUtils.DeSerialize(
                            JsonUtils.Serialize(applyObjectRequest.getFilters()),
                            new TypeToken<List<PromoApplyFilterRequest>>() {
                            }.getType()));
            promo_data.setPromo_apply_object(promoApplyObjectRequest);
            return promo_data;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    private List<Agency> getListAgencyByFilter(String promo_data) {
        List<Agency> agencyList = new ArrayList<>();

        Program program = this.dataManager.getProgramManager().importProgram(
                promo_data);
        if (program == null) {
            return agencyList;
        }

        List<Agency> jsAgencyList = this.dataManager.getProgramManager().getListAgencyReadyJoinMission();
        for (Agency agency : jsAgencyList) {
            if (this.checkProgramFilter(
                    agency,
                    program,
                    Source.WEB,
                    null)) {
                agencyList.add(agency);
            }
        }
        return agencyList;
    }

    private boolean checkAgencyJoinMissionSetting(int agency_id, String promo_data) {
        try {
            Program program = this.dataManager.getProgramManager().importProgram(
                    promo_data);
            if (program == null) {
                return false;
            }

            Agency agency = this.dataManager.getProgramManager().getAgency(agency_id);
            if (this.checkProgramFilter(
                    agency,
                    program,
                    Source.WEB,
                    null)) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return false;
    }

    public ClientResponse runStartMissionBXH() {
        try {
            SessionData sessionData = new SessionData();
//            List<JSONObject> missionBXHList = this.missionDB.getListMissionBXHNeedStart();
//            for (JSONObject js : missionBXHList) {
//                BasicRequest basicRequest = new BasicRequest();
//                basicRequest.setId(ConvertUtils.toInt(js.get("id")));
//                ClientResponse crActiveMissionBXH = this.activeMissionBXH(sessionData, basicRequest);
//                if (crActiveMissionBXH.failed()) {
//                    this.alertToTelegram("runStartMissionBXH: " + JsonUtils.Serialize(crActiveMissionBXH), ResponseStatus.EXCEPTION);
//                }
//            }
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse doubleCheckMissionBXH(CreateMissionBXHRequest request) {
        try {
            ClientResponse crValidate = this.validateCreateMissionBXH(request);
            if (crValidate.failed()) {
                return crValidate;
            }

            PromoInfoRequest promoInfoRequest = new PromoInfoRequest();
            promoInfoRequest.setId(0);
            promoInfoRequest.setCode("");
            promoInfoRequest.setName("");
            promoInfoRequest.setDescription("");
            promoInfoRequest.setPromo_type(PromoType.NHIEM_VU_BXH.getKey());
            promoInfoRequest.setCondition_type(PromoConditionType.DTT.getKey());
            JSONObject jsMissionBXH = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(promoInfoRequest),
                    JSONObject.class);
            jsMissionBXH.put("apply_object_data", request.getApply_object());
            JSONObject jsMissionBXHData = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(
                            this.createPromoDataFilter(
                                    jsMissionBXH,
                                    request.getApply_object())),
                    JSONObject.class);
            jsMissionBXHData.put("promo_info", promoInfoRequest);
            String promo_data = JsonUtils.Serialize(jsMissionBXHData);
            List<Agency> agencyList = this.getListAgencyByFilter(promo_data);
            List<JSONObject> records = new ArrayList<>();
            for (Agency agency : agencyList) {
                List<JSONObject> promos = this.missionDB.getListMissionBXHByAgencyAndType(
                        agency.getId(), request.getInfo().getType());
                if (promos.size() > 0) {
                    JSONObject jsAgency = new JSONObject();
                    jsAgency.put("id", agency.getId());
                    jsAgency.put("shop_name", agency.getShop_name());
                    jsAgency.put("code", agency.getCode());
                    jsAgency.put("membership_id", agency.getMembershipId());
                    jsAgency.put("avatar", agency.getAvatar());
                    jsAgency.put("promos", promos);
                    records.add(jsAgency);
                }
            }
            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse cancelMissionBXH(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject missionBXH = this.missionDB.getMissionBXH(request.getId());
            if (missionBXH == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }
            boolean rs = this.missionDB.cancelMissionBXH(request.getId());
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.missionDB.clearMissionBXHAgencyJoin(request.getId());

            this.saveMissionBXHHistory(
                    request.getId(),
                    this.missionDB.getMissionBXH(request.getId()),
                    "",
                    sessionData.getId(),
                    MissionBXHStatus.CANCEL.getId()
            );

            JSONObject data = new JSONObject();
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Danh sách khoản tích lũy của đại lý
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filterTransactionMissionBXHByAgency(SessionData sessionData, FilterTransactionMissionBXHRequest request) {
        try {
            LogUtil.printDebug(JsonUtils.Serialize(request));
            int mission_bxh_id = request.getBtt_id();
            int agency_id = request.getAgency_id();

            JSONObject jsMissionBXH = this.missionDB.getMissionBXH(mission_bxh_id);
            if (jsMissionBXH == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            MissionBXHData missionBXHData = this.convertMissionBXHData(jsMissionBXH);
            if (missionBXHData == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            JSONObject data = new JSONObject();

            MissionPeriodType missionPeriodType = MissionPeriodType.from(missionBXHData.getInfo().getMission_period_id());
            Date now = new Date();
            switch (missionPeriodType) {
                case THANG: {
                    int month = ConvertUtils.toInt(this.appUtils.getMonth(now));
                    int year = ConvertUtils.toInt(this.appUtils.getYear(now));
                    if ((request.getPeriod() == 0 && request.getYear() == 0) || (year == request.getYear() && month == request.getPeriod())) {
                        Date start_date = this.getStartDateOfMissionBXH(missionBXHData.getInfo().getMission_period_id(), now);
                        Date end_date = this.getEndDateOfMissionBXH(missionBXHData.getInfo().getMission_period_id(), now);
                        return filterTransactionMissionBXHByAgencyByDate(request, start_date, end_date);
                    } else {
                        Date start_date = DateTimeUtils.getDateTime(
                                year + "-" + String.format("%02d", request.getPeriod()) + "-01 00:00:00",
                                "yyyy-MM-dd HH:mm:ss");
                        Date end_date = DateTimeUtils.getDateTime(
                                year + "-" + String.format("%02d", request.getPeriod()) +
                                        "-" + String.format("%02d", this.appUtils.getLastDayOfMonth(start_date)) +
                                        " 23:59:59",
                                "yyyy-MM-dd HH:mm:ss");
                        return filterTransactionMissionBXHByAgencyByDate(request, start_date, end_date);
                    }
                }
                case QUY: {
                    int quy = this.getQuy(now);
                    int year = ConvertUtils.toInt(this.appUtils.getYear(now));
                    if (request.getYear() != 0) {
                        year = request.getYear();
                    }
                    if (request.getPeriod() != 0) {
                        quy = request.getPeriod();
                    }
                    Date start_date = DateTimeUtils.getDateTime(
                            year + "-" + this.getNgayBatDauQuy(quy) + " 00:00:00",
                            "yyyy-MM-dd HH:mm:ss");
                    Date end_date = DateTimeUtils.getDateTime(
                            year + "-" + this.getNgayKetThucQuy(quy) + " 23:59:59",
                            "yyyy-MM-dd HH:mm:ss");

                    return filterTransactionMissionBXHByAgencyByDate(
                            request,
                            start_date,
                            end_date);
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse filterTransactionMissionBXHByAgencyByDate(FilterTransactionMissionBXHRequest request,
                                                                     Date start_date, Date end_date) {
        try {

            List<FilterRequest> filters = new ArrayList<>();
            request.getFilters().forEach(
                    f -> {
                        if (f.getKey().equals("created_date")) {
                            filters.add(f);
                        }
                    }
            );
            request.setFilters(filters);
            int agency_id = request.getAgency_id();
            /* Đại lý */
            FilterRequest agencyRequest = new FilterRequest();
            agencyRequest.setType(TypeFilter.SELECTBOX);
            agencyRequest.setValue(ConvertUtils.toString(agency_id));
            agencyRequest.setKey("agency_id");
            request.getFilters().add(agencyRequest);

            /* Thời gian của BXH */
            FilterRequest timeBXH = new FilterRequest();
            timeBXH.setKey("");
            timeBXH.setType(TypeFilter.SQL);
            timeBXH.setValue("(created_date BETWEEN '" +
                    this.appUtils.convertDateToString(start_date, "yyyy-MM-dd HH:mm:ss") + "'" +
                    " AND '" + this.appUtils.convertDateToString(end_date, "yyyy-MM-dd HH:mm:ss") + "')");
            request.getFilters().add(timeBXH);
            String query = this.filterUtils.getQuery(FunctionList.FILTER_TK_MISSION_BXH_AGENCY_INFO, request.getFilters(), request.getSorts());
            LogUtil.printDebug(query);
            List<JSONObject> records = this.missionDB.filter(
                    query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private long getGiaTriHuyHieu(CSDMTransactionData cttlTransactionData) {
        return cttlTransactionData.getTotalValue();
    }

    private List<JSONObject> getPromoLimitInfo(JSONObject missionBXH) {
        List<JSONObject> promoLimitInfoList = new ArrayList<>();
        return promoLimitInfoList;
    }

    private void parseCTTLTransactionData(CSDMTransactionData cttlTransactionData) {
        try {
            if (cttlTransactionData.getType().equals(CTXHTransactionType.DON_HANG.getKey())) {
                cttlTransactionData.setTransaction_type(CTXHTransactionType.DON_HANG.getId());
                cttlTransactionData.setAccumulate_type(
                        CSDMAccumulateType.DON_HANG.getId()
                );
            } else if (cttlTransactionData.getType().equals(CTXHTransactionType.HBTL.getKey())) {
                cttlTransactionData.setTransaction_type(CSDMTransactionType.HBTL.getId());
                cttlTransactionData.setTt(cttlTransactionData.getTt());
                cttlTransactionData.setAccumulate_type(
                        CTXHTransactionType.HBTL.getId()
                );
            } else if (cttlTransactionData.getType().equals(CTXHTransactionType.TANG_CONG_NO.getKey())) {
                cttlTransactionData.setTransaction_type(CTXHTransactionType.TANG_CONG_NO.getId());
                cttlTransactionData.setTt(cttlTransactionData.getTt());
                cttlTransactionData.setAccumulate_type(
                        CTXHTransactionType.TANG_CONG_NO.getId()
                );
            } else if (cttlTransactionData.getType().equals(CTXHTransactionType.GIAM_CONG_NO.getKey())) {
                cttlTransactionData.setTransaction_type(CTXHTransactionType.GIAM_CONG_NO.getId());
                cttlTransactionData.setTt(cttlTransactionData.getTt());
                cttlTransactionData.setAccumulate_type(
                        CTXHTransactionType.GIAM_CONG_NO.getId()
                );
            } else if (cttlTransactionData.getType().equals(CTXHTransactionType.DIEU_CHINH_DTT.getKey())) {
                cttlTransactionData.setTransaction_type(CTXHTransactionType.DIEU_CHINH_DTT.getId());
                cttlTransactionData.setTt(cttlTransactionData.getTt());
                cttlTransactionData.setAccumulate_type(
                        CTXHTransactionType.DIEU_CHINH_DTT.getId()
                );
            }

            JSONObject rsCTTLTransactionInfo = this.getCTTLTransactionInfo(
                    cttlTransactionData
            );

            cttlTransactionData.setTransaction_value(
                    ConvertUtils.toLong(rsCTTLTransactionInfo.get("gia_tri"))
            );
            cttlTransactionData.setPayment_value(
                    ConvertUtils.toLong(rsCTTLTransactionInfo.get("thanh_toan"))
            );
            cttlTransactionData.setPayment_date(
                    ConvertUtils.toLong(rsCTTLTransactionInfo.get("ngay_thanh_toan"))
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
    }

    private JSONObject getCTTLTransactionInfo(
            CSDMTransactionData cttlTransactionData
    ) {
        JSONObject rs = new JSONObject();
        long gia_tri = 0;
        long thanh_toan = 0;
        long ngay_thanh_toan = 0;
        long uu_dai_dam_me = 0;
        try {
            if (cttlTransactionData.getTransaction_type() == CTXHTransactionType.DON_HANG.getId()) {
                JSONObject transaction_info = this.deptDB.getDeptOrderInfoByCode(
                        cttlTransactionData.getCode()
                );
                if (transaction_info != null) {
                    gia_tri = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    thanh_toan = ConvertUtils.toLong(transaction_info.get("payment_value"));
                    ngay_thanh_toan = transaction_info.get("payment_date") == null ? 0L :
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toString(
                                            transaction_info.get("payment_date"))).getTime();
                    uu_dai_dam_me = ConvertUtils.toLong(transaction_info.get("uu_dai_dam_me"));
                }
            } else if (cttlTransactionData.getTransaction_type() == CTXHTransactionType.HBTL.getId()) {
                JSONObject transaction_info = this.orderDB.getAgencyHBTL(
                        cttlTransactionData.getTransactionId()
                );
                if (transaction_info != null) {
                    gia_tri = ConvertUtils.toLong(transaction_info.get("total_end_price"));
                    thanh_toan = ConvertUtils.toLong(transaction_info.get("total_end_price"));
                    ngay_thanh_toan = transaction_info.get("created_date") == null ? 0L :
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toString(
                                            transaction_info.get("created_date"))).getTime();
                }
            } else if (cttlTransactionData.getTransaction_type() == CTXHTransactionType.TANG_CONG_NO.getId()) {
                JSONObject transaction_info = this.deptDB.getDeptTransactionJs(
                        cttlTransactionData.getTransactionId()
                );
                if (transaction_info != null) {
                    gia_tri = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    thanh_toan = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    ngay_thanh_toan = transaction_info.get("created_date") == null ? 0L :
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toString(
                                            transaction_info.get("created_date"))).getTime();
                }
            } else if (cttlTransactionData.getTransaction_type() == CTXHTransactionType.GIAM_CONG_NO.getId()) {
                JSONObject transaction_info = this.deptDB.getDeptTransactionJs(
                        cttlTransactionData.getTransactionId()
                );
                if (transaction_info != null) {
                    gia_tri = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    thanh_toan = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    ngay_thanh_toan = transaction_info.get("created_date") == null ? 0L :
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toString(
                                            transaction_info.get("created_date"))).getTime();
                }
            } else if (cttlTransactionData.getTransaction_type() == CTXHTransactionType.DIEU_CHINH_DTT.getId()) {
                JSONObject transaction_info = this.deptDB.getDeptTransactionJs(
                        cttlTransactionData.getTransactionId()
                );
                if (transaction_info != null) {
                    gia_tri = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    thanh_toan = ConvertUtils.toLong(transaction_info.get("transaction_value"));
                    ngay_thanh_toan = transaction_info.get("created_date") == null ? 0L :
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toString(
                                            transaction_info.get("created_date"))).getTime();
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.MISSION.name(), ex);
        }
        rs.put("gia_tri", gia_tri);
        rs.put("thanh_toan", thanh_toan);
        rs.put("ngay_thanh_toan", ngay_thanh_toan);
        rs.put("uu_dai_dam_me", uu_dai_dam_me);
        return rs;
    }

    private int getSoLuongThamGia(int mission_bxh_id) {
        try {
            return this.missionDB.getSoLuongThamGiaMissionBXH(mission_bxh_id);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.MISSION.name(), ex);
        }
        return 0;
    }

    public ClientResponse createMissionGroup(SessionData sessionData, CreateMissionGroupRequest request) {
        try {
            ClientResponse crValidate = this.validateCreateMissionGroup(request);
            if (crValidate.failed()) {
                return crValidate;
            }

            int rsMissionGroup = this.missionDB.insertMissionGroup(
                    request.getName(), sessionData.getId());
            if (rsMissionGroup <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (request.getMissions() != null) {
                for (MissionRequest missionRequest : request.getMissions()) {
                    MissionType missionType = MissionType.from(missionRequest.getMission_type_id());
                    if (missionType == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                    switch (missionType) {
                        case MUA_HANG:
                            if (missionRequest.getTuan() != null) {
                                int rsMissionTuan = this.missionDB.createMission(
                                        missionRequest, missionRequest.getTuan(), rsMissionGroup, MissionPeriodType.TUAN.getId());
                                if (rsMissionTuan <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }

                            if (missionRequest.getThang() != null) {
                                int rsMissionThang = this.missionDB.createMission(
                                        missionRequest, missionRequest.getThang(), rsMissionGroup, MissionPeriodType.THANG.getId());
                                if (rsMissionThang <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }

                            if (missionRequest.getQuy() != null) {
                                int rsMissionQuy = this.missionDB.createMission(
                                        missionRequest, missionRequest.getQuy(), rsMissionGroup, MissionPeriodType.QUY.getId());
                                if (rsMissionQuy <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }
                            break;
                        case THANH_TOAN:
                            if (missionRequest.getTuan() != null) {
                                int rsMissionTuan = this.missionDB.createMission(
                                        missionRequest, missionRequest.getTuan(), rsMissionGroup, MissionPeriodType.TUAN.getId());
                                if (rsMissionTuan <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }

                            if (missionRequest.getThang() != null) {
                                int rsMissionThang = this.missionDB.createMission(
                                        missionRequest, missionRequest.getThang(), rsMissionGroup, MissionPeriodType.THANG.getId());
                                if (rsMissionThang <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }

                            if (missionRequest.getQuy() != null) {
                                int rsMissionQuy = this.missionDB.createMission(
                                        missionRequest, missionRequest.getQuy(), rsMissionGroup,
                                        MissionPeriodType.QUY.getId());
                                if (rsMissionQuy <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }
                            break;
                        case NQH: {
                            if (missionRequest.getTuan() != null) {
                                int rsMissionTuan = this.missionDB.createMission(
                                        missionRequest,
                                        missionRequest.getTuan(),
                                        rsMissionGroup,
                                        MissionPeriodType.TUAN.getId());
                                if (rsMissionTuan <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }

                            if (missionRequest.getThang() != null) {
                                int rsMissionThang = this.missionDB.createMission(
                                        missionRequest,
                                        missionRequest.getThang(),
                                        rsMissionGroup,
                                        MissionPeriodType.THANG.getId());
                                if (rsMissionThang <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }

                            if (missionRequest.getQuy() != null) {
                                int rsMissionQuy = this.missionDB.createMission(
                                        missionRequest,
                                        missionRequest.getQuy(),
                                        rsMissionGroup,
                                        MissionPeriodType.QUY.getId());
                                if (rsMissionQuy <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }
                            break;
                        }
                    }
                }
            }

            this.missionDB.insertMissionGroupHistory(
                    rsMissionGroup,
                    this.missionDB.getMission(rsMissionGroup),
                    "",
                    sessionData.getId(),
                    1
            );

            JSONObject data = new JSONObject();
            data.put("id", rsMissionGroup);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse validateCreateMissionGroup(CreateMissionGroupRequest request) {
        try {
            if (request.getName() == null || request.getName().isEmpty()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID);
            }

            if (request.getMissions() != null) {
                for (MissionRequest missionRequest : request.getMissions()) {
                    ClientResponse crMission = missionRequest.validate();
                    if (crMission.failed()) {
                        return crMission;
                    }
                }
            }

            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterMissionBXHHistory(FilterListByIdRequest request) {
        try {
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setValue(ConvertUtils.toString(request.getId()));
            filterRequest.setKey("mission_bxh_id");
            filterRequest.setType(TypeFilter.SELECTBOX);
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_BXH_HISTORY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query,
                    this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionDB.getTotal(query);
            for (JSONObject js : records) {
                JSONObject missionBXH = JsonUtils.DeSerialize(js.get("data").toString(), JSONObject.class);
                if (missionBXH != null) {
                    js.put("status", missionBXH.get("status"));
                }

                js.put("creator_info", this.dataManager.getStaffManager().getStaffProfile(ConvertUtils.toInt(js.get("creator_id"))));
            }
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deleteMissionBXH(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject missionBXH = this.missionDB.getMissionBXH(request.getId());
            if (missionBXH == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            if (!(MissionBXHStatus.DRAFT.getId() == ConvertUtils.toInt(missionBXH.get("status")) ||
                    MissionBXHStatus.CANCEL.getId() == ConvertUtils.toInt(missionBXH.get("status")))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rs = this.missionDB.deleteMissionBXH(request.getId());
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.saveMissionBXHHistory(
                    request.getId(),
                    this.missionDB.getMissionBXH(request.getId()),
                    "",
                    sessionData.getId(),
                    MissionBXHStatus.DELETE.getId()
            );

            JSONObject data = new JSONObject();
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse cancelMission(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject mission = this.missionDB.getMission(request.getId());
            if (mission == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            boolean rs = this.missionDB.cancelMission(request.getId());
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            JSONObject data = new JSONObject();
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editMissionGroup(SessionData sessionData, EditMissionGroupRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }
            JSONObject mission = this.missionDB.getMissionGroupInfo(request.getId());
            if (mission == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            boolean rs = this.missionDB.updateMissionGroup(request);
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Lưu lịch sử
             */
            this.missionDB.insertMissionGroupHistory(
                    request.getId(),
                    this.missionDB.getMissionGroupInfo(request.getId()),
                    "",
                    sessionData.getId(),
                    1
            );

            JSONObject data = new JSONObject();
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createMissionSetting(SessionData sessionData, CreateMissionSettingRequest request) {
        try {
            ClientResponse crValidateMissionSetting = this.validateCreateMissionSetting(request);
            if (crValidateMissionSetting.failed()) {
                return crValidateMissionSetting;
            }

            int rsInsertMissionSetting = this.missionDB.insertMissionSetting(request, sessionData.getId());
            if (rsInsertMissionSetting <= 0) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, rsInsertMissionSetting);
            }

            this.missionDB.insertMissionSettingHistory(
                    rsInsertMissionSetting,
                    this.missionDB.getMissionSettingInfo(rsInsertMissionSetting),
                    "",
                    sessionData.getId(),
                    DataStatus.ACTIVE.getValue()
            );
            JSONObject data = new JSONObject();
            data.put("id", rsInsertMissionSetting);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse validateCreateMissionSetting(CreateMissionSettingRequest request) {
        if (StringUtils.isBlank(request.getName())) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.NAME_INVALID);
        }

        if (request.getStart_date_millisecond() <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BEGIN_INVALID);
        }

        JSONObject missionGroup = this.missionDB.getMissionGroupInfo(request.getMission_group_id());
        if (missionGroup == null) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "request.getMission_group_id(): " + request.getMission_group_id());
        }

        if (request.getGenerate_rate_data() == null) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "request.getGenerate_rate_data(): " + request.getGenerate_rate_data());
        }

        if (request.getApply_object() == null) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "request.getApply_object(): " + request.getApply_object());
        }

        if (request.getOffer_data() == null) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "request.getOffer_data(): " + request.getOffer_data());
        }

        ClientResponse crGenerateRateData = request.getGenerate_rate_data().validate();
        if (crGenerateRateData.failed()) {
            return crGenerateRateData;
        }

        for (ApplyFilterRequest applyFilterRequest : request.getApply_object().getFilters()) {
            for (ApplyFilterDetailRequest applyFilterDetailRequest : applyFilterRequest.getFilter_types()) {
                if (!this.checkFilterType(applyFilterDetailRequest.getFilter_type())) {
                    return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_TYPE_INVALID, JsonUtils.Serialize(applyFilterDetailRequest));
                }
            }
        }

        for (AgencyBasicData ai : request.getApply_object().getAgency_includes()) {
            if (this.dataManager.getAgencyManager().getAgencyBasicData(ai.getId()) == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_TYPE_INVALID, "Đại lý chỉ định - " + JsonUtils.Serialize(ai));
            }
        }

        for (AgencyBasicData ai : request.getApply_object().getAgency_ignores()) {
            if (this.dataManager.getAgencyManager().getAgencyBasicData(ai.getId()) == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.SETTING_OBJECT_TYPE_INVALID, "Đại lý loại trừ - " + JsonUtils.Serialize(ai));
            }
        }

        List<Integer> nhiemVuUuTienTuan = request.getGenerate_rate_data().getTuan().getMua_hang().getNhiem_vu_uu_tien().getNhiem_vu();
        for (Integer nhiemVuUuTien : nhiemVuUuTienTuan) {
            JSONObject jsMission = this.missionDB.getMission(nhiemVuUuTien);
            if (jsMission == null ||
                    ConvertUtils.toInt(jsMission.get("mission_group_id")) != request.getMission_group_id()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL,
                        "nhiemVuUuTienTuan - Nhiệm vụ không tồn tại hoặc không thuộc nhóm nhiệm vụ: " + nhiemVuUuTien);
            }
        }

        List<Integer> nhiemVuUuTienThang = request.getGenerate_rate_data().getThang().getMua_hang().getNhiem_vu_uu_tien().getNhiem_vu();
        for (Integer nhiemVuUuTien : nhiemVuUuTienThang) {
            JSONObject jsMission = this.missionDB.getMission(nhiemVuUuTien);
            if (jsMission == null ||
                    ConvertUtils.toInt(jsMission.get("mission_group_id")) != request.getMission_group_id()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL,
                        "nhiemVuUuTienThang - Nhiệm vụ không tồn tại hoặc không thuộc nhóm nhiệm vụ: " + nhiemVuUuTien);
            }
        }

        List<Integer> nhiemVuUuTienQuy = request.getGenerate_rate_data().getQuy().getMua_hang().getNhiem_vu_uu_tien().getNhiem_vu();
        for (Integer nhiemVuUuTien : nhiemVuUuTienQuy) {
            JSONObject jsMission = this.missionDB.getMission(nhiemVuUuTien);
            if (jsMission == null ||
                    ConvertUtils.toInt(jsMission.get("mission_group_id")) != request.getMission_group_id()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL,
                        "nhiemVuUuTienQuy - Nhiệm vụ không tồn tại hoặc không thuộc nhóm nhiệm vụ: " + nhiemVuUuTien);
            }
        }

        /**
         * Mức ưu đãi
         */
        if (request.getOffer_data() == null || request.getOffer_data().getLimits().size() == 0) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL,
                    "Mức ưu đãi");
        }

        int size = request.getOffer_data().getLimits().size();
        if (size != request.getOffer_data().getLimits().get(size - 1).getLevel()) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Mức ưu đãi không hợp lệ: level - " + JsonUtils.Serialize(request.getOffer_data()));
        }

        for (MissionSettingLimitRequest l : request.getOffer_data().getLimits()) {
            if (l.getImage() == null || l.getImage().isEmpty()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Mức ưu đãi không hợp lệ: level - " + JsonUtils.Serialize(request.getOffer_data()));
            }
            if (l.getValue() <= 0) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Số lượng huy hiệu - " + JsonUtils.Serialize(l));
            }
            if (l.getOffers().isEmpty()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Mức ưu đãi không hợp lệ: level - " + JsonUtils.Serialize(request.getOffer_data()));
            }
            for (MissionSettingLimitOfferRequest o : l.getOffers()) {
                VoucherOfferType voucherOfferType = VoucherOfferType.from(o.getOffer_type());
                if (voucherOfferType == null) {
                    return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Loại ưu đãi không hợp lệ: " + JsonUtils.Serialize(o));
                }
                if (o.getOffer_value() <= 0) {
                    return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Loại ưu đãi không hợp lệ: offer_value" + JsonUtils.Serialize(o));
                }
                if (VoucherOfferType.ACOIN.getId() != voucherOfferType.getId() &&
                        o.getExpire_day() <= 0) {
                    return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Loại ưu đãi không hợp lệ: expire_day" + JsonUtils.Serialize(o));
                }

                if (VoucherOfferType.ACOIN.getId() != voucherOfferType.getId()) {
                    JSONObject jsVRP = this.missionDB.getVRP(o.getVrp_id());
                    if (jsVRP == null) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Nguyên tắc không tồn tại: " + o.getVrp_id());
                    }
                } else {
                    JSONObject jsVRP = this.missionDB.getVRPAcoin();
                    if (jsVRP == null) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Nguyên tắc không tồn tại: " + o.getVrp_id());
                    }
                    o.setVrp_id(ConvertUtils.toInt(jsVRP.get("id")));
                }
            }
        }
        return ResponseConstants.success;
    }

    public ClientResponse editMissionSetting(SessionData sessionData, EditMissionSettingRequest request) {
        try {
            JSONObject jsMissionSetting = this.missionDB.getMissionSettingInfo(request.getId());
            if (jsMissionSetting == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Thiết lập không tồn tại");
            }
            ClientResponse crValidate = this.validateCreateMissionSetting(request);
            if (crValidate.failed()) {
                return crValidate;
            }

            int status = ConvertUtils.toInt(jsMissionSetting.get("status"));
            if (MissionBXHStatus.DRAFT.getId() != status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsUpdateMissionSetting = this.missionDB.updateMissionSetting(request.getId(), request);
            if (!rsUpdateMissionSetting) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.missionDB.insertMissionSettingHistory(
                    request.getId(),
                    this.missionDB.getMissionSettingInfo(request.getId()),
                    "",
                    sessionData.getId(),
                    DataStatus.ACTIVE.getValue()
            );
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse activeMissionSetting(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject missionSetting = this.missionDB.getMissionSettingInfo(request.getId());
            if (missionSetting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int status = ConvertUtils.toInt(missionSetting.get("status"));
            if (MissionSettingStatus.DRAFT.getId() != status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            Date now = new Date();
            Date start_date = DateTimeUtils.getDateTime(
                    ConvertUtils.toString(
                            missionSetting.get("start_date")));
            if (start_date == null || start_date.before(now)) {
                start_date = now;
            }

            boolean rsActive = this.missionDB.activeMissionSetting(request.getId(), start_date);
            if (!rsActive) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Lưu danh sách đại lý thuộc đối tượng
             */
            this.deleteMissionSettingAgencyJoin(request.getId());
            this.saveMissionSettingAgencyJoin(request.getId());

            this.missionDB.insertMissionSettingHistory(
                    request.getId(),
                    this.missionDB.getMissionSettingInfo(request.getId()),
                    "",
                    sessionData.getId(),
                    DataStatus.ACTIVE.getValue());

            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<Agency> saveMissionSettingAgencyJoin(int mission_setting_id) {
        try {
            JSONObject jsMissionSetting = this.missionDB.getMissionSettingInfo(mission_setting_id);
            if (jsMissionSetting == null) {
                return null;
            }
            String data = JsonUtils.Serialize(
                    this.convertMissionSettingDataToPromoData(jsMissionSetting));
            List<Agency> agencyList = this.getListAgencyByFilter(data);

            for (Agency agency : agencyList) {
                this.missionDB.insertMissionSettingAgencyJoin(agency.getId(), mission_setting_id);
            }
            return agencyList;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    private void deleteMissionSettingAgencyJoin(int mission_setting_id) {
        try {
            boolean rs = this.missionDB.deleteMissionSettingAgencyJoin(mission_setting_id);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    public ClientResponse cancelMissionSetting(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject missionSetting = this.missionDB.getMissionSettingInfo(request.getId());
            if (missionSetting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int status = ConvertUtils.toInt(missionSetting.get("status"));
            if (MissionBXHStatus.CANCEL.getId() == status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsCancel = this.missionDB.cancelMissionSetting(request.getId());
            if (!rsCancel) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rsStopMissionSettingAgency = this.missionDB.stopMissionSettingAgency(request.getId());

            this.missionDB.insertMissionSettingHistory(
                    request.getId(),
                    this.missionDB.getMissionSettingInfo(request.getId()),
                    "",
                    sessionData.getId(),
                    DataStatus.ACTIVE.getValue());

            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deleteMissionSetting(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject missionSetting = this.missionDB.getMissionSettingInfo(request.getId());
            if (missionSetting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int status = ConvertUtils.toInt(missionSetting.get("status"));
            if (MissionBXHStatus.RUNNING.getId() == status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsDelete = this.missionDB.deleteMissionSetting(request.getId());
            if (!rsDelete) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.missionDB.insertMissionSettingHistory(
                    request.getId(),
                    this.missionDB.getMissionSettingInfo(request.getId()),
                    "",
                    sessionData.getId(),
                    DataStatus.ACTIVE.getValue());

            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getMissionBXHHistoryInfo(BasicRequest request) {
        try {
            JSONObject missionBXHHistory = this.missionDB.getMissionBXHHistory(request.getId());
            if (missionBXHHistory == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            JSONObject missionBXH = JsonUtils.DeSerialize(missionBXHHistory.get("data").toString(), JSONObject.class);

            MissionBXHData missionBXHData = this.convertMissionBXHData(missionBXH);

            missionBXHData.getInfo().setCreated_date(missionBXHHistory.get("created_date") == null ? null :
                    DateTimeUtils.getDateTime(
                            ConvertUtils.toString(
                                    missionBXHHistory.get("created_date")))
            );

            JSONObject data = new JSONObject();
            data.put("mission_bxh", missionBXHData);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchMissionGroup(FilterListRequest request) {
        try {
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("status");
            filterRequest.setValue(ConvertUtils.toString(MissionGroupStatus.RUNNING.getId()));
            filterRequest.setType(TypeFilter.SELECTBOX);
            request.getFilters().add(filterRequest);

            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_GROUP, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterTKMissionAgency(FilterListRequest request) {
        try {
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(request.getId()));
            filterRequest.setKey("mission_setting_id");
            request.getFilters().add(filterRequest);

            String query = this.filterUtils.getQuery(FunctionList.FILTER_TK_MISSION_AGENCY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionDB.getTotal(query);
            records.forEach(
                    js -> {
                        int agency_id = ConvertUtils.toInt(js.get("agency_id"));
                        js.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(agency_id));
                        js.put("tong_huy_hieu", this.getTongHuyHieuTKMissionAgency(request.getId(), agency_id));
                        js.put("tong_nhiem_vu", this.getTongNhiemVuTKMissionAgency(request.getId(), agency_id));
                        js.put("tong_nhiem_vu_hoan_thanh", this.getTongNhiemVuHoanThanhTKMissionAgency(request.getId(), agency_id));
                        js.put("start_date", this.getNgayBatDauThucHienNhiemVuByThietLap(request.getId(), agency_id));
                        js.put("end_date", this.getNgayKetThucThucHienNhiemVuByThietLap(request.getId(), agency_id));
                    }
            );
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private Object getNgayBatDauThucHienNhiemVuByThietLap(int mission_setting_id, int agency_id) {
        try {
            JSONObject js = this.missionDB.getAgencyMissionInfoEarly(mission_setting_id, agency_id);
            if (js != null) {
                return js.get("created_date");
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    private Object getNgayKetThucThucHienNhiemVuByThietLap(int mission_setting_id, int agency_id) {
        try {
            JSONObject js = this.missionDB.getAgencyMissionInfoLasted(mission_setting_id, agency_id);
            if (js != null) {
                return js.get("mission_end_date");
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    private long getTongHuyHieuTKMissionAgency(int mission_setting_id, int agency_id) {
        try {
            return this.missionDB.getTongHuyHieuTKMissionAgency(mission_setting_id, agency_id);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return 0;
    }

    private int getTongNhiemVuTKMissionAgency(int mission_setting_id, int agency_id) {
        try {
            return this.missionDB.getTongNhiemVuTKMissionAgency(mission_setting_id, agency_id);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return 0;
    }

    private int getTongNhiemVuHoanThanhTKMissionAgency(int mission_setting_id, int agency_id) {
        try {
            return this.missionDB.getTongNhiemVuHoanThanhTKMissionAgency(mission_setting_id, agency_id);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return 0;
    }

    private CreatePromoRequest convertMissionSettingDataToPromoData(JSONObject js) {
        try {
            CreatePromoRequest promo_data = new CreatePromoRequest();
            PromoInfoRequest promoInfoRequest = new PromoInfoRequest();
            promoInfoRequest.setId(ConvertUtils.toInt(js.get("id")));
            promoInfoRequest.setCode("");
            promoInfoRequest.setName("");
            promoInfoRequest.setDescription("");
            promoInfoRequest.setPromo_type(PromoType.NHIEM_VU_BXH.getKey());
            promoInfoRequest.setCondition_type(PromoConditionType.DTT.getKey());
            promo_data.setPromo_info(promoInfoRequest);
            ApplyObjectRequest applyObjectRequest = JsonUtils.DeSerialize(
                    js.get("apply_object_data").toString(),
                    ApplyObjectRequest.class);
            PromoApplyObjectRequest promoApplyObjectRequest = new PromoApplyObjectRequest();
            promoApplyObjectRequest.setPromo_agency_ignores(applyObjectRequest.getAgency_ignores());
            promoApplyObjectRequest.setPromo_agency_includes(applyObjectRequest.getAgency_includes());
            promoApplyObjectRequest.setPromo_filters(
                    JsonUtils.DeSerialize(
                            JsonUtils.Serialize(applyObjectRequest.getFilters()),
                            new TypeToken<List<PromoApplyFilterRequest>>() {
                            }.getType()));
            promo_data.setPromo_apply_object(promoApplyObjectRequest);
            return promo_data;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    public ClientResponse filterTKMission(FilterListRequest request) {
        try {
            FilterRequest settingRequest = new FilterRequest();
            settingRequest.setType(TypeFilter.SELECTBOX);
            settingRequest.setValue(ConvertUtils.toString(request.getId()));
            settingRequest.setKey("mission_setting_id");
            request.getFilters().add(settingRequest);

            FilterRequest agencyRequest = new FilterRequest();
            agencyRequest.setType(TypeFilter.SELECTBOX);
            agencyRequest.setValue(ConvertUtils.toString(request.getAgency_id()));
            agencyRequest.setKey("agency_id");
            request.getFilters().add(agencyRequest);

            String query = this.filterUtils.getQuery(FunctionList.FILTER_TK_MISSION, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionDB.getTotal(query);
            records.forEach(
                    js -> {
                        js.put("mission_name", this.convertMissionName(js));
                    }
            );
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterTKMissionTransaction(FilterListRequest request) {
        try {
            try {
                JSONObject jsAgencyMission = this.missionDB.getAgencyMissionInfo(request.getId());
                if (jsAgencyMission == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                List<MissionTransactionData> mission_data_all = this.convertMissionData(jsAgencyMission);

                List<MissionTransactionInfo> finalMissionTransactionInfoList = new ArrayList<>();
                List<MissionTransactionData> mission_data = null;
                MissionType missionType = MissionType.from(ConvertUtils.toInt(jsAgencyMission.get("mission_type_id")));
                switch (missionType) {
                    case MUA_HANG: {
                        mission_data = mission_data_all.stream().filter(
                                x -> MissionTransactionType.DON_HANG.getKey().equals(x.getType())
                        ).collect(Collectors.toList());

                        List<MissionTransactionData> mission_child_data = mission_data_all.stream().filter(
                                x -> !MissionTransactionType.DON_HANG.getKey().equals(x.getType())
                        ).collect(Collectors.toList());

                        for (MissionTransactionData md : mission_data) {
                            MissionTransactionInfo missionTransactionInfo = parseMissionTransactionData(md);
                            missionTransactionInfo.setTotalValueFinal(missionTransactionInfo.getTotalValue());
                            mission_child_data.forEach(
                                    mcd -> {
                                        mcd.getLtChangedTransaction().forEach(
                                                ct -> {
                                                    if (ct.getTransactionCode().equals(md.getCode()) &&
                                                            mcd.getCreatedTime() > missionTransactionInfo.getCreatedTime()) {
                                                        MissionTransactionInfo changedInfo = JsonUtils.DeSerialize(JsonUtils.Serialize(mcd), MissionTransactionInfo.class);
                                                        changedInfo.setTotalValue(ct.getChangedValue() * -1);
                                                        changedInfo.setId(changedInfo.getOrderId());
                                                        changedInfo = parseMissionChangedTransactionData(changedInfo);
                                                        missionTransactionInfo.setTotalValueFinal(
                                                                missionTransactionInfo.getTotalValueFinal() + changedInfo.getTotalValue());
                                                        if (missionTransactionInfo.getTotalValueFinal() < 0) {
                                                            missionTransactionInfo.setTotalValueFinal(0);
                                                        }
                                                        missionTransactionInfo.getChildList().add(changedInfo);
                                                    }
                                                });
                                    }
                            );

                            finalMissionTransactionInfoList.add(missionTransactionInfo);
                        }

                        break;
                    }
                    default: {
                        mission_data = new ArrayList<>();
                        mission_data.addAll(mission_data_all);
                        for (MissionTransactionData md : mission_data) {
                            finalMissionTransactionInfoList.add(parseMissionTransactionData(md));
                        }
                    }
                }


                int type = ConvertUtils.toInt(
                        this.filterUtils.getValueByKey(
                                request.getFilters(),
                                "type"));
                int status = ConvertUtils.toInt(
                        this.filterUtils.getValueByKey(
                                request.getFilters(),
                                "status"));
                String search = this.filterUtils.getValueByType(
                        request.getFilters(),
                        "search");
                PromoTimeRequest timeRequest = this.filterUtils.getValueByTime(
                        request.getFilters(),
                        "created_date");

                finalMissionTransactionInfoList = finalMissionTransactionInfoList.stream().filter(
                        x -> filterMissionTransaction(
                                x,
                                search,
                                type,
                                status,
                                timeRequest)
                ).collect(Collectors.toList());
                int total = finalMissionTransactionInfoList.size();

                Collections.sort(finalMissionTransactionInfoList, (a, b) -> a.getCreatedTime() < b.getCreatedTime() ? -1 : a.getCreatedTime() == b.getCreatedTime() ? 0 : 1);

                int from = this.appUtils.getOffset(request.getPage());
                int to = from + ConfigInfo.PAGE_SIZE;
                total = finalMissionTransactionInfoList.size();
                if (to > total) {
                    to = total;
                }
                List<MissionTransactionInfo> subTransactionDataList;
                if (from < total) {
                    subTransactionDataList = finalMissionTransactionInfoList.subList(
                            from,
                            to);
                } else {
                    subTransactionDataList = new ArrayList<>();
                }

                JSONObject data = new JSONObject();
                data.put("records", subTransactionDataList);
                data.put("total", total);
                data.put("total_page", this.appUtils.getTotalPage(total));
                return ClientResponse.success(data);
            } catch (Exception e) {
                LogUtil.printDebug("MISSION", e);
            }
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<MissionTransactionData> convertMissionData(JSONObject jsAgencyMission) {
        List<MissionTransactionData> missionTransactionDataList = new ArrayList<>();
        try {
            if (jsAgencyMission.get("mission_data") == null ||
                    jsAgencyMission.get("mission_data").toString().isEmpty()) {
                return missionTransactionDataList;
            }

            List<JSONObject> jsonObjectList = JsonUtils.DeSerialize(
                    jsAgencyMission.get("mission_data").toString(),
                    new TypeToken<List<JSONObject>>() {
                    }.getType());
            for (JSONObject jsonObject : jsonObjectList) {
                missionTransactionDataList.add(MissionTransactionData.from(jsonObject));
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return missionTransactionDataList;
    }

    private MissionTransactionInfo parseMissionChangedTransactionData(MissionTransactionInfo transaction) {
        try {
            MissionTransactionType missionTransactionType = MissionTransactionType.fromKey(transaction.getType());
            switch (missionTransactionType) {
                case DON_HANG: {
                    transaction.setAccumulate_type(
                            missionTransactionType.getId()
                    );
                    JSONObject js = this.missionDB.getAgencyOrderDeptTransactionInfoByDeptCode(transaction.getCode());
                    if (js == null) {
                        return transaction;
                    }
                    transaction.setTransaction_info(JsonUtils.DeSerialize(JsonUtils.Serialize(js), TransactionInfo.class));
                    break;
                }
                case GIAM_CONG_NO: {
                    transaction.setAccumulate_type(
                            missionTransactionType.getId()
                    );
                    JSONObject js = this.missionDB.getDeptTransaction(transaction.getCode());
                    if (js == null) {
                        return transaction;
                    }
                    transaction.setTransaction_info(JsonUtils.DeSerialize(JsonUtils.Serialize(js), TransactionInfo.class));

                    break;
                }
                case TANG_CONG_NO: {
                    transaction.setAccumulate_type(
                            missionTransactionType.getId()
                    );
                    JSONObject js = this.missionDB.getDeptTransaction(transaction.getCode());
                    if (js == null) {
                        return transaction;
                    }
                    transaction.setTransaction_info(JsonUtils.DeSerialize(JsonUtils.Serialize(js), TransactionInfo.class));
                    break;
                }
                case HBTL: {
                    transaction.setAccumulate_type(
                            missionTransactionType.getId()
                    );
                    JSONObject js = this.missionDB.getHBTL(transaction.getOrderId());
                    if (js == null) {
                        return transaction;
                    }
                    transaction.setTransaction_info(JsonUtils.DeSerialize(JsonUtils.Serialize(js), TransactionInfo.class));
                    break;
                }
            }
            return transaction;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    private boolean filterMissionTransaction(MissionTransactionInfo x, String search, int type, int status, PromoTimeRequest timeRequest) {
        if (x == null) {
            return false;
        }

        if (((search.isEmpty() ||
                x.getCode().contains(search) ||
                x.getCode().toLowerCase().contains(search.toLowerCase())) &&
                (type == 0 ||
                        type == x.getAccumulate_type()) &&
                (status == 0 || x.getStatus() == status) &&
                (timeRequest == null ||
                        (
                                (timeRequest.getStart_date_millisecond() == 0 || x.getCreatedTime() >= timeRequest.getStart_date_millisecond()) &&
                                        (timeRequest.getEnd_date_millisecond() == 0 || x.getCreatedTime() <= timeRequest.getEnd_date_millisecond())
                        )
                )
        )
        ) {
            return true;
        }
        return false;
    }

    private MissionTransactionInfo parseMissionTransactionData(MissionTransactionData transactionData) {
        try {
            MissionTransactionInfo transaction = JsonUtils.DeSerialize(JsonUtils.Serialize(transactionData), MissionTransactionInfo.class);
            MissionTransactionType missionTransactionType = MissionTransactionType.fromKey(transaction.getType());
            switch (missionTransactionType) {
                case DON_HANG: {
                    transaction.setAccumulate_type(
                            missionTransactionType.getId()
                    );
                    JSONObject js = this.missionDB.getAgencyOrderDeptTransactionInfoByDeptCode(transaction.getCode());
                    if (js == null) {
                        return transaction;
                    }
                    transaction.setTransaction_info(JsonUtils.DeSerialize(JsonUtils.Serialize(js), TransactionInfo.class));
                    break;
                }
                case GIAM_CONG_NO: {
                    transaction.setAccumulate_type(
                            missionTransactionType.getId()
                    );
                    JSONObject js = this.missionDB.getDeptTransaction(transaction.getCode());
                    if (js == null) {
                        return transaction;
                    }
                    transaction.setTransaction_info(JsonUtils.DeSerialize(JsonUtils.Serialize(js), TransactionInfo.class));

                    break;
                }
                case TANG_CONG_NO: {
                    transaction.setAccumulate_type(
                            missionTransactionType.getId()
                    );
                    JSONObject js = this.missionDB.getDeptTransaction(transaction.getCode());
                    if (js == null) {
                        return transaction;
                    }
                    transaction.setTransaction_info(JsonUtils.DeSerialize(JsonUtils.Serialize(js), TransactionInfo.class));
                    break;
                }
                case HBTL: {
                    transaction.setAccumulate_type(
                            missionTransactionType.getId()
                    );
                    JSONObject js = this.missionDB.getHBTL(transaction.getOrderId());
                    if (js == null) {
                        return transaction;
                    }
                    transaction.setTransaction_info(JsonUtils.DeSerialize(JsonUtils.Serialize(js), TransactionInfo.class));
                    break;
                }
            }
            return transaction;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    public ClientResponse getMissionAgencyInfo(BasicRequest request) {
        try {
            JSONObject jsAgencyMission = this.missionDB.getAgencyMissionInfo(request.getId());
            if (jsAgencyMission == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            JSONObject mission = new JSONObject();
            mission.put("id", jsAgencyMission.get("id"));
            mission.put("mission_name",
                    this.convertMissionName(jsAgencyMission));
            mission.put("created_date", jsAgencyMission.get("created_date"));
            mission.put("claimed_date", jsAgencyMission.get("claimed_date"));
            mission.put("status", jsAgencyMission.get("status"));
            mission.put("mission_type_id", jsAgencyMission.get("mission_type_id"));

            AgencyBasicData agency_info = this.dataManager.getAgencyManager().getAgencyBasicData(ConvertUtils.toInt(jsAgencyMission.get("agency_id")));
            JSONObject data = new JSONObject();
            data.put("mission", mission);
            data.put("agency_info", agency_info);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse addMissionToGroup(SessionData sessionData, AddMissionToGroupRequest request) {
        try {
            ClientResponse crValidate = this.validateAddMissionToGroup(request);
            if (crValidate.failed()) {
                return crValidate;
            }

            int rsMissionGroup = request.getMission_group_id();

            JSONObject jsMissionGroup = this.missionDB.getMissionGroupInfo(request.getMission_group_id());
            if (jsMissionGroup == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Mission group is null");
            }

            for (MissionRequest missionRequest : request.getMissions()) {
                MissionType missionType = MissionType.from(missionRequest.getMission_type_id());
                if (missionType == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                switch (missionType) {
                    case MUA_HANG:
                        if (missionRequest.getTuan() != null) {
                            int rsMissionTuan = this.missionDB.createMission(
                                    missionRequest, missionRequest.getTuan(), rsMissionGroup, MissionPeriodType.TUAN.getId());
                            if (rsMissionTuan <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }

                        if (missionRequest.getThang() != null) {
                            int rsMissionThang = this.missionDB.createMission(
                                    missionRequest, missionRequest.getThang(), rsMissionGroup, MissionPeriodType.THANG.getId());
                            if (rsMissionThang <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }

                        if (missionRequest.getQuy() != null) {
                            int rsMissionQuy = this.missionDB.createMission(
                                    missionRequest, missionRequest.getQuy(), rsMissionGroup, MissionPeriodType.QUY.getId());
                            if (rsMissionQuy <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }
                        break;
                    case THANH_TOAN:
                        if (missionRequest.getTuan() != null) {
                            int rsMissionTuan = this.missionDB.createMission(
                                    missionRequest, missionRequest.getTuan(), rsMissionGroup, MissionPeriodType.TUAN.getId());
                            if (rsMissionTuan <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }

                        if (missionRequest.getThang() != null) {
                            int rsMissionThang = this.missionDB.createMission(
                                    missionRequest, missionRequest.getThang(), rsMissionGroup, MissionPeriodType.THANG.getId());
                            if (rsMissionThang <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }

                        if (missionRequest.getQuy() != null) {
                            int rsMissionQuy = this.missionDB.createMission(
                                    missionRequest, missionRequest.getQuy(), rsMissionGroup,
                                    MissionPeriodType.QUY.getId());
                            if (rsMissionQuy <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }
                        break;
                    case NQH: {
                        if (missionRequest.getTuan() != null ||
                                missionRequest.getThang() != null ||
                                missionRequest.getQuy() != null
                        ) {
                            if (missionRequest.getTuan() != null) {
                                int rsMissionTuan = this.missionDB.createMission(
                                        missionRequest,
                                        missionRequest.getTuan(),
                                        rsMissionGroup,
                                        MissionPeriodType.TUAN.getId());
                                if (rsMissionTuan <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }

                            if (missionRequest.getThang() != null) {
                                int rsMissionThang = this.missionDB.createMission(
                                        missionRequest,
                                        missionRequest.getThang(),
                                        rsMissionGroup,
                                        MissionPeriodType.THANG.getId());
                                if (rsMissionThang <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }

                            if (missionRequest.getQuy() != null) {
                                int rsMissionQuy = this.missionDB.createMission(
                                        missionRequest,
                                        missionRequest.getQuy(),
                                        request.getMission_group_id(),
                                        MissionPeriodType.QUY.getId());
                                if (rsMissionQuy <= 0) {
                                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                }
                            }
                        } else {
                            int rsMissionTuan = this.missionDB.createMission(
                                    missionRequest,
                                    missionRequest.getTuan(),
                                    rsMissionGroup,
                                    MissionPeriodType.TUAN.getId());
                            if (rsMissionTuan <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }

                            int rsMissionThang = this.missionDB.createMission(
                                    missionRequest,
                                    missionRequest.getThang(),
                                    rsMissionGroup,
                                    MissionPeriodType.THANG.getId());
                            if (rsMissionThang <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }

                            int rsMissionQuy = this.missionDB.createMission(
                                    missionRequest,
                                    missionRequest.getQuy(),
                                    request.getMission_group_id(),
                                    MissionPeriodType.QUY.getId());
                            if (rsMissionQuy <= 0) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }
                        break;
                    }
                }
            }

            JSONObject data = new JSONObject();
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse validateAddMissionToGroup(AddMissionToGroupRequest request) {
        try {
            if (request.getMissions() == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "MISSION: " + request.getMissions());
            }

            for (MissionRequest missionRequest : request.getMissions()) {
                ClientResponse crMission = missionRequest.validate();
                if (crMission.failed()) {
                    return crMission;
                }
            }

            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse validateMissionSyntax(MissionRequest missionRequest) {
        try {
            MissionType missionType = MissionType.from(missionRequest.getMission_type_id());
            if (missionType == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "mission_type");
            }
            List<Integer> numbers = JsonUtils.DeSerialize(
                    missionType.getData(),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            switch (missionType) {
                case THANH_TOAN:
                    for (Integer number : numbers) {
                        if (!missionRequest.getName().contains("[" + number + "]")) {
                            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Cú pháp " + missionRequest.getName());
                        }
                    }
                    break;
                case MUA_HANG:
                    for (Integer number : numbers) {
                        if (!missionRequest.getName().contains("[" + number + "]")) {
                            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Cú pháp " + missionRequest.getName());
                        }
                    }
                    break;
                case NQH:
                    if (missionRequest.getName().contains("[") ||
                            missionRequest.getName().contains("]")) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Cú pháp " + missionRequest.getName());
                    }
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editMission(SessionData sessionData, AddMissionToGroupRequest request) {
        try {
            ClientResponse crValidate = this.validateAddMissionToGroup(request);
            if (crValidate.failed()) {
                return crValidate;
            }

            int rsMissionGroup = request.getMission_group_id();

            JSONObject jsMissionGroup = this.missionDB.getMissionGroupInfo(request.getMission_group_id());
            if (jsMissionGroup == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Mission group is null");
            }

            for (MissionRequest missionRequest : request.getMissions()) {
                JSONObject jsMission = this.missionDB.getMission(missionRequest.getId());
                if (jsMission == null) {
                    return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Mission not found: " + JsonUtils.Serialize(missionRequest));
                }
            }

            for (MissionRequest missionRequest : request.getMissions()) {
                MissionType missionType = MissionType.from(missionRequest.getMission_type_id());
                if (missionType == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                switch (missionType) {
                    case MUA_HANG:
                        if (missionRequest.getTuan() != null) {
                            boolean rsMissionTuan = this.missionDB.editMission(
                                    missionRequest, missionRequest.getTuan(), rsMissionGroup, MissionPeriodType.TUAN.getId());
                            if (!rsMissionTuan) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }

                        if (missionRequest.getThang() != null) {
                            boolean rsMissionThang = this.missionDB.editMission(
                                    missionRequest, missionRequest.getThang(), rsMissionGroup, MissionPeriodType.THANG.getId());
                            if (!rsMissionThang) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }

                        if (missionRequest.getQuy() != null) {
                            boolean rsMissionQuy = this.missionDB.editMission(
                                    missionRequest, missionRequest.getQuy(), rsMissionGroup, MissionPeriodType.QUY.getId());
                            if (!rsMissionQuy) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }
                        break;
                    case THANH_TOAN:
                        if (missionRequest.getTuan() != null) {
                            boolean rsMissionTuan = this.missionDB.editMission(
                                    missionRequest, missionRequest.getTuan(), rsMissionGroup, MissionPeriodType.TUAN.getId());
                            if (!rsMissionTuan) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }

                        if (missionRequest.getThang() != null) {
                            boolean rsMissionThang = this.missionDB.editMission(
                                    missionRequest, missionRequest.getThang(), rsMissionGroup, MissionPeriodType.THANG.getId());
                            if (!rsMissionThang) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }

                        if (missionRequest.getQuy() != null) {
                            boolean rsMissionQuy = this.missionDB.editMission(
                                    missionRequest, missionRequest.getQuy(), rsMissionGroup,
                                    MissionPeriodType.QUY.getId());
                            if (!rsMissionQuy) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }
                        break;
                    case NQH:
                        if (missionRequest.getTuan() != null) {
                            boolean rsMissionTuan = this.missionDB.editMission(
                                    missionRequest,
                                    missionRequest.getTuan(),
                                    rsMissionGroup,
                                    MissionPeriodType.TUAN.getId());
                            if (!rsMissionTuan) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }

                        if (missionRequest.getThang() != null) {
                            boolean rsMissionThang = this.missionDB.editMission(
                                    missionRequest,
                                    missionRequest.getThang(),
                                    rsMissionGroup,
                                    MissionPeriodType.THANG.getId());
                            if (!rsMissionThang) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }

                        if (missionRequest.getQuy() != null) {
                            boolean rsMissionQuy = this.missionDB.editMission(
                                    missionRequest,
                                    missionRequest.getQuy(),
                                    request.getMission_group_id(),
                                    MissionPeriodType.QUY.getId());
                            if (!rsMissionQuy) {
                                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            }
                        }
                        break;
                }
            }

            JSONObject data = new JSONObject();
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getMissionConfig() {
        try {
            JSONObject data = new JSONObject();

            Map<String, String> missionConfigList = this.dataManager.getConfigManager().getMPMissionConfig();
            for (Map.Entry<String, String> entry : missionConfigList.entrySet()) {
                if (entry.getKey().equals(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN)) {
                    String[] time_data = entry.getValue().split("-");
                    JSONObject js = new JSONObject();
                    js.put("time", time_data[0]);
                    js.put("day_of_week", time_data[1]);
                    data.put(entry.getKey(), js);
                } else if (entry.getKey().equals(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN_KY_KE_TIEP)) {
                    String[] time_data = entry.getValue().split("-");
                    JSONObject js = new JSONObject();
                    js.put("time", time_data[0]);
                    js.put("day_of_week", time_data[1]);
                    data.put(entry.getKey(), js);
                } else if (entry.getKey().equals(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_THANG)) {
                    String[] time_data = entry.getValue().split("-");
                    data.put(entry.getKey(), time_data[0]);
                } else if (entry.getKey().equals(MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG)) {
                    String[] time_data = entry.getValue().split("-");
                    data.put(entry.getKey(), time_data[0]);
                } else if (entry.getKey().equals(MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG_KY_KE_TIEP)) {
                    String[] time_data = entry.getValue().split("-");
                    data.put(entry.getKey(), time_data[0]);
                } else if (entry.getKey().equals(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_QUY)) {
                    String[] time_data = entry.getValue().split("-");
                    data.put(entry.getKey(), time_data[0]);
                } else if (entry.getKey().equals(MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_QUY)) {
                    String[] time_data = entry.getValue().split("-");
                    data.put(entry.getKey(), time_data[0]);
                } else if (entry.getKey().equals(MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_QUY_KY_KE_TIEP)) {
                    String[] time_data = entry.getValue().split("-");
                    data.put(entry.getKey(), time_data[0]);
                } else {
                    data.put(entry.getKey(), entry.getValue());
                }
            }

            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse updateMissionConfig(SessionData sessionData, UpdateMissionConfigRequest request) {
        try {
            if (request.getData() == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DATA_INVALID);
            }

            Map<String, String> missionConfigList = this.dataManager.getConfigManager().getMPMissionConfig();
            for (Map.Entry<String, String> entry : missionConfigList.entrySet()) {
                if (MissionConstants.KY_NHIEM_VU.equals(entry.getKey())) {
                    MissionConfigData kyNhiemVu = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(entry.getKey())));
                    if (kyNhiemVu == null) {
                        return ResponseConstants.failed;
                    }
                    ClientResponse crValidateKyNhiemVu = kyNhiemVu.validateKyNhiemVu();
                    if (crValidateKyNhiemVu.failed()) {
                        return crValidateKyNhiemVu;
                    }
                } else if (MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN.equals(entry.getKey())) {
                    MissionConfigData thoigianketthucnhiemvutuan = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(entry.getKey())));
                    if (thoigianketthucnhiemvutuan == null) {
                        return ResponseConstants.failed;
                    }
                    ClientResponse crValidateThoiGianKetThucNhiemVuTuan = thoigianketthucnhiemvutuan.validateThoiGianKetThucNhiemVuTuan();
                    if (crValidateThoiGianKetThucNhiemVuTuan.failed()) {
                        return crValidateThoiGianKetThucNhiemVuTuan;
                    }
                } else if (MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG.equals(entry.getKey())) {
                    MissionConfigData thoigianketthucnhiemvuthang = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(entry.getKey())));
                    if (thoigianketthucnhiemvuthang == null) {
                        return ResponseConstants.failed;
                    }
                    ClientResponse crValidateThoiGianKetThucTichLuyNhiemVuThang = thoigianketthucnhiemvuthang.validateThoiGianKetThucTichLuyNhiemVuThang();
                    if (crValidateThoiGianKetThucTichLuyNhiemVuThang.failed()) {
                        return crValidateThoiGianKetThucTichLuyNhiemVuThang;
                    }
                } else if (MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_QUY.equals(entry.getKey())) {
                    MissionConfigData thoigianketthucnhiemvuquy = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(entry.getKey())));
                    if (thoigianketthucnhiemvuquy == null) {
                        return ResponseConstants.failed;
                    }
                    ClientResponse crValidateThoiGianKetThucTichLuyNhiemVuQuy = thoigianketthucnhiemvuquy.validateThoiGianKetThucTichLuyNhiemVuQuy();
                    if (crValidateThoiGianKetThucTichLuyNhiemVuQuy.failed()) {
                        return crValidateThoiGianKetThucTichLuyNhiemVuQuy;
                    }
                } else if (MissionConstants.UU_DAI_TUAN.equals(entry.getKey())) {
                    MissionConfigData uuDaiTuan = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(entry.getKey())));
                    if (uuDaiTuan == null) {
                        return ResponseConstants.failed;
                    }
                    ClientResponse crValidateUuDaiTuan = uuDaiTuan.validateUuDaiTuan();
                    if (crValidateUuDaiTuan.failed()) {
                        return crValidateUuDaiTuan;
                    }
                } else if (MissionConstants.UU_DAI_THANG.equals(entry.getKey())) {
                    MissionConfigData uuDaiThang = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(entry.getKey())));
                    if (uuDaiThang == null) {
                        return ResponseConstants.failed;
                    }
                    ClientResponse crValidateUuDaiThang = uuDaiThang.validateUuDaiThang();
                    if (crValidateUuDaiThang.failed()) {
                        return crValidateUuDaiThang;
                    }
                } else if (MissionConstants.UU_DAI_QUY.equals(entry.getKey())) {
                    MissionConfigData uuDaiQuy = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(entry.getKey())));
                    if (uuDaiQuy == null) {
                        return ResponseConstants.failed;
                    }
                    ClientResponse crValidateUuDaiQuy = uuDaiQuy.validateUuDaiQuy();
                    if (crValidateUuDaiQuy.failed()) {
                        return crValidateUuDaiQuy;
                    }
                } else if (MissionConstants.SO_HUY_HIEU_DOI_TUAN.equals(entry.getKey())) {
                    MissionConfigData soHuyHieuDoiTuan = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(entry.getKey())));
                    if (soHuyHieuDoiTuan == null) {
                        return ResponseConstants.failed;
                    }
                    ClientResponse crValidateSoHuyHieuDoiTuan = soHuyHieuDoiTuan.validateSoHuyHieuDoiTuan();
                    if (crValidateSoHuyHieuDoiTuan.failed()) {
                        return crValidateSoHuyHieuDoiTuan;
                    }
                } else if (MissionConstants.SO_HUY_HIEU_DOI_THANG.equals(entry.getKey())) {
                    MissionConfigData soHuyHieuDoiThang = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(entry.getKey())));
                    if (soHuyHieuDoiThang == null) {
                        return ResponseConstants.failed;
                    }
                    ClientResponse crValidateSoHuyHieuDoiThang = soHuyHieuDoiThang.validateSoHuyHieuDoiThang();
                    if (crValidateSoHuyHieuDoiThang.failed()) {
                        return crValidateSoHuyHieuDoiThang;
                    }
                } else if (MissionConstants.SO_HUY_HIEU_DOI_QUY.equals(entry.getKey())) {
                    MissionConfigData soHuyHieuDoiQuy = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(entry.getKey())));
                    if (soHuyHieuDoiQuy == null) {
                        return ResponseConstants.failed;
                    }
                    ClientResponse crValidateSoHuyHieuDoiQuy = soHuyHieuDoiQuy.validateSoHuyHieuDoiQuy();
                    if (crValidateSoHuyHieuDoiQuy.failed()) {
                        return crValidateSoHuyHieuDoiQuy;
                    }
                }
            }


            for (Map.Entry<String, String> entry : missionConfigList.entrySet()) {
                if (MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN.equals(entry.getKey())) {
                    JSONObject js = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())),
                            JSONObject.class);
                    if (js != null) {
                        boolean rsUpdate = this.missionDB.updateMissionConfig(
                                MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN_KY_KE_TIEP,
                                js.get("time").toString() + "-" + js
                                        .get("day_of_week").toString());
                        if (!rsUpdate) {
                            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, entry.getKey() + " " + request.getData().get(entry.getKey()));
                        }
                    } else {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, entry.getKey() + " " + request.getData().get(entry.getKey()));
                    }
                } else if (MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG.equals(entry.getKey())) {
                    JSONObject js = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())),
                            JSONObject.class);
                    String time_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_THANG);
                    int day_of_month = 0;
                    if (time_data != null && !time_data.isEmpty()) {
                        day_of_month = ConvertUtils.toInt(time_data.split("-")[1]);
                    }
                    boolean rsUpdate = this.missionDB.updateMissionConfig(
                            MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG_KY_KE_TIEP,
                            js.get("time") + "-" + day_of_month);
                    if (!rsUpdate) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, entry.getKey() + " " + request.getData().get(entry.getKey()));
                    }
                } else if (MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_QUY.equals(entry.getKey())) {
                    JSONObject js = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())),
                            JSONObject.class);
                    String time_data = this.dataManager.getConfigManager().getMPMissionConfig().get(
                            entry.getKey());
                    int month_of_week = 0;
                    if (time_data != null && !time_data.isEmpty()) {
                        month_of_week = ConvertUtils.toInt(time_data.split("-")[1]);
                    }
                    boolean rsUpdate = this.missionDB.updateMissionConfig(
                            MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_QUY_KY_KE_TIEP,
                            js.get("time") + "-" + month_of_week);
                    if (!rsUpdate) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, entry.getKey() + " " + request.getData().get(entry.getKey()));
                    }
                } else if (MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG.equals(entry.getKey())) {
                    MissionConfigData missionConfigData = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())));
                    boolean rsUpdate = this.missionDB.updateMissionConfig(
                            MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG_KY_KE_TIEP,
                            missionConfigData.getTime());
                    if (YesNoStatus.YES.getValue() == missionConfigData.getIs_effect_now()) {
                        this.missionDB.updateMissionConfig(
                                MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG,
                                missionConfigData.getTime());

                        this.missionDB.resetEstimateAgencyOrderAutoTichLuy();
                    }
                } else if (MissionConstants.KY_NHIEM_VU.equals(entry.getKey())) {
                    MissionConfigData missionConfigData = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())));
                    boolean rsUpdate = this.missionDB.updateMissionConfig(
                            MissionConstants.KY_NHIEM_VU_KY_KE_TIEP,
                            missionConfigData.getData());
                    if (!rsUpdate) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, entry.getKey() + " " + request.getData().get(entry.getKey()));
                    }
                } else if (MissionConstants.MO_TA_NHIEM_VU.equals(entry.getKey())) {
                    MissionConfigData missionConfigData = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())));
                    this.missionDB.updateMissionConfig(
                            MissionConstants.MO_TA_NHIEM_VU_KY_KE_TIEP, missionConfigData.getData());
                    if (YesNoStatus.YES.getValue() == missionConfigData.getIs_effect_now()) {
                        this.missionDB.updateMissionConfig(
                                MissionConstants.MO_TA_NHIEM_VU, missionConfigData.getData());
                    }
                } else if (MissionConstants.UU_DAI_TUAN.equals(entry.getKey())) {
                    MissionConfigData missionConfigData = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())));
                    this.missionDB.updateMissionConfig(
                            MissionConstants.UU_DAI_TUAN_KY_KE_TIEP, missionConfigData.getData());
                } else if (MissionConstants.UU_DAI_THANG.equals(entry.getKey())) {
                    MissionConfigData missionConfigData = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())));
                    this.missionDB.updateMissionConfig(
                            MissionConstants.UU_DAI_THANG_KY_KE_TIEP, missionConfigData.getData());
                } else if (MissionConstants.UU_DAI_QUY.equals(entry.getKey())) {
                    MissionConfigData missionConfigData = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())));
                    this.missionDB.updateMissionConfig(
                            MissionConstants.UU_DAI_QUY_KY_KE_TIEP, missionConfigData.getData());
                } else if (MissionConstants.SO_HUY_HIEU_DOI_TUAN.equals(entry.getKey())) {
                    MissionConfigData missionConfigData = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())));
                    this.missionDB.updateMissionConfig(
                            MissionConstants.SO_HUY_HIEU_DOI_TUAN_KY_KE_TIEP, missionConfigData.getData());
                } else if (MissionConstants.SO_HUY_HIEU_DOI_THANG.equals(entry.getKey())) {
                    MissionConfigData missionConfigData = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())));
                    this.missionDB.updateMissionConfig(
                            MissionConstants.SO_HUY_HIEU_DOI_THANG_KY_KE_TIEP, missionConfigData.getData());
                } else if (MissionConstants.SO_HUY_HIEU_DOI_QUY.equals(entry.getKey())) {
                    MissionConfigData missionConfigData = MissionConfigData.from(
                            entry.getKey(),
                            JsonUtils.Serialize(request.getData().get(
                                    entry.getKey())));
                    this.missionDB.updateMissionConfig(
                            MissionConstants.SO_HUY_HIEU_DOI_QUY_KY_KE_TIEP, missionConfigData.getData());
                }
            }

            this.missionLogDB.saveMissionConfigHistory(sessionData, request);

            this.dataManager.getConfigManager().loadMissionConfig();

            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e
                    .getMessage());
        }
    }

    public ClientResponse runGenerateAssignMissionTuan() {
        try {
            List<Integer> mission_period_running_list = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            if (mission_period_running_list == null || mission_period_running_list.isEmpty()) {
                return ResponseConstants.success;
            }

            JSONObject data = new JSONObject();
            int ky_dai = Collections.max(mission_period_running_list);
            if (MissionPeriodType.THANG.getId() == ky_dai) {
                /**
                 * Nếu đầu tháng
                 */
                if (this.checkGenerateMissionThang()) {
//                    this.generateAll(mission_period_running_list);
//                    data.put("mission_period_running_list", mission_period_running_list);
                } else if (this.checkGenerateMissionTuan()) {
                    this.resetMissionConfigKyTuan();

                    this.generateAll(Arrays.asList(MissionPeriodType.TUAN.getId()));
                    data.put("mission_period_running_list", Arrays.asList(MissionPeriodType.TUAN.getId()));
                }
            } else if (MissionPeriodType.QUY.getId() == ky_dai) {
                /**
                 * Nếu đầu quý
                 */
                if (this.checkGenerateMissionQuy()) {
                    //bỏ qua
                } else if (this.checkGenerateMissionThang()) {
                    this.resetMissionConfigKyThang();

                    this.generateAll(Arrays.asList(MissionPeriodType.THANG.getId()));
                    data.put("mission_period_running_list", Arrays.asList(MissionPeriodType.THANG.getId()));
                }
            }
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runGenerateAssignMissionThang() {
        try {
            List<Integer> mission_period_running_list = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU),
                    new TypeToken<List<Integer>>() {
                    }.getType());

            JSONObject data = new JSONObject();
            int ky_dai = Collections.max(mission_period_running_list);
            if (MissionPeriodType.THANG.getId() == ky_dai) {
                /**
                 * Nếu đầu tháng
                 */
                if (this.checkGenerateMissionThang()) {
                    this.resetMissionSettingAgencyData();
                    this.generateAll(mission_period_running_list);
                    data.put("mission_period_running_list", mission_period_running_list);
                }
            } else if (MissionPeriodType.QUY.getId() == ky_dai) {
                /**
                 * Nếu đầu quý
                 */
                if (this.checkGenerateMissionQuy()) {
                    this.generateAll(mission_period_running_list);
                    data.put("mission_period_running_list", mission_period_running_list);
                } else if (this.checkGenerateMissionThang()) {
                    this.resetMissionConfigKyThang();

                    this.generateAll(Arrays.asList(MissionPeriodType.THANG.getId()));
                    data.put("mission_period_running_list", Arrays.asList(MissionPeriodType.THANG.getId()));
                }
            }

            this.reportToTelegram("RESET_MISSION", ResponseStatus.SUCCESS);
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
            this.reportToTelegram("RESET_MISSION", ResponseStatus.FAIL);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void resetMissionSettingAgencyData() {
        try {
            List<JSONObject> missionSettingRunningList = this.missionDB.getListMissionSettingRunning();
            missionSettingRunningList.forEach(
                    missionSettingRunning -> {
                        int mission_setting_id = ConvertUtils.toInt(missionSettingRunning.get("id"));
                        List<Agency> agencyJoinList = this.getListAgencyByFilter(
                                JsonUtils.Serialize(
                                        this.convertMissionSettingDataToPromoData(
                                                missionSettingRunning)));

                        List<Integer> agencyIdList = agencyJoinList.stream().map(
                                e -> e.getId()
                        ).collect(Collectors.toList());

                        this.missionDB.deleteMissionSettingAgencyJoin(mission_setting_id);

                        for (Agency agency : agencyJoinList) {
                            this.missionDB.insertMissionSettingAgencyJoin(agency.getId(), mission_setting_id);
                        }

                        boolean rsInsertMissionSettingAgency = this.missionDB.updateAgencyDataForMissionSettingAgency(
                                mission_setting_id,
                                JsonUtils.Serialize(agencyIdList)
                        );
                        if (!rsInsertMissionSettingAgency) {
                            this.alertToTelegram("saveMissionAgencySetting: " + mission_setting_id + "-" + JsonUtils.Serialize(agencyIdList), ResponseStatus.EXCEPTION);
                        }
                    }
            );
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }


    private ClientResponse generateMissionThang() {
        return ClientResponse.success(null);
    }

    private ClientResponse generateMissionTuan() {
        try {
            /**
             * Tạo nhiệm vụ tuần
             */
            this.generateAll(Arrays.asList(MissionPeriodType.TUAN.getId()));

            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkGenerateMissionTuan() {
        try {
            String time_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN);
            String[] tuan_config = time_data.split("-");
            if (tuan_config.length != 2) {
                return false;
            }
            Date now = new Date();
            int dayOfWeek = this.appUtils.getDateOfWeek(now);
            int dayOfWeekConfig = ConvertUtils.toInt(tuan_config[1]);
            if (dayOfWeek != dayOfWeekConfig) {
                return false;
            }
            String time_current = DateTimeUtils.toString(now, "HH:mm");
            if (!time_current.equals(tuan_config[0])) {
                return false;
            }

            return true;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return false;
    }

    private boolean checkGenerateMissionThang() {
        try {
            String time_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_BAT_DAU_NHIEM_VU_THANG);
            String[] thang_config = time_data.split("-");
            if (thang_config.length != 2) {
                return false;
            }
            Date now = new Date();
            int dayOfMonth = ConvertUtils.toInt(this.appUtils.getDay(now));
            int dayConfig = ConvertUtils.toInt(thang_config[1]);
            if (dayOfMonth != dayConfig) {
                return false;
            }
            String time_current = DateTimeUtils.toString(now, "HH:mm");
            if (!time_current.equals(thang_config[0])) {
                return false;
            }

            return true;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return false;
    }

    private boolean checkGenerateMissionQuy() {
        try {
            String time_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_BAT_DAU_NHIEM_VU_QUY);
            String[] config = time_data.split("-");
            if (config.length != 3) {
                return false;
            }
            Date now = new Date();


            String day_start = null;
            String day_end = null;
            if (ConvertUtils.toInt(config[1]) == 0) {
                int quy = this.getQuy(now);
                day_start = this.getNgayBatDauQuy(quy);

            } else {
                return false;
            }

            String day_current = DateTimeUtils.toString(now, "MM-dd");
            if (day_start.equals(day_current)) {
                return true;
            }

            String time_current = DateTimeUtils.toString(now, "HH:mm");
            if (!time_current.equals(config[0])) {
                return false;
            }

            return true;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return false;
    }

    private String getNgayBatDauQuy(int quy) {
        switch (quy) {
            case 1:
                return "01-01";
            case 2:
                return "04-01";
            case 3:
                return "07-01";
            case 4:
                return "10-01";
        }
        return null;
    }

    private String getNgayKetThucQuy(int quy) {
        switch (quy) {
            case 1:
                return "31-03";
            case 2:
                return "30-06";
            case 3:
                return "30-09";
            case 4:
                return "31-12";
        }
        return null;
    }


    private ClientResponse generateMissionQuy() {
        try {
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runStartMissionSetting() {
        try {
            SessionData sessionData = new SessionData();
            List<JSONObject> missionBXHList = this.missionDB.getListMissionSettingNeedStart();
            for (JSONObject js : missionBXHList) {
                BasicRequest basicRequest = new BasicRequest();
                basicRequest.setId(ConvertUtils.toInt(js.get("id")));
                ClientResponse crStartMissionSetting = this.startMissionSetting(sessionData, basicRequest);
                if (crStartMissionSetting.failed()) {
                    this.alertToTelegram("crStartMissionSetting: " + JsonUtils.Serialize(crStartMissionSetting), ResponseStatus.EXCEPTION);
                }
            }
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse startMissionSetting(SessionData sessionData, BasicRequest request) {
        try {
            boolean rsStart = this.missionDB.startMissionSetting(request.getId());
            if (!rsStart) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.saveMissionPeriodTime();

            /**
             * Lưu danh sách đại lý thuộc đối tượng
             */
            this.deleteMissionSettingAgencyJoin(request.getId());


            List<Agency> agencyJoinList = this.saveMissionSettingAgencyJoin(request.getId());

            JSONObject jsMissionSetting = this.missionDB.getMissionSettingInfo(request.getId());

            /**
             * Lưu danh sách đại lý cho App
             */
            this.saveMissionAgencySetting(
                    request.getId(),
                    DateTimeUtils.getDateTime(jsMissionSetting.get("start_date").toString(), "yyyy-MM-dd HH:mm:ss"),
                    agencyJoinList);


            this.missionDB.insertMissionSettingHistory(
                    request.getId(),
                    jsMissionSetting,
                    "",
                    sessionData.getId(),
                    DataStatus.ACTIVE.getValue());

            this.generateMissionBySetting(request.getId(), jsMissionSetting, agencyJoinList);

            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void saveMissionAgencySetting(int mission_setting_id, Date start_date, List<Agency> agencyJoinList) {
        try {
            List<Integer> agencyIdList = agencyJoinList.stream().map(
                    e -> e.getId()
            ).collect(Collectors.toList());
            List<Integer> missionPeriodRunningList = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            for (Integer missionPeriodRunning : missionPeriodRunningList) {
                int rsInsertMissionSettingAgency = this.missionDB.insertMissionSettingAgency(
                        mission_setting_id,
                        missionPeriodRunning,
                        JsonUtils.Serialize(agencyIdList),
                        start_date,
                        null
                );
                if (rsInsertMissionSettingAgency <= 0) {
                    this.alertToTelegram("saveMissionAgencySetting: " + mission_setting_id + "-" + JsonUtils.Serialize(agencyIdList), ResponseStatus.EXCEPTION);
                }
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    private void generateMissionBySetting(int mission_setting_id, JSONObject jsMissionSetting, List<Agency> agencyJoinList) {
        try {
            if (jsMissionSetting == null) {
                this.alertToTelegram("generateMission: " + mission_setting_id, ResponseStatus.EXCEPTION);
            }

            Date start_date = DateTimeUtils.getNow();

            List<Integer> missionPeriodRunningList = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(
                            MissionConstants.KY_NHIEM_VU),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            List<JSONObject> data = new ArrayList<>();
            String time_tuan_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN).toString();
            String time_thang_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_THANG).toString();
            String time_tl_thang_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG).toString();
            String time_tl_quy_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_QUY).toString();
            missionPeriodRunningList.forEach(
                    mp -> {
                        MissionPeriodType missionPeriodType = MissionPeriodType.from(mp);
                        Date end_date = this.getMissionPeriodEndTime(missionPeriodType, start_date);
                        String mission_period_code = this.generateMissionPeriodCode(
                                missionPeriodType,
                                start_date,
                                end_date);
                        agencyJoinList.forEach(
                                agency -> {
                                    List<JSONObject> AgencyMissionList = this.missionDB.getListAgencyMission(
                                            agency.getId(),
                                            mp.intValue());
                                    if (AgencyMissionList.isEmpty()) {
                                        int rsInsertMissionPeriodAgency = this.missionDB.insertMissionPeriodAgency(
                                                agency.getId(), missionPeriodType.getId(),
                                                start_date,
                                                end_date,
                                                mission_period_code,
                                                mission_setting_id
                                        );
                                        if (rsInsertMissionPeriodAgency <= 0) {
                                            this.alertToTelegram("insertMissionPeriodAgency:" +
                                                    agency.getId() + "," +
                                                    missionPeriodType.getId() + "," +
                                                    start_date + "," +
                                                    end_date + "," +
                                                    mission_period_code + "," +
                                                    mission_setting_id, ResponseStatus.EXCEPTION);
                                        }
                                    }
                                }
                        );

                        switch (missionPeriodType) {
                            case TUAN: {
                                JSONObject jsPeriod = new JSONObject();
                                jsPeriod.put("id", mission_setting_id);
                                jsPeriod.put("mission_period_id", missionPeriodType.getId());
                                jsPeriod.put("type", missionPeriodType.getKey());
                                jsPeriod.put("start_time", start_date.getTime());
                                jsPeriod.put("end_time",
                                        this.appUtils.getMissionPeriodEndTimeOfTuan(
                                                time_tuan_data,
                                                start_date, time_thang_data).getTime());
                                jsPeriod.put("end_tl_time",
                                        this.appUtils.getMissionPeriodEndTimeOfTuan(
                                                time_tuan_data,
                                                start_date, time_thang_data).getTime());
                                data.add(jsPeriod);
                                break;
                            }
                            case THANG: {
                                JSONObject jsPeriod = new JSONObject();
                                jsPeriod.put("id", mission_setting_id);
                                jsPeriod.put("mission_period_id", missionPeriodType.getId());
                                jsPeriod.put("type", missionPeriodType.getKey());
                                jsPeriod.put("start_time", start_date.getTime());
                                jsPeriod.put("end_time",
                                        this.appUtils.getMissionPeriodEndTimeOfThang(
                                                time_tuan_data,
                                                start_date, time_thang_data).getTime());
                                jsPeriod.put("end_tl_time",
                                        this.appUtils.getMissionPeriodEndTLTimeOfThang(
                                                time_tuan_data,
                                                start_date, time_tl_thang_data).getTime());
                                data.add(jsPeriod);
                                break;
                            }
                            case QUY:
                                JSONObject jsPeriod = new JSONObject();
                                jsPeriod.put("id", mission_setting_id);
                                jsPeriod.put("mission_period_id", missionPeriodType.getId());
                                jsPeriod.put("type", missionPeriodType.getKey());
                                jsPeriod.put("start_time", start_date.getTime());
                                jsPeriod.put("end_time",
                                        DateTimeUtils.getDateTime(
                                                this.appUtils.getYear(start_date) + " -" +
                                                        this.getNgayKetThucQuy(this.getQuy(start_date)) + " 23:59:59",
                                                "yyyy-MM-dd HH:mm:ss").getTime());
                                jsPeriod.put("end_tl_time",
                                        DateTimeUtils.getDateTime(
                                                this.appUtils.getYear(start_date) + " -" +
                                                        this.getNgayKetThucQuy(this.getQuy(start_date)) + " "
                                                        + time_tl_quy_data.split("-")[0] + ":00",
                                                "yyyy-MM-dd HH:mm:ss").getTime());
                                data.add(jsPeriod);
                                break;
                            default:
                                this.alertToTelegram("generateAll: " + mp, ResponseStatus.EXCEPTION);
                        }
                    }
            );

            ClientResponse clientResponse = this.assignMissionService.assignMissionBySetting(
                    JsonUtils.Serialize(data)
            );
            if (clientResponse.failed()) {
                this.alertToTelegram(clientResponse.getMessage(), ResponseStatus.EXCEPTION);
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    private String generateMissionPeriodCode(MissionPeriodType missionPeriodType, Date start_date, Date end_date) {
        if (start_date != null && end_date != null) {
            return DateTimeUtils.toString(start_date, "yyyy/MM/dd HH:mm:ss") + "-" +
                    DateTimeUtils.toString(end_date, "yyyy/MM/dd HH:mm:ss");
        }
        int day_of_week = this.appUtils.getDateOfWeek(start_date);
        String month = this.appUtils.getMonth(start_date);
        String year = this.appUtils.getYear(start_date);
        int quy = this.getQuy(start_date);
        switch (missionPeriodType) {
            case TUAN:
                return "TUAN" + day_of_week + "THANG" + month + "QUY" + quy + "NAM" + year;
            case THANG:
                return "THANG" + month + "QUY" + quy + "NAM" + year;
            case QUY:
                return "QUY" + quy + "NAM" + year;
        }
        return missionPeriodType.name();
    }

    public int getQuy(Date date) {
        try {
            int month = date.getMonth() + 1;

            int quy = month / 3;
            if (month % 3 > 0) {
                quy += 1;
            }

            return quy;

        } catch (Exception e) {
            LogUtil.printDebug(Module.MISSION.name(), e);
        }
        return 0;
    }

    private Date getMissionPeriodEndTime(MissionPeriodType missionPeriodType, Date start_date) {
        switch (missionPeriodType) {
            case TUAN:
                return this.appUtils.getMissionPeriodEndTimeOfTuan(
                        this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN),
                        start_date,
                        this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_THANG)
                );
        }
        return null;
    }

    public ClientResponse stopMissionSetting(SessionData sessionData, StopPromoRequest request) {
        try {
            JSONObject missionSetting = this.missionDB.getMissionSettingInfo(request.getId());
            if (missionSetting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int status = ConvertUtils.toInt(missionSetting.get("status"));
            if (MissionSettingStatus.RUNNING.getId() != status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            PromoStopType promoStopType = PromoStopType.from(request.getStop_type());
            if (promoStopType == PromoStopType.STOP_NOW) {
                boolean rsStop = this.missionDB.stopMissionSetting(request.getId(), request.getNote());
                if (!rsStop) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                boolean rsStopMissionSettingAgency = this.missionDB.stopMissionSettingAgency(request.getId());
                if (!rsStopMissionSettingAgency) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                this.missionDB.insertMissionSettingHistory(
                        request.getId(),
                        this.missionDB.getMissionSettingInfo(request.getId()),
                        "",
                        sessionData.getId(),
                        DataStatus.ACTIVE.getValue());
            } else {
                boolean rs = this.missionDB.saveMissionSettingEndDate(
                        request.getId(), request.getStop_time(),
                        request.getNote());
                this.missionDB.insertMissionSettingHistory(
                        request.getId(),
                        this.missionDB.getMissionSettingInfo(request.getId()),
                        "Hẹn kết thúc: " + request.getStop_time(),
                        sessionData.getId(),
                        DataStatus.ACTIVE.getValue());
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse doubleCheckMissionSetting(CreateMissionSettingRequest request) {
        try {
            ClientResponse crValidateMissionSetting = this.validateCreateMissionSetting(request);
            if (crValidateMissionSetting.failed()) {
                return crValidateMissionSetting;
            }
            JSONObject jsMissionSetting = new JSONObject();
            jsMissionSetting.put("apply_object_data", JsonUtils.Serialize(request.getApply_object()));
            String mission_setting_data = JsonUtils.Serialize(
                    this.convertMissionSettingDataToPromoData(jsMissionSetting));
            List<Agency> agencyList = this.getListAgencyByFilter(mission_setting_data);
            List<JSONObject> records = new ArrayList<>();
            for (Agency agency : agencyList) {
                List<JSONObject> missionSettingList = this.missionDB.getListMissionSettingByAgency(
                        agency.getId());
                if (missionSettingList.size() > 0) {
                    JSONObject jsAgency = new JSONObject();
                    jsAgency.put("id", agency.getId());
                    jsAgency.put("shop_name", agency.getShop_name());
                    jsAgency.put("code", agency.getCode());
                    jsAgency.put("membership_id", agency.getMembershipId());
                    jsAgency.put("avatar", agency.getAvatar());
                    jsAgency.put("promos", missionSettingList);
                    records.add(jsAgency);
                }
            }
            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runGioHanhChinh() {
        try {
            if (ConfigInfo.READY) {
                Date now = new Date();
                if (this.checkGioHanhChinh(DateTimeUtils.getDateTime(now, "HH:mm"))) {
                    List<JSONObject> orders = this.missionDB.getListOrderPrepareOver(
                            DateTimeUtils.toString(now, "yyyy-MM-dd HH:mm:ss")
                    );
                    for (JSONObject order : orders) {
                        Date confirm_prepare_date = DateTimeUtils.getDateTime(
                                order.get("confirm_prepare_date").toString(),
                                "yyyy-MM-dd HH:mm:ss"
                        );
                        if (now.getTime() - confirm_prepare_date.getTime() < 3 * 60 * 60 * 1000) {
                            continue;
                        }
                        this.alertToTelegram(order.get("code").toString(), ResponseStatus.EXCEPTION);
                    }
                }

            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkGioHanhChinh(Date date) {
        int day_of_week = this.appUtils.getDateOfWeek(date);
        TimeToWorkData timeToWorkData = this.dataManager.getConfigManager().getGioHanhChinhByDayOfWeek(day_of_week);
        if (timeToWorkData == null) {
            return false;
        }
        if ((timeToWorkData.getMorning().getDate_from().getTime() <= date.getTime() &&
                timeToWorkData.getMorning().getDate_to().getTime() >= date.getTime()) ||
                (timeToWorkData.getAfternoon().getDate_from().getTime() <= date.getTime() &&
                        timeToWorkData.getAfternoon().getDate_to().getTime() >= date.getTime())) {
            return true;
        }
        return false;
    }

    public ClientResponse filterMissionTuanByAgency(SessionData sessionData, FilterListRequest request) {
        try {
            FilterRequest filterAgency = new FilterRequest();
            filterAgency.setKey("agency_id");
            filterAgency.setType(TypeFilter.SELECTBOX);
            filterAgency.setValue(ConvertUtils.toString(request.getAgency_id()));
            request.getFilters().add(filterAgency);

            FilterRequest filterPeriod = new FilterRequest();
            filterPeriod.setKey("mission_period_id");
            filterPeriod.setType(TypeFilter.SELECTBOX);
            filterPeriod.setValue(ConvertUtils.toString(MissionPeriodType.TUAN.getId()));
            request.getFilters().add(filterPeriod);
            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_AGENCY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("mission_name", this.convertMissionName(js));
                js.put("completed", this.checkMissionComplete(js));
            }
            int total = this.missionDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkMissionComplete(JSONObject js) {
        try {
            int mission_type = ConvertUtils.toInt(js.get("mission_type_id"));
            int currentActionNumber = ConvertUtils.toInt(js.get("mission_current_action_number"));
            int actionNumber = ConvertUtils.toInt(js.get("mission_action_number"));
            long currentValue = ConvertUtils.toLong(js.get("mission_current_value"));
            long requiredValue = ConvertUtils.toLong(js.get("mission_required_value"));
            int actionStatus = ConvertUtils.toInt(js.get("mission_action_status"));
            if (mission_type == MissionType.NQH.getId()) {
                Date today = new Date();
                Date endDate = DateTimeUtils.getDateTime(js.get("mission_end_date").toString(), "yyyy-MM-dd HH:mm:ss");
                Date allowClaimDate = DateTimeUtils.getDateTime(js.get("mission_allow_claim_date").toString(), "yyyy-MM-dd HH:mm:ss");
                if ((today.after(allowClaimDate) || today.equals(allowClaimDate))
                        && (today.before(endDate) || today.equals(endDate))
                        && (currentActionNumber == 0 && currentValue == 0))
                    return true;
            } else if (mission_type == MissionType.MUA_HANG.getId() && actionNumber > 0 && actionStatus == 1
                    && currentActionNumber > 0 && currentActionNumber <= actionNumber
                    && currentValue >= requiredValue) {
                return true;
            } else
                return (currentActionNumber >= actionNumber && currentValue >= requiredValue);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return false;
    }

    private String convertMissionName(JSONObject js) {
        try {
            if (js == null) {
                return "";
            }

            String mission_name = ConvertUtils.toString(js.get("mission_name"));

            MissionType missionType = MissionType.from(ConvertUtils.toInt(js.get("mission_type_id")));
            switch (missionType) {
                case THANH_TOAN: {
                    long gia_tri_dang_thuc_hien = ConvertUtils.toLong(js.get("mission_current_value"));
                    long gia_tri_yeu_cau = ConvertUtils.toLong(js.get("mission_required_value"));

                    mission_name = mission_name.replace("[" + 6 + "]", this.appUtils.numberFormat(gia_tri_dang_thuc_hien));
                    mission_name = mission_name.replace("[" + 3 + "]", this.appUtils.numberFormat(gia_tri_yeu_cau));
                    return mission_name;
                }
                case MUA_HANG: {
                    long gia_tri_dang_thuc_hien = ConvertUtils.toLong(js.get("mission_current_value"));
                    long gia_tri_yeu_cau = ConvertUtils.toLong(js.get("mission_required_value"));
                    long so_lan_dang_thuc_hien = ConvertUtils.toLong(js.get("mission_current_action_number"));
                    long so_lan_thuc_hien = ConvertUtils.toLong(js.get("mission_action_number"));
                    long tong_cac_lan = ConvertUtils.toInt(js.get("action_status"));
                    mission_name = mission_name.replace("[" + 5 + "]", this.appUtils.numberFormat(so_lan_dang_thuc_hien));
                    mission_name = mission_name.replace("[" + 2 + "]", this.appUtils.numberFormat(so_lan_thuc_hien));
                    int mission_unit_id = ConvertUtils.toInt(js.get("mission_unit_id"));
                    mission_name = mission_name.replace("[" + 6 + "]", this.appUtils.numberFormat(gia_tri_dang_thuc_hien));
                    mission_name = mission_name.replace("[" + 3 + "]", this.appUtils.numberFormat(gia_tri_yeu_cau));
                    mission_name = mission_name.replace("[" + 4 + "]", MissionUnitType.from(mission_unit_id).getLabel());
                    return mission_name;
                }
                case NQH: {
                    return mission_name;
                }
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return "";
    }

    public ClientResponse filterMissionThangByAgency(SessionData sessionData, FilterListRequest request) {
        try {
            FilterRequest filterAgency = new FilterRequest();
            filterAgency.setKey("agency_id");
            filterAgency.setType(TypeFilter.SELECTBOX);
            filterAgency.setValue(ConvertUtils.toString(request.getAgency_id()));
            request.getFilters().add(filterAgency);

            FilterRequest filterPeriod = new FilterRequest();
            filterPeriod.setKey("mission_period_id");
            filterPeriod.setType(TypeFilter.SELECTBOX);
            filterPeriod.setValue(ConvertUtils.toString(MissionPeriodType.THANG.getId()));
            request.getFilters().add(filterPeriod);
            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_AGENCY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("mission_name", this.convertMissionName(js));
                js.put("completed", this.checkMissionComplete(js));
            }
            int total = this.missionDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterMissionReplaceByAgency(SessionData sessionData, FilterListRequest request) {
        try {
            FilterRequest filterAgency = new FilterRequest();
            filterAgency.setKey("agency_id");
            filterAgency.setType(TypeFilter.SELECTBOX);
            filterAgency.setValue(ConvertUtils.toString(request.getAgency_id()));
            request.getFilters().add(filterAgency);

            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_AGENCY_REPLACE, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("mission_name", this.convertMissionName(js));
                js.put("mission_change_date",
                        this.getMissionChange(ConvertUtils.toInt(js.get("mission_changed_id"))));
            }
            int total = this.missionDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private Object getMissionChange(int mission_changed_id) {
        try {
            JSONObject js = this.missionDB.getAgencyMissionInfo(mission_changed_id);
            if (js != null) {
                return js.get("created_date");
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    public ClientResponse getMissionInfo(BasicRequest request) {
        try {
            JSONObject mission = this.missionDB.getMission(request.getId());
            if (mission == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.parseItemDataOfMission(mission);

            JSONObject data = new JSONObject();
            data.put("mission", mission);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterMissionQuyByAgency(SessionData sessionData, FilterListRequest request) {
        try {
            FilterRequest filterAgency = new FilterRequest();
            filterAgency.setKey("agency_id");
            filterAgency.setType(TypeFilter.SELECTBOX);
            filterAgency.setValue(ConvertUtils.toString(request.getAgency_id()));
            request.getFilters().add(filterAgency);

            FilterRequest filterPeriod = new FilterRequest();
            filterPeriod.setKey("mission_period_id");
            filterPeriod.setType(TypeFilter.SELECTBOX);
            filterPeriod.setValue(ConvertUtils.toString(MissionPeriodType.QUY.getId()));
            request.getFilters().add(filterPeriod);
            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_AGENCY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("mission_name", this.convertMissionName(js));
                js.put("completed", this.checkMissionComplete(js));
            }
            int total = this.missionDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse modifyAgencyMissionPoint(SessionData sessionData, ModifyAgencyMissionPointRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }
            JSONObject jsAgencyMissionPoint = this.missionDB.getAgencyMissionPoint(request.getAgency_id());
            if (jsAgencyMissionPoint == null) {
                int rsInsert = this.missionDB.initAgencyMisionPoint(
                        request.getAgency_id());
                if (rsInsert <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                jsAgencyMissionPoint = new JSONObject();
                jsAgencyMissionPoint.put("point", 0);
            }

            long old_point = ConvertUtils.toLong(jsAgencyMissionPoint.get("point"));

            if (TransactionEffectValueType.INCREASE.getCode().equals(request.getType())) {
                boolean rsModify = this.missionDB.increaseAgencyMissionPoint(
                        request.getAgency_id(),
                        request.getPoint()
                );
                if (!rsModify) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                int rsSaveAgencyMissionPointHistory = this.missionDB.saveAgencyMissionPointHistory(
                        request.getAgency_id(),
                        old_point,
                        request.getPoint(),
                        old_point + request.getPoint(),
                        null,
                        request.getNote());
                if (rsSaveAgencyMissionPointHistory <= 0) {
                    this.alertToTelegram("rsSaveAgencyMissionPointHistory: " + JsonUtils.Serialize(request), ResponseStatus.EXCEPTION);
                }
            } else if (TransactionEffectValueType.DECREASE.getCode().equals(request.getType())) {
                boolean rsModify = this.missionDB.decreaseAgencyMissionPoint(
                        request.getAgency_id(),
                        request.getPoint()
                );
                if (!rsModify) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                int rsSaveAgencyMissionPointHistory = this.missionDB.saveAgencyMissionPointHistory(
                        request.getAgency_id(),
                        old_point,
                        request.getPoint() * -1,
                        old_point - request.getPoint(),
                        null,
                        request.getNote());
                if (rsSaveAgencyMissionPointHistory <= 0) {
                    this.alertToTelegram("rsSaveAgencyMissionPointHistory: " + JsonUtils.Serialize(request), ResponseStatus.EXCEPTION);
                }
            } else {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Loại tăng giảm");
            }
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runTest() {
        try {
            List<Integer> missionPeriodRunningList = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU).toString(),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            this.generateAll(missionPeriodRunningList);

            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public void generateAll(List<Integer> missionPeriodRunningList) {
        try {
            Date start_date = DateTimeUtils.getNow();
            Date end_date = getMissionPeriodEndTime(
                    MissionPeriodType.TUAN,
                    start_date
            );

            List<JSONObject> data = new ArrayList<>();
            String time_tuan_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN).toString();
            String time_thang_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_THANG).toString();
            String time_tl_thang_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG).toString();
            for (Integer missionPeriodRunning : missionPeriodRunningList) {
                MissionPeriodType missionPeriodType = MissionPeriodType.from(missionPeriodRunning);
                switch (missionPeriodType) {
                    case TUAN: {
                        JSONObject jsPeriod = new JSONObject();
                        jsPeriod.put("mission_period_id", missionPeriodType.getId());
                        jsPeriod.put("type", missionPeriodType.getKey());
                        jsPeriod.put("start_time", start_date.getTime());
                        jsPeriod.put("end_time",
                                this.appUtils.getMissionPeriodEndTimeOfTuan(
                                        time_tuan_data,
                                        start_date, time_thang_data).getTime());
                        jsPeriod.put("end_tl_time", jsPeriod.get("end_time"));
                        data.add(jsPeriod);
                        break;
                    }
                    case THANG: {
                        JSONObject jsPeriod = new JSONObject();
                        jsPeriod.put("mission_period_id", missionPeriodType.getId());
                        jsPeriod.put("type", missionPeriodType.getKey());
                        jsPeriod.put("start_time", start_date.getTime());
                        jsPeriod.put("end_time",
                                this.appUtils.getMissionPeriodEndTimeOfThang(
                                        time_tuan_data,
                                        start_date, time_thang_data).getTime());
                        jsPeriod.put("end_tl_time", this.appUtils.getMissionPeriodEndTLTimeOfThang(
                                time_tuan_data,
                                start_date, time_tl_thang_data).getTime());
                        data.add(jsPeriod);
                        break;
                    }
                    case QUY:
                        JSONObject jsPeriod = new JSONObject();
                        jsPeriod.put("mission_period_id", missionPeriodType.getId());
                        jsPeriod.put("type", missionPeriodType.getKey());
                        jsPeriod.put("start_time", start_date.getTime());
                        jsPeriod.put("end_time",
                                this.getThoiGianKetThucNhiemVuQuy(start_date).getTime());
                        jsPeriod.put("end_tl_time", this.getThoiGianKetThucTichLuyNhiemVuQuy(start_date).getTime());
                        data.add(jsPeriod);
                        break;
                    default:
                        this.alertToTelegram("generateAll: " + missionPeriodRunning, ResponseStatus.EXCEPTION);
                }
            }

            /**
             * Lưu thời gian bắt đầu và kết thúc của từng kỳ
             */
            for (JSONObject mission_period : data) {
                this.resetMissionPeriodTime(mission_period);
            }

            ClientResponse crAssignMissionAllAgency = this.assignMissionService.assignMissionAllAgency(
                    JsonUtils.Serialize(data)
            );
            LogUtil.printDebug(JsonUtils.Serialize(crAssignMissionAllAgency));
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    private Date getThoiGianKetThucNhiemVuQuy(Date start_date) {
        return DateTimeUtils.getDateTime(
                this.appUtils.getYear(start_date) + "-" +
                        this.getNgayKetThucQuy(this.getQuy(start_date)) + " 23:59:59",
                "yyyy-MM-dd HH:mm:ss");
    }

    private Date getThoiGianKetThucTichLuyNhiemVuQuy(Date start_date) {
        String time_quy_data = this.dataManager.getConfigManager().getMPMissionConfig().get(
                MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_QUY
        );

        String time = time_quy_data.split("-")[0];
        return DateTimeUtils.getDateTime(
                this.appUtils.getYear(start_date) + "-" +
                        this.getNgayKetThucQuy(this.getQuy(start_date)) + " " + time + ":00",
                "yyyy-MM-dd HH:mm:ss");
    }

    private void resetMissionPeriodTime(JSONObject missionPeriod) {
        try {
            int mission_period_id = ConvertUtils.toInt(missionPeriod.get("mission_period_id"));
            JSONObject jsMissionPeriodRunning = this.missionDB.getMissionPeriodRunning(
                    mission_period_id
            );
            if (jsMissionPeriodRunning == null) {
                this.missionDB.insertMissionPeriodRunning(
                        mission_period_id,
                        DateTimeUtils.getDateTime(
                                ConvertUtils.toLong(missionPeriod.get("start_time"))),
                        DateTimeUtils.getDateTime(
                                ConvertUtils.toLong(missionPeriod.get("end_time"))),
                        DateTimeUtils.getDateTime(
                                ConvertUtils.toLong(missionPeriod.get("end_tl_time")))
                );
            } else {
                this.missionDB.resetMissionPeriodRunning(
                        mission_period_id,
                        DateTimeUtils.getDateTime(
                                ConvertUtils.toLong(missionPeriod.get("start_time"))),
                        DateTimeUtils.getDateTime(
                                ConvertUtils.toLong(missionPeriod.get("end_time"))),
                        DateTimeUtils.getDateTime(
                                ConvertUtils.toLong(missionPeriod.get("end_tl_time")))
                );
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    private void saveMissionPeriodTime() {
        try {
            Date start_date = new Date();
            List<Integer> missionPeriodRunningList = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            List<JSONObject> data = new ArrayList<>();
            String time_tuan_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN).toString();
            String time_thang_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_THANG).toString();
            for (Integer missionPeriodRunning : missionPeriodRunningList) {
                MissionPeriodType missionPeriodType = MissionPeriodType.from(missionPeriodRunning);
                switch (missionPeriodType) {
                    case TUAN: {
                        JSONObject jsPeriod = new JSONObject();
                        jsPeriod.put("mission_period_id", missionPeriodType.getId());
                        jsPeriod.put("type", missionPeriodType.getKey());
                        jsPeriod.put("start_time", start_date.getTime());
                        jsPeriod.put("end_time",
                                this.appUtils.getMissionPeriodEndTimeOfTuan(
                                        time_tuan_data,
                                        start_date, time_thang_data).getTime());
                        jsPeriod.put("end_time",
                                this.appUtils.getMissionPeriodEndTimeOfTuan(
                                        time_tuan_data,
                                        start_date, time_thang_data).getTime());
                        data.add(jsPeriod);
                        break;
                    }
                    case THANG: {
                        JSONObject jsPeriod = new JSONObject();
                        jsPeriod.put("mission_period_id", missionPeriodType.getId());
                        jsPeriod.put("type", missionPeriodType.getKey());
                        jsPeriod.put("start_time", start_date.getTime());
                        jsPeriod.put("end_time",
                                this.appUtils.getMissionPeriodEndTimeOfThang(
                                        time_tuan_data,
                                        start_date, time_thang_data).getTime());
                        jsPeriod.put("end_time",
                                this.appUtils.getMissionPeriodEndTLTimeOfThang(
                                        time_tuan_data,
                                        start_date, time_thang_data).getTime());
                        data.add(jsPeriod);
                        break;
                    }
                    case QUY:
                        JSONObject jsPeriod = new JSONObject();
                        jsPeriod.put("mission_period_id", missionPeriodType.getId());
                        jsPeriod.put("type", missionPeriodType.getKey());
                        jsPeriod.put("start_time", start_date.getTime());
                        jsPeriod.put("end_time",
                                DateTimeUtils.getDateTime(
                                        this.appUtils.getYear(start_date) + " -" +
                                                this.getNgayKetThucQuy(this.getQuy(start_date)) + " 23:59:59",
                                        "yyyy-MM-dd HH:mm:ss").getTime());
                        jsPeriod.put("end_tl_time",
                                DateTimeUtils.getDateTime(
                                        this.appUtils.getYear(start_date) + " -" +
                                                this.getNgayKetThucQuy(this.getQuy(start_date)) + " 23:59:59",
                                        "yyyy-MM-dd HH:mm:ss").getTime());
                        data.add(jsPeriod);
                        break;
                    default:
                        this.alertToTelegram("generateAll: " + missionPeriodRunning, ResponseStatus.EXCEPTION);
                }
            }

            for (JSONObject missionPeriod : data) {
                int mission_period_id = ConvertUtils.toInt(missionPeriod.get("mission_period_id"));
                JSONObject jsMissionPeriodRunning = this.missionDB.getMissionPeriodRunning(
                        mission_period_id
                );
                if (jsMissionPeriodRunning == null) {
                    this.missionDB.insertMissionPeriodRunning(
                            mission_period_id,
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toLong(missionPeriod.get("start_time"))),
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toLong(missionPeriod.get("end_time"))),
                            DateTimeUtils.getDateTime(
                                    ConvertUtils.toLong(missionPeriod.get("end_tl_time")))
                    );
                }
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    public ClientResponse runThongBaoNhanHuyHieu() {
        try {
            Date now = DateTimeUtils.getNow();
            List<Integer> missionPeriodRunningList = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU).toString(),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            if (missionPeriodRunningList == null || missionPeriodRunningList.isEmpty()) {
                return ResponseConstants.success;
            }
            for (Integer missionPeriodRunning : missionPeriodRunningList) {
                MissionPeriodType missionPeriodType = MissionPeriodType.from(missionPeriodRunning);
                switch (missionPeriodType) {
                    case TUAN: {
                        JSONObject jsMissionPeriodTuan = this.missionDB.getMissionPeriodRunning(missionPeriodRunning);
                        if (jsMissionPeriodTuan == null) {
                            break;
                        }

                        if (YesNoStatus.NO.getValue() == ConvertUtils.toInt(jsMissionPeriodTuan.get("push_notify_claim"))) {
                            Date end_date = DateTimeUtils.getDateTime(jsMissionPeriodTuan.get("end_date").toString(), "yyyy-MM-dd HH:mm:ss");
                            if (end_date == null) {
                                break;
                            }

                            if (end_date.getTime() - (
                                    MissionConstants.THOI_GIAN_THONG_BAO_NHAN_HUY_HIEU_TUAN * 60 * 60 * 1000) < now.getTime()) {
                                this.thongBaoNhanHuyHieu(
                                        missionPeriodType
                                );
                            }
                        }
                        break;
                    }
                    case THANG: {
                        JSONObject jsMissionPeriodTuan = this.missionDB.getMissionPeriodRunning(missionPeriodRunning);
                        if (jsMissionPeriodTuan == null) {
                            break;
                        }

                        if (YesNoStatus.NO.getValue() == ConvertUtils.toInt(jsMissionPeriodTuan.get("push_notify_claim"))) {
                            Date end_date = DateTimeUtils.getDateTime(jsMissionPeriodTuan.get("end_date").toString(), "yyyy-MM-dd HH:mm:ss");
                            if (end_date == null) {
                                break;
                            }

                            int day = ConvertUtils.toInt(this.appUtils.getDay(now));
                            int hour = ConvertUtils.toInt(DateTimeUtils.toString(now,
                                    "HH"));
                            if (ConvertUtils.toInt(this.appUtils.getDay(end_date)) == day
                                    && MissionConstants.THOI_GIAN_THONG_BAO_NHAN_HUY_HIEU_THANG == hour) {
                                this.thongBaoNhanHuyHieu(
                                        missionPeriodType
                                );
                            }
                        }
                        break;
                    }
                    case QUY: {
                        JSONObject jsMissionPeriodTuan = this.missionDB.getMissionPeriodRunning(missionPeriodRunning);
                        if (jsMissionPeriodTuan == null) {
                            break;
                        }


                        if (YesNoStatus.NO.getValue() == ConvertUtils.toInt(jsMissionPeriodTuan.get("push_notify_claim"))) {
                            Date end_date = DateTimeUtils.getDateTime(jsMissionPeriodTuan.get("end_date").toString(), "yyyy-MM-dd HH:mm:ss");
                            if (end_date == null) {
                                break;
                            }

                            int day = ConvertUtils.toInt(this.appUtils.getDay(now));
                            int hour = ConvertUtils.toInt(DateTimeUtils.toString(now,
                                    "HH"));
                            if (ConvertUtils.toInt(this.appUtils.getDay(end_date)) == day
                                    && MissionConstants.THOI_GIAN_THONG_BAO_NHAN_HUY_HIEU_QUY == hour) {
                                this.thongBaoNhanHuyHieu(
                                        missionPeriodType
                                );
                            }
                        }
                        break;
                    }
                }
            }

            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse thongBaoNhanHuyHieu(MissionPeriodType missionPeriodType) {
        try {
            String content = "";
            String name = "";
            switch (missionPeriodType) {
                case TUAN:
                    content = MissionConstants.THONG_BAO_NHAN_HUY_HIEU_TUAN;
                    name = MissionPeriodType.TUAN.name();
                    break;
                case THANG:
                    content = MissionConstants.THONG_BAO_NHAN_HUY_HIEU_THANG;
                    name = MissionPeriodType.THANG.name();
                    break;
                case QUY:
                    content = MissionConstants.THONG_BAO_NHAN_HUY_HIEU_QUY;
                    name = MissionPeriodType.QUY.name();
                    break;
            }
            List<JSONObject> agencyList = this.missionDB.getListAgencyByMissionPeriod(
                    missionPeriodType.getId());
            JSONObject mission_data = new JSONObject();
            mission_data.put("type", "NHIEM_VU");
            mission_data.put("value", name);
            for (JSONObject jsAgency : agencyList) {
                ClientResponse crPushNotifyToastToAgency = this.pushPopupToAgency(
                        0,
                        null,
                        MissionConstants.MISSION_POINT_IMAGE,
                        "",
                        NotifyType.NHIEM_VU.getCode(),
                        "[]",
                        "",
                        "CHÚ Ý",
                        content,
                        ConvertUtils.toInt(jsAgency.get("agency_id")),
                        JsonUtils.Serialize(mission_data)
                );
            }
            boolean rsSavePushedThongBaoKhiKetThucNhiemVu = this.missionDB.savePushedThongBaoKhiKetThucNhiemVu(
                    missionPeriodType.getId());
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterTKMissionBXHAgency(SessionData sessionData, FilterTKAgencyMissionBXHRequest request) {
        try {
            int mission_bxh_id = request.getId();
            JSONObject jsMissionBXH = this.missionDB.getMissionBXH(mission_bxh_id);
            if (jsMissionBXH == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            MissionBXHData missionBXHData = this.convertMissionBXHData(jsMissionBXH);
            if (missionBXHData == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            int mission_period_id = ConvertUtils.toInt(jsMissionBXH.get("mission_period_id"));
            Date now = new Date();

            MissionPeriodType missionPeriodType = MissionPeriodType.from(mission_period_id);
            switch (missionPeriodType) {
                case THANG: {
                    int month = ConvertUtils.toInt(this.appUtils.getMonth(now));
                    int year = ConvertUtils.toInt(this.appUtils.getYear(now));
                    if ((request.getPeriod() == 0 && request.getYear() == 0) || (year == request.getYear() && month == request.getPeriod())) {
                        return filterTKMissionBXHAgencyByCurrent(
                                sessionData,
                                request,
                                mission_bxh_id,
                                missionBXHData,
                                jsMissionBXH.get("agency_data").toString());
                    } else {
                        Date start_date = DateTimeUtils.getDateTime(
                                year + "-" + String.format("%02d", request.getPeriod()) + "-01 00:00:00",
                                "yyyy-MM-dd HH:mm:ss");
                        Date end_date = DateTimeUtils.getDateTime(
                                year + "-" + String.format("%02d", request.getPeriod()) +
                                        "-" + String.format("%02d", this.appUtils.getLastDayOfMonth(start_date)) +
                                        " 23:59:59",
                                "yyyy-MM-dd HH:mm:ss");
                        return filterTKMissionBXHAgencyByPeriod(
                                sessionData,
                                request,
                                mission_bxh_id,
                                missionBXHData,
                                start_date,
                                end_date
                        );
                    }
                }
                case QUY: {
                    int quy = this.getQuy(now);
                    int year = ConvertUtils.toInt(this.appUtils.getYear(now));
                    if (year == request.getYear() && quy == request.getPeriod()) {
                        return filterTKMissionBXHAgencyByCurrent(
                                sessionData,
                                request,
                                mission_bxh_id,
                                missionBXHData,
                                jsMissionBXH.get("agency_data").toString());
                    } else {
                        Date start_date = DateTimeUtils.getDateTime(
                                year + "-" + this.getNgayBatDauQuy(quy) + " 00:00:00",
                                "yyyy-MM-dd HH:mm:ss");
                        Date end_date = DateTimeUtils.getDateTime(
                                year + "-" + this.getNgayKetThucQuy(quy) + " 23:59:59",
                                "yyyy-MM-dd HH:mm:ss");
                        return filterTKMissionBXHAgencyByPeriod(
                                sessionData,
                                request,
                                mission_bxh_id,
                                missionBXHData,
                                start_date,
                                end_date);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse filterTKMissionBXHAgencyByCurrent(
            SessionData sessionData,
            FilterTKAgencyMissionBXHRequest request,
            int mission_bxh_id,
            MissionBXHData missionBXHData,
            String agency_join_data) {
        try {
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(mission_bxh_id));
            filterRequest.setKey("t.mission_bxh_id");
            request.getFilters().add(filterRequest);

            List<JSONObject> records = new ArrayList<>();

            long require_accumulate_value = missionBXHData.getInfo().getRequire_accumulate_value();

            List<MissionBXHLimitRequest> promoOfferList = missionBXHData.getLimits();
            Date now = new Date();
            Date start_date = this.getStartDateOfMissionBXH(missionBXHData.getInfo().getMission_period_id(), now);
            Date end_date = this.getEndDateOfMissionBXH(missionBXHData.getInfo().getMission_period_id(), now);

            List<Integer> agencyIdList = JsonUtils.DeSerialize(agency_join_data, new TypeToken<List<Integer>>() {
            }.getType());

            List<JSONObject> agencyJoinList = this.agencyDB.getListAgencyByIds(
                    agencyIdList.stream().map(
                            e -> ConvertUtils.toString(e.toString())
                    ).collect(Collectors.toList()));
            List<MissionAgencyRankData> agencyBasicDataList = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(agencyJoinList),
                    new TypeToken<List<MissionAgencyRankData>>() {
                    }.getType());

            List<MissionBXHAgencyRankData> agencyRankList = this.getListAgencyRank(
                    mission_bxh_id,
                    agencyIdList,
                    start_date,
                    end_date);


            int business_department_id = ConvertUtils.toInt(
                    this.filterUtils.getValueByType(
                            request.getFilters(),
                            "business_department_id"));
            String search = this.filterUtils.getValueByType(
                    request.getFilters(),
                    "search");

            List<MissionAgencyRankData> agencyResponseList = agencyBasicDataList.stream().filter(
                    x -> filterAgency(
                            x,
                            search,
                            business_department_id)
            ).collect(Collectors.toList());

            /**
             * Xếp hạng
             */
            agencyResponseList.forEach(
                    ar -> {
                        ar.setRank(
                                this.getAgencyRank(ar.getId(), agencyRankList));
                    }
            );

            Collections.sort(
                    agencyResponseList, (a, b) -> {
                        if (a.getRank() < b.getRank()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
            );

            agencyResponseList.forEach(
                    agencyJoin -> {
                        JSONObject agency = new JSONObject();
                        int agency_id = ConvertUtils.toInt(agencyJoin.getId());
                        int rank_value = this.getAgencyMissionBXHPoint(agency_id, start_date, end_date);
                        agency.put("agency_id", agency_id);
                        agency.put("agency_info", agencyJoin);

                        /**
                         * nickname
                         */
                        agency.put("nickname", agencyJoin.getNick_name());
                        agency.put("tong_gia_tri_tich_luy", rank_value);

                        /**
                         * Hạng hiện tại
                         */
                        if (rank_value >= require_accumulate_value) {
                            agency.put("hang_hien_tai", agencyJoin.getRank());

                            /**
                             * Xếp hạng có quà
                             */
                            if (agencyJoin.getRank() <= promoOfferList.size()) {
                                /**
                                 * phần trăm và giá trị nếu là ưu đãi giảm tiền
                                 */
                                MissionBXHLimitRequest promoOffer = promoOfferList.get(agencyJoin.getRank() - 1);
                                if (promoOffer != null) {
                                    List<JSONObject> vrps = this.getListVRPByRank(agencyJoin.getRank(), promoOfferList);

                                    MissionBXHOfferRequest offer = promoOffer.getGroups().get(0).getOffer();
                                    agency.put("uu_dai_acoin", offer.getOffer_acoin_value());
                                    agency.put("uu_dai_giam_tien", offer.getOffer_value());
                                    long dtt = this.getGiaTriDTT(agency_id,
                                            start_date,
                                            end_date);
                                    agency.put("dtt", dtt);
                                    agency.put("uu_dai_gia_tri_dtt", dtt <= 0 ? 0 : this.getGiaTriUuDaiDTT(
                                            dtt,
                                            offer.getOffer_dtt_value()));
                                    agency.put("uu_dai_phan_tram_dtt", offer.getOffer_dtt_value());

                                    /**
                                     * Ưu đãi quà tặng
                                     */
                                    List<JSONObject> uu_dai_qua_tang = new ArrayList<>();
                                    for (JSONObject vrp : vrps) {
                                        if (VoucherOfferType.GIFT_OFFER.getKey().equals(
                                                ConvertUtils.toString(vrp.get("offer_type")))) {
                                            List<JSONObject> gifts = this.missionDB.getListVRPItem(ConvertUtils.toInt(vrp.get("id")));
                                            for (JSONObject gift : gifts) {
                                                gift.put("product_info", this.dataManager.getProductManager().getProductBasicData(ConvertUtils.toInt(gift.get("item_id"))));
                                            }
                                            vrp.put("item_data", gifts);
                                            uu_dai_qua_tang.add(vrp);
                                        }
                                    }
                                    agency.put("vrps", vrps);
                                    agency.put("uu_dai_qua_tang", uu_dai_qua_tang);
                                }
                            }
                        }
                        records.add(agency);
                    }
            );

            int total = agencyResponseList.size();
            int from = this.appUtils.getOffset(request.getPage());
            int to = from + ConfigInfo.PAGE_SIZE;
            if (to > total) {
                to = total;
            }
            List<JSONObject> subList = new ArrayList<>();
            if (from < total) {
                subList = records.subList(
                        from,
                        to);
            }
            JSONObject data = new JSONObject();
            data.put("records", subList);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean filterAgency(MissionAgencyRankData x, String search, int business_department_id) {
        if (
                (search.isEmpty() ||
                        x.getCode().contains(search) ||
                        x.getCode().toLowerCase().contains(search.toLowerCase()) ||
                        x.getShop_name().contains(search) ||
                        x.getShop_name().toLowerCase().contains(search.toLowerCase())) &&
                        (business_department_id == 0 ||
                                x.getBusiness_department_id() == business_department_id)
        ) {
            return true;
        }
        return false;
    }

    private int getAgencyMissionBXHPoint(int agency_id, Date startDate, Date endDate) {
        try {
            return this.missionDB.getTotalMissionBXHPoint(agency_id,
                    this.appUtils.convertDateToString(startDate, "yyyy-MM-dd"),
                    this.appUtils.convertDateToString(endDate, "yyyy-MM-dd"));
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return 0;

    }

    private int getAgencyMissionPoint(int agency_id) {
        try {
            JSONObject js = this.missionDB.getAgencyMissionPoint(agency_id);
            if (js != null) {
                return ConvertUtils.toInt(js.get("point"));
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return 0;

    }

    private long getGiaTriUuDaiDTT(long giaTriDTT, Long offerDttValue) {
        return ConvertUtils.toLong(
                this.appUtils.roundPrice(giaTriDTT * (offerDttValue * 1.0F / 100))
        );
    }

    private long getGiaTriDTT(int agency_id, Date startDate, Date endDate) {
        return this.deptDB.getTotalDttByAgency(agency_id,
                DateTimeUtils.toString(startDate, "yyyy-MM-dd HH:mm:ss"),
                DateTimeUtils.toString(endDate, "yyyy-MM-dd HH:mm:ss")
        );
    }

    public ClientResponse getResultMissionBXHInfo(SessionData sessionData, GetResultMissionBXHInfoRequest request) {
        try {
            int mission_bxh_id = request.getId();
            JSONObject jsMissionBXH = this.missionDB.getMissionBXH(mission_bxh_id);
            if (jsMissionBXH == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int mission_period_id = ConvertUtils.toInt(jsMissionBXH.get("mission_period_id"));
            Date now = new Date();

            MissionPeriodType missionPeriodType = MissionPeriodType.from(mission_period_id);
            switch (missionPeriodType) {
                case THANG: {
                    int month = ConvertUtils.toInt(this.appUtils.getMonth(now));
                    int year = ConvertUtils.toInt(this.appUtils.getYear(now));
                    if ((0 == request.getYear() && 0 == request.getPeriod()) || (year == request.getYear() && month == request.getPeriod())) {
                        return this.getResultMissionBXHInfoByCurrent(request.getId(), null, null);
                    } else {
                        Date start_date = DateTimeUtils.getDateTime(
                                year + "-" + String.format("%02d", request.getPeriod()) + "-01 00:00:00",
                                "yyyy-MM-dd HH:mm:ss");
                        Date end_date = DateTimeUtils.getDateTime(
                                year + "-" + String.format("%02d", request.getPeriod()) +
                                        "-" + String.format("%02d", this.appUtils.getLastDayOfMonth(start_date)) +
                                        " 23:59:59",
                                "yyyy-MM-dd HH:mm:ss");
                        return this.getResultMissionBXHInfoByPeriod(request.getId(),
                                start_date, end_date);
                    }
                }
                case QUY: {
                    int quy = this.getQuy(now);
                    int year = ConvertUtils.toInt(this.appUtils.getYear(now));
                    if (year == request.getYear() && quy == request.getPeriod()) {
                        return this.getResultMissionBXHInfoByCurrent(request.getId(), null, null);
                    } else {
                        Date start_date = DateTimeUtils.getDateTime(
                                year + "-" + this.getNgayBatDauQuy(quy) + " 00:00:00",
                                "yyyy-MM-dd HH:mm:ss");
                        Date end_date = DateTimeUtils.getDateTime(
                                year + "-" + this.getNgayKetThucQuy(quy) + " 23:59:59",
                                "yyyy-MM-dd HH:mm:ss");
                        return this.getResultMissionBXHInfoByPeriod(request.getId(),
                                start_date, end_date);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private String getMissionPeriodCode(int year, int period) {
        return year + "-" + String.format("%02d", period);
    }

    private ClientResponse getResultMissionBXHInfoByCurrent(int mission_bxh_id, Date start_date, Date end_date) {
        try {
            JSONObject result = new JSONObject();
            result.put("tong_khach_hang_tham_gia", this.missionDB.getTongKhachHangThamGiaBXH(mission_bxh_id));
            JSONObject data = new JSONObject();
            data.put("result", result);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse getResultMissionBXHInfoByPeriod(int mission_bxh_id, Date start_date, Date end_date) {
        try {
            JSONObject result = new JSONObject();
            result.put("tong_khach_hang_tham_gia", this.missionLogDB.getTongKhachHangThamGiaBXHByPeriod(mission_bxh_id, start_date, end_date));
            JSONObject data = new JSONObject();
            data.put("result", result);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse ketThucKyDai() {
        try {
            List<Integer> mission_period_running_list = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            int ky_dai = Collections.max(mission_period_running_list);
            if (MissionPeriodType.THANG.getId() == ky_dai) {
                this.generateAll(mission_period_running_list);
            } else if (MissionPeriodType.QUY.getId() == ky_dai) {
                this.generateAll(mission_period_running_list);
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse ketThucKyNgan() {
        try {
            List<Integer> mission_period_running_list = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            int ky_ngan = Collections.min(mission_period_running_list);
            if (MissionPeriodType.TUAN.getId() == ky_ngan) {
                this.generateAll(Arrays.asList(MissionPeriodType.TUAN.getId()));
            } else if (MissionPeriodType.THANG.getId() == ky_ngan) {
                this.generateAll(Arrays.asList(MissionPeriodType.THANG.getId()));
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getMissionLimitOfferInfo(BasicRequest request) {
        try {
            JSONObject jsAgency = this.agencyDB.getAgencyInfo(request.getId());
            if (jsAgency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int ky_dai = this.dataManager.getConfigManager().getKyDai();
            List<JSONObject> mission_limit_offer_list = new ArrayList<>();
            JSONObject mission_setting_agency = this.missionDB.getOneMissionSettingOfAgency(
                    request.getId());
            if (mission_setting_agency != null) {
                JSONObject jsMissionAgencyData = this.missionDB.getMissionAgencyData(request.getId(), ky_dai);
                if (jsMissionAgencyData != null) {
                    List<Integer> claimed_offer_limit = JsonUtils.DeSerialize(
                            jsMissionAgencyData.get("claimed_offer_limit").toString(),
                            new TypeToken<List<Integer>>() {
                            }.getType());
                    JSONObject jsMissionSetting = this.missionDB.getMissionSettingInfo(
                            ConvertUtils.toInt(jsMissionAgencyData.get("mission_setting_id")));
                    if (jsMissionSetting != null) {
                        /**
                         * Mức ưu đãi
                         */
                        MissionOfferRequest offer_data =
                                JsonUtils.DeSerialize(jsMissionSetting.get("offer_data").toString(),
                                        MissionOfferRequest.class);
                        offer_data.getLimits().forEach(l -> {
                            JSONObject offer = new JSONObject();
                            offer.put("level", l.getLevel());
                            offer.put("require_value", l.getValue());
                            offer.put("image", l.getImage());
                            mission_limit_offer_list.add(offer);
                        });
                    }

                    mission_limit_offer_list.forEach(o -> {
                        o.put("claim_status", 0);
                        if (claimed_offer_limit != null) {
                            claimed_offer_limit.forEach(c -> {
                                if (c.intValue() == ConvertUtils.toInt(o.get("level"))) {
                                    o.put("claim_status", 1);
                                }
                            });
                        }
                    });
                }
            }

            JSONObject jsAgencyMissionPoint = this.missionDB.getAgencyMissionPoint(request.getId());
            JSONObject data = new JSONObject();
            data.put("mission_point", jsAgencyMissionPoint == null ? 0 : jsAgencyMissionPoint.get("point"));
            data.put("mission_limit_offer_list", mission_limit_offer_list);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterAgencyMissionHistory(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_AGENCY_MISSION_HISTORY, request.getFilters(), request.getSorts());
            List<JSONObject> missionHistory = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());

            request.getFilters().forEach(f -> {
                if (f.getKey().equals("created_date")) {
                    f.setKey("mission_created_date");
                }
            });

            List<JSONObject> records = new ArrayList<>();
            for (JSONObject jsMission : missionHistory) {
                jsMission.put("mission_name", this.convertMissionName(jsMission));
                JSONObject jsMissionSettingInfo = this.missionDB.getMissionSettingInfo(
                        ConvertUtils.toInt(jsMission.get("mission_setting_id")));
                jsMission.put("mission_setting_info", jsMissionSettingInfo);
                int status = ConvertUtils.toInt(jsMission.get("mission_status"));
                jsMission.put("so_huy_hieu_thuong", jsMission.get("mission_reward_point"));
                if (MissionAgencyStatus.FINISH.getId() == status) {
                    jsMission.put("so_huy_hieu_thuc_nhan", jsMission.get("so_huy_hieu_thuong"));
                } else {
                    jsMission.put("so_huy_hieu_thuc_nhan", 0);
                }

                jsMission.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(jsMission.get("agency_id"))
                ));

                if (jsMissionSettingInfo != null) {
                    jsMission.put("mission_group_info", this.missionDB.getMissionGroupInfo(
                            ConvertUtils.toInt(jsMissionSettingInfo.get("mission_group_id"))));
                }
                jsMission.put("status", status);
                jsMission.put("created_date", jsMission.get("mission_created_date"));

                records.add(jsMission);
            }
            int total = this.missionDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterMissionSetting(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_SETTING, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionDB.getTotal(query);
            for (JSONObject js : records) {
                js.put("creator_info", this.dataManager.getStaffManager().getStaffProfile(ConvertUtils.toInt(js.get("creator_id"))));
                js.put("mission_group_info", this.missionDB.getMissionGroupInfo(ConvertUtils.toInt(js.get("mission_group_id"))));
            }
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runTichLuyNhiemVuChoDonHangSoanHang() {
        try {
            Date now = DateTimeUtils.getNow();
            List<JSONObject> agencyOrderList = this.missionDB.getListOrderReadyTichLuy(
                    ConfigInfo.SCHEDULE_RUNNING_LIMIT
            );
            for (JSONObject jsAgencyOrder : agencyOrderList) {
                int agency_order_id = ConvertUtils.toInt(jsAgencyOrder.get("id"));
                int agency_id = ConvertUtils.toInt(jsAgencyOrder.get("agency_id"));
                if (this.checkAgencyHasAnyMission(agency_id)) {
                    List<JSONObject> agencyOrderDeptList = this.orderDB.getListAgencyOrderDeptNotFinish(
                            agency_order_id);
                    for (JSONObject agencyOrderDept : agencyOrderDeptList) {
                        String dept_code = ConvertUtils.toString(agencyOrderDept.get("dept_code"));
                        if (dept_code == null || dept_code.isEmpty()) {
                            dept_code = this.appUtils.getAgencyOrderDeptCode(
                                    ConvertUtils.toString(jsAgencyOrder.get("code")),
                                    ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                                    ConvertUtils.toInt(jsAgencyOrder.get("total")),
                                    ConvertUtils.toInt(agencyOrderDept.get("promo_id")) != 0,
                                    this.hasOrderNormal(agency_order_id));
                            this.orderDB.saveDeptCode(
                                    ConvertUtils.toInt(agencyOrderDept.get("id")),
                                    dept_code
                            );
                            agencyOrderDept.put("dept_code", dept_code);
                        }

                        agencyOrderDept.put("dept_code", dept_code);
                    }

                    this.callGhiNhanTichLuyNhiemVuChoDonHang(
                            agency_order_id,
                            ConvertUtils.toInt(jsAgencyOrder.get("agency_id")),
                            agencyOrderDeptList
                    );

                    this.orderDB.ghiNhanTrangThaiTichLuyNhiemVuChoDonHangOver(agency_order_id);
                } else {
                    this.orderDB.ghiNhanDonHangOver(agency_order_id);
                }
            }
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean hasOrderNormal(int agency_order_id) {
        return this.orderDB.getOrderNormalDept(
                agency_order_id) != null;
    }

    public void callGhiNhanTichLuyNhiemVuChoDonHang(
            int agency_order_id,
            int agency_id,
            List<JSONObject> agencyOrderDeptList
    ) {
        try {
            for (JSONObject agencyOrderDept : agencyOrderDeptList) {
                this.accumulateMissionService.tichLuyTuDongOrder(
                        agency_id,
                        agency_order_id,
                        ConvertUtils.toInt(agencyOrderDept.get("id")),
                        ConvertUtils.toString(agencyOrderDept.get("dept_code"))
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    private boolean checkOrderReadyTichLuy(Date prepare_date, Date now) {
        try {
            int hour_prepare = ConvertUtils.toInt(
                    DateTimeUtils.toString(prepare_date, "HH"));
            int hour_now = ConvertUtils.toInt(
                    DateTimeUtils.toString(now, "HH"));

            int day_prepare = ConvertUtils.toInt(
                    DateTimeUtils.toString(prepare_date, "dd"));

            int day_now = ConvertUtils.toInt(
                    DateTimeUtils.toString(now, "dd"));

            int day_of_week = this.appUtils.getDateOfWeek(prepare_date);
            if (day_prepare == day_now) {
                long time_over = 0;
                TimeToWorkData timeToWorkData = this.dataManager.getConfigManager().getGioHanhChinhByDayOfWeek(
                        day_of_week);
                if (timeToWorkData != null) {
                    int hour_start_morning =
                            ConvertUtils.toInt(DateTimeUtils.getDateTime(timeToWorkData.getMorning().getDate_from(), "HH"));
                    int hour_end_morning =
                            ConvertUtils.toInt(DateTimeUtils.getDateTime(timeToWorkData.getMorning().getDate_from(), "HH"));
                    if (hour_prepare <= hour_start_morning) {
                        time_over = 0;
                    } else if (hour_prepare >= hour_start_morning &&
                            hour_prepare < hour_end_morning) {
                        if (hour_now <= hour_end_morning) {
                            time_over = now.getTime() - prepare_date.getTime();
                            if (time_over >= MissionConstants.THOI_GIAN_DON_SOAN_HANG_TICH_LUY * 60 * 60 * 1000) {
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            time_over +=
                                    DateTimeUtils.getDateTime(now, "HH:mm").getTime() -
                                            DateTimeUtils.getDateTime(prepare_date, "HH:mm").getTime();

                        }
                    }
                }
            }


            int hour = ConvertUtils.toInt(
                    DateTimeUtils.toString(prepare_date, "HH"));
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return false;
    }

    public ClientResponse runEstimateAgencyOrderAutoTichLuy() {
        try {
            List<JSONObject> agencyOrderList = this.missionDB.getListOrderReady(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject jsAgencyOrder : agencyOrderList) {
                if (jsAgencyOrder.get("confirm_prepare_date") != null) {
                    Date time_tich_luy = this.estimateAgencyOrderAutoTichLuy(
                            DateTimeUtils.getDateTime(jsAgencyOrder.get("confirm_prepare_date").toString(), "yyyy-MM-dd HH:mm:ss"));
                    if (time_tich_luy != null) {
                        this.missionDB.saveAgencyOrderEstimateTime(
                                ConvertUtils.toInt(jsAgencyOrder.get("id")),
                                time_tich_luy
                        );
                    } else {
                        this.alertToTelegram("runEstimateAgencyOrderAutoTichLuy: " + JsonUtils.Serialize(jsAgencyOrder), ResponseStatus.EXCEPTION);
                    }
                } else {
                    this.missionDB.saveConfirmPrepareDate(ConvertUtils.toInt(jsAgencyOrder.get("id")));
                }
            }

            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private Date estimateAgencyOrderAutoTichLuy(Date confirmPrepareDate) {
        try {
            int day_of_week = this.appUtils.getDateOfWeek(confirmPrepareDate);
            switch (day_of_week) {
                case 8: {
                    Date date =
                            DateTimeUtils.getDateTime(
                                    DateTimeUtils.toString(
                                            confirmPrepareDate, "yyyy-MM-dd") +
                                            " 00:00:00", "yyyy-MM-dd HH:mm:ss");
                    date = this.appUtils.getDateAfterHour(date, 24 + 8 + MissionConstants.THOI_GIAN_DON_SOAN_HANG_TICH_LUY);
                    return date;
                }
                case 7: {
                    int hour_prepare = ConvertUtils.toInt(this.appUtils.getHour(confirmPrepareDate));
                    String home_start = "";
                    if (hour_prepare < 8) {
                        return this.appUtils.getDateAfterHour(
                                DateTimeUtils.getDateTime(
                                        DateTimeUtils.toString(
                                                confirmPrepareDate, "yyyy-MM-dd") +
                                                " 08:00:00", "yyyy-MM-dd HH:mm:ss"),
                                MissionConstants.THOI_GIAN_DON_SOAN_HANG_TICH_LUY);
                    } else if (hour_prepare >= 8 && hour_prepare < 12) {
                        Date date = this.appUtils.getDateAfterHour(confirmPrepareDate, MissionConstants.THOI_GIAN_DON_SOAN_HANG_TICH_LUY);
                        int hour = ConvertUtils.toInt(this.appUtils.getHour(date));
                        if (hour >= 12) {
                            date = this.appUtils.getDateAfterHour(date, 12 + 8 - (hour - 12));
                        }
                        return date;
                    } else if (hour_prepare >= 12) {
                        return this.appUtils.getDateAfterHour(
                                DateTimeUtils.getDateTime(
                                        DateTimeUtils.toString(confirmPrepareDate, "yyyy-MM-dd") + " 12:00:00",
                                        "yyyy-MM-dd HH:mm:ss"),
                                12 + 8 + MissionConstants.THOI_GIAN_DON_SOAN_HANG_TICH_LUY);
                    }
                    break;
                }
                default: {
                    int hour_prepare = ConvertUtils.toInt(this.appUtils.getHour(confirmPrepareDate));
                    String home_start = "";
                    if (hour_prepare < 8) {
                        return this.appUtils.getDateAfterHour(
                                DateTimeUtils.getDateTime(
                                        DateTimeUtils.toString(
                                                confirmPrepareDate, "yyyy-MM-dd") +
                                                " 08:00:00", "yyyy-MM-dd HH:mm:ss"),
                                MissionConstants.THOI_GIAN_DON_SOAN_HANG_TICH_LUY);
                    } else if (hour_prepare >= 8 && hour_prepare < 12) {
                        Date date = this.appUtils.getDateAfterHour(confirmPrepareDate, MissionConstants.THOI_GIAN_DON_SOAN_HANG_TICH_LUY);
                        int hour = ConvertUtils.toInt(this.appUtils.getHour(date));
                        if (hour >= 12) {
                            date = this.appUtils.getDateAfterHour(date, 1);
                        }
                        return date;
                    } else if (hour_prepare >= 12 && hour_prepare < 13) {
                        Date date = this.appUtils.getDateAfterHour(
                                DateTimeUtils.getDateTime(
                                        DateTimeUtils.toString(confirmPrepareDate, "yyyy-MM-dd") + " 13:00:00",
                                        "yyyy-MM-dd HH:mm:ss"),
                                MissionConstants.THOI_GIAN_DON_SOAN_HANG_TICH_LUY);
                        int minute = ConvertUtils.toInt(this.appUtils.getMinute(date));
                        date = this.appUtils.getDateAfterMinute(
                                date, 60 - minute
                        );
                        int hour = ConvertUtils.toInt(this.appUtils.getHour(date));
                        if (hour >= 12) {
                            date = this.appUtils.getDateAfterHour(date, 1);
                        }

                        return date;
                    } else if (hour_prepare >= 13 && hour_prepare < 17) {
                        Date date = this.appUtils.getDateAfterHour(confirmPrepareDate, MissionConstants.THOI_GIAN_DON_SOAN_HANG_TICH_LUY);
                        int hour = ConvertUtils.toInt(this.appUtils.getHour(date));
                        if (hour >= 17) {
                            date = this.appUtils.getDateAfterHour(date, 7 + 8 - (hour - 17));
                        }

                        return date;
                    } else if (hour_prepare >= 17) {
                        Date date =
                                DateTimeUtils.getDateTime(
                                        DateTimeUtils.toString(confirmPrepareDate, "yyyy-MM-dd") + " 17:00:00",
                                        "yyyy-MM-dd HH:mm:ss"
                                );
                        date = this.appUtils.getDateAfterHour(date, 7 + 8 + MissionConstants.THOI_GIAN_DON_SOAN_HANG_TICH_LUY);

                        return date;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    public ClientResponse runCallRewardMission() {
        try {
            List<JSONObject> agencyMissionPeriodRunningList = this.missionDB.getListMissionPeriodNeedReward(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            if (agencyMissionPeriodRunningList.size() > 0) {
                List<Integer> data = agencyMissionPeriodRunningList.stream().map(
                        e -> ConvertUtils.toInt(e.get("id"))
                ).collect(Collectors.toList());

                this.callRewardMission(data);
            }
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse callRewardMission(List<Integer> data) {
        try {
            this.accumulateMissionService.rewardMission(JsonUtils.Serialize(data));
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public void giaoNhiemVu(int agency_id) {
        try {
            if (!this.checkAgencyHasAnyMission(agency_id)) {
                this.addAgencyToMissionSetting(agency_id);
            }

            String time_tuan_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN).toString();
            String time_thang_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_THANG).toString();
            List<Integer> missionPeriodRunningList = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            Date start_date = DateTimeUtils.getNow();
            missionPeriodRunningList.forEach(
                    mp -> {
                        MissionPeriodType missionPeriodType = MissionPeriodType.from(mp);
                        Date end_date = this.appUtils.getMissionPeriodEndTime(start_date, missionPeriodType, time_tuan_data, time_thang_data);
                        if (end_date != null) {
                            boolean hasNhiemVu = this.checkAgencyHasMission(agency_id, mp);
                            if (!hasNhiemVu) {
                                Date end_tl_date = end_date;
                                if (missionPeriodType == MissionPeriodType.THANG) {
                                    String time_tl_thang_data = this.dataManager.getConfigManager().getMPMissionConfig().get(
                                            MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG).toString();
                                    end_tl_date = this.appUtils.getMissionPeriodEndTLTimeOfThang(time_tuan_data, start_date, time_tl_thang_data);
                                } else if (missionPeriodType == MissionPeriodType.QUY) {
                                    String time_tl_thang_data = this.dataManager.getConfigManager().getMPMissionConfig().get(
                                            MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG).toString();
                                    end_tl_date = this.appUtils.getMissionPeriodEndTLTimeOfThang(time_tuan_data, start_date, time_tl_thang_data);
                                }
                                ClientResponse crAssign = this.assignMissionService.assignMissionForOneAgency(
                                        agency_id,
                                        missionPeriodType.name(),
                                        start_date.getTime(),
                                        end_date.getTime(),
                                        end_tl_date.getTime()
                                );
                                if (crAssign.failed()) {
                                    this.alertToTelegram("giaoNhiemVu: " + agency_id + " - " + crAssign, ResponseStatus.EXCEPTION);
                                }
                            }
                        } else {
                            this.alertToTelegram("giaoNhiemVu: " + agency_id + "-" + end_date, ResponseStatus.EXCEPTION);
                        }
                    }
            );
        } catch (Exception e) {
            LogUtil.printDebug("AGENCY", e);
        }
    }

    private void addAgencyToMissionSetting(int agencyId) {
        try {
            List<JSONObject> missionSettingList = this.missionDB.getListMissionSettingRunning();
            for (JSONObject jsMissionSetting : missionSettingList) {
                int mission_setting_id = ConvertUtils.toInt(jsMissionSetting.get("id"));
                List<Integer> agencyList = this.missionDB.getListMissionSettingAgencyJoin(
                        mission_setting_id
                ).stream().map(e -> ConvertUtils.toInt(e.get("agency_id"))).collect(Collectors.toList());
                if (!agencyList.contains(agencyId)) {
                    String data = JsonUtils.Serialize(
                            this.convertMissionSettingDataToPromoData(jsMissionSetting));
                    if (this.checkAgencyJoinMissionSetting(agencyId, data)) {
                        agencyList.add(agencyId);
                        this.missionDB.insertMissionSettingAgencyJoin(
                                agencyId,
                                mission_setting_id);

                        this.missionDB.updateAgencyDataForMissionSettingAgency(
                                ConvertUtils.toInt(jsMissionSetting.get("id")),
                                JsonUtils.Serialize(agencyList));
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.printDebug("AGENCY", e);
        }
    }

    private boolean checkAgencyHasMission(
            int agencyId,
            int mission_type_id) {
        try {
            List<JSONObject> missionList = this.agencyDB.getListAgencyMissionByAgency(agencyId, mission_type_id);
            if (missionList.size() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printDebug("AGENCY", e);
        }
        return true;
    }

    private boolean checkAgencyHasAnyMission(
            int agencyId) {
        try {
            List<JSONObject> missionList = this.agencyDB.getAllAgencyMissionByAgency(agencyId);
            if (missionList.size() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printDebug("AGENCY", e);
        }
        return true;
    }

    public void addAgencyToMissionBXH(int agency_id) {
        try {
            Agency agency = this.dataManager.getProgramManager().getAgency(agency_id);
            if (agency == null) {
                return;
            }

            /**
             * Bang Thanh Tich 01
             */
            JSONObject jsMissionBxhAgencyJoin01 = this.missionDB.getMissionBXHAgencyJoin(MissionBXHType.BANG_THANH_TICH_01.getId(), agency_id);
            if (jsMissionBxhAgencyJoin01 == null) {
                List<JSONObject> missionBXHList = this.missionDB.getListMissionBXHRunningByType(MissionBXHType.BANG_THANH_TICH_01.getId());
                for (JSONObject missionBXH : missionBXHList) {
                    int mission_bxh_id = ConvertUtils.toInt(missionBXH.get("id"));
                    CreatePromoRequest promo_data = this.createPromoDataFilter(
                            missionBXH,
                            JsonUtils.DeSerialize(
                                    missionBXH.get("apply_object_data").toString(),
                                    ApplyObjectRequest.class)
                    );

                    Program program = this.dataManager.getProgramManager().importProgram(
                            JsonUtils.Serialize(promo_data));
                    if (program == null) {
                        return;
                    }

                    if (this.checkProgramFilter(
                            agency,
                            program,
                            null, null)) {
                        List<Integer> agencyList =
                                JsonUtils.DeSerialize(missionBXH.get("agency_data").toString(), new TypeToken<List<Integer>>() {
                                }.getType());
                        if (!agencyList.contains(agency_id)) {
                            agencyList.add(agency_id);
                            this.missionDB.updateAgencyDataForMissionBXH(mission_bxh_id, JsonUtils.Serialize(agencyList));
                        }

                        this.missionDB.insertMissionBXHAgencyJoin(
                                agency_id,
                                ConvertUtils.toInt(missionBXH.get("id")),
                                ConvertUtils.toInt(missionBXH.get("type")));

                        break;
                    }
                }
            }

            /**
             * Bang Thanh Tich 02
             */
            JSONObject jsMissionBxhAgencyJoin02 = this.missionDB.getMissionBXHAgencyJoin(MissionBXHType.BANG_THANH_TICH_02.getId(), agency_id);
            if (jsMissionBxhAgencyJoin02 == null) {
                List<JSONObject> missionBXHList = this.missionDB.getListMissionBXHRunningByType(MissionBXHType.BANG_THANH_TICH_02.getId());
                for (JSONObject missionBXH : missionBXHList) {
                    int mission_bxh_id = ConvertUtils.toInt(missionBXH.get("id"));
                    CreatePromoRequest promo_data = this.createPromoDataFilter(
                            missionBXH,
                            JsonUtils.DeSerialize(
                                    missionBXH.get("apply_object_data").toString(),
                                    ApplyObjectRequest.class)
                    );

                    Program program = this.dataManager.getProgramManager().importProgram(
                            JsonUtils.Serialize(promo_data));
                    if (program == null) {
                        return;
                    }

                    if (this.checkProgramFilter(
                            agency,
                            program,
                            null, null)) {
                        List<Integer> agencyList =
                                JsonUtils.DeSerialize(missionBXH.get("agency_data").toString(), new TypeToken<List<Integer>>() {
                                }.getType());
                        if (!agencyList.contains(agency_id)) {
                            agencyList.add(agency_id);
                            this.missionDB.updateAgencyDataForMissionBXH(mission_bxh_id, JsonUtils.Serialize(agencyList));
                        }

                        this.missionDB.insertMissionBXHAgencyJoin(
                                agency_id,
                                ConvertUtils.toInt(missionBXH.get("id")),
                                ConvertUtils.toInt(missionBXH.get("type")));

                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.printDebug("AGENCY", e);
        }
    }

    public ClientResponse giaoNhiemVuKyDai() {
        try {
            int ky_dai = this.dataManager.getConfigManager().getKyDai();
            int ky_ngan = this.dataManager.getConfigManager().getKyNgan();

            this.resetMissionSettingAgencyData();

            this.generateAll(Arrays.asList(ky_ngan, ky_dai));
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("AGENCY", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse giaoNhiemVuKyNgan() {
        try {
            int ky_ngan = this.dataManager.getConfigManager().getKyNgan();
            this.generateAll(Arrays.asList(ky_ngan));
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("AGENCY", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runCallResetMissionPoint() {
        try {
            int ky_dai = this.dataManager.getConfigManager().getKyDai();
            MissionPeriodType missionPeriodType = MissionPeriodType.from(ky_dai);

            switch (missionPeriodType) {
                case THANG: {
                    Date now = DateTimeUtils.getNow();
                    int month = ConvertUtils.toInt(this.appUtils.getMonth(now));
                    int day = ConvertUtils.toInt(this.appUtils.getDay(now));
                    int last_day_of_month = ConvertUtils.toInt(this.appUtils.getLastDayOfMonth(now));
                    if (day == last_day_of_month) {
                        this.callResetMissionPoint();
                    }
                    break;
                }
                case QUY: {
                    Date now = DateTimeUtils.getNow();
                    int month = ConvertUtils.toInt(this.appUtils.getMonth(now));
                    int quy = this.getQuy(now);
                    String ngay_key_thuc_quy = getNgayKetThucQuy(quy);
                    String ngay_hien_tai = DateTimeUtils.toString(now, "MM-dd");
                    if (ngay_key_thuc_quy.equals(ngay_hien_tai)) {
                        this.callResetMissionPoint();
                    }
                    break;
                }
            }
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse callResetMissionPoint() {
        try {
            return this.accumulateMissionService.resetMissionPoint();
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse callResetMissionAll() {
        try {
            return this.accumulateMissionService.resetMissionAll();
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse giaoNhiemVuTheoThietLap(int mission_setting_id) {
        try {
            JSONObject jsMissionSetting = this.missionDB.getMissionSettingInfo(mission_setting_id);
            if (jsMissionSetting == null) {
                this.alertToTelegram("generateMission: " + mission_setting_id, ResponseStatus.EXCEPTION);
            }

            String agency_data = JsonUtils.Serialize(
                    this.convertMissionSettingDataToPromoData(jsMissionSetting));
            List<Agency> agencyJoinList = this.getListAgencyByFilter(agency_data);

            Date start_date = DateTimeUtils.getNow();

            List<Integer> missionPeriodRunningList = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(
                            MissionConstants.KY_NHIEM_VU),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            List<JSONObject> data = new ArrayList<>();
            String time_tuan_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN).toString();
            String time_thang_data = this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_THANG).toString();
            missionPeriodRunningList.forEach(
                    mp -> {
                        MissionPeriodType missionPeriodType = MissionPeriodType.from(mp);
                        Date end_date = this.getMissionPeriodEndTime(missionPeriodType, start_date);
                        String mission_period_code = this.generateMissionPeriodCode(
                                missionPeriodType,
                                start_date,
                                end_date);
                        agencyJoinList.forEach(
                                agency -> {
                                    List<JSONObject> AgencyMissionList = this.missionDB.getListAgencyMission(
                                            agency.getId(),
                                            mp.intValue());
                                    if (AgencyMissionList.isEmpty()) {
                                        int rsInsertMissionPeriodAgency = this.missionDB.insertMissionPeriodAgency(
                                                agency.getId(), missionPeriodType.getId(),
                                                start_date,
                                                end_date,
                                                mission_period_code,
                                                mission_setting_id
                                        );
                                        if (rsInsertMissionPeriodAgency <= 0) {
                                            this.alertToTelegram("insertMissionPeriodAgency:" +
                                                    agency.getId() + "," +
                                                    missionPeriodType.getId() + "," +
                                                    start_date + "," +
                                                    end_date + "," +
                                                    mission_period_code + "," +
                                                    mission_setting_id, ResponseStatus.EXCEPTION);
                                        }
                                    }
                                }
                        );

                        switch (missionPeriodType) {
                            case TUAN: {
                                JSONObject jsPeriod = new JSONObject();
                                jsPeriod.put("id", mission_setting_id);
                                jsPeriod.put("mission_period_id", missionPeriodType.getId());
                                jsPeriod.put("type", missionPeriodType.getKey());
                                jsPeriod.put("start_time", start_date.getTime());
                                jsPeriod.put("end_time",
                                        this.appUtils.getMissionPeriodEndTimeOfTuan(
                                                time_tuan_data,
                                                start_date, time_thang_data).getTime());
                                data.add(jsPeriod);
                                break;
                            }
                            case THANG: {
                                JSONObject jsPeriod = new JSONObject();
                                jsPeriod.put("id", mission_setting_id);
                                jsPeriod.put("mission_period_id", missionPeriodType.getId());
                                jsPeriod.put("type", missionPeriodType.getKey());
                                jsPeriod.put("start_time", start_date.getTime());
                                jsPeriod.put("end_time",
                                        this.appUtils.getMissionPeriodEndTimeOfThang(
                                                time_tuan_data,
                                                start_date, time_thang_data).getTime());
                                data.add(jsPeriod);
                                break;
                            }
                            case QUY:
                                JSONObject jsPeriod = new JSONObject();
                                jsPeriod.put("id", mission_setting_id);
                                jsPeriod.put("mission_period_id", missionPeriodType.getId());
                                jsPeriod.put("type", missionPeriodType.getKey());
                                jsPeriod.put("start_time", start_date.getTime());
                                jsPeriod.put("end_time",
                                        DateTimeUtils.getDateTime(
                                                this.appUtils.getYear(start_date) + " -" +
                                                        this.getNgayKetThucQuy(this.getQuy(start_date)) + " 23:59:59",
                                                "yyyy-MM-dd HH:mm:ss").getTime());
                                data.add(jsPeriod);
                                break;
                            default:
                                this.alertToTelegram("generateAll: " + mp, ResponseStatus.EXCEPTION);
                        }
                    }
            );

            ClientResponse clientResponse = this.assignMissionService.assignMissionBySetting(
                    JsonUtils.Serialize(data)
            );
            if (clientResponse.failed()) {
                this.alertToTelegram(clientResponse.getMessage(), ResponseStatus.EXCEPTION);
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse activeMissionGroup(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject missionGroupInfo = this.missionDB.getMissionGroupInfo(request.getId());
            if (missionGroupInfo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int status = ConvertUtils.toInt(missionGroupInfo.get("status"));
            if (MissionGroupStatus.RUNNING.getId() == status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsActive = this.missionDB.activeMissionGroup(request.getId());
            if (!rsActive) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse stopMissionGroup(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject missionGroupInfo = this.missionDB.getMissionGroupInfo(request.getId());
            if (missionGroupInfo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int status = ConvertUtils.toInt(missionGroupInfo.get("status"));
            if (MissionSettingStatus.STOP.getId() == status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            List<JSONObject> missionSettingList = this.missionDB.getListMissionSetting(
                    request.getId(), MissionSettingStatus.RUNNING.getId());
            if (!missionSettingList.isEmpty()) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Vui lòng kết thúc thiết lập giao nhiệm vụ");
            }

            boolean rsStop = this.missionDB.stopMissionGroup(request.getId());
            if (!rsStop) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse doubleCheckMissionGroup(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject missionGroupInfo = this.missionDB.getMissionGroupInfo(request.getId());
            if (missionGroupInfo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            JSONObject data = new JSONObject();

            data.put("records", this.missionDB.getListMissionSettingByMissionGroup(request.getId()));

            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public String getMissionName(
            MissionType missionType,
            String name,
            String item_data,
            int action_number,
            int require_value,
            int mission_unit_id) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);
        if (missionType == MissionType.MUA_HANG) {
            name = name.replace("[1]", getMissionItemName(item_data));
            name = name.replace("[2]", numberFormat.format(action_number));
            name = name.replace("[3]", numberFormat.format(require_value));
            name = name.replace("[4]", MissionUnitType.from(mission_unit_id).getLabel());
        } else if (missionType == MissionType.THANH_TOAN)
            name = name.replace("[3]", numberFormat.format(require_value));
        return name;
    }

    /**
     * Lấy tên thông tin mô tả cho nhiệm vụ mua hàng
     */
    private String getMissionItemName(
            String item_data) {
        String name = "";
        try {
            JSONObject jsItemData = JsonUtils.DeSerialize(item_data, JSONObject.class);
            if (jsItemData == null) {
                return name;
            }
            if (jsItemData.get("type") == null || jsItemData.get("type").toString().isEmpty()) {
                return name;
            }
            List<String> ltName = new ArrayList<>();
            if (!jsItemData.get("type").toString().equals("ALL")) {
                List<ItemInfo> itemInfoList = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(jsItemData.get("items")), new TypeToken<List<ItemInfo>>() {
                        }.getType());
                for (ItemInfo itemInfo : itemInfoList) {
                    MissionProductType missionProductType = MissionProductType.fromKey(itemInfo.getItem_type());
                    if (missionProductType == MissionProductType.PRODUCT) {
                        ltName.add(this.dataManager.getProductManager().getProductFullName(itemInfo.getItem_id()));
                    } else if (missionProductType == MissionProductType.PRODUCT_GROUP) {
                        ltName.add(this.dataManager.getProductManager().getMpProductGroup().get(itemInfo.getItem_id()).getName());
                    } else if (missionProductType == MissionProductType.CATEGORY) {
                        ltName.add(this.dataManager.getProductManager().getCategoryById(itemInfo.getItem_id()).getName());
                    } else if (missionProductType == MissionProductType.ITEM_TYPE) {
                        ltName.add(ItemType.from(itemInfo.getItem_id()).name());
                    }
                }
            }
            if (!ltName.isEmpty())
                name = ltName.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return name;
    }

    public ClientResponse getMissionEndTime(BasicRequest request) {
        try {
            List<JSONObject> mission_period_running_list = this.missionDB.getListMissionPeriodRunning();
            JSONObject data = new JSONObject();

            JSONObject mission_setting_agency = this.missionDB.getOneMissionSettingOfAgency(
                    request.getId());
            if (mission_setting_agency != null) {
                data.put("mission_setting_info", this.missionDB.getMissionSettingInfo(ConvertUtils.toInt(mission_setting_agency.get("mission_setting_id"))));
            }

            data.put("records", mission_period_running_list);
            data.put("bang_thanh_tich_01", this.missionDB.getListMissionBXHByAgency(request.getId(), MissionBXHType.BANG_THANH_TICH_01.getId()));
            data.put("bang_thanh_tich_02", this.missionDB.getListMissionBXHByAgency(request.getId(), MissionBXHType.BANG_THANH_TICH_02.getId()));

            /* Bảng thành tích đang tham gia */

            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runResetMissionBXH() {
        try {
            Date now = new Date();
            int day = ConvertUtils.toInt(this.appUtils.getDay(now));
            if (day == 1) {
                this.stopMissionBXHNotRepeatOfThang();

                /**
                 * Reset đại lý tham gia bảng xếp hạng tháng
                 */
                this.resetAgencyDataForMissionBXHForNhiemVuThang(MissionBXHType.BANG_THANH_TICH_01);
                this.resetAgencyDataForMissionBXHForNhiemVuThang(MissionBXHType.BANG_THANH_TICH_02);

                /**
                 * Reset đại lý tham gia bảng xếp hạng quý
                 */
                int quy = this.getQuy(now);
                int month = ConvertUtils.toInt(this.appUtils.getMonth(now));
                if (kiemTraThangBatDauCuaQuy(month, quy)) {
                    this.stopMissionBXHNotRepeatOfQuy();

                    this.resetAgencyDataForMissionBXHForNhiemVuQuy(MissionBXHType.BANG_THANH_TICH_01);
                    this.resetAgencyDataForMissionBXHForNhiemVuQuy(MissionBXHType.BANG_THANH_TICH_02);
                }
            }
            return ResponseConstants.success;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ResponseConstants.failed;
    }

    private void stopMissionBXHNotRepeatOfThang() {
        try {
            this.missionDB.stopMissionBXHNotRepeat(MissionPeriodType.THANG.getId());
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private void stopMissionBXHNotRepeatOfQuy() {
        try {
            this.missionDB.stopMissionBXHNotRepeat(MissionPeriodType.QUY.getId());
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private boolean kiemTraThangBatDauCuaQuy(int month, int quy) {
        switch (quy) {
            case 1: {
                return month == 1;
            }
            case 2: {
                return month == 4;
            }
            case 3: {
                return month == 7;
            }
            case 4: {
                return month == 10;
            }
        }
        return false;
    }

    private void resetAgencyDataForMissionBXHForNhiemVuThang(MissionBXHType missionBXHType) {
        try {
            this.missionDB.clearAgencyDataForMissionBXH(missionBXHType);
            List<JSONObject> missionBXHList = this.missionDB.getListMissionBXHRunningByType(missionBXHType.getId());
            for (JSONObject missionBXH : missionBXHList) {
                int mission_bxh_id = ConvertUtils.toInt(missionBXH.get("id"));
                CreatePromoRequest promo_data = this.createPromoDataFilter(
                        missionBXH,
                        JsonUtils.DeSerialize(
                                missionBXH.get("apply_object_data").toString(),
                                ApplyObjectRequest.class));
                List<Agency> agencyList = this.getListAgencyByFilter(JsonUtils.Serialize(promo_data));
                List<Agency> mission_bxh_agency_join_list = new ArrayList<>();
                agencyList.forEach(
                        agency -> {
                            if (this.missionDB.getMissionBXHAgencyJoin(missionBXHType.getId(), agency.getId()) == null) {
                                mission_bxh_agency_join_list.add(agency);
                            }
                        }
                );
                this.missionDB.updateAgencyDataForMissionBXH(
                        mission_bxh_id,
                        JsonUtils.Serialize(mission_bxh_agency_join_list.stream().map(
                                e -> e.getId()
                        ).collect(Collectors.toList())));

                for (Agency agency : mission_bxh_agency_join_list) {
                    this.missionDB.insertMissionBXHAgencyJoin(agency.getId(), mission_bxh_id, missionBXHType.getId());
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private void resetAgencyDataForMissionBXHForNhiemVuQuy(MissionBXHType missionBXHType) {
        try {
            this.missionDB.clearAgencyDataForMissionBXH(missionBXHType);
            List<JSONObject> missionBXHList = this.missionDB.getListMissionBXHRunningByType(missionBXHType.getId());
            for (JSONObject missionBXH : missionBXHList) {
                int mission_bxh_id = ConvertUtils.toInt(missionBXH.get("id"));
                CreatePromoRequest promo_data = this.createPromoDataFilter(
                        missionBXH,
                        JsonUtils.DeSerialize(
                                missionBXH.get("apply_object_data").toString(),
                                ApplyObjectRequest.class));
                List<Agency> agencyList = this.getListAgencyByFilter(JsonUtils.Serialize(promo_data));
                List<Agency> mission_bxh_agency_join_list = new ArrayList<>();
                agencyList.forEach(
                        agency -> {
                            if (this.missionDB.getMissionBXHAgencyJoin(missionBXHType.getId(), agency.getId()) == null) {
                                mission_bxh_agency_join_list.add(agency);
                            }
                        }
                );
                this.missionDB.updateAgencyDataForMissionBXH(
                        mission_bxh_id,
                        JsonUtils.Serialize(mission_bxh_agency_join_list.stream().map(
                                e -> e.getId()
                        ).collect(Collectors.toList())));

                for (Agency agency : mission_bxh_agency_join_list) {
                    this.missionDB.insertMissionBXHAgencyJoin(agency.getId(), mission_bxh_id, missionBXHType.getId());
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    public ClientResponse searchMission(FilterListRequest request) {
        try {
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("status");
            filterRequest.setValue(ConvertUtils.toString(MissionGroupStatus.RUNNING.getId()));
            filterRequest.setType(TypeFilter.SELECTBOX);
            request.getFilters().add(filterRequest);

            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_BY_GROUP, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterAgencyMissionHistoryOfAgency(SessionData sessionData, FilterListByIdRequest request) {
        try {
            request.getFilters().forEach(f -> {
                if (f.getKey().equals("created_date")) {
                    f.setKey("thoi_gian_giao");
                }
            });

            FilterRequest agencyFilterRequest = new FilterRequest();
            agencyFilterRequest.setKey("agency_id");
            agencyFilterRequest.setType(TypeFilter.SELECTBOX);
            agencyFilterRequest.setValue(ConvertUtils.toString(request.getId()));
            request.getFilters().add(agencyFilterRequest);

            String query = this.filterUtils.getQuery(FunctionList.FILTER_AGENCY_MISSION_HISTORY, request.getFilters(), request.getSorts());
            List<JSONObject> missionHistory = this.missionLogDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());

            List<JSONObject> records = new ArrayList<>();
            for (JSONObject jsMission : missionHistory) {
                jsMission.put("mission_name", this.convertMissionName(jsMission));
                JSONObject jsMissionSettingInfo = this.missionDB.getMissionSettingInfo(
                        ConvertUtils.toInt(jsMission.get("mission_setting_id")));
                jsMission.put("mission_setting_info", jsMissionSettingInfo);
                int status = ConvertUtils.toInt(jsMission.get("mission_status"));
                jsMission.put("so_huy_hieu_thuong", jsMission.get("mission_reward_point"));
                if (MissionAgencyStatus.FINISH.getId() == status) {
                    jsMission.put("so_huy_hieu_thuc_nhan", jsMission.get("so_huy_hieu_thuong"));
                } else {
                    jsMission.put("so_huy_hieu_thuc_nhan", 0);
                }

                jsMission.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(jsMission.get("agency_id"))
                ));

                if (jsMissionSettingInfo != null) {
                    jsMission.put("mission_group_info", this.missionDB.getMissionGroupInfo(
                            ConvertUtils.toInt(jsMissionSettingInfo.get("mission_group_id"))));
                }
                jsMission.put("status", status);

                records.add(jsMission);
            }
            int total = this.missionLogDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(request.getId()));
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getResultMissionBXHInfoOfAgency(SessionData sessionData, GetResultMissionBXHOfAgencyRequest request) {
        try {
            int agency_id = request.getAgency_id();
            int mission_bxh_id = request.getBtt_id();

            JSONObject jsMissionBXH = this.missionDB.getMissionBXH(mission_bxh_id);
            if (jsMissionBXH == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            MissionBXHData missionBXHData = this.convertMissionBXHData(jsMissionBXH);
            if (missionBXHData == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            MissionPeriodType missionPeriodType = MissionPeriodType.from(missionBXHData.getInfo().getMission_period_id());
            Date now = new Date();
            switch (missionPeriodType) {
                case THANG: {
                    int month = ConvertUtils.toInt(this.appUtils.getMonth(now));
                    int year = ConvertUtils.toInt(this.appUtils.getYear(now));
                    if ((request.getPeriod() == 0 && request.getYear() == 0) || (year == request.getYear() && month == request.getPeriod())) {
                        Date start_date = this.getStartDateOfMissionBXH(missionBXHData.getInfo().getMission_period_id(), now);
                        Date end_date = this.getEndDateOfMissionBXH(missionBXHData.getInfo().getMission_period_id(), now);
                        return getResultMissionBXHInfoOfAgencyByCurrent(
                                mission_bxh_id,
                                missionBXHData,
                                agency_id,
                                start_date,
                                end_date,
                                jsMissionBXH.get("agency_data").toString());
                    } else {
                        Date start_date = DateTimeUtils.getDateTime(
                                year + "-" + String.format("%02d", request.getPeriod()) + "-01 00:00:00",
                                "yyyy-MM-dd HH:mm:ss");
                        Date end_date = DateTimeUtils.getDateTime(
                                year + "-" + String.format("%02d", request.getPeriod()) +
                                        "-" + String.format("%02d", this.appUtils.getLastDayOfMonth(start_date)) +
                                        " 23:59:59",
                                "yyyy-MM-dd HH:mm:ss");
                        return getResultMissionBXHInfoOfAgencyByCurrent(
                                mission_bxh_id,
                                missionBXHData,
                                agency_id,
                                start_date,
                                end_date,
                                jsMissionBXH.get("agency_data").toString());
                    }
                }
                case QUY: {
                    int quy = this.getQuy(now);
                    int year = ConvertUtils.toInt(this.appUtils.getYear(now));
                    if (request.getYear() != 0) {
                        year = request.getYear();
                    }
                    if (request.getPeriod() != 0) {
                        quy = request.getPeriod();
                    }
                    Date start_date = DateTimeUtils.getDateTime(
                            year + "-" + this.getNgayBatDauQuy(quy) + " 00:00:00",
                            "yyyy-MM-dd HH:mm:ss");
                    Date end_date = DateTimeUtils.getDateTime(
                            year + "-" + this.getNgayKetThucQuy(quy) + " 23:59:59",
                            "yyyy-MM-dd HH:mm:ss");
                    return getResultMissionBXHInfoOfAgencyByCurrent(
                            mission_bxh_id,
                            missionBXHData,
                            agency_id,
                            start_date,
                            end_date,
                            jsMissionBXH.get("agency_data").toString());
                }
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse getResultMissionBXHInfoOfAgencyByCurrent(
            int mission_bxh_id,
            MissionBXHData missionBXHData,
            int agency_id,
            Date start_date,
            Date end_date,
            String agency_join_data
    ) {
        try {
            long require_accumulate_value = missionBXHData.getInfo().getRequire_accumulate_value();

            List<MissionBXHLimitRequest> promoOfferList = missionBXHData.getLimits();

            JSONObject agencyInfo = this.missionDB.getAgencyInfo(agency_id);

            JSONObject result = new JSONObject();
            int rank_value = this.getAgencyMissionBXHPoint(agency_id, start_date, end_date);
            result.put("agency_id", agency_id);
            result.put("agency_info", agencyInfo);

            /**
             * nickname
             */
            result.put("nickname", this.getNickName(agencyInfo));
            result.put("tong_gia_tri_tich_luy", rank_value);

            /**
             * Hạng hiện tại
             */

            if (rank_value >= require_accumulate_value) {
                List<MissionBXHAgencyRankData> agencyRankList = this.getListAgencyRank(
                        mission_bxh_id,
                        JsonUtils.DeSerialize(agency_join_data, new TypeToken<List<Integer>>() {
                        }.getType()),
                        start_date, end_date);
                int rank = this.getAgencyRank(agency_id, agencyRankList);
                result.put("hang_hien_tai", rank);

                /**
                 * Xếp hạng có quà
                 */
                if (rank <= promoOfferList.size()) {
                    /**
                     * phần trăm và giá trị nếu là ưu đãi giảm tiền
                     */
                    MissionBXHLimitRequest promoOffer = promoOfferList.get(rank - 1);
                    if (promoOffer != null) {
                        List<JSONObject> vrps = this.getListVRPByRank(rank, promoOfferList);

                        MissionBXHOfferRequest offer = promoOffer.getGroups().get(0).getOffer();
                        result.put("uu_dai_acoin", offer.getOffer_acoin_value());
                        result.put("uu_dai_giam_tien", offer.getOffer_value());
                        long dtt = this.getGiaTriDTT(agency_id,
                                start_date,
                                end_date);
                        result.put("dtt", dtt);
                        result.put("uu_dai_gia_tri_dtt", dtt <= 0 ? 0 : this.getGiaTriUuDaiDTT(
                                dtt,
                                offer.getOffer_dtt_value()));
                        result.put("uu_dai_phan_tram_dtt", offer.getOffer_dtt_value());

                        /**
                         * Ưu đãi quà tặng
                         */
                        List<JSONObject> uu_dai_qua_tang = new ArrayList<>();
                        for (JSONObject vrp : vrps) {
                            if (VoucherOfferType.GIFT_OFFER.getKey().equals(
                                    ConvertUtils.toString(vrp.get("offer_type")))) {
                                List<JSONObject> gifts = this.missionDB.getListVRPItem(ConvertUtils.toInt(vrp.get("id")));
                                for (JSONObject gift : gifts) {
                                    gift.put("product_info", this.dataManager.getProductManager().getProductBasicData(ConvertUtils.toInt(gift.get("item_id"))));
                                }
                                vrp.put("item_data", gifts);
                                uu_dai_qua_tang.add(vrp);
                            }
                        }
                        result.put("vrps", vrps);
                        result.put("uu_dai_qua_tang", uu_dai_qua_tang);
                    }
                }
            }

            JSONObject data = new JSONObject();
            data.put("result", result);
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private Date getStartDateOfMissionBXH(int missionPeriodId, Date now) {
        try {
            MissionPeriodType missionPeriodType = MissionPeriodType.from(missionPeriodId);
            switch (missionPeriodType) {
                case THANG: {
                    return DateTimeUtils.getDateTime(
                            DateTimeUtils.toString(now, "yyyy-MM") + "-01 00:00:00",
                            "yyyy-MM-dd HH:mm:ss");
                }
                case QUY: {
                    int quy = this.getQuy(now);
                    return DateTimeUtils.getDateTime(
                            DateTimeUtils.toString(now, "yyyy") + "-" + this.getNgayBatDauQuy(quy) + " 00:00:00",
                            "yyyy-MM-dd HH:mm:ss");
                }
                default:
                    return now;
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    private Date getEndDateOfMissionBXH(int missionPeriodId, Date now) {
        try {
            MissionPeriodType missionPeriodType = MissionPeriodType.from(missionPeriodId);
            switch (missionPeriodType) {
                case THANG: {
                    return this.appUtils.convertStringToDate(
                            this.appUtils.convertDateToString(now, "yyyy-MM") + "-" + String.format("%02d", this.appUtils.getLastDayOfMonth(now)) + " 23:59:59",
                            "yyyy-MM-dd HH:mm:ss");
                }
                case QUY: {
                    int quy = this.getQuy(now);
                    return this.appUtils.convertStringToDate(
                            this.appUtils.convertDateToString(now, "yyyy") + "-" + this.getNgayKetThucQuy(quy) + " 23:59:59",
                            "yyyy-MM-dd HH:mm:ss");
                }
                default:
                    return now;
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return null;
    }

    public ClientResponse filterMissionBXHRewardHistory(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_BXH_REWARD_HISTORY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionLogDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionLogDB.getTotal(query);
            for (JSONObject js : records) {
                JSONObject jsMissionBXH = this.missionDB.getMissionBXH(ConvertUtils.toInt(js.get("mission_bxh_id")));
                js.put("mission_bxh_info", jsMissionBXH);
                int agency_id = ConvertUtils.toInt(js.get("agency_id"));
                js.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(agency_id));

                this.convertMissionBXHReward(
                        js, this.convertMissionBXHData(jsMissionBXH),
                        ConvertUtils.toInt(js.get("position")),
                        agency_id,
                        ConvertUtils.toInt(js.get("mission_period_running_id"))
                );
            }
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void convertMissionBXHReward(JSONObject result, MissionBXHData missionBXHData, int position, int agency_id, int agency_mission_period_running_id) {
        try {
            Date created_date = DateTimeUtils.getDateTime(result.get("created_date").toString(), "yyyy-MM-dd HH:mm:ss");
            int mission_period_id = ConvertUtils.toInt(result.get("mission_period_id"));
            Date start_date = this.getStartDateOfMissionBXH(mission_period_id, created_date);
            Date end_date = this.getEndDateOfMissionBXH(mission_period_id, created_date);
            List<MissionBXHLimitRequest> promoOfferList = missionBXHData.getLimits();
            MissionBXHLimitRequest promoOffer = promoOfferList.get(position - 1);
            if (promoOffer != null) {
                List<JSONObject> vrps = this.getListVRPByRank(position, promoOfferList);
                MissionBXHOfferRequest offer = promoOffer.getGroups().get(0).getOffer();
                result.put("uu_dai_acoin", offer.getOffer_acoin_value());
                result.put("uu_dai_giam_tien", offer.getOffer_value());
                long dtt = this.getGiaTriDTT(
                        agency_id,
                        start_date,
                        end_date
                );
                result.put("dtt", dtt);
                result.put("uu_dai_gia_tri_dtt", dtt <= 0 ? 0 : this.getGiaTriUuDaiDTT(
                        dtt,
                        offer.getOffer_dtt_value()));
                result.put("uu_dai_phan_tram_dtt", offer.getOffer_dtt_value());

                /**
                 * Ưu đãi quà tặng
                 */
                List<JSONObject> uu_dai_qua_tang = new ArrayList<>();
                for (JSONObject vrp : vrps) {
                    if (VoucherOfferType.GIFT_OFFER.getKey().equals(
                            ConvertUtils.toString(vrp.get("offer_type")))) {
                        List<JSONObject> gifts = this.missionDB.getListVRPItem(ConvertUtils.toInt(vrp.get("id")));
                        for (JSONObject gift : gifts) {
                            gift.put("product_info", this.dataManager.getProductManager().getProductBasicData(ConvertUtils.toInt(gift.get("item_id"))));
                        }
                        vrp.put("item_data", gifts);
                        uu_dai_qua_tang.add(vrp);
                    }
                }
                result.put("vrps", vrps);
                result.put("uu_dai_qua_tang", uu_dai_qua_tang);
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    public ClientResponse searchMissionBXH(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_MISSION_BXH, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, 1);
            int total = this.missionDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse filterTKMissionBXHAgencyByPeriod(
            SessionData sessionData,
            FilterTKAgencyMissionBXHRequest request,
            int mission_bxh_id,
            MissionBXHData missionBXHData, Date start_date, Date end_date) {
        try {
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(mission_bxh_id));
            filterRequest.setKey("t.mission_bxh_id");
            request.getFilters().add(filterRequest);

            String mission_period_code = this.getMissionPeriodCode(
                    request.getYear(), request.getPeriod());

            List<JSONObject> agencyJoinList = this.missionLogDB.getListAgencyMissionBXHAgencyByPeriod(
                    mission_bxh_id, start_date, end_date);
            List<JSONObject> records = new ArrayList<>();

            long require_accumulate_value = missionBXHData.getInfo().getRequire_accumulate_value();

            List<MissionBXHLimitRequest> promoOfferList = missionBXHData.getLimits();

            AtomicInteger agency_rank = new AtomicInteger();
            agencyJoinList.forEach(
                    agencyJoin -> {
                        agency_rank.getAndIncrement();
                        JSONObject agency = new JSONObject();
                        int agency_id = ConvertUtils.toInt(agencyJoin.get("agency_id"));
                        int rank_value = this.getAgencyMissionBXHPoint(agency_id, start_date, end_date);
                        agency.put("agency_id", agency_id);
                        JSONObject jsAgencyInfo = this.missionDB.getAgencyInfo(agency_id);
                        agency.put("agency_info", jsAgencyInfo);

                        /**
                         * nickname
                         */
                        agency.put("nickname", this.getNickName(jsAgencyInfo));
                        agency.put("tong_gia_tri_tich_luy", rank_value);

                        /**
                         * Hạng hiện tại
                         *
                         */
                        if (rank_value >= require_accumulate_value) {
                            int rank = agency_rank.get();
                            agency.put("hang_hien_tai", rank);

                            /**
                             * Xếp hạng có quà
                             */
                            if (rank <= promoOfferList.size()) {
                                /**
                                 * phần trăm và giá trị nếu là ưu đãi giảm tiền
                                 */
                                MissionBXHLimitRequest promoOffer = promoOfferList.get(rank - 1);
                                if (promoOffer != null) {
                                    List<JSONObject> vrps = this.getListVRPByRank(rank, promoOfferList);

                                    MissionBXHOfferRequest offer = promoOffer.getGroups().get(0).getOffer();
                                    agency.put("uu_dai_acoin", offer.getOffer_acoin_value());
                                    agency.put("uu_dai_giam_tien", offer.getOffer_value());
                                    long dtt = this.getGiaTriDTT(agency_id,
                                            start_date,
                                            end_date);
                                    agency.put("dtt", dtt);
                                    agency.put("uu_dai_gia_tri_dtt", dtt <= 0 ? 0 : this.getGiaTriUuDaiDTT(
                                            dtt,
                                            offer.getOffer_dtt_value()));
                                    agency.put("uu_dai_phan_tram_dtt", offer.getOffer_dtt_value());

                                    /**
                                     * Ưu đãi quà tặng
                                     */
                                    List<JSONObject> uu_dai_qua_tang = new ArrayList<>();
                                    for (JSONObject vrp : vrps) {
                                        if (VoucherOfferType.GIFT_OFFER.getKey().equals(
                                                ConvertUtils.toString(vrp.get("offer_type")))) {
                                            List<JSONObject> gifts = this.missionDB.getListVRPItem(ConvertUtils.toInt(vrp.get("id")));
                                            for (JSONObject gift : gifts) {
                                                gift.put("product_info", this.dataManager.getProductManager().getProductBasicData(ConvertUtils.toInt(gift.get("item_id"))));
                                            }
                                            vrp.put("item_data", gifts);
                                            uu_dai_qua_tang.add(vrp);
                                        }
                                    }
                                    agency.put("vrps", vrps);
                                    agency.put("uu_dai_qua_tang", uu_dai_qua_tang);
                                }
                            }
                        }
                        records.add(agency);
                    }
            );
            int total = agencyJoinList.size();
            int from = this.appUtils.getOffset(request.getPage());
            int to = from + ConfigInfo.PAGE_SIZE;
            if (to > total) {
                to = total;
            }
            List<JSONObject> subList = new ArrayList<>();
            if (from < total) {
                subList = agencyJoinList.subList(
                        from,
                        to);
            }

            JSONObject data = new JSONObject();
            data.put("records", subList);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterAgencyMissionPointHistory(SessionData sessionData, FilterListRequest request) {
        try {
            if (!this.dataManager.getStaffManager().checkStaffManageAgency(
                    sessionData.getId(),
                    this.dataManager.getAgencyManager().getAgencyInfo(request.getId())
            )) {
                return ClientResponse.fail(ResponseStatus.NOT_PERMISSION, ResponseMessage.USER_FORBIDDEN);
            }
            FilterRequest agencyRequest = new FilterRequest();
            agencyRequest.setKey("agency_id");
            agencyRequest.setValue(ConvertUtils.toString(request.getId()));
            agencyRequest.setType(TypeFilter.SELECTBOX);
            request.getFilters().add(agencyRequest);
            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_POINT_HISTORY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionLogDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, 1);
            int total = this.missionLogDB.getTotal(query);

            int total_mission_point = this.missionLogDB.getTotalMissionPoint(query);

            records.forEach(js -> {
                int data_id = ConvertUtils.toInt(js.get("data_id"));
                if (data_id != 0) {
                    JSONObject mission_info = this.missionDB.getAgencyMissionInfo(data_id);
                    if (mission_info == null) {
                        mission_info = this.missionLogDB.getAgencyMissionHistory(data_id);
                        if (mission_info != null) {
                            mission_info.put("start_date", mission_info.get("mission_created_date"));
                            mission_info.put("end_date", mission_info.get("mission_end_date"));
                        }
                    } else {
                        mission_info.put("start_date", mission_info.get("created_date"));
                        mission_info.put("end_date", mission_info.get("mission_end_date"));
                    }

                    if (mission_info != null) {
                        mission_info.put("name", this.convertMissionName(mission_info));

                        JSONObject mission_setting = this.missionDB.getMissionSettingBasicInfo(
                                ConvertUtils.toInt(mission_info.get("mission_setting_id"))
                        );
                        if (mission_setting != null) {
                            js.put("mission_setting_info", mission_setting);
                            js.put("mission_group_info", this.missionDB.getMissionGroupInfo(
                                    ConvertUtils.toInt(mission_setting.get("mission_group_id"))));
                        }
                    }
                    js.put("mission_info", mission_info);
                }
            });
            JSONObject data = new JSONObject();
            data.put("agency_info", this.missionDB.getAgencyInfo(request.getId()));
            data.put("total_mission_point", total_mission_point);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterAgencyMissionAchievementHistory(SessionData sessionData, FilterListRequest request) {
        try {
            if (request.getId() != 0) {
                FilterRequest agencyRequest = new FilterRequest();
                agencyRequest.setKey("agency_id");
                agencyRequest.setValue(ConvertUtils.toString(request.getId()));
                agencyRequest.setType(TypeFilter.SELECTBOX);
                request.getFilters().add(agencyRequest);
            }
            String query = this.filterUtils.getQuery(FunctionList.FILTER_AGENCY_MISSION_ACHIEVEMENT_HISTORY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionLogDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, 1);
            int total = this.missionLogDB.getTotal(query);


            records.forEach(js -> {
                JSONObject jsMissionSetting = this.missionDB.getMissionSettingInfo(
                        ConvertUtils.toInt(js.get("mission_setting_id")));
                js.put("mission_setting_info", jsMissionSetting);

                js.put("agency_info", this.missionDB.getAgencyInfo(ConvertUtils.toInt(js.get("agency_id"))));

                js.put("offer", this.convertAchievement(jsMissionSetting, ConvertUtils.toInt(js.get("achievement_level"))));
            });
            JSONObject data = new JSONObject();
            data.put("agency_info", this.missionDB.getAgencyInfo(request.getId()));
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private JSONObject convertAchievement(JSONObject jsMissionSetting, int achievementLevel) {
        try {
            JSONObject offer = new JSONObject();
            if (jsMissionSetting != null) {
                /**
                 * Mức ưu đãi
                 */
                MissionOfferRequest offer_data =
                        JsonUtils.DeSerialize(jsMissionSetting.get("offer_data").toString(),
                                MissionOfferRequest.class);
                List<JSONObject> vrps = new ArrayList<>();
                offer_data.getLimits().forEach(l -> {
                    if (l.getLevel() == achievementLevel) {
                        offer.put("level", l.getLevel());
                        offer.put("require_value", l.getValue());
                        offer.put("image", l.getImage());
                        offer.put("offers", l.getOffers());
                        l.getOffers().forEach(o -> {
                            JSONObject jsVRP = this.missionDB.getVRP(o.getVrp_id());
                            if (jsVRP != null) {
                                if (VoucherOfferType.GIFT_OFFER.getKey().equals(
                                        jsVRP.get("offer_type").toString())) {
                                    List<JSONObject> gifts = this.missionDB.getListVRPItem(o.getVrp_id());
                                    for (JSONObject gift : gifts) {
                                        gift.put("product_info", this.dataManager.getProductManager().getProductBasicData(ConvertUtils.toInt(gift.get("item_id"))));
                                    }
                                    jsVRP.put("item_data", gifts);
                                }
                                vrps.add(jsVRP);
                            }
                        });
                    }
                });

                offer.put("vrps", vrps);
            }
            return offer;
        } catch (Exception e) {
            LogUtil.printDebug("", e);
        }
        return null;
    }

    public ClientResponse filterAgencyMissionAchievementHistoryOfAgency(SessionData sessionData, FilterListRequest request) {
        try {
            if (request.getId() != 0) {
                FilterRequest agencyRequest = new FilterRequest();
                agencyRequest.setKey("agency_id");
                agencyRequest.setValue(ConvertUtils.toString(request.getId()));
                agencyRequest.setType(TypeFilter.SELECTBOX);
                request.getFilters().add(agencyRequest);
            }
            String query = this.filterUtils.getQuery(FunctionList.FILTER_AGENCY_MISSION_ACHIEVEMENT_HISTORY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionLogDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, 1);
            int total = this.missionLogDB.getTotal(query);


            records.forEach(js -> {
                JSONObject jsMissionSetting = this.missionDB.getMissionSettingInfo(
                        ConvertUtils.toInt(js.get("mission_setting_id")));
                js.put("mission_setting_info", jsMissionSetting);

                js.put("agency_info", this.missionDB.getAgencyInfo(ConvertUtils.toInt(js.get("agency_id"))));

                js.put("offer", this.convertAchievement(jsMissionSetting, ConvertUtils.toInt(js.get("achievement_level"))));
            });
            JSONObject data = new JSONObject();
            data.put("agency_info", this.missionDB.getAgencyInfo(request.getId()));
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchMissionSetting(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_MISSION_SETTING, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, 1);
            int total = this.missionDB.getTotal(query);
            JSONObject data = new JSONObject();
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runMissionWaitingGenerate() {
        try {
            List<JSONObject> missionWaitingGenerateList = this.missionDB.getListMissionWaitingGenerate(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject missionWaitingGenerate : missionWaitingGenerateList) {
                int missionWaitingGenerateId = ConvertUtils.toInt(missionWaitingGenerate.get("id"));
                /**
                 * Giao nhiệm vụ
                 */
                this.giaoNhiemVu(ConvertUtils.toInt(missionWaitingGenerate.get("agency_id")));

                /**
                 * Gắn bảng xếp hạng thành tích
                 */
                this.addAgencyToMissionBXH(ConvertUtils.toInt(missionWaitingGenerate.get("agency_id")));

                this.missionDB.setMissionWaitingGenerateToDone(missionWaitingGenerateId);
            }
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse resetMission() {
        try {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        /**
                         * Trả thưởng
                         */
                        resetRewardMission();

                        Thread.sleep(5000);

                        /**
                         * Reset huy hiệu
                         */
                        callResetMissionPoint();

                        Thread.sleep(60000);

                        resetMissionConfigKyDai();

                        Thread.sleep(60000);

                        callResetMissionAll();

                        /**
                         * Giao nhiệm vụ kỳ dài
                         */
                        giaoNhiemVuKyDai();

                        /**
                         * Reset tham gia bảng xếp hạng
                         */
                        runResetMissionBXH();
                    } catch (Exception e) {
                        LogUtil.printDebug("MISSION", e);
                    }
                }
            };
            thread.start();
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse resetRewardMission() {
        try {
            List<JSONObject> agencyMissionPeriodRunningList = this.missionDB.getListMissionPeriodNeedRewardForReset(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            if (agencyMissionPeriodRunningList.size() > 0) {
                List<Integer> data = agencyMissionPeriodRunningList.stream().map(
                        e -> ConvertUtils.toInt(e.get("id"))
                ).collect(Collectors.toList());

                this.callRewardMission(data);
            }
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getOrderInfo(SessionData sessionData, GetOrderInfoByCodeRequest request) {
        try {
            JSONObject agencyOrder = this.orderDB.getAgencyOrderByOrderCode(
                    request.getCode()
            );
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            int agency_order_id = ConvertUtils.toInt(agencyOrder.get("id"));
            int agency_id = ConvertUtils.toInt(agencyOrder.get("agency_id"));
            String agency_order_code = ConvertUtils.toString(agencyOrder.get("code"));

            Map<Integer, Long> mpProductDTT = this.getProductDTTOfAgencyOrder(
                    agency_id,
                    agency_order_id,
                    agency_order_code
            );

            JSONObject jsOrderDeptNormal = this.orderDB.getOrderNormalDept(
                    ConvertUtils.toInt(agencyOrder.get("id"))
            );
            if (jsOrderDeptNormal == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            List<JSONObject> products = new ArrayList<>();
            List<JSONObject> orderProductList = this.orderDB.getListProductInOrder(ConvertUtils.toInt(agencyOrder.get("id")));
            for (JSONObject orderProduct : orderProductList) {
                AgencyOrderDetailEntity agencyOrderDetailEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(orderProduct), AgencyOrderDetailEntity.class);
                if (agencyOrderDetailEntity.getPromo_id() == 0) {
                    JSONObject jsProduct = new JSONObject();
                    jsProduct.put("id", agencyOrderDetailEntity.getProduct_id());
                    jsProduct.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                            agencyOrderDetailEntity.getProduct_id()
                    ));
                    jsProduct.put("price", agencyOrderDetailEntity.getProduct_price());
                    jsProduct.put("quantity", agencyOrderDetailEntity.getProduct_total_quantity());

                    /**
                     * tính tổng ưu đãi
                     */
                    jsProduct.put("uu_dai",
                            agencyOrderDetailEntity.getProduct_total_promotion_price_ctkm() +
                                    agencyOrderDetailEntity.getProduct_total_promotion_price() +
                                    agencyOrderDetailEntity.getProduct_total_dm_price()
                    );


                    jsProduct.put("dtt",
                            mpProductDTT.get(agencyOrderDetailEntity.getProduct_id()) != null ?
                                    mpProductDTT.get(agencyOrderDetailEntity.getProduct_id()) :
                                    agencyOrderDetailEntity.getProduct_total_end_price()
                    );
                    products.add(jsProduct);
                }
            }
            JSONObject order = new JSONObject();
            order.put("code",
                    request.getCode()
            );
            order.put("id",
                    agency_order_id
            );

            List<JSONObject> agencyMissionList = this.missionDB.getListAgencyMissionByMissionType(
                    agency_id, MissionType.MUA_HANG.getId());
            List<JSONObject> missions = new ArrayList<>();
            for (JSONObject js : agencyMissionList) {
                if (!this.checkOrderEffectMission(agency_id, js, products, request.getCode())) {
                    continue;
                }
                js.put("mission_name", this.convertMissionName(js));
                js.put("completed", this.checkMissionComplete(js));
                if (js.get("mission_data") == null || js.get("mission_data").toString().isEmpty()) {
                    js.put("accumulate_status", MissionTransactionStatus.CHUA_TICH_LUY.getId());
                } else {
                    MissionTransactionData transactionInfo = this.getLastMissionTransactionInData(request.getCode(),
                            convertMissionData(js));
                    if (transactionInfo == null) {
                        js.put("accumulate_status", MissionTransactionStatus.CHUA_TICH_LUY.getId());
                    } else {
                        js.put("accumulate_status", transactionInfo.getStatus());
                    }
                }
                missions.add(js);
            }

            JSONObject data = new JSONObject();
            data.put("order", order);
            data.put("products", products);
            data.put("missions", missions);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.MISSION.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkOrderEffectMission(
            int agencyId,
            JSONObject agency_mission,
            List<JSONObject> orderProductList,
            String order_code) {
        try {
            if (agency_mission == null ||
                    agency_mission.get("mission_item_data") == null ||
                    agency_mission.get("mission_item_data").toString().isEmpty()
            ) {
                return false;
            }

            if (agency_mission.get("mission_data") != null &&
                    !agency_mission.get("mission_data").toString().isEmpty() &&
                    this.getMissionTransactionInData(
                            order_code,
                            JsonUtils.DeSerialize(
                                    agency_mission.get("mission_data").toString(),
                                    new TypeToken<List<MissionTransactionData>>() {
                                    }.getType())) != null) {
                return true;
            }

            MissionItemDataRequest missionItemDataRequest =
                    JsonUtils.DeSerialize(agency_mission.get("mission_item_data").toString(), MissionItemDataRequest.class);
            if (MissionConstants.MISSION_ALL_ITEM.equals(missionItemDataRequest.getType())) {
                return true;
            }

            for (JSONObject jsProduct : orderProductList) {
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(ConvertUtils.toInt(jsProduct.get("id")));
                if (productCache == null) {
                    continue;
                }
                for (ItemInfo item : missionItemDataRequest.getItems()) {
                    MissionProductType missionProductType = MissionProductType.fromKey(item.getItem_type());
                    switch (missionProductType) {
                        case ITEM_TYPE: {
                            if (productCache.getItem_type() == item.getItem_id()) {
                                return true;
                            }
                            break;
                        }
                        case PRODUCT_GROUP: {
                            if (productCache.getProduct_group_id() == item.getItem_id()) {
                                return true;
                            }
                            break;
                        }
                        case CATEGORY: {
                            if (item.getCategory_level() == 2) {
                                if (productCache.getMat_hang_id() == item.getItem_id()) {
                                    return true;
                                }
                                break;
                            } else if (item.getCategory_level() == 3) {
                                if (productCache.getPlsp_id() == item.getItem_id()) {
                                    return true;
                                }
                                break;
                            } else if (item.getCategory_level() == 4) {
                                if (productCache.getPltth_id() == item.getItem_id()) {
                                    return true;
                                }
                                break;
                            } else {
                                break;
                            }
                        }
                        case PRODUCT: {
                            if (productCache.getId() == item.getItem_id()) {
                                return true;
                            }
                            break;
                        }
                    }
                }
            }
            return false;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.MISSION.name(), ex);
        }
        return false;
    }

    private MissionTransactionData getMissionTransactionInData(String code, List<MissionTransactionData> missionData) {
        try {
            return missionData.stream().filter(
                    x -> x.getCode().contains(code)
            ).findFirst().orElse(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.MISSION.name(), ex);
        }
        return null;
    }

    private MissionTransactionData getLastMissionTransactionInData(String code, List<MissionTransactionData> missionData) {
        try {
            MissionTransactionData result = null;
            for (MissionTransactionData missionTransactionData : missionData) {
                if (missionTransactionData.getCode().contains(code)) {
                    result = missionTransactionData;
                }
            }
            return result;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.MISSION.name(), ex);
        }
        return null;
    }

    private Map<Integer, Long> getProductDTTOfAgencyOrder(int agency_id, int agency_order_id, String agency_order_code) {
        Map<Integer, Long> mpProductDTT = new HashMap<>();
        try {
            ClientResponse crProductDTT = this.accumulateMissionService.getProductDTT(
                    agency_id,
                    agency_order_code,
                    agency_order_id
            );
            if (crProductDTT.failed()) {
                this.alertToTelegram(
                        "DTT sản phẩm của đơn " + agency_order_code +
                                ": " + crProductDTT.getMessage(),
                        ResponseStatus.FAIL
                );
                return mpProductDTT;
            }

            List<JSONObject> productList = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(crProductDTT.getData()),
                    new com.google.gson.reflect.TypeToken<List<JSONObject>>() {
                    }.getType()
            );

            for (JSONObject k : productList) {
                mpProductDTT.put(
                        ConvertUtils.toInt(k.get("product_id")),
                        ConvertUtils.toLong(k.get("product_dtt")));
            }
            return mpProductDTT;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
            this.alertToTelegram(
                    "DTT sản phẩm của đơn " + agency_order_code +
                            ": " + ex.getMessage(),
                    ResponseStatus.FAIL
            );
        }
        return mpProductDTT;
    }

    public ClientResponse filterMissionBXHRewardHistoryOfAgency(SessionData sessionData, FilterListRequest request) {
        try {
            int agency_id = request.getId();
            FilterRequest agencyRequest = new FilterRequest();
            agencyRequest.setType(TypeFilter.SELECTBOX);
            agencyRequest.setValue(ConvertUtils.toString(agency_id));
            agencyRequest.setKey("agency_id");
            request.getFilters().add(agencyRequest);

            String query = this.filterUtils.getQuery(FunctionList.FILTER_MISSION_BXH_REWARD_HISTORY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.missionLogDB.filter(query, this.appUtils.getOffset(request.getPage())
                    , ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.missionLogDB.getTotal(query);
            for (JSONObject js : records) {
                JSONObject jsMissionBXH = this.missionDB.getMissionBXH(ConvertUtils.toInt(js.get("mission_bxh_id")));
                js.put("mission_bxh_info", jsMissionBXH);
                this.convertMissionBXHReward(
                        js, this.convertMissionBXHData(jsMissionBXH),
                        ConvertUtils.toInt(js.get("position")),
                        agency_id,
                        ConvertUtils.toInt(js.get("mission_period_running_id"))
                );
            }
            JSONObject data = new JSONObject();
            data.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(agency_id));
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse rejectAccumulateOrder(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject agencyOrder = this.orderDB.getAgencyOrder(
                    request.getId()
            );
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            int agency_order_id = ConvertUtils.toInt(agencyOrder.get("id"));
            int agency_id = ConvertUtils.toInt(agencyOrder.get("agency_id"));
            LogUtil.printDebug("rejectAccumulateOrder: " + JsonUtils.Serialize(request));
            ClientResponse crRejectAccumulateOrder = this.accumulateMissionService.rejectAccumulateOrder(
                    agency_order_id,
                    agency_id
            );

            if (crRejectAccumulateOrder.success()) {
                this.missionDB.rejectAgencyOrder(agency_order_id);
            }
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse acceptAccumulateOrder(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject agencyOrder = this.orderDB.getAgencyOrder(
                    request.getId()
            );
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            /**
             * Đơn hàng đã được ghi nhận công nợ
             */
            if (YesNoStatus.YES.getValue() != ConvertUtils.toInt(agencyOrder.get("increase_dept"))) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "Đơn hàng chưa ghi nhận công nợ");
            }

            int agency_order_id = ConvertUtils.toInt(agencyOrder.get("id"));
            int agency_id = ConvertUtils.toInt(agencyOrder.get("agency_id"));
            LogUtil.printDebug("acceptAccumulateOrder: " + JsonUtils.Serialize(request));
            List<JSONObject> agencyOrderDeptList = this.orderDB.getListAgencyOrderDept(agency_order_id);
            for (JSONObject jsAgencyOrderDept : agencyOrderDeptList) {
                this.accumulateMissionService.acceptAccumulateOrder(
                        agency_id,
                        agency_order_id,
                        ConvertUtils.toInt(jsAgencyOrderDept.get("id")),
                        ConvertUtils.toString(jsAgencyOrderDept.get("dept_code"))
                );
            }

            this.orderDB.ghiNhanTrangThaiTichLuyNhiemVuChoDonHang(agency_order_id);

            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse onOffMissionBXH(SessionData sessionData, OnOffMissionBXHRequest request) {
        try {
            if (request.getIs_repeat() != 0 && request.getIs_repeat() != 1) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "request.getIs_repeat(): " + request.getIs_repeat());
            }

            JSONObject missionBXH = this.missionDB.getMissionBXH(request.getId());
            if (missionBXH == null) {
                return ResponseConstants.failed;
            }

            boolean rsOnOff = this.missionDB.onOffRepeatMissionBXH(request.getId(), request.getIs_repeat());
            if (!rsOnOff) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.saveMissionBXHHistory(
                    request.getId(),
                    this.missionDB.getMissionBXH(request.getId()),
                    "",
                    sessionData.getId(),
                    ConvertUtils.toInt(missionBXH.get("status")));

            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }

    public ClientResponse runResetMissionConfig() {
        try {
            List<Integer> mission_period_running_list = JsonUtils.DeSerialize(
                    this.dataManager.getConfigManager().getMPMissionConfig().get(MissionConstants.KY_NHIEM_VU),
                    new TypeToken<List<Integer>>() {
                    }.getType());

            int ky_dai = Collections.max(mission_period_running_list);
            if (MissionPeriodType.THANG.getId() == ky_dai) {
                /**
                 * Nếu đầu tháng
                 */
                if (this.checkGenerateMissionThang()) {
                    this.resetMissionConfigKyDai();

                    callResetMissionAll();
                }
            } else if (MissionPeriodType.QUY.getId() == ky_dai) {
                /**
                 * Nếu đầu quý
                 */
                if (this.checkGenerateMissionQuy()) {
                    this.resetMissionConfigKyDai();

                    callResetMissionAll();
                }
            }
            return ResponseConstants.success;
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }

    private void resetMissionConfigKyDai() {
        try {
            Map<String, String> missionConfigList = this.dataManager.getConfigManager().getMPMissionConfig();
            /**
             * Kỳ nhiệm vụ
             */
            boolean rsUpdate = this.missionDB.updateMissionConfig(
                    MissionConstants.KY_NHIEM_VU,
                    missionConfigList.get(MissionConstants.KY_NHIEM_VU_KY_KE_TIEP));

            /**
             * MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN,
                    missionConfigList.get(MissionConstants.THOI_GIAN_KET_THUC_NHIEM_VU_TUAN_KY_KE_TIEP));

            /**
             * MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG,
                    missionConfigList.get(MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG_KY_KE_TIEP));
            /**
             * MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_QUY
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_QUY,
                    missionConfigList.get(MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_QUY_KY_KE_TIEP));

            /**
             * MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG,
                    missionConfigList.get(MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG_KY_KE_TIEP));

            /**
             * MissionConstants.MO_TA_NHIEM_VU
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.MO_TA_NHIEM_VU,
                    missionConfigList.get(MissionConstants.MO_TA_NHIEM_VU_KY_KE_TIEP));

            /**
             * MissionConstants.UU_DAI_TUAN
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.UU_DAI_TUAN,
                    missionConfigList.get(MissionConstants.UU_DAI_TUAN_KY_KE_TIEP));


            /**
             * MissionConstants.UU_DAI_THANG_KY_KE_TIEP
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.UU_DAI_THANG,
                    missionConfigList.get(MissionConstants.UU_DAI_THANG_KY_KE_TIEP));
            /**
             * MissionConstants.UU_DAI_QUY
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.UU_DAI_QUY,
                    missionConfigList.get(MissionConstants.UU_DAI_QUY_KY_KE_TIEP));
            /**
             * MissionConstants.SO_HUY_HIEU_DOI_TUAN
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.SO_HUY_HIEU_DOI_TUAN,
                    missionConfigList.get(MissionConstants.SO_HUY_HIEU_DOI_TUAN_KY_KE_TIEP));

            /**
             *    MissionConstants.SO_HUY_HIEU_DOI_THANG
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.SO_HUY_HIEU_DOI_THANG,
                    missionConfigList.get(MissionConstants.SO_HUY_HIEU_DOI_THANG_KY_KE_TIEP));

            /**
             * MissionConstants.SO_HUY_HIEU_DOI_QUY
             */
            this.missionDB.updateMissionConfig(MissionConstants.SO_HUY_HIEU_DOI_QUY,
                    missionConfigList.get(MissionConstants.SO_HUY_HIEU_DOI_QUY_KY_KE_TIEP));

            this.dataManager.getConfigManager().loadMissionConfig();
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    private void resetMissionConfigKyThang() {
        try {
            Map<String, String> missionConfigList = this.dataManager.getConfigManager().getMPMissionConfig();

            /**
             * MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG,
                    missionConfigList.get(MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG_KY_KE_TIEP));

            if (this.dataManager.getConfigManager().getKyDai() == MissionPeriodType.THANG.getId()) {
                /**
                 * MissionConstants.MO_TA_NHIEM_VU
                 */
                this.missionDB.updateMissionConfig(
                        MissionConstants.MO_TA_NHIEM_VU,
                        missionConfigList.get(MissionConstants.MO_TA_NHIEM_VU_KY_KE_TIEP));

                /**
                 * MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG
                 */
                this.missionDB.updateMissionConfig(
                        MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG,
                        missionConfigList.get(MissionConstants.THOI_GIAN_KET_THUC_TICH_LUY_NHIEM_VU_THANG_KY_KE_TIEP));
            }

            /**
             * MissionConstants.UU_DAI_TUAN
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.UU_DAI_TUAN,
                    missionConfigList.get(MissionConstants.UU_DAI_TUAN_KY_KE_TIEP));

            /**
             * MissionConstants.UU_DAI_THANG_KY_KE_TIEP
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.UU_DAI_THANG,
                    missionConfigList.get(MissionConstants.UU_DAI_THANG_KY_KE_TIEP));

            /**
             * MissionConstants.SO_HUY_HIEU_DOI_TUAN
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.SO_HUY_HIEU_DOI_TUAN,
                    missionConfigList.get(MissionConstants.SO_HUY_HIEU_DOI_TUAN_KY_KE_TIEP));

            /**
             *    MissionConstants.SO_HUY_HIEU_DOI_THANG
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.SO_HUY_HIEU_DOI_THANG,
                    missionConfigList.get(MissionConstants.SO_HUY_HIEU_DOI_THANG_KY_KE_TIEP));

            this.dataManager.getConfigManager().loadMissionConfig();
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    private void resetMissionConfigKyTuan() {
        try {
            Map<String, String> missionConfigList = this.dataManager.getConfigManager().getMPMissionConfig();

            /**
             * MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG,
                    missionConfigList.get(MissionConstants.THOI_GIAN_XAC_NHAN_HOAN_THANH_NHIEM_VU_MUA_HANG_KY_KE_TIEP));

            /**
             * MissionConstants.UU_DAI_TUAN
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.UU_DAI_TUAN,
                    missionConfigList.get(MissionConstants.UU_DAI_TUAN_KY_KE_TIEP));

            /**
             * MissionConstants.SO_HUY_HIEU_DOI_TUAN
             */
            this.missionDB.updateMissionConfig(
                    MissionConstants.SO_HUY_HIEU_DOI_TUAN,
                    missionConfigList.get(MissionConstants.SO_HUY_HIEU_DOI_TUAN_KY_KE_TIEP));

            this.dataManager.getConfigManager().loadMissionConfig();
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
        }
    }

    public ClientResponse runKetThucThietLapGiao() {
        try {
            SessionData sessionData = new SessionData();
            List<JSONObject> missionBXHList = this.missionDB.getListMissionSettingWaitingStop(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject js : missionBXHList) {
                StopPromoRequest basicRequest = new StopPromoRequest();
                basicRequest.setId(ConvertUtils.toInt(js.get("id")));
                basicRequest.setStop_type(PromoStopType.STOP_NOW.getKey());
                basicRequest.setNote(ConvertUtils.toString(js.get("note")));
                ClientResponse crStopMissionSetting = this.stopMissionSetting(sessionData, basicRequest);
                if (crStopMissionSetting.failed()) {
                    this.alertToTelegram("crStopMissionSetting: " + JsonUtils.Serialize(crStopMissionSetting), ResponseStatus.EXCEPTION);
                }
            }
            return ClientResponse.success(null);
        } catch (Exception e) {
            LogUtil.printDebug("MISSION", e);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, e.getMessage());
        }
    }
}