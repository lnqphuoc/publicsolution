package com.app.server.database.sql;

import com.app.server.data.entity.*;
import com.app.server.data.*;
import com.app.server.enums.ActiveStatus;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.net.URLEncoder;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ProductSQL {

    public String createCategory(CategoryEntity request) {
        return "INSERT INTO category(" +
                "name," +
                "parent_id," +
                "image," +
                "is_branch," +
                "category_level," +
                "priority," +
                "parent_priority," +
                "status" +
                ")" +
                " VALUES(" +
                "'" + request.getName() + "'," +
                "'" + request.getParent_id() + "'," +
                "'" + request.getImage() + "'," +
                "'" + request.getIs_branch() + "'," +
                "'" + request.getCategory_level() + "'," +
                "'" + request.getPriority() + "'," +
                "'" + request.getParent_priority() + "'," +
                "'" + request.getStatus() + "'" +
                ")";
    }

    public String editCategory(CategoryEntity entity) {
        return "UPDATE category SET " +
                "name = '" + entity.getName() + "', " +
                "image = '" + entity.getImage() + "', " +
                "parent_id = '" + entity.getParent_id() + "', " +
                "status= '" + entity.getStatus() + "'" +
                " WHERE id = " + entity.getId();
    }

    public String createBrand(BrandEntity entity) {
        return "INSERT INTO brand(" +
                "name," +
                "image," +
                "is_highlight," +
                "highlight_priority," +
                "status" +
                ")" +
                " VALUES(" +
                "'" + entity.getName() + "'," +
                "'" + entity.getImage() + "'," +
                "'" + entity.getIs_highlight() + "'," +
                "'" + entity.getHighlight_priority() + "'," +
                "'" + entity.getStatus() + "'" +
                ")";
    }

    public String editBrand(BrandEntity entity) {
        return "UPDATE brand SET " +
                "name = '" + entity.getName() + "', " +
                "image = '" + entity.getImage() + "', " +
                "is_highlight = '" + entity.getIs_highlight() + "', " +
                "highlight_priority = '" + entity.getHighlight_priority() + "', " +
                "status = '" + entity.getStatus() + "'" +
                " WHERE id = " + entity.getId();
    }

    public String getProductInfo(int id) {
        return "SELECT * FROM product WHERE id=" + id;
    }

    public String getProductGroupInfo(int product_group_id) {
        return "SELECT * FROM product_group WHERE id=" + product_group_id;
    }

    public String createProductGroup(ProductGroupEntity entity) {
        return "INSERT INTO product_group(" +
                "name," +
                "similar_name," +
                "code," +
                "category_id," +
                "status," +
                "sort_data," +
                "business_department_id" +
                ")" +
                " VALUES(" +
                "'" + entity.getName() + "'," +
                "'" + entity.getSimilar_name() + "'," +
                "'" + entity.getCode() + "'," +
                "'" + entity.getCategory_id() + "'," +
                "'" + entity.getStatus() + "'," +
                "'" + entity.getSort_data() + "'," +
                "'" + entity.getBusiness_department_id() + "'" +
                ")";
    }

    public String editProductGroup(ProductGroupEntity entity) {
        return "UPDATE product_group SET " +
                "name = '" + entity.getName() + "', " +
                "similar_name = '" + entity.getSimilar_name() + "', " +
                "code = '" + entity.getCode() + "', " +
                "category_id = '" + entity.getCategory_id() + "', " +
                "status = '" + entity.getStatus() + "'," +
                "sort_data = '" + entity.getSort_data() + "'" +
                " WHERE id = " + entity.getId();
    }

    public String getBrandInfo(int id) {
        return "SELECT * FROM brand WHERE id=" + id;
    }

    public String deleteBrand(BrandEntity brandEntity) {
        return "UPDATE brand SET status=" + ActiveStatus.DELETE.getValue() + "" +
                " WHERE id=" + brandEntity.getId();
    }

    public String getCategoryInfo(int id) {
        return "SELECT * FROM category WHERE id=" + id;
    }

    public String deleteCategory(CategoryEntity categoryEntity) {
        return "UPDATE category SET status=" + ActiveStatus.DELETE.getValue() + "" +
                " WHERE id=" + categoryEntity.getId();
    }

    public String createProduct(ProductEntity productEntity) {
        return "INSERT INTO product(" +
                "code," +
                "short_name," +
                "full_name," +
                "warranty_time," +
                "images," +
                "specification," +
                "product_color_id," +
                "characteristic," +
                "description," +
                "user_manual," +
                "technical_data," +
                "status," +
                "price," +
                "product_small_unit_id," +
                "product_big_unit_id," +
                "convert_small_unit_ratio," +
                "minimum_purchase," +
                "step," +
                "product_group_id," +
                "warehouse_quantity," +
                "item_type," +
                "sort_data," +
                "brand_id," +
                "category_id," +
                "other_name," +
                "app_active," +
                "hot_label," +
                "business_department_id" +
                ")" +
                " VALUES(" +
                "'" + productEntity.getCode() + "'," + //"code," +
                "?," + //"short_name," +
                "?," + //"full_name," +
                "'" + productEntity.getWarranty_time() + "'," + //"warranty_time," +
                "'" + productEntity.getImages() + "'," + //"images," +
                "'" + productEntity.getSpecification() + "'," + //"specification," +
                "" + productEntity.getProduct_color_id() + "," + //"product_color_id," +
                "'" + productEntity.getCharacteristic() + "'," + //"characteristic," +
                "?," + //"description," +
                "'" + productEntity.getUser_manual() + "'," + //"user_manual," +
                "?," + //"technical_data," +
                "'" + productEntity.getStatus() + "'," + //"status,
                "'" + productEntity.getPrice() + "'," + // price," +
                "'" + productEntity.getProduct_small_unit_id() + "'," + //"product_small_unit_id," +
                "" + productEntity.getProduct_big_unit_id() + "," + //"product_big_unit_id," +
                "'" + productEntity.getConvert_small_unit_ratio() + "'," + //"convert_small_unit_ratio," +
                "'" + productEntity.getMinimum_purchase() + "'," + //"minimum_purchase,
                "'" + productEntity.getStep() + "'," +// step," +
                "'" + productEntity.getProduct_group_id() + "'," + //"product_group_id," +
                "'" + productEntity.getWarehouse_quantity() + "'," + //"warehouse_quantity," +
                "'" + productEntity.getItem_type() + "'," + //"item_type," +
                "'" + productEntity.getSort_data() + "'," + //"sort_data" +
                "" + productEntity.getBrand_id() + "," + //"brand_id" +
                "" + productEntity.getCategory_id() + "," + //"brand_id" +
                "'" + productEntity.getOther_name() + "'," + //"other_name" +
                "" + productEntity.getApp_active() + "," + //"other_name" +
                "'" + productEntity.getHot_label() + "'," + //"hot_label" +
                "'" + productEntity.getBusiness_department_id() + "'" +
                ")";
    }

    public String getProductByCode(String code) {
        return "SELECT * FROM product " +
                "WHERE code='" + code + "' " +
                "LIMIT 1";
    }

    public String addProductColor(ProductColorEntity productColorEntity) {
        return "INSERT INTO product_color(" +
                "name)" +
                " VALUES(" +
                "'" + productColorEntity.getName() + "'" +
                ")";
    }

    public String editProductTechnicalData(int id, String data) {
        return "UPDATE product SET " +
                "technical_data = '" + data + "' " +
                "WHERE id=" + id;
    }

    public String editProductUserManual(int id, String data) {
        return "UPDATE product SET " +
                "user_manual = '" + data + "' " +
                "WHERE id=" + id;
    }

    public String editProductDescription(int id, String data) {
        return "UPDATE product SET " +
                "description = '" + data + "' " +
                "WHERE id=" + id;
    }

    public String editProductWarrantyTime(int id, String data) {
        return "UPDATE product SET " +
                "warranty_time = '" + data + "' " +
                "WHERE id=" + id;
    }

    public String getProductColorByName(String name) {
        return "SELECT * FROM product_color WHERE name ='" + name + "' LIMIT 1";
    }

    public String editProductInfo(ProductEntity entity) {
        return "UPDATE product SET " +
                " product_group_id = '" + entity.getProduct_group_id() + "' " +
                ", full_name = '" + entity.getFull_name() + "' " +
                ", short_name = '" + entity.getShort_name() + "' " +
                ", status = '" + entity.getStatus() + "' " +
                ", specification = '" + entity.getSpecification() + "' " +
                ", product_color_id = " + entity.getProduct_color_id() + " " +
                ", characteristic = '" + entity.getCharacteristic() + "' " +
                ", product_small_unit_id = '" + entity.getProduct_small_unit_id() + "' " +
                ", product_big_unit_id = " + entity.getProduct_big_unit_id() + " " +
                ", convert_small_unit_ratio = '" + entity.getConvert_small_unit_ratio() + "' " +
                ", minimum_purchase = '" + entity.getMinimum_purchase() + "' " +
                ", step = '" + entity.getStep() + "' " +
                ", images = '" + entity.getImages() + "' " +
                ", warranty_time = '" + entity.getWarranty_time() + "' " +
                ", description = '" + entity.getDescription() + "' " +
                ", user_manual = '" + entity.getUser_manual() + "' " +
                ", technical_data = '" + entity.getTechnical_data() + "', " +
                ", other_name = '" + entity.getOther_name() + "' " +
                " WHERE id=" + entity.getId();
    }

    public String updateCategoryPriority() {
        return "UPDATE category SET priority =? WHERE id=?";
    }

    public String increaseProductNumberInProductGroup(int id) {
        return "UPDATE product_group SET product_number=product_number+1 WHERE id=" + id;
    }

    public String decreaseProductNumberInProductGroup(int id) {
        return "UPDATE product_group SET product_number=product_number-1 WHERE id=" + id;
    }

    public String getListProductByProductGroup(int id) {
        return "SELECT * FROM product WHERE product_group_id=" + id;
    }

    public String unHighlightBrand() {
        return "UPDATE brand SET is_highlight = 0, highlight_priority=0 WHERE is_highlight=1";
    }

    public String highlightBrand() {
        return "UPDATE brand SET is_highlight = 1, highlight_priority=? WHERE id = ?";
    }

    public String updateCategoryForProductByProductGroup(int product_group_id, int category_id) {
        return "UPDATE product SET category_id = " + category_id +
                " WHERE product_group_id = " + product_group_id;
    }

    public String loadAllCategory() {
        return "SELECT * FROM category ORDER BY category_level ASC, priority ASC, id ASC";
    }

    public String updateCategoryParentPriority() {
        return "UPDATE category SET parent_priority=? WHERE parent_id=?";
    }
}