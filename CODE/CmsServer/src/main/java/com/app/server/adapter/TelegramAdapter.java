package com.app.server.adapter;

public class TelegramAdapter {
    public static void sendMsg(String msg, String chat_id) {
        QueueTelegramLogger l = new QueueTelegramLogger(msg, chat_id);
        l.execute();
    }
}