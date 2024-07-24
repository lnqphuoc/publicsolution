package com.app.server.data.entity;

import com.app.server.enums.NotifyStatus;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
public class NotifyWaitingPushEntity {
    private Integer id;
    private String name;
    private String image;
    private String description;
    private int status = NotifyStatus.WAITING.getId();
    private Date created_date;
    private Integer creator_id;
    private Date modified_date;
    private Integer modifier_id;
    private String firebase_token_data;
}