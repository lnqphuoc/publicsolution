package com.ygame.framework.ipchecking;

import com.ygame.framework.utils.FileUtils;
import com.ygame.framework.utils.NetworkUtils;
import java.io.File;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author ThanhNT
 */
public class Variable {

    public static final String PRE_ZONE = "z_";
    public static final String PRE_BANER = "b_";
    public static final String PRE_LINK = "l_";
    public static final String PRE_LINK_OVER_SEA = "lo_";
    public static final String PRE_LOG_DELI = "dl_";
    public static final String PRE_LOG_IMPR = "i_";
    public static final String PRE_LOG_CLICK = "c_";
    public static final String PRE_LOG_EXTRA = "ex_";
    public static final String PRE_DISPLAYED_BANNER = "disp_";
    public static final int JOB_LOG_IMPRESSION = 1;
    public static final int JOB_LOG_CLICK = 2;
    public static final int JOB_LOG_DELIVERY = 3;
    public static final int JOB_LOG_EXTRA = 4;
    public static int TIME_EXPIRED_UNIQUE_DISPLAY = 24 * 60 * 60;
    public static int PREROLL_MAX_BANNER = 3;
    public static final String result = "{\"error\":0,\"status\":false}";
    public static String secretKey = "123123";
    public static boolean writeScribe = false;
    public static final Set<String> lDenyBrowsers = new HashSet<>();
    public static final NavigableMap<Long, Long> mVietnamIP = new TreeMap<>();
    public static String playerInnerSong = "Inner_Player";
    public static String playerInnerPL = "Inner_Playlist";
    public static Set<String> sZoneCheckUnique = new HashSet<>();

    static {
        loadRule();
    }

    public static void loadRule() {
        if (mVietnamIP == null || mVietnamIP.isEmpty()) {

            String HOME_PATH = System.getProperty("apppath");
            if (HOME_PATH == null) {
                HOME_PATH = "";
            } else {
                HOME_PATH = HOME_PATH + File.separator;
            }
String path = HOME_PATH + "conf" + File.separator + "VN.subnets";
            System.err.println("path >> " + path);
            String[] listConfig = FileUtils.readAllString(HOME_PATH + "conf" + File.separator + "VN.subnets").split("\n");
            if (listConfig != null) {
                for (int i = 0; i < listConfig.length; i++) {
                    String value = listConfig[i];
                    if (value != null) {
                        RuleEnt rule = new RuleEnt(value, value, StringUtils.EMPTY, StringUtils.EMPTY, true);
                        long[] range = NetworkUtils.ipRangeToRangeNumber(rule.fileterKey);
                        mVietnamIP.put(range[0], range[1]);
                        
                        System.err.println("put ... " + range[0] + " --- " + range[1]);
                    }
                }
            }
        }
    }
}
