package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.product.*;
import com.app.server.data.dto.staff.Staff;
import com.app.server.data.entity.*;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.*;
import com.app.server.data.request.product.*;
import com.app.server.data.response.product.ProductInfoResponse;
import com.app.server.data.response.product.ProductMoneyResponse;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.app.server.utils.SortUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ProductService extends BaseService {
    public ClientResponse filterCategory(SessionData sessionData, FilterListRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }
            JSONObject data = new JSONObject();
            List<Category> records = new ArrayList<>();
            records.addAll(this.dataManager.getProductManager().getLtNganhHang().stream().filter(
                    x -> x.getBusiness_department_id() == staff.getDepartment_id()
            ).collect(Collectors.toList()));
            records.addAll(this.dataManager.getProductManager().getLtMatHang().stream().filter(
                    x -> x.getBusiness_department_id() == staff.getDepartment_id()
            ).collect(Collectors.toList()));
            records.addAll(this.dataManager.getProductManager().getLtPhanLoai().stream().filter(
                    x -> x.getBusiness_department_id() == staff.getDepartment_id()
            ).collect(Collectors.toList()));
            records.addAll(this.dataManager.getProductManager().getLtPhanLoaiTheoThuongHieu().stream().filter(
                    x -> x.getBusiness_department_id() == staff.getDepartment_id()
            ).collect(Collectors.toList()));
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProduct(SessionData sessionData, FilterListRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("business_department_id");
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(staff.getDepartment_id()));
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQueryV2(FunctionList.LIST_PRODUCT, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.filterProduct(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.PRODUCT.getImageUrl());
            }
            int total = this.productDB.getTotalProduct(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProductGroup(SessionData sessionData, FilterListRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("business_department_id");
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(staff.getDepartment_id()));
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_GROUP, request.getFilters(), request.getSorts());

            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());

            for (JSONObject js : records) {
                js.put("image_url", ImagePath.PRODUCT.getImageUrl());
                js.put("product_number", this.productDB.countProductByGroup(ConvertUtils.toInt(js.get("id"))));
                js.put("pltth_name", this.dataManager.getProductManager().getMpCategory().get(ConvertUtils.toInt(js.get("pltth_id"))).getName());
            }
            int total = this.productDB.getTotalProductGroup(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createCategory(CreateCategoryRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * validate parent nếu có
             */
            int parent_priority = 0;
            CategoryEntity categoryParentEntity = null;
            if (request.getParent_id() != 0) {
                categoryParentEntity = this.productDB.getCategoryInfo(request.getParent_id());
                if (categoryParentEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CATEGORY_PARENT_INVALID);
                }

                parent_priority = categoryParentEntity.getPriority();
            }

            CategoryLevel categoryLevel = CategoryLevel.from(request.getCategory_level());


            /**
             * create entity
             */
            CategoryEntity categoryEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(request), CategoryEntity.class);
            categoryEntity.setIs_branch(CategoryLevel.isBranch(request.getCategory_level()));
            categoryEntity.setPriority(
                    categoryParentEntity == null ?
                            this.dataManager.getProductManager().getCategoryPriority(categoryEntity) :
                            (this.dataManager.getProductManager().getLastPriorityByParent(categoryParentEntity) + 1)
            );
            categoryEntity.setParent_priority(parent_priority);
            int rsCreateCategory = this.productDB.createCategory(categoryEntity);
            if (rsCreateCategory > 0) {
                /**
                 * reload memory cache
                 */
                categoryEntity.setId(rsCreateCategory);
                this.dataManager.reloadCategory(rsCreateCategory);

                JSONObject data = new JSONObject();
                data.put("record", categoryEntity);
                return ClientResponse.success(data);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("PRODUCT", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editCategory(EditCategoryRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            CategoryEntity categoryEntity = this.productDB.getCategoryInfo(request.getId());
            if (categoryEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.BRAND_NOT_FOUND);
            }

            /**
             * validate parent nếu có
             */
            if (request.getParent_id() != 0) {
                CategoryEntity categoryParentEntity = this.productDB.getCategoryInfo(request.getParent_id());
                if (categoryParentEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CATEGORY_PARENT_INVALID);
                }

                this.productDB.updateParentPriorityOfCategory(
                        request.getId(),
                        categoryParentEntity.getPriority()
                );
            }

            /**
             * edit entity
             */
            categoryEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(request), CategoryEntity.class);
            boolean rsEditCategory = this.productDB.editCategory(categoryEntity);
            if (rsEditCategory) {
                /**
                 * reload memory cache
                 */
                this.dataManager.reloadCategory(request.getId());
                return ClientResponse.success(categoryEntity);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("PRODUCT", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deleteCategory(BasicRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            CategoryEntity categoryEntity = this.productDB.getCategoryInfo(request.getId());
            if (categoryEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.BRAND_NOT_FOUND);
            }

            /**
             * Kiểm tra trước khi xóa
             */
            boolean rsCheckCanDeleteCategory = this.checkCanDeleteCategory(categoryEntity);
            if (!rsCheckCanDeleteCategory) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CATEGORY_USING);
            }

            boolean rsDeleteCategory = this.productDB.deleteCategory(categoryEntity);
            if (rsDeleteCategory) {
                /**
                 * reload memory cache
                 */
                this.dataManager.reloadCategory(request.getId());

                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("PRODUCT", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createProductGroup(SessionData sessionData, CreateProductGroupRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }

            int category_id = this.dataManager.getProductManager().getPLTTHDefault(staff.getDepartment_id());
            request.setCategory_id(category_id);
            JSONObject oldCode = this.productDB.getProductGroupByCode(request.getCode());
            if (oldCode != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_NOT_AVAILABLE);
            }

            /**
             * create entity
             */
            ProductGroupEntity productGroupEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(request), ProductGroupEntity.class);
            productGroupEntity.setSort_data(SortUtils.convertProductCodeToProductSortData(request.getCode()));
            productGroupEntity.setBusiness_department_id(staff.getDepartment_id());
            int rsCreateProductGroup = this.productDB.createProductGroup(productGroupEntity);
            if (rsCreateProductGroup > 0) {
                productGroupEntity.setId(rsCreateProductGroup);

                /**
                 * reload cache
                 */
                this.dataManager.reloadProductGroup(CacheType.PRODUCT_GROUP, rsCreateProductGroup);

                return ClientResponse.success(productGroupEntity);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editProductGroup(EditProductGroupRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            ProductGroupEntity productGroupEntity = this.productDB.getProductGroupInfo(request.getId());
            if (productGroupEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_GROUP_NOT_FOUND);
            }

            int oldCategoryId = productGroupEntity.getCategory_id();

            /**
             * create entity
             */
            productGroupEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(request), ProductGroupEntity.class);
            productGroupEntity.setSort_data(SortUtils.convertProductCodeToProductSortData(request.getCode()));
            boolean rsCreateProductGroup = this.productDB.editProductGroup(productGroupEntity);
            if (rsCreateProductGroup) {
                /**
                 * Nếu nhóm đổi danh mục thì sku cũng đổi theo
                 */
                if (oldCategoryId != productGroupEntity.getCategory_id()) {
                    boolean rsuUpdateCategoryForProductByProductGroup = this.productDB.updateCategoryForProductByProductGroup(request.getId(), productGroupEntity.getCategory_id());
                }

                /**
                 * reload cache
                 */
                this.dataManager.reloadProductGroup(CacheType.PRODUCT_GROUP, request.getId());

                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("PRODUCT", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createProduct(SessionData sessionData, CreateProductRequest request) {
        try {
            /**
             * Validate request
             */
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }
            /**
             * Validate rule
             * - thông số kỹ thuat sai format
             * - hinh anh sai format
             * - mã sản phẩm chưa tồn tại
             */
            if (this.appUtils.convertStringToArrayObject(request.getTechnical_data()) == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TECHNICAL_DATA_INVALID);
            }
            if (this.appUtils.convertStringToArray(request.getImages()) == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.IMAGE_INVALID);
            }
            if (this.productDB.getProductByCode(request.getCode()) != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_NOT_AVAILABLE);
            }

            ProductGroupEntity productGroupEntity = this.productDB.getProductGroupInfo(request.getProduct_group_id());
            if (productGroupEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_GROUP_NOT_FOUND);
            }

            if (request.getHot_label() != null &&
                    !request.getHot_label().isEmpty()) {
                JSONObject productHotType = this.dataManager.getConfigManager()
                        .getProductHotTypeByCode(request.getHot_label());
                if (productHotType == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_HOT_TYPE_INVALID);
                }
            }


            /**
             * Insert sản phẩm
             * - lưu thông tin bản
             * - tính toán sort_data
             */
            ProductEntity productEntity = this.getProductCreate(request);
            productEntity.setBusiness_department_id(staff.getDepartment_id());
            productEntity.setSort_data(SortUtils.convertProductCodeToProductSortData(request.getCode()));
            productEntity.setCategory_id(productGroupEntity.getCategory_id());
            int rsCreateProduct = this.productDB.createProduct(productEntity);
            if (rsCreateProduct > 0) {
                /**
                 * Cập nhật số lượng phiên bản cho nhóm
                 * - tăng số lương phiên bản của nhóm
                 */
                this.productDB.increaseProductNumberInProductGroup(productGroupEntity.getId());

                /**
                 * Update cache
                 */
                this.dataManager.reloadProduct(CacheType.PRODUCT, rsCreateProduct);

                productEntity.setId(rsCreateProduct);


                /**
                 * Khởi tạo tồn kho cho sản phẩm
                 */
                this.initWarehouseInfo(productEntity);

                return ClientResponse.success(productEntity);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void initWarehouseInfo(ProductEntity productEntity) {
        try {
            WarehouseInfoEntity warehouseInfoEntity = new WarehouseInfoEntity();
            warehouseInfoEntity.setProduct_id(productEntity.getId());
            warehouseInfoEntity.setQuantity_start_today(this.dataManager.getConfigManager().getProductQuantityStart());
            this.warehouseDB.insertWarehouseInfo(warehouseInfoEntity);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
    }

    private ProductEntity getProductCreate(CreateProductRequest request) {
        ProductEntity productEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(request), ProductEntity.class);
        if (productEntity.getProduct_big_unit_id() == null) {
            productEntity.setConvert_small_unit_ratio(0);
        }

        if (productEntity.getHot_label() == null) {
            productEntity.setHot_label("");
        }

        return productEntity;
    }

    public ClientResponse editProduct(SessionData sessionData, EditProductRequest request) {
        try {
            /**
             * Validate request
             */
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            ProductEntity productEntity = this.productDB.getProduct(request.getId());
            if (productEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(request.getId());
            if (productCache == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            JSONObject before_data = this.convertProductData(
                    productEntity,
                    productCache
            );

            if (request.getPrice() != null && request.getPrice() != productEntity.getPrice()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_PRICE_INVALID);
            }

            if ((request.getHot_label() == null || request.getHot_label().isEmpty()) &&
                    productEntity.getHot_priority() != 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_HOT_TYPE_INVALID);
            }

            ProductGroupEntity productGroupEntity = this.productDB.getProductGroupInfo(request.getProduct_group_id());
            if (productGroupEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_GROUP_NOT_FOUND);
            }

            /**
             * Validate rule
             * - thông số kỹ thuat sai format
             */
            if (StringUtils.isNotEmpty(request.getTechnical_data()) && this.appUtils.convertStringToArrayObject(request.getTechnical_data()) == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TECHNICAL_DATA_INVALID);
            }
            if (StringUtils.isNotEmpty(request.getImages()) && this.appUtils.convertStringToArray(request.getImages()) == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.IMAGE_INVALID);
            }
            if (!StringUtils.isNotEmpty(request.getCode()) && this.productDB.getProductByCode(request.getCode()) != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_NOT_AVAILABLE);
            }

            if (request.getHot_label() != null &&
                    !request.getHot_label().isEmpty()) {
                JSONObject productHotType = this.dataManager.getConfigManager()
                        .getProductHotTypeByCode(request.getHot_label());
                if (productHotType == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_HOT_TYPE_INVALID);
                }
            }

            boolean hasEditProductGroup = true;
            int old_product_group_id = productEntity.getProduct_group_id();

            /**
             * tạo data cập nhật sản phẩm
             */
            String old_code = productEntity.getCode();
            productEntity = this.getProductUpdate(productEntity, request);
            if (!old_code.equals(productEntity.getCode())) {
                productEntity.setSort_data(
                        SortUtils.convertProductCodeToProductSortData(productEntity.getCode())
                );
            }
            if (request.getProduct_group_id() != old_product_group_id) {
                hasEditProductGroup = true;
                productEntity.setCategory_id(productGroupEntity.getCategory_id());
            } else {
                hasEditProductGroup = false;
            }

            boolean rsEdit = this.productDB.editProductInfo(productEntity);
            if (rsEdit) {
                if (hasEditProductGroup) {
                    /**
                     * Cập nhật số lượng phiên bản
                     * giảm số lượng nhóm củ
                     * tăng sô lượng nhóm mới
                     */
                    this.productDB.increaseProductNumberInProductGroup(request.getProduct_group_id());
                    this.productDB.decreaseProductNumberInProductGroup(old_product_group_id);
                }
                /**
                 * Update cache
                 */
                this.dataManager.reloadProduct(CacheType.PRODUCT, request.getId());

                JSONObject after_data =
                        this.convertProductData(
                                productEntity,
                                this.dataManager.getProductManager().getProductBasicData(request.getId())
                        );

                this.saveProductHistory(
                        request.getId(),
                        JsonUtils.Serialize(before_data),
                        JsonUtils.Serialize(after_data),
                        sessionData.getId());

                return ClientResponse.success(productEntity);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ProductEntity getProductUpdate(ProductEntity productEntity, EditProductRequest request) {
        if (StringUtils.isNotBlank(request.getCode())) {
            productEntity.setCode(request.getCode());
        }
        if (StringUtils.isNotBlank(request.getFull_name())) {
            productEntity.setFull_name(request.getFull_name());
        }
        if (StringUtils.isNotBlank(request.getShort_name())) {
            productEntity.setShort_name(request.getShort_name());
        }
        if (StringUtils.isNotBlank(request.getWarranty_time())) {
            productEntity.setWarranty_time(request.getWarranty_time());
        }
        if (StringUtils.isNotBlank(request.getImages())) {
            productEntity.setImages(request.getImages());
        }
        if (StringUtils.isNotBlank(request.getOther_name())) {
            productEntity.setOther_name(request.getOther_name());
        }

        productEntity.setSpecification(request.getSpecification());
        productEntity.setProduct_color_id(request.getProduct_color_id());
        productEntity.setCharacteristic(request.getCharacteristic());

        if (StringUtils.isNotBlank(request.getDescription())) {
            productEntity.setDescription(request.getDescription());
        }
        if (StringUtils.isNotBlank(request.getUser_manual())) {
            productEntity.setUser_manual(request.getUser_manual());
        }
        if (StringUtils.isNotBlank(request.getTechnical_data())) {
            productEntity.setTechnical_data(request.getTechnical_data());
        }
        if (request.getStatus() != null) {
            productEntity.setStatus(request.getStatus());
        }
        if (request.getPrice() != null) {
            productEntity.setPrice(request.getPrice());
        }
        if (request.getProduct_small_unit_id() != 0) {
            productEntity.setProduct_small_unit_id(request.getProduct_small_unit_id());
        }
        productEntity.setProduct_big_unit_id(request.getProduct_big_unit_id());
        if (request.getProduct_big_unit_id() == null) {
            productEntity.setConvert_small_unit_ratio(0);
        } else if (request.getConvert_small_unit_ratio() != null) {
            productEntity.setConvert_small_unit_ratio(request.getConvert_small_unit_ratio());
        }
        if (request.getMinimum_purchase() != null) {
            productEntity.setMinimum_purchase(request.getMinimum_purchase());
        }
        if (request.getStep() != null) {
            productEntity.setStep(request.getStep());
        }
        if (request.getProduct_group_id() != null) {
            productEntity.setProduct_group_id(request.getProduct_group_id());
        }
        if (request.getItem_type() != null) {
            productEntity.setItem_type(request.getItem_type());
        }
        if (request.getBrand_id() != null) {
            productEntity.setBrand_id(request.getBrand_id());
        }
        if (request.getApp_active() != null) {
            productEntity.setApp_active(request.getApp_active());
        }

        productEntity.setHot_label(
                request.getHot_label() == null ? "" : request.getHot_label()
        );
        return productEntity;
    }

    private ProductEntity getProductUpdateByImport(ProductEntity productEntity, ImportUpdateProductRequest request) {
        if (request.getCode() != null) {
            productEntity.setCode(request.getCode());
        }
        if (request.getFull_name() != null) {
            productEntity.setFull_name(request.getFull_name());
        }
        if (request.getShort_name() != null) {
            productEntity.setShort_name(request.getShort_name());
        }
        if (request.getWarranty_time() != null) {
            productEntity.setWarranty_time(request.getWarranty_time());
        }
        if (request.getImages() != null) {
            productEntity.setImages(request.getImages());
        }
        if (request.getOther_name() != null) {
            productEntity.setOther_name(request.getOther_name());
        }
        if (request.getSpecification() != null) {
            productEntity.setSpecification(request.getSpecification());
        }
        if (request.getProduct_color_id() != null) {
            productEntity.setProduct_color_id(
                    request.getProduct_color_id() == -1 ? null :
                            request.getProduct_color_id());
        }
        if (request.getCharacteristic() != null) {
            productEntity.setCharacteristic(request.getCharacteristic());
        }
        if (request.getDescription() != null) {
            productEntity.setDescription(request.getDescription());
        }
        if (request.getUser_manual() != null) {
            productEntity.setUser_manual(request.getUser_manual());
        }
        if (request.getTechnical_data() != null) {
            productEntity.setTechnical_data(request.getTechnical_data());
        }
        if (request.getStatus() != null) {
            productEntity.setStatus(request.getStatus());
        }
        if (request.getPrice() != null) {
            productEntity.setPrice(request.getPrice());
        }
        if (request.getProduct_small_unit_id() != null) {
            productEntity.setProduct_small_unit_id(request.getProduct_small_unit_id());
        }
        if (request.getProduct_big_unit_id() != null) {
            productEntity.setProduct_big_unit_id(
                    request.getProduct_big_unit_id() == -1 ? null :
                            request.getProduct_big_unit_id()
            );
        }
        if (request.getConvert_small_unit_ratio() != null) {
            productEntity.setConvert_small_unit_ratio(request.getConvert_small_unit_ratio());
        }
        if (request.getMinimum_purchase() != null) {
            productEntity.setMinimum_purchase(request.getMinimum_purchase());
        }
        if (request.getStep() != null) {
            productEntity.setStep(request.getStep());
        }
        if (request.getProduct_group_id() != null) {
            productEntity.setProduct_group_id(request.getProduct_group_id());
        }
        if (request.getItem_type() != null) {
            productEntity.setItem_type(request.getItem_type());
        }
        if (request.getBrand_id() != null) {
            productEntity.setBrand_id(request.getBrand_id());
        }
        if (request.getApp_active() != null) {
            productEntity.setApp_active(request.getApp_active());
        }

        if (productEntity.getHot_label() != null) {
            productEntity.setHot_label(
                    request.getHot_label() == null ? "" : request.getHot_label()
            );
        }
        return productEntity;
    }

    public ClientResponse filterBrand(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_BRAND, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.BRAND.getImageUrl());
            }
            int total = this.productDB.getTotalProductGroup(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createBrand(CreateBrandRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Nếu nổi bật thì add vào cuối
             */
            BrandEntity lastHighlightBrand = this.dataManager.getProductManager().getLastHighlightBrand();
            if (lastHighlightBrand != null) {
                request.setHighlight_priority(lastHighlightBrand.getHighlight_priority() + 1);
            } else {
                request.setHighlight_priority(1);
            }

            /**
             * create entity
             */
            BrandEntity brandEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(request), BrandEntity.class);
            int rsCreateBrand = this.productDB.createBrand(brandEntity);
            if (rsCreateBrand > 0) {
                brandEntity.setId(rsCreateBrand);

                /**
                 * reload cache
                 */
                this.dataManager.reloadBrand(rsCreateBrand);

                return ClientResponse.success(brandEntity);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("PRODUCT", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editBrand(EditBrandRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            BrandEntity brandEntity = this.productDB.getBrandInfo(request.getId());
            if (brandEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.BRAND_NOT_FOUND);
            }

            /**
             * update entity
             */
            brandEntity.setName(request.getName());
            brandEntity.setImage(request.getImage());
            brandEntity.setHighlight_priority(request.getHighlight_priority());
            brandEntity.setIs_highlight(request.getIs_highlight());
            brandEntity.setStatus(request.getStatus());
            boolean rsEditBrand = this.productDB.editBrand(brandEntity);
            if (rsEditBrand) {
                /**
                 * reload cache
                 */
                this.dataManager.reloadBrand(request.getId());

                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("PRODUCT", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deleteBrand(BasicRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            BrandEntity brandEntity = this.productDB.getBrandInfo(request.getId());
            if (brandEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.BRAND_NOT_FOUND);
            }

            /**
             * Kiểm tra trước khi xóa thương hiệu
             */
            boolean rsCheckCanDeleteBrand = this.checkCanDeleteBrand(brandEntity);
            if (!rsCheckCanDeleteBrand) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.BRAND_USING);
            }

            boolean rsEditBrand = this.productDB.deleteBrand(brandEntity);
            if (rsEditBrand) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("PRODUCT", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkCanDeleteBrand(BrandEntity brandEntity) {
        return true;
    }

    public ClientResponse getProductInfo(BasicRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            ProductEntity productEntity = this.productDB.getProductInfo(request.getId());
            if (productEntity != null) {
                JSONObject data = new JSONObject();
                ProductGroupEntity jsProductGroup = this.productDB.getProductGroupInfo(productEntity.getProduct_group_id());
                if (jsProductGroup != null) {
                    data.put("product_group", jsProductGroup);

                    /**
                     * Thong tin danh muc
                     */
                    Category plhtth = this.dataManager.getProductManager().getCategoryById(jsProductGroup.getCategory_id());
                    if (plhtth != null && plhtth.getCategory_level() == CategoryLevel.PHAN_LOAI_HANG_THEO_THUONG_HIEU.getKey()) {
                        Category plh = this.dataManager.getProductManager().getCategoryById(plhtth.getParent_id());
                        Category mh = this.dataManager.getProductManager().getCategoryById(plh.getParent_id());
                        Category nh = this.dataManager.getProductManager().getCategoryById(mh.getParent_id());

                        JSONObject category = new JSONObject();
                        category.put("phan_loai_hang_theo_thuong_hieu", plhtth);
                        category.put("phan_loai_hang", plh);
                        category.put("mat_hang", mh);
                        category.put("nganh_hang", nh);

                        data.put("category", category);
                    }
                }

                if (productEntity.getHot_label() != null && productEntity.getHot_label().isEmpty()) {
                    productEntity.setHot_label(null);
                }
                data.put("product", productEntity);
                return ClientResponse.success(data);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }


    private boolean checkCanDeleteCategory(CategoryEntity categoryEntity) {
        return true;
    }

    public ClientResponse addProductColor(AddProductColorRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Kiem tra trung
             */
            if (productDB.checkProductColorExist(request.getName())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.COLOR_EXIST);
            }

            ProductColorEntity productColorEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(request), ProductColorEntity.class);
            int rsAddProductColor = this.productDB.addProductColor(productColorEntity);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editProductInfo(EditProductInfoRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }


            ProductEntity productEntity = this.productDB.getProductInfo(request.getId());
            if (productEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean hasEditProductGroup = false;
            int old_product_group_id = productEntity.getProduct_group_id();
            /**
             *
             */
            if (request.getProduct_group_id() != old_product_group_id) {
                hasEditProductGroup = true;
            }

            productEntity = JsonUtils.DeSerialize(JsonUtils.Serialize(request), ProductEntity.class);
            boolean rsEdit = this.productDB.editProductInfo(productEntity);
            if (rsEdit) {
                if (hasEditProductGroup) {
                    /**
                     * Cập nhật số lượng phiên bản
                     * giảm số lượng nhóm củ
                     * tăng sô lượng nhóm mới
                     */
                    this.productDB.increaseProductNumberInProductGroup(request.getProduct_group_id());
                    this.productDB.decreaseProductNumberInProductGroup(request.getProduct_group_id());
                }
                return ClientResponse.success(productEntity);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editProductTechnicalData(EditDataRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
            if (this.appUtils.convertStringToArray(request.getData()) == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TECHNICAL_DATA_INVALID);
            }

            boolean rsEditProductTechnicalData = this.productDB.editProductTechnicalData(request.getId(), request.getData());
            if (rsEditProductTechnicalData) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editProductUserManual(EditDataRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
            if (this.appUtils.convertStringToArray(request.getData()) == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rsEditProductTechnicalData = this.productDB.editProductUserManual(request.getId(), request.getData());
            if (rsEditProductTechnicalData) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editProductDescription(EditDataRequest request) {
        ClientResponse clientResponse = request.validate();
        if (clientResponse.failed()) {
            return clientResponse;
        }
        if (this.appUtils.convertStringToArray(request.getData()) == null) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }

        boolean rsEditProductTechnicalData = this.productDB.editProductDescription(request.getId(), request.getData());
        if (rsEditProductTechnicalData) {
            return ClientResponse.success(null);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editProductWarrantyTime(EditDataRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            boolean rsEditProductTechnicalData = this.productDB.editProductWarrantyTime(request.getId(), request.getData());
            if (rsEditProductTechnicalData) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse sortCategory(SortRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            Category category = this.dataManager.getProductManager().getCategoryById(request.getId());
            if (category == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CATEGORY_INVALID);
            }

            List<Category> ltCategory = this.dataManager.getProductManager().getLtCategoryByLevel(category.getCategory_level());
            int currentPosition = ltCategory.indexOf(category);
            if (request.getPriority() >= ltCategory.size() + 1
                    || request.getPriority() == currentPosition) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRIORITY_INVALID);
            }

            /**
             * Sort phân loại theo thương hiệu
             */
            int priority = request.getPriority() - 1;
            List<Category> rsSortCategory = this.dataManager.getProductManager().reArrange(
                    ltCategory,
                    currentPosition,
                    priority,
                    category);

            /**
             * Cập nhật database
             */
            boolean rsUpdateSortCategory = this.productDB.updateSortCategory(rsSortCategory);
            if (rsUpdateSortCategory) {
                /**
                 * Cập nhật parent_priority sắp xếp của thứ mục con
                 */
                boolean rsUpdateSortParentCategory = this.productDB.updateSortParentCategory(rsSortCategory);

                /**
                 * reload cache
                 */
                this.dataManager.reloadCategory(0);
                return ClientResponse.success(ltCategory);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse sortCategoryChild(SortBrandRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * Chỉ sắp xếp danh mục thuộc cùng danh mục cha
             */
            int parent_id = -1;
            for (int iCategory = 0; iCategory < request.getIds().size(); iCategory++) {
                int category_id = request.getIds().get(iCategory);
                Category category = this.dataManager.getProductManager().getMpCategory().get(category_id);
                if (category == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CATEGORY_INVALID);
                }

                if (parent_id != -1 && category.getParent_id() != parent_id) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SORT_CATEGORY_IN_PARENT);
                }

                if (parent_id == -1) {
                    parent_id = category.getParent_id();
                }
            }

            for (int iCategory = 0; iCategory < request.getIds().size(); iCategory++) {
                int id = request.getIds().get(iCategory);
                Category category = this.dataManager.getProductManager().getCategoryById(id);
                if (category == null) {
                    continue;
                }
                boolean rsCategory = this.productDB.updatePriorityForCategory(
                        id,
                        iCategory + 1
                );

                boolean rsChild = this.productDB.updateParentPriorityForCategory(
                        id,
                        iCategory + 1
                );
            }
            /**
             * reload cache
             */
            this.dataManager.reloadCategory(0);
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getProductGroupInfo(BasicRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            ProductGroupEntity jsProductGroup = this.productDB.getProductGroupInfo(request.getId());
            if (jsProductGroup == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_GROUP_NOT_FOUND);
            }

            JSONObject data = new JSONObject();
            data.put("product_group", jsProductGroup);


            List<ProductInfoResponse> productInfoResponses = new ArrayList<>();
            List<JSONObject> productEntities = this.productDB.getListProductByProductGroup(request.getId());
            for (JSONObject pro : productEntities) {
                ProductInfoResponse productInfoResponse = JsonUtils.DeSerialize(JsonUtils.Serialize(pro), ProductInfoResponse.class);
                productInfoResponse.setImages(
                        productInfoResponse.getImages() == null ? "[]" : productInfoResponse.getImages());
                productInfoResponse.setImage_url(ImagePath.PRODUCT.getImageUrl());
                productInfoResponses.add(productInfoResponse);
            }

            data.put("products", productInfoResponses);
            /**
             * Thong tin danh muc
             */
            Category plhtth = this.dataManager.getProductManager().getCategoryById(jsProductGroup.getCategory_id());
            if (plhtth != null && plhtth.getCategory_level() == CategoryLevel.PHAN_LOAI_HANG_THEO_THUONG_HIEU.getKey()) {
                Category plh = this.dataManager.getProductManager().getCategoryById(plhtth.getParent_id());
                Category mh = this.dataManager.getProductManager().getCategoryById(plh.getParent_id());
                Category nh = this.dataManager.getProductManager().getCategoryById(mh.getParent_id());
                JSONObject category = new JSONObject();
                category.put("phan_loai_hang_theo_thuong_hieu", plhtth);
                category.put("phan_loai_hang", plh);
                category.put("mat_hang", mh);
                category.put("nganh_hang", nh);

                data.put("category", category);
            }


            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse sortBrand(SortBrandRequest request) {
        try {
            /**
             * Khi sắp xếp thương hiệu nỗi bật, clien sẽ truyền lên dánh sách mới nhất
             * 1. Bỏ nổi bật thương hiệu cũ
             * 2. Cập nhật nổi bật và thứ tự sắp mới theo dữ liệu mới
             */
            boolean rsUnHighlightBrand = this.productDB.unHighlightBrand();

            /**
             * Cập nật nổi bật cho thương hiệu mới
             */
            boolean rsHighlightBrand = this.productDB.highlightBrand(request.getIds());
            if (rsHighlightBrand) {
                /**
                 * reload cache brand
                 */
                this.dataManager.reloadBrand(0);
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ProductMoneyResponse getProductPrice(int agency_id, ProductEntity productEntity) {
        AgencyEntity agencyEntity = this.agencyDB.getAgencyEntity(agency_id);
        return new ProductMoneyResponse();
    }

    public ClientResponse getProductPriceByAgency(GetProductPriceByAgencyRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            ProductEntity productEntity = this.productDB.getProduct(request.getProduct_id());
            if (productEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            ProductMoneyResponse productMoneyResponse = this.getProductPrice(
                    request.getAgency_id(), productEntity);

            JSONObject data = new JSONObject();
            data.put("product_money", productMoneyResponse);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProductVisibilitySetting(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_VISIBILITY_SETTING, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());

            long now = DateTimeUtils.getMilisecondsNow();
            for (JSONObject js : records) {
                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js
                                .get("creator_id"))
                ));
                int agency_id = ConvertUtils.toInt(js
                        .get("agency_id"));
                if (agency_id > 0) {
                    js.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                            ConvertUtils.toInt(js
                                    .get("agency_id"))
                    ));
                }

                if (ConvertUtils.toInt(js.get("status")) == SettingStatus.RUNNING.getId()) {
                    ProductVisibilitySettingDetailEntity productVisibilitySettingDetailEntity =
                            ProductVisibilitySettingDetailEntity.from(
                                    js
                            );
                    if (productVisibilitySettingDetailEntity.getEnd_date() != null &&
                            productVisibilitySettingDetailEntity.getEnd_date().getTime() <= now) {
                        js.put("status", SettingStatus.PENDING.getId());
                        productVisibilitySettingDetailEntity.setStatus(
                                SettingStatus.PENDING.getId()
                        );
                        this.productDB.updateProductVisibilitySettingDetail(
                                productVisibilitySettingDetailEntity
                        );
                    }
                }
            }
            int total = this.productDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProductVisibilitySettingDetail(FilterListByIdRequest request) {
        try {

            JSONObject setting = this.productDB.getProductVisibilitySetting(request.getId());
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            int visibility_object_id = ConvertUtils.toInt(setting.get("visibility_object_id"));
            String visibility_object_type = ConvertUtils.toString(setting.get("visibility_object_type"));

            JSONObject data = new JSONObject();
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setType("select");
            filterRequest.setKey("product_visibility_setting_id");
            filterRequest.setValue(ConvertUtils.toString(request.getId()));
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_VISIBILITY_SETTING_DETAIL, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(js
                                .get("product_id"))
                ));

                VisibilityDataType visibilityDataType = VisibilityDataType.from(ConvertUtils.toString(js
                        .get("visibility_data_type")));
                VisibilityType currentVisibility = VisibilityType.HIDE;
                switch (visibilityDataType) {
                    case PRODUCT: {
                        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                                ConvertUtils.toInt(js.get("product_id"))
                        );

                        if (productCache != null && productCache.getStatus() == 1) {
                            currentVisibility = VisibilityType.SHOW;
                        } else {
                            currentVisibility = VisibilityType.HIDE;
                        }
                        break;
                    }
                    case PRODUCT_GROUP: {
                        ProductGroup group = this.dataManager.getProductManager().getMpProductGroup().get(
                                ConvertUtils.toInt(js
                                        .get("visibility_data_id"))
                        );

                        if (group != null && group.getStatus() == 1) {
                            currentVisibility = VisibilityType.SHOW;
                        } else {
                            currentVisibility = VisibilityType.HIDE;
                        }
                        break;
                    }
                    case BRAND_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                ConvertUtils.toInt(js.get("category_level_4_id"))
                        );

                        if (category != null && category.getStatus() == 1) {
                            currentVisibility = VisibilityType.SHOW;
                        } else {
                            currentVisibility = VisibilityType.HIDE;
                        }
                        break;
                    }
                    case PRODUCT_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                ConvertUtils.toInt(js.get("category_level_3_id"))
                        );

                        if (category != null && category.getStatus() == 1) {
                            currentVisibility = VisibilityType.SHOW;
                        } else {
                            currentVisibility = VisibilityType.HIDE;
                        }
                        break;
                    }
                    case ITEM_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                ConvertUtils.toInt(js.get("category_level_2_id"))
                        );

                        if (category != null && category.getStatus() == 1) {
                            currentVisibility = VisibilityType.SHOW;
                        } else {
                            currentVisibility = VisibilityType.HIDE;
                        }
                        break;
                    }
                    case BRAND: {
                        BrandEntity brand = this.dataManager.getProductManager().getMpBrand().get(
                                ConvertUtils.toInt(js
                                        .get("visibility_data_id"))
                        );

                        if (brand != null && brand.getStatus() == 1) {
                            currentVisibility = VisibilityType.SHOW;
                        } else {
                            currentVisibility = VisibilityType.HIDE;
                        }
                        break;
                    }
                }
                js.put("current_visibility", currentVisibility.getId());

                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js
                                .get("creator_id"))
                ));
            }

            int total = this.productDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterAllProductVisibilitySettingDetail(FilterListRequest request) {
        try {
            JSONObject data = new JSONObject();
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_VISIBILITY_SETTING_DETAIL, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(js
                                .get("product_id"))
                ));

                VisibilityDataType visibilityDataType = VisibilityDataType.from(ConvertUtils.toString(js
                        .get("visibility_data_type")));
                VisibilityType currentVisibility = VisibilityType.HIDE;
                switch (visibilityDataType) {
                    case PRODUCT: {
                        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(
                                ConvertUtils.toInt(js.get("product_id"))
                        );

                        if (productCache != null && productCache.getStatus() == 1) {
                            currentVisibility = VisibilityType.SHOW;
                        } else {
                            currentVisibility = VisibilityType.HIDE;
                        }
                        break;
                    }
                    case PRODUCT_GROUP: {
                        ProductGroup group = this.dataManager.getProductManager().getMpProductGroup().get(
                                ConvertUtils.toInt(js.get("product_id"))
                        );

                        if (group != null && group.getStatus() == 1) {
                            currentVisibility = VisibilityType.SHOW;
                        } else {
                            currentVisibility = VisibilityType.HIDE;
                        }
                        break;
                    }
                    case BRAND_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                ConvertUtils.toInt(js.get("category_level_4_id"))
                        );

                        if (category != null && category.getStatus() == 1) {
                            currentVisibility = VisibilityType.SHOW;
                        } else {
                            currentVisibility = VisibilityType.HIDE;
                        }
                        break;
                    }
                    case PRODUCT_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                ConvertUtils.toInt(js.get("category_level_3_id"))
                        );

                        if (category != null && category.getStatus() == 1) {
                            currentVisibility = VisibilityType.SHOW;
                        } else {
                            currentVisibility = VisibilityType.HIDE;
                        }
                        break;
                    }
                    case ITEM_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                ConvertUtils.toInt(js.get("category_level_2_id"))
                        );

                        if (category != null && category.getStatus() == 1) {
                            currentVisibility = VisibilityType.SHOW;
                        } else {
                            currentVisibility = VisibilityType.HIDE;
                        }
                        break;
                    }
                }
                js.put("current_visibility", currentVisibility.getId());
            }

            int total = this.productDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getProductVisibilitySettingDetail(BasicRequest request) {
        try {
            JSONObject setting = this.productDB.getProductVisibilitySetting(request.getId());
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            int agency_id = ConvertUtils.toInt(setting
                    .get("agency_id"));
            if (agency_id > 0) {
                setting.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(setting
                                .get("agency_id"))
                ));
                setting.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(setting
                                .get("creator_id"))
                ));

            }

            JSONObject data = new JSONObject();
            data.put("product_visibility_setting", setting);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }


    public ClientResponse createProductVisibilitySetting(SessionData sessionData, CreateProductVisibilitySettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject pvs = this.productDB.checkProductVisibilitySettingByObject(
                    request.getVisibility_object_id(),
                    request.getVisibility_object_type()
            );
            if (pvs != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_DUPLICATE);
            }

            ProductVisibilitySettingEntity productVisibilitySettingEntity
                    = new ProductVisibilitySettingEntity();
            productVisibilitySettingEntity.setName(request.getName());
            productVisibilitySettingEntity.setVisibility_object_type(request.getVisibility_object_type());
            productVisibilitySettingEntity.setVisibility_object_id(request.getVisibility_object_id());
            productVisibilitySettingEntity.setCreator_id(sessionData.getId());
            productVisibilitySettingEntity.setCreated_date(DateTimeUtils.getNow());
            productVisibilitySettingEntity.setStart_date(DateTimeUtils.getDateTime(request.getStart_date()));
            productVisibilitySettingEntity.setEnd_date(request.getEnd_date() == null ? null : DateTimeUtils.getDateTime(request.getEnd_date()));
            productVisibilitySettingEntity.setVisibility_object_type(request.getVisibility_object_type());
            productVisibilitySettingEntity.setVisibility_object_id(request.getVisibility_object_id());
            SettingObjectType visibilityObjectType = SettingObjectType.from(request.getVisibility_object_type());
            switch (visibilityObjectType) {
                case AGENCY:
                    productVisibilitySettingEntity.setAgency_id(request.getVisibility_object_id());
                    break;
                case CITY:
                    productVisibilitySettingEntity.setCity_id(request.getVisibility_object_id());
                    break;
                case REGION:
                    productVisibilitySettingEntity.setRegion_id(request.getVisibility_object_id());
                    break;
                case MEMBERSHIP:
                    productVisibilitySettingEntity.setMembership_id(request.getVisibility_object_id());
                    break;
            }
            productVisibilitySettingEntity.setStatus(SettingStatus.DRAFT.getId());

            int rsInsertSetting = this.productDB.insertProductVisibilitySetting(productVisibilitySettingEntity);
            if (rsInsertSetting <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            productVisibilitySettingEntity.setId(rsInsertSetting);

            for (ProductVisibilitySettingDetailRequest productVisibilitySettingDetailRequest : request.getRecords()) {
                ProductVisibilitySettingDetailEntity productVisibilitySettingDetailEntity = new ProductVisibilitySettingDetailEntity();
                productVisibilitySettingDetailEntity.setProduct_visibility_setting_id(productVisibilitySettingEntity.getId());
                if (productVisibilitySettingDetailRequest.getStart_date() == null || productVisibilitySettingDetailRequest.getStart_date() == 0) {
                    productVisibilitySettingDetailEntity.setStart_date(
                            productVisibilitySettingEntity.getStart_date());
                } else {
                    productVisibilitySettingDetailEntity.setStart_date(
                            DateTimeUtils.getDateTime(productVisibilitySettingDetailRequest.getStart_date()
                            ));
                }

                if (request.getEnd_date() != null) {
                    productVisibilitySettingDetailEntity.setEnd_date(
                            DateTimeUtils.getDateTime(request.getEnd_date())
                    );
                }

                VisibilityDataType visibilityDataType = VisibilityDataType.from(
                        productVisibilitySettingDetailRequest.getVisibility_data_type()
                );
                if (visibilityDataType == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                switch (visibilityDataType) {
                    case PRODUCT: {
                        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (productCache == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setProduct_id(productCache.getId());
                        break;
                    }
                    case PRODUCT_GROUP: {
                        ProductGroup productGroup = this.dataManager.getProductManager().getMpProductGroup().get(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (productGroup == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setProduct_group_id(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        break;
                    }
                    case BRAND_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (category == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setCategory_level_4_id(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        break;
                    }
                    case PRODUCT_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (category == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setCategory_level_3_id(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        break;
                    }
                    case ITEM_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (category == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setCategory_level_2_id(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        break;
                    }
                    case BRAND: {
                        BrandEntity brand = this.dataManager.getProductManager().getMpBrand().get(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (brand == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setBrand_id(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        break;
                    }
                    default: {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }

                productVisibilitySettingDetailEntity.setCreator_id(sessionData.getId());
                productVisibilitySettingDetailEntity.setVisibility(
                        productVisibilitySettingDetailRequest.getVisibility()
                );
                productVisibilitySettingDetailEntity.setStatus(
                        productVisibilitySettingDetailRequest.getStatus()
                );
                productVisibilitySettingDetailEntity.setCreated_date(DateTimeUtils.getNow());
                productVisibilitySettingDetailEntity.setVisibility_data_type(
                        productVisibilitySettingDetailRequest.getVisibility_data_type()
                );
                productVisibilitySettingDetailEntity.setVisibility_data_id(
                        productVisibilitySettingDetailRequest.getVisibility_data_id()
                );

                JSONObject jsSettingDetail
                        = JsonUtils.DeSerialize(
                        JsonUtils.Serialize(this.convertProductVisibilitySettingDetail(productVisibilitySettingDetailEntity)),
                        JSONObject.class);

                int rsInsertSettingDetail =
                        this.productDB.insertProductVisibilitySettingDetail(productVisibilitySettingDetailEntity);
                if (rsInsertSettingDetail <= 0) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
                productVisibilitySettingDetailEntity.setId(rsInsertSettingDetail);

                /**
                 * Lưu lịch sử
                 */
                this.saveProductVisibilitySettingDetailHistory(sessionData,
                        jsSettingDetail,
                        productVisibilitySettingEntity.getStatus(),
                        productVisibilitySettingDetailEntity.getStart_date(),
                        productVisibilitySettingDetailEntity.getEnd_date(),
                        productVisibilitySettingDetailEntity.getProduct_visibility_setting_id());
            }

            JSONObject data = new JSONObject();
            data.put("id", productVisibilitySettingEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ProductVisibilitySettingDetailEntity convertProductVisibilitySettingDetail(
            ProductVisibilitySettingDetailEntity productVisibilitySettingDetailEntity) {
        ProductVisibilitySettingDetailEntity result = new ProductVisibilitySettingDetailEntity();
//        private Integer id;
        result.setId(
                productVisibilitySettingDetailEntity.getId());
//        private int creator_id;
        result.setCreator_id(
                productVisibilitySettingDetailEntity.getCreator_id());
//        private Date created_date;
        result.setCreated_date(
                productVisibilitySettingDetailEntity.getCreated_date());
//        private Integer modifier_id;
        result.setModifier_id(
                productVisibilitySettingDetailEntity.getModifier_id());
//        private Date modified_date;
        result.setModified_date(
                productVisibilitySettingDetailEntity.getModified_date());
//        private Integer product_id;
        result.setProduct_id(
                productVisibilitySettingDetailEntity.getProduct_id()
        );
//        private Integer product_group_id;
        result.setProduct_group_id(
                productVisibilitySettingDetailEntity.getProduct_group_id()
        );
//        private Integer category_level_1_id;
        result.setCategory_level_1_id(
                productVisibilitySettingDetailEntity.getCategory_level_1_id()
        );
//        private Integer category_level_2_id;
        result.setCategory_level_2_id(
                productVisibilitySettingDetailEntity.getCategory_level_2_id()
        );
//        private Integer category_level_3_id;
        result.setCategory_level_3_id(
                productVisibilitySettingDetailEntity.getCategory_level_3_id()
        );
//        private Integer category_level_4_id;
        result.setCategory_level_4_id(
                productVisibilitySettingDetailEntity.getCategory_level_4_id()
        );
//        private Integer brand_id;
        result.setBrand_id(
                productVisibilitySettingDetailEntity.getBrand_id()
        );
//        private int status = ProductVisibilitySettingStatus.DRAFT.getId();
        result.setStatus(
                productVisibilitySettingDetailEntity.getStatus()
        );
//        private Date start_date;
        result.setStart_date(
                productVisibilitySettingDetailEntity.getStart_date()
        );
//        private Date end_date;
        result.setEnd_date(
                productVisibilitySettingDetailEntity.getEnd_date()
        );
//        private int visibility;
        result.setVisibility(
                productVisibilitySettingDetailEntity.getVisibility()
        );
//        private String visibility_data_type;
        result.setVisibility_data_type(
                productVisibilitySettingDetailEntity.getVisibility_data_type()
        );
//        private int visibility_data_id;
        result.setVisibility_data_id(
                productVisibilitySettingDetailEntity.getVisibility_data_id()
        );
//        private int product_visibility_setting_id;
        result.setProduct_visibility_setting_id(
                productVisibilitySettingDetailEntity.getProduct_visibility_setting_id()
        );
        return result;

    }

    public ClientResponse activeProductVisibilitySetting(SessionData sessionData, ActiveProductVisibilitySettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
            String message = "";

            for (int iSetting = 0; iSetting < request.getIds().size(); iSetting++) {
                Integer id = request.getIds().get(iSetting);
                if (id == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ClientResponse crActiveSetting = this.activeProductVisibilitySettingOne(sessionData, id);
                if (crActiveSetting.failed()) {
                    clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    message += "\n [Thiết lập thứ " + (iSetting + 1) + "] " + crActiveSetting.getMessage();
                }
            }

            if (clientResponse.failed()) {
                clientResponse.setMessage(message);
                return clientResponse;
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deactivateProductVisibilitySetting(SessionData sessionData, ActiveProductVisibilitySettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
            String message = "";

            for (int iSetting = 0; iSetting < request.getIds().size(); iSetting++) {
                Integer id = request.getIds().get(iSetting);
                if (id == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ClientResponse crActiveSetting = this.deactivateProductVisibilitySettingOne(sessionData, id);
                if (crActiveSetting.failed()) {
                    clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    message += "\n [Thiết lập thứ " + (iSetting + 1) + "] " + crActiveSetting.getMessage();
                }
            }

            if (clientResponse.failed()) {
                clientResponse.setMessage(message);
                return clientResponse;
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse activeProductVisibilitySettingOne(SessionData sessionData, Integer id) {
        try {
            ProductVisibilitySettingEntity setting = this.productDB.getProductVisibilitySettingEntity(id);
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }

            int status = setting.getStatus();

            if (!(SettingStatus.DRAFT.getId() == status ||
                    SettingStatus.ACTIVE.getId() == status ||
                    SettingStatus.PENDING.getId() == status)) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            if ((SettingStatus.DRAFT.getId() == status ||
                    SettingStatus.ACTIVE.getId() == status)
                    && setting.getEnd_date() != null
                    && setting.getEnd_date().before(DateTimeUtils.getNow())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TIME_END_INVALID);
            }

            if (setting.getStart_date().getTime() > DateTimeUtils.getMilisecondsNow()) {
                /**
                 * Kích hoạt và chờ chạy
                 */
                if (SettingStatus.PENDING.getId() == status) {
                    setting.setEnd_date(null);
                }
                setting.setModified_date(DateTimeUtils.getNow());
                setting.setModifier_id(sessionData.getId());
                setting.setStatus(SettingStatus.ACTIVE.getId());
                boolean rsUpdateSetting = this.productDB.updateProductVisibilitySetting(setting);
                if (!rsUpdateSetting) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                List<JSONObject> settingDetailList = this.productDB.getListProductVisibilitySettingDetailNotPending(id);
                for (JSONObject settingDetail : settingDetailList) {
                    /**
                     * Lưu lịch sử
                     */
                    this.saveProductVisibilitySettingDetailHistory(sessionData,
                            settingDetail,
                            setting.getStatus(),
                            setting.getStart_date(),
                            setting.getEnd_date(),
                            setting.getId()
                    );
                }
            } else {
                /**
                 * Run thiết lập
                 */
                if (SettingStatus.PENDING.getId() == status) {
                    setting.setEnd_date(null);
                }
                setting.setStart_date(DateTimeUtils.getNow());
                setting.setModified_date(DateTimeUtils.getNow());
                setting.setModifier_id(sessionData.getId());
                setting.setStatus(SettingStatus.RUNNING.getId());
                boolean rsUpdateSetting = this.productDB.updateProductVisibilitySetting(setting);
                if (!rsUpdateSetting) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                List<JSONObject> settingDetailList = this.productDB.getListProductVisibilitySettingDetailNotPending(id);
                for (JSONObject settingDetail : settingDetailList) {
                    ProductVisibilitySettingDetailEntity productVisibilitySettingDetailEntity =
                            ProductVisibilitySettingDetailEntity.from(settingDetail);
                    if (productVisibilitySettingDetailEntity == null) {
                        continue;
                    }
                    if (productVisibilitySettingDetailEntity.getStart_date() != null &&
                            productVisibilitySettingDetailEntity.getEnd_date() != null &&
                            productVisibilitySettingDetailEntity.getEnd_date().getTime() <= DateTimeUtils.getMilisecondsNow()) {
                        productVisibilitySettingDetailEntity.setStatus(SettingStatus.PENDING.getId());
                        this.productDB.updateProductVisibilitySettingDetail(productVisibilitySettingDetailEntity);

                        /**
                         * Lưu lịch sử
                         */
                        this.saveProductVisibilitySettingDetailHistory(
                                sessionData,
                                settingDetail,
                                productVisibilitySettingDetailEntity.getStatus(),
                                productVisibilitySettingDetailEntity.getStart_date(),
                                productVisibilitySettingDetailEntity.getEnd_date(),
                                setting.getId());
                    } else {
                        this.productDB.updateStartDateProductVisibilitySetting(
                                ConvertUtils.toInt(settingDetail.get("id"))
                        );

                        /**
                         * Lưu lịch sử
                         */
                        this.saveProductVisibilitySettingDetailHistory(
                                sessionData,
                                settingDetail,
                                setting.getStatus(),
                                setting.getStart_date(),
                                setting.getEnd_date(),
                                setting.getId());
                    }
                }
            }

            this.dataManager.callReloadProductVisibilitySetting(id);
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse deactivateProductVisibilitySettingOne(SessionData sessionData, Integer id) {
        try {
            ProductVisibilitySettingEntity setting = this.productDB.getProductVisibilitySettingEntity(id);
            if (setting == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }
            int status = setting.getStatus();
            if (!(SettingStatus.RUNNING.getId() == status
                    || SettingStatus.ACTIVE.getId() == status)) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            setting.setEnd_date(DateTimeUtils.getNow());
            setting.setModified_date(DateTimeUtils.getNow());
            setting.setModifier_id(sessionData.getId());
            setting.setStatus(SettingStatus.PENDING.getId());
            boolean rsUpdateSetting = this.productDB.updateProductVisibilitySetting(setting);
            if (!rsUpdateSetting) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            List<JSONObject> settingDetailList = this.productDB.getListProductVisibilitySettingDetailNotPending(id);
            for (JSONObject settingDetail : settingDetailList) {
                /**
                 * Lưu lịch sử
                 */
                this.saveProductVisibilitySettingDetailHistory(
                        sessionData,
                        settingDetail,
                        SettingStatus.PENDING.getId(),
                        setting.getStart_date(),
                        setting.getEnd_date(),
                        setting.getId());
            }

            this.dataManager.callReloadProductVisibilitySetting(id);

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private void saveProductVisibilitySettingDetailHistory(
            SessionData sessionData,
            JSONObject settingDetail,
            int status,
            Date start_date,
            Date end_date,
            int setting_id) {
        try {
            ProductVisibilitySettingDetailHistoryEntity productVisibilitySettingDetailHistoryEntity =
                    this.createProductVisibilitySettingDetailHistory(sessionData, settingDetail);
            if (productVisibilitySettingDetailHistoryEntity == null) {
                /**
                 * Thong bao qua tele
                 */
                return;
            }

            productVisibilitySettingDetailHistoryEntity.setId(null);
            productVisibilitySettingDetailHistoryEntity.setProduct_visibility_setting_id(setting_id);
            productVisibilitySettingDetailHistoryEntity.setStart_date(start_date);
            productVisibilitySettingDetailHistoryEntity.setEnd_date(end_date);
            productVisibilitySettingDetailHistoryEntity.setStatus(status);
            productVisibilitySettingDetailHistoryEntity.setCreator_id(sessionData.getId());
            int rs = this.productDB.insertProductVisibilitySettingDetailHistory(productVisibilitySettingDetailHistoryEntity);
            if (rs <= 0) {

            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private ProductVisibilitySettingDetailHistoryEntity createProductVisibilitySettingDetailHistory(
            SessionData sessionData,
            JSONObject settingDetail
    ) {
        try {
            ProductVisibilitySettingDetailHistoryEntity productVisibilitySettingDetailHistoryEntity
                    = new ProductVisibilitySettingDetailHistoryEntity();
            productVisibilitySettingDetailHistoryEntity.setVisibility_data_type(
                    ConvertUtils.toString(settingDetail.get("visibility_data_type")));
            productVisibilitySettingDetailHistoryEntity.setVisibility_data_id(
                    ConvertUtils.toInt(settingDetail.get("visibility_data_id")));
            productVisibilitySettingDetailHistoryEntity.setVisibility(
                    ConvertUtils.toInt(settingDetail.get("visibility"))
            );
            productVisibilitySettingDetailHistoryEntity.setCreator_id(sessionData.getId());
            productVisibilitySettingDetailHistoryEntity.setCreated_date(DateTimeUtils.getNow());
            return productVisibilitySettingDetailHistoryEntity;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    public ClientResponse filterProductVisibilitySettingDetailHistory(FilterListProductInvisibilityBySettingHistoryRequest request) {
        try {
            JSONObject data = new JSONObject();
            FilterRequest filterRequest1 = new FilterRequest();
            filterRequest1.setType("select");
            filterRequest1.setKey("product_visibility_setting_id");
            filterRequest1.setValue(ConvertUtils.toString(request.getId()));
            request.getFilters().add(filterRequest1);
            FilterRequest filterRequest2 = new FilterRequest();
            filterRequest2.setType("select");
            filterRequest2.setKey("visibility_data_type");
            filterRequest2.setValue(ConvertUtils.toString(request.getVisibility_data_type()));
            request.getFilters().add(filterRequest2);
            FilterRequest filterRequest3 = new FilterRequest();
            filterRequest3.setType("select");
            filterRequest3.setKey("visibility_data_id");
            filterRequest3.setValue(ConvertUtils.toString(request.getVisibility_data_id()));
            request.getFilters().add(filterRequest3);
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_VISIBILITY_SETTING_DETAIL_HISTORY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 0);
            for (JSONObject js : records) {
                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js
                                .get("creator_id"))
                ));
            }

            int total = this.productDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runStopProductVisibilitySettingSchedule() {
        try {
            /**
             * Stop
             */
            List<JSONObject> settings = this.productDB.getListProductVisibilitySettingNeedStop(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject setting : settings) {
                int id = ConvertUtils.toInt(setting.get("id"));
                ClientResponse clientResponse = this.deactivateProductVisibilitySettingOne(this.dataManager.getStaffManager().getSessionStaffBot(), id);
                if (clientResponse.failed()) {
                    /**
                     * Thông báo tele
                     */
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse runStartProductVisibilitySettingSchedule() {
        try {
            /**
             * start
             */
            List<JSONObject> settings = this.productDB.getListProductVisibilitySettingNeedStart(ConfigInfo.SCHEDULE_RUNNING_LIMIT);
            for (JSONObject setting : settings) {
                int id = ConvertUtils.toInt(setting.get("id"));
                ClientResponse clientResponse = this.activeProductVisibilitySettingOne(
                        this.dataManager.getStaffManager().getSessionStaffBot(),
                        id);
                if (clientResponse.failed()) {
                    this.alertToTelegram("[setting: " + id + "]" + clientResponse.getMessage(),
                            ResponseStatus.FAIL);
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse activeProductVisibilitySettingDetail(SessionData sessionData, ActiveProductVisibilitySettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
            String message = "";

            for (int iSetting = 0; iSetting < request.getIds().size(); iSetting++) {
                Integer id = request.getIds().get(iSetting);
                if (id == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ClientResponse crActiveSetting = this.activeProductVisibilitySettingDetailOne(sessionData, id);
                if (crActiveSetting.failed()) {
                    clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    message += "\n [Thiết lập thứ " + (iSetting + 1) + "] " + crActiveSetting.getMessage();
                }

                return crActiveSetting;
            }

            if (clientResponse.failed()) {
                clientResponse.setMessage(message);
                return clientResponse;
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse activeProductVisibilitySettingDetailOne(SessionData sessionData, Integer id) {
        try {
            ProductVisibilitySettingDetailEntity settingDetailEntity = this.productDB.getProductVisibilitySettingDetailEntity(id);
            if (settingDetailEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }

            int status = settingDetailEntity.getStatus();
            if (!(SettingStatus.PENDING.getId() == status)) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            if (settingDetailEntity.getStart_date().getTime() > DateTimeUtils.getMilisecondsNow()) {
                /**
                 * Kích hoạt và chờ chạy
                 */
                if (SettingStatus.PENDING.getId() == status) {
                    settingDetailEntity.setEnd_date(null);
                }

                settingDetailEntity.setModified_date(DateTimeUtils.getNow());
                settingDetailEntity.setModifier_id(sessionData.getId());
                settingDetailEntity.setStatus(SettingStatus.ACTIVE.getId());
                boolean rsUpdateDetail = this.productDB.updateProductVisibilitySettingDetail(settingDetailEntity);
                if (!rsUpdateDetail) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * Lưu lịch sử
                 */
                this.saveProductVisibilitySettingDetailHistory(
                        sessionData,
                        JsonUtils.DeSerialize(
                                JsonUtils.Serialize(settingDetailEntity), JSONObject.class),
                        SettingStatus.ACTIVE.getId(),
                        settingDetailEntity.getStart_date(),
                        settingDetailEntity.getEnd_date(),
                        settingDetailEntity.getProduct_visibility_setting_id());
            } else {
                /**
                 * Run thiết lập
                 */
                if (SettingStatus.PENDING.getId() == status) {
                    settingDetailEntity.setEnd_date(null);
                }
                settingDetailEntity.setStart_date(DateTimeUtils.getNow());
                settingDetailEntity.setModified_date(DateTimeUtils.getNow());
                settingDetailEntity.setModifier_id(sessionData.getId());
                settingDetailEntity.setStatus(SettingStatus.RUNNING.getId());
                boolean rsUpdateDetail = this.productDB.updateProductVisibilitySettingDetail(settingDetailEntity);
                if (!rsUpdateDetail) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                this.saveProductVisibilitySettingDetailHistory(
                        sessionData,
                        JsonUtils.DeSerialize(
                                JsonUtils.Serialize(settingDetailEntity), JSONObject.class),
                        SettingStatus.RUNNING.getId(),
                        settingDetailEntity.getStart_date(),
                        settingDetailEntity.getEnd_date(),
                        settingDetailEntity.getProduct_visibility_setting_id());
            }

            this.dataManager.callReloadProductVisibilitySetting(
                    settingDetailEntity.getProduct_visibility_setting_id());
            JSONObject data = new JSONObject();
            data.put("record", settingDetailEntity);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deactivateProductVisibilitySettingDetail(SessionData sessionData, ActiveProductVisibilitySettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }
            String message = "";

            for (int iSetting = 0; iSetting < request.getIds().size(); iSetting++) {
                Integer id = request.getIds().get(iSetting);
                if (id == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                ClientResponse crActiveSetting = this.deactivateProductVisibilitySettingDetailOne(sessionData, id);
                if (crActiveSetting.failed()) {
                    clientResponse = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    message += "\n [Thiết lập thứ " + (iSetting + 1) + "] " + crActiveSetting.getMessage();
                }

                return crActiveSetting;
            }

            if (clientResponse.failed()) {
                clientResponse.setMessage(message);
                return clientResponse;
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse deactivateProductVisibilitySettingDetailOne(SessionData sessionData, Integer id) {
        try {
            ProductVisibilitySettingDetailEntity settingDetailEntity = this.productDB.getProductVisibilitySettingDetailEntity(id);
            if (settingDetailEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_NOT_FOUND);
            }
            int status = settingDetailEntity.getStatus();
            if (!(SettingStatus.RUNNING.getId() == status
                    || SettingStatus.ACTIVE.getId() == status)) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            settingDetailEntity.setEnd_date(DateTimeUtils.getNow());
            settingDetailEntity.setModified_date(DateTimeUtils.getNow());
            settingDetailEntity.setModifier_id(sessionData.getId());
            settingDetailEntity.setStatus(SettingStatus.PENDING.getId());
            boolean rsUpdateDetail = this.productDB.updateProductVisibilitySettingDetail(settingDetailEntity);
            if (!rsUpdateDetail) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            JSONObject jsSettingDetail = this.productDB.getProductVisibilitySettingDetail(id);
            if (jsSettingDetail == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            /**
             * Lưu lịch sử
             */
            this.saveProductVisibilitySettingDetailHistory(
                    sessionData,
                    jsSettingDetail,
                    SettingStatus.PENDING.getId(),
                    settingDetailEntity.getStart_date(),
                    settingDetailEntity.getEnd_date(),
                    settingDetailEntity.getProduct_visibility_setting_id());

            this.dataManager.callReloadProductVisibilitySetting(
                    settingDetailEntity.getProduct_visibility_setting_id());
            JSONObject data = new JSONObject();
            data.put("record", settingDetailEntity);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editProductVisibilitySetting(SessionData sessionData, EditProductVisibilitySettingRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            JSONObject pvs = this.productDB.checkProductVisibilitySettingByObject(
                    request.getVisibility_object_id(),
                    request.getVisibility_object_type()
            );
            if (pvs != null && ConvertUtils.toInt(pvs.get("id")) != request.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_DUPLICATE);
            }

            ProductVisibilitySettingEntity productVisibilitySettingEntity
                    = this.productDB.getProductVisibilitySettingEntity(request.getId());
            if (productVisibilitySettingEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            productVisibilitySettingEntity.setName(request.getName());
            productVisibilitySettingEntity.setVisibility_object_type(request.getVisibility_object_type());
            productVisibilitySettingEntity.setVisibility_object_id(request.getVisibility_object_id());
            productVisibilitySettingEntity.setStart_date(DateTimeUtils.getDateTime(request.getStart_date()));
            productVisibilitySettingEntity.setEnd_date(request.getEnd_date() == null ? null : DateTimeUtils.getDateTime(request.getEnd_date()));
            productVisibilitySettingEntity.setVisibility_object_type(request.getVisibility_object_type());
            productVisibilitySettingEntity.setVisibility_object_id(request.getVisibility_object_id());
            SettingObjectType visibilityObjectType = SettingObjectType.from(request.getVisibility_object_type());
            switch (visibilityObjectType) {
                case AGENCY:
                    productVisibilitySettingEntity.setAgency_id(request.getVisibility_object_id());
                    break;
                case CITY:
                    productVisibilitySettingEntity.setCity_id(request.getVisibility_object_id());
                    break;
                case REGION:
                    productVisibilitySettingEntity.setRegion_id(request.getVisibility_object_id());
                    break;
                case MEMBERSHIP:
                    productVisibilitySettingEntity.setMembership_id(request.getVisibility_object_id());
                    break;
            }

            /**
             *
             */
            if (productVisibilitySettingEntity.getStatus() == SettingStatus.ACTIVE.getId()) {
                if (productVisibilitySettingEntity.getStart_date().before(DateTimeUtils.getNow())) {
                    productVisibilitySettingEntity.setStart_date(DateTimeUtils.getNow());
                    productVisibilitySettingEntity.setStatus(SettingStatus.RUNNING.getId());
                }
            }

            boolean rsUpdateSetting = this.productDB.updateProductVisibilitySetting(productVisibilitySettingEntity);
            if (!rsUpdateSetting) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            for (ProductVisibilitySettingDetailRequest productVisibilitySettingDetailRequest : request.getRecords()) {
                boolean isAddNew;
                ProductVisibilitySettingDetailEntity productVisibilitySettingDetailEntity;
                if (productVisibilitySettingDetailRequest.getId() != 0) {
                    isAddNew = false;
                    productVisibilitySettingDetailEntity =
                            this.productDB.getProductVisibilitySettingDetailEntity(
                                    productVisibilitySettingDetailRequest.getId()
                            );
                    if (productVisibilitySettingDetailEntity == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SETTING_DATA_TYPE_INVALID);
                    }
                    productVisibilitySettingDetailEntity.setModifier_id(sessionData.getId());
                    productVisibilitySettingDetailEntity.setModified_date(DateTimeUtils.getNow());
                } else {
                    isAddNew = true;
                    productVisibilitySettingDetailEntity = new ProductVisibilitySettingDetailEntity();
                    productVisibilitySettingDetailEntity.setCreator_id(sessionData.getId());
                    productVisibilitySettingDetailEntity.setCreated_date(DateTimeUtils.getNow());
                }

                productVisibilitySettingDetailEntity.setProduct_visibility_setting_id(productVisibilitySettingEntity.getId());
                if (productVisibilitySettingDetailEntity.getStatus() != SettingStatus.PENDING.getId()) {
                    if (productVisibilitySettingDetailRequest.getStart_date() == null || productVisibilitySettingDetailRequest.getStart_date() == 0) {
                        productVisibilitySettingDetailEntity.setStart_date(
                                productVisibilitySettingEntity.getStart_date());
                    } else {
                        productVisibilitySettingDetailEntity.setStart_date(
                                DateTimeUtils.getDateTime(productVisibilitySettingDetailRequest.getStart_date()
                                ));
                    }

                    if (request.getEnd_date() != null) {
                        productVisibilitySettingDetailEntity.setEnd_date(
                                DateTimeUtils.getDateTime(request.getEnd_date())
                        );
                    }
                }

                VisibilityDataType visibilityDataType = VisibilityDataType.from(
                        productVisibilitySettingDetailRequest.getVisibility_data_type()
                );
                if (visibilityDataType == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                switch (visibilityDataType) {
                    case PRODUCT: {
                        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (productCache == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setProduct_id(productCache.getId());
                        break;
                    }
                    case PRODUCT_GROUP: {
                        ProductGroup productGroup = this.dataManager.getProductManager().getMpProductGroup().get(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (productGroup == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setProduct_group_id(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        break;
                    }
                    case BRAND_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (category == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setCategory_level_4_id(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        break;
                    }
                    case PRODUCT_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (category == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setCategory_level_3_id(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        break;
                    }
                    case ITEM_CATEGORY: {
                        Category category = this.dataManager.getProductManager().getMpCategory().get(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (category == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setCategory_level_2_id(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        break;
                    }
                    case BRAND: {
                        BrandEntity brand = this.dataManager.getProductManager().getMpBrand().get(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        if (brand == null) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }

                        productVisibilitySettingDetailEntity.setBrand_id(
                                productVisibilitySettingDetailRequest.getVisibility_data_id());
                        break;
                    }
                    default: {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }


                productVisibilitySettingDetailEntity.setVisibility(
                        productVisibilitySettingDetailRequest.getVisibility()
                );

                productVisibilitySettingDetailEntity.setVisibility_data_type(
                        productVisibilitySettingDetailRequest.getVisibility_data_type()
                );
                productVisibilitySettingDetailEntity.setVisibility_data_id(
                        productVisibilitySettingDetailRequest.getVisibility_data_id()
                );

                JSONObject jsSettingDetail
                        = JsonUtils.DeSerialize(JsonUtils.Serialize(productVisibilitySettingDetailRequest), JSONObject.class);

                if (isAddNew) {
                    int rsInsertDetail =
                            this.productDB.insertProductVisibilitySettingDetail(productVisibilitySettingDetailEntity);
                    if (rsInsertDetail <= 0) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                    productVisibilitySettingDetailEntity.setId(rsInsertDetail);
                } else {
                    boolean rsUpdateDetail = this.productDB.updateProductVisibilitySettingDetail(productVisibilitySettingDetailEntity);
                    if (!rsUpdateDetail) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }

                /**
                 * Lưu lịch sử
                 */
                this.saveProductVisibilitySettingDetailHistory(sessionData,
                        jsSettingDetail,
                        productVisibilitySettingEntity.getStatus(),
                        productVisibilitySettingDetailEntity.getStart_date(),
                        productVisibilitySettingDetailEntity.getEnd_date(),
                        productVisibilitySettingDetailEntity.getProduct_visibility_setting_id());
            }

            if (SettingStatus.RUNNING.getId() == productVisibilitySettingEntity.getStatus()) {
                this.dataManager.callReloadProductVisibilitySetting(
                        request.getId()
                );
            }

            JSONObject data = new JSONObject();
            data.put("id", productVisibilitySettingEntity.getId());
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProductVisibilityByAgency(FilterListByIdRequest request) {
        try {
            String status = this.filterUtils.getValueByKey(
                    request.getFilters(), "status");
            Integer visibility = status.equals("") ? null : ConvertUtils.toInt(status);
            String search = this.filterUtils.getValueByType(request.getFilters(), "search");

            if (visibility == null) {
                String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT, request.getFilters(), request.getSorts());
                List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
                int total = this.productDB.getTotal(query);
                for (JSONObject js : records) {
                    js.put("visibility", this.getProductVisibilityByAgency(
                            request.getId(),
                            ConvertUtils.toInt(js.get("id")))
                    );
                }
                JSONObject data = new JSONObject();
                data.put("records", records);
                data.put("total", total);
                data.put("total_page", this.appUtils.getTotalPage(total));
                return ClientResponse.success(data);
            } else {
                int from = this.appUtils.getOffset(request.getPage());
                int to = from + ConfigInfo.PAGE_SIZE;
                List<ProductCache> result = this.dataManager.getProductManager()
                        .getMpProduct().values().stream().filter(
                                x -> filterVisibility(x, search, visibility, request.getId())
                        ).collect(Collectors.toList());
                int total = result.size();
                if (to > total) {
                    to = total;
                }
                List<ProductCache> records = new ArrayList<>();
                if (from < total) {
                    List<ProductCache> rsSubList = result.subList(
                            from,
                            to);
                    for (ProductCache productCache : rsSubList) {
                        ProductCache p = JsonUtils.DeSerialize(
                                JsonUtils.Serialize(productCache),
                                ProductCache.class
                        );
                        p.setStatus(p.getVisibility());
                        records.add(
                                p
                        );
                    }
                }
                JSONObject data = new JSONObject();
                data.put("records", records);
                data.put("total", total);
                data.put("total_page", this.appUtils.getTotalPage(total));
                return ClientResponse.success(data);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean filterVisibility(
            ProductCache x,
            String search,
            Integer visibility,
            int agency_id) {
        if ((search.isEmpty() ||
                x.getCode().contains(search) ||
                x.getCode().toLowerCase().contains(search.toLowerCase()) ||
                x.getFull_name().contains(search)) ||
                x.getFull_name().toLowerCase().contains(search.toLowerCase())
        ) {
            int status = getProductVisibilityByAgency(
                    agency_id,
                    x.getId()
            );
            x.setVisibility(status);

            if (visibility == null ||
                    (visibility == 0 && VisibilityType.HIDE.getId() == status) ||
                    (visibility == 1 && VisibilityType.SHOW.getId() == status)) {
                return true;
            }
        }

        return false;
    }

    public ClientResponse filterListProductHot(FilterListRequest request) {
        try {
            List<ProductHotData> hotList = new ArrayList<>();
            String query = this.filterUtils.getQuery(
                    FunctionList.LIST_PRODUCT_HOT,
                    new ArrayList<>(),
                    request.getSorts());
            JSONObject data = new JSONObject();
            List<ProductHotData> hotSettingList = new ArrayList<>();
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject jsonObject : records) {
                jsonObject.put("creator_info",
                        this.dataManager.getStaffManager().getStaff(
                                ConvertUtils.toInt(jsonObject.get("hot_modifier_id"))
                        ));
                ProductHotData productHotData = JsonUtils.DeSerialize(JsonUtils.Serialize(jsonObject),
                        ProductHotData.class);
                hotSettingList.add(productHotData);
            }

            List<String> productHotSettingIds = hotSettingList.stream().map(
                    e -> ConvertUtils.toString(e.getId())
            ).collect(Collectors.toList());

            hotList.addAll(hotSettingList);

            int hot_common = this.dataManager.getConfigManager().getHotCommon();
            if (hot_common > records.size()) {
                List<JSONObject> rsHotCommonList = this.productDB.filterHotCommon(
                        JsonUtils.Serialize(productHotSettingIds), hot_common - records.size());
                for (JSONObject rsHotCommon : rsHotCommonList) {
                    hotList.add(
                            JsonUtils.DeSerialize(JsonUtils.Serialize(rsHotCommon), ProductHotData.class)
                    );
                }
            }

            FilterRequest searchFilter = request.getFilters().stream().filter(
                    x -> x.getType().equals(TypeFilter.SEARCH)
            ).findFirst().orElse(null);

            FilterRequest hotLabelFilter = request.getFilters().stream().filter(
                    x -> x.getKey().equals("hot_label")
            ).findFirst().orElse(null);

            hotList = hotList.stream().
                    filter(
                            x -> isFilterProductHotSearch(searchFilter, x)
                    ).
                    filter(
                            x -> isFilterProductHotLabel(hotLabelFilter, x)
                    ).
                    collect(Collectors.toList());

            int total = this.productDB.getTotal(query);
            data.put("hot_common", this.dataManager.getConfigManager().getHotCommon());
            data.put("hot_agency", this.dataManager.getConfigManager().getHotAgency());
            data.put("records", hotList);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean isFilterProductHotSearch(FilterRequest searchFilter, ProductHotData x) {
        if (searchFilter == null ||
                x.getFull_name().contains(searchFilter.getValue()) ||
                x.getCode().contains(searchFilter.getValue())) {
            return true;
        }
        return false;
    }

    private boolean isFilterProductHotLabel(FilterRequest hotLabelFilter, ProductHotData x) {
        if (hotLabelFilter == null ||
                (x.getHot_label() != null && x.getHot_label().equals(hotLabelFilter.getValue()))) {
            return true;
        }
        return false;
    }

    public ClientResponse settingProductHot(SessionData sessionData, SettingProductHotRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            boolean rsUpdateHotConfig = this.dataManager.getConfigManager().updateHotConfig(
                    request.getNumber_common(),
                    request.getNumber_agency());
            if (rsUpdateHotConfig) {
                /**
                 * reload cache
                 */
                this.dataManager.reloadProductHot(0);
            }

            /**
             * Bỏ nổi bật sp cũ không tồn tại trong danh mới
             */
            List<JSONObject> oldProductHotList = this.productDB.getListProductHot();
            for (JSONObject js : oldProductHotList) {
                int id = ConvertUtils.toInt(js.get("id"));
                if (this.getProductHotInList(id, request.getRecords()) == null) {
                    /**
                     * bỏ nổi bật
                     */
                    boolean rsRemoveProductHot = this.productDB.removeProductHot(
                            id,
                            sessionData.getId());
                    if (rsRemoveProductHot) {
                        /**
                         * reload cache
                         */
                        this.dataManager.reloadProduct(
                                CacheType.PRODUCT, id);
                    }
                }
            }

            for (int iHot = 0; iHot < request.getRecords().size(); iHot++) {
                ProductHotRequest productHotRequest = request.getRecords().get(iHot);
                /**
                 * bỏ nổi bật
                 */
                boolean rsSetProductHot = this.productDB.setProductHot(
                        productHotRequest.getId(),
                        iHot + 1,
                        productHotRequest.getHot_label(),
                        sessionData.getId());
                if (rsSetProductHot) {
                    /**
                     * reload cache
                     */
                    this.dataManager.reloadProduct(
                            CacheType.PRODUCT,
                            productHotRequest.getId());
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ProductHotRequest getProductHotInList(int id, List<ProductHotRequest> records) {
        for (ProductHotRequest request : records) {
            if (request.getId() == id) {
                return request;
            }
        }
        return null;
    }

    public ClientResponse filterProductHotByAgency(FilterListByIdRequest request) {
        try {
            List<JSONObject> records = this.productDB.getListProductHot();
            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterProductHotType(SessionData sessionData, FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(
                    FunctionList.LIST_PRODUCT_HOT_TYPE,
                    request.getFilters(),
                    request.getSorts());
            List<JSONObject> records = this.productDB.filter(
                    query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE,
                    request.getIsLimit());
            JSONObject data = new JSONObject();
            int total = this.productDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse createProductHotType(SessionData sessionData, EditBasicDataRequest request) {
        try {
            if (request.getCode().isEmpty() ||
                    request.getName().isEmpty() ||
                    request.getImage().isEmpty()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
            }

            int rsInsert = this.productDB.insertProductHotType(
                    request.getName(),
                    request.getCode(),
                    request.getImage());

            if (rsInsert <= 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }
            request.setId(rsInsert);

            /**
             * Call reload cache app
             */
            this.dataManager.callReloadProductHotType(request.getId());

            JSONObject data = new JSONObject();
            data.put("record", request);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editProductHotType(SessionData sessionData, EditBasicDataRequest request) {
        try {
            if (request.getId() == 0 ||
                    request.getCode().isEmpty() ||
                    request.getName().isEmpty() ||
                    request.getImage().isEmpty()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
            }

            boolean rsUpdate = this.productDB.updateProductHotType(
                    request.getId(),
                    request.getName(),
                    request.getCode(),
                    request.getImage());
            if (!rsUpdate) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Call reload cache app
             */
            this.dataManager.callReloadProductHotType(request.getId());

            JSONObject data = new JSONObject();
            data.put("record", request);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse checkSettingProductHot(SessionData sessionData, SettingProductHotRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            List<JSONObject> records = new ArrayList<>();
            for (ProductHotRequest productHotRequest : request.getRecords()) {
                ProductEntity productEntity = this.productDB.getProduct(productHotRequest.getId());
                if (!productEntity.getHot_label().isEmpty()
                        && !productEntity.getHot_label().equals(productHotRequest.getHot_label())) {
                    JSONObject record = JsonUtils.DeSerialize(
                            JsonUtils.Serialize(productHotRequest),
                            JSONObject.class
                    );
                    record.put("hot_label_old", productEntity.getHot_label());
                    record.put("full_name", productEntity.getFull_name());
                    record.put("code", productEntity.getCode());
                    records.add(record);
                }
            }

            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public void saveProductHistory(
            int id,
            String before_data,
            String after_data,
            int staff_id
    ) {
        try {
            this.productDB.insertProductHistory(
                    id,
                    before_data,
                    after_data,
                    staff_id
            );
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    public ClientResponse filterProductHistory(FilterListRequest request) {
        try {
            FilterRequest filterRequest = new FilterRequest();
            filterRequest.setKey("product_id");
            filterRequest.setType(TypeFilter.SELECTBOX);
            filterRequest.setValue(ConvertUtils.toString(request.getId()));
            request.getFilters().add(filterRequest);
            String query = this.filterUtils.getQuery(FunctionList.FILTER_PRODUCT_HISTORY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("creator_info", this.dataManager.getStaffManager().getStaff(
                        ConvertUtils.toInt(js.get("creator_id"))
                ));

                js.put("before_data", JsonUtils.DeSerialize(
                        js.get("before_data").toString(), ProductHistoryData.class));
                js.put("after_data", JsonUtils.DeSerialize(
                        js.get("after_data").toString(), ProductHistoryData.class));
            }
            data.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                    ConvertUtils.toInt(request.getId())
            ));
            int total = this.productDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getProductHistoryDetail(BasicRequest request) {
        try {
            JSONObject detail = this.productDB.getProductHistoryDetail(
                    request.getId()
            );
            if (detail == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            detail.put("creator_info", this.dataManager.getStaffManager().getStaff(
                    ConvertUtils.toInt(detail.get("creator_id"))
            ));
            detail.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                    ConvertUtils.toInt(detail.get("product_id"))
            ));

            detail.put("before_data", JsonUtils.DeSerialize(
                    detail.get("before_data").toString(), ProductHistoryData.class));
            detail.put("after_data", JsonUtils.DeSerialize(
                    detail.get("after_data").toString(), ProductHistoryData.class));
            return ClientResponse.success(detail);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public JSONObject convertProductData(
            ProductEntity productEntity, ProductCache productCache) {
        try {
            JSONObject data = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(productEntity),
                    JSONObject.class
            );
            data.put("mat_hang_id", productCache.getMat_hang_id());
            data.put("plsp_id", productCache.getPlsp_id());
            data.put("pltth_id", productCache.getPltth_id());
            return data;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return new JSONObject();
    }

    /**
     * Import cập nhật sản phẩm
     *
     * @param sessionData
     * @param request
     * @return
     */
    public ClientResponse importUpdateProduct(SessionData sessionData, ImportUpdateProductRequest request) {
        try {
            /**
             * Validate request
             */
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            ProductEntity productEntity = this.productDB.getProduct(request.getId());
            if (productEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(request.getId());
            if (productCache == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            JSONObject before_data = this.convertProductData(
                    productEntity,
                    productCache
            );

            if (request.getPrice() != null && request.getPrice() != productEntity.getPrice()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EDIT_PRICE_INVALID);
            }

            /**
             * Validate rule
             * - thông số kỹ thuat sai format
             */
            if (StringUtils.isNotEmpty(request.getTechnical_data()) && this.appUtils.convertStringToArrayObject(request.getTechnical_data()) == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TECHNICAL_DATA_INVALID);
            }
            if (StringUtils.isNotEmpty(request.getImages()) && this.appUtils.convertStringToArray(request.getImages()) == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.IMAGE_INVALID);
            }
            if (!StringUtils.isNotEmpty(request.getCode()) && this.productDB.getProductByCode(request.getCode()) != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CODE_NOT_AVAILABLE);
            }

            if (request.getHot_label() != null &&
                    !request.getHot_label().isEmpty()) {
                JSONObject productHotType = this.dataManager.getConfigManager()
                        .getProductHotTypeByCode(request.getHot_label());
                if (productHotType == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_HOT_TYPE_INVALID);
                }
            }

            boolean hasEditProductGroup = true;
            int old_product_group_id = productEntity.getProduct_group_id();

            /**
             * tạo data cập nhật sản phẩm
             */
            String old_code = productEntity.getCode();
            productEntity = this.getProductUpdateByImport(productEntity, request);
            if (!old_code.equals(productEntity.getCode())) {
                ClientResponse crProductCode = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                crProductCode.setMessage("Vui lòng không cập nhật mã sản phẩm");
                return crProductCode;
            }
            if (request.getProduct_group_id() != null && request.getProduct_group_id() != old_product_group_id) {
                hasEditProductGroup = true;

                ProductGroupEntity productGroupEntity = this.productDB.getProductGroupInfo(request.getProduct_group_id());
                if (productGroupEntity == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_GROUP_NOT_FOUND);
                }
                productEntity.setCategory_id(productGroupEntity.getCategory_id());
            } else {
                hasEditProductGroup = false;
            }

            boolean rsEdit = this.productDB.editProductInfo(productEntity);
            if (rsEdit) {
                if (hasEditProductGroup) {
                    /**
                     * Cập nhật số lượng phiên bản
                     * giảm số lượng nhóm củ
                     * tăng sô lượng nhóm mới
                     */
                    this.productDB.increaseProductNumberInProductGroup(request.getProduct_group_id());
                    this.productDB.decreaseProductNumberInProductGroup(old_product_group_id);
                }
                /**
                 * Update cache
                 */
                this.dataManager.reloadProduct(CacheType.PRODUCT, request.getId());

                JSONObject after_data =
                        this.convertProductData(
                                productEntity,
                                this.dataManager.getProductManager().getProductBasicData(request.getId())
                        );

                this.saveProductHistory(
                        request.getId(),
                        JsonUtils.Serialize(before_data),
                        JsonUtils.Serialize(after_data),
                        sessionData.getId());

                return ClientResponse.success(productEntity);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.PRODUCT.getValue(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    /**
     * Import cập nhật nhóm hàng
     *
     * @param request
     * @return
     */
    public ClientResponse importEditProductGroup(SessionData sessionData, ImportEditProductGroupRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            ProductGroupEntity productGroupEntity = this.productDB.getProductGroupInfo(request.getId());
            if (productGroupEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_GROUP_NOT_FOUND);
            }

            int oldCategoryId = productGroupEntity.getCategory_id();

            /**
             * Mã nhóm
             */
            if (request.getCode() != null && !request.getCode().equals(productGroupEntity.getCode())) {
                productGroupEntity.setCode(request.getCode());
                productGroupEntity.setSort_data(SortUtils.convertProductCodeToProductSortData(request.getCode()));
            }
            /**
             * Tên nhóm
             */
            if (request.getName() != null) {
                productGroupEntity.setName(request.getName());
            }
            /**
             * Tên khác
             */
            if (request.getSimilar_name() != null) {
                productGroupEntity.setSimilar_name(request.getSimilar_name());
            }
            /**
             * ID PLTTH
             */
            if (request.getCategory_id() != null) {
                productGroupEntity.setCategory_id(request.getCategory_id());
            }
            /**
             * Trạng thái
             */
            if (request.getStatus() != null) {
                productGroupEntity.setStatus(request.getStatus());
            }

            boolean rsCreateProductGroup = this.productDB.editProductGroup(productGroupEntity);
            if (rsCreateProductGroup) {
                /**
                 * Nếu nhóm đổi danh mục thì sku cũng đổi theo
                 */
                if (oldCategoryId != productGroupEntity.getCategory_id()) {
                    boolean rsuUpdateCategoryForProductByProductGroup = this.productDB.updateCategoryForProductByProductGroup(request.getId(), productGroupEntity.getCategory_id());
                }

                /**
                 * reload cache
                 */
                this.dataManager.reloadProductGroup(CacheType.PRODUCT_GROUP, request.getId());

                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("PRODUCT", ex);
        }
        
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}