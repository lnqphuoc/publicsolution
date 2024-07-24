package com.app.server.manager;

import com.app.server.data.dto.program.DeptInfo;
import com.app.server.data.dto.program.Program;
import com.app.server.data.dto.program.Source;
import com.app.server.data.dto.program.agency.Agency;
import com.app.server.data.dto.program.filter.ProgramFilter;
import com.app.server.data.entity.PromoHistoryEntity;
import com.app.server.data.entity.WarehouseInfoEntity;
import com.app.server.enums.CacheType;
import com.app.server.enums.CircleType;
import com.app.server.enums.Module;
import com.app.server.enums.PromoActiveStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Service
public class DataManager {
    private ProductManager productManager;

    @Autowired
    public void setProductManager(ProductManager productManager) {
        this.productManager = productManager;
    }

    private ProgramManager programManager;

    @Autowired
    public void setProgramManager(ProgramManager programManager) {
        this.programManager = programManager;
    }

    private AgencyManager agencyManager;

    @Autowired
    public void setAgencyManager(AgencyManager agencyManager) {
        this.agencyManager = agencyManager;
    }

    private ConfigManager configManager;

    @Autowired
    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    private WarehouseManager warehouseManager;

    @Autowired
    public void setWarehouseManager(WarehouseManager warehouseManager) {
        this.warehouseManager = warehouseManager;
    }

    private StaffManager staffManager;

    @Autowired
    public void setStaffManager(StaffManager staffManager) {
        this.staffManager = staffManager;
    }

    private BannerManager bannerManager;

    @Autowired
    public void setBannerManager(BannerManager bannerManager) {
        this.bannerManager = bannerManager;
    }

    // load init data
    public void loadData() {
        productManager.loadData();
        programManager.loadData();
        agencyManager.loadData();
        configManager.loadData();
        warehouseManager.loadData();
        bannerManager.loadData();
        staffManager.loadData();
    }

    public void loadWarehouseInfo() {
        List<JSONObject> jsProductList = this.productManager.getAllProduct();
        for (JSONObject jsProduct : jsProductList) {
            int product_id = ConvertUtils.toInt(jsProduct.get("id"));
            JSONObject jsWarehouseInfo = this.warehouseManager.getWarehouseInfoByProduct(product_id);
            if (jsWarehouseInfo == null) {
                this.warehouseManager.initWarehouseStartDate(product_id);
            }
        }
    }

    public int getAcoinReward(
            Date dept_time,
            Date payment_deadline,
            long total_value_dept_acoin,
            Date payment_date,
            int dept_cycle,
            boolean hasCongNoAm) {
        try {
            if (payment_deadline.before(payment_date)
                    && hasCongNoAm == false) {
                return 0;
            }

            int acoin_rate_default = this.getConfigManager().getAcoinRateDefault();
            /**
             * Kỳ hạn nợ > 10 ngày
             */
            if (dept_cycle > 10) {
                int date_number = ConvertUtils.toInt((DateTimeUtils.getDateTime(payment_date, "yyyy-MM-dd").getTime()
                        - DateTimeUtils.getDateTime(dept_time, "yyyy-MM-dd").getTime()) / 86400000L);
                /**
                 * thanh toán trong vòng 5 ngày
                 */

                if (date_number <= 5) {
                    return ConvertUtils.toInt(total_value_dept_acoin * 1.5 / acoin_rate_default);
                } else {
                    return ConvertUtils.toInt(total_value_dept_acoin / acoin_rate_default);
                }
            } else {
                return ConvertUtils.toInt(total_value_dept_acoin / acoin_rate_default);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name());
        }
        return 0;
    }

    public void initDeptAgencyInfo() {
        /**
         *
         */
        this.agencyManager.initDeptAgencyInfo();
    }

    public void reloadBanner(int id) {
        this.bannerManager.loadData();
        this.productManager.getReloadService().callAppServerReload(
                CacheType.BANNER.getValue(), id
        );
    }

    public void reloadProduct(CacheType product, Integer product_id) {
        this.productManager.reloadProduct(product, product_id);
        if (product_id == 0) {
            this.programManager.loadAllProduct();
        } else {
            this.programManager.loadOneProduct(product_id);
        }
    }

    public void reloadProductGroup(CacheType productGroup, int rsCreateProductGroup) {
        this.productManager.reloadProductGroup(productGroup, rsCreateProductGroup);
        this.programManager.loadAllProductGroup();
    }

    public void reloadCategory(int rsCreateCategory) {
        this.productManager.reloadCategory(rsCreateCategory);
        this.programManager.loadAllCategory();
    }

    public void reloadBrand(int rsCreateBrand) {
        this.productManager.reloadBrand(rsCreateBrand);
        this.programManager.loadAllBrand();
    }

    public void callReloadProductVisibilitySetting(Integer id) {
        this.productManager.reloadProductVisibilitySetting(id);
    }

    public void reloadProductHot(int id) {
        this.productManager.reloadProductHot(id);
    }

    public void callReloadProductPriceSetting(int id) {
        this.productManager.callReloadProductPriceSetting(id);
    }

    public void reloadCombo(int id) {
        this.productManager.reloadCombo(id);
    }

    public void callReloadProductHotType(int id) {
        this.configManager
                .loadProductHotType();
        this.productManager.getReloadService().callAppServerReload(
                CacheType.PRODUCT_HOT_TYPE.getValue(),
                0
        );
    }

    public void reloadProductSmallUnit(int id) {
        this.productManager
                .loadProductSmallUnit();
        this.productManager.getReloadService().callAppServerReload(
                CacheType.PRODUCT_SMALL_UNIT.getValue(),
                0
        );
    }

    public void callReloadProductNew() {
        this.productManager.getReloadService().callAppServerReload(
                CacheType.PRODUCT_NEW.getValue(),
                0
        );
    }

    public void callReloadCatalog(int id) {
        this.productManager.loadCatalog();
        this.productManager.getReloadService().callAppServerReload(
                CacheType.CATALOG.getValue(),
                id
        );
    }

    public void callReloadConfig() {
        this.productManager.getReloadService().callAppServerReload(
                CacheType.CONFIG.getValue(),
                0
        );
    }

    public boolean addAgencyToCTXHByCircleType(Agency agency) {
        List<Program> programList = this.getListProgramCanJoin(agency, CircleType.DATE);
        if (programList.size() > 1) {
            return false;
        } else if (programList.size() == 1) {

        }
        return true;
    }

    public List<Program> getListProgramCanJoin(Agency agency, CircleType circleType) {
        List<Program> programList = new ArrayList<>();
        for (Program program : this.programManager.getMpCTXH().values()) {
            if (program.getStatus() == PromoActiveStatus.RUNNING.getId() &&
                    circleType.getCode().equals(program.getCircle_type())) {
                if (this.checkProgramFilter(agency, program)) {
                    programList.add(program);
                }
            }
        }
        return programList;
    }

    public List<JSONObject> getAllProgramCanJoin(
            Agency agency,
            String circleType,
            List<JSONObject> promoList) {
        List<JSONObject> programList = new ArrayList<>();
        for (JSONObject promo : promoList) {
            PromoHistoryEntity promoHistoryEntity = this.programManager.getPromoDB().getLastPromoHistory(
                    ConvertUtils.toInt(promo.get("id"))
            );
            if (promoHistoryEntity == null) {
                continue;
            }
            if (this.checkProgramFilter(agency, this.getProgramManager().importProgram(promoHistoryEntity.getPromo_data()))) {
                programList.add(promo);
            }
        }
        return programList;
    }

    public boolean checkProgramFilter(Agency agency, Program program) {
        try {
            // Loại trừ đại lý
            if (program.getLtIgnoreAgencyId().contains(agency.getId()))
                return false;
            // Bao gồm đại lý
            if (program.getLtIncludeAgencyId().contains(agency.getId()))
                return true;
            if (program.getLtIncludeAgencyId().isEmpty() && program.getLtProgramFilter().isEmpty())
                return true;
            if (!program.getLtIncludeAgencyId().isEmpty() && program.getLtProgramFilter().isEmpty())
                return false;
            // Bộ lọc
            for (ProgramFilter programFilter : program.getLtProgramFilter()) {
                // Kiểm tra cấp bậc
                boolean isMatchedMembership = true;
                if (!programFilter.getLtAgencyMembershipId().isEmpty())
                    isMatchedMembership = programFilter.getLtAgencyMembershipId().contains(agency.getMembershipId());
                if (!isMatchedMembership)
                    continue;
                // Kiểm tra phòng kinh doanh
                boolean isMatchedAgencyBusinessDepartment = true;
                if (!programFilter.getLtAgencyBusinessDepartmentId().isEmpty())
                    isMatchedAgencyBusinessDepartment = programFilter.getLtAgencyBusinessDepartmentId().contains(agency.getBusinessDepartmentId());
                if (!isMatchedAgencyBusinessDepartment)
                    continue;
                // Kiểm tra tỉnh - tp
                boolean isMatchedAgencyCity = true;
                if (!programFilter.getLtAgencyCityId().isEmpty())
                    isMatchedAgencyCity = programFilter.getLtAgencyCityId().contains(agency.getCityId());
                if (!isMatchedAgencyCity)
                    continue;
                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return false;
    }
}