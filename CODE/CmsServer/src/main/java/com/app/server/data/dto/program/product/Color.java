package com.app.server.data.dto.program.product;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Color {
    private int id;
    private String name;
    private Date createdDate;
}