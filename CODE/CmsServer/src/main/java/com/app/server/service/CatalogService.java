package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.catalog.Catalog;
import com.app.server.data.dto.notify.NotifyData;
import com.app.server.data.entity.AgencyEntity;
import com.app.server.data.entity.NotifyHistoryEntity;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.catalog.*;
import com.app.server.database.AgencyDB;
import com.app.server.database.CatalogDB;
import com.app.server.database.NotifyDB;
import com.app.server.database.ProductDB;
import com.app.server.enums.*;
import com.app.server.manager.DataManager;
import com.app.server.response.ClientResponse;
import com.app.server.utils.AppUtils;
import com.app.server.utils.FilterUtils;
import com.app.server.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.sql.Update;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CatalogService {
    private FilterUtils filterUtils;

    @Autowired
    public void setFilterUtils(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    private CatalogDB catalogDB;

    @Autowired
    public void setCatalogDB(CatalogDB catalogDB) {
        this.catalogDB = catalogDB;
    }

    private AppUtils appUtils;

    @Autowired
    public void setAppUtils(AppUtils appUtils) {
        this.appUtils = appUtils;
    }

    private DataManager dataManager;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    private NotifyDB notifyDB;

    @Autowired
    public void setNotifyDB(NotifyDB notifyDB) {
        this.notifyDB = notifyDB;
    }

    private AgencyDB agencyDB;

    @Autowired
    public void setAgencyDB(AgencyDB agencyDB) {
        this.agencyDB = agencyDB;
    }

    public ClientResponse filterCatalog(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_CATALOG, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.catalogDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.catalogDB.getTotal(query);

            for (JSONObject js : records) {
                int id = ConvertUtils.toInt(js.get("id"));
                js.put("creator_info", this.dataManager.getStaffManager().getStaffProfile(
                        ConvertUtils.toInt(js.get("creator_id"))
                ));

                js.put("categories", this.catalogDB.getListCategoryInCatalog(id));
            }
            data.put("n_register", this.dataManager.getConfigManager().getCatalogNRegister());
            data.put("n_add", this.dataManager.getConfigManager().getCatalogNAdd());
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createCatalog(SessionData sessionData, CreateCatalogRequest request) {
        try {
            ClientResponse cr = request.validate();
            if (cr.failed()) {
                return cr;
            }

            Catalog catalog = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(request),
                    Catalog.class
            );

            int id = this.catalogDB.insertCategory(
                    request.getName(),
                    request.getImage(),
                    request.getIs_show(),
                    sessionData.getId()
            );
            if (id <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            catalog.setId(id);
            int last_priority = this.catalogDB.getLastPriority();

            this.catalogDB.updateCatalogPriority(id, last_priority + 1);

            this.dataManager.callReloadCatalog(id);

            this.catalogDB.saveCatalogHistory(
                    catalog.getId(),
                    JsonUtils.Serialize(catalog),
                    1
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editCatalog(SessionData sessionData, EditCatalogRequest request) {
        try {
            ClientResponse cr = request.validate();
            if (cr.failed()) {
                return cr;
            }

            JSONObject catalog = this.catalogDB.getCatalog(request.getId());
            if (catalog == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rsUpdate = this.catalogDB.updateCatalog(
                    request.getId(),
                    request.getName(),
                    request.getImage(),
                    request.getIs_show(),
                    sessionData.getId()
            );
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.dataManager.callReloadCatalog(request.getId());

            this.catalogDB.saveCatalogHistory(
                    request.getId(),
                    JsonUtils.Serialize(request),
                    2
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse addCategory(SessionData sessionData, AddCategoryRequest request) {
        try {
            ClientResponse cr = request.validate();
            if (cr.failed()) {
                return cr;
            }

            JSONObject catalog = this.catalogDB.getCatalog(request.getCatalog_id());
            if (catalog == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            for (Integer category_id : request.getCategories()) {
                JSONObject catalogOther = this.catalogDB.getCatalogOfCategory(
                        category_id
                );
                if (catalogOther != null) {
                    ClientResponse crCatalogOther = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    crCatalogOther.setMessage("Danh mục " +
                            this.dataManager.getProductManager().getCategoryById(
                                    category_id
                            ).getName() + " đã tồn tại trong catalog khác");
                    return crCatalogOther;
                }
            }

            for (Integer category_id : request.getCategories()) {
                int rsInsert = this.catalogDB.addCategory(
                        category_id,
                        request.getCatalog_id()
                );
            }

            this.dataManager.callReloadCatalog(
                    request.getCatalog_id()
            );

            this.catalogDB.saveCatalogHistory(
                    request.getCatalog_id(),
                    JsonUtils.Serialize(request.getCategories()),
                    3
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getCatalogDetail(BasicRequest request) {
        try {
            JSONObject catalog = this.catalogDB.getCatalog(request.getId());
            if (catalog == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            catalog.put("creator_info", this.dataManager.getStaffManager().getStaff(
                    ConvertUtils.toInt(catalog.get("creator_id"))
            ));

            catalog.put("categories", this.catalogDB.getListCategoryInCatalog(request.getId()));

            catalog.put("agencies", this.catalogDB.getListAgency(request.getId()));

            JSONObject data = new JSONObject();
            data.put("catalog", catalog);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deleteCategory(SessionData sessionData, AddCategoryRequest request) {
        try {
            ClientResponse cr = request.validate();
            if (cr.failed()) {
                return cr;
            }

            JSONObject catalog = this.catalogDB.getCatalog(request.getCatalog_id());
            if (catalog == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            for (Integer category_id : request.getCategories()) {
                boolean rsDelete = this.catalogDB.deleteCategory(
                        category_id,
                        request.getCatalog_id()
                );
            }

            this.dataManager.callReloadCatalog(
                    request.getCatalog_id()
            );

            this.catalogDB.saveCatalogHistory(
                    request.getCatalog_id(),
                    JsonUtils.Serialize(request.getCategories()),
                    4
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editNCatalog(UpdateNCategoryRequest request) {
        try {
            boolean rsUpdate = this.dataManager.getConfigManager().updateNCatalog(
                    request.getN_register(),
                    request.getN_add()
            );
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.dataManager.callReloadConfig();

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }


    public ClientResponse sortCatalog(SortCatalogRequest request) {
        try {
            ClientResponse cr = request.validate();
            if (cr.failed()) {
                return cr;
            }

            for (int i = 0; i < request.getCatalogs().size(); i++) {
                boolean rsUpdate = this.catalogDB.updateCatalogPriority(
                        request.getCatalogs().get(i),
                        i + 1
                );
                this.dataManager.callReloadCatalog(request.getCatalogs().get(i));
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterCatalogByAgency(FilterListRequest request) {
        try {
            for (FilterRequest filterRequest : request.getFilters()) {
                if (filterRequest.getKey().equals("status")) {
                    filterRequest.setKey("t.status");
                }
            }

            FilterRequest agencyRequest = new FilterRequest();
            agencyRequest.setKey("t.agency_id");
            agencyRequest.setValue(ConvertUtils.toString(request.getAgency_id()));
            agencyRequest.setType(TypeFilter.SELECTBOX);
            request.getFilters().add(agencyRequest);

            String query = this.filterUtils.getQuery(FunctionList.FILTER_AGENCY_CATALOG_DETAIL, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.catalogDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("note", this.parseAnswer(js.get("note")));

                if (ConvertUtils.toInt(js.get("status")) == AgencyCatalogDetailStatus.APPROVED.getId()) {
                    js.put("confirmer_info",
                            this.dataManager.getStaffManager().getStaff(
                                    ConvertUtils.toInt(js.get("confirmer_id"))
                            )
                    );
                }
            }
            int total = this.catalogDB.getTotal(query);

            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse approveAgencyRequestOpenCatalog(SessionData sessionData, ApproveCatalogRequest request) {
        try {
            JSONObject agency_catalog_detail = this.catalogDB.getAgencyCatalogDetail(
                    request.getId()
            );
            if (agency_catalog_detail == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (ConvertUtils.toInt(agency_catalog_detail.get("status")) == AgencyCatalogDetailStatus.APPROVED.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            AgencyEntity agencyEntity = this.dataManager.getAgencyManager().getAgency(
                    ConvertUtils.toInt(agency_catalog_detail.get("agency_id"))
            );
            if (agencyEntity == null || agencyEntity.getStatus() == AgencyStatus.WAITING_APPROVE.getValue()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsApprove = this.catalogDB.approveAgencyCatalogDetail(
                    request.getId(),
                    AgencyCatalogDetailStatus.APPROVED.getId(),
                    sessionData.getId(),
                    request.getNote()
            );
            if (!rsApprove) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int agency_catalog_request_id = ConvertUtils.toInt(agency_catalog_detail.get("agency_catalog_request_id"));
            if (this.checkFinishAgencyCatalogRequest(
                    agency_catalog_request_id
            )) {
                this.catalogDB.finishAgencyCatalogRequest(agency_catalog_request_id);

                /**
                 * Gửi noti
                 */
                this.sendNotify(agency_catalog_request_id, agencyEntity.getId());
            } else {
                this.catalogDB.processingAgencyCatalogRequest(agency_catalog_request_id);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void sendNotify(int agencyCatalogRequestId, int agency_id) {
        try {
            List<JSONObject> catalogs = this.catalogDB.getListCategoryByRequest(
                    agencyCatalogRequestId
            );
            String catalogApproveList = "";
            for (JSONObject catalog : catalogs) {
                if (ConvertUtils.toInt(catalog.get("status")) == AgencyCatalogDetailStatus.APPROVED.getId()) {
                    if (!catalogApproveList.isEmpty()) {
                        catalogApproveList += ", ";
                    }
                    catalogApproveList += ConvertUtils.toString(catalog.get("name"));
                }
            }

            if (!catalogApproveList.isEmpty()) {
                catalogApproveList = "Yêu cầu mở mặt hàng " +
                        catalogApproveList +
                        " đã được chấp nhận. Quý khách vui lòng xem tại trang Sản phẩm";
                this.pushNotify(agency_id, "Thông báo", catalogApproveList);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
    }

    private void pushNotify(int agency_id, String title, String description) {
        try {
            this.pushNotifyToAgency(
                    0,
                    title,
                    description,
                    "[]",
                    NotifyType.TAB_SAN_PHAM.getCode(),
                    "",
                    "[]",
                    agency_id
            );
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
    }

    public ClientResponse rejectAgencyRequestOpenCatalog(SessionData sessionData, ApproveCatalogRequest request) {
        try {
            JSONObject agency_catalog_detail = this.catalogDB.getAgencyCatalogDetail(
                    request.getId()
            );
            if (agency_catalog_detail == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (ConvertUtils.toInt(agency_catalog_detail.get("status")) == AgencyCatalogDetailStatus.REJECT.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            AgencyEntity agencyEntity = this.dataManager.getAgencyManager().getAgency(
                    ConvertUtils.toInt(agency_catalog_detail.get("agency_id"))
            );
            if (agencyEntity == null || agencyEntity.getStatus() == AgencyStatus.WAITING_APPROVE.getValue()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            if (ConvertUtils.toInt(agency_catalog_detail.get("status")) == AgencyCatalogDetailStatus.APPROVED.getId()) {
                boolean rsReject = this.catalogDB.rejectAgencyCatalogDetail(
                        request.getId(),
                        AgencyCatalogDetailStatus.REJECT.getId(),
                        sessionData.getId(),
                        "Từ chối sau khi duyệt: " + request.getNote()
                );
                if (!rsReject) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                return ClientResponse.success(null);
            }

            boolean rsReject = this.catalogDB.rejectAgencyCatalogDetail(
                    request.getId(),
                    AgencyCatalogDetailStatus.REJECT.getId(),
                    sessionData.getId(),
                    request.getNote()
            );
            if (!rsReject) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int agency_catalog_request_id = ConvertUtils.toInt(agency_catalog_detail.get("agency_catalog_request_id"));
            if (this.checkFinishAgencyCatalogRequest(
                    agency_catalog_request_id
            )) {
                this.catalogDB.finishAgencyCatalogRequest(agency_catalog_request_id);

                /**
                 * Gửi noti
                 */
                this.sendNotify(agency_catalog_request_id, agencyEntity.getId());
            } else {
                this.catalogDB.processingAgencyCatalogRequest(agency_catalog_request_id);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkFinishAgencyCatalogRequest(int agency_catalog_request_id) {
        try {
            JSONObject jsWaiting = this.catalogDB.getAgencyCatalogDetailWaiting(
                    agency_catalog_request_id,
                    AgencyCatalogDetailStatus.WAITING.getId()
            );
            if (jsWaiting == null) {
                return true;
            }

            return false;
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return false;
    }

    public ClientResponse createAgencyRequestOpenCatalog(SessionData sessionData, CreateAgencyCatalogRequest request) {
        try {
            ClientResponse cr = request.validate();
            if (cr.failed()) {
                return cr;
            }

            for (Integer catalog_id : request.getCatalogs()) {
                if (this.checkAgencyCatalogWaiting(request.getAgency_id(), catalog_id)) {
                    JSONObject jsCatalog = this.dataManager.getProductManager().getMpCatalog().get(catalog_id);
                    ClientResponse crCheck = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    crCheck.setMessage("Catalog " +
                            ConvertUtils.toString(jsCatalog.get("name")) + " đang chờ duyệt");
                    return crCheck;
                }
            }

            String code = this.generateCatalogCode();
            int id = this.catalogDB.insertAgencyCatalogRequest(
                    request.getAgency_id(),
                    "[]",
                    code,
                    sessionData.getId()
            );
            if (id <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            for (Integer catalog_id : request.getCatalogs()) {
                this.catalogDB.insertAgencyCatalogDetail(
                        id,
                        catalog_id,
                        request.getAgency_id()
                );
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private String generateCatalogCode() {
        try {

            /**
             * yyMMdd + loại chính sách + stt của loại chính sách
             */
            int count = this.catalogDB.getCount();
            if (count < 0) {
                return "";
            }
            count = count + 1;
            String tmp = StringUtils.leftPad(String.valueOf(count), 4, '0');
            DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
            String date = dateFormat.format(new Date());

            return (date + "CATA_" + tmp);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return "";
    }

    private boolean checkAgencyCatalogWaiting(int agencyId, Integer catalogId) {
        JSONObject catalog = this.catalogDB.getAgencyCatalogWaitingByCatalog(
                agencyId, catalogId, AgencyCatalogDetailStatus.WAITING.getId()
        );
        if (catalog == null) {
            return false;
        }

        return true;
    }

    public ClientResponse filterAgencyRequestOpenCatalog(SessionData sessionData, FilterListRequest request) {
        try {
            this.addFilterAgencyData(sessionData, request);
            String query = this.filterUtils.getQuery(FunctionList.FILTER_CATALOG_REQUEST, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.catalogDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 1);
            int total = this.catalogDB.getTotal(query);

            for (JSONObject js : records) {
                js.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(js
                                .get("agency_id"))
                ));

                List<JSONObject> catalogs = this.catalogDB.getListCategoryByRequest(
                        ConvertUtils.toInt(js
                                .get("id"))
                );
                for (JSONObject jsCatalog : catalogs) {
                    jsCatalog.put("modifier_info", this.dataManager.getStaffManager().getStaffProfile(
                            ConvertUtils.toInt(jsCatalog.get("confirmer_id"))));
                }

                js.put("note", this.parseAnswer(js.get("note")));

                js.put("catalogs", catalogs);
            }

            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<JSONObject> parseAnswer(Object note) {
        try {
            if (note == null || note.toString().isEmpty()) {
                return null;
            }
            /**
             * Q&A
             */
            List<String> questions = Arrays.asList(
                    "Hãy cho chúng tôi biết lý do bạn cần mở nhóm hàng này?",
                    "Bạn cần gì?",
                    "Bạn đang quan tâm",
                    "Trung bình 1 tháng bạn mua sản phẩm này là:"
            );
            List<String> answers = JsonUtils.DeSerialize(
                    note.toString(),
                    new TypeToken<List<String>>() {
                    }.getType()
            );

            if (answers.size() < 4) {
                return null;
            }

            List<JSONObject> result = new ArrayList<>();
            for (int iA = 0; iA < questions.size(); iA++) {
                JSONObject js = new JSONObject();
                js.put("question", questions.get(iA));
                js.put("answer", answers.get(iA));
                result.add(js);
            }
            return result;
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
            return new ArrayList<>();
        }
    }

    protected ClientResponse pushNotifyToAgency(
            int staff_id,
            String title,
            String description,
            String image,
            String setting_type,
            String setting_value,
            String setting_data,
            int agency_id) {
        try {
            String agencyIds = JsonUtils.Serialize(Arrays.asList(ConvertUtils.toString(agency_id)));
            boolean rsSaveHistory = this.saveNotifyHistory(
                    title,
                    "",
                    description,
                    1,
                    DateTimeUtils.getNow(),
                    staff_id,
                    agencyIds,
                    "[]",
                    "[]",
                    "[]",
                    setting_type,
                    setting_value
            );
            if (!rsSaveHistory) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<String> fbAgencyList = this.getListFirebaseAgency(
                    agencyIds,
                    "[]",
                    "[]",
                    "[]"
            );
            if (!fbAgencyList.isEmpty()) {
                ClientResponse crSaveNotifyWaitingPush = this.saveNotifyWaitingPush(
                        fbAgencyList,
                        title,
                        description,
                        image);
            }
        } catch (Exception ex) {

        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected boolean saveNotifyHistory(
            String name,
            String image,
            String description,
            Integer status,
            Date created_date,
            Integer creator_id,
            String agency_ids,
            String city_ids,
            String region_ids,
            String membership_ids,
            String setting_type,
            String setting_value) {
        try {
            List<String> agencyList = JsonUtils.DeSerialize(
                    agency_ids,
                    new TypeToken<List<String>>() {
                    }.getType()
            );
            for (String agency : agencyList) {
                NotifyHistoryEntity notifyHistoryEntity = new NotifyHistoryEntity();

            /*
            private String name;
             */
                notifyHistoryEntity.setName(name);
            /*
            private String image;
             */
                notifyHistoryEntity.setImage(image);
            /*
             description;
             */
                notifyHistoryEntity.setDescription(description);
            /*
            private int status = 1;
             */
                notifyHistoryEntity.setStatus(1);
            /*
            private Date created_date;
             */
                notifyHistoryEntity.setCreated_date(created_date);
            /*
            private Integer creator_id;
             */
                notifyHistoryEntity.setCreator_id(creator_id);

            /*
            private String agency_ids = "[]";
             */
                notifyHistoryEntity.setAgency_ids("[\"" + agency + "\"]");

            /*
            private String city_ids = "[]";
            */
                notifyHistoryEntity.setCity_ids(city_ids);
            /*
            private String region_ids = "[]";
             */
                notifyHistoryEntity.setRegion_ids(region_ids);
            /*
            private String membership_ids = "[]";
             */
                notifyHistoryEntity.setMembership_ids(membership_ids);
            /*
            private String setting_type;
             */
                notifyHistoryEntity.setSetting_type(setting_type);
            /*
            private String setting_value;
             */
                notifyHistoryEntity.setSetting_value(setting_value);
                int rsInsert = this.notifyDB.insertNotifyHistory(notifyHistoryEntity);
            }
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name(), ex);
        }
        return false;
    }

    protected ClientResponse saveNotifyWaitingPush(
            List<String> firebase,
            String title,
            String description,
            String image) {
        try {
            int total = firebase.size();
            Pageable pageable = PageRequest.of(0, 1);
            Page<String> pages = new PageImpl<String>(firebase, pageable, total);
            int totalPage = this.appUtils.getTotalPageBySize(total, ConfigInfo.PAGE_SIZE);
            for (int i = 0; i < totalPage; i++) {
                pageable = PageRequest.of(i, ConfigInfo.PAGE_SIZE);
                Page<String> page2s = new PageImpl<String>(firebase, pageable, total);
                final int toIndex = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(),
                        firebase.size());
                final int fromIndex = Math.max(toIndex - pageable.getPageSize(), 0);
                List<String> fb = firebase.subList(fromIndex, toIndex);

                int rs = this.notifyDB.insertNotifyWaitingPush(
                        JsonUtils.Serialize(fb),
                        title,
                        description,
                        image,
                        NotifyWaitingPushStatus.WAITING.getId());
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.NOTIFY.name());
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected List<String> getListFirebaseAgency(
            String agency_ids,
            String city_ids,
            String region_ids,
            String membership_ids) {
        List<JSONObject> accountList = this.agencyDB.getListFirebaseAgencyAccount(
                agency_ids,
                city_ids,
                region_ids,
                membership_ids
        );

        List<String> fbList = accountList.stream().map(
                e -> e.get("firebase_token").toString()
        ).collect(Collectors.toList());

        return fbList;
    }

    public ClientResponse filterCategory(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.FILTER_CATEGORY_CATALOG, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.catalogDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("image_url", js.get("image"));
                js.put("ltSub", this.dataManager.getProductManager().getMpCategory().get(
                        ConvertUtils.toInt(js.get("id"))
                ).getLtSub());
            }
            int total = this.catalogDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Danh sách lịch sử yêu cầu mở catalog
     *
     * @param request
     * @return
     */
    public ClientResponse filterAgencyCatalogHistory(FilterListRequest request) {
        try {
            /**
             * STT| Đại lý| Mã phiếu| Tên Catalogue| Hình ảnh| Trạng thái hiển thị| Ngày cập nhật| Lý do| Trạng thái
             */
            String query = this.filterUtils.getQuery(FunctionList.FILTER_AGENCY_CATALOG_HISTORY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.catalogDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(js.get("agency_id"))
                ));
            }
            int total = this.catalogDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse resetAgencyCatalog(SessionData sessionData, ApproveCatalogRequest request) {
        try {
            JSONObject agency = this.agencyDB.getAgencyInfo(
                    request.getId()
            );

            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            if (request.getNote() == null || request.getNote().isEmpty()) {
                request.setNote("Reset");
            }

            /**
             * Cập nhật require_catalog = 1:cần chọn
             * Từ chối các catalog đang chờ duyệt và đã duyệt
             * Hoàn tất các phiếu yêu cầu chờ xử lý và đang xử lý
             */

            this.agencyDB.requireCatalog(request.getId(), 1);

            this.catalogDB.rejectAllCatalog(
                    request.getId(),
                    request.getNote(),
                    sessionData.getId()
            );

            this.catalogDB.finishAllAgencyCatalogRequest(
                    request.getId()
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("CATALOG", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    protected void addFilterAgencyData(SessionData sessionData, FilterListRequest request) {
        FilterRequest filterRequest = this.dataManager.getStaffManager().getFilterAgency(
                this.dataManager.getStaffManager().getStaffManageData(
                        sessionData.getId()
                ));
        if (filterRequest != null) {
            request.getFilters().add(filterRequest);
        }
    }
}