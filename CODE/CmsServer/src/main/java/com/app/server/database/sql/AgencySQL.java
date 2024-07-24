package com.app.server.database.sql;

import com.app.server.data.dto.agency.Agency;
import com.app.server.data.dto.agency.AgencyAccount;
import com.app.server.data.dto.agency.Membership;
import com.app.server.data.request.agency.*;
import com.app.server.enums.AgencyStatus;
import com.app.server.enums.MembershipType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AgencySQL {
    public String addNewAgency(AddNewAgencyRequest request, String code, String pin_code) {
        String sql = "INSERT INTO agency(" +
                "avatar," +
                "code," +
                "pin_code," +
                "full_name," +
                "phone," +
                "email," +
                "shop_name," +
                "address," +
                "tax_number," +
                "region_id," +
                "city_id," +
                "district_id," +
                "ward_id," +
                "gender," +
                "images," +
                "birthday," +
                "business_type," +
                "business_department_id," +
                "mainstay_industry_id," +
                "status)" +
                " VALUES(" +
                "'" + request.getAvatar() + "'" + "," +
                "'" + code + "'" + "," +
                "'" + pin_code + "'" + "," +
                "'" + request.getFull_name() + "'" + "," +
                "'" + request.getPhone() + "'" + "," +
                "'" + request.getEmail() + "'" + "," +
                "'" + request.getShop_name() + "'" + "," +
                "'" + request.getAddress() + "'" + "," +
                "'" + request.getTax_number() + "'" + "," +
                "'" + request.getRegion_id() + "'" + "," +
                "'" + request.getCity_id() + "'" + "," +
                "'" + request.getDistrict_id() + "'" + "," +
                request.getWard_id() + "," +
                "'" + request.getGender() + "'" + "," +
                "'" + request.getImages() + "'" + "," +
                "'" + request.getBirthday() + "'" + "," +
                "'" + request.getBusiness_type() + "'" + "," +
                "" + request.getBusiness_department_id() + "" + "," +
                "'" + request.getMainstay_industry_id() + "'" + "," +
                "'" + AgencyStatus.WAITING_APPROVE.getValue() + "'" +
                ")";

        return sql;
    }

    public String addNewAgencyAccount(AgencyAccount account) {
        return "INSERT INTO agency_account(" +
                "full_name," +
                "username," +
                "password," +
                "agency_id," +
                "agency_phone," +
                "is_primary," +
                "status" +
                ")" +
                " VALUES(" +
                "'" + account.getFullName() + "'," +
                "'" + account.getUsername() + "'," +
                "'" + account.getPassword() + "'," +
                "'" + account.getAgencyId() + "'," +
                "'" + account.getAgencyPhone() + "'," +
                "'" + account.getIsPrimary() + "'," +
                "'" + account.getStatus() + "'" +
                ")";
    }

    public String getAgencyById(int id) {
        return "SELECT * FROM agency WHERE id=" + id;
    }

    public String updateCode(int id, String code) {
        return "UPDATE agency SET code = '" + code + "' WHERE id = " + id;
    }

    public String approveAgency(int id, int status) {
        return "UPDATE agency SET status = '" + status + "'" +
                ", membership_id = " + MembershipType.THANH_VIEN.getKey() +
                ", approved_date=NOW()" +
                " WHERE id = " + id;
    }

    public String getCurrentIndexAgencyInCity(int cityId) {
        return "SELECT city_index FROM agency_code_generate WHERE city_id=" + cityId +
                " ORDER BY id DESC" +
                " LIMIT 1";
    }

    public String saveCityIndex(int cityId, int nextIndexCity, int agencyId) {
        return "INSERT INTO agency_code_generate(" +
                "city_id," +
                "city_index," +
                "agency_id," +
                "status)" +
                " VALUES(" +
                "'" + cityId + "'," +
                "'" + nextIndexCity + "'," +
                "'" + agencyId + "'," +
                "'" + "1" + "'" +
                ")";
    }

    public String updateAgency(Agency agency) {
        return "UPDATE agency SET" +
                " avatar = '" + agency.getAvatar() + "'" +
                ", full_name = '" + agency.getFullName() + "'" +
                ", shop_name = '" + agency.getShop_name() + "'" +
                ", gender = '" + agency.getGender() + "'" +
                ", birthday = '" + agency.getBirthday() + "'" +
                ", email = '" + agency.getEmail() + "'" +
                ", address = '" + agency.getAddress() + "'" +
                ", city_id = '" + agency.getCityId() + "'" +
                ", district_id = '" + agency.getDistrictId() + "'" +
                ", ward_id = " + agency.getWardId() + "" +
                ", region_id = '" + agency.getRegionId() + "'" +
                ", business_department_id = '" + agency.getBusinessDepartmentId() + "'" +
                ", images = '" + agency.getLtImage() + "'" +
                ", business_type = '" + agency.getBusinessType() + "'" +
                ", mainstay_industry_id = '" + agency.getMainstayIndustryId() + "'" +
                " WHERE id = " + agency.getId();
    }

    public String updateStatusAgency(int id, int status) {
        return "UPDATE agency SET" +
                " status = '" + status + "'" +
                " WHERE id = " + id;
    }

    public String getAgencyAccountByPhone(String phone) {
        return "SELECT * FROM agency_account WHERE username ='" + phone + "'";
    }

    public String getListAgencyAccount(int agency_id) {
        return "SELECT * FROM agency_account WHERE agency_id ='" + agency_id + "'" +
                " ORDER BY is_primary DESC, id ASC";
    }

    public String getListAddressDelivery(int agency_id) {
        return "SELECT * FROM agency_address_delivery WHERE agency_id ='" + agency_id + "' AND status = 1";
    }

    public String getListAddressExportBilling(int agency_id) {
        return "SELECT * FROM agency_address_export_billing WHERE agency_id ='" + agency_id + "' AND status = 1";
    }

    public String getAddressDeliveryDetail(int id) {
        return "SELECT * FROM agency_address_delivery WHERE id ='" + id + "'";
    }

    public String getAddressExportBillingDetail(int id) {
        return "SELECT * FROM agency_address_export_billing WHERE id ='" + id + "'";
    }

    public String getAgencyAccountDetail(int id) {
        return "SELECT * FROM agency_account WHERE id ='" + id + "'";
    }

    public String editAgencyAccount(AgencyAccount account) {
        return "UPDATE agency_account SET" +
                " username = '" + account.getUsername() + "'" +
                ", password = '" + account.getPassword() + "'" +
                ", status = '" + account.getStatus() + "'" +
                ", is_primary = '" + account.getIsPrimary() + "'" +
                ", full_name = '" + account.getFullName() + "'" +
                " WHERE id = " + account.getId();
    }

    public String updateStatusAddressDelivery(int id, int status) {
        return "UPDATE agency_address_delivery SET" +
                " status = '" + status + "'" +
                " WHERE id = " + id;
    }

    public String addAddressDelivery(AddAddressDeliveryRequest request) {
        return "INSERT INTO agency_address_delivery(" +
                "agency_id," +
                "full_name," +
                "phone," +
                "address," +
                "truck_number," +
                "is_default" +
                ")" +
                " VALUES(" +
                "'" + request.getAgency_id() + "'," +
                "'" + request.getFull_name() + "'," +
                "'" + request.getPhone() + "'," +
                "'" + request.getAddress() + "'," +
                "'" + request.getTruck_number() + "'," +
                "'" + request.getIs_default() + "'" +
                ")";
    }

    public String editAddressDelivery(EditAddressDeliveryRequest request) {
        return "UPDATE agency_address_delivery SET" +
                " full_name='" + request.getFull_name() + "'," +
                " phone='" + request.getPhone() + "'," +
                " address='" + request.getAddress() + "'," +
                " truck_number='" + request.getTruck_number() + "'," +
                " is_default='" + request.getIs_default() + "'" +
                " WHERE id = " + request.getId();
    }

    public String addAddressExportBilling(AddAddressExportBillingRequest request) {
        return "INSERT INTO agency_address_export_billing(" +
                "agency_id," +
                "billing_label," +
                "billing_name," +
                "email," +
                "address," +
                "is_default," +
                "tax_number" +
                ")" +
                " VALUES(" +
                "'" + request.getAgency_id() + "'," +
                "'" + request.getBilling_label() + "'," +
                "'" + request.getBilling_name() + "'," +
                "'" + request.getEmail() + "'," +
                "'" + request.getAddress() + "'," +
                "'" + request.getIs_default() + "'," +
                "'" + request.getTax_number() + "'" +
                ")";
    }

    public String editAddressExportBilling(EditAddressExportBillingRequest request) {
        return "UPDATE agency_address_export_billing SET" +
                " billing_label = '" + request.getBilling_label() + "'" +
                ", billing_name = '" + request.getBilling_name() + "'" +
                ", email = '" + request.getEmail() + "'" +
                ", address = '" + request.getAddress() + "'" +
                ", tax_number = '" + request.getTax_number() + "'" +
                ", is_default = '" + request.getIs_default() + "'" +
                " WHERE id = " + request.getId();
    }

    public String updateStatusAddressExportDelivery(int id, int status) {
        return "UPDATE agency_address_export_billing SET" +
                " status = '" + status + "'" +
                " WHERE id = " + id;
    }

    public String setAgencyAccountMain(int id) {
        return "UPDATE agency_account SET" +
                " is_primary = '" + 1 + "'" +
                " WHERE id = " + id;
    }

    public String getAgencyAccountMain(int agency_id) {
        return "SELECT * FROM agency_account WHERE agency_id = '" + agency_id + "'" +
                " AND is_primary=1";
    }

    public String setAgencyAccountSub(int id) {
        return "UPDATE agency_account SET" +
                " is_primary = '" + 0 + "'" +
                " WHERE id = " + id;
    }

    public String updateStatusAgencyAccount(int id, int status) {
        return "UPDATE agency_account SET" +
                " status = '" + status + "'" +
                " WHERE id = " + id;
    }

    public String getAgencyAddressExportDefault(int agency_id) {
        return "SELECT * FROM agency_address_export_billing WHERE agency_id = '" + agency_id + "'" +
                " AND is_default=1";
    }

    public String setAgencyAddressExportDefault(int id) {
        return "UPDATE agency_address_export_billing SET" +
                " is_default = '" + 1 + "'" +
                " WHERE id = " + id;
    }

    public String setAgencyAddressExportNotDefault(int id) {
        return "UPDATE agency_address_export_billing SET" +
                " is_default = '" + 0 + "'" +
                " WHERE id = " + id;
    }

    public String getAgencyAddressDeliveryDefault(int agency_id) {
        return "SELECT * FROM agency_address_delivery WHERE agency_id = '" + agency_id + "'" +
                " AND is_default=1";
    }

    public String setAgencyAddressDeliveryNotDefault(int id) {
        return "UPDATE agency_address_delivery SET" +
                " is_default = '" + 0 + "'" +
                " WHERE id = " + id;
    }

    public String setAgencyAddressDeliveryDefault(int id) {
        return "UPDATE agency_address_delivery SET" +
                " is_default = '" + 1 + "'" +
                " WHERE id = " + id;
    }

    public String getAgencyContractInfoByAgencyId(int agency_id) {
        return "SELECT * FROM agency_contract_info WHERE agency_id='" + agency_id + "'";
    }

    public String createAgencyContractInfo(UpdateAgencyContractInfoRequest request) {
        return "INSERT INTO agency_contract_info(" +
                "agency_id," +
                "contract_number," +
                "company_name," +
                "representative," +
                "tax_number," +
                "identity_number," +
                "identity_date" +
                ")" +
                " VALUES(" +
                "'" + request.getAgency_id() + "'," +
                "'" + request.getContract_number() + "'," +
                "'" + request.getCompany_name() + "'," +
                "'" + request.getRepresentative() + "'," +
                "'" + request.getTax_number() + "'," +
                "'" + request.getIdentity_number() + "'," +
                "'" + request.getIdentity_date() + "'" +
                ")";
    }

    public String updateAgencyContractInfo(UpdateAgencyContractInfoRequest request) {
        return "UPDATE agency_contract_info SET " +
                "contract_number='" + request.getContract_number() + "'" +
                ", company_name='" + request.getCompany_name() + "'" +
                ", representative='" + request.getRepresentative() + "'" +
                ", tax_number='" + request.getTax_number() + "'" +
                ", identity_number='" + request.getContract_number() + "'" +
                ", identity_date='" + request.getContract_number() + "'" +
                " WHERE agency_id = " + request.getAgency_id();
    }

    public String getBusinessDepartmentIdByRegionId(int regionId) {
        return "SELECT business_department_id FROM business_department_detail" +
                " WHERE region_id='" + regionId + "'";
    }

    public String setBusinessDepartment(int id, int businessDepartmentId) {
        return "UPDATE agency SET business_department_id='" +
                businessDepartmentId + "'" +
                " WHERE id=" + id;

    }
}