package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.CTXHConstants;
import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.ctxh.OrderAccumulateCTXHData;
import com.app.server.data.dto.ctxh.OrderVoucherGiftData;
import com.app.server.data.dto.ctxh.VoucherData;
import com.app.server.data.dto.order.*;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.product.ProductData;
import com.app.server.data.dto.program.Order;
import com.app.server.data.dto.program.Program;
import com.app.server.data.dto.program.Source;
import com.app.server.data.dto.program.TLProductReward;
import com.app.server.data.dto.program.agency.Agency;
import com.app.server.data.dto.program.offer.ProgramSanSaleOffer;
import com.app.server.data.dto.program.product.OfferProduct;
import com.app.server.data.dto.program.product.ProgramOrderProduct;
import com.app.server.data.dto.promo.*;
import com.app.server.data.dto.staff.BusinessDepartment;
import com.app.server.data.dto.staff.Staff;
import com.app.server.data.entity.*;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.BasicRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.bravo.BravoResponse;
import com.app.server.data.request.ctxh.VRPDataRequest;
import com.app.server.data.request.order.*;
import com.app.server.data.response.agency.AgencyInfoInListResponse;
import com.app.server.data.response.order.ItemOfferResponse;
import com.app.server.data.response.order.OrderSummaryInfoResponse;
import com.app.server.data.response.product.ProductMoneyResponse;
import com.app.server.data.response.order.OrderProductResponse;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.AppUtils;
import com.app.server.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mysql.cj.log.Log;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import io.swagger.models.auth.In;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.spring.web.json.Json;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderService extends ProgramService {

    private BravoService bravoService;

    @Autowired
    public void setBravoService(BravoService bravoService) {
        this.bravoService = bravoService;
    }

    private AccumulateService accumulateService;

    @Autowired
    public void setAccumulateService(AccumulateService accumulateService) {
        this.accumulateService = accumulateService;
    }

    private AccumulateCSDMService accumulateCSDMService;

    @Autowired
    public void setAccumulateCSDMService(AccumulateCSDMService accumulateCSDMService) {
        this.accumulateCSDMService = accumulateCSDMService;
    }

    private AccumulateCTXHService accumulateCTXHService;

    @Autowired
    public void setAccumulateCTXHService(AccumulateCTXHService accumulateCTXHService) {
        this.accumulateCTXHService = accumulateCTXHService;
    }

    private AccumulateMissionService accumulateMissionService;

    @Autowired
    public void setAccumulateMissionService(AccumulateMissionService accumulateMissionService) {
        this.accumulateMissionService = accumulateMissionService;
    }

    /**
     * Tạo đơn hàng giao ngay
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse createOrderInstantly(SessionData sessionData, CreateOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            AgencyEntity agencyEntity = this.agencyDB.getAgencyEntity(request.getAgency_id());
            /**
             * Validate data create order
             */
            clientResponse = this.validateCreateOrder(agencyEntity);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            return this.saveOrder(
                    agencyEntity,
                    request,
                    sessionData,
                    null,
                    true,
                    SourceOrderType.CMS.getValue()
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private CreateOrderRequest createSaveOrderRequest(
            EditOrderProductRequest request
    ) {
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setAgency_id(request.getAgency_id());
        createOrderRequest.setAddress_delivery_id(request.getAddress_delivery_id());
        createOrderRequest.setAddress_export_billing_id(request.getAddress_export_billing_id());
        createOrderRequest.setHunt_sale_products(request.getHunt_sale_products());
        createOrderRequest.setEstimate_order_data(request.getEstimate_order_data());
        createOrderRequest.setGoods(request.getGoods());
        createOrderRequest.setNote_internal(request.getNote_internal());
        createOrderRequest.setDelivery_time_request(request.getDelivery_time_request());
        createOrderRequest.setVouchers(request.getVouchers());
        return createOrderRequest;
    }

    private ClientResponse saveOrder(
            AgencyEntity agencyEntity,
            CreateOrderRequest request,
            SessionData sessionData,
            AgencyOrderEntity oldOrder,
            boolean isCreate,
            int source
    ) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }
            /**
             * Công nợ hiện tại
             */
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(request.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = initDeptAgencyInfo(request.getAgency_id());
                if (deptAgencyInfoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_AGENCY_INFO_EMPTY);
                }
            }

            if (!request.getVouchers().isEmpty() && this.validateVoucher(request.getVouchers()).failed()) {
                ClientResponse crVoucher = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                crVoucher.setMessage("[VOUCHER] Voucher không tồn tại hoặc đã hết hạn");
                return crVoucher;
            }

            /**
             * Săn sale
             */
            Map<Integer, ProductCache> mpProductCache = new ConcurrentHashMap<>();
            Map<Integer, ProgramOrderProduct> mpProgramOrderProduct =
                    this.initMpProductHuntSale(
                            agencyEntity,
                            mpProductCache,
                            request.getHunt_sale_products(),
                            request.getProducts());
            ClientResponse crCheckVisibility = this.checkVisibilityWhenCreateOrder(
                    agencyEntity.getId(),
                    agencyEntity.getCity_id(),
                    agencyEntity.getRegion_id(),
                    agencyEntity.getMembership_id(),
                    new ArrayList<>(mpProgramOrderProduct.values()));
            if (crCheckVisibility.failed()) {
                return crCheckVisibility;
            }
            Map<Integer, Integer> mpProductQuantityUsed = new ConcurrentHashMap<>();
            Map<Integer, ProgramOrderProduct> mpAllProduct = new ConcurrentHashMap<>();
            mpAllProduct.putAll(mpProgramOrderProduct);
            EstimatePromo estimatePromoHuntSale = this.createEstimatePromoHuntSale(
                    agencyEntity,
                    request.getHunt_sale_products());
            Map<Integer, Integer> mpStock = this.initMpStock(mpAllProduct);

            List<HuntSaleOrder> huntSaleOrderList = this.estimateHuntSaleOrder(
                    agencyEntity.getId(),
                    estimatePromoHuntSale,
                    SourceOrderType.CMS.getValue(),
                    request.getHunt_sale_products(),
                    mpProgramOrderProduct,
                    mpAllProduct,
                    mpProductQuantityUsed,
                    mpProductCache,
                    mpStock,
                    deptAgencyInfoEntity,
                    this.dataManager.getProgramManager().getAgency(agencyEntity.getId()),
                    agencyEntity
            );

            Map<Integer, Double> mpProductQuotation = this.parseProductPrice(huntSaleOrderList);
            huntSaleOrderList.clear();
            /**
             * Khởi tạo dữ liệu tính toán
             */
            OrderSummaryInfoResponse orderMoneyInfoResponse = new OrderSummaryInfoResponse();
            List<ProductMoneyResponse> products = new ArrayList<>();
            List<AgencyOrderDetailEntity> agencyOrderDetailEntityList = new ArrayList<>();

            List<OrderProductResponse> productInputs = new ArrayList<>();
            List<ItemOfferResponse> goodsForSelectList = new ArrayList<>();
            List<ItemOfferResponse> goodsClaimList = new ArrayList<>();
            EstimatePromo estimatePromo = new EstimatePromo();

            Map<Integer, ProductCache> mpProductPrice = new ConcurrentHashMap<>();

            /**
             * Lấy thông tin sản phẩm và tạo entity
             */
            for (ProgramOrderProduct programOrderProduct : mpAllProduct.values()) {
                ProductEntity productEntity = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(mpProductCache.get(programOrderProduct.getProduct().getId())),
                        ProductEntity.class
                );
                if (productEntity == null) {
                    continue;
                }

                ProductCache productPrice = mpProductPrice.get(productEntity.getId());
                if (productPrice == null) {
                    productPrice = this.getFinalPriceByAgency(
                            productEntity.getId(),
                            agencyEntity.getId(),
                            agencyEntity.getCity_id(),
                            agencyEntity.getRegion_id(),
                            agencyEntity.getMembership_id()
                    );

                    mpProductPrice.put(productEntity.getId(), productPrice);
                }

                /**
                 * Kiểm tra ẩn hiện sản phẩm
                 */

                /**
                 * Khởi tạo sản phẩm cho đơn hàng
                 */
                AgencyOrderDetailEntity agencyOrderDetailEntity = this.initAgencyOrderEntityByProduct(
                        productEntity,
                        programOrderProduct.getProductQuantity(),
                        productPrice.getPrice(),
                        productPrice.getMinimum_purchase()
                );

                /**
                 * tính giá
                 */

                agencyOrderDetailEntityList.add(agencyOrderDetailEntity);

                mpProductQuantityUsed.put(
                        agencyOrderDetailEntity.getProduct_id(),
                        agencyOrderDetailEntity.getProduct_total_quantity());
            }

            /**
             * Duyệt từng sản phẩm
             */
            for (AgencyOrderDetailEntity agencyOrderDetailEntity : agencyOrderDetailEntityList) {
                ProductEntity productEntity = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(mpProductCache.get(agencyOrderDetailEntity.getProduct_id())),
                        ProductEntity.class
                );

                ProductMoneyResponse productMoneyResponse = this.productUtils.getProductMoney(
                        agencyEntity,
                        productEntity,
                        agencyOrderDetailEntity.getProduct_total_quantity(),
                        agencyOrderDetailEntity.getProduct_price(),
                        agencyOrderDetailEntity.getProduct_minimum_purchase()
                );

                Double promoProductPriceQuotation = mpProductQuotation.get(agencyOrderDetailEntity.getProduct_id());
                if (promoProductPriceQuotation != null) {
                    productMoneyResponse.setPrice(promoProductPriceQuotation * 1L);
                }

                agencyOrderDetailEntity.setAgency_order_id(request.getAgency_id());
                agencyOrderDetailEntity.setProduct_total_quantity(agencyOrderDetailEntity.getProduct_total_quantity());
                agencyOrderDetailEntity.setProduct_price(productMoneyResponse.getPrice());
                agencyOrderDetailEntity.setProduct_total_begin_price(
                        productMoneyResponse.getPrice() *
                                agencyOrderDetailEntity.getProduct_total_quantity() * 1L);
                agencyOrderDetailEntity.setProduct_total_end_price(
                        agencyOrderDetailEntity.getProduct_total_begin_price());


                /**
                 * Không tính tổng tiền của đơn hàng,
                 * bỏ qua những dòng sản phẩm có giá liên hệ -1
                 */
                if (productMoneyResponse.getPrice() <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_FAIL_BY_PRODUCT_PRICE_CONTACT);
                }
                products.add(productMoneyResponse);

                orderMoneyInfoResponse.setTotal_product_quantity(orderMoneyInfoResponse.getTotal_product_quantity() + agencyOrderDetailEntity.getProduct_total_quantity());

                /**
                 * Đưa vào danh sách sản phẩm tính chính sách
                 */
                estimatePromo.getPromoProductInputList().add(new PromoProductBasic(
                        productMoneyResponse.getId(),
                        productMoneyResponse.getQuantity(),
                        productMoneyResponse.getPrice() * 1L,
                        null));
            }

            /**
             * Tính chính sách
             */
            ResultEstimatePromo resultEstimatePromo = this.estimatePromo(
                    request.getAgency_id(),
                    estimatePromo,
                    request.getGoods(),
                    new HashMap<>(),
                    source
            );
            if (resultEstimatePromo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }


            /**
             * Tổng tiền đổi hàng không vượt quá số tiền được tặng
             */
            if (orderMoneyInfoResponse.getTotal_goods_offer_price() < orderMoneyInfoResponse.getTotal_goods_offer_claimed_price()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_GOODS_OFFER_OVERLOAD_PRICE);
            }

            orderMoneyInfoResponse.setTotal_goods_offer_remain_price(
                    orderMoneyInfoResponse.getTotal_goods_offer_price()
                            - orderMoneyInfoResponse.getTotal_goods_offer_claimed_price()
            );
            List<AgencyOrderPromoDetailEntity> agencyOrderPromoDetailEntityList = new ArrayList<>();

            /**
             * Chiết khấu/giảm tiền sản phẩm
             */
            for (AgencyOrderDetailEntity agencyOrderDetailEntity : agencyOrderDetailEntityList) {
                /**
                 * Chiết khấu/Giảm tiền CSBH
                 */
                Double promoProductPriceCSBH = resultEstimatePromo.getMpPromoProductPriceCSBH().get(agencyOrderDetailEntity.getProduct_id());
                if (promoProductPriceCSBH == null) {
                    promoProductPriceCSBH = 0.0;
                }
                agencyOrderDetailEntity.setProduct_total_promotion_price(
                        promoProductPriceCSBH
                );
                orderMoneyInfoResponse.setTotal_promotion_price_of_product(
                        orderMoneyInfoResponse.getTotal_promotion_price_of_product()
                                + promoProductPriceCSBH);

                /**
                 * Chiết khấu/Giảm tiền CTKM
                 */
                Double promoProductPriceCTKM = resultEstimatePromo.getMpPromoProductPriceCTKM().get(agencyOrderDetailEntity.getProduct_id());
                if (promoProductPriceCTKM == null) {
                    promoProductPriceCTKM = 0.0;
                }

                agencyOrderDetailEntity.setProduct_total_promotion_price_ctkm(
                        promoProductPriceCTKM
                );

                orderMoneyInfoResponse.setTotal_promotion_price(
                        orderMoneyInfoResponse.getTotal_promotion_price()
                                + promoProductPriceCTKM);

                /**
                 * CSDM - Giảm tiền
                 */
                Double promoProductPriceCSDM = resultEstimatePromo.getMpPromoProductPriceCSDM().get(agencyOrderDetailEntity.getProduct_id());
                if (promoProductPriceCSDM == null) {
                    promoProductPriceCSDM = 0.0;
                } else {
                    agencyOrderDetailEntity.setDm_id(
                            resultEstimatePromo.getMpPromoProductCSDM().get(
                                    agencyOrderDetailEntity.getProduct_id()
                            ).get(0).getId()
                    );
                }
                agencyOrderDetailEntity.setProduct_total_dm_price(
                        promoProductPriceCSDM
                );
                orderMoneyInfoResponse.setTong_uu_dai_dam_me(
                        orderMoneyInfoResponse.getTong_uu_dai_dam_me()
                                + promoProductPriceCSDM);

                /**
                 * Tổng tiền của sản phẩm
                 */
                agencyOrderDetailEntity.setProduct_total_end_price(
                        agencyOrderDetailEntity.getProduct_total_begin_price()
                                - agencyOrderDetailEntity.getProduct_total_promotion_price()
                                - agencyOrderDetailEntity.getProduct_total_dm_price()
                                - agencyOrderDetailEntity.getProduct_total_promotion_price_ctkm()
                );
            }

            /**
             * Tiền tạm tính của đơn hàng
             */
            orderMoneyInfoResponse.setTotal_begin_price(resultEstimatePromo.getTotal_begin_price());

            /**
             * Danh sách hàng tặng nếu có
             */
            for (PromoProductBasic promoProductBasic : resultEstimatePromo.getPromoGoodsOfferList()) {
                OrderProductRequest goodsSelect = this.getProductInGoodsSelect(promoProductBasic, request.getGoods());

                ProductCache productCache = this.dataManager.getProductManager().getMpProduct().get(promoProductBasic.getItem_id());
                if (productCache == null) {
                    continue;
                }

                ProductCache productPrice = mpProductPrice.get(productCache.getId());
                if (productPrice == null) {
                    productPrice = this.getFinalPriceByAgency(
                            productCache.getId(),
                            agencyEntity.getId(),
                            agencyEntity.getCity_id(),
                            agencyEntity.getRegion_id(),
                            agencyEntity.getMembership_id()
                    );
                    mpProductPrice.put(productCache.getId(), productPrice);
                }

                /**
                 * Không tặng hàng bị ẩn
                 */
                if (VisibilityType.HIDE.getId() == this.getProductVisibilityByAgency(
                        agencyEntity.getId(),
                        promoProductBasic.getItem_id()
                )) {
                    continue;
                }

                ProductEntity productEntity = this.productDB.getProduct(productCache.getId());
                if (productEntity == null) {
                    continue;
                }

                PromoProductBasic orderProductResponse = this.getProductInOrder(estimatePromo.getPromoProductInputList(), promoProductBasic.getItem_id());
                if (orderProductResponse == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                PromoProductBasic goods = new PromoProductBasic(
                        promoProductBasic.getItem_id(),
                        promoProductBasic.getItem_quantity(),
                        orderProductResponse.getItem_price() * 1L,
                        PromoOfferType.GOODS_OFFER.getKey()
                );
                estimatePromo.getPromoGoodsSelectList().add(goods);

                ItemOfferResponse itemOfferResponse = this.convertGoodOfferResponse(promoProductBasic, productCache);
                itemOfferResponse.setPrice(
                        orderProductResponse.getItem_price());
                itemOfferResponse.setQuantity_select(goodsSelect != null ? goodsSelect.getProduct_quantity() : 0);

                int stock = this.getTonKho(itemOfferResponse.getId());
                Integer quantity_used = mpProductQuantityUsed.get(itemOfferResponse.getId());
                if (quantity_used == null) {
                    quantity_used = 0;
                }

                if (stock - quantity_used < itemOfferResponse.getQuantity_select()) {
                    ClientResponse crTonKho = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.OUT_OF_STOCK);
                    String message = "[" + crTonKho.getMessage() + "] Tồn kho của sản phẩm " +
                            itemOfferResponse.getName() + " chỉ còn " + appUtils.priceFormat(Math.max(0, stock)) + ";";
                    crTonKho.setMessage(message);
                    return crTonKho;
                }

                goodsForSelectList.add(itemOfferResponse);

                /**
                 * Thêm vào danh sách hàng tặng của đơn
                 */
                if (itemOfferResponse != null && itemOfferResponse.getQuantity_select() > 0) {
                    AgencyOrderPromoDetailEntity agencyOrderPromoDetailEntity = this.initAgencyOrderPromoEntity(
                            productEntity,
                            itemOfferResponse.getQuantity_select(),
                            productPrice.getPrice(),
                            productPrice.getMinimum_purchase()
                    );
                    agencyOrderPromoDetailEntity.setAgency_order_id(request.getAgency_id());
                    agencyOrderPromoDetailEntity.setProduct_total_quantity(agencyOrderPromoDetailEntity.getProduct_total_quantity());
                    agencyOrderPromoDetailEntity.setProduct_price(itemOfferResponse.getPrice());
                    agencyOrderPromoDetailEntity.setProduct_total_begin_price(
                            agencyOrderPromoDetailEntity.getProduct_price()
                                    * agencyOrderPromoDetailEntity.getProduct_total_quantity() * 1L);
                    agencyOrderPromoDetailEntity.setProduct_total_end_price(agencyOrderPromoDetailEntity.getProduct_total_begin_price());
                    agencyOrderPromoDetailEntity.setType(AgencyOrderPromoType.GOODS_OFFER.getId());
                    agencyOrderPromoDetailEntityList.add(agencyOrderPromoDetailEntity);

                    orderMoneyInfoResponse.setTotal_goods_quantity(
                            orderMoneyInfoResponse.getTotal_goods_quantity()
                                    + itemOfferResponse.getQuantity_select()
                    );
                }

                mpProductQuantityUsed.put(itemOfferResponse.getId(), quantity_used + itemOfferResponse.getQuantity_select());
            }

            orderMoneyInfoResponse.setTotal_goods_offer_price(resultEstimatePromo.getTotalMoneyGoodsOffer());
            orderMoneyInfoResponse.setTotal_goods_offer_claimed_price(
                    goodsForSelectList.stream().reduce(0L, (total, object) -> total + (
                            ConvertUtils.toLong(object.getQuantity_select() * object.getPrice())), Long::sum)
            );
            orderMoneyInfoResponse.setTotal_goods_offer_remain_price(
                    orderMoneyInfoResponse.getTotal_goods_offer_price()
                            - orderMoneyInfoResponse.getTotal_goods_offer_claimed_price()
            );

            if (orderMoneyInfoResponse.getTotal_goods_offer_remain_price() < 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_GOODS_OFFER_OVERLOAD_PRICE);
            }

            double total_refund_price = 0;
            /**
             * Giá trị khuyến mãi còn lại có thể đổi thêm hàng tặng, vui lòng chọn thêm hàng tặng
             */
            if (orderMoneyInfoResponse.getTotal_goods_offer_remain_price() > 0) {
                for (ItemOfferResponse itemOfferResponse : goodsForSelectList) {
                    int stock = this.getTonKho(itemOfferResponse.getId());
                    Integer quantity_used = mpProductQuantityUsed.get(itemOfferResponse.getId());
                    if (quantity_used == null) {
                        quantity_used = 0;
                    }

                    if (orderMoneyInfoResponse.getTotal_goods_offer_remain_price()
                            >= itemOfferResponse.getPrice()) {
                        if (stock - quantity_used > 0) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_GOODS_OFFER_PRODUCT_PRICE_UPPER_PRICE_MIN);
                        }

                        total_refund_price = orderMoneyInfoResponse.getTotal_goods_offer_remain_price();
                    }
                }
            }

            LogUtil.printDebug(JsonUtils.Serialize(orderMoneyInfoResponse));


            /**
             * Tặng hàng/Tặng quà
             */
            for (PromoGiftClaim promoProductBasic : resultEstimatePromo.getPromoGiftClaimList()) {
                ProductCache productCache = this.dataManager.getProductManager().getMpProduct().get(promoProductBasic.getItem_id());
                if (productCache == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ProductCache productPrice = mpProductPrice.get(productCache.getId());
                if (productPrice == null) {
                    productPrice = this.getFinalPriceByAgency(
                            productCache.getId(),
                            agencyEntity.getId(),
                            agencyEntity.getCity_id(),
                            agencyEntity.getRegion_id(),
                            agencyEntity.getMembership_id()
                    );
                    mpProductPrice.put(productCache.getId(), productPrice);
                }

                /**
                 * Không tặng hàng bị ẩn
                 */
                if (VisibilityType.HIDE.getId() == this.getProductVisibilityByAgency(
                        agencyEntity.getId(),
                        promoProductBasic.getItem_id()
                )) {
                    continue;
                }

                ProductEntity productEntity = this.productDB.getProduct(productCache.getId());
                if (productEntity == null) {
                    continue;
                }

                ItemOfferResponse itemOfferResponse = this.convertItemOfferResponse(promoProductBasic, productCache);
                itemOfferResponse.setPrice(
                        promoProductBasic.getItem_price()
                );
                goodsClaimList.add(itemOfferResponse);

                int stock = this.getTonKho(itemOfferResponse.getId());
                Integer quantity_used = mpProductQuantityUsed.get(itemOfferResponse.getId());
                if (quantity_used == null) {
                    quantity_used = 0;
                }
                int quantity = Math.min(itemOfferResponse.getOffer_value(), Math.max(0, stock - quantity_used));
                if (quantity > 0) {
                    /**
                     * Thêm vào danh sách hàng tặng của đơn
                     */
                    AgencyOrderPromoDetailEntity agencyOrderPromoDetailEntity = this.initAgencyOrderPromoEntity(
                            productEntity,
                            itemOfferResponse.getQuantity_select(),
                            productPrice.getPrice(),
                            productPrice.getMinimum_purchase());


                    if (PromoOfferType.GOODS_OFFER.getKey().equals(itemOfferResponse.getOffer_type())) {
                        agencyOrderPromoDetailEntity.setType(AgencyOrderPromoType.GOODS_BONUS.getId());

                        orderMoneyInfoResponse.setTotal_bonus_goods_quantity(
                                orderMoneyInfoResponse.getTotal_bonus_goods_quantity() +
                                        quantity
                        );
                    } else if (PromoOfferType.GIFT_OFFER.getKey().equals(itemOfferResponse.getOffer_type())) {
                        agencyOrderPromoDetailEntity.setType(AgencyOrderPromoType.GIFT_BONUS.getId());

                        orderMoneyInfoResponse.setTotal_bonus_gift_quantity(
                                orderMoneyInfoResponse.getTotal_bonus_gift_quantity() +
                                        quantity
                        );
                    }

                    agencyOrderPromoDetailEntity.setProduct_total_quantity(quantity);
                    agencyOrderPromoDetailEntity.setAgency_order_id(request.getAgency_id());
                    agencyOrderPromoDetailEntity.setProduct_price(itemOfferResponse.getPrice());
                    agencyOrderPromoDetailEntity.setProduct_total_begin_price(agencyOrderPromoDetailEntity.getProduct_price() * agencyOrderPromoDetailEntity.getProduct_total_quantity() * 1L);
                    agencyOrderPromoDetailEntity.setProduct_total_end_price(agencyOrderPromoDetailEntity.getProduct_total_begin_price());
                    itemOfferResponse.setOffer_value(quantity);
                    goodsClaimList.add(itemOfferResponse);
                    agencyOrderPromoDetailEntityList.add(agencyOrderPromoDetailEntity);
                    mpProductQuantityUsed.put(itemOfferResponse.getId(), quantity_used + quantity);
                }

                if (PromoOfferType.GOODS_OFFER.getKey().equals(itemOfferResponse.getOffer_type())
                        && promoProductBasic.getItem_quantity() - quantity > 0) {
                    long price =
                            ConvertUtils.toLong(productPrice == null ? 0 :
                                    productPrice.getPrice() < 0 ? 0 : productPrice.getPrice());
                    total_refund_price += (promoProductBasic.getItem_quantity() - quantity) * price;
                }
            }

            /**
             * Chiết khấu giảm tiền đơn hàng csbh
             */
            orderMoneyInfoResponse.setTotal_promotion_price_of_order(resultEstimatePromo.getTotalMoneyDiscountOrderOfferByCSBH());

            /**
             * Chiết khấu giảm tiền đơn hàng ctkm
             */
            orderMoneyInfoResponse.setTotal_promotion_order_price_ctkm(resultEstimatePromo.getTotalMoneyDiscountOrderOfferByCTKM());

            /**
             * hoafn tien
             */
            orderMoneyInfoResponse.setTotal_refund_price(total_refund_price);

            /**
             * Tính ưu đãi voucher nếu có
             */
            double max_voucher_price = this.getMaxVoucherPrice(orderMoneyInfoResponse.getTotal_begin_price()
                    - orderMoneyInfoResponse.getTotal_promotion_price_of_product()
                    - orderMoneyInfoResponse.getTong_uu_dai_dam_me()
                    - orderMoneyInfoResponse.getTotal_promotion_price_of_order()
                    - orderMoneyInfoResponse.getTotal_promotion_price()
                    - orderMoneyInfoResponse.getTotal_promotion_order_price_ctkm()
                    - orderMoneyInfoResponse.getTotal_refund_price());
            long total_voucher_price = 0;
            List<JSONObject> voucher_gift_list = new ArrayList<>();
            List<JSONObject> voucher_money_list = new ArrayList<>();
            total_voucher_price = this.convertVoucherData(
                    voucher_money_list,
                    voucher_gift_list,
                    request.getVouchers(),
                    max_voucher_price, mpProductQuantityUsed);
            orderMoneyInfoResponse.setTotal_voucher_price(total_voucher_price);

            /**
             * Tính toán tổng tiền
             */
            orderMoneyInfoResponse.setTotal_end_price(orderMoneyInfoResponse.getTotal_begin_price()
                    - orderMoneyInfoResponse.getTotal_promotion_price_of_product()
                    - orderMoneyInfoResponse.getTong_uu_dai_dam_me()
                    - orderMoneyInfoResponse.getTotal_promotion_price_of_order()
                    - orderMoneyInfoResponse.getTotal_promotion_price()
                    - orderMoneyInfoResponse.getTotal_promotion_order_price_ctkm()
                    - orderMoneyInfoResponse.getTotal_refund_price()
                    + orderMoneyInfoResponse.getTotal_service_fee()
                    - orderMoneyInfoResponse.getTotal_voucher_price());

            if (orderMoneyInfoResponse.getTotal_end_price() < 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_ORDER_TOTAL_END_PRICE_FAIL);
            }

            /**
             * Voucher quà tặng
             */
            for (JSONObject voucher : voucher_gift_list) {
                int voucher_id = ConvertUtils.toInt(voucher.get("id"));
                List<JSONObject> voucherGifts = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(voucher.get("products")), new TypeToken<List<JSONObject>>() {
                        }.getType());
                for (JSONObject jsGift : voucherGifts) {
                    int item_id = ConvertUtils.toInt(jsGift.get("item_id"));
                    int item_quantity = ConvertUtils.toInt(jsGift.get("item_quantity"));
                    ProductCache productCache = this.dataManager.getProductManager().getMpProduct().get(
                            item_id
                    );
                    if (productCache == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    int stock = this.getTonKho(item_id);
                    Integer quantity_used = mpProductQuantityUsed.get(item_id);
                    if (quantity_used == null) {
                        quantity_used = 0;
                    }

                    if (stock - quantity_used < item_quantity) {
                        item_quantity = stock - quantity_used;
                    }

                    if (item_quantity > 0) {
                        ProductEntity productEntity = this.productDB.getProduct(productCache.getId());
                        if (productEntity == null) {
                            continue;
                        }
                        /**
                         * Thêm vào danh sách hàng tặng của đơn
                         */
                        AgencyOrderPromoDetailEntity agencyOrderPromoDetailEntity = this.initAgencyOrderPromoEntity(
                                productEntity,
                                item_quantity,
                                0,
                                productCache.getMinimum_purchase());


                        agencyOrderPromoDetailEntity.setType(AgencyOrderPromoType.GIFT_VOUCHER.getId());

                        orderMoneyInfoResponse.setTotal_bonus_goods_quantity(
                                orderMoneyInfoResponse.getTotal_bonus_goods_quantity() +
                                        item_quantity
                        );

                        agencyOrderPromoDetailEntity.setProduct_total_quantity(item_quantity);
                        agencyOrderPromoDetailEntity.setAgency_order_id(request.getAgency_id());
                        agencyOrderPromoDetailEntity.setProduct_price(agencyOrderPromoDetailEntity.getProduct_price());
                        agencyOrderPromoDetailEntity.setProduct_total_begin_price(agencyOrderPromoDetailEntity.getProduct_price() * agencyOrderPromoDetailEntity.getProduct_total_quantity() * 1L);
                        agencyOrderPromoDetailEntity.setProduct_total_end_price(agencyOrderPromoDetailEntity.getProduct_total_begin_price());
                        agencyOrderPromoDetailEntity.setVoucher_id(voucher_id);
                        agencyOrderPromoDetailEntityList.add(agencyOrderPromoDetailEntity);

                        ItemOfferResponse itemOfferResponse = new ItemOfferResponse();
                        itemOfferResponse.setOffer_type(AgencyOrderPromoType.GIFT_VOUCHER.getKey());
                        itemOfferResponse.setId(item_id);
                        itemOfferResponse.setOffer_value(item_quantity);
                        goodsClaimList.add(itemOfferResponse);
                        mpProductQuantityUsed.put(item_id, quantity_used + item_quantity);
                    }
                }
            }

            /**
             * Kiểm tra tồn kho
             */
            Map<Integer, ProductData> productDataList = this.groupItemOrder(
                    products,
                    goodsClaimList,
                    goodsForSelectList,
                    huntSaleOrderList);
            ClientResponse rsTonKho = this.checkTonKho(new ArrayList<>(productDataList.values()));
            if (rsTonKho.failed()) {
                return rsTonKho;
            }

            /**
             * Thông tin đơn hàng
             */
            AgencyOrderEntity agencyOrderEntity = new AgencyOrderEntity();
            agencyOrderEntity.setType(OrderType.INSTANTLY.getValue());
            agencyOrderEntity.setAgency_id(agencyEntity.getId());
            agencyOrderEntity.setAgency_account_id(null);
            agencyOrderEntity.setMembership_id(agencyEntity.getMembership_id());
            agencyOrderEntity.setAddress_delivery(this.getAddressDelivery(request.getAddress_delivery_id()));
            agencyOrderEntity.setAddress_billing(this.getAddressExportBilling(request.getAddress_export_billing_id()));
            agencyOrderEntity.setRequest_delivery_date(request.getDelivery_time_request() == 0 ? null : new Date(request.getDelivery_time_request()));
            agencyOrderEntity.setNote_internal(request.getNote_internal());

            /**
             * Tổng tiền săn sale
             */
            OrderSummaryInfoResponse huntSaleSummaryInfo = new OrderSummaryInfoResponse();
            huntSaleSummaryInfo.setTotal_begin_price(
                    huntSaleOrderList.stream().reduce(0L, (total, object) -> total + object.getTotal_begin_price(), Long::sum)
            );
            huntSaleSummaryInfo.setTotal_promotion_price(
                    huntSaleOrderList.stream().reduce(0L, (total, object) -> total + object.getTotal_promo_price(), Long::sum)
            );
            huntSaleSummaryInfo.setTotal_end_price(
                    huntSaleOrderList.stream().reduce(0L, (total, object) -> total + object.getTotal_end_price(), Long::sum)
            );
            huntSaleSummaryInfo.setTotal_product_quantity(
                    huntSaleOrderList.stream().reduce(0, (total, object) -> total +
                            object.sumProductTotalQuantity(), Integer::sum)
            );
            huntSaleSummaryInfo.setTotal_bonus_gift_quantity(
                    huntSaleOrderList.stream().reduce(0, (total, object) -> total +
                            object.sumGiftTotalQuantity(), Integer::sum)
            );

            /**
             * Tổng tiền đơn hàng
             */
            agencyOrderEntity.setTotal_begin_price(
                    orderMoneyInfoResponse.getTotal_begin_price()
                            + huntSaleSummaryInfo.getTotal_begin_price()
            );
            agencyOrderEntity.setTotal_promotion_price(
                    orderMoneyInfoResponse.getTotal_promotion_price()
            );
            agencyOrderEntity.setTotal_promotion_product_price(
                    orderMoneyInfoResponse.getTotal_promotion_price_of_product()
            );
            agencyOrderEntity.setTotal_promotion_order_price(
                    orderMoneyInfoResponse.getTotal_promotion_price_of_order()
            );
            agencyOrderEntity.setTotal_promotion_order_price_ctkm(
                    orderMoneyInfoResponse.getTotal_promotion_order_price_ctkm()
            );
            agencyOrderEntity.setTotal_sansale_promotion_price(
                    huntSaleSummaryInfo.getTotal_promotion_price()
            );
            agencyOrderEntity.setTotal_dm_price(
                    orderMoneyInfoResponse.getTong_uu_dai_dam_me())
            ;
            agencyOrderEntity.setTotal_end_price(
                    orderMoneyInfoResponse.getTotal_end_price()
                            + huntSaleSummaryInfo.getTotal_end_price());

            agencyOrderEntity.setTotal_product_quantity(
                    orderMoneyInfoResponse.getTotal_product_quantity()
                            + huntSaleSummaryInfo.getTotal_product_quantity()
            );

            agencyOrderEntity.setTotal_goods_quantity(
                    orderMoneyInfoResponse.getTotal_goods_quantity()
            );

            agencyOrderEntity.setTotal_bonus_goods_quantity(
                    orderMoneyInfoResponse.getTotal_bonus_goods_quantity()
            );

            agencyOrderEntity.setTotal_bonus_gift_quantity(
                    orderMoneyInfoResponse.getTotal_bonus_gift_quantity()
                            + huntSaleSummaryInfo.getTotal_bonus_gift_quantity()
            );


            agencyOrderEntity.setTotal_refund_price(
                    orderMoneyInfoResponse.getTotal_refund_price()
            );
            agencyOrderEntity.setTotal_voucher_price(
                    orderMoneyInfoResponse.getTotal_voucher_price());

            agencyOrderEntity.setTotal(
                    huntSaleOrderList.size() +
                            (orderMoneyInfoResponse.getTotal_begin_price() > 0 ? 1 : 0)
            );

            agencyOrderEntity.setVoucher_info(JsonUtils.Serialize(request.getVouchers()));

            /**
             * Kiểm tra lại giá trị giỏ hàng
             */
            if (request.getEstimate_order_data().getTotal_order() != 0) {
                if (
                        request.getEstimate_order_data().getTotal_order() != agencyOrderEntity.getTotal_end_price()
//                                || request.getEstimate_order_data().getGift_total_quantity() != this.getGiftTotalQuantity(huntSaleOrderList, agencyOrderPromoDetailEntityList)
//                                || request.getEstimate_order_data().getProduct_total_quantity() != this.getProductTotalQuantity(huntSaleOrderList, agencyOrderDetailEntityList)
                ) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TOTAL_ORDER_TOO_LONG_TIME);
                }
            }

            /**
             * Chính sách của sản phẩm
             */
            List<PromoProductInfoData> promoProducts = new ArrayList<>();
            for (Map.Entry<Integer, List<Program>> entry : resultEstimatePromo.getMpPromoProductCSBH().entrySet()) {
                PromoProductInfoData promoProductInfoData = new PromoProductInfoData();
                promoProductInfoData.setProduct_id(entry.getKey());
                for (Program program : entry.getValue()) {
                    PromoOrderData promoOrderData = new PromoOrderData();
                    promoOrderData.setPromo_id(program.getId());
                    promoOrderData.setPromo_name(program.getName());
                    promoOrderData.setPromo_description(
                            this.getCmsProgramDescriptionForProduct(
                                    this.dataManager.getProgramManager().getAgency(agencyEntity.getId()),
                                    entry.getKey(),
                                    program));
                    promoOrderData.setPromo_code(program.getCode());
                    promoProductInfoData.getPromo().add(promoOrderData);
                }
                promoProducts.add(promoProductInfoData);
            }


            agencyOrderEntity.setPromo_product_info(
                    this.convertPromoProductToString(promoProducts));

            /**
             * Chính sách cho đơn hàng
             */
            List<PromoBasicData> csbhOrders = new ArrayList<>();
            for (Program program : resultEstimatePromo.getCsbhOrderList()) {
                PromoBasicData promoBasicData = new PromoBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(program.getDescription());
                csbhOrders.add(promoBasicData);
            }
            agencyOrderEntity.setPromo_order_info(
                    this.convertPromoToString(csbhOrders));

            /**
             * Chính sách đam mê của sản phẩm
             */
            List<PromoProductInfoData> csdmProducts = new ArrayList<>();
            for (Map.Entry<Integer, List<Program>> entry : resultEstimatePromo.getMpPromoProductCSDM().entrySet()) {
                PromoProductInfoData promoProductInfoData = new PromoProductInfoData();
                promoProductInfoData.setProduct_id(entry.getKey());
                for (Program program : entry.getValue()) {
                    PromoOrderData promoOrderData = new PromoOrderData();
                    promoOrderData.setPromo_id(program.getId());
                    promoOrderData.setPromo_name(program.getName());
                    promoOrderData.setPromo_percent(program.getOfferPercent());
                    promoOrderData.setPromo_description(
                            this.getCmsCSDMDescriptionForProduct(
                                    program.getOfferPercent()
                            )
                    );
                    promoOrderData.setPromo_code(program.getCode());
                    promoProductInfoData.getPromo().add(promoOrderData);
                }
                csdmProducts.add(promoProductInfoData);
            }
            agencyOrderEntity.setDm_product_info(
                    this.convertPromoProductToString(csdmProducts));


            Agency agency = this.dataManager.getProgramManager().getAgency(agencyEntity.getId());

            /**
             * CTKM tổng đơn
             */
            List<PromoBasicData> allCtkm = new ArrayList<>();
            /**
             * CTKM của sản phẩm
             */
            List<PromoProductInfoData> ctkmProducts = new ArrayList<>();
            for (Map.Entry<Integer, List<Program>> entry : resultEstimatePromo.getMpPromoProductCTKM().entrySet()) {
                PromoProductInfoData promoProductInfoData = new PromoProductInfoData();
                promoProductInfoData.setProduct_id(entry.getKey());
                for (Program program : entry.getValue()) {
                    PromoOrderData promoOrderData = new PromoOrderData();
                    promoOrderData.setPromo_id(program.getId());
                    promoOrderData.setPromo_name(program.getName());
                    promoOrderData.setPromo_code(program.getCode());
                    promoOrderData.setPromo_description(
                            this.getCmsProgramDescriptionForProduct(
                                    agency,
                                    entry.getKey(),
                                    program));
                    promoProductInfoData.getPromo().add(promoOrderData);

                    PromoBasicData promoBasicData = new PromoBasicData();
                    promoBasicData.setId(promoOrderData.getPromo_id());
                    promoBasicData.setName(promoOrderData.getPromo_name());
                    promoBasicData.setCode(promoOrderData.getPromo_code());
                    promoBasicData.setDescription(promoOrderData.getPromo_description());
                    allCtkm.add(promoBasicData);
                }
                ctkmProducts.add(promoProductInfoData);
            }

            agencyOrderEntity.setPromo_product_info_ctkm(
                    this.convertPromoProductToString(ctkmProducts));

            /**
             * CTKM cua đơn hàng
             */
            List<PromoBasicData> ctkmOrders = new ArrayList<>();
            for (Program program : resultEstimatePromo.getCtkmOrderList()) {
                PromoBasicData promoBasicData = new PromoBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(program.getDescription());
                allCtkm.add(promoBasicData);
                ctkmOrders.add(promoBasicData);
            }
            agencyOrderEntity.setPromo_order_info_ctkm(
                    this.convertPromoToString(ctkmOrders));

            /**
             * CSBH tặng hàng
             */
            List<PromoBasicData> csbhGoods = new ArrayList<>();
            for (Program program : resultEstimatePromo.getCsbhGoodList()) {
                PromoBasicData promoBasicData = new PromoBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(program.getDescription());
                csbhGoods.add(promoBasicData);
            }
            agencyOrderEntity.setPromo_good_offer_info(
                    this.convertPromoToString(csbhGoods));

            /**
             * CTKM tặng hàng
             */
            List<PromoBasicData> ctkmGoods = new ArrayList<>();
            for (Program program : resultEstimatePromo.getCtkmGoodList()) {
                PromoBasicData promoBasicData = new PromoBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(program.getDescription());
                ctkmGoods.add(promoBasicData);
            }
            agencyOrderEntity.setPromo_good_offer_info_ctkm(
                    this.convertPromoToString(ctkmGoods));

            /**
             * tất cả ưu đãi
             */
            List<String> allProgram = new ArrayList<>();
            for (Program program : resultEstimatePromo.getAllProgram()) {
                allProgram.add(ConvertUtils.toString(program.getId()));
            }
            for (HuntSaleOrder huntSaleOrder : huntSaleOrderList) {
                allProgram.add(ConvertUtils.toString(huntSaleOrder.getPromo_id()));
            }
            agencyOrderEntity.setPromo_all_id_info(
                    JsonUtils.Serialize(allProgram));


            /**
             * Lưu thông tin đại lý
             */
            agencyOrderEntity.setAgency_info(JsonUtils.Serialize(new AgencyInfoInListResponse(
                    agencyEntity.getId(),
                    agencyEntity.getCode(),
                    agencyEntity.getShop_name(),
                    agencyEntity.getAvatar()
            )));

            /**
             * Trường hợp của đơn hàng
             */
            List<StuckData> stuckDataList = this.getAllStuckType(agencyOrderEntity, request.getProducts());
            if (stuckDataList == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (!stuckDataList.isEmpty()) {
                agencyOrderEntity.setStuck_type(stuckDataList.get(0).getStuck_type().getId());
                agencyOrderEntity.setStuck_info(this.parseStuckInfoToString(stuckDataList));
            }

            LogUtil.printDebug(JsonUtils.Serialize(agencyOrderEntity));
            double hmkd = this.getAgencyHMKD(
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getDept_limit()),
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getNgd_limit()),
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getCurrent_dept()),
                    this.orderDB.getTotalPriceOrderDoing(request.getAgency_id()));
            agencyOrderEntity.setNqh_order(
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getNqh()));
            double hmkd_over_order = hmkd - agencyOrderEntity.getTotal_end_price();
            agencyOrderEntity.setHmkd_over_order(hmkd_over_order < 0 ? hmkd_over_order * -1.0 : 0.0);

            agencyOrderEntity.setDept_cycle(deptAgencyInfoEntity.getDept_cycle());

            if (oldOrder == null) {
                agencyOrderEntity.setStatus(OrderStatus.DRAFT.getKey());
                /**
                 * Tạo mã đơn
                 */
                String code = this.generateOrderCode(agencyEntity, staff.getDepartment_id());
                if (code.isEmpty()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_ORDER_FAIL);
                }
                agencyOrderEntity.setCode(code);

                /**
                 * Admin tạo
                 * lưu người tạo
                 */
                agencyOrderEntity.setSource(SourceOrderType.CMS.getValue());
                agencyOrderEntity.setCreator_id(sessionData.getId());

                /**
                 * Lưu đơn hàng
                 */
                int rsInsertAgencyOrder = this.orderDB.insertAgencyOrder(agencyOrderEntity);
                if (rsInsertAgencyOrder <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                agencyOrderEntity.setId(rsInsertAgencyOrder);
            } else {
                /**
                 * Lưu đơn hàng chỉnh sửa
                 */
                agencyOrderEntity.setId(oldOrder.getId());
                agencyOrderEntity.setCreator_id(oldOrder.getCreator_id());
                agencyOrderEntity.setCreated_date(oldOrder.getCreated_date());
                agencyOrderEntity.setModifier_id(sessionData.getId());
                agencyOrderEntity.setCode(oldOrder.getCode());
                agencyOrderEntity.setUpdate_status_date(DateTimeUtils.getNow());
                agencyOrderEntity.setStatus(oldOrder.getStatus());
                agencyOrderEntity.setSource(oldOrder.getSource());


                boolean rsUpdateAgencyOrder = this.orderDB.updateAgencyOrder(agencyOrderEntity);
                if (!rsUpdateAgencyOrder) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                this.deleteAgencyOrder(agencyOrderEntity.getId());
            }

            /**
             * Lưu sản phẩm của đơn hàng
             */
            for (AgencyOrderDetailEntity agencyOrderDetailEntity : agencyOrderDetailEntityList) {
                agencyOrderDetailEntity.setAgency_order_id(agencyOrderEntity.getId());
                if (agencyOrderDetailEntity.getProduct_total_quantity() <= 0) {
                    continue;
                }
                int rsInsertDetail = this.orderDB.createAgencyOrderDetail(agencyOrderDetailEntity);
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_ORDER_FAIL);
                }
            }

            /**
             * Lưu hàng tặng của đơn hàng
             */
            for (AgencyOrderPromoDetailEntity agencyOrderPromoDetailEntity : agencyOrderPromoDetailEntityList) {
                agencyOrderPromoDetailEntity.setAgency_order_id(agencyOrderEntity.getId());
                if (agencyOrderPromoDetailEntity.getProduct_total_quantity() <= 0) {
                    continue;
                }
                int rsInsertDetail = this.orderDB.createAgencyOrderPromoDetail(agencyOrderPromoDetailEntity);
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_ORDER_FAIL);
                }
            }

            /**
             * Lưu tổng tiền đơn thường
             */
            if (agencyOrderDetailEntityList.size() > 0) {
                int rsInsertAgencyOrderNormal = this.orderDB.insertAgencyOrderDept(
                        agencyOrderEntity.getId(),
                        AgencyOrderDeptType.NORMAL.getId(),
                        0,
                        deptAgencyInfoEntity.getDept_cycle(),
                        orderMoneyInfoResponse.getTotal_begin_price(),
                        orderMoneyInfoResponse.getTotal_promotion_price(),
                        orderMoneyInfoResponse.getTotal_end_price(),
                        0,
                        "[]",
                        orderMoneyInfoResponse.getTotal_promotion_price_of_product(),
                        orderMoneyInfoResponse.getTotal_promotion_price_of_order(),
                        orderMoneyInfoResponse.getTotal_promotion_order_price_ctkm(),
                        orderMoneyInfoResponse.getTotal_refund_price(),
                        orderMoneyInfoResponse.getTong_uu_dai_dam_me(),
                        orderMoneyInfoResponse.getTotal_voucher_price()
                );
            }

            /**
             * Lưu săn sale
             */

            int total_order = agencyOrderDetailEntityList.size() > 0 ? 1 : 0;
            for (int iHO = 0; iHO < huntSaleOrderList.size(); iHO++) {
                HuntSaleOrder huntSaleOrder = huntSaleOrderList.get(iHO);
                boolean rsSaveHuntSaleOrder = this.saveHuntSaleOrder(
                        agencyOrderEntity.getId(),
                        huntSaleOrder,
                        iHO + 1,
                        agency
                );
            }

            /**
             * Lưu tổng dơn đơn hàng
             */

            boolean rsUpdateTotalOrder = this.orderDB.updateTotalOrder(
                    agencyOrderEntity.getId(),
                    total_order + huntSaleOrderList.size()
            );
            JSONObject data = new JSONObject();
            data.put("id", agencyOrderEntity.getId());
            data.put("code", agencyOrderEntity.getCode());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean saveVoucherUsed(Integer agency_order_id, String voucher_info) {
        try {
            List<Integer> vouchers = JsonUtils.DeSerialize(
                    voucher_info, new TypeToken<List<Integer>>() {
                    }.getType());
            for (Integer voucher_id : vouchers) {
                boolean rs = this.orderDB.saveVoucherUsed(agency_order_id, voucher_id);
                if (!rs) {
                    this.alertToTelegram("saveVoucherUsed-" + voucher_id, ResponseStatus.FAIL);
                }
            }
            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return false;
    }

    private int getProductTotalQuantity(List<HuntSaleOrder> huntSaleOrderList, List<AgencyOrderDetailEntity> agencyOrderDetailEntityList) {
        return huntSaleOrderList.stream().reduce(0, (total, object) -> total + object.sumProductTotalQuantity(), Integer::sum)
                + agencyOrderDetailEntityList.stream().reduce(0, (total, object) -> total + object.getProduct_total_quantity(), Integer::sum);
    }

    private int getGiftTotalQuantity(List<HuntSaleOrder> huntSaleOrderList, List<AgencyOrderPromoDetailEntity> agencyOrderPromoDetailEntityList) {
        return huntSaleOrderList.stream().reduce(0, (total, object) -> total + object.sumGiftTotalQuantity(), Integer::sum)
                + agencyOrderPromoDetailEntityList.stream().reduce(0, (total, object) -> total + object.getProduct_total_quantity(), Integer::sum);
    }

    private ClientResponse deleteAgencyOrder(Integer agency_order_id) {
        try {
            /**
             * Xóa dữ liệu công nợ cũ
             */
            this.orderDB.deleteAgencyOrderDept(agency_order_id);

            /**
             * Xóa dữ liệu sản phẩm cũ
             */

            boolean rsDeleteAgencyOrderDetail = this.orderDB.deleteAgencyOrderDetail(agency_order_id);
            if (!rsDeleteAgencyOrderDetail) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_ORDER_FAIL);
            }

            /**
             * Xóa hàng tặng cũ
             */
            boolean rsDeleteAgencyOrderPromoDetail = this.orderDB.deleteAgencyOrderPromoDetail(
                    agency_order_id
            );
            if (!rsDeleteAgencyOrderPromoDetail) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_ORDER_FAIL);
            }

        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }

        return ClientResponse.success(null);
    }

    private boolean saveHuntSaleOrder(
            Integer agency_order_id,
            HuntSaleOrder huntSaleOrder,
            int order_data_index,
            Agency agency) {
        try {
            PromoBasicData promoBasicData = new PromoBasicData();
            promoBasicData.setId(huntSaleOrder.getPromo_id());
            promoBasicData.setName(huntSaleOrder.getPromo_name());
            promoBasicData.setCode(huntSaleOrder.getPromo_code());
            promoBasicData.setDescription(huntSaleOrder.getPromo_description());
            int rsInsertAgencyOrderDept = this.orderDB.insertAgencyOrderDept(
                    agency_order_id,
                    AgencyOrderDeptType.HUNT_SALE.getId(),
                    huntSaleOrder.getPromo_id(),
                    huntSaleOrder.getDept_cycle(),
                    huntSaleOrder.getTotal_begin_price(),
                    0,
                    huntSaleOrder.getTotal_end_price(),
                    order_data_index,
                    JsonUtils.Serialize(promoBasicData),
                    huntSaleOrder.getTotal_promo_price(),
                    0,
                    0,
                    0,
                    0,
                    0
            );

            /**
             * Lưu sản phẩm
             */
            for (HuntSaleOrderDetail huntSaleOrderDetail : huntSaleOrder.getProducts()) {
                if (huntSaleOrderDetail.getIs_combo() == YesNoStatus.YES.getValue()) {
                    String promo_description = this.getProgramById(
                            huntSaleOrder.getPromo_id()).getMpCombo().get(
                            huntSaleOrderDetail.getId()).getNote();
                    for (ProductData productData : huntSaleOrderDetail.getProducts()) {
                        ProductEntity productEntity = this.dataManager.getProductManager().getProductDB().getProduct(
                                productData.getId()
                        );
                        AgencyOrderDetailEntity agencyOrderDetailEntity = this.initAgencyOrderEntityByHuntSale(
                                productEntity
                        );
                        agencyOrderDetailEntity.setAgency_order_id(
                                agency_order_id
                        );
                        agencyOrderDetailEntity.setProduct_price(
                                productData.getPrice()
                        );
                        agencyOrderDetailEntity.setProduct_minimum_purchase(
                                productEntity.getMinimum_purchase()
                        );
                        agencyOrderDetailEntity.setProduct_total_quantity(
                                huntSaleOrderDetail.getQuantity() * productData.getQuantity()
                        );
                        agencyOrderDetailEntity.setPromo_id(
                                huntSaleOrder.getPromo_id()
                        );
                        agencyOrderDetailEntity.setCombo_id(
                                huntSaleOrderDetail.getId()
                        );
                        agencyOrderDetailEntity.setProduct_total_begin_price(
                                agencyOrderDetailEntity.getProduct_price() * agencyOrderDetailEntity.getProduct_total_quantity()
                        );
                        agencyOrderDetailEntity.setProduct_total_promotion_price(
                                productData.getTotal_promo_price() * huntSaleOrderDetail.getQuantity()
                        );
                        agencyOrderDetailEntity.setProduct_total_end_price(
                                agencyOrderDetailEntity.getProduct_total_begin_price() -
                                        agencyOrderDetailEntity.getProduct_total_promotion_price()
                        );
                        agencyOrderDetailEntity.setCombo_quantity(
                                huntSaleOrderDetail.getQuantity()
                        );
                        agencyOrderDetailEntity.setCombo_data(
                                JsonUtils.Serialize(this.promoDB.getComboInfo(huntSaleOrderDetail.getId()))
                        );
                        agencyOrderDetailEntity.setPromo_description(
                                promo_description.isEmpty() ? huntSaleOrder.getPromo_description() : promo_description
                        );
                        int rsInsertHuntSaleOrderDetail = this.orderDB.createAgencyOrderDetail(
                                agencyOrderDetailEntity
                        );
                    }
                } else {
                    ProductEntity productEntity = this.dataManager.getProductManager().getProductDB().getProduct(
                            huntSaleOrderDetail.getId()
                    );

                    AgencyOrderDetailEntity agencyOrderDetailEntity = this.initAgencyOrderEntityByHuntSale(
                            productEntity
                    );
                    agencyOrderDetailEntity.setAgency_order_id(
                            agency_order_id
                    );
                    agencyOrderDetailEntity.setProduct_price(
                            huntSaleOrderDetail.getPrice()
                    );
                    agencyOrderDetailEntity.setProduct_minimum_purchase(
                            huntSaleOrderDetail.getMinimum_purchase()
                    );
                    agencyOrderDetailEntity.setProduct_total_quantity(
                            huntSaleOrderDetail.getQuantity());
                    agencyOrderDetailEntity.setProduct_total_begin_price(
                            huntSaleOrderDetail.getTotal_begin_price()
                    );
                    agencyOrderDetailEntity.setProduct_total_promotion_price(
                            huntSaleOrderDetail.getTotal_promo_price()
                    );
                    agencyOrderDetailEntity.setProduct_total_end_price(
                            huntSaleOrderDetail.getTotal_end_price()
                    );
                    agencyOrderDetailEntity.setPromo_id(
                            huntSaleOrder.getPromo_id()
                    );
                    agencyOrderDetailEntity.setPromo_id(
                            huntSaleOrder.getPromo_id()
                    );
                    agencyOrderDetailEntity.setCombo_id(0);
                    agencyOrderDetailEntity.setPromo_description(
                            huntSaleOrderDetail.getPromo_description());
                    int rsInsertHuntSaleOrderDetail = this.orderDB.createAgencyOrderDetail(
                            agencyOrderDetailEntity
                    );
                }
            }

            for (HuntSaleOrderDetail huntSaleOrderDetail : huntSaleOrder.getGifts()) {
                ProductEntity productEntity = this.dataManager.getProductManager().getProductDB().getProduct(
                        huntSaleOrderDetail.getId()
                );

                AgencyOrderPromoDetailEntity agencyOrderDetailEntity = this.initAgencyOrderPromoEntityByHuntSale(
                        productEntity
                );
                agencyOrderDetailEntity.setAgency_order_id(
                        agency_order_id
                );
                agencyOrderDetailEntity.setProduct_price(
                        huntSaleOrderDetail.getPrice()
                );
                agencyOrderDetailEntity.setProduct_minimum_purchase(
                        huntSaleOrderDetail.getMinimum_purchase()
                );
                agencyOrderDetailEntity.setProduct_total_quantity(
                        huntSaleOrderDetail.getReal_value());
                agencyOrderDetailEntity.setProduct_total_begin_price(
                        huntSaleOrderDetail.getTotal_begin_price()
                );
                agencyOrderDetailEntity.setProduct_total_promotion_price(
                        huntSaleOrderDetail.getTotal_promo_price()
                );
                agencyOrderDetailEntity.setProduct_total_end_price(
                        huntSaleOrderDetail.getTotal_end_price()
                );
                agencyOrderDetailEntity.setPromo_id(
                        huntSaleOrder.getPromo_id()
                );
                agencyOrderDetailEntity.setPromo_id(
                        huntSaleOrder.getPromo_id()
                );
                agencyOrderDetailEntity.setType(
                        5
                );
                int rsInsertHuntSaleOrderDetail = this.orderDB.createAgencyOrderPromoDetail(
                        agencyOrderDetailEntity
                );
            }

            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }

        return false;
    }

    private String convertPromoProductToStringOld(List<PromoProductInfoData> data) {
        JsonArray arr = new JsonArray();
        try {
            for (PromoProductInfoData promoProductInfoData : data) {
                JsonObject obProgramProductInfo = new JsonObject();
                obProgramProductInfo.addProperty("product_id", promoProductInfoData.getProduct_id());
                JsonArray arrProgram = new JsonArray();

                for (PromoOrderData promoOrderData : promoProductInfoData.getPromo()) {
                    JsonObject obProgram = new JsonObject();
                    obProgram.addProperty("promo_id", promoOrderData.getPromo_id());
                    obProgram.addProperty("promo_code", promoOrderData.getPromo_code());
                    obProgram.addProperty("promo_name", promoOrderData.getPromo_name());
                    obProgram.addProperty("promo_description", promoOrderData.getPromo_description());
                    arrProgram.add(obProgram);
                }
                obProgramProductInfo.add("promo", arrProgram);
                arr.add(obProgramProductInfo);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }

        return arr.toString();
    }

    private String convertPromoToString(List<PromoBasicData> data) {
        JsonArray arr = new JsonArray();
        try {
            for (PromoBasicData promoBasicData : data) {
                JsonObject obProgram = new JsonObject();
                obProgram.addProperty("id", promoBasicData.getId());
                obProgram.addProperty("code", promoBasicData.getCode());
                obProgram.addProperty("name", promoBasicData.getName());
                obProgram.addProperty("description", promoBasicData.getDescription());
                arr.add(obProgram);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return arr.toString();
    }

    private Map<Integer, ProductData> groupItemOrder(
            List<ProductMoneyResponse> products,
            List<ItemOfferResponse> goodsClaimList,
            List<ItemOfferResponse> goodsForSelectList,
            List<HuntSaleOrder> huntSaleOrderList) {
        Map<Integer, ProductData> mpProductData = new ConcurrentHashMap<>();
        for (ProductMoneyResponse product : products) {
            ProductData productData = mpProductData.get(product.getId());
            if (productData == null) {
                productData = new ProductData(
                        product.getId(), product.getFull_name(), product.getQuantity());
                mpProductData.put(productData.getId(), productData);
            } else {
                productData.setQuantity(productData.getQuantity() + product.getQuantity());
                mpProductData.put(productData.getId(), productData);
            }
        }

        for (ItemOfferResponse itemOfferResponse : goodsClaimList) {
            ProductData productData = mpProductData.get(itemOfferResponse.getId());
            if (productData == null) {
                productData = new ProductData(
                        itemOfferResponse.getId(),
                        itemOfferResponse.getName(),
                        itemOfferResponse.getQuantity_select());
                mpProductData.put(productData.getId(), productData);
            } else {
                productData.setQuantity(productData.getQuantity() + itemOfferResponse.getQuantity_select());
                mpProductData.put(productData.getId(), productData);
            }
        }

        for (ItemOfferResponse itemOfferResponse : goodsForSelectList) {
            ProductData productData = mpProductData.get(itemOfferResponse.getId());
            if (productData == null) {
                productData = new ProductData(
                        itemOfferResponse.getId(),
                        itemOfferResponse.getName(),
                        itemOfferResponse.getQuantity_select());
                mpProductData.put(productData.getId(), productData);
            } else {
                productData.setQuantity(productData.getQuantity() + itemOfferResponse.getQuantity_select());
                mpProductData.put(productData.getId(), productData);
            }
        }

        for (HuntSaleOrder huntSaleOrder : huntSaleOrderList) {
            for (HuntSaleOrderDetail huntSaleOrderDetail : huntSaleOrder.getProducts()) {
                if (huntSaleOrderDetail.getIs_combo() == YesNoStatus.YES.getValue()) {
                    for (ProductData productInCombo : huntSaleOrderDetail.getProducts()) {
                        ProductData productData = mpProductData.get(productInCombo.getId());
                        if (productData == null) {
                            productData = new ProductData(
                                    productInCombo.getId(),
                                    productInCombo.getFull_name(),
                                    productInCombo.getQuantity() * huntSaleOrderDetail.getQuantity());
                            mpProductData.put(productData.getId(), productData);
                        } else {
                            productData.setQuantity(
                                    productData.getQuantity() + (productInCombo.getQuantity() * huntSaleOrderDetail.getQuantity()));
                            mpProductData.put(productData.getId(), productData);
                        }
                    }
                } else {
                    ProductData productData = mpProductData.get(huntSaleOrderDetail.getId());
                    if (productData == null) {
                        productData = new ProductData(
                                huntSaleOrderDetail.getId(),
                                huntSaleOrderDetail.getFull_name(),
                                huntSaleOrderDetail.getQuantity());
                        mpProductData.put(productData.getId(), productData);
                    } else {
                        productData.setQuantity(
                                productData.getQuantity() + huntSaleOrderDetail.getQuantity());
                        mpProductData.put(productData.getId(), productData);
                    }
                }
            }

            for (HuntSaleOrderDetail huntSaleOrderDetail : huntSaleOrder.getGifts()) {
                ProductData productData = mpProductData.get(huntSaleOrderDetail.getId());
                if (productData == null) {
                    productData = new ProductData(
                            huntSaleOrderDetail.getId(),
                            huntSaleOrderDetail.getFull_name(),
                            huntSaleOrderDetail.getReal_value());
                    mpProductData.put(productData.getId(), productData);
                } else {
                    productData.setQuantity(
                            productData.getQuantity() + huntSaleOrderDetail.getReal_value());
                    mpProductData.put(productData.getId(), productData);
                }
            }
        }
        return mpProductData;
    }

    private List<ProductData> groupItemOrder(List<JSONObject> products, List<JSONObject> goods) {
        Map<Integer, ProductData> productDataList = new ConcurrentHashMap<>();
        for (JSONObject product : products) {
            ProductData productData = productDataList.get(ConvertUtils.toInt(product.get("product_id")));
            if (productData == null) {
                productData = new ProductData(
                        ConvertUtils.toInt(product.get("product_id")),
                        ConvertUtils.toString(product.get("product_full_name")),
                        ConvertUtils.toInt(product.get("product_total_quantity")));
            } else {
                productData.setQuantity(
                        productData.getQuantity() + ConvertUtils.toInt(product.get("product_total_quantity"))
                );
            }
            productDataList.put(productData.getId(), productData);
        }

        for (JSONObject good : goods) {
            ProductData goodData = productDataList.get(
                    ConvertUtils.toInt(good.get("product_id")));
            if (goodData == null) {
                goodData = new ProductData(
                        ConvertUtils.toInt(good.get("product_id")),
                        ConvertUtils.toString(good.get("product_full_name")),
                        ConvertUtils.toInt(good.get("product_total_quantity"))
                );
            } else {
                goodData.setQuantity(goodData.getQuantity() +
                        ConvertUtils.toInt(good.get("product_total_quantity")));
            }
            productDataList.put(goodData.getId(), goodData);
        }
        return new ArrayList<>(productDataList.values());
    }

    private ProductData getProductDataInList(List<ProductData> productDataList, int id) {
        for (ProductData item : productDataList) {
            if (id == item.getId()) {
                return item;
            }
        }
        return null;
    }


    /**
     * Chuyển hóa trường hợp sang kiểu chuỗi
     *
     * @param stuckDataList
     * @return
     */
    private String parseStuckInfoToString(List<StuckData> stuckDataList) {
        return JsonUtils.Serialize(stuckDataList);
    }

    private List<StuckData> getAllStuckType(AgencyOrderEntity agencyOrderEntity, List<OrderProductRequest> products) {
        List<StuckData> stuckDataList = new ArrayList<>();
        try {
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agencyOrderEntity.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agencyOrderEntity.getAgency_id());
            }


            /**
             * Nợ quá hạn
             */
            if (deptAgencyInfoEntity.getNqh() > 0) {
                stuckDataList.add(new StuckData(StuckType.NQH, StuckType.NQH.getLabel() + " " + this.appUtils.priceFormat(deptAgencyInfoEntity.getNqh())));
            }


            /**
             * VHMKD
             */
            long totalValueOrderDoing = this.orderDB.getTotalPriceOrderDoing(agencyOrderEntity.getAgency_id());
            long hmkd = ConvertUtils.toLong(this.getAgencyHMKD(
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getDept_limit()),
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getNgd_limit()),
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getCurrent_dept()),
                    totalValueOrderDoing));

            if (agencyOrderEntity.getTotal_end_price() > hmkd) {
                /**
                 * KHN = 0
                 */
                if (deptAgencyInfoEntity.getDept_cycle() == 0) {
                    stuckDataList.add(new StuckData(StuckType.KHN_0, StuckType.KHN_0.getLabel()));
                } else {
                    stuckDataList.add(new StuckData(StuckType.V_HMKD, StuckType.V_HMKD.getLabel() + " " +
                            this.appUtils.priceFormat(
                                    Math.abs(hmkd - agencyOrderEntity.getTotal_end_price()))));
                }
            }

            /**
             * Kiểm tra Giá trị tối thiểu
             */
            if (agencyOrderEntity.getTotal_end_price() < dataManager.getProductManager().getGiaTriToiThieu()) {
                stuckDataList.add(new StuckData(StuckType.GTTT, StuckType.GTTT.getLabel()));
            }

            /**
             * Số lượng tối thiểu và bước nhảy
             */
            for (OrderProductRequest orderProductRequest : products) {
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(orderProductRequest.getProduct_id());
                if (productCache == null) {
                    stuckDataList.add(new StuckData(StuckType.SLTT, StuckType.SLTT.getLabel()));
                }

                /**
                 * Check số lượng tối thiêu và bước nhảy
                 */
                ClientResponse clientResponse = this.validateProductStep(
                        orderProductRequest.getProduct_quantity(),
                        productCache.getMinimum_purchase(),
                        productCache.getStep());
                if (clientResponse.failed()) {
                    stuckDataList.add(new StuckData(StuckType.SLTT, "Sản phẩm không thỏa SLTT, BN"));
                    break;
                }
            }

            return stuckDataList;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
            stuckDataList = new ArrayList<>();
        }
        return null;
    }

    private String getAddressExportBilling(int address_id) {
        if (address_id == 0) {
            return null;
        }
        AddressExportBillingEntity rs = this.agencyDB.getAddressExportBillingDetail(address_id);
        if (rs == null) {
            return null;
        }
        return JsonUtils.Serialize(rs);
    }

    private String getAddressDelivery(int address_delivery_id) {
        if (address_delivery_id == 0) {
            return null;
        }
        AgencyAddressDeliveryEntity rs = this.agencyDB.getAddressDeliveryDetail(address_delivery_id);
        if (rs == null) {
            return null;
        }
        return JsonUtils.Serialize(rs);
    }

    /**
     * Kiểm tra trước khi tạo đơn
     *
     * @param agencyEntity
     * @return
     */
    private ClientResponse validateCreateOrder(AgencyEntity agencyEntity) {
        /**
         * Kiểm tra đại lý hợp lệ
         */
        if (agencyEntity == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
        }

        /**
         * Trạng thái được đặt hàng
         */
        if (agencyEntity.getStatus() != AgencyStatus.APPROVED.getValue()
                && agencyEntity.getStatus() != AgencyStatus.LOCK.getValue()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
        }
        return ClientResponse.success(null);
    }

    /**
     * Tạo mã đơn
     *
     * @param agencyEntity
     * @return
     */
    public String generateOrderCode(AgencyEntity agencyEntity, int business_department_id) {
        try {
            BusinessDepartment businessDepartment = this.dataManager.getProductManager().getMpBusinessDepartment().get(business_department_id);
            if (businessDepartment == null) {
                return "";
            }
            /**
             *
             */
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = dateFormat.format(new Date());
            String startDate = currentDate + " 00:00:00";
            String endDate = currentDate + " 23:59:59";
            String agencyCode = StringUtils.leftPad(String.valueOf(agencyEntity.getId()), 2, '0');
            int currentNumberOrder = this.orderDB.countOrderByDate(startDate, endDate, agencyEntity.getId());
            if (currentNumberOrder < 0) {
                return "";
            }
            String business_department_code = businessDepartment.getCode();
            String pre_code = business_department_code + agencyCode;
            String code = this.getOrderCodeByCityAndNextNumberCode(pre_code, (currentNumberOrder + 1));
            return code;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return "";
    }

    /**
     * Chuẩn hóa mã đơn hàng: Ngày + City code + Next number code
     *
     * @param cityCode
     * @param nextNumberOrder
     * @return
     */
    public String getOrderCodeByCityAndNextNumberCode(String cityCode, int nextNumberOrder) {
        String tmp = StringUtils.leftPad(String.valueOf(nextNumberOrder), 4, '0');
        DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
        String date = dateFormat.format(new Date());
        return (date + cityCode + "_" + tmp);
    }


    private AgencyOrderDetailEntity initAgencyOrderEntityByProduct(
            ProductEntity productEntity,
            int product_total_quantity,
            double price,
            int minimum_purchase) {
        AgencyOrderDetailEntity agencyOrderDetailEntity = new AgencyOrderDetailEntity();

        /**
         * id của đơn hàng
         */

        /**
         * id của sản phẩm
         */
        agencyOrderDetailEntity.setProduct_id(productEntity.getId());

        /**
         * tag của product
         */
        agencyOrderDetailEntity.setProduct_tag("");

        /**
         * mã phiên bản
         */
        agencyOrderDetailEntity.setProduct_code(productEntity.getCode());

        /**
         * tên phiên bản đầy đủ
         */
        agencyOrderDetailEntity.setProduct_full_name(productEntity.getFull_name());

        /**
         * thời gian bảo hành
         */
        agencyOrderDetailEntity.setProduct_warranty_time(productEntity.getWarranty_time());
        /**
         * hình ảnh của sản phẩm
         */
        agencyOrderDetailEntity.setProduct_images(productEntity.getImages());

        /**
         * quy cách
         */
        agencyOrderDetailEntity.setProduct_specification(productEntity.getSpecification());

        /**
         * id màu sắc
         */
        agencyOrderDetailEntity.setProduct_color_id(productEntity.getProduct_color_id());

        /**
         * tên màu sắc
         */
        agencyOrderDetailEntity.setProduct_color_name(this.dataManager.getProductManager().getColorName(productEntity.getProduct_color_id()));

        /**
         * đặc điểm
         */
        agencyOrderDetailEntity.setProduct_characteristic(productEntity.getCharacteristic());

        /**
         * mô tả
         */
        agencyOrderDetailEntity.setProduct_description(productEntity.getDescription());

        /**
         * hướng dẫn sử dụng
         */
        agencyOrderDetailEntity.setProduct_user_manual(productEntity.getUser_manual());

        /**
         * thông số kỹ thuật
         */
        agencyOrderDetailEntity.setProduct_technical_data(productEntity.getTechnical_data());

        /**
         * giá sản phẩm
         */
        agencyOrderDetailEntity.setProduct_price(
                price
        );

        /**
         * id của đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_small_unit_id(productEntity.getProduct_small_unit_id());

        /**
         * tên của đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_small_unit_name(this.dataManager.getProductManager().getSmallUnitName(productEntity.getProduct_small_unit_id()));

        /**
         * id của đơn vị lớn
         */
        agencyOrderDetailEntity.setProduct_big_unit_id(productEntity.getProduct_big_unit_id());

        /**
         * tên của đơn vị lớn
         */
        agencyOrderDetailEntity.setProduct_big_unit_name(this.dataManager.getProductManager().getBigUnitName(productEntity.getProduct_big_unit_id()));

        /**
         * tỷ lệ quy đổi ra đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_convert_small_unit_ratio(productEntity.getConvert_small_unit_ratio());

        /**
         * mua tối thiểu
         */
        agencyOrderDetailEntity.setProduct_minimum_purchase(
                minimum_purchase
        );

        /**
         * bước nhảy
         */
        agencyOrderDetailEntity.setProduct_step(productEntity.getStep());

        /**
         * loại hàng hóa: 1-máy móc, 2-phụ tùng
         */
        agencyOrderDetailEntity.setProduct_item_type(productEntity.getItem_type());

        /**
         * giá trị chuyển đổi từ mã, dùng để sort
         */
        agencyOrderDetailEntity.setProduct_sort_data(productEntity.getSort_data());

        /**
         * số lượng sản phẩm
         */
        agencyOrderDetailEntity.setProduct_total_quantity(product_total_quantity);

        /**
         * tổng giá trước khi áp dụng ưu đãi / giảm giá
         */

        /**
         * tổng giá ưu đãi / giảm giá
         */

        /**
         * tổng giá sau khi áp dụng ưu đãi / giảm giá
         */

        /**
         * ngày tạo
         */

        return agencyOrderDetailEntity;
    }

    private AgencyOrderDetailEntity initAgencyOrderEntityByHuntSale(
            ProductEntity productEntity) {
        AgencyOrderDetailEntity agencyOrderDetailEntity = new AgencyOrderDetailEntity();

        /**
         * id của đơn hàng
         */

        /**
         * id của sản phẩm
         */
        agencyOrderDetailEntity.setProduct_id(productEntity.getId());

        /**
         * tag của product
         */
        agencyOrderDetailEntity.setProduct_tag("");

        /**
         * mã phiên bản
         */
        agencyOrderDetailEntity.setProduct_code(productEntity.getCode());

        /**
         * tên phiên bản đầy đủ
         */
        agencyOrderDetailEntity.setProduct_full_name(productEntity.getFull_name());

        /**
         * thời gian bảo hành
         */
        agencyOrderDetailEntity.setProduct_warranty_time(productEntity.getWarranty_time());
        /**
         * hình ảnh của sản phẩm
         */
        agencyOrderDetailEntity.setProduct_images(productEntity.getImages());

        /**
         * quy cách
         */
        agencyOrderDetailEntity.setProduct_specification(productEntity.getSpecification());

        /**
         * id màu sắc
         */
        agencyOrderDetailEntity.setProduct_color_id(productEntity.getProduct_color_id());

        /**
         * tên màu sắc
         */
        agencyOrderDetailEntity.setProduct_color_name(this.dataManager.getProductManager().getColorName(productEntity.getProduct_color_id()));

        /**
         * đặc điểm
         */
        agencyOrderDetailEntity.setProduct_characteristic(productEntity.getCharacteristic());

        /**
         * mô tả
         */
        agencyOrderDetailEntity.setProduct_description(productEntity.getDescription());

        /**
         * hướng dẫn sử dụng
         */
        agencyOrderDetailEntity.setProduct_user_manual(productEntity.getUser_manual());

        /**
         * thông số kỹ thuật
         */
        agencyOrderDetailEntity.setProduct_technical_data(productEntity.getTechnical_data());

        /**
         * id của đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_small_unit_id(productEntity.getProduct_small_unit_id());

        /**
         * tên của đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_small_unit_name(this.dataManager.getProductManager().getSmallUnitName(productEntity.getProduct_small_unit_id()));

        /**
         * id của đơn vị lớn
         */
        agencyOrderDetailEntity.setProduct_big_unit_id(productEntity.getProduct_big_unit_id());

        /**
         * tên của đơn vị lớn
         */
        agencyOrderDetailEntity.setProduct_big_unit_name(this.dataManager.getProductManager().getBigUnitName(productEntity.getProduct_big_unit_id()));

        /**
         * tỷ lệ quy đổi ra đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_convert_small_unit_ratio(productEntity.getConvert_small_unit_ratio());

        /**
         * bước nhảy
         */
        agencyOrderDetailEntity.setProduct_step(productEntity.getStep());

        /**
         * loại hàng hóa: 1-máy móc, 2-phụ tùng
         */
        agencyOrderDetailEntity.setProduct_item_type(productEntity.getItem_type());

        /**
         * giá trị chuyển đổi từ mã, dùng để sort
         */
        agencyOrderDetailEntity.setProduct_sort_data(productEntity.getSort_data());

        /**
         * ngày tạo
         */

        return agencyOrderDetailEntity;
    }

    private AgencyOrderPromoDetailEntity initAgencyOrderPromoEntityByHuntSale(
            ProductEntity productEntity) {
        AgencyOrderPromoDetailEntity agencyOrderDetailEntity = new AgencyOrderPromoDetailEntity();

        /**
         * id của đơn hàng
         */

        /**
         * id của sản phẩm
         */
        agencyOrderDetailEntity.setProduct_id(productEntity.getId());

        /**
         * tag của product
         */
        agencyOrderDetailEntity.setProduct_tag("");

        /**
         * mã phiên bản
         */
        agencyOrderDetailEntity.setProduct_code(productEntity.getCode());

        /**
         * tên phiên bản đầy đủ
         */
        agencyOrderDetailEntity.setProduct_full_name(productEntity.getFull_name());

        /**
         * thời gian bảo hành
         */
        agencyOrderDetailEntity.setProduct_warranty_time(productEntity.getWarranty_time());
        /**
         * hình ảnh của sản phẩm
         */
        agencyOrderDetailEntity.setProduct_images(productEntity.getImages());

        /**
         * quy cách
         */
        agencyOrderDetailEntity.setProduct_specification(productEntity.getSpecification());

        /**
         * id màu sắc
         */
        agencyOrderDetailEntity.setProduct_color_id(productEntity.getProduct_color_id());

        /**
         * tên màu sắc
         */
        agencyOrderDetailEntity.setProduct_color_name(this.dataManager.getProductManager().getColorName(productEntity.getProduct_color_id()));

        /**
         * đặc điểm
         */
        agencyOrderDetailEntity.setProduct_characteristic(productEntity.getCharacteristic());

        /**
         * mô tả
         */
        agencyOrderDetailEntity.setProduct_description(productEntity.getDescription());

        /**
         * hướng dẫn sử dụng
         */
        agencyOrderDetailEntity.setProduct_user_manual(productEntity.getUser_manual());

        /**
         * thông số kỹ thuật
         */
        agencyOrderDetailEntity.setProduct_technical_data(productEntity.getTechnical_data());

        /**
         * id của đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_small_unit_id(productEntity.getProduct_small_unit_id());

        /**
         * tên của đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_small_unit_name(this.dataManager.getProductManager().getSmallUnitName(productEntity.getProduct_small_unit_id()));

        /**
         * id của đơn vị lớn
         */
        agencyOrderDetailEntity.setProduct_big_unit_id(productEntity.getProduct_big_unit_id());

        /**
         * tên của đơn vị lớn
         */
        agencyOrderDetailEntity.setProduct_big_unit_name(this.dataManager.getProductManager().getBigUnitName(productEntity.getProduct_big_unit_id()));

        /**
         * tỷ lệ quy đổi ra đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_convert_small_unit_ratio(productEntity.getConvert_small_unit_ratio());

        /**
         * bước nhảy
         */
        agencyOrderDetailEntity.setProduct_step(productEntity.getStep());

        /**
         * loại hàng hóa: 1-máy móc, 2-phụ tùng
         */
        agencyOrderDetailEntity.setProduct_item_type(productEntity.getItem_type());

        /**
         * giá trị chuyển đổi từ mã, dùng để sort
         */
        agencyOrderDetailEntity.setProduct_sort_data(productEntity.getSort_data());

        /**
         * ngày tạo
         */

        return agencyOrderDetailEntity;
    }

    private AgencyOrderPromoDetailEntity initAgencyOrderPromoEntity(
            ProductEntity productEntity,
            int quantity,
            double price,
            int minimum_purchase) {
        AgencyOrderPromoDetailEntity agencyOrderDetailEntity = new AgencyOrderPromoDetailEntity();

        /**
         * id của đơn hàng
         */

        /**
         * id của sản phẩm
         */
        agencyOrderDetailEntity.setProduct_id(productEntity.getId());

        /**
         * tag của product
         */
        agencyOrderDetailEntity.setProduct_tag("");

        /**
         * mã phiên bản
         */
        agencyOrderDetailEntity.setProduct_code(productEntity.getCode());

        /**
         * tên phiên bản đầy đủ
         */
        agencyOrderDetailEntity.setProduct_full_name(productEntity.getFull_name());

        /**
         * thời gian bảo hành
         */
        agencyOrderDetailEntity.setProduct_warranty_time(productEntity.getWarranty_time());
        /**
         * hình ảnh của sản phẩm
         */
        agencyOrderDetailEntity.setProduct_images(productEntity.getImages());

        /**
         * quy cách
         */
        agencyOrderDetailEntity.setProduct_specification(productEntity.getSpecification());

        /**
         * id màu sắc
         */
        agencyOrderDetailEntity.setProduct_color_id(productEntity.getProduct_color_id());

        /**
         * tên màu sắc
         */
        agencyOrderDetailEntity.setProduct_color_name(this.dataManager.getProductManager().getColorName(productEntity.getProduct_color_id()));

        /**
         * đặc điểm
         */
        agencyOrderDetailEntity.setProduct_characteristic(productEntity.getCharacteristic());

        /**
         * mô tả
         */
        agencyOrderDetailEntity.setProduct_description(productEntity.getDescription());

        /**
         * hướng dẫn sử dụng
         */
        agencyOrderDetailEntity.setProduct_user_manual(productEntity.getUser_manual());

        /**
         * thông số kỹ thuật
         */
        agencyOrderDetailEntity.setProduct_technical_data(productEntity.getTechnical_data());

        /**
         * giá sản phẩm
         */
        agencyOrderDetailEntity.setProduct_price(
                price
        );

        /**
         * id của đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_small_unit_id(productEntity.getProduct_small_unit_id());

        /**
         * tên của đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_small_unit_name(this.dataManager.getProductManager().getSmallUnitName(productEntity.getProduct_small_unit_id()));

        /**
         * id của đơn vị lớn
         */
        agencyOrderDetailEntity.setProduct_big_unit_id(productEntity.getProduct_big_unit_id());

        /**
         * tên của đơn vị lớn
         */
        agencyOrderDetailEntity.setProduct_big_unit_name(this.dataManager.getProductManager().getBigUnitName(productEntity.getProduct_big_unit_id()));

        /**
         * tỷ lệ quy đổi ra đơn vị nhỏ
         */
        agencyOrderDetailEntity.setProduct_convert_small_unit_ratio(productEntity.getConvert_small_unit_ratio());

        /**
         * mua tối thiểu
         */
        agencyOrderDetailEntity.setProduct_minimum_purchase(
                minimum_purchase
        );

        /**
         * bước nhảy
         */
        agencyOrderDetailEntity.setProduct_step(productEntity.getStep());

        /**
         * loại hàng hóa: 1-máy móc, 2-phụ tùng
         */
        agencyOrderDetailEntity.setProduct_item_type(productEntity.getItem_type());

        /**
         * giá trị chuyển đổi từ mã, dùng để sort
         */
        agencyOrderDetailEntity.setProduct_sort_data(productEntity.getSort_data());

        /**
         * số lượng sản phẩm
         */
        agencyOrderDetailEntity.setProduct_total_quantity(quantity);

        /**
         * tổng giá trước khi áp dụng ưu đãi / giảm giá
         */

        /**
         * tổng giá ưu đãi / giảm giá
         */

        /**
         * tổng giá sau khi áp dụng ưu đãi / giảm giá
         */

        /**
         * ngày tạo
         */


        return agencyOrderDetailEntity;
    }

    /**
     * Chỉnh sửa đơn hàng
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse editOrder(SessionData sessionData, EditOrderProductRequest request) {
        try {
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            AgencyOrderEntity agencyOrderEntity = this.orderDB.getAgencyOrderEntity(request.getId());
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            if (!this.dataManager.getStaffManager().checkManageOrder(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(agencyOrderEntity.getAgency_id()),
                    agencyOrderEntity.getStatus()
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            clientResponse = this.validateEditOrder(agencyOrderEntity.getStatus());
            if (clientResponse.failed()) {
                return clientResponse;
            }

            if (agencyOrderEntity.getStatus() == OrderStatus.RESPONSE.getKey() && this.checkOrderHuntSale(agencyOrderEntity.getId())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CANNOT_EDIT_ORDER);
            }

            AgencyEntity agencyEntity = this.agencyDB.getAgencyEntity(agencyOrderEntity.getAgency_id());
            if (agencyEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            int current_order_status = agencyOrderEntity.getStatus();
            request.setAgency_id(agencyOrderEntity.getAgency_id());
            if (OrderStatus.DRAFT.getKey() == current_order_status) {
                ClientResponse crEditOrderDraft = this.editOrderDraft(
                        request,
                        agencyOrderEntity,
                        agencyEntity,
                        sessionData
                );

                if (crEditOrderDraft.failed()) {
                    return crEditOrderDraft;
                }

                return ClientResponse.success(null);
            } else if (OrderStatus.RESPONSE.getKey() == current_order_status) {
                ClientResponse crEditOrderResponse = this.editOrderResponse(
                        request,
                        agencyOrderEntity,
                        agencyEntity,
                        sessionData
                );

                if (crEditOrderResponse.failed()) {
                    return crEditOrderResponse;
                }
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse editOrderDraft(EditOrderProductRequest request,
                                          AgencyOrderEntity agencyOrderEntity,
                                          AgencyEntity agencyEntity,
                                          SessionData sessionData) {
        try {
            return this.saveOrder(
                    agencyEntity,
                    this.createSaveOrderRequest(request),
                    sessionData,
                    agencyOrderEntity,
                    false,
                    agencyOrderEntity.getSource()
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse editOrderResponse(
            EditOrderProductRequest request,
            AgencyOrderEntity oldOrder,
            AgencyEntity agencyEntity,
            SessionData sessionData) {
        try {
            if (oldOrder.getIncrease_dept() == YesNoStatus.YES.getValue()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_ORDER_INCREASE_DEPT_FAILED);
            }

            Map<Integer, ProductCache> mpProductCache = new ConcurrentHashMap<>();
            Map<Integer, ProgramOrderProduct> mpProgramOrderProduct =
                    this.initMpProductHuntSale(
                            agencyEntity,
                            mpProductCache,
                            request.getHunt_sale_products(),
                            new ArrayList<>());

            ClientResponse crCheckVisibility = this.checkVisibilityWhenCreateOrder(
                    agencyEntity.getId(),
                    agencyEntity.getCity_id(),
                    agencyEntity.getRegion_id(),
                    agencyEntity.getMembership_id(),
                    new ArrayList<>(mpProgramOrderProduct.values()));
            if (crCheckVisibility.failed()) {
                return crCheckVisibility;
            }

            Map<Integer, Integer> mpProductQuantityUsed = new ConcurrentHashMap<>();

            /**
             * Khởi tạo dữ liệu tính toán
             */
            OrderSummaryInfoResponse orderMoneyInfoResponse = new OrderSummaryInfoResponse();
            List<ProductMoneyResponse> products = new ArrayList<>();
            List<AgencyOrderDetailEntity> agencyOrderDetailEntityList = new ArrayList<>();

            List<ItemOfferResponse> goodsForSelectList = new ArrayList<>();
            List<ItemOfferResponse> goodsClaimList = new ArrayList<>();


            Map<Integer, ProductCache> mpProductPrice = new ConcurrentHashMap<>();
            Map<Integer, Integer> mpStock = new ConcurrentHashMap<>();
            List<ProductData> productInOrderList = this.getAllProductInOrder(oldOrder.getId());
            Map<Integer, ProgramOrderProduct> mpAllProduct = this.initMpProductHuntSale(
                    agencyEntity,
                    mpProductCache,
                    request.getHunt_sale_products(),
                    new ArrayList<>());
            EstimatePromo estimatePromoHuntSale = new EstimatePromo();


            /**
             * Lấy thông tin sản phẩm và tạo entity
             */
            EstimatePromo estimatePromo = new EstimatePromo();
            for (ProgramOrderProduct orderProductRequest : mpAllProduct.values()) {
                ProductEntity productEntity = this.productDB.getProduct(orderProductRequest.getProductId());
                if (productEntity == null) {
                    continue;
                }

                ProductCache productPrice = mpProductPrice.get(productEntity.getId());
                if (productPrice == null) {
                    productPrice = this.getFinalPriceByAgency(
                            productEntity.getId(),
                            agencyEntity.getId(),
                            agencyEntity.getCity_id(),
                            agencyEntity.getRegion_id(),
                            agencyEntity.getMembership_id()
                    );
                    mpProductPrice.put(productEntity.getId(), productPrice);
                }

                /**
                 * Khởi tạo sản phẩm cho đơn hàng
                 */
                AgencyOrderDetailEntity agencyOrderDetailEntity = this.initAgencyOrderEntityByProduct(
                        productEntity,
                        orderProductRequest.getProductQuantity(),
                        productPrice.getPrice(),
                        productPrice.getMinimum_purchase()
                );

                /**
                 * tính giá
                 */

                agencyOrderDetailEntityList.add(agencyOrderDetailEntity);

                mpProductQuantityUsed.put(
                        orderProductRequest.getProductId(),
                        orderProductRequest.getProductQuantity());
            }

            /**
             * Duyệt từng sản phẩm
             */
            for (AgencyOrderDetailEntity agencyOrderDetailEntity : agencyOrderDetailEntityList) {
                ProductEntity productEntity = this.productDB.getProduct(agencyOrderDetailEntity.getProduct_id());

                ProductMoneyResponse productMoneyResponse = this.productUtils.getProductMoney(
                        agencyEntity,
                        productEntity,
                        agencyOrderDetailEntity.getProduct_total_quantity(),
                        agencyOrderDetailEntity.getProduct_price(),
                        agencyOrderDetailEntity.getProduct_minimum_purchase()
                );

                agencyOrderDetailEntity.setAgency_order_id(request.getAgency_id());
                agencyOrderDetailEntity.setProduct_total_quantity(agencyOrderDetailEntity.getProduct_total_quantity());
                agencyOrderDetailEntity.setProduct_price(productMoneyResponse.getPrice());
                agencyOrderDetailEntity.setProduct_total_begin_price(
                        productMoneyResponse.getPrice() * agencyOrderDetailEntity.getProduct_total_quantity() * 1L);
                agencyOrderDetailEntity.setProduct_total_end_price(agencyOrderDetailEntity.getProduct_total_begin_price());


                /**
                 * Không tính tổng tiền của đơn hàng,
                 * bỏ qua những dòng sản phẩm có giá liên hệ -1
                 */
                if (productMoneyResponse.getPrice() <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_FAIL_BY_PRODUCT_PRICE_CONTACT);
                }
                products.add(productMoneyResponse);

                orderMoneyInfoResponse.setTotal_product_quantity(orderMoneyInfoResponse.getTotal_product_quantity() + agencyOrderDetailEntity.getProduct_total_quantity());

                /**
                 * Đưa vào danh sách sản phẩm tính chính sách
                 */
                estimatePromo.getPromoProductInputList().add(new PromoProductBasic(
                        productMoneyResponse.getId(),
                        productMoneyResponse.getQuantity(),
                        productMoneyResponse.getPrice() * 1L,
                        null));
            }

            /**
             * Tính chính sách
             */
            ResultEstimatePromo resultEstimatePromo = this.estimatePromo(request.getAgency_id(),
                    estimatePromo,
                    request.getGoods(),
                    this.convertListProductInOrderToMapInOrder(productInOrderList),
                    oldOrder.getSource()
            );
            if (resultEstimatePromo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }


            /**
             * Tổng tiền đổi hàng không vượt quá số tiền được tặng
             */
            if (orderMoneyInfoResponse.getTotal_goods_offer_price() < orderMoneyInfoResponse.getTotal_goods_offer_claimed_price()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_GOODS_OFFER_OVERLOAD_PRICE);
            }

            orderMoneyInfoResponse.setTotal_goods_offer_remain_price(
                    orderMoneyInfoResponse.getTotal_goods_offer_price()
                            - orderMoneyInfoResponse.getTotal_goods_offer_claimed_price()
            );
            List<AgencyOrderPromoDetailEntity> agencyOrderPromoDetailEntityList = new ArrayList<>();

            /**
             * Chiết khấu/giảm tiền sản phẩm
             */
            for (AgencyOrderDetailEntity agencyOrderDetailEntity : agencyOrderDetailEntityList) {
                /**
                 * Chiết khấu/Giảm tiền CSBH
                 */
                Double promoProductPriceCSBH = resultEstimatePromo.getMpPromoProductPriceCSBH().get(agencyOrderDetailEntity.getProduct_id());
                if (promoProductPriceCSBH == null) {
                    promoProductPriceCSBH = 0.0;
                }
                agencyOrderDetailEntity.setProduct_total_promotion_price(
                        promoProductPriceCSBH
                );
                orderMoneyInfoResponse.setTotal_promotion_price_of_product(
                        orderMoneyInfoResponse.getTotal_promotion_price_of_product()
                                + promoProductPriceCSBH);

                /**
                 * Chiết khấu/Giảm tiền CTKM
                 */
                Double promoProductPriceCTKM = resultEstimatePromo.getMpPromoProductPriceCTKM().get(agencyOrderDetailEntity.getProduct_id());
                if (promoProductPriceCTKM == null) {
                    promoProductPriceCTKM = 0.0;
                }

                agencyOrderDetailEntity.setProduct_total_promotion_price_ctkm(
                        promoProductPriceCTKM
                );
                orderMoneyInfoResponse.setTotal_promotion_price(
                        orderMoneyInfoResponse.getTotal_promotion_price()
                                + promoProductPriceCTKM);

                /**
                 * Tổng tiền của sản phẩm
                 */
                agencyOrderDetailEntity.setProduct_total_end_price(
                        agencyOrderDetailEntity.getProduct_total_begin_price()
                                - agencyOrderDetailEntity.getProduct_total_promotion_price()
                                - agencyOrderDetailEntity.getProduct_total_promotion_price_ctkm()
                );
            }

            /**
             * Tiền tạm tính của đơn hàng
             */
            orderMoneyInfoResponse.setTotal_begin_price(resultEstimatePromo.getTotal_begin_price());

            /**
             * Danh sách hàng tặng nếu có
             */
            for (PromoProductBasic promoProductBasic : resultEstimatePromo.getPromoGoodsOfferList()) {
                OrderProductRequest goodsSelect = this.getProductInGoodsSelect(promoProductBasic, request.getGoods());

                ProductCache productCache = this.dataManager.getProductManager().getMpProduct().get(promoProductBasic.getItem_id());
                if (productCache == null) {
                    continue;
                }

                ProductCache productPrice = mpProductPrice.get(productCache.getId());
                if (productPrice == null) {
                    productPrice = this.getFinalPriceByAgency(
                            productCache.getId(),
                            agencyEntity.getId(),
                            agencyEntity.getCity_id(),
                            agencyEntity.getRegion_id(),
                            agencyEntity.getMembership_id()
                    );
                    mpProductPrice.put(productCache.getId(), productPrice);
                }

                /**
                 * Không tặng hàng bị ẩn
                 */
                if (VisibilityType.HIDE.getId() == this.getProductVisibilityByAgency(
                        agencyEntity.getId(),
                        promoProductBasic.getItem_id()
                )) {
                    continue;
                }

                ProductEntity productEntity = this.productDB.getProduct(productCache.getId());
                if (productEntity == null) {
                    continue;
                }

                PromoProductBasic orderProductResponse = this.getProductInOrder(estimatePromo.getPromoProductInputList(), promoProductBasic.getItem_id());
                if (orderProductResponse == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                PromoProductBasic goods = new PromoProductBasic(
                        promoProductBasic.getItem_id(),
                        promoProductBasic.getItem_quantity(),
                        orderProductResponse.getItem_price() * 1L,
                        PromoOfferType.GOODS_OFFER.getKey()
                );
                estimatePromo.getPromoGoodsSelectList().add(goods);

                ItemOfferResponse itemOfferResponse = this.convertGoodOfferResponse(promoProductBasic, productCache);
                itemOfferResponse.setPrice(orderProductResponse.getItem_price());
                itemOfferResponse.setQuantity_select(goodsSelect != null ? goodsSelect.getProduct_quantity() : 0);

                int stock = this.getTonKho(itemOfferResponse.getId())
                        + this.getProductQuantityInOrder(productInOrderList, itemOfferResponse.getId());
                Integer quantity_used = mpProductQuantityUsed.get(itemOfferResponse.getId());
                if (quantity_used == null) {
                    quantity_used = 0;
                }

                if (stock - quantity_used < itemOfferResponse.getQuantity_select()) {
                    ClientResponse crTonKho = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.OUT_OF_STOCK);
                    String message = "[" + crTonKho.getMessage() + "] Tồn kho của sản phẩm " +
                            itemOfferResponse.getName() + " chỉ còn " + appUtils.priceFormat(Math.max(0, stock)) + ";";
                    crTonKho.setMessage(message);
                    return crTonKho;
                }

                goodsForSelectList.add(itemOfferResponse);

                /**
                 * Thêm vào danh sách hàng tặng của đơn
                 */
                if (itemOfferResponse != null && itemOfferResponse.getQuantity_select() > 0) {
                    AgencyOrderPromoDetailEntity agencyOrderPromoDetailEntity = this.initAgencyOrderPromoEntity(
                            productEntity,
                            itemOfferResponse.getQuantity_select(),
                            productPrice.getPrice(),
                            productPrice.getMinimum_purchase()
                    );
                    agencyOrderPromoDetailEntity.setAgency_order_id(request.getAgency_id());
                    agencyOrderPromoDetailEntity.setProduct_total_quantity(agencyOrderPromoDetailEntity.getProduct_total_quantity());
                    agencyOrderPromoDetailEntity.setProduct_price(itemOfferResponse.getPrice());
                    agencyOrderPromoDetailEntity.setProduct_total_begin_price(
                            agencyOrderPromoDetailEntity.getProduct_price()
                                    * agencyOrderPromoDetailEntity.getProduct_total_quantity() * 1L);
                    agencyOrderPromoDetailEntity.setProduct_total_end_price(agencyOrderPromoDetailEntity.getProduct_total_begin_price());
                    agencyOrderPromoDetailEntity.setType(AgencyOrderPromoType.GOODS_OFFER.getId());
                    agencyOrderPromoDetailEntityList.add(agencyOrderPromoDetailEntity);
                }

                mpProductQuantityUsed.put(itemOfferResponse.getId(), quantity_used + itemOfferResponse.getQuantity_select());
            }

            orderMoneyInfoResponse.setTotal_goods_offer_price(resultEstimatePromo.getTotalMoneyGoodsOffer());
            orderMoneyInfoResponse.setTotal_goods_offer_claimed_price(

                    goodsForSelectList.stream().reduce(0L, (total, object) -> total +
                            ConvertUtils.toLong((object.getQuantity_select() * object.getPrice())), Long::sum)
            );
            orderMoneyInfoResponse.setTotal_goods_offer_remain_price(
                    orderMoneyInfoResponse.getTotal_goods_offer_price()
                            - orderMoneyInfoResponse.getTotal_goods_offer_claimed_price()
            );

            double total_refund_price = 0;
            /**
             * Giá trị khuyến mãi còn lại có thể đổi thêm hàng tặng, vui lòng chọn thêm hàng tặng
             */
            if (orderMoneyInfoResponse.getTotal_goods_offer_remain_price() > 0) {
                for (ItemOfferResponse itemOfferResponse : goodsForSelectList) {
                    int stock = this.getTonKho(itemOfferResponse.getId())
                            + this.getProductQuantityInOrder(productInOrderList, itemOfferResponse.getId());
                    Integer quantity_used = mpProductQuantityUsed.get(itemOfferResponse.getId());
                    if (quantity_used == null) {
                        quantity_used = 0;
                    }

                    if (orderMoneyInfoResponse.getTotal_goods_offer_remain_price()
                            >= itemOfferResponse.getPrice()) {
                        if (stock - quantity_used > 0) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_GOODS_OFFER_PRODUCT_PRICE_UPPER_PRICE_MIN);
                        }

                        total_refund_price = orderMoneyInfoResponse.getTotal_goods_offer_remain_price();
                    }
                }
            }

            LogUtil.printDebug(JsonUtils.Serialize(orderMoneyInfoResponse));


            /**
             * Tặng hàng/Tặng quà
             */
            for (PromoGiftClaim promoProductBasic : resultEstimatePromo.getPromoGiftClaimList()) {
                ProductCache productCache = this.dataManager.getProductManager().getMpProduct().get(promoProductBasic.getItem_id());
                if (productCache == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ProductCache productPrice = mpProductPrice.get(productCache.getId());
                if (productPrice == null) {
                    productPrice = this.getFinalPriceByAgency(
                            productCache.getId(),
                            agencyEntity.getId(),
                            agencyEntity.getCity_id(),
                            agencyEntity.getRegion_id(),
                            agencyEntity.getMembership_id()
                    );
                    mpProductPrice.put(productCache.getId(), productPrice);
                }

                /**
                 * Không tặng hàng bị ẩn
                 */
                if (VisibilityType.HIDE.getId() == this.getProductVisibilityByAgency(
                        agencyEntity.getId(),
                        promoProductBasic.getItem_id()
                )) {
                    continue;
                }

                ProductEntity productEntity = this.productDB.getProduct(productCache.getId());
                if (productEntity == null) {
                    continue;
                }

                ItemOfferResponse itemOfferResponse = this.convertItemOfferResponse(promoProductBasic, productCache);
                itemOfferResponse.setPrice(promoProductBasic.getItem_price());
                goodsClaimList.add(itemOfferResponse);


                int stock = this.getTonKho(itemOfferResponse.getId())
                        + this.getProductQuantityInOrder(productInOrderList, itemOfferResponse.getId());

                Integer quantity_used = mpProductQuantityUsed.get(itemOfferResponse.getId());
                if (quantity_used == null) {
                    quantity_used = 0;
                }

                int quantity = Math.min(itemOfferResponse.getOffer_value(), Math.max(0, stock - quantity_used));
                if (quantity > 0) {
                    /**
                     * Thêm vào danh sách hàng tặng của đơn
                     */
                    AgencyOrderPromoDetailEntity agencyOrderPromoDetailEntity = this.initAgencyOrderPromoEntity(
                            productEntity,
                            itemOfferResponse.getQuantity_select(),
                            productPrice.getPrice(),
                            productPrice.getMinimum_purchase());
                    agencyOrderPromoDetailEntity.setAgency_order_id(request.getAgency_id());
                    agencyOrderPromoDetailEntity.setProduct_total_quantity(promoProductBasic.getItem_quantity());
                    agencyOrderPromoDetailEntity.setProduct_price(itemOfferResponse.getPrice());
                    agencyOrderPromoDetailEntity.setProduct_total_begin_price(agencyOrderPromoDetailEntity.getProduct_price() * agencyOrderPromoDetailEntity.getProduct_total_quantity() * 1L);
                    agencyOrderPromoDetailEntity.setProduct_total_end_price(agencyOrderPromoDetailEntity.getProduct_total_begin_price());

                    if (PromoOfferType.GOODS_OFFER.getKey().equals(itemOfferResponse.getOffer_type())) {
                        agencyOrderPromoDetailEntity.setType(AgencyOrderPromoType.GOODS_BONUS.getId());
                    } else if (PromoOfferType.GIFT_OFFER.getKey().equals(itemOfferResponse.getOffer_type())) {
                        agencyOrderPromoDetailEntity.setType(AgencyOrderPromoType.GIFT_BONUS.getId());
                    }

                    itemOfferResponse.setOffer_value(quantity);
                    goodsClaimList.add(itemOfferResponse);
                    agencyOrderPromoDetailEntity.setProduct_total_quantity(quantity);
                    agencyOrderPromoDetailEntityList.add(agencyOrderPromoDetailEntity);
                    mpProductQuantityUsed.put(itemOfferResponse.getId(), quantity_used + quantity);
                }

                if (PromoOfferType.GOODS_OFFER.getKey().equals(itemOfferResponse.getOffer_type())
                        && promoProductBasic.getItem_quantity() - quantity > 0) {
                    long price = ConvertUtils.toLong(productPrice == null ? 0 :
                            productPrice.getPrice() < 0 ? 0 : productPrice.getPrice());
                    total_refund_price += (promoProductBasic.getItem_quantity() - quantity) * price;
                }
            }

            /**
             * Chiết khấu giảm tiền đơn hàng csbh
             */
            orderMoneyInfoResponse.setTotal_promotion_price_of_order(resultEstimatePromo.getTotalMoneyDiscountOrderOfferByCSBH());

            /**
             * Chiết khấu giảm tiền đơn hàng ctkm
             */
            orderMoneyInfoResponse.setTotal_promotion_order_price_ctkm(resultEstimatePromo.getTotalMoneyDiscountOrderOfferByCTKM());

            /**
             * hoafn tien
             */
            orderMoneyInfoResponse.setTotal_refund_price(total_refund_price);

            /**
             * Tính toán tổng tiền
             */
            orderMoneyInfoResponse.setTotal_end_price(orderMoneyInfoResponse.getTotal_begin_price()
                    - orderMoneyInfoResponse.getTotal_refund_price()
                    - orderMoneyInfoResponse.getTotal_promotion_price_of_product()
                    - orderMoneyInfoResponse.getTotal_promotion_price_of_order()
                    - orderMoneyInfoResponse.getTotal_promotion_price()
                    - orderMoneyInfoResponse.getTotal_promotion_order_price_ctkm()
                    + orderMoneyInfoResponse.getTotal_service_fee());
            if (orderMoneyInfoResponse.getTotal_end_price() < 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_ORDER_TOTAL_END_PRICE_FAIL);
            }

            /**
             * Kiểm tra tồn kho
             */
            Map<Integer, ProductData> productDataList = this.groupItemOrder(
                    products, goodsClaimList, goodsForSelectList,
                    new ArrayList<>());
            ClientResponse rsTonKho = this.checkTonKhoEdit(productDataList, productInOrderList);
            if (rsTonKho.failed()) {
                return rsTonKho;
            }

            /**
             * Thông tin đơn hàng
             */
            AgencyOrderEntity agencyOrderEntity = new AgencyOrderEntity();
            agencyOrderEntity.setType(OrderType.INSTANTLY.getValue());
            agencyOrderEntity.setAgency_id(agencyEntity.getId());
            agencyOrderEntity.setAgency_account_id(null);
            agencyOrderEntity.setMembership_id(agencyEntity.getMembership_id());
            agencyOrderEntity.setAddress_delivery(this.getAddressDelivery(request.getAddress_delivery_id()));
            agencyOrderEntity.setAddress_billing(this.getAddressExportBilling(request.getAddress_export_billing_id()));
            agencyOrderEntity.setRequest_delivery_date(request.getDelivery_time_request() == 0 ? null : new Date(request.getDelivery_time_request()));
            agencyOrderEntity.setNote_internal(request.getNote_internal());
            agencyOrderEntity.setTotal_begin_price(orderMoneyInfoResponse.getTotal_begin_price());
            agencyOrderEntity.setTotal_promotion_price(orderMoneyInfoResponse.getTotal_promotion_price_of_order());
            agencyOrderEntity.setTotal_promotion_product_price(orderMoneyInfoResponse.getTotal_promotion_price_of_product());
            agencyOrderEntity.setTotal_promotion_order_price(orderMoneyInfoResponse.getTotal_promotion_price_of_order());
            agencyOrderEntity.setTotal_promotion_price(orderMoneyInfoResponse.getTotal_promotion_price());
            agencyOrderEntity.setTotal_promotion_order_price_ctkm(orderMoneyInfoResponse.getTotal_promotion_order_price_ctkm());
            agencyOrderEntity.setTotal_end_price(orderMoneyInfoResponse.getTotal_end_price());
            agencyOrderEntity.setTotal_product_quantity(orderMoneyInfoResponse.getTotal_product_quantity());
            agencyOrderEntity.setTotal_refund_price(orderMoneyInfoResponse.getTotal_refund_price());

            /**
             * Chính sách của sản phẩm
             */
            List<PromoProductInfoData> promoProducts = new ArrayList<>();
            for (Map.Entry<Integer, List<Program>> entry : resultEstimatePromo.getMpPromoProductCSBH().entrySet()) {
                PromoProductInfoData promoProductInfoData = new PromoProductInfoData();
                promoProductInfoData.setProduct_id(entry.getKey());
                for (Program program : entry.getValue()) {
                    PromoOrderData promoOrderData = new PromoOrderData();
                    promoOrderData.setPromo_id(program.getId());
                    promoOrderData.setPromo_name(program.getName());
                    promoOrderData.setPromo_description(
                            this.getCmsProgramDescriptionForProduct(
                                    this.dataManager.getProgramManager().getAgency(agencyEntity.getId()),
                                    entry.getKey(),
                                    program));
                    promoOrderData.setPromo_code(program.getCode());
                    promoProductInfoData.getPromo().add(promoOrderData);
                }
                promoProducts.add(promoProductInfoData);
            }
            agencyOrderEntity.setPromo_product_info(
                    this.convertPromoProductToString(promoProducts));

            /**
             * Chính sách cho đơn hàng
             */
            List<PromoBasicData> csbhOrders = new ArrayList<>();
            for (Program program : resultEstimatePromo.getCsbhOrderList()) {
                PromoBasicData promoBasicData = new PromoBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(program.getDescription());
                csbhOrders.add(promoBasicData);
            }
            agencyOrderEntity.setPromo_order_info(
                    this.convertPromoToString(csbhOrders));

            Agency agency = this.dataManager.getProgramManager().getAgency(agencyEntity.getId());

            /**
             * CTKM tổng đơn
             */
            List<PromoBasicData> allCtkm = new ArrayList<>();
            /**
             * CTKM của sản phẩm
             */
            List<PromoProductInfoData> ctkmProducts = new ArrayList<>();
            for (Map.Entry<Integer, List<Program>> entry : resultEstimatePromo.getMpPromoProductCTKM().entrySet()) {
                PromoProductInfoData promoProductInfoData = new PromoProductInfoData();
                promoProductInfoData.setProduct_id(entry.getKey());
                for (Program program : entry.getValue()) {
                    PromoOrderData promoOrderData = new PromoOrderData();
                    promoOrderData.setPromo_id(program.getId());
                    promoOrderData.setPromo_name(program.getName());
                    promoOrderData.setPromo_code(program.getCode());
                    promoOrderData.setPromo_description(
                            this.getCmsProgramDescriptionForProduct(
                                    agency,
                                    entry.getKey(),
                                    program));
                    promoProductInfoData.getPromo().add(promoOrderData);

                    PromoBasicData promoBasicData = new PromoBasicData();
                    promoBasicData.setId(promoOrderData.getPromo_id());
                    promoBasicData.setName(promoOrderData.getPromo_name());
                    promoBasicData.setCode(promoOrderData.getPromo_code());
                    promoBasicData.setDescription(promoOrderData.getPromo_description());
                    allCtkm.add(promoBasicData);
                }
                ctkmProducts.add(promoProductInfoData);
            }
            agencyOrderEntity.setPromo_product_info_ctkm(
                    this.convertPromoProductToString(ctkmProducts));

            /**
             * CTKM cua đơn hàng
             */
            List<PromoBasicData> ctkmOrders = new ArrayList<>();
            for (Program program : resultEstimatePromo.getCtkmOrderList()) {
                PromoBasicData promoBasicData = new PromoBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(program.getDescription());
                allCtkm.add(promoBasicData);
                ctkmOrders.add(promoBasicData);
            }
            agencyOrderEntity.setPromo_order_info_ctkm(
                    this.convertPromoToString(ctkmOrders));

            /**
             * CSBH tặng hàng
             */
            List<PromoBasicData> csbhGoods = new ArrayList<>();
            for (Program program : resultEstimatePromo.getCsbhGoodList()) {
                PromoBasicData promoBasicData = new PromoBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(program.getDescription());
                csbhGoods.add(promoBasicData);
            }
            agencyOrderEntity.setPromo_good_offer_info(
                    this.convertPromoToString(csbhGoods));

            /**
             * CTKM tặng hàng
             */
            List<PromoBasicData> ctkmGoods = new ArrayList<>();
            for (Program program : resultEstimatePromo.getCtkmGoodList()) {
                PromoBasicData promoBasicData = new PromoBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(program.getDescription());
                ctkmGoods.add(promoBasicData);
            }
            agencyOrderEntity.setPromo_good_offer_info_ctkm(
                    this.convertPromoToString(ctkmGoods));

            /**
             * tất cả ưu đãi
             */
            List<String> allProgram = new ArrayList<>();
            for (Program program : resultEstimatePromo.getAllProgram()) {
                allProgram.add(ConvertUtils.toString(program.getId()));
            }
            agencyOrderEntity.setPromo_all_id_info(
                    JsonUtils.Serialize(allProgram));


            /**
             * Lưu thông tin đại lý
             */
            agencyOrderEntity.setAgency_info(JsonUtils.Serialize(new AgencyInfoInListResponse(
                    agencyEntity.getId(),
                    agencyEntity.getCode(),
                    agencyEntity.getShop_name(),
                    agencyEntity.getAvatar()
            )));

            /**
             * Trường hợp của đơn hàng
             */

            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(request.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_ORDER_FAIL);
            }

            long hmkd = ConvertUtils.toLong(this.getAgencyHMKD(
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getDept_limit()),
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getNgd_limit()),
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getCurrent_dept()),
                    this.orderDB.getTotalPriceOrderDoing(request.getAgency_id())));
            agencyOrderEntity.setNqh_order(
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getNqh()));
            double hmkd_over_order = hmkd -
                    agencyOrderEntity.getTotal_end_price();

            List<StuckData> stuckDataList = this.checkStuckEditOrderResponse(
                    deptAgencyInfoEntity,
                    agencyOrderEntity,
                    products,
                    oldOrder.getTotal_end_price(),
                    request.getId()
            );
            if (!stuckDataList.isEmpty()) {
                ClientResponse crStuck = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                crStuck.setMessage(this.getMessageByStuckList(stuckDataList));
                return crStuck;
            }

            agencyOrderEntity.setHmkd_over_order(hmkd_over_order < 0 ? hmkd_over_order * -1 : 0);


            List<JSONObject> oldProductList = this.orderDB.getListProductInOrder(oldOrder.getId());
            List<JSONObject> oldGoodList = this.orderDB.getListGoodsInOrder(oldOrder.getId());
            /**
             * Lưu đơn hàng chỉnh sửa
             */
            agencyOrderEntity.setId(oldOrder.getId());
            agencyOrderEntity.setCreator_id(oldOrder.getCreator_id());
            agencyOrderEntity.setCreated_date(oldOrder.getCreated_date());
            agencyOrderEntity.setModifier_id(sessionData.getId());
            agencyOrderEntity.setCode(oldOrder.getCode());
            agencyOrderEntity.setUpdate_status_date(DateTimeUtils.getNow());
            agencyOrderEntity.setSource(oldOrder.getSource());
            agencyOrderEntity.setStatus(OrderStatus.PREPARE.getKey());
            agencyOrderEntity.setConfirm_prepare_date(DateTimeUtils.getNow());
            boolean rsUpdateAgencyOrder = this.orderDB.updateAgencyOrder(agencyOrderEntity);
            if (!rsUpdateAgencyOrder) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.updateDeptCycleToOrder(
                    request.getId(),
                    deptAgencyInfoEntity.getDept_cycle(),
                    agencyOrderEntity.getTotal()
            );

            /**
             * Xóa dữ liệu sản phẩm cũ
             */
            if (!oldProductList.isEmpty()) {
                boolean rsDeleteAgencyOrderDetail = this.orderDB.deleteAgencyOrderDetail(agencyOrderEntity.getId());
                if (!rsDeleteAgencyOrderDetail) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_ORDER_FAIL);
                }
            }
            if (!oldGoodList.isEmpty()) {
                boolean rsDeleteAgencyOrderPromoDetail = this.orderDB.deleteAgencyOrderPromoDetail(agencyOrderEntity.getId());
                if (!rsDeleteAgencyOrderPromoDetail) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_ORDER_FAIL);
                }
            }


            /**
             * Lưu sản phẩm của đơn hàng
             */
            for (AgencyOrderDetailEntity agencyOrderDetailEntity : agencyOrderDetailEntityList) {
                agencyOrderDetailEntity.setAgency_order_id(agencyOrderEntity.getId());
                int rsInsertDetail = this.orderDB.createAgencyOrderDetail(agencyOrderDetailEntity);
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_ORDER_FAIL);
                }
            }

            /**
             * Lưu hàng tặng của đơn hàng
             */
            for (AgencyOrderPromoDetailEntity agencyOrderPromoDetailEntity : agencyOrderPromoDetailEntityList) {
                agencyOrderPromoDetailEntity.setAgency_order_id(agencyOrderEntity.getId());
                int rsInsertDetail = this.orderDB.createAgencyOrderPromoDetail(agencyOrderPromoDetailEntity);
                if (rsInsertDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_ORDER_FAIL);
                }
            }

            JSONObject data = new JSONObject();
            data.put("id", agencyOrderEntity.getId());
            data.put("code", agencyOrderEntity.getCode());


            /**
             * Trừ cũ và cộng mới tồn kho
             */

            ClientResponse rsDecrease = this.decreaseWarehouseWaitingShip(
                    productInOrderList,
                    agencyOrderEntity.getCode()
            );
            List<ProductData> productNewOrderList = this.getAllProductInOrder(agencyOrderEntity.getId());
            ClientResponse rsIncrease = this.increaseWarehouseWaitingShip(
                    productNewOrderList,
                    agencyOrderEntity.getCode()
            );

            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void updateDeptCycleToOrder(int agency_order_id, Integer dept_cycle, int total_order_child) {
        try {
            this.orderDB.updateDeptCycleToAgencyOrder(
                    agency_order_id, dept_cycle
            );

            if (total_order_child != 0) {
                this.orderDB.updateDeptCycleToAgencyOrderDept(
                        agency_order_id, dept_cycle, 0
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private Map<Integer, Integer> convertListProductInOrderToMapInOrder(
            List<ProductData> productInOrderList) {
        Map<Integer, Integer> mp = new ConcurrentHashMap<>();
        for (ProductData productData : productInOrderList) {
            mp.put(productData.getId(), productData.getQuantity());
        }
        return mp;
    }

    private List<StuckData> checkStuckEditOrderResponse(
            DeptAgencyInfoEntity deptAgencyInfoEntity,
            AgencyOrderEntity agencyOrderEntity,
            List<ProductMoneyResponse> products,
            double old_total_end_price,
            int order_id
    ) {
        List<StuckData> stuckDataList = new ArrayList<>();

        /**
         * Số lượng tối thiểu và bước nhảy
         */
        for (ProductMoneyResponse product : products) {
            int product_id = product.getId();
            int product_total_quantity = product.getQuantity();
            ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                    product_id);
            if (productCache == null) {
                stuckDataList.add(new StuckData(StuckType.SLTT, StuckType.SLTT.getLabel()));
                continue;
            }

            /**
             * Check số lượng tối thiêu và bước nhảy
             */
            ClientResponse clientResponse = this.validateProductStep(
                    product_total_quantity,
                    productCache.getMinimum_purchase(),
                    productCache.getStep());
            if (clientResponse.failed()) {
                stuckDataList.add(new StuckData(StuckType.SLTT, StuckType.SLTT.getLabel() + ": " + productCache.getFull_name()));
            }
        }

        /**
         * Nợ quá hạn
         */
        if (deptAgencyInfoEntity.getNqh() > 0) {
            JSONObject commit = this.orderDB.getAgencyOrderCommitByOrder(order_id);
            if (commit != null) {
                if (agencyOrderEntity.getCommit_approve_status() != CommitApproveStatus.APPROVED.getId()) {
                    stuckDataList.add(new StuckData(StuckType.NQH_CK, StuckType.NQH_CK.getLabel() + " " + this.appUtils.priceFormat(deptAgencyInfoEntity.getNqh())));
                }
            } else {
                stuckDataList.add(new StuckData(StuckType.NQH, StuckType.NQH.getLabel() + " " + this.appUtils.priceFormat(deptAgencyInfoEntity.getNqh())));
            }
        }

        /**
         * VHMKD
         */
        double total_end_price = agencyOrderEntity.getTotal_end_price();
        double totalOrderDept = this.orderDB.getTotalPriceOrderDeptDoing(agencyOrderEntity.getAgency_id());
        long hmkd_over_current = ConvertUtils.toLong(
                this.getAgencyHMKD(
                        ConvertUtils.toDouble(deptAgencyInfoEntity.getDept_limit()),
                        ConvertUtils.toDouble(deptAgencyInfoEntity.getNgd_limit()),
                        ConvertUtils.toDouble(deptAgencyInfoEntity.getCurrent_dept()),
                        totalOrderDept));
        if (hmkd_over_current + old_total_end_price - agencyOrderEntity.getTotal_end_price() < 0) {
            stuckDataList.add(
                    new StuckData(
                            StuckType.V_HMKD,
                            StuckType.V_HMKD.getLabel() + " " + this.appUtils.priceFormat(Math.abs(hmkd_over_current)),
                            this.appUtils.priceFormat(Math.abs(
                                    hmkd_over_current + old_total_end_price - agencyOrderEntity.getTotal_end_price()))));
        }

        /**
         * Kiểm tra Giá trị tối thiểu
         */
        if (agencyOrderEntity.getTotal_end_price() < dataManager.getProductManager().getGiaTriToiThieu()) {
            stuckDataList.add(new StuckData(StuckType.GTTT, StuckType.GTTT.getLabel()));
        }

        return stuckDataList;
    }

    private int getProductQuantityInOrder(List<ProductData> productInOrderList, int product_id) {
        for (ProductData productData : productInOrderList) {
            if (productData.getId() == product_id) {
                return productData.getQuantity();
            }
        }
        return 0;
    }

    private List<ProductData> getListProductChange(
            List<ProductData> newProductDataList,
            List<ProductData> productDataInOrderList) {
        List<ProductData> productDataList = new ArrayList<>();
        for (ProductData newProductData : newProductDataList) {
            int quantityChange = newProductData.getQuantity();
            for (ProductData oldProductData : productDataInOrderList) {
                if (oldProductData.getId() == newProductData.getId()) {
                    quantityChange =
                            newProductData.getQuantity() -
                                    oldProductData.getQuantity();
                    break;
                }
            }

            if (quantityChange == 0) {
                continue;
            }
            ProductData product = new ProductData(
                    newProductData.getId(),
                    newProductData.getFull_name(),
                    quantityChange);
            productDataList.add(product);
        }

        for (ProductData oldProductData : productDataInOrderList) {
            boolean exist = false;
            for (ProductData productData : newProductDataList) {
                if (oldProductData.getId() == productData.getId()) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                ProductData product = new ProductData(
                        oldProductData.getId(),
                        oldProductData.getFull_name(),
                        oldProductData.getQuantity() * -1);
                productDataList.add(product);
            }
        }

        return productDataList;
    }

    /**
     * Lưu lịch sử đơn hàng
     *
     * @param agencyOrderHistoryEntity
     * @param oldAgencyOrderDetailEntityList
     * @param oldAgencyOrderPromoDetailEntityList
     * @return
     */
    private ClientResponse saveOrderHistory(AgencyOrderHistoryEntity agencyOrderHistoryEntity,
                                            List<AgencyOrderDetailEntity> oldAgencyOrderDetailEntityList,
                                            List<AgencyOrderPromoDetailEntity> oldAgencyOrderPromoDetailEntityList) {
        LogUtil.printDebug(JsonUtils.Serialize(agencyOrderHistoryEntity));
        agencyOrderHistoryEntity.setId(null);
        int rsInsertOrder = this.orderDB.insertAgencyOrderHistory(agencyOrderHistoryEntity);
        if (rsInsertOrder <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_ORDER_FAIL);
        }
        agencyOrderHistoryEntity.setId(rsInsertOrder);
        for (AgencyOrderDetailEntity agencyOrderDetailEntity : oldAgencyOrderDetailEntityList) {
            AgencyOrderDetailHistoryEntity agencyOrderDetailHistoryEntity = new DefaultMapperFactory.Builder()
                    .build()
                    .getMapperFacade()
                    .map(agencyOrderDetailEntity, AgencyOrderDetailHistoryEntity.class);
            LogUtil.printDebug(JsonUtils.Serialize(agencyOrderHistoryEntity));
            agencyOrderDetailHistoryEntity.setId(null);
            agencyOrderDetailHistoryEntity.setAgency_order_history_id(agencyOrderHistoryEntity.getId());
            int rsInsertDetail = this.orderDB.insertAgencyOrderDetailHistory(agencyOrderDetailHistoryEntity);
            if (rsInsertDetail <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_ORDER_FAIL);
            }
        }
        return ClientResponse.success(null);
    }

    private ClientResponse validateProductStep(int product_quantity, int minimum_purchase, int step) {
        int remain = product_quantity - minimum_purchase;
        if (remain < 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_QUANTITY_MINIMUM_INVALID);
        }
        int du = remain % step;
        if (du != 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_QUANTITY_INVALID);
        }
        return ClientResponse.success(null);
    }

    private ClientResponse validateEditOrder(Integer status) {
        if (status == OrderStatus.DRAFT.getKey()
                || status == OrderStatus.RESPONSE.getKey()) {
            return ClientResponse.success(null);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CANNOT_EDIT_ORDER);
    }

    /**
     * Danh sách đơn đặt hàng
     *
     * @param request
     * @return
     */
    public ClientResponse filterPurchaseOrder(SessionData sessionData, FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();

            for (FilterRequest filterRequest : request.getFilters()) {
                if (filterRequest.getKey().equals("membership_id")) {
                    filterRequest.setKey("t_agency.membership_id");
                } else if (filterRequest.getKey().equals("status")) {
                    filterRequest.setKey("t.status");
                }
            }

            this.addFilterOrderData(sessionData, request);

            String query = this.filterUtils.getQuery(FunctionList.LIST_PURCHASE_ORDER, request.getFilters(), request.getSorts());

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
    public ClientResponse getOrderInfo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject data = new JSONObject();
            JSONObject agencyOrder = this.orderDB.getAgencyOrder(request.getId());
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            if (!this.dataManager.getStaffManager().checkManageOrder(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(ConvertUtils.toInt(agencyOrder.get("agency_id"))),
                    ConvertUtils.toInt(agencyOrder.get("status")))) {
                return ClientResponse.fail(ResponseStatus.NOT_PERMISSION, ResponseMessage.USER_FORBIDDEN);
            }

            List<JSONObject> products = new ArrayList<>();
            Map<Integer, HuntSaleOrderDetail> huntSaleProducts = new ConcurrentHashMap<>();
            List<JSONObject> agencyOrderHuntSaleList = new ArrayList<>();
            List<JSONObject> orderProductList = this.orderDB.getListProductInOrder(request.getId());
            for (JSONObject orderProduct : orderProductList) {
                AgencyOrderDetailEntity agencyOrderDetailEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(orderProduct), AgencyOrderDetailEntity.class);
                if (agencyOrderDetailEntity.getPromo_id() == 0) {
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
                    productMoneyResponse.setTotal_csbh_price(
                            agencyOrderDetailEntity.getProduct_total_promotion_price());
                    productMoneyResponse.setTotal_end_price(agencyOrderDetailEntity.getProduct_total_end_price());
                    productMoneyResponse.setUu_dai_dam_me(agencyOrderDetailEntity.getProduct_total_dm_price());
                    productMoneyResponse.setItem_type(agencyOrderDetailEntity.getProduct_item_type());
                    products.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                } else {
                    orderProduct.put("is_gift", 0);
                    agencyOrderHuntSaleList.add(orderProduct);
                }


                HuntSaleOrderDetail huntSaleOrderDetail = huntSaleProducts.get(
                        agencyOrderDetailEntity.getProduct_id()
                );
                if (huntSaleOrderDetail == null) {
                    huntSaleOrderDetail = (
                            this.parseHuntSaleProduct(agencyOrderDetailEntity)
                    );
                } else {
                    huntSaleOrderDetail.setQuantity(
                            huntSaleOrderDetail.getQuantity() +
                                    agencyOrderDetailEntity.getProduct_total_quantity()
                    );
                }
                huntSaleProducts.put(huntSaleOrderDetail.getId(), huntSaleOrderDetail);
            }

            List<JSONObject> gifts = new ArrayList<>();
            List<JSONObject> goods = new ArrayList<>();
            List<JSONObject> orderGoodsList = this.orderDB.getListGoodsInOrder(request.getId());

            for (JSONObject orderProduct : orderGoodsList) {
                AgencyOrderPromoDetailEntity agencyOrderDetailEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(orderProduct), AgencyOrderPromoDetailEntity.class);
                if (agencyOrderDetailEntity.getPromo_id() == 0) {
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
                    productMoneyResponse.setTotal_end_price(agencyOrderDetailEntity.getProduct_total_end_price());
                    if (AgencyOrderPromoType.GOODS_OFFER.getId() == agencyOrderDetailEntity.getType()) {
                        productMoneyResponse.setOffer_type(PromoOfferType.GOODS_OFFER.getKey());
                        productMoneyResponse.setQuantity_select(productMoneyResponse.getQuantity());
                        if (productMoneyResponse.getTotal_end_price() == 0) {
                            productMoneyResponse.setTotal_end_price(productMoneyResponse.getTotal_begin_price());
                        }
                        goods.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                    } else if (AgencyOrderPromoType.GOODS_BONUS.getId() == agencyOrderDetailEntity.getType()) {
                        productMoneyResponse.setOffer_type(PromoOfferType.GOODS_OFFER.getKey());
                        gifts.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                    } else if (AgencyOrderPromoType.GIFT_BONUS.getId() == agencyOrderDetailEntity.getType()) {
                        productMoneyResponse.setOffer_type(PromoOfferType.GIFT_OFFER.getKey());
                        gifts.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                    }
                } else {
                    orderProduct.put("is_gift", 1);
                    agencyOrderHuntSaleList.add(orderProduct);
                }
            }

            List<JSONObject> orderFlow = this.getOrderFlow(agencyOrder);

            agencyOrder.put("total_promotion_price_of_product", agencyOrder.get("total_promotion_product_price"));
            agencyOrder.put("total_promotion_price_of_order", agencyOrder.get("total_promotion_order_price"));


            /**
             * Chính sách cho sản phẩm
             */
            List<PromoProductBasicData> promoProductList = new ArrayList<>();
            if (agencyOrder.get("promo_product_info") != null) {
                List<PromoProductInfoData> promoProductInfoDataList = this.convertPromoProductToList(
                        agencyOrder.get("promo_product_info").toString()
                );

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
             * Chính sách đam mê cho sản phẩm
             */
            Map<Integer, PromoBasicData> csdmOrders = new HashMap<>();
            if (agencyOrder.get("dm_product_info") != null) {
                List<PromoProductInfoData> promoProductInfoDataList = this.convertPromoProductToList(
                        agencyOrder.get("dm_product_info").toString()
                );

                for (PromoProductInfoData promoProductInfoData : promoProductInfoDataList) {
                    for (PromoOrderData promoOrderData : promoProductInfoData.getPromo()) {
                        PromoProductBasicData promoProductBasicData = new PromoProductBasicData();
                        promoProductBasicData.setProduct_id(promoProductInfoData.getProduct_id());
                        promoProductBasicData.setId(promoOrderData.getPromo_id());
                        promoProductBasicData.setName(promoOrderData.getPromo_name());
                        promoProductBasicData.setCode(promoOrderData.getPromo_code());
                        promoProductBasicData.setDescription(
                                this.getCmsCSDMDescriptionForProduct(
                                        promoOrderData.getPromo_percent()
                                )
                        );
                        promoProductList.add(promoProductBasicData);

                        PromoBasicData promoBasicData = new PromoBasicData();
                        promoBasicData.setId(promoProductBasicData.getId());
                        promoBasicData.setName(promoProductBasicData.getName());
                        promoBasicData.setCode(promoProductBasicData.getCode());
                        promoBasicData.setDescription(promoProductBasicData.getDescription());
                        csdmOrders.put(promoBasicData.getId(), promoBasicData);
                    }
                }
            }

            /**
             * Chính sách cho đơn hàng
             */
            List<PromoBasicData> promoOrderList = new ArrayList<>();
            if (agencyOrder.get("promo_order_info") != null) {
                List<PromoBasicData> promoOrderDataList = this.convertPromoToList(
                        agencyOrder.get("promo_order_info").toString());

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
            long hmkd = ConvertUtils.toLong(this.getAgencyHMKD(
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getDept_limit()),
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getNgd_limit()),
                    ConvertUtils.toDouble(deptAgencyInfoEntity.getCurrent_dept()),
                    totalPriceOrderDoingDept));
            orderDeptInfo.setHmkd(hmkd);
            orderDeptInfo.setCno(deptAgencyInfoEntity.getCurrent_dept());
            long hmkd_over_order = ConvertUtils.toLong(agencyOrder.get("hmkd_over_order"));
            orderDeptInfo.setHmkd_over_order(
                    hmkd_over_order);
            orderDeptInfo.setDept_cycle(
                    agencyOrder.get("dept_cycle") != null ?
                            ConvertUtils.toInt(agencyOrder.get("dept_cycle")) :
                            deptAgencyInfoEntity.getDept_cycle()
            );
            long total_end_price = ConvertUtils.toLong(agencyOrder.get("total_end_price"));
            double totalOrderDept = this.orderDB.getTotalPriceOrderDeptDoing(agency_id);
            long hmkd_over_current = this.getVuotHMKDCurrent(
                    ConvertUtils.toInt(agencyOrder.get("status")),
                    deptAgencyInfoEntity.getDept_limit(),
                    deptAgencyInfoEntity.getNgd_limit(),
                    deptAgencyInfoEntity.getCurrent_dept(),
                    total_end_price,
                    ConvertUtils.toLong(totalOrderDept));

            if (ConvertUtils.toInt(agencyOrder.get("total")) != 0) {
                JSONObject orderNormalDept = this.orderDB.getOrderNormalDept(
                        request.getId()
                );
                if (orderNormalDept != null) {
                    agencyOrder.put("total_begin_price", orderNormalDept.get("total_begin_price"));
                    agencyOrder.put("total_promotion_price", orderNormalDept.get("total_promotion_price"));
                    agencyOrder.put("total_promotion_product_price", orderNormalDept.get("total_promotion_product_price"));
                    agencyOrder.put("total_promotion_order_price", orderNormalDept.get("total_promotion_order_price"));
                    agencyOrder.put("total_promotion_order_price_ctkm", orderNormalDept.get("total_promotion_order_price_ctkm"));
                    agencyOrder.put("total_end_price", orderNormalDept.get("total_end_price"));
                    agencyOrder.put("tong_uu_dai_dam_me", orderNormalDept.get("total_dm_price"));
                } else {
                    agencyOrder.put("total_begin_price", 0);
                    agencyOrder.put("total_promotion_price", 0);
                    agencyOrder.put("total_promotion_product_price", 0);
                    agencyOrder.put("total_promotion_order_price", 0);
                    agencyOrder.put("total_promotion_order_price_ctkm", 0);
                    agencyOrder.put("total_end_price", 0);
                    agencyOrder.put("tong_uu_dai_dam_me", 0);
                }
            }

            /**
             * Chính sách cho đơn hàng
             */
            List<PromoBasicData> ctkmOrders = new ArrayList<>();
            Map<Integer, PromoBasicData> mpCtkmOrder = new ConcurrentHashMap<>();
            if (agencyOrder.get("promo_product_info_ctkm") != null) {
                List<PromoProductInfoData> promoProductInfoDataList = this.convertPromoProductToList(
                        agencyOrder.get("promo_product_info_ctkm").toString());

                for (PromoProductInfoData promoProductInfoData : promoProductInfoDataList) {
                    for (PromoOrderData promoOrderData : promoProductInfoData.getPromo()) {
                        PromoBasicData promoProductBasicData = new PromoBasicData();
                        promoProductBasicData.setId(promoOrderData.getPromo_id());
                        promoProductBasicData.setName(promoOrderData.getPromo_name());
                        promoProductBasicData.setCode(promoOrderData.getPromo_code());
                        promoProductBasicData.setDescription(promoOrderData.getPromo_description());
                        mpCtkmOrder.put(promoProductBasicData.getId(), promoProductBasicData);


                        PromoProductBasicData promoProductCtkm = new PromoProductBasicData();
                        promoProductCtkm.setProduct_id(promoProductInfoData.getProduct_id());
                        promoProductCtkm.setId(promoOrderData.getPromo_id());
                        promoProductCtkm.setName(promoOrderData.getPromo_name());
                        promoProductCtkm.setCode(promoOrderData.getPromo_code());
                        promoProductCtkm.setDescription(promoOrderData.getPromo_description());
                        promoProductList.add(promoProductCtkm);
                    }
                }
            }

            if (agencyOrder.get("promo_order_info_ctkm") != null) {
                List<PromoBasicData> promoOrderDataList = this.convertPromoToList(
                        agencyOrder.get("promo_order_info_ctkm").toString());

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
            Map<Integer, PromoBasicData> mpPromoGood = new ConcurrentHashMap<>();
            if (agencyOrder.get("promo_product_info") != null) {
                List<PromoProductInfoData> promoProductInfoDataList = this.convertPromoProductToList(
                        agencyOrder.get("promo_product_info").toString());

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
                List<PromoBasicData> promoOrderDataList = this.convertPromoToList(
                        agencyOrder.get("promo_product_info_ctkm").toString());

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

            orderDeptInfo.setHmkd_over_current(hmkd_over_current < 0 ? hmkd_over_current * -1 : 0);
            orderDeptInfo.setNqh_order(ConvertUtils.toLong(agencyOrder.get("nqh_order")));
            orderDeptInfo.setNqh_current(deptAgencyInfoEntity.getNqh());
            orderDeptInfo.setNgd_limit(deptAgencyInfoEntity.getNgd_limit());

            /**
             * nếu cam kết
             */
            int stuck_type = ConvertUtils.toInt(agencyOrder.get("stuck_type"));
            String stuck_info = ConvertUtils.toString(agencyOrder.get("stuck_info"));
            stuck_info = this.getStuckInfoByStuckInfo(stuck_info, stuck_type);


            JSONObject agency_order_commit = this.orderDB.getAgencyOrderCommitByOrder(request.getId());
            if (agency_order_commit != null) {
                orderDeptInfo.setCommitted_date(ConvertUtils.toString(agency_order_commit.get("committed_date")));
                orderDeptInfo.setCommitted_money(ConvertUtils.toLong(agency_order_commit.get("committed_money")));

                stuck_info += ": " + appUtils.priceFormat(orderDeptInfo.getCommitted_money()) +
                        " - ngày cam kết: " + DateTimeUtils.toString(
                        DateTimeUtils.getDateTime(orderDeptInfo.getCommitted_date(), "yyyy-MM-dd"), "dd-MM-yyyy");
            }

            agencyOrder.put("agency_info", JsonUtils.Serialize(
                    this.dataManager.getAgencyManager().getAgencyBasicData(agency_id))
            );

            long total_promotion_price = ConvertUtils.toLong(agencyOrder.get("total_promotion_price"));
            long total_promotion_order_price_ctkm = ConvertUtils.toLong(agencyOrder.get("total_promotion_order_price_ctkm"));
            agencyOrder.put("total_promotion_price",
                    total_promotion_price
                            + total_promotion_order_price_ctkm);

            int order_status = ConvertUtils.toInt(agencyOrder.get("status"));
            if (order_status == OrderStatus.DRAFT.getKey() ||
                    order_status == OrderStatus.WAITING_APPROVE.getKey()) {
                for (JSONObject js : products) {
                    ProductMoneyResponse productMoneyResponse = JsonUtils.DeSerialize(JsonUtils.Serialize(js), ProductMoneyResponse.class);
                    ClientResponse crValidate = this.validateProductStep(productMoneyResponse.getQuantity(), productMoneyResponse.getMinimum_purchase(), productMoneyResponse.getStep());
                    if (crValidate.failed()) {
                        js.put("is_error", 1);
                        js.put("note", "Sản phẩm không thỏa SLTT " + productMoneyResponse.getMinimum_purchase() + " - BN " + productMoneyResponse.getStep() + ";");
                    }
                }
            }

            agencyOrder.put("creator_info",
                    SourceOrderType.APP.getValue() == ConvertUtils.toInt(agencyOrder.get("source")) ? null :
                            this.dataManager.getStaffManager().getStaff(
                                    ConvertUtils.toInt(agencyOrder.get("creator_id"))
                            ));

            int locked = ConvertUtils.toInt(agencyOrder.get("locked"));
            Date update_status_date = DateTimeUtils.getDateTime(ConvertUtils.toString(
                    agencyOrder.get("update_status_date")));
            if (ConvertUtils.toInt(agencyOrder.get("source")) == SourceOrderType.APP.getValue()
                    && (order_status == OrderStatus.WAITING_CONFIRM.getKey() ||
                    order_status == OrderStatus.PREPARE.getKey())
                    && locked == 0) {
                Date time_lock = this.appUtils.getTimeLock(
                        update_status_date, this.dataManager.getConfigManager().getTimeLock());
                agencyOrder.put("time_lock", time_lock.getTime());
            } else {
                agencyOrder.put("time_lock", update_status_date.getTime());
            }

            if (products.size() > 0) {
                agencyOrder.put("dept_code", this.appUtils.getAgencyOrderDeptCode(
                        ConvertUtils.toString(agencyOrder.get("code")),
                        0,
                        ConvertUtils.toInt(agencyOrder.get("total")),
                        false,
                        true)
                );
            }

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
            data.put("csdm_orders", csdmOrders.values());
            data.put("stuck_info", stuck_info);

            List<HuntSaleOrder> huntSaleOrderList = this.convertHuntSaleOrder(
                    ConvertUtils.toString(agencyOrder.get("code")),
                    this.orderDB.getListAgencyOrderDeptHuntSale(request.getId()),
                    agencyOrderHuntSaleList,
                    ConvertUtils.toInt(agencyOrder.get("total"))
            );

            data.put("hunt_sale_orders", huntSaleOrderList);
            long total_money_order =
                    ConvertUtils.toLong(agencyOrder.get("total_end_price"))
                            + huntSaleOrderList.stream().reduce(0L, (total, object) -> total + object.getTotal_end_price(), Long::sum);
            data.put("total_money_order", total_money_order);
            data.put("hunt_sale_products", new ArrayList<>(huntSaleProducts.values()));

            /**
             * Thông tin giao hàng
             */
            data.put("order_delivery_info", this.getTotalOrderDeliveryInfo(request.getId()));

            /**
             * Thông tin voucher
             */
            if (!(order_status == OrderStatus.DRAFT.getKey() || order_status == OrderStatus.RETURN_AGENCY.getKey())) {
                data.put("voucher_gift", this.convertVoucherGiftData(request.getId()));
                data.put("voucher_money", this.convertVoucherMoneyData(request.getId()));
            } else {
                List<Integer> voucher_info = (agencyOrder.get("voucher_info") == null ||
                        agencyOrder.get("voucher_info").toString().isEmpty()) ? new ArrayList<>() :
                        JsonUtils.DeSerialize(agencyOrder.get("voucher_info").toString(), new TypeToken<List<Integer>>() {
                        }.getType());
                data.put("voucher_gift", this.convertVoucherGiftDataDraft(request.getId(), voucher_info));
                data.put("voucher_money", this.convertVoucherMoneyDataDraft(request.getId(), voucher_info));
            }
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<JSONObject> convertVoucherMoneyData(int agency_order_id) {
        try {
            return this.orderDB.getListVoucherMoneyByAgencyOrder(agency_order_id);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return null;
    }

    private List<JSONObject> convertVoucherGiftData(int agency_order_id) {
        try {
            Map<Integer, JSONObject> mpVoucher = new HashMap<>();
            List<JSONObject> voucherUseGiftList = this.orderDB.getListVoucherUseGift(agency_order_id);
            List<JSONObject> voucherGiftList = this.orderDB.getListVoucherGift(agency_order_id);
            for (JSONObject jsVoucher : voucherUseGiftList) {
                List<JSONObject> giftList = JsonUtils.DeSerialize(
                        jsVoucher.get("items").toString(),
                        new TypeToken<List<JSONObject>>() {
                        }.getType());
                for (JSONObject giftData : giftList) {
                    int item_id = ConvertUtils.toInt(giftData.get("item_id"));
                    giftData.put("product_info", this.dataManager.getProductManager().getProductBasicData(item_id));
                    giftData.put("id", item_id);
                    giftData.put("offer_value", giftData.get("item_quantity"));

                    int product_total_quantity = 0;
                    for (JSONObject gift : voucherGiftList) {
                        OrderVoucherGiftData orderVoucherGiftData =
                                JsonUtils.DeSerialize(JsonUtils.Serialize(gift), OrderVoucherGiftData.class);
                        if (item_id == orderVoucherGiftData.getProduct_id()) {
                            product_total_quantity = orderVoucherGiftData.getProduct_total_quantity();
                            break;
                        }
                    }
                    giftData.put("product_total_quantity", product_total_quantity);
                }
                jsVoucher.put("products", giftList);
            }
            return voucherUseGiftList;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return null;
    }

    private List<JSONObject> convertVoucherGiftDataDraft(int agency_order_id, List<Integer> vouchers) {
        try {
            List<JSONObject> voucherUseGiftList = new ArrayList<>();
            List<JSONObject> voucherGiftList = this.orderDB.getListVoucherGift(agency_order_id);
            for (Integer voucherId : vouchers) {
                JSONObject jsVoucher = this.orderDB.getVoucher(voucherId);
                if (jsVoucher == null) {
                    continue;
                }
                if (VoucherOfferType.MONEY_DISCOUNT.getKey().equals(ConvertUtils.toString(jsVoucher.get("offer_type")))) {
                    continue;
                }
                List<JSONObject> giftList = JsonUtils.DeSerialize(
                        jsVoucher.get("items").toString(),
                        new TypeToken<List<JSONObject>>() {
                        }.getType());
                for (JSONObject giftData : giftList) {
                    int item_id = ConvertUtils.toInt(giftData.get("item_id"));
                    giftData.put("product_info", this.dataManager.getProductManager().getProductBasicData(item_id));
                    giftData.put("id", item_id);
                    giftData.put("offer_value", giftData.get("item_quantity"));

                    int product_total_quantity = 0;
                    for (JSONObject gift : voucherGiftList) {
                        OrderVoucherGiftData orderVoucherGiftData =
                                JsonUtils.DeSerialize(JsonUtils.Serialize(gift), OrderVoucherGiftData.class);
                        if (item_id == orderVoucherGiftData.getProduct_id()) {
                            product_total_quantity = orderVoucherGiftData.getProduct_total_quantity();
                            break;
                        }
                    }
                    giftData.put("product_total_quantity", product_total_quantity);
                }
                jsVoucher.put("products", giftList);
                voucherUseGiftList.add(jsVoucher);
            }
            return voucherUseGiftList;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return null;
    }

    private List<JSONObject> convertVoucherMoneyDataDraft(int agency_order_id, List<Integer> vouchers) {
        try {
            List<JSONObject> result = new ArrayList<>();
            for (Integer voucherId : vouchers) {
                JSONObject jsVoucher = this.orderDB.getVoucher(voucherId);
                if (jsVoucher == null) {
                    continue;
                }
                if (VoucherOfferType.GIFT_OFFER.getKey().equals(ConvertUtils.toString(jsVoucher.get("offer_type")))) {
                    continue;
                }
                result.add(jsVoucher);
            }
            return result;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return null;
    }

    private HuntSaleOrderDetail parseHuntSaleProduct(AgencyOrderDetailEntity agencyOrderDetailEntity) {
        HuntSaleOrderDetail productMoneyResponse = new HuntSaleOrderDetail();
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
        productMoneyResponse.setTotal_end_price(agencyOrderDetailEntity.getProduct_total_end_price());
        productMoneyResponse.setItem_type(agencyOrderDetailEntity.getProduct_item_type());
        return productMoneyResponse;
    }

    private List<HuntSaleOrder> convertHuntSaleOrder(
            String agency_order_code,
            List<JSONObject> agencyOrderDeptList,
            List<JSONObject> agencyOrderHuntSaleList,
            int total) {
        List<HuntSaleOrder> hunt_sale_orders = new ArrayList<>();
        try {
            for (JSONObject agencyOrderDept : agencyOrderDeptList) {
                int promo_id = ConvertUtils.toInt(agencyOrderDept.get("promo_id"));
                JSONObject jsPromo = JsonUtils.DeSerialize(
                        agencyOrderDept.get("promo_info").toString(),
                        JSONObject.class
                );

                HuntSaleOrder huntSaleOrder = new HuntSaleOrder();
                if (jsPromo != null) {
                    huntSaleOrder.setPromo_id(
                            ConvertUtils.toInt(jsPromo.get("id"))
                    );
                    huntSaleOrder.setPromo_code(
                            ConvertUtils.toString(jsPromo.get("code"))
                    );
                    huntSaleOrder.setPromo_name(
                            ConvertUtils.toString(jsPromo.get("name"))
                    );
                    huntSaleOrder.setPromo_description(
                            ConvertUtils.toString(jsPromo.get("description"))
                    );
                    huntSaleOrder.setDept_cycle(
                            ConvertUtils.toInt(agencyOrderDept.get("dept_cycle"))
                    );
                }

                Map<Integer, HuntSaleOrderDetail> mpCombo = new ConcurrentHashMap<>();
                for (JSONObject agencyOrderHuntSale : agencyOrderHuntSaleList) {
                    int is_gift = ConvertUtils.toInt(agencyOrderHuntSale.get("is_gift"));
                    int combo_id = ConvertUtils.toInt(agencyOrderHuntSale.get("combo_id"));
                    int combo_quantity = ConvertUtils.toInt(agencyOrderHuntSale.get("combo_quantity"));
                    if (is_gift == 0) {
                        AgencyOrderHuntSaleEntity agencyOrderHuntSaleEntity = AgencyOrderHuntSaleEntity.from(
                                agencyOrderHuntSale
                        );
                        if (agencyOrderHuntSaleEntity.getPromo_id() == huntSaleOrder.getPromo_id()) {
                            if (combo_id != 0) {
                                HuntSaleOrderDetail huntSaleCombo = mpCombo.get(combo_id);
                                if (huntSaleCombo == null) {
                                    huntSaleCombo = this.convertHuntSaleCombo(
                                            combo_id,
                                            combo_quantity
                                    );

                                    huntSaleCombo.setPromo_description(ConvertUtils.toString(agencyOrderHuntSale.get("promo_description")));
                                }

                                ProductData productData = new ProductData();
                                productData.setId(ConvertUtils.toInt(agencyOrderHuntSale.get("product_id")));
                                productData.setCode(ConvertUtils.toString(agencyOrderHuntSale.get("product_code")));
                                productData.setFull_name(ConvertUtils.toString(agencyOrderHuntSale.get("product_full_name")));
                                productData.setImages(ConvertUtils.toString(agencyOrderHuntSale.get("product_images")));
                                productData.setPrice(ConvertUtils.toLong(agencyOrderHuntSale.get("product_price")));
                                productData.setQuantity(
                                        ConvertUtils.toInt(agencyOrderHuntSale.get("product_total_quantity")) / huntSaleCombo.getQuantity());
                                productData.setTotal_begin_price(
                                        ConvertUtils.toLong(agencyOrderHuntSale.get("product_total_begin_price")));
                                productData.setTotal_promo_price(
                                        ConvertUtils.toLong(agencyOrderHuntSale.get("product_total_promotion_price")));
                                productData.setTotal_end_price(
                                        ConvertUtils.toLong(agencyOrderHuntSale.get("product_total_end_price")));
                                huntSaleCombo.getProducts().add(
                                        productData
                                );
                                huntSaleCombo.setPrice(
                                        huntSaleCombo.getPrice() + (productData.getPrice() * productData.getQuantity())
                                );
                                huntSaleCombo.setTotal_begin_price(
                                        huntSaleCombo.getPrice() * huntSaleCombo.getQuantity()
                                );
                                huntSaleCombo.setTotal_promo_price(
                                        ConvertUtils.toLong(huntSaleCombo.getTotal_promo_price() + productData.getTotal_promo_price())
                                );
                                huntSaleCombo.setTotal_end_price(
                                        huntSaleCombo.getTotal_begin_price() - huntSaleCombo.getTotal_promo_price()
                                );
                                huntSaleCombo.setIs_hunt_sale(YesNoStatus.YES.getValue());
                                mpCombo.put(combo_id, huntSaleCombo);
                            } else {
                                HuntSaleOrderDetail huntSaleProduct = this.convertHuntSaleProduct(
                                        agencyOrderHuntSale
                                );
                                huntSaleProduct.setIs_hunt_sale(YesNoStatus.YES.getValue());

                                huntSaleProduct.setPromo_description(
                                        ConvertUtils.toString(agencyOrderHuntSale.get("promo_description")));

                                huntSaleOrder.getProducts().add(huntSaleProduct);
                            }
                        }
                    } else {
                        /**
                         * Quà
                         */
                        HuntSaleOrderDetail huntSaleProduct = this.convertHuntSaleProduct(
                                agencyOrderHuntSale
                        );
                        huntSaleOrder.getGifts().add(huntSaleProduct);
                    }
                }

                for (HuntSaleOrderDetail huntSaleOrderDetail : mpCombo.values()) {
                    huntSaleOrder.getProducts().add(huntSaleOrderDetail);
                }

                huntSaleOrder.setTotal_begin_price(
                        ConvertUtils.toLong(agencyOrderDept.get("total_begin_price"))
                );
                huntSaleOrder.setTotal_promo_price(
                        ConvertUtils.toLong(agencyOrderDept.get("total_promotion_product_price"))
                );
                huntSaleOrder.setTotal_end_price(
                        ConvertUtils.toLong(agencyOrderDept.get("total_end_price"))
                );

                huntSaleOrder.setDept_cycle(ConvertUtils.toInt(agencyOrderDept.get("dept_cycle")));

                huntSaleOrder.setDept_code(
                        this.appUtils.getAgencyOrderDeptCode(
                                agency_order_code,
                                ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                                total,
                                promo_id != 0,
                                this.hasOrderNormal(ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")))
                        )
                );
                hunt_sale_orders.add(huntSaleOrder);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return hunt_sale_orders;
    }

    private HuntSaleOrderDetail convertHuntSaleCombo(int combo_id, int combo_quantity) {
        HuntSaleOrderDetail huntSaleOrderDetail = new HuntSaleOrderDetail();
        try {
            JSONObject jsCombo = this.promoDB.getComboInfo(combo_id);
            if (jsCombo == null) {
                return huntSaleOrderDetail;
            }

            huntSaleOrderDetail.setIs_combo(1);
            huntSaleOrderDetail.setId(ConvertUtils.toInt(jsCombo.get("id")));
            huntSaleOrderDetail.setCode(ConvertUtils.toString(jsCombo.get("code")));
            huntSaleOrderDetail.setFull_name(ConvertUtils.toString(jsCombo.get("full_name")));
            huntSaleOrderDetail.setProduct_small_unit_id(1);
            huntSaleOrderDetail.setStep(1);
            huntSaleOrderDetail.setMinimum_purchase(1);
            huntSaleOrderDetail.setImages(ConvertUtils.toString(jsCombo.get("images")));
            huntSaleOrderDetail.setQuantity(combo_quantity);
            huntSaleOrderDetail.setItem_type(0);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return huntSaleOrderDetail;
    }

    private HuntSaleOrderDetail convertHuntSaleProduct(JSONObject product) {
        HuntSaleOrderDetail huntSaleOrderDetail = new HuntSaleOrderDetail();
        try {
            huntSaleOrderDetail.setIs_combo(0);
            huntSaleOrderDetail.setId(ConvertUtils.toInt(product.get("product_id")));
            huntSaleOrderDetail.setCode(ConvertUtils.toString(product.get("product_code")));
            huntSaleOrderDetail.setFull_name(ConvertUtils.toString(product.get("product_full_name")));
            huntSaleOrderDetail.setProduct_small_unit_id(1);
            huntSaleOrderDetail.setStep(1);
            huntSaleOrderDetail.setMinimum_purchase(1);
            huntSaleOrderDetail.setImages(ConvertUtils.toString(product.get("product_images")));
            huntSaleOrderDetail.setPrice(ConvertUtils.toInt(product.get("product_price")));
            huntSaleOrderDetail.setQuantity(ConvertUtils.toInt(product.get("product_total_quantity")));
            huntSaleOrderDetail.setTotal_begin_price(ConvertUtils.toLong(product.get("product_total_begin_price")));
            huntSaleOrderDetail.setTotal_promo_price(ConvertUtils.toLong(product.get("product_total_promotion_price")));
            huntSaleOrderDetail.setTotal_end_price(ConvertUtils.toLong(product.get("product_total_end_price")));
            huntSaleOrderDetail.setItem_type(ConvertUtils.toInt(product.get("product_item_type")));
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return huntSaleOrderDetail;
    }

    private JSONObject getTotalOrderDeliveryInfo(int agency_order_id) {
        try {
            JSONObject order_delivery_info = new JSONObject();

            Map<Integer, ProductMoneyResponse> products = new HashMap<>();
            List<JSONObject> orderProductList = this.orderDB.getListProductInOrder(agency_order_id);
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

                ProductMoneyResponse product = products.get(productMoneyResponse.getId());
                if (product == null) {
                    productMoneyResponse.setQuantity_delivery(
                            this.orderDB.getProductQuantityDelivery(
                                    agency_order_id,
                                    productMoneyResponse.getId())
                    );
                    products.put(productMoneyResponse.getId(), productMoneyResponse);
                } else {
                    product.setQuantity(product.getQuantity() + productMoneyResponse.getQuantity());
                    products.put(product.getId(), product);
                }
                products.put(productMoneyResponse.getId(), productMoneyResponse);
            }

            Map<Integer, ProductMoneyResponse> gifts = new HashMap<>();
            List<JSONObject> orderGoodsList = this.orderDB.getListGoodsInOrder(agency_order_id);
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
                } else if (AgencyOrderPromoType.GOODS_BONUS.getId() == agencyOrderDetailEntity.getType()) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GOODS_OFFER.getKey());
                } else if (AgencyOrderPromoType.GIFT_BONUS.getId() == agencyOrderDetailEntity.getType()) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GIFT_OFFER.getKey());
                }

                ProductMoneyResponse gift = gifts.get(productMoneyResponse.getId());
                if (gift == null) {
                    productMoneyResponse.setQuantity_delivery(
                            this.orderDB.getGiftQuantityDelivery(
                                    agency_order_id,
                                    productMoneyResponse.getId())
                    );
                    gifts.put(productMoneyResponse.getId(), productMoneyResponse);
                } else {
                    gift.setQuantity(gift.getQuantity() + productMoneyResponse.getQuantity());
                    gifts.put(gift.getId(), gift);
                }
            }


            order_delivery_info.put("products", new ArrayList<>(products.values()));
            order_delivery_info.put("gifts", new ArrayList<>(gifts.values()));
            order_delivery_info.put("order_deliveries", this.getListAgencyOrderDelivery(agency_order_id));

            return order_delivery_info;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }

        return null;
    }

    private List<JSONObject> getListAgencyOrderDelivery(int agency_order_id) {
        try {
            Map<Integer, JSONObject> mpOrderDelivery = new HashMap<>();
            List<JSONObject> odOfProductList = this.orderDB.getListAgencyOrderDeliveryHasProduct(
                    agency_order_id
            );
            for (JSONObject odOfProduct : odOfProductList) {
                if (mpOrderDelivery.containsKey(
                        ConvertUtils.toInt(odOfProduct.get("id")))) {
                    continue;
                }
                mpOrderDelivery.put(
                        ConvertUtils.toInt(odOfProduct.get("id")),
                        this.orderDB.getAgencyOrderDelivery(
                                ConvertUtils.toInt(odOfProduct.get("id"))
                        )
                );
            }

            List<JSONObject> odOfGiftList = this.orderDB.getListAgencyOrderDeliveryHasGift(
                    agency_order_id
            );
            for (JSONObject odOfGift : odOfGiftList) {
                if (mpOrderDelivery.containsKey(
                        ConvertUtils.toInt(odOfGift.get("id")))) {
                    continue;
                }
                mpOrderDelivery.put(
                        ConvertUtils.toInt(odOfGift.get("id")),
                        this.orderDB.getAgencyOrderDelivery(
                                ConvertUtils.toInt(odOfGift.get("id"))
                        )
                );
            }

            return new ArrayList<>(mpOrderDelivery.values());
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return new ArrayList<>();
    }

    private List<PromoBasicData> convertPromoToList(String data) {
        try {
            List<PromoBasicData> promoOrderDataList = JsonUtils.DeSerialize(
                    data,
                    new TypeToken<List<PromoBasicData>>() {
                    }.getType());
            return promoOrderDataList;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return new ArrayList<>();
    }

    private List<JSONObject> getOrderFlow(JSONObject agencyOrder) {
        List<JSONObject> orderFlow = new ArrayList<>();
        JSONObject jsCreator = new JSONObject();
        jsCreator.put("status", "Created");
        jsCreator.put("created_date", agencyOrder.get("created_date"));
        jsCreator.put("name", SourceOrderType.CMS.getValue() == ConvertUtils.toInt(agencyOrder.get("source"))
                ? SourceOrderType.CMS.getLabel() :
                SourceOrderType.APP.getLabel());
        orderFlow.add(jsCreator);
        List<JSONObject> rsOrderHistory = this.orderDB.getListOrderStatusHistory(
                ConvertUtils.toInt(agencyOrder.get("id")));

        for (JSONObject js : rsOrderHistory) {
            JSONObject jsFlow = new JSONObject();
            jsFlow.put("created_date", js.get("created_date"));
            jsFlow.put("note", js.get("note"));
            if (ConvertUtils.toInt(js.get("creator_id")) != 0) {
                jsFlow.put("name", this.dataManager.getStaffManager().getStaffFullName(
                        ConvertUtils.toInt(js.get("creator_id"))));
            } else {
                jsFlow.put("name", SourceOrderType.APP.getLabel());
            }

            jsFlow.put("status", OrderStatus.from(ConvertUtils.toInt(js.get("agency_order_status"))).getLabel());
            orderFlow.add(jsFlow);
        }
        return orderFlow;
    }

    protected long getVuotHMKDCurrent(int status,
                                      long dep_limit,
                                      long ngd_limit,
                                      long cno,
                                      long total_end_price,
                                      long totalOrderDept) {
        if (!OrderStatus.isStatusDeptDoing(status)) {
            return ConvertUtils.toLong(this.getAgencyHMKD(
                    ConvertUtils.toDouble(dep_limit),
                    ConvertUtils.toDouble(ngd_limit),
                    ConvertUtils.toDouble(cno),
                    totalOrderDept) - total_end_price);
        } else {
            return ConvertUtils.toLong(
                    this.getAgencyHMKD(
                            ConvertUtils.toDouble(dep_limit),
                            ConvertUtils.toDouble(ngd_limit),
                            ConvertUtils.toDouble(cno),
                            totalOrderDept));
        }
    }

    protected String getStuckInfoByStuckInfo(String stuck_info, int stuck_type) {
        try {
            String result = "";
            if (stuck_info != null && !stuck_info.isEmpty()) {
                List<JSONObject> stucks = JsonUtils.DeSerialize(stuck_info, new TypeToken<List<JSONObject>>() {
                }.getType());

                for (JSONObject stuck : stucks) {
                    if (!result.isEmpty()) {
                        result += "; ";
                    }

                    result += StuckType.from(
                            ConvertUtils.toString(stuck.get("stuck_type"))).getLabel();
                }

                return result;
            } else {
                return StuckType.from(stuck_type).getLabel();
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return "";
    }

    /**
     * Xác nhận soạn hàng
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse confirmPrepareOrder(SessionData sessionData, ConfirmPrepareOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            AgencyOrderEntity agencyOrderEntity = this.orderDB.getAgencyOrderEntity(request.getId());
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (!this.dataManager.getStaffManager().checkManageOrder(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(agencyOrderEntity.getAgency_id()),
                    agencyOrderEntity.getStatus()
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            if (!OrderStatus.canPrepareOrder(agencyOrderEntity.getStatus())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            int current_order_status = agencyOrderEntity.getStatus();
            /**
             * Đối với đơn hàng nháp kiểm tra voucher nếu có
             */
            if (current_order_status == OrderStatus.DRAFT.getKey()) {
                if (agencyOrderEntity.getVoucher_info() != null &&
                        !agencyOrderEntity.getVoucher_info().isEmpty() &&
                        !agencyOrderEntity.getVoucher_info().equals("[]")) {
                    if (this.validateVoucher(
                            JsonUtils.DeSerialize(agencyOrderEntity.getVoucher_info(), new TypeToken<List<Integer>>() {
                            }.getType())).failed()) {
                        ClientResponse crVoucher = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        crVoucher.setMessage("[VOUCHER] Voucher không tồn tại hoặc đã hết hạn");
                        return crVoucher;
                    }
                }
            }
            if (current_order_status == OrderStatus.WAITING_CONFIRM.getKey()) {
                if (StuckType.NQH_CK.getId() == agencyOrderEntity.getStuck_type()) {
                    /**
                     * check tồn kho
                     */
                    List<JSONObject> products = this.orderDB.getListProductInOrder(request.getId());
                    List<JSONObject> goods = this.orderDB.getListGoodsInOrder(request.getId());
                    List<ProductData> productDataList = this.groupItemOrder(
                            products,
                            goods);
                    ClientResponse crCheckTonKho = this.checkTonKho(productDataList);

                    DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agencyOrderEntity.getAgency_id());
                    if (deptAgencyInfoEntity == null) {
                        deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agencyOrderEntity.getAgency_id());
                        if (deptAgencyInfoEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                    }

                    List<StuckData> stuckDataList = this.checkStuck(agencyOrderEntity, deptAgencyInfoEntity);
                    List<StuckData> stuckOrderNQH = this.stuckOtherNQH(stuckDataList);
                    if (crCheckTonKho.failed()) {
                        /**
                         * Chuyển về trạng thái chờ điều chỉnh và duyệt cam kết
                         */
                        boolean rsApproveCommit = this.orderDB.approveCommit(
                                request.getId());
                        if (!rsApproveCommit) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        this.orderDB.saveUpdateOrderStatusHistory(request.getId(), OrderStatus.RETURN_AGENCY.getKey(), request.getNote(), sessionData.getId());

                        this.pushNotifyToAgency(
                                0,
                                NotifyAutoContentType.APPROVE_ORDER_COMMIT,
                                "",
                                NotifyAutoContentType.APPROVE_ORDER_COMMIT.getType(),
                                JsonUtils.Serialize(
                                        Arrays.asList(ConvertUtils.toString(request.getId()))
                                ),
                                "Không đủ hàng đáp ứng cho đơn đặt hàng " +
                                        agencyOrderEntity.getCode() + " của Quý khách, vui lòng điều chỉnh số lượng đặt hàng.",
                                agencyOrderEntity.getAgency_id()
                        );

                        /**
                         * Xóa săn phẩm ăn săn sale của đơn
                         */
                        this.orderDB.deleteAgencyPromoHuntSale(request.getId());

                        /**
                         * Nhả voucher nếu có
                         */
                        this.orderDB.returnVoucher(request.getId());

                        ClientResponse crApprove = ClientResponse.fail(ResponseStatus.EXCEPTION, ResponseMessage.APPROVE_COMMIT_OUT_STOCK);
                        crApprove.setMessage(crApprove.getMessage() + ": " + "Không đủ tồn kho");
                        return crApprove;
                    } else if (stuckDataList.size() > 0 && stuckOrderNQH.size() > 0) {
                        /**
                         * Chuyển về trạng thái chờ điều chỉnh và duyệt cam kết
                         */
                        boolean rsApproveCommit = this.orderDB.approveCommit(request.getId());
                        if (!rsApproveCommit) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        this.orderDB.saveUpdateOrderStatusHistory(request.getId(), OrderStatus.RETURN_AGENCY.getKey(), request.getNote(), sessionData.getId());

                        this.pushNotifyToAgency(
                                0,
                                NotifyAutoContentType.APPROVE_ORDER_COMMIT,
                                "",
                                NotifyAutoContentType.APPROVE_ORDER_COMMIT.getType(),
                                JsonUtils.Serialize(
                                        Arrays.asList(ConvertUtils.toString(request.getId()))
                                ),
                                "Giá trị đơn hàng " + agencyOrderEntity.getCode() + " đang vượt hạn mức khả dụng." +
                                        " Vui lòng giảm giá trị đơn hàng " +
                                        stuckOrderNQH.get(0).getData() + ".",
                                agencyOrderEntity.getAgency_id()
                        );

                        /**
                         * Xóa săn phẩm ăn săn sale của đơn
                         */
                        this.orderDB.deleteAgencyPromoHuntSale(request.getId());

                        /**
                         * Nhả voucher nếu có
                         */
                        this.orderDB.returnVoucher(request.getId());

                        String message = this.getMessageByStuckList(stuckOrderNQH);
                        ClientResponse crApprove = ClientResponse.fail(ResponseStatus.EXCEPTION, ResponseMessage.APPROVE_COMMIT_OUT_STOCK);
                        crApprove.setMessage(crApprove.getMessage() + ": " + message);
                        return crApprove;
                    }
                } else {
                    /**
                     * Kiểm tra điều kiện đơn hàng
                     */
                    DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agencyOrderEntity.getAgency_id());
                    if (deptAgencyInfoEntity == null) {
                        deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agencyOrderEntity.getAgency_id());
                        if (deptAgencyInfoEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_AGENCY_INFO_EMPTY);
                        }
                    }

                    List<StuckData> stuckDataList = this.checkStuck(agencyOrderEntity, deptAgencyInfoEntity);
                    if (stuckDataList.size() > 0) {
                        boolean rsUpdateAgencyOrderStuckType = this.orderDB.updateAgencyOrderStuckType(
                                request.getId(),
                                stuckDataList.get(0).getStuck_type().getId(),
                                this.parseStuckInfoToString(stuckDataList)
                        );
                        if (!rsUpdateAgencyOrderStuckType) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                        ClientResponse rsVerifyStuckType = ClientResponse.fail(ResponseStatus.EXCEPTION, ResponseMessage.ORDER_STUCK);
                        rsVerifyStuckType.setMessage(stuckDataList.get(0).getStuck_info());
                        return rsVerifyStuckType;
                    }
                }
            } else if (current_order_status == OrderStatus.RESPONSE.getKey()) {
                boolean rsUpdateAgencyOrderStatus = this.orderDB.confirmPrepareOrder(
                        request.getId(),
                        request.getNote(),
                        sessionData.getId());
                if (!rsUpdateAgencyOrderStatus) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * save update order status history
                 */
                this.gotoPrepareOrder(request.getId(), sessionData.getId());

                /**
                 * Lưu sản phẩm ăn săn sale
                 */
                this.saveAgencyPromoHuntSale(
                        request.getId(),
                        agencyOrderEntity.getAgency_id());

                return ClientResponse.success(null);
            } else {
                /**
                 * Kiểm tra điều kiện đơn hàng
                 */
                DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agencyOrderEntity.getAgency_id());
                if (deptAgencyInfoEntity == null) {
                    deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agencyOrderEntity.getAgency_id());
                    if (deptAgencyInfoEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_AGENCY_INFO_EMPTY);
                    }
                }

                List<StuckData> stuckDataList = this.checkStuck(agencyOrderEntity, deptAgencyInfoEntity);
                if (stuckDataList.size() > 0) {
                    boolean rsUpdateAgencyOrderStuckType = this.orderDB.updateAgencyOrderStuckType(
                            request.getId(),
                            stuckDataList.get(0).getStuck_type().getId(),
                            this.parseStuckInfoToString(stuckDataList)
                    );
                    if (!rsUpdateAgencyOrderStuckType) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                    ClientResponse rsVerifyStuckType = ClientResponse.fail(ResponseStatus.EXCEPTION, ResponseMessage.ORDER_STUCK);
                    rsVerifyStuckType.setMessage(stuckDataList.get(0).getStuck_info());
                    return rsVerifyStuckType;
                }
            }

            boolean rsUpdateAgencyOrderStatus = this.orderDB.confirmPrepareOrder(
                    request.getId(),
                    request.getNote(),
                    sessionData.getId());
            if (!rsUpdateAgencyOrderStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Lưu sản phẩm ăn săn sale
             */
            this.saveAgencyPromoHuntSale(request.getId(), agencyOrderEntity.getAgency_id());

            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agencyOrderEntity.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agencyOrderEntity.getAgency_id());
                if (deptAgencyInfoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_AGENCY_INFO_EMPTY);
                }
            }

            this.updateDeptCycleToOrder(
                    request.getId(),
                    deptAgencyInfoEntity.getDept_cycle(),
                    agencyOrderEntity.getTotal()
            );


            /**
             * save update order status history
             */
            this.orderDB.saveUpdateOrderStatusHistory(request.getId(), OrderStatus.PREPARE.getKey(), "", sessionData.getId());

            /**
             * Ngậm kho
             */
            List<JSONObject> products = this.orderDB.getListProductInOrder(request.getId());
            List<JSONObject> goods = this.orderDB.getListGoodsInOrder(request.getId());
            List<ProductData> productDataList = this.groupItemOrder(products, goods);
            if (current_order_status == OrderStatus.DRAFT.getKey()) {
                /**
                 * Lưu voucher đã sử dụng
                 */
                this.saveVoucherUsed(agencyOrderEntity.getId(), agencyOrderEntity.getVoucher_info());

                for (ProductData js : productDataList) {
                    int product_id = js.getId();
                    int product_total_quantity = js.getQuantity();

                    boolean rsIncreaseQuantityWaitingShipToday = this.increaseQuantityWaitingShipToday(
                            product_id, product_total_quantity,
                            agencyOrderEntity.getCode());
                    if (!rsIncreaseQuantityWaitingShipToday) {
                        ClientResponse crIncreaseQuantityWaitingShipToday = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.OUT_OF_STOCK);
                        crIncreaseQuantityWaitingShipToday.setMessage("Sản phẩm " + js.getFull_name() + " " + crIncreaseQuantityWaitingShipToday.getMessage());
                        this.alertToTelegram(crIncreaseQuantityWaitingShipToday.getMessage(), ResponseStatus.FAIL);
                        return crIncreaseQuantityWaitingShipToday;
                    }
                }
            } else if (current_order_status == OrderStatus.WAITING_CONFIRM.getKey()) {
                for (ProductData js : productDataList) {
                    int product_id = js.getId();
                    int product_total_quantity = js.getQuantity();

                    /**
                     * Tăng kho chờ giao
                     */
                    boolean rsIncreaseQuantityWaitingShipToday = this.increaseQuantityWaitingShipToday(
                            product_id, product_total_quantity, agencyOrderEntity.getCode());
                    if (!rsIncreaseQuantityWaitingShipToday) {
                        /**
                         * Thông báo tele
                         */
                        ClientResponse crIncreaseQuantityWaitingShipToday = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.OUT_OF_STOCK);
                        crIncreaseQuantityWaitingShipToday.setMessage("Sản phẩm " + js.getFull_name() + " " + crIncreaseQuantityWaitingShipToday.getMessage());
                        this.alertToTelegram(crIncreaseQuantityWaitingShipToday.getMessage(), ResponseStatus.FAIL);

                        return crIncreaseQuantityWaitingShipToday;
                    }

                    if (agencyOrderEntity.getStuck_type() != StuckType.NQH_CK.getId()) {
                        /**
                         * Giảm kho chờ xác nhận
                         */
                        boolean rsIncreaseQuantityWaitingApproveToday = this.decreaseQuantityWaitingApproveToday(
                                product_id, product_total_quantity,
                                agencyOrderEntity.getCode());
                        if (!rsIncreaseQuantityWaitingApproveToday) {
                            /**
                             * Thông báo tele
                             */
                            ClientResponse crIncreaseQuantityWaitingShipToday = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.OUT_OF_STOCK);
                            crIncreaseQuantityWaitingShipToday.setMessage("Sản phẩm " + js.getFull_name() + " " + crIncreaseQuantityWaitingShipToday.getMessage());
                            this.alertToTelegram(crIncreaseQuantityWaitingShipToday.getMessage(), ResponseStatus.FAIL);
                            return crIncreaseQuantityWaitingShipToday;
                        }
                    }
                }
            }

            if (StuckType.NQH_CK.getId() == agencyOrderEntity.getStuck_type()) {
                this.pushNotifyToAgency(
                        0,
                        NotifyAutoContentType.APPROVE_ORDER_COMMIT,
                        "",
                        NotifyAutoContentType.APPROVE_ORDER_COMMIT.getType(),
                        JsonUtils.Serialize(
                                Arrays.asList(ConvertUtils.toString(request.getId()))
                        ),
                        "Cam kết thanh toán của Quý Khách đã được chấp nhận, Anh Tin sẽ giao hàng trong thời gian sớm nhất.",
                        agencyOrderEntity.getAgency_id()
                );
            } else {
                this.pushNotifyToAgency(
                        0,
                        NotifyAutoContentType.CONFIRM_PREPARE_ORDER,
                        "",
                        NotifyAutoContentType.CONFIRM_PREPARE_ORDER.getType(),
                        JsonUtils.Serialize(
                                Arrays.asList(ConvertUtils.toString(request.getId()))
                        ),
                        "Đơn đặt hàng " + agencyOrderEntity.getCode() + " của Quý khách đã được tiếp nhận và đang xử lý.",
                        agencyOrderEntity.getAgency_id()
                );
            }

            //this.createAgencyOrderConfirm(sessionData, request.getId());

            this.createPOMFromPO(sessionData, request.getId());

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse createAgencyOrderConfirm(SessionData sessionData, int agency_order_id) {
        try {
            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrder(agency_order_id);
            if (jsAgencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            int agency_order_confirm_id = this.orderDB.insertAgencyOrderConfirm(jsAgencyOrder, "");
            if (agency_order_confirm_id <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<JSONObject> products = this.orderDB.getListProductInOrder(agency_order_id);
            for (JSONObject jsProduct : products) {
                this.orderDB.insertAgencyOrderConfirmProduct(
                        agency_order_id,
                        agency_order_confirm_id,
                        jsProduct,
                        "",
                        "");
            }

            return ResponseConstants.success;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, ex.getMessage());
        }
    }

    private void gotoPrepareOrder(int order_id, int staff_id) {
        try {
            this.orderDB.saveUpdateOrderStatusHistory(
                    order_id,
                    OrderStatus.PREPARE.getKey(), "",
                    staff_id);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    private String getMessageByStuckList(List<StuckData> stuckDataList) {
        String message = "";
        for (StuckData stuckData : stuckDataList) {
            message += " " + stuckData.getStuck_info();
        }
        return message;
    }

    private List<StuckData> stuckOtherNQH(List<StuckData> stuckDataList) {
        List<StuckData> result = new ArrayList<>();
        for (StuckData stuckData : stuckDataList) {
            if (stuckData.getStuck_type().getId() != StuckType.NQH_CK.getId()) {
                result.add(stuckData);
            }
        }
        return result;
    }

    private ClientResponse checkTonKho(List<ProductData> products) {
        try {
            ClientResponse clientResponse = ClientResponse.success(null);
            String message = "";
            for (ProductData productData : products) {
                int product_id = productData.getId();
                int product_total_quantity = productData.getQuantity();
                int stock = getTonKho(product_id);
                if (stock < product_total_quantity) {
                    clientResponse.setStatus(ResponseStatus.FAIL);
                    message += " Tồn kho của sản phẩm " +
                            productData.getFull_name() + " chỉ còn " + appUtils.priceFormat(Math.max(0, stock)) + ";";
                }
            }
            if (clientResponse.failed()) {
                clientResponse.setMessage("[" + ResponseMessage.OUT_OF_STOCK + "]" + message);
                return clientResponse;
            }
            return clientResponse;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse checkTonKhoEdit(Map<Integer, ProductData> products, List<ProductData> oldProductDataList) {
        try {
            ClientResponse clientResponse = ClientResponse.success(null);
            String message = "";
            for (ProductData productData : products.values()) {
                int product_id = productData.getId();
                int product_total_quantity = productData.getQuantity();
                int stock = getTonKho(product_id)
                        + this.getProductQuantityInOrder(oldProductDataList, product_id);
                if (stock < product_total_quantity) {
                    clientResponse.setStatus(ResponseStatus.FAIL);
                    message += " Tồn kho của sản phẩm " +
                            productData.getFull_name() + " chỉ còn " + appUtils.priceFormat(Math.max(0, stock)) + ";";
                }
            }
            if (clientResponse.failed()) {
                clientResponse.setMessage("[" + ResponseMessage.OUT_OF_STOCK + "]" + message);
                return clientResponse;
            }
            return clientResponse;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse checkPrepareOrder(AgencyOrderEntity agencyOrderEntity) {
        try {
            /**
             * Check rang buoc don hang
             */
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agencyOrderEntity.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agencyOrderEntity.getAgency_id());
                if (deptAgencyInfoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            List<StuckData> stuckDataList = this.checkStuck(agencyOrderEntity, deptAgencyInfoEntity);

            if (!stuckDataList.isEmpty()) {
                ClientResponse rsVerifyStuckType = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_STUCK);
                rsVerifyStuckType.setMessage(stuckDataList.get(0).getStuck_info());
                return rsVerifyStuckType;
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<StuckData> checkStuck(
            AgencyOrderEntity agencyOrderEntity,
            DeptAgencyInfoEntity deptAgencyInfoEntity
    ) {
        List<StuckData> stuckDataList = new ArrayList<>();

        /**
         * Nợ quá hạn
         */
        if (deptAgencyInfoEntity.getNqh() > 0) {
            JSONObject commit = this.orderDB.getAgencyOrderCommitByOrder(agencyOrderEntity.getId());
            if (commit != null) {
                if (agencyOrderEntity.getCommit_approve_status() != CommitApproveStatus.APPROVED.getId()) {
                    stuckDataList.add(new StuckData(StuckType.NQH_CK, StuckType.NQH_CK.getLabel() + " " + this.appUtils.priceFormat(deptAgencyInfoEntity.getNqh())));
                }
            } else {
                if (agencyOrderEntity.getSource() == SourceOrderType.CMS.getValue()) {
                    stuckDataList.add(new StuckData(StuckType.NQH, StuckType.NQH.getLabel() + " " + this.appUtils.priceFormat(deptAgencyInfoEntity.getNqh())));
                } else {
                    stuckDataList.add(new StuckData(StuckType.NQH_TT, StuckType.NQH_TT.getLabel() + " " + this.appUtils.priceFormat(deptAgencyInfoEntity.getNqh())));
                }
            }
        }

        /**
         * VHMKD
         */
        double total_end_price = agencyOrderEntity.getTotal_end_price();
        double totalOrderDept = this.orderDB.getTotalPriceOrderDeptDoing(agencyOrderEntity.getAgency_id());
        long hmkd_over_current = this.getVuotHMKDCurrent(agencyOrderEntity.getStatus(),
                deptAgencyInfoEntity.getDept_limit(),
                deptAgencyInfoEntity.getNgd_limit(),
                deptAgencyInfoEntity.getCurrent_dept(),
                ConvertUtils.toLong(total_end_price),
                ConvertUtils.toLong(totalOrderDept));
        if (hmkd_over_current < 0) {
            stuckDataList.add(new StuckData(StuckType.V_HMKD, StuckType.V_HMKD.getLabel() + " " + this.appUtils.priceFormat(Math.abs(hmkd_over_current)), this.appUtils.priceFormat(Math.abs(hmkd_over_current))));
        }

        if (agencyOrderEntity.getStatus() != OrderStatus.WAITING_CONFIRM.getKey()) {
            /**
             * Kiểm tra Giá trị tối thiểu
             */
            if (agencyOrderEntity.getTotal_end_price() < dataManager.getProductManager().getGiaTriToiThieu()) {
                stuckDataList.add(new StuckData(StuckType.GTTT, StuckType.GTTT.getLabel()));
            }

            /**
             * Số lượng tối thiểu và bước nhảy
             */
            List<JSONObject> products = this.orderDB.getListProductInOrder(agencyOrderEntity.getId());
            for (JSONObject product : products) {
                if (ConvertUtils.toInt(product.get("promo_id")) == 0) {
                    int product_id = ConvertUtils.toInt(product.get("product_id"));
                    int product_total_quantity = ConvertUtils.toInt(product.get("product_total_quantity"));
                    int product_minimum_purchase = ConvertUtils.toInt(product.get("product_minimum_purchase"));
                    int product_step = ConvertUtils.toInt(product.get("product_step"));
                    ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                            product_id);
                    if (productCache == null) {
                        stuckDataList.add(new StuckData(StuckType.SLTT, StuckType.SLTT.getLabel()));
                        continue;
                    }

                    /**
                     * Check số lượng tối thiêu và bước nhảy
                     */
                    ClientResponse clientResponse = this.validateProductStep(
                            product_total_quantity,
                            product_minimum_purchase,
                            product_step);
                    if (clientResponse.failed()) {
                        int stock = getTonKho(product_id);
                        if (stock > product_total_quantity) {
                            stuckDataList.add(new StuckData(StuckType.SLTT, StuckType.SLTT.getLabel() + ": " + productCache.getFull_name()));
                        }
                    }
                }
            }
        }
        return stuckDataList;
    }

    public ClientResponse confirmDeliveryOrder(SessionData sessionData, ConfirmDeliveryOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrder(request.getId());
            if (jsAgencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            AgencyOrderEntity agencyOrderEntity = AgencyOrderEntity.from(jsAgencyOrder);
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (agencyOrderEntity.getType() != OrderType.INSTANTLY.getValue()) {
                ClientResponse crInstantly = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                crInstantly.setMessage("Tính năng này chỉ dùng cho Đơn giao ngay");
                return crInstantly;
            }


            if (!this.dataManager.getStaffManager().checkManageOrder(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(agencyOrderEntity.getAgency_id()),
                    agencyOrderEntity.getStatus()
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            if (OrderStatus.SHIPPING.getKey() != agencyOrderEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            List<JSONObject> agencyOrderDeptList = this.orderDB.getListAgencyOrderDeptNotFinish(agencyOrderEntity.getId());
            for (JSONObject agencyOrderDept : agencyOrderDeptList) {
                this.orderDB.completeAgencyOrderDept(
                        ConvertUtils.toInt(agencyOrderDept.get("id"))
                );
            }

            /**
             * Ghi nhận tích lũy đơn hàng
             */
            this.callGhiNhanTichLuyDonHang(
                    agencyOrderEntity.getId(),
                    agencyOrderEntity.getAgency_id(),
                    agencyOrderDeptList,
                    agencyOrderEntity.getCreated_date().getTime(),
                    DateTimeUtils.getMilisecondsNow()
            );

            /**
             * Tích lũy đam mê
             */
            this.callGhiNhanCSDMDonHang(
                    agencyOrderEntity.getId(),
                    agencyOrderEntity.getAgency_id(),
                    agencyOrderDeptList,
                    agencyOrderEntity.getCode(),
                    agencyOrderEntity.getType()
            );

            /**
             * Tích lũy xếp hạng
             */
            this.callGhiNhanCTXHDonHang(
                    agencyOrderEntity.getId(),
                    agencyOrderEntity.getAgency_id(),
                    agencyOrderDeptList,
                    agencyOrderEntity.getCode(),
                    agencyOrderEntity.getType()
            );

            /**
             * Tích lũy nhiệm vụ
             */
            if (ConvertUtils.toInt(jsAgencyOrder.get("accumulate_mission_status")) == 0) {
                this.callGhiNhanTichLuyNhiemVuChoDonHang(
                        agencyOrderEntity.getId(),
                        agencyOrderEntity.getAgency_id(),
                        agencyOrderDeptList
                );
                boolean rs = this.orderDB.ghiNhanTrangThaiTichLuyNhiemVuChoDonHang(request.getId());
                if (rs == false) {
                    this.alertToTelegram("ghiNhanTrangThaiTichLuyNhiemVuChoDonHang: " + request.getId(), ResponseStatus.EXCEPTION);
                }
            }

            ClientResponse crCompleteOrder = this.completeAgencyOrder(sessionData, agencyOrderEntity);
            if (crCompleteOrder.failed()) {
                return crCompleteOrder;
            }

            this.orderDB.setOrderIncreaseDept(
                    request.getId(),
                    IncreaseDeptStatus.YES.getValue());

            /**
             * Lưu ưu đãi đam mê được hưởng, nếu có
             */
            this.orderDB.saveAgencyCSDMClaim(agencyOrderEntity.getId(),
                    agencyOrderEntity.getCode(),
                    agencyOrderEntity.getAgency_id());

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse confirmDeliveryOC(SessionData sessionData, ConfirmDeliveryOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrderConfirm(request.getId());
            if (jsAgencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int status = ConvertUtils.toInt(jsAgencyOrder.get("status"));
            if (status != OrderStatus.SHIPPING.getKey()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rs = this.orderDB.completeOC(
                    request.getId(),
                    OrderStatus.COMPLETE.getKey()
            );
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse completeAgencyOrder(SessionData sessionData, AgencyOrderEntity agencyOrderEntity) {
        try {
            boolean rsUpdateAgencyOrderStatus = this.orderDB.deliveryAgencyOrder(
                    agencyOrderEntity.getId(),
                    OrderStatus.COMPLETE.getKey(),
                    "",
                    sessionData.getId());
            if (!rsUpdateAgencyOrderStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * save update order status history
             */
            this.orderDB.saveUpdateOrderStatusHistory(
                    agencyOrderEntity.getId(),
                    OrderStatus.COMPLETE.getKey(),
                    "",
                    sessionData.getId());

            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agencyOrderEntity.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agencyOrderEntity.getAgency_id());
                if (deptAgencyInfoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            if (IncreaseDeptStatus.YES.getValue() != agencyOrderEntity.getIncrease_dept()) {
                /**
                 * Ghi nhận công nợ cho đơn hàng
                 */
                DeptTransactionSubTypeEntity deptTransactionSubTypeEntity =
                        this.dataManager.getConfigManager().getDeptTransactionByOrder();
                if (deptTransactionSubTypeEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }


                double total_end_price = agencyOrderEntity.getTotal_end_price();

                String agency_order_code = agencyOrderEntity.getCode();
                int agency_order_dept_cycle = agencyOrderEntity.getDept_cycle();
                int total = agencyOrderEntity.getTotal();
                if (total == 0) {
                    int rsInsertAgencyOrderNormal = this.orderDB.insertAgencyOrderDept(
                            agencyOrderEntity.getId(),
                            AgencyOrderDeptType.NORMAL.getId(),
                            0,
                            agencyOrderEntity.getDept_cycle(),
                            agencyOrderEntity.getTotal_begin_price(),
                            agencyOrderEntity.getTotal_promotion_price(),
                            agencyOrderEntity.getTotal_end_price(),
                            0,
                            "[]",
                            agencyOrderEntity.getTotal_promotion_product_price(),
                            agencyOrderEntity.getTotal_promotion_order_price(),
                            agencyOrderEntity.getTotal_promotion_order_price_ctkm(),
                            agencyOrderEntity.getTotal_refund_price(),
                            agencyOrderEntity.getTotal_dm_price(),
                            agencyOrderEntity.getTotal_voucher_price()
                    );
                    if (rsInsertAgencyOrderNormal <= 0) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    total = 1;
                    this.orderDB.updateTotalOrder(agencyOrderEntity.getId(), total);
                }

                /**
                 * Ghi nhận công nợ đơn thường
                 */
                JSONObject orderNormalDept = this.orderDB.getOrderNormalDept(
                        agencyOrderEntity.getId()
                );
                if (orderNormalDept != null) {
                    String dept_code = this.appUtils.getAgencyOrderDeptCode(
                            agency_order_code,
                            ConvertUtils.toInt(orderNormalDept.get("order_data_index")),
                            total,
                            ConvertUtils.toInt(orderNormalDept.get("promo_id")) != 0,
                            true
                    );
                    ClientResponse crGhiNhanCongNo = this.ghiNhanCongNoDonHang(
                            agencyOrderEntity.getId(),
                            agencyOrderEntity.getAgency_id(),
                            ConvertUtils.toLong(orderNormalDept.get("total_end_price")),
                            agency_order_code,
                            deptTransactionSubTypeEntity.getId(),
                            sessionData.getId(),
                            DateTimeUtils.getNow(),
                            ConvertUtils.toInt(orderNormalDept.get("dept_cycle")),
                            0,
                            dept_code
                    );
                    if (crGhiNhanCongNo.failed()) {
                        return crGhiNhanCongNo;
                    }
                }

                /**
                 * Ghi nhận công nợ săn sale
                 */
                List<JSONObject> agencyOrderDeptList = this.orderDB.getAgencyOrderDeptHuntSaleList(agencyOrderEntity.getId());
                for (int iDept = 0; iDept < agencyOrderDeptList.size(); iDept++) {
                    JSONObject agencyOrderDept = agencyOrderDeptList.get(iDept);
                    String dept_code = this.appUtils.getAgencyOrderDeptCode(
                            agencyOrderEntity.getCode(),
                            ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                            total,
                            ConvertUtils.toInt(agencyOrderDept.get("promo_id")) != 0,
                            this.hasOrderNormal(ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")))
                    );
                    ClientResponse crGhiNhanCongNoHuntSale = this.ghiNhanCongNoDonHang(
                            agencyOrderEntity.getId(),
                            agencyOrderEntity.getAgency_id(),
                            ConvertUtils.toLong(agencyOrderDept.get("total_end_price")),
                            agencyOrderEntity.getCode(),
                            deptTransactionSubTypeEntity.getId(),
                            sessionData.getId(),
                            DateTimeUtils.getNow(),
                            ConvertUtils.toInt(agencyOrderDept.get("dept_cycle")),
                            ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                            dept_code
                    );
                    if (crGhiNhanCongNoHuntSale.failed()) {
                        return crGhiNhanCongNoHuntSale;
                    }
                }
            }

            /**
             * Tạo đơn giao hàng
             */
            if (agencyOrderEntity.getType() == OrderType.INSTANTLY.getValue()) {
                this.createOrderDeliveryOfOrderInstantly(
                        agencyOrderEntity
                );


                /**
                 * Phát sinh phiếu xuất kho
                 * loại xuất bán, trạng thái hoàn thành
                 */
                List<JSONObject> products = this.orderDB.getListProductInOrder(
                        agencyOrderEntity.getId());
                List<JSONObject> goods = this.orderDB.getListGoodsInOrder(
                        agencyOrderEntity.getId()
                );
                List<ProductData> productDataList = this.groupItemOrder(products, goods);
                List<JSONObject> productExportList = new ArrayList<>();
                for (ProductData jsProduct : productDataList) {
                    int product_id = jsProduct.getId();
                    int product_total_quantity = jsProduct.getQuantity();
                    JSONObject productExport = new JSONObject();
                    productExport.put("product_id", product_id);
                    productExport.put("product_total_quantity", product_total_quantity);
                    productExportList.add(productExport);

                    /**
                     * Trừ tồn kho chờ giao
                     */
                    boolean rsDecreaseQuantityWaitingShipToday = this.decreaseQuantityWaitingShipToday(
                            product_id, product_total_quantity,
                            agencyOrderEntity.getCode());
                    if (!rsDecreaseQuantityWaitingShipToday) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                    /**
                     * Công tồn kho xuất
                     */
                    boolean rsIncreaseQuantityExportToday = this.increaseQuantityExportToday(
                            product_id,
                            product_total_quantity,
                            agencyOrderEntity.getCode());
                    if (!rsIncreaseQuantityExportToday) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }

                ClientResponse rsInsertWarehouseBillExport = this.insertWarehouseBill(
                        agencyOrderEntity.getAgency_id(),
                        WarehouseBillType.EXPORT,
                        agencyOrderEntity.getCode(),
                        productExportList,
                        sessionData.getId());
                if (rsInsertWarehouseBillExport.failed()) {
                    this.alertToTelegram("rsInsertWarehouseBillExport: " + JsonUtils.Serialize(rsInsertWarehouseBillExport), ResponseStatus.EXCEPTION);
                }
            }
            /**
             * Push thông báo
             */
            this.pushNotifyToAgency(
                    0,
                    NotifyAutoContentType.DELIVERY_ORDER,
                    "",
                    NotifyAutoContentType.DELIVERY_ORDER.getType(),
                    JsonUtils.Serialize(Arrays.asList(ConvertUtils.toString(agencyOrderEntity.getId()))),
                    "Đơn đặt hàng " + agencyOrderEntity.getCode() + " của Quý khách đã được Anh Tin giao tới địa chỉ nhận hàng.",
                    agencyOrderEntity.getAgency_id()
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void createOrderDeliveryOfOrderInstantly(AgencyOrderEntity agencyOrderEntity) {
        try {
            int rsInsert = this.orderDB.insertAgencyOrderDelivery(
                    agencyOrderEntity,
                    agencyOrderEntity.getId(),
                    agencyOrderEntity.getCode(),
                    agencyOrderEntity.getCode(),
                    agencyOrderEntity.getTotal_end_price()
            );
            if (rsInsert > 0) {
                List<JSONObject> agencyOrderDeptList = this.orderDB.getListAgencyOrderDept(
                        agencyOrderEntity.getId());
                for (JSONObject agencyOrderDept : agencyOrderDeptList) {
                    List<JSONObject> productList = this.orderDB.getListProductByAgencyOrderDept(
                            agencyOrderEntity.getId(),
                            ConvertUtils.toInt(agencyOrderDept.get("promo_id"))
                    );
                    for (JSONObject product : productList) {
                        this.orderDB.insertAgencyOrderDeliveryProduct(
                                AgencyOrderDetailEntity.from(product),
                                rsInsert,
                                agencyOrderEntity.getCode(),
                                ConvertUtils.toInt(agencyOrderDept.get("id"))
                        );
                    }

                    List<JSONObject> giftList = this.orderDB.getListGoodsInOrder(
                            agencyOrderEntity.getId()
                    );
                    for (JSONObject gift : giftList) {
                        this.orderDB.insertAgencyOrderDeliveryGift(
                                AgencyOrderPromoDetailEntity.from(gift),
                                rsInsert,
                                agencyOrderEntity.getCode(),
                                ConvertUtils.toInt(agencyOrderDept.get("id"))
                        );
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    /**
     * Tính toán đơn đặt hàng
     *
     * @param request
     * @return
     */
    public ClientResponse estimateCostOrder(EstimateCostOrderRequest request) {
        try {
            /**
             * Kiểm tra dũ liệu request
             */
            request.validRequest();

            /**
             * Kiểm tra voucher nếu có sử dụng
             */
            if (!request.getVouchers().isEmpty() && this.validateVoucher(request.getVouchers()).failed()) {
                ClientResponse crVoucher = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                crVoucher.setMessage("[VOUCHER] Voucher không tồn tại hoặc đã hết hạn");
                return crVoucher;
            }

            if (request.getHunt_sale_products().isEmpty()) {
                return ClientResponse.success(null);
            }

            /**
             * Kiểm tra đại lý
             */
            AgencyEntity agencyEntity = this.agencyDB.getAgencyEntity(request.getAgency_id());
            if (agencyEntity == null) {
                return ClientResponse.success(null);
            }

            if (request.getOrder_id() == 0) {
                return this.estimateOrder(request, agencyEntity, SourceOrderType.CMS.getValue());
            } else {
                AgencyOrderEntity agencyOrderEntity = this.orderDB.getAgencyOrderEntity(request.getOrder_id());
                if (agencyOrderEntity == null) {
                    return ClientResponse.success(null);
                }
                if (agencyOrderEntity != null && agencyOrderEntity.getStatus() == OrderStatus.RESPONSE.getKey()) {
                    return this.estimateOrderResponse(request, agencyEntity, agencyOrderEntity.getSource());
                } else {
                    return this.estimateOrder(request, agencyEntity, agencyOrderEntity.getSource());
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse validateVoucher(List<Integer> vouchers) {
        try {
            for (Integer voucherId : vouchers) {
                JSONObject jsVoucher = this.orderDB.getVoucher(voucherId);
                if (jsVoucher == null ||
                        VoucherStatus.READY.getId() != ConvertUtils.toInt(jsVoucher.get("status"))) {
                    ClientResponse crVoucher = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    crVoucher.setMessage("[VOUCHER] Voucher không tồn tại hoặc đã hết hạn");
                    return crVoucher;
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse estimateOrder(
            EstimateCostOrderRequest request,
            AgencyEntity agencyEntity,
            int source) {

        /**
         * Công nợ hiện tại
         */
        DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(request.getAgency_id());
        if (deptAgencyInfoEntity == null) {
            deptAgencyInfoEntity = initDeptAgencyInfo(request.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                return ClientResponse.success(null);
            }
        }

        /**
         * Săn sale
         */
        Map<Integer, ProductCache> mpProductCache = new ConcurrentHashMap<>();


        Map<Integer, ProgramOrderProduct> mpProgramOrderProduct =
                this.initMpProductHuntSale(
                        agencyEntity,
                        mpProductCache,
                        request.getHunt_sale_products(),
                        new ArrayList<>());
        Map<Integer, ProgramOrderProduct> mpProgramOrderProductAll = new LinkedHashMap<>();
        mpProgramOrderProductAll.putAll(mpProgramOrderProduct);
        /**
         * Kiểm tra ẩn hiện
         */
        ClientResponse crCheckVisibilityAndPrice = this.checkVisibilityWhenCreateOrder(
                agencyEntity.getId(),
                agencyEntity.getCity_id(),
                agencyEntity.getRegion_id(),
                agencyEntity.getMembership_id(),
                new ArrayList<>(mpProgramOrderProduct.values()));
        if (crCheckVisibilityAndPrice.failed()) {
            return ClientResponse.success(null);
        }

        Map<Integer, ProgramOrderProduct> mpAllProduct = new ConcurrentHashMap<>();
        mpAllProduct.putAll(mpProgramOrderProduct);

        Map<Integer, Integer> mpStock = this.initMpStock(mpAllProduct);
        List<HuntSaleOrderDetail> huntSaleProductList =
                this.checkOrder(
                        agencyEntity.getId(),
                        request.getHunt_sale_products(),
                        mpProductCache,
                        mpStock
                );
        HuntSaleOrderDetail itemPriceContact = huntSaleProductList.stream().filter(
                x -> x.getPrice() <= 0
        ).findFirst().orElse(null);
        if (itemPriceContact != null) {
            JSONObject data = new JSONObject();
            data.put("hunt_sale_products", huntSaleProductList);
            return ClientResponse.success(data);
        }
        /**
         * Số lượng đã sử dun trong đơn hàng
         */
        Map<Integer, Integer> mpProductQuantityUsed = new HashMap();

        EstimatePromo estimatePromoHuntSale = this.createEstimatePromoHuntSale(
                agencyEntity,
                request.getHunt_sale_products());

        List<HuntSaleOrder> hunt_sale_orders = this.estimateHuntSaleOrder(
                agencyEntity.getId(),
                estimatePromoHuntSale,
                SourceOrderType.CMS.getValue(),
                request.getHunt_sale_products(),
                mpProgramOrderProduct,
                mpAllProduct,
                mpProductQuantityUsed,
                mpProductCache,
                mpStock,
                deptAgencyInfoEntity,
                this.dataManager.getProgramManager().getAgency(agencyEntity.getId()),
                agencyEntity
        );

        Map<Integer, Double> mpProductPrice = this.parseProductPrice(hunt_sale_orders);
        hunt_sale_orders.clear();
        /**
         * Sản phẩm còn lại sau săn sale
         */
        OrderSummaryInfoResponse orderMoneyInfoResponse = new OrderSummaryInfoResponse();
        List<OrderProductResponse> products = new ArrayList<>();
        List<ItemOfferResponse> goodsForSelectList = new ArrayList<>();
        List<ItemOfferResponse> goodsClaimList = new ArrayList<>();
        EstimatePromo estimatePromo = new EstimatePromo();


        for (ProgramOrderProduct programOrderProduct : mpProgramOrderProductAll.values()) {
            ProductEntity productEntity = this.productDB.getProduct(
                    programOrderProduct.getProduct().getId());
            if (productEntity == null) {
                continue;
            }

            ProductCache productPrice = mpProductCache.get(productEntity.getId());
            if (productPrice == null) {
                return ClientResponse.success(null);
            }

            ProductMoneyResponse productMoneyResponse = this.productUtils.getProductMoney(
                    agencyEntity,
                    productEntity,
                    programOrderProduct.getProductQuantity(),
                    productPrice.getPrice(),
                    productPrice.getMinimum_purchase());

            /**
             * Không tính tổng tiền của giá liên hệ - 1
             */

            if (validateProductStep(
                    programOrderProduct.getProductQuantity(),
                    productMoneyResponse.getMinimum_purchase(),
                    productEntity.getStep()
            ).failed()) {
                productMoneyResponse.setIs_error(1);
                productMoneyResponse.setNote(" Sản phẩm không thỏa SLTT " + productMoneyResponse.getMinimum_purchase() + "- BN " + productMoneyResponse.getStep() + ";");
            }

            int tonKho = this.getStockByMpStock(mpStock, programOrderProduct.getProduct().getId());
            int quantityUsed = ConvertUtils.toInt(mpProductQuantityUsed.get(programOrderProduct.getProduct().getId()));
            if (tonKho <
                    programOrderProduct.getProductQuantity() + quantityUsed) {
                productMoneyResponse.setIs_error(1);
                productMoneyResponse.setNote(productMoneyResponse.getNote() + " Tồn kho chỉ còn " + this.appUtils.priceFormat(tonKho));
            }

            products.add(
                    JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse),
                            OrderProductResponse.class));

            /**
             * Đưa vào danh sách sản phẩm tính chính sách
             */
            estimatePromo.getPromoProductInputList().add(new PromoProductBasic(
                    productMoneyResponse.getId(),
                    productMoneyResponse.getQuantity(),
                    productMoneyResponse.getPrice() * 1L,
                    null));

            mpProductQuantityUsed.put(
                    programOrderProduct.getProduct().getId(),
                    programOrderProduct.getProductQuantity());
        }

        /**
         * Tính chính sách
         */
        ResultEstimatePromo resultEstimatePromo = this.estimatePromo(
                request.getAgency_id(),
                estimatePromo,
                request.getGoods(),
                new HashMap<>(),
                source
        );
        if (resultEstimatePromo == null) {
            return ClientResponse.success(null);
        }

        /**
         * Chiết khấu/giảm tiền
         */
        for (OrderProductResponse orderProductResponse : products) {
            Double promoProductPriceQuotation = mpProductPrice.get(orderProductResponse.getId());
            if (promoProductPriceQuotation != null) {
                orderProductResponse.setPrice(promoProductPriceQuotation);
                orderProductResponse.setTotal_begin_price(1.0 *
                        promoProductPriceQuotation *
                        orderProductResponse.getQuantity());
            }
            orderProductResponse.setTotal_csbh_price(orderProductResponse.getTotal_csbh_price());

            orderProductResponse.setTotal_end_price(
                    orderProductResponse.getTotal_begin_price()
                            - orderProductResponse.getTotal_csbh_price()
                            - orderProductResponse.getUu_dai_dam_me()
                            - orderProductResponse.getTotal_promo_price()
            );

            orderMoneyInfoResponse.setTotal_promotion_price_of_product(
                    orderMoneyInfoResponse.getTotal_promotion_price_of_product()
            );
            orderMoneyInfoResponse.setTotal_promotion_product_price_ctkm(
                    orderMoneyInfoResponse.getTotal_promotion_product_price_ctkm()
            );
            orderMoneyInfoResponse.setTong_uu_dai_dam_me(
                    orderMoneyInfoResponse.getTong_uu_dai_dam_me()

            );

            /**
             * Tổng tiền tạm tính
             */
            orderMoneyInfoResponse.setTotal_begin_price(
                    orderMoneyInfoResponse.getTotal_begin_price() +
                            orderProductResponse.getTotal_begin_price());
        }

        /**
         * Danh sách hàng tặng
         */
        for (PromoProductBasic promoProductBasic : resultEstimatePromo.getPromoGoodsOfferList()) {
            ProductCache productCache = this.dataManager.getProductManager().getMpProduct().get(promoProductBasic.getItem_id());
            if (productCache == null) {
                continue;
            }

            /**
             * Không tặng hàng bị ẩn
             */
            if (VisibilityType.HIDE.getId() == this.getProductVisibilityByAgency(
                    agencyEntity.getId(),
                    promoProductBasic.getItem_id()
            )) {
                continue;
            }

            ProductCache productPrice = this.getFinalPriceByAgency(
                    productCache.getId(),
                    agencyEntity.getId(),
                    agencyEntity.getCity_id(),
                    agencyEntity.getRegion_id(),
                    agencyEntity.getMembership_id());

            OrderProductRequest goodsSelect = this.getProductInGoodsSelect(promoProductBasic, request.getGoods());

            PromoProductBasic orderProductResponse = this.getProductInOrder(estimatePromo.getPromoProductInputList(), promoProductBasic.getItem_id());
            if (orderProductResponse == null) {
                return ClientResponse.success(null);
            }


            PromoProductBasic goods = new PromoProductBasic(
                    promoProductBasic.getItem_id(),
                    promoProductBasic.getItem_quantity(),
                    productPrice.getPrice() * 1L,
                    null
            );

            estimatePromo.getPromoGoodsSelectList().add(goods);

            ItemOfferResponse itemOfferResponse = this.convertGoodOfferResponse(
                    promoProductBasic,
                    productCache);
            itemOfferResponse.setPrice(productPrice.getPrice());
            int tonkho = this.getStockByMpStock(mpStock, itemOfferResponse.getId());
            Integer quantityUsed = ConvertUtils.toInt(mpProductQuantityUsed.get(itemOfferResponse.getId()));
            int quantity_hint = itemOfferResponse.getOffer_value();
            itemOfferResponse.setOffer_value(
                    ConvertUtils.toInt(resultEstimatePromo.getTotalMoneyGoodsOffer() /
                            productPrice.getPrice())
            );
            itemOfferResponse.setQuantity_select(
                    request.getGoods().size() == 0 ?
                            (request.getHas_gift_hint() == 0 ? 0 :
                                    promoProductBasic.getItem_quantity()) :
                            goodsSelect == null ? 0 :
                                    (goodsSelect.getProduct_quantity() <= itemOfferResponse.getOffer_value()) ?
                                            goodsSelect.getProduct_quantity() :
                                            request.getHas_gift_hint() == 1 ? promoProductBasic.getItem_quantity() : 0);

            if (tonkho - quantityUsed < itemOfferResponse.getQuantity_select()) {
                itemOfferResponse.setIs_error(1);
                itemOfferResponse.setNote("Tồn kho chỉ còn: " + this.appUtils.priceFormat(tonkho));
            }
            goodsForSelectList.add(itemOfferResponse);

            /**
             * Cập nhật số lượng sử dụng
             */
            mpProductQuantityUsed.put(itemOfferResponse.getId(), quantityUsed + itemOfferResponse.getQuantity_select());
        }

        /**
         * tong tien duoc doi qua
         */
        orderMoneyInfoResponse.setTotal_goods_offer_price(resultEstimatePromo.getTotalMoneyGoodsOffer());

        /**
         * Tính tổng tiền đã đổi quà nếu có
         */
        orderMoneyInfoResponse.setTotal_goods_offer_claimed_price(
                goodsForSelectList.stream().reduce(0L, (total, object) -> total +
                        ConvertUtils.toLong((object.getQuantity_select() * object.getPrice())), Long::sum));

        /**
         * Tổng tiền đổi hàng không vượt quá số tiền được tặng
         */
//        if (orderMoneyInfoResponse.getTotal_goods_offer_price() < orderMoneyInfoResponse.getTotal_goods_offer_claimed_price()) {
//            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_GOODS_OFFER_OVERLOAD_PRICE);
//        }

        double total_goods_offer_price_remain = orderMoneyInfoResponse.getTotal_goods_offer_price()
                - orderMoneyInfoResponse.getTotal_goods_offer_claimed_price();
        orderMoneyInfoResponse.setTotal_goods_offer_remain_price(total_goods_offer_price_remain);

        long total_refund_price = 0;

        /**
         * Tặng hàng/Tặng quà
         */
        for (PromoGiftClaim promoProductBasic : resultEstimatePromo.getPromoGiftClaimList()) {
            ProductCache productCache = this.dataManager.getProductManager().getMpProduct().get(promoProductBasic.getItem_id());
            if (productCache == null) {
                continue;
            }

            /**
             * Không tặng hàng bị ẩn
             */
            if (VisibilityType.HIDE.getId() == this.getProductVisibilityByAgency(
                    agencyEntity.getId(),
                    promoProductBasic.getItem_id()
            )) {
                continue;
            }

            ItemOfferResponse itemOfferResponse = this.convertItemOfferResponse(promoProductBasic, productCache);
            int tonKho = this.getStockByMpStock(mpStock, itemOfferResponse.getId());
            Integer quantityUsed = ConvertUtils.toInt(mpProductQuantityUsed.get(itemOfferResponse.getId()));
            if (tonKho - quantityUsed < itemOfferResponse.getOffer_value()) {
                itemOfferResponse.setIs_error(1);
                itemOfferResponse.setNote("Tồn kho chỉ còn: " + this.appUtils.priceFormat((tonKho)));
            }

            int quantity = Math.min(itemOfferResponse.getOffer_value(), Math.max(0, tonKho - quantityUsed));
            if (quantity > 0) {
                itemOfferResponse.setReal_value(quantity);
                mpProductQuantityUsed.put(itemOfferResponse.getId(), quantityUsed + quantity);
            } else {
                itemOfferResponse.setReal_value(0);
            }

            goodsClaimList.add(itemOfferResponse);


            if (promoProductBasic.getItem_quantity() - quantity > 0 &&
                    PromoOfferType.GOODS_OFFER.getKey().equals(itemOfferResponse.getOffer_type())) {
                ProductCache productPrice = this.getFinalPriceByAgency(
                        itemOfferResponse.getId(),
                        agencyEntity.getId(),
                        agencyEntity.getCity_id(),
                        agencyEntity.getRegion_id(),
                        agencyEntity.getMembership_id());
                long price = ConvertUtils.toLong(productPrice == null ? 0 :
                        productPrice.getPrice() < 0 ? 0 : productPrice.getPrice());
                total_refund_price += (promoProductBasic.getItem_quantity() - quantity) * price;
            }
        }

        /**
         * Giảm giá đơn hàng bởi CSBH
         */
        orderMoneyInfoResponse.setTotal_promotion_price_of_order(
                resultEstimatePromo.getTotalMoneyDiscountOrderOfferByCSBH())
        ;

        /**
         * Giảm giá đơn hàng bởi CTKM
         */
        orderMoneyInfoResponse.setTotal_promotion_order_price_ctkm(
                resultEstimatePromo.getTotalMoneyDiscountOrderOfferByCTKM());

        /**
         * tong giam
         */
        orderMoneyInfoResponse.setTotal_promotion_price(
                orderMoneyInfoResponse.getTotal_promotion_product_price_ctkm()
                        + orderMoneyInfoResponse.getTotal_promotion_order_price_ctkm());

        /**
         * Tính toán tổng tiền hoàn nếu có
         * - chưa check tồn kho nên không có hoàn tiền
         */
        orderMoneyInfoResponse.setTotal_refund_price(total_refund_price);

        /**
         * Tính toán voucher
         */
        long max_voucher_price = 0;
        long total_voucher_price = 0;
        List<JSONObject> voucher_gift_list = new ArrayList<>();
        List<JSONObject> voucher_money_list = new ArrayList<>();
        total_voucher_price = this.convertVoucherData(
                voucher_money_list,
                voucher_gift_list,
                request.getVouchers(),
                max_voucher_price,
                mpProductQuantityUsed);
        orderMoneyInfoResponse.setTotal_voucher_price(total_voucher_price);

        /**
         * Tổng tiền đơn hàng
         */
        orderMoneyInfoResponse.setTotal_end_price(
                orderMoneyInfoResponse.getTotal_begin_price()
                        - orderMoneyInfoResponse.getTotal_promotion_price_of_product()
                        - orderMoneyInfoResponse.getTong_uu_dai_dam_me()
                        - orderMoneyInfoResponse.getTotal_promotion_price_of_order()
                        - orderMoneyInfoResponse.getTotal_promotion_price()
                        - orderMoneyInfoResponse.getTotal_refund_price()
                        - orderMoneyInfoResponse.getTotal_voucher_price()
        );

        /**
         * CSBH/CTKM cho san pham
         */
        Map<Integer, PromoBasicData> mpPromoGood = new ConcurrentHashMap<>();

        /**
         * CSBH của sản phẩm
         */
        List<PromoProductBasicData> promoProducts = new ArrayList<>();
        for (Map.Entry<Integer, List<Program>> entry : resultEstimatePromo.getMpPromoProductCSBH().entrySet()) {
            for (Program program : entry.getValue()) {
                PromoProductBasicData promoBasicData = new PromoProductBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(
                        this.getCmsProgramDescriptionForProduct(
                                this.dataManager.getProgramManager().getAgency(agencyEntity.getId()),
                                entry.getKey(),
                                program));
                promoBasicData.setProduct_id(entry.getKey());
                promoProducts.add(promoBasicData);

                PromoBasicData promoGood = new PromoBasicData();
                promoGood.setId(promoBasicData.getId());
                promoGood.setName(promoBasicData.getName());
                promoGood.setCode(promoBasicData.getCode());
                promoGood.setDescription(promoBasicData.getDescription());
                mpPromoGood.put(promoBasicData.getId(), promoGood);
            }
        }

        Map<Integer, PromoBasicData> mpCSDM = new ConcurrentHashMap<>();
        /**
         * CSDM sản phẩm
         */
        for (Map.Entry<Integer, List<Program>> entry : resultEstimatePromo.getMpPromoProductCSDM().entrySet()) {
            for (Program program : entry.getValue()) {
                PromoProductBasicData promoBasicData = new PromoProductBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(
                        this.getCmsCSDMDescriptionForProduct(
                                program.getOfferPercent()
                        )
                );
                promoBasicData.setProduct_id(entry.getKey());
                promoProducts.add(promoBasicData);

                PromoBasicData promoGood = new PromoBasicData();
                promoGood.setId(promoBasicData.getId());
                promoGood.setName(promoBasicData.getName());
                promoGood.setCode(promoBasicData.getCode());
                promoGood.setDescription(promoBasicData.getDescription());
                mpCSDM.put(promoBasicData.getId(), promoGood);
            }
        }

        /**
         * CSBH cho đơn hàng
         */
        List<PromoBasicData> promoOrders = new ArrayList<>();
        for (Program program : resultEstimatePromo.getCsbhOrderList()) {
            PromoBasicData promoBasicData = new PromoBasicData();
            promoBasicData.setId(program.getId());
            promoBasicData.setName(program.getName());
            promoBasicData.setCode(program.getCode());
            promoBasicData.setDescription(program.getDescription());
            promoOrders.add(promoBasicData);
        }

        Map<Integer, PromoBasicData> mpCTKM = new ConcurrentHashMap<>();

        /**
         * CTKM cho san pham
         */
        for (List<Program> programs : resultEstimatePromo.getMpPromoProductCTKM().values()) {
            for (Program program : programs) {
                PromoBasicData promoBasicData = new PromoBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(program.getDescription());
                mpCTKM.put(promoBasicData.getId(), promoBasicData);

                mpPromoGood.put(promoBasicData.getId(), promoBasicData);
            }
        }

        /**
         * thêm ctkm sản phẩm vào hộp quà
         */
        for (Map.Entry<Integer, List<Program>> entry : resultEstimatePromo.getMpPromoProductCTKM().entrySet()) {
            for (Program program : entry.getValue()) {
                PromoProductBasicData promoBasicData = new PromoProductBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(
                        this.getCmsProgramDescriptionForProduct(
                                this.dataManager.getProgramManager().getAgency(agencyEntity.getId()),
                                entry.getKey(),
                                program));
                promoBasicData.setProduct_id(entry.getKey());
                promoProducts.add(promoBasicData);
            }
        }

        /**
         * CTKM cho don hang
         */
        for (Program program : resultEstimatePromo.getCtkmOrderList()) {
            PromoBasicData promoBasicData = new PromoBasicData();
            promoBasicData.setId(program.getId());
            promoBasicData.setName(program.getName());
            promoBasicData.setCode(program.getCode());
            promoBasicData.setDescription(program.getDescription());
            mpCTKM.put(promoBasicData.getId(), promoBasicData);
        }

        OrderDeptInfo orderDeptInfo = new OrderDeptInfo();
        long hmkd = ConvertUtils.toLong(
                this.getAgencyHMKD(
                        ConvertUtils.toDouble(deptAgencyInfoEntity.getDept_limit()),
                        ConvertUtils.toDouble(deptAgencyInfoEntity.getNgd_limit()),
                        ConvertUtils.toDouble(deptAgencyInfoEntity.getCurrent_dept()),
                        this.orderDB.getTotalPriceOrderDoing(request.getAgency_id())));
        orderDeptInfo.setCno(deptAgencyInfoEntity.getCurrent_dept());
        orderDeptInfo.setNgd_limit(deptAgencyInfoEntity.getNgd_limit());
        orderDeptInfo.setNqh_order(deptAgencyInfoEntity.getNqh());
        orderDeptInfo.setNqh_current(deptAgencyInfoEntity.getNqh());
        orderDeptInfo.setHmkd(hmkd < 0 ? 0 : hmkd);
        long hmkd_over_order = ConvertUtils.toLong(hmkd - orderMoneyInfoResponse.getTotal_end_price());
        orderDeptInfo.setHmkd_over_order(
                hmkd_over_order < 0 ? hmkd_over_order * -1 : 0);
        orderDeptInfo.setHmkd_over_current(hmkd < 0 ? hmkd * -1 : 0);

        JSONObject data = new JSONObject();
        data.put("order_money", orderMoneyInfoResponse);
        data.put("products", products);
        data.put("goods_for_select", goodsForSelectList);
        data.put("goods_claim", goodsClaimList);
        data.put("promo_products", promoProducts);
        data.put("promo_orders", promoOrders);
        data.put("promo_goods", new ArrayList<>(mpPromoGood.values()));
        data.put("ctkm_orders", new ArrayList<>(mpCTKM.values()));
        data.put("dept_info", orderDeptInfo);

        data.put("hunt_sale_orders", hunt_sale_orders);
        data.put("hunt_sale_hints", this.getHuntSaleHint());

        long total_money_order = ConvertUtils.toLong(
                orderMoneyInfoResponse.getTotal_end_price()
                        + hunt_sale_orders.stream().reduce(0L, (total, object) -> total + (object.getTotal_end_price()), Long::sum));
        data.put("total_money_order", total_money_order);

        data.put("hunt_sale_products", huntSaleProductList);

        /**
         * Thông tin voucher
         */
        data.put("voucher_gift", voucher_gift_list);
        data.put("voucher_money", voucher_money_list);
        data.put("max_voucher_price", max_voucher_price);
        data.put("max_voucher_percent", ConvertUtils.toInt(
                this.dataManager.getConfigManager().getMPConfigData().get(CTXHConstants.MAX_PERCENT_OF_VOUCHER_PER_ORDER)));
        data.put("vouchers", request.getVouchers());

        /**
         * csdm_orders
         */
        data.put("csdm_orders", new ArrayList<>(mpCSDM.values()));

        return ClientResponse.success(data);
    }

    private Map<Integer, Double> parseProductPrice(List<HuntSaleOrder> huntSaleOrders) {
        Map<Integer, Double> mpProductPrice = new LinkedHashMap<>();
        try {
            for (HuntSaleOrder huntSaleOrder : huntSaleOrders) {
                for (HuntSaleOrderDetail huntSaleOrderDetail : huntSaleOrder.getProducts()) {
                    mpProductPrice.put(huntSaleOrderDetail.getId(),
                            this.appUtils.roundPrice(ConvertUtils.toDouble(huntSaleOrderDetail.getTotal_end_price() / huntSaleOrderDetail.getQuantity())));
                }
            }
        } catch (Exception exception) {
            LogUtil.printDebug(exception.getMessage());
        }
        return mpProductPrice;
    }

    private double getMaxVoucherPrice(double totalBeginPrice) {
        int max_percent_per_order = ConvertUtils.toInt(
                this.dataManager.getConfigManager().getMPConfigData().get(CTXHConstants.MAX_PERCENT_OF_VOUCHER_PER_ORDER));
        if (max_percent_per_order == 0) {
            return totalBeginPrice;
        }
        return this.appUtils.roundPrice(totalBeginPrice * max_percent_per_order * 1F / 100);
    }

    private long convertVoucherData(
            List<JSONObject> voucherMoneyList,
            List<JSONObject> voucherGiftList,
            List<Integer> vouchers,
            double max_voucher_price,
            Map<Integer, Integer> mpProductQuantityUsed) {
        try {
            long total_voucher_data = 0;
            for (Integer voucher_id : vouchers) {
                JSONObject voucher = this.orderDB.getVoucher(voucher_id);
                VoucherData voucherData = JsonUtils.DeSerialize(JsonUtils.Serialize(voucher), VoucherData.class);
                if (voucherData == null) {
                    continue;
                }
                if (VoucherOfferType.GIFT_OFFER.getKey().equals(voucherData.getOffer_type())) {
                    voucher.put("products", this.convertVoucherProductData(voucherData.getItems(), mpProductQuantityUsed));
                    voucherGiftList.add(voucher);
                } else if (VoucherOfferType.MONEY_DISCOUNT.getKey().equals(voucherData.getOffer_type())) {
                    voucherMoneyList.add(voucher);
                    total_voucher_data += voucherData.getTotal_value();
                }
            }

            if (max_voucher_price < total_voucher_data) {
                total_voucher_data = ConvertUtils.toLong(max_voucher_price);
            }

            return total_voucher_data;
        } catch (Exception e) {
            this.alertToTelegram(e.getMessage(), ResponseStatus.FAIL);
        }
        return 0;
    }

    private List<JSONObject> convertVoucherProductData(String items, Map<Integer, Integer> mpProductQuantityUsed) {
        try {
            if (items == null) {
                return null;
            }
            List<JSONObject> products = JsonUtils.DeSerialize(items, new TypeToken<List<JSONObject>>() {
            }.getType());
            for (JSONObject product : products) {
                int item_id = ConvertUtils.toInt(product.get("item_id"));
                int item_quantity = ConvertUtils.toInt(product.get("item_quantity"));
                int real_value = item_quantity;
                int offer_value = item_quantity;
                product.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                        item_id));
                int stock = this.getTonKho(item_id);
                Integer quantity_used = mpProductQuantityUsed.get(item_id);
                if (quantity_used == null) {
                    quantity_used = 0;
                }
                if (stock - quantity_used < item_quantity) {
                    real_value = stock - quantity_used;
                    item_quantity = real_value;
                    product.put("is_error", 1);
                    product.put("note", "Tồn kho chỉ còn: " + this.appUtils.priceFormat(real_value));
                }

                mpProductQuantityUsed.put(item_id, real_value);
                product.put("item_quantity", item_quantity);
                product.put("offer_value", offer_value);
                product.put("real_value", real_value);
            }
            return products;
        } catch (Exception e) {
            this.alertToTelegram(e.getMessage(), ResponseStatus.FAIL);
        }
        return null;
    }

    private long getTotalVoucherPrice(OrderSummaryInfoResponse orderMoneyInfoResponse, List<Integer> vouchers) {
        return vouchers.size() > 0 ? 100000 : 0;
    }

    private List<HuntSaleOrderDetail> checkOrder(
            int agency_id,
            List<HuntSaleOrderDetail> hunt_sale_products,
            Map<Integer, ProductCache> mpProductCache,
            Map<Integer, Integer> mpStock) {
        List<HuntSaleOrderDetail> huntSaleOrderDetailList = new ArrayList<>();
        for (HuntSaleOrderDetail huntSaleOrderDetail : hunt_sale_products) {
            if (huntSaleOrderDetail.getIs_combo() == 1) {
                JSONObject jsCombo = this.promoDB.getComboInfo(huntSaleOrderDetail.getId());
                HuntSaleOrderDetail huntSaleOrderDetailInput = this.convertComboHuntSale(
                        huntSaleOrderDetail.getId(),
                        jsCombo,
                        huntSaleOrderDetail.getQuantity()
                );
                List<ProductData> productDataList = JsonUtils.DeSerialize(
                        jsCombo.get("data").toString(),
                        new TypeToken<List<ProductData>>() {
                        }.getType()
                );

                boolean isPriceContact = false;
                for (ProductData productInCombo : productDataList) {
                    ProductCache productCache = mpProductCache.get(productInCombo.getId());
                    ProductData productData = new ProductData(
                            productCache.getId(),
                            productCache.getCode(),
                            productCache.getFull_name(),
                            productInCombo.getQuantity(),
                            productCache.getImages(),
                            productCache.getPrice()
                    );
                    if (productData.getQuantity() > mpStock.get(productData.getId())) {
                        productData.setIs_error(1);
                        productData.setNote("Tồn kho chỉ còn " + this.appUtils.numberFormat(
                                mpStock.get(productData.getId())
                        ));
                        huntSaleOrderDetailInput.setIs_error(1);
                        huntSaleOrderDetailInput.setNote("Tồn kho không đủ");
                    }
                    huntSaleOrderDetailInput.getProducts().add(
                            productData
                    );

                    if (!isPriceContact && productCache.getPrice() <= 0) {
                        isPriceContact = true;
                        productData.setIs_error(1);
                        productData.setNote("Sản phẩm có giá liên hệ");
                    }
                }
                huntSaleOrderDetailInput.setIs_hunt_sale(
                        this.checkHuntSale(
                                agency_id,
                                huntSaleOrderDetailInput.getId(),
                                huntSaleOrderDetailInput.getIs_combo())
                );
                if (!isPriceContact) {
                    huntSaleOrderDetailInput.calculateTotalMoney();
                } else {
                    huntSaleOrderDetailInput.setPriceContact();
                    huntSaleOrderDetailInput.setIs_error(1);
                    huntSaleOrderDetailInput.setNote("Sản phẩm có giá liên hệ");
                }
                huntSaleOrderDetailList.add(huntSaleOrderDetailInput);
            } else {
                HuntSaleOrderDetail huntSaleOrderDetailInput = this.convertProductHuntSale(
                        huntSaleOrderDetail.getId(),
                        huntSaleOrderDetail.getQuantity()
                );
                ProductCache productPrice = mpProductCache.get(huntSaleOrderDetail.getId());
                if (productPrice.getPrice() < 0) {
                    huntSaleOrderDetailInput.setPrice(0);
                    huntSaleOrderDetailInput.setIs_error(1);
                    huntSaleOrderDetailInput.setNote("Sản phẩm có giá liên hệ");
                } else {
                    huntSaleOrderDetailInput.setPrice(productPrice.getPrice());
                }

                huntSaleOrderDetailInput.setTotal_begin_price(
                        huntSaleOrderDetailInput.getQuantity() * huntSaleOrderDetailInput.getPrice()
                );
                huntSaleOrderDetailInput.setIs_hunt_sale(
                        this.checkHuntSale(
                                agency_id,
                                huntSaleOrderDetailInput.getId(),
                                huntSaleOrderDetailInput.getIs_combo())
                );

                if (huntSaleOrderDetailInput.getQuantity() > mpStock.get(huntSaleOrderDetailInput.getId())) {
                    huntSaleOrderDetailInput.setIs_error(1);
                    huntSaleOrderDetailInput.setNote("Tồn kho chỉ còn " + this.appUtils.numberFormat(
                            mpStock.get(huntSaleOrderDetailInput.getId())
                    ));
                }

                huntSaleOrderDetailList.add(huntSaleOrderDetailInput);
            }
        }

        return huntSaleOrderDetailList;
    }

    private int checkHuntSale(int agency_id, int item_id, int is_combo) {
        try {
            Agency agency = this.dataManager.getProgramManager().getAgency(agency_id);
            if (agency == null) {
                return 0;
            }
            if (is_combo == 0) {
                List<JSONObject> promoList = this.promoDB.getPromoHuntSaleRunningByProduct(item_id, is_combo);
                for (JSONObject promo : promoList) {
                    Program program = this.getProgramById(
                            ConvertUtils.toInt(promo.get("id"))
                    );
                    if (program == null) {
                        continue;
                    }
                    if (this.checkProgramVisibility(
                            agency,
                            program
                    )) {
                        return 1;
                    }
                }
            } else {
                List<JSONObject> promoList = this.promoDB.getPromoHuntSaleRunningByCombo(item_id);
                for (JSONObject promo : promoList) {
                    Program program = this.getProgramById(
                            ConvertUtils.toInt(promo.get("id"))
                    );
                    if (program == null) {
                        continue;
                    }
                    if (this.checkProgramVisibility(
                            agency,
                            program
                    )) {
                        return 1;
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return 0;
    }


    private Map<Integer, Integer> initMpStock(Map<Integer, ProgramOrderProduct> mpAllProduct) {
        Map<Integer, Integer> mpStock = new ConcurrentHashMap<>();
        for (ProgramOrderProduct programOrderProduct : mpAllProduct.values()) {
            mpStock.put(programOrderProduct.getProductId(), this.getTonKho(programOrderProduct.getProductId()));
        }
        return mpStock;
    }

    private Map<Integer, ProgramOrderProduct> initMpProductHuntSale(
            AgencyEntity agencyEntity,
            Map<Integer, ProductCache> mpProductCache,
            List<HuntSaleOrderDetail> hunt_sale_products,
            List<OrderProductRequest> products) {
        Map<Integer, ProgramOrderProduct> mpProgramOrderProduct = new ConcurrentHashMap<>();
        for (HuntSaleOrderDetail huntSaleOrderDetail : hunt_sale_products) {
            if (huntSaleOrderDetail.getIs_combo() == YesNoStatus.YES.getValue()) {
                JSONObject jsCombo = this.promoDB.getComboInfo(huntSaleOrderDetail.getId());
                List<ProductData> productDataList = JsonUtils.DeSerialize(
                        jsCombo.get("data").toString(),
                        new TypeToken<List<ProductData>>() {
                        }.getType()
                );
                for (ProductData productData : productDataList) {
                    this.parseProgramOrderProduct(
                            mpProductCache,
                            agencyEntity,
                            mpProgramOrderProduct,
                            productData.getId(),
                            productData.getQuantity() * huntSaleOrderDetail.getQuantity());
                }
            } else {
                this.parseProgramOrderProduct(
                        mpProductCache,
                        agencyEntity,
                        mpProgramOrderProduct,
                        huntSaleOrderDetail.getId(),
                        huntSaleOrderDetail.getQuantity());
            }
        }

        if (products == null) {
            return mpProgramOrderProduct;
        }

        for (OrderProductRequest orderProductRequest : products) {
            this.parseProgramOrderProduct(
                    mpProductCache,
                    agencyEntity,
                    mpProgramOrderProduct,
                    orderProductRequest.getProduct_id(),
                    orderProductRequest.getProduct_quantity());
        }

        return mpProgramOrderProduct;
    }

    private void parseProgramOrderProduct(
            Map<Integer, ProductCache> mpProductCache,
            AgencyEntity agencyEntity,
            Map<Integer, ProgramOrderProduct> mpProgramOrderProduct,
            int product_id,
            int product_quantity) {
        ProgramOrderProduct programOrderProduct = mpProgramOrderProduct.get(
                product_id
        );
        if (programOrderProduct == null) {
            programOrderProduct = new ProgramOrderProduct();
            programOrderProduct.setProductId(product_id);
            ProductCache productBasicData = this.dataManager.getProductManager().getProductBasicData(
                    product_id
            );
            if (productBasicData == null) {
                return;
            }

            ProductCache productPrice = mpProductCache.get(productBasicData.getId());
            if (productPrice == null) {
                productPrice = this.getFinalPriceByAgency(
                        productBasicData.getId(),
                        agencyEntity.getId(),
                        agencyEntity.getCity_id(),
                        agencyEntity.getRegion_id(),
                        agencyEntity.getMembership_id());

                ProductCache productData = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(productBasicData),
                        ProductCache.class
                );
                productData.setPrice(productPrice.getPrice());
                productData.setMinimum_purchase(productPrice.getMinimum_purchase());
                mpProductCache.put(productData.getId(), productData);
            }
            if (productPrice == null) {
                return;
            }
            programOrderProduct.setProductPrice(productPrice.getPrice());
            programOrderProduct.setProduct(
                    this.dataManager.getProgramManager().getProductById(product_id, "")
            );

            programOrderProduct.getProduct().setPrice(productPrice.getPrice());
            programOrderProduct.setCommonPrice(productBasicData.getCommon_price());
            programOrderProduct.setProductQuantity(
                    product_quantity
            );
        } else {
            programOrderProduct.setProductQuantity(
                    programOrderProduct.getProductQuantity() + product_quantity
            );
        }

        programOrderProduct.setBeginPrice(
                programOrderProduct.getProductPrice() * programOrderProduct.getProductQuantity()
        );
        mpProgramOrderProduct.put(programOrderProduct.getProduct().getId(), programOrderProduct);
    }

    private List<HuntSaleOrderDetail> getHuntSaleHint() {
        return new ArrayList<>();
    }

    private EstimatePromo createEstimatePromoHuntSale(AgencyEntity agencyEntity,
                                                      List<HuntSaleOrderDetail> hunt_sale_products) {
        EstimatePromo estimatePromo = new EstimatePromo();
        try {
            for (HuntSaleOrderDetail huntSaleOrderDetail : hunt_sale_products) {
                if (huntSaleOrderDetail.getIs_combo() == YesNoStatus.NO.getValue()) {
                    ProductCache productPrice = this.getFinalPriceByAgency(
                            huntSaleOrderDetail.getId(),
                            agencyEntity.getId(),
                            agencyEntity.getCity_id(),
                            agencyEntity.getRegion_id(),
                            agencyEntity.getMembership_id()
                    );
                    if (productPrice.getPrice() <= 0) {
                        productPrice.setPrice(0);
                    }
                    estimatePromo.getPromoProductInputList().add(
                            new PromoProductBasic(
                                    huntSaleOrderDetail.getId(),
                                    huntSaleOrderDetail.getQuantity(),
                                    productPrice.getPrice() * 1.0,
                                    null)
                    );
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return estimatePromo;
    }

    private List<HuntSaleOrder> estimateHuntSaleOrder(int agency_id,
                                                      EstimatePromo estimatePromo,
                                                      int source,
                                                      List<HuntSaleOrderDetail> hunt_sale_products,
                                                      Map<Integer, ProgramOrderProduct> mpProgramOrderProduct,
                                                      Map<Integer, ProgramOrderProduct> mpAllProduct,
                                                      Map<Integer, Integer> mpProductQuantityUsed,
                                                      Map<Integer, ProductCache> mpProductCache,
                                                      Map<Integer, Integer> mpStock,
                                                      DeptAgencyInfoEntity deptAgencyInfoEntity,
                                                      Agency agencyProgram,
                                                      AgencyEntity agencyEntity
    ) {
        List<HuntSaleOrder> hunt_sale_orders = new ArrayList<>();
        Map<Integer, ProgramSanSaleOffer> mpResponseProgramSanSaleOffer = new ConcurrentHashMap<>();
        try {
            this.checkSanSaleForProduct(
                    agencyProgram,
                    mpProgramOrderProduct,
                    source == Source.APP.getValue() ? Source.APP : Source.WEB,
                    mpResponseProgramSanSaleOffer
            );

            List<HuntSaleOrderDetail> huntSaleOrderDetailList = new ArrayList<>();
            for (HuntSaleOrderDetail huntSaleOrderDetail : hunt_sale_products) {
                if (huntSaleOrderDetail.getIs_combo() == 1) {
                    JSONObject jsCombo = this.promoDB.getComboInfo(huntSaleOrderDetail.getId());
                    HuntSaleOrderDetail huntSaleOrderDetailInput = this.convertComboHuntSale(
                            huntSaleOrderDetail.getId(),
                            jsCombo,
                            huntSaleOrderDetail.getQuantity()
                    );
                    List<ProductData> productDataList = JsonUtils.DeSerialize(
                            jsCombo.get("data").toString(),
                            new TypeToken<List<ProductData>>() {
                            }.getType()
                    );
                    for (ProductData productData : productDataList) {
                        ProductCache productCache = mpProductCache.get(productData.getId());
                        huntSaleOrderDetail.getProducts().add(
                                new ProductData(
                                        productCache.getId(),
                                        productCache.getCode(),
                                        productCache.getFull_name(),
                                        productData.getQuantity(),
                                        productCache.getImages(),
                                        productCache.getPrice()
                                )
                        );
                    }

                    huntSaleOrderDetail.calculateTotalMoney();
                    huntSaleOrderDetailList.add(huntSaleOrderDetailInput);
                } else {
                    HuntSaleOrderDetail huntSaleOrderDetailInput = this.convertProductHuntSale(
                            huntSaleOrderDetail.getId(),
                            huntSaleOrderDetail.getQuantity()
                    );
                    ProductCache productPrice = mpProductCache.get(huntSaleOrderDetail.getId());
                    if (productPrice.getPrice() < 0) {
                        huntSaleOrderDetailInput.setPrice(0);
                    } else {
                        huntSaleOrderDetailInput.setPrice(productPrice.getPrice());
                    }

                    huntSaleOrderDetailInput.setTotal_begin_price(
                            huntSaleOrderDetailInput.getQuantity() * huntSaleOrderDetailInput.getPrice()
                    );
                    huntSaleOrderDetailList.add(huntSaleOrderDetailInput);
                }
            }


            /**
             * Add số lượng sử dụng (trừ số lượng còn lại)
             */
            for (ProgramOrderProduct programOrderProduct : mpAllProduct.values()) {
                int quantity = programOrderProduct.getProductQuantity();
                ProgramOrderProduct remain = mpProgramOrderProduct.get(programOrderProduct.getProduct().getId());
                if (remain != null) {
                    quantity -= remain.getProductQuantity();
                }

                Integer quantityUsed = mpProductQuantityUsed.get(programOrderProduct.getProduct().getId());
                if (quantityUsed == null) {
                    quantityUsed = 0;
                }
                quantityUsed += quantity;
                mpProductQuantityUsed.put(programOrderProduct.getProduct().getId(), quantityUsed);
            }

            for (Map.Entry<Integer, ProgramSanSaleOffer> entry : mpResponseProgramSanSaleOffer.entrySet()) {
                HuntSaleOrder huntSaleOrder = new HuntSaleOrder();
                Program program = this.getProgramById(entry.getKey());
                huntSaleOrder.setPriority(program.getPriority());
                huntSaleOrder.setPromo_id(program.getId());
                huntSaleOrder.setPromo_code(program.getCode());
                huntSaleOrder.setPromo_name(program.getName());
                huntSaleOrder.setDept_cycle(
                        program.getDeptCycle() > deptAgencyInfoEntity.getDept_cycle()
                                ? deptAgencyInfoEntity.getDept_cycle() :
                                program.getDeptCycle() == -1 ? deptAgencyInfoEntity.getDept_cycle() : program.getDeptCycle()
                );
                huntSaleOrder.setPromo_description(program.getDescription());
                ProgramSanSaleOffer programSanSaleOffer = entry.getValue();
                Map<Integer, HuntSaleOrderDetail> mpCombo = new ConcurrentHashMap<>();
                Map<Integer, Integer> mpAllProductSanSale = new ConcurrentHashMap<>();
                mpAllProductSanSale.putAll(programSanSaleOffer.getMpProductQuantity());
                for (Map.Entry<Integer, Integer> entryCombo : programSanSaleOffer.getMpComboQuantity().entrySet()) {
                    int combo_id = entryCombo.getKey();
                    LogUtil.printDebug("combo_id" + combo_id);
                    int combo_quantity = entryCombo.getValue();
                    JSONObject jsCombo = this.promoDB.getComboInfo(
                            combo_id);
                    if (jsCombo == null) {
                        continue;
                    }
                    HuntSaleOrderDetail huntSaleOrderDetail = this.convertComboHuntSale(
                            combo_id,
                            jsCombo,
                            combo_quantity
                    );
                    huntSaleOrderDetail.setIs_hunt_sale(YesNoStatus.YES.getValue());
                    List<ProductData> productDataInComboList = JsonUtils.DeSerialize(
                            jsCombo.get("data").toString(),
                            new TypeToken<List<ProductData>>() {
                            }.getType()
                    );
                    List<ProductData> productDataList = new ArrayList<>();
                    for (ProductData productInCombo : productDataInComboList) {
                        ProductData productData =
                                JsonUtils.DeSerialize(
                                        JsonUtils.Serialize(mpProductCache.get(productInCombo.getId())),
                                        ProductData.class
                                );
                        productData.setId(productInCombo.getId());
                        productData.setQuantity(productInCombo.getQuantity());
                        productData.setPrice(
                                this.getSanSalePrice(
                                        program.getApplyPrivatePrice(),
                                        mpProductCache.get(productData.getId()).getPrice(),
                                        mpProductCache.get(productData.getId()).getCommon_price()
                                )
                        );
                        productData.setTotal_begin_price(
                                productData.getQuantity() * productData.getPrice()
                        );

                        Double total_money_discount = 0.0;
                        Double fixedPrice = programSanSaleOffer.getMpProductFixedPrice().get(productData.getId());
                        if (fixedPrice != null) {
                            total_money_discount += (productData.getPrice() - fixedPrice) * productData.getQuantity();
                        }

                        Integer percentDiscount = programSanSaleOffer.getMpProductPercentDiscount().get(
                                productData.getId());
                        if (percentDiscount != null) {
                            total_money_discount += productData.getQuantity()
                                    * (this.appUtils.roundPrice(productData.getPrice() * (percentDiscount * 1.0F / 100)));
                        }

                        Double moneyDiscount = programSanSaleOffer.getMpProductMoneyDiscount().get(
                                productData.getId()
                        );
                        if (moneyDiscount != null) {
                            total_money_discount += moneyDiscount * productData.getQuantity();
                        }
                        productData.setTotal_begin_price(
                                productData.getPrice()
                                        * productData.getQuantity()
                        );

                        productData.setTotal_promo_price(
                                total_money_discount
                        );

                        productData.setTotal_end_price(
                                productData.getTotal_begin_price()
                                        - productData.getTotal_promo_price()
                        );

                        int stock = getStockByMpStock(mpStock, productData.getId());
                        int quantityUsed = ConvertUtils.toInt(mpProductQuantityUsed.get(productData.getId()));
                        if (stock <
                                (combo_quantity * productData.getQuantity()) + quantityUsed) {
                            productData.setIs_error(1);
                            productData.setNote("Tồn kho chỉ còn " + this.appUtils.numberFormat(stock));

                            huntSaleOrderDetail.setIs_error(1);
                            huntSaleOrderDetail.setNote("Tồn kho không đủ");
                        }

                        /**
                         * Thêm vào số lượng đã sử dụng
                         */
                        mpProductQuantityUsed.put(productData.getId(),
                                quantityUsed + (productData.getQuantity() * combo_quantity));

                        productDataList.add(productData);
                        mpAllProductSanSale.remove(productData.getId());
                    }


                    huntSaleOrderDetail.setProducts(productDataList);
                    huntSaleOrderDetail.calculateTotalMoney();

                    String combo_note = program.getMpCombo().get(huntSaleOrderDetail.getId()).getNote();
                    huntSaleOrderDetail.setPromo_description(
                            combo_note.isEmpty() ? huntSaleOrder.getPromo_description() : combo_note
                    );

                    mpCombo.put(combo_id, huntSaleOrderDetail);
                    huntSaleOrder.getProducts().add(huntSaleOrderDetail);
                }

                for (Map.Entry<Integer, Integer> entryProduct : mpAllProductSanSale.entrySet()) {
                    HuntSaleOrderDetail huntSaleOrderDetail = this.convertProductHuntSale(
                            entryProduct.getKey(),
                            entryProduct.getValue());
                    huntSaleOrderDetail.setIs_hunt_sale(YesNoStatus.YES.getValue());
                    huntSaleOrderDetail.setPrice(this.getSanSalePrice(
                                    program.getApplyPrivatePrice(),
                                    mpProductCache.get(huntSaleOrderDetail.getId()).getPrice(),
                                    mpProductCache.get(huntSaleOrderDetail.getId()).getCommon_price()
                            )
                    );


                    Double total_money_discount = 0.0;
                    Double fixedPrice = programSanSaleOffer.getMpProductFixedPrice().get(huntSaleOrderDetail.getId());
                    if (fixedPrice != null) {
                        total_money_discount += (huntSaleOrderDetail.getPrice() - fixedPrice) * huntSaleOrderDetail.getQuantity();
                    }

                    Integer percentDiscount = programSanSaleOffer.getMpProductPercentDiscount().get(huntSaleOrderDetail.getId());
                    if (percentDiscount != null) {
                        total_money_discount += huntSaleOrderDetail.getQuantity()
                                * (this.appUtils.roundPrice(huntSaleOrderDetail.getPrice() * percentDiscount * 1.0F / 100));
                    }

                    Double moneyDiscount = programSanSaleOffer.getMpProductMoneyDiscount().get(
                            huntSaleOrderDetail.getId()
                    );
                    if (moneyDiscount != null) {
                        total_money_discount += moneyDiscount * huntSaleOrderDetail.getQuantity();
                    }

                    huntSaleOrderDetail.setTotal_promo_price(
                            total_money_discount
                    );

                    huntSaleOrderDetail.setTotal_begin_price(
                            huntSaleOrderDetail.getQuantity() * huntSaleOrderDetail.getPrice()
                    );

                    huntSaleOrderDetail.setTotal_end_price(
                            huntSaleOrderDetail.getTotal_begin_price()
                                    - huntSaleOrderDetail.getTotal_promo_price()
                    );
                    int stock = getStockByMpStock(mpStock, huntSaleOrderDetail.getId());
                    int quantityUsed = ConvertUtils.toInt(mpProductQuantityUsed.get(huntSaleOrderDetail.getId()));
                    if (stock <
                            huntSaleOrderDetail.getQuantity() + quantityUsed) {
                        huntSaleOrderDetail.setIs_error(1);
                        huntSaleOrderDetail.setNote("Tồn kho chỉ còn " + this.appUtils.numberFormat(stock));
                    }

                    huntSaleOrderDetail.setPromo_description(this.getCmsProgramDescriptionForProduct(
                            agencyProgram, huntSaleOrderDetail.getId(), program
                    ));
                    huntSaleOrder.getProducts().add(huntSaleOrderDetail);

                    /**
                     * Thêm vào số lượng đã sử dụng
                     */
                    mpProductQuantityUsed.put(huntSaleOrderDetail.getId(),
                            quantityUsed + huntSaleOrderDetail.getQuantity());
                }

                huntSaleOrder.setTotal_begin_price(
                        huntSaleOrder.getProducts().stream()
                                .reduce(0L, (total, object) -> total +
                                        ConvertUtils.toLong((object.getPrice() * object.getQuantity())), Long::sum)
                );

                huntSaleOrder.setTotal_promo_price(
                        huntSaleOrder.getProducts().stream()
                                .reduce(0L, (total, object) -> total + ConvertUtils.toLong(object.getTotal_promo_price()), Long::sum)
                );

                huntSaleOrder.setTotal_end_price(
                        huntSaleOrder.getTotal_begin_price() -
                                huntSaleOrder.getTotal_promo_price()
                );

                /**
                 * Quà tặng
                 */
                for (OfferProduct offerProduct : programSanSaleOffer.getLtBonusGift()) {
                    HuntSaleOrderDetail huntSaleOrderDetail = this.convertProductHuntSale(
                            offerProduct.getProduct().getId(), offerProduct.getQuantity());
                    huntSaleOrderDetail.setOffer_value(offerProduct.getQuantity());
                    huntSaleOrderDetail.setQuantity(offerProduct.getQuantity());
                    huntSaleOrderDetail.setReal_value(offerProduct.getQuantity());
                    int stock = getStockByMpStock(mpStock, huntSaleOrderDetail.getId());
                    Integer quantityUsed = ConvertUtils.toInt(mpProductQuantityUsed.get(huntSaleOrderDetail.getId()));
                    if (stock <
                            huntSaleOrderDetail.getQuantity() + quantityUsed) {
                        huntSaleOrderDetail.setIs_error(1);
                        huntSaleOrderDetail.setNote("Tồn kho chỉ còn " + this.appUtils.numberFormat(stock));

                        huntSaleOrderDetail.setReal_value(
                                Math.min(stock - quantityUsed, huntSaleOrderDetail.getQuantity()));
                    }

                    mpProductQuantityUsed.put(huntSaleOrderDetail.getId(),
                            quantityUsed + huntSaleOrderDetail.getQuantity());
                    huntSaleOrder.getGifts().add(huntSaleOrderDetail);
                }

                hunt_sale_orders.add(huntSaleOrder);
            }

            Collections.sort(hunt_sale_orders,
                    (a, b) -> a.getPriority() > b.getPriority() ? -1 : a.getPriority() == b.getPriority() ? 0 : 1);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return hunt_sale_orders;
    }

    private int getStockByMpStock(Map<Integer, Integer> mpStock, int product_id) {
        Integer stock = mpStock.get(product_id);
        if (stock == null) {
            stock = this.getTonKho(product_id);
            mpStock.put(product_id, stock);
        }

        return stock;
    }

    private ResultEstimatePromo estimatePromoHuntSale(
            int agency_id,
            EstimatePromo estimatePromo,
            List<OrderProductRequest> goodSelectedList,
            Map<Integer, Integer> mpProductQuantityInOrder,
            int source) {
        ResultEstimatePromo resultEstimatePromo = new ResultEstimatePromo();

        /**
         * Chính sách đang chạy cho đại lý
         */

        /**
         * Tính chính sách
         */
        EstimatePromoData estimatePromoData = this.estimatePromoDataHuntSale(
                agency_id,
                estimatePromo,
                goodSelectedList,
                mpProductQuantityInOrder,
                source
        );


        estimatePromoData.log();
        resultEstimatePromo = this.convertPromoDataToResult(resultEstimatePromo, estimatePromoData);
        return resultEstimatePromo;
    }

    private HuntSaleOrderDetail convertProductHuntSale(int id, int quantity) {
        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                id
        );
        if (productCache == null) {
            return new HuntSaleOrderDetail();
        }
        HuntSaleOrderDetail huntSaleOrderDetail = JsonUtils.DeSerialize(
                JsonUtils.Serialize(productCache),
                HuntSaleOrderDetail.class
        );
        huntSaleOrderDetail.setQuantity(quantity);
        huntSaleOrderDetail.setTotal_begin_price(
                huntSaleOrderDetail.getPrice() * huntSaleOrderDetail.getQuantity()
        );
        huntSaleOrderDetail.setTotal_end_price(
                huntSaleOrderDetail.getPrice() * huntSaleOrderDetail.getQuantity()
        );
        return huntSaleOrderDetail;
    }

    private HuntSaleOrderDetail convertComboHuntSale(int id, JSONObject comboInfo, int quantity
    ) {
        HuntSaleOrderDetail huntSaleOrderDetail = new HuntSaleOrderDetail();
        huntSaleOrderDetail.setId(id);
        huntSaleOrderDetail.setFull_name(
                ConvertUtils.toString(comboInfo.get("full_name"))
        );
        huntSaleOrderDetail.setCode(
                ConvertUtils.toString(comboInfo.get("code"))
        );
        huntSaleOrderDetail.setQuantity(quantity);
        huntSaleOrderDetail.setImages(
                ConvertUtils.toString(comboInfo.get("images"))
        );
        huntSaleOrderDetail.setIs_combo(1);
        huntSaleOrderDetail.setMinimum_purchase(1);
        huntSaleOrderDetail.setStep(1);
        return huntSaleOrderDetail;
    }

    private ClientResponse estimateOrderResponse(EstimateCostOrderRequest request,
                                                 AgencyEntity agencyEntity,
                                                 int source) {
        /**
         * Công nợ hiện tại
         */
        DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(request.getAgency_id());
        if (deptAgencyInfoEntity == null) {
            deptAgencyInfoEntity = initDeptAgencyInfo(request.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                return ClientResponse.success(null);
            }
        }

        List<ProductData> productInOrderList = this.getAllProductInOrder(request.getOrder_id());

        /**
         * Săn sale
         */
        Map<Integer, ProductCache> mpProductCache = new ConcurrentHashMap<>();

        Map<Integer, ProgramOrderProduct> mpProgramOrderProduct =
                this.initMpProductHuntSale(
                        agencyEntity,
                        mpProductCache,
                        request.getHunt_sale_products(),
                        new ArrayList<>());
        Map<Integer, Integer> mpProductQuantityUsed = new HashMap();

        /**
         * Kiểm tra ẩn hiện
         */
        ClientResponse crCheckVisibilityAndPrice = this.checkVisibilityWhenCreateOrder(
                agencyEntity.getId(),
                agencyEntity.getCity_id(),
                agencyEntity.getRegion_id(),
                agencyEntity.getMembership_id(),
                new ArrayList<>(mpProgramOrderProduct.values()));
        if (crCheckVisibilityAndPrice.failed()) {
            return crCheckVisibilityAndPrice;
        }

        Map<Integer, ProgramOrderProduct> mpAllProduct = new ConcurrentHashMap<>();
        mpAllProduct.putAll(mpProgramOrderProduct);

        Map<Integer, Integer> mpStock = this.initMpStock(mpAllProduct);
        List<HuntSaleOrderDetail> huntSaleProductList =
                this.checkOrder(
                        agencyEntity.getId(),
                        request.getHunt_sale_products(),
                        mpProductCache,
                        mpStock
                );

        /**
         * Số lượng đã sử dun trong đơn hàng
         */
        EstimatePromo estimatePromoHuntSale = this.createEstimatePromoHuntSale(
                agencyEntity,
                request.getHunt_sale_products());

        List<HuntSaleOrder> hunt_sale_orders = this.estimateHuntSaleOrder(
                agencyEntity.getId(),
                estimatePromoHuntSale,
                SourceOrderType.CMS.getValue(),
                request.getHunt_sale_products(),
                mpProgramOrderProduct,
                mpAllProduct,
                mpProductQuantityUsed,
                mpProductCache,
                mpStock,
                deptAgencyInfoEntity,
                this.dataManager.getProgramManager().getAgency(agencyEntity.getId()),
                agencyEntity
        );

        OrderSummaryInfoResponse orderMoneyInfoResponse = new OrderSummaryInfoResponse();
        List<OrderProductResponse> products = new ArrayList<>();
        List<ItemOfferResponse> goodsForSelectList = new ArrayList<>();
        List<ItemOfferResponse> goodsClaimList = new ArrayList<>();
        EstimatePromo estimatePromo = new EstimatePromo();
        for (ProgramOrderProduct orderProductRequest : mpProgramOrderProduct.values()) {
            ProductEntity productEntity = this.productDB.getProduct(orderProductRequest.getProductId());
            if (productEntity == null) {
                continue;
            }

            ProductCache productPrice = mpProductCache.get(orderProductRequest.getProductId());

            ProductMoneyResponse productMoneyResponse = this.productUtils.getProductMoney(
                    agencyEntity,
                    productEntity,
                    orderProductRequest.getProductQuantity(),
                    productPrice.getPrice(),
                    productPrice.getMinimum_purchase());

            /**
             * Không tính tổng tiền của giá liên hệ - 1
             */

            if (validateProductStep(
                    orderProductRequest.getProductQuantity(),
                    productMoneyResponse.getMinimum_purchase(),
                    productEntity.getStep()
            ).failed()) {
                productMoneyResponse.setIs_error(1);
                productMoneyResponse.setNote(" Sản phẩm không thỏa SLTT - BN;");
            }

            int tonKho = this.getTonKho(orderProductRequest.getProductId());
            int quantityInOrder = this.getProductQuantityInOrder(productInOrderList,
                    orderProductRequest.getProductId());

            if (tonKho + quantityInOrder <
                    orderProductRequest.getProductQuantity()) {
                productMoneyResponse.setIs_error(1);
                productMoneyResponse.setNote(productMoneyResponse.getNote() + " Tồn kho chỉ còn " + this.appUtils.priceFormat(tonKho + quantityInOrder));
            }

            products.add(
                    JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse),
                            OrderProductResponse.class));

            /**
             * Đưa vào danh sách sản phẩm tính chính sách
             */
            estimatePromo.getPromoProductInputList().add(new PromoProductBasic(
                    productMoneyResponse.getId(),
                    productMoneyResponse.getQuantity(),
                    productMoneyResponse.getPrice() * 1L,
                    null));

            mpProductQuantityUsed.put(
                    orderProductRequest.getProductId(),
                    orderProductRequest.getProductQuantity());
        }

        /**
         * Tính chính sách
         */
        ResultEstimatePromo resultEstimatePromo = this.estimatePromo(request.getAgency_id(),
                estimatePromo,
                request.getGoods(),
                this.convertListProductInOrderToMapInOrder(
                        productInOrderList
                ),
                source
        );
        if (resultEstimatePromo == null) {
            return ClientResponse.success(null);
        }

        /**
         * Chiết khấu/giảm tiền
         */
        for (OrderProductResponse orderProductResponse : products) {
            Double promoProductPriceCSBH = resultEstimatePromo.getMpPromoProductPriceCSBH().get(orderProductResponse.getId());
            if (promoProductPriceCSBH == null) {
                promoProductPriceCSBH = 0.0;
            }
            orderProductResponse.setTotal_csbh_price(promoProductPriceCSBH);

            Double promoProductPriceCTKM = resultEstimatePromo.getMpPromoProductPriceCTKM().get(orderProductResponse.getId());
            if (promoProductPriceCTKM == null) {
                promoProductPriceCTKM = 0.0;
            }
            orderProductResponse.setTotal_promo_price(promoProductPriceCTKM);

            orderProductResponse.setTotal_end_price(
                    orderProductResponse.getTotal_begin_price()
                            - orderProductResponse.getTotal_csbh_price()
                            - orderProductResponse.getTotal_promo_price()
            );

            orderMoneyInfoResponse.setTotal_promotion_price_of_product(
                    orderMoneyInfoResponse.getTotal_promotion_price_of_product()
                            + promoProductPriceCSBH);
            orderMoneyInfoResponse.setTotal_promotion_product_price_ctkm(
                    orderMoneyInfoResponse.getTotal_promotion_product_price_ctkm()
                            + promoProductPriceCTKM);
            /**
             * Tổng tiền tạm tính
             */
            orderMoneyInfoResponse.setTotal_begin_price(orderMoneyInfoResponse.getTotal_begin_price() + orderProductResponse.getTotal_begin_price());
        }

        /**
         * Danh sách hàng tặng
         */
        for (PromoProductBasic promoProductBasic : resultEstimatePromo.getPromoGoodsOfferList()) {
            ProductCache productCache = this.dataManager.getProductManager().getMpProduct().get(promoProductBasic.getItem_id());
            if (productCache == null) {
                continue;
            }

            /**
             * Không tặng hàng bị ẩn
             */
            if (VisibilityType.HIDE.getId() == this.getProductVisibilityByAgency(
                    agencyEntity.getId(),
                    promoProductBasic.getItem_id()
            )) {
                continue;
            }

            ProductCache productPrice = this.getFinalPriceByAgency(
                    productCache.getId(),
                    agencyEntity.getId(),
                    agencyEntity.getCity_id(),
                    agencyEntity.getRegion_id(),
                    agencyEntity.getMembership_id());

            OrderProductRequest goodsSelect = this.getProductInGoodsSelect(promoProductBasic, request.getGoods());

            PromoProductBasic orderProductResponse = this.getProductInOrder(estimatePromo.getPromoProductInputList(), promoProductBasic.getItem_id());
            if (orderProductResponse == null) {
                return ClientResponse.success(null);
            }


            PromoProductBasic goods = new PromoProductBasic(
                    promoProductBasic.getItem_id(),
                    promoProductBasic.getItem_quantity(),
                    productPrice.getPrice() * 1L,
                    null
            );

            estimatePromo.getPromoGoodsSelectList().add(goods);

            ItemOfferResponse itemOfferResponse = this.convertGoodOfferResponse(
                    promoProductBasic,
                    productCache);
            itemOfferResponse.setPrice(productPrice.getPrice());
            Integer quantity_used = mpProductQuantityUsed.get(itemOfferResponse.getId());
            int tonkho = this.getTonKho(itemOfferResponse.getId());
            int quantityInOrder = this.getProductQuantityInOrder(
                    productInOrderList, itemOfferResponse.getId()
            );

            itemOfferResponse.setOffer_value(tonkho + quantityInOrder - quantity_used < itemOfferResponse.getOffer_value()
                    ? tonkho + quantityInOrder - quantity_used
                    : itemOfferResponse.getOffer_value());
            itemOfferResponse.setQuantity_select(
                    (goodsSelect != null && goodsSelect.getProduct_quantity() < itemOfferResponse.getOffer_value()) ?
                            goodsSelect.getProduct_quantity() :
                            request.getHas_gift_hint() == 1 ? promoProductBasic.getItem_quantity() : 0);

            if (tonkho + quantityInOrder - quantity_used < itemOfferResponse.getQuantity_select()) {
                itemOfferResponse.setIs_error(1);
                itemOfferResponse.setNote("Tồn kho chỉ còn: " + this.appUtils.priceFormat(tonkho + quantityInOrder));
            }
            goodsForSelectList.add(itemOfferResponse);

            /**
             * Cập nhật số lượng sử dụng
             */
            mpProductQuantityUsed.put(itemOfferResponse.getId(), quantity_used + itemOfferResponse.getQuantity_select());
        }

        /**
         * tong tien duoc doi qua
         */
        orderMoneyInfoResponse.setTotal_goods_offer_price(resultEstimatePromo.getTotalMoneyGoodsOffer());

        /**
         * Tính tổng tiền đã đổi quà nếu có
         */
        orderMoneyInfoResponse.setTotal_goods_offer_claimed_price(
                goodsForSelectList.stream().reduce(0L, (total, object) -> total +
                        ConvertUtils.toLong((object.getQuantity_select() * object.getPrice())), Long::sum));

        /**
         * Tổng tiền đổi hàng không vượt quá số tiền được tặng
         */
//        if (orderMoneyInfoResponse.getTotal_goods_offer_price() < orderMoneyInfoResponse.getTotal_goods_offer_claimed_price()) {
//            return ClientResponse.success(null);
//        }

        double total_goods_offer_price_remain = orderMoneyInfoResponse.getTotal_goods_offer_price()
                - orderMoneyInfoResponse.getTotal_goods_offer_claimed_price();
        orderMoneyInfoResponse.setTotal_goods_offer_remain_price(total_goods_offer_price_remain);

        long total_refund_price = 0;
        if (total_goods_offer_price_remain > 0) {
            total_refund_price = this.getTotalRefundPrice(
                    total_goods_offer_price_remain,
                    mpProductQuantityUsed,
                    goodsForSelectList,
                    productInOrderList);
        }

        /**
         * Tặng hàng/Tặng quà
         */
        for (PromoGiftClaim promoProductBasic : resultEstimatePromo.getPromoGiftClaimList()) {
            ProductCache productCache = this.dataManager.getProductManager().getMpProduct().get(promoProductBasic.getItem_id());
            if (productCache == null) {
                continue;
            }

            /**
             * Không tặng hàng bị ẩn
             */
            if (VisibilityType.HIDE.getId() == this.getProductVisibilityByAgency(
                    agencyEntity.getId(),
                    promoProductBasic.getItem_id()
            )) {
                continue;
            }

            ItemOfferResponse itemOfferResponse = this.convertItemOfferResponse(promoProductBasic, productCache);
            int tonkho = this.getTonKho(itemOfferResponse.getId());
            int quantityInOrder = this.getProductQuantityInOrder(productInOrderList, itemOfferResponse.getId());
            Integer quantity_used = mpProductQuantityUsed.get(itemOfferResponse.getId());
            if (quantity_used == null) {
                quantity_used = 0;
            }
            if (tonkho + quantityInOrder - quantity_used < itemOfferResponse.getOffer_value()) {
                itemOfferResponse.setIs_error(1);
                itemOfferResponse.setNote("Tồn kho chỉ còn: " + this.appUtils.priceFormat((tonkho + quantityInOrder)));
            }

            int quantity = Math.min(itemOfferResponse.getOffer_value(), Math.max(0, tonkho + quantityInOrder - quantity_used));
            if (quantity > 0) {
                itemOfferResponse.setReal_value(quantity);
                mpProductQuantityUsed.put(itemOfferResponse.getId(), quantity_used + quantity);
            } else {
                itemOfferResponse.setReal_value(0);
            }

            goodsClaimList.add(itemOfferResponse);


            if (promoProductBasic.getItem_quantity() - quantity > 0 &&
                    PromoOfferType.GOODS_OFFER.getKey().equals(itemOfferResponse.getOffer_type())) {
                ProductCache productPrice = this.getFinalPriceByAgency(
                        itemOfferResponse.getId(),
                        agencyEntity.getId(),
                        agencyEntity.getCity_id(),
                        agencyEntity.getRegion_id(),
                        agencyEntity.getMembership_id());
                long price = ConvertUtils.toLong(productPrice == null ? 0 :
                        productPrice.getPrice() < 0 ? 0 : productPrice.getPrice());
                total_refund_price += (promoProductBasic.getItem_quantity() - quantity) * price;
            }
        }

        /**
         * Giảm giá đơn hàng bởi CSBH
         */
        orderMoneyInfoResponse.setTotal_promotion_price_of_order(
                resultEstimatePromo.getTotalMoneyDiscountOrderOfferByCSBH())
        ;

        /**
         * Giảm giá đơn hàng bởi CTKM
         */
        orderMoneyInfoResponse.setTotal_promotion_order_price_ctkm(
                resultEstimatePromo.getTotalMoneyDiscountOrderOfferByCTKM());

        /**
         * tong giam
         */
        orderMoneyInfoResponse.setTotal_promotion_price(
                orderMoneyInfoResponse.getTotal_promotion_product_price_ctkm()
                        + orderMoneyInfoResponse.getTotal_promotion_order_price_ctkm());

        /**
         * Tính toán tổng tiền hoàn nếu có
         * - chưa check tồn kho nên không có hoàn tiền
         */
        orderMoneyInfoResponse.setTotal_refund_price(total_refund_price);

        /**
         * Tổng tiền đơn hàng
         */
        orderMoneyInfoResponse.setTotal_end_price(
                orderMoneyInfoResponse.getTotal_begin_price()
                        - orderMoneyInfoResponse.getTotal_promotion_price_of_product()
                        - orderMoneyInfoResponse.getTotal_promotion_price_of_order()
                        - orderMoneyInfoResponse.getTotal_promotion_price()
                        - orderMoneyInfoResponse.getTotal_refund_price()
        );

        /**
         * CSBH/CTKM cho san pham
         */
        Map<Integer, PromoBasicData> mpPromoGood = new ConcurrentHashMap<>();

        /**
         * CSBH của sản phẩm
         */
        List<PromoProductBasicData> promoProducts = new ArrayList<>();
        for (Map.Entry<Integer, List<Program>> entry : resultEstimatePromo.getMpPromoProductCSBH().entrySet()) {
            for (Program program : entry.getValue()) {
                PromoProductBasicData promoBasicData = new PromoProductBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(
                        this.getCmsProgramDescriptionForProduct(
                                this.dataManager.getProgramManager().getAgency(agencyEntity.getId()),
                                entry.getKey(),
                                program));
                promoBasicData.setProduct_id(entry.getKey());
                promoProducts.add(promoBasicData);

                PromoBasicData promoGood = new PromoBasicData();
                promoGood.setId(promoBasicData.getId());
                promoGood.setName(promoBasicData.getName());
                promoGood.setCode(promoBasicData.getCode());
                promoGood.setDescription(promoBasicData.getDescription());
                mpPromoGood.put(promoBasicData.getId(), promoGood);
            }
        }

        /**
         * CSBH cho đơn hàng
         */
        List<PromoBasicData> promoOrders = new ArrayList<>();
        for (Program program : resultEstimatePromo.getCsbhOrderList()) {
            PromoBasicData promoBasicData = new PromoBasicData();
            promoBasicData.setId(program.getId());
            promoBasicData.setName(program.getName());
            promoBasicData.setCode(program.getCode());
            promoBasicData.setDescription(program.getDescription());
            promoOrders.add(promoBasicData);
        }

        Map<Integer, PromoBasicData> mpCTKM = new ConcurrentHashMap<>();
        /**
         * CTKM cho san pham
         */
        for (List<Program> programs : resultEstimatePromo.getMpPromoProductCTKM().values()) {
            for (Program program : programs) {
                PromoBasicData promoBasicData = new PromoBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(program.getDescription());
                mpCTKM.put(promoBasicData.getId(), promoBasicData);

                mpPromoGood.put(promoBasicData.getId(), promoBasicData);
            }
        }

        /**
         * thêm ctkm sản phẩm vào hộp quà
         */
        for (Map.Entry<Integer, List<Program>> entry : resultEstimatePromo.getMpPromoProductCTKM().entrySet()) {
            for (Program program : entry.getValue()) {
                PromoProductBasicData promoBasicData = new PromoProductBasicData();
                promoBasicData.setId(program.getId());
                promoBasicData.setName(program.getName());
                promoBasicData.setCode(program.getCode());
                promoBasicData.setDescription(
                        this.getCmsProgramDescriptionForProduct(
                                this.dataManager.getProgramManager().getAgency(agencyEntity.getId()),
                                entry.getKey(),
                                program));
                promoBasicData.setProduct_id(entry.getKey());
                promoProducts.add(promoBasicData);
            }
        }

        /**
         * CTKM cho don hang
         */
        for (Program program : resultEstimatePromo.getCtkmOrderList()) {
            PromoBasicData promoBasicData = new PromoBasicData();
            promoBasicData.setId(program.getId());
            promoBasicData.setName(program.getName());
            promoBasicData.setCode(program.getCode());
            promoBasicData.setDescription(program.getDescription());
            mpCTKM.put(promoBasicData.getId(), promoBasicData);
        }

        OrderDeptInfo orderDeptInfo = new OrderDeptInfo();
        long hmkd =
                ConvertUtils.toLong(
                        this.getAgencyHMKD(
                                ConvertUtils.toDouble(deptAgencyInfoEntity.getDept_limit()),
                                ConvertUtils.toDouble(deptAgencyInfoEntity.getNgd_limit()),
                                ConvertUtils.toDouble(deptAgencyInfoEntity.getCurrent_dept()),
                                this.orderDB.getTotalPriceOrderDoing(request.getAgency_id())));
        orderDeptInfo.setCno(deptAgencyInfoEntity.getCurrent_dept());
        orderDeptInfo.setNgd_limit(deptAgencyInfoEntity.getNgd_limit());
        orderDeptInfo.setNqh_order(deptAgencyInfoEntity.getNqh());
        orderDeptInfo.setNqh_current(deptAgencyInfoEntity.getNqh());
        orderDeptInfo.setHmkd(hmkd < 0 ? 0 : hmkd);
        long hmkd_over_order = ConvertUtils.toLong(hmkd - orderMoneyInfoResponse.getTotal_end_price());
        orderDeptInfo.setHmkd_over_order(
                hmkd_over_order < 0 ? hmkd_over_order * -1 : 0);
        orderDeptInfo.setHmkd_over_current(hmkd < 0 ? hmkd * -1 : 0);
        JSONObject data = new JSONObject();
        data.put("order_money", orderMoneyInfoResponse);
        data.put("products", products);
        data.put("goods_for_select", goodsForSelectList);
        data.put("goods_claim", goodsClaimList);
        data.put("promo_products", promoProducts);
        data.put("promo_orders", promoOrders);
        data.put("promo_goods", new ArrayList<>(mpPromoGood.values()));
        data.put("ctkm_orders", new ArrayList<>(mpCTKM.values()));
        data.put("dept_info", orderDeptInfo);

        data.put("hunt_sale_orders", hunt_sale_orders);
        data.put("hunt_sale_hints", this.getHuntSaleHint());

        double total_money_order = orderMoneyInfoResponse.getTotal_end_price()
                + hunt_sale_orders.stream().reduce(0L, (total, object) -> total + (object.getTotal_end_price()), Long::sum);
        data.put("total_money_order", total_money_order);

        data.put("hunt_sale_products", huntSaleProductList);

        return ClientResponse.success(data);
    }

    private long getTotalRefundPrice(double total_goods_offer_price_remain,
                                     Map<Integer, Integer> mpProductQuantityUsed,
                                     List<ItemOfferResponse> goodsForSelectList,
                                     List<ProductData> oldProductList) {
        for (ItemOfferResponse itemOfferResponse : goodsForSelectList) {
            int tonkho = this.getTonKho(itemOfferResponse.getId());
            int quantityInOrder = this.getProductQuantityInOrder(oldProductList, itemOfferResponse.getId());

            Integer quantity_used = mpProductQuantityUsed.get(itemOfferResponse.getId());

            if (quantity_used == null) {
                quantity_used = 0;
            }
            if (total_goods_offer_price_remain >= itemOfferResponse.getPrice()) {
                if (tonkho + quantityInOrder - quantity_used <= 0) {
                    return ConvertUtils.toLong(total_goods_offer_price_remain);
                }
            }
        }
        return 0;
    }

    private boolean validateGoodsOfferProductPriceRemainCanClaimGoodsPriceMin(long moneyRemain, long goodsOfferProductMin) {
        LogUtil.printDebug("moneyRemain: " + moneyRemain);
        LogUtil.printDebug("goodsOfferProductMin: " + goodsOfferProductMin);
        if (moneyRemain == 0) {
            return true;
        }
        if (moneyRemain >= goodsOfferProductMin) {
            return false;
        }
        return true;
    }

    private long getGoodsOfferProductMin(List<PromoProductBasic> promoGoodsOfferList) {
        Long min = 0L;
        for (PromoProductBasic promoProductBasic : promoGoodsOfferList) {
            if (promoProductBasic.getItem_price() < min) {
                min = ConvertUtils.toLong(promoProductBasic.getItem_price());
            }
        }
        return min;
    }

    private OrderProductRequest getProductInGoodsSelect(PromoProductBasic promoProductBasic, List<OrderProductRequest> promoProductInputList) {
        for (OrderProductRequest orderProductResponse : promoProductInputList) {
            if (orderProductResponse.getProduct_id() == promoProductBasic.getItem_id()) {
                return orderProductResponse;
            }
        }
        return null;
    }

    private ItemOfferResponse convertItemOfferResponse(PromoGiftClaim promoProductBasic, ProductCache productCache) {
        ItemOfferResponse itemOfferResponse = new ItemOfferResponse();
        itemOfferResponse.setId(promoProductBasic.getItem_id());
        itemOfferResponse.setOffer_value(promoProductBasic.getItem_quantity());
        itemOfferResponse.setOffer_type(promoProductBasic.getOffer_type());
        itemOfferResponse.setImages(productCache.getImages());
        itemOfferResponse.setName(productCache.getFull_name());
        itemOfferResponse.setCode(productCache.getCode());
        itemOfferResponse.setImages(productCache.getImages());
        itemOfferResponse.setPrice(promoProductBasic.getItem_price() == null ? 0 : promoProductBasic.getItem_price());
        return itemOfferResponse;
    }

    private ItemOfferResponse convertGoodOfferResponse(PromoProductBasic promoProductBasic, ProductCache productCache) {
        ItemOfferResponse itemOfferResponse = new ItemOfferResponse();
        itemOfferResponse.setId(promoProductBasic.getItem_id());
        itemOfferResponse.setOffer_value(promoProductBasic.getItem_quantity());
        itemOfferResponse.setOffer_type(promoProductBasic.getOffer_type());
        itemOfferResponse.setImages(productCache.getImages());
        itemOfferResponse.setName(productCache.getFull_name());
        itemOfferResponse.setCode(productCache.getCode());
        itemOfferResponse.setImages(productCache.getImages());
        return itemOfferResponse;
    }

    private PromoProductBasic getProductInOrder(List<PromoProductBasic> products, int product_id) {
        for (PromoProductBasic orderProductResponse : products) {
            if (orderProductResponse.getItem_id() == product_id) {
                return orderProductResponse;
            }
        }
        return null;
    }

    private PromoProductBasic getProductInProductInput(List<PromoProductBasic> products, PromoProductBasic promoProductBasic) {
        for (PromoProductBasic orderProductResponse : products) {
            if (orderProductResponse.getItem_id() == promoProductBasic.getItem_id()) {
                return orderProductResponse;
            }
        }
        return null;
    }

    private ResultEstimatePromo estimatePromo(
            int agency_id,
            EstimatePromo estimatePromo,
            List<OrderProductRequest> goodSelectedList,
            Map<Integer, Integer> mpProductQuantityInOrder,
            int source) {
        ResultEstimatePromo resultEstimatePromo = new ResultEstimatePromo();

        /**
         * Chính sách đang chạy cho đại lý
         */

        /**
         * Tính chính sách
         */
        EstimatePromoData estimatePromoData = this.estimatePromoData(
                agency_id,
                estimatePromo,
                goodSelectedList,
                mpProductQuantityInOrder,
                source
        );


        estimatePromoData.log();
        resultEstimatePromo = this.convertPromoDataToResult(resultEstimatePromo, estimatePromoData);
        return resultEstimatePromo;
    }

    private ResultEstimatePromo convertPromoDataToResult(ResultEstimatePromo resultEstimatePromo, EstimatePromoData estimatePromoData) {
        /**
         * tổng tạm tính
         */
        resultEstimatePromo.setTotal_begin_price(estimatePromoData.getTotal_begin_price());
        resultEstimatePromo.setTotal_refund_price(estimatePromoData.getTotal_refund_price());
        /**
         * Hàng tặng
         */
        for (OfferProduct offerProduct : estimatePromoData.getLtResponseGoods()) {
            LogUtil.printDebug("ltResponseGoodsForProduct-" + offerProduct.getProduct() + " " + offerProduct.getQuantity());
            resultEstimatePromo.getPromoGoodsOfferList().add(new PromoProductBasic(
                    offerProduct.getProduct().getId(),
                    offerProduct.getQuantity(),
                    0.0,
                    ""
            ));
        }

        /**
         * Tổng tiền được tặng hàng để đổi quà
         */
        resultEstimatePromo.setTotalMoneyGoodsOffer(
                estimatePromoData.getTotalMoneyOfferClaimGift()
        );

        /**
         * Tổng tiền đã đổi quà
         */

        /**
         * Hàng tặng kèm CSBH
         */
        for (OfferProduct offerProduct : estimatePromoData.getLtResponseBonusGoods()) {
            LogUtil.printDebug("ltResponseBonusGoodsForProduct-" + offerProduct.getProduct() + " " + offerProduct.getQuantity());
            resultEstimatePromo.getPromoGiftClaimList().add(new PromoGiftClaim(
                    offerProduct.getProduct().getId(),
                    offerProduct.getQuantity(),
                    0.0,
                    PromoOfferType.GOODS_OFFER.getKey()
            ));
        }

        /**
         * Quà tặng kèm CSBH
         */
        for (OfferProduct offerProduct : estimatePromoData.getLtResponseBonusGift()) {
            LogUtil.printDebug("ltResponseBonusGiftForProduct-" + offerProduct.getProduct() + " " + offerProduct.getQuantity());
            resultEstimatePromo.getPromoGiftClaimList().add(new PromoGiftClaim(
                    offerProduct.getProduct().getId(),
                    offerProduct.getQuantity(),
                    0.0,
                    PromoOfferType.GIFT_OFFER.getKey()
            ));
        }

        /**
         * Tổng chiết khấu/Giảm tiền đơn hàng của CSBH
         */
        resultEstimatePromo.setTotalMoneyDiscountOrderOfferByCSBH(estimatePromoData.getTotalOrderPriceByCSBH());

        /**
         * Tổng chiết khấu/Giảm tiền đơn hàng của CTKM
         */
        resultEstimatePromo.setTotalMoneyDiscountOrderOfferByCTKM(estimatePromoData.getTotalOrderPriceByCTKM());

        /**
         * DS CSBH tren san pham
         */
        resultEstimatePromo.setMpPromoProductCSBH(estimatePromoData.getLtMatchedSalePolicyForProduct());
        /**
         * DS CTKM tren san pham
         */
        resultEstimatePromo.setMpPromoProductCTKM(estimatePromoData.getLtMatchedCTKMForProduct());

        /**
         * DS CSBH giam tren don hang
         */
        resultEstimatePromo.setCsbhOrderList(estimatePromoData.getLtCSBHMatchedForOrder());
        /**
         * DS CTKM giam tren don hang
         */
        resultEstimatePromo.setCtkmOrderList(estimatePromoData.getLtCTKMMatchedForOrder());

        /**
         * csbh hàng tặng
         */
        resultEstimatePromo.setCsbhGoodList(estimatePromoData.getCsbhGoodsOfferList());

        /**
         * ctkm hàng tặng
         */
        resultEstimatePromo.setCtkmGoodList(estimatePromoData.getCtkmGoodsOfferList());

        resultEstimatePromo.setMpPromoProductCSDM(estimatePromoData.getLtMatchedCSDMForProduct());

        /**
         * tất cả ưu đãi được ăn
         */
        resultEstimatePromo.setAllProgram(estimatePromoData.getAllProgram());
        return resultEstimatePromo;
    }

    private EstimatePromoData estimatePromoDataHuntSale(
            int agency_id,
            EstimatePromo estimatePromo,
            List<OrderProductRequest> goodSelectedList,
            Map<Integer, Integer> mpProductQuantityInOrder,
            int sourceOrderType) {

        Source source = Source.APP.getValue() == sourceOrderType ? Source.APP : Source.WEB;
        Agency agency = this.dataManager.getProgramManager().getAgency(agency_id);

        Map<Integer, ProgramOrderProduct> mpProductForProgram = this.getProgramProductInput(estimatePromo.getPromoProductInputList());
        Map<Integer, Long> mpResponsePromotionProductPriceCSBH = new LinkedHashMap<>();
        List<OfferProduct> ltResponseGoods = new LinkedList<>();
        List<OfferProduct> ltResponseBonusGoodsForProduct = new ArrayList<>();
        List<OfferProduct> ltResponseBonusGiftForProduct = new LinkedList<>();
        Map<Integer, List<Program>> mpMatchedCSBHForProduct = new LinkedHashMap<>();

        Map<Integer, List<Program>> mpMatchedCTKMForProduct = new LinkedHashMap<>(); // danh sách sản phẩm tham gia ctkm

        List<Program> ltMatchedProgramForDiscountCTKM = new ArrayList<>(); // danh sách ctkm có giảm giá hay chiết khấu
        List<Program> ltMatchedProgramForGoodsOffer = new ArrayList<>(); // danh sách csbh có hàng tặng
        List<Program> ltMatchedProgramForGoodsOfferCTKM = new ArrayList<>(); // danh sách ctkm có hàng tặng

        List<Program> ltMatchedProgramForAll = new ArrayList<>();
        /**
         * Tong tam tinh cua don hang
         */
        estimatePromo.setTotal_begin_price(
                estimatePromo.getPromoProductInputList().stream()
                        .reduce(0.0, (total, object) -> total + (object.getItem_quantity() * object.getItem_price()), Double::sum));

        /**
         * Tính chính sách áp dụng trên sản phẩm
         * - Số lượng sản phẩm
         * - Doanh số sản phẩm
         */
        long totalPriceRemainOfCSBH = this.checkSalePolicyForProductHuntSale(
                agency,
                mpProductForProgram,
                mpResponsePromotionProductPriceCSBH,
                ltResponseGoods,
                ltResponseBonusGoodsForProduct,
                ltResponseBonusGiftForProduct,
                mpMatchedCSBHForProduct,
                source,
                ltMatchedProgramForGoodsOffer,
                ltMatchedProgramForAll
        );

        LogUtil.printDebug("totalPriceRemainOfCSBH: " + totalPriceRemainOfCSBH);
        double totalPriceExchangeOfCSBH = ltResponseGoods.stream()
                .reduce(0.0, (total, object) -> total + (object.getQuantity() * object.getPrice()), Double::sum);
        double totalPriceOfferOfCSBH = totalPriceExchangeOfCSBH + totalPriceRemainOfCSBH;


        /**
         * Tính hoàn tiền
         */
        long total_refund_price = 0;
        /**
         * Số lượng đã sử dụng
         */
        Map<Integer, Integer> mpProductQuantityUsed = new HashMap();
        for (PromoProductBasic product : estimatePromo.getPromoProductInputList()) {
            Integer quantityUsed = mpProductQuantityUsed.get(product.getItem_id());
            if (quantityUsed == null) {
                mpProductQuantityUsed.put(product.getItem_id(), product.getItem_quantity());
            } else {
                mpProductQuantityUsed.put(product.getItem_id(), quantityUsed + product.getItem_quantity());
            }
        }

        total_refund_price = this.estimateTotalRefundPrice(
                agency,
                mpProductQuantityUsed,
                ltResponseGoods,
                ltResponseBonusGoodsForProduct,
                goodSelectedList,
                ConvertUtils.toLong(totalPriceOfferOfCSBH),
                mpProductQuantityInOrder
        );

        LogUtil.printDebug("total_refund_price: " + total_refund_price);

        /**
         * Tạo danh sách sản phẩm dùng kiểm tra trong chương trình
         * thanh tien = thanh tien - so tien da giam cua csbh
         */
        Map<Integer, ProgramOrderProduct> mpProductForProgramCTKM = new LinkedHashMap<>();
        for (ProgramOrderProduct promoProductBasic : mpProductForProgram.values()) {
            ProgramOrderProduct programOrderProduct = new ProgramOrderProduct();
            programOrderProduct.setProduct(promoProductBasic.getProduct());
            programOrderProduct.setProductQuantity(promoProductBasic.getProductQuantity());
            programOrderProduct.setBeginPrice(promoProductBasic.getBeginPrice());
            long csbhPrice = 0;
            if (mpResponsePromotionProductPriceCSBH.containsKey(programOrderProduct.getProduct().getId())) {
                csbhPrice = mpResponsePromotionProductPriceCSBH.get(programOrderProduct.getProduct().getId());
            }
            programOrderProduct.setBeginPrice(programOrderProduct.getBeginPrice() - csbhPrice);
            mpProductForProgramCTKM.put(programOrderProduct.getProduct().getId(), programOrderProduct);
        }

        Map<Integer, Long> mpResponsePromotionProductPriceCTKM = new LinkedHashMap<>();
        List<OfferProduct> ltResponseGoodsForProductCTKM = new LinkedList<>();
        List<OfferProduct> ltResponseBonusGoodsForProductCTKM = new ArrayList<>();
        List<OfferProduct> ltResponseBonusGiftForProductCTKM = new LinkedList<>();
        List<Program> ltResponsePriorityProgramCTKM = new ArrayList<>();
        long totalPriceRemainOfCTKM = this.checkPromotionForProduct(
                agency,
                mpProductForProgramCTKM,
                mpResponsePromotionProductPriceCTKM,
                ltResponseGoodsForProductCTKM,
                ltResponseBonusGoodsForProductCTKM,
                ltResponseBonusGiftForProductCTKM,
                mpMatchedCTKMForProduct,
                source,
                ltMatchedProgramForGoodsOfferCTKM,
                ltMatchedProgramForDiscountCTKM,
                ltResponsePriorityProgramCTKM,
                ltMatchedProgramForAll);
        long totalPromotionPriceCSBH = estimatePromo.getPromoProductInputList().stream()
                .filter(product -> this.allowProgram("") && mpResponsePromotionProductPriceCSBH.containsKey(product.getItem_id()))
                .map(product -> mpResponsePromotionProductPriceCSBH.get(product.getItem_id()))
                .reduce(0L, Long::sum);

        /**
         * Tổng tiền đã đổi quà
         */
        long totalPriceExchangeOfCTKM = 0;
        long totalPriceOfferOfCTKM = totalPriceExchangeOfCTKM + totalPriceRemainOfCTKM;

        long totalPromotionPriceCTKM = estimatePromo.getPromoProductInputList().stream()
                .filter(product -> this.allowProgram("") && mpResponsePromotionProductPriceCTKM.containsKey(product.getItem_id()))
                .map(product -> mpResponsePromotionProductPriceCTKM.get(product.getItem_id()))
                .reduce(0L, Long::sum);
        ltResponseGoods.addAll(ltResponseGoodsForProductCTKM);
        ltResponseBonusGoodsForProduct.addAll(ltResponseBonusGoodsForProductCTKM);
        ltResponseBonusGiftForProduct.addAll(ltResponseBonusGiftForProductCTKM);

        List<OfferProduct> ltResponseBonusGoodsForOrder = new ArrayList<>();
        List<OfferProduct> ltResponseBonusGiftForOrder = new LinkedList<>();
        List<Program> ltMatchedSalePolicyForOrder = new ArrayList<>();


        double totalOrderPrice = estimatePromo.getTotal_begin_price()
                - totalPromotionPriceCSBH;


        double totalOrderPriceCal = totalOrderPrice - total_refund_price;

        /**
         * Tính chính sách áp dụng trên đơn hàng
         * - Giá trị đơn hàng
         */
        long totalOrderPriceByCSBH = this.checkSalePolicyForOrder(
                agency,
                ConvertUtils.toLong(totalOrderPrice),
                ConvertUtils.toLong(totalOrderPriceCal),
                mpProductForProgram,
                mpResponsePromotionProductPriceCSBH,
                ltResponseBonusGoodsForOrder,
                ltResponseBonusGiftForOrder,
                ltMatchedSalePolicyForOrder,
                source,
                ltMatchedProgramForGoodsOffer,
                ltMatchedProgramForAll
        );

        // kiểm tra chương trình khuyến mãi cho đơn hàng
        double totalOrderPriceCTKM = estimatePromo.getTotal_begin_price()
                - totalPromotionPriceCSBH
                - totalPromotionPriceCTKM
                - totalOrderPriceByCSBH;

        /**
         * khuyen mai tren don hang cua CTKM
         * 1. totalOrderPriceByCTKM: so tien duoc giam tren don hang
         * 2. ltResponseBonusGoodsForOrderCTKM: hang tang tren don hang
         * 3. ltResponseBonusGiftForOrderCTKM: quan tang tren don hang
         */
        List<OfferProduct> ltResponseBonusGoodsForOrderCTKM = new ArrayList<>();
        List<OfferProduct> ltResponseBonusGiftForOrderCTKM = new LinkedList<>();

        /**
         * tổng tiền dùng để tính thưởng
         */
        double totalOrderPriceCTKMForCal = estimatePromo.getTotal_begin_price()
                - totalPromotionPriceCSBH
                - totalPromotionPriceCTKM
                - totalOrderPriceByCSBH
                - total_refund_price;
        double totalOrderPriceCTKMForCheck = totalOrderPriceCTKMForCal;

        long totalOrderPriceByCTKM = this.checkPromotionForOrder(
                agency,
                ConvertUtils.toLong(totalOrderPriceCTKMForCheck),
                ConvertUtils.toLong(totalOrderPriceCTKMForCal),
                mpProductForProgramCTKM,
                mpResponsePromotionProductPriceCSBH,
                mpResponsePromotionProductPriceCTKM,
                ltResponseBonusGoodsForOrderCTKM,
                ltResponseBonusGiftForOrderCTKM,
                ltMatchedProgramForDiscountCTKM,
                source,
                ltMatchedProgramForGoodsOfferCTKM,
                ltResponsePriorityProgramCTKM,
                ltMatchedProgramForAll);

        /**
         * Sum hàng tặng kèm
         */
        List<OfferProduct> ltBonusGoods = new ArrayList<>();
        ltBonusGoods.addAll(ltResponseBonusGoodsForProduct);
        ltBonusGoods.addAll(ltResponseBonusGoodsForOrder);
        ltBonusGoods = this.optimizeOfferProducts(ltBonusGoods);
        /**
         * Sum quà tặng kèm
         */
        List<OfferProduct> ltBonusGifts = new ArrayList<>();
        ltBonusGifts.addAll(ltResponseBonusGiftForProduct);
        ltBonusGifts.addAll(ltResponseBonusGiftForOrder);
        ltBonusGifts = this.optimizeOfferProducts(ltBonusGifts);

        EstimatePromoData estimatePromoData = new EstimatePromoData();
        estimatePromoData.setTotal_begin_price(estimatePromo.getTotal_begin_price());
        estimatePromoData.setLtResponseGoods(ltResponseGoods);
        estimatePromoData.setMpResponseProductPriceCSBH(mpResponsePromotionProductPriceCSBH);
        estimatePromoData.setMpResponseProductPriceCTKM(mpResponsePromotionProductPriceCTKM);
        estimatePromoData.setLtResponseBonusGoods(ltBonusGoods);
        estimatePromoData.setLtResponseBonusGift(ltBonusGifts);
        estimatePromoData.setTotal_refund_price(total_refund_price);
        /**
         * Tong tien doi qua con lai
         */
        estimatePromoData.setTotalMoneyRemainClaimGift(
                totalPriceRemainOfCSBH
                        + totalPriceRemainOfCTKM);

        /**
         * tong tien doi qua cua csbh
         */
        estimatePromoData.setTotalMoneyOfferClaimGift(totalPriceOfferOfCSBH
                + totalPriceOfferOfCTKM);

        /**
         * tong giam gia don hang cua csbh
         */
        estimatePromoData.setTotalOrderPriceByCSBH(totalOrderPriceByCSBH);

        /**
         * Danh sach csbh tren san pham
         */
        estimatePromoData.setLtMatchedSalePolicyForProduct(mpMatchedCSBHForProduct);

        /**
         * Danh sach csbh tren san pham
         */
        estimatePromoData.setLtMatchedCTKMForProduct(mpMatchedCTKMForProduct);

        /**
         * Danh sach chinh sach giam gia tren don hang cua csbh
         */
        estimatePromoData.setLtCSBHMatchedForOrder(ltMatchedSalePolicyForOrder);

        /**
         * Giảm giá trên đơn hàng của CTKM
         */
        estimatePromoData.setTotalOrderPriceByCTKM(totalOrderPriceByCTKM);
        /**
         * Danh sach ctkm giam gia tren don hang
         */
        estimatePromoData.setLtCTKMMatchedForOrder(ltMatchedProgramForDiscountCTKM);

        /**
         * CTKM/CSBH tặng hàng
         */
        estimatePromoData.getCsbhGoodsOfferList().addAll(ltMatchedProgramForGoodsOffer);
        estimatePromoData.getCtkmGoodsOfferList().addAll(ltMatchedProgramForGoodsOfferCTKM);

        /**
         * Tất cả ưu đãi
         */
        estimatePromoData.setAllProgram(ltMatchedProgramForAll);
        return estimatePromoData;
    }

    private EstimatePromoData estimatePromoData(
            int agency_id,
            EstimatePromo estimatePromo,
            List<OrderProductRequest> goodSelectedList,
            Map<Integer, Integer> mpProductQuantityInOrder,
            int sourceOrderType) {

        Source source = Source.APP.getValue() == sourceOrderType ? Source.APP : Source.WEB;
        Agency agency = this.dataManager.getProgramManager().getAgency(agency_id);

        Map<Integer, ProgramOrderProduct> mpProductForProgram = this.getProgramProductInput(estimatePromo.getPromoProductInputList());
        Map<Integer, Long> mpResponsePromotionProductPriceCSBH = new LinkedHashMap<>();
        List<OfferProduct> ltResponseGoods = new LinkedList<>();
        List<OfferProduct> ltResponseBonusGoodsForProduct = new ArrayList<>();
        List<OfferProduct> ltResponseBonusGiftForProduct = new LinkedList<>();
        Map<Integer, List<Program>> mpMatchedCSBHForProduct = new LinkedHashMap<>();

        Map<Integer, List<Program>> mpMatchedCTKMForProduct = new LinkedHashMap<>(); // danh sách sản phẩm tham gia ctkm

        List<Program> ltMatchedProgramForDiscountCTKM = new ArrayList<>(); // danh sách ctkm có giảm giá hay chiết khấu
        List<Program> ltMatchedProgramForGoodsOffer = new ArrayList<>(); // danh sách csbh có hàng tặng
        List<Program> ltMatchedProgramForGoodsOfferCTKM = new ArrayList<>(); // danh sách ctkm có hàng tặng

        List<Program> ltMatchedProgramForAll = new ArrayList<>();
        /**
         * Tong tam tinh cua don hang
         */
        estimatePromo.setTotal_begin_price(estimatePromo.getPromoProductInputList().stream()
                .reduce(0.0, (total, object) -> total + (object.getItem_quantity() * object.getItem_price()), Double::sum));

        /**
         * Tính chính sách áp dụng trên sản phẩm
         * - Số lượng sản phẩm
         * - Doanh số sản phẩm
         */
        long totalPriceRemainOfCSBH = this.checkSalePolicyForProduct(
                agency,
                mpProductForProgram,
                mpResponsePromotionProductPriceCSBH,
                ltResponseGoods,
                ltResponseBonusGoodsForProduct,
                ltResponseBonusGiftForProduct,
                mpMatchedCSBHForProduct,
                source,
                ltMatchedProgramForGoodsOffer,
                ltMatchedProgramForAll
        );

        LogUtil.printDebug("totalPriceRemainOfCSBH: " + totalPriceRemainOfCSBH);
        double totalPriceExchangeOfCSBH = ltResponseGoods.stream()
                .reduce(0.0, (total, object) -> total + (object.getQuantity() * object.getPrice()), Double::sum);
        double totalPriceOfferOfCSBH = totalPriceExchangeOfCSBH + totalPriceRemainOfCSBH;

        /**
         * CSDM cho sản phẩm
         */
        Map<Integer, TLProductReward> mpDMProductPrice = new LinkedHashMap<>();
        mpDMProductPrice = this.checkDMForProduct(agency, mpProductForProgram);
        long totalDMPrice = mpDMProductPrice.values().stream().map(
                TLProductReward::getOfferValue).reduce(0L, Long::sum);

        Map<Integer, List<Program>> mpMatchedCSDMForProduct = new LinkedHashMap<>();
        Map<Integer, Long> mpResponsePromotionProductPriceCSDM = new LinkedHashMap<>();
        Map<Integer, Program> mpDMProduct = new HashMap<>();
        for (Map.Entry<Integer, TLProductReward> entry : mpDMProductPrice.entrySet()) {
            mpResponsePromotionProductPriceCSDM.put(entry.getKey(),
                    entry.getValue().getOfferValue());

            Program promoBasicData = new Program();
            promoBasicData.setId(entry.getValue().getProgram().getId());
            promoBasicData.setCode(entry.getValue().getProgram().getCode());
            promoBasicData.setName(entry.getValue().getProgram().getName());
            promoBasicData.setOfferPercent(
                    entry.getValue().getOfferPercent());

            mpMatchedCSDMForProduct.put(entry.getKey(), Arrays.asList(promoBasicData));

            if (!mpDMProduct.containsKey(entry.getValue().getProgram().getId())) {
                mpDMProduct.put(promoBasicData.getId(), promoBasicData);
            }
        }

        /**
         * Tính hoàn tiền
         */
        long total_refund_price = 0;
        /**
         * Số lượng đã sử dụng
         */
        Map<Integer, Integer> mpProductQuantityUsed = new HashMap();
        for (PromoProductBasic product : estimatePromo.getPromoProductInputList()) {
            Integer quantityUsed = mpProductQuantityUsed.get(product.getItem_id());
            if (quantityUsed == null) {
                mpProductQuantityUsed.put(product.getItem_id(), product.getItem_quantity());
            } else {
                mpProductQuantityUsed.put(product.getItem_id(), quantityUsed + product.getItem_quantity());
            }
        }

        total_refund_price = this.estimateTotalRefundPrice(
                agency,
                mpProductQuantityUsed,
                ltResponseGoods,
                ltResponseBonusGoodsForProduct,
                goodSelectedList,
                ConvertUtils.toLong(totalPriceOfferOfCSBH),
                mpProductQuantityInOrder
        );

        LogUtil.printDebug("total_refund_price: " + total_refund_price);

        /**
         * Tạo danh sách sản phẩm dùng kiểm tra trong chương trình
         * thanh tien = thanh tien - so tien da giam cua csbh
         */
        Map<Integer, ProgramOrderProduct> mpProductForProgramCTKM = new LinkedHashMap<>();
        for (ProgramOrderProduct promoProductBasic : mpProductForProgram.values()) {
            ProgramOrderProduct programOrderProduct = new ProgramOrderProduct();
            programOrderProduct.setProduct(promoProductBasic.getProduct());
            programOrderProduct.setProductQuantity(promoProductBasic.getProductQuantity());
            programOrderProduct.setBeginPrice(promoProductBasic.getBeginPrice());
            long csbhPrice = 0;
            if (mpResponsePromotionProductPriceCSBH.containsKey(programOrderProduct.getProduct().getId())) {
                csbhPrice = mpResponsePromotionProductPriceCSBH.get(programOrderProduct.getProduct().getId());
            }
            programOrderProduct.setBeginPrice(programOrderProduct.getBeginPrice() - csbhPrice);
            mpProductForProgramCTKM.put(programOrderProduct.getProduct().getId(), programOrderProduct);
        }

        Map<Integer, Long> mpResponsePromotionProductPriceCTKM = new LinkedHashMap<>();
        List<OfferProduct> ltResponseGoodsForProductCTKM = new LinkedList<>();
        List<OfferProduct> ltResponseBonusGoodsForProductCTKM = new ArrayList<>();
        List<OfferProduct> ltResponseBonusGiftForProductCTKM = new LinkedList<>();
        List<Program> ltResponsePriorityProgramCTKM = new ArrayList<>();
        long totalPriceRemainOfCTKM = this.checkPromotionForProduct(
                agency,
                mpProductForProgramCTKM,
                mpResponsePromotionProductPriceCTKM,
                ltResponseGoodsForProductCTKM,
                ltResponseBonusGoodsForProductCTKM,
                ltResponseBonusGiftForProductCTKM,
                mpMatchedCTKMForProduct,
                source,
                ltMatchedProgramForGoodsOfferCTKM,
                ltMatchedProgramForDiscountCTKM,
                ltResponsePriorityProgramCTKM,
                ltMatchedProgramForAll);
        long totalPromotionPriceCSBH = estimatePromo.getPromoProductInputList().stream()
                .filter(product -> this.allowProgram("") && mpResponsePromotionProductPriceCSBH.containsKey(product.getItem_id()))
                .map(product -> mpResponsePromotionProductPriceCSBH.get(product.getItem_id()))
                .reduce(0L, Long::sum);

        /**
         * Tổng tiền đã đổi quà
         */
        double totalPriceExchangeOfCTKM = ltResponseGoodsForProductCTKM.stream()
                .reduce(0.0, (total, object) -> total + (object.getQuantity() * object.getPrice()), Double::sum);
        double totalPriceOfferOfCTKM = totalPriceExchangeOfCTKM + totalPriceRemainOfCTKM;

        double totalPromotionPriceCTKM = estimatePromo.getPromoProductInputList().stream()
                .filter(product -> this.allowProgram("") && mpResponsePromotionProductPriceCTKM.containsKey(product.getItem_id()))
                .map(product -> mpResponsePromotionProductPriceCTKM.get(product.getItem_id()))
                .reduce(0L, Long::sum);
        ltResponseGoods.addAll(ltResponseGoodsForProductCTKM);
        ltResponseBonusGoodsForProduct.addAll(ltResponseBonusGoodsForProductCTKM);
        ltResponseBonusGiftForProduct.addAll(ltResponseBonusGiftForProductCTKM);

        List<OfferProduct> ltResponseBonusGoodsForOrder = new ArrayList<>();
        List<OfferProduct> ltResponseBonusGiftForOrder = new LinkedList<>();
        List<Program> ltMatchedSalePolicyForOrder = new ArrayList<>();


        double totalOrderPrice =
                estimatePromo.getTotal_begin_price()
                        - totalPromotionPriceCSBH
                        - totalDMPrice;


        double totalOrderPriceCal = totalOrderPrice - total_refund_price;

        /**
         * Tính chính sách áp dụng trên đơn hàng
         * - Giá trị đơn hàng
         */
        long totalOrderPriceByCSBH = this.checkSalePolicyForOrder(
                agency,
                ConvertUtils.toLong(totalOrderPrice),
                ConvertUtils.toLong(totalOrderPriceCal),
                mpProductForProgram,
                mpResponsePromotionProductPriceCSBH,
                ltResponseBonusGoodsForOrder,
                ltResponseBonusGiftForOrder,
                ltMatchedSalePolicyForOrder,
                source,
                ltMatchedProgramForGoodsOffer,
                ltMatchedProgramForAll
        );

        // kiểm tra chương trình khuyến mãi cho đơn hàng
        double totalOrderPriceCTKM = estimatePromo.getTotal_begin_price()
                - totalPromotionPriceCSBH
                - totalDMPrice
                - totalPromotionPriceCTKM
                - totalOrderPriceByCSBH;

        /**
         * khuyen mai tren don hang cua CTKM
         * 1. totalOrderPriceByCTKM: so tien duoc giam tren don hang
         * 2. ltResponseBonusGoodsForOrderCTKM: hang tang tren don hang
         * 3. ltResponseBonusGiftForOrderCTKM: quan tang tren don hang
         */
        List<OfferProduct> ltResponseBonusGoodsForOrderCTKM = new ArrayList<>();
        List<OfferProduct> ltResponseBonusGiftForOrderCTKM = new LinkedList<>();

        /**
         * Tổng tiền check điều kiện
         */
        double totalOrderPriceCTKMForCheck = estimatePromo.getTotal_begin_price()
                - totalPromotionPriceCSBH
                - totalDMPrice;
        /**
         * tổng tiền dùng để tính thưởng
         */
        double totalOrderPriceCTKMForCal = estimatePromo.getTotal_begin_price()
                - totalPromotionPriceCSBH
                - totalDMPrice
                - totalPromotionPriceCTKM
                - totalOrderPriceByCSBH
                - total_refund_price;

        double totalOrderPriceByCTKM = this.checkPromotionForOrder(
                agency,
                ConvertUtils.toLong(totalOrderPriceCTKMForCheck),
                ConvertUtils.toLong(totalOrderPriceCTKMForCal),
                mpProductForProgramCTKM,
                mpResponsePromotionProductPriceCSBH,
                mpResponsePromotionProductPriceCTKM,
                ltResponseBonusGoodsForOrderCTKM,
                ltResponseBonusGiftForOrderCTKM,
                ltMatchedProgramForDiscountCTKM,
                source,
                ltMatchedProgramForGoodsOfferCTKM,
                ltResponsePriorityProgramCTKM,
                ltMatchedProgramForAll);

        /**
         * Sum hàng tặng kèm
         */
        List<OfferProduct> ltBonusGoods = new ArrayList<>();
        ltBonusGoods.addAll(ltResponseBonusGoodsForProduct);
        ltBonusGoods.addAll(ltResponseBonusGoodsForOrder);
        ltBonusGoods = this.optimizeOfferProducts(ltBonusGoods);
        /**
         * Sum quà tặng kèm
         */
        List<OfferProduct> ltBonusGifts = new ArrayList<>();
        ltBonusGifts.addAll(ltResponseBonusGiftForProduct);
        ltBonusGifts.addAll(ltResponseBonusGiftForOrder);
        ltBonusGifts = this.optimizeOfferProducts(ltBonusGifts);

        EstimatePromoData estimatePromoData = new EstimatePromoData();
        estimatePromoData.setTotal_begin_price(estimatePromo.getTotal_begin_price());
        estimatePromoData.setLtResponseGoods(ltResponseGoods);
        estimatePromoData.setMpResponseProductPriceCSBH(mpResponsePromotionProductPriceCSBH);
        estimatePromoData.setMpResponseProductPriceCTKM(mpResponsePromotionProductPriceCTKM);
        estimatePromoData.setLtResponseBonusGoods(ltBonusGoods);
        estimatePromoData.setLtResponseBonusGift(ltBonusGifts);
        estimatePromoData.setTotal_refund_price(total_refund_price);
        /**
         * Tong tien doi qua con lai
         */
        estimatePromoData.setTotalMoneyRemainClaimGift(
                totalPriceRemainOfCSBH
                        + totalPriceRemainOfCTKM);

        /**
         * tong tien doi qua cua csbh
         */
        estimatePromoData.setTotalMoneyOfferClaimGift(totalPriceOfferOfCSBH
                + totalPriceOfferOfCTKM);

        /**
         * tong giam gia don hang cua csbh
         */
        estimatePromoData.setTotalOrderPriceByCSBH(totalOrderPriceByCSBH);

        /**
         * Danh sach csbh tren san pham
         */
        estimatePromoData.setLtMatchedSalePolicyForProduct(mpMatchedCSBHForProduct);

        /**
         * Danh sach csbh tren san pham
         */
        estimatePromoData.setLtMatchedCTKMForProduct(mpMatchedCTKMForProduct);

        /**
         * Danh sach chinh sach giam gia tren don hang cua csbh
         */
        estimatePromoData.setLtCSBHMatchedForOrder(ltMatchedSalePolicyForOrder);

        /**
         * Giảm giá trên đơn hàng của CTKM
         */
        estimatePromoData.setTotalOrderPriceByCTKM(totalOrderPriceByCTKM);
        /**
         * Danh sach ctkm giam gia tren don hang
         */
        estimatePromoData.setLtCTKMMatchedForOrder(ltMatchedProgramForDiscountCTKM);

        /**
         * CTKM/CSBH tặng hàng
         */
        estimatePromoData.getCsbhGoodsOfferList().addAll(ltMatchedProgramForGoodsOffer);
        estimatePromoData.getCtkmGoodsOfferList().addAll(ltMatchedProgramForGoodsOfferCTKM);

        /**
         * Thêm CSDM vào ds chương trình của đơn
         */
        estimatePromoData.setMpResponseProductPriceCSDM(mpResponsePromotionProductPriceCSDM);
        estimatePromoData.setLtMatchedCSDMForProduct(mpMatchedCSDMForProduct);
        ltMatchedProgramForAll.addAll(mpDMProduct.values());

        /**
         * Tất cả ưu đãi
         */
        estimatePromoData.setAllProgram(ltMatchedProgramForAll);
        return estimatePromoData;
    }

    private long estimateTotalRefundPrice(
            Agency agency,
            Map<Integer, Integer> mpProductQuantityCart,
            List<OfferProduct> ltResponseGoods,
            List<OfferProduct> ltResponseBonusGoodsForProduct,
            List<OrderProductRequest> goodSelectedList,
            long totalMoneyOffer,
            Map<Integer, Integer> mpProductQuantityInOrder) {
        long total_refund_price = 0;
        List<ItemOfferResponse> goodOfferList = new ArrayList<>();
        for (OfferProduct offerProduct : ltResponseGoods) {
            ItemOfferResponse itemOfferResponse = new ItemOfferResponse();
            itemOfferResponse.setId(offerProduct.getProduct().getId());
            itemOfferResponse.setPrice(offerProduct.getPrice());
            itemOfferResponse.setQuantity_select(
                    this.getQuantityGoodSelect(itemOfferResponse.getId(), goodSelectedList)
            );
            goodOfferList.add(itemOfferResponse);
        }

        Map<Integer, Integer> mpProductQuantityUsed = new ConcurrentHashMap<>();
        mpProductQuantityUsed.putAll(mpProductQuantityCart);
        long totalClaimedPrice =
                goodOfferList.stream().reduce(0L, (total, object) -> total + ConvertUtils.toLong(object.getQuantity_select() * object.getPrice()), Long::sum);
        long totalRemainPrice = totalMoneyOffer - totalClaimedPrice;

        for (ItemOfferResponse itemOfferResponse : goodOfferList) {
            int tonKho = this.getTonKho(itemOfferResponse.getId());
            Integer quantity_order = mpProductQuantityInOrder.get(itemOfferResponse.getId());
            if (quantity_order != null) {
                tonKho += quantity_order;
            }
            Integer quantity_used = mpProductQuantityUsed.get(itemOfferResponse.getId());
            if (quantity_used == null) {
                quantity_used = 0;
            } else {
                quantity_used += itemOfferResponse.getQuantity_select();
            }
            mpProductQuantityUsed.put(itemOfferResponse.getId(), quantity_used);

            if (totalRemainPrice >= itemOfferResponse.getPrice()) {
                if (tonKho - quantity_used <= 0) {
                    total_refund_price = totalRemainPrice;
                    break;
                }
            }
        }

        for (OfferProduct offerProduct : ltResponseBonusGoodsForProduct) {
            int tonKho = this.getTonKho(offerProduct.getProduct().getId());
            Integer quantity_order = mpProductQuantityInOrder.get(offerProduct.getProduct().getId());
            if (quantity_order != null) {
                tonKho += quantity_order;
            }
            Integer quantity_used = mpProductQuantityUsed.get(offerProduct.getProduct().getId());
            if (quantity_used == null) {
                quantity_used = 0;
            }
            int quantity = tonKho - quantity_used - offerProduct.getQuantity();
            if (quantity < 0) {
                ProductCache productPrice = this.getFinalPriceByAgency(
                        offerProduct.getProduct().getId(),
                        agency.getId(),
                        agency.getCityId(),
                        agency.getRegionId(),
                        agency.getMembershipId());
                double price = productPrice == null ? 0 :
                        productPrice.getPrice() < 0 ? 0 : productPrice.getPrice();
                total_refund_price += quantity * -1 * price;
            }
        }

        return total_refund_price;
    }

    private int getQuantityGoodSelect(int id, List<OrderProductRequest> goodSelectedList) {
        for (OrderProductRequest itemOfferResponse : goodSelectedList) {
            if (itemOfferResponse.getProduct_id() == id) {
                return itemOfferResponse.getProduct_quantity();
            }
        }

        return 0;
    }

    private long getTotalPromotionPriceProduct(Map<Integer, Long> mpResponseProductPrice) {
        Long total_promo_product_price = 0L;
        for (Long promoPrice : mpResponseProductPrice.values()) {
            total_promo_product_price += promoPrice;
        }
        return total_promo_product_price;
    }

    private Map<Integer, ProgramOrderProduct> getProgramProductInput(List<PromoProductBasic> promoProductInputList) {
        Map<Integer, ProgramOrderProduct> mpProgramOrderProduct = new LinkedHashMap<>();
        for (PromoProductBasic promoProductBasic : promoProductInputList) {
            ProgramOrderProduct programOrderProduct = new ProgramOrderProduct();
            programOrderProduct.setProduct(this.dataManager.getProgramManager().getProductById(promoProductBasic.getItem_id(), ""));
            programOrderProduct.setProductQuantity(promoProductBasic.getItem_quantity());
            programOrderProduct.setBeginPrice(promoProductBasic.getItem_price() * promoProductBasic.getItem_quantity());
            mpProgramOrderProduct.put(programOrderProduct.getProduct().getId(), programOrderProduct);
        }
        return mpProgramOrderProduct;
    }

    /**
     * Hủy đơn hàng
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse cancelOrder(SessionData sessionData, CancelOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject jsOrder = this.orderDB.getAgencyOrder(request.getId());
            if (jsOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            AgencyOrderEntity agencyOrderEntity = AgencyOrderEntity.from(jsOrder);
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int current_status = agencyOrderEntity.getStatus();

            if (!this.dataManager.getStaffManager().checkManageOrder(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(agencyOrderEntity.getAgency_id()),
                    agencyOrderEntity.getStatus()
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            /**
             * ràng buộc trạng thái được phép hủy đơn
             */
            clientResponse = this.checkCanCancelOrder(agencyOrderEntity.getStatus());
            if (clientResponse.failed()) {
                return clientResponse;
            }

            ClientResponse crExcuteCancelOrder = this.excuteCancelOrder(request, agencyOrderEntity, sessionData);
            if (crExcuteCancelOrder.failed()) {
                return crExcuteCancelOrder;
            }

            if (current_status != OrderStatus.DRAFT.getKey() &&
                    current_status != OrderStatus.WAITING_APPROVE.getKey()
            ) {
                this.pushNotifyToAgency(
                        0,
                        NotifyAutoContentType.CANCEL_ORDER,
                        "",
                        NotifyAutoContentType.CANCEL_ORDER.getType(),
                        JsonUtils.Serialize(
                                Arrays.asList(ConvertUtils.toString(agencyOrderEntity.getId()))
                        ),
                        "Đơn đặt hàng " + agencyOrderEntity.getCode() + " của Quý khách đã hủy vì " + request.getNote() + ".",
                        agencyOrderEntity.getAgency_id()
                );
            }

            if (current_status == OrderStatus.SHIPPING.getKey()) {
                this.callRemoveTransaction(
                        agencyOrderEntity
                );
            }

            /**
             * Trả lại voucher nếu có
             */
            this.returnVoucher(request.getId());

            /**
             * Hủy tích lũy nhiệm vụ
             */
            int accumulate_mission_status = ConvertUtils.toInt(jsOrder.get("accumulate_mission_status"));
            if (accumulate_mission_status == 1) {
                this.accumulateMissionService.removeOrder(agencyOrderEntity.getId(), agencyOrderEntity.getAgency_id());
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void returnVoucher(int id) {
        try {
            this.orderDB.returnVoucher(id);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    private void callRemoveTransaction(AgencyOrderEntity agencyOrderEntity) {
        try {
            List<JSONObject> agencyOrderDeptList = this.orderDB.getListAgencyOrderDept(
                    agencyOrderEntity.getId()
            );
            for (JSONObject agencyOrderDept : agencyOrderDeptList) {
                if (this.appUtils.isOrderNormal(
                        ConvertUtils.toInt(
                                agencyOrderDept.get("promo_id"))
                )) {
                    this.accumulateService.autoRemoveTransaction(
                            CTTLTransactionType.DON_HANG.getKey(),
                            agencyOrderEntity.getAgency_id(),
                            ConvertUtils.toString(agencyOrderDept.get("dept_code")),
                            agencyOrderEntity.getId(),
                            0,
                            ConvertUtils.toInt(
                                    agencyOrderDept.get("id")),
                            CTTLTransactionSource.AUTO.getId(),
                            0
                    );
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    public ClientResponse lockTimeOrder(SessionData sessionData, LockTimeOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            AgencyOrderEntity agencyOrderEntity = this.orderDB.getAgencyOrderEntity(request.getId()
            );
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }


            if (agencyOrderEntity.getSource() != SourceOrderType.APP.getValue() ||
                    !(agencyOrderEntity.getStatus() == OrderStatus.WAITING_CONFIRM.getKey() ||
                            agencyOrderEntity.getStatus() == OrderStatus.PREPARE.getKey())
            ) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsUpdate = this.orderDB.lockTimeOrder(request.getId());
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse excuteCancelOrder(
            CancelOrderRequest request,
            AgencyOrderEntity agencyOrderEntity,
            SessionData sessionData) {
        try {

            int current_status = agencyOrderEntity.getStatus();

            boolean rsUpdateAgencyOrderStatus = this.orderDB.cancelAgencyOrder(request.getId(), OrderStatus.CANCEL.getKey(), request.getNote(), sessionData.getId());
            if (!rsUpdateAgencyOrderStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            /**
             * save update order status history
             */
            this.orderDB.saveUpdateOrderStatusHistory(request.getId(), OrderStatus.CANCEL.getKey(), request.getNote(), sessionData.getId());

            /**
             * Xóa săn phẩm ăn săn sale của đơn
             */
            this.orderDB.deleteAgencyPromoHuntSale(request.getId());
//            /**
//             * Hủy đơn hẹn giao,
//             * giảm công nợ
//             */
//            if (IncreaseDeptStatus.YES.getValue() == agencyOrderEntity.getIncrease_dept()) {
//                DeptAgencyInfoEntity deptAgencyInfoEntity = deptDB.getDeptAgencyInfo(agencyOrderEntity.getAgency_id());
//                if (deptAgencyInfoEntity == null) {
//                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
//                }
//
//                DeptTransactionSubTypeEntity deptTransactionSubTypeEntity = this.dataManager.getConfigManager().getHanMucHuyDonHangHenGiao();
//                if (deptTransactionSubTypeEntity == null) {
//                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
//                }
//
//                /**
//                 * Lưu dept transaction
//                 */
//                DeptTransactionEntity cancelTransactionEntity = this.createDeptTransactionEntity(
//                        deptTransactionSubTypeEntity.getDept_type_id(),
//                        deptTransactionSubTypeEntity.getDept_transaction_main_type_id(),
//                        deptTransactionSubTypeEntity.getDept_type_id(),
//                        deptTransactionSubTypeEntity.getCn_effect_type(),
//                        deptTransactionSubTypeEntity.getDtt_effect_type(),
//                        deptTransactionSubTypeEntity.getTt_effect_type(),
//                        deptTransactionSubTypeEntity.getAcoin_effect_type(),
//                        agencyOrderEntity.getTotal_end_price(),
//                        agencyOrderEntity.getAgency_id(),
//                        deptAgencyInfoEntity.getDept_cycle_end(),
//                        agencyOrderEntity.getCode(),
//                        agencyOrderEntity.getCode(),
//                        DeptTransactionStatus.CONFIRMED.getId(),
//                        DateTimeUtils.getNow(),
//                        null,
//                        deptTransactionSubTypeEntity.getFunction_type(),
//                        sessionData.getId(),
//                        null,
//                        DateTimeUtils.getNow(),
//                        DateTimeUtils.getNow(),
//                        "",
//                        0L,
//                        0L
//                );
//                int rsCancelDeptTransaction = this.deptDB.createDeptTransaction(cancelTransactionEntity);
//                if (rsCancelDeptTransaction <= 0) {
//                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
//                }
//                cancelTransactionEntity.setId(rsCancelDeptTransaction);
//
//                boolean rsDecreaseCNO = this.decreaseCNO(
//                        agencyOrderEntity.getAgency_id(),
//                        agencyOrderEntity.getTotal_end_price(),
//                        null,
//                        null,
//                        deptAgencyInfoEntity.getCurrent_dept(),
//                        deptAgencyInfoEntity.getDept_cycle(),
//                        deptAgencyInfoEntity.getDept_limit(),
//                        deptAgencyInfoEntity.getNgd_limit(),
//                        deptTransactionSubTypeEntity.getName(),
//                        DateTimeUtils.getNow()
//                );
//                if (!rsDecreaseCNO) {
//                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
//                }
//
//                /**
//                 * Giảm dtt
//                 */
//                boolean rsDecreaseDTT = this.decreaseDTT(
//                        agencyOrderEntity.getAgency_id(),
//                        agencyOrderEntity.getTotal_end_price(),
//                        null,
//                        deptTransactionSubTypeEntity.getName(),
//                        DateTimeUtils.getNow(),
//                        false,
//                        sessionData.getId()
//                );
//                if (!rsDecreaseDTT) {
//                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
//                }
//
//                /**
//                 * Ghi nhận công nợ cho đơn hàng
//                 * cho hạn mục Hủy đơn hàng hẹn giao
//                 */
//
//                DeptOrderEntity deptOrderEntity = this.createDeptOrderEntity(
//                        agencyOrderEntity.getAgency_id(),
//                        DeptType.DEPT_DON_HANG.getId(),
//                        DeptTransactionMainType.INCREASE.getId(),
//                        deptTransactionSubTypeEntity.getDept_type_id(),
//                        DateTimeUtils.getNow(),
//                        deptAgencyInfoEntity.getDept_cycle(),
//                        deptTransactionSubTypeEntity.getCn_effect_type(),
//                        deptTransactionSubTypeEntity.getDtt_effect_type(),
//                        deptTransactionSubTypeEntity.getTt_effect_type(),
//                        deptTransactionSubTypeEntity.getAcoin_effect_type(),
//                        agencyOrderEntity.getTotal_end_price(),
//                        deptAgencyInfoEntity.getDept_cycle_end(),
//                        agencyOrderEntity.getCode(),
//                        agencyOrderEntity.getCode(),
//                        null,
//                        0L,
//                        DeptOrderStatus.WAITING.getId()
//                );
//                deptOrderEntity.setCreated_date(DateTimeUtils.getNow());
//                deptOrderEntity.setCreator_id(sessionData.getId());
//                int rsCreateDeptOrder = this.deptDB.createDeptOrder(deptOrderEntity);
//                if (rsCreateDeptOrder <= 0) {
//                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
//                }
//                deptOrderEntity.setId(rsCreateDeptOrder);
//                JSONObject deptOrderByDonHang = this.deptDB.getDeptOrderInfoByOrderCode(
//                        agencyOrderEntity.getCode(),
//                        DeptType.DEPT_DON_HANG.getId()
//                );
//                if (deptOrderByDonHang != null &&
//                        ConvertUtils.toInt(deptOrderByDonHang.get("status")) != DeptOrderStatus.FINISH.getId()) {
//                    ClientResponse crCanTru = this.canTruCongNoToDeptOrder(
//                            this.deptDB.getDeptTransaction(cancelTransactionEntity.getId()),
//                            deptOrderByDonHang,
//                            DateTimeUtils.getNow());
//                    if (crCanTru.failed()) {
//                        return crCanTru;
//                    }
//                }
//
//                /**
//                 * Nếu tồn tại thanh toán chưa dùng hết
//                 */
//                ClientResponse crCanTru = this.canTruCongNo(
//                        agencyOrderEntity.getAgency_id(),
//                        false,
//                        DateTimeUtils.getNow());
//                if (crCanTru.failed()) {
//                    return crCanTru;
//                }
//
//
//                /**
//                 * Tính lại nợ
//                 */
//                this.updateDeptAgencyInfo(agencyOrderEntity.getAgency_id());
//            }

            /**
             * Nhả tồn kho
             */
            if (OrderStatus.isOrderWaitingApprove(current_status)
                    && !(OrderStatus.WAITING_CONFIRM.getKey() == current_status && agencyOrderEntity.getStuck_type() == StuckType.NQH_CK.getId())) {
                /**
                 * Nhả tồn kho chờ xác nhận
                 */
                List<JSONObject> products = this.orderDB.getListProductInOrder(request.getId());
                List<JSONObject> goods = this.orderDB.getListGoodsInOrder(request.getId());
                List<ProductData> productDataList = this.groupItemOrder(products, goods);
                for (ProductData product : productDataList) {
                    int product_id = product.getId();
                    int product_total_quantity = product.getQuantity();

                    boolean rsDecreaseQuantityWaitingApprove = this.decreaseQuantityWaitingApproveToday(
                            product_id, product_total_quantity,
                            agencyOrderEntity.getCode());
                    if (!rsDecreaseQuantityWaitingApprove) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }
            } else if (OrderStatus.isOrderWaitingShip(current_status)) {
                /**
                 * Nhả tồn kho chờ giao
                 */
                List<JSONObject> products = this.orderDB.getListProductInOrder(request.getId());
                List<JSONObject> goods = this.orderDB.getListGoodsInOrder(request.getId());
                List<ProductData> productDataList = this.groupItemOrder(products, goods);
                for (ProductData product : productDataList) {
                    int product_id = product.getId();
                    int product_total_quantity = product.getQuantity();

                    boolean rsDecreaseQuantityWaitingShip = this.decreaseQuantityWaitingShipToday(
                            product_id, product_total_quantity,
                            agencyOrderEntity.getCode());
                    if (!rsDecreaseQuantityWaitingShip) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }
            }

            /**
             * Nếu có cam kết thì hủy cam kết
             */
            JSONObject commit = this.orderDB.getAgencyOrderCommitByOrder(request.getId());
            if (commit != null) {
                int miss_commit = ConvertUtils.toInt(commit.get("miss_commit"));
                boolean rsCancelCommit = this.orderDB.cancelCommit(request.getId());
                if (!rsCancelCommit) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                if (miss_commit == YesNoStatus.YES.getValue()) {
                    this.deptDB.increaseMissCommit(agencyOrderEntity.getAgency_id());
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Trả về đại lý
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse refuseOrderToAgency(SessionData sessionData, RefuseOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject agencyOrder = this.orderDB.getAgencyOrder(request.getId()
            );
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int agency_id = ConvertUtils.toInt(
                    agencyOrder.get("agency_id"));
            int status = ConvertUtils.toInt(
                    agencyOrder.get("status"));

            if (!this.dataManager.getStaffManager().checkManageOrder(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(agency_id),
                    status
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            String code = ConvertUtils.toString(agencyOrder.get("code"));

            if (IncreaseDeptStatus.YES.getValue() ==
                    ConvertUtils.toInt(agencyOrder.get("increase_dept"))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INCREASE_DEPT);
            }

            /**
             * ràng buộc trạng thái được phép trả đơn
             */
            clientResponse = this.checkCanRefuseOrder(
                    status,
                    ConvertUtils.toInt(agencyOrder.get("source"))
            );
            if (clientResponse.failed()) {
                return clientResponse;
            }

            boolean rsUpdateAgencyOrderStatus = this.orderDB.updateAgencyOrderStatus(request.getId(), OrderStatus.RETURN_AGENCY.getKey(), request.getNote(), sessionData.getId());
            if (!rsUpdateAgencyOrderStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.orderDB.deleteAgencyPromoHuntSale(request.getId());

            /**
             * lưu lịch sử thay đổi trạng thái đơn hàng
             */
            this.orderDB.saveUpdateOrderStatusHistory(request.getId(), OrderStatus.RETURN_AGENCY.getKey(), request.getNote(), sessionData.getId());

            if (status == OrderStatus.WAITING_CONFIRM.getKey()) {
                if (ConvertUtils.toInt(agencyOrder.get("stuck_type")) != StuckType.NQH_CK.getId()) {
                    /**
                     * Nhả kho chờ xác nhận
                     */
                    List<ProductData> productDataList = this.getAllProductInOrder(request.getId());
                    ClientResponse crDecrease = this.decreaseWarehouseWaitingApprove(
                            productDataList,
                            code);
                    if (crDecrease.failed()) {
                        return crDecrease;
                    }
                }
            } else if (status == OrderStatus.PREPARE.getKey() ||
                    status == OrderStatus.RESPONSE.getKey()) {
                /**
                 * Nhả kho chờ giao
                 */
                List<ProductData> productDataList = this.getAllProductInOrder(request.getId());
                ClientResponse crDecrease = this.decreaseWarehouseWaitingShip(
                        productDataList,
                        code
                );
                if (crDecrease.failed()) {
                    return crDecrease;
                }
            }

            /**
             * Nếu đơn hàng có cam kết đang thực hiện thì hủy cam kết của đơn hàng đó
             */
            JSONObject commit = this.orderDB.getAgencyOrderCommitByOrder(
                    request.getId()
            );
            if (commit != null &&
                    ConvertUtils.toInt(agencyOrder.get("commit_approve_status")) == CommitApproveStatus.WAITING.getId()) {
                /**
                 * Hủy cam kết
                 */
                boolean rsCancelCommit = this.orderDB.cancelCommit(request.getId());
                if (!rsCancelCommit) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CANCEL_COMMIT_FAILED);
                }

                this.pushNotifyToAgency(0,
                        NotifyAutoContentType.REFUSE_ORDER_COMMIT,
                        "",
                        NotifyAutoContentType.REFUSE_ORDER_COMMIT.getType(),
                        JsonUtils.Serialize(
                                Arrays.asList(ConvertUtils.toString(request.getId()))
                        ),
                        "Đơn đặt hàng " + code + " của Quý khách đã bị từ chối vì cam kết không phù hợp.",
                        ConvertUtils.toInt(agencyOrder.get("agency_id")));
            } else {
                this.pushNotifyToAgency(0,
                        NotifyAutoContentType.REFUSE_ORDER,
                        "",
                        NotifyAutoContentType.REFUSE_ORDER.getType(),
                        JsonUtils.Serialize(
                                Arrays.asList(ConvertUtils.toString(request.getId()))
                        ),
                        "Đơn đặt hàng " + code + " đã được trả về cho Quý khách điều chỉnh, vui lòng hoàn tất đơn hàng",
                        ConvertUtils.toInt(agencyOrder.get("agency_id")));
            }

            /**
             * Nhả voucher nếu có
             */
            this.orderDB.returnVoucher(request.getId());

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<ProductData> getAllProductInOrder(int agency_order_id) {
        List<JSONObject> products = this.orderDB.getListProductInOrder(agency_order_id);
        List<JSONObject> goods = this.orderDB.getListGoodsInOrder(agency_order_id);
        return this.groupItemOrder(products, goods);
    }

    private List<ProductData> sumProductInOrder(
            List<JSONObject> products,
            List<JSONObject> goods) {
        return this.groupItemOrder(products, goods);
    }

    private ClientResponse checkCanRefuseOrder(Integer status, int source) {
        if (status == OrderStatus.WAITING_CONFIRM.getKey() ||
                status == OrderStatus.RESPONSE.getKey() ||
                status == OrderStatus.PREPARE.getKey()) {
            return ClientResponse.success(null);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
    }

    private ClientResponse checkCanRejectOrder(Integer status, int source) {
        if (status == OrderStatus.WAITING_CONFIRM.getKey() ||
                status == OrderStatus.WAITING_APPROVE.getKey()) {
            return ClientResponse.success(null);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
    }

    private ClientResponse checkCanCancelOrder(int status) {
        if (status == OrderStatus.COMPLETE.getKey()
                || status == OrderStatus.CANCEL.getKey()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        return ClientResponse.success(null);
    }

    /**
     * Xác nhận đang giao hàng
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse confirmShippingOrder(SessionData sessionData, ConfirmShippingOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            AgencyOrderEntity agencyOrderEntity = this.orderDB.getAgencyOrderEntity(
                    request.getId()
            );
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int previousStatus = agencyOrderEntity.getStatus();
            if (OrderStatus.PREPARE.getKey() != agencyOrderEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsUpdateAgencyOrderStatus = this.orderDB.confirmShippingOrder(
                    request.getId(),
                    OrderStatus.SHIPPING.getKey(),
                    request.getNote(),
                    sessionData.getId());
            if (!rsUpdateAgencyOrderStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            /**
             * save update order status history
             */
            this.orderDB.saveUpdateOrderStatusHistory(request.getId(), OrderStatus.SHIPPING.getKey(), request.getNote(), sessionData.getId());

            /**
             * Kiểm tra ngày hẹn giao -> chuyển về loại phiếu hẹn giao
             */
            //this.forwardOrderToOrderAppointment(agencyOrderEntity);

            JSONObject agencyOrderDept = this.orderDB.getAgencyOrderDept(request.getId());
            if (agencyOrderDept == null) {
                int rsSaveAgencyOrderDept = this.saveAgencyOrderDept(
                        agencyOrderEntity);
                if (rsSaveAgencyOrderDept <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                boolean rsUpdateAgencyOrderTotal = this.orderDB.updateAgencyOrderTotal(
                        request.getId(),
                        1
                );
            }

            /**
             * Lưu mã công nợ
             */
            List<JSONObject> agencyOrderDeptList = this.saveDeptCode(agencyOrderEntity);


            /**
             * ghi nhận tích lũy
             */
//            this.callGhiNhanTichLuy(
//                    agencyOrderEntity,
//                    agencyOrderDeptList
//            );

            /**
             * Đồng bộ kết nối với Bravo thất bại.
             */
            BasicRequest basicRequest = new BasicRequest();
            basicRequest.setId(request.getId());
            ClientResponse crSync = this.syncOrderToBravo(
                    sessionData,
                    basicRequest
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public void callGhiNhanTichLuyDonHang(
            int agency_order_id,
            int agency_id,
            List<JSONObject> agencyOrderDeptList,
            long order_time,
            long dept_time
    ) {
        try {
            for (JSONObject agencyOrderDept : agencyOrderDeptList) {
                if (this.appUtils.isOrderNormal(
                        ConvertUtils.toInt(
                                agencyOrderDept.get("promo_id"))
                )) {
                    this.accumulateService.autoAddTransactionOrder(
                            CTTLTransactionType.DON_HANG.getKey(),
                            agency_id,
                            ConvertUtils.toString(agencyOrderDept.get("dept_code")),
                            agency_order_id,
                            ConvertUtils.toLong(agencyOrderDept.get("total_end_price")),
                            ConvertUtils.toInt(
                                    agencyOrderDept.get("id")),
                            CTTLTransactionSource.AUTO.getId(),
                            0,
                            order_time,
                            dept_time
                    );
                    break;
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    private List<JSONObject> saveDeptCode(AgencyOrderEntity agencyOrderEntity) {
        try {
            List<JSONObject> agencyOrderDeptList = this.orderDB.getListAgencyOrderDept(
                    agencyOrderEntity.getId()
            );
            for (JSONObject agencyOrderDept : agencyOrderDeptList) {
                String dept_code = this.appUtils.getAgencyOrderDeptCode(
                        agencyOrderEntity.getCode(),
                        ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                        agencyOrderEntity.getTotal(),
                        ConvertUtils.toInt(agencyOrderDept.get("promo_id")) != 0,
                        this.hasOrderNormal(agencyOrderEntity.getId())
                );

                this.orderDB.saveDeptCode(
                        ConvertUtils.toInt(agencyOrderDept.get("id")),
                        dept_code
                );

                agencyOrderDept.put("dept_code", dept_code);
            }
            return agencyOrderDeptList;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return null;
    }

    private int saveAgencyOrderDept(AgencyOrderEntity agencyOrderEntity) {
        try {
            return this.orderDB.insertAgencyOrderDept(
                    agencyOrderEntity.getId(),
                    1,
                    0,
                    agencyOrderEntity.getDept_cycle(),
                    agencyOrderEntity.getTotal_begin_price(),
                    agencyOrderEntity.getTotal_promotion_price(),
                    agencyOrderEntity.getTotal_end_price(),
                    0,
                    "{}",
                    agencyOrderEntity.getTotal_promotion_product_price(),
                    agencyOrderEntity.getTotal_promotion_order_price(),
                    agencyOrderEntity.getTotal_promotion_order_price_ctkm(),
                    agencyOrderEntity.getTotal_refund_price(),
                    agencyOrderEntity.getTotal_dm_price(),
                    agencyOrderEntity.getTotal_voucher_price()
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return 0;
    }

    private void forwardOrderToOrderAppointment(AgencyOrderEntity agencyOrderEntity) {
        try {
            Date confirm_prepare_date = DateTimeUtils.getDateTime(
                    agencyOrderEntity.getConfirm_prepare_date(), "yyyy-MM-dd");
            Date request_delivery_date = DateTimeUtils.getDateTime(
                    agencyOrderEntity.getRequest_delivery_date(), "yyyy-MM-dd");
            if (AppUtils.isHenGiao(
                    confirm_prepare_date,
                    request_delivery_date,
                    this.dataManager.getConfigManager().getNumberDateScheduleDelivery())
            ) {
                this.orderDB.forwardToOrderAppointment(agencyOrderEntity.getId());
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    /**
     * Từ chối duyệt đơn hàng
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse rejectOrder(SessionData sessionData, RefuseOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            AgencyOrderEntity agencyOrderEntity = this.orderDB.getAgencyOrderEntity(request.getId()
            );
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (!this.dataManager.getStaffManager().checkManageOrder(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(agencyOrderEntity.getAgency_id()),
                    agencyOrderEntity.getStatus()
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            int previousStatus = agencyOrderEntity.getStatus();

            /**
             * ràng buộc trạng thái được từ chối
             */
            clientResponse = this.checkCanRejectOrder(agencyOrderEntity.getStatus(), agencyOrderEntity.getSource());
            if (clientResponse.failed()) {
                return clientResponse;
            }

            if (previousStatus == OrderStatus.WAITING_CONFIRM.getKey()) {
                int stuck_type = ConvertUtils.toInt(agencyOrderEntity.getStuck_type());
                if (StuckType.NQH_CK.getId() == stuck_type) {
                    /**
                     * Nếu có cam kết thì hủy cam kết
                     */
                    JSONObject commit = this.orderDB.getAgencyOrderCommitByOrder(request.getId());
                    if (commit != null) {
                        boolean rsCancelCommit = this.orderDB.cancelCommit(request.getId());
                        if (!rsCancelCommit) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                    }

                    boolean rsUpdateAgencyOrderStatus = this.orderDB.updateAgencyOrderStatus(request.getId(), OrderStatus.CANCEL.getKey(), request.getNote(), sessionData.getId());
                    if (!rsUpdateAgencyOrderStatus) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    this.orderDB.saveUpdateOrderStatusHistory(request.getId(), OrderStatus.CANCEL.getKey(), request.getNote(), sessionData.getId());

                } else {
                    boolean rsUpdateAgencyOrderStatus = this.orderDB.updateAgencyOrderStatus(request.getId(), OrderStatus.RETURN_AGENCY.getKey(), request.getNote(), sessionData.getId());
                    if (!rsUpdateAgencyOrderStatus) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    this.orderDB.saveUpdateOrderStatusHistory(request.getId(), OrderStatus.RETURN_AGENCY.getKey(), request.getNote(), sessionData.getId());

                    /**
                     * Nhả kho
                     */
                    ClientResponse crDecrease = this.decreaseWarehouseWaitingApprove(
                            this.getAllProductInOrder(request.getId()),
                            agencyOrderEntity.getCode()
                    );
                    if (crDecrease.failed()) {
                        return crDecrease;
                    }
                }

                this.orderDB.deleteAgencyPromoHuntSale(request.getId());

                return ClientResponse.success(null);
            } else if (previousStatus == OrderStatus.WAITING_APPROVE.getKey()) {
                boolean rsUpdateAgencyOrderStatus = this.orderDB.updateAgencyOrderStatus(request.getId(), OrderStatus.DRAFT.getKey(), request.getNote(), sessionData.getId());
                if (!rsUpdateAgencyOrderStatus) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                this.orderDB.deleteAgencyPromoHuntSale(request.getId());

                /**
                 * save update order status history
                 */
                this.orderDB.saveUpdateOrderStatusHistory(request.getId(), OrderStatus.DRAFT.getKey(), request.getNote(), sessionData.getId());

                /**
                 * Nhả kho nếu ngậm
                 */
                ClientResponse crDecrease = this.decreaseWarehouseWaitingApprove(
                        this.getAllProductInOrder(request.getId()),
                        agencyOrderEntity.getCode()
                );
                if (crDecrease.failed()) {
                    this.alertToTelegram("Nhả kho nếu ngậm " + agencyOrderEntity.getCode(),
                            ResponseStatus.FAIL);
                }

                /**
                 * Nhả voucher nếu có
                 */
                this.returnVoucher(request.getId());

                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Phản hồi đơn soạn hàng
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse responseOrder(SessionData sessionData, RefuseOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            AgencyOrderEntity agencyOrderEntity = this.orderDB.getAgencyOrderEntity(request.getId()
            );
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (!this.dataManager.getStaffManager().checkManageOrder(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(agencyOrderEntity.getAgency_id()),
                    agencyOrderEntity.getStatus()
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            int previousStatus = agencyOrderEntity.getStatus();

            /**
             * ràng buộc trạng thái được phép trả đơn
             */
            clientResponse = this.checkCanResponseOrder(agencyOrderEntity.getStatus());
            if (clientResponse.failed()) {
                return clientResponse;
            }

            boolean rsUpdateAgencyOrderStatus = this.orderDB.updateAgencyOrderStatus(request.getId(), OrderStatus.RESPONSE.getKey(), request.getNote(), sessionData.getId());
            if (!rsUpdateAgencyOrderStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * save update order status history
             */
            this.orderDB.saveUpdateOrderStatusHistory(request.getId(), OrderStatus.RESPONSE.getKey(), request.getNote(), sessionData.getId());

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse checkCanResponseOrder(Integer status) {
        if (status != OrderStatus.PREPARE.getKey()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_PROCESSING);
        }
        return ClientResponse.success(null);
    }

    /**
     * Yêu cầu duyệt đơn hàng
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse createRequestApproveOrder(SessionData sessionData, CreateRequestApproveOrderRequest request) {
        try {
            AgencyOrderEntity agencyOrderEntity = this.orderDB.getAgencyOrderEntity(request.getId());
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            if (!this.dataManager.getStaffManager().checkManageOrder(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(agencyOrderEntity.getAgency_id()),
                    agencyOrderEntity.getStatus()
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            if (OrderStatus.DRAFT.getKey() != agencyOrderEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            if (agencyOrderEntity.getVoucher_info() != null &&
                    !agencyOrderEntity.getVoucher_info().isEmpty() &&
                    !agencyOrderEntity.getVoucher_info().equals("[]")) {
                if (this.validateVoucher(
                        JsonUtils.DeSerialize(agencyOrderEntity.getVoucher_info(), new TypeToken<List<Integer>>() {
                        }.getType())).failed()) {
                    ClientResponse crVoucher = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    crVoucher.setMessage("[VOUCHER] Voucher không tồn tại hoặc đã hết hạn");
                    return crVoucher;
                }
            }

            /**
             * Check tồn kho
             */
            List<JSONObject> products = this.orderDB.getListProductInOrder(request.getId());
            List<JSONObject> goods = this.orderDB.getListGoodsInOrder(request.getId());
            List<ProductData> productDataList = this.sumProductInOrder(products, goods);
            ClientResponse crTonKho = this.checkTonKho(productDataList);
            if (crTonKho.failed()) {
                return crTonKho;
            }

            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agencyOrderEntity.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agencyOrderEntity.getAgency_id());
                if (deptAgencyInfoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_AGENCY_INFO_EMPTY);
                }
            }
            List<StuckData> stuckDataList = this.checkStuck(agencyOrderEntity, deptAgencyInfoEntity);
            if (stuckDataList.size() > 0) {
                boolean rsUpdateAgencyOrderStuckType = this.orderDB.updateAgencyOrderStuckType(
                        request.getId(),
                        stuckDataList.get(0).getStuck_type().getId(),
                        this.parseStuckInfoToString(stuckDataList)
                );
                if (!rsUpdateAgencyOrderStuckType) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            boolean rsCreateRequestApprove = this.orderDB.updateAgencyOrderStatus(
                    request.getId(),
                    OrderStatus.WAITING_APPROVE.getKey(),
                    request.getNote(),
                    sessionData.getId());
            if (!rsCreateRequestApprove) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.orderDB.saveUpdateOrderStatusHistory(request.getId(),
                    OrderStatus.WAITING_APPROVE.getKey(), request.getNote(),
                    sessionData.getId());

            /**
             * Lưu sản phẩm ăn ctss
             */
            this.saveAgencyPromoHuntSale(
                    request.getId(),
                    agencyOrderEntity.getAgency_id()
            );

            /**
             * Cộng kho chờ duyệt
             */
            ClientResponse crIncrease = this.increaseWarehouseWaitingApprove(
                    productDataList,
                    agencyOrderEntity.getCode()
            );
            if (crIncrease.failed()) {
                return crIncrease;
            }

            this.pushNotifyToAgency(
                    0,
                    NotifyAutoContentType.CREATE_REQUEST_APPROVE_ORDER,
                    "",
                    NotifyAutoContentType.CREATE_REQUEST_APPROVE_ORDER.getType(),
                    JsonUtils.Serialize(
                            Arrays.asList(ConvertUtils.toString(request.getId()))
                    ),
                    "Anh Tin đã tạo giúp Quý khách đơn đặt hàng " + agencyOrderEntity.getCode() + ". Vui lòng kiểm tra đơn hàng.",
                    agencyOrderEntity.getAgency_id()
            );

            /**
             * Lưu voucher đã sử dụng
             */
            this.saveVoucherUsed(agencyOrderEntity.getId(), agencyOrderEntity.getVoucher_info());

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void saveAgencyPromoHuntSale(
            int agency_order_id,
            int agency_id) {
        try {

            List<JSONObject> agencyPromoHuntSaleByOrderList = this.orderDB.getAgencyPromoHuntSaleByOrder(agency_order_id);
            if (agencyPromoHuntSaleByOrderList.size() > 0) {
                this.orderDB.deleteAgencyPromoHuntSale(agency_order_id);
            }

            List<JSONObject> products = this.orderDB.getListProductHuntSaleInOrder(
                    agency_order_id
            );
            for (JSONObject product : products) {
                this.orderDB.insertAgencyPromoHuntSale(
                        agency_order_id,
                        agency_id,
                        ConvertUtils.toInt(product.get("promo_id")),
                        ConvertUtils.toInt(product.get("product_id")),
                        ConvertUtils.toInt(product.get("product_total_quantity"))
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    /**
     * Duyệt đơn hàng
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse approveRequestOrder(SessionData sessionData, ApproveRequestOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            AgencyOrderEntity agencyOrderEntity = this.orderDB.getAgencyOrderEntity(request.getId());
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (!this.dataManager.getStaffManager().checkManageOrder(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(agencyOrderEntity.getAgency_id()),
                    agencyOrderEntity.getStatus()
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            boolean rsUpdateAgencyOrderStatus = this.orderDB.confirmPrepareOrder(
                    request.getId(),
                    request.getNote(),
                    sessionData.getId());
            if (!rsUpdateAgencyOrderStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(agencyOrderEntity.getAgency_id());
            if (deptAgencyInfoEntity == null) {
                deptAgencyInfoEntity = this.initDeptAgencyDateByStartDate(deptAgencyInfoEntity, agencyOrderEntity.getAgency_id());
                if (deptAgencyInfoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DEPT_AGENCY_INFO_EMPTY);
                }
            }

            this.updateDeptCycleToOrder(
                    request.getId(),
                    deptAgencyInfoEntity.getDept_cycle(),
                    agencyOrderEntity.getTotal()
            );

            /**
             * Lưu sản phẩm ăn săn sale
             */
            this.saveAgencyPromoHuntSale(
                    agencyOrderEntity.getId(),
                    agencyOrderEntity.getAgency_id()
            );

            /**
             * save update order status history
             */
            this.orderDB.saveUpdateOrderStatusHistory(request.getId(), OrderStatus.PREPARE.getKey(), "", sessionData.getId());

            /**
             * Cộng kho chờ giao, giảm kho chờ xác nhận
             */
            List<ProductData> productDataList = this.getAllProductInOrder(request.getId());
            ClientResponse crIncreaseWaitingShip = this.increaseWarehouseWaitingShip(productDataList, agencyOrderEntity.getCode());
            if (crIncreaseWaitingShip.failed()) {
                return crIncreaseWaitingShip;
            }

            ClientResponse crDecreaseWaitingApprove = this.decreaseWarehouseWaitingApprove(productDataList, agencyOrderEntity.getCode());
            if (crDecreaseWaitingApprove.failed()) {
                return crDecreaseWaitingApprove;
            }


            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterOrderTemp(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_ORDER_TEMP, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.orderDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.orderDB.getTotal(query);
            for (JSONObject js : records) {
                js.put("agency_info", JsonUtils.Serialize(this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(js.get("agency_id"))
                )));
                String note = ConvertUtils.toString(js
                        .get("note"));
                js.put("stuck_type", this.getStuckTypeByNote(note).getId());
                js.put("stuck_info", this.getStuckInfoByNote(note));
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

    private String getStuckInfoByNote(String note) {
        try {
            String result = "";
            if (note != null && !note.isEmpty()) {
                List<JSONObject> stucks = JsonUtils.DeSerialize(note, new TypeToken<List<JSONObject>>() {
                }.getType());

                for (JSONObject stuck : stucks) {
                    if (!result.isEmpty()) {
                        result += ", ";
                    }
                    result += stuck.get("message").toString();
                }

                return result;
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return "";
    }

    private StuckType getStuckTypeByNote(String note) {
        try {
            String result = "";
            if (note != null && !note.isEmpty()) {
                List<JSONObject> stucks = JsonUtils.DeSerialize(note, new TypeToken<List<JSONObject>>() {
                }.getType());
                if (stucks != null && !stucks.isEmpty()) {
                    int error_type = ConvertUtils.toInt(stucks.get(0).get("id"));
                    return OrderErrorType.getStuckTypeBy(OrderErrorType.from(error_type));
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return StuckType.NONE;
    }

    /**
     * Chi tiết đơn hàng tạm
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse getOrderTempInfo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject data = new JSONObject();
            JSONObject agencyOrder = this.orderDB.getAgencyOrderTemp(request.getId());
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            List<JSONObject> products = new ArrayList<>();
            List<JSONObject> orderProductList = this.getListItemInOrderTemp(agencyOrder.get("products"));

            for (JSONObject orderProduct : orderProductList) {
                int product_id = ConvertUtils.toInt(orderProduct.get("id"));
                int quantity = ConvertUtils.toInt(orderProduct.get("quantity"));
                Long price = ConvertUtils.toLong(orderProduct.get("price"));
                String note = JsonUtils.Serialize(orderProduct.get("note"));

                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(product_id);
                if (productCache == null) {
                    continue;
                }
                ProductMoneyResponse productMoneyResponse = new ProductMoneyResponse();
                productMoneyResponse.setId(productCache.getId());
                productMoneyResponse.setCode(productCache.getCode());
                productMoneyResponse.setFull_name(productCache.getFull_name());
                productMoneyResponse.setProduct_small_unit_id(productCache.getProduct_small_unit_id());
                productMoneyResponse.setStep(productCache.getStep());
                productMoneyResponse.setMinimum_purchase(productCache.getMinimum_purchase());
                productMoneyResponse.setImages(productCache.getImages());
                productMoneyResponse.setPrice(price);
                productMoneyResponse.setQuantity(quantity);
                productMoneyResponse.setTotal_begin_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity());
                productMoneyResponse.setTotal_promo_price(0L);
                productMoneyResponse.setTotal_end_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity() - productMoneyResponse.getTotal_promo_price());
                productMoneyResponse.setNote(note);
                products.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
            }
            List<JSONObject> gifts = new ArrayList<>();
            List<JSONObject> goods = new ArrayList<>();
            List<JSONObject> orderGoodsList = this.getListItemInOrderTemp(agencyOrder.get("goods"));
            for (JSONObject orderProduct : orderGoodsList) {
                int product_id = ConvertUtils.toInt(orderProduct.get("id"));
                int quantity = ConvertUtils.toInt(orderProduct.get("quantity"));
                Long price = 0L;
                int type = AgencyOrderPromoType.GOODS_OFFER.getId();
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(product_id);
                if (productCache == null) {
                    continue;
                }
                ProductMoneyResponse productMoneyResponse = new ProductMoneyResponse();
                productMoneyResponse.setId(productCache.getId());
                productMoneyResponse.setCode(productCache.getCode());
                productMoneyResponse.setFull_name(productCache.getFull_name());
                productMoneyResponse.setProduct_small_unit_id(productCache.getProduct_small_unit_id());
                productMoneyResponse.setStep(productCache.getStep());
                productMoneyResponse.setMinimum_purchase(productCache.getMinimum_purchase());
                productMoneyResponse.setImages(productCache.getImages());
                productMoneyResponse.setPrice(price);
                productMoneyResponse.setQuantity(quantity);
                productMoneyResponse.setTotal_begin_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity());
                productMoneyResponse.setTotal_promo_price(0L);
                productMoneyResponse.setTotal_end_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity() - productMoneyResponse.getTotal_promo_price());
                if (AgencyOrderPromoType.GOODS_OFFER.getId() == type) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GOODS_OFFER.getKey());
                    goods.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                } else if (AgencyOrderPromoType.GOODS_BONUS.getId() == type) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GOODS_OFFER.getKey());
                    gifts.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                } else if (AgencyOrderPromoType.GIFT_BONUS.getId() == type) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GIFT_OFFER.getKey());
                    gifts.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                }
            }

            /**
             * GIft
             */
            List<JSONObject> orderGiftList = this.getListItemInOrderTemp(agencyOrder.get("bonus_gift"));
            for (JSONObject orderProduct : orderGiftList) {
                int product_id = ConvertUtils.toInt(orderProduct.get("id"));
                int quantity = ConvertUtils.toInt(orderProduct.get("quantity"));

                Long price = 0L;
                int type = AgencyOrderPromoType.GIFT_BONUS.getId();
                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(product_id);
                if (productCache == null) {
                    continue;
                }
                ProductMoneyResponse productMoneyResponse = new ProductMoneyResponse();
                productMoneyResponse.setId(productCache.getId());
                productMoneyResponse.setCode(productCache.getCode());
                productMoneyResponse.setFull_name(productCache.getFull_name());
                productMoneyResponse.setProduct_small_unit_id(productCache.getProduct_small_unit_id());
                productMoneyResponse.setStep(productCache.getStep());
                productMoneyResponse.setMinimum_purchase(productCache.getMinimum_purchase());
                productMoneyResponse.setImages(productCache.getImages());
                productMoneyResponse.setPrice(price);
                productMoneyResponse.setQuantity(quantity);
                productMoneyResponse.setTotal_begin_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity());
                productMoneyResponse.setTotal_promo_price(0L);
                productMoneyResponse.setTotal_end_price(productMoneyResponse.getPrice() * productMoneyResponse.getQuantity() - productMoneyResponse.getTotal_promo_price());
                if (AgencyOrderPromoType.GOODS_OFFER.getId() == type) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GOODS_OFFER.getKey());
                    gifts.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                } else if (AgencyOrderPromoType.GOODS_BONUS.getId() == type) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GOODS_OFFER.getKey());
                    gifts.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                } else if (AgencyOrderPromoType.GIFT_BONUS.getId() == type) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GIFT_OFFER.getKey());
                    gifts.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
                }
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
            long hmkd =
                    ConvertUtils.toLong(this.getAgencyHMKD(
                            ConvertUtils.toDouble(deptAgencyInfoEntity.getDept_limit()),
                            ConvertUtils.toDouble(deptAgencyInfoEntity.getNgd_limit()),
                            ConvertUtils.toDouble(deptAgencyInfoEntity.getCurrent_dept()),
                            totalPriceOrderDoingDept));
            orderDeptInfo.setHmkd(hmkd < 0 ? 0 : hmkd);
            orderDeptInfo.setCno(deptAgencyInfoEntity.getCurrent_dept() > 0 ? deptAgencyInfoEntity.getCurrent_dept() : deptAgencyInfoEntity.getCurrent_dept() * -1);
            long hmkd_over_order = ConvertUtils.toLong(agencyOrder.get("hmkd_over_order"));
            orderDeptInfo.setHmkd_over_order(
                    hmkd_over_order);
            orderDeptInfo.setHmkd_over_current(hmkd < 0 ? hmkd * -1 : 0);
            orderDeptInfo.setNqh_order(ConvertUtils.toLong(agencyOrder.get("nqh_order")));
            orderDeptInfo.setNqh_current(deptAgencyInfoEntity.getNqh());
            orderDeptInfo.setNgd_limit(deptAgencyInfoEntity.getNgd_limit());

            /**
             * nếu cam kết
             */
            if (StuckType.NQH_CK.getId() == ConvertUtils.toInt(agencyOrder.get("stuck_type"))) {
                JSONObject agency_order_commit = this.orderDB.getAgencyOrderCommit(agency_id);
                if (agency_order_commit != null) {
                    orderDeptInfo.setCommitted_date(ConvertUtils.toString(agency_order_commit.get("committed_date")));
                    orderDeptInfo.setCommitted_money(ConvertUtils.toLong(agency_order_commit.get("committed_money")));
                }
            }

            agencyOrder.put("agency_info", JsonUtils.Serialize(
                    this.dataManager.getAgencyManager().getAgencyBasicData(agency_id))
            );

            String note = ConvertUtils.toString(agencyOrder
                    .get("note"));
            agencyOrder.put("note", "");

            data.put("order", agencyOrder);
            data.put("dept_info", orderDeptInfo);
            data.put("products", products);
            data.put("goods", goods);
            data.put("gifts", gifts);
            data.put("order_flow", orderFlow);
            data.put("promo_products", promoProductList);
            data.put("promo_orders", promoOrderList);
            data.put("stuck_info", this.getStuckInfoByNote(note));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<JSONObject> getListItemInOrderTemp(Object data) {
        try {
            if (data != null) {
                return JsonUtils.DeSerialize(data.toString(),
                        new TypeToken<List<JSONObject>>() {
                        }.getType());
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return new ArrayList<>();
    }


    public ClientResponse cancelOrderWaitingConfirmOvertimeProcess() {
        try {
            List<JSONObject> orders = this.orderDB.getListOrderWaitingConfirm(
                    6 * 60 * 60,
                    ConfigInfo.SCHEDULE_RUNNING_LIMIT
            );

            for (JSONObject order : orders) {
                Date date = DateTimeUtils.getDateTime(order.get("update_status_date").toString());
                if (DateTimeUtils.getMilisecondsNow() >= AppUtils.getDateCancelAfterHour(date.getTime(), 6).getTime()) {
                    String order_code = ConvertUtils.toString(order.get("code"));
                    int order_id = ConvertUtils.toInt(order.get("id"));
                    int agency_id = ConvertUtils.toInt(order.get("agency_id"));

                    LogUtil.printDebug("PROCESS: order - " + order_code);

                    boolean rs = this.orderDB.cancelAgencyOrder(
                            order_id,
                            OrderStatus.CANCEL.getKey(),
                            "Quá 6 tiếng",
                            this.dataManager.getStaffManager().getStaffSystemId()
                    );
                    if (!rs) {
                        continue;
                    }

                    this.orderDB.deleteAgencyPromoHuntSale(order_id);

                    this.orderDB.saveUpdateOrderStatusHistory(
                            order_id,
                            OrderStatus.CANCEL.getKey(),
                            "Quá 6 tiếng",
                            0);

                    int stuck_type = ConvertUtils.toInt(order.get("stuck_type"));
                    if (StuckType.NQH_CK.getId() != stuck_type) {
                        /**
                         * nhả tồn kho
                         */
                        List<ProductData> productDataList = this.getAllProductInOrder(
                                ConvertUtils.toInt(order.get("id"))
                        );
                        ClientResponse clientResponse = this.decreaseWarehouseWaitingApprove(
                                productDataList,
                                ConvertUtils.toString(order.get("code")));
                        if (clientResponse.failed()) {
                            /**
                             * alert telegram
                             */
                            this.alertToTelegram(ConvertUtils.toString(order.get("code")) + ": nhả tồn kho thất bại",
                                    ResponseStatus.FAIL);
                        }

                        this.pushNotifyToAgency(
                                0,
                                NotifyAutoContentType.CANCEL_ORDER_WAITING_CONFIRM,
                                "",
                                NotifyAutoContentType.CANCEL_ORDER_WAITING_CONFIRM.getType(),
                                JsonUtils.Serialize(
                                        Arrays.asList(ConvertUtils.toString(order_id))
                                ),
                                order_code,
                                agency_id);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse cancelOrderWaitingApproveOvertimeProcess() {
        try {
            List<JSONObject> orders = this.orderDB.getListOrderWaitingApprove(
                    4 * 60 * 60,
                    ConfigInfo.SCHEDULE_RUNNING_LIMIT
            );

            for (JSONObject order : orders) {
                Date date = DateTimeUtils.getDateTime(order.get("update_status_date").toString());
                if (DateTimeUtils.getMilisecondsNow() >= AppUtils.getDateCancelAfterHour(date.getTime(), 4).getTime()) {
                    LogUtil.printDebug("PROCESS: order - " + ConvertUtils.toString(order.get("code")));
                    int order_id = ConvertUtils.toInt(order.get("id"));
                    boolean rs = this.orderDB.cancelAgencyOrder(
                            order_id,
                            OrderStatus.CANCEL.getKey(),
                            "Quá 4 tiếng",
                            1
                    );
                    if (!rs) {
                        continue;
                    }

                    this.orderDB.deleteAgencyPromoHuntSale(order_id);

                    this.orderDB.saveUpdateOrderStatusHistory(
                            order_id,
                            OrderStatus.CANCEL.getKey(),
                            "Quá 4 tiếng",
                            0);

                    /**
                     * nhả tồn kho
                     */
                    List<ProductData> productDataList = this.getAllProductInOrder(
                            ConvertUtils.toInt(order.get("id"))
                    );
                    ClientResponse clientResponse = this.decreaseWarehouseWaitingApprove(
                            productDataList,
                            ConvertUtils.toString(order.get("code")));
                    if (clientResponse.failed()) {
                        /**
                         *
                         */
                    }
                }
            }
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
    public ClientResponse filterOrderConfirmation(SessionData sessionData, FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();

            for (FilterRequest filterRequest : request.getFilters()) {
                if (filterRequest.getKey().equals("membership_id")) {
                    filterRequest.setKey("t_agency.membership_id");
                    break;
                }
            }

            this.addFilterOrderData(sessionData, request);

            String query = this.filterUtils.getQuery(FunctionList.LIST_ORDER_CONFIRMATION, request.getFilters(), request.getSorts());

            List<JSONObject> records = this.orderDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.orderDB.getTotal(query);
            for (JSONObject js : records) {
                js.put("agency_info", JsonUtils.Serialize(this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(js.get("agency_id"))
                )));
                js.put("creator_info",
                        SourceOrderType.APP.getValue() == ConvertUtils.toInt(js.get("source")) ? null :
                                this.dataManager.getStaffManager().getStaff(
                                        ConvertUtils.toInt(js.get("creator_id"))
                                ));
                List<JSONObject> jsProductList = this.orderDB.getListProductInOrderConfirm(
                        ConvertUtils.toInt(js
                                .get("id"))
                );

                JSONObject jsProductInfo = jsProductList.get(0).get("product_info") == null ? null :
                        JsonUtils.DeSerialize(jsProductList.get(0).get("product_info").toString(),
                                JSONObject.class);
                if (jsProductInfo == null) {
                    js.put("item_info", this.dataManager.getProductManager().getProductBasicData(
                            ConvertUtils.toInt(jsProductList.get(0).get("product_id"))).getFull_name());
                } else {
                    js.put("item_info", jsProductInfo.get("full_name"));
                }
                js.put("item_note", jsProductList.get(0).get("product_note"));
                js.put("item_price", jsProductList.get(0).get("product_price"));
                js.put("item_quantity", jsProductList.get(0).get("product_total_quantity"));
                js.put("total_end_price", jsProductList.get(0).get("product_total_end_price"));
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
     * Danh sách phiếu xác nhận đặt hàng
     *
     * @param request
     * @return
     */
    public ClientResponse filterOrderConfirmationProduct(SessionData sessionData, FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();

            this.filterUtils.parseFilter(FunctionList.LIST_ORDER_CONFIRMATION_PRODUCT, request);

            this.addFilterOrderData(sessionData, request);

            String query = this.filterUtils.getQuery(FunctionList.LIST_ORDER_CONFIRMATION_PRODUCT, request.getFilters(), request.getSorts());

            List<JSONObject> records = this.orderDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.orderDB.getTotal(query);
            for (JSONObject js : records) {
                try {
                    js.put("agency_info", JsonUtils.Serialize(this.dataManager.getAgencyManager().getAgencyBasicData(
                            ConvertUtils.toInt(js.get("agency_id"))
                    )));
                    js.put("creator_info",
                            SourceOrderType.APP.getValue() == ConvertUtils.toInt(js.get("source")) ? null :
                                    this.dataManager.getStaffManager().getStaff(
                                            ConvertUtils.toInt(js.get("creator_id"))
                                    ));
                    JSONObject jsProductInfo = (js.get("product_info") == null || js.get("product_info").toString().isEmpty()) ? null :
                            JsonUtils.DeSerialize(js.get("product_info").toString(),
                                    JSONObject.class);
                    if (jsProductInfo == null) {
                        js.put("item_info", this.dataManager.getProductManager().getProductBasicData(
                                ConvertUtils.toInt(js.get("product_id"))).getFull_name());
                    } else {
                        js.put("item_info", jsProductInfo.get("full_name"));
                    }
                    js.put("item_note", js.get("product_note"));
                    js.put("item_price", js.get("product_price"));
                    js.put("item_quantity", js.get("product_total_quantity"));
                    js.put("total_end_price", js.get("product_total_end_price"));
                } catch (Exception ex) {
                    LogUtil.printDebug(Module.ORDER.name(), ex);
                }
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

    public ClientResponse filterOrderDeliveryPlanProduct(SessionData sessionData, FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();

            this.filterUtils.parseFilter(FunctionList.LIST_ORDER_DELIVERY_PLAN_PRODUCT, request);

            this.addFilterOrderData(sessionData, request);

            String query = this.filterUtils.getQuery(FunctionList.LIST_ORDER_DELIVERY_PLAN_PRODUCT, request.getFilters(), request.getSorts());

            List<JSONObject> records = this.orderDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.orderDB.getTotal(query);
            for (JSONObject js : records) {
                try {
                    js.put("agency_info", JsonUtils.Serialize(this.dataManager.getAgencyManager().getAgencyBasicData(
                            ConvertUtils.toInt(js.get("agency_id"))
                    )));
                    js.put("creator_info",
                            SourceOrderType.APP.getValue() == ConvertUtils.toInt(js.get("source")) ? null :
                                    this.dataManager.getStaffManager().getStaff(
                                            ConvertUtils.toInt(js.get("creator_id"))
                                    ));
                    JSONObject jsProductInfo = (js.get("product_info") == null || js.get("product_info").toString().isEmpty()) ? null :
                            JsonUtils.DeSerialize(js.get("product_info").toString(),
                                    JSONObject.class);
                    if (jsProductInfo == null) {
                        js.put("item_info", this.dataManager.getProductManager().getProductBasicData(
                                ConvertUtils.toInt(js.get("product_id"))).getFull_name());
                    } else {
                        js.put("item_info", jsProductInfo.get("full_name"));
                    }
                    js.put("plan_delivery_date", js.get("delivery_date"));
                    js.put("item_note", js.get("product_note"));
                    js.put("item_price", js.get("product_price"));
                    js.put("item_quantity", js.get("product_total_quantity"));
                    js.put("total_end_price", js.get("product_total_end_price"));
                } catch (Exception ex) {
                    LogUtil.printDebug(Module.ORDER.name(), ex);
                }
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
    public ClientResponse getAgencyOrderConfirm(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject jsAOC = this.orderDB.getAgencyOrderConfirm(request.getId());
            if (jsAOC == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            int agency_order_id = ConvertUtils.toInt(jsAOC.get("agency_order_id"));
            JSONObject data = new JSONObject();
            JSONObject agencyOrder = this.orderDB.getAgencyOrder(agency_order_id);
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            if (!this.dataManager.getStaffManager().checkManageOrder(
                    sessionData.getId(),
                    this.agencyDB.getAgencyInfoById(ConvertUtils.toInt(agencyOrder.get("agency_id"))),
                    ConvertUtils.toInt(agencyOrder.get("status")))) {
                return ClientResponse.fail(ResponseStatus.NOT_PERMISSION, ResponseMessage.USER_FORBIDDEN);
            }

            Map<Integer, ProductMoneyResponse> products = new ConcurrentHashMap<>();
            List<JSONObject> orderProductList = this.orderDB.getListProductInOrderConfirm(request.getId());
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

                if (orderProduct.get("product_info") != null &&
                        !orderProduct.get("product_info").toString().isEmpty()) {
                    JSONObject jsProductInfo = JsonUtils.DeSerialize(
                            orderProduct.get("product_info").toString(),
                            JSONObject.class);
                    productMoneyResponse.setFull_name(
                            ConvertUtils.toString(jsProductInfo.get("full_name")));
                }

                productMoneyResponse.setNote(ConvertUtils.toString(orderProduct.get("product_note")));
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
            List<JSONObject> orderGoodsList = this.orderDB.getListGoodsInOrder(request.getId());
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

                ProductMoneyResponse product = products.get(productMoneyResponse.getId());
                if (product == null) {
                    products.put(productMoneyResponse.getId(), productMoneyResponse);
                } else {
                    product.setQuantity(product.getQuantity() + productMoneyResponse.getQuantity());
                    products.put(product.getId(), product);
                }
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
            long hmkd_over_current = this.getVuotHMKDCurrent(
                    ConvertUtils.toInt(agencyOrder.get("status")),
                    deptAgencyInfoEntity.getDept_limit(),
                    deptAgencyInfoEntity.getNgd_limit(),
                    deptAgencyInfoEntity.getCurrent_dept(),
                    total_end_price,
                    ConvertUtils.toLong(totalOrderDept));

            /**
             * Chính sách cho đơn hàng
             */
            List<PromoBasicData> ctkmOrders = new ArrayList<>();
            Map<Integer, PromoBasicData> mpCtkmOrder = new ConcurrentHashMap<>();
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
            Map<Integer, PromoBasicData> mpPromoGood = new ConcurrentHashMap<>();
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

            orderDeptInfo.setHmkd_over_current(hmkd_over_current < 0 ? hmkd_over_current * -1 : 0);
            orderDeptInfo.setNqh_order(ConvertUtils.toLong(agencyOrder.get("nqh_order")));
            orderDeptInfo.setNqh_current(deptAgencyInfoEntity.getNqh());
            orderDeptInfo.setNgd_limit(deptAgencyInfoEntity.getNgd_limit());

            /**
             * nếu cam kết
             */
            int stuck_type = ConvertUtils.toInt(agencyOrder.get("stuck_type"));
            String stuck_info = ConvertUtils.toString(agencyOrder.get("stuck_info"));
            stuck_info = this.getStuckInfoByStuckInfo(stuck_info, stuck_type);

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

            agencyOrder.put("request_delivery_date", jsAOC.get("request_delivery_date"));
            agencyOrder.put("plan_delivery_date", jsAOC.get("plan_delivery_date"));
            agencyOrder.put("note_internal", jsAOC.get("note"));
            agencyOrder.put("doc_no", jsAOC.get("doc_no"));
            data.put("order", agencyOrder);
            data.put("dept_info", orderDeptInfo);
            data.put("products", new ArrayList<ProductMoneyResponse>(products.values()));
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

    /**
     * Điều chỉnh ngày hẹn giao
     *
     * @return
     */
    public ClientResponse editRequestDeliveryDate(SessionData sessionData, EditRequestDeliveryDateRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            AgencyOrderEntity agencyOrder = this.orderDB.getAgencyOrderEntity(
                    request.getOrder_id()
            );
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            /**
             * Trạng thái không được điều chỉnh: Đang giao, đã giao, hủy
             */
            if (agencyOrder.getStatus() == OrderStatus.SHIPPING.getKey() ||
                    agencyOrder.getStatus() == OrderStatus.COMPLETE.getKey() ||
                    agencyOrder.getStatus() == OrderStatus.CANCEL.getKey()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rs = this.orderDB.editRequestDeliveryDate(
                    request.getOrder_id(),
                    request.getRequest_delivery_date() == 0 ? null
                            : DateTimeUtils.toString(
                            DateTimeUtils.getDateTime(request.getRequest_delivery_date())),
                    agencyOrder.getStatus(),
                    "Điều chỉnh ngày hẹn giao",
                    sessionData.getId()
            );
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.orderDB.saveUpdateOrderStatusHistory(
                    request.getOrder_id(),
                    agencyOrder.getStatus(),
                    "Điều chỉnh ngày hẹn giao",
                    sessionData.getId());
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Điều chỉnh ngày hẹn giao
     *
     * @return
     */
    public ClientResponse editPlanDeliveryDate(SessionData sessionData, EditRequestDeliveryDateRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrderConfirm(
                    request.getOrder_id()
            );
            if (jsAgencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            boolean rs = this.orderDB.editPlanDeliveryDate(
                    request.getOrder_id(),
                    request.getRequest_delivery_date() == 0 ? null
                            : DateTimeUtils.toString(
                            DateTimeUtils.getDateTime(request.getRequest_delivery_date())),
                    ConvertUtils.toInt(jsAgencyOrder.get("status")),
                    "Edit delivery date plan",
                    sessionData.getId()
            );
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse cancelOrderByApp(CancelOrderByAppRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            AgencyOrderEntity agencyOrder = this.orderDB.getAgencyOrderEntity(
                    request.getId()
            );
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
            cancelOrderRequest.setId(request.getId());
            cancelOrderRequest.setNote("Đại lý hủy đơn");
            ClientResponse crExcuteCancelOrder = this.excuteCancelOrder(
                    cancelOrderRequest,
                    agencyOrder,
                    this.dataManager.getStaffManager().getSessionStaffBot());
            if (crExcuteCancelOrder.failed()) {
                return crExcuteCancelOrder;
            }

            /**
             * Trả lại voucher nếu có
             */
            this.returnVoucher(request.getId());

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse checkSanSale(EstimateCostOrderRequest request) {
        try {
            /**
             * Kiểm tra dũ liệu request
             */
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            if (request.getHunt_sale_products().isEmpty()) {
                return ClientResponse.success(null);
            }

            /**
             * Kiểm tra đại lý
             */
            AgencyEntity agencyEntity = this.agencyDB.getAgencyEntity(request.getAgency_id());
            if (agencyEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            /**
             * Săn sale
             */
            Map<Integer, ProductCache> mpProductCache = new ConcurrentHashMap<>();

            Map<Integer, ProgramOrderProduct> mpProgramOrderProduct =
                    this.initMpProductHuntSale(
                            agencyEntity,
                            mpProductCache,
                            request.getHunt_sale_products(),
                            new ArrayList<>());

            /**
             * Kiểm tra ẩn hiện
             */
            ClientResponse crCheckVisibilityAndPrice = this.checkVisibilityWhenCreateOrder(
                    agencyEntity.getId(),
                    agencyEntity.getCity_id(),
                    agencyEntity.getRegion_id(),
                    agencyEntity.getMembership_id(),
                    new ArrayList<>(mpProgramOrderProduct.values()));
            if (crCheckVisibilityAndPrice.failed()) {
                return crCheckVisibilityAndPrice;
            }

            Map<Integer, ProgramOrderProduct> mpAllProduct = new ConcurrentHashMap<>();
            mpAllProduct.putAll(mpProgramOrderProduct);

            Map<Integer, Integer> mpStock = this.initMpStock(mpAllProduct);
            List<HuntSaleOrderDetail> huntSaleProductList =
                    this.checkOrder(
                            agencyEntity.getId(),
                            request.getHunt_sale_products(),
                            mpProductCache,
                            mpStock
                    );

            /**
             * Số lượng đã sử dun trong đơn hàng
             */
            Map<Integer, Integer> mpProductQuantityUsed = new HashMap();

            EstimatePromo estimatePromoHuntSale = this.createEstimatePromoHuntSale(
                    agencyEntity,
                    request.getHunt_sale_products());

            Map<Integer, ProgramSanSaleOffer> mpResponseProgramSanSaleOffer = new ConcurrentHashMap<>();
            int source = Source.WEB.getValue();
            this.checkSanSaleForProduct(
                    this.dataManager.getProgramManager().getAgency(
                            request.getAgency_id()
                    ),
                    mpProgramOrderProduct,
                    source == Source.APP.getValue() ? Source.APP : Source.WEB,
                    mpResponseProgramSanSaleOffer
            );
            JSONObject data = new JSONObject();
            data.put("products", new ArrayList<>(mpAllProduct.values()));
            data.put("mpResponseProgramSanSaleOffer", new ArrayList<>(mpResponseProgramSanSaleOffer.values()));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse cancelOrderByBravo(int agency_order_dept_id) {
        try {
            JSONObject agencyOrderDept = this.orderDB.getAgencyOrderDeptById(agency_order_dept_id);
            if (agencyOrderDept == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            AgencyOrderEntity agencyOrder = this.orderDB.getAgencyOrderEntity(
                    ConvertUtils.toInt(agencyOrderDept.get("agency_order_id"))
            );
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            ClientResponse crCheckCancel = this.checkCanCancelOrder(agencyOrder.getStatus());
            if (crCheckCancel.failed()) {
                return crCheckCancel;
            }

            JSONObject agencyOrderDeptComplete = this.orderDB.getAgencyOrderDeptCompleteByAgencyOrderId(
                    agencyOrder.getId()
            );
            if (agencyOrderDeptComplete != null) {
                return crCheckCancel;
            }

            CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
            cancelOrderRequest.setId(
                    agencyOrder.getId()
            );
            cancelOrderRequest.setNote("Hủy dơn từ Bravo");
            ClientResponse crExcuteCancelOrder = this.excuteCancelOrder(
                    cancelOrderRequest,
                    agencyOrder,
                    this.dataManager.getStaffManager().getSessionStaffBot());
            if (crExcuteCancelOrder.failed()) {
                return crExcuteCancelOrder;
            }

            /**
             * Trả lại voucher nếu có
             */
            this.returnVoucher(agencyOrder.getId());

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse syncOrderToBravo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject agency_order = this.orderDB.getAgencyOrder(request.getId());
            if (agency_order == null) {
                this.orderDB.syncOrderFailed(request.getId(), ResponseMessage.ORDER_NOT_FOUND.getValue());
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            JSONObject agency_info = this.agencyDB.getAgencyInfo(
                    ConvertUtils.toInt(agency_order.get("agency_id"))
            );
            if (agency_info == null) {
                this.orderDB.syncOrderFailed(request.getId(), ResponseMessage.AGENCY_NOT_FOUND.getValue());
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            JSONObject deliveryApp = JsonUtils.DeSerialize(
                    agency_order.get("address_delivery").toString(),
                    JSONObject.class
            );
            if (deliveryApp == null) {
                this.orderDB.syncOrderFailed(request.getId(), ResponseMessage.ADDRESS_INVALID.getValue());
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ADDRESS_INVALID);
            }

            JSONObject delivery_info = this.agencyDB.getDeliveryInfo(
                    ConvertUtils.toInt(deliveryApp.get("id"))
            );
            if (delivery_info == null) {
                this.orderDB.syncOrderFailed(request.getId(), ResponseMessage.ADDRESS_INVALID.getValue());
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ADDRESS_INVALID);
            }

            JSONObject bill_info = null;
            JSONObject billingApp = JsonUtils.DeSerialize(
                    agency_order.get("address_billing").toString(),
                    JSONObject.class
            );
            if (billingApp != null) {
                bill_info = this.agencyDB.getBillingInfo(
                        ConvertUtils.toInt(billingApp.get("id")));
            }

            boolean isSuccess = true;
            ClientResponse crSyncOrderToBravo = ClientResponse.success(null);
            List<JSONObject> agency_order_depts = this.orderDB.getListAgencyOrderDept(request.getId());
            for (JSONObject agency_order_dept : agency_order_depts) {
                if (ConvertUtils.toInt(agency_order_dept.get("sync_status")) == SyncStatus.SUCCESS.getValue()) {
                    continue;
                }
                crSyncOrderToBravo = this.callSyncOrderToBravo(
                        agency_order,
                        agency_order_dept,
                        this.orderDB.getListProductByAgencyOrderDept(
                                ConvertUtils.toInt(agency_order_dept.get("agency_order_id")),
                                ConvertUtils.toInt(agency_order_dept.get("promo_id"))
                        ),
                        this.orderDB.getListGiftByAgencyOrderDept(
                                ConvertUtils.toInt(agency_order_dept.get("agency_order_id")),
                                ConvertUtils.toInt(agency_order_dept.get("promo_id"))
                        ),
                        delivery_info,
                        bill_info,
                        agency_info
                );
                if (crSyncOrderToBravo.failed()) {
                    this.orderDB.syncOrderDeptFailed(
                            request.getId(),
                            crSyncOrderToBravo.getMessage());
                    isSuccess = false;
                    break;
                } else {
                    this.orderDB.syncOrderDeptSuccess(request.getId());
                }
            }
            if (!isSuccess) {
                this.orderDB.syncOrderFailed(request.getId(), crSyncOrderToBravo.getMessage());
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SYNC_FAILED);
            } else {
                this.orderDB.syncOrderSuccess(request.getId());
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse callSyncOrderToBravo(
            JSONObject agencyOrder,
            JSONObject agencyOrderDept,
            List<JSONObject> products,
            List<JSONObject> gifts,
            JSONObject deliveryInfo,
            JSONObject billInfo,
            JSONObject agencyInfo
    ) {
        try {
            JSONObject order = new JSONObject();
//            {
//                "appId": 1,
            order.put("appId", ConvertUtils.toInt(agencyOrderDept.get("id")));
//                    "docDate": "2023-07-31",
            order.put("docDate", DateTimeUtils.toString(
                    DateTimeUtils.getDateTime(
                            agencyOrder.get("confirm_prepare_date").toString(), "yyyy-MM-dd"),
                    "yyyy-MM-dd"));
//                    "docNo": "SO0001",
            int order_data_index = ConvertUtils.toInt(agencyOrderDept.get("order_data_index"));

            order.put("docNo",
                    this.appUtils.getAgencyOrderDeptCode(
                            ConvertUtils.toString(agencyOrder.get("code")),
                            order_data_index,
                            ConvertUtils.toInt(agencyOrder.get("total")),
                            ConvertUtils.toInt(agencyOrderDept.get("promo_id")) != 0,
                            this.hasOrderNormal(ConvertUtils.toInt(agencyOrderDept.get("agency_order_id"))))
            );
//                    "customerAppId": 2,
            order.put("customerAppId", ConvertUtils.toInt(agencyInfo.get("id")));
//                    "customerCode": "BRAVO",
            order.put("customerCode", ConvertUtils.toString(agencyInfo.get("code")));
//                    "customerName": "Công ty cổ phần phần mềm BRAVO",
            order.put("customerName", ConvertUtils.toString(agencyInfo.get("shop_name")));
//                    "contactBravoId": 4640,
            order.put("contactBravoId", billInfo == null ? 0 : ConvertUtils.toString(billInfo.get("bravo_id")));
//                    "contactCode": "ANHTIN14_001",
            order.put("contactCode", "");
//                    "contactName": "Dũng Hương",
            order.put("contactName", "");
//                    "deliveryBravoId": 66282,
            order.put("deliveryBravoId", ConvertUtils.toString(deliveryInfo.get("bravo_id")));
//                    "deliveryCode": "ANHTIN14_G001",
            order.put("deliveryCode", "");
//                    "deliveryName": "Vận tải Tiến Phát Hưng",
            order.put("deliveryName", "");
//                    "estimatedTimeDelivery": "2023-07-31",
            order.put("estimatedTimeDelivery",
                    ConvertUtils.toString(agencyOrder.get("request_delivery_date")).isEmpty() ?
                            order.get("docDate") : (
                            DateTimeUtils.toString(
                                    DateTimeUtils.getDateTime(
                                            ConvertUtils.toString(agencyOrder.get("request_delivery_date"))), "yyyy-MM-dd")));
//                    "warehouseTypeAvailableCode": "01",
            order.put("warehouseTypeAvailableCode", "");
//                    "noteWarehouse": "Ghi chú cho kho",
            order.put("noteWarehouse", "");
//                    "note": "Ghi chú",
            order.put("note",
                    (ConvertUtils.toString(agencyOrder.get("note"))
                            + ConvertUtils.toString(agencyOrder.get("note_internal"))))
            ;
//                    "dueDate": 30,
            order.put("dueDate", ConvertUtils.toInt(agencyOrderDept.get("dept_cycle")));
//                    "totalQuantityApp": 5,
            int totalQuantityApp = 0;
//            "detailData": [
            Map<Integer, JSONObject> detailData = new ConcurrentHashMap<>();
            for (JSONObject product : products) {
                int product_id = ConvertUtils.toInt(product.get("product_id"));
                int product_total_quantity = ConvertUtils.toInt(product.get("product_total_quantity"));
                if (product_total_quantity <= 0) {
                    continue;
                }
                JSONObject detail = detailData.get(product_id);
                if (detail == null) {
                    detail = new JSONObject();
                    //                    "builtinOrder": 1,
                    detail.put("builtinOrder", detailData.size() + 1);
//                        "bravoItemId": 7981,
                    detail.put("appItemId", product_id);
                    detail.put("bravoItemId",
                            this.dataManager.getProductManager().getProductBasicData(
                                    product_id
                            ).getBravo_id());
//                        "itemCode": "HYUNDAI_HD-4111",
                    detail.put("itemCode", ConvertUtils.toString(product.get("product_code")));
//                        "itemName": "Máy cưa xích HYUNDAI HD-4111 (Lam 16\", xích Oregon 28.5 mắc)",
                    detail.put("itemName", ConvertUtils.toString(product.get("product_full_name")));
                    //                        "quantity": 2,
                    detail.put("quantity", ConvertUtils.toInt(product.get("product_total_quantity")));
                    //                        "quantityGift": 1,
                    detail.put("quantityGift", 0);
                    //                        "totalItemQuantity": 3,
                    detail.put("totalItemQuantity", detail.get("quantity"));
                    //                        "itemExchangeType": "1"
                    detail.put("itemExchangeType", 0);

                    totalQuantityApp += ConvertUtils.toInt(product.get("product_total_quantity"));
                } else {
                    //                        "quantity": 2,
                    detail.put("quantity", ConvertUtils.toInt(detail.get("quantity"))
                            + ConvertUtils.toInt(product.get("product_total_quantity")));
                    //                        "totalItemQuantity": 3,
                    detail.put("totalItemQuantity", detail.get("quantity"));

                    totalQuantityApp += ConvertUtils.toInt(product.get("product_total_quantity"));
                }
                detailData.put(product_id, detail);
            }

            for (JSONObject product : gifts) {
                int product_id = ConvertUtils.toInt(product.get("product_id"));
                JSONObject detail = detailData.get(product_id);
                if (detail == null) {
                    detail = new JSONObject();
                    //                    "builtinOrder": 1,
                    detail.put("builtinOrder", detailData.size() + 1);
                    detail.put("appItemId", product_id);
//                        "bravoItemId": 7981,
                    detail.put("bravoItemId",
                            this.dataManager.getProductManager().getProductBasicData(
                                    product_id
                            ).getBravo_id());
//                        "itemCode": "HYUNDAI_HD-4111",
                    detail.put("itemCode", ConvertUtils.toString(product.get("product_code")));
//                        "itemName": "Máy cưa xích HYUNDAI HD-4111 (Lam 16\", xích Oregon 28.5 mắc)",
                    detail.put("itemName", ConvertUtils.toString(product.get("product_full_name")));
                    //                        "quantity": 2,
                    detail.put("quantity", 0);
                    //                        "quantityGift": 1,
                    detail.put("quantityGift", ConvertUtils.toInt(product.get("product_total_quantity")));
                    //                        "totalItemQuantity": 3,
                    detail.put("totalItemQuantity",
                            ConvertUtils.toInt(detail.get("quantityGift"))
                                    + ConvertUtils.toInt(detail.get("quantity")));
                    //                        "itemExchangeType": "1"
                    detail.put("itemExchangeType", 0);

                    totalQuantityApp += ConvertUtils.toInt(product.get("product_total_quantity"));
                } else {
                    //                        "quantity": 2,
                    detail.put("quantityGift", ConvertUtils.toInt(detail.get("quantityGift"))
                            + ConvertUtils.toInt(product.get("product_total_quantity")));
                    //                        "totalItemQuantity": 3,
                    detail.put("totalItemQuantity",
                            ConvertUtils.toInt(detail.get("quantityGift"))
                                    + ConvertUtils.toInt(detail.get("quantity")));
                    totalQuantityApp += ConvertUtils.toInt(product.get("product_total_quantity"));
                }
                detailData.put(product_id, detail);
            }

            order.put("detailData", new ArrayList<>(detailData.values()));
//                    "totalQuantityApp": 5,
            order.put("totalQuantityApp", totalQuantityApp);
//                    "totalAmount9App": 10000,
            order.put("totalAmount9App", agencyOrderDept.get("total_begin_price"));
//                    "discountAmountApp": 2000,
            order.put("discountAmountApp",
                    ConvertUtils.toLong(agencyOrderDept.get("total_begin_price")) -
                            ConvertUtils.toLong(agencyOrderDept.get("total_end_price")));
//                    "totalAmount0App": 8000,
            order.put("totalAmount0App", agencyOrderDept.get("total_end_price"));
//            }

            /**
             * DHB: 01
             * PHG: C2
             */
            order.put("soType",
                    OrderType.INSTANTLY.getValue() == ConvertUtils.toInt(agencyOrder.get("type")) ?
                            "01" :
                            "C2");

            return bravoService.syncAgencyOrder(
                    order
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse completeAgencyOrderDept(int id) {
        try {
            JSONObject agencyOrderDept = this.orderDB.getAgencyOrderDeptById(id);
            if (agencyOrderDept == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrder(ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")));
            if (jsAgencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            AgencyOrderEntity agencyOrderEntity = AgencyOrderEntity.from(jsAgencyOrder);
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            if (agencyOrderEntity.getStatus() != OrderStatus.SHIPPING.getKey()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            if (agencyOrderEntity.getType() == OrderType.INSTANTLY.getValue()) {
                if (AgencyOrderDeptType.NORMAL.getId() ==
                        ConvertUtils.toInt(agencyOrderDept.get("type"))) {
                    this.callGhiNhanTichLuyDonHang(
                            agencyOrderEntity.getId(),
                            agencyOrderEntity.getAgency_id(),
                            Arrays.asList(agencyOrderDept),
                            agencyOrderEntity.getCreated_date().getTime(),
                            DateTimeUtils.getMilisecondsNow()
                    );

                    this.callGhiNhanCSDMDonHang(
                            agencyOrderEntity.getId(),
                            agencyOrderEntity.getAgency_id(),
                            Arrays.asList(agencyOrderDept),
                            agencyOrderEntity.getCode(),
                            agencyOrderEntity.getType()
                    );
                }

                /**
                 * Tích lũy xếp hạng
                 */
                this.callGhiNhanCTXHDonHang(
                        agencyOrderEntity.getId(),
                        agencyOrderEntity.getAgency_id(),
                        Arrays.asList(agencyOrderDept),
                        agencyOrderEntity.getCode(),
                        agencyOrderEntity.getType()
                );

                this.completeAgencyOrderDeptOfOrderInstantly(
                        id,
                        agencyOrderEntity,
                        agencyOrderDept
                );

                List<JSONObject> agencyOrderDeptList = this.orderDB.getListAgencyOrderDept(
                        agencyOrderEntity.getId()
                );
                if (this.checkAllCompleteAgencyOrderDept(agencyOrderDeptList)) {
                    boolean rsUpdateAgencyOrderStatus = this.orderDB.deliveryAgencyOrder(
                            agencyOrderEntity.getId(),
                            OrderStatus.COMPLETE.getKey(),
                            "",
                            0);
                    if (!rsUpdateAgencyOrderStatus) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * save update order status history
                     */
                    this.orderDB.saveUpdateOrderStatusHistory(
                            agencyOrderEntity.getId(),
                            OrderStatus.COMPLETE.getKey(),
                            "Đồng bộ từ Bravo",
                            0);

                    /**
                     * Push thông báo
                     */
                    this.pushNotifyToAgency(
                            0,
                            NotifyAutoContentType.DELIVERY_ORDER,
                            "",
                            NotifyAutoContentType.DELIVERY_ORDER.getType(),
                            JsonUtils.Serialize(Arrays.asList(ConvertUtils.toString(agencyOrderEntity.getId()))),
                            "Đơn đặt hàng " + agencyOrderEntity.getCode() + " của Quý khách đã được Anh Tin giao tới địa chỉ nhận hàng.",
                            agencyOrderEntity.getAgency_id()
                    );

                    /**
                     * Lưu ưu đãi đam mê
                     */
                    this.orderDB.saveAgencyCSDMClaim(agencyOrderEntity.getId(),
                            agencyOrderEntity.getCode(),
                            agencyOrderEntity.getAgency_id());
                }

                /**
                 * Cập nhật thời gian đơn hàng được ghi nhận công nợ
                 */
                this.agencyDB.saveNgayGhiNhanCongNo(
                        agencyOrderEntity.getAgency_id());

                /**
                 * Tích lũy nhiệm vụ
                 */
                if (ConvertUtils.toInt(jsAgencyOrder.get("accumulate_mission_status")) == 0) {
                    this.callGhiNhanTichLuyNhiemVuChoDonHang(
                            agencyOrderEntity.getId(),
                            agencyOrderEntity.getAgency_id(),
                            Arrays.asList(agencyOrderDept)
                    );

                    boolean rs = this.orderDB.ghiNhanTrangThaiTichLuyNhiemVuChoDonHang(agencyOrderEntity.getId());
                    if (rs == false) {
                        this.alertToTelegram("ghiNhanTrangThaiTichLuyNhiemVuChoDonHang: " + agencyOrderEntity.getId(), ResponseStatus.EXCEPTION);
                    }
                }

                return ClientResponse.success(null);
            } else if (agencyOrderEntity.getType() == OrderType.APPOINTMENT.getValue()) {
                boolean rsCompleteOrderDept = this.orderDB.completeAgencyOrderDept(id);
                if (!rsCompleteOrderDept) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                List<JSONObject> agencyOrderDeptList = this.orderDB.getListAgencyOrderDept(
                        agencyOrderEntity.getId()
                );
                if (this.checkAllCompleteAgencyOrderDept(agencyOrderDeptList)) {
                    boolean rsUpdateAgencyOrderStatus = this.orderDB.deliveryAgencyOrder(
                            agencyOrderEntity.getId(),
                            OrderStatus.COMPLETE.getKey(),
                            "Đồng bộ từ Bravo",
                            0);
                    if (!rsUpdateAgencyOrderStatus) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * Nhả kho còn lại nếu có
                     */
                    this.refundWarehouse(
                            agencyOrderEntity.getId(),
                            agencyOrderEntity.getCode()
                    );

                    /**
                     * save update order status history
                     */
                    this.orderDB.saveUpdateOrderStatusHistory(
                            agencyOrderEntity.getId(),
                            OrderStatus.COMPLETE.getKey(),
                            "Đồng bộ từ Bravo",
                            0);

                    /**
                     * Push thông báo
                     */
                    this.pushNotifyToAgency(
                            0,
                            NotifyAutoContentType.DELIVERY_ORDER,
                            "",
                            NotifyAutoContentType.DELIVERY_ORDER.getType(),
                            JsonUtils.Serialize(Arrays.asList(ConvertUtils.toString(agencyOrderEntity.getId()))),
                            "Đơn đặt hàng " + agencyOrderEntity.getCode() + " của Quý khách đã được Anh Tin giao tới địa chỉ nhận hàng.",
                            agencyOrderEntity.getAgency_id()
                    );

                    /**
                     * Lưu ưu đãi đam mê đã nhận
                     */
                    this.orderDB.saveAgencyCSDMClaim(agencyOrderEntity.getId(),
                            agencyOrderEntity.getCode(),
                            agencyOrderEntity.getAgency_id());
                }

                return ClientResponse.success(null);
            } else if (agencyOrderEntity.getType() == OrderType.CONTRACT.getValue()) {
                boolean rsCompleteOrderDept = this.orderDB.completeAgencyOrderDept(id);
                if (!rsCompleteOrderDept) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                boolean rsUpdateAgencyOrderStatus = this.orderDB.deliveryAgencyOrder(
                        agencyOrderEntity.getId(),
                        OrderStatus.COMPLETE.getKey(),
                        "Đồng bộ từ Bravo",
                        0);
                if (!rsUpdateAgencyOrderStatus) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkAllCompleteAgencyOrderDept(List<JSONObject> agencyOrderDeptList) {
        for (JSONObject jsonObject : agencyOrderDeptList) {
            if (ConvertUtils.toInt(jsonObject.get("status")) != OrderStatus.COMPLETE.getKey()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkAllGNCNAgencyOrderDept(Integer id) {
        List<JSONObject> agencyOrderDeptList = this.orderDB.getListAgencyOrderDeptNotGNCN(id);
        if (agencyOrderDeptList.isEmpty()) {
            return true;
        }
        return false;
    }

    public ClientResponse closeOrder(int id) {
        try {
            JSONObject agencyOrderDept = this.orderDB.getAgencyOrderDeptById(id);
            if (agencyOrderDept == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            AgencyOrderEntity agencyOrderEntity = this.orderDB.getAgencyOrderEntity(
                    ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")));
            if (agencyOrderEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            if (agencyOrderEntity.getType() == OrderType.INSTANTLY.getValue()) {
                boolean rsCompleteOrderDept = this.orderDB.completeAgencyOrderDept(id);
                if (!rsCompleteOrderDept) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                List<JSONObject> agencyOrderDeptList = this.orderDB.getListAgencyOrderDept(
                        id
                );
                if (this.checkAllCompleteAgencyOrderDept(agencyOrderDeptList)) {
                    return this.completeAgencyOrder(
                            new SessionData(), agencyOrderEntity
                    );
                }
                return ClientResponse.success(null);
            } else if (agencyOrderEntity.getType() == OrderType.APPOINTMENT.getValue()) {
                boolean rsCompleteOrderDept = this.orderDB.completeAgencyOrderDept(id);
                if (!rsCompleteOrderDept) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                List<JSONObject> agencyOrderDeptList = this.orderDB.getListAgencyOrderDept(
                        id
                );
                if (this.checkAllCompleteAgencyOrderDept(agencyOrderDeptList)) {
                    boolean rsUpdateAgencyOrderStatus = this.orderDB.deliveryAgencyOrder(
                            agencyOrderEntity.getId(),
                            OrderStatus.COMPLETE.getKey(),
                            "Đồng bộ từ Bravo",
                            0);
                    if (!rsUpdateAgencyOrderStatus) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * Nhả kho còn lại nếu có
                     */
                    this.refundWarehouse(
                            agencyOrderEntity.getId(),
                            agencyOrderEntity.getCode()
                    );
                }
                return ClientResponse.success(null);
            } else if (agencyOrderEntity.getType() == OrderType.CONTRACT.getValue()) {
                boolean rsCompleteOrderDept = this.orderDB.completeAgencyOrderDept(id);
                if (!rsCompleteOrderDept) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                boolean rsUpdateAgencyOrderStatus = this.orderDB.deliveryAgencyOrder(
                        agencyOrderEntity.getId(),
                        OrderStatus.COMPLETE.getKey(),
                        "Đồng bộ từ Bravo",
                        0);
                if (!rsUpdateAgencyOrderStatus) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createDeptOrderByAgencyOrderDept(int id) {
        try {
            JSONObject agencyOrderDept = this.orderDB.getAgencyOrderDeptById(
                    id
            );
            if (agencyOrderDept == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            JSONObject agencyOrder = this.orderDB.getAgencyOrder(
                    ConvertUtils.toInt(agencyOrderDept.get("agency_order_id"))
            );

            if (ConvertUtils.toInt(agencyOrder.get("type")) == OrderType.CONTRACT.getValue()
                    && ConvertUtils.toString(agencyOrderDept.get("dept_code")).isEmpty()) {
                String dept_code = this.appUtils.getAgencyOrderDeptCode(
                        ConvertUtils.toString(agencyOrder.get("code")),
                        ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                        ConvertUtils.toInt(agencyOrder.get("total")),
                        ConvertUtils.toInt(agencyOrderDept.get("promo_id")) != 0,
                        this.hasOrderNormal(ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")))
                );
                this.orderDB.saveDeptCode(
                        id,
                        dept_code
                );

                agencyOrderDept.put("dept_code",
                        dept_code
                );
            }

            boolean rsIncreaseDeptForAgencyOrderDept = this.orderDB.increaseDeptForAgencyOrderDept(id);
            if (!rsIncreaseDeptForAgencyOrderDept) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Đơn hẹn giao -> Tích lũy đơn thường, không tích lũy săn giá rẻ
             */
            if (OrderType.APPOINTMENT.getValue() == ConvertUtils.toInt(agencyOrder.get("type")) &&
                    AgencyOrderDeptType.NORMAL.getId() == ConvertUtils.toInt(agencyOrderDept.get("type"))) {
                this.callGhiNhanTichLuyDonHang(
                        ConvertUtils.toInt(agencyOrder.get("id")),
                        ConvertUtils.toInt(agencyOrder.get("agency_id")),
                        Arrays.asList(agencyOrderDept),
                        AppUtils.convertJsonToDate(agencyOrder.get("created_date")).getTime(),
                        DateTimeUtils.getMilisecondsNow()
                );
            }

            if (ConvertUtils.toInt(agencyOrder.get("total")) == 1) {
                DeptTransactionSubTypeEntity deptTransactionSubTypeEntity = null;
                if (ConvertUtils.toInt(agencyOrder.get("type")) == OrderType.APPOINTMENT.getValue()) {
                    deptTransactionSubTypeEntity =
                            this.dataManager.getConfigManager().getHanMucDonHangHenGiao();
                } else if (ConvertUtils.toInt(agencyOrder.get("type")) == OrderType.CONTRACT.getValue()) {
                    deptTransactionSubTypeEntity =
                            this.dataManager.getConfigManager().getHanMucDonHangHopDong();
                }
                if (deptTransactionSubTypeEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                String dept_code = this.appUtils.getAgencyOrderDeptCode(
                        ConvertUtils.toString(agencyOrder.get("code")),
                        ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                        ConvertUtils.toInt(agencyOrder.get("total")),
                        ConvertUtils.toInt(agencyOrderDept.get("promo_id")) != 0,
                        this.hasOrderNormal(ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")))
                );

                ClientResponse crGNCN = this.ghiNhanCongNoDonHang(
                        ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")),
                        ConvertUtils.toInt(agencyOrder.get("agency_id")),
                        ConvertUtils.toLong(agencyOrderDept.get("total_end_price")),
                        ConvertUtils.toString(agencyOrder.get("code")),
                        deptTransactionSubTypeEntity.getId(),
                        0,
                        DateTimeUtils.getNow(),
                        ConvertUtils.toInt(agencyOrder.get("dept_cycle")),
                        ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                        dept_code
                );
                if (crGNCN.failed()) {
                    return crGNCN;
                }
            } else {
                DeptTransactionSubTypeEntity deptTransactionSubTypeEntity = null;
                if (ConvertUtils.toInt(agencyOrder.get("type")) == OrderType.APPOINTMENT.getValue()) {
                    deptTransactionSubTypeEntity =
                            this.dataManager.getConfigManager().getHanMucDonHangHenGiao();
                } else if (ConvertUtils.toInt(agencyOrder.get("type")) == OrderType.CONTRACT.getValue()) {
                    deptTransactionSubTypeEntity =
                            this.dataManager.getConfigManager().getHanMucDonHangHopDong();
                }
                if (deptTransactionSubTypeEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                if (deptTransactionSubTypeEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                String dept_code = this.appUtils.getAgencyOrderDeptCode(
                        ConvertUtils.toString(agencyOrder.get("code")),
                        ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                        ConvertUtils.toInt(agencyOrder.get("total")),
                        ConvertUtils.toInt(agencyOrderDept.get("promo_id")) != 0,
                        this.hasOrderNormal(ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")))
                );
                ClientResponse crGNCN = this.ghiNhanCongNoDonHang(
                        ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")),
                        ConvertUtils.toInt(agencyOrder.get("agency_id")),
                        ConvertUtils.toLong(agencyOrderDept.get("total_end_price")),
                        ConvertUtils.toString(agencyOrder.get("code")),
                        deptTransactionSubTypeEntity.getId(),
                        0,
                        DateTimeUtils.getNow(),
                        ConvertUtils.toInt(agencyOrderDept.get("dept_cycle")),
                        ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                        dept_code
                );
                if (crGNCN.failed()) {
                    return crGNCN;
                }

                this.increaseTotalMoneyDeptOfAgencyOrder(
                        ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")),
                        ConvertUtils.toLong(agencyOrderDept.get("total_end_price"))
                );

                /**
                 * tất cả đơn hẹn giao đã cập nhật công nợ
                 */
                if (checkAllGNCNAgencyOrderDept(ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")))) {
                    this.orderDB.setOrderIncreaseDept(
                            ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")),
                            IncreaseDeptStatus.YES.getValue());
                }
            }

            /**
             * Cập nhật ngày đơn hàng được ghi nhận công nợ
             */
            this.agencyDB.saveNgayGhiNhanCongNo(
                    ConvertUtils.toInt(agencyOrder.get("agency_id"))
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean hasOrderNormal(int agency_order_id) {
        return this.orderDB.getOrderNormalDept(
                agency_order_id) != null;
    }

    private ClientResponse exportWarehouseForOrderAppoint(
            int agency_id,
            String order_code,
            int agency_order_id) {
        try {
            /**
             * Phát sinh phiếu xuất kho
             * loại xuất bán, trạng thái hoàn thành
             */
            List<JSONObject> products = this.orderDB.getListProductInOrderDeliveryByAgencyOrderId(
                    agency_order_id);
            List<JSONObject> goods = this.orderDB.getListGoodsInOrderDeliveryByAgencyOrderId(
                    agency_order_id);
            Map<Integer, Integer> productDataList = this.getMpItemInOrder(products, goods);
            List<JSONObject> productExportList = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : productDataList.entrySet()) {
                int product_id = entry.getKey();
                int product_total_quantity = entry.getValue();
                JSONObject productExport = new JSONObject();
                productExport.put("product_id", product_id);
                productExport.put("product_total_quantity", product_total_quantity);
                productExportList.add(productExport);

                /**
                 * Trừ tồn kho chờ giao
                 */
                boolean rsDecreaseQuantityWaitingShipToday = this.decreaseQuantityWaitingShipToday(
                        product_id, product_total_quantity,
                        order_code);
                if (!rsDecreaseQuantityWaitingShipToday) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * Công tồn kho xuất
                 */
                boolean rsIncreaseQuantityExportToday = this.increaseQuantityExportToday(
                        product_id,
                        product_total_quantity,
                        order_code
                );
                if (!rsIncreaseQuantityExportToday) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ClientResponse rsInsertWarehouseBillExport = this.insertWarehouseBill(
                        agency_id,
                        WarehouseBillType.EXPORT,
                        order_code,
                        productExportList,
                        0);
                if (rsInsertWarehouseBillExport.failed()) {
                    return rsInsertWarehouseBillExport;
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void refundWarehouse(Integer id, String code) {
        try {
            List<JSONObject> productInOrders = this.orderDB.getListProductInOrder(id);
            List<JSONObject> giftInOrders = this.orderDB.getListGoodsInOrder(id);
            List<JSONObject> productDeliveries = this.orderDB.getListProductInOrderDeliveryByAgencyOrderId(id);
            List<JSONObject> giftDeliveries = this.orderDB.getListGoodsInOrderDeliveryByAgencyOrderId(id);
            Map<Integer, Integer> mpItemInOrder =
                    this.getMpItemInOrder(productInOrders, giftInOrders);

            Map<Integer, Integer> mpItemDelivery =
                    this.getMpItemInOrder(productDeliveries, giftDeliveries);

            for (Map.Entry<Integer, Integer> entry : mpItemInOrder.entrySet()) {
                Integer quantityDelivery = mpItemDelivery.get(entry.getKey());
                if (quantityDelivery == null) {
                    quantityDelivery = 0;
                }
                if (entry.getValue() > quantityDelivery) {
                    boolean rsDecreaseQuantityWaitingShip = this.decreaseQuantityWaitingShipToday(
                            entry.getKey(),
                            entry.getValue() - quantityDelivery,
                            "Đóng đơn: " + code);
                    if (!rsDecreaseQuantityWaitingShip) {

                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    private Map<Integer, Integer> getMpItemInOrder(
            List<JSONObject> products,
            List<JSONObject> gifts
    ) {
        Map<Integer, Integer> mpItemInOrder = new HashMap<>();

        for (JSONObject product : products) {
            Integer itemQuantity = mpItemInOrder.get(
                    ConvertUtils.toInt(product.get("product_id")));
            if (itemQuantity == null) {
                mpItemInOrder.put(ConvertUtils.toInt(product.get("product_id")),
                        ConvertUtils.toInt(product.get("product_total_quantity")));
            } else {
                itemQuantity += ConvertUtils.toInt(product.get("product_total_quantity"));
                mpItemInOrder.put(ConvertUtils.toInt(product.get("product_id")),
                        itemQuantity);
            }
        }

        for (JSONObject gift : gifts) {
            Integer itemQuantity = mpItemInOrder.get(
                    ConvertUtils.toInt(gift.get("product_id")));
            if (itemQuantity == null) {
                mpItemInOrder.put(ConvertUtils.toInt(gift.get("product_id")),
                        ConvertUtils.toInt(gift.get("product_total_quantity")));
            } else {
                itemQuantity += ConvertUtils.toInt(gift.get("product_total_quantity"));
                mpItemInOrder.put(ConvertUtils.toInt(gift.get("product_id")),
                        itemQuantity);
            }
        }
        return mpItemInOrder;
    }

    public ClientResponse checkCompleteAgencyOrderDept(int id) {
        try {
            JSONObject agencyOrderDept = this.orderDB.getAgencyOrderDeptById(
                    id
            );
            if (agencyOrderDept == null) {
                return ClientResponse.fail(
                        ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND
                );
            }

            int total_quantity_buy = this.getTotalQuantityBuyByAgencyOrderDept(
                    ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")),
                    ConvertUtils.toInt(agencyOrderDept.get("promo_id"))
            );
            int total_quantity_delivery = this.getTotalQuantityDeliveryByAgencyOrderDept(
                    id
            );

            if (total_quantity_delivery >= total_quantity_buy) {
                this.completeAgencyOrderDept(id);
            }

            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrder(
                    ConvertUtils.toInt(agencyOrderDept.get("agency_order_id"))
            );
            if (jsAgencyOrder != null &&
                    ConvertUtils.toInt(jsAgencyOrder.get("status")) == OrderStatus.SHIPPING.getKey()) {
                this.runCheckOneCompleteAgencyOrder(
                        ConvertUtils.toInt(agencyOrderDept.get("agency_order_id"))
                );
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private int getTotalQuantityDeliveryByAgencyOrderDept(int id) {
        int total_product = this.orderDB.getProductQuantityDeliveryByAgencyOrderDept(
                id
        );
        int total_gift = this.orderDB.getGiftQuantityDeliveryByAgencyOrderDept(
                id
        );
        return total_product + total_gift;
    }

    private int getTotalQuantityBuyByAgencyOrderDept(
            int agency_order_id,
            int promo_id
    ) {
        int total_product = this.orderDB.getTotalProductQuantityBuyByAgencyOrderDept(
                agency_order_id,
                promo_id
        );
        int total_gift = this.orderDB.getTotalGiftQuantityBuyByAgencyOrderDept(
                agency_order_id,
                promo_id
        );
        return total_product + total_gift;
    }

    private void createOrderDeliveryOfAgencyOrderDept(
            AgencyOrderEntity agencyOrderEntity,
            int agency_order_dept_id,
            int order_data_index,
            int total,
            int promo_id,
            long total_end_price) {
        try {
            int rsInsert = this.orderDB.insertAgencyOrderDelivery(
                    agencyOrderEntity,
                    agencyOrderEntity.getId(),
                    agencyOrderEntity.getCode(),
                    this.appUtils.getAgencyOrderDeptCode(
                            agencyOrderEntity.getCode(),
                            order_data_index,
                            total,
                            promo_id != 0,
                            this.hasOrderNormal(agencyOrderEntity.getId())
                    ),
                    total_end_price
            );
            if (rsInsert > 0) {
                List<JSONObject> productList = this.orderDB.getListProductInOrder(
                        agencyOrderEntity.getId()
                );
                for (JSONObject product : productList) {
                    this.orderDB.insertAgencyOrderDeliveryProduct(
                            AgencyOrderDetailEntity.from(product),
                            rsInsert,
                            agencyOrderEntity.getCode(),
                            agency_order_dept_id
                    );
                }

                List<JSONObject> giftList = this.orderDB.getListGoodsInOrder(
                        agencyOrderEntity.getId()
                );
                for (JSONObject gift : giftList) {
                    this.orderDB.insertAgencyOrderDeliveryGift(
                            AgencyOrderPromoDetailEntity.from(gift),
                            rsInsert,
                            agencyOrderEntity.getCode(),
                            agency_order_dept_id
                    );
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    private ClientResponse completeAgencyOrderDeptOfOrderInstantly(
            int id,
            AgencyOrderEntity agencyOrderEntity,
            JSONObject agencyOrderDept) {
        try {
            boolean rsCompleteOrderDept = this.orderDB.completeAgencyOrderDept(id);
            if (!rsCompleteOrderDept) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.increaseTotalMoneyDeptOfAgencyOrder(
                    agencyOrderEntity.getId(),
                    ConvertUtils.toLong(agencyOrderDept.get("total_end_price"))
            );

            /**
             * 1. Ghi nhận công nợ
             * 2. Tạo đơn hàng giao
             * 3. Trừ kho chờ giao
             * 4. Công kho xuất
             */

            /**
             * Ghi nhận công nợ cho đơn hàng
             */
            DeptTransactionSubTypeEntity deptTransactionSubTypeEntity =
                    this.dataManager.getConfigManager().getDeptTransactionByOrder();
            if (deptTransactionSubTypeEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            String dept_code = this.appUtils.getAgencyOrderDeptCode(
                    agencyOrderEntity.getCode(),
                    ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                    agencyOrderEntity.getTotal(),
                    ConvertUtils.toInt(agencyOrderDept.get("promo_id")) != 0,
                    this.hasOrderNormal(ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")))
            );
            ClientResponse crGhiNhanCongNo = this.ghiNhanCongNoDonHang(
                    agencyOrderEntity.getId(),
                    agencyOrderEntity.getAgency_id(),
                    ConvertUtils.toLong(agencyOrderDept.get("total_end_price")),
                    agencyOrderEntity.getCode(),
                    deptTransactionSubTypeEntity.getId(),
                    0,
                    DateTimeUtils.getNow(),
                    ConvertUtils.toInt(agencyOrderDept.get("dept_cycle")),
                    ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                    dept_code
            );
            if (crGhiNhanCongNo.failed()) {
                return crGhiNhanCongNo;
            }

            /**
             * Tạo đơn hàng giao
             */
            this.createOrderDeliveryOfAgencyOrderDept(
                    agencyOrderEntity,
                    id,
                    ConvertUtils.toInt(agencyOrderDept.get("order_data_index")),
                    agencyOrderEntity.getTotal(),
                    ConvertUtils.toInt(agencyOrderDept.get("promo_id")),
                    ConvertUtils.toLong(agencyOrderDept.get("total_end_price"))
            );

            this.exportWarehouseForAgencyOrderDept(
                    agencyOrderEntity,
                    agencyOrderDept,
                    agencyOrderEntity.getAgency_id(),
                    dept_code
            );
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse exportWarehouseForAgencyOrderDept(
            AgencyOrderEntity agencyOrderEntity,
            JSONObject agencyOrderDept,
            int agency_id,
            String code) {
        try {
            /**
             * Phát sinh phiếu xuất kho
             * loại xuất bán, trạng thái hoàn thành
             */
            List<JSONObject> products = this.orderDB.getListProductByAgencyOrderDept(
                    ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")),
                    ConvertUtils.toInt(agencyOrderDept.get("promo_id"))
            );
            List<JSONObject> goods = this.orderDB.getListGiftByAgencyOrderDept(
                    ConvertUtils.toInt(agencyOrderDept.get("agency_order_id")),
                    ConvertUtils.toInt(agencyOrderDept.get("promo_id"))
            );
            Map<Integer, Integer> productDataList = this.getMpItemInOrder(products, goods);
            List<JSONObject> productExportList = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : productDataList.entrySet()) {
                int product_id = entry.getKey();
                int product_total_quantity = entry.getValue();
                JSONObject productExport = new JSONObject();
                productExport.put("product_id", product_id);
                productExport.put("product_total_quantity", product_total_quantity);
                productExportList.add(productExport);

                /**
                 * Trừ tồn kho chờ giao
                 */
                boolean rsDecreaseQuantityWaitingShipToday = this.decreaseQuantityWaitingShipToday(
                        product_id, product_total_quantity,
                        code);
                if (!rsDecreaseQuantityWaitingShipToday) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                /**
                 * Công tồn kho xuất
                 */
                boolean rsIncreaseQuantityExportToday = this.increaseQuantityExportToday(
                        product_id,
                        product_total_quantity,
                        code
                );
                if (!rsIncreaseQuantityExportToday) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            ClientResponse rsInsertWarehouseBillExport = this.insertWarehouseBill(
                    agency_id,
                    WarehouseBillType.EXPORT,
                    code,
                    productExportList,
                    0);
            if (rsInsertWarehouseBillExport.failed()) {
                return rsInsertWarehouseBillExport;
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public void increaseTotalMoneyDeptOfAgencyOrder(
            int agency_order_id,
            long total_end_price) {
        try {
            this.orderDB.increaseTotalMoneyDeptOfAgencyOrder(
                    agency_order_id,
                    total_end_price
            );
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }


    private boolean checkOrderHuntSale(Integer id) {
        List<JSONObject> rs = this.orderDB.getListAgencyOrderDept(
                id
        );

        for (JSONObject js : rs) {
            if (ConvertUtils.toInt(js.get("promo_id")) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Danh sách hàng bán trả lại
     *
     * @param request
     * @return
     */
    public ClientResponse filterHBTL(SessionData sessionData, FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();

            this.addFilterOrderData(sessionData, request);

            String query = this.filterUtils.getQuery(FunctionList.FILTER_HBTL, request.getFilters(), request.getSorts());

            List<JSONObject> records = this.orderDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.orderDB.getTotal(query);
            for (JSONObject js : records) {
                js.put("agency_info", JsonUtils.Serialize(this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(js.get("agency_id"))
                )));
                js.put("code", ConvertUtils.toString(js.get("code")) + "-" + ConvertUtils.toString(js.get("doc_no")));
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
     * CHi tiết hàng bán trả lại
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse getHBTLInfo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject data = new JSONObject();
            JSONObject agencyOrder = this.orderDB.getAgencyHBTL(request.getId());
            if (agencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            agencyOrder.put("agency_info",
                    this.dataManager.getAgencyManager().getAgencyBasicData(ConvertUtils.toInt(agencyOrder.get("agency_id"))));

            List<JSONObject> products = new ArrayList<>();
            List<JSONObject> orderProductList = this.orderDB.getListHBTLDetail(request.getId());
            for (JSONObject orderProduct : orderProductList) {
                ProductMoneyResponse productMoneyResponse = new ProductMoneyResponse();
                productMoneyResponse.setId(ConvertUtils.toInt(orderProduct.get("product_id")));
                productMoneyResponse.setCode(ConvertUtils.toString(orderProduct.get("product_code")));
                productMoneyResponse.setFull_name(ConvertUtils.toString(orderProduct.get("product_full_name")));
                productMoneyResponse.setProduct_small_unit_id(ConvertUtils.toInt(orderProduct.get("product_small_unit_id")));
                if (productMoneyResponse.getProduct_small_unit_id() == 0) {
                    productMoneyResponse.setProduct_small_unit_name(
                            ConvertUtils.toString(orderProduct.get("product_small_unit_name"))
                    );
                } else {
                    productMoneyResponse.setProduct_small_unit_name(this.dataManager.getProductManager().getSmallUnitName(
                            productMoneyResponse.getProduct_small_unit_id()
                    ));
                }
                productMoneyResponse.setStep(ConvertUtils.toInt(orderProduct.get("product_step")));
                productMoneyResponse.setMinimum_purchase(ConvertUtils.toInt(orderProduct.get("product_minimum_purchase")));
                productMoneyResponse.setImages(ConvertUtils.toString(orderProduct.get("product_images")));
                productMoneyResponse.setPrice(ConvertUtils.toLong(orderProduct.get("product_price")));
                productMoneyResponse.setQuantity(ConvertUtils.toInt(orderProduct.get("product_total_quantity")));
                productMoneyResponse.setTotal_begin_price(ConvertUtils.toLong(orderProduct.get("product_total_begin_price")));
                productMoneyResponse.setTotal_end_price(ConvertUtils.toLong(orderProduct.get("product_total_end_price")));
                productMoneyResponse.setItem_type(ConvertUtils.toInt(orderProduct.get("product_item_type")));
                products.add(JsonUtils.DeSerialize(JsonUtils.Serialize(productMoneyResponse), JSONObject.class));
            }

            agencyOrder.put("code", ConvertUtils.toString(agencyOrder.get("code")) + "-" + ConvertUtils.toString(agencyOrder.get("doc_no")));
            data.put("order", agencyOrder);
            data.put("products", products);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse accumulateHBTL(int id) {
        try {
            JSONObject hbtl = this.orderDB.getAgencyHBTL(id);
            if (hbtl == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }
            long now = DateTimeUtils.getMilisecondsNow();

            /**
             * Chương trình tích lũy
             */
            this.accumulateService.autoAddTransactionOrder(
                    CTTLTransactionType.HBTL.getKey(),
                    ConvertUtils.toInt(hbtl.get("agency_id")),
                    ConvertUtils.toString(hbtl.get("code")),
                    0,
                    ConvertUtils.toLong(hbtl.get("total_end_price")) * -1,
                    id,
                    CTTLTransactionSource.AUTO.getId(),
                    0,
                    now,
                    now
            );

            /**
             * Chính sách đam mê
             */
            this.accumulateCSDMService.addTransaction(
                    CTTLTransactionType.HBTL.getKey(),
                    ConvertUtils.toInt(hbtl.get("agency_id")),
                    ConvertUtils.toString(hbtl.get("code")),
                    id,
                    id,
                    CTTLTransactionSource.AUTO.getId(),
                    0,
                    now,
                    now,
                    "[]"
            );

            /**
             * CTXH
             */
            this.accumulateCTXHService.addTransaction(
                    CTTLTransactionType.HBTL.getKey(),
                    ConvertUtils.toInt(hbtl.get("agency_id")),
                    ConvertUtils.toString(hbtl.get("code")),
                    id,
                    id,
                    CTTLTransactionSource.AUTO.getId(),
                    0,
                    now,
                    now,
                    ConvertUtils.toLong(hbtl.get("total_end_price")) * -1
            );

            /**
             * Tích lũy nhiệm vụ
             */
            this.accumulateMissionService.tichLuyTuDongHBTL(
                    ConvertUtils.toInt(hbtl.get("agency_id")),
                    id
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public void callGhiNhanCSDMDonHang(
            int agency_order_id,
            int agency_id,
            List<JSONObject> agencyOrderDeptList,
            String agency_order_code,
            int type
    ) {
        try {
            for (JSONObject agencyOrderDept : agencyOrderDeptList) {
                if (this.appUtils.isOrderNormal(
                        ConvertUtils.toInt(
                                agencyOrderDept.get("promo_id"))
                )) {
                    this.accumulateCSDMService.addTransaction(
                            CTTLTransactionType.DON_HANG.getKey(),
                            agency_id,
                            agency_order_code,
                            agency_order_id,
                            agency_order_id,
                            CTTLTransactionSource.AUTO.getId(),
                            0,
                            0,
                            0,
                            JsonUtils.Serialize(
                                    this.getProductListProductTichLuyCSDM(agency_order_id, type)
                            )
                    );
                    break;
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    private List<JSONObject> getProductListProductTichLuyCSDM(int agencyOrderId, int type) {
        if (type == OrderType.INSTANTLY.getValue()) {
            return this.orderDB.getListProductInOrderNormal(
                    agencyOrderId
            );
        } else {
            return this.orderDB.getListProductInOrderDeliveryByAgencyOrderId(
                    agencyOrderId
            );
        }
    }

    /**
     * Tích lũy đơn hàng giao
     *
     * @param id
     * @return
     */
    public ClientResponse accumulateOrderDelivery(int id) {
        try {
            JSONObject jsAgencyOrderDelivery = this.orderDB.getAgencyOrderDelivery(
                    id
            );
            if (jsAgencyOrderDelivery == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<JSONObject> productList = this.orderDB.getListProductInOrderDelivery(
                    id
            );

            /**
             * Tích lũy cho Đam mê
             */
            this.ghiNhanTichLuyCSDMOfOrderDelivery(jsAgencyOrderDelivery, productList);

            /**
             * Tích lũy cho Xếp hạng
             */
            this.ghiNhanTichLuyCTXHOfOrderDelivery(jsAgencyOrderDelivery, productList);

            JSONObject data = new JSONObject();
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Tích lũy cho Đam mê
     *
     * @param jsAgencyOrderDelivery
     * @param productList
     */
    private void ghiNhanTichLuyCSDMOfOrderDelivery(JSONObject jsAgencyOrderDelivery, List<JSONObject> productList) {
        try {
            Map<String, String> records = new HashMap<>();

            int type = ConvertUtils.toInt(jsAgencyOrderDelivery.get("type"));
            if (type == OrderType.APPOINTMENT.getValue()) {
                Map<Integer, Map<Integer, Integer>> mpOrder = new ConcurrentHashMap<>();
                Map<Integer, String> mpAgencyOrderCode = new ConcurrentHashMap<>();
                for (JSONObject product : productList) {
                    int agency_order_id = ConvertUtils.toInt(product.get("agency_order_id"));
                    int agency_order_dept_id = ConvertUtils.toInt(product.get("agency_order_dept_id"));

                    JSONObject jsAgencyOrderDept = this.orderDB.getAgencyOrderDeptById(agency_order_dept_id);
                    if (jsAgencyOrderDept == null ||
                            ConvertUtils.toInt(jsAgencyOrderDept.get("type")) == AgencyOrderDeptType.HUNT_SALE.getId()) {
                        continue;
                    }

                    int product_id = ConvertUtils.toInt(product.get("product_id"));
                    int product_total_quantity = ConvertUtils.toInt(product.get("product_total_quantity"));

                    Map<Integer, Integer> mpProduct = mpOrder.get(agency_order_id);
                    if (mpProduct == null) {
                        mpProduct = new HashMap<>();
                        mpProduct.put(product_id, product_total_quantity);
                        mpOrder.put(agency_order_id, mpProduct);
                    } else {
                        mpProduct.put(product_id, product_total_quantity);
                        mpOrder.put(agency_order_id, mpProduct);
                    }

                    mpAgencyOrderCode.put(agency_order_id, ConvertUtils.toString(product.get("agency_order_code")));
                }

                int agency_id = ConvertUtils.toInt(jsAgencyOrderDelivery.get("agency_id"));
                for (Map.Entry<Integer, Map<Integer, Integer>> entryOrder : mpOrder.entrySet()) {
                    int agency_order_id = entryOrder.getKey();
                    List<JSONObject> jsProductList = new ArrayList<>();
                    for (Map.Entry<Integer, Integer> entryProduct : entryOrder.getValue().entrySet()) {
                        JSONObject p = new JSONObject();
                        p.put("product_id", entryProduct.getKey());
                        p.put("product_quantity", entryProduct.getValue());
                        jsProductList.add(p);
                    }

                    this.accumulateCSDMService.addTransaction(
                            CSDMTransactionType.DON_HANG.getKey(),
                            agency_id,
                            mpAgencyOrderCode.get(agency_order_id),
                            agency_order_id,
                            agency_order_id,
                            CTTLTransactionSource.AUTO.getId(),
                            0,
                            0,
                            0,
                            JsonUtils.Serialize(
                                    jsProductList
                            )
                    );

                    records.put(mpAgencyOrderCode.get(agency_order_id), JsonUtils.Serialize(
                            jsProductList
                    ));
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    /**
     * Tích lũy cho Xếp hạng
     *
     * @param jsAgencyOrderDelivery
     * @param productList
     */
    private void ghiNhanTichLuyCTXHOfOrderDelivery(JSONObject jsAgencyOrderDelivery, List<JSONObject> productList) {
        try {
            int agency_id = ConvertUtils.toInt(jsAgencyOrderDelivery.get("agency_id"));
            Map<Integer, OrderAccumulateCTXHData> mpOrder = new ConcurrentHashMap<>();
            for (JSONObject product : productList) {
                int agency_order_id = ConvertUtils.toInt(product.get("agency_order_id"));
                long product_total_end_price = ConvertUtils.toLong(product.get("product_total_end_price"));

                OrderAccumulateCTXHData orderData = mpOrder.get(agency_order_id);
                if (orderData == null) {
                    orderData = new OrderAccumulateCTXHData();
                    orderData.setAgency_order_id(agency_order_id);
                    orderData.setAgency_order_code(ConvertUtils.toString(product.get("agency_order_code")));
                    orderData.setTotal_end_price(product_total_end_price);
                    mpOrder.put(agency_order_id, orderData);
                } else {
                    orderData.setTotal_end_price(orderData.getTotal_end_price() + product_total_end_price);
                    mpOrder.put(agency_order_id, orderData);
                }
            }

            for (OrderAccumulateCTXHData orderAccumulateCTXHData : mpOrder.values()) {
                this.accumulateCTXHService.addTransaction(
                        CTTLTransactionType.DON_HANG.getKey(),
                        agency_id,
                        orderAccumulateCTXHData.getAgency_order_code(),
                        orderAccumulateCTXHData.getAgency_order_id(),
                        orderAccumulateCTXHData.getAgency_order_id(),
                        CTTLTransactionSource.AUTO.getId(),
                        0,
                        0,
                        0,
                        orderAccumulateCTXHData.getTotal_end_price()
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    /**
     * Ghi nhận tích lũy cho CTXH
     *
     * @param agency_order_id
     * @param agency_id
     * @param agencyOrderDeptList
     * @param agency_order_code
     * @param type
     */
    public void callGhiNhanCTXHDonHang(
            int agency_order_id,
            int agency_id,
            List<JSONObject> agencyOrderDeptList,
            String agency_order_code,
            int type
    ) {
        try {
            for (JSONObject agencyOrderDept : agencyOrderDeptList) {
                this.accumulateCTXHService.addTransaction(
                        CTTLTransactionType.DON_HANG.getKey(),
                        agency_id,
                        agency_order_code,
                        agency_order_id,
                        agency_order_id,
                        CTTLTransactionSource.AUTO.getId(),
                        0,
                        0,
                        0,
                        ConvertUtils.toLong(agencyOrderDept.get("total_end_price"))
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    public ClientResponse searchVoucher(SessionData sessionData, FilterListByIdRequest request) {
        try {
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(request.getId()));
            filterRequest.setKey("agency_id");
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_VOUCHER, request.getFilters(), request.getSorts());

            List<JSONObject> voucherList = this.orderDB.filter(query, this.appUtils.getOffset(request.getPage()), request.getPage(), request.getIsLimit());
            for (JSONObject jsVoucher : voucherList) {
                jsVoucher.put("vrp", this.orderDB.getVRP(
                        ConvertUtils.toInt(jsVoucher.get("voucher_release_period_id"))));

                String offer_type = ConvertUtils.toString(jsVoucher.get("offer_type"));
                if (VoucherOfferType.GIFT_OFFER.getKey().equals(offer_type)) {
                    List<JSONObject> products = JsonUtils.DeSerialize(
                            jsVoucher.get("items").toString(), new TypeToken<List<JSONObject>>() {
                            }.getType());
                    for (JSONObject product : products) {
                        int item_id = ConvertUtils.toInt(product.get("item_id"));
                        product.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                                item_id));
                    }
                    jsVoucher.put("products", products);
                }
            }
            JSONObject data = new JSONObject();
            data.put("records", voucherList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runCheckCompleteAgencyOrder() {
        try {
            List<JSONObject> orderList = this.orderDB.getlistAgencyOrderShipping(
                    OrderType.APPOINTMENT.getValue(),
                    OrderStatus.SHIPPING.getKey()
            );
            for (JSONObject jsOrder : orderList) {
                int agency_order_id = ConvertUtils.toInt(jsOrder.get("id"));
                if (this.checkFinishDeliveryOrder(agency_order_id)) {
                    this.orderDB.deliveryAgencyOrder(
                            agency_order_id,
                            OrderStatus.COMPLETE.getKey(),
                            "Đồng bộ từ Bravo",
                            0);
                    this.orderDB.saveUpdateOrderStatusHistory(
                            agency_order_id,
                            OrderStatus.COMPLETE.getKey(),
                            "Đồng bộ từ Bravo",
                            0);
                    this.alertToTelegram(
                            "[APPOINTMENT] check complete - " + agency_order_id,
                            ResponseStatus.FAIL
                    );
                }
            }

            List<JSONObject> orderContractList = this.orderDB.getlistAgencyOrderShipping(
                    OrderType.CONTRACT.getValue(),
                    OrderStatus.SHIPPING.getKey()
            );
            for (JSONObject jsOrder : orderContractList) {
                int agency_order_id = ConvertUtils.toInt(jsOrder.get("id"));
                if (this.checkFinishDeliveryOrder(agency_order_id)) {
                    this.orderDB.deliveryAgencyOrder(
                            agency_order_id,
                            OrderStatus.COMPLETE.getKey(),
                            "Đồng bộ từ Bravo",
                            0);
                    this.orderDB.saveUpdateOrderStatusHistory(
                            agency_order_id,
                            OrderStatus.COMPLETE.getKey(),
                            "Đồng bộ từ Bravo",
                            0);
                    this.alertToTelegram(
                            "[APPOINTMENT] check complete - " + agency_order_id,
                            ResponseStatus.FAIL
                    );
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkFinishDeliveryOrder(int agency_order_id) {
        try {
            Map<Integer, ProductMoneyResponse> products = new HashMap<>();
            List<JSONObject> orderProductList = this.orderDB.getListProductInOrder(agency_order_id);
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

                ProductMoneyResponse product = products.get(productMoneyResponse.getId());
                if (product == null) {
                    productMoneyResponse.setQuantity_delivery(
                            this.orderDB.getProductQuantityDelivery(
                                    agency_order_id,
                                    productMoneyResponse.getId())
                    );
                    products.put(productMoneyResponse.getId(), productMoneyResponse);
                } else {
                    product.setQuantity(product.getQuantity() + productMoneyResponse.getQuantity());
                    products.put(product.getId(), product);
                }
                products.put(productMoneyResponse.getId(), productMoneyResponse);
            }

            for (ProductMoneyResponse productMoneyResponse : products.values()) {
                if (productMoneyResponse.getQuantity() > productMoneyResponse.getQuantity_delivery()) {
                    return false;
                }
            }

            Map<Integer, ProductMoneyResponse> gifts = new HashMap<>();
            List<JSONObject> orderGoodsList = this.orderDB.getListGoodsInOrder(agency_order_id);
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
                } else if (AgencyOrderPromoType.GOODS_BONUS.getId() == agencyOrderDetailEntity.getType()) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GOODS_OFFER.getKey());
                } else if (AgencyOrderPromoType.GIFT_BONUS.getId() == agencyOrderDetailEntity.getType()) {
                    productMoneyResponse.setOffer_type(PromoOfferType.GIFT_OFFER.getKey());
                }

                ProductMoneyResponse gift = gifts.get(productMoneyResponse.getId());
                if (gift == null) {
                    productMoneyResponse.setQuantity_delivery(
                            this.orderDB.getGiftQuantityDelivery(
                                    agency_order_id,
                                    productMoneyResponse.getId())
                    );
                    gifts.put(productMoneyResponse.getId(), productMoneyResponse);
                } else {
                    gift.setQuantity(gift.getQuantity() + productMoneyResponse.getQuantity());
                    gifts.put(gift.getId(), gift);
                }
            }

            for (ProductMoneyResponse productMoneyResponse : gifts.values()) {
                if (productMoneyResponse.getQuantity() > productMoneyResponse.getQuantity_delivery()) {
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return false;
    }

    public ClientResponse runCheckOneCompleteAgencyOrder(int agency_order_id) {
        try {
            if (this.checkFinishDeliveryOrder(agency_order_id)) {
                this.orderDB.deliveryAgencyOrder(
                        agency_order_id,
                        OrderStatus.COMPLETE.getKey(),
                        "Đồng bộ từ Bravo",
                        0);
                this.orderDB.saveUpdateOrderStatusHistory(
                        agency_order_id,
                        OrderStatus.COMPLETE.getKey(),
                        "Đồng bộ từ Bravo",
                        0);
                this.alertToTelegram(
                        "[APPOINTMENT] check complete - " + agency_order_id,
                        ResponseStatus.FAIL
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Bổ sung Tích lũy đơn hàng giao
     *
     * @param id
     * @return
     */
    public ClientResponse addAccumulateOrderDelivery(int id) {
        try {
            JSONObject jsAgencyOrderDelivery = this.orderDB.getAgencyOrderDelivery(
                    id
            );
            if (jsAgencyOrderDelivery == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<JSONObject> productList = this.orderDB.getListProductInOrderDelivery(
                    id
            );

            /**
             * Tích lũy cho Đam mê
             */
            this.ghiNhanTichLuyCSDMOfOrderDelivery(jsAgencyOrderDelivery, productList);

            JSONObject data = new JSONObject();
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Ghi nhận tích lũy nhiệm vụ cho đơn hàng
     *
     * @param agency_order_id
     * @param agency_id
     * @param agencyOrderDeptList
     */
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

            this.orderDB.ghiNhanTrangThaiTichLuyNhiemVuChoDonHang(agency_order_id);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    public ClientResponse importOrderConfirmation(SessionData sessionData, ImportOrderConfirmationRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "STAFF");
            }

            BusinessDepartment businessDepartment = this.dataManager.getProductManager().getMpBusinessDepartment().get(staff.getDepartment_id());
            if (businessDepartment == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "BusinessDepartment");
            }

            long total_amount = 0;
            int agency_id = 0;
            String request_delivery_date = null;
            int agency_order_id = 0;
            String code = "";
            String note = "";

            Map<String, JSONObject> mpShippingPlanProduct = new LinkedHashMap<>();
            Map<String, JSONObject> mpOCP = new LinkedHashMap<>();
            Map<String, JSONObject> mpProduct = new LinkedHashMap<>();
            for (OrderConfirmationProductData orderConfirmationProductData : request.getProducts()) {
                JSONObject jsAgencyOrder = this.orderDB.getAgencyOrderByOrderCode(
                        orderConfirmationProductData.getPo_code());
                if (jsAgencyOrder == null) {
                    return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, orderConfirmationProductData.getPo_code());
                }

                if (!note.isEmpty()) {
                    note += "\n";
                }
                note += orderConfirmationProductData.getNote();
                code = ConvertUtils.toString(jsAgencyOrder.get("code"));
                agency_order_id = ConvertUtils.toInt(jsAgencyOrder.get("id"));
                agency_id = ConvertUtils.toInt(jsAgencyOrder.get("agency_id"));
                request_delivery_date = jsAgencyOrder.get("request_delivery_date") == null ? null :
                        jsAgencyOrder.get("request_delivery_date").toString()
                ;
                JSONObject jsOCP = mpOCP.get(orderConfirmationProductData.getItem_code());
                if (jsOCP == null) {
                    JSONObject productCache = this.dataManager.getProductManager().getProductByCode(
                            orderConfirmationProductData.getItem_code());
                    if (productCache == null) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, orderConfirmationProductData.getItem_code());
                    }

                    JSONObject jsProduct = this.orderDB.getAgencyOrderDetailByProduct(
                            agency_order_id,
                            ConvertUtils.toInt(productCache.get("id")));
                    if (ConvertUtils.toDouble(jsProduct.get("product_price")) != orderConfirmationProductData.getItem_price()) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, orderConfirmationProductData.getItem_price());
                    }

                    jsOCP = new JSONObject();
                    jsOCP.put("product_total_quantity", orderConfirmationProductData.getItem_quantity());
                    jsOCP.put("price_total_begin_price",
                            orderConfirmationProductData.getItem_quantity() * orderConfirmationProductData.getItem_price());
                    jsOCP.put("product", productCache);
                    jsOCP.put("code", orderConfirmationProductData.getItem_code());
                    mpProduct.put(orderConfirmationProductData.getItem_code(), productCache);
                } else {
                    int product_total_quantity = ConvertUtils.toInt(jsOCP.get("product_total_quantity"));
                    jsOCP.put("product_total_quantity",
                            product_total_quantity + orderConfirmationProductData.getItem_quantity());
                    jsOCP.put("product_total_begin_price",
                            product_total_quantity * orderConfirmationProductData.getItem_price());
                }

                total_amount += orderConfirmationProductData.getItem_amount();

                jsOCP.put("ocp", orderConfirmationProductData);
                mpOCP.put(orderConfirmationProductData.getItem_code(), jsOCP);
            }

            if (businessDepartment.getOc_type() == 1) {
                JSONObject jsPO = new JSONObject();
                jsPO.put("id", agency_order_id);
                jsPO.put("agency_id", agency_id);
                jsPO.put("code", code);
                jsPO.put("total_begin_price", total_amount);
                jsPO.put("total_end_price", total_amount);
                jsPO.put("request_delivery_date", request_delivery_date);
                for (JSONObject jsOCP : mpOCP.values()) {
                    int agency_order_confirm_id = this.orderDB.insertAgencyOrderConfirm(
                            jsPO,
                            note);
                    LogUtil.printDebug("agency_order_confirm_id:" + agency_order_confirm_id);
                    if (agency_order_confirm_id <= 0) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "insertAgencyOrderConfirm");
                    }

                    JSONObject jsProduct = mpProduct.get(ConvertUtils.toString(jsOCP.get("code")));

                    int product_id = ConvertUtils.toInt(jsProduct.get("id"));

                    OrderConfirmationProductData orderConfirmationProductData = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(jsOCP.get("ocp")),
                            OrderConfirmationProductData.class
                    );

                    jsProduct.put("product_id", product_id);
                    jsProduct.put("product_total_quantity", jsOCP.get("product_total_quantity"));
                    jsProduct.put("product_total_begin_price", jsOCP.get("product_total_begin_price"));
                    jsProduct.put("product_total_end_price", jsOCP.get("product_total_begin_price"));
                    jsProduct.put("product_price", orderConfirmationProductData.getItem_price());
                    jsProduct.put("product_small_unit_id", jsProduct.get("product_small_unit_id"));

                    JSONObject jsProductInfo = new JSONObject();
                    jsProductInfo.put("full_name", this.generateSKUNumberFullname(
                            ConvertUtils.toString(jsProduct.get("full_name")),
                            this.orderDB.countOCP(product_id)
                    ));
                    LogUtil.printDebug("a");
                    this.orderDB.insertAgencyOrderConfirmProduct(
                            agency_order_id,
                            agency_order_confirm_id,
                            jsProduct,
                            orderConfirmationProductData.getItem_description(),
                            JsonUtils.Serialize(jsProductInfo)
                    );

                    this.orderDB.insertProductSummary(
                            product_id,
                            orderConfirmationProductData.getItem_quantity());
                }
            } else {
                JSONObject jsAgencyOrder = this.orderDB.getAgencyOrderByOrderCode(
                        request.getProducts().get(0).getPo_code());
                if (jsAgencyOrder == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                int po_id = ConvertUtils.toInt(jsAgencyOrder.get("id"));
                jsAgencyOrder.put("request_delivery_date",
                        DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(
                                        request.getProducts().get(0).getDelivery_date(),
                                        "dd/MM/yyyy"), "yyyy-MM-dd HH:mm:ss"));
                int agency_order_confirm_id = this.orderDB.insertAgencyOrderConfirm(
                        jsAgencyOrder,
                        "");
                if (agency_order_confirm_id <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                for (OrderConfirmationProductData orderConfirmationProductData : request.getProducts()) {
                    JSONObject productCache = this.dataManager.getProductManager().getProductByCode(
                            orderConfirmationProductData.getItem_code());
                    if (productCache == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    JSONObject jsProduct = this.orderDB.getAgencyOrderDetailByProduct(
                            agency_order_id,
                            ConvertUtils.toInt(productCache.get("id")));
                    if (productCache == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    if (orderConfirmationProductData.getItem_price() != ConvertUtils.toLong(jsProduct.get("product_price"))) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL,
                                orderConfirmationProductData.getItem_code() + "PRICE IN PO: " + ConvertUtils.toLong(jsProduct.get("product_price")));
                    }

                    jsProduct.put("product_total_quantity", orderConfirmationProductData.getItem_quantity());
                    jsProduct.put("product_price", orderConfirmationProductData.getItem_price());
                    jsProduct.put("product_total_begin_price", orderConfirmationProductData.getItem_amount());
                    jsProduct.put("product_total_end_price", orderConfirmationProductData.getItem_amount());

                    int rs = this.orderDB.insertAgencyOrderConfirmProduct(
                            agency_order_id,
                            agency_order_confirm_id,
                            jsProduct,
                            orderConfirmationProductData.getItem_description(),
                            null
                    );
                }
            }

            return ResponseConstants.success;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
    }

    private String generateSKUNumberFullname(String fullName, int number) {
        return "#" + (number + 1) + " " + fullName;
    }

    public ClientResponse createPOMFromPO(SessionData sessionData, int agency_order_id) {
        try {
            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrder(agency_order_id);
            if (jsAgencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            JSONObject jsBusinessDepartment = this.productDB.getBusinessDepartment(staff.getDepartment_id());
            if (jsBusinessDepartment == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "SUPPLIER");
            }

            jsAgencyOrder.put("agency_id", ConvertUtils.toInt(jsBusinessDepartment.get("supplier_default_id")));

            jsAgencyOrder.put("business_department_id", staff.getDepartment_id());
            jsAgencyOrder.put("code", jsAgencyOrder.get("code"));

            List<JSONObject> products = this.orderDB.getListProductInOrder(agency_order_id);

            Map<Integer, ProductCache> mpProductCache = new LinkedHashMap<>();
            Map<Integer, ProgramOrderProduct> mpProgramOrderProduct =
                    this.initMpProductQuotation(
                            products,
                            mpProductCache);
            Map<Integer, ProgramSanSaleOffer> mpResponseProgramSanSaleOffer = new LinkedHashMap<>();
            this.estimatePOM(
                    staff.getDepartment_id(),
                    mpProgramOrderProduct,
                    Source.WEB.getValue(),
                    mpResponseProgramSanSaleOffer
            );

            LogUtil.printDebug(JsonUtils.Serialize(mpResponseProgramSanSaleOffer));

            long total_begin_price = 0;
            for (JSONObject jsProduct : products) {
                int product_id = ConvertUtils.toInt(jsProduct.get("product_id"));
                int product_total_quantity = ConvertUtils.toInt(jsProduct.get("product_total_quantity"));
                double product_price = ConvertUtils.toDouble(jsProduct.get("product_price"));
                for (Map.Entry<Integer, ProgramSanSaleOffer> entry : mpResponseProgramSanSaleOffer.entrySet()) {
                    Double fixed_price = entry.getValue().getMpProductFixedPrice().get(product_id);
                    if (fixed_price != null) {
                        product_price = fixed_price;
                    }
                }
                double product_total_begin_price = product_price * product_total_quantity;
                jsProduct.put("product_price", product_price);
                jsProduct.put("product_total_begin_price", product_total_begin_price);
                jsProduct.put("product_total_end_price", product_total_begin_price);
                total_begin_price += product_total_begin_price;
            }
            jsAgencyOrder.put("total_begin_price", total_begin_price);
            jsAgencyOrder.put("total_end_price", total_begin_price);

            int agency_order_confirm_id = this.orderDB.insertPOM(jsAgencyOrder);
            if (agency_order_confirm_id <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            for (JSONObject jsProduct : products) {
                this.orderDB.insertPOMProduct(agency_order_id, agency_order_confirm_id, jsProduct);
            }

            return ResponseConstants.success;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, ex.getMessage());
        }
    }

    private Map<Integer, ProgramOrderProduct> initMpProductQuotation(
            List<JSONObject> products,
            Map<Integer, ProductCache> mpProductCache) {
        Map<Integer, ProgramOrderProduct> mpProgramOrderProduct = new ConcurrentHashMap<>();
        for (JSONObject jsProduct : products) {
            int product_id = ConvertUtils.toInt(jsProduct.get("product_id"));
            int product_quantity = ConvertUtils.toInt(jsProduct.get("product_total_quantity"));
            ProgramOrderProduct programOrderProduct = new ProgramOrderProduct();
            programOrderProduct.setProductId(product_id);
            ProductCache productBasicData = this.dataManager.getProductManager().getProductBasicData(
                    product_id
            );
            if (productBasicData == null) {
                continue;
            }
            programOrderProduct.setProductPrice(productBasicData.getPrice());
            programOrderProduct.setProduct(
                    this.dataManager.getProgramManager().getProductById(product_id, "")
            );

            programOrderProduct.getProduct().setPrice(productBasicData.getPrice());
            programOrderProduct.setCommonPrice(productBasicData.getCommon_price());
            programOrderProduct.setProductQuantity(
                    product_quantity
            );
            mpProgramOrderProduct.put(programOrderProduct.getProductId(), programOrderProduct);
        }

        return mpProgramOrderProduct;
    }

    private void estimatePOM(int business_department_id,
                             Map<Integer, ProgramOrderProduct> mpProgramOrderProduct,
                             int source,
                             Map<Integer, ProgramSanSaleOffer> mpResponseProgramSanSaleOffer) {
        try {
            this.checkQuotationForProduct(
                    business_department_id,
                    mpProgramOrderProduct,
                    source == Source.APP.getValue() ? Source.APP : Source.WEB,
                    mpResponseProgramSanSaleOffer
            );
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
    }

    public ClientResponse editOCNo(SessionData sessionData, EditOCNoRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrderConfirm(
                    request.getOrder_id()
            );
            if (jsAgencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            boolean rs = this.orderDB.editOCNo(
                    request.getOrder_id(),
                    request.getDoc_no()
            );
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editPONo(SessionData sessionData, EditOCNoRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            JSONObject jsAgencyOrder = this.orderDB.getAgencyOrder(
                    request.getOrder_id()
            );
            if (jsAgencyOrder == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ORDER_NOT_FOUND);
            }

            boolean rs = this.orderDB.editPONo(
                    request.getOrder_id(),
                    request.getDoc_no()
            );
            if (!rs) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse confirmShippingOC(SessionData sessionData, ConfirmShippingOrderRequest request) {
        try {
            ClientResponse clientResponse = request.validRequest();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject jsOC = this.orderDB.getAgencyOrderConfirm(
                    request.getId()
            );
            if (jsOC == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int previousStatus = ConvertUtils.toInt(jsOC.get("status"));
            if (OrderStatus.PREPARE.getKey() != previousStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsUpdateAgencyOrderStatus = this.orderDB.confirmShippingOC(
                    request.getId(),
                    OrderStatus.SHIPPING.getKey());
            if (!rsUpdateAgencyOrderStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchOrder(SessionData sessionData, FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();

            this.addFilterOrderData(sessionData, request);

            String query = this.filterUtils.getQuery(FunctionList.SEARCH_ORDER, request.getFilters(), request.getSorts());

            List<JSONObject> records = this.orderDB.filter(query, 0, ConfigInfo.PAGE_SIZE, 1);

            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse importNewSOCAndCreatePO(SessionData sessionData, ImportOrderConfirmationRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "STAFF");
            }

            BusinessDepartment businessDepartment = this.dataManager.getProductManager().getMpBusinessDepartment().get(staff.getDepartment_id());
            if (businessDepartment == null) {
                return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "BusinessDepartment");
            }

            long total_amount = 0;
            int agency_id = 0;
            String request_delivery_date = null;
            int agency_order_id = 0;
            String code = "";
            String note = "";

            Map<String, JSONObject> mpShippingPlanProduct = new LinkedHashMap<>();
            Map<String, JSONObject> mpOCP = new LinkedHashMap<>();
            Map<String, JSONObject> mpProduct = new LinkedHashMap<>();

            for (OrderConfirmationProductData orderConfirmationProductData : request.getProducts()) {
                if (!note.isEmpty()) {
                    note += "\n";
                }

                JSONObject jsOCP = mpOCP.get(orderConfirmationProductData.getItem_code());
                if (jsOCP == null) {
                    JSONObject productCache = this.dataManager.getProductManager().getProductByCode(
                            orderConfirmationProductData.getItem_code());
                    if (productCache == null) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, orderConfirmationProductData.getItem_code());
                    }

                    JSONObject jsProduct = this.orderDB.getAgencyOrderDetailByProduct(
                            agency_order_id,
                            ConvertUtils.toInt(productCache.get("id")));
                    if (ConvertUtils.toLong(jsProduct.get("product_price")) != orderConfirmationProductData.getItem_price()) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, orderConfirmationProductData.getItem_price());
                    }

                    jsOCP = new JSONObject();
                    jsOCP.put("product_total_quantity", orderConfirmationProductData.getItem_quantity());
                    jsOCP.put("price_total_begin_price",
                            orderConfirmationProductData.getItem_quantity() * orderConfirmationProductData.getItem_price());
                    jsOCP.put("product", productCache);
                    jsOCP.put("code", orderConfirmationProductData.getItem_code());
                } else {
                    int product_total_quantity = ConvertUtils.toInt(jsOCP.get("product_total_quantity"));
                    jsOCP.put("product_total_quantity",
                            product_total_quantity + orderConfirmationProductData.getItem_quantity());
                    jsOCP.put("price_total_begin_price",
                            product_total_quantity * orderConfirmationProductData.getItem_price());
                }

                total_amount += orderConfirmationProductData.getItem_amount();

                jsOCP.put("ocp", jsOCP);
                mpOCP.put(orderConfirmationProductData.getItem_code(), jsOCP);
            }

            if (businessDepartment.getOc_type() == 1) {
                JSONObject jsPO = new JSONObject();
                jsPO.put("id", agency_order_id);
                jsPO.put("code", code);
                jsPO.put("total_begin_price", total_amount);
                jsPO.put("total_end_price", total_amount);
                jsPO.put("request_delivery_date", request_delivery_date);
                for (JSONObject jsOCP : mpOCP.values()) {
                    int agency_order_confirm_id = this.orderDB.insertAgencyOrderConfirm(
                            jsPO,
                            note);
                    if (agency_order_confirm_id <= 0) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "insertAgencyOrderConfirm");
                    }

                    JSONObject jsProduct = mpProduct.get(ConvertUtils.toString(jsOCP.get("code")));

                    int product_id = ConvertUtils.toInt(jsProduct.get("id"));

                    OrderConfirmationProductData orderConfirmationProductData = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(jsOCP.get("ocp")),
                            OrderConfirmationProductData.class
                    );

                    jsProduct.put("product_id", product_id);
                    jsProduct.put("product_total_quantity", jsOCP.get("product_total_quantity"));
                    jsProduct.put("product_total_begin_price", jsOCP.get("product_total_begin_price"));
                    jsProduct.put("product_total_end_price", jsOCP.get("product_total_begin_price"));
                    jsProduct.put("product_price", orderConfirmationProductData.getItem_price());
                    jsProduct.put("product_small_unit_id", jsProduct.get("product_small_unit_id"));

                    JSONObject jsProductInfo = new JSONObject();
                    jsProductInfo.put("full_name", this.generateSKUNumberFullname(
                            ConvertUtils.toString(jsProduct.get("full_name")),
                            this.orderDB.countOCP(product_id)
                    ));
                    this.orderDB.insertAgencyOrderConfirmProduct(
                            agency_order_id,
                            agency_order_confirm_id,
                            jsProduct,
                            orderConfirmationProductData.getItem_description(),
                            JsonUtils.Serialize(jsProductInfo)
                    );

                    this.orderDB.insertProductSummary(
                            product_id,
                            orderConfirmationProductData.getItem_quantity());
                }
            } else {
                JSONObject jsAgencyOrder = this.orderDB.getAgencyOrderByOrderCode(
                        request.getProducts().get(0).getPo_code());
                if (jsAgencyOrder == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                int po_id = ConvertUtils.toInt(jsAgencyOrder.get("id"));
                jsAgencyOrder.put("request_delivery_date",
                        DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(
                                        request.getProducts().get(0).getDelivery_date(),
                                        "dd/MM/yyyy"), "yyyy-MM-dd HH:mm:ss"));
                int agency_order_confirm_id = this.orderDB.insertAgencyOrderConfirm(
                        jsAgencyOrder,
                        "");
                if (agency_order_confirm_id <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                for (OrderConfirmationProductData orderConfirmationProductData : request.getProducts()) {
                    JSONObject productCache = this.dataManager.getProductManager().getProductByCode(
                            orderConfirmationProductData.getItem_code());
                    if (productCache == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    JSONObject jsProduct = this.orderDB.getAgencyOrderDetailByProduct(
                            agency_order_id,
                            ConvertUtils.toInt(productCache.get("id")));
                    if (productCache == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    if (orderConfirmationProductData.getItem_price() != ConvertUtils.toLong(jsProduct.get("product_price"))) {
                        return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL,
                                orderConfirmationProductData.getItem_code() + "PRICE IN PO: " + ConvertUtils.toLong(jsProduct.get("product_price")));
                    }

                    jsProduct.put("product_total_quantity", orderConfirmationProductData.getItem_quantity());
                    jsProduct.put("product_price", orderConfirmationProductData.getItem_price());
                    jsProduct.put("product_total_begin_price", orderConfirmationProductData.getItem_amount());
                    jsProduct.put("product_total_end_price", orderConfirmationProductData.getItem_amount());

                    int rs = this.orderDB.insertAgencyOrderConfirmProduct(
                            agency_order_id,
                            agency_order_confirm_id,
                            jsProduct,
                            orderConfirmationProductData.getItem_description(),
                            null
                    );
                }
            }

            return ResponseConstants.success;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ORDER.name(), ex);
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
    }
}