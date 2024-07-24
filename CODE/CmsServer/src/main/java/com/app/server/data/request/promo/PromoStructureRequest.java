package com.app.server.data.request.promo;

import com.app.server.data.dto.promo.PromoBasicData;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PromoStructureRequest {
    private List<String> promo_lien_ket_list = new ArrayList<>();
    private List<String> promo_dong_thoi_list = new ArrayList<>();
    private List<String> promo_dong_thoi_tru_gtdtt_list = new ArrayList<>();
    private List<String> promo_loai_tru_list = new ArrayList<>();
    private int is_ignore_all_csbh = 0;
    private int is_ignore_all_ctkm = 0;
    private List<JSONObject> promo_loai_tru_ctkm_list = new ArrayList<>();
    private List<JSONObject> promo_loai_tru_csbh_list = new ArrayList<>();
    private List<JSONObject> promo_loai_tru_ctss_list = new ArrayList<>();
    private List<JSONObject> promo_loai_tru_cttl_list = new ArrayList<>();
}