package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.Membership;
import com.app.server.data.dto.location.Region;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.product.ProductData;
import com.app.server.data.dto.staff.BusinessDepartment;
import com.app.server.data.dto.staff.MenuData;
import com.app.server.data.dto.staff.MenuItemInfo;
import com.app.server.data.dto.staff.Staff;
import com.app.server.data.dto.warehouse.WarehouseTypeData;
import com.app.server.data.entity.AgencyEntity;
import com.app.server.data.entity.ProductBigUnitEntity;
import com.app.server.data.entity.ProductColorEntity;
import com.app.server.data.entity.ProductSmallUnitEntity;
import com.app.server.data.dto.location.City;
import com.app.server.data.dto.location.District;
import com.app.server.data.dto.location.Ward;
import com.app.server.data.dto.product.Category;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.BasicResponse;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.SearchRequest;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.dept.ApproveDeptSettingRequest;
import com.app.server.data.request.product.FilterProductByAgencyRequest;
import com.app.server.data.request.product.ProductDataRequest;
import com.app.server.data.request.product.SearchProductDataRequest;
import com.app.server.data.request.promo.PromoItemGroupDetailRequest;
import com.app.server.data.response.product.ProductTreeResponse;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.data.response.FilterContentResponse;
import com.app.server.data.response.agency.AgencyStatusResponse;
import com.app.server.data.response.extra.ConfigResponse;
import com.app.server.data.response.location.CityResponse;
import com.app.server.data.response.location.DistrictResponse;
import com.app.server.data.response.location.WardResponse;
import com.app.server.constants.ResponseMessage;
import com.app.server.utils.FilterUtils;
import com.app.server.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UtilityService extends BaseService {
    private FilterUtils filterUtils;

    @Autowired
    public void setFilterUtils(FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }

    /**
     * Lấy sql theo danh sách chỉ định
     *
     * @param type
     * @param request
     * @return
     */
    public String filterQuery(String type, FilterListRequest request) {
        try {
            FunctionList functionList = FunctionList.from(type);
            String query = this.filterUtils.getQuery(functionList, request.getFilters(), request.getSorts());
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return "";
    }

    /**
     * Lấy danh sách trường thông tin có thể filter của danh sách chỉ định
     *
     * @param type
     * @return
     */
    public ResponseEntity<FilterContentResponse> getFilterContent(String type) {
        try {
            FunctionList functionList = FunctionList.from(type);
            JSONObject data = new JSONObject();
            data.put("filters", JsonUtils.DeSerialize(functionList.getFilterBy(), JSONArray.class));
            return new ResponseEntity<FilterContentResponse>(FilterContentResponse.success(data), HttpStatus.OK);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return new ResponseEntity<FilterContentResponse>((FilterContentResponse) FilterContentResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL), HttpStatus.OK);
    }

    /**
     * Lấy thông tin config của cms
     *
     * @return
     */
    public ClientResponse getConfig() {
        ClientResponse clientResponse = new ClientResponse();
        try {
            ConfigResponse configResponse = new ConfigResponse();

            /**
             * danh sách tỉnh/thành - quận/huyện - phường/xã
             */
            Map<Integer, City> mpCity = dataManager.getProductManager().getMpCity();
            for (City city : mpCity.values()) {
                CityResponse cityResponse = new CityResponse();
                cityResponse.setId(city.getId());
                cityResponse.setName(city.getName());
                cityResponse.setRegion_id(city.getRegion().getId());
                for (District district : city.getLtDistrict()) {
                    DistrictResponse districtResponse = new DistrictResponse();
                    districtResponse.setId(district.getId());
                    districtResponse.setName(district.getName());
                    for (Ward ward : district.getLtWard()) {
                        WardResponse wardResponse = new WardResponse();
                        wardResponse.setId(ward.getId());
                        wardResponse.setName(ward.getName());
                        districtResponse.getLtWard().add(wardResponse);
                    }
                    cityResponse.getLtDistrict().add(districtResponse);
                }
                configResponse.getLtCity().add(cityResponse);
            }

            /**
             * danh sách cấp bậc của địa lý
             */
            Map<Integer, Membership> mpMembership = dataManager.getProductManager().getMpMembership();
            for (Membership membership : mpMembership.values()) {
                configResponse.getLtMembership().add(membership);
            }

            /**
             * Danh sách trạng thái của đại lý
             */
            for (Map.Entry<Integer, JSONObject> entry : this.dataManager.getConfigManager().getMPAgencyStatus().entrySet()) {
                configResponse.getLtAgencyStatus().add(
                        new AgencyStatusResponse(
                                ConvertUtils.toInt(entry.getKey()),
                                ConvertUtils.toString(entry.getValue().get("name")))
                );
            }

            /**
             * Loại hàng hóa
             */
            for (ItemType itemType : ItemType.values()) {
                configResponse.getLtItemType().add(new BasicResponse(itemType.getKey(), itemType.getValue()));
            }

            /**
             * Cấp danh mục
             */
            for (CategoryLevel categoryLevel : CategoryLevel.values()) {
                configResponse.getLtCategoryLevel().add(new BasicResponse(categoryLevel.getKey(), categoryLevel.getValue()));
            }

            /**
             * Quy cách nhỏ
             */
            Map<Integer, ProductSmallUnitEntity> mpProductSmallUnit = dataManager.getProductManager().getMpProductSmallUnit();
            for (ProductSmallUnitEntity productSmallUnit : mpProductSmallUnit.values()) {
                configResponse.getLtProductSmallUnit().add(new BasicResponse(productSmallUnit.getId(), productSmallUnit.getName()));
            }

            /**
             * Quy cách lớn
             */
            Map<Integer, ProductBigUnitEntity> mpProductBigUnit = dataManager.getProductManager().getMpProductBigUnit();
            for (ProductBigUnitEntity productBigUnit : mpProductBigUnit.values()) {
                configResponse.getLtProductBigUnit().add(new BasicResponse(productBigUnit.getId(), productBigUnit.getName()));
            }

            /**
             * Màu sắc
             */
            Map<Integer, ProductColorEntity> mpProductColor = dataManager.getProductManager().getMpProductColor();
            for (ProductColorEntity productColorEntity : mpProductColor.values()) {
                configResponse.getLtProductColor().add(new BasicResponse(productColorEntity.getId(), productColorEntity.getName()));
            }

            /**
             * Image path
             */
            for (ImagePath type : ImagePath.values()) {
                configResponse.getLtImagePath().add(new BasicResponse(type.name(), type.getImageUrl()));
            }

            /**
             * Trạng thái đơn hàng
             */
            for (Map.Entry<Integer, JSONObject> entry : this.dataManager.getConfigManager().getMPOrderStatus().entrySet()) {
                configResponse.getLtOrderStatus().add(
                        new BasicResponse(
                                entry.getKey(),
                                ConvertUtils.toString(entry.getValue().get("name"))));
            }

            /**
             * Bộ lọc đối tượng
             */
            for (ApplyObjectType type : ApplyObjectType.values()) {
                configResponse.getLtApplyObject().add(new BasicResponse(type.getKey(), type.getLabel()));
            }

            /**
             * Vùng địa lý
             */
            Map<Integer, Region> mpRegion = dataManager.getProductManager().getMpRegion();
            for (Region region : mpRegion.values()) {
                BasicResponse basicResponse = new BasicResponse(
                        region.getId(),
                        region.getName());
                configResponse.getLtRegion().add(basicResponse);
            }

            /**
             * Phòng kinh doanh
             */
            Map<Integer, BusinessDepartment> mpBusinessDepartment = dataManager.getProductManager().getMpBusinessDepartment();
            for (BusinessDepartment businessDepartment : mpBusinessDepartment.values()) {
                BasicResponse basicResponse = new BasicResponse(
                        businessDepartment.getId(),
                        businessDepartment.getName());
                configResponse.getLtBusinessApartment().add(basicResponse);
            }

            /**
             * Loại công nợ
             */
            for (JSONObject jsonObject : dataManager.getProductManager().getMpDeptType().values()) {
                jsonObject.put("label", jsonObject.get("name"));
                configResponse.getLtDeptType().add(jsonObject);
            }

            /**
             * Loại giao dịch
             */
            for (JSONObject jsonObject : dataManager.getProductManager().getMpDeptTransactionMainType().values()) {
                jsonObject.put("label", jsonObject.get("name"));
                configResponse.getLtDeptTransactionMainType().add(jsonObject);
            }

            /**
             * Loại hạng mục công việc
             */
            for (JSONObject jsonObject : dataManager.getProductManager().getMpDeptTransactionSubType().values()) {
                jsonObject.put("label", jsonObject.get("name"));
                configResponse.getLtDeptTransactionSubType().add(jsonObject);
            }

            /**
             * Trạng thái giao dịch
             */
            for (DeptTransactionStatus type : DeptTransactionStatus.values()) {
                configResponse.getLtDeptTransactionStatus().add(new BasicResponse(type.getId(), type.getLabel()));
            }

            /**
             * Trạng thái thiết lập công nợ
             */
            for (DeptSettingStatus type : DeptSettingStatus.values()) {
                configResponse.getLtDeptSettingStatus().add(new BasicResponse(type.getId(), type.getLabel()));
            }

            /**
             * Trạng thái phiếu nhập kho/xuất kho
             */
            for (WarehouseBillStatus type : WarehouseBillStatus.values()) {
                configResponse.getLtWarehouseBillStatus().add(new BasicResponse(type.getId(), type.getLabel()));
            }

            /**
             * Loại kho
             */
            Map<Integer, WarehouseTypeData> warehouseTypeDataMap = this
                    .dataManager.getWarehouseManager().getMpWarehouseType();
            for (WarehouseTypeData type : warehouseTypeDataMap.values()) {
                configResponse.getLtWarehouseType().add(new BasicResponse(type.getId(), type.getName()));
            }
            /**
             * Trạng thái kho
             */
            for (WarehouseStatus type : WarehouseStatus.values()) {
                configResponse.getLtWarehouseStatus().add(new BasicResponse(type.getId(), type.getLabel()));
            }

            /**
             * Các trường hợp bị vướng của đơn hàng
             */
            for (StuckType type : StuckType.values()) {
                configResponse.getLtStuckType().add(new BasicResponse(type.getId(), type.getLabel()));
            }

            /**
             * Trạng thái thiet lap
             */
            for (SettingStatus type : SettingStatus.values()) {
                configResponse.getLtProductVisibilitySettingStatus().add(new BasicResponse(type.getId(), type.getCode(), type.getLabel()));
            }

            /**
             * Đối tượng thiet lap ẩn hiện
             */
            for (SettingObjectType type : SettingObjectType.values()) {
                configResponse.getLtVisibilityObjectType().add(new BasicResponse(type.getId(), type.getCode(), type.getLabel()));
            }

            /**
             * Dữ liệu thiet lap ẩn hiện
             */
            for (VisibilityDataType type : VisibilityDataType.values()) {
                configResponse.getLtVisibilityDataType().add(new BasicResponse(type.getCode(), type.getLabel()));
            }

            /**
             * Loai banner
             */
            for (SettingType type : SettingType.values()) {
                configResponse.getLtBannerType().add(new BasicResponse(
                        type.getId(),
                        type.getCode(),
                        type.getLabel()));
            }

            /**
             * Trạng thái banner
             */
            for (BannerStatus type : BannerStatus.values()) {
                configResponse.getLtBannerStatus().add(new BasicResponse(
                        type.getId(),
                        type.getCode(),
                        type.getLabel()));
            }

            /**
             * Loai thông báo
             */
            for (SettingType type : SettingType.values()) {
                configResponse.getLtNotifyType().add(new BasicResponse(
                        type.getId(), type.getCode(), type.getLabel()));
            }

            /**
             * Trạng thái thông báo
             */
            for (NotifyStatus type : NotifyStatus.values()) {
                configResponse.getLtNotifyStatus().add(new BasicResponse(
                        type.getId(), type.getCode(), type.getLabel()));
            }

            /**
             * Hình thức hiển thị thông báo
             */
            for (NotifyDisplayType type : NotifyDisplayType.values()) {
                configResponse.getLtNotifyDisplayType().add(new BasicResponse(
                        type.getId(), type.getCode(), type.getLabel()));
            }

            /**
             * Phân loại thông báo tự động
             */
            for (NotifyAutoType type : NotifyAutoType.values()) {
                configResponse.getLtNotifyAutoType().add(new BasicResponse(
                        type.getId(), type.getCode(), type.getLabel()));
            }

            /**
             * Trạng thái nhóm phân quyền
             */
            for (PermissionStatus type : PermissionStatus.values()) {
                configResponse.getLtPermissionStatus().add(new BasicResponse(
                        type.getId(), type.getCode(), type.getLabel()));
            }
            /**
             * Trạng thái hẹn giờ giá bán chung
             */
            for (ProductPriceTimerStatus type : ProductPriceTimerStatus.values()) {
                configResponse.getLtProductPriceTimerStatus().add(new BasicResponse(
                        type.getId(), type.getCode(), type.getLabel()));
            }

            /**
             * Loại sản phẩm hot
             */
            for (JSONObject type : this.dataManager.getConfigManager().getMpProductHotType().values()) {
                configResponse.getLtProductHotType().add(new BasicResponse(
                        ConvertUtils.toInt(type.get("id")),
                        ConvertUtils.toString(type.get("code")),
                        ConvertUtils.toString(type.get("name")),
                        ConvertUtils.toString(type.get("image"))));
            }

            /**
             * Trạng thái thách giá
             */
            for (DealPriceStatus type : DealPriceStatus.values()) {
                configResponse.getLtDealPriceStatus().add(new BasicResponse(
                        type.getId(), type.getCode(), type.getLabel()));
            }

            /**
             * Loại đơn hàng
             */
            for (OrderType type : OrderType.values()) {
                configResponse.getLtOrderType().add(new BasicResponse(
                        type.getValue(), type.getCode(), type.getLabel()));
            }

            /**
             * Cơ cấu chương trình tích lũy
             */
            for (PromoStructureType type : PromoStructureType.values()) {
                configResponse.getLtPromoStructure().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Loại ưu đãi
             */
            for (PromoOfferType type : PromoOfferType.values()) {
                configResponse.getLtPromoOfferType().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Loại điều kiện tích lũy của CTTL
             */
            for (PromoConditionCTTLType type : PromoConditionCTTLType.values()) {
                configResponse.getLtPromoConditionCTTLType().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Hình thức tính thưởng
             */
            for (PromoFormOfRewardType type : PromoFormOfRewardType.values()) {
                configResponse.getLtPromoFormOfReward().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Trạng thái CTTL
             */
            for (PromoCTTLStatus type : PromoCTTLStatus.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtPromoCTLLStatus().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Loại các khoản tích lũy của CTTL
             */
            for (CTTLTransactionType type : CTTLTransactionType.values()) {
                configResponse.getLtPromoCTLLTransactionType().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Trạng thái khóa đại lý
             */
            for (LockDataStatus type : LockDataStatus.values()) {
                configResponse.getLtLockDataStatus().add(
                        new BasicResponse(
                                type.getId(),
                                type.getCode(),
                                type.getLabel()));
            }

            /*
             * Loại khóa đại lý
             */
            for (LockOptionType type : LockOptionType.values()) {
                configResponse.getLtOptionLock().add(
                        new BasicResponse(
                                type.getId(),
                                type.getCode(),
                                type.getLabel()));
            }

            /**
             * Trạng thái yêu cầu Catalog
             */
            for (AgencyCatalogRequestStatus type : AgencyCatalogRequestStatus.values()) {
                configResponse.getLtAgencyCatalogRequestStatus().add(
                        new BasicResponse(
                                type.getId(),
                                type.getCode(),
                                type.getLabel()));
            }

            /**
             * Trạng thái chi tiết Catalog của đại lý
             */
            for (AgencyCatalogDetailStatus type : AgencyCatalogDetailStatus.values()) {
                configResponse.getLtAgencyCatalogDetailStatus().add(
                        new BasicResponse(
                                type.getId(),
                                type.getCode(),
                                type.getLabel()));
            }

            /**
             * Nguồn thay đổi cấp bậc
             */
            for (MembershipChangeSourceType type : MembershipChangeSourceType.values()) {
                configResponse.getLtMembershipChangeSourceType().add(
                        new BasicResponse(
                                type.getKey(),
                                type.getCode(),
                                type.getValue()));
            }

            /**
             * Trạng thái Đam mê
             */
            for (PromoActiveStatus type : PromoActiveStatus.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtPromoDamMeStatus().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Loại các khoản tích lũy của Đam mê
             */
            for (CSDMAccumulateType type : CSDMAccumulateType.values()) {
                configResponse.getLtPromoDamMeTransactionType().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Loại điều kiện tích lũy của Đam mê
             */
            for (PromoDamMeConditionType type : PromoDamMeConditionType.values()) {
                configResponse.getLtPromoConditionDamMeType().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Màu hạn mức tích lũy đam mê
             */
            for (PromoDamMeColorType type : PromoDamMeColorType.values()) {
                JSONObject js = new JSONObject();
                js.put("key", type.getId());
                js.put("value", type.getLabel());
                configResponse.getLtPromoDamMeColorType().add(js);
            }

            /**
             * Loại điều chỉnh tích lũy của Đam mê
             */
            for (DamMeDieuChinhType type : DamMeDieuChinhType.values()) {
                configResponse.getLtPromoDamMeDieuChinhType().add(
                        new BasicResponse(
                                type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Loại ưu đãi của CTXH
             */
            for (CTXHOfferType type : CTXHOfferType.values()) {
                configResponse.getLtCTXHOfferType().add(
                        new BasicResponse(
                                type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Loại ưu đãi của Voucher
             */
            for (VoucherOfferType type : VoucherOfferType.values()) {
                configResponse.getLtVoucherOfferType().add(
                        new BasicResponse(
                                type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Trạng thái của đợt voucher
             */
            for (VoucherReleasePeriodStatus type : VoucherReleasePeriodStatus.values()) {
                configResponse.getLtVoucherReleasePeriodStatus().add(
                        new BasicResponse(
                                type.getId(), type.getCode(), type.getLabel()));
            }

            /**
             * Trạng thái voucher
             */
            for (VoucherStatus type : VoucherStatus.values()) {
                configResponse.getLtVoucherStatus().add(
                        new BasicResponse(
                                type.getId(), type.getCode(), type.getLabel()));
            }

            /**
             * Trạng thái CTXH
             */
            for (PromoActiveStatus type : PromoActiveStatus.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtCTXHStatus().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Thiết lập hệ thống
             */
            for (Map.Entry<String, String> entry : this.dataManager.getConfigManager().getMPConfigData().entrySet()) {
                configResponse.getLtConfigData().add(new BasicResponse(
                        0, entry.getKey(), entry.getValue()));
            }

            this.getConfigNhiemVu(configResponse);

            /**
             * Chương trình tích lũy
             */
            this.getCTTLConfig(configResponse);
            return ClientResponse.success(configResponse);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void getConfigNhiemVu(ConfigResponse configResponse) {
        try {
            /**
             * Trạng thái bảng thành tích nhiệm vụ
             */
            for (MissionBXHStatus type : MissionBXHStatus.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtTrangThaiBangThanhTich().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Loại bảng thành tích nhiệm vụ
             */
            for (MissionBXHType type : MissionBXHType.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtLoaiBangThanhTich().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Loại loại nhiệm vụ
             */
            for (MissionType type : MissionType.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtLoaiNhiemVu().add(new BasicResponse(
                        type.getId(), type.getData(), type.getLabel()));
            }

            /**
             * Kỳ nhiệm vụ
             */
            for (MissionPeriodType type : MissionPeriodType.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtKyNhiemVu().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Đơn vị nhiệm vụ
             */
            for (MissionUnitType type : MissionUnitType.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtLoaiDonViNhiemVu().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Trạng thái thực hiện nhiệm vụ
             */
            for (MissionAgencyStatus type : MissionAgencyStatus.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtTrangThaiThucHienNhiemVu().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Loại tích lũy nhiệm vụ
             */
            for (MissionTransactionType type : MissionTransactionType.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtLoaiTichLuyNhiemVu().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Trạng thái thiết lập nhiệm vụ
             */
            for (MissionSettingStatus type : MissionSettingStatus.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtTrangThaiThietLapNhiemVu().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Trạng thái nhóm nhiệm vụ
             */
            for (MissionGroupStatus type : MissionGroupStatus.values()) {
                if (type.getId() == -1) {
                    continue;
                }
                configResponse.getLtTrangThaiNhomNhiemVu().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }

            /**
             * Trạng thái tích lũy nhiệm vụ
             */
            for (MissionTransactionStatus type : MissionTransactionStatus.values()) {
                if (type.getId() > 0) {
                    continue;
                }
                configResponse.getLtTrangThaiTichLuyNhiemVu().add(new BasicResponse(
                        type.getId(), type.getKey(), type.getLabel()));
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private void getCTTLConfig(ConfigResponse configResponse) {
        try {
            /**
             * Các cột xuất thống kê CTTL
             */
            for (CTTLColumnExportType type : CTTLColumnExportType.values()) {
                if (type != CTTLColumnExportType.THANH_TOAN_CON_THIEU) {
                    configResponse.getLtPromoCTLLExportColumnType().add(
                            new BasicResponse(
                                    type.getId(), type.getKey(), type.getLabel()));
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    public void createQuery() {

    }

    /**
     * Lưu file (hình ảnh, file tài liệu)
     *
     * @param files
     * @param type
     * @return
     */
    public List<String> saveFile(MultipartFile[] files, String type) {
        try {
            List<String> rs = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                byte[] bytes = file.getBytes();
                String extension = FilenameUtils.getExtension(file.getOriginalFilename());
                String name = System.currentTimeMillis() + i + "." + extension;
                Files.write(Paths.get(ConfigInfo.IMAGE_FOLDER_PATH + "/" + type + "/" + name), bytes);

                rs.add(name);
            }
            return rs;
        } catch (IOException ex) {
            LogUtil.printDebug("", ex);
        }
        return new ArrayList<>();
    }

    public List<String> saveFileBase64(String files, String type, String extension) {
        try {
            List<String> rs = new ArrayList<>();

            byte[] bytes = Base64.getDecoder().decode(files);
            String name = System.currentTimeMillis() + "0" + "." + extension;
            Files.write(Paths.get(ConfigInfo.IMAGE_FOLDER_PATH + "/" + type + "/" + name), bytes);
            rs.add(name);

            return rs;
        } catch (IOException ex) {
            LogUtil.printDebug("", ex);
        }
        return new ArrayList<>();
    }

    public ClientResponse uploadFile(MultipartFile[] files, String type) {
        try {
            JSONObject data = new JSONObject();
            data.put("images", this.saveFile(files, type));
            data.put("url", ConfigInfo.IMAGE_URL + "/" + type + "/");
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchCategory(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_CATEGORY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.searchCategory(query, this.appUtils.getOffset(1), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.CATEGORY.getImageUrl());
            }

            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchBrand(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_BRAND, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.searchBrand(query, this.appUtils.getOffset(1), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.BRAND.getImageUrl());
            }

            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchProductGroup(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_PRODUCT_GROUP, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.searchProductGroup(query, this.appUtils.getOffset(1), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.PRODUCT.getImageUrl());
            }

            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchProductSmallUnit(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_PRODUCT_SMALL_UNIT, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.searchProductGroup(query, this.appUtils.getOffset(1), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.PRODUCT.getImageUrl());
            }

            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchProductBigUnit(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_PRODUCT_BIG_UNIT, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.searchProductGroup(query, this.appUtils.getOffset(1), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.PRODUCT.getImageUrl());
            }

            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchProductColor(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_PRODUCT_COLOR, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.searchProductGroup(query, this.appUtils.getOffset(1), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.PRODUCT.getImageUrl());
            }

            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchProduct(FilterProductByAgencyRequest request) {
        try {
            AgencyEntity agencyEntity = this.dataManager.getAgencyManager().getAgency(request.getAgency_id());

            String query = this.filterUtils.getQuery(FunctionList.SEARCH_PRODUCT, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = new ArrayList<>();
            List<JSONObject> rs = this.productDB.searchProduct(query, this.appUtils.getOffset(1), 100, request.getIsLimit());
            for (JSONObject js : rs) {
                if (agencyEntity != null) {
                    int product_id = ConvertUtils.toInt(js.get("id"));
                    ProductCache productPrice = this.getFinalPriceByAgency(
                            product_id,
                            agencyEntity.getId(),
                            agencyEntity.getCity_id(),
                            agencyEntity.getRegion_id(),
                            agencyEntity.getMembership_id()
                    );
                    int visibility = this.getProductVisibilityByAgency(
                            request.getAgency_id(),
                            product_id);
                    js.put("visibility", visibility);
                    js.put("image_url", ImagePath.PRODUCT.getImageUrl());
                    js.put("price", productPrice.getPrice());
                    js.put("minimum_purchase", productPrice.getMinimum_purchase());
                }

                records.add(js);
            }

            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchProductData(SearchProductDataRequest request) {
        try {
            JSONObject data = new JSONObject();
            List<JSONObject> records = new ArrayList<>();
            List<ProductDataRequest> errorList = new ArrayList<>();

            Map<String, String> mpProduct = new HashMap<>();
            for (int iProduct = 0; iProduct < request.getProducts().size(); iProduct++) {
                ProductDataRequest productDataRequest = request.getProducts().get(iProduct);
                ClientResponse crValidate = productDataRequest.validate();
                JSONObject record = this.dataManager.getProductManager().getProductByCode(productDataRequest.getCode());
                String message = "";
                if (mpProduct.containsKey(productDataRequest.getCode())) {
                    message = message.isEmpty() ?
                            ResponseMessage.DUPLICATE.getValue() :
                            (message + ", " + ResponseMessage.DUPLICATE.getValue());
                }

                if (crValidate.failed()) {
                    message = message.isEmpty() ?
                            crValidate.getMessage() :
                            (message + ", " + crValidate.getMessage());
                }

                if (record == null) {
                    message = message.isEmpty() ?
                            ResponseMessage.PRODUCT_NOT_FOUND.getValue() :
                            (message + ", " + ResponseMessage.PRODUCT_NOT_FOUND.getValue());
                }

                if (record != null && !ConvertUtils.toString(record.get("full_name")).equals(productDataRequest.getFull_name())) {
                    message = message.isEmpty() ?
                            ResponseMessage.NAME_NOT_MATCH.getValue() :
                            (message + ", " + ResponseMessage.NAME_NOT_MATCH.getValue());
                }

                if (!message.isEmpty()) {
                    productDataRequest.setError(message);
                    errorList.add(productDataRequest);
                } else {
                    record.put("note", productDataRequest.getNote());
                    record.put("quantity", productDataRequest.getQuantity());
                    record.put("price", productDataRequest.getPrice());
                    records.add(record);
                }
                mpProduct.put(productDataRequest.getCode(), productDataRequest.getCode());
            }

            if (errorList.isEmpty()) {
                data.put("records", records);
            } else {
                data.put("records", errorList);
                data.put("is_error", true);
            }

            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchProductByAgency(FilterProductByAgencyRequest request) {
        try {
            AgencyEntity agencyEntity = this.dataManager.getAgencyManager().getAgency(request.getAgency_id());
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_PRODUCT, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = new ArrayList<>();
            List<JSONObject> rs = this.productDB.searchProduct(query, this.appUtils.getOffset(1), 100, request.getIsLimit());
            for (JSONObject js : rs) {
                if (agencyEntity != null) {
                    int product_id = ConvertUtils.toInt(js.get("id"));
                    ProductCache productPrice = this.getFinalPriceByAgency(
                            product_id,
                            agencyEntity.getId(),
                            agencyEntity.getCity_id(),
                            agencyEntity.getRegion_id(),
                            agencyEntity.getMembership_id()
                    );
                    int visibility = this.getProductVisibilityByAgency(
                            request.getAgency_id(),
                            product_id);
                    if (VisibilityType.SHOW.getId() == visibility) {
                        js.put("visibility", visibility);
                        js.put("image_url", ImagePath.PRODUCT.getImageUrl());
                        js.put("price", productPrice.getPrice());
                        js.put("minimum_purchase", productPrice.getMinimum_purchase());
                        records.add(js);
                    }
                }
            }

            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getAllCategory() {
        try {
            List<Category> ltCategoryFinal = this.dataManager.getProductManager().getLtCategory();
            JSONObject data = new JSONObject();
            data.put("categories", ltCategoryFinal);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.UTILITY.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchAgency(SessionData sessionData, FilterListRequest request) {
        try {
            this.addFilterAgencyData(sessionData, request);
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_AGENCY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.searchAgency(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 0);
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchAgencyAddressDelivery(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_AGENCY_ADDRESS_DELIVERY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.searchAgencyAddressDelivery(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchAgencyAddressExportBilling(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_AGENCY_ADDRESS_EXPORT_BILLING, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.searchAgencyAddressExportBilling(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchTreeProduct(SessionData sessionData, SearchRequest request) {
        try {
            FunctionList functionList = FunctionList.SEARCH_TREE_PRODUCT;
            StringBuilder query = new StringBuilder(functionList.getSql());
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            query.append(" WHERE product.status != -1 AND product.business_department_id=" + staff.getDepartment_id());
            List<JSONObject> records = null;
            List<JSONObject> rs = null;
            if (!request.getSearch().isEmpty()) {
                query.append(" AND");
                query.append(" MATCH(product.full_name,product.code) AGAINST('" + request.getSearch() + "')");
                String sql = query.toString();
                rs = this.productDB.searchTreeProduct(sql, this.appUtils.getOffset(1), 100, 0);
            } else {
                String sql = query.toString();
                rs = this.productDB.searchTreeProduct(sql, this.appUtils.getOffset(1), 100, 0);
            }

            List<ProductTreeResponse> treeResponseList = new ArrayList<>();
            for (JSONObject js : rs) {
                int id = ConvertUtils.toInt(js.get("product_group_id"));
                ProductTreeResponse treeResponse = getProductTreeInList(id, treeResponseList);
                if (treeResponse == null) {
                    treeResponse = new ProductTreeResponse();
                    treeResponse.setId(id);
                    treeResponse.setCode(ConvertUtils.toString(js.get("product_group_code")));
                    treeResponse.setName(ConvertUtils.toString(js.get("product_group_name")));

                    ProductTreeResponse child = new ProductTreeResponse();
                    child.setId(ConvertUtils.toInt(js.get("id")));
                    child.setCode(ConvertUtils.toString(js.get("code")));
                    child.setName(ConvertUtils.toString(js.get("full_name")));
                    child.setImages(ConvertUtils.toString(js.get("images")));
                    child.setPrice(ConvertUtils.toLong(js.get("price")));
                    child.setItem_type(ConvertUtils.toInt(js.get("item_type")));
                    treeResponse.getChilds().add(child);
                    treeResponseList.add(treeResponse);
                } else {
                    ProductTreeResponse child = new ProductTreeResponse();
                    child.setId(ConvertUtils.toInt(js.get("id")));
                    child.setCode(ConvertUtils.toString(js.get("code")));
                    child.setName(ConvertUtils.toString(js.get("full_name")));
                    child.setImages(ConvertUtils.toString(js.get("images")));
                    child.setPrice(ConvertUtils.toLong(js.get("price")));
                    child.setItem_type(ConvertUtils.toInt(js.get("item_type")));
                    treeResponse.getChilds().add(child);
                }
            }

            JSONObject data = new JSONObject();
            data.put("records", treeResponseList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ProductTreeResponse getProductTreeInList(int id, List<ProductTreeResponse> productTreeResponseList) {
        for (ProductTreeResponse productTreeResponse : productTreeResponseList) {
            if (productTreeResponse.getId() == id) {
                return productTreeResponse;
            }
        }
        return null;
    }

    public ClientResponse searchWarehouse(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_WAREHOUSE, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.warehouseDB.searchWarehouse(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchStaff(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_STAFF, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.staffDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchPromotion(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_PROMOTION, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchBanner(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_BANNER, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.bannerDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getAllMenuInfo() {
        try {
            List<MenuItemInfo> menuList = new ArrayList<>();
            for (MenuData menuData : this.dataManager.getStaffManager().getMpMenu().values()) {
                MenuItemInfo menu = new MenuItemInfo();
                menu.setId(menuData.getId());
                menu.setName(menuData.getName());
                menu.setCode(menuData.getCode());
                menu.setLevel(menuData.getLevel());
                menu.setParent_id(menuData.getParent_id());
                List<MenuItemInfo> subMenuList = new ArrayList<>();
                for (MenuData subMenuData : menuData.getChildren().values()) {
                    MenuItemInfo subMenu = new MenuItemInfo();
                    subMenu.setId(subMenuData.getId());
                    subMenu.setName(subMenuData.getName());
                    subMenu.setCode(subMenuData.getCode());
                    subMenu.setLevel(subMenuData.getLevel());
                    subMenu.setParent_id(subMenuData.getParent_id());
                    subMenu.setPriority(subMenuData.getPriority());
                    List<MenuItemInfo> actionList = new ArrayList<>();
                    for (MenuData actionData : subMenuData.getChildren().values()) {
                        MenuItemInfo action = new MenuItemInfo();
                        action.setId(actionData.getId());
                        action.setName(actionData.getName());
                        action.setCode(actionData.getCode());
                        action.setLevel(actionData.getLevel());
                        action.setParent_id(actionData.getParent_id());
                        action.setPriority(actionData.getPriority());
                        actionList.add(action);
                    }
                    Collections.sort(actionList, (a, b) ->
                            a.getPriority() < b.getPriority() ? 1 :
                                    a.getPriority() > b.getPriority() ? -1 : a.getId() < b.getId() ? 1 : -1);
                    subMenu.setChildren(actionList);
                    subMenuList.add(subMenu);
                }
                Collections.sort(subMenuList, (a, b) ->
                        a.getPriority() < b.getPriority() ? 1 :
                                a.getPriority() > b.getPriority() ? -1 : a.getId() < b.getId() ? 1 : -1);
                menu.setChildren(subMenuList);
                menuList.add(menu);
            }

            JSONObject data = new JSONObject();
            data.put("records", menuList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse uploadFileBase64(String files, String type, String extension) {
        try {
            JSONObject data = new JSONObject();
            data.put("images", this.saveFileBase64(files, type, extension));
            data.put("url", ConfigInfo.IMAGE_URL + "/" + type + "/");
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchProductGroupByCategory(FilterListByIdRequest request) {
        try {
            List<JSONObject> records = new ArrayList<>();

            if (request.getId() > 0) {
                FilterRequest filterRequest = new FilterRequest();
                filterRequest.setValue(ConvertUtils.toString(request.getId()));
                filterRequest.setType(TypeFilter.SELECTBOX);
                filterRequest.setKey("category_id");
                request.getFilters().add(filterRequest);
                String query = this.filterUtils.getQuery(FunctionList.SEARCH_PRODUCT_GROUP, request.getFilters(), request.getSorts());
                records = this.productDB.searchProductGroup(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 0);
            }
            JSONObject data = new JSONObject();
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.PRODUCT.getImageUrl());
            }

            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchCombo(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_COMBO, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.searchProductGroup(
                    query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE,
                    request.getIsLimit());
            int total = this.productDB.getTotal(query);
            for (JSONObject js : records) {
                List<JSONObject> productList = JsonUtils.DeSerialize(
                        js.get("data").toString(),
                        new TypeToken<List<JSONObject>>() {
                        }.getType()
                );
                List<ProductData> productDataList = new ArrayList<>();
                for (JSONObject jsProduct : productList) {
                    ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                            ConvertUtils.toInt(jsProduct.get("id"))
                    );

                    ProductData productData = new ProductData(
                            ConvertUtils.toInt(jsProduct.get("id")),
                            productCache == null ? "" : productCache.getCode(),
                            productCache == null ? "" : productCache.getFull_name(),
                            ConvertUtils.toInt(jsProduct.get("quantity")),
                            productCache == null ? "[]" : productCache.getImages(),
                            productCache == null ? 0 : productCache.getPrice()
                    );

                    productDataList.add(productData);
                }
                js.put("products", productDataList);
                js.put("image_url", ImagePath.COMBO.getImageUrl());
            }

            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProductStock(ApproveDeptSettingRequest request) {
        try {
            List<String> strIds = new ArrayList<>();
            for (Integer id : request.getIds()) {
                strIds.add(ConvertUtils.toString(id));
            }
            List<JSONObject> records = this.warehouseDB.getStockByListProduct(
                    JsonUtils.Serialize(strIds)
            );
            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterPromo(FilterListRequest request) {
        try {
            if (request.getPromo_id() != 0) {
                FilterRequest filterRequest = new FilterRequest();
                filterRequest.setKey("id");
                filterRequest.setValue(ConvertUtils.toString(request.getPromo_id()));
                filterRequest.setType(TypeFilter.NOT_LIKE);
                request.getFilters().add(filterRequest);
            }
            String query = this.filterUtils.getQuery(FunctionList.FILTER_PROMO, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.promoDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse searchSupplier(SessionData sessionData, FilterListRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("business_department_id");
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(staff.getDepartment_id()));
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.SEARCH_SUPPLIER, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 0);
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}