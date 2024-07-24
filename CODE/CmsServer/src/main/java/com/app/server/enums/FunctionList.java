package com.app.server.enums;

import com.app.server.constants.DeptConstants;
import com.app.server.data.dto.program.SalePolicy;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
public enum FunctionList {
    NONE(
            "",
            "",
            "",
            "",
            "",
            ""),
    LIST_AGENCY(
            "agency/filter_agency",
            "SELECT t_agency.*," +
                    "t_agency.current_point as acoin," +
                    "t1.current_dept," +
                    "t1.nqh," +
                    "t1.dept_cycle," +
                    "t1.total_tt_cycle," +
                    "t1.total_dtt_cycle," +
                    "t1.dept_limit," +
                    "t1.ngd_limit" +
                    " FROM agency t_agency" +
                    " LEFT JOIN (" +
                    " SELECT current_dept," +
                    " nqh," +
                    " dept_cycle," +
                    " total_tt_cycle," +
                    " total_dtt_cycle," +
                    " dept_limit," +
                    " ngd_limit," +
                    " agency_id" +
                    " FROM dept_agency_info" +
                    ") as t1 ON t1.agency_id=t_agency.id" +
                    " LEFT JOIN agency_status t2 ON t2.id = t_agency.status" +
                    " WHERE t_agency.status != -1",
            "[]",
            "[]",
            "t_agency.full_name,t_agency.shop_name,t_agency.code,t_agency.phone",
            "t2.priority ASC,t_agency.approved_date DESC,t_agency.id DESC"),
    EXPORT_AGENCY(
            "agency/filter_agency",
            "SELECT t_agency.*," +
                    "t_agency.current_point as acoin," +
                    "t1.current_dept," +
                    "t1.nqh," +
                    "t1.dept_cycle," +
                    "t1.total_tt_cycle," +
                    "t1.total_dtt_cycle," +
                    "t1.dept_limit," +
                    "t1.ngd_limit" +
                    " FROM agency t_agency" +
                    " LEFT JOIN (" +
                    " SELECT current_dept," +
                    " nqh," +
                    " dept_cycle," +
                    " total_tt_cycle," +
                    " total_dtt_cycle," +
                    " dept_limit," +
                    " ngd_limit," +
                    " agency_id" +
                    " FROM dept_agency_info" +
                    ") as t1 ON t1.agency_id=t_agency.id" +
                    " WHERE t_agency.status != -1",
            "[]",
            "[]",
            "t_agency.full_name,t_agency.shop_name,t_agency.code,t_agency.phone",
            ""),
    LIST_MEMBERSHIP(
            "agency/filter_agency",
            "SELECT * FROM membership",
            "[]",
            "[]",
            "",
            ""),
    LIST_AGENCY_ACCOUNT(
            "agency/filter_agency_account",
            "SELECT * FROM agency_account",
            "[]",
            "[]",
            "",
            ""),
    LIST_CATEGORY(
            "product/filter_category",
            "SELECT * FROM category",
            "[]",
            "[]",
            "name",
            ""),
    LIST_PRODUCT(
            "product/filter_product",
            "SELECT t.*," +
                    " t2.pltth_id," +
                    " t3.plsp_id," +
                    " t4.mathang_id" +
                    " FROM product t" +
                    " LEFT JOIN (SELECT id, sort_data FROM product_group) AS t1 on t.product_group_id = t1.id" +
                    " LEFT JOIN (select id as pltth_id, parent_priority, priority, parent_id FROM category) as t2 ON t2.pltth_id = t.category_id" +
                    " LEFT JOIN (select id as plsp_id, parent_priority, priority, parent_id FROM category) as t3 ON t3.plsp_id = t2.parent_id" +
                    " LEFT JOIN (select id as mathang_id, parent_priority, priority, parent_id FROM category) as t4 ON t4.mathang_id = t3.parent_id" +
                    " WHERE t.status != -1",
            "[]",
            "[]",
            "t.code,t.full_name,t.short_name",
            "t4.priority,t3.priority,t2.priority,t1.sort_data ASC,t.sort_data ASC"),
    LIST_PRODUCT_GROUP(
            "product/filter_product",
            "SELECT t.*," +
                    " t2.pltth_id," +
                    " t3.plsp_id," +
                    " t4.mathang_id" +
                    " FROM product_group t" +
                    " LEFT JOIN (select id as pltth_id, parent_priority, priority, parent_id FROM category) as t2 ON t2.pltth_id = t.category_id" +
                    " LEFT JOIN (select id as plsp_id, parent_priority, priority, parent_id FROM category) as t3 ON t3.plsp_id = t2.parent_id" +
                    " LEFT JOIN (select id as mathang_id, parent_priority, priority, parent_id FROM category) as t4 ON t4.mathang_id = t3.parent_id",
            "[]",
            "[]",
            "t.name,t.code",
            "t4.priority,t3.priority,t2.priority,t.sort_data ASC"),
    LIST_BRAND(
            "product/filter_brand",
            "SELECT * FROM brand",
            "[]",
            "[]",
            "name",
            "id DESC"),
    SEARCH_CATEGORY(
            "utility/search_category", "SELECT * FROM category", "[]", "[]",
            "",
            ""),
    SEARCH_BRAND("utility/search_brand",
            "SELECT * FROM brand", "[]", "[]",
            "",
            ""),
    SEARCH_PRODUCT_GROUP(
            "utility/search_product_group",
            "SELECT * FROM product_group",
            "[]",
            "[]",
            "name",
            "name ASC"),
    SEARCH_PRODUCT_SMALL_UNIT("utility/search_product_small_unit",
            "SELECT * FROM product_small_unit", "[]", "[]",
            "",
            ""),
    SEARCH_PRODUCT_BIG_UNIT("utility/search_product_big_unit",
            "SELECT * FROM product_big_unit", "[]", "[]", "",
            ""),
    SEARCH_PRODUCT_COLOR("utility/search_product_color",
            "SELECT * FROM product_color", "[]", "[]", "",
            ""),
    SEARCH_PRODUCT("utility/search_product",
            "SELECT t.*," +
                    " t2.pltth_id," +
                    " t3.plsp_id," +
                    " t4.mathang_id" +
                    " FROM product t" +
                    " LEFT JOIN (SELECT id, sort_data FROM product_group) AS t1 on t.product_group_id = t1.id" +
                    " LEFT JOIN (select id as pltth_id, parent_priority, priority, parent_id FROM category) as t2 ON t2.pltth_id = t.category_id" +
                    " LEFT JOIN (select id as plsp_id, parent_priority, priority, parent_id FROM category) as t3 ON t3.plsp_id = t2.parent_id" +
                    " LEFT JOIN (select id as mathang_id, parent_priority, priority, parent_id FROM category) as t4 ON t4.mathang_id = t3.parent_id",
            "[]",
            "[]",
            "t.code,t.full_name,t.short_name",
            "t4.priority,t3.priority,t2.priority,t1.sort_data ASC,t.sort_data ASC"),
    SEARCH_AGENCY("utility/search_agency",
            "SELECT *" +
                    " FROM agency t_agency WHERE status != 0 and status != 4",
            "[]",
            "[]",
            "code,shop_name,phone",
            ""),
    SEARCH_AGENCY_ADDRESS_DELIVERY("utility/search_agency_address_delivery",
            "SELECT * FROM agency_address_delivery",
            "[]",
            "[]",
            "",
            ""),
    SEARCH_AGENCY_ADDRESS_EXPORT_BILLING("utility/search_agency_address_export_billing",
            "SELECT * FROM agency_address_export_billing", "[]", "[]", "",
            ""),
    LIST_PURCHASE_ORDER("order/filter_purchase_order",
            "SELECT t.*,t_agency.business_department_id " +
                    " FROM agency_order t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id,status from agency) as t_agency on t_agency.id=t.agency_id" +
                    " left join (SELECT id, priority FROM order_status) AS t2 ON t2.id = t.status",
            "[]",
            "[]",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t2.priority ASC,t.update_status_date DESC,t.id DESC"),
    LIST_DELIVERY_ORDER("order/filter_purchase_order",
            "SELECT t.*,t_agency.business_department_id " +
                    " FROM agency_order t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id,status from agency) as t_agency on t_agency.id=t.agency_id" +
                    " left join (SELECT id, priority FROM order_status) AS t2 ON t2.id = t.status" +
                    " WHERE t.status IN (0,1,5)",
            "[]",
            "[]",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t2.priority ASC,t.update_status_date DESC,t.id DESC"),
    SEARCH_TREE_PRODUCT("utility/search_tree_product",
            "SELECT product.id" +
                    ", product.full_name" +
                    ", product.code" +
                    ", product.images" +
                    ", product.price" +
                    ", product_group.id as product_group_id" +
                    ", product_group.code as product_group_code" +
                    ", product_group.name as product_group_name" +
                    ", product.item_type" +
                    " FROM product left join product_group on product.product_group_id=product_group.id",
            "[]",
            "[]",
            "product.code,product.full_name,product_group_name,product_group_name.code",
            "product_group.sort_data ASC, product.sort_data ASC"),
    LIST_PROMO("promo/filter_promo",
            "SELECT * FROM promo WHERE " +
                    "(promo_type = 'SALE_POLICY' OR promo_type = 'PROMOTION')" +
                    " AND status != -1",
            "[]",
            "[]",
            "promo.code,promo.name",
            "id DESC"),
    LIST_PROMO_WAITING("promo/filter_promo",
            "SELECT t.* FROM promo t" +
                    " WHERE (t.promo_type = 'SALE_POLICY'" +
                    " OR t.promo_type = 'PROMOTION'" +
                    " OR t.promo_type = 'CTSS') AND t.status IN (1,2)",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id DESC"),
    LIST_DEPT_TRANSACTION("dept/filter_dept_transaction",
            "SELECT t.* FROM dept_transaction as t" +
                    " LEFT JOIN ( select id, function_type FROM dept_transaction_sub_type) as t1 ON t1.id = t.dept_transaction_sub_type_id" +
                    " LEFT JOIN (SELECT id, shop_name, code, city_id, region_id, business_department_id, status FROM agency) as t_agency ON t_agency.id = t.agency_id",
            "[]",
            "[]",
            "t_agency.code,t_agency.shop_name,t.doc_no,t.code",
            "t.id DESC"),
    LIST_DEPT_ORDER("dept/filter_dept_order",
            "SELECT t.*, t_agency.business_department_id, t2.function_type" +
                    " FROM dept_order t" +
                    " left join (select id, code, shop_name, business_department_id,status FROM agency) as t_agency on t_agency.id = t.agency_id" +
                    " left join ( select id, function_type FROM dept_transaction_sub_type) as t2 ON t2.id = t.dept_transaction_sub_type_id",
            "[]",
            "[]",
            "t.code,t_agency.code,t_agency.shop_name,t.dept_type_data,t.doc_no",
            "t.id DESC"),
    LIST_DEPT_SETTING("dept/filter_dept_setting",
            "SELECT dept_setting.* FROM dept_setting" +
                    " LEFT JOIN (SELECT id,shop_name,code" +
                    " FROM agency) AS t1 ON t1.id = dept_setting.agency_id",
            "[]",
            "[]",
            "t1.code,t1.shop_name",
            "id DESC"),
    LIST_WAREHOUSE("dept/filter_warehouse",
            "SELECT * FROM warehouse", "" +
            "[]", "[]", "code,name", "id ASC"),

    LIST_WAREHOUSE_BILL_IMPORT("dept/filter_warehouse_bill_import",
            "SELECT * FROM warehouse_bill WHERE warehouse_bill_type_id=1",
            "[]",
            "[]",
            "code",
            "id DESC"),
    LIST_WAREHOUSE_BILL_EXPORT("dept/filter_warehouse_bill_export",
            "SELECT * FROM warehouse_bill WHERE warehouse_bill_type_id=2",
            "[]",
            "[]",
            "code",
            "id DESC"),
    SEARCH_WAREHOUSE("/utility/search_warehouse",
            "SELECT id,name,code FROM warehouse",
            "[]",
            "[]",
            "",
            "id DESC"),
    LIST_WAREHOUSE_EXPORT_HISTORY("/warehouse/filter_warehouse_export_history",
            "SELECT warehouse_bill_detail.*," +
                    "warehouse_bill.warehouse_id," +
                    "warehouse_bill.target_warehouse_id," +
                    "warehouse_bill.agency_id," +
                    "warehouse_bill.code," +
                    "warehouse_bill.order_code," +
                    "warehouse_bill.warehouse_export_bill_type_id," +
                    "warehouse_bill.confirmed_date," +
                    "warehouse_bill.creator_id" +
                    " FROM warehouse_bill_detail" +
                    " LEFT JOIN warehouse_bill ON warehouse_bill.id=warehouse_bill_detail.warehouse_bill_id" +
                    " LEFT JOIN (SELECT id, item_type, code as product_code,full_name FROM product) as t2 ON t2.id = warehouse_bill_detail.product_id" +
                    " WHERE warehouse_bill.warehouse_bill_type_id = 2 AND status = 3",
            "[]",
            "[]",
            "warehouse_bill.code,t2.product_code,t2.full_name",
            "warehouse_bill_detail.id DESC"),
    LIST_WAREHOUSE_IMPORT_HISTORY("/warehouse/filter_warehouse_export_history",
            "SELECT warehouse_bill_detail.*," +
                    "warehouse_bill.warehouse_id," +
                    "warehouse_bill.target_warehouse_id," +
                    "warehouse_bill.agency_id," +
                    "warehouse_bill.code," +
                    "warehouse_bill.warehouse_export_bill_type_id," +
                    "warehouse_bill.confirmed_date," +
                    "warehouse_bill.creator_id" +
                    " FROM warehouse_bill_detail" +
                    " LEFT JOIN warehouse_bill ON warehouse_bill.id = warehouse_bill_detail.warehouse_bill_id" +
                    " LEFT JOIN (SELECT id, item_type, code as product_code,full_name FROM product) as t2 ON t2.id = warehouse_bill_detail.product_id" +
                    " WHERE warehouse_bill.warehouse_bill_type_id = 1 AND status = 3",
            "[]",
            "[]",
            "warehouse_bill.code,t2.product_code,t2.full_name",
            "warehouse_bill_detail.id DESC"),
    LIST_WAREHOUSE_REPORT(
            "/warehouse/filter_warehouse_report",
            "SELECT warehouse_info.*," +
                    " quantity_start_today + quantity_import_today - quantity_export_today as quantity_reality," +
                    " quantity_start_today + quantity_import_today - quantity_export_today - quantity_waiting_ship_today - quantity_waiting_approve_today as quantity_availability," +
                    " t2.item_type, t2.product_code, t2.full_name, t2.product_status," +
                    " (CASE WHEN t2.product_status = 1 THEN 'Hiện' ELSE 'Ẩn' END) as product_status_name," +
                    " t3.name as don_vi_tinh" +
                    " FROM warehouse_info" +
                    " LEFT JOIN (SELECT id, item_type, code as product_code,full_name,status as product_status,product_small_unit_id FROM product) as t2 ON t2.id = warehouse_info.product_id" +
                    " LEFT JOIN product_small_unit t3 ON t3.id = t2.product_small_unit_id" +
                    "",
            "search," +
                    "confirmed_date," +
                    "item_type," +
                    "product_status," +
                    "quantity_start_today," +
                    "quantity_import_today," +
                    "quantity_export_today," +
                    "quantity_waiting_ship_today," +
                    "quantity_waiting_approve_today," +
                    "quantity_availability," +
                    "quantity_reality",
            "[]",
            "t2.product_code,t2.full_name",
            ""),
    SEARCH_STAFF("/utility/search_staff",
            "SELECT * FROM staff",
            "[]",
            "[]",
            "",
            "id DESC"),
    LIST_DEPT_AGENCY_HISTORY("/dept/filter_dept_agency_history",
            "SELECT t.* FROM dept_agency_history t" +
                    " LEFT JOIN (SELECT id,shop_name,code" +
                    " FROM agency) AS t1 ON t1.id = t.agency_id",
            "",
            "",
            "t1.code,t1.shop_name",
            "t.id DESC"
    ),
    LIST_AGENCY_MEMBERSHIP_HISTORY("/agency/filter_agency_membership_history",
            "SELECT t.* FROM agency_membership_history t" +
                    " LEFT JOIN (SELECT id,shop_name,code" +
                    " FROM agency) AS t1 ON t1.id = t.agency_id",
            "",
            "",
            "t1.code,t1.shop_name",
            "t.id DESC"
    ),
    LIST_DEPT_ORDER_BY_AGENCY("/dept/get_list_order_by_agency",
            "SELECT * FROM dept_order",
            "",
            "",
            "",
            "id DESC"
    ),
    LIST_ORDER_TEMP("/order/filter_order_temp",
            "SELECT agency_order_tmp.*,t1.business_department_id " +
                    " FROM agency_order_tmp LEFT JOIN (select id, shop_name, code AS agency_code, phone, business_department_id from agency) as t1 on t1.id=agency_order_tmp.agency_id",
            "[]",
            "[]",
            "t1.shop_name,t1.agency_code,t1.phone,note",
            "agency_order_tmp.id DESC"),
    LIST_PROMO_CTKM("promo/filter_promo_ctkm",
            "SELECT * FROM promo WHERE status IN (" + PromoActiveStatus.canSort() + ")" +
                    " AND promo_type = '" + PromoType.PROMO.getKey() + "'",
            "[]",
            "[]",
            "promo.code,promo.name",
            "priority ASC"),
    LIST_AGENCY_ACOIN_HISTORY("/agency/filter_agency_acoin_history",
            "SELECT * FROM agency_acoin_history",
            "",
            "",
            "",
            "id DESC"
    ),
    LIST_PRODUCT_VISIBILITY_SETTING("/product/filter_product_visibility_setting",
            "SELECT t.* FROM product_visibility_setting AS t" +
                    " LEFT JOIN (select id, shop_name, code FROM agency) as t1 ON t1.id = t.agency_id",
            "",
            "",
            "t.name,t1.shop_name,t1.code",
            "t.id DESC"
    ),
    LIST_PRODUCT_VISIBILITY_SETTING_DETAIL("/product/filter_product_visibility_setting_detail",
            "SELECT t.* FROM product_visibility_setting_detail as t" +
                    " LEFT JOIN (SELECT id, full_name, code FROM product) as t1 ON t1.id = t.product_id" +
                    " LEFT JOIN (SELECT id, name FROM category) as t2 ON t2.id = t.category_level_2_id" +
                    " LEFT JOIN (SELECT id, name FROM category) as t3 ON t3.id = t.category_level_3_id" +
                    " LEFT JOIN (SELECT id, name FROM category) as t4 ON t4.id = t.category_level_4_id" +
                    " LEFT JOIN (SELECT id, name FROM brand) as t5 ON t5.id = t.brand_id" +
                    " LEFT JOIN (SELECT id, name FROM product_group) as t6 ON t6.id = t.product_group_id",

            "",
            "",
            "t1.code,t1.full_name,t2.name,t3.name,t4.name,t5.name,t6.name",
            "t.id ASC"
    ),
    LIST_PRODUCT_VISIBILITY_SETTING_DETAIL_HISTORY(
            "/product/filter_product_visibility_setting_detail_history",
            "SELECT t.* FROM product_visibility_setting_detail_history as t" +
                    " LEFT JOIN (SELECT id, full_name, code FROM product) as t1 ON t1.id = t.product_id" +
                    " LEFT JOIN (SELECT id, name FROM category) as t2 ON t2.id = t.category_level_2_id" +
                    " LEFT JOIN (SELECT id, name FROM category) as t3 ON t3.id = t.category_level_3_id" +
                    " LEFT JOIN (SELECT id, name FROM category) as t4 ON t4.id = t.category_level_4_id" +
                    " LEFT JOIN (SELECT id, name FROM brand) as t5 ON t5.id = t.brand_id" +
                    " LEFT JOIN (SELECT id, name FROM product_group) as t6 ON t6.id = t.product_group_id",

            "",
            "",
            "t1.code,t1.full_name,t2.name,t3.name,t4.name,t5.name,t6.name",
            "t.id DESC"
    ),
    LIST_PRODUCT_PRICE_SETTING(
            "/product/filter_product_price_setting",
            "SELECT t.* FROM product_price_setting t" +
                    " LEFT JOIN (select id, shop_name, code, business_department_id, status from agency) as t_agency ON t_agency.id = t.agency_id" +
                    " LEFT JOIN (select id, name from city) as t2 ON t2.id = t.city_id" +
                    " LEFT JOIN (select id, name from region) as t3 ON t3.id = t.region_id" +
                    " LEFT JOIN (select id, name from membership) as t4 ON t4.id = t.membership_id",
            "[]",
            "[]",
            "t.name,t_agency.shop_name,t_agency.code,t2.name,t3.name,t4.name",
            "t.id DESC"),
    LIST_PRODUCT_PRICE_TIMER(
            "/product/filter_product_price_setting",
            "SELECT t.* FROM product_price_timer t",
            "[]",
            "[]",
            "t.name,t.note",
            "t.id DESC"),
    LIST_PRODUCT_PRICE_SETTING_DETAIL(
            "/product/filter_product_price_setting_detail",
            "SELECT t.* FROM product_price_setting_detail as t" +
                    " LEFT JOIN (SELECT id, full_name, code FROM product) as t1 ON t1.id = t.product_id",
            "[]",
            "[]",
            "t1.full_name,t1.code",
            "t.id ASC"),
    LIST_PRODUCT_PRICE_TIMER_DETAIL(
            "/product/filter_product_price_timer_detail",
            "SELECT t.* FROM product_price_timer_detail as t" +
                    " LEFT JOIN (SELECT id, full_name, code FROM product) as t1 ON t1.id = t.product_id",
            "[]",
            "[]",
            "t1.full_name,t1.code",
            "t.id ASC"),
    LIST_PRODUCT_HOT(
            "/product/filter_product_hot",
            "SELECT t.id," +
                    "t.full_name," +
                    "t.code," +
                    "t.images," +
                    "t.hot_label," +
                    "t.hot_priority," +
                    "t.hot_date," +
                    "t.hot_modifier_id" +
                    " FROM product as t WHERE hot_priority != 0",
            "[]",
            "[]",
            "t.full_name,t.code",
            "t.hot_priority ASC"),
    LIST_PRODUCT_HOT_COMMON(
            "/product/filter_product_hot",
            "SELECT t.id," +
                    "t.full_name," +
                    "t.code," +
                    "t.images," +
                    "t.hot_label," +
                    "t.hot_date," +
                    "t.hot_modifier_id" +
                    " FROM product as t" +
                    " JOIN product_hot_common t1 ON t1.product_id = t.id",
            "[]",
            "[]",
            "t.full_name,t.code",
            "t1.total_sell_quantity"),
    LIST_SETTING_PRICE("",
            "SELECT t1.* FROM product_price_setting_detail as t" +
                    " LEFT JOIN product_price_setting t1 ON t1.id = t.product_price_setting_id" +
                    " LEFT JOIN (select id, shop_name, code FROM agency) as t2 ON t2.id = t1.agency_id",
            "[]", "[]",
            "t2.code,t2.shop_name",
            "t1.id DESC"),
    LIST_PRODUCT_PRICE_SETTING_DETAIL_HISTORY(
            "",
            "SELECT t.*, t1.creator_id" +
                    " FROM product_price_setting_timer_detail t" +
                    " JOIN product_price_setting_timer t1 ON t1.id = t.product_price_setting_timer_id",
            "[]",
            "[]",
            "t1.name",
            "t.id DESC"),
    SEARCH_PROMOTION(
            "search_promotion",
            "SELECT t.id, t.code, t.name" +
                    " FROM promo t WHERE t.status IN (1,2)",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id ASC"),
    FILTER_NOTIFY_SETTING(
            "filter_notify_setting",
            "SELECT t.*" +
                    " FROM notify_setting t WHERE t.status != -1",
            "[]",
            "[]",
            "t.name",
            "CASE status " +
                    "    WHEN 1 THEN 1 " +
                    "    WHEN 2 THEN 1 " +
                    "    WHEN 3 THEN 2 " +
                    "    ELSE 3 " +
                    "END ASC, t.id DESC"),
    FILTER_BANNER(
            "filter_banner",
            "SELECT t.* FROM banner t WHERE t.status != -1",
            "[]",
            "[]",
            "t.name",
            "CASE t.status " +
                    "    WHEN 1 THEN 1 " +
                    "    WHEN 2 THEN 1 " +
                    "    WHEN 3 THEN 2 " +
                    "    ELSE 3 END ASC, t.id DESC"),
    FILTER_NOTIFY_AUTO_CONFIG("filter_notify_auto_config",
            "SELECT t.*" +
                    " FROM notify_auto_config t",
            "[]",
            "[]",
            "t.name",
            "t.id DESC"),
    SEARCH_BANNER("SEARCH_BANNER",
            "SELECT t.*" +
                    " FROM banner t WHERE t.status != 3",
            "[]",
            "[]",
            "t.name",
            "t.id DESC"),
    FILTER_BANNER_PRIORITY("FILTER_BANNER_PRIORITY",
            "SELECT t.*" +
                    " FROM banner t WHERE t.priority != 0",
            "[]",
            "[]",
            "t.name",
            "t.priority ASC"),
    LIST_STAFF(
            "get_list_staff",
            "SELECT t.id," +
                    "t.full_name," +
                    "t.address," +
                    "t.phone," +
                    "t.username," +
                    "t.email," +
                    "t.code," +
                    "t.created_date," +
                    "t.creator_id," +
                    "t.status," +
                    "t.staff_group_permission_id" +
                    " FROM staff t WHERE t.is_account_system = 0",
            "[]", "[]",
            "t.full_name,t.code,t.phone",
            "t.id DESC"),
    FILER_GROUP_PERMISSION(
            "filer_group_permission",
            "SELECT t.* FROM staff_group_permission t",
            "[]",
            "[]",
            "t.name",
            "t.id DESC"),
    FILER_TASK(
            "FILER_TASK",
            "SELECT t.* FROM task t",
            "[]",
            "[]",
            "t.name",
            "t.id DESC"),
    LIST_DEPT_DTT_HISTORY("/dept/filter_dept_dtt_history",
            "SELECT t.* FROM dept_dtt_history t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id=t.agency_id",
            "",
            "",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t.created_date DESC,t.id DESC"
    ),
    LIST_DEAL_PRICE("",
            "SELECT t.*," +
                    " (case when t.status = 1 then 1" +
                    " when t.status = 2 then 1" +
                    " when t.status = 3 then 2" +
                    " when t.status = 4 then 3" +
                    " end) as `sort_data`" +
                    " FROM agency_deal_price t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id,status from agency) as t_agency on t_agency.id=t.agency_id" +
                    " left join (select id, full_name, code AS product_code FROM product) as t1 ON t1.id = t.product_id" +
                    " WHERE t.hide = 0",
            "[]", "[]",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone,t.product_full_name,t1.full_name",
            "sort_data ASC,t.update_status_date DESC"),
    FILTER_AGENCY_ACCESS_APP_REPORT("",
            "SELECT t.*" +
                    " FROM (SELECT agency_id, DATE(created_date) as created_date, count(*) as quantity" +
                    " FROM agency_access_app_log GROUP BY agency_id, DATE(created_date)" +
                    " ORDER BY created_date desc, agency_id asc) as t",
            "[]", "[]",
            "",
            "created_date DESC,agency_id asc,quantity desc"),
    FILTER_NOTIFY_CMS_HISTORY("",
            "SELECT t.agency_id,t.quantity, t.date_time as created_date" +
                    " FROM (SELECT agency_id, DATE(created_date) as date_time, count(*) as quantity" +
                    " FROM agency_access_app_log" +
                    "GROUP BY agency_id, date_time" +
                    ") as t",
            "[]", "[]",
            "t.name,t.description",
            "t.created_date DESC"),
    LIST_PRODUCT_HOT_TYPE("",
            "SELECT t.*" +
                    " FROM product_hot_type t",
            "[]", "[]",
            "t.name,t.code",
            "t.id DESC"),
    LIST_COMBO(
            "filter_combo",
            "SELECT *" +
                    " FROM combo t" +
                    " WHERE t.status != -1",
            "[]",
            "[]",
            "t.full_name,t.code",
            "t.created_date DESC"),
    LIST_PROMO_HUNT_SALE("promo/filter_promo_hunt_sale",
            "SELECT * FROM promo t WHERE promo_type = '" + PromoType.CTSS.getKey() + "'" +
                    " AND status != -1",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id DESC"),
    SEARCH_PRODUCT_HUNT_SALE("promo/search_product_hunt_sale",
            "SELECT * FROM promo_hunt_sale t WHERE status != -1",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id DESC"),
    LIST_PROMO_CTSS_SORT("promo/filter_promo_ctts_sort",
            "SELECT * FROM promo WHERE status IN (" + PromoActiveStatus.canSort() + ")" +
                    " AND promo_type = '" + PromoType.CTSS.getKey() + "'",
            "[]",
            "[]",
            "promo.code,promo.name",
            "priority ASC"),
    SEARCH_COMBO("promo/search_combo",
            "SELECT * FROM combo t WHERE status != -1",
            "[]",
            "[]",
            "t.code,t.full_name",
            "t.id DESC"),
    LIST_ORDER_APPOINTMENT("order/filter_order_appointment",
            "SELECT t.*,t_agency.business_department_id " +
                    " FROM agency_order t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id=t.agency_id" +
                    " left join (SELECT id, priority FROM order_status) AS t2 ON t2.id = t.status" +
                    " WHERE t.is_order_appointment = " + OrderType.APPOINTMENT.getValue(),
            "[]",
            "[]",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t2.priority ASC,t.update_status_date DESC,t.id DESC"),
    LIST_ORDER_DELIVERY_BILL("order/filter_order_delivery_bill",
            "SELECT t.*,t_agency.business_department_id " +
                    " FROM agency_order_delivery t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id,status from agency) as t_agency on t_agency.id=t.agency_id" +
                    " left join (SELECT id, priority FROM order_status) AS t2 ON t2.id = t.status",
            "[]",
            "[]",
            "t.code,t.agency_order_code,t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t2.priority ASC,t.update_status_date DESC,t.id DESC"),
    LIST_ORDER_CONTRACT("order/filter_order_contract",
            "SELECT t.*,t_agency.business_department_id " +
                    " FROM agency_order t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id=t.agency_id" +
                    " left join (SELECT id, priority FROM order_status) AS t2 ON t2.id = t.status" +
                    " WHERE t.is_order_appointment = " + OrderType.CONTRACT.getValue(),
            "[]",
            "[]",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t2.priority ASC,t.update_status_date DESC,t.id DESC"),

    LIST_SYNC_HISTORY("",
            "SELECT t.*" +
                    " FROM sync_log t",
            "[]", "[]",
            "t.name,t.code,type,t.request_data,t.response_data",
            "t.id DESC"),
    LIST_PRODUCT_PRICE_SETTING_TIMER(
            "/product/filter_product_price_setting_timer",
            "SELECT t.* FROM product_price_setting_timer t" +
                    " LEFT JOIN (select id, shop_name, code, business_department_id, status from agency) as t_agency ON t_agency.id = t.agency_id" +
                    " LEFT JOIN (select id, name from city) as t2 ON t2.id = t.city_id" +
                    " LEFT JOIN (select id, name from region) as t3 ON t3.id = t.region_id" +
                    " LEFT JOIN (select id, name from membership) as t4 ON t4.id = t.membership_id",
            "[]",
            "[]",
            "t.name,t_agency.shop_name,t_agency.code,t2.name,t3.name,t4.name",
            "t.id DESC"),
    LIST_PRODUCT_PRICE_SETTING_TIMER_DETAIL(
            "/product/filter_product_price_setting_timer_detail",
            "SELECT t.* FROM product_price_setting_timer_detail as t" +
                    " LEFT JOIN (SELECT id, full_name, code FROM product) as t1 ON t1.id = t.product_id",
            "[]",
            "[]",
            "t1.full_name,t1.code",
            "t.id ASC"),
    REPORT_PRODUCT_PRICE_TIMER_DETAIL(
            "/product/filter_product_price_timer_detail",
            "SELECT t.*," +
                    " t1.full_name," +
                    " t1.code," +
                    " t1.product_small_unit_id" +
                    " FROM product_price_timer_detail as t" +
                    " LEFT JOIN (SELECT id, full_name, code, product_small_unit_id FROM product) as t1 ON t1.id = t.product_id",
            "[]",
            "[]",
            "t1.full_name,t1.code",
            "t.id ASC"),
    REPORT_ORDER("order/filter_purchase_order",
            "SELECT t.id," +
                    "t.code," +
                    "t.created_date," +
                    "t.total_end_price," +
                    "t_agency.agency_code," +
                    "t_agency.shop_name as agency_shop_name," +
                    "t_agency.business_department_id," +
                    "t.source," +
                    "t.creator_id," +
                    "t2.name as status_name" +
                    " FROM agency_order t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id,status from agency) as t_agency on t_agency.id=t.agency_id" +
                    " left join (SELECT id, priority, name FROM order_status) AS t2 ON t2.id = t.status",
            "[]",
            "[]",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t2.priority ASC,t.update_status_date DESC,t.id DESC"),
    LIST_PROMO_CTTL("promo/filter_promo_hunt_sale",
            "SELECT * FROM promo t WHERE promo_type = '" + PromoType.CTTL.getKey() + "'" +
                    " AND status != -1",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id DESC"),
    FILTER_PROMO("utility/filter_promo",
            "SELECT id,code,name,promo_type,start_date,end_date,status" +
                    " FROM promo WHERE status != -1",
            "[]",
            "[]",
            "promo.code,promo.name",
            "id DESC"),
    LIST_ORDER_CHILD_CTTL("order/filter_purchase_order",
            "SELECT t.*,t1.code,t1.total," +
                    "t1.created_date as order_created_date," +
                    "t1.update_status_date as order_confirm_date" +
                    " FROM agency_order_dept t" +
                    " left join agency_order t1 on t1.id = t.agency_order_id" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id=t1.agency_id" +
                    " WHERE t1.status IN (" +
                    OrderStatus.SHIPPING.getKey() + "," +
                    OrderStatus.COMPLETE.getKey() + ")",
            "[]",
            "[]",
            "t1.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t.id DESC"),
    LIST_TANG_CONG_NO_CTTL("order/filter_purchase_order",
            "SELECT t.*" +
                    " FROM dept_transaction t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id=t.agency_id" +
                    " WHERE t.status = 2 AND t.dept_type_id = 2",
            "[]",
            "[]",
            "t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t.id DESC"),
    LIST_GIAM_CONG_NO_CTTL("order/filter_purchase_order",
            "SELECT t.*" +
                    " FROM dept_transaction t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id=t.agency_id" +
                    " WHERE t.status = 2 AND t.dept_type_id = 3 and dept_transaction_sub_type_id != " + DeptConstants.GHI_NHAN_TRA_HANG,
            "[]",
            "[]",
            "t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t.id DESC"),
    LIST_DIEU_CHINH_CTTL("order/filter_purchase_order",
            "SELECT t.*" +
                    " FROM agency_dept_dtt t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id=t.agency_id",
            "[]",
            "[]",
            "t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t.id DESC"),
    LIST_HBTL_CTTL("order/filter_purchase_order",
            "SELECT t.*" +
                    " FROM agency_hbtl t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id=t.agency_id",
            "[]",
            "[]",
            "t_agency.agency_code,t_agency.shop_name,t_agency.phone,t.code,t.doc_no",
            "t.id DESC"),
    FILTER_RESULT_CTTL("utility/filter_promo",
            "SELECT id,code,name,promo_type,start_date,end_date,status,condition_type,order_date_data,payment_date_data" +
                    " FROM promo" +
                    " WHERE promo_type = " + "'" + PromoType.CTTL.getKey() + "'" +
                    " AND status IN (2,3,5,6)",
            "[]",
            "[]",
            "promo.code,promo.name",
            "id DESC"),
    FILTER_TRANSACTION_CTTL("utility/filter_promo",
            "SELECT *" +
                    " FROM agency_transaction_cttl t" +
                    " WHERE status != -1",
            "[]",
            "[]",
            "",
            "id DESC"),
    FILTER_AGENCY_JOIN_CTTL("utility/filter_promo",
            "SELECT t.*," +
                    "t_agency.code," +
                    "t_agency.shop_name," +
                    "t_agency.avatar," +
                    "t_agency.business_department_id," +
                    "t_agency.membership_id" +
                    " FROM agency_cttl_info t" +
                    " left join (select id, shop_name, avatar, code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id = t.agency_id",
            "[]",
            "[]",
            "t_agency.code,t_agency.shop_name",
            "t.id DESC"),
    FILTER_RESULT_CTTL_ACGENCY("FILTER_RESULT_CTTL_ACGENCY",
            "SELECT t.id,t.agency_id," +
                    "t_agency.code," +
                    "t_agency.shop_name," +
                    "t_agency.avatar," +
                    "t_agency.business_department_id," +
                    "t_agency.membership_id" +
                    " FROM agency_cttl_info t" +
                    " left join (select id, shop_name, avatar, code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id = t.agency_id",
            "[]",
            "[]",
            "t_agency.code,t_agency.shop_name",
            "t.id DESC"),
    FILTER_HBTL("order/filter_hbtl",
            "SELECT t.*,t_agency.business_department_id " +
                    " FROM agency_hbtl t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id,status from agency) as t_agency on t_agency.id=t.agency_id",
            "[]",
            "[]",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone,t.doc_no",
            "t.id DESC"),
    REPORT_ORDER_TEMP("/order/filter_order_temp",
            "SELECT agency_order_tmp.*,t1.business_department_id,t1.agency_code,t1.shop_name as agency_shop_name" +
                    " FROM agency_order_tmp LEFT JOIN (select id, shop_name, code AS agency_code, phone, business_department_id, status from agency) as t1 on t1.id=agency_order_tmp.agency_id",
            "[]",
            "[]",
            "t1.shop_name,t1.agency_code,t1.phone,note",
            "agency_order_tmp.id DESC"),
    LIST_PRODUCT_NEW(
            "/product/filter_product_new",
            "SELECT t.*," +
                    "t1.full_name," +
                    "t1.code," +
                    "t1.images," +
                    "t1.hot_label" +
                    " FROM product_new t" +
                    " LEFT JOIN product t1 ON t1.id=t.product_id",
            "[]",
            "[]",
            "t1.code,t1.full_name,t1.short_name",
            "t.priority ASC"),
    REPORT_TRANSACTION_CTTL("utility/filter_promo",
            "SELECT *" +
                    " FROM agency_transaction_cttl t" +
                    " WHERE status != -1",
            "[]",
            "[]",
            "",
            "id DESC"),
    LIST_CTTL_WAITING("promo/filter_promo",
            "SELECT t.* FROM promo t" +
                    " WHERE t.promo_type = 'CTTL' AND t.status IN (2,3,5,6)",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id DESC"),
    LIST_AGENCY_MISS_COMMIT_HISTORY("/dept/filter_agency_miss_commit_history",
            "SELECT t.* FROM agency_miss_commit_history t" +
                    " left join (select id, shop_name, code, phone,city_id,region_id,membership_id,business_department_id from agency) as t1 on t1.id=t.agency_id",
            "",
            "",
            "t.note,t.link_code,t1.shop_name,t1.code",
            "t.id DESC"
    ),
    LIST_DEPT_COMMIT_SETTING("/dept/filter_agency_miss_commit_history",
            "SELECT t.* FROM dept_commit_setting t" +
                    " left join (select id, shop_name, code, phone,city_id,region_id,membership_id,business_department_id from agency) as t1 on t1.id=t.agency_id",
            "",
            "",
            "t.note,t1.shop_name,t1.code",
            "t.id DESC"),
    FILTER_AGENCY_LOCK_HISTORY("/dept/filter_agency_lock_history",
            "SELECT t.* FROM agency_lock_history t" +
                    " LEFT JOIN (select id, shop_name, code, phone,city_id,region_id,membership_id,business_department_id from agency) as t1 on t1.id=t.agency_id",
            "",
            "",
            "t1.shop_name,t1.code",
            "t.id DESC"
    ),
    FILTER_AGENCY_LOCK_DATA("/dept/filter_agency_lock_data",
            "SELECT * FROM agency_lock_data",
            "",
            "",
            "agency_lock_setting_code",
            "effect_date DESC,id DESC"
    ),
    FILTER_AGENCY_LOCK_SETTING("/dept/filter_agency_lock_setting",
            "SELECT * FROM agency_lock_setting",
            "",
            "",
            "code",
            "id DESC"
    ),
    LIST_PROMO_CTXH("LIST_PROMO_CTXH",
            "SELECT * FROM promo t WHERE promo_type = '" + PromoType.BXH.getKey() + "'" +
                    " AND status != -1",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id DESC"),
    LIST_VOUCHER_RELEASE_PERIOD("LIST_PROMO_CTXH",
            "SELECT * FROM voucher_release_period t WHERE t.offer_type != '" + VoucherOfferType.ACOIN.getKey() + "'",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id DESC"),
    LIST_VOUCHER("LIST_VOUCHER",
            "SELECT * FROM voucher t",
            "[]",
            "[]",
            "t.code",
            "t.id DESC"),
    LIST_VOUCHER_BY_VRP("LIST_VOUCHER_BY_VRP",
            "SELECT t.* FROM voucher t" +
                    " LEFT JOIN promo t1 ON t1.id = t.promo_id" +
                    " LEFT JOIN agency t2 ON t2.id = t.agency_id",
            "status:t.status,voucher_release_period_id:voucher_release_period_id,created_date:t.created_date",
            "[]",
            "t.code,t1.code,t2.code,t2.shop_name",
            "t.id DESC"),
    FILTER_RESULT_CTXH("utility/FILTER_RESULT_CTXH",
            "SELECT id,code,name,promo_type,start_date,end_date,status,condition_type,order_date_data,payment_date_data" +
                    " FROM promo" +
                    " WHERE promo_type = " + "'" + PromoType.BXH.getKey() + "'" +
                    " AND status IN (2,3,5,6)",
            "[]",
            "[]",
            "promo.code,promo.name",
            "id DESC"),
    SEARCH_VOUCHER_RELEASE_PERIOD("LIST_PROMO_CTXH",
            "SELECT * FROM voucher_release_period t WHERE t.status = " + VoucherReleasePeriodStatus.ACTIVATED.getId() +
                    " AND t.offer_type != '" + VoucherOfferType.ACOIN.getKey() + "'",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id DESC"),
    FILTER_AGENCY_JOIN_CTXH("utility/filter_promo",
            "SELECT t.*,t1.data,t1.rank_value,t1.rank_date" +
                    " FROM bxh_agency_join t" +
                    " LEFT JOIN agency_bxh_info t1 ON t.promo_id=t1.program_id AND t1.agency_id=t.agency_id" +
                    " LEFT JOIN agency t_agency ON t_agency.id = t.agency_id",
            "[]",
            "[]",
            "t_agency.code,t_agency.shop_name",
            "t1.rank_value DESC,t1.rank_date ASC,t.id ASC"),
    LIST_TANG_CONG_NO_CTXH("order/filter_purchase_order",
            "SELECT t.*" +
                    " FROM dept_transaction t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id=t.agency_id" +
                    " WHERE t.status = 2 AND t.dept_type_id = 2",
            "[]",
            "[]",
            "t_agency.agency_code,t_agency.shop_name,t_agency.phone,t.code,t.doc_no",
            "t.id DESC"),
    LIST_GIAM_CONG_NO_CTXH("order/filter_purchase_order",
            "SELECT t.*" +
                    " FROM dept_transaction t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id=t.agency_id" +
                    " WHERE t.status = 2 AND t.dept_type_id = 3 AND t.dept_transaction_sub_type_id != 11",
            "[]",
            "[]",
            "t_agency.agency_code,t_agency.shop_name,t_agency.phone,t.code,t.doc_no",
            "t.id DESC"),
    FILTER_PRODUCT_HISTORY("/dept/filter_product_history",
            "SELECT t.* FROM product_history t" +
                    " LEFT JOIN product t1 ON t1.id = t.product_id",
            "",
            "",
            "t1.code,t1.full_name",
            "t.id DESC"
    ),
    FILTER_CATALOG("/dept/filter_agency_lock_setting",
            "SELECT * FROM catalog",
            "",
            "",
            "name",
            "priority ASC,id DESC"),
    FILTER_AGENCY_CATALOG_DETAIL("",
            "SELECT t.id," +
                    "t.catalog_id," +
                    "t1.name," +
                    "t1.image," +
                    "t1.is_show," +
                    "t.status," +
                    "t2.note," +
                    "t.note as reason," +
                    "t2.code," +
                    "t.modified_date," +
                    "t.confirmer_id" +
                    " FROM agency_catalog_detail t" +
                    " LEFT JOIN catalog t1 ON t1.id = t.catalog_id" +
                    " LEFT JOIN agency_catalog_request t2 ON t2.id = t.agency_catalog_request_id" +
                    " WHERE t.status != " + AgencyCatalogDetailStatus.REJECT.getId(),
            "",
            "",
            "t1.name,t.note,t2.code",
            "t.status ASC, t.id DESC"),
    FILTER_CATALOG_REQUEST("/dept/filter_agency_lock_setting",
            "SELECT t.* FROM agency_catalog_request t" +
                    " left join (select id, shop_name, code, phone,city_id,region_id,membership_id,business_department_id,status from agency) as t_agency on t_agency.id=t.agency_id WHERE t.status != -1",
            "",
            "",
            "t.note,t_agency.shop_name,t_agency.code,t.code",
            "t.status ASC, t.modified_date DESC, t.id DESC"),
    FILTER_CATEGORY_CATALOG("/catalog/filter_category",
            "SELECT t.*, t1.catalog_id" +
                    " FROM category t" +
                    " LEFT JOIN catalog_category t1 ON t1.category_id=t.id" +
                    " WHERE t.category_level IN (2,3)",
            "",
            "",
            "name",
            "t.category_level ASC,t.priority ASC,t.id DESC"),
    FILTER_AGENCY_CATALOG_HISTORY("",
            "SELECT m.*" +
                    " FROM (SELECT t.id," +
                    "t.catalog_id," +
                    "t.agency_id," +
                    "t1.name," +
                    "t1.image," +
                    "t1.is_show," +
                    "t.status," +
                    "t2.note," +
                    "t.note as reason," +
                    "t2.code," +
                    "t_agency.code as agency_code," +
                    "t_agency.shop_name," +
                    "t.modified_date" +
                    " FROM agency_catalog_detail t" +
                    " LEFT JOIN catalog t1 ON t1.id = t.catalog_id" +
                    " LEFT JOIN agency_catalog_request t2 ON t2.id = t.agency_catalog_request_id" +
                    " LEFT JOIN (select id,shop_name,code FROM agency) as t_agency ON t_agency.id=t.agency_id) as m",
            "",
            "",
            "m.name,m.shop_name,m.code,m.agency_code,m.reason",
            "m.id DESC"),
    EXPORT_PRODUCT(
            "product/export_product",
            "SELECT t.*," +
                    " t2.pltth_id," +
                    " t2.pltth_name," +
                    " t3.plsp_id," +
                    " t4.mathang_id," +
                    " t1.product_group_name," +
                    " t1.product_group_code," +
                    " t5.*," +
                    " t6.*," +
                    " t7.*," +
                    " t8.*" +
                    " FROM product t" +
                    " LEFT JOIN (SELECT id, sort_data, name as product_group_name, code as product_group_code FROM product_group) AS t1 on t.product_group_id = t1.id" +
                    " LEFT JOIN (select id as pltth_id, name as pltth_name, parent_priority, priority, parent_id FROM category) as t2 ON t2.pltth_id = t.category_id" +
                    " LEFT JOIN (select id as plsp_id, parent_priority, priority, parent_id FROM category) as t3 ON t3.plsp_id = t2.parent_id" +
                    " LEFT JOIN (select id as mathang_id, parent_priority, priority, parent_id FROM category) as t4 ON t4.mathang_id = t3.parent_id" +
                    " LEFT JOIN (select id as id_dac_diem_2, name as ten_dac_diem_2 FROM product_color) as t5 ON t5.id_dac_diem_2 = t.product_color_id" +
                    " LEFT JOIN (select id as id_don_vi_nho, name as ten_don_vi_nho FROM product_small_unit) as t6 ON t6.id_don_vi_nho = t.product_small_unit_id" +
                    " LEFT JOIN (select id as id_don_vi_lon, name as ten_don_vi_lon FROM product_big_unit) as t7 ON t7.id_don_vi_lon = t.product_big_unit_id" +
                    " LEFT JOIN (select id as id_thuong_hieu, name as ten_thuong_hieu FROM brand) as t8 ON t8.id_thuong_hieu = t.brand_id",
            "[]",
            "[]",
            "t.code,t.full_name,t.short_name",
            "t4.priority,t3.priority,t2.priority,t1.sort_data ASC,t.sort_data ASC"),
    EXPORT_PRODUCT_GROUP(
            "product/filter_product",
            "SELECT t.*," +
                    " t2.pltth_id," +
                    " t2.pltth_name," +
                    " t3.plsp_id," +
                    " t4.mathang_id," +
                    " pg.total" +
                    " FROM product_group t" +
                    " LEFT JOIN (select product_group_id as pg_id, count(id) as total FROM product GROUP BY product_group_id) as pg ON pg.pg_id = t.id" +
                    " LEFT JOIN (select id as pltth_id, name as pltth_name, parent_priority, priority, parent_id FROM category) as t2 ON t2.pltth_id = t.category_id" +
                    " LEFT JOIN (select id as plsp_id, parent_priority, priority, parent_id FROM category) as t3 ON t3.plsp_id = t2.parent_id" +
                    " LEFT JOIN (select id as mathang_id, parent_priority, priority, parent_id FROM category) as t4 ON t4.mathang_id = t3.parent_id",
            "[]",
            "[]",
            "t.name,t.code",
            "t4.priority,t3.priority,t2.priority,t.sort_data ASC"),
    EXPORT_CATEGORY(
            "EXPORT_CATEGORY",
            "SELECT * FROM category",
            "[]",
            "[]",
            "name",
            ""),
    EXPORT_CATALOG("EXPORT_CATALOG",
            "SELECT * FROM catalog",
            "",
            "",
            "name",
            "priority ASC,id DESC"),
    LIST_PROMO_DAM_ME("promo/filter_promo_dam_me",
            "SELECT * FROM promo t WHERE promo_type = '" + PromoType.DAMME.getKey() + "'" +
                    " AND status != -1",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id DESC"),
    FILTER_RESULT_DAM_ME("utility/filter_promo",
            "SELECT id,code,name,promo_type,start_date,end_date,status,condition_type,order_date_data,payment_date_data" +
                    " FROM promo" +
                    " WHERE promo_type = " + "'" + PromoType.DAMME.getKey() + "'" +
                    " AND status IN (2,3,5,6)",
            "[]",
            "[]",
            "promo.code,promo.name",
            "id DESC"),
    SEARCH_VOUCHER("SEARCH_VOUCHER",
            "SELECT * FROM voucher t WHERE t.status = " + VoucherStatus.READY.getId(),
            "[]",
            "[]",
            "t.code",
            "t.expired_date ASC,t.offer_type ASC,t.total_value DESC,t.id DESC"),
    LIST_DAM_ME_RUNNING("promo/filter_promo",
            "SELECT t.* FROM promo t" +
                    " WHERE t.promo_type = '" + PromoType.DAMME.getKey() + "' AND t.status IN (2,3)",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id DESC"),
    LIST_ORDER_CSDM("order/filter_purchase_order",
            "SELECT t1.*," +
                    "t1.created_date as order_created_date," +
                    "t1.update_status_date as order_confirm_date" +
                    " FROM agency_order t1" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id from agency) as t_agency on t_agency.id=t1.agency_id" +
                    " WHERE" +
                    "(" +
                    "((t1.type = " + OrderType.INSTANTLY.getValue() + " OR t1.type = " + OrderType.APPOINTMENT.getValue() + ")" +
                    " AND t1.status IN (" + OrderStatus.COMPLETE.getKey() + ")" +
                    ") OR" +
                    "(t1.type = " + OrderType.CONTRACT.getValue() +
                    " AND t1.status IN (" +
                    OrderStatus.SHIPPING.getKey() + "," +
                    OrderStatus.COMPLETE.getKey() + ")" +
                    "))",
            "[]",
            "[]",
            "t1.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t1.id DESC"),
    FILTER_DIEU_CHINH_TICH_LUY_DAM_ME_HISTORY(
            "LICH_SU_DIEU_CHINH_TICH_LUY_DAM_ME",
            "SELECT t.*, t1.code FROM agency_csdm_modify_accumulate_order t LEFT JOIN agency_order t1 ON t1.id = t.agency_order_id", "created_date:t.created_date", "[]", "t1.code", "t.id DESC"),
    LIST_CTXH_RUNNING("promo/filter_promo",
            "SELECT t.* FROM promo t" +
                    " WHERE t.promo_type = '" + PromoType.BXH.getKey() + "' AND t.status IN (2,3)",
            "[]",
            "[]",
            "t.code,t.name",
            "t.id DESC"),
    LIST_VOUCHER_AGENCY("LIST_VOUCHER_AGENCY",
            "SELECT t.* FROM voucher t" +
                    " LEFT JOIN promo t1 ON t1.id = t.promo_id" +
                    " LEFT JOIN agency t_agency ON t_agency.id = t.agency_id" +
                    " LEFT JOIN agency_order t2 ON t2.id = t.agency_order_id",
            "status:t.status,voucher_release_period_id:voucher_release_period_id,created_date:t.created_date",
            "[]",
            "t.code,t1.code,t_agency.code,t_agency.shop_name,t2.code",
            "t.status ASC,t.expired_date ASC,t.id DESC"),
    FILTER_MISSION_GROUP("FILTER_MISSION_GROUP",
            "SELECT t.* FROM mission_group t WHERE t.status != -1",
            "[]",
            "[]",
            "t.name",
            "t.id DESC"),
    FILTER_MISSION_BY_GROUP("FILTER_MISSION_BY_GROUP",
            "SELECT t.* FROM mission t WHERE status != -1",
            "[]",
            "[]",
            "t.name",
            "t.id DESC"),
    FILTER_MISSION_BXH("FILTER_MISSION_BXH",
            "SELECT t.* FROM mission_bxh t WHERE status != -1",
            "[]",
            "[]",
            "t.name,t.code",
            "t.id DESC"),
    FILTER_MISSION_BXH_HISTORY("FILTER_MISSION_BXH_HISTORY",
            "SELECT t.* FROM mission_bxh_history t",
            "[]",
            "[]",
            "",
            "t.id DESC"),
    FILTER_TK_MISSION_AGENCY("FILTER_TK_MISSION_AGENCY",
            "SELECT t.* FROM mission_setting_agency_join t" +
                    " LEFT JOIN agency t1 ON t1.id = t.agency_id",
            "[]",
            "[]",
            "t1.shop_name,t1.code",
            "t.id ASC"),
    FILTER_TK_MISSION("FILTER_TK_MISSION",
            "SELECT t.*" +
                    " FROM agency_mission t",
            "[]",
            "[]",
            "",
            "t.id ASC"),
    FILTER_TK_MISSION_HISTORY("FILTER_TK_MISSION_HISTORY",
            "SELECT t.*" +
                    " FROM agency_mission_history t",
            "[]",
            "[]",
            "",
            "t.id ASC"),
    FILTER_TK_MISSION_TRANSACTION("FILTER_TK_MISSION_TRANSACTION",
            "SELECT t.* FROM mission_setting_agency_join t" +
                    " LEFT JOIN agency t1 ON t1.id = t.agency_id",
            "[]",
            "[]",
            "t1.shop_name,t1.code",
            "t.id ASC"),
    FILTER_MISSION_AGENCY("FILTER_MISSION_AGENCY",
            "SELECT t.*" +
                    " FROM agency_mission t WHERE t.status != " + MissionAgencyStatus.REPLACED.getId(),
            "[]",
            "[]",
            "",
            "t.id ASC,t.status ASC"),
    FILTER_MISSION_AGENCY_REPLACE("FILTER_MISSION_AGENCY_REPLACE",
            "SELECT t.id,t.mission_name,t.mission_period_id,t.mission_type_id,t.mission_unit_id,t.status,t.mission_reward_point,t.created_date,t.mission_end_date," +
                    "t.mission_current_value,t.mission_required_value,t.mission_current_action_number,t.mission_changed_id" +
                    " FROM agency_mission t WHERE t.status = " + MissionAgencyStatus.REPLACED.getId(),
            "[]",
            "[]",
            "",
            "t.id ASC"),
    FILTER_TK_MISSION_BXH_AGENCY("FILTER_TK_MISSION_BHX_AGENCY",
            "SELECT t.id, t.agency_id, t2.point FROM mission_bxh_agency_join t" +
                    " LEFT JOIN agency t1 ON t1.id = t.agency_id" +
                    " LEFT JOIN agency_mission_point t2 ON t2.agency_id = t.agency_id",
            "[]",
            "[]",
            "t1.shop_name,t1.code",
            "t2.point DESC,t.agency_id ASC"),
    FILTER_AGENCY_MISSION_HISTORY("FILTER_AGENCY_MISSION_HISTORY",
            "SELECT t.*" +
                    " FROM agency_mission_history t",
            "[]",
            "[]",
            "t.mission_data",
            "t.id DESC"),
    FILTER_MISSION_SETTING("FILTER_MISSION_SETTING",
            "SELECT t.* FROM mission_setting t",
            "[]",
            "[]",
            "t.name",
            "t.id DESC"),
    FILTER_TK_MISSION_BXH_AGENCY_INFO("FILTER_TK_MISSION_BXH_AGENCY_INFO",
            "SELECT t.* FROM agency_mission_bxh_info t",
            "[]",
            "[]",
            "t.note",
            "t.id ASC"),
    FILTER_MISSION_BXH_REWARD_HISTORY("FILTER_MISSION_BXH_REWARD_HISTORY",
            "SELECT t.* FROM mission_bxh_agency_join_history t WHERE offer_status = 1",
            "[]",
            "[]",
            "",
            "t.id DESC"),
    SEARCH_MISSION_BXH("SEARCH_MISSION_BXH",
            "SELECT t.id,t.name, t.code FROM mission_bxh t WHERE t.status IN (2,4)",
            "[]",
            "[]",
            "t.name,t.code",
            "t.id DESC"),
    FILTER_MISSION_POINT_HISTORY("FILTER_MISSION_POINT_HISTORY",
            "SELECT t.* FROM agency_mission_point_history t",
            "[]",
            "[]",
            "note",
            "t.id DESC"),
    FILTER_AGENCY_MISSION_ACHIEVEMENT_HISTORY("FILTER_MISSION_POINT_HISTORY",
            "SELECT t.* FROM agency_mission_achievement_history t",
            "[]",
            "[]",
            "note",
            "t.id DESC"),
    SEARCH_MISSION_SETTING("SEARCH_MISSION_SETTING",
            "SELECT t.id,t.name FROM mission_setting t WHERE t.status IN (2,3,4)",
            "[]",
            "[]",
            "t.name",
            "t.id DESC"),
    LIST_ORDER_CONFIRMATION("order/filter_purchase_order",
            "SELECT t.*,t_agency.business_department_id " +
                    " FROM agency_order_confirm t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id,status from agency) as t_agency on t_agency.id=t.agency_id" +
                    " left join (SELECT id, priority FROM order_status) AS t2 ON t2.id = t.status" +
                    " WHERE t.status IN (0,1,5)",
            "[]",
            "[]",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone,",
            "t2.priority ASC,t.update_status_date DESC,t.id DESC"),
    LIST_ORDER_CONFIRMATION_PRODUCT("order/filter_purchase_order",
            "SELECT t.*,t_agency.business_department_id," +
                    "tp.product_id," +
                    "tp.product_price," +
                    "tp.product_total_quantity," +
                    "tp.product_total_end_price," +
                    "tp.product_info," +
                    "tp.product_note," +
                    "t_agency.shop_name" +
                    " FROM agency_order_confirm t" +
                    " left join agency_order_confirm_product tp ON tp.agency_order_confirm_id = t.id" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id,status from agency) as t_agency on t_agency.id=t.agency_id" +
                    " left join (SELECT id, priority FROM order_status) AS t2 ON t2.id = t.status" +
                    " WHERE t.status IN (0,1,5)",
            "created_date:t.created_date,status:t.status",
            "[]",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone,tp.product_info",
            "t2.priority ASC,t.update_status_date DESC,t.id DESC"),
    LIST_ORDER_DELIVERY_PLAN_PRODUCT("order/LIST_ORDER_DELIVERY_PLAN_PRODUCT",
            "SELECT t.*,t_agency.business_department_id,tp.delivery_date," +
                    "tp.product_id," +
                    "tp.product_price," +
                    "tp.product_total_quantity," +
                    "tp.product_total_end_price," +
                    "tp.product_info," +
                    "tp.product_note" +
                    " FROM agency_order_confirm t" +
                    " join agency_order_shipping_plan_product tp ON tp.agency_order_confirm_id = t.id" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id,status from agency) as t_agency on t_agency.id=t.agency_id" +
                    " left join (SELECT id, priority FROM order_status) AS t2 ON t2.id = t.status" +
                    " WHERE t.status IN (0,1,5)",
            "created_date:t.created_date,status:t.status",
            "[]",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone,tp.product_info",
            "t2.priority ASC,t.update_status_date DESC,t.id DESC"),
    LIST_POM("/pom/filter_pom",
            "SELECT t.*" +
                    " FROM pom t",
            "[]",
            "[]",
            "t.code",
            "t.id DESC"),
    SEARCH_SUPPLIER("utility/SEARCH_SUPPLIER",
            "SELECT *" +
                    " FROM supplier",
            "[]",
            "[]",
            "code,shop_name,phone",
            ""),
    LIST_QOM("/pom/filter_qom",
            "SELECT t.*" +
                    " FROM supplier_quotation t",
            "[]",
            "[]",
            "t.code",
            "t.id DESC"),
    SEARCH_ORDER("order/SEARCH_ORDER",
            "SELECT t.id, t.code, t.agency_id" +
                    " FROM agency_order t" +
                    " left join (select id, shop_name, code AS agency_code, phone,city_id,region_id,membership_id,business_department_id,status from agency) as t_agency on t_agency.id=t.agency_id" +
                    " left join (SELECT id, priority FROM order_status) AS t2 ON t2.id = t.status",
            "[]",
            "[]",
            "t.code,t_agency.agency_code,t_agency.shop_name,t_agency.phone",
            "t2.priority ASC,t.update_status_date DESC,t.id DESC"),
    ;

    private String name;
    private String sql;
    private String filterBy;
    private String sortBy;
    private String searchDefault;
    private String sortDefault;

    FunctionList(String name, String sql, String filterBy, String sortBy, String searchDefault, String sortDefault) {
        this.name = name;
        this.filterBy = filterBy;
        this.sql = sql;
        this.sortBy = sortBy;
        this.searchDefault = searchDefault;
        this.sortDefault = sortDefault;
    }

    public static FunctionList from(String name) {
        for (FunctionList f : FunctionList.values()) {
            if (f.getName().equals(name)) {
                return f;
            }
        }

        return NONE;
    }
}