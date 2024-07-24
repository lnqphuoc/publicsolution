package com.app.server.data.dto.mission;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TimeToWorkData {
    private TimePartData morning;
    private TimePartData afternoon;
}