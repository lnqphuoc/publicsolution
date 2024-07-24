package com.app.server.data.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class AgencyContractInfoEntity {
    private Integer id;
    private int agency_id;
    private String contract_number;
    private String company_name;
    private String representative;
    private String tax_number;
    private String identity_number;
    private String identity_date;
}