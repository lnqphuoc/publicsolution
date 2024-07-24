package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ConfigConstants;
import com.app.server.constants.MissionConstants;
import com.app.server.constants.ProductConstants;
import com.app.server.data.SessionData;
import com.app.server.data.dto.agency.Agency;
import com.app.server.data.dto.agency.AgencyAccount;
import com.app.server.data.dto.program.DeptInfo;
import com.app.server.data.dto.program.Program;
import com.app.server.data.dto.program.Source;
import com.app.server.data.dto.program.filter.ProgramFilter;
import com.app.server.data.dto.staff.Staff;
import com.app.server.data.entity.*;
import com.app.server.data.request.*;
import com.app.server.data.request.agency.*;
import com.app.server.data.response.agency.AgencyInfoResponse;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.constants.ResponseMessage;
import com.app.server.utils.AppUtils;
import com.app.server.utils.JsonUtils;
import com.google.common.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AgencyService extends BaseService {

    private AssignMissionService assignMissionService;

    @Autowired
    public void setAssignMissionService(AssignMissionService assignMissionService) {
        this.assignMissionService = assignMissionService;
    }

    /**
     * Lấy danh sách đại lý
     *
     * @param request
     * @return
     */
    public ClientResponse filterAgency(SessionData sessionData, FilterListRequest request) {
        try {
            this.addFilterAgencyDataForListAgency(sessionData, request);
            String query = this.filterUtils.getQuery(FunctionList.LIST_AGENCY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.filterAgency(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE);

            int total_rank = this.agencyDB.getTotalRank();
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.AVATAR.getImageUrl());
                int agency_id = ConvertUtils.toInt(js.get("id"));

                int status = ConvertUtils.toInt(js.get("status"));
                if (status == AgencyStatus.WAITING_APPROVE.getValue()) {
                    js.put("membership_id", 0);
                }
                js.put("total_rank_value", total_rank);
                js.put("dept_info", this.getDeptInfo(agency_id));

                JSONObject lock_info = this.getLockTime(
                        agency_id,
                        ConvertUtils.toInt(js.get("city_id")),
                        ConvertUtils.toInt(js.get("region_id")),
                        ConvertUtils.toInt(js.get("membership_id")),
                        ConvertUtils.toInt(js.get("status")),
                        js.get("dept_order_date") == null ? null :
                                AppUtils.convertJsonToDate(js.get("dept_order_date")),
                        js.get("lock_check_date") == null ? null :
                                AppUtils.convertJsonToDate(js.get("lock_check_date")),
                        AppUtils.convertJsonToDate(js.get("created_date")),
                        AppUtils.convertJsonToDate(js.get("approved_date")),
                        AppUtils.convertJsonToDate(js.get("locked_date"))
                );
                js.put("lock_info", lock_info);

                js.put("nick_name", "");
            }
            int total = this.agencyDB.getTotalAgency(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse validateApproveAgency(Agency agency) {
        /**
         * Trạng thái chưa duyệt
         */
        if (agency.getStatus() != AgencyStatus.WAITING_APPROVE.getValue()) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
        }

        return ClientResponse.success(null);
    }

    public ClientResponse addNewAgency(SessionData sessionData, AddNewAgencyRequest request) {
        try {
            /**
             * validate info request add new agency
             */
            ClientResponse clientResponse = this.validateUtils.validateAddNewAgency(request);
            if (clientResponse.failed()) {
                return clientResponse;
            }

//            if ((request.getWard_id() == null || request.getWard_id() == 0)
//                    && this.dataManager.getProductManager().getMpDistrict().get(request.getDistrict_id()).getLtWard().size() > 0) {
//                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.WARD_INVALID);
//            }

            /**
             * check phone available
             */
            if (this.agencyDB.getAgencyAccountByPhone(request.getPhone()) != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PHONE_USED);
            }

            request.setCity_id(ProductConstants.CITY_ID);
            request.setDistrict_id(ProductConstants.DISTRICT_ID);
            request.setWard_id(ProductConstants.WARD_ID);

            /**
             * Gán vùng cho đại lý
             */
            int regionId = this.dataManager.getProductManager().getRegionFromCity(request.getCity_id());
            request.setRegion_id(regionId);
            /**
             * Gán phòng kinh doanh cho đại lý
             */
            Integer businessDepartmentId = this.dataManager.getStaffManager().getBussinessDepartment(
                    sessionData.getId());
            request.setBusiness_department_id(businessDepartmentId);

            if (request.getBusiness_type() == null) {
                request.setBusiness_type(0);
            }
            if (request.getMainstay_industry_id() == null) {
                request.setMainstay_industry_id(0);
            }

            /**
             * Nếu vùng chưa phân bổ vào phòng kinh doanh
             */
            if (businessDepartmentId == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.REGION_MISS_BUSINESS_DEPARTMENT);
            }

            /**
             * Tạo đại lý mới
             * - nếu chưa có hình ảnh thì lưu hình mặc định
             * - chọn hình đầu tiên làm hình đại diện của đại lý
             * - md5 password
             */

            /**
             - chọn tầm hình đầu tiên làm hình đại diện của đại lý
             */
            if (request.getAvatar().isEmpty()) {
                request.setAvatar("default.png");
            }

            String pin_code = this.generatePinCode();
            int rsAddNewAgency = this.agencyDB.addNewAgency(request, "", pin_code);
            if (rsAddNewAgency > 0) {
                /**
                 * lưu tài khoản đăng nhập
                 * tài khoản này là tài khoản chính
                 */
                AgencyAccount agencyAccount = new AgencyAccount();
                agencyAccount.setFullName(request.getFull_name());
                agencyAccount.setUsername(request.getPhone());
                agencyAccount.setPassword(DigestUtils.md5Hex(request.getPassword()));
                agencyAccount.setAgencyId(rsAddNewAgency);
                agencyAccount.setAgencyPhone(request.getPhone());
                agencyAccount.setIsPrimary(AccountType.PRIMARY.getValue());
                agencyAccount.setStatus(ActiveStatus.ACTIVATED.getValue());
                int rsAddAgencyAccount = this.agencyDB.addAgencyAccount(agencyAccount);
                if (rsAddAgencyAccount > 0) {
                    return ClientResponse.success(null);
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private String generatePinCode() {
        SecureRandom random = new SecureRandom();
        int rand = random.nextInt(10000);
        return String.format("%04d", rand);
    }

    private int getMembershipDefault() {
        return 1;
    }

    public ClientResponse editAgency(SessionData sessionData, EditAgencyRequest request) {
        try {
            ClientResponse clientResponse = this.validateUtils.validateEditAgency(request);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            if ((request.getWard_id() == null || request.getWard_id() == 0)
                    && this.dataManager.getProductManager().getMpDistrict().get(request.getDistrict_id()).getLtWard().size() > 0) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.WARD_INVALID);
            }

            Agency agency = agencyDB.getAgencyById(request.getId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            /**
             * Cập nhật nhân viên phụ trách
             */

            agency = this.getAgencyInfoUpdate(agency, request);

            boolean rsUpdateAgency = this.agencyDB.updateAgency(agency);
            if (!rsUpdateAgency) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (!request.getCode().isEmpty() &&
                    !request.getCode().equals(agency.getCode())) {
                this.agencyDB.updateAgencyCode(agency.getId(), request.getCode());
            }

            if (request.getCommit_limit() != null) {
                DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(request.getId());
                if (deptAgencyInfoEntity == null) {
                    deptAgencyInfoEntity = this.initDeptAgencyInfo(request.getId());
                }


                boolean rsUpdateCommitLimit = this.deptDB.updateCommitLimit(
                        request.getId(),
                        request.getCommit_limit(),
                        deptAgencyInfoEntity.getMiss_commit() > request.getCommit_limit() ?
                                request.getCommit_limit() :
                                deptAgencyInfoEntity.getMiss_commit()

                );
            }

            if (ConvertUtils.toInt(this.dataManager.getConfigManager().getMPConfigData().get("SYNC")) == 1 &&
                    agency.getStatus() == AgencyStatus.APPROVED.getValue()
                    && !this.checkBusinessPKDHeThong(agency.getBusinessDepartmentId())) {
                JSONObject jsAgencyInfo = this.agencyDB.getAgencyInfoById(request.getId());
                jsAgencyInfo.put("full_address", this.dataManager.getProductManager().getFullAddress(jsAgencyInfo));

                ClientResponse crSyncAgencyInfo = this.bravoService.syncAgencyInfo(
                        jsAgencyInfo
                );
                if (crSyncAgencyInfo.failed()) {
                    this.agencyDB.syncAgencyInfoFail(
                            request.getId(),
                            crSyncAgencyInfo.getMessage(),
                            1
                    );
                } else {
                    this.agencyDB.syncAgencyInfoSuccess(
                            request.getId()
                    );
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);

        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private boolean checkBusinessPKDHeThong(Integer businessDepartmentId) {
        if (businessDepartmentId == 5) {
            return true;
        }
        return false;
    }

    private Agency getAgencyInfoUpdate(Agency agency, EditAgencyRequest request) {
        if (StringUtils.isNotBlank(request.getFull_name())) {
            agency.setFullName(request.getFull_name());
        }

        if (StringUtils.isNotBlank(request.getShop_name())) {
            agency.setShop_name(request.getShop_name());
        }

        if (request.getGender() != null) {
            agency.setGender(request.getGender());
        }

        if (request.getBirthday() != null) {
            agency.setBirthday(request.getBirthday());
        }

        if (request.getEmail() != null) {
            agency.setEmail(request.getEmail());
        }

        if (StringUtils.isNotBlank(request.getAddress())) {
            agency.setAddress(request.getAddress());
        }

        if (request.getCity_id() != null) {
            agency.setCityId(request.getCity_id());
            agency.setWardId(request.getWard_id());
        }

        if (request.getDistrict_id() != null) {
            agency.setDistrictId(request.getDistrict_id());
            agency.setWardId(request.getWard_id());
        }

        if (request.getRegion_id() != null) {
            agency.setRegionId(request.getRegion_id());
        }

        if (request.getImages() != null && StringUtils.isNotBlank(request.getImages())) {
            agency.setLtImage(request.getImages());
        }

        if (StringUtils.isNotBlank(request.getAvatar())) {
            agency.setAvatar(request.getAvatar());
        }

        if (request.getBusiness_type() != null) {
            agency.setBusinessType(request.getBusiness_type());
        }

        if (request.getMainstay_industry_id() != null) {
            agency.setMainstayIndustryId(request.getMainstay_industry_id());
        }

        if (request.getBusiness_department_id() != null) {
            agency.setBusinessDepartmentId(request.getBusiness_department_id());
        }

        return agency;
    }

    public ClientResponse approveAgency(SessionData sessionData, BasicRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }
            Agency agency = agencyDB.getAgencyById(request.getId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            ClientResponse clientResponse = this.validateApproveAgency(agency);
            if (clientResponse.failed()) {
                return clientResponse;
            }

            /**
             * phòng kinh doanh
             */
            if (agency.getBusinessDepartmentId() == null || agency.getBusinessDepartmentId() == 0) {
                /**
                 * Chuyền tình thành cần cập nhật lại vùng kinh doanh
                 */
                Integer businessDepartmentId = this.agencyDB.getBusinessDepartmentIdByRegionId(agency.getRegionId());
                /**
                 * Nếu vùng chưa phân bổ vào phòng kinh doanh
                 */
                if (businessDepartmentId == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.REGION_MISS_BUSINESS_DEPARTMENT);
                } else {
                    boolean rsSetBusinessDepartment = this.agencyDB.setBusinessDepartment(agency.getId(), businessDepartmentId);
                    if (!rsSetBusinessDepartment) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }
            }

            /**
             * tạo mã cho đại lý
             */
//            String code = this.generateAgencyCode(agency.getRegionId(), agency.getCityId(), agency.getMembershipId(), agency.getId());
            String code = this.generateAgencyCodeV2(agency.getId(), staff.getDepartment_id());
            if (!this.validateUtils.checkValidateAgencyCode(code)) {
                code = this.generateAgencyCode(agency.getRegionId(), agency.getCityId(), agency.getMembershipId(), agency.getId());
            }

            boolean rsUpdateCode = this.agencyDB.updateCode(agency.getId(), code);
            if (!rsUpdateCode) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * cập nhật trạng thái đại lý thành đã duyệt
             * trạng thái: APPROVED
             * ngày duyệt
             */
            boolean rsApproveAgency = this.agencyDB.approveAgency(agency.getId());
            if (!rsApproveAgency) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            this.insertDeptSettingDefault(request.getId());

            /**
             * Tạo dữ liệu cho công nợ
             */
            this.updateDeptAgencyInfo(agency.getId());

            this.initDeptAgencyDateByStartDate(
                    this.deptDB.getDeptAgencyInfo(agency.getId()), agency.getId());

            //this.createAddressDeliveryDefault(agency);

            /**
             * Push notify duyệt đại lý
             */
            this.pushNotifyToAgency(
                    0,
                    NotifyAutoContentType.APPROVE_AGENCY,
                    "",
                    NotifyAutoContentType.APPROVE_AGENCY.getType(),
                    "[]",
                    NotifyAutoContentType.APPROVE_AGENCY.getLabel(),
                    agency.getId()
            );

            /**
             * Đồng bộ qua Bravo
             */
            JSONObject jsAgencyInfo = this.agencyDB.getAgencyInfoById(request.getId());
            jsAgencyInfo.put("full_address", this.dataManager.getProductManager().getFullAddress(jsAgencyInfo));

            ClientResponse crSyncAgencyInfo = this.bravoService.syncAgencyInfo(
                    jsAgencyInfo
            );
            if (crSyncAgencyInfo.failed()) {
                this.agencyDB.syncAgencyInfoFail(
                        request.getId(),
                        crSyncAgencyInfo.getMessage(),
                        1
                );
            } else {
                this.agencyDB.syncAgencyInfoSuccess(
                        request.getId()
                );

                this.bravoService.syncAgencyMembership(
                        request.getId(),
                        MembershipType.THANH_VIEN.getKey(),
                        code
                );
            }

            /**
             * Cập nhật lại dept_order_date
             */
            this.agencyDB.saveLockCheckDate(request.getId(), DateTimeUtils.getNow());

            /**
             * Duyệt đại lý mặc định ẩn CSBH/CTKM/CTSS/CTTL và giá liên hệ
             */
//            this.agencyDB.blockAll(request.getId());

            /**
             * Lưu lịch sử cấp bậc gốc
             */
            this.agencyDB.saveAgencyMembershipHistoryBySource(
                    request.getId(),
                    MembershipType.THANH_VIEN.getKey(),
                    0,
                    code,
                    "",
                    0,
                    "GOC"
            );

            /**
             * Duyệt đại lý tự phát sinh nickname
             * nguyên tắc: Cxxxx - xxxx là số thứ tự 4 chữ số
             */
            this.agencyDB.createNickname(request.getId(),
                    "C" + String.format("%04d", request.getId()));

            boolean rsAddAgencyToCTXH = this.addAgencyToCTXH(request.getId());

            JSONObject data = new JSONObject();
            data.put("waiting_approve_ctxh", rsAddAgencyToCTXH == false ? 1 : 0);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return new ClientResponse();
    }

    private void insertDeptSettingDefault(int agency_id) {
        try {
            DeptSettingEntity deptSettingEntity = new DeptSettingEntity();
            deptSettingEntity.setAgency_include("[\"" + agency_id + "\"]");
            deptSettingEntity.setAgency_id(agency_id);
            deptSettingEntity.setDept_cycle(
                    ConvertUtils.toInt(this.dataManager.getConfigManager().getMPConfigData().get(ConfigConstants.DEPT_CYCLE_DEFAULT)));
            deptSettingEntity.setDept_limit(
                    ConvertUtils.toLong(this.dataManager.getConfigManager().getMPConfigData().get(ConfigConstants.DEPT_LIMIT_DEFAULT)));
            deptSettingEntity.setNgd_limit(0L);
            deptSettingEntity.setStatus(DeptSettingStatus.CONFIRMED.getId());
            deptSettingEntity.setCreated_date(DateTimeUtils.getNow());
            deptSettingEntity.setModified_date(DateTimeUtils.getNow());
            deptSettingEntity.setStart_date(DateTimeUtils.getNow());
            deptSettingEntity.setConfirmed_date(DateTimeUtils.getNow());
            deptSettingEntity.setEnd_date(null);
            int rsInsert = this.deptDB.insertDeptSetting(deptSettingEntity);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private boolean addAgencyToCTXH(int agency_id) {
        boolean is_error = false;
        List<JSONObject> promoDateJoinList = this.promoDB.getListCTXHRunningByAgency(
                agency_id, CircleType.DATE.getCode());
        if (promoDateJoinList.size() == 0) {
            List<Program> ctxhDateList = this.dataManager.getListProgramCanJoin(
                    this.dataManager.getProgramManager().getAgency(agency_id),
                    CircleType.DATE);
            if (ctxhDateList.size() > 1) {
                is_error = true;
            } else if (ctxhDateList.size() == 1) {
                this.addAgencyToCTXHRunning(agency_id, ctxhDateList.get(0).getId());
            }
        }

        List<JSONObject> promoYearJoinList = this.promoDB.getListCTXHRunningByAgency(
                agency_id, CircleType.YEAR.getCode());
        if (promoYearJoinList.size() == 0) {
            List<Program> ctxhYearList = this.dataManager.getListProgramCanJoin(
                    this.dataManager.getProgramManager().getAgency(agency_id),
                    CircleType.YEAR);
            if (ctxhYearList.size() > 1) {
                is_error = true;
            } else if (ctxhYearList.size() == 1) {
                this.addAgencyToCTXHRunning(agency_id, ctxhYearList.get(0).getId());
            }
        }
        return !is_error;
    }

    private void addAgencyToCTXHRunning(int agencyId, int promo_id) {
        try {
            JSONObject ctxh_running = this.promoDB.getCTXHRunningJs(promo_id);
            if (ctxh_running == null) {
                this.alertToTelegram(
                        "[addAgencyToCTXHRunning] CTXH: " + promo_id + "",
                        ResponseStatus.FAIL);
                return;
            }

            List<Integer> agencyList = JsonUtils.DeSerialize(ctxh_running.get("agency_data").toString(),
                    new TypeToken<List<Integer>>() {
                    }.getType());
            agencyList.add(agencyId);
            boolean rs = this.promoDB.updateAgencyDataForCTXH(
                    promo_id,
                    JsonUtils.Serialize(agencyList));
            if (!rs) {
                this.alertToTelegram(
                        "[addAgencyToCTXHRunning] CTXH-" + promo_id + ", AGENCY-" + agencyId,
                        ResponseStatus.FAIL);
            }

            this.promoDB.insertBXHAgencyJoin(agencyId, promo_id);

            this.dataManager.getProgramManager().reloadCTXHRunning(
                    promo_id,
                    PromoScheduleType.START,
                    PromoType.BXH.getCode()
            );
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    private void createAddressDeliveryDefault(Agency agency) {
        try {
            /**
             * Địa chỉ nhận hàng
             */
            List<JSONObject> addressList = this.agencyDB.getListAddressDelivery(agency.getId());
            if (addressList.size() <= 0) {
                AddAddressDeliveryRequest addAddressDeliveryRequest = new AddAddressDeliveryRequest();
                addAddressDeliveryRequest.setAgency_id(agency.getId());
                addAddressDeliveryRequest.setAddress(
                        agency.getAddress() + ", " +
                                this.dataManager.getProductManager().getWardNameById(agency.getWardId()) + ", " +
                                this.dataManager.getProductManager().getDistrictNameById(agency.getDistrictId()) + ", " +
                                this.dataManager.getProductManager().getCityNameById(agency.getCityId()) + " ");
                addAddressDeliveryRequest.setPhone(agency.getPhone());
                addAddressDeliveryRequest.setFull_name(agency.getFullName());
                addAddressDeliveryRequest.setIs_default(1);
                this.agencyDB.addAddressDelivery(addAddressDeliveryRequest);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }

    public ClientResponse getAgencyInfo(SessionData sessionData, BasicRequest request) {
        try {
            JSONObject agency = agencyDB.getAgencyInfoById(request.getId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            if (!this.dataManager.getStaffManager().checkStaffManageAgency(
                    sessionData.getId(),
                    agency
            )) {
                return ClientResponse.fail(ResponseStatus.NOT_PERMISSION, ResponseMessage.USER_FORBIDDEN);
            }

            int status = ConvertUtils.toInt(agency.get("status"));
            if (status == AgencyStatus.WAITING_APPROVE.getValue()) {
                agency.put("membership_id", 0);
            }
            DeptAgencyInfoEntity deptAgencyInfoEntity = this.deptDB.getDeptAgencyInfo(request.getId());
            if (deptAgencyInfoEntity != null) {
                agency.put("commit_limit", deptAgencyInfoEntity.getCommit_limit());
            }
            agency.put("total_rank_value", this.agencyDB.getTotalRank());
            agency.put("dept_info", deptAgencyInfoEntity);
            agency.put("image_url", ImagePath.AGENCY.getImageUrl());
            agency.put("avatar_url", ImagePath.AVATAR.getImageUrl());


            JSONObject lock_info = this.getLockTime(
                    request.getId(),
                    ConvertUtils.toInt(agency.get("city_id")),
                    ConvertUtils.toInt(agency.get("region_id")),
                    ConvertUtils.toInt(agency.get("membership_id")),
                    ConvertUtils.toInt(agency.get("status")),
                    agency.get("dept_order_date") == null ? null :
                            AppUtils.convertJsonToDate(agency.get("dept_order_date")),
                    agency.get("lock_check_date") == null ? null :
                            AppUtils.convertJsonToDate(agency.get("lock_check_date")),
                    AppUtils.convertJsonToDate(agency.get("created_date")),
                    AppUtils.convertJsonToDate(agency.get("approved_date")),
                    AppUtils.convertJsonToDate(agency.get("locked_date"))
            );

            /**
             * Tạm thời trả về rỗng
             */
            agency.put("nick_name", "");

            /**
             * Cần duyệt catalog
             */
            agency.put("waiting_approve_catalog", this.checkWaitingApproveCatalog(request.getId()));

            /*Mission info*/
            JSONObject mission_info = this.getMissionInfo(request.getId());

            JSONObject data = new JSONObject();
            data.put("agency", agency);
            data.put("lock_info", lock_info);
            data.put("mission_info", mission_info);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private JSONObject getMissionInfo(int agency_id) {
        try {
            JSONObject agency_mission_point = this.agencyDB.getMissionPoint(agency_id);
            JSONObject mission_info = new JSONObject();
            mission_info.put("point", agency_mission_point == null ? 0 : ConvertUtils.toLong(agency_mission_point.get("point")));
            return mission_info;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    private int checkWaitingApproveCTXH(int agency_id) {
        try {
            return 0;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return 0;
    }

    private int checkWaitingApproveCatalog(int agency_id) {
        try {
            List<JSONObject> catalogWaitingApproveList = this.agencyDB.getCatalogWaitingApprove(agency_id);
            if (catalogWaitingApproveList.isEmpty()) {
                return 0;
            }
            return 1;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return 0;
    }

    public ClientResponse getListAddressDelivery(BasicRequest request) {
        try {
            List<JSONObject> rsListAddressDelivery = this.agencyDB.getListAddressDelivery(request.getId());
            JSONObject data = new JSONObject();
            data.put("addresses", rsListAddressDelivery);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.AGENCY.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse addAddressDelivery(AddAddressDeliveryRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            Agency agency = agencyDB.getAgencyById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            AgencyAddressDeliveryEntity rsAgencyAddressDeliveryDefault = this.agencyDB.getAgencyDeliveryDefault(request.getAgency_id());
            int rsAddAddressDelivery = this.agencyDB.addAddressDelivery(request);
            if (rsAddAddressDelivery > 0) {
                /**
                 * Set địa chỉ mặc định
                 */
                if (rsAgencyAddressDeliveryDefault == null) {
                    if (request.getIs_default() == 0) {
                        this.agencyDB.setAgencyAddressDeliveryDefault(rsAddAddressDelivery);
                    }
                } else if (request.getIs_default() == 1) {
                    this.agencyDB.setAgencyAddressDeliveryNotDefault(ConvertUtils.toInt(rsAgencyAddressDeliveryDefault.getId()));
                }

                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.AGENCY.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editAddressDelivery(EditAddressDeliveryRequest request) {
        try {
            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }

            Agency agency = agencyDB.getAgencyById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            AgencyAddressDeliveryEntity defaultAgencyAddressDeliveryEntity = this.agencyDB.getAgencyDeliveryDefault(request.getAgency_id());
            AgencyAddressDeliveryEntity agencyAddressDeliveryEntity = this.agencyDB.getAddressDeliveryDetail(request.getId());

            if (request.getIs_default() == 0
                    && agencyAddressDeliveryEntity.getIs_default() == 1
            ) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_ADDRESS_DELIVERY_IS_DEFAULT);
            }

            boolean rsAddAddressDelivery = this.agencyDB.editAddressDelivery(request);
            if (rsAddAddressDelivery) {
                /**
                 *  mặc định
                 */
                if (request.getIs_default() == 1 && agencyAddressDeliveryEntity.getIs_default() == 0) {
                    this.agencyDB.setAgencyAddressDeliveryNotDefault(defaultAgencyAddressDeliveryEntity.getId());
                }

                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.AGENCY.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getListAddressExportBilling(BasicRequest request) {
        try {
            List<JSONObject> rsListAddressExportBilling = this.agencyDB.getListAddressExportBilling(request.getId());
            JSONObject data = new JSONObject();
            data.put("accounts", rsListAddressExportBilling);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("AGENCY", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse addAddressExportBilling(AddAddressExportBillingRequest request) {
        try {
            Agency agency = agencyDB.getAgencyById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            /**
             * Set địa chỉ mặc định đối với đại lý chưa có địa chỉ mặc định
             */
            AddressExportBillingEntity jsAgencyAddressExportDefault = this.agencyDB.getAgencyAddressExportDefault(request.getAgency_id());

            int rsAddAddressExportBilling = this.agencyDB.addAddressExportBilling(request);
            if (rsAddAddressExportBilling > 0) {
                /**
                 * Set địa chỉ mặc định
                 */
                if (jsAgencyAddressExportDefault == null) {
                    if (request.getIs_default() == 0) {
                        this.agencyDB.setAgencyAddressExportDefault(rsAddAddressExportBilling);
                    }
                } else if (request.getIs_default() == 1) {
                    this.agencyDB.setAgencyAddressExportNotDefault(ConvertUtils.toInt(jsAgencyAddressExportDefault.getId()));
                }

                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editAddressExportBilling(EditAddressExportBillingRequest request) {
        try {
            Agency agency = agencyDB.getAgencyById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            AddressExportBillingEntity addressExportBillingEntity = this.agencyDB.getAddressExportBillingDetail(request.getId());
            if (addressExportBillingEntity == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            if (request.getIs_default() == 0 && addressExportBillingEntity.getIs_default() == 1) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            AddressExportBillingEntity defaultAddress = this.agencyDB.getAgencyAddressExportDefault(addressExportBillingEntity.getAgency_id());

            ClientResponse clientResponse = this.validateEditAddressExportBilling(request);

            boolean rsEditAddressExportBilling = this.agencyDB.editAddressExportBilling(request);
            if (rsEditAddressExportBilling) {
                /**
                 *  mặc định
                 */
                if (request.getIs_default() == 1 && addressExportBillingEntity.getIs_default() == 0) {
                    this.agencyDB.setAgencyAddressExportNotDefault(defaultAddress.getId());
                }
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse validateEditAddressExportBilling(EditAddressExportBillingRequest request) {
        return ClientResponse.success(null);
    }

    public ClientResponse deleteAddressDelivery(BasicRequest request) {
        try {
            AgencyAddressDeliveryEntity rsAddressDelivery = this.agencyDB.getAddressDeliveryDetail(request.getId());
            if (rsAddressDelivery == null) {
                ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ADDRESS_INVALID);
            }

            if (rsAddressDelivery.getIs_default() == 1) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_ADDRESS_DELIVERY_IS_DEFAULT);
            }

            boolean rsDeleteAddressDelivery = this.agencyDB.updateStatusAddressDelivery(request.getId(), ActiveStatus.INACTIVE.getValue());
            if (rsDeleteAddressDelivery) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {

        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deleteAddressExportBilling(BasicRequest request) {
        try {
            AddressExportBillingEntity rsAddressExportBilling = this.agencyDB.getAddressExportBillingDetail(request.getId());
            if (rsAddressExportBilling == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.ADDRESS_INVALID);
            }

            if (rsAddressExportBilling.getIs_default() == 1) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_ADDRESS_EXPORT_IS_DEFAULT);
            }

            boolean rsDeleteAddressExportBilling = this.agencyDB.updateStatusAddressExportDelivery(request.getId(), ActiveStatus.INACTIVE.getValue());
            if (rsDeleteAddressExportBilling) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {

        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse updateStatusAgency(SessionData sessionData, UpdateStatusRequest request) {
        try {
            Staff staff = this.dataManager.getStaffManager().getStaff(sessionData.getId());
            if (staff == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.USER_FORBIDDEN);
            }
            AgencyInfoResponse agencyInfoResponse = new AgencyInfoResponse();
            Agency agency = agencyDB.getAgencyById(request.getId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            if (request.getStatus() == AgencyStatus.APPROVED.getValue()) {
                return this.approveAgency(sessionData, request);
            }
            if (request.getStatus() == AgencyStatus.LOCK.getValue()) {
                return this.approveAgency(sessionData, request);
            }
            if (request.getStatus() == AgencyStatus.INACTIVE.getValue()) {
                return this.approveAgency(sessionData, request);
            }

            /**
             * validate update status
             */
            if (agency.getStatus() == request.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            boolean rsUpdateStatus = this.agencyDB.updateStatusAgency(request.getId(), request.getStatus());
            if (rsUpdateStatus) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {

        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse lockAgency(SessionData sessionData, UpdateStatusRequest request) {
        try {
            Agency agency = agencyDB.getAgencyById(request.getId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            /**
             * validate update status
             */
            if (agency.getStatus() == request.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            boolean rsUpdateStatus = this.agencyDB.updateStatusAgency(request.getId(), AgencyStatus.LOCK.getValue());
            if (!rsUpdateStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rsLockedDate = this.agencyDB.saveLockedDate(request.getId());

            /**
             * Lưu lịch sử
             */
            /**
             * Lưu lịch sử
             */
            this.agencyDB.saveAgencyLockHistory(
                    request.getId(),
                    sessionData.getId(),
                    "",
                    2
            );

            return ClientResponse.success(null);
        } catch (Exception ex) {

        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse unlockAgency(UpdateStatusRequest request) {
        try {
            Agency agency = agencyDB.getAgencyById(request.getId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            /**
             * validate update status
             */
            if (agency.getStatus() == request.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            boolean rsUpdateStatus = this.agencyDB.updateStatusAgency(request.getId(), AgencyStatus.APPROVED.getValue());
            if (!rsUpdateStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Cập nhật lại dept_order_date
             */
            this.agencyDB.resetLockCheckDate(request.getId());

            return ClientResponse.success(null);
        } catch (Exception ex) {

        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse deactivateAgency(UpdateStatusRequest request) {
        try {
            Agency agency = agencyDB.getAgencyById(request.getId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            /**
             * validate update status
             */
            if (agency.getStatus() != AgencyStatus.APPROVED.getValue()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            boolean rsUpdateStatus = this.agencyDB.updateStatusAgency(request.getId(), AgencyStatus.INACTIVE.getValue());
            if (rsUpdateStatus) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {

        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse activeAgency(UpdateStatusRequest request) {
        try {
            Agency agency = agencyDB.getAgencyById(request.getId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            /**
             * validate update status
             */
            if (agency.getStatus() == request.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            boolean rsUpdateStatus = this.agencyDB.updateStatusAgency(request.getId(), AgencyStatus.APPROVED.getValue());
            if (!rsUpdateStatus) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Bổ sung đại lý vào CTXH nếu có
             */
            boolean rsAddAgencyToCTXH = this.addAgencyToCTXH(request.getId());

            JSONObject data = new JSONObject();
            data.put("waiting_approve_ctxh", rsAddAgencyToCTXH == false ? 1 : 0);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("AGENCY", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse addNewAgencyAccount(AddNewAgencyAccountRequest request) {
        try {
            ClientResponse clientResponse = this.validateAddNewAgencyAccount(request);

            Agency agency = this.agencyDB.getAgencyById(request.getAgencyId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            JSONObject jsAccountWithUsername = this.agencyDB.getAgencyAccountByPhone(request.getUsername());
            if (jsAccountWithUsername != null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PHONE_USED);
            }

            JSONObject rsAccountPrimary = this.agencyDB.getAgencyAccountMain(request.getAgencyId());
            /**
             * Nếu đã tồn tại tài khoản chỉnh
             * cập nhật tk chính cũ thành tk phụ
             */
            if (rsAccountPrimary != null) {
                if (request.getIsPrimary() == 1) {
                    boolean rsUpdateAccountPrimary = this.agencyDB.setAgencyAccountSub(ConvertUtils.toInt(rsAccountPrimary.get("id")));
                    if (!rsUpdateAccountPrimary) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }
            }

            AgencyAccount account = new AgencyAccount();
            account.setAgencyId(request.getAgencyId());
            account.setFullName(request.getFullName());
            account.setAgencyPhone(agency.getPhone());
            account.setUsername(request.getUsername());
            account.setPassword(this.appUtils.md5(request.getPassword()));
            account.setStatus(request.getStatus());
            account.setIsPrimary(request.getIsPrimary());
            int rsAddNewAgencyAccount = this.agencyDB.addAgencyAccount(account);
            if (rsAddNewAgencyAccount > 0) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("AGENCY", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ClientResponse validateAddNewAgencyAccount(AddNewAgencyAccountRequest request) {
        if (request.getAgencyId() == 0) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
        }

        if (!appUtils.checkPassword(request.getPassword())) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PASSWORD_INVALID);
        }

        /**
         * check phone available
         */
        if (this.validateUtils.checkPhoneExistInAgencyAccount(
                this.agencyDB.getAgencyAccountByPhone(request.getUsername()))) {
            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PHONE_USED);
        }

        return ClientResponse.success(null);
    }


    public ClientResponse getListAgencyAccount(BasicRequest request) {
        try {
            List<JSONObject> rsListAgencyAccount = this.agencyDB.getListAgencyAccount(request.getId());
            JSONObject data = new JSONObject();
            data.put("accounts", rsListAgencyAccount);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("AGENCY", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getAddressDeliveryDetail(BasicRequest request) {
        try {
            AgencyAddressDeliveryEntity rsAddressDeliveryDetail = this.agencyDB.getAddressDeliveryDetail(request.getId());
            JSONObject data = new JSONObject();
            data.put("address", rsAddressDeliveryDetail);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("AGENCY", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getAddressExportBillingDetail(BasicRequest request) {
        try {
            AddressExportBillingEntity rsAddressExportBillingDetail = this.agencyDB.getAddressExportBillingDetail(request.getId());
            JSONObject data = new JSONObject();
            data.put("address", rsAddressExportBillingDetail);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("AGENCY", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getAgencyAccountDetail(BasicRequest request) {
        try {
            JSONObject rsAgencyAccountDetail = this.agencyDB.getAgencyAccountDetail(request.getId());
            JSONObject data = new JSONObject();
            data.put("account", rsAgencyAccountDetail);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("AGENCY", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse editAgencyAccount(EditAgencyAccountRequest request) {
        try {
            JSONObject jsAgencyAccount = this.agencyDB.getAgencyAccountDetail(request.getId());
            if (jsAgencyAccount == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            /**
             * Tài khoản đang hoạt động
             * không chỉnh sửa username và password
             */
            AgencyAccount account = JsonUtils.DeSerialize(JsonUtils.Serialize(jsAgencyAccount), AgencyAccount.class);
            if (account.getStatus() == ActiveStatus.ACTIVATED.getValue()
                    && (!request.getUsername().equals(account.getUsername())
                    || (request.getPassword() != null && !this.appUtils.md5(request.getPassword()).equals(account.getPassword()
            ))
            )) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.CANNOT_EDIT_ACCOUNT_IS_ACTIVE);
            }

            ClientResponse clientResponse = request.validate();
            if (clientResponse.failed()) {
                return clientResponse;
            }


            /**
             * Account username exist
             */
            JSONObject jsAccountWithUsername = this.agencyDB.getAgencyAccountByPhone(request.getUsername());
            if (jsAccountWithUsername != null && request.getId() != ConvertUtils.toInt(jsAccountWithUsername.get("id"))) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.PHONE_USED);
            }


            /**
             * Cannot set Primary to Sub
             */
            if (request.getIsPrimary() == AccountType.SUB.getValue() && account.getIsPrimary() == AccountType.PRIMARY.getValue()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_ACCOUNT_CANNOT_MISS_PRIMARY);
            }

            account.setFullName(request.getFullName());
            account.setUsername(request.getUsername());
            if (request.getPassword() != null && !this.appUtils.md5(request.getPassword()).equals(request.getPassword())) {
                account.setForceUpdateStatus(ForceUpdateStatus.FORCE_LOGOUT.getValue());
                account.setPassword(this.appUtils.md5(request.getPassword()));
            }

            if (request.getIsPrimary() == AccountType.PRIMARY.getValue() && account.getIsPrimary() == AccountType.SUB.getValue()) {
                JSONObject rsAccountPrimary = this.agencyDB.getAgencyAccountMain(account.getAgencyId());
                /**
                 * Nếu chuyển đổi tài khoản chính
                 * cập nhật tk chính cũ thành tk phụ
                 */
                if (rsAccountPrimary != null) {
                    boolean rsUpdateAccountSub = this.agencyDB.setAgencyAccountSub(ConvertUtils.toInt(rsAccountPrimary.get("id")));
                    if (!rsUpdateAccountSub) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }
            }

            account.setStatus(request.getStatus());
            account.setIsPrimary(request.getIsPrimary());
            boolean rsEditAgencyAccount = this.agencyDB.editAgencyAccount(account);
            if (rsEditAgencyAccount) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {
            LogUtil.printDebug("AGENCY", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse setAgencyAccountMain(BasicRequest request) {
        try {
            JSONObject jsAgencyAccount = this.agencyDB.getAgencyAccountDetail(request.getId());
            if (jsAgencyAccount == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            JSONObject jsAgencyAccountMain = this.agencyDB.getAgencyAccountMain(ConvertUtils.toInt(jsAgencyAccount.get("agency_id")));
            if (ConvertUtils.toInt(jsAgencyAccountMain.get("id")) == request.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_ACCOUNT_IS_MAIN);
            }

            boolean rsSetAgencyAccountSub = this.agencyDB.setAgencyAccountSub(ConvertUtils.toInt(jsAgencyAccountMain.get("id")));
            if (!rsSetAgencyAccountSub) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rsSetAgencyAccountMain = this.agencyDB.setAgencyAccountMain(request.getId());
            if (rsSetAgencyAccountMain) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {

        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse updateStatusAgencyAccount(UpdateStatusRequest request) {
        try {
            JSONObject jsAgencyAccount = this.agencyDB.getAgencyAccountDetail(request.getId());
            if (jsAgencyAccount == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            /**
             * validate update status
             */
            if (ConvertUtils.toInt(jsAgencyAccount.get("status")) == request.getStatus()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsUpdateStatusAgencyAccount = this.agencyDB.updateStatusAgencyAccount(request.getId(), request.getStatus());
            if (rsUpdateStatusAgencyAccount) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {

        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse setAddressExportBillingDefault(BasicRequest request) {
        try {
            AddressExportBillingEntity jsAddress = this.agencyDB.getAddressExportBillingDetail(request.getId());
            if (jsAddress == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            AddressExportBillingEntity jsAgencyAddressExportDefault = this.agencyDB.getAgencyAddressExportDefault(jsAddress.getAgency_id());
            if (jsAgencyAddressExportDefault.getId() == request.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_ADDRESS_EXPORT_IS_DEFAULT);
            }

            boolean rsAgencyAddressExportNotDefault = this.agencyDB.setAgencyAddressExportNotDefault(jsAgencyAddressExportDefault.getId());
            if (!rsAgencyAddressExportNotDefault) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rsSetAgencyAccountMain = this.agencyDB.setAgencyAddressExportDefault(request.getId());
            if (rsSetAgencyAccountMain) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {

        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse setAddressDeliveryDefault(BasicRequest request) {
        try {
            AgencyAddressDeliveryEntity jsAddress = this.agencyDB.getAddressDeliveryDetail(request.getId());
            if (jsAddress == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            AgencyAddressDeliveryEntity jsAgencyAddressDeliveryDefault = this.agencyDB.getAgencyDeliveryDefault(jsAddress.getAgency_id());
            if (jsAgencyAddressDeliveryDefault.getId() == request.getId()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_ADDRESS_DELIVERY_IS_DEFAULT);
            }

            boolean rsAgencyAddressDeliveryNotDefault = this.agencyDB.setAgencyAddressDeliveryNotDefault(jsAgencyAddressDeliveryDefault.getId());
            if (!rsAgencyAddressDeliveryNotDefault) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            boolean rsSetAgencyAccountMain = this.agencyDB.setAgencyAddressDeliveryDefault(request.getId());
            if (rsSetAgencyAccountMain) {
                return ClientResponse.success(null);
            }
        } catch (Exception ex) {

        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse updateAgencyContractInfo(UpdateAgencyContractInfoRequest request) {
        try {
            Agency agency = this.agencyDB.getAgencyById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            boolean rsEditAgencyContractInfo = this.agencyDB.editAgencyContractInfo(request.getAgency_id(), request.getTax_number());
            if (!rsEditAgencyContractInfo) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("AGENCY", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getAgencyContractInfo(BasicRequest request) {
        try {
            AgencyContractInfoEntity rsAgencyContractInfo = this.agencyDB.getAgencyContractInfoByAgencyId(request.getId());
            JSONObject data = new JSONObject();
            data.put("agency_contract_info", rsAgencyContractInfo);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("AGENCY", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterAgencyMemberShipHistory(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_AGENCY_MEMBERSHIP_HISTORY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 1);
            for (JSONObject js : records) {
                js.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(js.get("agency_id"))
                ));
            }
            int total = this.agencyDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterAgencyAcoinHistory(FilterListRequest request) {
        try {
            String query = this.filterUtils.getQuery(FunctionList.LIST_AGENCY_ACOIN_HISTORY, request.getFilters(), request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.filter(query, this.appUtils.getOffset(request.getPage()), ConfigInfo.PAGE_SIZE, 1);
            for (JSONObject js : records) {
                js.put("agency_info", this.dataManager.getAgencyManager().getAgencyBasicData(
                        ConvertUtils.toInt(js.get("agency_id"))
                ));
            }
            int total = this.agencyDB.getTotal(query);
            data.put("records", records);
            data.put("total", total);
            data.put("total_page", this.appUtils.getTotalPage(total));
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse approveAgencyAll() {
        try {
            List<JSONObject> all = this.agencyDB.getAllAgency();
            for (JSONObject one : all) {
                int agency_id = ConvertUtils.toInt(one.get("id"));

                Agency agency = agencyDB.getAgencyById(agency_id);
                if (agency == null) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
                }

                ClientResponse clientResponse = this.validateApproveAgency(agency);
                if (clientResponse.failed()) {
                    return clientResponse;
                }

                /**
                 * phòng kinh doanh
                 */
                if (agency.getBusinessDepartmentId() == null || agency.getBusinessDepartmentId() == 0) {
                    /**
                     * Chuyền tình thành cần cập nhật lại vùng kinh doanh
                     */
                    Integer businessDepartmentId = this.agencyDB.getBusinessDepartmentIdByRegionId(agency.getRegionId());
                    /**
                     * Nếu vùng chưa phân bổ vào phòng kinh doanh
                     */
                    if (businessDepartmentId == null) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.REGION_MISS_BUSINESS_DEPARTMENT);
                    } else {
                        boolean rsSetBusinessDepartment = this.agencyDB.setBusinessDepartment(agency.getId(), businessDepartmentId);
                        if (!rsSetBusinessDepartment) {
                            return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                        }
                    }
                }

                /**
                 * tạo mã cho đại lý
                 */
                String code = this.generateAgencyCode(
                        agency.getRegionId(),
                        agency.getCityId(),
                        agency.getMembershipId(),
                        agency.getId());
                if (!this.validateUtils.checkValidateAgencyCode(code)) {
                    code = this.generateAgencyCode(agency.getRegionId(), agency.getCityId(), agency.getMembershipId(), agency.getId());
                }

                boolean rsUpdateCode = this.agencyDB.updateCode(agency.getId(), code);
                if (!rsUpdateCode) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * cập nhật trạng thái đại lý thành đã duyệt
                 * trạng thái: APPROVED
                 * ngày duyệt
                 */
                boolean rsApproveAgency = this.agencyDB.approveAgency(agency.getId());
                if (!rsApproveAgency) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                }

                /**
                 * Tạo dữ liệu cho công nợ
                 */
                this.initDeptAgencyInfo(agency.getId());
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return new ClientResponse();
    }

    public ClientResponse blockCSBH(SessionData sessionData, BlockAgencyRequest request) {
        try {
            JSONObject agency = this.agencyDB.getAgencyInfoById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            int status = ConvertUtils.toInt(agency.get("status"));
            if (AgencyStatus.WAITING_APPROVE.getValue() == status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            int block_csbh = ConvertUtils.toInt(agency.get("block_csbh"));
            if (block_csbh == request.getBlock()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsBlock = this.agencyDB.blockCSBH(request.getAgency_id(), request.getBlock());
            if (!rsBlock) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse blockCTKM(SessionData sessionData, BlockAgencyRequest request) {
        try {
            JSONObject agency = this.agencyDB.getAgencyInfoById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            int status = ConvertUtils.toInt(agency.get("status"));
            if (AgencyStatus.WAITING_APPROVE.getValue() == status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            int block_ctkm = ConvertUtils.toInt(agency.get("block_ctkm"));
            if (block_ctkm == request.getBlock()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsBlock = this.agencyDB.blockCTKM(request.getAgency_id(), request.getBlock());
            if (!rsBlock) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse blockCTSN(SessionData sessionData, BlockAgencyRequest request) {
        try {
            JSONObject agency = this.agencyDB.getAgencyInfoById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            int status = ConvertUtils.toInt(agency.get("status"));
            if (AgencyStatus.WAITING_APPROVE.getValue() == status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            int block_ctsn = ConvertUtils.toInt(agency.get("block_ctsn"));
            if (block_ctsn == request.getBlock()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsBlock = this.agencyDB.blockCTSN(request.getAgency_id(), request.getBlock());
            if (!rsBlock) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse blockPrice(SessionData sessionData, BlockAgencyRequest request) {
        try {
            JSONObject agency = this.agencyDB.getAgencyInfoById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            int status = ConvertUtils.toInt(agency.get("status"));
            if (AgencyStatus.WAITING_APPROVE.getValue() == status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            int block_price = ConvertUtils.toInt(agency.get("block_price"));
            if (block_price == request.getBlock()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsBlock = this.agencyDB.blockPrice(request.getAgency_id(), request.getBlock());
            if (!rsBlock) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse getListStaffManageAgency(BasicRequest request) {
        try {
            JSONObject agency = this.dataManager.getAgencyManager().getAgencyInfo(
                    request.getId()
            );
            int staff_manage_id = ConvertUtils.toInt(agency.get("staff_manage_id"));
            int staff_support_id = ConvertUtils.toInt(agency.get("staff_support_id"));
            List<JSONObject> records = this.staffDB.getAllStaffActive();
            List<JSONObject> staffList = new ArrayList<>();
            for (JSONObject record : records) {
                int staff_group_permission_id = ConvertUtils.toInt(record.get("staff_group_permission_id"));
                if (!this.dataManager.getStaffManager().checkGroupFullPermission(staff_group_permission_id) &&
                        this.dataManager.getStaffManager().checkStaffManageAgency(
                                record,
                                agency)) {
                    record.put("is_manage", staff_manage_id == ConvertUtils.toInt(record.get("id")));
                    record.put("is_support", staff_support_id == ConvertUtils.toInt(record.get("id")));
                    staffList.add(record);
                }
            }
            JSONObject data = new JSONObject();
            data.put("records", staffList);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.STAFF.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse settingDttLimit(SessionData sessionData, SettingDttLimitRequest request) {
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse syncAgencyToBravo(SessionData sessionData, BasicRequest request) {
        try {

            JSONObject agency = this.agencyDB.getAgencyInfoById(request.getId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            agency.put("full_address", this.dataManager.getProductManager().getFullAddress(agency));

            /**
             * Đồng bộ qua Bravo
             */
            if (ConvertUtils.toInt(agency.get("sync_type")) == 1) {
                ClientResponse crSyncAgencyInfo = this.bravoService.syncAgencyInfo(
                        agency
                );
                if (crSyncAgencyInfo.failed()) {
                    this.agencyDB.syncAgencyInfoFail(
                            request.getId(),
                            crSyncAgencyInfo.getMessage(),
                            1
                    );

                    return crSyncAgencyInfo;
                } else {
                    this.agencyDB.syncAgencyInfoSuccess(
                            request.getId()
                    );
                }
            } else {
                ClientResponse crSync = this.bravoService.syncAgencyMembership(
                        ConvertUtils.toInt(agency.get("id")),
                        ConvertUtils.toInt(agency.get("membership_id")),
                        ConvertUtils.toString(agency.get("code")));
                if (crSync.failed()) {
                    this.agencyDB.syncAgencyInfoFail(
                            request.getId(),
                            crSync.getMessage(),
                            2
                    );
                    return crSync;
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.STAFF.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse blockCTTL(SessionData sessionData, BlockAgencyRequest request) {
        try {
            JSONObject agency = this.agencyDB.getAgencyInfoById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            int status = ConvertUtils.toInt(agency.get("status"));
            if (AgencyStatus.WAITING_APPROVE.getValue() == status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            int block_cttl = ConvertUtils.toInt(agency.get("block_cttl"));
            if (block_cttl == request.getBlock()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsBlock = this.agencyDB.blockCTTL(request.getAgency_id(), request.getBlock());
            if (!rsBlock) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse filterListAgency(SessionData sessionData, FilterListAgencyRequest request) {
        try {

            int hasFilter = 0;
            if (!request.getCity_data().isEmpty() ||
                    !request.getRegion_data().isEmpty() ||
                    !request.getMembership_data().isEmpty()) {
                hasFilter = 1;
            }
            List<JSONObject> records = this.agencyDB.filterAgency(
                    JsonUtils.Serialize(request.getAgency_include_data()),
                    JsonUtils.Serialize(request.getAgency_ignore_data()),
                    JsonUtils.Serialize(request.getCity_data()),
                    JsonUtils.Serialize(request.getRegion_data()),
                    JsonUtils.Serialize(request.getMembership_data()),
                    hasFilter
            );
            JSONObject data = new JSONObject();
            data.put("records", records);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.STAFF.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public JSONObject getLockTime(
            int agency_id,
            int city_id,
            int region_id,
            int membership_id,
            int status,
            Date dept_order_date,
            Date lock_check_date,
            Date created_date,
            Date approved_date,
            Date locked_date
    ) {
        try {
            if (lock_check_date == null) {
                lock_check_date = this.createLockCheckDate(
                        agency_id,
                        created_date,
                        approved_date
                );
            }

            if (status == AgencyStatus.LOCK.getValue()) {
                JSONObject lockTime = new JSONObject();
                lockTime.put("day_lock", null);
                lockTime.put("option_lock", null);
                lockTime.put("lock_check_date", null);
                lockTime.put("locked_date", locked_date);
                return lockTime;
            }

            JSONObject jsAgencyLockSetting = this.agencyDB.getAgencyLockData(
                    SettingObjectType.AGENCY.getCode(),
                    agency_id
            );
            if (jsAgencyLockSetting != null &&
                    ConvertUtils.toInt(jsAgencyLockSetting.get("status")) == LockDataStatus.RUNNING.getId()) {
                return convertLockTimeInfo(
                        agency_id,
                        jsAgencyLockSetting,
                        dept_order_date,
                        lock_check_date
                );
            }

            JSONObject jsCityLockSetting = this.agencyDB.getAgencyLockData(
                    SettingObjectType.CITY.getCode(),
                    city_id
            );
            if (jsCityLockSetting != null &&
                    ConvertUtils.toInt(jsCityLockSetting.get("status")) == LockDataStatus.RUNNING.getId()) {
                return convertLockTimeInfo(
                        agency_id,
                        jsCityLockSetting,
                        dept_order_date,
                        lock_check_date
                );
            }

            JSONObject jsRegionLockSetting = this.agencyDB.getAgencyLockData(
                    SettingObjectType.REGION.getCode(),
                    region_id
            );
            if (jsRegionLockSetting != null &&
                    ConvertUtils.toInt(jsRegionLockSetting.get("status")) == LockDataStatus.RUNNING.getId()) {
                return convertLockTimeInfo(
                        agency_id,
                        jsRegionLockSetting,
                        dept_order_date,
                        lock_check_date
                );
            }

            JSONObject jsMembershipLockSetting = this.agencyDB.getAgencyLockData(
                    SettingObjectType.MEMBERSHIP.getCode(),
                    membership_id
            );
            if (jsMembershipLockSetting != null &&
                    ConvertUtils.toInt(jsMembershipLockSetting.get("status")) == LockDataStatus.RUNNING.getId()) {
                return convertLockTimeInfo(
                        agency_id,
                        jsMembershipLockSetting,
                        dept_order_date,
                        lock_check_date
                );
            }

            JSONObject lockTime = new JSONObject();
            lockTime.put("day_lock", null);
            lockTime.put("option_lock", null);
            lockTime.put("lock_check_date", null);
            lockTime.put("dept_order_date", dept_order_date);
            return lockTime;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    private Date createLockCheckDate(
            int agency_id,
            Date created_date,
            Date approved_date) {
        try {
            JSONObject jsDeptOrder = this.deptDB.getLastDeptOrderDate(
                    agency_id
            );
            if (jsDeptOrder != null) {
                Date date = AppUtils.convertJsonToDate(
                        jsDeptOrder.get("created_date")
                );
                this.agencyDB.initNgayGhiNhanCongNo(agency_id, date);
                this.agencyDB.saveLockCheckDate(agency_id, date);
                return date;
            }

            Date now = DateTimeUtils.getNow();
            this.agencyDB.saveLockCheckDate(
                    agency_id,
                    now);
            return now;
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    public JSONObject convertLockTimeInfo(
            int agency_id,
            JSONObject setting,
            Date dept_order_date,
            Date lock_check_date
    ) {
        try {
            int option_lock = ConvertUtils.toInt(setting.get("option_lock"));
            if (option_lock == LockOptionType.KHONG_KHOA.getId()) {
                JSONObject lockTime = new JSONObject();
                lockTime.put("day_lock", 0);
                lockTime.put("option_lock", option_lock);
                lockTime.put("lock_check_date", lock_check_date);
                lockTime.put("dept_order_date", dept_order_date);
                return lockTime;
            } else if (option_lock == LockOptionType.KHOA_CUOI_NGAY.getId()) {
                JSONObject lockTime = new JSONObject();
                lockTime.put("day_lock", 0);
                lockTime.put("option_lock", option_lock);
                lockTime.put("lock_check_date", lock_check_date);
                lockTime.put("dept_order_date", dept_order_date);
                lockTime.put("time", this.appUtils.getDateAfterDay(
                        DateTimeUtils.getDateTime(
                                DateTimeUtils.getNow("yyyy-MM-dd"),
                                "yyyy-MM-dd"),
                        1)
                );
                return lockTime;
            } else if (option_lock == LockOptionType.KHOA_N_NGAY.getId()) {
                int day_lock = ConvertUtils.toInt(setting.get("day_lock"));

                JSONObject lockTime = new JSONObject();
                lockTime.put("day_lock", day_lock);
                Date dateLock = DateTimeUtils.getDateTime(
                        DateTimeUtils.toString(
                                this.appUtils.getDateAfterDay(
                                        lock_check_date,
                                        day_lock + 1
                                ), "yyyy-MM-dd"
                        ), "yyyy-MM-dd"
                );

                Date endDateLock = this.appUtils.getDateAfterDay(
                        DateTimeUtils.getDateTime(
                                DateTimeUtils.getNow("yyyy-MM-dd"),
                                "yyyy-MM-dd"),
                        1);
                if (dateLock.before(endDateLock)) {
                    dateLock = endDateLock;
                }
                lockTime.put("time", dateLock);
                lockTime.put("option_lock", option_lock);
                lockTime.put("dept_order_date", dept_order_date);
                lockTime.put("lock_check_date", lock_check_date);
                return lockTime;
            } else {
                JSONObject lockTime = new JSONObject();
                lockTime.put("day_lock", null);
                lockTime.put("option_lock", null);
                lockTime.put("lock_check_date", null);
                lockTime.put("dept_order_date", dept_order_date);
                return lockTime;
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    public ClientResponse blockCTSS(SessionData sessionData, BlockAgencyRequest request) {
        try {
            JSONObject agency = this.agencyDB.getAgencyInfoById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            int status = ConvertUtils.toInt(agency.get("status"));
            if (AgencyStatus.WAITING_APPROVE.getValue() == status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            int block_ctss = ConvertUtils.toInt(agency.get("block_ctss"));
            if (block_ctss == request.getBlock()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsBlock = this.agencyDB.blockCTSS(request.getAgency_id(), request.getBlock());
            if (!rsBlock) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse blockCSDM(SessionData sessionData, BlockAgencyRequest request) {
        try {
            JSONObject agency = this.agencyDB.getAgencyInfoById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            int status = ConvertUtils.toInt(agency.get("status"));
            if (AgencyStatus.WAITING_APPROVE.getValue() == status) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            int block_csdm = ConvertUtils.toInt(agency.get("block_csdm"));
            if (block_csdm == request.getBlock()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }

            boolean rsBlock = this.agencyDB.blockCSDM(request.getAgency_id(), request.getBlock());
            if (!rsBlock) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse rejectAgency(SessionData sessionData, ApproveRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            JSONObject agency = this.agencyDB.getAgencyInfoById(request.getId());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }
            if (ConvertUtils.toInt(agency.get("status")) != AgencyStatus.WAITING_APPROVE.getValue()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            String phone_delete = ConvertUtils.toString(agency.get("phone"))
                    + "_" + request.getId();

            boolean rsReject = this.agencyDB.rejectAgency(
                    request.getId(),
                    request.getNote(),
                    phone_delete);
            if (!rsReject) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
            }

            /**
             * Xóa thông tin đăng nhập
             */
            List<JSONObject> accountList = this.agencyDB.getListAgencyAccount(request.getId());
            LogUtil.printDebug(JsonUtils.Serialize(accountList));
            this.agencyDB.deleteAgencyAccount(request.getId());
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse setStaffManageAgency(SessionData sessionData, SetStaffManageAgencyRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            JSONObject agency = this.agencyDB.getAgencyInfoById(request.getAgency_id());
            if (agency == null) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.AGENCY_NOT_FOUND);
            }

            int staff_manage_id = ConvertUtils.toInt(agency.get("staff_manage_id"));
            int staff_support_id = ConvertUtils.toInt(agency.get("staff_support_id"));

            if (!request.isRemove()) {
                /**
                 * Đang hỗ trợ thì không thể phụ trách
                 */
                if (request.getType() == 1 &&
                        request.getStaff_id() == staff_support_id
                ) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
                }

                /**
                 * Đang phụ trách thì không thể hỗ trợ
                 */
                if (request.getType() == 2 &&
                        request.getStaff_id() == staff_manage_id
                ) {
                    return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.INFO_INVALID);
                }

                if (request.getType() == 1) {
                    boolean rs = this.agencyDB.setStaffManageAgency(request.getAgency_id(), request.getStaff_id());
                    if (!rs) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                } else if (request.getType() == 2) {
                    boolean rs = this.agencyDB.setStaffSupportAgency(request.getAgency_id(), request.getStaff_id());
                    if (!rs) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }
            } else {
                if (request.getType() == 1) {
                    boolean rs = this.agencyDB.setStaffManageAgency(request.getAgency_id(), null);
                    if (!rs) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                } else if (request.getType() == 2) {
                    boolean rs = this.agencyDB.setStaffSupportAgency(request.getAgency_id(), null);
                    if (!rs) {
                        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
                    }
                }
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }

        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private List<Integer> getListAgencyByFilter(String promo_data) {
        List<Integer> agencyList = new ArrayList<>();

        Program program = this.dataManager.getProgramManager().importProgram(
                promo_data);
        if (program == null) {
            return agencyList;
        }

        List<com.app.server.data.dto.program.agency.Agency> jsAgencyList = this.dataManager.getProgramManager().getListAgencyReadyJoinCTXH();
        for (com.app.server.data.dto.program.agency.Agency agency : jsAgencyList) {
            DeptInfo deptInfo = this.dataManager.getProgramManager().getDeptInfo(
                    agency.getId());
            if (this.checkProgramFilter(
                    agency,
                    program,
                    Source.WEB,
                    deptInfo)) {
                agencyList.add(agency.getId());
            }
        }
        return agencyList;
    }

    private boolean checkProgramFilter(com.app.server.data.dto.program.agency.Agency agency, Program program, Source source, DeptInfo deptInfo) {
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