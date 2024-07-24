package com.app.server.data.dto.program;

import com.app.server.data.dto.program.filter.ProgramFilter;
import com.app.server.data.dto.program.limit.ProgramLimit;
import com.app.server.data.dto.program.product.ProgramProduct;
import com.app.server.data.dto.program.product.ProgramProductGroup;
import com.app.server.enums.PromoActiveStatus;
import com.app.server.enums.RepeatType;
import com.ygame.framework.common.LogUtil;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@Getter
@Setter
public class Program {
    private int id;
    private ProgramType type;
    private String code;
    private String name;
    private Date startDate;
    private Date endDate;
    private int orderNumber;
    private String image;
    private String description;
    private ProgramConditionType conditionType;
    private ProgramGoodsType goodsType;
    private boolean isEndLimit;
    private List<ProgramLimit> ltProgramLimit;
    private ProgramLimit ltStepLimit;
    private boolean allowShowTag;
    private Map<Integer, Integer> mpProductCategory;
    private Map<Integer, ProgramProduct> mpProduct; // danh sách sản phẩm không có trong combo
    private Map<Integer, ProgramProduct> mpProductWithCombo; //danh sách sản phẩm bao cả sản phẩm trong combo
    private List<Integer> ltIgnoreProductId;
    private List<Integer> ltIgnoreAgencyId;
    private List<Integer> ltIncludeAgencyId;
    private List<ProgramFilter> ltProgramFilter;
    private int priority; // độ ưu tiên
    private int useLimitPerAgency; // giới hạn số lượng đơn hàng ctkm
    private int userLimit; // giới hạn số lượng đơn hàng mỗi đại lý
    private long promoMaxValue; // giá trị ưu đãi tối đa

    // săn sale
    private int applyPrivatePrice; // áp dụng giá riêng
    private int deptCycle; // kỳ hạn nợ
    private boolean showSanSaleModule; // hiển thị trên module săn sale
    private RepeatType repeatType; // loại lặp lại, 0: ko lặp, 1: hằng ngày, 2: hằng tuần, 3: hằng tháng
    private List<Integer> repeatDataList; // danh sách dữ liệu lặp lại (thứ, ngày)
    private LocalTime startTime; // thời gian bắt đầu (giờ, phút, giây)
    private LocalTime endTime; // thời gian bắt đầu (giờ, phút, giây)
    private Map<Integer, ProgramProductGroup> mpCombo; // danh sách combo
    private int showSanSale;
    private int status;
    private int offerPercent;
    private List<ProgramItem> ltProgramItem; // danh sách sản phẩm, nhóm sản phẩm, danh mục dành cho đam mê

    private String circle_type;

    public Program() {
        ltProgramLimit = new ArrayList<>();
        mpProductCategory = new LinkedHashMap<>();
        mpProduct = new LinkedHashMap<>();
        mpProductWithCombo = new LinkedHashMap<>();
        ltIgnoreProductId = new ArrayList<>();
        ltIgnoreAgencyId = new ArrayList<>();
        ltIncludeAgencyId = new ArrayList<>();
        ltProgramFilter = new ArrayList<>();
        this.repeatDataList = new ArrayList<>();
        this.mpCombo = new LinkedHashMap<>();
        ltProgramItem = new ArrayList<>();
    }

    public boolean isRunning() {
        if (startDate == null)
            return false;
        Date today = new Date();
        if ((today.after(startDate) || today.equals(startDate)) && endDate == null)
            return true;
        else if ((today.after(startDate) || today.equals(startDate)) && (today.before(endDate) || today.equals(endDate)))
            return true;
        else
            return false;
    }

    public boolean containProductCategoryId(int productCategoryId) {
        return mpProductCategory.containsKey(productCategoryId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Program p = (Program) o;
        return id == p.getId();
    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean isBirthdayFilter() {
        try {
            for (ProgramFilter programFilter : ltProgramFilter) {
                if (programFilter.isBirthday())
                    return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return false;
    }

    /**
     * Kiểm tra trạng thái hoạt động chương trình săn sale
     */
    public boolean isSanSaleRunning() {
        boolean isRunning = isRunning();
        try {
            if (isRunning && repeatType != RepeatType.NONE) {
                boolean isDayPass = false;
                if (repeatType == RepeatType.DAILY) // kiểm tra hàng ngày
                    isDayPass = true;
                else if (repeatType == RepeatType.WEEKLY) { // kiểm tra hàng tuần
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_WEEK);
                    if (repeatDataList.contains(day))
                        isDayPass = true;
                } else if (repeatType == RepeatType.MONTHLY) { // kiểm tra hàng tháng
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    if (repeatDataList.contains(day))
                        isDayPass = true;
                    else if (repeatDataList.contains(0)) { // kiểm tra cuối tháng
                        int maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                        if (day == maxDayOfMonth)
                            isDayPass = true;
                    }
                }
                // Nếu kiểm tra xong ngày thì kiểm tra giờ phút
                if (isDayPass) {
                    LocalTime now = LocalTime.now();
                    if (!((now.isAfter(startTime) || now.equals(startTime)) && (now.isBefore(endTime) || now.equals(endTime))))
                        isRunning = false;
                } else
                    isRunning = false;
            }
        } catch (Exception ex) {
            isRunning = false;
            LogUtil.printDebug("", ex);
        }
        return isRunning;
    }

    public long getSanSaleRemainTime() {
        long time = 0;
        try {
            if (isSanSaleRunning()) {
                if (repeatType == RepeatType.NONE) {
                    if (endDate == null)
                        time = -1;
                    else
                        time = endDate.getTime() - startDate.getTime();
                } else
                    time = Duration.between(LocalTime.now(), endTime).toMillis();
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return time;
    }

    public boolean isCancel() {
        return (status == PromoActiveStatus.CANCEL.getId());
    }
}