package com.app.server.config;

import com.ygame.framework.common.Config;
import com.ygame.framework.utils.ConvertUtils;

import java.net.URI;

public class ConfigInfo {
    public static final String SPRING_BOOT_CONFIG_PATH = Config.getParam("spring_boot", "conf_path");

    public static final String ENV_LEVEL = Config.getParam("env", "level");
    public static final String DATABASE = "database";
    public static final String DATABASE_LOG = "database_log";
    public static final String REDIS = "redis";
    public static final String IMAGE_FOLDER_PATH = Config.getParam("image", "folder");
    public static final String IMAGE_URL = Config.getParam("image", "url");
    public static final String JWT_SECRET_KEY = Config.getParam("jwt", "secret_key");
    public static final int PAGE_SIZE = 10;
    public static final String RELOAD_CACHE_URL = Config.getParam("cache", "url");
    public static final String RELOAD_CACHE_KEY = Config.getParam("cache", "key");
    public static final int SCHEDULE_RUNNING_LIMIT = ConvertUtils.toInt(Config.getParam("schedule", "limit"));
    public static final String CHAT_EXCEPTION_ID = Config.getParam("telegram", "chat_exception_id");
    public static final String CHAT_REPORT_ID = Config.getParam("telegram", "chat_report_id");
    public static final String URL_TELEGRAM = Config.getParam("telegram", "url");

    public static final String PUSH_NOTIFY_URL = Config.getParam("notify", "url");
    public static final String PUSH_NOTIFY_CMS_URL = Config.getParam("notify", "url_cms");
    public static final String PUSH_NOTIFY_KEY = Config.getParam("notify", "key");
    public static final String SERVICE_BRAVO_URL = Config.getParam("bravo", "url");
    public static final String SERVICE_BRAVO_KEY = Config.getParam("bravo", "key");
    public static final String ACCUMULATE_URL = Config.getParam("accumulate", "url");
    public static final String ACCUMULATE_KEY = Config.getParam("accumulate", "key");
    public static final String IMAGE_PUSH_NOTIFY_URL = Config.getParam("notify", "image_url");
    public static boolean READY = false;
}