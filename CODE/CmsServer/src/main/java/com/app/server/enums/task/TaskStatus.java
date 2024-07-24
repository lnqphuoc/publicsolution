package com.app.server.enums.task;

import lombok.Getter;

@Getter
public enum TaskStatus {
    DOING(1),
    DONE(2);
    private int id;

    TaskStatus(int id) {
        this.id = id;
    }
}