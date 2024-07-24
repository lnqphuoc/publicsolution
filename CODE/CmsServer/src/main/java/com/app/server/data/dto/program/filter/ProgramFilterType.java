
package com.app.server.data.dto.program.filter;

import lombok.Getter;

@Getter
public enum ProgramFilterType {
    GIOI_TINH(0, "GIOI_TINH", "Giới tính"),
    /*
     - Cấp bậc: Thành viên, Bạc, Titan, Vàng, Bạch kim
     */
    CAP_BAC(0, "CAP_BAC", "Cấp bậc"),

    /*
    - Phòng kinh doanh: PKD miền Đông, PKD miền Tây, PKD miền Trung, BHQĐT …
     */
    PKD(0, "PKD", "Phòng kinh doanh"),

    /*
    - Vùng địa lý: Khu vực miền bắc, miền trung, miền đông, miền tây,
    - Khi lựa chọn 1 vùng địa lý, thì Tỉnh/Thành phố sẽ được tự động điền vào tương ứng với vùng địa lý lựa chọn. Khi bỏ vùng địa lý, các Tỉnh/thành phố tương ứng cũng tự động được bỏ ra.
    */
    VUNG_DIA_LY(0, "VUNG_DIA_LY", "Vùng địa lý"),

    /*
    - Tỉnh/ thành phố: Tỉnh thành phố theo bản đồ hành chính
     */
    TINH_THANH(0, "TINH_THANH", "Tỉnh/Thành phố"),

    /*
    - Ngày sinh nhật
     */
    NGAY_SINH_NHAT(0, "NGAY_SINH_NHAT", "Ngày sinh nhật"),

    /*
    - Có nợ xấu: Không áp dụng cho đại lý có nợ xấu
     */
    CO_NO_XAU(0, "CO_NO_XAU", "Có nợ xấu"),

    /*
    - Giá trị nợ quá hạn từ: Mức nợ quá hạn cho phép đặt đơn hưởng CSBH/CTKM
     */
    GIA_TRI_NO_QUA_HAN(0, "GIA_TRI_NO_QUA_HAN", "Giá trị nợ quá hạn"),
    /*
    - Có nợ quá hạn: Không áp dụng cho đại lý có nợ quá hạn (Xét nợ quá hạn tại thời điểm lên đơn hàng).
     */
    CO_NO_QUA_HAN(0, "CO_NO_QUA_HAN", "Có nợ quá hạn"),
    /*
    - Số lần cam kết sai không quá: Giới hạn đại lý có số lần cam kết sai được hưởng CSBH/CTKM
     */
    SO_LAN_SAI_CAM_KET(0, "SO_LAN_SAI_CAM_KET", "Số lần cam kết sai không quá"),
    /*
    - Có hạn mức gối đầu: Cho phép hoặc không cho phép đại lý có hạn mức gối đầu tham gia CSBH/CTKM
     */
    CO_HAN_MUC_GOI_DAU(0, "CO_HAN_MUC_GOI_DAU", "Có hạn mức gối đầu"),
    /*
    - Khoảng cách đơn hàng cuối: Giới hạn các đại lý không đặt hàng trong thời gian dài [n] ngày
     */
    KHOANG_CACH_DON_HANG_CUOI(0, "KHOANG_CACH_DON_HANG_CUOI", "Khoảng cách đơn hàng cuối"),

    /*
    - Kỳ hạn nợ từ - đến: Giới hạn các đại lý có kỳ hạn nợ trong khoản được tham gia CSBH/CTKM
     */
    KY_HAN_NO(0, "KY_HAN_NO", "Kỳ hạn nợ từ - đến"),
    /*
    - Hạn mức nợ từ - đến: Giới hạn các đại lý có hạn mức nợ trong khoản được tham gia CSBH/CTKM
     */
    HAN_MUC_NO(0, "HAN_MUC_NO", "Hạn mức nợ từ - đến"),
    /*
    - Doanh thu thuần từ: CSBH/CTKM dành cho các đại lý thoả điều kiện doanh thu thuần
     */
    DOANH_THU_THUAN_TU(0, "DOANH_THU_THUAN_TU", "Doanh thu thuần từ"),
    /*
    - Doanh thu thuần từ - đến: Giới hạn các đại lý có doanh thu thuần trong khoản được tham gia CSBH/CTKM
     */
    DOANH_THU_THUAN_TU_DEN(0, "DOANH_THU_THUAN_TU_DEN", "Doanh thu thuần từ - đến"),
    /*
    - Doanh số từ: CSBH/CTKM dành cho các đại lý thoả điều kiện doanh số
     */
    DOANH_SO_TU(0, "DOANH_SO_TU", "Doanh số từ"),
    /*
    - Doanh số từ - đến: Giới hạn các đại lý có doanh số trong khoản được tham gia CSBH/CTKM
     */
    DOANH_SO_TU_DEN(0, "DOANH_SO_TU_DEN", "Doanh số từ - đến"),
    /*
    - Công nợ cuối kỳ từ - đến: Giới hạn các đại lý có công nợ cuối kỳ trong khoản được tham gia CSBH/CTKM
     */
    CONG_NO_CUOI_KY_TU_DEN(0, "CONG_NO_CUOI_KY_TU_DEN", "Công nợ cuối kỳ từ - đến"),
    /*
    - Tiền thu: Số tiền khách hàng đã thanh toán trước đó
     */
    TIEN_THU(0, "TIEN_THU", "Tiền thu"),

    /**
     * Nguồn tạo của đơn hàng, 1: app, 2 web
     */
    NGUON_DON_HANG(0, "NGUON_DON_HANG", "Nguồn đơn hàng"),
    GIA_TRI_DON_HANG_TOI_THIEU(0, "GIA_TRI_DON_HANG_TOI_THIEU", "Giá trị đơn hàng tối thiểu"),
    DOANH_THU_THUAN_NAM_TRUOC(0, "DOANH_THU_THUAN_NAM_TRUOC", "Doanh thu thuần năm trước từ");
    private int id;
    private String key;
    private String label;

    ProgramFilterType(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }
}