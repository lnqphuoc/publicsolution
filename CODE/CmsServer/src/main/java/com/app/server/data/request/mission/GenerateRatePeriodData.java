package com.app.server.data.request.mission;

import com.app.server.constants.ResponseConstants;
import com.app.server.constants.ResponseMessage;
import com.app.server.enums.ResponseStatus;
import com.app.server.response.ClientResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateRatePeriodData {
    private GenerateRateThanhToanData thanh_toan;
    private GenerateRateMuaHangData mua_hang;
    private int so_luong_nhiem_vu;
    private int gia_tri_n;
    private int so_lan_doi_mien_phi;

    public ClientResponse validate() {
        if (thanh_toan == null) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "GenerateRateData-thanh_toan");
        }

        ClientResponse crThanhToan = thanh_toan.validate();
        if (crThanhToan.failed()) {
            return crThanhToan;
        }

        if (so_luong_nhiem_vu < 0) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "GenerateRateData-so_luong_nhiem_vu");
        }

        if (gia_tri_n < 0) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "GenerateRateData-gia_tri_n");
        }
        if (so_lan_doi_mien_phi < 0) {
            return ClientResponse.error(ResponseStatus.FAIL, ResponseMessage.FAIL, "GenerateRateData-so_lan_doi_mien_phi");
        }

        if (mua_hang != null) {
            ClientResponse crMuaHang = mua_hang.validate();
            if (crMuaHang.failed()) {
                return crMuaHang;
            }
        }
        return ResponseConstants.success;
    }
}