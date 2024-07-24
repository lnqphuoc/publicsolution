package com.app.server.utils;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.request.agency.AddNewAgencyRequest;
import com.app.server.data.request.agency.EditAgencyRequest;
import com.app.server.enums.GenderType;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import com.ygame.framework.common.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ValidateUtils {
    private AppUtils appUtils;

    @Autowired
    public void setAppUtils(AppUtils appUtils) {
        this.appUtils = appUtils;
    }

    public boolean validAgencyImage(String images) {
        List<String> rsImage = this.appUtils.convertStringToArray(images);
        if (rsImage == null) {
            return false;
        }

        return true;
    }

    public ClientResponse validateEditAgency(EditAgencyRequest request) {
        try {
            if (request.getFull_name() != null && (request.getFull_name().isEmpty() || request.getFull_name().trim().length() > 100)) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FULL_NAME_INVALID);
            } else if (request.getPhone() != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PHONE_INVALID);
            } else if (request.getEmail() != null && !request.getEmail().isEmpty() && !appUtils.checkEmail(request.getEmail())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EMAIL_INVALID);
            } else if (request.getShop_name() != null && (StringUtils.isBlank(request.getShop_name()) || request.getShop_name().trim().length() > 200)) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SHOP_NAME_INVALID);
            } else if (request.getCity_id() != null && request.getCity_id() < 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CITY_INVALID);
            } else if (request.getDistrict_id() != null && request.getDistrict_id() < 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DISTRICT_INVALID);
            } else if (request.getAddress() != null && (StringUtils.isBlank(request.getAddress()) || request.getAddress().trim().length() > 500)) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ADDRESS_INVALID);
            } else if (request.getBirthday() != null && StringUtils.isNotBlank(request.getBirthday()) && !this.appUtils.validBirthday(request.getBirthday())) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.BIRTHDAY_INVALID);
            } else if (request.getPassword() != null && (StringUtils.isNotBlank(request.getPassword()) || !appUtils.checkPassword(request.getPassword()))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PASSWORD_INVALID);
            } else if (request.getImages() != null && (StringUtils.isBlank(request.getImages()))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.IMAGE_INVALID);
            } else if (request.getBusiness_type() != null && request.getBusiness_type() < 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.BUSINESS_TYPE_NOT_EMPTY);
            } else if (request.getMainstay_industry_id() != null && request.getMainstay_industry_id() < 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.MAINSTAY_INDUSTRY_NOT_EMPTY);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        return ClientResponse.success(null);
    }

    public ClientResponse validateAddNewAgency(AddNewAgencyRequest request) {
        try {
//            if (StringUtils.isBlank(request.getFull_name()) || request.getFull_name().trim().length() > 100) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FULL_NAME_INVALID);
//            } else if (!appUtils.checkPhone(request.getPhone())) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PHONE_INVALID);
//            } else if (!appUtils.checkEmail(request.getEmail())) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.EMAIL_INVALID);
//            } else if (StringUtils.isBlank(request.getShop_name()) || request.getShop_name().trim().length() > 200) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.SHOP_NAME_INVALID);
//            } else if (request.getCity_id() <= 0) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CITY_INVALID);
//            } else if (request.getDistrict_id() <= 0) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.DISTRICT_INVALID);
//            } else if (StringUtils.isBlank(request.getAddress()) || request.getAddress().trim().length() > 100) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ADDRESS_INVALID);
//            } else if (StringUtils.isNotBlank(request.getBirthday()) && !this.appUtils.validBirthday(request.getBirthday())) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.BIRTHDAY_INVALID);
//            } else if (!appUtils.checkPassword(request.getPassword())) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PASSWORD_INVALID);
//            } else if (StringUtils.isNotBlank(request.getTax_number()) && request.getTax_number().trim().length() > 20) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.TAX_NUMBER_INVALID);
////            } else if (!GenderType.valid(request.getGender())) {
////                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.GENDER_INVALID);
//            } else if (!this.validAgencyImage(request.getImages())) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.IMAGE_INVALID);
//            }
        } catch (Exception ex) {
            LogUtil.printDebug("A", ex);
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
        }
        return ClientResponse.success(null);
    }

    public boolean checkValidateAgencyCode(String code) {
        return true;
    }

    public boolean checkPhoneExistInAgencyAccount(JSONObject jsonObject) {
        if (jsonObject != null) {
            return true;
        }
        return false;
    }
}