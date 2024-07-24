package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.order.OrderDeptInfo;
import com.app.server.data.dto.promo.PromoBasicData;
import com.app.server.data.dto.promo.PromoOrderData;
import com.app.server.data.dto.promo.PromoProductBasicData;
import com.app.server.data.dto.promo.PromoProductInfoData;
import com.app.server.data.entity.AgencyOrderDetailEntity;
import com.app.server.data.entity.AgencyOrderPromoDetailEntity;
import com.app.server.data.entity.DeptAgencyInfoEntity;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.response.product.ProductMoneyResponse;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderDeliveryService extends BaseService {
    /**
     * Danh sách phiếu hẹn
     *
     * @param request
     * @return
     */
    public ClientResponse filterOrderDelivery(SessionData sessionData, FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();

            for (FilterRequest filterRequest : request.getFilters()) {
                if (filterRequest.getKey().equals("membership_id")) {
                    filterRequest.setKey("t_agency.membership_id");
                    break;
                }
            }

            this.addFilterOrderData(sessionData, request);

            String query = this.filterUtils.getQuery(FunctionList.LIST_ORDER_DELIVERY_BILL, request.getFilters(), request.getSorts());

            List<JSONObject> records = this.orderDB.filterPurchaseOrder(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.orderDB.getTotalPurchaseOrder(query);
            for (JSONObject js : records) {
                js.put("agency_info", JsonUtils.Serialize(this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(js.get("agency_id"))
                )));
                int source = ConvertUtils.toInt(js.get("source"));
                js.put("creator_info",
                        SourceOrderType.APP.getValue() == ConvertUtils.toInt(js.get("source")) ? null :
                                this.dataManager.getStaffManager().getStaff(
                                        ConvertUtils.toInt(js.get("creator_id"))
                                ));
                int locked = ConvertUtils.toInt(js.get("locked"));
                int order_status = ConvertUtils.toInt(js.get("status"));
                Date update_status_date = DateTimeUtils.getDateTime(ConvertUtils.toString(
                        js.get("update_status_date")));
                if (ConvertUtils.toInt(js.get("source")) == SourceOrderType.APP.getValue()
                        && (order_status == OrderStatus.WAITING_CONFIRM.getKey() ||
                        order_status == OrderStatus.PREPARE.getKey())
                        && locked == 0) {
                    Date time_lock = this.appUtils.getTimeLock(
                            update_status_date, this.dataManager.getConfigManager().getTimeLock());
                    js.put("time_lock", time_lock.getTime());
                } else {
                    js.put("time_lock", update_status_date.getTime());
                }

                List<JSONObject> agency_orders =
                        this.orderDB.getListAgencyOrderOfOrderDelivery(
                                ConvertUtils.toInt(js.get("id"))
                        );
                if (agency_orders.isEmpty()) {
                    agency_orders =
                            this.orderDB.getListAgencyOrderOfOrderDeliveryGift(
                                    ConvertUtils.toInt(js.get("id"))
                            );
                }
                js.put("agency_orders", agency_orders);
            }
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Chi tiết đơn hàng
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse getOrderDeliveryBillInfo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject data = new JSONObject();
            JSONObject agencyOrder = this.orderDB.getAgencyOrderDelivery(request.getId());
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            if (!this.dataManager.getStaffManager().checkStaffManageAgency(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(ConvertUtils.toInt(agencyOrder.get("agency_id"))))) {
                return ClientResponse.fail(ResponseStatus.NOT_PERMISSION, ResponseMessage.USER_FORBIDDEN);
            }

            List<ProductMoneyResponse> products = new ArrayList<>();
            List<JSONObject> orderProductList = this.orderDB.getListProductInOrderDelivery(request.getId());
            for (JSONObject orderProduct : orderProductList) {
                AgencyOrderDetailEntity agencyOrderDetailEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(orderProduct), AgencyOrderDetailEntity.class);
                ProductMoneyResponse productMoneyResponse = new ProductMoneyResponse();
                productMoneyResponse.setId(agencyOrderDetailEntity.getProduct_id());
                productMoneyResponse.setCode(agencyOrderDetailEntity.getProduct_code());
                productMoneyResponse.setFull_name(agencyOrderDetailEntity.getProduct_full_name());
                productMoneyResponse.setProduct_small_unit_id(agencyOrderDetailEntity.getProduct_small_unit_id());
                productMoneyResponse.setStep(agencyOrderDetailEntity.getProduct_step());
                productMoneyResponse.setMinimum_purchase(agencyOrderDetailEntity.getProduct_minimum_purchase());
                productMoneyResponse.setImages(agencyOrderDetailEntity.getProduct_images());
                productMoneyResponse.setPrice(agencyOrderDetailEntity.getProduct_price());
                productMoneyResponse.setQuantity(agencyOrderDetailEntity.getProduct_total_quantity());
                productMoneyResponse.setTotal_begin_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity());
                productMoneyResponse.setTotal_promo_price(agencyOrderDetailEntity.getProduct_total_promotion_price_ctkm());
                productMoneyResponse.setTotal_csbh_price(agencyOrderDetailEntity.getProduct_total_promotion_price());
                productMoneyResponse.setTotal_end_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity() - productMoneyResponse.getTotal_promo_price());
                productMoneyResponse.setAgency_order_id(
                        ConvertUtils.toInt(orderProduct.get("agency_order_id"))
                );
                productMoneyResponse.setAgency_order_code(
                        ConvertUtils.toString(orderProduct.get("agency_order_code"))
                );
                products.add(productMoneyResponse);
            }
            List<JSONObject> gifts = new ArrayList<>();
            List<JSONObject> goods = new ArrayList<>();
            List<JSONObject> orderGoodsList = this.orderDB.getListGoodsInOrderDelivery(request.getId());
            for (JSONObject orderProduct : orderGoodsList) {
                AgencyOrderPromoDetailEntity agencyOrderDetailEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(orderProduct), AgencyOrderPromoDetailEntity.class);
                ProductMoneyResponse productMoneyResponse = new ProductMoneyResponse();
                productMoneyResponse.setId(agencyOrderDetailEntity.getProduct_id());
                productMoneyResponse.setCode(agencyOrderDetailEntity.getProduct_code());
                productMoneyResponse.setFull_name(agencyOrderDetailEntity.getProduct_full_name());
                productMoneyResponse.setProduct_small_unit_id(agencyOrderDetailEntity.getProduct_small_unit_id());
                productMoneyResponse.setStep(agencyOrderDetailEntity.getProduct_step());
                productMoneyResponse.setMinimum_purchase(agencyOrderDetailEntity.getProduct_minimum_purchase());
                productMoneyResponse.setImages(agencyOrderDetailEntity.getProduct_images());
                productMoneyResponse.setPrice(agencyOrderDetailEntity.getProduct_price());
                productMoneyResponse.setQuantity(agencyOrderDetailEntity.getProduct_total_quantity());
                productMoneyResponse.setTotal_begin_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity());
                productMoneyResponse.setTotal_promo_price(agencyOrderDetailEntity.getProduct_total_promotion_price());
                productMoneyResponse.setTotal_end_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity() - productMoneyResponse.getTotal_promo_price());
                if (AgencyOrderPromoType.GOODS_OFFER.getId() == agencyOrderDetailEntity.getType()) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GOODS_OFFER.getKey());
                    goods.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                } else if (AgencyOrderPromoType.GOODS_BONUS.getId() == agencyOrderDetailEntity.getType()) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GOODS_OFFER.getKey());
                    gifts.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                } else if (AgencyOrderPromoType.GIFT_BONUS.getId() == agencyOrderDetailEntity.getType()) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GIFT_OFFER.getKey());
                    gifts.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                }

                products.add(productMoneyResponse);
            }

            List<JSONObject> orderFlow = this.orderDB.getListOrderStatusHistory(request.getId());

            agencyOrder.put("total_promotion_price_of_product", agencyOrder.get("total_promotion_product_price"));
            agencyOrder.put("total_promotion_price_of_order", agencyOrder.get("total_promotion_order_price"));


            /**
             * Chính sách cho sản phẩm
             */
            List<PromoProductBasicData> promoProductList = new ArrayList<>();
            if (agencyOrder.get("promo_product_info") != null) {
                List<PromoProductInfoData> promoProductInfoDataList = JsonUtils.DeSerialize(
                        agencyOrder.get("promo_product_info").toString(),
                        new TypeToken<List<PromoProductInfoData>>() {
                        }.getType());

                for (PromoProductInfoData promoProductInfoData : promoProductInfoDataList) {
                    for (PromoOrderData promoOrderData : promoProductInfoData.getPromo()) {
                        PromoProductBasicData promoProductBasicData = new PromoProductBasicData();
                        promoProductBasicData.setProduct_id(promoProductInfoData.getProduct_id());
                        promoProductBasicData.setId(promoOrderData.getPromo_id());
                        promoProductBasicData.setName(promoOrderData.getPromo_name());
                        promoProductBasicData.setCode(promoOrderData.getPromo_code());
                        promoProductBasicData.setDescription(promoOrderData.getPromo_description());
                        promoProductList.add(promoProductBasicData);
                    }
                }
            }

            /**
             * Chính sách cho đơn hàng
             */
            List<PromoBasicData> promoOrderList = new ArrayList<>();
            if (agencyOrder.get("promo_order_info") != null) {
                List<PromoBasicData> promoOrderDataList = JsonUtils.DeSerialize(
                        agencyOrder.get("promo_order_info").toString(),
                        new TypeToken<List<PromoBasicData>>() {
                        }.getType());

                for (PromoBasicData promoOrderData : promoOrderDataList) {
                    PromoBasicData promoProductBasicData = new PromoBasicData();
                    promoProductBasicData.setId(promoOrderData.getId());
                    promoProductBasicData.setName(promoOrderData.getName());
                    promoProductBasicData.setCode(promoOrderData.getCode());
                    promoProductBasicData.setDescription(promoOrderData.getDescription());
                    promoOrderList.add(promoProductBasicData);
                }
            }
            int agency_id = ConvertUtils.toInt(agencyOrder.get("agency_id"));
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agency_id);
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agency_id);
                if (deptAgencyInfoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }
            /**
             * Công nợ theo đơn hàng
             */
            OrderDeptInfo orderDeptInfo = new OrderDeptInfo();
            long totalPriceOrderDoingDept = this.orderDB.getTotalPriceOrderDoing(agency_id);
            long hmkd = ConvertUtils.toLong(
                    this.getAgencyHMKD(
                            ConvertUtils.toDouble(deptAgencyInfoEntity.getDept_limit()),
                            ConvertUtils.toDouble(deptAgencyInfoEntity.getNgd_limit()),
                            ConvertUtils.toDouble(deptAgencyInfoEntity.getCurrent_dept()),
                            totalPriceOrderDoingDept));
            orderDeptInfo.setHmkd(hmkd);
            orderDeptInfo.setCno(deptAgencyInfoEntity.getCurrent_dept());
            long hmkd_over_order = ConvertUtils.toLong(agencyOrder.get("hmkd_over_order"));
            orderDeptInfo.setHmkd_over_order(
                    hmkd_over_order);
            long total_end_price = ConvertUtils.toLong(agencyOrder.get("total_end_price"));
            double totalOrderDept = this.orderDB.getTotalPriceOrderDeptDoing(agency_id);
//            long hmkd_over_current = this.getVuotHMKDCurrent(
//                    ConvertUtils.toInt(agencyOrder.get("status")),
//                    deptAgencyInfoEntity.getDept_limit(),
//                    deptAgencyInfoEntity.getNgd_limit(),
//                    deptAgencyInfoEntity.getCurrent_dept(),
//                    total_end_price,
//                    totalOrderDept);

            /**
             * Chính sách cho đơn hàng
             */
            List<PromoBasicData> ctkmOrders = new ArrayList<>();
            Map<Integer, PromoBasicData> mpCtkmOrder = new HashMap<>();
            if (agencyOrder.get("promo_product_info_ctkm") != null) {
                List<PromoProductInfoData> promoProductInfoDataList = JsonUtils.DeSerialize(
                        agencyOrder.get("promo_product_info_ctkm").toString(),
                        new TypeToken<List<PromoProductInfoData>>() {
                        }.getType());

                for (PromoProductInfoData promoProductInfoData : promoProductInfoDataList) {
                    for (PromoOrderData promoOrderData : promoProductInfoData.getPromo()) {
                        PromoBasicData promoProductBasicData = new PromoBasicData();
                        promoProductBasicData.setId(promoOrderData.getPromo_id());
                        promoProductBasicData.setName(promoOrderData.getPromo_name());
                        promoProductBasicData.setCode(promoOrderData.getPromo_code());
                        promoProductBasicData.setDescription(promoOrderData.getPromo_description());
                        mpCtkmOrder.put(promoProductBasicData.getId(), promoProductBasicData);
                    }
                }
            }

            if (agencyOrder.get("promo_order_info_ctkm") != null) {
                List<PromoBasicData> promoOrderDataList = JsonUtils.DeSerialize(
                        agencyOrder.get("promo_order_info_ctkm").toString(),
                        new TypeToken<List<PromoBasicData>>() {
                        }.getType());

                for (PromoBasicData promoOrderData : promoOrderDataList) {
                    PromoBasicData promoProductBasicData = new PromoBasicData();
                    promoProductBasicData.setId(promoOrderData.getId());
                    promoProductBasicData.setName(promoOrderData.getName());
                    promoProductBasicData.setCode(promoOrderData.getCode());
                    promoProductBasicData.setDescription(promoOrderData.getDescription());
                    mpCtkmOrder.put(promoProductBasicData.getId(), promoProductBasicData);
                }
            }
            for (PromoBasicData promoBasicData : mpCtkmOrder.values()) {
                ctkmOrders.add(promoBasicData);
            }

            /**
             * csbh/ctkm cho san pham
             */
            List<PromoBasicData> promoGoodList = new ArrayList<>();
            Map<Integer, PromoBasicData> mpPromoGood = new HashMap<>();
            if (agencyOrder.get("promo_product_info") != null) {
                List<PromoProductInfoData> promoProductInfoDataList = JsonUtils.DeSerialize(
                        agencyOrder.get("promo_product_info").toString(),
                        new TypeToken<List<PromoProductInfoData>>() {
                        }.getType());

                for (PromoProductInfoData promoProductInfoData : promoProductInfoDataList) {
                    for (PromoOrderData promoOrderData : promoProductInfoData.getPromo()) {
                        PromoBasicData promoProductBasicData = new PromoBasicData();
                        promoProductBasicData.setId(promoOrderData.getPromo_id());
                        promoProductBasicData.setName(promoOrderData.getPromo_name());
                        promoProductBasicData.setCode(promoOrderData.getPromo_code());
                        promoProductBasicData.setDescription(promoOrderData.getPromo_description());
                        mpPromoGood.put(promoProductBasicData.getId(), promoProductBasicData);
                    }
                }
            }
            if (agencyOrder.get("promo_product_info_ctkm") != null) {
                List<PromoBasicData> promoOrderDataList = JsonUtils.DeSerialize(
                        agencyOrder.get("promo_product_info_ctkm").toString(),
                        new TypeToken<List<PromoBasicData>>() {
                        }.getType());

                for (PromoBasicData promoOrderData : promoOrderDataList) {
                    PromoBasicData promoProductBasicData = new PromoBasicData();
                    promoProductBasicData.setId(promoOrderData.getId());
                    promoProductBasicData.setName(promoOrderData.getName());
                    promoProductBasicData.setCode(promoOrderData.getCode());
                    promoProductBasicData.setDescription(promoOrderData.getDescription());
                    mpPromoGood.put(promoProductBasicData.getId(), promoProductBasicData);
                }
            }
            for (PromoBasicData promoBasicData : mpPromoGood.values()) {
                promoGoodList.add(promoBasicData);
            }

            orderDeptInfo.setNqh_order(ConvertUtils.toLong(agencyOrder.get("nqh_order")));
            orderDeptInfo.setNqh_current(deptAgencyInfoEntity.getNqh());
            orderDeptInfo.setNgd_limit(deptAgencyInfoEntity.getNgd_limit());

            /**
             * nếu cam kết
             */
            int stuck_type = ConvertUtils.toInt(agencyOrder.get("stuck_type"));
            String stuck_info = ConvertUtils.toString(agencyOrder.get("stuck_info"));

            if (StuckType.NQH_CK.getId() == stuck_type) {
                JSONObject agency_order_commit = this.orderDB.getAgencyOrderCommitByOrder(request.getId());
                if (agency_order_commit != null) {
                    orderDeptInfo.setCommitted_date(ConvertUtils.toString(agency_order_commit.get("committed_date")));
                    orderDeptInfo.setCommitted_money(ConvertUtils.toLong(agency_order_commit.get("committed_money")));

                    stuck_info += ": " + appUtils.priceFormat(orderDeptInfo.getCommitted_money()) +
                            " - ngày cam kết: " + DateTimeUtils.toString(
                            DateTimeUtils.getDateTime(orderDeptInfo.getCommitted_date(), "yyyy-MM-dd"), "dd-MM-yyyy");
                }
            }

            agencyOrder.put("agency_info", JsonUtils.Serialize(
                    this.dataManager.getAgencyManager().getAgencyBasicData(agency_id))
            );

            long total_promotion_price = ConvertUtils.toLong(agencyOrder.get("total_promotion_price"));
            long total_promotion_order_price_ctkm = ConvertUtils.toLong(agencyOrder.get("total_promotion_order_price_ctkm"));
            agencyOrder.put("total_promotion_price",
                    total_promotion_price
                            + total_promotion_order_price_ctkm);

            data.put("order", agencyOrder);
            data.put("dept_info", orderDeptInfo);
            data.put("products", products);
            data.put("goods", goods);
            data.put("gifts", gifts);
            data.put("order_flow", orderFlow);
            data.put("promo_products", promoProductList);
            data.put("promo_goods", promoGoodList);
            data.put("promo_orders", promoOrderList);
            data.put("ctkm_orders", ctkmOrders);
            data.put("stuck_info", stuck_info);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}