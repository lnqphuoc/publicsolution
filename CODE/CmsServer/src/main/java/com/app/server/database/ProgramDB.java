package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.data.dto.agency.AgencyBasicData;
import com.app.server.data.dto.ctxh.CTXHAgencyResult;
import com.app.server.data.dto.program.DeptInfo;
import com.app.server.data.dto.program.Program;
import com.app.server.data.dto.program.ProgramType;
import com.app.server.data.dto.program.agency.Agency;
import com.app.server.data.dto.program.product.*;
import com.app.server.enums.AgencyStatus;
import com.app.server.enums.ImagePath;
import com.app.server.service.ProgramService;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import com.ygame.framework.utils.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProgramDB {
    public void loadAllColor(Map<Integer, Color> mpColor) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM product_color";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Color color = new Color();
                        color.setId(rs.getInt("id"));
                        color.setName(rs.getString("name"));
                        Timestamp timestamp = rs.getTimestamp("created_date");
                        color.setCreatedDate(new Date(timestamp.getTime()));
                        mpColor.put(color.getId(), color);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    // load all small unit
    public void loadAllSmallUnit(Map<Integer, Unit> mpUnit) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM product_small_unit";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Unit unit = new Unit();
                        unit.setId(rs.getInt("id"));
                        unit.setName(rs.getString("name"));
                        Timestamp timestamp = rs.getTimestamp("created_date");
                        unit.setCreatedDate(new Date(timestamp.getTime()));
                        mpUnit.put(unit.getId(), unit);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    // load all big unit
    public void loadAllBigUnit(Map<Integer, Unit> mpUnit) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM product_big_unit";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Unit unit = new Unit();
                        unit.setId(rs.getInt("id"));
                        unit.setName(rs.getString("name"));
                        Timestamp timestamp = rs.getTimestamp("created_date");
                        unit.setCreatedDate(new Date(timestamp.getTime()));
                        mpUnit.put(unit.getId(), unit);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    // convert ResultSet to brand
    public Brand convertResultSetToBrand(ResultSet rs) throws SQLException {
        Brand brand = new Brand();
        brand.setId(rs.getInt("id"));
        brand.setName(rs.getString("name"));
        String image = ImagePath.BRAND.getImageUrl() + rs.getString("image");
        brand.setImage(image);
        brand.setIsHighlight(rs.getInt("is_highlight"));
        brand.setHighlightPriority(rs.getInt("highlight_priority"));
        brand.setStatus(rs.getInt("status"));
        Timestamp timestamp = rs.getTimestamp("created_date");
        brand.setCreatedDate(new Date(timestamp.getTime()));
        return brand;
    }

    // load all brand
    public void loadAllBrand(Map<Integer, Brand> mpBrand) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM brand";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Brand brand = convertResultSetToBrand(rs);
                        mpBrand.put(brand.getId(), brand);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    public void loadAllProduct(Map<Integer, Product> mpProduct, Map<Integer, ProductGroup> mpProductGroup, Map<Integer, Brand> mpBrand) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM product";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Product product = convertResultSetToProduct(rs, mpProductGroup, mpBrand);
                        if (product.getProductGroup() != null)
                            product.getProductGroup().getLtProduct().add(product);
                        if (product.getBrand() != null)
                            product.getBrand().getLtProduct().add(product);
                        mpProduct.put(product.getId(), product);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    public void loadAllProductGroup(Map<Integer, ProductGroup> mpProductGroup, Map<Integer, Category> mpCategory) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM product_group";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        ProductGroup productGroup = convertResultSetToProductGroup(rs, mpCategory);
                        if (productGroup.getCategory() != null)
                            productGroup.getCategory().getLtProductGroup().add(productGroup);
                        mpProductGroup.put(productGroup.getId(), productGroup);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    // convert ResultSet to product
    public Product convertResultSetToProduct(ResultSet rs, Map<Integer, ProductGroup> mpProductGroup, Map<Integer, Brand> mpBrand) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setCode(rs.getString("code"));
        product.setShortName(rs.getString("short_name"));
        product.setFullName(rs.getString("full_name"));
        product.setWarrantyTime(rs.getString("warranty_time"));
        product.setImages(rs.getString("images"));
        product.setSpecification(rs.getString("specification"));
        product.setColorId(rs.getInt("product_color_id"));
        product.setCharacteristic(rs.getString("characteristic"));
        product.setDescription(rs.getString("description"));
        product.setUserManual(rs.getString("user_manual"));
        product.setTechnicalData(rs.getString("technical_data"));
        product.setStatus(rs.getInt("status"));
        product.setPrice(rs.getLong("price"));
        product.setSmallUnitId(rs.getInt("product_small_unit_id"));
        product.setBigUnitId(rs.getInt("product_big_unit_id"));
        product.setConvertSmallUnitRatio(rs.getInt("convert_small_unit_ratio"));
        product.setMinimumPurchase(rs.getInt("minimum_purchase"));
        product.setStep(rs.getInt("step"));
        product.setTotalSellQuantity(rs.getLong("total_sell_quantity"));
        product.setTotalSellTurn(rs.getLong("total_sell_turn"));
        product.setHotPriority(rs.getInt("hot_priority"));
        int productGroupId = rs.getInt("product_group_id");
        ProductGroup productGroup = mpProductGroup.get(productGroupId);
        product.setProductGroup(productGroup);
        product.setWarehouseQuantity(rs.getInt("warehouse_quantity"));
        Timestamp timestamp = rs.getTimestamp("created_date");
        product.setCreatedDate(new Date(timestamp.getTime()));
        product.setItemType(rs.getInt("item_type"));
        product.setSortData(rs.getString("sort_data"));
        int brandId = rs.getInt("brand_id");
        Brand brand = mpBrand.get(brandId);
        product.setBrand(brand);
        return product;
    }

    public ProductGroup convertResultSetToProductGroup(ResultSet rs, Map<Integer, Category> mpCategory) throws SQLException {
        ProductGroup productGroup = new ProductGroup();
        productGroup.setId(rs.getInt("id"));
        productGroup.setCode(rs.getString("code"));
        productGroup.setName(rs.getString("name"));
        productGroup.setStatus(rs.getInt("status"));
        Timestamp timestamp = rs.getTimestamp("created_date");
        productGroup.setCreatedDate(new Date(timestamp.getTime()));
        productGroup.setItemType(rs.getInt("item_type"));
        productGroup.setSimilarName(rs.getString("similar_name"));
        productGroup.setProductNumber(rs.getInt("product_number"));
        productGroup.setSortData(rs.getString("sort_data"));
        int categoryId = rs.getInt("category_id");
        Category category = mpCategory.get(categoryId);
        productGroup.setCategory(category);
        return productGroup;
    }

    public void loadAllCategory(Map<Integer, Category> mpCategory) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM category";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Category category = convertResultSetToCategory(rs);
                        mpCategory.put(category.getId(), category);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    public Category convertResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        String image = ImagePath.CATEGORY.getImageUrl() + rs.getString("image");
        category.setImage(image);
        category.setIsBranch(rs.getInt("is_branch"));
        category.setParentId(rs.getInt("parent_id"));
        category.setPriority(rs.getInt("priority"));
        category.setStatus(rs.getInt("status"));
        Timestamp timestamp = rs.getTimestamp("created_date");
        category.setCreatedDate(new Date(timestamp.getTime()));
        category.setItemType(rs.getInt("item_type"));
        category.setLevel(rs.getInt("category_level"));
        category.setParentPriority(rs.getInt("parent_priority"));
        return category;
    }

    public Agency getAgency(Integer agency_id) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM agency WHERE id=" + agency_id;
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Agency agency = new Agency();
                        agency.setId(rs.getInt("id"));
                        agency.setCode(rs.getString("code"));
                        agency.setPhone(rs.getString("phone"));
                        agency.setStatus(rs.getInt("status"));
                        agency.setMembershipId(rs.getInt("membership_id"));
                        agency.setCityId(rs.getInt("city_id"));
                        agency.setAvatar(rs.getString("avatar"));
                        agency.setBusinessDepartmentId(rs.getInt("business_department_id"));
                        agency.setBirthday(rs.getString("birthday"));
                        agency.setGender(rs.getInt("gender"));
                        agency.setBlockCsbh(rs.getInt("block_csbh"));
                        agency.setBlockCtkm(rs.getInt("block_ctkm"));
                        agency.setBlockCtsn(rs.getInt("block_ctsn"));
                        agency.setBlockPrice(rs.getInt("block_price"));
                        agency.setBlockCttl(rs.getInt("block_cttl"));
                        agency.setBlockCtss(rs.getInt("block_ctss"));
                        agency.setBlockCsdm(rs.getInt("block_csdm"));
                        return agency;
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return null;
    }

    public DeptInfo getDeptInfo(int agencyId) {
        DeptInfo deptInfo = new DeptInfo();
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM dept_agency_info WHERE agency_id=?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, agencyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        deptInfo.setDeptCycleStart(rs.getLong("dept_cycle_start"));
                        deptInfo.setDeptCycleEnd(rs.getLong("dept_cycle_end"));
                        deptInfo.setTotalPriceOrder(rs.getLong("total_price_order"));
                        deptInfo.setTotalPricePayment(rs.getLong("total_price_payment"));
                        deptInfo.setTotalPriceSales(rs.getLong("total_price_sales"));
                        deptInfo.setCurrentDept(rs.getLong("current_dept"));
                        deptInfo.setNgd(rs.getLong("ngd"));
                        deptInfo.setNqh(rs.getLong("nqh"));
                        deptInfo.setNx(rs.getLong("nx"));
                        deptInfo.setNth(rs.getLong("nth"));
                        deptInfo.setNdh(rs.getLong("ndh"));
                        deptInfo.setDeptCycle(rs.getInt("dept_cycle"));
                        deptInfo.setNgdLimit(rs.getLong("ngd_limit"));
                        deptInfo.setDeptLimit(rs.getLong("dept_limit"));
                        deptInfo.setDtt(rs.getLong("dtt"));
                        deptInfo.setTt(rs.getLong("tt"));
                        deptInfo.setTotalDttCycle(rs.getLong("total_dtt_cycle"));
                        deptInfo.setTotalTtCycle(rs.getLong("total_tt_cycle"));
                    }
                }
            }
        } catch (Exception ex) {
            deptInfo = new DeptInfo();
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return deptInfo;
    }

    public int countOrderByProgram(int agencyId, int programId, List<Integer> ltStatus) {
        int count = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            String param = ltStatus.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",", "(", ")"));
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT COUNT(id) AS count FROM agency_order WHERE promo_all_id_info LIKE '%\"" + programId + "\"%' AND status IN (?)";
            if (agencyId > 0)
                sql = "SELECT COUNT(id) AS count FROM agency_order WHERE agency_id=? AND promo_all_id_info LIKE '%\"" + programId + "\"%' AND status IN (?)";
            sql = sql.replace("(?)", param);
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                if (agencyId > 0)
                    stmt.setInt(1, agencyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        count = rs.getInt("count");
                }
            }
        } catch (Exception ex) {
            count = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return count;
    }

    public List<Agency> getAllAgency() {
        List<Agency> result = new ArrayList<>();
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM agency WHERE status != -1";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Agency agency = new Agency();
                        agency.setId(rs.getInt("id"));
                        agency.setCode(rs.getString("code"));
                        agency.setShop_name(rs.getString("shop_name"));
                        agency.setPhone(rs.getString("phone"));
                        agency.setStatus(rs.getInt("status"));
                        agency.setMembershipId(rs.getInt("membership_id"));
                        agency.setCityId(rs.getInt("city_id"));
                        agency.setAvatar(rs.getString("avatar"));
                        agency.setBusinessDepartmentId(rs.getInt("business_department_id"));
                        agency.setBirthday(rs.getString("birthday"));
                        agency.setGender(rs.getInt("gender"));
                        agency.setBlockCsbh(rs.getInt("block_csbh"));
                        agency.setBlockCtkm(rs.getInt("block_ctkm"));
                        agency.setBlockCtsn(rs.getInt("block_ctsn"));
                        agency.setBlockPrice(rs.getInt("block_price"));
                        result.add(agency);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return result;
    }

    public Product loadOneProduct(
            Map<Integer, Product> mpProduct,
            Map<Integer, ProductGroup> mpProductGroup,
            Map<Integer, Brand> mpBrand,
            int id) {
        ManagerIF cm = null;
        Connection con = null;
        Product product = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM product WHERE id = " + id;
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        product = convertResultSetToProduct(rs, mpProductGroup, mpBrand);
                        if (product.getProductGroup() != null)
                            product.getProductGroup().getLtProduct().add(product);
                        if (product.getBrand() != null)
                            product.getBrand().getLtProduct().add(product);
                        mpProduct.put(product.getId(), product);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return product;
    }

    public List<CTXHAgencyResult> getAllAgencyReadyJoinCTXH() {
        List<CTXHAgencyResult> result = new ArrayList<>();
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM agency WHERE status = " + AgencyStatus.APPROVED.getValue() + "" +
                    " OR status = " + AgencyStatus.LOCK.getValue();
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        CTXHAgencyResult ctxhAgencyResult = new CTXHAgencyResult();
                        AgencyBasicData agency = new AgencyBasicData();
                        agency.setId(rs.getInt("id"));
                        agency.setCode(rs.getString("code"));
                        agency.setPhone(rs.getString("phone"));
                        agency.setStatus(rs.getInt("status"));
                        agency.setMembership_id(rs.getInt("membership_id"));
                        agency.setCity_id(rs.getInt("city_id"));
                        agency.setAvatar(rs.getString("avatar"));
                        agency.setBusiness_department_id(rs.getInt("business_department_id"));
                        ctxhAgencyResult.setAgency_info(agency);
                        ctxhAgencyResult.setNickname(ConvertUtils.toString(rs.getString("nick_name")));

                        Agency agencyProgram = new Agency();
                        agencyProgram.setId(rs.getInt("id"));
                        agencyProgram.setCode(rs.getString("code"));
                        agencyProgram.setShop_name(rs.getString("shop_name"));
                        agencyProgram.setPhone(rs.getString("phone"));
                        agencyProgram.setStatus(rs.getInt("status"));
                        agencyProgram.setMembershipId(rs.getInt("membership_id"));
                        agencyProgram.setCityId(rs.getInt("city_id"));
                        agencyProgram.setAvatar(rs.getString("avatar"));
                        agencyProgram.setBusinessDepartmentId(rs.getInt("business_department_id"));
                        agencyProgram.setBirthday(rs.getString("birthday"));
                        agencyProgram.setGender(rs.getInt("gender"));
                        agencyProgram.setBlockCsbh(rs.getInt("block_csbh"));
                        agencyProgram.setBlockCtkm(rs.getInt("block_ctkm"));
                        agencyProgram.setBlockCtsn(rs.getInt("block_ctsn"));
                        agencyProgram.setBlockPrice(rs.getInt("block_price"));
                        ctxhAgencyResult.setAgency(agencyProgram);
                        result.add(ctxhAgencyResult);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return result;
    }

    public List<Agency> getListAgencyReadyJoinCTXH() {
        List<Agency> result = new ArrayList<>();
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM agency WHERE status = " + AgencyStatus.APPROVED.getValue() + "" +
                    " OR status = " + AgencyStatus.LOCK.getValue();
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Agency agency = new Agency();
                        agency.setId(rs.getInt("id"));
                        agency.setCode(rs.getString("code"));
                        agency.setShop_name(rs.getString("shop_name"));
                        agency.setPhone(rs.getString("phone"));
                        agency.setStatus(rs.getInt("status"));
                        agency.setMembershipId(rs.getInt("membership_id"));
                        agency.setCityId(rs.getInt("city_id"));
                        agency.setAvatar(rs.getString("avatar"));
                        agency.setBusinessDepartmentId(rs.getInt("business_department_id"));
                        agency.setBirthday(rs.getString("birthday"));
                        agency.setGender(rs.getInt("gender"));
                        agency.setBlockCsbh(rs.getInt("block_csbh"));
                        agency.setBlockCtkm(rs.getInt("block_ctkm"));
                        agency.setBlockCtsn(rs.getInt("block_ctsn"));
                        agency.setBlockPrice(rs.getInt("block_price"));
                        agency.setBlockCttl(rs.getInt("block_cttl"));
                        agency.setBlockCtss(rs.getInt("block_ctss"));
                        agency.setBlockCsdm(rs.getInt("block_csdm"));
                        result.add(agency);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return result;
    }

    public List<Agency> getListAgencyReadyJoinMission() {
        List<Agency> result = new ArrayList<>();
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM agency WHERE status = " + AgencyStatus.APPROVED.getValue();
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Agency agency = new Agency();
                        agency.setId(rs.getInt("id"));
                        agency.setCode(rs.getString("code"));
                        agency.setShop_name(rs.getString("shop_name"));
                        agency.setPhone(rs.getString("phone"));
                        agency.setStatus(rs.getInt("status"));
                        agency.setMembershipId(rs.getInt("membership_id"));
                        agency.setCityId(rs.getInt("city_id"));
                        agency.setAvatar(rs.getString("avatar"));
                        agency.setBusinessDepartmentId(rs.getInt("business_department_id"));
                        agency.setBirthday(rs.getString("birthday"));
                        agency.setGender(rs.getInt("gender"));
                        agency.setBlockCsbh(rs.getInt("block_csbh"));
                        agency.setBlockCtkm(rs.getInt("block_ctkm"));
                        agency.setBlockCtsn(rs.getInt("block_ctsn"));
                        agency.setBlockPrice(rs.getInt("block_price"));
                        agency.setBlockCttl(rs.getInt("block_cttl"));
                        agency.setBlockCtss(rs.getInt("block_ctss"));
                        agency.setBlockCsdm(rs.getInt("block_csdm"));
                        result.add(agency);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return result;
    }
}