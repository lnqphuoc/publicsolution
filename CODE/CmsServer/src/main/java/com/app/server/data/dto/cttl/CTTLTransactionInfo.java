package com.app.server.data.dto.cttl;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CTTLTransactionInfo {
    private int id;
    private int agency_id;
    private int promo_id;
    private int transaction_id;
    private int type;
    private long total_transaction_value;
    private long total_transaction_end;
    private Date created_date;
    private String code;
    private String name;
    private Integer dept_transaction_id;
    private Integer agency_order_dept_id;
    private Integer agency_hbtl_id;
    private Integer agency_dept_dtt_id;
    private int status;
    private Date confirm_date;
    private Date transaction_date;
    private long gia_tri;
    private long gia_tri_tich_luy;
    private long so_luong_dtt_tich_luy;
    private long da_thanh_toan;
    private long da_thanh_toan_hop_le;
    private long con_phai_thu;
    private long con_phai_thu_hop_le;
    private long ky_han_no;
    private int transaction_source;
    private long dtt_tich_luy;
    private long payment_date;
}