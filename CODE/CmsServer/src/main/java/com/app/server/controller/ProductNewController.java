package com.app.server.controller;

import com.app.server.constants.path.ProductPath;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.product.EditProductNewRequest;
import com.app.server.response.ClientResponse;
import com.app.server.service.ProductNewService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class ProductNewController extends BaseController {
    private ProductNewService productNewService;

    @Autowired
    public void setProductNewService(ProductNewService productNewService) {
        this.productNewService = productNewService;
    }

    @RequestMapping(value = ProductPath.FILTER_PRODUCT_NEW, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Lấy danh sach sản phẩm", notes = "")
    public ClientResponse filterProductNew(
            @RequestBody FilterListRequest request) {
        return this.productNewService.filterProductNew(request);
    }

    @RequestMapping(value = ProductPath.EDIT_PRODUCT_NEW, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Chỉnh sửa danh sach sản phẩm", notes = "")
    public ClientResponse editProductNew(
            @RequestBody EditProductNewRequest request) {
        return this.productNewService.editProductNew(request);
    }
}