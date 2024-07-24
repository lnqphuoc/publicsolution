package com.ygame.framework.ipchecking;

import com.ygame.framework.utils.NetworkUtils;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.apache.commons.codec.binary.Base64;

public class Helper {

    private static final String defaultResponse = "{\"status\":true,\"error\":0}";
    private static final Random random = new Random();
    public static final byte[] PIXEL_BYTES = Base64.decodeBase64("R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==".getBytes());

    public static String defaultResponse() {
        return defaultResponse;
    }

    public static long randomNum() {
        return random.nextInt(1000000000);
    }

    public static boolean validBrowser(String userAgent) {
        if (userAgent == null) {
            return false;
        }
        Iterator<String> scan = Variable.lDenyBrowsers.iterator();
        while (scan.hasNext()) {
            if (userAgent.contains(scan.next())) {
                return false;
            }
        }
        return true;
    }

    public static long ipToLong(String ipAddress) {
        try {
            String[] arr = ipAddress.split(",");
            return ipToLong(InetAddress.getByName(arr[0].trim()));
        } catch (Exception ex) {
        }
        return 0;
    }

    private static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    public static boolean isVietnamIp(String ip) {
        long key = NetworkUtils.ipToLong(ip);
        Map.Entry<Long, Long> entry = Variable.mVietnamIP.floorEntry(key);
        if (key <= 0 || entry == null) {
            return true;
        } else if (key <= entry.getValue()) {
            return true;
        }

        return false;
    }
}
