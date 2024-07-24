package com.app.server.data.dto.mission;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TimePartData {
    private Date date_from;
    private Date date_to;
}