package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.order.OrderDeptInfo;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.promo.PromoBasicData;
import com.app.server.data.dto.promo.PromoOrderData;
import com.app.server.data.dto.promo.PromoProductBasicData;
import com.app.server.data.dto.promo.PromoProductInfoData;
import com.app.server.data.dto.staff.Staff;
import com.app.server.data.entity.AgencyOrderDetailEntity;
import com.app.server.data.entity.AgencyOrderPromoDetailEntity;
import com.app.server.data.entity.DeptAgencyInfoEntity;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.commit.SettingNumberDayNQHMissCommitRequest;
import com.app.server.data.request.promo.ApprovePromoRequest;
import com.app.server.data.request.promo.CreatePromoRequest;
import com.app.server.data.request.promo.EditPromoRequest;
import com.app.server.data.response.product.ProductMoneyResponse;
import com.app.server.database.AgencyDB;
import com.app.server.database.PomDB;
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
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class POMService extends ProgramService {
    private AgencyDB agencyDB;

    @Autowired
    public void setAgencyDB(AgencyDB agencyDB) {
        this.agencyDB = agencyDB;
    }

    private FilterUtils filterUtils;

    @Autowired
    public void setFilterUtils(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    public DataManager dataManager;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    protected AppUtils appUtils;

    @Autowired
    public void setAppUtil(AppUtils appUtils) {
        this.appUtils = appUtils;
    }

    private PomDB pomDB;

    @Autowired
    public void setPomDB(PomDB pomDB) {
        this.pomDB = pomDB;
    }

    /**
     * Danh sách phiếu xác nhận đặt hàng
     *
     * @param request
     * @return
     */
    public ClientResponse filterPOM(SessionData sessionData, FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();

            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("business_department_id");
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(staff.getDepartment_id()));
            request.getFilters().add(filterRequest);

            String query = this.filterUtils.getQuery(FunctionList.LIST_POM, request.getFilters(), request.getSorts());

            List<JSONObject> records = this.orderDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.orderDB.getTotal(query);
            for (JSONObject js : records) {
                js.put("agency_info", JsonUtils.Serialize(this.agencyDB.getSupplierInfo(
                        ConvertUtils.toInt(js.get("agency_id"))
                )));
                js.put("creator_info",
                        SourceOrderType.APP.getValue() == ConvertUtils.toInt(js.get("source")) ? null :
                                this.dataManager.getStaffManager().getStaff(
                                        ConvertUtils.toInt(js.get("creator_id"))
                                ));
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
     * Chi tiết phiếu đặt hàng
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse getPOMInfo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject agencyOrder = this.orderDB.getPOMInfo(request.getId());
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            JSONObject data = new JSONObject();

            Map<Integer, ProductMoneyResponse> products = new ConcurrentHashMap<>();
            List<JSONObject> orderProductList = this.orderDB.getListProductInPOM(request.getId());
            for (JSONObject orderProduct : orderProductList) {
                AgencyOrderDetailEntity agencyOrderDetailEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(orderProduct), AgencyOrderDetailEntity.class);

                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(agencyOrderDetailEntity.getProduct_id());
                if (productCache == null) {
                    continue;
                }
                ProductMoneyResponse productMoneyResponse = new ProductMoneyResponse();
                productMoneyResponse.setId(agencyOrderDetailEntity.getProduct_id());
                productMoneyResponse.setCode(productCache.getCode());
                productMoneyResponse.setFull_name(productCache.getFull_name());
                productMoneyResponse.setProduct_small_unit_id(productCache.getProduct_small_unit_id());
                productMoneyResponse.setStep(productCache.getStep());
                productMoneyResponse.setMinimum_purchase(productCache.getMinimum_purchase());
                productMoneyResponse.setImages(productCache.getImages());
                productMoneyResponse.setPrice(agencyOrderDetailEntity.getProduct_price());
                productMoneyResponse.setQuantity(agencyOrderDetailEntity.getProduct_total_quantity());
                productMoneyResponse.setTotal_begin_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity());
                productMoneyResponse.setTotal_promo_price(agencyOrderDetailEntity.getProduct_total_promotion_price_ctkm());
                productMoneyResponse.setTotal_csbh_price(agencyOrderDetailEntity.getProduct_total_promotion_price());
                productMoneyResponse.setTotal_end_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity() - productMoneyResponse.getTotal_promo_price());

                ProductMoneyResponse product = products.get(productMoneyResponse.getId());
                if (product == null) {
                    products.put(productMoneyResponse.getId(), productMoneyResponse);
                } else {
                    product.setQuantity(product.getQuantity() + productMoneyResponse.getQuantity());
                    products.put(product.getId(), product);
                }
            }
            List<JSONObject> gifts = new ArrayList<>();
            List<JSONObject> goods = new ArrayList<>();
            List<JSONObject> orderGoodsList = new ArrayList<>();

            List<JSONObject> orderFlow = new ArrayList<>();

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


            agencyOrder.put("agency_info", JsonUtils.Serialize(
                    this.agencyDB.getSupplierInfo(ConvertUtils.toInt(agencyOrder.get("agency_id")))));

            long total_promotion_price = ConvertUtils.toLong(agencyOrder.get("total_promotion_price"));
            long total_promotion_order_price_ctkm = ConvertUtils.toLong(agencyOrder.get("total_promotion_order_price_ctkm"));
            agencyOrder.put("total_promotion_price",
                    total_promotion_price
                            + total_promotion_order_price_ctkm);

            data.put("order", agencyOrder);
            data.put("products", new ArrayList<ProductMoneyResponse>(products.values()));
            data.put("goods", goods);
            data.put("gifts", gifts);
            data.put("order_flow", orderFlow);
            data.put("promo_products", promoProductList);
            data.put("promo_orders", promoOrderList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Danh sách phiếu xác nhận đặt hàng
     *
     * @param request
     * @return
     */
    public ClientResponse filterQOM(SessionData sessionData, FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();

            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("business_department_id");
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(staff.getDepartment_id()));
            request.getFilters().add(filterRequest);

            String query = this.filterUtils.getQuery(FunctionList.LIST_QOM, request.getFilters(), request.getSorts());

            List<JSONObject> records = this.orderDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.orderDB.getTotal(query);
            for (JSONObject js : records) {
                js.put("agency_info", JsonUtils.Serialize(this.agencyDB.getSupplierInfo(
                        ConvertUtils.toInt(js.get("agency_id"))
                )));
                js.put("creator_info",
                        SourceOrderType.APP.getValue() == ConvertUtils.toInt(js.get("source")) ? null :
                                this.dataManager.getStaffManager().getStaff(
                                        ConvertUtils.toInt(js.get("creator_id"))
                                ));
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

    public ClientResponse getQOMInfo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject jsQOM = this.pomDB.getQOM(request.getId());
            if (jsQOM == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            CreatePromoRequest createPromoRequest = this.convertQOMToPromo(jsQOM);

            JSONObject data = new JSONObject();
            data.put("promo", createPromoRequest);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private CreatePromoRequest convertQOMToPromo(JSONObject jsQOM) {
        try {
            CreatePromoRequest createPromoRequest = JsonUtils.DeSerialize(
                    jsQOM.get("data").toString(), CreatePromoRequest.class
            );
            createPromoRequest.getPromo_info().setId(
                    ConvertUtils.toInt(jsQOM.get("id"))
            );
            createPromoRequest.getPromo_info().setCode(
                    ConvertUtils.toString(jsQOM.get("code"))
            );
            createPromoRequest.getPromo_info().setName(
                    ConvertUtils.toString(jsQOM.get("name"))
            );
            createPromoRequest.getPromo_info().setStart_date(
                    jsQOM.get("start_date").toString()
            );
            createPromoRequest.getPromo_info().setStatus(
                    ConvertUtils.toInt(jsQOM.get("status")));

            createPromoRequest.getPromo_item_groups().forEach(
                    g -> g.getProducts().forEach(
                            p -> {
                                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(p.getItem_id());
                                if (productCache != null) {
                                    p.setItem_code(productCache.getCode());
                                    p.setItem_name(productCache.getFull_name());
                                }
                            }
                    )
            );

            createPromoRequest.getPromo_limits().forEach(
                    l -> l.getPromo_limit_groups().forEach(
                            lg -> lg.getOffer().getOffer_products().forEach(
                                    p -> {
                                        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(p.getProduct_id());
                                        if (productCache != null) {
                                            p.setProduct_code(productCache.getCode());
                                            p.setProduct_name(productCache.getFull_name());
                                        }
                                    }

                            )
                    )
            );
            return createPromoRequest;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return null;
    }

    private String generatePromoCode(String promo_type) {
        try {
            /**
             * yyMMdd + loại chính sách + stt của loại chính sách
             */
            int count = this.pomDB.getTotalQOM();
            if (count < 0) {
                return "";
            }
            count = count + 1;
            String tmp = StringUtils.leftPad(String.valueOf(count), 4, '0');
            DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
            String date = dateFormat.format(new Date());

            return (date + "QOM" + "_" + tmp);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return "";
    }

    public ClientResponse createQOM(SessionData sessionData, CreatePromoRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            String code = this.generatePromoCode(request.getPromo_info().getPromo_type());
            if (code.isEmpty()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
            }

            int rs = this.pomDB.createQOM(
                    code,
                    request.getPromo_info().getName(),
                    staff.getDepartment_id(),
                    DateTimeUtils.getDateTime(request.getPromo_info().getStart_date_millisecond()),
                    JsonUtils.Serialize(request)
            );

            JSONObject data = new JSONObject();
            data.put("id", rs);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse approveQOM(SessionData sessionData, ApprovePromoRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            JSONObject jsQOM = this.pomDB.getQOM(request.getId());
            if (jsQOM == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rsApprove = this.pomDB.approveQOM(
                    request.getId()
            );
            if (rsApprove == false) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            CreatePromoRequest createPromoRequest = this.convertQOMToPromo(
                    this.pomDB.getQOM(request.getId()));
            if (createPromoRequest != null) {
                this.pomDB.updateQOMData(
                        request.getId(),
                        JsonUtils.Serialize(createPromoRequest));
            }

            JSONObject data = new JSONObject();
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse stopQOM(SessionData sessionData, ApprovePromoRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            JSONObject jsQOM = this.pomDB.getQOM(request.getId());
            if (jsQOM == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rsStop = this.pomDB.stopQOM(
                    request.getId()
            );
            if (rsStop == false) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            JSONObject data = new JSONObject();
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editQOM(SessionData sessionData, EditPromoRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            JSONObject jsQOM = this.pomDB.getQOM(request.getPromo_info().getId());
            if (jsQOM == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            int status = ConvertUtils.toInt(jsQOM.get("status"));
            boolean rsAction = this.pomDB.updateQOM(
                    request.getPromo_info().getId(),
                    request.getPromo_info().getName(),
                    DateTimeUtils.getDateTime(request.getPromo_info().getStart_date_millisecond()),
                    JsonUtils.Serialize(request));
            if (!rsAction) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (PromoActiveStatus.RUNNING.getId() == status) {
                CreatePromoRequest createPromoRequest = this.convertQOMToPromo(
                        this.pomDB.getQOM(request.getPromo_info().getId()));
                if (createPromoRequest != null) {
                    this.pomDB.updateQOMData(
                            request.getPromo_info().getId(),
                            JsonUtils.Serialize(createPromoRequest));
                }
            }

            JSONObject data = new JSONObject();
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}