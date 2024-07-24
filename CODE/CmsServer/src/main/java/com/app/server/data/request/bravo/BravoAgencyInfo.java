package com.app.server.data.request.bravo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BravoAgencyInfo {
    private String customerAppId;
    private String code;
    private String name;
    private String person;
    private String tel;
    private String address;
    private int provinceAppId;
    private int districtAppId;
    private int wardAppId;
    private String callNameCode;
    private String birthDay;
    private String email;
    private String createdAt;
    private int customerLevelAppId;

    /**
     * Example
     * --data-raw '[
     * {
     * "createdAt": "2023-07-01",
     * "customerAppId": "12",
     * "code": "AnhTin12",
     * "name": "Công ty TNHH Anh Tin12",
     * "person": "Nguyễn Văn A12",
     * "tel": "011 111 1001",
     * "address": "HCM1",
     * "provinceAppId": 1,
     * "districtAppId": 2,
     * "wardAppId": 4,
     * "callNameCode": "1",
     * "birthDay": "2008-08-08",
     * "email": "anhtin@gmail.com"
     * }
     * ]'
     */
}