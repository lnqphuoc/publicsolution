package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.CTXHConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.csdm.CSDMAgencyReward;
import com.app.server.data.dto.cttl.CTTLAgencyReward;
import com.app.server.data.dto.product.Category;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.product.ProductData;
import com.app.server.data.dto.product.ProductGroup;
import com.app.server.data.dto.program.DeptInfo;
import com.app.server.data.dto.program.Program;
import com.app.server.data.dto.program.ProgramType;
import com.app.server.data.dto.program.Source;
import com.app.server.data.dto.program.agency.Agency;
import com.app.server.data.dto.program.filter.ProgramFilterType;
import com.app.server.data.dto.program.limit.ProgramLimit;
import com.app.server.data.dto.promo.PromoBasicData;
import com.app.server.data.dto.promo.PromoOrderData;
import com.app.server.data.dto.promo.PromoProductBasicData;
import com.app.server.data.dto.promo.PromoProductInfoData;
import com.app.server.data.dto.staff.Staff;
import com.app.server.data.entity.*;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.*;
import com.app.server.data.request.ctxh.VRPDataRequest;
import com.app.server.data.request.promo.*;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import io.swagger.models.auth.In;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.spring.web.json.Json;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PromoService extends ProgramService {
    /**
     * Danh sách chính sách
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filerPromo(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_PROMO, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filerPromo(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.promoDB.getTotalPromo(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getPromoInfo(SessionData sessionData, BasicRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            PromoEntity promoEntity = this.promoDB.getPromoInfo(request.getId());
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            CreatePromoRequest createPromoRequest = this.convertPromoToData(request.getId());

            /**
             * promo_structure
             */
            createPromoRequest.setPromo_structure_info(
                    this.convertPromoStructureInfo(
                            request.getId(),
                            createPromoRequest.getPromo_structure()
                    )
            );
            JSONObject data = new JSONObject();

            /**
             * Chương trình không tính vào các chương trình sau
             */
            if (promoEntity.getPromo_type().equals(PromoType.CTTL.getKey())) {
                data.put("promo_be_ignores", new ArrayList<>());
            } else if (promoEntity.getPromo_type().equals(PromoType.BXH.getKey())) {

            } else {
                List<JSONObject> promo_be_ignores =
                        this.promoDB.getListPromoIgnorePromo(
                                request.getId()
                        );
                data.put("promo_be_ignores", promo_be_ignores);
            }

            data.put("promo", createPromoRequest);

            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private JSONObject convertPromoStructureInfo(int promo_id, PromoStructureRequest promo_structure) {
        JSONObject data = new JSONObject();
        try {
            List<JSONObject> lien_ket =
                    this.promoDB.getListPromoLinkByPromoId(
                            promo_id
                    );
            data.put("lien_ket", lien_ket);

            List<JSONObject> dong_thoi =
                    this.promoDB.getPromoFilterListInPromoIds(
                            JsonUtils.Serialize(promo_structure.getPromo_dong_thoi_list())
                    );
            data.put("dong_thoi", dong_thoi);

            List<JSONObject> dong_thoi_tru_gtdtt =
                    this.promoDB.getPromoFilterListInPromoIds(
                            JsonUtils.Serialize(promo_structure.getPromo_dong_thoi_tru_gtdtt_list())
                    );
            data.put("dong_thoi_tru_gtdtt", dong_thoi_tru_gtdtt);

            List<JSONObject> loai_tru =
                    this.promoDB.getPromoFilterListInPromoIds(
                            JsonUtils.Serialize(promo_structure.getPromo_loai_tru_list())
                    );
            data.put("loai_tru", loai_tru);

            List<JSONObject> bi_loai_tru =
                    this.promoDB.getListPromoIgnorePromo(
                            promo_id
                    );
            data.put("bi_loai_tru", bi_loai_tru);

            data.put("is_ignore_all_csbh", promo_structure.getIs_ignore_all_csbh());
            data.put("is_ignore_all_ctkm", promo_structure.getIs_ignore_all_ctkm());
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return data;
    }

    public ClientResponse createPromo(SessionData sessionData, CreatePromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());

            /**
             * Ràng buộc tạo chính sách
             */
            clientResponse = this.validateCreatePromo(request);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Kiểm tra CTTL Liên kết
             */
            int group_id = 0;
            if (request.getPromo_info().getPromo_type().equals(PromoType.CTTL.getKey())) {
                if (!request.getPromo_structure().getPromo_lien_ket_list().isEmpty()) {
                    List<JSONObject> promoLinkList = this.promoDB.getListPromoLink(
                            JsonUtils.Serialize(request.getPromo_structure().getPromo_lien_ket_list())
                    );

                    for (int iLienKet = 0; iLienKet < promoLinkList.size(); iLienKet++) {
                        JSONObject promoLink = promoLinkList.get(iLienKet);
                        if (ConvertUtils.toInt(promoLink.get("group_id")) != 0) {
                            if (group_id != 0 && group_id != ConvertUtils.toInt(promoLink.get("group_id"))) {
                                ClientResponse crLienKet = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                crLienKet.setMessage("[CTTL thứ " + (iLienKet + 1) + "] hiện đang liên kết ở nhóm khác");
                                return crLienKet;
                            }

                            group_id = ConvertUtils.toInt(promoLink.get("group_id"));
                        }
                    }

                    Map<Integer, String> promoLinkFinal = new HashMap<>();
                    for (JSONObject promoLink : promoLinkList) {
                        int promo_link_id = ConvertUtils.toInt(promoLink.get("promo_id"));
                        int group_link_id = ConvertUtils.toInt(promoLink.get("group_id"));
                        promoLinkFinal.put(promo_link_id,
                                ConvertUtils.toString(promo_link_id));
                        if (group_id != 0) {
                            List<JSONObject> promoRelateList = this.promoDB.getListPromoLinkByGroup(
                                    group_link_id
                            );
                            for (JSONObject promoRelate : promoRelateList) {
                                int promo_relate_id = ConvertUtils.toInt(promoRelate.get("promo_id"));
                                if (!promoLinkFinal.containsKey(promo_relate_id)) {
                                    promoLinkFinal.put(
                                            promo_relate_id,
                                            ConvertUtils.toString(promo_relate_id));
                                }
                            }
                        }
                    }
                    if (!promoLinkFinal.isEmpty()) {
                        request.getPromo_structure().setPromo_lien_ket_list(
                                new ArrayList<>(promoLinkFinal.values())
                        );
                    }
                }
            }

            /**
             * Tạo mã chính sách
             */
            String code = this.generatePromoCode(request.getPromo_info().getPromo_type());
            if (code.isEmpty()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
            }

            /**
             * Lưu thông tin chính sách
             */
            PromoEntity promoEntity = this.createPromoEntity(request.getPromo_info());
            promoEntity.setCode(code);
            promoEntity.setStart_date(new Date(request.getPromo_info().getStart_date_millisecond()));
            promoEntity.setEnd_date(request.getPromo_info().getEnd_date_millisecond() == 0 ? null : new Date(request.getPromo_info().getEnd_date_millisecond()));
            promoEntity.setPromo_type(request.getPromo_info().getPromo_type());
            promoEntity.setCondition_type(request.getPromo_info().getCondition_type());
            promoEntity.setOffer_info(this.createOfferInfo(request.getPromo_limits()));
            promoEntity.setStatus(PromoActiveStatus.DRAFT.getId());
            promoEntity.setBusiness_department_id(staff.getDepartment_id());

            if (request.getRepeat_data() != null && request.getRepeat_data().getType() != 0) {
                promoEntity.setRepeat_type(request.getRepeat_data().getType());
                promoEntity.setRepeat_data(JsonUtils.Serialize(
                        request.getRepeat_data()
                ));
            }

            if (request.getPromo_info().getPromo_type().equals(PromoType.CTTL.getKey())) {
                promoEntity.setOrder_date_data(
                        JsonUtils.Serialize(
                                request.getOrder_date_data()
                        )
                );

                promoEntity.setPayment_date_data(
                        JsonUtils.Serialize(
                                request.getPayment_date_data()
                        )
                );

                promoEntity.setReward_date_data(
                        JsonUtils.Serialize(
                                request.getReward_date_data()
                        )
                );

                promoEntity.setConfirm_result_date_data(
                        JsonUtils.Serialize(
                                request.getConfirm_result_date_data()
                        )
                );
            }

            promoEntity = this.promoDB.createPromo(promoEntity);
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
            }

            /**
             * Luu đối tượng áp dụng
             */
            if (request.getPromo_apply_object() != null) {
                PromoApplyObjectEntity promoApplyObjectEntity = this.createPromoApplyObjectEntity(request.getPromo_apply_object());
                promoApplyObjectEntity.setPromo_id(promoEntity.getId());
                promoApplyObjectEntity = this.promoDB.createPromoApplyObject(promoApplyObjectEntity);
                if (promoApplyObjectEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }

                /**`
                 * Bộ lọc
                 */
                for (PromoApplyFilterRequest promoApplyFilterRequest : request.getPromo_apply_object().getPromo_filters()) {
                    PromoFilterEntity promoFilterEntity = this.createPromoFilterEntity(promoApplyFilterRequest);
                    promoFilterEntity.setPromo_id(promoEntity.getId());
                    promoFilterEntity = this.promoDB.createPromoFilter(promoFilterEntity);
                    if (promoFilterEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }
                }
            }

            /**
             * Lưu nhóm sản phẩm áp dụng
             */
            for (int iGroup = 0; iGroup < request.getPromo_item_groups().size(); iGroup++) {
                PromoItemGroupRequest promoItemGroupRequest = request.getPromo_item_groups().get(iGroup);
                promoItemGroupRequest.setData_index(iGroup + 1);
                PromoItemGroupEntity promoItemGroupEntity = this.createPromoItemGroupEntity(promoItemGroupRequest);
                promoItemGroupEntity.setPromo_id(promoEntity.getId());
                promoItemGroupEntity.setData_index(iGroup + 1);
                promoItemGroupEntity = this.promoDB.createPromoItemGroup(promoItemGroupEntity);
                if (promoItemGroupEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }
                promoItemGroupRequest.setId(promoItemGroupEntity.getId());

                for (PromoItemGroupDetailRequest promoItemGroupDetailRequest : promoItemGroupRequest.getProducts()) {
                    promoItemGroupDetailRequest.setItem_name(this.dataManager.getProductManager().getProductFullName(promoItemGroupDetailRequest.getItem_id()));
                    promoItemGroupDetailRequest.setItem_code(this.dataManager.getProductManager().getProductCode(promoItemGroupDetailRequest.getItem_id()));
                    PromoItemGroupDetailEntity promoItemGroupDetailEntity = this.createPromoItemGroupDetailEntity(promoItemGroupDetailRequest);
                    promoItemGroupDetailEntity.setPromo_item_group_id(promoItemGroupEntity.getId());
                    promoItemGroupDetailEntity.setPromo_id(promoEntity.getId());

                    if (promoItemGroupRequest.getCombo_id() != 0) {
                        promoItemGroupDetailEntity.setNote(
                                promoItemGroupRequest.getNote()
                        );
                    }

                    promoItemGroupDetailEntity = this.promoDB.createPromoItemGroupDetail(promoItemGroupDetailEntity);
                    if (promoItemGroupDetailEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }
                }
            }

            /**
             * Lưu sản phẩm loại trừ
             */
            for (int iItem = 0; iItem < request.getPromo_item_ignores().size(); iItem++) {
                PromoItemIgnoreRequest promoItemIgnoreRequest = request.getPromo_item_ignores().get(iItem);
                promoItemIgnoreRequest.setItem_code(this.dataManager.getProductManager().getProductCode(promoItemIgnoreRequest.getItem_id()));
                promoItemIgnoreRequest.setItem_name(this.dataManager.getProductManager().getProductFullName(promoItemIgnoreRequest.getItem_id()));
                PromoItemIgnoreEntity promoItemIgnoreEntity = this.createPromoItemIgnoreEntity(promoItemIgnoreRequest);
                promoItemIgnoreEntity.setPromo_id(promoEntity.getId());
                promoItemIgnoreEntity = this.promoDB.createPromoItemIgnore(promoItemIgnoreEntity);
                if (promoItemIgnoreEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }
            }

            /**
             * Lưu thông tin hạn mức - promo_limit
             */
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                PromoLimitEntity promoLimitEntity = this.createPromoLimitEntity(promoLimitRequest);
                promoLimitEntity.setPromo_id(promoEntity.getId());
                promoLimitEntity.setLevel(iLimit + 1);
                promoLimitRequest.setLevel(iLimit + 1);
                promoLimitEntity = this.promoDB.createPromoLimit(promoLimitEntity);
                if (promoLimitEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }
                promoLimitRequest.setId(promoLimitEntity.getId());

                /**
                 * Lưu thông tin chi tiết hạn mức của từng nhóm - promo_limit_group
                 */
                for (int iLimitGroup = 0; iLimitGroup < promoLimitRequest.getPromo_limit_groups().size(); iLimitGroup++) {
                    PromoLimitGroupRequest promoLimitGroupRequest = promoLimitRequest.getPromo_limit_groups().get(iLimitGroup);
                    promoLimitGroupRequest.setData_index(iLimitGroup + 1);
                    PromoLimitGroupEntity promoLimitGroupEntity = this.createPromoLimitGroupEntity(promoLimitGroupRequest);
                    promoLimitGroupEntity.setPromo_limit_id(promoLimitEntity.getId());
                    promoLimitGroupEntity.setData_index(iLimitGroup + 1);
                    promoLimitGroupEntity.setPromo_id(promoEntity.getId());
                    promoLimitGroupEntity = this.promoDB.createPromoLimitGroup(promoLimitGroupEntity);
                    if (promoLimitEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }
                    promoLimitGroupRequest.setId(promoLimitGroupEntity.getId());

                    /**
                     * Lưu thông tin ưu đãi - promo_offer của từng nhóm
                     */
                    PromoOfferRequest promoOfferRequest = promoLimitGroupRequest.getOffer();
                    if (PromoConditionType.PRODUCT_PRICE.getKey().equals(request.getPromo_info().getCondition_type())
                    ) {
                        promoOfferRequest.setConversion_ratio(promoOfferRequest.getOffer_value());
                    } else {
                        promoOfferRequest.setConversion_ratio(promoOfferRequest.getOffer_value() * 1.0 / promoLimitGroupRequest.getFrom_value() * 100);
                    }
                    PromoOfferEntity promoOfferEntity = this.createPromoOfferEntity(promoOfferRequest);
                    promoOfferEntity.setPromo_limit_group_id(promoLimitGroupEntity.getId());
                    promoOfferEntity.setPromo_limit_id(promoLimitGroupEntity.getPromo_limit_id());
                    promoOfferEntity.setPromo_id(promoEntity.getId());
                    promoOfferEntity = this.promoDB.createPromoOffer(promoOfferEntity);
                    if (promoOfferEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }

                    /**
                     * Lưu thông tin ưu đãi sản phẩm
                     */
                    for (PromoOfferProductRequest promoOfferProductRequest : promoOfferRequest.getOffer_products()) {
                        promoOfferProductRequest.setProduct_name(this.dataManager.getProductManager().getProductFullName(promoOfferProductRequest.getProduct_id()));
                        promoOfferProductRequest.setProduct_code(this.dataManager.getProductManager().getProductCode(promoOfferProductRequest.getProduct_id()));
                        PromoOfferProductEntity promoOfferProductEntity = this.createPromoOfferProductEntity(promoOfferProductRequest);
                        promoOfferProductEntity.setPromo_offer_id(promoOfferEntity.getId());
                        promoOfferProductEntity.setPromo_id(promoEntity.getId());
                        promoOfferProductEntity = this.promoDB.createPromoOfferProduct(promoOfferProductEntity);
                        if (promoOfferProductEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                        }
                    }

                    /**
                     * Lưu thông tin ưu đãi tặng kèm
                     */
                    for (PromoOfferBonusRequest promoOfferBonusRequest : promoOfferRequest.getOffer_bonus()) {
                        promoOfferBonusRequest.setProduct_name(this.dataManager.getProductManager().getProductFullName(promoOfferBonusRequest.getProduct_id()));
                        promoOfferBonusRequest.setProduct_code(this.dataManager.getProductManager().getProductCode(promoOfferBonusRequest.getProduct_id()));
                        PromoOfferBonusEntity promoOfferBonusEntity = this.createPromoOfferBonusEntity(promoOfferBonusRequest);
                        promoOfferBonusEntity.setPromo_offer_id(promoOfferEntity.getId());
                        promoOfferBonusEntity.setPromo_id(promoEntity.getId());
                        promoOfferBonusEntity = this.promoDB.createPromoOfferBonus(promoOfferBonusEntity);
                        if (promoOfferBonusEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                        }
                    }
                }
            }

            /**
             * Lưu cơ cấu
             */
            if (request.getPromo_info().getPromo_type().equals(PromoType.CTTL.getKey())) {
                this.promoDB.createPromoStructure(
                        promoEntity.getId(),
                        request.getPromo_structure().getPromo_lien_ket_list(),
                        request.getPromo_structure().getPromo_dong_thoi_list(),
                        request.getPromo_structure().getPromo_dong_thoi_tru_gtdtt_list(),
                        request.getPromo_structure().getPromo_loai_tru_list(),
                        request.getPromo_structure().getIs_ignore_all_csbh(),
                        request.getPromo_structure().getIs_ignore_all_ctkm()
                );

                if (!request.getPromo_structure().getPromo_lien_ket_list().isEmpty()) {
                    this.savePromoLink(
                            request.getPromo_structure().getPromo_lien_ket_list(),
                            group_id,
                            promoEntity.getId()
                    );
                }
            }

            /**
             * Lưu lịch sử
             *
             */
            request.getPromo_info().setId(promoEntity.getId());
            request.getPromo_info().setCode(code);
            request.getPromo_info().setStart_date(DateTimeUtils.toString(promoEntity.getStart_date()));
            request.getPromo_info().setEnd_date(promoEntity.getEnd_date() == null ? null : DateTimeUtils.toString(promoEntity.getEnd_date()));
            this.insertPromoHistory(
                    promoEntity.getId(),
                    promoEntity.getStart_date(),
                    promoEntity.getEnd_date(),
                    JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())),
                    "Tạo mới",
                    PromoActiveStatus.DRAFT.getId(),
                    sessionData.getId());

            JSONObject data = new JSONObject();
            data.put("id", promoEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void savePromoLink(
            List<String> promo_lien_ket_list,
            int group_id,
            int promo_id) {
        try {

            /**
             * Clear liên kết cũ
             */
            if (group_id != 0) {
                this.promoDB.clearPromoLinkGroup(
                        group_id);
            } else {
                group_id = promo_id;
            }

            this.promoDB.insertPromoLink(
                    promo_id,
                    group_id
            );

            for (String strPromoID : promo_lien_ket_list) {
                int promo_link_id = ConvertUtils.toInt(strPromoID);
                this.promoDB.insertPromoLink(
                        promo_link_id,
                        group_id
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
    }

    private Integer getPromoPriority(String promo_type) {
        if (PromoType.PROMO.getKey() != promo_type) {
            return 0;
        }

        JSONObject last = this.promoDB.getLastPromoPriority();
        if (last != null) {
            return ConvertUtils.toInt(last.get("priority")) + 1;
        }
        return 1;
    }

    private PromoFilterEntity createPromoFilterEntity(PromoApplyFilterRequest promoApplyFilterRequest) {
        PromoFilterEntity promoFilterEntity = new PromoFilterEntity();
        promoFilterEntity.setFilter_types(JsonUtils.Serialize(promoApplyFilterRequest.getFilter_types()));
        return promoFilterEntity;
    }

    private PromoApplyObjectEntity createPromoApplyObjectEntity(PromoApplyObjectRequest promo_apply_object) {
        try {
            PromoApplyObjectEntity promoApplyObjectEntity = new PromoApplyObjectEntity();
            promoApplyObjectEntity.setPromo_agency_ignore("[]");
            promoApplyObjectEntity.setPromo_agency_include("[]");
            if (promo_apply_object == null) {
                return promoApplyObjectEntity;
            }

            promoApplyObjectEntity.setPromo_agency_include(
                    JsonUtils.Serialize(promo_apply_object.getPromo_agency_includes())
            );


            promoApplyObjectEntity.setPromo_agency_ignore(
                    JsonUtils.Serialize(promo_apply_object.getPromo_agency_ignores())
            );

            promoApplyObjectEntity.setPromo_sufficient_condition(
                    JsonUtils.Serialize(promo_apply_object.getPromo_sufficient_conditions())
            );

            return promoApplyObjectEntity;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return null;
    }

    private PromoAgencyIgnoreEntity createPromoAgencyIgnoreEntity(AgencyBasicData agencyBasicData) {
        PromoAgencyIgnoreEntity promoAgencyIgnoreEntity = new PromoAgencyIgnoreEntity();
        promoAgencyIgnoreEntity.setAgency_id(agencyBasicData.getId());
        return promoAgencyIgnoreEntity;
    }

    private PromoAgencyIncludeEntity createPromoAgencyIncludeEntity(AgencyBasicData agencyBasicData) {
        PromoAgencyIncludeEntity promoAgencyIncludeEntity = new PromoAgencyIncludeEntity();
        promoAgencyIncludeEntity.setAgency_id(agencyBasicData.getId());
        return promoAgencyIncludeEntity;
    }

    private PromoItemIgnoreEntity createPromoItemIgnoreEntity(PromoItemIgnoreRequest promoItemIgnoreRequest) {
        PromoItemIgnoreEntity promoItemIgnoreEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(promoItemIgnoreRequest), PromoItemIgnoreEntity.class);
        return promoItemIgnoreEntity;
    }

    /**
     * Luu lịch sử chinh sách
     *
     * @param promo_id
     * @param start_date
     * @param end_date
     * @param promo_data
     * @param note
     * @param status
     * @return
     */
    private boolean insertPromoHistory(
            int promo_id,
            Date start_date,
            Date end_date,
            String promo_data,
            String note,
            int status,
            int staff_id) {
        try {
            PromoHistoryEntity promoHistoryEntity = new PromoHistoryEntity();
            promoHistoryEntity.setPromo_id(promo_id);
            promoHistoryEntity.setPromo_data(promo_data);
            promoHistoryEntity.setNote(note);
            promoHistoryEntity.setStart_date(start_date);
            promoHistoryEntity.setEnd_date(end_date);
            promoHistoryEntity.setStatus(status);
            promoHistoryEntity.setCreator_id(staff_id);
            promoHistoryEntity = this.promoDB.savePromoHistory(promoHistoryEntity);
            if (promoHistoryEntity == null) {
                return false;
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return true;
    }

    /**
     * Cập nhật lịch sử chinh sách
     *
     * @param promo_id
     * @param start_date
     * @param end_date
     * @param promo_data
     * @param note
     * @param status
     * @return
     */
    private boolean updatePromoHistory(int id, int promo_id, Date start_date, Date end_date, String promo_data, String note, int status, Date created_date) {
        PromoHistoryEntity promoHistoryEntity = new PromoHistoryEntity();
        promoHistoryEntity.setId(id);
        promoHistoryEntity.setPromo_id(promo_id);
        promoHistoryEntity.setPromo_data(promo_data);
        promoHistoryEntity.setNote(note);
        promoHistoryEntity.setStart_date(start_date);
        promoHistoryEntity.setEnd_date(end_date);
        promoHistoryEntity.setStatus(status);
        promoHistoryEntity.setCreated_date(created_date);
        promoHistoryEntity = this.promoDB.savePromoHistory(promoHistoryEntity);
        if (promoHistoryEntity == null) {
            return false;
        }
        return true;
    }

    /**
     * Ràng buộc tạo chính sách
     *
     * @param request
     * @return
     */
    private ClientResponse validateCreatePromo(CreatePromoRequest request) {
        /**
         * Chỉ CSBH - SLSP và DSSP mới tặng hàng
         */
        if ((request.getPromo_info().getPromo_type().equals(PromoType.PROMO.getKey()) ||
                request.getPromo_info().getPromo_type().equals(PromoType.SALE_POLICY.getKey())) &&
                request.getPromo_info().getCondition_type().equals(PromoConditionType.ORDER_PRICE.getKey())) {
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest limit = request.getPromo_limits().get(iLimit);
                if (limit.getOffer_type().equals(PromoOfferType.GOODS_OFFER.getKey())) {
                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    clientResponse.setMessage("[Hạn mức " + (iLimit + 1) + "]" +
                            "CSBH/CTKM giá trị đơn hàng thì không tặng hàng");
                    return clientResponse;
                }
            }
        }

        /**
         * Hạn mức phải tăng dần
         */
        if (request.getPromo_limits().size() > 1) {
            for (int iLimit = 1; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest currentPromoLimit = request.getPromo_limits().get(iLimit - 1);
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);

                ClientResponse clientResponse = promoLimitRequest.validateUpperValue(currentPromoLimit);
                if (clientResponse.failed()) {
                    clientResponse.setMessage("[Hạn mức " + (iLimit + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }
            }
        }

        /**
         * Điều kiện áp dụng: doanh số sản phẩm, Ưu đãi: giảm tiền
         * Danh sách sản phẩm ưu đãi không được trống
         */
        if (PromoConditionType.PRODUCT_PRICE.getKey().equals(request.getPromo_info().getCondition_type())) {
            for (PromoLimitRequest promoLimitRequest : request.getPromo_limits()) {
                for (PromoLimitGroupRequest promoLimitGroupRequest : promoLimitRequest.getPromo_limit_groups()) {
                    if (PromoOfferType.MONEY_DISCOUNT.getKey().equals(promoLimitGroupRequest.getOffer().getOffer_type())
                            && promoLimitGroupRequest.getOffer().getOffer_products().isEmpty()) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_PRODUCT_INVALID);
                    }
                }
            }
        } else if (PromoConditionType.ORDER_PRICE.getKey().equals(request.getPromo_info().getCondition_type())
                && !request.getPromo_item_groups().isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_ORDER_PRICE_NOT_ITEM_GROUP);
        }

        /**
         * Chính sách không trả thưởng tự động
         * - thì không có bước nhảy
         * Chính sách trả thưởng tự động
         * - chỉ áp dụng cho nhóm có 1 sản phẩm
         */
        if (request.getPromo_info().getIs_automatic_allocation() == 0) {
            for (PromoLimitRequest promoLimitRequest : request.getPromo_limits()) {
                if (PromoConditionType.STEP.getKey().equals(promoLimitRequest.getCondition_type())) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_AUTOMATIC_ALLOCATION_NOT_STEP);
                }
            }
        }
//        else {
//            for (int iGroup = 0; iGroup < request.getPromo_item_groups().size(); iGroup++) {
//                PromoItemGroupRequest promoItemGroupRequest = request.getPromo_item_groups().get(iGroup);
//                if (promoItemGroupRequest.getProducts().size() > 1) {
//                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_AUTOMATIC_ALLOCATION_NOT_GROUP_UPPER_1_SP);
//                    clientResponse.setMessage("[Nhóm " + (iGroup + 1) + "]" + clientResponse.getMessage());
//                    return clientResponse;
//                }
//            }
//        }

        /**
         * Loại giá trị đến của chính sách
         * 1. Chính sách có giá trị đến thì end_value của nhóm hạn mức phải != 0, trừ hạn mức cuối
         * 2. Chính sách không có giá trị đến thì các end_value của nhóm hạn mức phải = 0
         *
         */
        if (PromoEndValueType.IS_NOT_NULL.getKey().equals(request.getPromo_info().getPromo_end_value_type())) {
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                for (int iGroup = 0; iGroup < promoLimitRequest.getPromo_limit_groups().size(); iGroup++) {
                    PromoLimitGroupRequest promoLimitGroupRequest = promoLimitRequest.getPromo_limit_groups().get(iGroup);
                    if (iLimit != request.getPromo_limits().size() - 1 && promoLimitGroupRequest.getEnd_value() == 0) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_END_VALUE_TYPE_IS_NOT_NULL_INVALID);
                        clientResponse.setMessage("[Hạn mức " + (iLimit + 1) + "][Nhóm " + (iGroup + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }
        } else if (PromoEndValueType.IS_NULL.getKey().equals(request.getPromo_info().getPromo_end_value_type())) {
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                for (PromoLimitGroupRequest promoLimitGroupRequest : promoLimitRequest.getPromo_limit_groups()) {
                    if (promoLimitGroupRequest.getEnd_value() != 0) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_END_VALUE_TYPE_IS_NULL_INVALID);
                    }
                }
            }
        }

        /**
         * Giá trị đơn hàng
         */
        if (PromoConditionType.ORDER_PRICE.getKey().equals(request.getPromo_info().getCondition_type()) ||
                PromoConditionType.DTT.getKey().equals(request.getPromo_info().getCondition_type())
        ) {
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                for (int iGroup = 0; iGroup < promoLimitRequest.getPromo_limit_groups().size(); iGroup++) {
                    PromoLimitGroupRequest promoLimitGroupRequest = promoLimitRequest.getPromo_limit_groups().get(iGroup);
                    String offer_type = promoLimitGroupRequest.getOffer().getOffer_type();
                    if ((PromoOfferType.GOODS_OFFER.getKey().equals(promoLimitRequest.getOffer_type())
                            || PromoOfferType.GIFT_OFFER.getKey().equals(offer_type))
                            && promoLimitGroupRequest.getOffer().getOffer_bonus().isEmpty()) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_BONUS_INVALID);
                        clientResponse.setMessage("[Hạn mức " + (iLimit + 1) + "][Nhóm " + (iGroup + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }
        } else {
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                for (int iGroup = 0; iGroup < promoLimitRequest.getPromo_limit_groups().size(); iGroup++) {
                    PromoLimitGroupRequest promoLimitGroupRequest = promoLimitRequest.getPromo_limit_groups().get(iGroup);
                    if (PromoOfferType.GIFT_OFFER.getKey().equals(promoLimitRequest.getOffer_type())
                            && promoLimitGroupRequest.getOffer().getOffer_bonus().isEmpty()) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_BONUS_INVALID);
                        clientResponse.setMessage("[Hạn mức " + (iLimit + 1) + "][Nhóm " + (iGroup + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    } else if ((PromoOfferType.MONEY_DISCOUNT.getKey().equals(promoLimitRequest.getOffer_type())
                            || PromoOfferType.PERCENT_DISCOUNT.getKey().equals(promoLimitRequest.getOffer_type()))
                            && promoLimitGroupRequest.getOffer().getOffer_products().isEmpty()) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_PRODUCT_INVALID);
                        clientResponse.setMessage("[Hạn mức " + (iLimit + 1) + "][Nhóm " + (iGroup + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }
        }

        /**
         * Sản phẩm áp dụng không thể chứa sản phẩm loại trừ
         */
        for (int iItemIgnore = 0; iItemIgnore < request.getPromo_item_ignores().size(); iItemIgnore++) {
            for (int iGroup = 0; iGroup < request.getPromo_item_groups().size(); iGroup++) {
                for (int iItemGroup = 0; iItemGroup < request.getPromo_item_groups().get(iGroup).getProducts().size(); iItemGroup++) {
                    if (request.getPromo_item_groups().get(iGroup).getProducts().get(iItemGroup).getItem_id()
                            == request.getPromo_item_ignores().get(iItemIgnore).getItem_id()) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_ITEM_GROUP_CONTAIN_ITEM_IGNORE);
                        clientResponse.setMessage("[Nhóm " + (iGroup + 1) + "][Sản phẩm thứ " + (iItemGroup + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }
        }

        /**
         * Đại lý chỉ định không trùng với đại lý loại trừ
         */
        if (request.getPromo_apply_object() != null) {
            for (int iDaiLyChiDinh = 0; iDaiLyChiDinh < request.getPromo_apply_object().getPromo_agency_includes().size(); iDaiLyChiDinh++) {
                for (int iDaiLyLoaiTru = 0; iDaiLyLoaiTru < request.getPromo_apply_object().getPromo_agency_ignores().size(); iDaiLyLoaiTru++) {
                    if (request.getPromo_apply_object().getPromo_agency_includes().get(iDaiLyChiDinh).getId()
                            == request.getPromo_apply_object().getPromo_agency_ignores().get(iDaiLyLoaiTru).getId()) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_AGENCY_INCLUDE_CONTAIN_AGENCY_IGNORE);
                        clientResponse.setMessage("[Đại lý chỉ định thứ " + (iDaiLyChiDinh + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }
        }

        /**
         * Bắt buộc nhập số lượng giới hạn chương trình săn sale
         */
//        if (request.getPromo_info().getPromo_type().equals(PromoType.CTSS.getKey())) {
//            for (int iGroup = 0; iGroup < request.getPromo_item_groups().size(); iGroup++) {
//                for (int iDetail = 0; iDetail < request.getPromo_item_groups().get(iGroup).getProducts().size(); iDetail++) {
//                    if (request.getPromo_item_groups().get(iGroup).getProducts().get(iDetail).getMax_offer_per_promo() <= 0) {
//                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_HUNT_SALE_MAX_OFFER_PER_PROMO_INVALD);
//                        clientResponse.setMessage("[Nhóm " + (iGroup + 1) + "][SP " + (iDetail + 1) + "]" + clientResponse.getMessage());
//                        return clientResponse;
//                    }
//                }
//            }
//        }

        /**
         * CTTL Bậc thang thì hạn mức phải có giá trị đến,
         * trừ hạn mức cuối
         */
        if (request.getPromo_info().getPromo_type().equals(PromoType.CTTL.getKey())) {
            if (request.getPromo_info().getForm_of_reward().equals(PromoFormOfRewardType.BAC_THANG.getKey())) {
                for (int iHanMuc = 0; iHanMuc < request.getPromo_limits().size(); iHanMuc++) {
                    if (iHanMuc != request.getPromo_limits().size() - 1) {
                        for (PromoLimitGroupRequest promoLimitGroupRequest : request.getPromo_limits().get(iHanMuc).getPromo_limit_groups()) {
                            if (promoLimitGroupRequest.getEnd_value() <= 0) {
                                ClientResponse crBT = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                crBT.setMessage("[Hạn mức " + (iHanMuc + 1) + "]" +
                                        "CTTL Bậc thang nên cần nhập giá trị đến dưới");
                                return crBT;
                            }
                        }
                    }
                }
            }
        }

        return ClientResponse.success(null);
    }

    private ClientResponse validateCreateDamMe(CreatePromoRequest request) {
        /**
         * Đam mê chỉ 1 nhóm sản phẩm
         */
        if (request.getPromo_item_groups().size() != 1) {
            ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            clientResponse.setMessage("Đam mê chỉ thiết lập cho 1 nhóm sản phẩm");
            return clientResponse;
        }

        if (request.getPromo_limits().size() > 5) {
            ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            clientResponse.setMessage("Đam mê chỉ thiết lập tối đa 5 hạn mức");
            return clientResponse;
        }


        /**
         * Hạn mức phải tăng dần
         */
        if (request.getPromo_limits().size() > 1) {
            for (int iLimit = 1; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest currentPromoLimit = request.getPromo_limits().get(iLimit - 1);
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);

                ClientResponse clientResponse = promoLimitRequest.validateUpperValue(currentPromoLimit);
                if (clientResponse.failed()) {
                    clientResponse.setMessage("[Hạn mức " + (iLimit + 1) + "]" + clientResponse.getMessage());
                    return clientResponse;
                }
            }
        }

        /**
         * Chính sách không trả thưởng tự động
         * - thì không có bước nhảy
         * Chính sách trả thưởng tự động
         * - chỉ áp dụng cho nhóm có 1 sản phẩm
         */
        if (request.getPromo_info().getIs_automatic_allocation() == 0) {
            for (PromoLimitRequest promoLimitRequest : request.getPromo_limits()) {
                if (PromoConditionType.STEP.getKey().equals(promoLimitRequest.getCondition_type())) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_AUTOMATIC_ALLOCATION_NOT_STEP);
                }
            }
        }
//        else {
//            for (int iGroup = 0; iGroup < request.getPromo_item_groups().size(); iGroup++) {
//                PromoItemGroupRequest promoItemGroupRequest = request.getPromo_item_groups().get(iGroup);
//                if (promoItemGroupRequest.getProducts().size() > 1) {
//                    ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_AUTOMATIC_ALLOCATION_NOT_GROUP_UPPER_1_SP);
//                    clientResponse.setMessage("[Nhóm " + (iGroup + 1) + "]" + clientResponse.getMessage());
//                    return clientResponse;
//                }
//            }
//        }

        /**
         * Loại giá trị đến của chính sách
         * 1. Chính sách có giá trị đến thì end_value của nhóm hạn mức phải != 0, trừ hạn mức cuối
         * 2. Chính sách không có giá trị đến thì các end_value của nhóm hạn mức phải = 0
         *
         */
        if (PromoEndValueType.IS_NOT_NULL.getKey().equals(request.getPromo_info().getPromo_end_value_type())) {
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                for (int iGroup = 0; iGroup < promoLimitRequest.getPromo_limit_groups().size(); iGroup++) {
                    PromoLimitGroupRequest promoLimitGroupRequest = promoLimitRequest.getPromo_limit_groups().get(iGroup);
                    if (iLimit != request.getPromo_limits().size() - 1 && promoLimitGroupRequest.getEnd_value() == 0) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_END_VALUE_TYPE_IS_NOT_NULL_INVALID);
                        clientResponse.setMessage("[Hạn mức " + (iLimit + 1) + "][Nhóm " + (iGroup + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }
        } else if (PromoEndValueType.IS_NULL.getKey().equals(request.getPromo_info().getPromo_end_value_type())) {
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                for (PromoLimitGroupRequest promoLimitGroupRequest : promoLimitRequest.getPromo_limit_groups()) {
                    if (promoLimitGroupRequest.getEnd_value() != 0) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_END_VALUE_TYPE_IS_NULL_INVALID);
                    }
                }
            }
        }

        /**
         * Sản phẩm áp dụng không thể chứa sản phẩm loại trừ
         */
        for (int iItemIgnore = 0; iItemIgnore < request.getPromo_item_ignores().size(); iItemIgnore++) {
            for (int iGroup = 0; iGroup < request.getPromo_item_groups().size(); iGroup++) {
                for (int iItemGroup = 0; iItemGroup < request.getPromo_item_groups().get(iGroup).getProducts().size(); iItemGroup++) {
                    if (request.getPromo_item_groups().get(iGroup).getProducts().get(iItemGroup).getItem_id()
                            == request.getPromo_item_ignores().get(iItemIgnore).getItem_id()) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_ITEM_GROUP_CONTAIN_ITEM_IGNORE);
                        clientResponse.setMessage("[Nhóm " + (iGroup + 1) + "][Sản phẩm thứ " + (iItemGroup + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }
        }

        /**
         * Đại lý chỉ định không trùng với đại lý loại trừ
         */
        if (request.getPromo_apply_object() != null) {
            for (int iDaiLyChiDinh = 0; iDaiLyChiDinh < request.getPromo_apply_object().getPromo_agency_includes().size(); iDaiLyChiDinh++) {
                for (int iDaiLyLoaiTru = 0; iDaiLyLoaiTru < request.getPromo_apply_object().getPromo_agency_ignores().size(); iDaiLyLoaiTru++) {
                    if (request.getPromo_apply_object().getPromo_agency_includes().get(iDaiLyChiDinh).getId()
                            == request.getPromo_apply_object().getPromo_agency_ignores().get(iDaiLyLoaiTru).getId()) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_AGENCY_INCLUDE_CONTAIN_AGENCY_IGNORE);
                        clientResponse.setMessage("[Đại lý chỉ định thứ " + (iDaiLyChiDinh + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }
        }

        /**
         * Bậc thang thì hạn mức phải có giá trị đến,
         * trừ hạn mức cuối
         */
        if (request.getPromo_info().getForm_of_reward().equals(PromoFormOfRewardType.BAC_THANG.getKey())) {
            for (int iHanMuc = 0; iHanMuc < request.getPromo_limits().size(); iHanMuc++) {
                if (iHanMuc != request.getPromo_limits().size() - 1) {
                    for (PromoLimitGroupRequest promoLimitGroupRequest : request.getPromo_limits().get(iHanMuc).getPromo_limit_groups()) {
                        if (promoLimitGroupRequest.getEnd_value() <= 0) {
                            ClientResponse crBT = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            crBT.setMessage("[Hạn mức " + (iHanMuc + 1) + "]" +
                                    "CTTL Bậc thang nên cần nhập giá trị đến dưới");
                            return crBT;
                        }

                        if (promoLimitGroupRequest.getOffer().getOffer_value() == null ||
                                promoLimitGroupRequest.getOffer().getOffer_value() <= 0) {
                            ClientResponse cfOffer = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_NOT_EMPTY);
                            cfOffer.setMessage("[Hạn mức " + (iHanMuc + 1) + "] " +
                                    cfOffer.getMessage());
                            return cfOffer;
                        }
                    }
                }
            }
        }

        return ClientResponse.success(null);
    }

    private String createOfferInfo(List<PromoLimitRequest> promo_limits) {
        List<String> offer_info = new ArrayList<>();
        if (promo_limits.size() > 0) {
            PromoLimitRequest promoLimitRequest = promo_limits.get(0);
            for (PromoLimitGroupRequest promoOfferRequest : promoLimitRequest.getPromo_limit_groups()) {
                if (offer_info.contains(promoOfferRequest.getOffer().getOffer_type())) {
                    continue;
                }
                offer_info.add(promoOfferRequest.getOffer().getOffer_type());
            }
        }
        return JsonUtils.Serialize(offer_info);
    }

    private PromoOfferBonusEntity createPromoOfferBonusEntity(PromoOfferBonusRequest promoOfferBonusRequest) {
        return JsonUtils.DeSerialize(JsonUtils.Serialize(promoOfferBonusRequest), PromoOfferBonusEntity.class);
    }

    private PromoOfferProductEntity createPromoOfferProductEntity(PromoOfferProductRequest promoOfferProductRequest) {
        return JsonUtils.DeSerialize(JsonUtils.Serialize(promoOfferProductRequest), PromoOfferProductEntity.class);
    }

    private PromoOfferEntity createPromoOfferEntity(PromoOfferRequest promoOfferRequest) {
        PromoOfferEntity promoOfferEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(promoOfferRequest), PromoOfferEntity.class);
        return promoOfferEntity;
    }

    private PromoLimitGroupEntity createPromoLimitGroupEntity(PromoLimitGroupRequest promoLimitGroupRequest) {
        return JsonUtils.DeSerialize(JsonUtils.Serialize(promoLimitGroupRequest), PromoLimitGroupEntity.class);
    }

    private PromoLimitEntity createPromoLimitEntity(PromoLimitRequest promoLimitRequest) {
        return JsonUtils.DeSerialize(JsonUtils.Serialize(promoLimitRequest), PromoLimitEntity.class);
    }

    private PromoItemGroupDetailEntity createPromoItemGroupDetailEntity(PromoItemGroupDetailRequest promoItemGroupDetailRequest) {
        return JsonUtils.DeSerialize(JsonUtils.Serialize(promoItemGroupDetailRequest), PromoItemGroupDetailEntity.class);
    }

    private PromoItemGroupEntity createPromoItemGroupEntity(PromoItemGroupRequest promoItemGroupRequest) {
        PromoItemGroupEntity promoItemGroupEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(promoItemGroupRequest), PromoItemGroupEntity.class);
        promoItemGroupEntity.setId(0);
        return promoItemGroupEntity;
    }

    private PromoEntity createPromoEntity(PromoInfoRequest promo_info) {
        promo_info.setStart_date(null);
        promo_info.setEnd_date(null);
        PromoEntity promoEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(promo_info), PromoEntity.class);

        /**
         * Nếu thời gian bắt đầu nhỏ hơn thời gian hiện tại
         * set thời gian bắt đầu bằng thời gian hiện tại
         */
        if (!promo_info.getPromo_type().equals(PromoType.CTTL.getKey())) {
            if (promo_info.getStart_date_millisecond() < DateTimeUtils.getMilisecondsNow()) {
                promo_info.setStart_date_millisecond(DateTimeUtils.getMilisecondsNow());
            }
        }

        return promoEntity;
    }

    private String generatePromoCode(String promo_type) {
        try {
            PromoType promoType = PromoType.from(promo_type);
            if (promoType == null) {
                return "";
            }

            /**
             * yyMMdd + loại chính sách + stt của loại chính sách
             */
            int count = this.promoDB.getTotalPromoByPromoType(promo_type);
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

    public ClientResponse editPromo(SessionData sessionData, EditPromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            PromoEntity oldPromoEntity = this.promoDB.getPromo(request.getPromo_info().getId());
            if (oldPromoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            if (oldPromoEntity.getPromo_type().equals(PromoType.CTTL.getKey())
                    && oldPromoEntity.getStatus() != PromoActiveStatus.DRAFT.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            CreatePromoRequest oldPromoData = this.convertPromoToData(request.getPromo_info().getId());

            if (!request.getPromo_info().getPromo_type().equals(oldPromoData.getPromo_info().getPromo_type())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_EDIT_PROMO_TYPE);
            }
            if (!request.getPromo_info().getCondition_type().equals(oldPromoData.getPromo_info().getCondition_type())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_EDIT_CONDITION_TYPE);
            }
            /**
             * Ràng buộc chỉnh sửa chính sách
             */
            clientResponse = this.validateCreatePromo(request);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Kiểm tra CTTL Liên kết
             */
            int group_id = 0;
            JSONObject jsGroup = this.promoDB.getPromoLinkGroupByPromoId(request.getPromo_info().getId());
            if (jsGroup != null) {
                group_id = ConvertUtils.toInt(jsGroup.get("group_id"));
            }
            if (request.getPromo_info().getPromo_type().equals(PromoType.CTTL.getKey())) {
                if (!request.getPromo_structure().getPromo_lien_ket_list().isEmpty()) {
                    if (request.getPromo_structure().getPromo_lien_ket_list().contains(
                            "\"" + request.getPromo_info().getId() + "\""
                    )) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL).fail(
                                "CTTL Liên kết không hợp lệ"
                        );
                    }
                    List<JSONObject> promoLinkList = this.promoDB.getListPromoLink(
                            JsonUtils.Serialize(request.getPromo_structure().getPromo_lien_ket_list())
                    );

                    for (int iLienKet = 0; iLienKet < promoLinkList.size(); iLienKet++) {
                        JSONObject promoLink = promoLinkList.get(iLienKet);
                        if (ConvertUtils.toInt(promoLink.get("group_id")) != 0) {
                            if (group_id != 0 && group_id != ConvertUtils.toInt(promoLink.get("group_id"))) {
                                ClientResponse crLienKet = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                crLienKet.setMessage("[CTTL thứ " + (iLienKet + 1) + "] hiện đang liên kết ở nhóm khác");
                                return crLienKet;
                            }

                            group_id = ConvertUtils.toInt(promoLink.get("group_id"));
                        }
                    }

                    Map<Integer, String> promoLinkFinal = new HashMap<>();
                    for (String promoLinkId : request.getPromo_structure().getPromo_lien_ket_list()) {
                        int promo_link_id = ConvertUtils.toInt(promoLinkId);
                        JSONObject promoLink = this.promoDB.getPromoLinkGroupByPromoId(
                                ConvertUtils.toInt(promoLinkId)
                        );
                        int group_link_id = 0;
                        if (promoLink != null) {
                            group_link_id = ConvertUtils.toInt(promoLink.get("group_id"));
                        }
                        promoLinkFinal.put(promo_link_id,
                                ConvertUtils.toString(promo_link_id));
                        if (group_id != 0 && group_link_id != 0) {
                            List<JSONObject> promoRelateList = this.promoDB.getListPromoLinkByGroup(
                                    group_link_id
                            );
                            for (JSONObject promoRelate : promoRelateList) {
                                int promo_relate_id = ConvertUtils.toInt(promoRelate.get("promo_id"));
                                if (promo_relate_id == request.getPromo_info().getId()) {
                                    continue;
                                }
                                if (!promoLinkFinal.containsKey(promo_relate_id)) {
                                    promoLinkFinal.put(
                                            promo_relate_id,
                                            ConvertUtils.toString(promo_relate_id));
                                }
                            }
                        }
                    }
                    if (!promoLinkFinal.isEmpty()) {
                        request.getPromo_structure().setPromo_lien_ket_list(
                                new ArrayList<>(promoLinkFinal.values())
                        );
                    }
                }
            }

            PromoEntity promoEntity = this.createPromoEntity(request.getPromo_info());
            promoEntity.setPriority(oldPromoEntity.getPriority());
            promoEntity.setId(oldPromoEntity.getId());
            promoEntity.setCode(oldPromoEntity.getCode());
            promoEntity.setStart_date(new Date(request.getPromo_info().getStart_date_millisecond()));
            promoEntity.setEnd_date(request.getPromo_info().getEnd_date_millisecond() == 0 ? null : new Date(request.getPromo_info().getEnd_date_millisecond()));
            promoEntity.setPromo_type(request.getPromo_info().getPromo_type());
            promoEntity.setCondition_type(request.getPromo_info().getCondition_type());
            promoEntity.setOffer_info(this.createOfferInfo(request.getPromo_limits()));
            promoEntity.setStatus(oldPromoEntity.getStatus());
            promoEntity.setBusiness_department_id(oldPromoEntity.getBusiness_department_id());
            if (request.getRepeat_data() != null &&
                    request.getRepeat_data().getType() != 0) {
                promoEntity.setRepeat_type(request.getRepeat_data().getType());
                promoEntity.setRepeat_data(JsonUtils.Serialize(request.getRepeat_data()));
            }

            if (request.getPromo_info().getPromo_type().equals(PromoType.CTTL.getKey())) {
                promoEntity.setOrder_date_data(
                        JsonUtils.Serialize(
                                request.getOrder_date_data()
                        )
                );

                promoEntity.setPayment_date_data(
                        JsonUtils.Serialize(
                                request.getPayment_date_data()
                        )
                );

                promoEntity.setReward_date_data(
                        JsonUtils.Serialize(
                                request.getReward_date_data()
                        )
                );

                promoEntity.setConfirm_result_date_data(
                        JsonUtils.Serialize(
                                request.getConfirm_result_date_data()
                        )
                );
            }

            int statusEdit = oldPromoEntity.getStatus();
            if (PromoActiveStatus.RUNNING.getId() != oldPromoEntity.getStatus()) {
                /**
                 * Chính sách chưa chạy hoặc đã kết thúc
                 * 1. Xóa dữ liệu cũ
                 * 2. Lưu thông tin mới
                 * 3. Lưu lịch sử
                 */

                /**
                 * Xóa promo cũ
                 */
                boolean rsDeletePromoPre = this.deletePromoPre(request.getPromo_info().getId());
                if (!rsDeletePromoPre) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * Lưu thông tin chính sách
                 */
                clientResponse = this.updatePromoNew(promoEntity, request);
                if (clientResponse.failed()) {
                    return clientResponse;
                }

                /**
                 * Lưu dữ liệu liên kết
                 */
                if (request.getPromo_info().getPromo_type().equals(PromoType.CTTL.getKey())) {
                    if (group_id != 0 && request.getPromo_structure().getPromo_lien_ket_list().isEmpty()) {
                        List<JSONObject> promoLinkList = this.promoDB.getListPromoLinkByGroup(
                                group_id
                        );
                        if (promoLinkList.size() <= 2) {
                            this.promoDB.clearPromoLinkGroup(
                                    group_id);
                        } else {
                            this.promoDB.removePromoLinkGroup(
                                    request.getPromo_info().getId());
                        }
                    } else if (!request.getPromo_structure().getPromo_lien_ket_list().isEmpty()) {
                        this.savePromoLink(
                                request.getPromo_structure().getPromo_lien_ket_list(),
                                group_id,
                                request.getPromo_info().getId()
                        );
                    }
                }

                if (PromoActiveStatus.WAITING.getId() == promoEntity.getStatus()) {
                    this.cancelPromoScheduleWaitingStart(request.getPromo_info().getId());
                    this.cancelPromoScheduleWaitingStop(request.getPromo_info().getId());
                    this.createPromoSchedule(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            PromoScheduleType.START);
                }
            } else {
                /**
                 * Chính sách đang chạy
                 * 1. Nếu thời gian bắt đầu < thời gian hiện tại:
                 * - xóa chính sách cũ
                 * - chạy ngay chính sách mới
                 * - đặt lịch kết thúc nếu có
                 * 2. Nếu thời gian bắt đầu > thời gian hiện tại:
                 * - hẹn giờ thực thi chính sách
                 */
                if (request.getPromo_info().getStart_date_millisecond() <= DateTimeUtils.getMilisecondsNow()) {
                    /**
                     * 1. Nếu thời gian bắt đầu < thời gian hiện tại
                     * - xóa chính sách cũ
                     * - chạy ngay chính sách mới
                     * - cập nhật lịch kết thúc nếu có
                     */

                    /**
                     * Xóa dữ liệu cơ cấu của chính sách cũ
                     */
                    boolean rsDeletePromoPre = this.deletePromoPre(request.getPromo_info().getId());
                    if (!rsDeletePromoPre) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * Lưu thông tin chính sách mới
                     */
                    clientResponse = this.updatePromoNew(promoEntity, request);
                    if (clientResponse.failed()) {
                        return clientResponse;
                    }

                    /**
                     * cập nhật lịch chính sách nếu có
                     * - Hủy lịch kích hoạt nếu có
                     * - Hủy lịch kết thúc cũ nếu có
                     * - Tạo lích kết thúc mới nếu có thời gian kết thúc
                     */
                    this.cancelPromoScheduleWaitingStart(request.getPromo_info().getId());
                    this.cancelPromoScheduleWaitingStop(request.getPromo_info().getId());
                    if (promoEntity.getEnd_date() != null) {
                        this.createPromoSchedule(
                                promoEntity.getId(),
                                promoEntity.getEnd_date(),
                                PromoScheduleType.STOP);
                    }

                    /**
                     * Chạy ngay chính sách mới
                     */
                    PromoRunningEntity promoRunningEntity = this.promoDB.getPromoRunningById(promoEntity.getId());
                    if (promoRunningEntity == null) {
                        promoRunningEntity = new PromoRunningEntity();
                    }
                    promoRunningEntity.setPromo_id(promoEntity.getId());
                    promoRunningEntity.setPromo_data(JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())));
                    promoRunningEntity = this.promoDB.savePromoRunning(promoRunningEntity);
                    if (promoRunningEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadPromoRunning(
                            promoEntity.getId(),
                            PromoScheduleType.START,
                            promoEntity.getPromo_type()
                    );
                } else {
                    /**
                     * 2. Nếu thời gian bắt đầu > thời gian hiện tại:
                     * - Lưu thông tin chính sách vào lịch sử
                     * - Hủy lịch kích hoạt cũ nếu có
                     * - Hủy lịch kết thúc cũ nếu có
                     * - Tạo lích kích hoạt mới
                     */
                    this.cancelPromoScheduleWaitingStart(request.getPromo_info().getId());
                    this.cancelPromoScheduleWaitingStop(request.getPromo_info().getId());
                    this.createPromoSchedule(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            PromoScheduleType.START);

                    statusEdit = PromoActiveStatus.WAITING.getId();
                }
            }

            /**
             * Lưu lịch sử
             *
             */
            request.getPromo_info().setId(promoEntity.getId());
            request.getPromo_info().setCode(promoEntity.getCode());
            request.getPromo_info().setStart_date(DateTimeUtils.toString(promoEntity.getStart_date()));
            request.getPromo_info().setEnd_date(promoEntity.getEnd_date() == null ? null : DateTimeUtils.toString(promoEntity.getEnd_date()));
            request.getPromo_info().setStatus(statusEdit);
            convertPromoDataHistory(request);

            this.insertPromoHistory(
                    promoEntity.getId(),
                    promoEntity.getStart_date(),
                    promoEntity.getEnd_date(),
                    JsonUtils.Serialize(request),
                    request.getNote(),
                    statusEdit,
                    sessionData.getId());
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void convertPromoDataHistory(EditPromoRequest request) {
        convertPromoItemGroup(request.getPromo_item_groups());
        convertPromoItemIgnore(request.getPromo_item_ignores());
        convertPromoLimit(request.getPromo_limits());
    }

    /**
     * Lưu lập lịch cho chính sách
     *
     * @param promo_id
     * @param start_time
     * @param promoScheduleType
     * @return
     */
    private ClientResponse createPromoSchedule(int promo_id, Date start_time, PromoScheduleType promoScheduleType) {
        try {
            PromoScheduleEntity promoScheduleEntity = new PromoScheduleEntity();
            promoScheduleEntity.setPromo_id(promo_id);
            promoScheduleEntity.setStatus(ProcessStatus.WAITING.getValue());
            promoScheduleEntity.setSchedule_time(start_time);
            promoScheduleEntity.setSchedule_type(promoScheduleType.getKey());
            this.promoDB.createPromoSchedule(promoScheduleEntity);
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Hủy lập lịch kích hoạt của chính sách
     *
     * @param promo_id
     */
    private ClientResponse cancelPromoScheduleWaitingStart(int promo_id) {
        try {
            JSONObject promoScheduleWaitingStart = this.promoDB.getPromoScheduleWaitingStartByPromo(promo_id);
            if (promoScheduleWaitingStart != null) {
                PromoScheduleEntity promoScheduleEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(promoScheduleWaitingStart), PromoScheduleEntity.class);
                if (promoScheduleEntity != null) {
                    boolean rsCancelPromoSchedule = this.promoDB.cancelPromoSchedule(promoScheduleEntity.getId());
                    if (rsCancelPromoSchedule) {
                        return ClientResponse.success(null);
                    }
                }
            } else {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Hủy lập lịch kết thúc của chính sách
     *
     * @param promo_id
     * @return
     */
    private ClientResponse cancelPromoScheduleWaitingStop(int promo_id) {
        try {
            JSONObject promoScheduleWaitingStop = this.promoDB.getPromoScheduleWaitingStopByPromo(promo_id);
            if (promoScheduleWaitingStop != null) {
                PromoScheduleEntity promoScheduleEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(promoScheduleWaitingStop), PromoScheduleEntity.class);
                if (promoScheduleEntity != null) {
                    boolean rsCancelPromoSchedule = this.promoDB.cancelPromoSchedule(promoScheduleEntity.getId());
                    if (rsCancelPromoSchedule) {
                        return ClientResponse.success(null);
                    }
                }
            } else {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Lưu chính sách sau khi chỉnh sửa
     *
     * @param promoEntity
     * @param request
     * @return
     */
    private ClientResponse updatePromoNew(PromoEntity promoEntity, CreatePromoRequest request) {
        try {
            promoEntity = this.promoDB.createPromo(promoEntity);
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Luu đối tượng áp dụng
             */
            if (request.getPromo_apply_object() != null) {
                PromoApplyObjectEntity promoApplyObjectEntity = this.createPromoApplyObjectEntity(request.getPromo_apply_object());
                promoApplyObjectEntity.setPromo_id(promoEntity.getId());
                promoApplyObjectEntity = this.promoDB.createPromoApplyObject(promoApplyObjectEntity);
                if (promoApplyObjectEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**`
                 * Bộ lọc
                 */
                for (PromoApplyFilterRequest promoApplyFilterRequest : request.getPromo_apply_object().getPromo_filters()) {
                    PromoFilterEntity promoFilterEntity = this.createPromoFilterEntity(promoApplyFilterRequest);
                    promoFilterEntity.setPromo_id(promoEntity.getId());
                    promoFilterEntity = this.promoDB.createPromoFilter(promoFilterEntity);
                    if (promoFilterEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }
            }

            /**
             * 1. Lưu nhóm sản phẩm áp dụng
             */
            for (int iGroup = 0; iGroup < request.getPromo_item_groups().size(); iGroup++) {
                PromoItemGroupRequest promoItemGroupRequest = request.getPromo_item_groups().get(iGroup);
                promoItemGroupRequest.setData_index(iGroup + 1);
                PromoItemGroupEntity promoItemGroupEntity = this.createPromoItemGroupEntity(promoItemGroupRequest);
                promoItemGroupEntity.setPromo_id(promoEntity.getId());
                promoItemGroupEntity.setData_index(iGroup + 1);
                promoItemGroupEntity.setCombo_id(promoItemGroupRequest.getCombo_id());
                if (promoItemGroupEntity.getType().equals(ItemGroupType.GROUP.getCode())) {
                    promoItemGroupEntity.setCombo_id(0);
                }
                promoItemGroupEntity = this.promoDB.createPromoItemGroup(promoItemGroupEntity);
                if (promoItemGroupEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                promoItemGroupRequest.setId(promoItemGroupEntity.getId());

                for (PromoItemGroupDetailRequest promoItemGroupDetailRequest : promoItemGroupRequest.getProducts()) {
                    PromoItemGroupDetailEntity promoItemGroupDetailEntity = this.createPromoItemGroupDetailEntity(promoItemGroupDetailRequest);
                    promoItemGroupDetailEntity.setPromo_item_group_id(promoItemGroupEntity.getId());
                    promoItemGroupDetailEntity.setPromo_id(promoEntity.getId());
                    if (promoItemGroupDetailRequest.getCategory_level() != 0 ||
                            promoItemGroupDetailRequest.getItem_type().equals(DamMeProductType.CATEGORY.getKey())) {
                        promoItemGroupDetailRequest.setCategory_level(
                                this.dataManager.getProductManager().getCategoryById(
                                        promoItemGroupDetailRequest.getItem_id()
                                ).getCategory_level()
                        );
                        promoItemGroupDetailRequest.setItem_type(DamMeProductType.CATEGORY.getKey());
                        promoItemGroupDetailEntity.setItem_type(DamMeProductType.CATEGORY.getKey());
                    }

                    if (promoItemGroupRequest.getType().equals("COMBO")) {
                        promoItemGroupDetailEntity.setNote(
                                promoItemGroupRequest.getNote()
                        );
                    }
                    promoItemGroupDetailEntity = this.promoDB.createPromoItemGroupDetail(promoItemGroupDetailEntity);
                    if (promoItemGroupDetailEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }
            }

            /**
             * Lưu sản phẩm loại trừ
             */
            for (int iItem = 0; iItem < request.getPromo_item_ignores().size(); iItem++) {
                PromoItemIgnoreRequest promoItemIgnoreRequest = request.getPromo_item_ignores().get(iItem);
                promoItemIgnoreRequest.setItem_code(this.dataManager.getProductManager().getProductCode(promoItemIgnoreRequest.getItem_id()));
                promoItemIgnoreRequest.setItem_name(this.dataManager.getProductManager().getProductFullName(promoItemIgnoreRequest.getItem_id()));
                PromoItemIgnoreEntity promoItemIgnoreEntity = this.createPromoItemIgnoreEntity(promoItemIgnoreRequest);
                promoItemIgnoreEntity.setPromo_id(promoEntity.getId());
                promoItemIgnoreEntity = this.promoDB.createPromoItemIgnore(promoItemIgnoreEntity);
                if (promoItemIgnoreEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            /**
             * 2. Lưu thông tin hạn mức - promo_limit
             */
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                PromoLimitEntity promoLimitEntity = this.createPromoLimitEntity(promoLimitRequest);
                promoLimitEntity.setPromo_id(promoEntity.getId());
                promoLimitEntity.setLevel(iLimit + 1);
                promoLimitRequest.setLevel(iLimit + 1);
                promoLimitEntity = this.promoDB.createPromoLimit(promoLimitEntity);
                if (promoLimitEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                promoLimitRequest.setId(promoLimitEntity.getId());

                /**
                 * Lưu thông tin chi tiết hạn mức của từng nhóm - promo_limit_group
                 */
                for (int iLimitGroup = 0; iLimitGroup < promoLimitRequest.getPromo_limit_groups().size(); iLimitGroup++) {
                    PromoLimitGroupRequest promoLimitGroupRequest = promoLimitRequest.getPromo_limit_groups().get(iLimitGroup);
                    promoLimitGroupRequest.setData_index(iLimitGroup + 1);
                    PromoLimitGroupEntity promoLimitGroupEntity = this.createPromoLimitGroupEntity(promoLimitGroupRequest);
                    promoLimitGroupEntity.setPromo_limit_id(promoLimitEntity.getId());
                    promoLimitGroupEntity.setData_index(iLimitGroup + 1);
                    promoLimitGroupEntity.setPromo_id(promoEntity.getId());
                    promoLimitGroupEntity = this.promoDB.createPromoLimitGroup(promoLimitGroupEntity);
                    if (promoLimitEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                    promoLimitGroupRequest.setId(promoLimitGroupEntity.getId());

                    /**
                     * Lưu thông tin ưu đãi - promo_offer của từng nhóm
                     */
                    PromoOfferRequest promoOfferRequest = promoLimitGroupRequest.getOffer();
                    /**
                     * Đối với DSSP
                     * thì offer_value = convertion_ratio
                     */
                    if (PromoConditionType.PRODUCT_PRICE.getKey().equals(request.getPromo_info().getCondition_type())
                    ) {
                        promoOfferRequest.setConversion_ratio(promoOfferRequest.getOffer_value());
                    } else {
                        promoOfferRequest.setConversion_ratio(promoOfferRequest.getOffer_value() * 1.0 / promoLimitGroupRequest.getFrom_value() * 100);
                    }
                    PromoOfferEntity promoOfferEntity = this.createPromoOfferEntity(promoOfferRequest);
                    promoOfferEntity.setPromo_limit_group_id(promoLimitGroupEntity.getId());
                    promoOfferEntity.setPromo_limit_id(promoLimitGroupEntity.getPromo_limit_id());
                    promoOfferEntity.setPromo_id(promoEntity.getId());

                    /**
                     * Lưu voucher
                     */
                    String voucher_data = promoOfferRequest.getVoucher_data();
                    promoOfferEntity.setVoucher_data(
                            voucher_data
                    );

                    promoOfferEntity = this.promoDB.createPromoOffer(promoOfferEntity);
                    if (promoOfferEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * Lưu thông tin ưu đãi sản phẩm
                     */
                    for (PromoOfferProductRequest promoOfferProductRequest : promoOfferRequest.getOffer_products()) {
                        PromoOfferProductEntity promoOfferProductEntity = this.createPromoOfferProductEntity(promoOfferProductRequest);
                        promoOfferProductEntity.setPromo_offer_id(promoOfferEntity.getId());
                        promoOfferProductEntity.setPromo_id(promoEntity.getId());
                        promoOfferProductEntity = this.promoDB.createPromoOfferProduct(promoOfferProductEntity);
                        if (promoOfferProductEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                    }

                    /**
                     * Lưu thông tin ưu đãi tặng kèm
                     */
                    for (PromoOfferBonusRequest promoOfferBonusRequest : promoOfferRequest.getOffer_bonus()) {
                        PromoOfferBonusEntity promoOfferBonusEntity = this.createPromoOfferBonusEntity(promoOfferBonusRequest);
                        promoOfferBonusEntity.setPromo_offer_id(promoOfferEntity.getId());
                        promoOfferBonusEntity.setPromo_id(promoEntity.getId());
                        promoOfferBonusEntity = this.promoDB.createPromoOfferBonus(promoOfferBonusEntity);
                        if (promoOfferBonusEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                    }
                }
            }

            /**
             * Lưu cơ cấu
             */
            if (request.getPromo_info().getPromo_type().equals(PromoType.CTTL.getKey())) {
                this.promoDB.createPromoStructure(
                        promoEntity.getId(),
                        request.getPromo_structure().getPromo_lien_ket_list(),
                        request.getPromo_structure().getPromo_dong_thoi_list(),
                        request.getPromo_structure().getPromo_dong_thoi_tru_gtdtt_list(),
                        request.getPromo_structure().getPromo_loai_tru_list(),
                        request.getPromo_structure().getIs_ignore_all_csbh(),
                        request.getPromo_structure().getIs_ignore_all_ctkm()
                );
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Xóa dữ liệu cơ cấu của chính sách
     *
     * @param promo_id
     * @return
     */
    private boolean deletePromoPre(int promo_id) {
        boolean rs = this.promoDB.deletePromoItemGroupDetail(promo_id);
        rs = this.promoDB.deletePromoItemGroup(promo_id);
        rs = this.promoDB.deletePromoItemIgnore(promo_id);
        rs = this.promoDB.deletePromoOfferBonus(promo_id);
        rs = this.promoDB.deletePromoOfferProduct(promo_id);
        rs = this.promoDB.deletePromoOffer(promo_id);
        rs = this.promoDB.deletePromoLimitGroup(promo_id);
        rs = this.promoDB.deletePromoLimit(promo_id);
        rs = this.promoDB.deletePromoApplyObject(promo_id);
        rs = this.promoDB.deletePromoFilter(promo_id);
        rs = this.promoDB.deletePromoStructure(promo_id);

        return true;
    }

    /**
     * Dừng chính sách
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse stopPromo(SessionData sessionData, StopPromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            PromoEntity promoEntity = this.promoDB.getPromo(request.getId());
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            /**
             * Chính sách phải ở trạng thái
             * - đã duyệt
             * - đã kích hoạt thì phải dừng ngay
             */
            if (!(PromoActiveStatus.RUNNING.getId() == promoEntity.getStatus() ||
                    (PromoActiveStatus.WAITING.getId() == promoEntity.getStatus() && PromoStopType.STOP_NOW.getKey().equals(request.getStop_type())))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            /**
             * 1. Trường hợp dừng ngay thì Xóa chính sách khỏi danh sách đnag chạy
             */
            if (PromoStopType.STOP_NOW.getKey().equals(request.getStop_type())) {
                promoEntity.setEnd_date(DateTimeUtils.getNow());
                promoEntity.setStatus(PromoActiveStatus.STOPPED.getId());
                boolean rsStopPromo = this.promoDB.stopPromo(
                        promoEntity.getId(),
                        promoEntity.getEnd_date(),
                        promoEntity.getStatus());
                if (!rsStopPromo) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * Lưu lịch sử
                 *
                 */
                this.insertPromoHistory(
                        promoEntity.getId(),
                        promoEntity.getStart_date(),
                        promoEntity.getEnd_date(),
                        JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())),
                        PromoStopType.STOP_NOW.getLabel() + ": " + request.getNote(),
                        promoEntity.getStatus(),
                        sessionData.getId());

                if (promoEntity.getPromo_type().equals(PromoType.SALE_POLICY.getKey()) ||
                        promoEntity.getPromo_type().equals(PromoType.PROMO.getKey()) ||
                        promoEntity.getPromo_type().equals(PromoType.CTSS.getKey())
                ) {
                    clientResponse = this.removePromoRunning(
                            request.getId()
                    );
                    if (clientResponse.failed()) {
                        return clientResponse;
                    }

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadPromoRunning(
                            request.getId(),
                            PromoScheduleType.STOP,
                            promoEntity.getPromo_type()
                    );
                } else if (promoEntity.getPromo_type().equals(PromoType.DAMME.getKey())) {
                    clientResponse = this.removeDamMeRunning(
                            request.getId()
                    );
                    if (clientResponse.failed()) {
                        return clientResponse;
                    }

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadDamMeRunning(
                            request.getId(),
                            PromoScheduleType.STOP,
                            promoEntity.getPromo_type()
                    );
                }

                this.cancelPromoScheduleWaitingStart(promoEntity.getId());
                this.cancelPromoScheduleWaitingStop(promoEntity.getId());

                if (PromoType.CTTL.getKey().equals(promoEntity.getPromo_type())) {
                    CreatePromoRequest promo_data = this.convertPromoToData(promoEntity.getId());
                    promo_data.getPromo_info().setStatus(
                            promoEntity.getStatus()
                    );
                    this.updatePromoRunning(
                            promoEntity.getId(),
                            promo_data
                    );
                }
            } else if (PromoStopType.STOP_SCHEDULE.getKey().equals(request.getStop_type())) {
                /**
                 * Lưu thời gian kết thúc
                 */
                promoEntity.setEnd_date(DateTimeUtils.getDateTime(request.getStop_time()));
                boolean rsStopPromo = this.promoDB.stopPromo(
                        promoEntity.getId(),
                        promoEntity.getEnd_date(),
                        promoEntity.getStatus()
                );
                if (!rsStopPromo) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }


                /**
                 * Lưu lịch sử
                 *
                 */
                this.insertPromoHistory(
                        promoEntity.getId(),
                        promoEntity.getStart_date(),
                        promoEntity.getEnd_date(),
                        JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())),
                        PromoStopType.STOP_SCHEDULE.getLabel() + ": " + request.getNote(),
                        promoEntity.getStatus(),
                        sessionData.getId());

                /**
                 * Cập nhật lại lập lịch
                 * 1. Hủy lập lịch kết thúc cũ
                 * 2. Tạo lập lịch kết thúc mới
                 */
                PromoScheduleEntity promoScheduleEntity = this.promoDB.getPromoScheduleWaitingStopByPromoId(request.getId());
                if (promoScheduleEntity != null) {
                    clientResponse = this.cancelPromoScheduleWaitingStop(request.getId());
                    if (clientResponse.failed()) {
                        return clientResponse;
                    }
                }

                clientResponse = this.createPromoSchedule(promoEntity.getId(),
                        new Date(request.getStop_time()),
                        PromoScheduleType.STOP);
                if (clientResponse.failed()) {
                    return clientResponse;
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse removePromoRunning(int id) {
        try {
            this.promoDB.removePromoRunning(id);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.success(null);
    }

    private ClientResponse removeDamMeRunning(int id) {
        try {
            this.promoDB.removeDamMeRunning(id);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.success(null);
    }

    /**
     * Hủy chính sách ở trạng thái nháp
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse cancelPromo(SessionData sessionData, CancelPromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            PromoEntity promoEntity = this.promoDB.getPromo(request.getId());
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            int old_status = promoEntity.getStatus();

            /**
             * CSBH/CTKM/CTSS - trạng thái nháp được hủy
             */
            if (!PromoType.CTTL.getKey().equals(promoEntity.getPromo_type())) {
                if (PromoActiveStatus.DRAFT.getId() != promoEntity.getStatus()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
                }
            }

            promoEntity.setStatus(PromoActiveStatus.CANCEL.getId());

            promoEntity = this.promoDB.cancelPromo(promoEntity);
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (old_status == PromoCTTLStatus.RUNNING.getId() &&
                    PromoType.CTTL.getKey().equals(promoEntity.getPromo_type())) {

                CreatePromoRequest promo_data = this.convertPromoToData(
                        request.getId()
                );
                promo_data.getPromo_info().setStatus(
                        PromoCTTLStatus.CANCEL.getId()
                );


                /**
                 * Cập nhật promo_running
                 */
                this.updatePromoRunning(
                        request.getId(),
                        promo_data
                );

                /**
                 * reload cache promo
                 */
                this.dataManager.getProgramManager().reloadPromoRunning(
                        request.getId(),
                        PromoScheduleType.START,
                        promoEntity.getPromo_type()
                );

                this.insertPromoHistory(
                        promoEntity.getId(),
                        promoEntity.getStart_date(),
                        promoEntity.getEnd_date(),
                        JsonUtils.Serialize(promo_data),
                        request.getNote(),
                        promoEntity.getStatus(),
                        sessionData.getId());
                this.cancelPromoScheduleWaitingStart(request.getId());
                this.cancelPromoScheduleWaitingStop(request.getId());
            } else {
                CreatePromoRequest promo_data = this.convertPromoToData(
                        request.getId()
                );
                promo_data.getPromo_info().setStatus(
                        PromoCTTLStatus.CANCEL.getId()
                );
                this.insertPromoHistory(
                        promoEntity.getId(),
                        promoEntity.getStart_date(),
                        promoEntity.getEnd_date(),
                        JsonUtils.Serialize(promo_data),
                        request.getNote(),
                        promoEntity.getStatus(),
                        sessionData.getId());
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Duyệt chính sách
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse approvePromo(SessionData sessionData, ApprovePromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
            /**
             * 1. Kiểm tra chính sách tồn tại
             */
            PromoEntity promoEntity = this.promoDB.getPromo(request.getId());
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            Date now = DateTimeUtils.getNow();
            /**
             * 2. Kiểm tra trước khi duyệt: thời gian và trạng thái
             */
            clientResponse = this.validateApprovePromo(promoEntity);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            if (promoEntity.getPromo_type().equals(PromoType.CTTL.getKey())) {
                PromoTimeRequest promoTimeRequest =
                        JsonUtils.DeSerialize(
                                promoEntity.getOrder_date_data(),
                                PromoTimeRequest.class);
                if (promoTimeRequest.getEnd_date_millisecond() <= now.getTime()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_BEGIN_INVALID);
                }
            }

            if (promoEntity.getPromo_type().equals(PromoType.CTSS.getKey()) &&
                    promoEntity.getPriority() == 0) {
                int lastPriority = this.promoDB.getLastPriorityPromoHuntSale();
                promoEntity.setPriority(lastPriority + 1);
            }

            boolean isPromoRunning = false;
            /**
             * Nếu thời gian bắt đầu < thời gian hiện tại
             * 1. lưu thời gian bắt đầu chính sách bằng thời gian duyệt hiện tại
             * 2. kích hoạt chính sách đang chạy
             */
            if (promoEntity.getStart_date().getTime() < DateTimeUtils.getMilisecondsNow()) {
                if (!promoEntity.getPromo_type().equals(PromoType.CTTL.getKey())) {
                    promoEntity.setStart_date(now);
                } else {
                    promoEntity.setStart_date(now);
                    PromoTimeRequest promoTimeRequest =
                            JsonUtils.DeSerialize(
                                    promoEntity.getOrder_date_data(),
                                    PromoTimeRequest.class);
                    promoTimeRequest.setStart_date_millisecond(now.getTime());
                    promoEntity.setOrder_date_data(
                            JsonUtils.Serialize(promoTimeRequest)
                    );
                }
                promoEntity.setStatus(PromoActiveStatus.RUNNING.getId());
                isPromoRunning = true;
            } else {
                promoEntity.setStatus(PromoActiveStatus.WAITING.getId());
            }

            promoEntity = this.promoDB.approvePromo(promoEntity);
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            CreatePromoRequest promo_data = this.convertPromoToData(promoEntity.getId());

            /**
             * 1. Nếu chính sách chạy ngay thì thêm vào danh sách chính sách đang chạy
             * 2. Nếu chính sách chờ chạy thì thêm vào danh sách hẹn giờ
             */
            /**
             * 1. Nếu chính sách đang chạy thì thêm vào danh sách chính sách đang chạy
             */
            if (isPromoRunning) {
                if (promoEntity.getPromo_type().equals(PromoType.DAMME.getKey())) {
                    /**
                     * Lưu lịch sử
                     *
                     */
                    this.insertPromoHistory(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            promoEntity.getEnd_date(),
                            JsonUtils.Serialize(promo_data),
                            "Duyệt",
                            promoEntity.getStatus(),
                            sessionData.getId());

                    /**
                     * 2. Nếu có ngày kết thúc thì đặt lịch kết thúc
                     */
                    if (promoEntity.getEnd_date() != null) {
                        this.createPromoSchedule(
                                promoEntity.getId(),
                                promoEntity.getEnd_date(),
                                PromoScheduleType.STOP);
                    }

                    ClientResponse crPromoRunning = this.updateDamMeRunning(
                            request.getId(),
                            this.convertPromoToData(promoEntity.getId()));
                    if (crPromoRunning.failed()) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadDamMeRunning(
                            request.getId(),
                            PromoScheduleType.START,
                            promoEntity.getPromo_type()
                    );
                } else if (promoEntity.getPromo_type().equals(PromoType.BXH.getKey())) {
                    /**
                     * Lưu lịch sử
                     *
                     */
                    this.insertPromoHistory(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            promoEntity.getEnd_date(),
                            JsonUtils.Serialize(promo_data),
                            "Duyệt",
                            promoEntity.getStatus(),
                            sessionData.getId());

                    /**
                     * 2. Nếu có ngày kết thúc thì đặt lịch kết thúc
                     */
                    if (promoEntity.getEnd_date() != null) {
                        this.createPromoSchedule(
                                promoEntity.getId(),
                                promoEntity.getEnd_date(),
                                PromoScheduleType.STOP);
                    }

                    CreatePromoRequest promo_data_runnning = this.convertPromoToData(promoEntity.getId());
                    ClientResponse crPromoRunning = this.updateCTXHRunning(
                            request.getId(),
                            promo_data_runnning,
                            "[]");
                    if (crPromoRunning.failed()) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadCTXHRunning(
                            request.getId(),
                            PromoScheduleType.START,
                            promoEntity.getPromo_type()
                    );
                } else {
                    /**
                     * Lưu lịch sử
                     *
                     */
                    this.insertPromoHistory(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            promoEntity.getEnd_date(),
                            JsonUtils.Serialize(promo_data),
                            "Duyệt",
                            promoEntity.getStatus(),
                            sessionData.getId());

                    /**
                     * 2. Nếu có ngày kết thúc thì đặt lịch kết thúc
                     */
                    if (promoEntity.getEnd_date() != null) {
                        this.createPromoSchedule(
                                promoEntity.getId(),
                                promoEntity.getEnd_date(),
                                PromoScheduleType.STOP);
                    }

                    PromoRunningEntity promoRunningEntity = this.promoDB.getPromoRunningById(request.getId());
                    if (promoRunningEntity == null) {
                        promoRunningEntity = new PromoRunningEntity();
                    }
                    promoRunningEntity.setPromo_id(request.getId());
                    promoRunningEntity.setPromo_data(JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())));
                    promoRunningEntity = this.promoDB.savePromoRunning(promoRunningEntity);
                    if (promoRunningEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadPromoRunning(
                            request.getId(),
                            PromoScheduleType.START,
                            promoEntity.getPromo_type()
                    );
                }
            } else {
                /**
                 * Lưu lịch sử
                 *
                 */
                this.insertPromoHistory(
                        promoEntity.getId(),
                        promoEntity.getStart_date(),
                        promoEntity.getEnd_date(),
                        JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())),
                        "Duyệt",
                        promoEntity.getStatus(),
                        sessionData.getId());

                /**
                 * 2. Nếu chính sách chờ chạy thì thêm vào danh sách hẹn giờ
                 */
                this.createPromoSchedule(
                        promoEntity.getId(),
                        promoEntity.getStart_date(),
                        PromoScheduleType.START);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse validateApprovePromo(PromoEntity promoEntity) {
        if (promoEntity.getEnd_date() != null
                && promoEntity.getEnd_date().getTime() < DateTimeUtils.getMilisecondsNow()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_END_INVALID);
        }

        if (promoEntity.getStatus() == PromoActiveStatus.RUNNING.getId()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_WAS_APPROVED);
        }

        if (promoEntity.getStatus() != PromoActiveStatus.DRAFT.getId()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
        }

        return ClientResponse.success(null);
    }

    /**
     * Thực thi lập lịch của chính sách
     */
    public ClientResponse runPromoSchedule() {
        try {
            List<JSONObject> promoScheduleEntityList = this.promoDB.getPromoScheduleWaiting(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject js : promoScheduleEntityList) {
                int promo_schedule_id = ConvertUtils.toInt(js.get("id"));
                LogUtil.printDebug(Module.PROMO.name() + ": run-" + promo_schedule_id);
                try {
                    ClientResponse clientResponse = ClientResponse.success(null);
                    PromoScheduleEntity promoScheduleEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(js), PromoScheduleEntity.class);
                    if (promoScheduleEntity != null) {
                        if (PromoScheduleType.STOP.getKey().equals(promoScheduleEntity.getSchedule_type())) {
                            PromoEntity oldPromoEntity = this.promoDB.getPromo(promoScheduleEntity.getPromo_id());
                            if (oldPromoEntity.getStatus() == PromoCTTLStatus.CANCEL.getId()) {
                                this.promoDB.updatePromoScheduleStatus(
                                        promo_schedule_id,
                                        ProcessStatus.FAIL.getValue(),
                                        "PROMO đã hủy");
                                continue;
                            }
                            boolean rsUpdatePromoStatus = this.promoDB.updatePromoStatus(promoScheduleEntity.getPromo_id(), PromoActiveStatus.STOPPED.getId());
                            if (!rsUpdatePromoStatus) {
                                this.alertToTelegram(
                                        "Dừng chính sách thất bại: " + oldPromoEntity.getCode(),
                                        ResponseStatus.FAIL
                                );
                                continue;
                            }
                            CreatePromoRequest promoData = this.convertPromoToData(oldPromoEntity.getId());
                            promoData.getPromo_info().setStatus(PromoActiveStatus.STOPPED.getId());

                            /**
                             * Lưu lịch sử
                             *
                             */
                            this.insertPromoHistory(
                                    oldPromoEntity.getId(),
                                    oldPromoEntity.getStart_date(),
                                    oldPromoEntity.getEnd_date(),
                                    JsonUtils.Serialize(promoData),
                                    "Dừng chính sách theo lịch hẹn",
                                    PromoActiveStatus.STOPPED.getId(),
                                    0);

                            if (PromoType.SALE_POLICY.getKey().equals(oldPromoEntity.getPromo_type()) ||
                                    PromoType.CTSS.getKey().equals(oldPromoEntity.getPromo_type()) ||
                                    PromoType.PROMO.getKey().equals(oldPromoEntity.getPromo_type())) {
                                clientResponse = this.removePromoRunning(promoScheduleEntity.getPromo_id());

                                this.dataManager.getProgramManager().reloadPromoRunning(
                                        promoScheduleEntity.getPromo_id(),
                                        PromoScheduleType.STOP,
                                        oldPromoEntity.getPromo_type()
                                );
                            } else if (PromoType.DAMME.getKey().equals(oldPromoEntity.getPromo_type())) {
                                clientResponse = this.removePromoRunning(promoScheduleEntity.getPromo_id());

                                this.dataManager.getProgramManager().reloadDamMeRunning(
                                        promoScheduleEntity.getPromo_id(),
                                        PromoScheduleType.STOP,
                                        oldPromoEntity.getPromo_type()
                                );
                            } else if (PromoType.CTTL.getKey().equals(oldPromoEntity.getPromo_type())) {
                                this.updatePromoRunning(
                                        oldPromoEntity.getId(),
                                        promoData
                                );

                                this.dataManager.getProgramManager().reloadPromoRunning(
                                        promoScheduleEntity.getPromo_id(),
                                        PromoScheduleType.STOP,
                                        oldPromoEntity.getPromo_type()
                                );

                                this.calculateRewardCTTL(promoScheduleEntity.getPromo_id());
                            } else if (PromoType.BXH.getKey().equals(oldPromoEntity.getPromo_type())) {
                                this.stopCTXHRunning(
                                        oldPromoEntity.getId(),
                                        promoData
                                );

                                this.dataManager.getProgramManager().reloadCTXHRunning(
                                        promoScheduleEntity.getPromo_id(),
                                        PromoScheduleType.STOP,
                                        oldPromoEntity.getPromo_type()
                                );

                                this.calculateRewardCTXH(promoScheduleEntity.getPromo_id());
                            }
                        } else if (PromoScheduleType.START.getKey().equals(promoScheduleEntity.getSchedule_type())) {
                            PromoEntity oldPromoEntity = this.promoDB.getPromo(promoScheduleEntity.getPromo_id());
                            if (PromoActiveStatus.RUNNING.getId() == oldPromoEntity.getStatus()
                                    || PromoActiveStatus.WAITING.getId() == oldPromoEntity.getStatus()) {
                                if (PromoActiveStatus.RUNNING.getId() == oldPromoEntity.getStatus()) {
                                    PromoHistoryEntity promoHistoryEntity = this.promoDB.getLastPromoHistory(promoScheduleEntity.getPromo_id());
                                    if (promoHistoryEntity != null && promoHistoryEntity.getStatus() == PromoActiveStatus.WAITING.getId()) {
                                        /**
                                         * Xóa promo cũ
                                         */
                                        boolean rsDeletePromoPre = this.deletePromoPre(promoScheduleEntity.getPromo_id());

                                        /**
                                         * Cập nhật dự liệu vào thông tin chính sách hiện tại
                                         */
                                        CreatePromoRequest promoData = JsonUtils.DeSerialize(promoHistoryEntity.getPromo_data(), CreatePromoRequest.class);
                                        PromoEntity promoEntity = this.createPromoEntity(promoData.getPromo_info());
                                        promoEntity.setId(oldPromoEntity.getId());
                                        promoEntity.setCode(oldPromoEntity.getCode());
                                        promoEntity.setStart_date(new Date(promoData.getPromo_info().getStart_date_millisecond()));
                                        promoEntity.setEnd_date(promoData.getPromo_info().getEnd_date_millisecond() == 0 ? null : new Date(promoData.getPromo_info().getEnd_date_millisecond()));
                                        promoEntity.setPromo_type(promoData.getPromo_info().getPromo_type());
                                        promoEntity.setCondition_type(promoData.getPromo_info().getCondition_type());
                                        promoEntity.setOffer_info(this.createOfferInfo(promoData.getPromo_limits()));
                                        promoEntity.setStatus(oldPromoEntity.getStatus());

                                        promoData.getPromo_info().setStart_date(
                                                DateTimeUtils.toString(promoEntity.getStart_date()));
                                        promoData.getPromo_info().setEnd_date(promoEntity.getEnd_date() == null ?
                                                null : DateTimeUtils.toString(promoEntity.getEnd_date()));
                                        clientResponse = this.updatePromoNew(promoEntity, promoData);

                                        promoData.getPromo_info().setStatus(PromoActiveStatus.RUNNING.getId());

                                        this.promoDB.updatePromoStatus(promoScheduleEntity.getPromo_id(), PromoActiveStatus.RUNNING.getId());

                                        /**
                                         * Cập nhật lịch sử chính sách
                                         */
                                        boolean rsSavePromoHistory = this.insertPromoHistory(
                                                promoEntity.getId(),
                                                promoEntity.getStart_date(),
                                                promoEntity.getEnd_date(),
                                                JsonUtils.Serialize(promoData),
                                                PromoActiveStatus.RUNNING.getLabel(),
                                                PromoActiveStatus.RUNNING.getId(),
                                                this.dataManager.getStaffManager().getStaffSystemId());

                                        /**
                                         * Tạo lịch kết thúc nếu có
                                         */
                                        if (promoEntity.getEnd_date() != null) {
                                            this.createPromoSchedule(
                                                    promoEntity.getId(),
                                                    promoEntity.getEnd_date(),
                                                    PromoScheduleType.STOP);
                                        }

                                        if (PromoType.SALE_POLICY.getKey().equals(oldPromoEntity.getPromo_type()) ||
                                                PromoType.CTSS.getKey().equals(oldPromoEntity.getPromo_type()) ||
                                                PromoType.PROMO.getKey().equals(oldPromoEntity.getPromo_type())) {
                                            this.updatePromoRunning(
                                                    oldPromoEntity.getId(),
                                                    promoData
                                            );

                                            this.dataManager.getProgramManager().reloadPromoRunning(
                                                    promoScheduleEntity.getPromo_id(),
                                                    PromoScheduleType.START,
                                                    oldPromoEntity.getPromo_type()
                                            );
                                        } else if (PromoType.CTTL.getKey().equals(oldPromoEntity.getPromo_type())) {
                                            this.updatePromoRunning(
                                                    oldPromoEntity.getId(),
                                                    promoData
                                            );

                                            this.dataManager.getProgramManager().reloadPromoRunning(
                                                    promoScheduleEntity.getPromo_id(),
                                                    PromoScheduleType.START,
                                                    oldPromoEntity.getPromo_type()
                                            );
                                        } else if (PromoType.DAMME.getKey().equals(oldPromoEntity.getPromo_type())) {
                                            this.updateDamMeRunning(
                                                    oldPromoEntity.getId(),
                                                    promoData
                                            );

                                            this.dataManager.getProgramManager().reloadDamMeRunning(
                                                    promoScheduleEntity.getPromo_id(),
                                                    PromoScheduleType.START,
                                                    oldPromoEntity.getPromo_type()
                                            );
                                        } else if (PromoType.BXH.getKey().equals(oldPromoEntity.getPromo_type())) {
                                            this.updateCTXHRunning(
                                                    oldPromoEntity.getId(),
                                                    promoData,
                                                    "[]"
                                            );

                                            this.dataManager.getProgramManager().reloadCTXHRunning(
                                                    promoScheduleEntity.getPromo_id(),
                                                    PromoScheduleType.START,
                                                    oldPromoEntity.getPromo_type()
                                            );
                                        }
                                    }
                                } else {
                                    CreatePromoRequest promoData = this.convertPromoToData(oldPromoEntity.getId());
                                    promoData.getPromo_info().setStatus(PromoActiveStatus.RUNNING.getId());
                                    boolean rsSavePromoHistory = this.insertPromoHistory(
                                            oldPromoEntity.getId(),
                                            oldPromoEntity.getStart_date(),
                                            oldPromoEntity.getEnd_date(),
                                            JsonUtils.Serialize(promoData),
                                            PromoActiveStatus.RUNNING.getLabel(),
                                            PromoActiveStatus.RUNNING.getId(),
                                            this.dataManager.getStaffManager().getStaffSystemId());

                                    if (oldPromoEntity.getEnd_date() != null) {
                                        this.createPromoSchedule(
                                                oldPromoEntity.getId(),
                                                oldPromoEntity.getEnd_date(),
                                                PromoScheduleType.STOP);
                                    }

                                    /**
                                     * Lưu trạng thái
                                     */
                                    this.promoDB.updatePromoStatus(promoScheduleEntity.getPromo_id(), PromoActiveStatus.RUNNING.getId());

                                    if (PromoType.SALE_POLICY.getKey().equals(oldPromoEntity.getPromo_type()) ||
                                            PromoType.PROMO.getKey().equals(oldPromoEntity.getPromo_type()) ||
                                            PromoType.CTSS.getKey().equals(oldPromoEntity.getPromo_type())) {
                                        this.updatePromoRunning(
                                                oldPromoEntity.getId(),
                                                promoData
                                        );

                                        this.dataManager.getProgramManager().reloadPromoRunning(
                                                promoScheduleEntity.getPromo_id(),
                                                PromoScheduleType.START,
                                                oldPromoEntity.getPromo_type()
                                        );
                                    } else if (PromoType.DAMME.getKey().equals(oldPromoEntity.getPromo_type())) {
                                        this.updateDamMeRunning(
                                                oldPromoEntity.getId(),
                                                promoData
                                        );

                                        this.dataManager.getProgramManager().reloadDamMeRunning(
                                                promoScheduleEntity.getPromo_id(),
                                                PromoScheduleType.START,
                                                oldPromoEntity.getPromo_type()
                                        );
                                    } else if (PromoType.CTTL.getKey().equals(oldPromoEntity.getPromo_type())) {
                                        this.updatePromoRunning(
                                                oldPromoEntity.getId(),
                                                promoData
                                        );

                                        this.dataManager.getProgramManager().reloadPromoRunning(
                                                promoScheduleEntity.getPromo_id(),
                                                PromoScheduleType.START,
                                                oldPromoEntity.getPromo_type()
                                        );
                                    } else if (PromoType.BXH.getKey().equals(oldPromoEntity.getPromo_type())) {
                                        this.updateCTXHRunning(
                                                oldPromoEntity.getId(),
                                                promoData,
                                                "[]"
                                        );

                                        this.dataManager.getProgramManager().reloadCTXHRunning(
                                                promoScheduleEntity.getPromo_id(),
                                                PromoScheduleType.START,
                                                oldPromoEntity.getPromo_type()
                                        );
                                    }
                                }
                            } else {
                                clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
                            }
                        }
                        if (clientResponse.failed()) {
                            this.promoDB.updatePromoScheduleStatus(
                                    promo_schedule_id,
                                    ProcessStatus.FAIL.getValue(),
                                    clientResponse.getMessage());
                        } else {
                            this.promoDB.updatePromoScheduleStatus(
                                    promo_schedule_id,
                                    ProcessStatus.SUCCESS.getValue(),
                                    ResponseMessage.SUCCESS.getValue());
                        }
                    }
                } catch (Exception ex) {
                    LogUtil.printDebug(Module.PROMO.name(), ex);
                    this.promoDB.updatePromoScheduleStatus(
                            promo_schedule_id,
                            ProcessStatus.FAIL.getValue(),
                            ResponseMessage.FAIL.getValue());
                }
                LogUtil.printDebug(Module.PROMO.name() + ": done-" + promo_schedule_id);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.success(null);
    }

    /**
     * Lưu chính sách vào danh sách chính sách đang chạy
     *
     * @param promo_id
     * @return
     */
    private ClientResponse createPromoRunning(int promo_id) {
        PromoRunningEntity promoRunningEntity = this.promoDB.getPromoRunningById(promo_id);
        if (promoRunningEntity == null) {
            promoRunningEntity = new PromoRunningEntity();
        }
        promoRunningEntity.setPromo_id(promo_id);
        CreatePromoRequest promo_data = this.convertPromoToData(promo_id);
        promoRunningEntity.setPromo_data(
                JsonUtils.Serialize(promo_data))
        ;
        promoRunningEntity = this.promoDB.savePromoRunning(promoRunningEntity);
        if (promoRunningEntity == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        /**
         * reload cache promo
         */
        this.dataManager.getProgramManager().reloadPromoRunning(
                promo_id,
                PromoScheduleType.START,
                promo_data.getPromo_info().getPromo_type()
        );

        return ClientResponse.success(null);
    }

    /**
     * Thực thi lệnh dừng chính sách
     *
     * @param promoScheduleEntity
     * @return
     */
    private ClientResponse runPromoScheduleStop(PromoScheduleEntity promoScheduleEntity) {
        try {
            PromoEntity promoEntity = this.promoDB.getPromo(promoScheduleEntity.getId());
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            CreatePromoRequest promoData = this.convertPromoToData(promoEntity.getId());

            /**
             * Lưu lịch sử
             *
             */
            this.insertPromoHistory(
                    promoEntity.getId(),
                    promoEntity.getStart_date(),
                    promoEntity.getEnd_date(),
                    JsonUtils.Serialize(promoData),
                    "Dừng chính sách theo lịch hẹn",
                    promoEntity.getStatus(),
                    0);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Chuyển đổi chính sách sang json
     *
     * @param promo_id
     * @return
     */
    private CreatePromoRequest convertPromoToData(int promo_id) {
        CreatePromoRequest request = new CreatePromoRequest();
        PromoEntity promoEntity = this.promoDB.getPromoInfo(promo_id);
        /**
         * promo info
         */
        request.setPromo_info(this.convertPromoInfo(promoEntity));

        if (promoEntity.getRepeat_type() != 0) {
            request.setRepeat_data_info(
                    promoEntity.getRepeat_data());
        } else {
            request.setRepeat_data_info(
                    JsonUtils.Serialize(new RepeatDataRequest())
            );
        }

        if (promoEntity.getOrder_date_data() != null &&
                !promoEntity.getOrder_date_data().isEmpty()) {
            request.setOrder_date_data_info(
                    promoEntity.getOrder_date_data()
            );
            request.setOrder_date_data(
                    JsonUtils.DeSerialize(
                            promoEntity.getOrder_date_data(),
                            PromoTimeRequest.class
                    )
            );
        }

        if (promoEntity.getPayment_date_data() != null &&
                !promoEntity.getPayment_date_data().isEmpty()) {
            request.setPayment_date_data_info(
                    promoEntity.getPayment_date_data()
            );
            request.setPayment_date_data(
                    JsonUtils.DeSerialize(
                            promoEntity.getPayment_date_data(),
                            PromoTimeRequest.class
                    )
            );
        }

        if (promoEntity.getReward_date_data() != null &&
                !promoEntity.getReward_date_data().isEmpty()) {
            request.setReward_date_data_info(
                    promoEntity.getReward_date_data()
            );
            request.setReward_date_data(
                    JsonUtils.DeSerialize(
                            promoEntity.getReward_date_data(),
                            PromoTimeRequest.class
                    )
            );
        }

        if (promoEntity.getConfirm_result_date_data() != null &&
                !promoEntity.getConfirm_result_date_data().isEmpty()) {
            request.setConfirm_result_date_data_info(
                    promoEntity.getConfirm_result_date_data()
            );
            request.setConfirm_result_date_data(
                    JsonUtils.DeSerialize(
                            promoEntity.getConfirm_result_date_data(),
                            PromoTimeRequest.class
                    )
            );
        }

        /**
         * promo apply object
         */
        request.setPromo_apply_object(this.convertPromoApplyObject(promo_id));

        /**
         * promo item group
         */
        request.setPromo_item_groups(this.convertPromoItemGroup(promo_id));

        /**
         * promo item ignore
         */
        request.setPromo_item_ignores(this.convertPromoItemIgnore(promo_id));

        /**
         * promo limit - offer
         */
        request.setPromo_limits(this.convertPromoLimit(promo_id, request.getPromo_item_groups()));

        /**
         * promo structure
         */
        request.setPromo_structure(
                this.convertPromoStructure(promo_id)
        );
        return request;
    }

    private PromoStructureRequest convertPromoStructure(int promo_id) {
        PromoStructureRequest request = new PromoStructureRequest();
        try {
            JSONObject jsPromoStructure = this.promoDB.getPromoStructure(promo_id);
            if (jsPromoStructure == null) {
                return request;
            }

            request.setPromo_lien_ket_list(
                    JsonUtils.DeSerialize(
                            jsPromoStructure.get("promo_lien_ket").toString(),
                            new TypeToken<List<String>>() {
                            }.getType())
            );

            request.setPromo_dong_thoi_list(
                    JsonUtils.DeSerialize(
                            jsPromoStructure.get("promo_dong_thoi").toString(),
                            new TypeToken<List<String>>() {
                            }.getType())
            );

            request.setPromo_dong_thoi_tru_gtdtt_list(
                    JsonUtils.DeSerialize(
                            jsPromoStructure.get("promo_dong_thoi_tru_gtdtt").toString(),
                            new TypeToken<List<String>>() {
                            }.getType())
            );

            request.setPromo_loai_tru_list(
                    JsonUtils.DeSerialize(
                            jsPromoStructure.get("promo_loai_tru").toString(),
                            new TypeToken<List<String>>() {
                            }.getType())
            );

            request.setIs_ignore_all_csbh(
                    ConvertUtils.toInt(jsPromoStructure.get("is_ignore_all_csbh"))
            );

            request.setIs_ignore_all_ctkm(
                    ConvertUtils.toInt(jsPromoStructure.get("is_ignore_all_ctkm"))
            );

            List<JSONObject> loai_tru =
                    this.promoDB.getPromoJSListInPromoIds(
                            jsPromoStructure.get("promo_loai_tru").toString()
                    );
            for (JSONObject promoJs : loai_tru) {
                if (ConvertUtils.toString(promoJs.get("promo_type")).equals(PromoType.SALE_POLICY.getKey())) {
                    request.getPromo_loai_tru_csbh_list().add(
                            promoJs
                    );
                } else if (ConvertUtils.toString(promoJs.get("promo_type")).equals(PromoType.PROMO.getKey())) {
                    request.getPromo_loai_tru_ctkm_list().add(
                            promoJs
                    );
                } else if (ConvertUtils.toString(promoJs.get("promo_type")).equals(PromoType.CTSS.getKey())) {
                    request.getPromo_loai_tru_ctss_list().add(
                            promoJs
                    );
                } else if (ConvertUtils.toString(promoJs.get("promo_type")).equals(PromoType.CTTL.getKey())) {
                    request.getPromo_loai_tru_cttl_list().add(
                            promoJs
                    );
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return request;
    }

    private PromoApplyObjectRequest convertPromoApplyObject(int promo_id) {
        PromoApplyObjectRequest promoApplyObjectRequest = new PromoApplyObjectRequest();
        PromoApplyObjectEntity promoApplyObjectEntity = this.promoDB.getPromoApplyObject(promo_id);
        if (promoApplyObjectEntity == null) {
            return promoApplyObjectRequest;
        }

        promoApplyObjectRequest.setPromo_agency_includes(
                JsonUtils.DeSerialize(promoApplyObjectEntity.getPromo_agency_include(), new TypeToken<List<AgencyBasicData>>() {
                }.getType()));
        for (AgencyBasicData agencyBasicData : promoApplyObjectRequest.getPromo_agency_includes()) {
            AgencyEntity agencyEntity = this.dataManager.getAgencyManager().getAgency(agencyBasicData.getId());
            if (agencyEntity != null) {
                agencyBasicData.initInfo(agencyEntity);
            }
        }

        promoApplyObjectRequest.setPromo_agency_ignores(
                JsonUtils.DeSerialize(promoApplyObjectEntity.getPromo_agency_ignore(), new TypeToken<List<AgencyBasicData>>() {
                }.getType()));
        for (AgencyBasicData agencyBasicData : promoApplyObjectRequest.getPromo_agency_ignores()) {
            AgencyEntity agencyEntity = this.dataManager.getAgencyManager().getAgency(agencyBasicData.getId());
            if (agencyEntity != null) {
                agencyBasicData.initInfo(agencyEntity);
            }
        }

        List<JSONObject> promo_filters = this.promoDB.getPromoFilterList(promo_id);
        for (JSONObject promoFilter : promo_filters) {
            PromoApplyFilterRequest promoApplyFilterRequest = new PromoApplyFilterRequest();
            promoApplyFilterRequest.setFilter_types(
                    JsonUtils.DeSerialize(promoFilter.get("filter_types").toString(), new TypeToken<List<PromoApplyFilterDetailRequest>>() {
                    }.getType()));
            promoApplyObjectRequest.getPromo_filters().add(promoApplyFilterRequest);
        }

        /**
         * Điều kiện đặt đơn hàng
         */
        if (promoApplyObjectEntity.getPromo_sufficient_condition() != null &&
                !promoApplyObjectEntity.getPromo_sufficient_condition().isEmpty()) {
            promoApplyObjectRequest.setPromo_sufficient_conditions(
                    JsonUtils.DeSerialize(promoApplyObjectEntity.getPromo_sufficient_condition(),
                            new TypeToken<List<PromoApplyFilterRequest>>() {
                            }.getType())
            );
        }

        return promoApplyObjectRequest;
    }

    private List<PromoItemIgnoreRequest> convertPromoItemIgnore(int promo_id) {
        try {
            List<PromoItemIgnoreRequest> promoItemIgnoreRequestList = new ArrayList<>();
            List<JSONObject> promoItemIgnoreEntityList = this.promoDB.getPromoItemIgnoreList(promo_id);

            for (JSONObject promoItemGroupEntity : promoItemIgnoreEntityList) {
                PromoItemIgnoreRequest promoItemGroupDetailRequest = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(promoItemGroupEntity), PromoItemIgnoreRequest.class);

                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(promoItemGroupDetailRequest.getItem_id());
                if (productCache != null) {
                    promoItemGroupDetailRequest.setItem_name(
                            productCache.getFull_name()
                    );
                    promoItemGroupDetailRequest.setItem_code(
                            productCache.getCode()
                    );
                    promoItemGroupDetailRequest.setItem_price(
                            productCache.getPrice()
                    );
                }
                promoItemIgnoreRequestList.add(promoItemGroupDetailRequest);
            }
            return promoItemIgnoreRequestList;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return new ArrayList<>();
    }

    private List<PromoItemIgnoreRequest> convertPromoItemIgnore(List<PromoItemIgnoreRequest> promoItemIgnoreRequests) {
        try {
            for (PromoItemIgnoreRequest promoItemGroupDetailRequest : promoItemIgnoreRequests) {
                promoItemGroupDetailRequest.setItem_name(this.dataManager.getProductManager().getProductFullName(promoItemGroupDetailRequest.getItem_id()));
                promoItemGroupDetailRequest.setItem_code(this.dataManager.getProductManager().getProductCode(promoItemGroupDetailRequest.getItem_id()));
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return promoItemIgnoreRequests;
    }

    /**
     * Chuyển đổi hạn mức của chính sách
     *
     * @param promo_id
     * @return
     */

    private List<PromoLimitRequest> convertPromoLimit(int promo_id, List<PromoItemGroupRequest> promo_item_groups) {
        try {
            List<PromoLimitRequest> promoLimitRequestList = new ArrayList<>();
            List<JSONObject> promoLimitList = this.promoDB.getPromoLimitList(promo_id);
            for (JSONObject promoLimit : promoLimitList) {
                PromoLimitRequest promoLimitRequest = JsonUtils.DeSerialize(JsonUtils.Serialize(promoLimit), PromoLimitRequest.class);
                promoLimitRequestList.add(promoLimitRequest);

                List<JSONObject> promoLimitGroupList = this.promoDB.getPromoLimitGroupList(promoLimitRequest.getId());
                for (int iGroup = 0; iGroup < promoLimitGroupList.size(); iGroup++) {
                    JSONObject promoLimitGroup = promoLimitGroupList.get(iGroup);

                    PromoLimitGroupRequest promoLimitGroupRequest = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(promoLimitGroup), PromoLimitGroupRequest.class
                    );

                    if (promo_item_groups.size() > 0) {
                        promoLimitGroupRequest.setGroup_info(this.convertGroupInfo(
                                promo_item_groups.get(iGroup)
                        ));
                    }

                    JSONObject promoOffer = this.promoDB.getPromoOffer(promoLimitGroupRequest.getId());
                    PromoOfferRequest promoOfferRequest = JsonUtils.DeSerialize(JsonUtils.Serialize(promoOffer), PromoOfferRequest.class);
                    if (promoOffer.get("voucher_data") != null && !promoOffer.get("voucher_data").toString().isEmpty()) {
                        List<JSONObject> voucher_data = JsonUtils.DeSerialize(promoOffer.get("voucher_data").toString(),
                                new TypeToken<List<JSONObject>>() {
                                }.getType());
                        for (JSONObject voucher : voucher_data) {
                            JSONObject jsVRP = this.promoDB.getVoucherReleasePeriod(ConvertUtils.toInt(voucher.get("id")));
                            if (jsVRP != null) {
                                jsVRP.put("expire_day", voucher.get("expire_day"));
                                promoOfferRequest.getOffer_voucher_info().add(jsVRP);
                            }
                        }

                        promoOfferRequest.setVoucher_data(JsonUtils.Serialize(promoOfferRequest.getOffer_voucher_info()));
                        promoOfferRequest.setOffer_voucher_release_periods(voucher_data);
                    }
                    /**
                     * Tặng hàng/tặng quà/chiết khấu từng sản phẩm/giảm tiền từng sản phẩm
                     */
                    List<JSONObject> promoOfferProductList = this.promoDB.getPromoOfferProductList(promoOfferRequest.getId());
                    for (JSONObject promoOfferProduct : promoOfferProductList) {
                        PromoOfferProductRequest promoOfferProductRequest = JsonUtils.DeSerialize(
                                JsonUtils.Serialize(promoOfferProduct), PromoOfferProductRequest.class
                        );
                        promoOfferProductRequest.setProduct_name(this.dataManager.getProductManager().getProductFullName(promoOfferProductRequest.getProduct_id()));
                        promoOfferProductRequest.setProduct_code(this.dataManager.getProductManager().getProductCode(promoOfferProductRequest.getProduct_id()));
                        promoOfferRequest.getOffer_products().add(promoOfferProductRequest);
                    }

                    /**
                     * Hàng tặng kèm/quà tặng kèm
                     */
                    List<JSONObject> promoOfferBonusList = this.promoDB.getPromoOfferBonusList(promoOfferRequest.getId());
                    for (JSONObject promoOfferBonus : promoOfferBonusList) {
                        PromoOfferBonusRequest promoOfferBonusRequest = JsonUtils.DeSerialize(
                                JsonUtils.Serialize(promoOfferBonus), PromoOfferBonusRequest.class
                        );
                        promoOfferBonusRequest.setProduct_name(this.dataManager.getProductManager().getProductFullName(promoOfferBonusRequest.getProduct_id()));
                        promoOfferBonusRequest.setProduct_code(this.dataManager.getProductManager().getProductCode(promoOfferBonusRequest.getProduct_id()));
                        promoOfferRequest.getOffer_bonus().add(promoOfferBonusRequest);
                    }

                    promoLimitGroupRequest.setOffer(promoOfferRequest);
                    promoLimitRequest.getPromo_limit_groups().add(promoLimitGroupRequest);
                }
            }
            return promoLimitRequestList;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return new ArrayList<>();
    }

    private JSONObject convertGroupInfo(PromoItemGroupRequest promoItemGroupRequest) {
        try {
            JSONObject groupInfo = new JSONObject();
            groupInfo.put("id", promoItemGroupRequest.getId());
            groupInfo.put("code", promoItemGroupRequest.getCode());
            groupInfo.put("full_name", promoItemGroupRequest.getFull_name());
            groupInfo.put("images", promoItemGroupRequest.getImages());
            groupInfo.put("combo_id", promoItemGroupRequest.getCombo_id());
            groupInfo.put("max_offer_per_promo", promoItemGroupRequest.getMax_offer_per_promo());
            groupInfo.put("max_offer_per_agency", promoItemGroupRequest.getMax_offer_per_agency());
            return groupInfo;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return null;
    }

    private List<PromoLimitRequest> convertPromoLimit(List<PromoLimitRequest> promoLimitRequests) {
        try {
            for (PromoLimitRequest promoLimitRequest : promoLimitRequests) {
                for (PromoLimitGroupRequest promoLimitGroupRequest : promoLimitRequest.getPromo_limit_groups()) {
                    /**
                     * Tặng hàng/tặng quà/chiết khấu từng sản phẩm/giảm tiền từng sản phẩm
                     */
                    for (PromoOfferProductRequest promoOfferProductRequest : promoLimitGroupRequest.getOffer().getOffer_products()) {
                        promoOfferProductRequest.setProduct_name(this.dataManager.getProductManager().getProductFullName(promoOfferProductRequest.getProduct_id()));
                        promoOfferProductRequest.setProduct_code(this.dataManager.getProductManager().getProductCode(promoOfferProductRequest.getProduct_id()));
                    }

                    /**
                     * Hàng tặng kèm/quà tặng kèm
                     */
                    for (PromoOfferBonusRequest promoOfferBonusRequest : promoLimitGroupRequest.getOffer().getOffer_bonus()) {
                        promoOfferBonusRequest.setProduct_name(this.dataManager.getProductManager().getProductFullName(promoOfferBonusRequest.getProduct_id()));
                        promoOfferBonusRequest.setProduct_code(this.dataManager.getProductManager().getProductCode(promoOfferBonusRequest.getProduct_id()));
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return promoLimitRequests;
    }

    private List<PromoItemGroupRequest> convertPromoItemGroup(int promo_id) {
        try {
            List<PromoItemGroupRequest> promoItemGroupRequestList = new ArrayList<>();
            List<JSONObject> promoItemGroupEntityList = this.promoDB.getPromoItemGroupList(promo_id);

            for (JSONObject promoItemGroupEntity : promoItemGroupEntityList) {
                PromoItemGroupRequest promoItemGroupRequest = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(promoItemGroupEntity), PromoItemGroupRequest.class);
                if (promoItemGroupRequest.getType().equals(ItemGroupType.COMBO.getCode())) {
                    JSONObject combo = this.promoDB.getComboInfo(
                            ConvertUtils.toInt(promoItemGroupEntity.get("combo_id"))
                    );
                    promoItemGroupRequest.setCode(
                            combo == null ? "" : ConvertUtils.toString(combo.get("code"))
                    );
                    promoItemGroupRequest.setFull_name(
                            combo == null ? "" : ConvertUtils.toString(combo.get("full_name"))
                    );
                    promoItemGroupRequest.setImages(
                            combo == null ? "" : ConvertUtils.toString(combo.get("images"))
                    );
                }
                List<JSONObject> promoItemGroupDetailEntityList = this.promoDB.getPromoItemGroupDetailList(
                        ConvertUtils.toInt(promoItemGroupEntity.get("id"))
                );

                for (JSONObject promoItemGroupDetailEntity : promoItemGroupDetailEntityList) {
                    PromoItemGroupDetailRequest promoItemGroupDetailRequest = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(promoItemGroupDetailEntity), PromoItemGroupDetailRequest.class);
                    if (promoItemGroupDetailRequest.getItem_type() == null ||
                            promoItemGroupDetailRequest.getItem_type().isEmpty()
                    ) {
                        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(promoItemGroupDetailRequest.getItem_id());
                        if (productCache != null) {
                            promoItemGroupDetailRequest.setItem_name(
                                    productCache.getFull_name()
                            );
                            promoItemGroupDetailRequest.setItem_code(
                                    productCache.getCode()
                            );
                            promoItemGroupDetailRequest.setItem_price(
                                    productCache.getPrice()
                            );
                        }
                    } else if (promoItemGroupDetailRequest.getItem_type().equals(DamMeProductType.PRODUCT.getKey())) {
                        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(promoItemGroupDetailRequest.getItem_id());
                        if (productCache != null) {
                            promoItemGroupDetailRequest.setItem_name(
                                    productCache.getFull_name()
                            );
                            promoItemGroupDetailRequest.setItem_code(
                                    productCache.getCode()
                            );
                            promoItemGroupDetailRequest.setItem_price(
                                    productCache.getPrice()
                            );
                        }
                    } else if (promoItemGroupDetailRequest.getItem_type().equals(DamMeProductType.PRODUCT_GROUP.getKey())) {
                        ProductGroup productGroup = this.dataManager.getProductManager().getMpProductGroup().get(
                                promoItemGroupDetailRequest.getItem_id()
                        );
                        if (productGroup != null) {
                            promoItemGroupDetailRequest.setItem_name(
                                    productGroup.getName()
                            );
                            promoItemGroupDetailRequest.setItem_code(
                                    productGroup.getCode()
                            );
                        }
                    } else if (promoItemGroupDetailRequest.getItem_type().equals(DamMeProductType.CATEGORY.getKey())) {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                promoItemGroupDetailRequest.getItem_id()
                        );
                        if (category != null) {
                            promoItemGroupDetailRequest.setItem_name(
                                    category.getName()
                            );
                        }
                    }
                    promoItemGroupRequest.getProducts().add(promoItemGroupDetailRequest);

                    promoItemGroupRequest.setMax_offer_per_promo(
                            this.getMaxOfferPerPromo(promoItemGroupRequest.getProducts())
                    );
                    promoItemGroupRequest.setMax_offer_per_agency(
                            this.getMaxOfferPerAgency(promoItemGroupRequest.getProducts())
                    );
                }

                promoItemGroupRequestList.add(promoItemGroupRequest);
            }
            return promoItemGroupRequestList;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return new ArrayList<>();
    }

    private long getMaxOfferPerPromo(List<PromoItemGroupDetailRequest> products) {
        try {
            Integer max = 0;
            for (PromoItemGroupDetailRequest promoItemGroupDetailRequest : products) {
                if (promoItemGroupDetailRequest.getItem_quantity() == 0) {
                    return 0;
                }
                if (promoItemGroupDetailRequest.getMax_offer_per_promo() != 0 &&
                        (max == 0 ||
                                max > ConvertUtils.toInt(promoItemGroupDetailRequest.getMax_offer_per_promo() / promoItemGroupDetailRequest.getItem_quantity()))) {
                    max = ConvertUtils.toInt(promoItemGroupDetailRequest.getMax_offer_per_promo() / promoItemGroupDetailRequest.getItem_quantity());
                }
            }
            return max;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return 0;
    }

    private long getMaxOfferPerAgency(List<PromoItemGroupDetailRequest> products) {
        try {
            Integer max = 0;
            for (PromoItemGroupDetailRequest promoItemGroupDetailRequest : products) {
                if (promoItemGroupDetailRequest.getItem_quantity() == 0) {
                    return 0;
                }
                if (promoItemGroupDetailRequest.getMax_offer_per_agency() != 0 &&
                        (max == 0 ||
                                max > ConvertUtils.toInt(promoItemGroupDetailRequest.getMax_offer_per_agency() / promoItemGroupDetailRequest.getItem_quantity()))) {
                    max = ConvertUtils.toInt(promoItemGroupDetailRequest.getMax_offer_per_agency() / promoItemGroupDetailRequest.getItem_quantity());
                }
            }
            return max;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return 0;
    }

    private List<PromoItemGroupRequest> convertPromoItemGroup(List<PromoItemGroupRequest> promoItemGroupRequests) {
        try {
            for (PromoItemGroupRequest promoItemGroupRequest : promoItemGroupRequests) {
                for (PromoItemGroupDetailRequest promoItemGroupDetailRequest : promoItemGroupRequest.getProducts()) {
                    promoItemGroupDetailRequest.setItem_name(this.dataManager.getProductManager().getProductFullName(promoItemGroupDetailRequest.getItem_id()));
                    promoItemGroupDetailRequest.setItem_code(this.dataManager.getProductManager().getProductCode(promoItemGroupDetailRequest.getItem_id()));
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return promoItemGroupRequests;
    }

    private PromoInfoRequest convertPromoInfo(PromoEntity promoEntity) {
        PromoInfoRequest promoInfoRequest = JsonUtils.DeSerialize(JsonUtils.Serialize(promoEntity), PromoInfoRequest.class);
        promoInfoRequest.setStart_date_millisecond(DateTimeUtils.getDateTime(promoEntity.getStart_date()).getTime());
        promoInfoRequest.setStart_date(DateTimeUtils.toString(promoEntity.getStart_date()));
        promoInfoRequest.setEnd_date_millisecond(promoEntity.getEnd_date() == null ? 0 : DateTimeUtils.getDateTime(promoEntity.getEnd_date()).getTime());
        promoInfoRequest.setEnd_date(promoEntity.getEnd_date() == null ? null : DateTimeUtils.toString(promoEntity.getEnd_date()));
        return promoInfoRequest;
    }

    public ClientResponse deletePromo(SessionData sessionData, CancelPromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            PromoEntity promoEntity = this.promoDB.getPromo(request.getId());
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            /**
             * Chinh sách nháp mới được hủy
             */
            if (PromoActiveStatus.DRAFT.getId() != promoEntity.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            promoEntity.setStatus(PromoActiveStatus.DELETE.getId());

            boolean rsDeletePromo = this.promoDB.deletePromo(request.getId());
            if (!rsDeletePromo) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Lịch sử chỉnh sửa của chính sách
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse getPromoHistory(SessionData sessionData, GetPromoHistoryRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            List<JSONObject> promoHistoryList = this.promoDB.getPromoHistory(request.getId());
            for (JSONObject record : promoHistoryList) {
                record.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(record.get("creator_id"))
                ));
            }
            JSONObject data = new JSONObject();
            data.put("promo_histories", promoHistoryList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }


    public ClientResponse getPromoByProduct(SessionData sessionData, GetPromoByProductRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            List<PromoBasicData> promoBasicDataList = new ArrayList<>();
            Agency agency = this.dataManager.getProgramManager().getAgency(request.getAgency_id());
            if (this.getProductVisibilityByAgency(
                    request.getAgency_id(),
                    request.getProduct_id()) == VisibilityType.HIDE.getId()) {
                JSONObject data = new JSONObject();
                data.put("promos", promoBasicDataList);
                return ClientResponse.success(data);
            }

            if (agency != null) {
                DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
                List<PromoBasicData> promos = this.dataManager.getProgramManager().getPromoByProduct(request.getProduct_id());
                for (PromoBasicData promoBasicData : promos) {
                    Program programCSBH = this.dataManager.getProgramManager().getMpSalePolicy().get(promoBasicData.getId());
                    Program programCTKM = this.dataManager.getProgramManager().getMpPromotion().get(promoBasicData.getId());
                    //LogUtil.printDebug(JsonUtils.Serialize(program));
                    if (programCSBH != null && programCSBH.isRunning()) {
                        if (this.checkProgramFilter(
                                agency,
                                programCSBH,
                                Source.WEB,
                                deptInfo)) {
                            promoBasicData.setDescription(
                                    this.getCmsProgramDescriptionForProduct(
                                            agency,
                                            request.getProduct_id(),
                                            programCSBH));
                            promoBasicDataList.add(promoBasicData);
                        }
                    } else if (programCTKM != null && programCTKM.isRunning()) {
                        if (this.checkProgramFilter(
                                agency, programCTKM, Source.WEB, deptInfo)) {
                            promoBasicData.setDescription(
                                    this.getCmsProgramDescriptionForProduct(
                                            agency,
                                            request.getProduct_id(),
                                            programCTKM));
                            promoBasicDataList.add(promoBasicData);
                        }
                    }
                }
                JSONObject data = new JSONObject();
                data.put("promos", promoBasicDataList);
                return ClientResponse.success(data);
            } else {
                List<JSONObject> promos = this.promoDB.getListPromoWaitingByProduct(request.getProduct_id());
                JSONObject data = new JSONObject();
                data.put("promos", promos);
                return ClientResponse.success(data);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Chi tiết của lịch sử thay đổi chính sách
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse getPromoHistoryDetail(SessionData sessionData, BasicRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject promoHistoryDetail = this.promoDB.getPromoHistoryDetail(request.getId());
            if (promoHistoryDetail == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            CreatePromoRequest promo = JsonUtils.DeSerialize(promoHistoryDetail.get("promo_data").toString(), CreatePromoRequest.class);

            if (promo.getPromo_apply_object() != null) {
                for (AgencyBasicData agencyIgnore : promo.getPromo_apply_object().getPromo_agency_ignores()) {
                    AgencyBasicData agency = this.dataManager.getAgencyManager().getAgencyBasicData(agencyIgnore.getId());
                    agencyIgnore.initInfo(agency);
                }

                for (AgencyBasicData agencyInclude : promo.getPromo_apply_object().getPromo_agency_ignores()) {
                    AgencyBasicData agency = this.dataManager.getAgencyManager().getAgencyBasicData(agencyInclude.getId());
                    agencyInclude.initInfo(agency);
                }
            }
            JSONObject data = new JSONObject();

            data.put("promo", promo);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Danh sách chính sách
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filerPromoCTKM(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_PROMO_CTKM, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filerPromo(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 0);
            int total = this.promoDB.getTotalPromo(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse sortPromo(SortPromoRequest request, SessionData sessionData) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Remove Ưu tiên
             */
            List<JSONObject> oldPromoList = this.promoDB.getPromoCTKMPriorityList();
            for (JSONObject promo : oldPromoList) {
                int oldId = ConvertUtils.toInt(promo.get("id"));
                /**
                 *
                 */
                if (!request.getIds().contains(oldId)) {
                    boolean rsUpdatePromoPriority = this.promoDB.updatePromoPriority(oldId, 0, sessionData.getId());
                    if (!rsUpdatePromoPriority) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     *
                     */
                    if (PromoActiveStatus.RUNNING.getId() == ConvertUtils.toInt(promo.get("status"))) {
                        PromoRunningEntity promoRunningEntity = this.promoDB.getPromoRunningById(oldId);
                        if (promoRunningEntity == null) {
                            promoRunningEntity = new PromoRunningEntity();
                        }
                        promoRunningEntity.setPromo_id(oldId);
                        CreatePromoRequest promo_data = this.convertPromoToData(oldId);
                        promoRunningEntity.setPromo_data(JsonUtils.Serialize(promo_data));
                        promoRunningEntity = this.promoDB.savePromoRunning(promoRunningEntity);
                        if (promoRunningEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                        this.dataManager.getProgramManager().reloadPromoRunning(
                                oldId,
                                PromoScheduleType.START,
                                promo_data.getPromo_info().getPromo_type()
                        );
                    }
                }
            }

            for (int iPromo = 0; iPromo < request.getIds().size(); iPromo++) {
                PromoEntity promoEntity = this.promoDB.getPromo(request.getIds().get(iPromo));
                if (promoEntity != null) {
                    if (promoEntity.getPriority() == iPromo + 1) {
                        continue;
                    }
                    /**
                     * cập nhật thứ tự ưu tiên
                     */
                    boolean rsUpdatePromoPriority = this.promoDB.updatePromoPriority(request.getIds().get(iPromo), iPromo + 1, sessionData.getId());
                    if (!rsUpdatePromoPriority) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * nếu promo đang chạy
                     * cập nhật promo running
                     */
                    if (PromoActiveStatus.RUNNING.getId() == promoEntity.getStatus()) {
                        PromoRunningEntity promoRunningEntity = this.promoDB.getPromoRunningById(promoEntity.getId());
                        if (promoRunningEntity == null) {
                            promoRunningEntity = new PromoRunningEntity();
                        }
                        promoRunningEntity.setPromo_id(promoEntity.getId());
                        promoRunningEntity.setPromo_data(JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())));
                        promoRunningEntity = this.promoDB.savePromoRunning(promoRunningEntity);
                        if (promoRunningEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                        this.dataManager.getProgramManager().reloadPromoRunning(
                                request.getIds().get(iPromo),
                                PromoScheduleType.START,
                                promoEntity.getPromo_type()
                        );
                    }
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterPromoByAgency(SessionData sessionData, FilterListByIdRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            List<JSONObject> promoBasicDataList = new ArrayList<>();
            Agency agency = this.dataManager.getProgramManager().getAgency(request.getId());
            if (agency != null) {
                DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
                request.setIsLimit(0);
                String query = this.filterUtils.getQuery(
                        FunctionList.LIST_PROMO_WAITING,
                        request.getFilters(),
                        request.getSorts()
                );
                List<JSONObject> records = this.promoDB.filter(query,
                        this.appUtils.getOffset(request.getPage()),
                        ConfigInfo.PAGE_SIZE,
                        request.getIsLimit());
                for (JSONObject record : records) {
                    Program program = this.getProgramById(
                            ConvertUtils.toInt(record.get("id"))
                    );
                    if (program == null) {
                        continue;
                    }
                    int allow = this.checkProgramFilter(
                            agency,
                            program,
                            Source.WEB,
                            deptInfo) == true ? 1 : 2;
                    JSONObject promo = JsonUtils.DeSerialize(
                            this.dataManager.getProgramManager().getMpPromoRunning().get(program.getId()),
                            JSONObject.class);
                    if (promo != null
                            && (request.getAllow() == 0 || request.getAllow() == allow)
                            && allow == 1) {
                        record.put("allow", allow);
                        promoBasicDataList.add(record);
                    }
                }
            }
            JSONObject data = new JSONObject();
            data.put("promos", promoBasicDataList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse doubleCheckPromo(SessionData sessionData, CreatePromoRequest request) {
        try {
            LogUtil.printDebug(JsonUtils.Serialize(request));
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Ràng buộc tạo chính sách
             */
            clientResponse = this.validateCreatePromo(request);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Kiểm tra CTTL Liên kết
             */
            int group_id = 0;
            if (request.getPromo_info().getId() != 0) {
                JSONObject jsGroup = this.promoDB.getPromoLinkGroupByPromoId(request.getPromo_info().getId());
                if (jsGroup != null) {
                    group_id = ConvertUtils.toInt(jsGroup.get("group_id"));
                }
            }
            if (request.getPromo_info().getPromo_type().equals(PromoType.CTTL.getKey())) {
                if (!request.getPromo_structure().getPromo_lien_ket_list().isEmpty()) {
                    List<JSONObject> promoLinkList = this.promoDB.getListPromoLink(
                            JsonUtils.Serialize(request.getPromo_structure().getPromo_lien_ket_list())
                    );

                    for (int iLienKet = 0; iLienKet < promoLinkList.size(); iLienKet++) {
                        JSONObject promoLink = promoLinkList.get(iLienKet);
                        if (ConvertUtils.toInt(promoLink.get("group_id")) != 0) {
                            if (group_id != 0 && group_id != ConvertUtils.toInt(promoLink.get("group_id"))) {
                                ClientResponse crLienKet = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                                crLienKet.setMessage("[CTTL thứ " + (iLienKet + 1) + "] hiện đang liên kết ở nhóm khác");
                                return crLienKet;
                            }

                            group_id = ConvertUtils.toInt(promoLink.get("group_id"));
                        }
                    }
                }
            }

            List<JSONObject> records = new ArrayList<>();
            if (PromoConditionType.PRODUCT_PRICE.getKey().equals(request.getPromo_info().getCondition_type())
                    || PromoConditionType.PRODUCT_QUANTITY.getKey().equals(request.getPromo_info().getCondition_type())) {
                List<String> products = new ArrayList<>();
                for (PromoItemGroupRequest itemGroupRequest : request.getPromo_item_groups()) {
                    for (PromoItemGroupDetailRequest productRequest : itemGroupRequest.getProducts()) {
                        products.add(ConvertUtils.toString(productRequest.getItem_id()));
                    }
                }
                if (!products.isEmpty()) {
                    records = this.promoDB.getListPromoLikeProduct(
                            JsonUtils.Serialize(products));
                }
            }

            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Duyệt chính sách
     *
     * @param sessionData
     * @param
     * @return
     */
    public ClientResponse approveAllPromo(SessionData sessionData) {
        try {
            List<JSONObject> rs = this.promoDB.getListPromoDraft();
            for (JSONObject jsonObject : rs) {
                int id = ConvertUtils.toInt(jsonObject.get("id"));
                /**
                 * 1. Kiểm tra chính sách tồn tại
                 */
                PromoEntity promoEntity = this.promoDB.getPromo(id);
                if (promoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
                }

                /**
                 * 2. Kiểm tra trước khi duyệt: thời gian và trạng thái
                 */
                ClientResponse clientResponse = this.validateApprovePromo(promoEntity);
                if (clientResponse.failed()) {
                    return clientResponse;
                }

                boolean isPromoRunning = false;
                /**
                 * Nếu thời gian bắt đầu < thời gian hiện tại
                 * 1. lưu thời gian bắt đầu chính sách bằng thời gian duyệt hiện tại
                 * 2. kích hoạt chính sách đang chạy
                 */
                if (promoEntity.getStart_date().getTime() < DateTimeUtils.getMilisecondsNow()) {
                    promoEntity.setStart_date(DateTimeUtils.getNow());
                    promoEntity.setStatus(PromoActiveStatus.RUNNING.getId());
                    isPromoRunning = true;
                } else {
                    promoEntity.setStatus(PromoActiveStatus.WAITING.getId());
                }


                promoEntity = this.promoDB.approvePromo(promoEntity);
                if (promoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * 1. Nếu chính sách chạy ngay thì thêm vào danh sách chính sách đang chạy
                 * 2. Nếu chính sách chờ chạy thì thêm vào danh sách hẹn giờ
                 */
                /**
                 * 1. Nếu chính sách đang chạy thì thêm vào danh sách chính sách đang chạy
                 */
                if (isPromoRunning) {

                    /**
                     * Lưu lịch sử
                     *
                     */
                    this.insertPromoHistory(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            promoEntity.getEnd_date(),
                            JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())),
                            "Duyệt",
                            promoEntity.getStatus(),
                            sessionData.getId());

                    PromoRunningEntity promoRunningEntity = this.promoDB.getPromoRunningById(id);
                    if (promoRunningEntity == null) {
                        promoRunningEntity = new PromoRunningEntity();
                    }
                    promoRunningEntity.setPromo_id(id);
                    promoRunningEntity.setPromo_data(JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())));
                    promoRunningEntity = this.promoDB.savePromoRunning(promoRunningEntity);
                    if (promoRunningEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadPromoRunning(
                            id,
                            PromoScheduleType.START,
                            promoEntity.getPromo_type()
                    );
                } else {
                    /**
                     * Lưu lịch sử
                     *
                     */
                    this.insertPromoHistory(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            promoEntity.getEnd_date(),
                            JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())),
                            "Duyệt",
                            promoEntity.getStatus(),
                            sessionData.getId());

                    /**
                     * 2. Nếu chính sách chờ chạy thì thêm vào danh sách hẹn giờ
                     */
                    this.createPromoSchedule(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            PromoScheduleType.START);
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse importPromoAgencyIgnore() {
        try {
            String filePath =
                    "/Users/lephuoc/Documents/SHT/source/BECmsServer/CODE/CmsServer/conf/promo_agency_ignore.xlsx";
            FileInputStream file = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            short index = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (index > 0) {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    List<Cell> ltCell = new ArrayList<>();
                    cellIterator.forEachRemaining(ltCell::add);
                    if (!ltCell.isEmpty()) {
                        Cell cell = ltCell.get(0);
                        int promo_id = getNumberData(cell);
                        cell = ltCell.get(4);
                        String phone = getStringData(cell);

                        AgencyEntity agency = this.agencyDB.getAgencyEntityByPhone(
                                phone
                        );
                        if (agency == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        PromoEntity promoEntity = this.promoDB.getPromo(promo_id);
                        if (promoEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        PromoApplyObjectEntity promoApplyObjectEntity = this.promoDB.getPromoApplyObject(
                                promo_id
                        );

                        Date start_date = new Date();

                        List<AgencyBasicData> agencyList =
                                JsonUtils.DeSerialize(
                                        promoApplyObjectEntity.getPromo_agency_ignore(),
                                        new TypeToken<List<AgencyBasicData>>() {
                                        }.getType());
                        AgencyBasicData agencyBasicData = new AgencyBasicData();
                        agencyBasicData.setId(agency.getId());
                        agencyBasicData.setCode(agency.getCode());
                        agencyList.add(agencyBasicData);
                        promoApplyObjectEntity.setPromo_agency_ignore(
                                JsonUtils.Serialize(agencyList)
                        );
                        boolean rsUpdate = this.promoDB.updatePromoApplyObject(
                                promo_id,
                                promoApplyObjectEntity.getPromo_agency_include(),
                                promoApplyObjectEntity.getPromo_agency_ignore()
                        );

                        if (!rsUpdate) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        this.createPromoRunning(promo_id);
                    }
                }
                index++;
            }
            file.close();

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse addListAgencyIgnoreToPromo(AddListAgencyToPromoRequest request) {
        try {
            int promo_id = request.getPromo_id();
            PromoEntity promoEntity = this.promoDB.getPromo(promo_id);
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            PromoApplyObjectEntity promoApplyObjectEntity = this.promoDB.getPromoApplyObject(
                    promo_id
            );
            List<AgencyBasicData> agencyList =
                    JsonUtils.DeSerialize(
                            promoApplyObjectEntity.getPromo_agency_ignore(),
                            new TypeToken<List<AgencyBasicData>>() {
                            }.getType());
            for (Integer agency_id : request.getAgency_ids()) {
                AgencyEntity agency = this.agencyDB.getAgencyEntity(
                        agency_id
                );
                if (agency == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }


                Date start_date = new Date();


                AgencyBasicData agencyBasicData = new AgencyBasicData();
                agencyBasicData.setId(agency.getId());
                agencyBasicData.setCode(agency.getCode());
                agencyList.add(agencyBasicData);
            }

            promoApplyObjectEntity.setPromo_agency_ignore(
                    JsonUtils.Serialize(agencyList)
            );
            boolean rsUpdate = this.promoDB.updatePromoApplyObject(
                    promo_id,
                    promoApplyObjectEntity.getPromo_agency_include(),
                    promoApplyObjectEntity.getPromo_agency_ignore()
            );

            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            this.createPromoRunning(promo_id);
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse addAgencyIgnoreToListPromo(AddAgencyToListPromoRequest request) {
        try {
            int agency_id = request.getAgency_id();
            AgencyEntity agency = this.agencyDB.getAgencyEntity(
                    agency_id
            );
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            AgencyBasicData agencyBasicData = new AgencyBasicData();
            agencyBasicData.setId(agency.getId());
            agencyBasicData.setCode(agency.getCode());

            for (Integer promo_id : request.getPromo_ids()) {
                PromoEntity promoEntity = this.promoDB.getPromo(promo_id);
                if (promoEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                PromoApplyObjectEntity promoApplyObjectEntity = this.promoDB.getPromoApplyObject(
                        promo_id
                );
                List<AgencyBasicData> agencyList =
                        JsonUtils.DeSerialize(
                                promoApplyObjectEntity.getPromo_agency_ignore(),
                                new TypeToken<List<AgencyBasicData>>() {
                                }.getType());


                Date start_date = new Date();

                agencyList.add(agencyBasicData);

                promoApplyObjectEntity.setPromo_agency_ignore(
                        JsonUtils.Serialize(agencyList)
                );
                boolean rsUpdate = this.promoDB.updatePromoApplyObject(
                        promo_id,
                        promoApplyObjectEntity.getPromo_agency_include(),
                        promoApplyObjectEntity.getPromo_agency_ignore()
                );

                if (!rsUpdate) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                this.createPromoRunning(promo_id);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Danh sách chính sách
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filterCombo(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_COMBO, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject rs : records) {
                rs.put("promo_count", this.promoDB.countPromo(
                        ConvertUtils.toInt(rs.get("id"))
                ));
                rs.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(rs.get("creator_id"))
                ));
            }
            int total = this.promoDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Danh sách chính sách săn sale
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filterPromoHuntSale(SessionData sessionData, FilterListRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("business_department_id");
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(staff.getDepartment_id()));
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.LIST_PROMO_HUNT_SALE, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.promoDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }


    public ClientResponse getPromoByDealPrice(SessionData sessionData, GetPromoByDealPriceRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject deal = this.dealDB.getDealPrice(request.getDeal_price_id());
            if (deal == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            int status = ConvertUtils.toInt(deal.get("status"));
            List<PromoBasicData> promoBasicDataList = new ArrayList<>();
            if (status != DealPriceStatus.CANCEL.getId() &&
                    status != DealPriceStatus.CONFIRMED.getId()) {
                Agency agency = this.dataManager.getProgramManager().getAgency(
                        ConvertUtils.toInt(deal.get("agency_id")));
                if (agency == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                int product_id = ConvertUtils.toInt(deal.get("product_id"));
                if (agency != null) {
                    DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
                    List<PromoBasicData> promos = this.dataManager.getProgramManager().getPromoByProduct(
                            product_id);
                    for (PromoBasicData promoBasicData : promos) {
                        Program programCSBH = this.dataManager.getProgramManager().getMpSalePolicy().get(promoBasicData.getId());
                        Program programCTKM = this.dataManager.getProgramManager().getMpPromotion().get(promoBasicData.getId());
                        //LogUtil.printDebug(JsonUtils.Serialize(program));
                        if (programCSBH != null && programCSBH.isRunning()) {
                            if (this.checkProgramFilter(
                                    agency,
                                    programCSBH,
                                    Source.WEB,
                                    deptInfo)) {
                                promoBasicData.setDescription(
                                        this.getCmsProgramDescriptionForProduct(
                                                agency,
                                                product_id,
                                                programCSBH));
                                promoBasicDataList.add(promoBasicData);
                            }
                        } else if (programCTKM != null && programCTKM.isRunning()) {
                            if (this.checkProgramFilter(
                                    agency, programCTKM, Source.WEB, deptInfo)) {
                                promoBasicData.setDescription(
                                        this.getCmsProgramDescriptionForProduct(
                                                agency,
                                                product_id,
                                                programCTKM));
                                promoBasicDataList.add(promoBasicData);
                            }
                        }
                    }
                }
            }

            JSONObject data = new JSONObject();
            data.put("promos", promoBasicDataList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse updatePromoRunning(
            int promo_id,
            CreatePromoRequest promo_data) {
        try {
            PromoRunningEntity promoRunningEntity = this.promoDB.getPromoRunningById(promo_id);
            if (promoRunningEntity == null) {
                promoRunningEntity = new PromoRunningEntity();
            }
            promoRunningEntity.setPromo_id(promo_id);
            promoRunningEntity.setPromo_data(
                    JsonUtils.Serialize(promo_data))
            ;
            promoRunningEntity = this.promoDB.savePromoRunning(promoRunningEntity);
            if (promoRunningEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchProductHuntSale(FilterListRequest request) {
        try {
            AgencyEntity agencyEntity = this.dataManager.getAgencyManager().getAgency(request.getAgency_id());
            String promo_search = this.filterUtils.getValueByKey(request.getFilters(), "promo");
            String product_search = this.filterUtils.getValueByKey(request.getFilters(), "product");
            String combo_search = this.filterUtils.getValueByKey(request.getFilters(), "combo");
            int item_group = ConvertUtils.toInt(this.filterUtils.getValueByKey(request.getFilters(), "item_group"));
            int is_hunt_sale = ConvertUtils.toInt(this.filterUtils.getValueByKey(request.getFilters(), "is_hunt_sale"));
            int item_type = ConvertUtils.toInt(this.filterUtils.getValueByKey(request.getFilters(), "item_type"));
            int plsp_id = ConvertUtils.toInt(this.filterUtils.getValueByKey(request.getFilters(), "plsp_id"));
            int pltth_id = ConvertUtils.toInt(this.filterUtils.getValueByKey(request.getFilters(), "pltth_id"));

            JSONObject data = new JSONObject();

            Agency agency = this.dataManager.getProgramManager().getAgency(request.getAgency_id());
            if (agency == null) {
                data.put("records", new ArrayList<>());
                return ClientResponse.success(data);
            }

            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());

            List<JSONObject> records = new ArrayList<>();
            if (item_group == 0 || item_group == 1) {
                if (product_search.isEmpty()) {
                    List<JSONObject> rsComboHuntSale = this.promoDB.searchComboHuntSale(
                            promo_search, combo_search);
                    Map<Integer, Integer> mpCombo = new HashMap<>();
                    for (JSONObject record : rsComboHuntSale) {
                        int item_id = ConvertUtils.toInt(record.get("item_id"));
                        if (mpCombo.containsKey(item_id)) {
                            continue;
                        }
                        Program programHuntSale = this.dataManager.getProgramManager().getMpHuntSale().get(
                                ConvertUtils.toInt(record.get("promo_id")));
                        if (programHuntSale == null ||
                                !programHuntSale.isRunning()
                                || !this.checkProgramFilter(
                                agency,
                                programHuntSale,
                                Source.WEB,
                                deptInfo)) {
                            continue;
                        }

                        JSONObject comboInfo = this.promoDB.getComboInfo(
                                item_id);
                        if (comboInfo != null) {
                            comboInfo.put("minimum_purchase", 1);
                            comboInfo.put("step", 1);
                            comboInfo.put("is_combo", 1);
                            List<ProductData> products = new ArrayList<>();
                            List<JSONObject> comboDetailList = this.promoDB.getListProductInCombo(item_id);
                            int show = VisibilityType.SHOW.getId();
                            long total_price = 0;
                            for (JSONObject comboDetail : comboDetailList) {
                                int product_id = ConvertUtils.toInt(comboDetail.get("product_id"));
                                int product_quantity = ConvertUtils.toInt(comboDetail.get("product_quantity"));
                                long product_price = 0;

                                ProductCache productPrice = this.getFinalPriceByAgency(
                                        product_id,
                                        agency.getId(),
                                        agency.getCityId(),
                                        agency.getRegionId(),
                                        agency.getMembershipId()
                                );
                                if (total_price == -1 || productPrice.getPrice() <= 0) {
                                    total_price = -1;
                                } else {
                                    total_price += productPrice.getPrice() < 0 ? 0 : (productPrice.getPrice() * product_quantity);
                                }
                                ProductCache productCache = this.dataManager.getProductManager()
                                        .getProductBasicData(product_id);
                                if (productCache != null) {
                                    show = show == VisibilityType.SHOW.getId() ? this.getProductVisibilityByAgency(
                                            agency.getId(),
                                            product_id
                                    ) : VisibilityType.HIDE.getId();
                                    if (show == VisibilityType.HIDE.getId()) {
                                        break;
                                    }

                                    ProductData product = new ProductData(
                                            product_id,
                                            productCache.getCode(),
                                            productCache.getFull_name(),
                                            product_quantity,
                                            productCache.getImages()
                                    );
                                    product.setQuantity(product_quantity);
                                    product.setPrice(product_price);
                                    products.add(product);
                                }
                            }

                            comboInfo.put("price", total_price);
                            comboInfo.put("products", products);
                            comboInfo.put("promo_info", this.dataManager.getProgramManager().getPromoCTSSData(
                                    ConvertUtils.toInt(record.get("promo_id"))
                            ));
                            comboInfo.put("is_hunt_sale", 1);
                            records.add(comboInfo);

                            mpCombo.put(item_id, item_id);
                        }
                    }
                }
            }

            if (item_group == 0 || item_group == 2) {
                if (combo_search.isEmpty()) {
                    List<JSONObject> rsProductHuntSale = this.promoDB.searchProductHuntSale(
                            promo_search, product_search);
                    Map<Integer, Integer> mpProduct = new HashMap<>();
                    for (JSONObject record : rsProductHuntSale) {
                        int item_id = ConvertUtils.toInt(record.get("item_id"));
                        if (mpProduct.containsKey(item_id)) {
                            continue;
                        }

                        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                                item_id
                        );
                        if (productCache == null) {
                            continue;
                        }
                        if (item_type != 0 && productCache.getItem_type() != item_type) {
                            continue;
                        }
                        if (plsp_id != 0 && productCache.getPlsp_id() != plsp_id) {
                            continue;
                        }
                        if (pltth_id != 0 && productCache.getPltth_id() != pltth_id) {
                            continue;
                        }

                        Program programHuntSale = this.dataManager.getProgramManager().getMpHuntSale().get(
                                ConvertUtils.toInt(record.get("promo_id")));
                        if (programHuntSale == null ||
                                !programHuntSale.isRunning()
                                || !this.checkProgramFilter(
                                agency,
                                programHuntSale,
                                Source.WEB,
                                deptInfo)) {
                            continue;
                        }

                        if (this.getProductVisibilityByAgency(
                                agency.getId(),
                                item_id
                        ) == VisibilityType.HIDE.getId()) {
                            continue;
                        }
                        ProductCache productPrice = this.getFinalPriceByAgency(
                                item_id,
                                agencyEntity.getId(),
                                agencyEntity.getCity_id(),
                                agencyEntity.getRegion_id(),
                                agencyEntity.getMembership_id()
                        );
                        JSONObject product = JsonUtils.DeSerialize(
                                JsonUtils.Serialize(
                                        productCache
                                ),
                                JSONObject.class
                        );
                        product.put("price", productPrice.getPrice());
                        product.put("is_combo", 0);
                        product.put("is_hunt_sale", 1);
                        records.add(product);

                        mpProduct.put(item_id, item_id);
                    }

                    if (is_hunt_sale == 0 && records.size() < 100) {
                        List<FilterRequest> filterRequests = new ArrayList<>();
                        if (!product_search.isEmpty()) {
                            FilterRequest filterRequest = new FilterRequest();
                            filterRequest.setType("search");
                            filterRequest.setValue(product_search);
                            filterRequest.setKey("");
                            filterRequests.add(filterRequest);
                        }
                        if (item_type != 0) {
                            FilterRequest filterRequest = new FilterRequest();
                            filterRequest.setType(TypeFilter.SELECTBOX);
                            filterRequest.setValue(ConvertUtils.toString(item_type));
                            filterRequest.setKey("item_type");
                            filterRequests.add(filterRequest);
                        }
                        if (plsp_id != 0) {
                            FilterRequest filterRequest = new FilterRequest();
                            filterRequest.setType(TypeFilter.SELECTBOX);
                            filterRequest.setValue(ConvertUtils.toString(plsp_id));
                            filterRequest.setKey("plsp_id");
                            filterRequests.add(filterRequest);
                        }
                        if (pltth_id != 0) {
                            FilterRequest filterRequest = new FilterRequest();
                            filterRequest.setType(TypeFilter.SELECTBOX);
                            filterRequest.setValue(ConvertUtils.toString(pltth_id));
                            filterRequest.setKey("pltth_id");
                            filterRequests.add(filterRequest);
                        }
                        List<JSONObject> productList = this.productDB.filter(
                                filterUtils.getQuery(FunctionList.SEARCH_PRODUCT, filterRequests, new ArrayList<>()),
                                0, 50, 1
                        );
                        for (JSONObject js : productList) {
                            int product_id = ConvertUtils.toInt(js.get("id"));
                            if (mpProduct.containsKey(product_id)) {
                                continue;
                            }

                            ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                                    product_id
                            );
                            if (productCache == null) {
                                continue;
                            }

                            int visibility = this.getProductVisibilityByAgency(
                                    request.getAgency_id(),
                                    product_id);
                            ProductCache productPrice = new ProductCache();
                            if (VisibilityType.SHOW.getId() == visibility) {
                                productPrice = this.getFinalPriceByAgency(
                                        ConvertUtils.toInt(js.get("id")),
                                        agencyEntity.getId(),
                                        agencyEntity.getCity_id(),
                                        agencyEntity.getRegion_id(),
                                        agencyEntity.getMembership_id()
                                );
                            } else {
                                productPrice.setPrice(-1);
                            }
                            js.put("visibility", visibility);
                            js.put("image_url", ImagePath.PRODUCT.getImageUrl());
                            js.put("price", productPrice.getPrice());
                            js.put("minimum_purchase", productPrice.getMinimum_purchase());
                            js.put("is_combo", 0);
                            js.put("is_hunt_sale", 0);
                            records.add(js);
                        }
                    }
                }
            }
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchProductOrder(SessionData sessionData, FilterListRequest request) {
        try {
            AgencyEntity agencyEntity = this.dataManager.getAgencyManager().getAgency(request.getAgency_id());
            String promo_search = this.filterUtils.getValueByKey(request.getFilters(), "promo");
            String product_search = this.filterUtils.getValueByKey(request.getFilters(), "product");
            String combo_search = this.filterUtils.getValueByKey(request.getFilters(), "combo");
            int item_group = ConvertUtils.toInt(this.filterUtils.getValueByKey(request.getFilters(), "item_group"));
            int is_hunt_sale = ConvertUtils.toInt(this.filterUtils.getValueByKey(request.getFilters(), "is_hunt_sale"));
            int item_type = ConvertUtils.toInt(this.filterUtils.getValueByKey(request.getFilters(), "item_type"));
            int plsp_id = ConvertUtils.toInt(this.filterUtils.getValueByKey(request.getFilters(), "plsp_id"));
            int pltth_id = ConvertUtils.toInt(this.filterUtils.getValueByKey(request.getFilters(), "pltth_id"));

            JSONObject data = new JSONObject();

            Agency agency = this.dataManager.getProgramManager().getAgency(request.getAgency_id());
            if (agency == null) {
                data.put("records", new ArrayList<>());
                return ClientResponse.success(data);
            }

            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());

            List<JSONObject> records = new ArrayList<>();

            List<FilterRequest> filterRequests = new ArrayList<>();
            if (!product_search.isEmpty()) {
                FilterRequest filterRequest = new FilterRequest();
                filterRequest.setType("search");
                filterRequest.setValue(product_search);
                filterRequest.setKey("");
                filterRequests.add(filterRequest);
            }
            if (item_type != 0) {
                FilterRequest filterRequest = new FilterRequest();
                filterRequest.setType(TypeFilter.SELECTBOX);
                filterRequest.setValue(ConvertUtils.toString(item_type));
                filterRequest.setKey("item_type");
                filterRequests.add(filterRequest);
            }
            if (plsp_id != 0) {
                FilterRequest filterRequest = new FilterRequest();
                filterRequest.setType(TypeFilter.SELECTBOX);
                filterRequest.setValue(ConvertUtils.toString(plsp_id));
                filterRequest.setKey("plsp_id");
                filterRequests.add(filterRequest);
            }
            if (pltth_id != 0) {
                FilterRequest filterRequest = new FilterRequest();
                filterRequest.setType(TypeFilter.SELECTBOX);
                filterRequest.setValue(ConvertUtils.toString(pltth_id));
                filterRequest.setKey("pltth_id");
                filterRequests.add(filterRequest);
            }

            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("t.business_department_id");
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(staff.getDepartment_id()));
            filterRequests.add(filterRequest);
            List<JSONObject> productList = this.productDB.filter(
                    filterUtils.getQuery(FunctionList.SEARCH_PRODUCT, filterRequests, new ArrayList<>()),
                    0, 50, 1
            );
            for (JSONObject js : productList) {
                int product_id = ConvertUtils.toInt(js.get("id"));

                ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                        product_id
                );
                if (productCache == null) {
                    continue;
                }

                int visibility = VisibilityType.SHOW.getId();

                js.put("visibility", visibility);
                js.put("image_url", ImagePath.PRODUCT.getImageUrl());
                js.put("price", productCache.getPrice());
                js.put("minimum_purchase", productCache.getMinimum_purchase());
                js.put("is_combo", 0);
                js.put("is_hunt_sale", 0);
                records.add(js);
            }
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Danh sách chương trình săn sale
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filerPromoCTSSSort(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_PROMO_CTSS_SORT, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filerPromo(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 0);
            int total = this.promoDB.getTotalPromo(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse sortPromoHuntSale(SortPromoRequest request, SessionData sessionData) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            this.promoDB.removePriorityWithPromoStopped();

            /**
             * Remove Ưu tiên
             */
            List<JSONObject> oldPromoList = this.promoDB.getPromoCTSSPriorityList();
            for (JSONObject promo : oldPromoList) {
                int oldId = ConvertUtils.toInt(promo.get("id"));
                /**
                 *
                 */
                if (!request.getIds().contains(oldId)) {
                    boolean rsUpdatePromoPriority = this.promoDB.updatePromoPriority(oldId, 0, sessionData.getId());
                    if (!rsUpdatePromoPriority) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     *
                     */
                    if (PromoActiveStatus.RUNNING.getId() == ConvertUtils.toInt(promo.get("status"))) {
                        PromoRunningEntity promoRunningEntity = this.promoDB.getPromoRunningById(oldId);
                        if (promoRunningEntity == null) {
                            promoRunningEntity = new PromoRunningEntity();
                        }
                        promoRunningEntity.setPromo_id(oldId);
                        CreatePromoRequest promo_data = this.convertPromoToData(oldId);
                        promoRunningEntity.setPromo_data(JsonUtils.Serialize(promo_data));
                        promoRunningEntity = this.promoDB.savePromoRunning(promoRunningEntity);
                        if (promoRunningEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                        this.dataManager.getProgramManager().reloadPromoRunning(
                                oldId,
                                PromoScheduleType.START,
                                promo_data.getPromo_info().getPromo_type()
                        );
                    }
                }
            }

            for (int iPromo = 0; iPromo < request.getIds().size(); iPromo++) {
                PromoEntity promoEntity = this.promoDB.getPromo(request.getIds().get(iPromo));
                if (promoEntity != null) {
                    if (promoEntity.getPriority() == iPromo + 1) {
                        continue;
                    }
                    /**
                     * cập nhật thứ tự ưu tiên
                     */
                    boolean rsUpdatePromoPriority = this.promoDB.updatePromoPriority(request.getIds().get(iPromo), iPromo + 1, sessionData.getId());
                    if (!rsUpdatePromoPriority) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * nếu promo đang chạy
                     * cập nhật promo running
                     */
                    if (PromoActiveStatus.RUNNING.getId() == promoEntity.getStatus()) {
                        PromoRunningEntity promoRunningEntity = this.promoDB.getPromoRunningById(promoEntity.getId());
                        if (promoRunningEntity == null) {
                            promoRunningEntity = new PromoRunningEntity();
                        }
                        promoRunningEntity.setPromo_id(promoEntity.getId());
                        CreatePromoRequest promo_data = this.convertPromoToData(promoEntity.getId());
                        promoRunningEntity.setPromo_data(JsonUtils.Serialize(promo_data));
                        promoRunningEntity = this.promoDB.savePromoRunning(promoRunningEntity);
                        if (promoRunningEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                        this.dataManager.getProgramManager().reloadPromoRunning(
                                request.getIds().get(iPromo),
                                PromoScheduleType.START,
                                promo_data.getPromo_info().getPromo_type());
                    }
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getPromoHuntSaleByProduct(SessionData sessionData, GetPromoByProductRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            List<JSONObject> promoBasicDataList = new ArrayList<>();
            Agency agency = this.dataManager.getProgramManager().getAgency(request.getAgency_id());
            if (agency != null) {
                DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
                if (request.getIs_combo() == YesNoStatus.YES.getValue()) {
                    List<JSONObject> promos = this.promoDB.getPromoHuntSaleRunningByCombo(
                            request.getProduct_id());
                    for (JSONObject promoBasicData : promos) {
                        Program programHuntSale = this.dataManager.getProgramManager().getMpHuntSale().get(
                                ConvertUtils.toInt(promoBasicData.get("id")));
                        if (programHuntSale != null && programHuntSale.isRunning()) {
                            if (this.checkProgramFilter(
                                    agency,
                                    programHuntSale,
                                    Source.WEB,
                                    deptInfo)) {
                                if (promoBasicData.get("note") != null &&
                                        !promoBasicData.get("note").toString().isEmpty()) {
                                    promoBasicData.put("description",
                                            promoBasicData.get("note"));
                                }
                                promoBasicDataList.add(promoBasicData);
                            }
                        }
                    }
                } else {
                    List<JSONObject> promos = this.promoDB.getPromoHuntSaleRunningByProduct(
                            request.getProduct_id(),
                            request.getIs_combo());
                    for (JSONObject promoBasicData : promos) {
                        if (checkComboVisibility(agency, ConvertUtils.toInt(promoBasicData.get("combo_id"))) == VisibilityType.HIDE.getId()) {
                            continue;
                        }
                        Program programHuntSale = this.dataManager.getProgramManager().getMpHuntSale().get(
                                ConvertUtils.toInt(promoBasicData.get("id")));
                        if (programHuntSale != null && programHuntSale.isRunning()) {
                            if (this.checkProgramFilter(
                                    agency,
                                    programHuntSale,
                                    Source.WEB,
                                    deptInfo)) {
                                String description = programHuntSale.getMpProductWithCombo().get(request.getProduct_id()).getDescription();
                                if (StringUtils.isBlank(description)) {
                                    description = programHuntSale.getDescription();
                                }
                                promoBasicData.put("description",
                                        description
                                );
                                promoBasicDataList.add(promoBasicData);
                            }
                        }
                    }
                }
                JSONObject data = new JSONObject();
                data.put("promos", promoBasicDataList);
                return ClientResponse.success(data);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getPromoHuntSaleByCombo(SessionData sessionData, FilterListByIdRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            int status = ConvertUtils.toInt(filterUtils.getValueByKey(request.getFilters(), "status"));
            String search = ConvertUtils.toString(filterUtils.getValueByKey(request.getFilters(), "search"));

            List<JSONObject> promos = this.promoDB.getAllPromoHuntSaleRunningByCombo(
                    request.getId(),
                    status,
                    search
            );
            JSONObject data = new JSONObject();
            data.put("promos", promos);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createCombo(SessionData sessionData, CreateComboRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject combo = this.promoDB.getComboInfoByCode(request.getCode());
            if (combo != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_INVALID);
            }

            int rsInsert = this.promoDB.insertCombo(
                    request.getName(),
                    request.getCode(),
                    request.getDescription(),
                    request.getImages(),
                    request.getHot_label(),
                    JsonUtils.Serialize(request.getProducts()),
                    sessionData.getId());
            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            for (ProductData productData : request.getProducts()) {
                int rsInsertDetail = this.promoDB.insertComboDetail(
                        rsInsert,
                        productData.getId(),
                        productData.getQuantity()
                );
            }

            /**
             * Call reload combo
             */
            this.dataManager.reloadCombo(rsInsert);

            request.setId(rsInsert);
            JSONObject data = new JSONObject();
            data.put("combo", request);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getComboDetail(SessionData sessionData, BasicRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject combo = this.promoDB.getComboInfo(request.getId());
            if (combo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (combo.get("data") == null || combo.get("data").toString().isEmpty()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<ProductData> productDataList = JsonUtils.DeSerialize(
                    combo.get("data").toString(),
                    new TypeToken<List<ProductData>>() {
                    }.getType()
            );

            List<ProductData> productList = new ArrayList<>();
            for (ProductData productData : productDataList) {
                ProductData product = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(this.dataManager.getProductManager().getProductBasicData(
                                productData.getId()
                        )),
                        ProductData.class
                );
                product.setQuantity(productData.getQuantity());
                productList.add(product);
            }

            combo.put("creator_info", this.dataManager.getStaffManager().getStaff(
                    ConvertUtils.toInt(combo.get("creator_id"))
            ));

            combo.put("products", productList);
            JSONObject data = new JSONObject();
            data.put("record", combo);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editCombo(SessionData sessionData, CreateComboRequest request) {
        try {
            JSONObject combo = this.promoDB.getComboInfo(request.getId());
            if (combo == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_INVALID);
            }

            boolean rsUpdate = this.promoDB.updateCombo(
                    request.getId(),
                    request.getHot_label(),
                    request.getStatus());
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Call reload combo
             */
            this.dataManager.reloadCombo(request.getId());

            JSONObject data = new JSONObject();
            data.put("combo", request);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse doubleCheckPromoHuntSale(SessionData sessionData, CreatePromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Ràng buộc tạo chính sách
             */
            clientResponse = this.validateCreatePromo(request);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            List<JSONObject> records = new ArrayList<>();
            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public int checkComboVisibility(Agency agency, int combo_id) {
        try {
            JSONObject comboInfo = this.promoDB.getComboInfo(
                    combo_id);
            if (comboInfo != null) {
                List<JSONObject> comboDetailList = this.promoDB.getListProductInCombo(
                        combo_id);
                int show = VisibilityType.SHOW.getId();
                for (JSONObject comboDetail : comboDetailList) {
                    int product_id = ConvertUtils.toInt(comboDetail.get("product_id"));
                    ProductCache productCache = this.dataManager.getProductManager()
                            .getProductBasicData(product_id);
                    if (productCache != null) {
                        show = show == VisibilityType.SHOW.getId() ? this.getProductVisibilityByAgency(
                                agency.getId(),
                                product_id
                        ) : VisibilityType.HIDE.getId();
                        if (show == VisibilityType.HIDE.getId()) {
                            return VisibilityType.HIDE.getId();
                        }
                    }
                }
            }
            return VisibilityType.SHOW.getId();
        } catch (Exception e) {
            LogUtil.printDebug("", e);
        }
        return VisibilityType.SHOW.getId();
    }

    /**
     * Danh sách chương trình tích lũy
     *
     * @param sessionData
     * @param request
     */
    public ClientResponse filterPromoCTTL(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_PROMO_CTTL, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            int total = this.promoDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterListAgency(SessionData sessionData, PromoApplyObjectRequest request) {
        try {
            JSONObject data = new JSONObject();
            List<AgencyBasicData> records = new ArrayList<>();
            List<Agency> agencyList = this.dataManager.getProgramManager().getProgramDB().getAllAgency();
            for (Agency agency : agencyList) {
                DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
                Program program = this.dataManager.getProgramManager().parseProgramFilterByPromoFilter(
                        request
                );

                if (this.checkProgramFilterByPromoApplyObject(
                        agency,
                        Source.WEB,
                        deptInfo,
                        program
                )) {
                    program = this.dataManager.getProgramManager().parseProgramFilterByPromoSufficientCondition(
                            request
                    );
                    if (this.checkProgramFilterByPromoApplyObject(
                            agency,
                            Source.WEB,
                            deptInfo,
                            program
                    )) {
                        AgencyBasicData agencyBasicData = new AgencyBasicData();
                        agencyBasicData.parseInfo(
                                agency.getId(),
                                agency.getCode(),
                                agency.getShop_name(),
                                agency.getMembershipId(),
                                agency.getBusinessDepartmentId(),
                                agency.getPhone(),
                                agency.getAddress(),
                                agency.getShop_name(),
                                agency.getAvatar()
                        );
                        records.add(
                                agencyBasicData
                        );
                    }
                }
            }

            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runPromoCTTLStatus() {
        try {
            long now = DateTimeUtils.getMilisecondsNow();
            List<JSONObject> promoList = this.promoDB.getListPromoCTTLRunning();
            for (JSONObject promo : promoList) {
                this.runPromoCTTLStatusOne(
                        ConvertUtils.toInt(promo.get("id")),
                        promo,
                        now
                );
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse runPromoCTTLStatusOne(int promo_id, JSONObject promo, long now) {
        try {
            if (PromoCTTLStatus.RUNNING.getId() == ConvertUtils.toInt(promo.get("status"))) {
                PromoTimeRequest paymentDate = JsonUtils.DeSerialize(
                        promo.get("payment_date_data").toString(),
                        PromoTimeRequest.class);
                if (paymentDate.getEnd_date_millisecond() <= now) {
                    CreatePromoRequest promo_data = this.convertPromoToData(
                            promo_id
                    );
                    promo_data.getPromo_info().setStatus(
                            PromoCTTLStatus.WAITING_REWARD.getId()
                    );

                    this.promoDB.setPromoWaitingReward(promo_id);
                    /**
                     * Cập nhật promo_running
                     */
                    this.updatePromoRunning(
                            promo_id,
                            promo_data
                    );

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadPromoRunning(
                            promo_id,
                            PromoScheduleType.START,
                            ConvertUtils.toString(promo.get("promo_type"))
                    );
                } else {
                    PromoTimeRequest orderDate = JsonUtils.DeSerialize(
                            promo.get("order_date_data").toString(),
                            PromoTimeRequest.class);
                    if (orderDate.getEnd_date_millisecond() <= now) {
                        CreatePromoRequest promo_data = this.convertPromoToData(
                                promo_id
                        );
                        promo_data.getPromo_info().setStatus(
                                PromoCTTLStatus.WAITING_PAYMENT.getId()
                        );
                        this.promoDB.setPromoWaitingPayment(
                                promo_id
                        );
                        /**
                         * Cập nhật promo_running
                         */
                        this.updatePromoRunning(
                                promo_id,
                                promo_data
                        );

                        /**
                         * reload cache promo
                         */
                        this.dataManager.getProgramManager().reloadPromoRunning(
                                promo_id,
                                PromoScheduleType.START,
                                ConvertUtils.toString(promo.get("promo_type"))
                        );
                    }
                }
            } else if (PromoCTTLStatus.WAITING_PAYMENT.getId() == ConvertUtils.toInt(promo.get("status"))) {
                PromoTimeRequest paymentDate = JsonUtils.DeSerialize(
                        promo.get("payment_date_data").toString(),
                        PromoTimeRequest.class);
                if (paymentDate.getEnd_date_millisecond() <= now) {
                    CreatePromoRequest promo_data = this.convertPromoToData(
                            promo_id
                    );
                    promo_data.getPromo_info().setStatus(
                            PromoCTTLStatus.WAITING_REWARD.getId()
                    );

                    this.promoDB.setPromoWaitingReward(promo_id);

                    /**
                     * Cập nhật promo_running
                     */
                    this.updatePromoRunning(
                            promo_id,
                            promo_data
                    );

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadPromoRunning(
                            promo_id,
                            PromoScheduleType.START,
                            ConvertUtils.toString(promo.get("promo_type"))
                    );
                }
            } else if (PromoCTTLStatus.WAITING_REWARD.getId() == ConvertUtils.toInt(promo.get("status"))) {
                PromoTimeRequest rewardDate = JsonUtils.DeSerialize(
                        promo.get("reward_date_data").toString(),
                        PromoTimeRequest.class);
                if (rewardDate.getEnd_date_millisecond() <= now) {
                    CreatePromoRequest promo_data = this.convertPromoToData(
                            promo_id
                    );
                    promo_data.getPromo_info().setStatus(
                            PromoCTTLStatus.STOPPED.getId()
                    );

                    this.promoDB.setPromoStop(promo_id);

                    /**
                     * Cập nhật promo_running
                     */
                    this.updatePromoRunning(
                            promo_id,
                            promo_data
                    );

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadPromoRunning(
                            promo_id,
                            PromoScheduleType.START,
                            ConvertUtils.toString(promo.get("promo_type"))
                    );
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterCTTLByAgency(SessionData sessionData, FilterListByIdRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            List<JSONObject> promoBasicDataList = new ArrayList<>();
            Agency agency = this.dataManager.getProgramManager().getAgency(request.getId());
            if (agency != null) {
                DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
                request.setIsLimit(0);
                String query = this.filterUtils.getQuery(
                        FunctionList.LIST_CTTL_WAITING,
                        request.getFilters(),
                        request.getSorts()
                );
                List<JSONObject> records = this.promoDB.filter(query,
                        this.appUtils.getOffset(request.getPage()),
                        ConfigInfo.PAGE_SIZE,
                        request.getIsLimit());
                for (JSONObject record : records) {
                    Program program = this.getProgramById(
                            ConvertUtils.toInt(record.get("id"))
                    );
                    if (program == null) {
                        continue;
                    }
                    int allow = this.checkProgramFilter(
                            agency,
                            program,
                            Source.WEB,
                            deptInfo) == true ? 1 : 2;
                    JSONObject promo = JsonUtils.DeSerialize(
                            this.dataManager.getProgramManager().getMpPromoRunning().get(program.getId()),
                            JSONObject.class);
                    if (promo != null
                            && (request.getAllow() == 0 || request.getAllow() == allow)
                            && allow == 1) {
                        record.put("allow", allow);
                        promoBasicDataList.add(record);
                    }
                }
            }
            JSONObject data = new JSONObject();
            data.put("promos", promoBasicDataList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public void calculateRewardCTTL(int promo_id) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    /**
                     * - /tl/reward_program
                     *     + key : reload key
                     *     + program_id (int)
                     */
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/tl/reward_program?" +
                            "key=" + ConfigInfo.ACCUMULATE_KEY
                            + "&program_id=" + promo_id;
                    LogUtil.printDebug("ACCUMULATE: " + url);
                    ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
                    LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
    }

    public ClientResponse filterDameMeByAgency(SessionData sessionData, FilterListByIdRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            List<JSONObject> promoBasicDataList = new ArrayList<>();
            Agency agency = this.dataManager.getProgramManager().getAgency(request.getId());
            if (agency != null) {
                DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(agency.getId());
                request.setIsLimit(0);
                String query = this.filterUtils.getQuery(
                        FunctionList.LIST_DAM_ME_RUNNING,
                        request.getFilters(),
                        request.getSorts()
                );
                List<JSONObject> records = this.promoDB.filter(query,
                        this.appUtils.getOffset(request.getPage()),
                        ConfigInfo.PAGE_SIZE,
                        request.getIsLimit());
                for (JSONObject record : records) {
                    Program program = this.dataManager.getProgramManager().getMpDamMe().get(
                            ConvertUtils.toInt(record.get("id"))
                    );
                    if (program == null) {
                        continue;
                    }
                    int allow = this.checkProgramFilter(
                            agency,
                            program,
                            Source.WEB,
                            deptInfo) == true ? 1 : 2;
                    JSONObject promo = JsonUtils.DeSerialize(
                            this.dataManager.getProgramManager().getMpPromoRunning().get(program.getId()),
                            JSONObject.class);
                    if (promo != null
                            && (request.getAllow() == 0 || request.getAllow() == allow)
                            && allow == 1) {
                        record.put("allow", allow);

                        /**
                         * Thông tin hạn mức tích lũy
                         */
                        record.put(
                                "promo_limit_info", this.getPromoLimitInfoOfCSDM(
                                        program.getLtProgramLimit()
                                )
                        );

                        /**
                         * Tổng giá trị tích lũy
                         * - tong_gia_tri_tich_luy
                         */

                        JSONObject tichLuyInfo = this.promoDB.getAgencyCSDMInfoJs(
                                program.getId(),
                                request.getId()
                        );
                        JSONObject rsCSDMReward = this.getCSDMReward(
                                tichLuyInfo
                        );
                        record.put(
                                "tong_gia_tri_tich_luy", rsCSDMReward.get("tong_gia_tri_tich_luy")
                        );

                        record.put("han_muc_duoc_huong", rsCSDMReward.get("han_muc_duoc_huong"));
                        record.put("phan_tram_duoc_huong", rsCSDMReward.get("phan_tram_duoc_huong"));

                        /**
                         * Ưu đãi được hưởng
                         * - tổng giá trị đã giảm cho các Đơn hàng
                         */
                        record.put("uu_dai_duoc_huong",
                                this.getTongUuDaiDuocHuongOfCSDM(
                                        program.getId(),
                                        request.getId()
                                ));

                        promoBasicDataList.add(record);
                    }
                }
            }
            JSONObject data = new JSONObject();
            data.put("promos", promoBasicDataList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private long getTongUuDaiDuocHuongOfCSDM(int promo_id, int agency_id) {
        return 0;
    }

    private JSONObject getCSDMReward(JSONObject tichLuyInfo) {
        JSONObject reward = new JSONObject();
        try {
            if (tichLuyInfo != null) {
                CSDMAgencyReward cttlAgencyReward =
                        JsonUtils.DeSerialize(
                                tichLuyInfo.get("reward").toString(),
                                CSDMAgencyReward.class
                        );
                reward.put("tong_gia_tri_tich_luy", cttlAgencyReward.getTotalValue());
                reward.put("han_muc_duoc_huong", cttlAgencyReward.getLimitValue());
                reward.put("phan_tram_duoc_huong", cttlAgencyReward.getPercentValue());
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return reward;
    }

    private List<JSONObject> getPromoLimitInfoOfCSDM(List<ProgramLimit> ltProgramLimit) {
        try {
            List<JSONObject> hamMucList = new ArrayList<>();
            for (int iLimit = ltProgramLimit.size() - 1; iLimit >= 0; iLimit--) {
                JSONObject js = new JSONObject();
                ProgramLimit programLimit = ltProgramLimit.get(iLimit);
                js.put("level", programLimit.getLevel());
                js.put("from", programLimit.getLtProgramProductGroup().get(0).getFromValue());
                js.put("to", programLimit.getLtProgramProductGroup().get(0).getEndValue());
                js.put("discount", programLimit.getLtProgramProductGroup().get(0).getOfferValue());
                hamMucList.add(js);
            }
            return hamMucList;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return new ArrayList<>();
    }

    public ClientResponse createDamMe(SessionData sessionData, CreatePromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Ràng buộc tạo chính sách
             */
            clientResponse = this.validateCreateDamMe(request);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Tạo mã chính sách
             */
            String code = this.generatePromoCode(request.getPromo_info().getPromo_type());
            if (code.isEmpty()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
            }

            /**
             * Lưu thông tin chính sách
             */
            PromoEntity promoEntity = this.createPromoEntity(request.getPromo_info());
            promoEntity.setCode(code);
            promoEntity.setStart_date(new Date(request.getPromo_info().getStart_date_millisecond()));
            promoEntity.setEnd_date(request.getPromo_info().getEnd_date_millisecond() == 0 ? null : new Date(request.getPromo_info().getEnd_date_millisecond()));
            promoEntity.setPromo_type(request.getPromo_info().getPromo_type());
            promoEntity.setCondition_type(request.getPromo_info().getCondition_type());
            promoEntity.setOffer_info(this.createOfferInfo(request.getPromo_limits()));
            promoEntity.setStatus(PromoActiveStatus.DRAFT.getId());

            if (request.getRepeat_data() != null && request.getRepeat_data().getType() != 0) {
                promoEntity.setRepeat_type(request.getRepeat_data().getType());
                promoEntity.setRepeat_data(JsonUtils.Serialize(
                        request.getRepeat_data()
                ));
            }

            promoEntity = this.promoDB.createPromo(promoEntity);
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
            }

            /**
             * Luu đối tượng áp dụng
             */
            if (request.getPromo_apply_object() != null) {
                PromoApplyObjectEntity promoApplyObjectEntity = this.createPromoApplyObjectEntity(request.getPromo_apply_object());
                promoApplyObjectEntity.setPromo_id(promoEntity.getId());
                promoApplyObjectEntity = this.promoDB.createPromoApplyObject(promoApplyObjectEntity);
                if (promoApplyObjectEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }

                /**`
                 * Bộ lọc
                 */
                for (PromoApplyFilterRequest promoApplyFilterRequest : request.getPromo_apply_object().getPromo_filters()) {
                    PromoFilterEntity promoFilterEntity = this.createPromoFilterEntity(promoApplyFilterRequest);
                    promoFilterEntity.setPromo_id(promoEntity.getId());
                    promoFilterEntity = this.promoDB.createPromoFilter(promoFilterEntity);
                    if (promoFilterEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }
                }
            }

            /**
             * Lưu nhóm sản phẩm áp dụng
             */
            for (int iGroup = 0; iGroup < request.getPromo_item_groups().size(); iGroup++) {
                PromoItemGroupRequest promoItemGroupRequest = request.getPromo_item_groups().get(iGroup);
                promoItemGroupRequest.setData_index(iGroup + 1);
                PromoItemGroupEntity promoItemGroupEntity = this.createPromoItemGroupEntity(promoItemGroupRequest);
                promoItemGroupEntity.setPromo_id(promoEntity.getId());
                promoItemGroupEntity.setData_index(iGroup + 1);
                promoItemGroupEntity = this.promoDB.createPromoItemGroup(promoItemGroupEntity);
                if (promoItemGroupEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }
                promoItemGroupRequest.setId(promoItemGroupEntity.getId());

                for (PromoItemGroupDetailRequest promoItemGroupDetailRequest : promoItemGroupRequest.getProducts()) {
                    if (promoItemGroupDetailRequest.getItem_type().equals(DamMeProductType.PRODUCT.getKey())) {
                        promoItemGroupDetailRequest.setItem_name(this.dataManager.getProductManager().getProductFullName(promoItemGroupDetailRequest.getItem_id()));
                        promoItemGroupDetailRequest.setItem_code(this.dataManager.getProductManager().getProductCode(promoItemGroupDetailRequest.getItem_id()));
                    } else if (promoItemGroupDetailRequest.getCategory_level() != 0 ||
                            promoItemGroupDetailRequest.getItem_type().equals(DamMeProductType.CATEGORY.getKey())) {
                        promoItemGroupDetailRequest.setCategory_level(
                                this.dataManager.getProductManager().getCategoryById(
                                        promoItemGroupDetailRequest.getItem_id()
                                ).getCategory_level()
                        );
                        promoItemGroupDetailRequest.setItem_type(
                                DamMeProductType.CATEGORY.getKey()
                        );
                    }
                    PromoItemGroupDetailEntity promoItemGroupDetailEntity = this.createPromoItemGroupDetailEntity(promoItemGroupDetailRequest);
                    promoItemGroupDetailEntity.setPromo_item_group_id(promoItemGroupEntity.getId());
                    promoItemGroupDetailEntity.setPromo_id(promoEntity.getId());

                    if (promoItemGroupRequest.getCombo_id() != 0) {
                        promoItemGroupDetailEntity.setNote(
                                promoItemGroupRequest.getNote()
                        );
                    }

                    promoItemGroupDetailEntity = this.promoDB.createPromoItemGroupDetail(promoItemGroupDetailEntity);
                    if (promoItemGroupDetailEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }
                }
            }

            /**
             * Lưu sản phẩm loại trừ
             */
            for (int iItem = 0; iItem < request.getPromo_item_ignores().size(); iItem++) {
                PromoItemIgnoreRequest promoItemIgnoreRequest = request.getPromo_item_ignores().get(iItem);
                promoItemIgnoreRequest.setItem_code(this.dataManager.getProductManager().getProductCode(promoItemIgnoreRequest.getItem_id()));
                promoItemIgnoreRequest.setItem_name(this.dataManager.getProductManager().getProductFullName(promoItemIgnoreRequest.getItem_id()));
                PromoItemIgnoreEntity promoItemIgnoreEntity = this.createPromoItemIgnoreEntity(promoItemIgnoreRequest);
                promoItemIgnoreEntity.setPromo_id(promoEntity.getId());
                promoItemIgnoreEntity = this.promoDB.createPromoItemIgnore(promoItemIgnoreEntity);
                if (promoItemIgnoreEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }
            }

            /**
             * Lưu thông tin hạn mức - promo_limit
             */
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                PromoLimitEntity promoLimitEntity = this.createPromoLimitEntity(promoLimitRequest);
                promoLimitEntity.setPromo_id(promoEntity.getId());
                promoLimitEntity.setLevel(iLimit + 1);
                promoLimitRequest.setLevel(iLimit + 1);
                promoLimitEntity = this.promoDB.createPromoLimit(promoLimitEntity);
                if (promoLimitEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }
                promoLimitRequest.setId(promoLimitEntity.getId());

                /**
                 * Lưu thông tin chi tiết hạn mức của từng nhóm - promo_limit_group
                 */
                for (int iLimitGroup = 0; iLimitGroup < promoLimitRequest.getPromo_limit_groups().size(); iLimitGroup++) {
                    PromoLimitGroupRequest promoLimitGroupRequest = promoLimitRequest.getPromo_limit_groups().get(iLimitGroup);
                    promoLimitGroupRequest.setData_index(iLimitGroup + 1);
                    PromoLimitGroupEntity promoLimitGroupEntity = this.createPromoLimitGroupEntity(promoLimitGroupRequest);
                    promoLimitGroupEntity.setPromo_limit_id(promoLimitEntity.getId());
                    promoLimitGroupEntity.setData_index(iLimitGroup + 1);
                    promoLimitGroupEntity.setPromo_id(promoEntity.getId());
                    promoLimitGroupEntity = this.promoDB.createPromoLimitGroup(promoLimitGroupEntity);
                    if (promoLimitEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }
                    promoLimitGroupRequest.setId(promoLimitGroupEntity.getId());

                    /**
                     * Lưu thông tin ưu đãi - promo_offer của từng nhóm
                     */
                    PromoOfferRequest promoOfferRequest = promoLimitGroupRequest.getOffer();
                    if (PromoConditionType.PRODUCT_PRICE.getKey().equals(request.getPromo_info().getCondition_type())
                    ) {
                        promoOfferRequest.setConversion_ratio(promoOfferRequest.getOffer_value());
                    } else {
                        promoOfferRequest.setConversion_ratio(promoOfferRequest.getOffer_value() * 1.0 / promoLimitGroupRequest.getFrom_value() * 100);
                    }
                    PromoOfferEntity promoOfferEntity = this.createPromoOfferEntity(promoOfferRequest);
                    promoOfferEntity.setPromo_limit_group_id(promoLimitGroupEntity.getId());
                    promoOfferEntity.setPromo_limit_id(promoLimitGroupEntity.getPromo_limit_id());
                    promoOfferEntity.setPromo_id(promoEntity.getId());
                    promoOfferEntity = this.promoDB.createPromoOffer(promoOfferEntity);
                    if (promoOfferEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }

                    /**
                     * Lưu thông tin ưu đãi sản phẩm
                     */
                    for (PromoOfferProductRequest promoOfferProductRequest : promoOfferRequest.getOffer_products()) {
                        promoOfferProductRequest.setProduct_name(this.dataManager.getProductManager().getProductFullName(promoOfferProductRequest.getProduct_id()));
                        promoOfferProductRequest.setProduct_code(this.dataManager.getProductManager().getProductCode(promoOfferProductRequest.getProduct_id()));
                        PromoOfferProductEntity promoOfferProductEntity = this.createPromoOfferProductEntity(promoOfferProductRequest);
                        promoOfferProductEntity.setPromo_offer_id(promoOfferEntity.getId());
                        promoOfferProductEntity.setPromo_id(promoEntity.getId());
                        promoOfferProductEntity = this.promoDB.createPromoOfferProduct(promoOfferProductEntity);
                        if (promoOfferProductEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                        }
                    }

                    /**
                     * Lưu thông tin ưu đãi tặng kèm
                     */
                    for (PromoOfferBonusRequest promoOfferBonusRequest : promoOfferRequest.getOffer_bonus()) {
                        promoOfferBonusRequest.setProduct_name(this.dataManager.getProductManager().getProductFullName(promoOfferBonusRequest.getProduct_id()));
                        promoOfferBonusRequest.setProduct_code(this.dataManager.getProductManager().getProductCode(promoOfferBonusRequest.getProduct_id()));
                        PromoOfferBonusEntity promoOfferBonusEntity = this.createPromoOfferBonusEntity(promoOfferBonusRequest);
                        promoOfferBonusEntity.setPromo_offer_id(promoOfferEntity.getId());
                        promoOfferBonusEntity.setPromo_id(promoEntity.getId());
                        promoOfferBonusEntity = this.promoDB.createPromoOfferBonus(promoOfferBonusEntity);
                        if (promoOfferBonusEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                        }
                    }
                }
            }

            /**
             * Lưu lịch sử
             *
             */
            request.getPromo_info().setId(promoEntity.getId());
            request.getPromo_info().setCode(code);
            request.getPromo_info().setStart_date(DateTimeUtils.toString(promoEntity.getStart_date()));
            request.getPromo_info().setEnd_date(promoEntity.getEnd_date() == null ? null : DateTimeUtils.toString(promoEntity.getEnd_date()));
            this.insertPromoHistory(
                    promoEntity.getId(),
                    promoEntity.getStart_date(),
                    promoEntity.getEnd_date(),
                    JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())),
                    "Tạo mới",
                    PromoActiveStatus.DRAFT.getId(),
                    sessionData.getId());

            JSONObject data = new JSONObject();
            data.put("id", promoEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse updateDamMeRunning(
            int promo_id,
            CreatePromoRequest promo_data) {
        try {
            PromoRunningEntity promoRunningEntity = this.promoDB.getDamMeRunningById(promo_id);
            if (promoRunningEntity == null) {
                promoRunningEntity = new PromoRunningEntity();
                promoRunningEntity.setPromo_id(promo_id);
                promoRunningEntity.setPromo_data(
                        JsonUtils.Serialize(promo_data))
                ;
                int rsInsert = this.promoDB.insertDamMeRunning(promoRunningEntity);
                if (rsInsert <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            } else {
                promoRunningEntity.setPromo_id(promo_id);
                promoRunningEntity.setPromo_data(
                        JsonUtils.Serialize(promo_data))
                ;
                boolean rsUpdate = this.promoDB.updateDamMeRunning(promoRunningEntity);
                if (!rsUpdate) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editDamMe(SessionData sessionData, EditPromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            PromoEntity oldPromoEntity = this.promoDB.getPromo(request.getPromo_info().getId());
            if (oldPromoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            CreatePromoRequest oldPromoData = this.convertPromoToData(request.getPromo_info().getId());

            if (!request.getPromo_info().getPromo_type().equals(oldPromoData.getPromo_info().getPromo_type())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_EDIT_PROMO_TYPE);
            }
            if (!request.getPromo_info().getCondition_type().equals(oldPromoData.getPromo_info().getCondition_type())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_EDIT_CONDITION_TYPE);
            }
            /**
             * Ràng buộc chỉnh sửa chính sách
             */
            clientResponse = this.validateCreateDamMe(request);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            PromoEntity promoEntity = this.createPromoEntity(request.getPromo_info());
            promoEntity.setPriority(oldPromoEntity.getPriority());
            promoEntity.setId(oldPromoEntity.getId());
            promoEntity.setCode(oldPromoEntity.getCode());

            if (oldPromoEntity.getStatus() == PromoActiveStatus.RUNNING.getId()) {
                promoEntity.setStart_date(oldPromoEntity.getStart_date());
            } else {
                promoEntity.setStart_date(new Date(request.getPromo_info().getStart_date_millisecond()));
            }
            promoEntity.setEnd_date(request.getPromo_info().getEnd_date_millisecond() == 0 ? null : new Date(request.getPromo_info().getEnd_date_millisecond()));
            promoEntity.setPromo_type(request.getPromo_info().getPromo_type());
            promoEntity.setCondition_type(request.getPromo_info().getCondition_type());
            promoEntity.setOffer_info(this.createOfferInfo(request.getPromo_limits()));
            promoEntity.setStatus(oldPromoEntity.getStatus());

            if (request.getRepeat_data() != null &&
                    request.getRepeat_data().getType() != 0) {
                promoEntity.setRepeat_type(request.getRepeat_data().getType());
                promoEntity.setRepeat_data(JsonUtils.Serialize(request.getRepeat_data()));
            }

            if (request.getPromo_info().getPromo_type().equals(PromoType.CTTL.getKey())) {
                promoEntity.setOrder_date_data(
                        JsonUtils.Serialize(
                                request.getOrder_date_data()
                        )
                );

                promoEntity.setPayment_date_data(
                        JsonUtils.Serialize(
                                request.getPayment_date_data()
                        )
                );

                promoEntity.setReward_date_data(
                        JsonUtils.Serialize(
                                request.getReward_date_data()
                        )
                );

                promoEntity.setConfirm_result_date_data(
                        JsonUtils.Serialize(
                                request.getConfirm_result_date_data()
                        )
                );
            }

            int statusEdit = oldPromoEntity.getStatus();
            if (PromoActiveStatus.RUNNING.getId() != oldPromoEntity.getStatus()) {
                /**
                 * Chính sách chưa chạy hoặc đã kết thúc
                 * 1. Xóa dữ liệu cũ
                 * 2. Lưu thông tin mới
                 * 3. Lưu lịch sử
                 */

                /**
                 * Xóa promo cũ
                 */
                boolean rsDeletePromoPre = this.deletePromoPre(request.getPromo_info().getId());
                if (!rsDeletePromoPre) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * Lưu thông tin chính sách
                 */
                clientResponse = this.updatePromoNew(promoEntity, request);
                if (clientResponse.failed()) {
                    return clientResponse;
                }

                if (PromoActiveStatus.WAITING.getId() == promoEntity.getStatus()) {
                    this.cancelPromoScheduleWaitingStart(request.getPromo_info().getId());
                    this.cancelPromoScheduleWaitingStop(request.getPromo_info().getId());
                    this.createPromoSchedule(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            PromoScheduleType.START);
                }
            } else {
                /**
                 * Chính sách đang chạy
                 * 1. Nếu thời gian bắt đầu < thời gian hiện tại:
                 * - xóa chính sách cũ
                 * - chạy ngay chính sách mới
                 * - đặt lịch kết thúc nếu có
                 * 2. Nếu thời gian bắt đầu > thời gian hiện tại:
                 * - hẹn giờ thực thi chính sách
                 */
                if (request.getPromo_info().getStart_date_millisecond() <= DateTimeUtils.getMilisecondsNow()) {
                    /**
                     * 1. Nếu thời gian bắt đầu < thời gian hiện tại
                     * - xóa chính sách cũ
                     * - chạy ngay chính sách mới
                     * - cập nhật lịch kết thúc nếu có
                     */

                    /**
                     * Xóa dữ liệu cơ cấu của chính sách cũ
                     */
                    boolean rsDeletePromoPre = this.deletePromoPre(request.getPromo_info().getId());
                    if (!rsDeletePromoPre) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * Lưu thông tin chính sách mới
                     */
                    clientResponse = this.updatePromoNew(promoEntity, request);
                    if (clientResponse.failed()) {
                        return clientResponse;
                    }

                    /**
                     * cập nhật lịch chính sách nếu có
                     * - Hủy lịch kích hoạt nếu có
                     * - Hủy lịch kết thúc cũ nếu có
                     * - Tạo lích kết thúc mới nếu có thời gian kết thúc
                     */
                    this.cancelPromoScheduleWaitingStart(request.getPromo_info().getId());
                    this.cancelPromoScheduleWaitingStop(request.getPromo_info().getId());
                    if (promoEntity.getEnd_date() != null) {
                        this.createPromoSchedule(
                                promoEntity.getId(),
                                promoEntity.getEnd_date(),
                                PromoScheduleType.STOP);
                    }

                    /**
                     * Chạy ngay chính sách mới
                     */
                    PromoRunningEntity promoRunningEntity = this.promoDB.getDamMeRunningById(promoEntity.getId());
                    if (promoRunningEntity == null) {
                        promoRunningEntity = new PromoRunningEntity();
                    }
                    promoRunningEntity.setPromo_id(promoEntity.getId());
                    CreatePromoRequest promo_data = this.convertPromoToData(promoEntity.getId());
                    promoRunningEntity.setPromo_data(JsonUtils.Serialize(promo_data));
                    ClientResponse crUpdateDamMeRunning = this.updateDamMeRunning(promoEntity.getId(), promo_data);
                    if (crUpdateDamMeRunning.failed()) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadDamMeRunning(
                            promoEntity.getId(),
                            PromoScheduleType.START,
                            promoEntity.getPromo_type()
                    );
                } else {
                    /**
                     * 2. Nếu thời gian bắt đầu > thời gian hiện tại:
                     * - Lưu thông tin chính sách vào lịch sử
                     * - Hủy lịch kích hoạt cũ nếu có
                     * - Hủy lịch kết thúc cũ nếu có
                     * - Tạo lích kích hoạt mới
                     */
                    this.cancelPromoScheduleWaitingStart(request.getPromo_info().getId());
                    this.cancelPromoScheduleWaitingStop(request.getPromo_info().getId());
                    this.createPromoSchedule(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            PromoScheduleType.START);

                    statusEdit = PromoActiveStatus.WAITING.getId();
                }
            }

            /**
             * Lưu lịch sử
             *
             */
            request.getPromo_info().setId(promoEntity.getId());
            request.getPromo_info().setCode(promoEntity.getCode());
            request.getPromo_info().setStart_date(DateTimeUtils.toString(promoEntity.getStart_date()));
            request.getPromo_info().setEnd_date(promoEntity.getEnd_date() == null ? null : DateTimeUtils.toString(promoEntity.getEnd_date()));
            request.getPromo_info().setStatus(statusEdit);
            convertPromoDataHistory(request);

            this.insertPromoHistory(
                    promoEntity.getId(),
                    promoEntity.getStart_date(),
                    promoEntity.getEnd_date(),
                    JsonUtils.Serialize(request),
                    request.getNote(),
                    statusEdit,
                    sessionData.getId());
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse removeAgencyIgnoreToListPromo(String request) {
        try {
            List<JSONObject> records = this.promoDB.getListPromoAgencyIngore(
                    request);
            for (JSONObject record : records) {
                this.removeAgencyIgnoreToListPromoOne(record);
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createCTXH(SessionData sessionData, CreatePromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Ràng buộc tạo chính sách
             */
            clientResponse = this.validateCreateCTXH(request);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Tạo mã chính sách
             */
            String code = this.generatePromoCode(request.getPromo_info().getPromo_type());
            if (code.isEmpty()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
            }

            /**
             * Lưu thông tin chính sách
             */
            PromoEntity promoEntity = this.createPromoEntity(request.getPromo_info());
            promoEntity.setCode(code);
            promoEntity.setStart_date(new Date(request.getPromo_info().getStart_date_millisecond()));
            promoEntity.setEnd_date(request.getPromo_info().getEnd_date_millisecond() == 0 ? null : new Date(request.getPromo_info().getEnd_date_millisecond()));
            promoEntity.setPromo_type(request.getPromo_info().getPromo_type());
            promoEntity.setCondition_type(request.getPromo_info().getCondition_type());
            promoEntity.setOffer_info(this.createOfferInfo(request.getPromo_limits()));
            promoEntity.setStatus(PromoActiveStatus.DRAFT.getId());

            if (request.getRepeat_data() != null && request.getRepeat_data().getType() != 0) {
                promoEntity.setRepeat_type(request.getRepeat_data().getType());
                promoEntity.setRepeat_data(JsonUtils.Serialize(
                        request.getRepeat_data()
                ));
            }

            promoEntity = this.promoDB.createPromo(promoEntity);
            if (promoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
            }

            /**
             * Luu đối tượng áp dụng
             */
            if (request.getPromo_apply_object() != null) {
                PromoApplyObjectEntity promoApplyObjectEntity = this.createPromoApplyObjectEntity(request.getPromo_apply_object());
                promoApplyObjectEntity.setPromo_id(promoEntity.getId());
                promoApplyObjectEntity = this.promoDB.createPromoApplyObject(promoApplyObjectEntity);
                if (promoApplyObjectEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }

                /**`
                 * Bộ lọc
                 */
                for (PromoApplyFilterRequest promoApplyFilterRequest : request.getPromo_apply_object().getPromo_filters()) {
                    PromoFilterEntity promoFilterEntity = this.createPromoFilterEntity(promoApplyFilterRequest);
                    promoFilterEntity.setPromo_id(promoEntity.getId());
                    promoFilterEntity = this.promoDB.createPromoFilter(promoFilterEntity);
                    if (promoFilterEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }
                }
            }

            /**
             * Lưu nhóm sản phẩm áp dụng
             */
            for (int iGroup = 0; iGroup < request.getPromo_item_groups().size(); iGroup++) {
                PromoItemGroupRequest promoItemGroupRequest = request.getPromo_item_groups().get(iGroup);
                promoItemGroupRequest.setData_index(iGroup + 1);
                PromoItemGroupEntity promoItemGroupEntity = this.createPromoItemGroupEntity(promoItemGroupRequest);
                promoItemGroupEntity.setPromo_id(promoEntity.getId());
                promoItemGroupEntity.setData_index(iGroup + 1);
                promoItemGroupEntity = this.promoDB.createPromoItemGroup(promoItemGroupEntity);
                if (promoItemGroupEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }
                promoItemGroupRequest.setId(promoItemGroupEntity.getId());

                for (PromoItemGroupDetailRequest promoItemGroupDetailRequest : promoItemGroupRequest.getProducts()) {
                    if (promoItemGroupDetailRequest.getItem_type().equals(DamMeProductType.PRODUCT.getKey())) {
                        promoItemGroupDetailRequest.setItem_name(this.dataManager.getProductManager().getProductFullName(promoItemGroupDetailRequest.getItem_id()));
                        promoItemGroupDetailRequest.setItem_code(this.dataManager.getProductManager().getProductCode(promoItemGroupDetailRequest.getItem_id()));
                    } else if (promoItemGroupDetailRequest.getCategory_level() != 0 ||
                            promoItemGroupDetailRequest.getItem_type().equals(DamMeProductType.CATEGORY.getKey())) {
                        promoItemGroupDetailRequest.setCategory_level(
                                this.dataManager.getProductManager().getCategoryById(
                                        promoItemGroupDetailRequest.getItem_id()
                                ).getCategory_level()
                        );
                        promoItemGroupDetailRequest.setItem_type(
                                DamMeProductType.CATEGORY.getKey()
                        );
                    }
                    PromoItemGroupDetailEntity promoItemGroupDetailEntity = this.createPromoItemGroupDetailEntity(promoItemGroupDetailRequest);
                    promoItemGroupDetailEntity.setPromo_item_group_id(promoItemGroupEntity.getId());
                    promoItemGroupDetailEntity.setPromo_id(promoEntity.getId());

                    if (promoItemGroupRequest.getCombo_id() != 0) {
                        promoItemGroupDetailEntity.setNote(
                                promoItemGroupRequest.getNote()
                        );
                    }

                    promoItemGroupDetailEntity = this.promoDB.createPromoItemGroupDetail(promoItemGroupDetailEntity);
                    if (promoItemGroupDetailEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }
                }
            }

            /**
             * Lưu sản phẩm loại trừ
             */
            for (int iItem = 0; iItem < request.getPromo_item_ignores().size(); iItem++) {
                PromoItemIgnoreRequest promoItemIgnoreRequest = request.getPromo_item_ignores().get(iItem);
                promoItemIgnoreRequest.setItem_code(this.dataManager.getProductManager().getProductCode(promoItemIgnoreRequest.getItem_id()));
                promoItemIgnoreRequest.setItem_name(this.dataManager.getProductManager().getProductFullName(promoItemIgnoreRequest.getItem_id()));
                PromoItemIgnoreEntity promoItemIgnoreEntity = this.createPromoItemIgnoreEntity(promoItemIgnoreRequest);
                promoItemIgnoreEntity.setPromo_id(promoEntity.getId());
                promoItemIgnoreEntity = this.promoDB.createPromoItemIgnore(promoItemIgnoreEntity);
                if (promoItemIgnoreEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }
            }

            /**
             * Lưu thông tin hạn mức - promo_limit
             */
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                PromoLimitEntity promoLimitEntity = this.createPromoLimitEntity(promoLimitRequest);
                promoLimitEntity.setPromo_id(promoEntity.getId());
                promoLimitEntity.setLevel(iLimit + 1);
                promoLimitRequest.setLevel(iLimit + 1);
                promoLimitEntity = this.promoDB.createPromoLimit(promoLimitEntity);
                if (promoLimitEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                }
                promoLimitRequest.setId(promoLimitEntity.getId());

                /**
                 * Lưu thông tin chi tiết hạn mức của từng nhóm - promo_limit_group
                 */
                for (int iLimitGroup = 0; iLimitGroup < promoLimitRequest.getPromo_limit_groups().size(); iLimitGroup++) {
                    PromoLimitGroupRequest promoLimitGroupRequest = promoLimitRequest.getPromo_limit_groups().get(iLimitGroup);
                    promoLimitGroupRequest.setData_index(iLimitGroup + 1);
                    PromoLimitGroupEntity promoLimitGroupEntity = this.createPromoLimitGroupEntity(promoLimitGroupRequest);
                    promoLimitGroupEntity.setPromo_limit_id(promoLimitEntity.getId());
                    promoLimitGroupEntity.setData_index(iLimitGroup + 1);
                    promoLimitGroupEntity.setPromo_id(promoEntity.getId());

                    promoLimitGroupEntity = this.promoDB.createPromoLimitGroup(promoLimitGroupEntity);
                    if (promoLimitEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }
                    promoLimitGroupRequest.setId(promoLimitGroupEntity.getId());

                    /**
                     * Lưu thông tin ưu đãi - promo_offer của từng nhóm
                     */
                    PromoOfferRequest promoOfferRequest = promoLimitGroupRequest.getOffer();
                    if (PromoConditionType.PRODUCT_PRICE.getKey().equals(request.getPromo_info().getCondition_type())
                    ) {
                        promoOfferRequest.setConversion_ratio(promoOfferRequest.getOffer_value());
                    } else {
                        promoOfferRequest.setConversion_ratio(promoOfferRequest.getOffer_value() * 1.0 / promoLimitGroupRequest.getFrom_value() * 100);
                    }

                    PromoOfferEntity promoOfferEntity = this.createPromoOfferEntity(promoOfferRequest);
                    promoOfferEntity.setPromo_limit_group_id(promoLimitGroupEntity.getId());
                    promoOfferEntity.setPromo_limit_id(promoLimitGroupEntity.getPromo_limit_id());
                    promoOfferEntity.setPromo_id(promoEntity.getId());

                    /**
                     * Lưu voucher
                     */
                    String voucher_data = promoOfferRequest.getVoucher_data();
                    promoOfferEntity.setVoucher_data(
                            voucher_data
                    );

                    promoOfferEntity = this.promoDB.createPromoOffer(promoOfferEntity);
                    if (promoOfferEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                    }

                    /**
                     * Lưu thông tin ưu đãi sản phẩm
                     */
                    for (PromoOfferProductRequest promoOfferProductRequest : promoOfferRequest.getOffer_products()) {
                        promoOfferProductRequest.setProduct_name(this.dataManager.getProductManager().getProductFullName(promoOfferProductRequest.getProduct_id()));
                        promoOfferProductRequest.setProduct_code(this.dataManager.getProductManager().getProductCode(promoOfferProductRequest.getProduct_id()));
                        PromoOfferProductEntity promoOfferProductEntity = this.createPromoOfferProductEntity(promoOfferProductRequest);
                        promoOfferProductEntity.setPromo_offer_id(promoOfferEntity.getId());
                        promoOfferProductEntity.setPromo_id(promoEntity.getId());
                        promoOfferProductEntity = this.promoDB.createPromoOfferProduct(promoOfferProductEntity);
                        if (promoOfferProductEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                        }
                    }

                    /**
                     * Lưu thông tin ưu đãi tặng kèm
                     */
                    for (PromoOfferBonusRequest promoOfferBonusRequest : promoOfferRequest.getOffer_bonus()) {
                        promoOfferBonusRequest.setProduct_name(this.dataManager.getProductManager().getProductFullName(promoOfferBonusRequest.getProduct_id()));
                        promoOfferBonusRequest.setProduct_code(this.dataManager.getProductManager().getProductCode(promoOfferBonusRequest.getProduct_id()));
                        PromoOfferBonusEntity promoOfferBonusEntity = this.createPromoOfferBonusEntity(promoOfferBonusRequest);
                        promoOfferBonusEntity.setPromo_offer_id(promoOfferEntity.getId());
                        promoOfferBonusEntity.setPromo_id(promoEntity.getId());
                        promoOfferBonusEntity = this.promoDB.createPromoOfferBonus(promoOfferBonusEntity);
                        if (promoOfferBonusEntity == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CREATE_PROMO_FAIL);
                        }
                    }
                }
            }

            /**
             * Lưu lịch sử
             *
             */
            request.getPromo_info().setId(promoEntity.getId());
            request.getPromo_info().setCode(code);
            request.getPromo_info().setStart_date(DateTimeUtils.toString(promoEntity.getStart_date()));
            request.getPromo_info().setEnd_date(promoEntity.getEnd_date() == null ? null : DateTimeUtils.toString(promoEntity.getEnd_date()));
            this.insertPromoHistory(
                    promoEntity.getId(),
                    promoEntity.getStart_date(),
                    promoEntity.getEnd_date(),
                    JsonUtils.Serialize(this.convertPromoToData(promoEntity.getId())),
                    "Tạo mới",
                    PromoActiveStatus.DRAFT.getId(),
                    sessionData.getId());

            JSONObject data = new JSONObject();
            data.put("id", promoEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse validateCreateCTXH(CreatePromoRequest request) {
        if (request.getPromo_info().getStart_date_millisecond() <= 0 ||
                request.getPromo_info().getEnd_date_millisecond() <= 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_INVALID);
        }
        /**
         * Loại xếp hàng
         */
        if (request.getPromo_info().getCircle_type() == null ||
                request.getPromo_info().getCircle_type().isEmpty()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
        }

        /**
         * Chính sách không trả thưởng tự động
         * - thì không có bước nhảy
         * Chính sách trả thưởng tự động
         * - chỉ áp dụng cho nhóm có 1 sản phẩm
         */
        if (request.getPromo_info().getIs_automatic_allocation() == 0) {
            for (PromoLimitRequest promoLimitRequest : request.getPromo_limits()) {
                if (PromoConditionType.STEP.getKey().equals(promoLimitRequest.getCondition_type())) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_AUTOMATIC_ALLOCATION_NOT_STEP);
                }
            }
        }

        /**
         * Loại giá trị đến của chính sách
         * 1. Chính sách có giá trị đến thì end_value của nhóm hạn mức phải != 0, trừ hạn mức cuối
         * 2. Chính sách không có giá trị đến thì các end_value của nhóm hạn mức phải = 0
         *
         */
        if (PromoEndValueType.IS_NOT_NULL.getKey().equals(request.getPromo_info().getPromo_end_value_type())) {
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                for (int iGroup = 0; iGroup < promoLimitRequest.getPromo_limit_groups().size(); iGroup++) {
                    PromoLimitGroupRequest promoLimitGroupRequest = promoLimitRequest.getPromo_limit_groups().get(iGroup);
                    if (iLimit != request.getPromo_limits().size() - 1 && promoLimitGroupRequest.getEnd_value() == 0) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_END_VALUE_TYPE_IS_NOT_NULL_INVALID);
                        clientResponse.setMessage("[Hạn mức " + (iLimit + 1) + "][Nhóm " + (iGroup + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }
        } else if (PromoEndValueType.IS_NULL.getKey().equals(request.getPromo_info().getPromo_end_value_type())) {
            for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
                PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
                for (PromoLimitGroupRequest promoLimitGroupRequest : promoLimitRequest.getPromo_limit_groups()) {
                    if (promoLimitGroupRequest.getEnd_value() != 0) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_END_VALUE_TYPE_IS_NULL_INVALID);
                    }
                }
            }
        }

        /**
         * Sản phẩm áp dụng không thể chứa sản phẩm loại trừ
         */
        for (int iItemIgnore = 0; iItemIgnore < request.getPromo_item_ignores().size(); iItemIgnore++) {
            for (int iGroup = 0; iGroup < request.getPromo_item_groups().size(); iGroup++) {
                for (int iItemGroup = 0; iItemGroup < request.getPromo_item_groups().get(iGroup).getProducts().size(); iItemGroup++) {
                    if (request.getPromo_item_groups().get(iGroup).getProducts().get(iItemGroup).getItem_id()
                            == request.getPromo_item_ignores().get(iItemIgnore).getItem_id()) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_ITEM_GROUP_CONTAIN_ITEM_IGNORE);
                        clientResponse.setMessage("[Nhóm " + (iGroup + 1) + "][Sản phẩm thứ " + (iItemGroup + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }
        }

        /**
         * Đại lý chỉ định không trùng với đại lý loại trừ
         */
        if (request.getPromo_apply_object() != null) {
            for (int iDaiLyChiDinh = 0; iDaiLyChiDinh < request.getPromo_apply_object().getPromo_agency_includes().size(); iDaiLyChiDinh++) {
                for (int iDaiLyLoaiTru = 0; iDaiLyLoaiTru < request.getPromo_apply_object().getPromo_agency_ignores().size(); iDaiLyLoaiTru++) {
                    if (request.getPromo_apply_object().getPromo_agency_includes().get(iDaiLyChiDinh).getId()
                            == request.getPromo_apply_object().getPromo_agency_ignores().get(iDaiLyLoaiTru).getId()) {
                        ClientResponse clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_AGENCY_INCLUDE_CONTAIN_AGENCY_IGNORE);
                        clientResponse.setMessage("[Đại lý chỉ định thứ " + (iDaiLyChiDinh + 1) + "]" + clientResponse.getMessage());
                        return clientResponse;
                    }
                }
            }
        }

        /**
         * Bậc thang thì hạn mức phải có giá trị đến,
         * trừ hạn mức cuối
         */
        if (request.getPromo_info().getForm_of_reward().equals(PromoFormOfRewardType.BAC_THANG.getKey())) {
            for (int iHanMuc = 0; iHanMuc < request.getPromo_limits().size(); iHanMuc++) {
                if (iHanMuc != request.getPromo_limits().size() - 1) {
                    for (PromoLimitGroupRequest promoLimitGroupRequest : request.getPromo_limits().get(iHanMuc).getPromo_limit_groups()) {
                        if (promoLimitGroupRequest.getEnd_value() <= 0) {
                            ClientResponse crBT = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                            crBT.setMessage("[Hạn mức " + (iHanMuc + 1) + "]" +
                                    "CTTL Bậc thang nên cần nhập giá trị đến dưới");
                            return crBT;
                        }
                    }
                }
            }
        }

        /**
         * Kiểm tra guyên tắc phát hành
         * - không được rỗng
         * - expire_day > 0
         * - nguyên tắc phát hành đã kích hoạt
         */
        for (int iLimit = 0; iLimit < request.getPromo_limits().size(); iLimit++) {
            PromoLimitRequest promoLimitRequest = request.getPromo_limits().get(iLimit);
            boolean hasMoneyDiscount = false;
            for (int iGroup = 0; iGroup < promoLimitRequest.getPromo_limit_groups().size(); iGroup++) {
                int countMoneyDiscount = 0;
                PromoLimitGroupRequest promoLimitGroupRequest = promoLimitRequest.getPromo_limit_groups().get(iGroup);
                String voucher_data = promoLimitGroupRequest.getOffer().getVoucher_data();
                if (voucher_data == null || voucher_data.isEmpty()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                List<JSONObject> vouchers = JsonUtils.DeSerialize(voucher_data, new TypeToken<List<JSONObject>>() {
                }.getType());
                if (vouchers == null || vouchers.isEmpty()) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
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

                        if (promoLimitGroupRequest.getOffer().getOffer_value() == null ||
                                promoLimitGroupRequest.getOffer().getOffer_value() == 0) {
                            ClientResponse crVRP = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_TYPE_INVALID);
                            crVRP.setMessage("[" + voucherReleasePeriodEntity.getCode() + "] " + crVRP.getMessage());
                            return crVRP;
                        }

                        hasMoneyDiscount = true;
                    }

                    vrp.put("key", DigestUtils.md5Hex(ConvertUtils.toString(vrpDataRequest.getId())));
                }

                if (promoLimitGroupRequest.getOffer().getOffer_value() > 0 &&
                        hasMoneyDiscount == false

                ) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_OFFER_TYPE_INVALID);
                }

                promoLimitGroupRequest.getOffer().setVoucher_data(JsonUtils.Serialize(vouchers));
            }
        }

        return ClientResponse.success(null);
    }

    public ClientResponse editCTXH(SessionData sessionData, EditPromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            PromoEntity oldPromoEntity = this.promoDB.getPromo(request.getPromo_info().getId());
            if (oldPromoEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_FOUND);
            }

            CreatePromoRequest oldPromoData = this.convertPromoToData(request.getPromo_info().getId());

            if (!request.getPromo_info().getPromo_type().equals(oldPromoData.getPromo_info().getPromo_type())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_EDIT_PROMO_TYPE);
            }
            if (!request.getPromo_info().getCondition_type().equals(oldPromoData.getPromo_info().getCondition_type())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PROMO_NOT_EDIT_CONDITION_TYPE);
            }
            /**
             * Ràng buộc chỉnh sửa chính sách
             */
            clientResponse = this.validateCreateCTXH(request);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            PromoEntity promoEntity = this.createPromoEntity(request.getPromo_info());
            promoEntity.setPriority(oldPromoEntity.getPriority());
            promoEntity.setId(oldPromoEntity.getId());
            promoEntity.setCode(oldPromoEntity.getCode());
            promoEntity.setStart_date(new Date(request.getPromo_info().getStart_date_millisecond()));
            promoEntity.setEnd_date(request.getPromo_info().getEnd_date_millisecond() == 0 ? null : new Date(request.getPromo_info().getEnd_date_millisecond()));
            promoEntity.setPromo_type(request.getPromo_info().getPromo_type());
            promoEntity.setCondition_type(request.getPromo_info().getCondition_type());
            promoEntity.setOffer_info(this.createOfferInfo(request.getPromo_limits()));
            promoEntity.setStatus(oldPromoEntity.getStatus());

            if (request.getRepeat_data() != null &&
                    request.getRepeat_data().getType() != 0) {
                promoEntity.setRepeat_type(request.getRepeat_data().getType());
                promoEntity.setRepeat_data(JsonUtils.Serialize(request.getRepeat_data()));
            }

            if (request.getPromo_info().getPromo_type().equals(PromoType.CTTL.getKey())) {
                promoEntity.setOrder_date_data(
                        JsonUtils.Serialize(
                                request.getOrder_date_data()
                        )
                );

                promoEntity.setPayment_date_data(
                        JsonUtils.Serialize(
                                request.getPayment_date_data()
                        )
                );

                promoEntity.setReward_date_data(
                        JsonUtils.Serialize(
                                request.getReward_date_data()
                        )
                );

                promoEntity.setConfirm_result_date_data(
                        JsonUtils.Serialize(
                                request.getConfirm_result_date_data()
                        )
                );
            }

            int statusEdit = oldPromoEntity.getStatus();
            if (PromoActiveStatus.RUNNING.getId() != oldPromoEntity.getStatus()) {
                /**
                 * Chính sách chưa chạy hoặc đã kết thúc
                 * 1. Xóa dữ liệu cũ
                 * 2. Lưu thông tin mới
                 * 3. Lưu lịch sử
                 */

                /**
                 * Xóa promo cũ
                 */
                boolean rsDeletePromoPre = this.deletePromoPre(request.getPromo_info().getId());
                if (!rsDeletePromoPre) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * Lưu thông tin chính sách
                 */
                clientResponse = this.updatePromoNew(promoEntity, request);
                if (clientResponse.failed()) {
                    return clientResponse;
                }

                if (PromoActiveStatus.WAITING.getId() == promoEntity.getStatus()) {
                    this.cancelPromoScheduleWaitingStart(request.getPromo_info().getId());
                    this.cancelPromoScheduleWaitingStop(request.getPromo_info().getId());
                    this.createPromoSchedule(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            PromoScheduleType.START);
                }
            } else {
                /**
                 * Chính sách đang chạy
                 * 1. Nếu thời gian bắt đầu < thời gian hiện tại:
                 * - xóa chính sách cũ
                 * - chạy ngay chính sách mới
                 * - đặt lịch kết thúc nếu có
                 * 2. Nếu thời gian bắt đầu > thời gian hiện tại:
                 * - hẹn giờ thực thi chính sách
                 */
                if (request.getPromo_info().getStart_date_millisecond() <= DateTimeUtils.getMilisecondsNow()) {
                    /**
                     * 1. Nếu thời gian bắt đầu < thời gian hiện tại
                     * - xóa chính sách cũ
                     * - chạy ngay chính sách mới
                     * - cập nhật lịch kết thúc nếu có
                     */

                    /**
                     * Xóa dữ liệu cơ cấu của chính sách cũ
                     */
                    boolean rsDeletePromoPre = this.deletePromoPre(request.getPromo_info().getId());
                    if (!rsDeletePromoPre) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * Lưu thông tin chính sách mới
                     */
                    clientResponse = this.updatePromoNew(promoEntity, request);
                    if (clientResponse.failed()) {
                        return clientResponse;
                    }

                    /**
                     * cập nhật lịch chính sách nếu có
                     * - Hủy lịch kích hoạt nếu có
                     * - Hủy lịch kết thúc cũ nếu có
                     * - Tạo lích kết thúc mới nếu có thời gian kết thúc
                     */
                    this.cancelPromoScheduleWaitingStart(request.getPromo_info().getId());
                    this.cancelPromoScheduleWaitingStop(request.getPromo_info().getId());
                    if (promoEntity.getEnd_date() != null) {
                        this.createPromoSchedule(
                                promoEntity.getId(),
                                promoEntity.getEnd_date(),
                                PromoScheduleType.STOP);
                    }

                    /**
                     * Chạy ngay chính sách mới
                     */
                    BXHRunningEntity promoRunningEntity = this.promoDB.getCTXHRunningById(promoEntity.getId());
                    if (promoRunningEntity == null) {
                        promoRunningEntity = new BXHRunningEntity();
                    }
                    promoRunningEntity.setPromo_id(promoEntity.getId());
                    CreatePromoRequest promo_data = this.convertPromoToData(promoEntity.getId());
                    promoRunningEntity.setPromo_data(JsonUtils.Serialize(promo_data));
                    promoRunningEntity.setAgency_data(
                            JsonUtils.Serialize(this.getListAgencyByFilter(JsonUtils.Serialize(promo_data)))
                    );
                    ClientResponse crUpdateCTXHRunning = this.updateCTXHRunning(
                            promoEntity.getId(),
                            promo_data,
                            promoRunningEntity.getAgency_data());
                    if (crUpdateCTXHRunning.failed()) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }

                    /**
                     * reload cache promo
                     */
                    this.dataManager.getProgramManager().reloadCTXHRunning(
                            promoEntity.getId(),
                            PromoScheduleType.START,
                            promoEntity.getPromo_type()
                    );
                } else {
                    /**
                     * 2. Nếu thời gian bắt đầu > thời gian hiện tại:
                     * - Lưu thông tin chính sách vào lịch sử
                     * - Hủy lịch kích hoạt cũ nếu có
                     * - Hủy lịch kết thúc cũ nếu có
                     * - Tạo lích kích hoạt mới
                     */
                    this.cancelPromoScheduleWaitingStart(request.getPromo_info().getId());
                    this.cancelPromoScheduleWaitingStop(request.getPromo_info().getId());
                    this.createPromoSchedule(
                            promoEntity.getId(),
                            promoEntity.getStart_date(),
                            PromoScheduleType.START);

                    statusEdit = PromoActiveStatus.WAITING.getId();
                }
            }

            /**
             * Lưu lịch sử
             *
             */
            request.getPromo_info().setId(promoEntity.getId());
            request.getPromo_info().setCode(promoEntity.getCode());
            request.getPromo_info().setStart_date(DateTimeUtils.toString(promoEntity.getStart_date()));
            request.getPromo_info().setEnd_date(promoEntity.getEnd_date() == null ? null : DateTimeUtils.toString(promoEntity.getEnd_date()));
            request.getPromo_info().setStatus(statusEdit);
            convertPromoDataHistory(request);

            this.insertPromoHistory(
                    promoEntity.getId(),
                    promoEntity.getStart_date(),
                    promoEntity.getEnd_date(),
                    JsonUtils.Serialize(request),
                    request.getNote(),
                    statusEdit,
                    sessionData.getId());
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<Integer> getListAgencyByFilter(String promo_data) {
        List<Integer> agencyList = new ArrayList<>();

        Program program = this.dataManager.getProgramManager().importProgram(
                promo_data);
        if (program == null) {
            return agencyList;
        }

        List<Agency> jsAgencyList = this.dataManager.getProgramManager().getListAgencyReadyJoinCTXH();
        for (Agency agency : jsAgencyList) {
            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(
                    agency.getId());
            if (this.checkProgramFilter(
                    agency,
                    program,
                    Source.WEB,
                    deptInfo)) {
                agencyList.add(agency.getId());
            }
        }
        return agencyList;
    }

    private List<JSONObject> doubleCheckAgencyJoinCTXH(CreatePromoRequest request) {
        List<JSONObject> agencyList = new ArrayList<>();
        Program program = new Program();
        program.setType(ProgramType.BXH);
        program.setCircle_type(request.getPromo_info().getCircle_type());
        program.setLtIncludeAgencyId(
                request.getPromo_apply_object().getPromo_agency_includes().stream().map(e ->
                        e.getId()
                ).collect(Collectors.toList()));
        program.setLtIgnoreAgencyId(
                request.getPromo_apply_object().getPromo_agency_ignores().stream().map(e ->
                        e.getId()
                ).collect(Collectors.toList()));
        this.dataManager.getProgramManager().parseFilter(program, request.getPromo_apply_object().getPromo_filters());

        List<JSONObject> promoCheckList = this.dataManager.getProgramManager().getPromoDB().getAllCTXHNotStopByCircle(program.getCircle_type());
        List<Agency> jsAgencyList = this.dataManager.getProgramManager().getListAgencyReadyJoinCTXH();
        for (Agency agency : jsAgencyList) {
            if (this.dataManager.checkProgramFilter(
                    agency,
                    program)) {
                List<JSONObject> promoList = this.dataManager.getAllProgramCanJoin(
                        agency,
                        program.getCircle_type(),
                        promoCheckList);
                if (promoList.size() > 0) {
                    JSONObject jsAgency = new JSONObject();
                    jsAgency.put("id", agency.getId());
                    jsAgency.put("shop_name", agency.getShop_name());
                    jsAgency.put("code", agency.getCode());
                    jsAgency.put("membership_id", agency.getMembershipId());
                    jsAgency.put("avatar", agency.getAvatar());
                    jsAgency.put("promos", promoList);
                    agencyList.add(jsAgency);
                }
            }
        }
        return agencyList;
    }

    private List<String> convertPromoFilterByType(List<PromoApplyFilterRequest> promoFilters, ProgramFilterType programFilterType) {
        for (PromoApplyFilterRequest promoApplyFilterRequest : promoFilters) {
            for (PromoApplyFilterDetailRequest type : promoApplyFilterRequest.getFilter_types()) {
                if (programFilterType.getKey().equals(type.getFilter_type())) {
                    List<Integer> data = JsonUtils.DeSerialize(type.getFilter_data(),
                            new TypeToken<List<Integer>>() {
                            }.getType());
                    return data.stream().map(
                            e -> ConvertUtils.toString(e)
                    ).collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }


    private ClientResponse updateCTXHRunning(
            int promo_id,
            CreatePromoRequest promo_data, String agency_data) {
        try {
            BXHRunningEntity promoRunningEntity = this.promoDB.getCTXHRunningById(promo_id);
            if (promoRunningEntity == null) {
                promoRunningEntity = new BXHRunningEntity();
                promoRunningEntity.setPromo_id(promo_id);
                promoRunningEntity.setPromo_data(
                        JsonUtils.Serialize(promo_data));
                int rsInsert = this.promoDB.insertCTXHRunning(promoRunningEntity);
                if (rsInsert <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                List<Integer> agencyList = this.getListAgencyByFilter(JsonUtils.Serialize(promo_data));
                this.promoDB.updateAgencyDataForCTXH(
                        promo_id,
                        JsonUtils.Serialize(agencyList));
                for (Integer agency_id : agencyList) {
                    this.promoDB.insertBXHAgencyJoin(agency_id, promo_id);
                }
            } else {
                promoRunningEntity.setPromo_id(promo_id);
                promoRunningEntity.setPromo_data(
                        JsonUtils.Serialize(promo_data))
                ;
                boolean rsUpdate = this.promoDB.updateCTXHRunning(promoRunningEntity);
                if (!rsUpdate) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                this.promoDB.updateAgencyDataForCTXH(
                        promo_id,
                        JsonUtils.Serialize(this.getListAgencyByFilter(JsonUtils.Serialize(promo_data))));
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse stopCTXHRunning(
            int promo_id,
            CreatePromoRequest promo_data) {
        try {
            BXHRunningEntity promoRunningEntity = this.promoDB.getCTXHRunningById(promo_id);
            if (promoRunningEntity != null) {
                promoRunningEntity.setPromo_id(promo_id);
                promoRunningEntity.setPromo_data(
                        JsonUtils.Serialize(promo_data))
                ;
                boolean rsUpdate = this.promoDB.updateCTXHRunning(promoRunningEntity);
                if (!rsUpdate) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void removeAgencyIgnoreToListPromoOne(JSONObject record) {
        try {
            int agency_id = ConvertUtils.toInt(record.get("agency_id"));
            String agency_code = ConvertUtils.toString(record.get("agency_code"));
            int promo_id = ConvertUtils.toInt(record.get("promo_id"));

            PromoEntity promoEntity = this.promoDB.getPromo(promo_id);
            if (promoEntity == null) {
                this.alertToTelegram(JsonUtils.Serialize(record), ResponseStatus.FAIL);
            }
            PromoApplyObjectEntity promoApplyObjectEntity = this.promoDB.getPromoApplyObject(
                    promo_id
            );
            List<AgencyBasicData> agencyList =
                    JsonUtils.DeSerialize(
                            promoApplyObjectEntity.getPromo_agency_ignore(),
                            new TypeToken<List<AgencyBasicData>>() {
                            }.getType());

            List<AgencyBasicData> newAgencyList = new ArrayList<>();
            for (AgencyBasicData agencyBasicData : agencyList) {
                if (agencyBasicData.getId() != agency_id) {
                    newAgencyList.add(agencyBasicData);
                }
            }

            Date start_date = new Date();

            promoApplyObjectEntity.setPromo_agency_ignore(
                    JsonUtils.Serialize(newAgencyList)
            );
            boolean rsUpdate = this.promoDB.updatePromoApplyObject(
                    promo_id,
                    promoApplyObjectEntity.getPromo_agency_include(),
                    promoApplyObjectEntity.getPromo_agency_ignore()
            );

            if (!rsUpdate) {
                this.alertToTelegram(JsonUtils.Serialize(record), ResponseStatus.FAIL);
            }
            this.createPromoRunning(promo_id);
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
    }

    public void calculateRewardCTXH(int promo_id) {
        try {
            Thread thread = new Thread() {
                public void run() {
                    /**
                     * - /bxh/reward_program
                     *     + key : reload key
                     *     + program_id (int)
                     */
                    RestTemplate restTemplate = new RestTemplate();
                    String url = ConfigInfo.ACCUMULATE_URL + "/bxh/reward_program?" +
                            "key=" + ConfigInfo.ACCUMULATE_KEY
                            + "&program_id=" + promo_id;
                    LogUtil.printDebug("ACCUMULATE: " + url);
                    ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
                    LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
                }
            };
            thread.start();
        } catch (Exception ex) {
            LogUtil.printDebug("CACHE", ex);
        }
    }

    public ClientResponse filterCTXHByAgency(SessionData sessionData, FilterListByIdRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            int agency_id = request.getId();

            Agency agency = this.dataManager.getProgramManager().getAgency(agency_id);
            if (agency != null) {
            }
            List<JSONObject> ctxhJoinList = this.promoDB.getListCTXHJoinByAgency(agency_id);
            List<JSONObject> ctxhDateRunning = this.promoDB.getListCTXHRunningByAgency(agency_id, CircleType.DATE.getCode());
            List<JSONObject> ctxhYearRunning = this.promoDB.getListCTXHRunningByAgency(agency_id, CircleType.YEAR.getCode());
            boolean hasCTXHDateRunning = ctxhDateRunning.size() > 0;
            boolean hasCTXHYearRunning = ctxhYearRunning.size() > 0;
            List<String> strJoinList = ctxhJoinList.stream().map(
                    e -> ConvertUtils.toString(e.get("promo_id"))).collect(Collectors.toList());
            FilterRequest promoRequest = new FilterRequest();
            promoRequest.setType(TypeFilter.SQL);
            promoRequest.setValue("'" + JsonUtils.Serialize(strJoinList) + "' LIKE CONCAT('%\"',id,'\"%')");
            request.getFilters().add(promoRequest);
            String query = this.filterUtils.getQuery(
                    FunctionList.LIST_CTXH_RUNNING,
                    request.getFilters(),
                    request.getSorts()
            );
            List<JSONObject> records = this.promoDB.filter(query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE,
                    request.getIsLimit());
            for (JSONObject js : records) {
                js.put("join", true);
            }
            int total = this.promoDB.getTotal(query);
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

    public ClientResponse doubleCheckPromoCTXH(SessionData sessionData, CreatePromoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Ràng buộc tạo chính sách
             */
            clientResponse = this.validateCreateCTXH(request);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Danh sách đại lý
             */
            List<JSONObject> records = this.doubleCheckAgencyJoinCTXH(request);
            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getListCTXHCanJoin(SessionData sessionData, BasicRequest request) {
        try {
            List<JSONObject> records = new ArrayList<>();
            JSONObject agency = this.agencyDB.getAgencyInfo(request.getId());
            if (agency == null) {
                JSONObject data = new JSONObject();
                data.put("records", records);
                return ClientResponse.success(data);
            }

            int agency_status = ConvertUtils.toInt(agency.get("status"));
            if (!(agency_status == AgencyStatus.APPROVED.getValue() || agency_status == AgencyStatus.LOCK.getValue())) {
                JSONObject data = new JSONObject();
                data.put("records", records);
                return ClientResponse.success(data);
            }
            List<JSONObject> promoDateJoinList = this.promoDB.getListCTXHRunningByAgency(
                    request.getId(), CircleType.DATE.getCode());
            if (promoDateJoinList.size() == 0) {
                List<Program> programList = this.dataManager.getListProgramCanJoin(
                        this.dataManager.getProgramManager().getAgency(request.getId()),
                        CircleType.DATE
                );
                for (Program program : programList) {
                    JSONObject jsPromo = new JSONObject();
                    jsPromo.put("id", program.getId());
                    jsPromo.put("name", program.getName());
                    jsPromo.put("code", program.getCode());
                    jsPromo.put("circle_type", program.getCircle_type());
                    jsPromo.put("start_date", program.getStartDate());
                    jsPromo.put("end_date", program.getStartDate());
                    jsPromo.put("status", program.getStatus());
                    records.add(jsPromo);
                }
            }

            List<JSONObject> promoYearJoinList = this.promoDB.getListCTXHRunningByAgency(
                    request.getId(), CircleType.YEAR.getCode());
            if (promoYearJoinList.size() == 0) {
                List<Program> programList = this.dataManager.getListProgramCanJoin(
                        this.dataManager.getProgramManager().getAgency(request.getId()),
                        CircleType.YEAR
                );
                for (Program program : programList) {
                    JSONObject jsPromo = new JSONObject();
                    jsPromo.put("id", program.getId());
                    jsPromo.put("name", program.getName());
                    jsPromo.put("code", program.getCode());
                    jsPromo.put("circle_type", program.getCircle_type());
                    jsPromo.put("start_date", program.getStartDate());
                    jsPromo.put("end_date", program.getStartDate());
                    jsPromo.put("status", program.getStatus());
                    records.add(jsPromo);
                }
            }

            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public int saveStatistic(int promo_id) {
        try {
            /**
             * - /tl/save_statistic
             *     + key : reload key
             *     + program_id (int)
             */
            RestTemplate restTemplate = new RestTemplate();
            String url = ConfigInfo.ACCUMULATE_URL + "/tl/save_statistic?" +
                    "key=" + ConfigInfo.ACCUMULATE_KEY
                    + "&program_id=" + promo_id;
            LogUtil.printDebug("ACCUMULATE: " + url);
            ClientResponse rs = restTemplate.getForObject(url, ClientResponse.class);
            LogUtil.printDebug("ACCUMULATE: " + JsonUtils.Serialize(rs));
            return ConvertUtils.toInt(rs.getData());
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PROMO.name(), ex);
            return 0;
        }
    }
}