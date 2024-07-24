package com.app.server.data.request.agency;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class AddNewAgencyRequest {
    @ApiModelProperty(value = "Tên đại lý")
    protected String full_name;
    @ApiModelProperty(value = "Tên cửa hàng")
    protected String shop_name;
    @ApiModelProperty(value = "Số điện thoại")
    protected String phone;
    @ApiModelProperty(value = "Mật khẩu: tổi thiểu 6 ký tự")
    protected String password;
    @ApiModelProperty(value = "Giới tính: 1-Nam, 0-Nữ")
    protected Integer gender;
    @ApiModelProperty(value = "Định dạng: dd/mm/yyyy")
    protected String birthday = "";
    @ApiModelProperty(value = "Địa chỉ cụ thể")
    protected String address;
    @ApiModelProperty(value = "Hộp thư")
    protected String email = "";
    @ApiModelProperty(value = "Phường xã")
    protected Integer ward_id = 8917;
    @ApiModelProperty(value = "Quận huyện")
    protected Integer district_id = 565;
    @ApiModelProperty(value = "Tỉnh thành")
    protected Integer city_id = 50;
    @ApiModelProperty(value = "Mã số thuế")
    protected String tax_number = "";
    @ApiModelProperty(value = "Hình đại diện")
    protected String avatar = "";
    @ApiModelProperty(value = "Vùng kinh doanh")
    protected Integer region_id;
    @ApiModelProperty(value = "Hình ảnh cửa hàng")
    protected String images;
    @ApiModelProperty(value = "phòng kinh doanh")
    protected Integer business_department_id;
    @ApiModelProperty(value = "ngành hàng chủ lực")
    protected Integer mainstay_industry_id;
    @ApiModelProperty(value = "Loại hình kinh doanh")
    protected Integer business_type;
}