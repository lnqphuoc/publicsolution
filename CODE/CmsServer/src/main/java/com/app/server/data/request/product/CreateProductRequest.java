package com.app.server.data.request.product;

import com.app.server.constants.ResponseMessage;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class CreateProductRequest {
    /**
     * mã phiên bản
     */
    @ApiModelProperty(value = "mã phiên bản")
    private String code;

    /**
     * tên phiên bản rút gọn
     */
    @ApiModelProperty(value = "tên phiên bản rút gọn")
    private String short_name = "";

    /**
     * tên phiên bản đầy đủ
     */

    @ApiModelProperty(value = "tên phiên bản đầy đủ")
    private String full_name = "";

    /**
     * thời gian bảo hành
     */
    @ApiModelProperty(value = "thời gian bảo hành")
    private String warranty_time = "";

    /**
     * hình ảnh của sản phẩm
     */
    @ApiModelProperty(value = "hình ảnh của sản phẩm")
    private String images = "[]";

    /**
     * quy cách
     */
    @ApiModelProperty(value = "quy cách")
    private String specification = "";

    /**
     * id màu sắc
     */
    @ApiModelProperty(value = "id màu sắc")
    private Integer product_color_id;

    /**
     * đặc điểm
     */
    @ApiModelProperty(value = "đặc điểm")
    private String characteristic = "";

    /**
     * mô tả
     */
    @ApiModelProperty(value = "mô tả")
    private String description = "";

    /**
     * hướng dẫn sử dụng
     */
    @ApiModelProperty(value = "hướng dẫn sử dụng")
    private String user_manual = "";

    /**
     * thông số kỹ thuật
     */
    @ApiModelProperty(value = "thông số kỹ thuật: [{\"name\":\"thuộc tính\", \"value\":\"giá trị\"}]")
    private String technical_data = "";

    /**
     * trạng thái: 0: ẩn, 1: hiển thị
     */
    @ApiModelProperty(value = "trạng thái: 0: ẩn, 1: hiển thị")
    private int status;

    /**
     * giá, -1: giá liên hệ, >=0 giá
     */
    @ApiModelProperty(value = "giá, -1: giá liên hệ, >=0 giá")
    private double price;

    /**
     * id của đơn vị nhỏ
     */
    @ApiModelProperty(value = "id của đơn vị nhỏ")
    private int product_small_unit_id;

    /**
     * id của đơn vị lớn
     */
    @ApiModelProperty(value = "id của đơn vị lớn")
    private Integer product_big_unit_id;

    /**
     * tỷ lệ quy đổi ra đơn vị nhỏ
     */
    @ApiModelProperty(value = "tỷ lệ quy đổi ra đơn vị nhỏ")
    private int convert_small_unit_ratio;

    /**
     * mua tối thiểu
     */
    @ApiModelProperty(value = "mua tối thiểu")
    private int minimum_purchase;

    /**
     * bước nhảy
     */
    @ApiModelProperty(value = "bước nhảy")
    private int step;

    /**
     * id của nhóm sản phẩm
     */
    @ApiModelProperty(value = "id của nhóm sản phẩm")
    private int product_group_id;

    /**
     * loại hàng hóa: 1-máy móc, 2-phụ tùng
     */
    @ApiModelProperty(value = "loại hàng hóa: 1-máy móc, 2-phụ tùng")
    private int item_type;

    /**
     * id của thương hiệu
     */
    @ApiModelProperty(value = "id của thương hiệu")
    private Integer brand_id;

    @ApiModelProperty(value = "tên khác")
    private String other_name;

    /**
     * Trạng thái hiển thị trên app
     */
    @ApiModelProperty(value = "0-Ẩn, 1-Hiện")
    private Integer app_active;

    @ApiModelProperty(value = "Nhãn/tag")
    private String hot_label = "";

    public ClientResponse validate() {
        if (StringUtils.isBlank(code)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_INVALID);
        }
        if (StringUtils.isBlank(full_name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FULL_NAME_INVALID);
        }
        if (StringUtils.isBlank(short_name)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SHORT_NAME_INVALID);
        }
        if (product_small_unit_id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_SMALL_UNIT_INVALID);
        }
        if (product_big_unit_id != null && product_big_unit_id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_BIG_UNIT_INVALID);
        }
        if (product_color_id != null && product_color_id < 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_COLOR_INVALID);
        }
        if (StringUtils.isBlank(technical_data)) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TECHNICAL_DATA_INVALID);
        }
        if (ActiveStatus.from(status) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
        }
        if (price < -1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRICE_INVALID);
        }
        if (step < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STEP_INVALID);
        }
        if (product_big_unit_id != null
                && convert_small_unit_ratio < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CONVERT_SMALL_UNIT_RATIO_INVALID);
        }
        if (product_group_id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_GROUP_NOT_FOUND);
        }
        if (ItemType.from(item_type) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ITEM_TYPE_INVALID);
        }
        if (brand_id == null || brand_id < 1) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.BRAND_INVALID);
        }
        return ClientResponse.success(null);
    }
}