package com.app.server.service;

import com.app.server.constants.ResponseMessage;
import com.app.server.data.dto.agency.Membership;
import com.app.server.data.entity.AgencyAcoinHistoryEntity;
import com.app.server.data.entity.AgencyEntity;
import com.app.server.data.entity.DeptOrderEntity;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ResetService extends BaseService {
    public ClientResponse resetEndYear(int year
    ) {
        try {
            /**
             * Lưu lịch sử
             */
            List<JSONObject> agencyList = this.agencyDB.getAllAgency();
            for (JSONObject jsAgency : agencyList) {
                int agency_id = ConvertUtils.toInt(jsAgency.get("id"));
                this.resetEndYearOne(
                        year,
                        agency_id
                );
            }

            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse resetEndYearOne(int year, int agency_id
    ) {
        try {
            /**
             * Lưu lịch sử
             */
            JSONObject jsAgency = this.agencyDB.getAgencyInfo(agency_id);
            if (jsAgency == null ||
                    ConvertUtils.toInt(jsAgency.get("status")) == AgencyStatus.WAITING_APPROVE.getValue()) {
                return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.STATUS_NOT_MATCH);
            }
            JSONObject jsDept = this.deptDB.getDeptInfo(agency_id);
            long dtt = 0;
            long tt = 0;
            if (jsDept != null) {
                dtt = ConvertUtils.toLong(jsDept.get("total_dtt_cycle"));
                tt = ConvertUtils.toLong(jsDept.get("total_tt_cycle"));
            }


            /**
             * resetDTT
             */
            this.deptDB.resetDTT(agency_id);
            /**
             * Doanh Thu Thuần
             */
            this.saveAgencyDTTYear(year, agency_id, dtt);
            this.deptDB.saveDeptDttHistory(
                    agency_id,
                    0,
                    dtt,
                    0,
                    "Tính lại cuối năm",
                    0,
                    DateTimeUtils.toString(DateTimeUtils.getNow()),
                    "Tính lại cuối năm",
                    ""
            );

            /**
             * Tiền thu
             */
            /**
             * resetTT
             */
            this.deptDB.resetTT(agency_id);
            this.saveAgencyTTYear(year, agency_id, tt);

            /**
             * Acoin
             */
            /**
             * resetAcoin
             */
            int acoin = ConvertUtils.toInt(jsAgency.get("current_point"));
            this.agencyDB.resetAcoin(agency_id, acoin);
            this.saveAgencyAcoinYear(year, agency_id, acoin);
            this.saveAgencyAcoinHistory(
                    agency_id,
                    0,
                    acoin,
                    0,
                    "Tính lại cuối năm",
                    new Date()
            );

            /**
             * Membership
             */
            int current_membership_id = ConvertUtils.toInt(jsAgency.get("membership_id"));
            String current_code = ConvertUtils.toString(jsAgency.get("code"));
            int membership_cycle_start_id = ConvertUtils.toInt(jsAgency.get("membership_cycle_start_id"));
            String reward_membership = TransactionEffectValueType.NONE.getCode();
            int membership_cycle_end_id = this.tinhLaiCapBac(
                    current_membership_id,
                    tt
            );

            String new_code = current_code;
            if (membership_cycle_end_id != current_membership_id) {
                new_code = getNewAgencyCode(current_code, membership_cycle_end_id);

                reward_membership = membership_cycle_end_id > current_membership_id ?
                        TransactionEffectValueType.INCREASE.getCode() :
                        TransactionEffectValueType.DECREASE.getCode();
            }

            this.saveAgencyMembershipYear(
                    year,
                    agency_id,
                    current_membership_id,
                    membership_cycle_end_id,
                    new_code,
                    reward_membership
            );

            /**
             * reset Membership
             */
            this.agencyDB.resetMembership(agency_id, membership_cycle_end_id, new_code);
            this.agencyDB.saveAgencyMembershipHistoryBySource(
                    agency_id,
                    membership_cycle_end_id,
                    current_membership_id,
                    new_code,
                    current_code,
                    0,
                    "RESET"
            );
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private int tinhLaiCapBac(int current_membership_id, long tt) {
        List<Membership> membershipTypeList = new ArrayList<>(
                this.dataManager.getProductManager().getMpMembership().values()
        );

        for (int i = membershipTypeList.size() - 1; i >= 0; i--) {
            if (membershipTypeList.get(i).getMoney_require() <= tt) {
                return membershipTypeList.get(i).getId();
            }
        }
        return current_membership_id;
    }

    private void saveAgencyDTTYear(int year, int agency_id, long dtt) {
        try {
            int rs = this.agencyDB.saveAgencyDTTYear(
                    agency_id,
                    dtt,
                    dtt,
                    year
            );
            if (rs <= 0) {
                this.alertToTelegram(
                        "saveAgencyDTTYear-" +
                                agency_id +
                                " FAILED",
                        ResponseStatus.FAIL
                );
            } else {
                this.alertToTelegram(
                        "saveAgencyDTTYear-" +
                                agency_id +
                                " SUCCESS",
                        ResponseStatus.SUCCESS
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
            this.alertToTelegram(
                    "saveAgencyDTTYear-" +
                            agency_id +
                            ex.getMessage(),
                    ResponseStatus.FAIL
            );
        }
    }

    private void saveAgencyTTYear(int year, int agency_id, long tt) {
        try {
            int rs = this.agencyDB.saveAgencyTTYear(
                    agency_id,
                    tt,
                    tt,
                    year
            );
            if (rs <= 0) {
                this.alertToTelegram(
                        " RESET TT-" +
                                agency_id +
                                " FAILED",
                        ResponseStatus.FAIL
                );
            } else {
                this.alertToTelegram(
                        " RESET TT-" +
                                agency_id +
                                " SUCCESS",
                        ResponseStatus.SUCCESS
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
            this.alertToTelegram(
                    " RESET TT-" +
                            agency_id +
                            ex.getMessage(),
                    ResponseStatus.FAIL
            );
        }
    }

    private void saveAgencyAcoinYear(int year, int agency_id, long acoin) {
        try {
            int rs = this.agencyDB.saveAgencyAcoinYear(
                    agency_id,
                    0,
                    acoin,
                    year
            );
            if (rs <= 0) {
                this.alertToTelegram(
                        " RESET ACOIN-" +
                                agency_id +
                                " FAILED",
                        ResponseStatus.FAIL
                );
            } else {
                this.alertToTelegram(
                        " RESET ACOIN-" +
                                agency_id +
                                " SUCCESS",
                        ResponseStatus.SUCCESS
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
            this.alertToTelegram(
                    " RESET ACOIN-" +
                            agency_id +
                            ex.getMessage(),
                    ResponseStatus.FAIL
            );
        }
    }

    private void saveAgencyMembershipYear(int year, int agency_id, int start_value, int end_value,
                                          String agency_code,
                                          String reward) {
        try {
            int rs = this.agencyDB.saveAgencyMembershipYear(
                    agency_id,
                    start_value,
                    end_value,
                    year,
                    "",
                    reward,
                    agency_code
            );
            if (rs <= 0) {
                this.alertToTelegram(
                        " RESET MEMBERSHIP-" +
                                agency_id +
                                " FAILED",
                        ResponseStatus.FAIL
                );
            } else {
                this.alertToTelegram(
                        " RESET MEMBERSHIP-" +
                                agency_id +
                                " SUCCESS",
                        ResponseStatus.SUCCESS
                );
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
            this.alertToTelegram(
                    " RESET MEMBERSHIP-" +
                            agency_id +
                            ex.getMessage(),
                    ResponseStatus.FAIL
            );
        }
    }

    private String getNewAgencyCode(String old_agency_code, int membership_id) {
        return old_agency_code.split("_")[0] + "_" + dataManager.getProductManager().getMpMembershipCodeById(membership_id);
    }

    public ClientResponse pushNotifyResetMembership(int year) {
        try {
            /**
             * Cấp bậc	"Icon thông báo gửi theo cấp bậc mới
             *
             * 1/. Trường hợp thăng cấp
             * CHÚC MỪNG QUÝ KHÁCH ĐÃ THĂNG CẤP BẬC VÀNG
             * Công ty ANH TIN biết Quý khách có nhiều sự lựa chọn
             * Cảm ơn Quý khách đã chọn ANH TIN làm đối tác để phát triển sự nghiệp của mình.
             * Kính chúc quý khách năm mới tiếp tục gặt gái thêm nhiều thành công mới!!!
             *
             * 2/. Trường hợp giảm cấp
             * CẤP BẬC MỚI CỦA QUÝ KHÁCH LÀ BẠC
             * Công ty ANH TIN biết chúng tôi vẫn còn nhiều thiếu sót và yếu kém.
             * Chúng tôi sẽ nỗ lực hoàn thiện hơn nữa để trở thành sự lựa chọn hàng đầu của Quý khách.
             * Kính chúc quý khách năm mới tiếp tục gặt gái thêm nhiều thành công mới!!!
             *
             * 3/. Trường hợp không đổi: Không push"
             */
            List<JSONObject> agencyList = this.agencyDB.getListAgencyMembershipYear(year);
            for (JSONObject jsAgency : agencyList) {
                int start_value = ConvertUtils.toInt(jsAgency.get("start_value"));
                int end_value = ConvertUtils.toInt(jsAgency.get("end_value"));
                String reward = ConvertUtils.toString(jsAgency.get("reward"));
                if (TransactionEffectValueType.INCREASE.getCode().equals(reward)) {
                    this.pushNotifyToAgencyByQuangBa(
                            0,
                            null,
                            MembershipType.from(end_value).getCode().toLowerCase() + ".png",
                            "https://img.anhtin.vn/sht/notify/" + MembershipType.from(end_value).getCode().toLowerCase() + ".png",
                            "QUANG_BA",
                            "[]",
                            "",
                            "CHÚC MỪNG QUÝ KHÁCH ĐÃ THĂNG CẤP BẬC " + MembershipType.from(end_value).getValue().toUpperCase(),
                            "Công ty ANH TIN biết Quý khách có nhiều sự lựa chọn\n" +
                                    "Cảm ơn Quý khách đã chọn ANH TIN làm đối tác để phát triển sự nghiệp của mình.\n" +
                                    "Kính chúc quý khách năm mới tiếp tục gặt gái thêm nhiều thành công mới!!!",
                            ConvertUtils.toInt(jsAgency.get("agency_id"))
                    );
                } else if (TransactionEffectValueType.DECREASE.getCode().equals(reward)) {
                    this.pushNotifyToAgencyByQuangBa(
                            0,
                            null,
                            MembershipType.from(end_value).getCode().toLowerCase() + ".png",
                            "https://img.anhtin.vn/sht/notify/" + MembershipType.from(end_value).getCode().toLowerCase() + ".png",
                            "QUANG_BA",
                            "[]",
                            "",
                            "CẤP BẬC MỚI CỦA QUÝ KHÁCH LÀ " + MembershipType.from(end_value).getValue().toUpperCase(),
                            "Công ty ANH TIN biết chúng tôi vẫn còn nhiều thiếu sót và yếu kém.\n" +
                                    "Chúng tôi sẽ nỗ lực hoàn thiện hơn nữa để trở thành sự lựa chọn hàng đầu của Quý khách.\n" +
                                    "Kính chúc quý khách năm mới tiếp tục gặt gái thêm nhiều thành công mới!!!",
                            ConvertUtils.toInt(jsAgency.get("agency_id"))
                    );
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse pushNotifyResetAcoin(int year) {
        try {
            /**
             * KẾT QUẢ QUY ĐỔI A-COIN 2023
             * Chúc mừng Quý khách đã đạt 2,060,000 VNĐ quy đổi từ A-coin. Thời gian sử dụng để mua hàng là 02/01/2024 - 31/01/2024 (Áp dụng duy nhất cho 1 đơn hàng)
             */
            List<JSONObject> agencyList = this.agencyDB.getListAgencyAcoinYear(year);
            for (JSONObject jsAgency : agencyList) {
                int start_value = ConvertUtils.toInt(jsAgency.get("start_value"));
                int end_value = ConvertUtils.toInt(jsAgency.get("end_value"));
                long reward = ConvertUtils.toLong(jsAgency.get("reward"));

                this.pushNotifyToAgencyByQuangBa(
                        0,
                        null,
                        "",
                        "",
                        "QUANG_BA",
                        "[]",
                        "",
                        "KẾT QUẢ QUY ĐỔI A-COIN " + year,
                        "Chúc mừng Quý khách đã đạt " +
                                appUtils.priceFormat(reward) + " VNĐ quy đổi từ A-coin. Thời gian sử dụng để mua hàng là 02/01/" + (year + 1) +
                                " - 31/01/" + (year + 1) + " (Áp dụng duy nhất cho 1 đơn hàng)",
                        ConvertUtils.toInt(jsAgency.get("agency_id"))
                );
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse sendChangeMembershipToBravo(int year) {
        try {
            /**
             * Cấp bậc	"Icon thông báo gửi theo cấp bậc mới
             *
             * 1/. Trường hợp thăng cấp
             * Đồng bộ
             *
             * 2/. Trường hợp giảm cấp
             * Đồng bộ
             *
             * 3/. Trường hợp không đổi: Không đồng bộ"
             */
            List<JSONObject> agencyList = this.agencyDB.getListAgencyMembershipYear(year);
            for (JSONObject jsAgency : agencyList) {
                int agency_id = ConvertUtils.toInt(jsAgency.get("agency_id"));
                int start_value = ConvertUtils.toInt(jsAgency.get("start_value"));
                int end_value = ConvertUtils.toInt(jsAgency.get("end_value"));
                String reward = ConvertUtils.toString(jsAgency.get("reward"));
                String agency_code = ConvertUtils.toString(jsAgency.get("agency_code"));
                if (TransactionEffectValueType.INCREASE.getCode().equals(reward)) {
                    /**
                     * Đồng bộ Bravo
                     */
                    ClientResponse crSyncMembership = this.bravoService.syncAgencyMembership(
                            agency_id,
                            end_value,
                            agency_code);
                    if (crSyncMembership.failed()) {
                        this.agencyDB.syncAgencyInfoFail(
                                agency_id,
                                crSyncMembership.getMessage(),
                                2
                        );
                    }
                } else if (TransactionEffectValueType.DECREASE.getCode().equals(reward)) {
                    /**
                     * Đồng bộ Bravo
                     */
                    ClientResponse crSyncMembership = this.bravoService.syncAgencyMembership(
                            agency_id,
                            end_value,
                            agency_code);
                    if (crSyncMembership.failed()) {
                        this.agencyDB.syncAgencyInfoFail(
                                agency_id,
                                crSyncMembership.getMessage(),
                                2
                        );
                    }
                }
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    public ClientResponse pushNotifyResetCNO(int year) {
        try {
            /**
             * ĐỐI CHIẾU CÔNG NỢ
             * Số dư công nợ tại ngày 31/12/2023 của Quý khách là: ……
             * Quý khách hàng vui lòng kiểm tra tại trang Công nợ để đối chiếu thông tin. Trường hợp có khác biệt, Quý khách vui lòng phản hồi trước ngày 20/01/24.
             * Sau thời gian nêu trên, nếu không có phản hồi của Quý khách thì thông tin được hiểu là đã xác nhận đúng.
             * Xin cảm ơn Quý khách.
             */
            List<JSONObject> agencyList = this.agencyDB.getListAgencyCNOYear(year);
            for (JSONObject jsAgency : agencyList) {
                int end_value = ConvertUtils.toInt(jsAgency.get("end_value"));

                this.pushNotifyToAgencyByQuangBa(
                        0,
                        null,
                        "",
                        "",
                        "QUANG_BA",
                        "[]",
                        "",
                        "ĐỐI CHIẾU CÔNG NỢ",
                        "Số dư công nợ tại ngày 31/12/2023 của Quý khách là: " + this.appUtils.priceFormat(end_value) + " VNĐ.\n" +
                                "Quý khách hàng vui lòng kiểm tra tại trang Công nợ để đối chiếu thông tin. Trường hợp có khác biệt, Quý khách vui lòng phản hồi trước ngày 20/01/24.\n" +
                                "Sau thời gian nêu trên, nếu không có phản hồi của Quý khách thì thông tin được hiểu là đã xác nhận đúng.\n" +
                                "Xin cảm ơn Quý khách.",
                        ConvertUtils.toInt(jsAgency.get("agency_id"))
                );
            }
            return ClientResponse.success(null);
        } catch (Exception ex) {
            LogUtil.printDebug(Module.ACOIN.name(), ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }
}