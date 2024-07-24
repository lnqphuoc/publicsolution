package com.app.server.data.response.extra;

import com.app.server.data.dto.agency.Membership;
import com.app.server.data.request.BasicResponse;
import com.app.server.data.response.agency.AgencyStatusResponse;
import com.app.server.data.response.location.CityResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ConfigResponse {
    /**
     * Tinh thanh
     */
    private List<CityResponse> ltCity = new ArrayList<>();
    private List<Membership> ltMembership = new ArrayList<>();
    private List<AgencyStatusResponse> ltAgencyStatus = new ArrayList<>();
    private List<BasicResponse> ltCategoryLevel = new ArrayList<>();
    private List<BasicResponse> ltItemType = new ArrayList<>();
    private List<BasicResponse> ltProductSmallUnit = new ArrayList<>();
    private List<BasicResponse> ltProductBigUnit = new ArrayList<>();
    private List<BasicResponse> ltProductColor = new ArrayList<>();
    private List<BasicResponse> ltImagePath = new ArrayList<>();
    private List<BasicResponse> ltOrderStatus = new ArrayList<>();
    private List<BasicResponse> ltApplyObject = new ArrayList<>();
    private List<BasicResponse> ltRegion = new ArrayList<>();
    private List<BasicResponse> ltBusinessApartment = new ArrayList<>();
    private List<JSONObject> ltDeptType = new ArrayList<>();
    private List<JSONObject> ltDeptTransactionMainType = new ArrayList<>();
    private List<JSONObject> ltDeptTransactionSubType = new ArrayList<>();
    private List<BasicResponse> ltDeptTransactionStatus = new ArrayList<>();
    private List<BasicResponse> ltDeptSettingStatus = new ArrayList<>();
    private List<BasicResponse> ltWarehouseBillStatus = new ArrayList<>();
    private List<BasicResponse> ltWarehouseType = new ArrayList<>();
    private List<BasicResponse> ltWarehouseStatus = new ArrayList<>();
    private List<BasicResponse> ltStuckType = new ArrayList<>();
    private List<BasicResponse> ltProductVisibilitySettingStatus = new ArrayList<>();
    private List<BasicResponse> ltProductPriceSettingStatus = new ArrayList<>();
    private List<BasicResponse> ltVisibilityDataType = new ArrayList<>();
    private List<BasicResponse> ltVisibilityObjectType = new ArrayList<>();
    private List<BasicResponse> ltBannerType = new ArrayList<>();
    private List<BasicResponse> ltBannerStatus = new ArrayList<>();
    private List<BasicResponse> ltNotifyType = new ArrayList<>();
    private List<BasicResponse> ltNotifyStatus = new ArrayList<>();
    private List<BasicResponse> ltNotifyDisplayType = new ArrayList<>();
    private List<BasicResponse> ltNotifyAutoType = new ArrayList<>();
    private List<BasicResponse> ltPermissionStatus = new ArrayList<>();
    private List<BasicResponse> ltProductPriceTimerStatus = new ArrayList<>();
    private List<BasicResponse> ltProductHotType = new ArrayList<>();
    private List<BasicResponse> ltDealPriceStatus = new ArrayList<>();
    private List<BasicResponse> ltOrderType = new ArrayList<>();
    private List<BasicResponse> ltPromoStructure = new ArrayList<>();
    private List<BasicResponse> ltPromoOfferType = new ArrayList<>();
    private List<BasicResponse> ltPromoConditionCTTLType = new ArrayList<>();
    private List<BasicResponse> ltPromoFormOfReward = new ArrayList<>();
    private List<BasicResponse> ltPromoCTLLStatus = new ArrayList<>();
    private List<BasicResponse> ltPromoCTLLTransactionType = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái khóa đại lý")
    private List<BasicResponse> ltLockDataStatus = new ArrayList<>();
    @ApiModelProperty(value = "Loại khóa đại lý")
    private List<BasicResponse> ltOptionLock = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái yêu cầu Catalog")
    private List<BasicResponse> ltAgencyCatalogRequestStatus = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái chi tiết Catalog của Đại lý")
    private List<BasicResponse> ltAgencyCatalogDetailStatus = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái dữ liệu")
    private List<BasicResponse> ltDataStatus = new ArrayList<>();
    @ApiModelProperty(value = "Nguồn thay đổi cấp bậc của đại lý")
    private List<BasicResponse> ltMembershipChangeSourceType = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái đam mê")
    private List<BasicResponse> ltPromoDamMeStatus = new ArrayList<>();
    @ApiModelProperty(value = "Điều kiện áp dụng đam mê")
    private List<BasicResponse> ltPromoConditionDamMeType = new ArrayList<>();
    @ApiModelProperty(value = "Các khoản tích lũy đam mê")
    private List<BasicResponse> ltPromoDamMeTransactionType = new ArrayList<>();
    @ApiModelProperty(value = "Màu hạn mức tích lũy đam mê")
    private List<JSONObject> ltPromoDamMeColorType = new ArrayList<>();
    @ApiModelProperty(value = "Các loại điều chỉnh tích lũy đam mê")
    private List<BasicResponse> ltPromoDamMeDieuChinhType = new ArrayList<>();
    @ApiModelProperty(value = "Loại ưu đãi của CTXH")
    private List<BasicResponse> ltCTXHOfferType = new ArrayList<>();
    @ApiModelProperty(value = "Loại ưu đãi của Voucher")
    private List<BasicResponse> ltVoucherOfferType = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái của voucher")
    private List<BasicResponse> ltVoucherStatus = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái của đợt voucher")
    private List<BasicResponse> ltVoucherReleasePeriodStatus = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái của CTXH")
    private List<BasicResponse> ltCTXHStatus = new ArrayList<>();
    @ApiModelProperty(value = "Thiết lập hệ thống")
    private List<BasicResponse> ltConfigData = new ArrayList<>();
    @ApiModelProperty(value = "Kỳ nhiệm vụ")
    private List<BasicResponse> ltKyNhiemVu = new ArrayList<>();
    @ApiModelProperty(value = "Loại nhiệm vụ")
    private List<BasicResponse> ltLoaiNhiemVu = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái bảng thành tích")
    private List<BasicResponse> ltTrangThaiBangThanhTich = new ArrayList<>();
    @ApiModelProperty(value = "Loại bảng thành tích nhiệm vụ")
    private List<BasicResponse> ltLoaiBangThanhTich = new ArrayList<>();
    @ApiModelProperty(value = "Loại đơn vị nhiệm vụ")
    private List<BasicResponse> ltLoaiDonViNhiemVu = new ArrayList<>();
    @ApiModelProperty(value = "Loại trạng thái thực hiện nhiệm vụ")
    private List<BasicResponse> ltTrangThaiThucHienNhiemVu = new ArrayList<>();
    @ApiModelProperty(value = "Loại tích lũy nhiệm vụ")
    private List<BasicResponse> ltLoaiTichLuyNhiemVu = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái thiết lập nhiệm vụ")
    private List<BasicResponse> ltTrangThaiThietLapNhiemVu = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái nhóm nhiệm vụ")
    private List<BasicResponse> ltTrangThaiNhomNhiemVu = new ArrayList<>();
    @ApiModelProperty(value = "Trạng thái tích lũy nhiệm vụ")
    private List<BasicResponse> ltTrangThaiTichLuyNhiemVu = new ArrayList<>();
    @ApiModelProperty(value = "Thông tin xuất thống kê CTTL")
    private List<BasicResponse> ltPromoCTLLExportColumnType = new ArrayList<>();
}