package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.warehouse.WarehouseBasicData;
import com.app.server.data.entity.*;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.SortByRequest;
import com.app.server.data.request.warehouse.*;
import com.app.server.database.WarehouseDB;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.CheckValueUtils;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.spring.web.json.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class WarehouseService extends BaseService {
    public ClientResponse filterWarehouse(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_WAREHOUSE, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.warehouseDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE);
            for (JSONObject jsonObject : records) {
                jsonObject.put("create_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(jsonObject.get("creator_id"))
                ));
            }
            int total = this.warehouseDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterWarehouseBillImport(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_WAREHOUSE_BILL_IMPORT, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.warehouseDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE);
            for (JSONObject js : records) {
                js.put("to_info", this.dataManager.getWarehouseManager().getWarehouse(
                        ConvertUtils.toInt(js.get("warehouse_id")))
                );
                if (ConvertUtils.toInt(js.get("target_warehouse_id")) != 0) {
                    js.put("from_info", this.dataManager.getWarehouseManager().getWarehouse(
                            ConvertUtils.toInt(js.get("target_warehouse_id")))
                    );
                } else {
                    js.put("from_info", null);
                }

                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js.get("creator_id")))
                );

                if (ConvertUtils.toInt(js.get("confirmer_id")) != 0) {
                    js.put("confirmer_info", this.dataManager.getStaffManager().getStaff(
                            ConvertUtils.toInt(js.get("confirmer_id")))
                    );
                }
            }
            int total = this.warehouseDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }


    public ClientResponse filterWarehouseBillExport(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_WAREHOUSE_BILL_EXPORT, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.warehouseDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE);
            for (JSONObject js : records) {
                js.put("from_info", this.dataManager.getWarehouseManager().getWarehouse(
                        ConvertUtils.toInt(js.get("warehouse_id")))
                );
                if (WarehouseExportBillType.XUAT_BAN.getValue() == ConvertUtils.toInt(js.get("warehouse_export_bill_type_id"))) {
                    js.put("to_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                            ConvertUtils.toInt(js.get("agency_id")))
                    );
                } else {
                    js.put("to_info", this.dataManager.getWarehouseManager().getWarehouse(
                            ConvertUtils.toInt(js.get("target_warehouse_id")))
                    );
                }

                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js.get("creator_id")))
                );

                if (ConvertUtils.toInt(js.get("confirmer_id")) != 0) {
                    js.put("confirmer_info", this.dataManager.getStaffManager().getStaff(
                            ConvertUtils.toInt(js.get("confirmer_id")))
                    );
                }
            }
            int total = this.warehouseDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Lịch sử xuất kho
     *
     * @param request
     * @return
     */
    public ClientResponse filterWarehouseExportHistory(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_WAREHOUSE_EXPORT_HISTORY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.warehouseDB.filter(query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE);
            for (JSONObject js : records) {
                js.put("warehouse_info", this.dataManager.getWarehouseManager().getWarehouse(
                        ConvertUtils.toInt(js.get("warehouse_id")))
                );
                if (WarehouseExportBillType.XUAT_BAN.getValue() == ConvertUtils.toInt(js.get("warehouse_export_bill_type_id"))) {
                    JSONObject jsOrder = this.orderDB.getAgencyOrderByOrderCode(
                            ConvertUtils.toString(js.get("order_code")));
                    AgencyBasicData agencyBasicData = this.dataManager.getAgencyManager().getAgencyBasicData(
                            ConvertUtils.toInt(js.get("agency_id")));
                    if (agencyBasicData != null && jsOrder != null) {
                        JSONObject jsAddress = JsonUtils.DeSerialize(
                                jsOrder.get("address_delivery").toString(),
                                JSONObject.class);
                        agencyBasicData.setAddress(jsAddress.get("full_name").toString() + " - " + jsAddress.get("address").toString());
                    }
                    js.put("target_info", agencyBasicData);
                } else {
                    js.put("target_info", this.dataManager.getWarehouseManager().getWarehouse(
                            ConvertUtils.toInt(js.get("target_warehouse_id")))
                    );
                }

                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js.get("creator_id")))
                );

                js.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(js.get("product_id"))
                ));
            }
            int total = this.warehouseDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Lịch sử nhập kho
     *
     * @param request
     * @return
     */
    public ClientResponse filterWarehouseImportHistory(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_WAREHOUSE_IMPORT_HISTORY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.warehouseDB.filter(query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE);
            for (JSONObject js : records) {
                js.put("warehouse_info", this.dataManager.getWarehouseManager().getWarehouse(
                        ConvertUtils.toInt(js.get("warehouse_id")))
                );
                if (WarehouseExportBillType.XUAT_BAN.getValue() == ConvertUtils.toInt(js.get("warehouse_export_bill_type_id"))) {
                    js.put("target_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                            ConvertUtils.toInt(js.get("agency_id")))
                    );
                } else {
                    js.put("target_info", this.dataManager.getWarehouseManager().getWarehouse(
                            ConvertUtils.toInt(js.get("warehouse_id")))
                    );
                }
                js.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(js.get("product_id")))
                );

                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js.get("creator_id")))
                );
            }
            int total = this.warehouseDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterWarehouseReport(FilterListRequest request) {
        try {
            String query = this.getQueryReport(FunctionList.LIST_WAREHOUSE_REPORT, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.warehouseDB.filter(query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE);

            String start_date = "";
            String end_date = "";

            for (int i = 0; i < request.getFilters().size(); i++) {
                FilterRequest filter = request.getFilters().get(i);
                if (filter.getType().equals(TypeFilter.DATE)) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    if (filter.getValue1() != null) {
                        start_date = dateFormat.format(new Date(filter.getValue1()));
                    }
                    if (filter.getValue2() != null) {
                        end_date = dateFormat.format(new Date(filter.getValue2()));
                    }
                }
            }

            Date today = DateTimeUtils.getDateTime(DateTimeUtils.getNow(), "yyyy-MM-dd");


            if (start_date.isEmpty() && end_date.isEmpty()) {
                start_date = DateTimeUtils.getNow("yyyy-MM-dd");
                end_date = start_date;
            } else if (start_date.isEmpty() && !end_date.isEmpty()) {
                if (DateTimeUtils.getDateTime(end_date, "yyyy-MM-dd").after(today)) {
                    end_date = DateTimeUtils.getNow("yyyy-MM-dd");
                    start_date = end_date;
                } else {
                    start_date = end_date;
                }
            } else if (!start_date.isEmpty() && end_date.isEmpty()) {
                if (DateTimeUtils.getDateTime(start_date, "yyyy-MM-dd").after(DateTimeUtils.getNow())) {
                    start_date = DateTimeUtils.getNow("yyyy-MM-dd");
                    end_date = start_date;
                } else {
                    end_date = DateTimeUtils.getNow("yyyy-MM-dd");
                }
            } else {
                if (DateTimeUtils.getDateTime(start_date, "yyyy-MM-dd").after(DateTimeUtils.getDateTime(end_date, "yyyy-MM-dd"))) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BETWEEN_INVALID);
                } else if (DateTimeUtils.getDateTime(start_date, "yyyy-MM-dd").after(DateTimeUtils.getNow())) {
                    start_date = DateTimeUtils.getNow("yyyy-MM-dd");
                    end_date = start_date;
                } else if (DateTimeUtils.getDateTime(end_date, "yyyy-MM-dd").after(DateTimeUtils.getNow())) {
                    end_date = DateTimeUtils.getNow("yyyy-MM-dd");
                }
            }

            Date sDate = DateTimeUtils.getDateTime(start_date, "yyyy-MM-dd");
            Date eDate = DateTimeUtils.getDateTime(end_date, "yyyy-MM-dd");

            LogUtil.printDebug("sDate" + sDate);
            LogUtil.printDebug("eDate" + eDate);

            if (sDate.equals(eDate)) {
                if (eDate.equals(today)) {
                    for (JSONObject js : records) {
                        int product_id = ConvertUtils.toInt(js.get("product_id"));
                        js.put("product_info",
                                this.dataManager.getProductManager().getProductBasicData(product_id));

                        JSONObject rsToday = this.warehouseDB.getWarehouseInfoByProduct(product_id);

                        /**
                         * tồn đầu ngày
                         */
                        js.put("quantity_start_today",
                                rsToday.get("quantity_start_today"));

                        /**
                         * nhập trong ngày
                         */
                        js.put("quantity_import_today",
                                rsToday.get("quantity_import_today"));

                        /**
                         * xuất trong ngày
                         */
                        js.put("quantity_export_today",
                                rsToday.get("quantity_export_today"));

                        /**
                         * tồn cuối ngày
                         */
                        js.put("quantity_end_today",
                                rsToday.get("quantity_end_today"));

                        /**
                         * hiện tại
                         */
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_ship_today", rsToday.get("quantity_waiting_ship_today"));
                        js.put("quantity_reality", this.getQuantityReality(rsToday));
                        js.put("quantity_availability", this.getQuantityAvailability(rsToday));
                    }
                } else {
                    for (JSONObject js : records) {
                        int product_id = ConvertUtils.toInt(js.get("product_id"));
                        js.put("product_info",
                                this.dataManager.getProductManager().getProductBasicData(product_id));

                        JSONObject rsToday = this.warehouseDB.getWarehouseInfoByProduct(product_id);
                        JSONObject rsDate = this.warehouseDB.getWarehouseInfoDateByProduct(product_id, start_date);

                        /**
                         * tồn đầu ngày
                         */
                        js.put("quantity_start_today",
                                rsDate == null ? 0 : rsDate.get("quantity_start_today"));

                        /**
                         * nhập trong ngày
                         */
                        js.put("quantity_import_today",
                                rsDate == null ? 0 : rsDate.get("quantity_import_today"));

                        /**
                         * xuất trong ngày
                         */
                        js.put("quantity_export_today",
                                rsDate == null ? 0 : rsDate.get("quantity_export_today"));

                        /**
                         * tồn cuối ngày
                         */
                        js.put("quantity_end_today",
                                rsDate == null ? 0 : rsDate.get("quantity_end_today"));

                        /**
                         * hiện tại
                         */
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_ship_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_reality", this.getQuantityReality(rsToday));
                        js.put("quantity_availability", this.getQuantityAvailability(rsToday));
                    }
                }
            } else {
                if (eDate.equals(today)) {
                    for (JSONObject js : records) {
                        int product_id = ConvertUtils.toInt(js.get("product_id"));
                        js.put("product_info",
                                this.dataManager.getProductManager().getProductBasicData(product_id));

                        JSONObject rsToday = this.warehouseDB.getWarehouseInfoByProduct(product_id);
                        JSONObject rsStartDate = this.warehouseDB.getWarehouseInfoDateByProduct(product_id, start_date);
                        /**
                         * tồn đầu ngày
                         */
                        js.put("quantity_start_today",
                                rsStartDate == null ? 0 : rsStartDate.get("quantity_start_today"));

                        /**
                         * nhập trong ngày
                         */
                        js.put("quantity_import_today",
                                ConvertUtils.toInt(rsToday.get("quantity_import_today"))
                                        + this.warehouseDB.getQuantityImportByDate(product_id, start_date, end_date));

                        /**
                         * xuất trong ngày
                         */
                        js.put("quantity_export_today",
                                ConvertUtils.toInt(rsToday.get("quantity_export_today"))
                                        + this.warehouseDB.getQuantityExportByDate(product_id, start_date, end_date));

                        /**
                         * tồn cuối kỳ
                         */
                        js.put("quantity_end_today",
                                rsToday.get("quantity_end_today"));

                        /**
                         * hiện tại
                         */
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_ship_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_reality", this.getQuantityReality(rsToday));
                        js.put("quantity_availability", this.getQuantityAvailability(rsToday));
                    }
                } else {
                    for (JSONObject js : records) {
                        int product_id = ConvertUtils.toInt(js.get("product_id"));
                        js.put("product_info",
                                this.dataManager.getProductManager().getProductBasicData(product_id));

                        JSONObject rsToday = this.warehouseDB.getWarehouseInfoByProduct(product_id);
                        JSONObject rsStartDate = this.warehouseDB.getWarehouseInfoDateByProduct(product_id, start_date);
                        /**
                         * tồn đầu ngày
                         */
                        js.put("quantity_start_today",
                                rsStartDate == null ? 0 : rsStartDate.get("quantity_start_today"));

                        /**
                         * nhập trong ngày
                         */
                        js.put("quantity_import_today",
                                this.warehouseDB.getQuantityImportByDate(product_id,
                                        start_date,
                                        end_date));

                        /**
                         * xuất trong ngày
                         */
                        js.put("quantity_export_today",
                                this.warehouseDB.getQuantityExportByDate(product_id, start_date, end_date));

                        /**
                         * tồn cuối kỳ
                         */
                        js.put("quantity_end_today",
                                rsToday.get("quantity_end_today"));

                        /**
                         * hiện tại
                         */
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_ship_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_reality", this.getQuantityReality(rsToday));
                        js.put("quantity_availability", this.getQuantityAvailability(rsToday));
                    }
                }
            }


            int total = this.warehouseDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private int getQuantityReality(JSONObject rsToday) {
        if (rsToday != null) {
            return
                    ConvertUtils.toInt(rsToday.get("quantity_start_today"))
                            + ConvertUtils.toInt(rsToday.get("quantity_import_today"))
                            - ConvertUtils.toInt(rsToday.get("quantity_export_today"))
                    ;
        }
        return 0;
    }

    private int getQuantityAvailability(JSONObject rsToday) {
        if (rsToday != null) {
            return
                    ConvertUtils.toInt(rsToday.get("quantity_start_today"))
                            + ConvertUtils.toInt(rsToday.get("quantity_import_today"))
                            - ConvertUtils.toInt(rsToday.get("quantity_export_today"))
                            - ConvertUtils.toInt(rsToday.get("quantity_waiting_approve_today"))
                            - ConvertUtils.toInt(rsToday.get("quantity_waiting_ship_today"))
                    ;
        }
        return 0;
    }

    private String getQueryReport(FunctionList functionList, List<FilterRequest> filterRequestList, List<SortByRequest> sorts) {
        StringBuilder query = new StringBuilder(functionList.getSql());

        List<FilterRequest> filters = new ArrayList<>();
        for (FilterRequest filterRequest : filterRequestList) {
            if (!filterRequest.getType().equals(TypeFilter.DATE)) {
                filters.add(filterRequest);
            }
        }

        if (filters.size() != 0) {
            if (!query.toString().toUpperCase(Locale.ROOT).contains("WHERE")) {
                query.append(" WHERE ");
            } else {
                query.append(" AND ");
            }
            for (int i = 0; i < filters.size(); i++) {
                FilterRequest filter = filters.get(i);
                String value = "";

                if (CheckValueUtils.isNumberic(ConvertUtils.toString(filter.getValue()))) {
                    value = "=" + ConvertUtils.toString(filter.getValue()) + " ";
                } else {
                    value = "  LIKE '" + ConvertUtils.toString(filter.getValue()) + "%' ";
                }
                if (filter.getType().equals(TypeFilter.SELECTBOX)
                        || filter.getType().equals(TypeFilter.CAS)) {
                    query.append(filter.getKey()).append(value);
                } else if (filter.getType().equals(TypeFilter.SEARCH)) {
                    String searchKey = functionList.getSearchDefault();
                    if (!filter.getKey().isEmpty()) {
                        searchKey = filter.getKey();
                    }

                    if (!searchKey.isEmpty() && !filter.getValue().isEmpty()) {
                        String[] stringKeys = searchKey.split(",");
                        String searchQuery = "";
                        for (String stringKey : stringKeys) {
                            if (!searchQuery.isEmpty()) {
                                searchQuery += " OR ";
                            }
                            searchQuery += stringKey + " LIKE '%" + filter.getValue() + "%' ";
                        }
                        searchQuery = " ( " + searchQuery + " ) ";
                        query.append(searchQuery);
                    } else {
                        continue;
                    }
                } else if (filter.getType().equals(TypeFilter.DATE)) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    if (filter.getValue1() != null || filter.getValue2() != null) {
                        if (filter.getValue1() != null && filter.getValue2() == null) {
                            String time1 = dateFormat.format(new Date(filter.getValue1()));
                            query.append(filter.getKey()).append(">='").append(time1).append("'");
                        } else if (filter.getValue1() == null && filter.getValue2() != null) {
                            String time2 = dateFormat.format(new Date(filter.getValue2()));
                            query.append(filter.getKey()).append("<='").append(time2).append("'");
                        } else {
                            String time1 = dateFormat.format(new Date(filter.getValue1()));
                            String time2 = dateFormat.format(new Date(filter.getValue2()));
                            query.append(filter.getKey()).append(">='").append(time1).append("' AND ").append(filter.getKey()).append("<='").append(time2).append("'");
                        }
                    } else {
                        continue;
                    }
                } else if (filter.getType().equals(TypeFilter.FROM_TO)) {
                    if (filter.getValue1() != null || filter.getValue2() != null) {
                        if (filter.getValue1() != null && filter.getValue2() == null) {
                            query.append(filter.getKey()).append(">='").append(filter.getValue1()).append("'");
                        } else if (filter.getValue1() == null && filter.getValue2() != null) {
                            query.append(filter.getKey()).append("<='").append(filter.getValue2()).append("'");
                        } else {
                            query.append(filter.getKey()).append(">='").append(filter.getValue1()).append("' AND ").append(filter.getKey()).append("<='").append(filter.getValue2()).append("'");
                        }
                    } else {
                        continue;
                    }
                }
                if (i < filters.size() - 1) {
                    query.append(" AND ");
                }

            }
        }

        if (sorts != null && !sorts.isEmpty()) {
            if (!query.toString().toUpperCase(Locale.ROOT).contains("ORDER BY")) {
                query.append(" ORDER BY");
            }

            for (int i = 0; i < sorts.size(); i++) {
                query.append(" " + sorts.get(i).getKey() + " " + sorts.get(i).getType());
                if (i < sorts.size() - 1) {
                    query.append(",");
                }
            }
        } else {
            if (functionList.getSortDefault().isEmpty()) {
                query.append(" ORDER BY id DESC");
            } else {
                query.append(" ORDER BY " + functionList.getSortDefault());

            }
        }
//        LogUtil.printDebug(query.toString());
        return query.toString();
    }

    public ClientResponse createWarehouseBillImport(SessionData sessionData, CreateWarehouseBillRequest request) {
        try {
            WarehouseBillEntity warehouseBillEntity = new WarehouseBillEntity();
            warehouseBillEntity.setCreator_id(sessionData.getId());
            warehouseBillEntity.setCode(this.generateWarehouseBillImportCode());
            warehouseBillEntity.setWarehouse_bill_type_id(WarehouseBillType.IMPORT.getValue());
            warehouseBillEntity.setNote(request.getNote());
            warehouseBillEntity.setTarget_warehouse_id(request.getTarget_info());
            warehouseBillEntity.setData(request.getTarget_info() == null ? "0" : request.getTarget_info().toString());
            warehouseBillEntity.setCreated_date(DateTimeUtils.getNow());
            warehouseBillEntity.setStatus(WarehouseBillStatus.DRAFT.getId());
            warehouseBillEntity.setWarehouse_id(request.getWarehouse_id());
            int rsInsert = this.warehouseDB.insertWarehouseBill(warehouseBillEntity);
            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            warehouseBillEntity.setId(rsInsert);

            for (WarehouseBillDetailRequest warehouseBillDetailRequest : request.getProducts()) {
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(warehouseBillDetailRequest.getProduct_id());
                if (productCache == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                WarehouseBillDetailEntity warehouseBillDetailEntity = new WarehouseBillDetailEntity();
                warehouseBillDetailEntity.setCreated_date(DateTimeUtils.getNow());
                warehouseBillDetailEntity.setWarehouse_bill_id(warehouseBillEntity.getId());
                warehouseBillDetailEntity.setProduct_id(warehouseBillDetailRequest.getProduct_id());
                warehouseBillDetailEntity.setProduct_quantity(warehouseBillDetailRequest.getProduct_quantity());
                warehouseBillDetailEntity.setNote(warehouseBillDetailRequest.getNote());

                int rsInsertDetail = this.warehouseDB.insertWarehouseBillDetail(warehouseBillDetailEntity);
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            JSONObject data = new JSONObject();
            data.put("id", warehouseBillEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private String generateWarehouseBillImportCode() {
        int count = this.warehouseDB.getTotalWarehouseBillImportToday();

        return "NK" + this.appUtils.getYYMMDD(DateTimeUtils.getNow()) + "_" + String.format("%03d", count + 1);
    }

    private String generateWarehouseBillExportCode() {
        int count = this.warehouseDB.getTotalWarehouseBillExportToday();
        return "CK" + this.appUtils.getYYMMDD(DateTimeUtils.getNow()) + "_" + String.format("%03d", count + 1);
    }

    public ClientResponse editWarehouseBillImport(SessionData sessionData, EditWarehouseBillRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            WarehouseBillEntity warehouseBillEntity = this.warehouseDB.getWarehouseBill(request.getId());
            if (warehouseBillEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);

            }
            warehouseBillEntity.setModifier_id(sessionData.getId());
            warehouseBillEntity.setModified_date(DateTimeUtils.getNow());
            warehouseBillEntity.setNote(request.getNote());
            warehouseBillEntity.setWarehouse_id(request.getWarehouse_id());
            warehouseBillEntity.setTarget_warehouse_id(request.getTarget_info());
            warehouseBillEntity.setData(request.getTarget_info() == null ? "0" : request.getTarget_info().toString());
            boolean rsUpdate = this.warehouseDB.updateWarehouseBill(warehouseBillEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Xóa dữ liệu sản phẩm cũ
             */
            boolean rsClearWarehouseBillDetail = this.warehouseDB.clearWarehouseBillDetail(request.getId());
            if (!rsClearWarehouseBillDetail) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            for (WarehouseBillDetailRequest warehouseBillDetailRequest : request.getProducts()) {
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(warehouseBillDetailRequest.getProduct_id());
                if (productCache == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                WarehouseBillDetailEntity warehouseBillDetailEntity = new WarehouseBillDetailEntity();
                warehouseBillDetailEntity.setCreated_date(DateTimeUtils.getNow());
                warehouseBillDetailEntity.setWarehouse_bill_id(warehouseBillEntity.getId());
                warehouseBillDetailEntity.setProduct_id(warehouseBillDetailRequest.getProduct_id());
                warehouseBillDetailEntity.setProduct_quantity(warehouseBillDetailRequest.getProduct_quantity());
                warehouseBillDetailEntity.setNote(warehouseBillDetailRequest.getNote());

                int rsInsertDetail = this.warehouseDB.insertWarehouseBillDetail(warehouseBillDetailEntity);
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createWarehouseBillExport(SessionData sessionData, CreateWarehouseBillRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            WarehouseBasicData warehouse = this.dataManager.getWarehouseManager().getWarehouse(request.getWarehouse_id());
            if (warehouse == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            if (this.dataManager.getWarehouseManager().checkAllowSell(warehouse.getAllow_sell())) {
                /**
                 * Check tồn kho
                 */
                for (WarehouseBillDetailRequest product : request.getProducts()) {
                    int quantity_availability = this.getQuantityAvailability(this.warehouseDB.getWarehouseInfoByProduct(product.getProduct_id()));
                    if (quantity_availability < product.getProduct_quantity()) {
                        ClientResponse crTonKho = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_QUANTITY_AVAILABILY_INVALID);
                        crTonKho.setMessage("[" + crTonKho.getMessage() + "] Sản phẩm " + this.dataManager.getProductManager().getProductFullName(product.getProduct_id()) + " chỉ còn " + quantity_availability);
                        return crTonKho;
                    }
                }
            }

            WarehouseBillEntity warehouseBillEntity = new WarehouseBillEntity();
            warehouseBillEntity.setCreator_id(sessionData.getId());
            warehouseBillEntity.setCode(this.generateWarehouseBillExportCode());
            warehouseBillEntity.setWarehouse_bill_type_id(WarehouseBillType.EXPORT.getValue());
            warehouseBillEntity.setWarehouse_export_bill_type_id(WarehouseExportBillType.CHUYEN_KHO.getValue());
            warehouseBillEntity.setNote(request.getNote());
            warehouseBillEntity.setWarehouse_id(request.getWarehouse_id());
            warehouseBillEntity.setTarget_warehouse_id(request.getTarget_info());
            warehouseBillEntity.setCreated_date(DateTimeUtils.getNow());
            warehouseBillEntity.setStatus(WarehouseBillStatus.DRAFT.getId());

            int rsInsert = this.warehouseDB.insertWarehouseBill(warehouseBillEntity);
            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            warehouseBillEntity.setId(rsInsert);

            for (WarehouseBillDetailRequest warehouseBillDetailRequest : request.getProducts()) {
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(warehouseBillDetailRequest.getProduct_id());
                if (productCache == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                WarehouseBillDetailEntity warehouseBillDetailEntity = new WarehouseBillDetailEntity();
                warehouseBillDetailEntity.setCreated_date(DateTimeUtils.getNow());
                warehouseBillDetailEntity.setWarehouse_bill_id(warehouseBillEntity.getId());
                warehouseBillDetailEntity.setProduct_id(warehouseBillDetailRequest.getProduct_id());
                warehouseBillDetailEntity.setProduct_quantity(warehouseBillDetailRequest.getProduct_quantity());
                warehouseBillDetailEntity.setNote(warehouseBillDetailRequest.getNote());

                int rsInsertDetail = this.warehouseDB.insertWarehouseBillDetail(warehouseBillDetailEntity);
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }
            JSONObject data = new JSONObject();
            data.put("id", warehouseBillEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runWarehouseInfoByStartDate() {
        try {
            List<JSONObject> jsProductList = this.productDB.getAllProduct();
            for (JSONObject jsProduct : jsProductList) {
                int product_id = ConvertUtils.toInt(jsProduct.get("id"));
                WarehouseInfoEntity warehouseInfoEntity = this.initWarehouseStartDate(product_id);
                if (warehouseInfoEntity == null) {
                    this.alertToTelegram(
                            "Reset tồn kho đầu ngày: product_id-" + product_id + " FAIL",
                            ResponseStatus.FAIL
                    );
                }
            }

            this.reportToTelegram(
                    "Reset tồn kho đầu ngày: FINISH",
                    ResponseStatus.SUCCESS
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private WarehouseInfoEntity initWarehouseStartDate(int product_id) {
        try {
            WarehouseInfoEntity warehouseInfoEntity;
            JSONObject jsWarehouseInfo = this.warehouseDB.getWarehouseInfoByProduct(product_id);
            if (jsWarehouseInfo != null) {
                warehouseInfoEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(jsWarehouseInfo), WarehouseInfoEntity.class);

                int total = warehouseInfoEntity.getQuantity_start_today()
                        + warehouseInfoEntity.getQuantity_import_today()
                        - warehouseInfoEntity.getQuantity_export_today();


                warehouseInfoEntity.setQuantity_start_today(total);
                warehouseInfoEntity.setQuantity_import_today(0);
                warehouseInfoEntity.setQuantity_export_today(0);
                boolean rsUpdate = this.warehouseDB.updateWarehouseInfo(warehouseInfoEntity);
                if (!rsUpdate) {
                    return null;
                }
            } else {
                warehouseInfoEntity = new WarehouseInfoEntity();
                warehouseInfoEntity.setProduct_id(product_id);
                warehouseInfoEntity.setCreated_date(DateTimeUtils.getNow());
                warehouseInfoEntity.setModified_date(DateTimeUtils.getNow());
                int rsInsert = this.warehouseDB.insertWarehouseInfo(warehouseInfoEntity);
                if (rsInsert <= 0) {
                    return null;
                }
                warehouseInfoEntity.setId(rsInsert);
            }

            return warehouseInfoEntity;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return null;
    }

    public ClientResponse runWarehouseInfoByEndDate() {
        try {
            List<JSONObject> jsProductList = this.productDB.getAllProduct();
            for (JSONObject jsProduct : jsProductList) {
                int product_id = ConvertUtils.toInt(jsProduct.get("id"));
                JSONObject jsWarehouseInfo = this.warehouseDB.getWarehouseInfoByProduct(product_id);
                if (jsWarehouseInfo != null) {
                    WarehouseInfoDateEntity warehouseInfoDateEntity = new WarehouseInfoDateEntity();
                    WarehouseInfoEntity warehouseInfoEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(jsWarehouseInfo), WarehouseInfoEntity.class);

                    warehouseInfoDateEntity.setProduct_id(product_id);
                    warehouseInfoDateEntity.setQuantity_start_today(warehouseInfoEntity.getQuantity_start_today());
                    warehouseInfoDateEntity.setQuantity_import_today(warehouseInfoEntity.getQuantity_import_today());
                    warehouseInfoDateEntity.setQuantity_export_today(warehouseInfoEntity.getQuantity_export_today());
                    warehouseInfoDateEntity.setQuantity_waiting_approve_today(warehouseInfoEntity.getQuantity_waiting_approve_today());
                    warehouseInfoDateEntity.setQuantity_waiting_ship_today(warehouseInfoEntity.getQuantity_waiting_ship_today());
                    warehouseInfoDateEntity.setQuantity_end_today(warehouseInfoEntity.getQuantity_end_today());
                    warehouseInfoDateEntity.setCreated_date(DateTimeUtils.getNow());
                    int rsInsert = this.warehouseDB.insertWarehouseInfoDate(warehouseInfoDateEntity);
                    if (rsInsert <= 0) {
                        this.alertToTelegram(
                                "Lưu tồn kho cuối ngày: product_id-" + product_id + " FAIL",
                                ResponseStatus.FAIL
                        );
                    }
                }
            }

            this.reportToTelegram(
                    "Lưu tồn kho cuối ngày: FINISH",
                    ResponseStatus.SUCCESS
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse approveWarehouseBillImport(SessionData sessionData, ApproveWarehouseBillRequest request) {
        try {
            WarehouseBillEntity warehouseBillEntity = this.warehouseDB.getWarehouseBill(request.getId());
            if (warehouseBillEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (WarehouseBillType.IMPORT.getValue() != warehouseBillEntity.getWarehouse_bill_type_id()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.WAREHOUSE_BILL_TYPE_INVALID);
            }

            if (WarehouseBillStatus.WAITING.getId() != warehouseBillEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            List<JSONObject> jsProductList = this.warehouseDB.getListProductInBill(request.getId());


            WarehouseBasicData warehouseBasicData = this.dataManager.getWarehouseManager().getWarehouse(warehouseBillEntity.getWarehouse_id());
            if (warehouseBasicData == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (this.dataManager.getWarehouseManager().checkAllowSell(warehouseBasicData.getAllow_sell())) {
                /**
                 * Cộng tồn kho
                 */
                for (JSONObject jsProduct : jsProductList) {
                    int product_id = ConvertUtils.toInt(jsProduct.get("product_id"));
                    int product_quantity = ConvertUtils.toInt(jsProduct.get("product_quantity"));
                    JSONObject warehouseInfo = this.warehouseDB.getWarehouseInfoByProduct(product_id);
                    if (warehouseInfo == null) {
                        this.initWarehouseStartDate(product_id);
                    }

                    boolean rsImportWarehouse = this.importWarehouse(
                            product_id, product_quantity,
                            warehouseBillEntity.getCode());
                    if (!rsImportWarehouse) {
                        /**
                         * thông báo qua tele
                         */
                    }
                }
            }

            warehouseBillEntity.setConfirmed_date(DateTimeUtils.getNow());
            warehouseBillEntity.setConfirmer_id(sessionData.getId());
            warehouseBillEntity.setReason(request.getNote());
            warehouseBillEntity.setStatus(WarehouseBillStatus.CONFIRMED.getId());
            boolean rsUpdate = this.warehouseDB.updateWarehouseBill(warehouseBillEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean importWarehouse(int product_id, int product_quantity,
                                    String note) {
        try {
            JSONObject jsonObject = this.warehouseDB.getWarehouseInfoByProduct(product_id);
            if (jsonObject == null) {
                return false;
            }

            int quantity_import_today = ConvertUtils.toInt(jsonObject.get("quantity_import_today"));
            boolean rsIncrease = this.warehouseDB.increaseQuantityImportToday1(
                    product_id, product_quantity);
            if (!rsIncrease) {
                return false;
            }

            /**
             * Lưu lịch sử
             */
            this.saveWarehouseStockHistory(
                    WarehouseChangeType.IMPORT,
                    product_id,
                    quantity_import_today,
                    product_quantity,
                    note);

            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return false;
    }

    private boolean exportWarehouse(int product_id, int product_quantity, String note) {
        try {
            JSONObject jsonObject = this.warehouseDB.getWarehouseInfoByProduct(product_id);
            if (jsonObject == null) {
                this.alertToTelegram("exportWarehouse: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
                return false;
            }
            int quantity = ConvertUtils.toInt(jsonObject.get("quantity_export_today"));
            boolean rs = this.warehouseDB.increaseQuantityExportToday1(product_id, product_quantity);
            if (!rs) {
                this.alertToTelegram("exportWarehouse: " + product_id + " FAILED",
                        ResponseStatus.FAIL);
            }

            /**
             * lưu lịch sử
             */
            this.saveWarehouseStockHistory(
                    WarehouseChangeType.EXPORT, product_id, quantity, product_quantity,
                    note);
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return false;
    }

    public ClientResponse rejectWarehouseBillImport(SessionData sessionData, RejectWarehouseBillRequest request) {
        try {
            WarehouseBillEntity warehouseBillEntity = this.warehouseDB.getWarehouseBill(request.getId());
            if (warehouseBillEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (WarehouseBillStatus.WAITING.getId() != warehouseBillEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            warehouseBillEntity.setModifier_id(sessionData.getId());
            warehouseBillEntity.setModified_date(DateTimeUtils.getNow());
            warehouseBillEntity.setStatus(WarehouseBillStatus.DRAFT.getId());
            warehouseBillEntity.setReason(request.getNote());
            boolean rsUpdate = this.warehouseDB.updateWarehouseBill(warehouseBillEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editWarehouseBillExport(SessionData sessionData, EditWarehouseBillRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            WarehouseBillEntity warehouseBillEntity = this.warehouseDB.getWarehouseBill(request.getId());
            if (warehouseBillEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            warehouseBillEntity.setModifier_id(sessionData.getId());
            warehouseBillEntity.setNote(request.getNote());
            warehouseBillEntity.setModified_date(DateTimeUtils.getNow());
            warehouseBillEntity.setTarget_warehouse_id(request.getTarget_info());
            warehouseBillEntity.setWarehouse_id(request.getWarehouse_id());
            boolean rsUpdate = this.warehouseDB.updateWarehouseBill(warehouseBillEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * xóa dữ liệu sản phẩm cũ
             */
            boolean rsClearWarehouseBillDetail = this.warehouseDB.clearWarehouseBillDetail(warehouseBillEntity.getId());
            if (!rsClearWarehouseBillDetail) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            for (WarehouseBillDetailRequest warehouseBillDetailRequest : request.getProducts()) {
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(warehouseBillDetailRequest.getProduct_id());
                if (productCache == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                WarehouseBillDetailEntity warehouseBillDetailEntity = new WarehouseBillDetailEntity();
                warehouseBillDetailEntity.setCreated_date(DateTimeUtils.getNow());
                warehouseBillDetailEntity.setWarehouse_bill_id(warehouseBillEntity.getId());
                warehouseBillDetailEntity.setProduct_id(warehouseBillDetailRequest.getProduct_id());
                warehouseBillDetailEntity.setProduct_quantity(warehouseBillDetailRequest.getProduct_quantity());
                warehouseBillDetailEntity.setNote(warehouseBillDetailRequest.getNote());

                int rsInsertDetail = this.warehouseDB.insertWarehouseBillDetail(warehouseBillDetailEntity);
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse approveWarehouseBillExport(SessionData sessionData, ApproveWarehouseBillRequest request) {
        try {
            WarehouseBillEntity warehouseBillEntity = this.warehouseDB.getWarehouseBill(request.getId());
            if (warehouseBillEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (WarehouseBillType.EXPORT.getValue() != warehouseBillEntity.getWarehouse_bill_type_id()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.WAREHOUSE_BILL_TYPE_INVALID);
            }

            if (WarehouseBillStatus.WAITING.getId() != warehouseBillEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            List<JSONObject> jsProductList = this.warehouseDB.getListProductInBill(request.getId());

            WarehouseBasicData warehouse = this.dataManager.getWarehouseManager().getWarehouse(warehouseBillEntity.getWarehouse_id());
            if (warehouse == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (this.dataManager.getWarehouseManager().checkAllowSell(warehouse.getAllow_sell())) {
                /**
                 * Check tồn kho
                 */
                for (JSONObject jsProduct : jsProductList) {
                    int product_id = ConvertUtils.toInt(jsProduct.get("product_id"));
                    int product_quantity = ConvertUtils.toInt(jsProduct.get("product_quantity"));

                    int quantity_availability = this.getQuantityAvailability(this.warehouseDB.getWarehouseInfoByProduct(product_id));
                    if (quantity_availability < product_quantity) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_QUANTITY_AVAILABILY_INVALID);
                        clientResponse.setMessage("[" + this.dataManager.getProductManager().getProductFullName(product_id) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }

                /**
                 * Trừ tồn kho xuất
                 */
                for (JSONObject jsProduct : jsProductList) {
                    int product_id = ConvertUtils.toInt(jsProduct.get("product_id"));
                    int product_quantity = ConvertUtils.toInt(jsProduct.get("product_quantity"));
                    boolean rsExportWarehouse = this.exportWarehouse(
                            product_id, product_quantity,
                            warehouseBillEntity.getCode());
                    if (!rsExportWarehouse) {
                        /**
                         * thông báo qua tele
                         */
                    }
                }
            }

            warehouseBillEntity.setConfirmed_date(DateTimeUtils.getNow());
            warehouseBillEntity.setConfirmer_id(sessionData.getId());
            warehouseBillEntity.setReason(request.getNote());
            warehouseBillEntity.setStatus(WarehouseBillStatus.CONFIRMED.getId());
            boolean rsUpdate = this.warehouseDB.updateWarehouseBill(warehouseBillEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Tạo phiếu nhập kho đối với loại chuyển kho
             */
            if (WarehouseExportBillType.CHUYEN_KHO.getValue() == warehouseBillEntity.getWarehouse_export_bill_type_id()) {
                WarehouseBasicData warehouseTarget = this.dataManager.getWarehouseManager().getWarehouse(warehouseBillEntity.getTarget_warehouse_id());
                if (warehouseTarget == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                /**
                 * Tạo phiếu nhập
                 */
                WarehouseBillEntity importBillEntity = new WarehouseBillEntity();
                importBillEntity.setCode(warehouseBillEntity.getCode());
                importBillEntity.setWarehouse_id(warehouseBillEntity.getTarget_warehouse_id());
                importBillEntity.setData(warehouseBillEntity.getWarehouse_id().toString());
                importBillEntity.setWarehouse_bill_type_id(WarehouseBillType.IMPORT.getValue());
                importBillEntity.setCreated_date(DateTimeUtils.getNow());
                importBillEntity.setCreator_id(warehouseBillEntity.getCreator_id());
                importBillEntity.setConfirmed_date(DateTimeUtils.getNow());
                importBillEntity.setConfirmer_id(warehouseBillEntity.getConfirmer_id());
                importBillEntity.setStatus(WarehouseBillStatus.CONFIRMED.getId());
                importBillEntity.setTarget_warehouse_id(warehouseBillEntity.getWarehouse_id());
                int rsInsert = this.warehouseDB.insertWarehouseBill(importBillEntity);
                if (rsInsert <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                importBillEntity.setId(rsInsert);

                /**
                 * Lưu sản phẩm chuyển kho vào phiếu nhập
                 */
                for (JSONObject jsProduct : jsProductList) {
                    WarehouseBillDetailEntity warehouseBillDetailEntity = new WarehouseBillDetailEntity();
                    warehouseBillDetailEntity.setWarehouse_bill_id(importBillEntity.getId());
                    warehouseBillDetailEntity.setNote(ConvertUtils.toString(jsProduct.get("note")));
                    warehouseBillDetailEntity.setProduct_id(ConvertUtils.toInt(jsProduct.get("product_id")));
                    warehouseBillDetailEntity.setProduct_quantity(ConvertUtils.toInt(jsProduct.get("product_quantity")));
                    warehouseBillDetailEntity.setCreated_date(DateTimeUtils.getNow());

                    int rsInsertDetail = this.warehouseDB.insertWarehouseBillDetail(warehouseBillDetailEntity);
                    if (rsInsertDetail <= 0) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }

                /**
                 * Cộng tồn kho
                 */
                if (this.dataManager.getWarehouseManager().checkAllowSell(warehouseTarget.getAllow_sell())) {
                    for (JSONObject jsProduct : jsProductList) {
                        int product_id = ConvertUtils.toInt(jsProduct.get("product_id"));
                        int product_quantity = ConvertUtils.toInt(jsProduct.get("product_quantity"));
                        boolean rsImportWarehouse = this.importWarehouse(
                                product_id, product_quantity,
                                warehouseBillEntity.getCode());
                        if (!rsImportWarehouse) {
                            /**
                             * thông báo qua tele
                             */
                        }
                    }
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse rejectWarehouseBillExport(SessionData sessionData, RejectWarehouseBillRequest request) {
        try {
            WarehouseBillEntity warehouseBillEntity = this.warehouseDB.getWarehouseBill(request.getId());
            if (warehouseBillEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (WarehouseBillStatus.WAITING.getId() != warehouseBillEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            warehouseBillEntity.setModifier_id(sessionData.getId());
            warehouseBillEntity.setModified_date(DateTimeUtils.getNow());
            warehouseBillEntity.setStatus(WarehouseBillStatus.DRAFT.getId());
            warehouseBillEntity.setReason(request.getNote());
            boolean rsUpdate = this.warehouseDB.updateWarehouseBill(warehouseBillEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse cancelWarehouseBillExport(SessionData sessionData, CancelWarehouseBillRequest request) {
        try {
            WarehouseBillEntity warehouseBillEntity = this.warehouseDB.getWarehouseBill(request.getId());
            if (warehouseBillEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (WarehouseBillStatus.DRAFT.getId() != warehouseBillEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            warehouseBillEntity.setModifier_id(sessionData.getId());
            warehouseBillEntity.setModified_date(DateTimeUtils.getNow());
            warehouseBillEntity.setReason(request.getNote());
            warehouseBillEntity.setStatus(WarehouseBillStatus.CANCEL.getId());
            boolean rsUpdate = this.warehouseDB.updateWarehouseBill(warehouseBillEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getListOrderWaitingShip(SessionData sessionData, GetListOrderWaitingShipRequest request) {
        try {
            List<JSONObject> records = new ArrayList<>();
            List<JSONObject> jsOrderList = this.orderDB.getListOrderWaitingShip(request.getProduct_id());
            for (JSONObject jsonObject : jsOrderList) {
                int total_quantity =
                        this.orderDB.getTotalProductQuantityBuy(
                                ConvertUtils.toInt(jsonObject.get("agency_order_id")),
                                request.getProduct_id()
                        ) + this.orderDB.getTotalGiftQuantityBuy(
                                ConvertUtils.toInt(jsonObject.get("agency_order_id")),
                                request.getProduct_id()
                        ) - this.orderDB.getProductQuantityDelivery(
                                ConvertUtils.toInt(jsonObject.get("agency_order_id")),
                                request.getProduct_id()
                        ) - this.orderDB.getGiftQuantityDelivery(
                                ConvertUtils.toInt(jsonObject.get("agency_order_id")),
                                request.getProduct_id()
                        );
                if (total_quantity <= 0) {
                    continue;
                }
                jsonObject.put("total_quantity", total_quantity);
                long total_end_price = ConvertUtils.toLong(jsonObject.get("total_end_price"));
                jsonObject.put("total_end_price", total_end_price);

                JSONObject jsOrder = this.orderDB.getAgencyOrder(ConvertUtils.toInt(jsonObject.get("agency_order_id")));
                if (jsOrder == null) {
                    continue;
                }
                if (!this.dataManager.getStaffManager().checkManageOrder(
                        sessionData.getId(),
                        this.agencyDB.getAgencyInfoById(ConvertUtils.toInt(jsOrder.get("agency_id"))),
                        ConvertUtils.toInt(jsOrder.get("status")))) {
                    continue;
                }


                records.add(jsonObject);
            }
            JSONObject data = new JSONObject();
            data.put("orders", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse requireApproveWarehouseBillExport(SessionData sessionData, RequireApproveWarehouseBillRequest request) {
        try {
            WarehouseBillEntity warehouseBillEntity = this.warehouseDB.getWarehouseBill(request.getId());
            if (warehouseBillEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (WarehouseBillStatus.DRAFT.getId() != warehouseBillEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            WarehouseBasicData warehouseBasicData = this.dataManager.getWarehouseManager().getWarehouse(warehouseBillEntity.getWarehouse_id());
            if (warehouseBasicData == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (this.dataManager.getWarehouseManager().checkAllowSell(warehouseBasicData.getAllow_sell())) {
                List<JSONObject> jsProductList = this.warehouseDB.getListProductInBill(request.getId());
                /**
                 * Check tồn kho
                 */
                for (JSONObject jsProduct : jsProductList) {
                    int product_id = ConvertUtils.toInt(jsProduct.get("product_id"));
                    int product_quantity = ConvertUtils.toInt(jsProduct.get("product_quantity"));

                    int quantity_availability = this.getQuantityAvailability(this.warehouseDB.getWarehouseInfoByProduct(product_id));
                    if (quantity_availability < product_quantity) {
                        ClientResponse crTonKho = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_QUANTITY_AVAILABILY_INVALID);
                        crTonKho.setMessage("[" + crTonKho.getMessage() + "] Sản phẩm " + this.dataManager.getProductManager().getProductFullName(product_id) + " chỉ còn " + quantity_availability);
                        return crTonKho;
                    }
                }
            }

            warehouseBillEntity.setModified_date(DateTimeUtils.getNow());
            warehouseBillEntity.setModifier_id(sessionData.getId());
            warehouseBillEntity.setStatus(WarehouseBillStatus.WAITING.getId());
            boolean rsUpdate = this.warehouseDB.updateWarehouseBill(warehouseBillEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse requireApproveWarehouseBillImport(SessionData sessionData, RequireApproveWarehouseBillRequest request) {
        try {
            WarehouseBillEntity warehouseBillEntity = this.warehouseDB.getWarehouseBill(request.getId());
            if (warehouseBillEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (WarehouseBillStatus.DRAFT.getId() != warehouseBillEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            warehouseBillEntity.setModified_date(DateTimeUtils.getNow());
            warehouseBillEntity.setModifier_id(sessionData.getId());
            warehouseBillEntity.setStatus(WarehouseBillStatus.WAITING.getId());
            boolean rsUpdate = this.warehouseDB.updateWarehouseBill(warehouseBillEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getListOrderWaitingApprove(SessionData sessionData, GetListOrderWaitingShipRequest request) {
        try {
            List<JSONObject> orders = this.orderDB.getListOrderWaitingApprove(request.getProduct_id());
            List<JSONObject> records = new ArrayList<>();
            for (JSONObject jsonObject : orders) {
                long total_end_price = ConvertUtils.toLong(jsonObject.get("total_end_price"));

                jsonObject.put("total_quantity",
                        ConvertUtils.toInt(jsonObject.get("total_final")));
                jsonObject.put("total_end_price", total_end_price);

                JSONObject jsOrder = this.orderDB.getAgencyOrder(ConvertUtils.toInt(jsonObject.get("agency_order_id")));
                if (jsOrder == null) {
                    continue;
                }
                if (!this.dataManager.getStaffManager().checkManageOrder(
                        sessionData.getId(),
                        this.agencyDB.getAgencyInfoById(ConvertUtils.toInt(jsOrder.get("agency_id"))),
                        ConvertUtils.toInt(jsOrder.get("status")))) {
                    continue;
                }
                records.add(jsonObject);
            }
            JSONObject data = new JSONObject();
            data.put("orders", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse cancelWarehouseBillImport(SessionData sessionData, CancelWarehouseBillRequest request) {
        try {
            WarehouseBillEntity warehouseBillEntity = this.warehouseDB.getWarehouseBill(request.getId());
            if (warehouseBillEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (WarehouseBillStatus.DRAFT.getId() != warehouseBillEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            warehouseBillEntity.setModifier_id(sessionData.getId());
            warehouseBillEntity.setModified_date(DateTimeUtils.getNow());
            warehouseBillEntity.setReason(request.getNote());
            warehouseBillEntity.setStatus(WarehouseBillStatus.CANCEL.getId());
            boolean rsUpdate = this.warehouseDB.updateWarehouseBill(warehouseBillEntity);
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getWarehouseBillInfo(SessionData sessionData, BasicRequest request) {
        try {
            WarehouseBillEntity warehouseBillEntity = this.warehouseDB.getWarehouseBill(request.getId());
            if (warehouseBillEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.WAREHOUSE_EMPTY);
            }

            JSONObject data = new JSONObject();
            data.put("warehouse_bill", warehouseBillEntity);
            data.put("to_info", this.dataManager.getWarehouseManager().getWarehouse(
                    warehouseBillEntity.getWarehouse_id())
            );

            if (WarehouseBillType.IMPORT.getValue() == warehouseBillEntity.getWarehouse_bill_type_id()) {
                if (warehouseBillEntity.getTarget_warehouse_id() != null) {
                    data.put("from_info", this.dataManager.getWarehouseManager().getWarehouse(
                            warehouseBillEntity.getTarget_warehouse_id())
                    );
                } else {
                    data.put("from_info", null);
                }
            } else {
                data.put("from_info", this.dataManager.getWarehouseManager().getWarehouse(
                        warehouseBillEntity.getWarehouse_id())
                );

                if (WarehouseExportBillType.XUAT_BAN.getValue() == warehouseBillEntity.getWarehouse_export_bill_type_id()) {
                    data.put("to_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                            warehouseBillEntity.getAgency_id())
                    );
                } else {
                    data.put("to_info", warehouseBillEntity.getTarget_warehouse_id() == null ? null
                            : this.dataManager.getWarehouseManager().getWarehouse(
                            warehouseBillEntity.getTarget_warehouse_id()));
                }
            }


            data.put("creator_info", this.dataManager.getStaffManager().getStaff(
                    warehouseBillEntity.getCreator_id())
            );

            if (warehouseBillEntity.getConfirmer_id() != null) {
                data.put("confirmer_info", this.dataManager.getStaffManager().getStaff(
                        warehouseBillEntity.getConfirmer_id())
                );
            }
            List<JSONObject> productList = new ArrayList<>();
            List<JSONObject> jsProductList = this.warehouseDB.getListProductInBill(request.getId());
            for (JSONObject jsProduct : jsProductList) {
                JSONObject product = JsonUtils.DeSerialize(JsonUtils.Serialize(this.dataManager.getProductManager().getProductBasicData(ConvertUtils.toInt(jsProduct.get("product_id")))), JSONObject.class);
                product.put("product_quantity", jsProduct.get("product_quantity"));
                product.put("note", jsProduct.get("note"));
                productList.add(product);
            }
            data.put("products", productList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse importQuantityStartToday(MultipartFile request) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(
                    request.getInputStream());
            XSSFSheet sheet = workbook.getSheetAt(1);
            Iterator<Row> rowIterator = sheet.iterator();
            short index = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (index > 3) {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    List<Cell> ltCell = new ArrayList<>();
                    cellIterator.forEachRemaining(ltCell::add);
                    if (!ltCell.isEmpty()) {
                        Cell cell = ltCell.get(0);
                        int product_id = getNumberData(cell);
                        cell = ltCell.get(4);
                        int quantity = getNumberData(cell);
                        boolean rsUpdate = this.warehouseDB.updateWarehouseStartToday(
                                product_id,
                                quantity
                        );
                    }
                }
                index++;
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse importWarehouseBillByBravo(CreateWarehouseBillRequest request) {
        try {

        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}