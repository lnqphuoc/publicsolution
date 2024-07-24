package com.app.server.manager;

import com.app.server.config.ConfigInfo;
import com.app.server.data.dto.agency.Membership;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.product.ProductGroup;
import com.app.server.data.dto.visibility.ProductVisibilitySetting;
import com.app.server.data.dto.visibility.ProductVisibilitySettingDetail;
import com.app.server.data.entity.*;
import com.app.server.data.dto.location.City;
import com.app.server.data.dto.location.District;
import com.app.server.data.dto.location.Region;
import com.app.server.data.dto.location.Ward;
import com.app.server.data.dto.product.Category;
import com.app.server.data.dto.staff.BusinessDepartment;
import com.app.server.data.extra.TypeFilter;
import com.app.server.database.AgencyDB;
import com.app.server.database.LocationDB;
import com.app.server.database.MasterDB;
import com.app.server.database.ProductDB;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.service.ReloadService;
import com.app.server.utils.JsonUtils;
import com.app.server.utils.SortUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Getter
@Setter
@Service
public class ProductManager {
    private LocationDB locationDB;
    private AgencyDB agencyDB;
    private ProductDB productDB;

    @Autowired
    public void setLocationDB(LocationDB locationDB) {
        this.locationDB = locationDB;
    }

    @Autowired
    public void setAgencyDB(AgencyDB agencyDB) {
        this.agencyDB = agencyDB;
    }

    @Autowired
    public void setProductDB(ProductDB productDB) {
        this.productDB = productDB;
    }

    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }

    private ReloadService reloadService;

    @Autowired
    public void setReloadService(ReloadService reloadService) {
        this.reloadService = reloadService;
    }

    private Map<Integer, Region> mpRegion = new LinkedHashMap<>(); // map of all region
    private Map<Integer, City> mpCity = new LinkedHashMap<>(); // map of all city
    private Map<Integer, District> mpDistrict = new LinkedHashMap<>(); // map of all district
    private Map<Integer, Ward> mpWard = new LinkedHashMap<>(); // map of all ward
    private Map<Integer, Category> mpCategory = new LinkedHashMap<>(); // map of all category
    private List<Category> ltCategory = new ArrayList<>(); // list of category will show in app
    private List<Category> ltNganhHang = new ArrayList<>();
    private List<Category> ltMatHang = new ArrayList<>();
    private List<Category> ltPhanLoai = new ArrayList<>();
    private List<Category> ltPhanLoaiTheoThuongHieu = new ArrayList<>();
    private Map<Integer, Membership> mpMembership = new LinkedHashMap<>(); // map of all Membership
    private Map<Integer, BusinessDepartment> mpBusinessArea = new LinkedHashMap<>(); // map of all BusinessArea
    private Map<Integer, ProductSmallUnitEntity> mpProductSmallUnit = new LinkedHashMap<>(); // map of all quy cách nhỏ
    private Map<Integer, ProductBigUnitEntity> mpProductBigUnit = new LinkedHashMap<>(); // map of all quy cách lớn
    private Map<Integer, ProductColorEntity> mpProductColor = new LinkedHashMap<>(); // map of all quy cách lớn

    private Map<Integer, BrandEntity> mpBrand = new LinkedHashMap<>();
    private Map<String, ConfigEntity> mpConfig = new LinkedHashMap<>();
    private Map<Integer, ProductCache> mpProduct = new LinkedHashMap<>();
    private Map<String, JSONObject> mpProductCode = new LinkedHashMap<>();
    private Map<Integer, BusinessDepartment> mpBusinessDepartment = new LinkedHashMap<>();
    private Map<Integer, JSONObject> mpDeptType = new LinkedHashMap<>();
    private Map<Integer, JSONObject> mpDeptTransactionMainType = new LinkedHashMap<>();
    private Map<Integer, JSONObject> mpDeptTransactionSubType = new LinkedHashMap<>();
    private Map<Integer, ProductGroup> mpProductGroup = new LinkedHashMap<>();

    private Map<Integer, ProductVisibilitySetting> mpProductVisibilitySetting = new HashMap<>();
    private Map<Integer, JSONObject> mpCatalog = new LinkedHashMap<>();

    // load init data
    public void loadData() {
        LogUtil.printDebug("PRODUCT-LOAD DATA");
        loadRegion();
        loadCity();
        loadDistrict();
        loadWard();
        loadCategory();
        loadMembership();
        loadBusinessArea();
        loadProductSmallUnit();
        loadProductBigUnit();
        loadProductColor();
        loadBrand();
        loadConfig();
        loadProductGroup();
        loadProduct();
        loadBusinessDepartment();
        loadDeptType();
        loadDeptTransactionMainType();
        loadDeptTransactionSubType();
        loadVisibility();
        loadCatalog();
        LogUtil.printDebug("PRODUCT-LOAD DATA DONE");
    }

    private void loadVisibility() {
        this.mpProductVisibilitySetting.clear();
        List<JSONObject> settingList = masterDB.find(
                "select *" +
                        " from product_visibility_setting" +
                        " where status =" + SettingStatus.RUNNING.getId() +
                        " AND start_date <= NOW() AND (end_date is NULL OR end_date >= NOW())");
        for (JSONObject setting : settingList) {
            int product_visibility_setting_id = ConvertUtils.toInt(setting.get("id"));
            ProductVisibilitySetting productVisibilitySetting = ProductVisibilitySetting.from(setting);
            List<JSONObject> settingDetailList = masterDB.find(
                    "select *" +
                            " from product_visibility_setting_detail" +
                            " where product_visibility_setting_id = " + product_visibility_setting_id +
                            " AND status = " + SettingStatus.RUNNING.getId() +
                            " AND start_date <= NOW() AND (end_date is NULL OR end_date >= NOW())");
            for (JSONObject settingDetail : settingDetailList) {
                ProductVisibilitySettingDetail productVisibilitySettingDetail =
                        JsonUtils.DeSerialize(JsonUtils.Serialize(settingDetail),
                                ProductVisibilitySettingDetail.class);

                productVisibilitySetting.getData().add(productVisibilitySettingDetail);
            }

            mpProductVisibilitySetting.put(product_visibility_setting_id, productVisibilitySetting);
        }
    }

    private void loadVisibilityOne(int id) {
        ProductVisibilitySetting oldSetting = this.mpProductVisibilitySetting.get(id);
        if (oldSetting != null) {
            this.mpProductVisibilitySetting.remove(id);
        }

        JSONObject jsSetting = masterDB.getOne("SELECT * FROM product_visibility_setting" +
                " WHERE id = " + id +
                " AND status =" + SettingStatus.RUNNING.getId());
        if (jsSetting == null) {
            return;
        }

        ProductVisibilitySetting productVisibilitySetting = JsonUtils.DeSerialize(JsonUtils.Serialize(jsSetting), ProductVisibilitySetting.class);
        List<JSONObject> settingDetailList = masterDB.find(
                "select *" +
                        " from product_visibility_setting_detail" +
                        " where product_visibility_setting_id = " + id +
                        " AND status = " + SettingStatus.RUNNING.getId());
        for (JSONObject settingDetail : settingDetailList) {
            ProductVisibilitySettingDetail productVisibilitySettingDetail =
                    JsonUtils.DeSerialize(JsonUtils.Serialize(settingDetail), ProductVisibilitySettingDetail.class);
            productVisibilitySetting.getData().add(productVisibilitySettingDetail);
        }

        mpProductVisibilitySetting.put(id, productVisibilitySetting);
    }

    private void loadProductGroup() {
        this.mpProductGroup.clear();
        List<JSONObject> rs = masterDB.find("select * from product_group");
        for (JSONObject js : rs) {
            mpProductGroup.put(
                    ConvertUtils.toInt(js.get("id")),
                    JsonUtils.DeSerialize(JsonUtils.Serialize(js), ProductGroup.class));
        }
    }

    private void loadDeptTransactionMainType() {
        this.mpDeptTransactionMainType.clear();
        List<JSONObject> rs = masterDB.find("select * from dept_transaction_main_type");
        for (JSONObject js : rs) {
            mpDeptTransactionMainType.put(ConvertUtils.toInt(js.get("id")), js);
        }
    }

    private void loadDeptTransactionSubType() {
        this.mpDeptTransactionSubType.clear();
        List<JSONObject> rs = masterDB.find("select * from dept_transaction_sub_type");
        for (JSONObject js : rs) {
            mpDeptTransactionSubType.put(ConvertUtils.toInt(js.get("id")), js);
        }
    }

    private void loadDeptType() {
        this.mpDeptType.clear();
        List<JSONObject> rs = masterDB.find("select * from dept_type");
        for (JSONObject js : rs) {
            mpDeptType.put(ConvertUtils.toInt(js.get("id")), js);
        }
    }

    private void loadBusinessDepartment() {
        mpBusinessDepartment.clear();
        List<JSONObject> rs = masterDB.find("select * from business_department");
        for (JSONObject js : rs) {
            BusinessDepartment businessDepartment = JsonUtils.DeSerialize(JsonUtils.Serialize(js), BusinessDepartment.class);
            mpBusinessDepartment.put(businessDepartment.getId(), businessDepartment);
        }
    }

    // load region
    private void loadRegion() {
        mpRegion.clear();
        locationDB.loadAllRegion(mpRegion);
    }

    // load city
    private void loadCity() {
        mpCity.clear();
        locationDB.loadAllCity(mpCity, mpRegion);
    }

    // load district
    private void loadDistrict() {
        mpDistrict.clear();
        locationDB.loadAllDistrict(mpDistrict, mpCity);
    }

    // load ward
    private void loadWard() {
        mpWard.clear();
        locationDB.loadAllWard(mpWard, mpDistrict);
    }

    // load category
    private void loadCategory() {
        mpCategory.clear();
        ltCategory.clear();
        ltNganhHang.clear();
        ltMatHang.clear();
        ltPhanLoai.clear();
        ltPhanLoaiTheoThuongHieu.clear();

        try {
            List<JSONObject> allCategory = this.productDB.loadAllCategory();
            List<Category> ltBranchCategory = new ArrayList<>();
            for (JSONObject jsCategory : allCategory) {
                try {
                    Category category = JsonUtils.DeSerialize(JsonUtils.Serialize(jsCategory), Category.class);
                    if (category == null) {
                        continue;
                    }
                    category.setImage_url(ImagePath.CATEGORY.getImageUrl());
                    /**
                     * Add category vào mp
                     */
                    mpCategory.put(category.getId(), category);

                    if (category.getParent_id() != 0) {
                        Category parentCategory = mpCategory.get(category.getParent_id());
                        if (parentCategory != null) {
                            parentCategory.getLtSub().add(category.getId());
                            category.setParent_name(parentCategory.getName());
                        }

                        CategoryLevel categoryLevel = CategoryLevel.from(category.getCategory_level());
                        switch (categoryLevel) {
                            case PHAN_LOAI_HANG_THEO_THUONG_HIEU: {
                                ltPhanLoaiTheoThuongHieu.add(category);
                                break;
                            }
                            case PHAN_LOAI_HANG: {
                                ltPhanLoai.add(category);
                                break;
                            }
                            case MAT_HANG: {
                                ltMatHang.add(category);
                                break;
                            }
                            case NGANH_HANG: {
                                ltNganhHang.add(category);
                                break;
                            }
                        }
                    } else if (category.getIs_branch() == 1) {
                        ltBranchCategory.add(category);
                        ltNganhHang.add(category);
                    }

                    ltCategory.add(category);
                } catch (Exception ex) {
                    LogUtil.printDebug(Module.PRODUCT.name(), ex);
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private void loadMembership() {
        mpMembership.clear();
        List<JSONObject> rs = masterDB.find("select * from membership");
        for (JSONObject js : rs) {
            Membership membership = JsonUtils.DeSerialize(JsonUtils.Serialize(js), Membership.class);
            mpMembership.put(membership.getId(), membership);
        }
    }

    private void loadBusinessArea() {
        mpBusinessArea.clear();
        List<JSONObject> rs = masterDB.find("select * from business_department");
        for (JSONObject js : rs) {
            BusinessDepartment entity = JsonUtils.DeSerialize(JsonUtils.Serialize(js), BusinessDepartment.class);
            mpBusinessArea.put(entity.getId(), entity);
        }
    }

    private void loadBrand() {
        mpBrand.clear();
        List<JSONObject> rs = masterDB.find("select * from brand");
        for (JSONObject js : rs) {
            BrandEntity entity = JsonUtils.DeSerialize(JsonUtils.Serialize(js), BrandEntity.class);
            mpBrand.put(entity.getId(), entity);
        }
    }

    public Map<Integer, City> getMpCity() {
        return mpCity;
    }

    public void setMpCity(Map<Integer, City> mpCity) {
        mpCity = mpCity;
    }

    /**
     * get region from city
     */
    public int getRegionFromCity(int cityId) {
        int regionId = 0;
        try {
            City city = mpCity.get(cityId);
            if (city != null) {
                Region region = city.getRegion();
                if (region != null)
                    regionId = region.getId();
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return regionId;
    }

    // get category from id
    public Category getCategoryById(int categoryId) {
        if (mpCategory.get(categoryId) != null)
            return mpCategory.get(categoryId);
        return null;
    }


    // get city name from id
    public String getCityNameById(int cityId) {
        if (mpCity.get(cityId) != null)
            return mpCity.get(cityId).getName();
        return "";
    }

    // get district name from id
    public String getDistrictNameById(int districtId) {
        if (mpDistrict.get(districtId) != null)
            return mpDistrict.get(districtId).getName();
        return "";
    }

    // get ward name from id
    public String getWardNameById(int wardId) {
        if (mpWard.get(wardId) != null)
            return mpWard.get(wardId).getName();
        return "";
    }

    public String getMembershipNameById(int membership_id) {
        if (mpMembership.get(membership_id) != null)
            return mpMembership.get(membership_id).getName();
        return "";
    }

    public String getCityCodeById(int cityId) {
        if (mpCity.get(cityId) != null)
            return mpCity.get(cityId).getCode();
        return "";
    }

    public String getRegionCodeById(int regionId) {
        if (mpRegion.get(regionId) != null)
            return mpRegion.get(regionId).getCode();
        return "";
    }

    public String getMpMembershipCodeById(int membershipId) {
        if (mpMembership.get(membershipId) != null)
            return mpMembership.get(membershipId).getCode();
        return "";
    }

    public void reloadCategory(int id) {
        /**
         * reload category
         */
        loadCategory();

        /**
         * update cache to appserver
         */
        this.reloadService.callAppServerReload(CacheType.CATEGORY.getValue(), id);
    }

    public void loadProductSmallUnit() {
        mpProductSmallUnit.clear();
        List<JSONObject> rs = masterDB.find("select * from product_small_unit WHERE status = 1");
        for (JSONObject js : rs) {
            ProductSmallUnitEntity entity = JsonUtils.DeSerialize(JsonUtils.Serialize(js), ProductSmallUnitEntity.class);
            mpProductSmallUnit.put(entity.getId(), entity);
        }
    }

    private void loadProductBigUnit() {
        mpProductBigUnit.clear();
        List<JSONObject> rs = masterDB.find("select * from product_big_unit WHERE status = 1");
        for (JSONObject js : rs) {
            ProductBigUnitEntity entity = JsonUtils.DeSerialize(JsonUtils.Serialize(js), ProductBigUnitEntity.class);
            mpProductBigUnit.put(entity.getId(), entity);
        }
    }

    private void loadProductColor() {
        mpProductColor.clear();
        List<JSONObject> rs = masterDB.find("select * from product_color");
        for (JSONObject js : rs) {
            ProductColorEntity entity = JsonUtils.DeSerialize(JsonUtils.Serialize(js), ProductColorEntity.class);
            mpProductColor.put(entity.getId(), entity);
        }
    }

    public void reloadBrand(int id) {
        /**
         * reload cache
         */
        if (id == 0) {
            loadBrand();
        } else {
            JSONObject jsonObject
                    = this.masterDB.getOne("select * from brand where id=" + id);
            if (jsonObject != null) {
                this.mpBrand.put(id, JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), BrandEntity.class));
            }
        }

        /**
         * update cache to appserver
         */
        this.reloadService.callAppServerReload(CacheType.BRAND.getValue(), id);
    }

    public int getCategoryPriority(CategoryEntity categoryEntity) {
        CategoryLevel categoryLevel = CategoryLevel.from(categoryEntity.getCategory_level());
        switch (categoryLevel) {
            case NGANH_HANG: {
                return this.ltNganhHang.size() + 1;
            }
            case MAT_HANG: {
                return this.ltMatHang.size() + 1;
            }
            case PHAN_LOAI_HANG: {
                return this.ltPhanLoai.size() + 1;
            }
            case PHAN_LOAI_HANG_THEO_THUONG_HIEU: {
                return this.ltPhanLoaiTheoThuongHieu.size() + 1;
            }
            default: {
                return 0;
            }
        }
    }

    public List<Category> getLtCategoryByLevel(int level) {
        CategoryLevel categoryLevel = CategoryLevel.from(level);
        switch (categoryLevel) {
            case NGANH_HANG: {
                return this.ltNganhHang;
            }
            case MAT_HANG: {
                return this.ltMatHang;
            }
            case PHAN_LOAI_HANG: {
                return this.ltPhanLoai;
            }
            case PHAN_LOAI_HANG_THEO_THUONG_HIEU: {
                return this.ltPhanLoaiTheoThuongHieu;
            }
            default: {
                return this.getLtCategory();
            }
        }
    }

    public List<Category> reArrange(List<Category> list, int j, int k, Category category) {
        list.remove(j);
        list.add(k, category);

        int a = 0;
        int b = 0;
        if (j < k) {
            a = j;
            b = k;
        } else {
            a = k;
            b = j;
        }

        List<Category> categories = new ArrayList<>();
        for (int i = a; i < b + 1; i++) {
            Category category1 = list.get(i);
            category1.setPriority(i + 1);

            LogUtil.printDebug(category1.getId() + "-" + (i + 1));

            categories.add(category1);
        }
        return categories;
    }

    public List<BrandEntity> getLtHighlightBrand() {
        List<BrandEntity> rs = new ArrayList<>();
        for (BrandEntity brandEntity : mpBrand.values()) {
            if (brandEntity.getIs_highlight() == 1) {
                rs.add(brandEntity);
            }
        }

        return rs;
    }

    public BrandEntity getLastHighlightBrand() {
        List<BrandEntity> rs = this.getLtHighlightBrand();
        if (rs.isEmpty()) {
            return null;
        } else {
            Collections.sort(rs, (a, b) -> a.getHighlight_priority() > b.getHighlight_priority() ? -1 : a.getHighlight_priority() == b.getHighlight_priority() ? 0 : 1);
            return rs.get(0);
        }
    }

    public int getListPriorityCategory(CategoryLevel categoryLevel) {
        switch (categoryLevel) {
            case MAT_HANG: {
                if (!this.ltMatHang.isEmpty()) {
                    Collections.sort(ltMatHang, (a, b) -> a.getPriority() > b.getPriority() ? -1 : a.getPriority() == b.getPriority() ? 0 : 1);
                    return ltCategory.get(0).getPriority();
                }
                return 0;
            }
            case PHAN_LOAI_HANG: {
                if (!this.ltPhanLoai.isEmpty()) {
                    Collections.sort(ltPhanLoai, (a, b) -> a.getPriority() > b.getPriority() ? -1 : a.getPriority() == b.getPriority() ? 0 : 1);
                    return ltPhanLoai.get(0).getPriority();
                }
                return 0;
            }
            case PHAN_LOAI_HANG_THEO_THUONG_HIEU: {
                if (!this.ltPhanLoaiTheoThuongHieu.isEmpty()) {
                    Collections.sort(ltPhanLoaiTheoThuongHieu, (a, b) -> a.getPriority() > b.getPriority() ? -1 : a.getPriority() == b.getPriority() ? 0 : 1);
                    return ltPhanLoaiTheoThuongHieu.get(0).getPriority();
                }
                return 0;
            }
        }
        return 0;
    }

    public void reloadProduct(CacheType cacheType, int id) {
        /**
         * call update cache to appserver
         */
        try {
            if (id == 0) {
                loadProduct();
            } else {
                JSONObject js = this.masterDB.getOne("SELECT * FROM product WHERE id=" + id);
                ProductCache entity = JsonUtils.DeSerialize(JsonUtils.Serialize(js), ProductCache.class);
                if (entity == null) {
                    return;
                }

                ProductGroup productGroup = this.mpProductGroup.get(entity.getProduct_group_id());
                if (productGroup == null) {
                    return;
                }
                Category pltth = this.getCategoryById(
                        productGroup.getCategory_id()
                );
                if (pltth == null) {
                    return;
                }
                entity.setPltth_id(pltth.getId());

                Category plsp = this.getCategoryById(
                        pltth.getParent_id()
                );
                if (plsp == null) {
                    return;
                }
                entity.setPlsp_id(plsp.getId());

                Category mat_hang = this.getCategoryById(
                        plsp.getParent_id()
                );
                if (mat_hang == null) {
                    return;
                }

                entity.setMat_hang_id(
                        mat_hang.getId()
                );
                entity.setCommon_price(entity.getPrice());
                mpProduct.put(entity.getId(), entity);
                mpProductCode.put(entity.getCode(), js);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("MASTER", ex);
        }


        this.reloadService.callAppServerReload(cacheType.getValue(), id);
    }

    public void reloadProductGroup(CacheType cacheType, int id) {
        if (id == 0) {
            loadProductGroup();
        } else {
            JSONObject jsonObject = this.masterDB.getOne("SELECT * FROM product_group WHERE id=" + id);
            ProductGroup productGroup = JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject), ProductGroup.class);
            mpProductGroup.put(productGroup.getId(), productGroup);
        }
        /**
         * call update cache to appserver
         */
        this.reloadService.callAppServerReload(cacheType.getValue(), id);
    }

    public String getColorName(Integer product_color_id) {
        ProductColorEntity productColorEntity = this.mpProductColor.get(product_color_id);
        if (productColorEntity != null) {
            return productColorEntity.getName();
        }
        return "";
    }

    public String getSmallUnitName(int product_small_unit_id) {
        ProductSmallUnitEntity productSmallUnitEntity = this.mpProductSmallUnit.get(product_small_unit_id);
        if (productSmallUnitEntity != null) {
            return productSmallUnitEntity.getName();
        }
        return "";
    }

    public String getBigUnitName(Integer product_big_unit_id) {
        if (product_big_unit_id == null) {
            return null;
        }
        ProductBigUnitEntity productBigUnitEntity = this.mpProductBigUnit.get(product_big_unit_id);
        if (productBigUnitEntity != null) {
            return productBigUnitEntity.getName();
        }
        return "";
    }

    private void loadConfig() {
        mpConfig.clear();
        List<JSONObject> rs = masterDB.find("select * from config");
        for (JSONObject js : rs) {
            ConfigEntity entity = JsonUtils.DeSerialize(JsonUtils.Serialize(js), ConfigEntity.class);
            mpConfig.put(entity.getCode(), entity);
        }
    }

    public Long getGiaTriToiThieu() {
        ConfigEntity config = mpConfig.get("GTTT");
        if (config == null) {
            return 0L;
        }

        return ConvertUtils.toLong(config.getData(), 0L);
    }

    public void sortData() {
        List<JSONObject> productGroupEntityList = productDB.getAllProductGroup();
        for (JSONObject productGroup : productGroupEntityList) {
            ProductGroupEntity productGroupEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(productGroup), ProductGroupEntity.class);
            productGroupEntity.setSort_data(SortUtils.convertProductCodeToProductSortData(productGroupEntity.getCode()));
            productDB.editProductGroup(productGroupEntity);
        }
    }

    private void loadProduct() {
        mpProduct.clear();
        mpProductCode.clear();
        List<JSONObject> rs = masterDB.find("select * from product WHERE status != -1");
        for (JSONObject js : rs) {
            ProductCache entity = JsonUtils.DeSerialize(JsonUtils.Serialize(js), ProductCache.class);
            if (entity == null) {
                continue;
            }

            ProductGroup productGroup = this.mpProductGroup.get(entity.getProduct_group_id());
            if (productGroup == null) {
                continue;
            }
            Category pltth = this.getCategoryById(
                    productGroup.getCategory_id()
            );
            if (pltth == null) {
                continue;
            }
            entity.setPltth_id(pltth.getId());

            Category plsp = this.getCategoryById(
                    pltth.getParent_id()
            );
            if (plsp == null) {
                continue;
            }
            entity.setPlsp_id(plsp.getId());

            Category mat_hang = this.getCategoryById(
                    plsp.getParent_id()
            );
            if (mat_hang == null) {
                continue;
            }

            entity.setMat_hang_id(
                    mat_hang.getId()
            );
            entity.setCommon_price(entity.getPrice());
            mpProduct.put(entity.getId(), entity);
            mpProductCode.put(entity.getCode(), js);
        }
    }

    public String getProductFullName(int item_id) {
        ProductCache productCache = this.mpProduct.get(item_id);
        if (productCache != null) {
            return productCache.getFull_name();
        }
        return "";
    }

    public String getProductCode(int item_id) {
        ProductCache productCache = this.mpProduct.get(item_id);
        if (productCache != null) {
            return productCache.getCode();
        }
        return "";
    }


    public ProductCache getProductBasicData(int product_id) {
        return this.mpProduct.get(product_id);
    }

    public List<JSONObject> getAllProduct() {
        return this.productDB.getAllProduct();
    }

    public double getProductPrice(int product_id, int agency_id) {
        ProductCache productCache = this.getProductBasicData(product_id);
        if (productCache != null) {
            return productCache.getPrice();
        }
        return 0;
    }

    public void reloadProductVisibilitySetting(Integer id) {
        loadVisibilityOne(id);
        /**
         * update cache to appserver
         */
        this.reloadService.callAppServerReload(CacheType.VISIBILITY.getValue(), id);
    }

    public void callReloadProductPriceSetting(Integer id) {
        /**
         * update cache to appserver
         */
        this.reloadService.callAppServerReload(CacheType.PRODUCT_PRICE.getValue(), id);
    }

    public void reloadProductHot(int id) {
        this.reloadService.callAppServerReload(CacheType.PRODUCT_HOT.getValue(), id);
    }

    public JSONObject getProductByCode(String code) {
        return this.mpProductCode.get(code);
    }

    public int getLastPriorityByParent(CategoryEntity categoryParentEntity) {
        int size = this.mpCategory.get(categoryParentEntity.getId()).getLtSub().size();
        if (size > 0) {
            int lastId = this.mpCategory.get(categoryParentEntity.getId()).getLtSub().get(size - 1);
            Category category = this.mpCategory.get(lastId);
            if (category != null) {
                return category.getPriority();
            }
        }
        return 0;
    }

    public void reloadCombo(Integer id) {


        /**
         * update cache to appserver
         */
        this.reloadService.callAppServerReload(CacheType.COMBO.getValue(), id);
    }

    public void callReloadCacheDealPrice() {
        /**
         * update cache to appserver
         */
        this.reloadService.callAppServerReload(CacheType.DEAL_PRICE.getValue(), 0);
    }

    public ProductVisibilitySetting getVisibilityByObject(String visibility_object_type, int visibility_object_id, Date date) {
        return mpProductVisibilitySetting.values().stream().filter(
                x -> x.checkRunning(visibility_object_type, visibility_object_id, date) == true
        ).findFirst().orElse(null);
    }

    public String getFullAddress(JSONObject agency) {
        String address = ConvertUtils.toString(agency.get("address"));
        if (mpWard.get(ConvertUtils.toInt(agency.get("ward_id"))) != null) {
            address += ", " + mpWard.get(ConvertUtils.toInt(agency.get("ward_id"))).getName();
        }
        if (mpDistrict.get(ConvertUtils.toInt(agency.get("district_id"))) != null) {
            address += ", " + mpDistrict.get(ConvertUtils.toInt(agency.get("district_id"))).getName();
        }
        if (mpCity.get(ConvertUtils.toInt(agency.get("city_id"))) != null) {
            address += ", " + mpCity.get(ConvertUtils.toInt(agency.get("city_id"))).getName();
        }

        return address;
    }

    public String getBusinessDepartmentName(int id) {
        BusinessDepartment businessDepartment = mpBusinessDepartment.get(id);
        if (businessDepartment != null) {
            return businessDepartment.getName();
        }
        return "";
    }

    public void loadCatalog() {
        this.mpCatalog.clear();
        List<JSONObject> rs = masterDB.find("select * from catalog");
        for (JSONObject js : rs) {
            mpCatalog.put(
                    ConvertUtils.toInt(js.get("id")),
                    js);
        }
    }

    public int getPLTTHDefault(int business_department_id) {
        Category category = ltPhanLoaiTheoThuongHieu.stream().filter(x -> x.getBusiness_department_id() == business_department_id).findFirst().orElse(null);
        if (category == null) {
            return 0;
        }
        return category.getId();
    }
}