package com.app.server.data;

import lombok.Data;

@Data
public class SessionData {
    private int id;
    private String name;
    private long expire;
}
