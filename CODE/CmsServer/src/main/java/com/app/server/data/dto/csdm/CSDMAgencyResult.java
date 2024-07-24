package com.app.server.data.dto.csdm;

import com.app.server.data.dto.agency.AgencyBasicData;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.Date;

@Getter
@Setter
public class CSDMAgencyResult {
    private int id;
    private int agency_id;
    private AgencyBasicData agency_info;
    private int program_id;
    private Date created_date;
    private int limit;
    private long tong_gia_tri_tham_gia;
    private long tong_gia_tri_tich_luy;
    private long tong_thanh_toan;
    private long tong_thanh_toan_hop_le;
    private long han_muc_uu_dai;
    private long uu_dai;
    private long tong_uu_dai_da_tra_thuong;
    private long tong_uu_dai_duoc_huong;
    private long tong_thanh_toan_con_thieu;
    private long tong_doanh_thu_thuan_tham_gia;
    private int han_muc_duoc_huong;
    private int phan_tram_duoc_huong;
    private long uu_dai_duoc_huong;

    public static CSDMAgencyResult from(JSONObject js) {
        CSDMAgencyResult entity = new CSDMAgencyResult();
        entity.setId(ConvertUtils.toInt(js.get("id")));
        entity.setAgency_id(ConvertUtils.toInt(js.get("agency_id")));
        entity.setCreated_date(js.get("created_date") == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                js.get("created_date")))
        );

        AgencyBasicData agency = new AgencyBasicData();
        agency.parseInfo(
                entity.getAgency_id(),
                ConvertUtils.toString(
                        js.get("code")),
                ConvertUtils.toString(
                        js.get("shop_name")),
                ConvertUtils.toInt(js.get("membership_id")),
                ConvertUtils.toInt(js.get("business_department_id")),
                null,
                null,
                ConvertUtils.toString(
                        js.get("shop_name")),
                ConvertUtils.toString(
                        js.get("avatar"))
        );
        entity.setAgency_info(agency);


        /**
         tong_gia_tri_tham_gia;
         */
        entity.setTong_gia_tri_tham_gia(
                ConvertUtils.toLong(js.get("tong_gia_tri_tham_gia"))
        );
        /**
         tong_gia_tri_tich_luy;
         */
        entity.setTong_gia_tri_tich_luy(
                ConvertUtils.toLong(js.get("tong_gia_tri_tich_luy"))
        );
        /**
         * tong_thanh_toan;
         */
        entity.setTong_thanh_toan(
                ConvertUtils.toLong(js.get("tong_thanh_toan"))
        );
        /**
         * tong_thanh_toan_hop_le;
         */
        entity.setTong_thanh_toan_hop_le(
                ConvertUtils.toLong(js.get("tong_thanh_toan_hop_le"))
        );
        /**
         * tong_thanh_toan_con_thieu;
         */
        entity.setTong_thanh_toan_con_thieu(
                ConvertUtils.toLong(js.get("tong_thanh_toan_con_thieu"))
        );
        /**
         han_muc_uu_dai;
         */
        entity.setHan_muc_uu_dai(
                ConvertUtils.toLong(js.get("han_muc_uu_dai"))
        );
        /**
         uu_dai;
         */
        entity.setUu_dai(
                ConvertUtils.toLong(js.get("uu_dai"))
        );
        /**
         tong_uu_dai_da_tra_thuong;
         */
        entity.setTong_uu_dai_da_tra_thuong(
                ConvertUtils.toLong(js.get("tong_uu_dai_da_tra_thuong"))
        );
        /**
         tong_uu_dai_duoc_thuong;
         */
        entity.setTong_uu_dai_duoc_huong(
                ConvertUtils.toLong(js.get("tong_uu_dai_duoc_huong"))
        );
        entity.setTong_doanh_thu_thuan_tham_gia(
                ConvertUtils.toLong(js.get("tong_doanh_thu_thuan_tham_gia"))
        );
        return entity;
    }
}