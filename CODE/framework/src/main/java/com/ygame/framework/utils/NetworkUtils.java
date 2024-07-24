/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author huynxt
 */
public class NetworkUtils {

    public static String createParams(JsonObject json) {
        String params = org.apache.commons.lang.StringUtils.EMPTY;
        try {
            Set<Map.Entry<String, JsonElement>> sets = json.entrySet();
            for (Map.Entry<String, JsonElement> entry : sets) {
                String key = entry.getKey();
                String value = entry.getValue().getAsString();
                if (entry.getValue().isJsonObject()) {
                    value = entry.getValue().toString();
                }
                params += key + "=" + value + "&";
            }
            return params;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getClientIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    public static String ipAddressToBinaryString(String ipAddress) {
        try {
            String result = "";
            String[] arr = ipAddress.split("\\.");
            if (arr.length != 4) {
                return "";
            }
            for (int i = 0; i < arr.length; ++i) {
                int val = ConvertUtils.toInt(arr[i], -1);
                if (val > 255 || val < 0) {
                    return "";
                }
                result += org.apache.commons.lang.StringUtils.leftPad(Integer.toBinaryString(val), 8, "0");
            }
            return result;
        } catch (Exception ex) {
        }
        return "";
    }

    public static String ipRangeToBinaryString(String ipRange) {
        try {
            String[] arr = ipRange.split("/");
            if (arr.length == 1) {
                return ipAddressToBinaryString(arr[0].trim());
            } else if (arr.length == 2) {
                int len = ConvertUtils.toInt(arr[1]);
                return ipAddressToBinaryString(arr[0].trim()).substring(0, len);
            }

        } catch (Exception ex) {
        }
        return "";
    }

    public static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    public static long ipToLong(String ipAddress) {
        try {
            String[] arr = ipAddress.split(",");
            return ipToLong(InetAddress.getByName(arr[0].trim()));
        } catch (Exception ex) {
        }
        return -1;
    }

    public static long[] ipRangeToRangeNumber(String ipRange) {
        try {
            String[] arr = ipRange.split("/");
            if (arr.length == 1) {
                long value = ipToLong(arr[0]);
                return new long[]{value, value};
            } else if (arr.length == 2) {
                int len = ConvertUtils.toInt(arr[1]);
                String bin = ipAddressToBinaryString(arr[0].trim()).substring(0, len);
                long from = Long.parseLong(StringUtils.rightPad(bin, 32, "0"), 2);
                long to = Long.parseLong(StringUtils.rightPad(bin, 32, "1"), 2);
                return new long[]{from, to};
            }

        } catch (Exception ex) {
        }
        return new long[]{-1, -1};
    }

    public static boolean isIPAddressInRange(String ipAddress, String ipRange) {
        String bIP = ipAddressToBinaryString(ipRange);
        String bRange = ipRangeToBinaryString(ipRange);
        return bIP.startsWith(bRange);
    }

    public static byte[] download(String url) throws Exception {
        //create a URL
        URL sourceURL = new URL(url);
        //obtain the connection
        HttpURLConnection sourceConnection = (HttpURLConnection) sourceURL.openConnection();
        //add parameters to the connection
        HttpURLConnection.setFollowRedirects(true);
        //allow both GZip and Deflate (ZLib) encodings
        sourceConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

        //establish connection, get response headers
        sourceConnection.connect();

        //obtain the encoding returned by the server
        String encoding = sourceConnection.getContentEncoding();

        int code = sourceConnection.getResponseCode();

        InputStream resultingInputStream;

        //create the appropriate stream wrapper based on
        //the encoding type
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            resultingInputStream = new GZIPInputStream(sourceConnection.getInputStream());
        } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
            resultingInputStream = new InflaterInputStream(sourceConnection.getInputStream(), new Inflater(true));
        } else {
            resultingInputStream = sourceConnection.getInputStream();
        }

        byte[] bytes = new byte[1024];
        int count;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((count = resultingInputStream.read(bytes, 0, 1024)) != -1) {
            outStream.write(bytes, 0, count);
        }
        resultingInputStream.close();
        sourceConnection.disconnect();
        return outStream.toByteArray();
    }

    public static void download(String url, String filePath) throws Exception {
        //create a URL
        URL sourceURL = new URL(url);
        //obtain the connection
        HttpURLConnection sourceConnection = (HttpURLConnection) sourceURL.openConnection();
        //add parameters to the connection
        HttpURLConnection.setFollowRedirects(true);
        //allow both GZip and Deflate (ZLib) encodings
        sourceConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

        //establish connection, get response headers
        sourceConnection.connect();

        //obtain the encoding returned by the server
        String encoding = sourceConnection.getContentEncoding();

        int code = sourceConnection.getResponseCode();

        InputStream resultingInputStream;

        //create the appropriate stream wrapper based on
        //the encoding type
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            resultingInputStream = new GZIPInputStream(sourceConnection.getInputStream());
        } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
            resultingInputStream = new InflaterInputStream(sourceConnection.getInputStream(), new Inflater(true));
        } else {
            resultingInputStream = sourceConnection.getInputStream();
        }

        byte[] bytes = new byte[1024];
        int count;
        BufferedOutputStream outStream = null;
        try {
            outStream = new BufferedOutputStream(new FileOutputStream(filePath));
            while ((count = resultingInputStream.read(bytes, 0, 1024)) != -1) {
                outStream.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        } finally {
            try {
                if (outStream != null) {
                    outStream.flush();
                    outStream.close();
                }
            } catch (Exception ex) {
            }
        }

        resultingInputStream.close();
        sourceConnection.disconnect();
    }

    public static int getContentLength(String url) throws Exception {
        try {
            if (url == null || url.length() <= 0) {
                return 0;
            }
            //create a URL
            URL sourceURL = new URL(url);
            //obtain the connection
            HttpURLConnection sourceConnection = (HttpURLConnection) sourceURL.openConnection();
            //establish connection, get response headers
            sourceConnection.connect();

            //InputStream resultingInputStream = sourceConnection.getInputStream();
            System.out.println(sourceConnection.getHeaderFields());
            int result = 0;
            if (sourceConnection.getResponseCode() >= 200 && sourceConnection.getResponseCode() < 300) {
                result = ConvertUtils.toInt(sourceConnection.getHeaderField("Content-Length"));
            }
            sourceConnection.getInputStream().close();
            sourceConnection.disconnect();

            return result;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static String getResponse(String url, JsonObject params) {
        String strParams = createParams(params);
        url = StringUtils.removeEnd(url, "?");
        return getResponse(url + "?" + strParams);
    }

    public static String getResponse(String url) {
        return getResponse(url, 0);
    }

    public static String getResponse(String url, int timeout) {
        try {
            URL _url = new URL(url);
            final URLConnection connection = _url.openConnection();

            connection.setDoOutput(true);
            connection.setReadTimeout(timeout);
            try (InputStream replyStream = connection.getInputStream()) {
                StringBuilder sb;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(replyStream))) {
                    sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append(System.getProperty("line.separator"));
                    }
                }
                return sb.toString();
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public static boolean sendRequest(String url) {
        try {
            URL sourceURL = new URL(url);
            HttpURLConnection sourceConnection = (HttpURLConnection) sourceURL.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            sourceConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            sourceConnection.connect();

            int code = sourceConnection.getResponseCode();

            boolean result = (code >= 200 && code < 500);
            if (!result) {
                System.out.println(code);
            }
            return (code >= 200 && code < 500);
        } catch (Exception ex) {
        }

        return false;
    }

    public static String post(String urlRequest, String params) throws Exception {
        String result = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlRequest);
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
            conn.setUseCaches(false);

            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.writeBytes(params);
                wr.flush();
            }

            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                while ((line = reader.readLine()) != null) {
                    result += "\r\n" + line;
                }
            }
            if (result.length() > 0) {
                result = result.substring(2);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }

    public static String doPost(String url, Map<String, String> header, JsonObject param) {
        StringBuilder sb = new StringBuilder();
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost req = new HttpPost(url);
            req.addHeader("accept", "application/json");

            Set<String> keys = header.keySet();
            for (String key : keys) {
                req.addHeader(key, header.get(key));
            }

            StringEntity input = new StringEntity(param.toString());
            input.setContentType("application/json");
            req.setEntity(input);

            HttpResponse response = httpClient.execute(req);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            httpClient.getConnectionManager().shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String doGet(String url, Map<String, String> header, JsonObject param) {
        StringBuilder sb = new StringBuilder();
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet req = new HttpGet(url);
            req.addHeader("accept", "application/json");

            Set<String> keys = header.keySet();
            for (String key : keys) {
                req.addHeader(key, header.get(key));
            }

//            StringEntity input = new StringEntity(param.toString());
//            input.setContentType("application/json");
//            req.setEntity(input);
            HttpResponse response = httpClient.execute(req);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            httpClient.getConnectionManager().shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String cookies;
    private static final String USER_AGENT = "Mozilla/5.0";

    public static int getHttpResponseCode(String url) {
        try {
            HttpClient client = HttpClientBuilder.create().build();

            HttpGet request = new HttpGet(url);

            request.setHeader("User-Agent", USER_AGENT);
            request.setHeader("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.setHeader("Accept-Language", "en-US,en;q=0.5");

            HttpResponse response = client.execute(request);
            return response.getStatusLine().getStatusCode();
        } catch (Exception ex) {
            return 400;
        }
    }

    public static String[] getPageContent(String url) throws IOException {
        String arr[] = new String[2];
        HttpClient client = HttpClientBuilder.create().build();

        HttpGet request = new HttpGet(url);

        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        HttpResponse response = client.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        System.err.println("RESPONSE CODE = " + responseCode);
        arr[0] = String.valueOf(responseCode);
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        arr[1] = result.toString();
        return arr;
    }

    public static String GetPageContent(String url) {
        try {
            HttpClient client = HttpClientBuilder.create().build();

            HttpGet request = new HttpGet(url);

            request.setHeader("User-Agent", USER_AGENT);
            request.setHeader("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.setHeader("Accept-Language", "en-US,en;q=0.5");

            HttpResponse response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            System.err.println("RESPONSE CODE = " + responseCode);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            return result.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return StringUtils.EMPTY;
        }
    }
}
