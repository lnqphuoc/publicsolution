package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.controller.BaseController;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.product.ProductNewData;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.product.EditProductNewRequest;
import com.app.server.database.PriceDB;
import com.app.server.database.ProductDB;
import com.app.server.enums.FunctionList;
import com.app.server.enums.ImagePath;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.Convert;
import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ProductNewService extends BaseService {
    public ClientResponse filterProductNew(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_PRODUCT_NEW, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.productDB.filterProduct(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, request.getIsLimit());
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.PRODUCT.getImageUrl());
                js.put("product_info", this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(js.get("product_id"))
                ));
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

    public ClientResponse editProductNew(EditProductNewRequest request) {
        try {
            for (int iP = 0; iP < request.getProducts().size(); iP++) {
                ProductNewData productNewData = request.getProducts().get(iP);
                JSONObject jsProduct = this.dataManager.getProductManager().getProductByCode(
                        productNewData.getCode()
                );
                if (jsProduct == null) {
                    ClientResponse crProduct = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PRODUCT_INVALID);
                    crProduct.setMessage("[Sản phẩm thứ " + (iP + 1) + "]" + crProduct.getMessage());
                    return crProduct;
                }

                productNewData.setProduct_id(
                        ConvertUtils.toInt(jsProduct.get("id")));
            }

            List<JSONObject> productNewList = this.productDB.getListProductNew();
            if (!productNewList.isEmpty()) {
                boolean rsClear = this.productDB.clearProductNew();
                if (!rsClear) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }
            }

            for (int iP = 0; iP < request.getProducts().size(); iP++) {
                ProductNewData productNewData = request.getProducts().get(iP);
                int rs = this.productDB.insertProductNew(
                        productNewData.getProduct_id(),
                        productNewData.getPriority(),
                        DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(
                                        productNewData.getCreated_date()),
                                "yyyy-MM-dd HH:mm:ss")

                );
                if (rs <= 0) {
                    ClientResponse cr = ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    if (cr.failed()) {
                        cr.setMessage("[Sản phẩm thứ " + (iP + 1) + "]" + cr.getMessage());
                        return cr;
                    }
                }
            }

            this.dataManager.callReloadProductNew();

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}