package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.data.dto.product.Category;
import com.app.server.data.dto.product.Product;
import com.app.server.data.entity.*;
import com.app.server.database.repository.*;
import com.app.server.database.sql.ProductSQL;
import com.app.server.enums.SettingStatus;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProductDB extends BaseDB {
    private MasterDB masterDB;

    @Autowired
    public void setMasterDB(MasterDB masterDB) {
        this.masterDB = masterDB;
    }

    private ProductSQL productSQL;

    @Autowired
    public void setProductSQL(ProductSQL productSQL) {
        this.productSQL = productSQL;
    }

    // load all category
    public List<JSONObject> loadAllCategory() {
        String sql = this.productSQL.loadAllCategory();
        return this.masterDB.find(sql);
    }

    // get product by category id
    public List<Product> getProductByCategoryId(int categoryId) {
        List<Product> ltProduct = new ArrayList<>();
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT p.id,p.name,p.image FROM product p, product_category pc " +
                    "WHERE p.status > 0 AND p.id=pc.product_id AND pc.category_id=? AND p.status=1";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, categoryId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Product product = new Product();
                        product.setId(rs.getInt("id"));
                        product.setName(rs.getString("name"));
                        product.setImage(ConfigInfo.IMAGE_URL + "product/" + rs.getString("image"));
                        ltProduct.add(product);
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
        return ltProduct;
    }

    public List<JSONObject> filterCategory(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotalCategory(String query) {
        return this.masterDB.getTotal(query);
    }

    public List<JSONObject> filterProduct(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotalProduct(String query) {
        return this.masterDB.getTotal(query);
    }

    public List<JSONObject> filterProductGroup(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotalProductGroup(String query) {
        return this.masterDB.getTotal(query);
    }

    public int createCategory(CategoryEntity request) {
        String sql = this.productSQL.createCategory(request);
        return this.masterDB.insert(sql);
    }

    public List<JSONObject> searchCategory(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public boolean editCategory(CategoryEntity entity) {
        String sql = this.productSQL.editCategory(entity);
        return this.masterDB.update(sql);
    }

    public int createBrand(BrandEntity brandEntity) {
        String sql = this.productSQL.createBrand(brandEntity);
        return this.masterDB.insert(sql);
    }

    public boolean editBrand(BrandEntity brandEntity) {
        String sql = this.productSQL.editBrand(brandEntity);
        return this.masterDB.update(sql);
    }

    public ProductEntity getProduct(int id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM product WHERE id = " + id
        );

        if (rs != null) {
            return ProductEntity.from(rs);
        }

        return null;
    }

    public ProductEntity getProductInfo(int id) {
        String sql = this.productSQL.getProductInfo(id);
        JSONObject rs = this.masterDB.getOne(sql);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), ProductEntity.class);
        }
        return null;
    }

    public ProductGroupEntity getProductGroupInfo(int product_group_id) {
        String sql = this.productSQL.getProductGroupInfo(product_group_id);
        JSONObject rs = this.masterDB.getOne(sql);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), ProductGroupEntity.class);
        }
        return null;
    }

    public int createProductGroup(ProductGroupEntity productGroupEntity) {
        String sql = this.productSQL.createProductGroup(productGroupEntity);
        return this.masterDB.insert(sql);
    }

    public List<JSONObject> searchBrand(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public List<JSONObject> searchProductGroup(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public boolean editProductGroup(ProductGroupEntity productGroupEntity) {
        String sql = this.productSQL.editProductGroup(productGroupEntity);
        return this.masterDB.update(sql);
    }

    public BrandEntity getBrandInfo(int id) {
        String sql = this.productSQL.getBrandInfo(id);
        JSONObject rs = this.masterDB.getOne(sql);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), BrandEntity.class);
        }
        return null;
    }

    public boolean deleteBrand(BrandEntity brandEntity) {
        String sql = this.productSQL.deleteBrand(brandEntity);
        return this.masterDB.update(sql);
    }

    public CategoryEntity getCategoryInfo(int id) {
        String sql = this.productSQL.getCategoryInfo(id);
        JSONObject rs = this.masterDB.getOne(sql);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), CategoryEntity.class);
        }
        return null;
    }

    public boolean deleteCategory(CategoryEntity categoryEntity) {
        String sql = this.productSQL.deleteCategory(categoryEntity);
        return this.masterDB.update(sql);
    }

    public int createProduct(ProductEntity productEntity) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query = this.productSQL.createProduct(productEntity);
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, productEntity.getShort_name());
                stmt.setString(2, productEntity.getFull_name());
                stmt.setString(3, productEntity.getDescription());
                stmt.setString(4, productEntity.getTechnical_data());
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;
    }

    public ProductEntity getProductByCode(String code) {
        String sql = this.productSQL.getProductByCode(code);
        JSONObject rs = this.masterDB.getOne(sql);
        if (rs != null) {
            return JsonUtils.DeSerialize(JsonUtils.Serialize(rs), ProductEntity.class);
        }
        return null;
    }

    public JSONObject getProductInfoByCode(String code) {
        String sql = this.productSQL.getProductByCode(code);
        return this.masterDB.getOne(sql);
    }

    public int addProductColor(ProductColorEntity productColorEntity) {
        String sql = this.productSQL.addProductColor(productColorEntity);
        return this.masterDB.insert(sql);
    }

    public boolean editProductTechnicalData(int id, String data) {
        String sql = this.productSQL.editProductTechnicalData(id, data);
        return this.masterDB.update(sql);
    }

    public boolean editProductUserManual(int id, String data) {
        String sql = this.productSQL.editProductUserManual(id, data);
        return this.masterDB.update(sql);
    }

    public boolean editProductDescription(int id, String data) {
        String sql = this.productSQL.editProductDescription(id, data);
        return this.masterDB.update(sql);
    }

    public boolean editProductWarrantyTime(int id, String data) {
        String sql = this.productSQL.editProductWarrantyTime(id, data);
        return this.masterDB.update(sql);
    }

    public boolean checkProductColorExist(String name) {
        JSONObject rs = this.getProductColorByName(name);
        if (rs != null) {
            return true;
        }
        return false;
    }

    private JSONObject getProductColorByName(String name) {
        String sql = productSQL.getProductColorByName(name);
        return this.masterDB.getOne(sql);
    }

    public boolean editProductInfo(ProductEntity entity) {
        boolean status = false;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "UPDATE product SET" +
                    " code = " + parseStringToSql(entity.getCode()) + "," +
                    " short_name = ?" + "," +
                    " full_name = ?" + "," +
                    " warranty_time = " + parseStringToSql(entity.getWarranty_time()) + "," +
                    " images = " + parseStringToSql(entity.getImages()) + "," +
                    " specification = ?" + "," +
                    " product_color_id = " + parseIntegerToSql(entity.getProduct_color_id()) + "," +
                    " characteristic = " + parseStringToSql(entity.getCharacteristic()) + "," +
                    " description = ?" + "," +
                    " user_manual = " + parseStringToSql(entity.getUser_manual()) + "," +
                    " technical_data = ?" + "," +
                    " status = " + parseIntegerToSql(entity.getStatus()) + "," +
                    " price = " + parseIntegerToSql(entity.getPrice()) + "," +
                    " product_small_unit_id = " + parseIntegerToSql(entity.getProduct_small_unit_id()) + "," +
                    " product_big_unit_id = " + parseIntegerToSql(entity.getProduct_big_unit_id()) + "," +
                    " convert_small_unit_ratio = " + parseIntegerToSql(entity.getConvert_small_unit_ratio()) + "," +
                    " minimum_purchase = " + parseIntegerToSql(entity.getMinimum_purchase()) + "," +
                    " step = " + parseIntegerToSql(entity.getStep()) + "," +
                    " total_sell_quantity = " + parseIntegerToSql(entity.getTotal_sell_quantity()) + "," +
                    " total_sell_turn = " + parseIntegerToSql(entity.getTotal_sell_turn()) + "," +
                    " hot_priority = " + parseIntegerToSql(entity.getHot_priority()) + "," +
                    " product_group_id = " + parseIntegerToSql(entity.getProduct_group_id()) + "," +
                    " warehouse_quantity = " + parseIntegerToSql(entity.getWarehouse_quantity()) + "," +
                    " item_type = " + parseIntegerToSql(entity.getItem_type()) + "," +
                    " sort_data = " + parseStringToSql(entity.getSort_data()) + "," +
                    " brand_id = " + parseIntegerToSql(entity.getBrand_id()) + "," +
                    " category_id = " + parseIntegerToSql(entity.getCategory_id()) + "," +
                    " other_name = " + parseStringToSql(entity.getOther_name()) + "," +
                    " app_active = " + parseIntegerToSql(entity.getApp_active()) + "," +
                    " hot_label = " + parseStringToSql(entity.getHot_label()) +
                    " WHERE id = " + entity.getId();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, entity.getShort_name());
                stmt.setString(2, entity.getFull_name());
                stmt.setString(3, entity.getSpecification());
                stmt.setString(4, entity.getDescription());
                stmt.setString(5, entity.getTechnical_data());
                int row = stmt.executeUpdate();
                if (row > 0) {
                    status = true;
                }
            }
        } catch (Exception ex) {
            status = false;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return status;
    }

    public List<JSONObject> searchProduct(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public boolean updateSortCategory(List<Category> categories) {
        String query = this.productSQL.updateCategoryPriority();

        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                for (int i = 0; i < categories.size(); i++) {
                    Category category = categories.get(i);
                    stmt.setInt(1, i + 1);
                    stmt.setInt(2, category.getId());
                    stmt.addBatch();
                }
                int rs[] = stmt.executeBatch();

                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return false;
    }

    public boolean updateSortCategoryChild(List<Integer> categories) {
        String query = this.productSQL.updateCategoryPriority();

        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                for (int i = 0; i < categories.size(); i++) {
                    stmt.setInt(1, i + 1);
                    stmt.setInt(2, categories.get(i));
                    stmt.addBatch();
                }
                int rs[] = stmt.executeBatch();

                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return false;
    }

    public boolean updateSortParentCategory(List<Category> categories) {
        String query = this.productSQL.updateCategoryParentPriority();

        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                for (Category category : categories) {
                    stmt.setInt(1, category.getPriority());
                    stmt.setInt(2, category.getId());
                    stmt.addBatch();
                }
                int rs[] = stmt.executeBatch();

                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return false;
    }

    public boolean increaseProductNumberInProductGroup(int id) {
        String sql = this.productSQL.increaseProductNumberInProductGroup(id);
        return this.masterDB.update(sql);
    }

    public boolean decreaseProductNumberInProductGroup(int id) {
        String sql = this.productSQL.decreaseProductNumberInProductGroup(id);
        return this.masterDB.update(sql);
    }

    public List<JSONObject> getListProductByProductGroup(int id) {
        String sql = this.productSQL.getListProductByProductGroup(id);
        return this.masterDB.find(sql);
    }

    public boolean unHighlightBrand() {
        String sql = this.productSQL.unHighlightBrand();
        return this.masterDB.update(sql);
    }

    public boolean highlightBrand(List<Integer> ids) {
        String sql = this.productSQL.highlightBrand();

        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                for (int i = 0; i < ids.size(); i++) {
                    stmt.setInt(1, i + 1);
                    stmt.setInt(2, ids.get(i));
                    stmt.addBatch();
                }
                int rs[] = stmt.executeBatch();

                return true;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return false;
    }

    public boolean updateCategoryForProductByProductGroup(int product_group_id, int category_id) {
        String sql = this.productSQL.updateCategoryForProductByProductGroup(product_group_id, category_id);
        return this.masterDB.update(sql);
    }

    public List<JSONObject> getAllProductGroup() {
        String sql = "SELECT * FROM product_group WHERE sort_data = '' OR sort_data IS NULL";
        return this.masterDB.find(sql);
    }

    public List<JSONObject> searchTreeProduct(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public List<JSONObject> getAllProduct() {
        return this.masterDB.find(
                "SELECT * FROM product"
        );
    }

    public List<JSONObject> filter(String query, int offset, int pageSize, int isLimit) {
        if (isLimit == 1) {
            query += " LIMIT " + offset + "," + pageSize;
        }
        return this.masterDB.find(query);
    }

    public int getTotal(String query) {
        return this.masterDB.getTotal(query);
    }

    public JSONObject getProductVisibilitySetting(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM product_visibility_setting WHERE id = " + id
        );
    }

    public int insertProductVisibilitySetting(
            ProductVisibilitySettingEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO product_visibility_setting(" +
                        "name" + "," +
                        "creator_id" + "," +
                        "created_date" + "," +
                        "modified_date" + "," +
                        "modifier_id" + "," +
                        "agency_id" + "," +
                        "city_id" + "," +
                        "region_id" + "," +
                        "membership_id" + "," +
                        "status" + "," +
                        "start_date" + "," +
                        "end_date" + "," +
                        "visibility_object_type" + "," +
                        "visibility_object_id" +
                        ")" +
                        " VALUES(" +
                        parseStringToSql(entity.getName()) + "," +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getAgency_id()) + "," +
                        parseIntegerToSql(entity.getCity_id()) + "," +
                        parseIntegerToSql(entity.getRegion_id()) + "," +
                        parseIntegerToSql(entity.getMembership_id()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getStart_date()) + "," +
                        parseDateToSql(entity.getEnd_date()) + "," +
                        parseStringToSql(entity.getVisibility_object_type()) + "," +
                        parseIntegerToSql(entity.getVisibility_object_id()) +
                        ")"
        );
    }

    public boolean updateProductVisibilitySetting(
            ProductVisibilitySettingEntity entity) {
        return this.masterDB.update(
                "UPDATE product_visibility_setting SET" +
                        " name = " + parseStringToSql(entity.getName()) + "," +
                        " creator_id = " + parseIntegerToSql(entity.getCreator_id()) + "," +
                        " created_date = " + parseDateToSql(entity.getCreated_date()) + "," +
                        " modified_date = " + parseDateToSql(entity.getModified_date()) + "," +
                        " modifier_id = " + parseIntegerToSql(entity.getModifier_id()) + "," +
                        " agency_id = " + parseIntegerToSql(entity.getAgency_id()) + "," +
                        " city_id = " + parseIntegerToSql(entity.getCity_id()) + "," +
                        " region_id = " + parseIntegerToSql(entity.getRegion_id()) + "," +
                        " membership_id = " + parseIntegerToSql(entity.getMembership_id()) + "," +
                        " status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        " start_date = " + parseDateToSql(entity.getStart_date()) + "," +
                        " end_date = " + parseDateToSql(entity.getEnd_date()) + "," +
                        " visibility_object_type = " + parseStringToSql(entity.getVisibility_object_type()) + "," +
                        " visibility_object_id = " + parseIntegerToSql(entity.getVisibility_object_id()) +
                        " WHERE id = " + entity.getId()
        );
    }

    public ProductVisibilitySettingEntity getProductVisibilitySettingEntity(int id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM product_visibility_setting WHERE id = " + id
        );

        if (rs != null) {
            return ProductVisibilitySettingEntity.from(rs);
        }
        return null;
    }

    public int insertProductVisibilitySettingDetail(
            ProductVisibilitySettingDetailEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO product_visibility_setting_detail(" +
                        "creator_id" + "," +
                        "created_date" + "," +
                        "modified_date" + "," +
                        "modifier_id" + "," +
                        "product_id" + "," +
                        "product_group_id" + "," +
                        "category_level_1_id" + "," +
                        "category_level_2_id" + "," +
                        "category_level_3_id" + "," +
                        "category_level_4_id" + "," +
                        "brand_id" + "," +
                        "status" + "," +
                        "start_date" + "," +
                        "end_date" + "," +
                        "visibility" + "," +
                        "visibility_data_type" + "," +
                        "visibility_data_id" + "," +
                        "product_visibility_setting_id" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getProduct_id()) + "," +
                        parseIntegerToSql(entity.getProduct_group_id()) + "," +
                        parseIntegerToSql(entity.getCategory_level_1_id()) + "," +
                        parseIntegerToSql(entity.getCategory_level_2_id()) + "," +
                        parseIntegerToSql(entity.getCategory_level_3_id()) + "," +
                        parseIntegerToSql(entity.getCategory_level_4_id()) + "," +
                        parseIntegerToSql(entity.getBrand_id()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getStart_date()) + "," +
                        parseDateToSql(entity.getEnd_date()) + "," +
                        parseIntegerToSql(entity.getVisibility()) + "," +
                        parseStringToSql(entity.getVisibility_data_type()) + "," +
                        parseIntegerToSql(entity.getVisibility_data_id()) + "," +
                        parseIntegerToSql(entity.getProduct_visibility_setting_id()) +
                        ")"
        );
    }

    public boolean updateProductVisibilitySettingDetail(
            ProductVisibilitySettingDetailEntity entity) {
        return this.masterDB.update(
                "UPDATE product_visibility_setting_detail SET" +
                        " creator_id = " + parseIntegerToSql(entity.getCreator_id()) + "," +
                        " created_date = " + parseDateToSql(entity.getCreated_date()) + "," +
                        " modified_date = " + parseDateToSql(entity.getModified_date()) + "," +
                        " modifier_id = " + parseIntegerToSql(entity.getModifier_id()) + "," +
                        " product_id = " + parseIntegerToSql(entity.getProduct_id()) + "," +
                        " product_group_id = " + parseIntegerToSql(entity.getProduct_group_id()) + "," +
                        " category_level_1_id = " + parseIntegerToSql(entity.getCategory_level_1_id()) + "," +
                        " category_level_2_id = " + parseIntegerToSql(entity.getCategory_level_2_id()) + "," +
                        " category_level_3_id = " + parseIntegerToSql(entity.getCategory_level_3_id()) + "," +
                        " category_level_4_id = " + parseIntegerToSql(entity.getCategory_level_4_id()) + "," +
                        " brand_id = " + parseIntegerToSql(entity.getBrand_id()) + "," +
                        " status = " + parseIntegerToSql(entity.getStatus()) + "," +
                        " start_date = " + parseDateToSql(entity.getStart_date()) + "," +
                        " end_date = " + parseDateToSql(entity.getEnd_date()) + "," +
                        " visibility = " + parseIntegerToSql(entity.getVisibility()) + "," +
                        " visibility_data_type = " + parseStringToSql(entity.getVisibility_data_type()) + "," +
                        " visibility_data_id = " + parseIntegerToSql(entity.getVisibility_data_id()) + "," +
                        " product_visibility_setting_id = " + parseIntegerToSql(entity.getProduct_visibility_setting_id()) +
                        " WHERE id = " + entity.getId()
        );
    }

    public int insertProductVisibilitySettingDetailHistory(
            ProductVisibilitySettingDetailHistoryEntity entity) {
        return this.masterDB.insert(
                "INSERT INTO product_visibility_setting_detail_history(" +
                        "creator_id" + "," +
                        "created_date" + "," +
                        "modified_date" + "," +
                        "modifier_id" + "," +
                        "product_id" + "," +
                        "product_group_id" + "," +
                        "category_level_1_id" + "," +
                        "category_level_2_id" + "," +
                        "category_level_3_id" + "," +
                        "category_level_4_id" + "," +
                        "brand_id" + "," +
                        "status" + "," +
                        "start_date" + "," +
                        "end_date" + "," +
                        "visibility" + "," +
                        "visibility_data_type" + "," +
                        "visibility_data_id" + "," +
                        "product_visibility_setting_id" +
                        ") VALUES(" +
                        parseIntegerToSql(entity.getCreator_id()) + "," +
                        parseDateToSql(entity.getCreated_date()) + "," +
                        parseDateToSql(entity.getModified_date()) + "," +
                        parseIntegerToSql(entity.getModifier_id()) + "," +
                        parseIntegerToSql(entity.getProduct_id()) + "," +
                        parseIntegerToSql(entity.getProduct_group_id()) + "," +
                        parseIntegerToSql(entity.getCategory_level_1_id()) + "," +
                        parseIntegerToSql(entity.getCategory_level_2_id()) + "," +
                        parseIntegerToSql(entity.getCategory_level_3_id()) + "," +
                        parseIntegerToSql(entity.getCategory_level_4_id()) + "," +
                        parseIntegerToSql(entity.getBrand_id()) + "," +
                        parseIntegerToSql(entity.getStatus()) + "," +
                        parseDateToSql(entity.getStart_date()) + "," +
                        parseDateToSql(entity.getEnd_date()) + "," +
                        parseIntegerToSql(entity.getVisibility()) + "," +
                        parseStringToSql(entity.getVisibility_data_type()) + "," +
                        parseIntegerToSql(entity.getVisibility_data_id()) + "," +
                        parseIntegerToSql(entity.getProduct_visibility_setting_id()) +
                        ")"
        );
    }

    public boolean runningProductVisibilitySettingDetail(int id, int modifier_id) {
        return this.masterDB.update(
                "UPDATE product_visibility_setting_detail SET" +
                        " status = " + SettingStatus.RUNNING.getId() +
                        " AND modifier_id = " + modifier_id +
                        " AND modified_date = NOW()" +
                        " WHERE id = " + id +
                        " AND status != " + SettingStatus.RUNNING.getId()
        );
    }

    public boolean runningProductVisibilitySetting(Integer id, int modifier_id) {
        return this.masterDB.update(
                "UPDATE product_visibility_setting SET" +
                        " status = " + SettingStatus.RUNNING.getId() +
                        " AND modifier_id = " + modifier_id +
                        " AND modified_date = NOW()" +
                        " WHERE id = " + id +
                        " AND status != " + SettingStatus.RUNNING.getId()
        );
    }

    public List<JSONObject> getListProductVisibilitySettingDetailNotPending(
            Integer id) {
        return this.masterDB.find(
                "SELECT *" +
                        " FROM product_visibility_setting_detail" +
                        " WHERE product_visibility_setting_id = " + id +
                        " AND status != " + SettingStatus.PENDING.getId()
        );
    }

    public List<JSONObject> getListProductVisibilitySettingNeedStop(int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM product_visibility_setting" +
                        " WHERE status = " + SettingStatus.RUNNING.getId() +
                        " AND end_date is not null AND NOW() > end_date" +
                        " LIMIT " + scheduleRunningLimit
        );
    }

    public List<JSONObject> getListProductVisibilitySettingNeedStart(int scheduleRunningLimit) {
        return this.masterDB.find(
                "SELECT * FROM product_visibility_setting" +
                        " WHERE status = " + SettingStatus.ACTIVE.getId() +
                        " AND NOW() >= start_date" +
                        " LIMIT " + scheduleRunningLimit
        );
    }

    public ProductVisibilitySettingDetailEntity getProductVisibilitySettingDetailEntity(Integer id) {
        JSONObject rs = this.masterDB.getOne(
                "SELECT * FROM product_visibility_setting_detail WHERE id = " + id
        );

        if (rs != null) {
            return ProductVisibilitySettingDetailEntity.from(rs);
        }
        return null;
    }

    public JSONObject getProductVisibilitySettingDetail(Integer id) {
        return this.masterDB.getOne(
                "SELECT * FROM product_visibility_setting_detail WHERE id = " + id
        );
    }

    public JSONObject getProductVisibilitySettingByVisibility(
            String visibility_data_type,
            int visibility_data_id,
            String visibility_object_type,
            int visibility_object_id
    ) {
        return this.masterDB.getOne(
                "SELECT *" +
                        " FROM product_visibility_setting_detail t" +
                        " LEFT JOIN product_visibility_setting t1 ON t1.id = t.product_visibility_setting_id" +
                        " WHERE t.visibility_data_type = '" + visibility_data_type + "'" +
                        " AND t.visibility_data_id = " + visibility_data_id +
                        " AND t1.visibility_object_type = '" + visibility_object_type + "'" +
                        " AND t1.visibility_object_id = " + visibility_object_id +
                        " AND t1.status = " + SettingStatus.RUNNING.getId() + " AND t1.start_date <= NOW() AND (t1.end_date is NULL OR t1.end_date >= NOW())" +
                        " AND t.status = " + SettingStatus.RUNNING.getId() + " AND (t.start_date is NULL OR t.start_date <= NOW()) AND (t.end_date is NULL OR t.end_date >= NOW())" +
                        " LIMIT 1"
        );
    }

    public JSONObject checkProductVisibilitySettingByObject(int visibility_object_id, String visibility_object_type) {
        return this.masterDB.getOne(
                "SELECT * FROM product_visibility_setting WHERE " +
                        " visibility_object_id = " + visibility_object_id +
                        " AND visibility_object_type = '" + visibility_object_type + "'" +
                        " LIMIT 1"
        );
    }

    public JSONObject checkProductPriceSettingByObject(
            int object_id,
            String object_type) {
        return this.masterDB.getOne(
                "SELECT * FROM product_price_setting" +
                        " WHERE price_object_id = " + object_id +
                        " AND price_object_type = '" + object_type + "'" +
                        " LIMIT 1"
        );
    }

    public boolean updateStartDateProductVisibilitySetting(int id) {
        return this.masterDB.update(
                "UPDATE product_visibility_setting_detail" +
                        " SET start_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public JSONObject getProductPriceSetting(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM product_price_setting WHERE id = " + id
        );
    }

    public List<JSONObject> getListProductHot() {
        return this.masterDB.find(
                "SELECT * FROM product WHERE hot_priority != 0"
        );
    }

    public List<JSONObject> filterHotCommon(String product_hot_setting_ids, int limit) {
        return this.masterDB.find(
                "SELECT t.id," +
                        "t.full_name," +
                        "t.code," +
                        "t.images," +
                        "t.hot_label," +
                        "t.hot_date," +
                        "t.hot_modifier_id," +
                        "t1.total_sell_quantity" +
                        " FROM product as t" +
                        " JOIN product_hot_common t1 ON t1.product_id = t.id" +
                        " WHERE '" + product_hot_setting_ids + "' NOT LIKE CONCAT('%\"',t1.product_id,'\"%')" +
                        " ORDER BY t1.total_sell_turn DESC, t1.total_sell_quantity DESC" +
                        " LIMIT " + limit
        );
    }

    public boolean removeProductHot(int id, int modifier_id) {
        return this.masterDB.update(
                "UPDATE product SET hot_priority = 0," +
                        " hot_modifier_id = " + modifier_id + "," +
                        " hot_date = NOW()" +
                        " WHERE id = " + id
        );
    }

    public boolean setProductHot(int id, int hot_priority, String hot_label, int modifier_id) {
        return this.masterDB.update(
                "UPDATE product SET hot_priority = " + hot_priority +
                        ", hot_label = '" + hot_label + "'" +
                        ", hot_date = NOW()" +
                        ", hot_modifier_id = " + modifier_id +
                        " WHERE id = " + id
        );
    }

    public boolean updatePriorityForCategory(int id, int priority) {
        return this.masterDB.update(
                "UPDATE category SET priority = " + priority +
                        " WHERE id = " + id
        );
    }

    public boolean updateParentPriorityForCategory(int parent_id, int parent_priority) {
        return this.masterDB.update(
                "UPDATE category SET parent_priority = " + parent_priority +
                        " WHERE parent_id = " + parent_id
        );
    }

    public boolean updateProductPrice(Integer product_id, Long price) {
        return this.masterDB.update(
                "UPDATE product SET" +
                        " price = " + price + "" +
                        " WHERE id = " + product_id
        );
    }

    public int insertProductHotType(String name, String code, String image) {
        return this.masterDB.insert(
                "INSERT INTO product_hot_type(" +
                        "name," +
                        "code," +
                        "image" +
                        ") VALUES(" +
                        parseStringToSql(name) + "," +
                        parseStringToSql(code) + "," +
                        parseStringToSql(image) + "" +
                        ")"
        );
    }

    public boolean updateProductHotType(int id, String name, String code, String image) {
        return this.masterDB.update(
                "UPDATE product_hot_type SET " +
                        " name = " + parseStringToSql(name) + "," +
                        " code = " + parseStringToSql(code) + "," +
                        " image = " + parseStringToSql(image) +
                        " WHERE id = " + id

        );
    }

    public List<JSONObject> getProductByIds(
            String productIds) {
        return this.masterDB.find("SELECT t.*," +
                " t2.pltth_id," +
                " t3.plsp_id," +
                " t4.mathang_id" +
                " FROM product t" +
                " LEFT JOIN (SELECT id, sort_data FROM product_group) AS t1 on t.product_group_id = t1.id" +
                " LEFT JOIN (select id as pltth_id, parent_priority, priority, parent_id FROM category) as t2 ON t2.pltth_id = t.category_id" +
                " LEFT JOIN (select id as plsp_id, parent_priority, priority, parent_id FROM category) as t3 ON t3.plsp_id = t2.parent_id" +
                " LEFT JOIN (select id as mathang_id, parent_priority, priority, parent_id FROM category) as t4 ON t4.mathang_id = t3.parent_id" +
                " WHERE " +
                " '" + productIds + "' LIKE CONCAT('%\"',t.id,'\"%')" +
                " ORDER BY t4.priority ASC, t3.priority ASC, t2.priority ASC, t1.sort_data ASC, t.sort_data ASC"
        );
    }

    public int countProductByGroup(int product_group_id) {
        return this.masterDB.getTotal(
                "SELECT *" +
                        " FROM product" +
                        " WHERE product_group_id=" + product_group_id
        );
    }

    public int insertProductNew(
            int product_id,
            int priority,
            String created_date) {
        return this.masterDB.insert(
                "INSERT INTO product_new(" +
                        "product_id," +
                        "priority," +
                        "created_date)" +
                        " VALUES(" +
                        "" + product_id + "," +
                        "" + priority + "," +
                        "'" + created_date + "'" +
                        ")"
        );
    }

    public boolean clearProductNew() {
        return this.masterDB.update(
                "DELETE FROM product_new WHERE id > 0"
        );
    }

    public List<JSONObject> getListProductNew() {
        return this.masterDB.find(
                "SELECT * FROM product_new"

        );
    }

    public boolean updateParentPriorityOfCategory(int id, int parent_priority) {
        return this.masterDB.update(
                "UPDATE category SET parent_priority = " + parent_priority +
                        " WHERE id = " + id
        );
    }

    public int insertProductHistory(
            int product_id,
            String before_data,
            String after_data,
            int staff_id) {
        int id = 0;
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String query = "INSERT INTO product_history(" +
                    "product_id," +
                    "before_data," +
                    "after_data," +
                    "creator_id" +
                    ") VALUES (" +
                    product_id + "," +
                    "?" + "," +
                    "?" + "," +
                    staff_id +
                    ")";
            try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, before_data);
                stmt.setString(2, after_data);
                int row = stmt.executeUpdate();
                if (row > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            id = 0;
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
        return id;
    }

    public JSONObject getProductHistoryDetail(int id) {
        return this.masterDB.getOne(
                "SELECT * FROM product_history WHERE id = " + id
        );
    }

    public JSONObject getCatalogByCategory(int agency_id, int category_id, int status) {
        return this.masterDB.getOne(
                "SELECT t.* FROM agency_catalog_detail t" +
                        " LEFT JOIN catalog_category t1 ON t1.catalog_id=t.catalog_id" +
                        " WHERE t.agency_id = " + agency_id +
                        " AND t1.category_id = " + category_id +
                        " AND status = " + status +
                        " LIMIT 1"
        );
    }

    public JSONObject getProductGroupByCode(String code) {
        return this.masterDB.getOne("SELECT * FROM product_group WHERE code = '" + code + "' LIMIT 1");
    }

    public JSONObject getBusinessDepartment(int departmentId) {
        return this.masterDB.getOne("SELECT * FROM business_department WHERE id = " + departmentId);
    }
}