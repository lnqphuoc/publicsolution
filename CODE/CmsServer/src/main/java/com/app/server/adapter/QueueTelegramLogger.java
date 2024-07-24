package com.app.server.adapter;

import com.app.server.config.ConfigInfo;
import com.app.server.utils.NetworkUtil;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.queue.QueueCommand;

import java.net.URLEncoder;

public class QueueTelegramLogger implements QueueCommand {
    private static final String URL_TELEGRAM = "https://api.telegram.org/bot6199727867:AAHZuy7Skp9IqN4vYiuPyQcrEWpgCWAD_fY/";
    private String chat_id = "";
    private String data;

    public QueueTelegramLogger(String data, String chat_id) {
        try {
            this.data = URLEncoder.encode(data, "UTF-8");
            this.chat_id = chat_id;
        } catch (Exception ex) {
            this.data = "error_msg";
            LogUtil.printDebug("", ex);
        }
    }

    @Override
    public void execute() {
        try {
            String response = NetworkUtil.getResponse(
                    ConfigInfo.URL_TELEGRAM + "sendMessage?chat_id=-" +
                            chat_id + "&text=" + data);
            LogUtil.printDebug(response);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
    }
}