package com.app.server.utils;

import com.app.server.config.ConfigInfo;
import com.app.server.data.SessionData;
import com.app.server.enums.MissionPeriodType;
import com.app.server.enums.Module;
import com.app.server.enums.PriceDataType;
import com.app.server.enums.PriceSettingType;
import com.google.gson.reflect.TypeToken;
import com.sun.javafx.binding.StringFormatter;
import com.ygame.framework.common.Config;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import io.jsonwebtoken.Claims;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
public class AppUtils {
    private JwtUtil jwtUtil;

    public static Date convertJsonToDate(Object object) {
        return object == null ? null :
                DateTimeUtils.getDateTime(
                        ConvertUtils.toString(
                                object));
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public static final boolean arrayWeekly[][] = setDataArrayWeekly();

    private static boolean[][] setDataArrayWeekly() { //chủ nhật = 0, thứ 7 = 6
        boolean result[][] = new boolean[14][24]; // [ngày][giờ]
        for (int ngay = 0; ngay < 14; ngay++) {
            //ngoài hành chính off
            for (int gio = 0; gio < 8; gio++) { // 0-8h sáng false
                result[ngay][gio] = false;
            }
            for (int gio = 12; gio < 13; gio++) { // 12-13h trưa false
                result[ngay][gio] = false;
            }
            for (int gio = 17; gio < 24; gio++) { // 17-24h tối false
                result[ngay][gio] = false;
            }
            // buổi sáng work
            if (ngay != 0 && ngay != 7) { // khác chủ nhât
                for (int gio = 8; gio < 12; gio++) { // 8-12h trưa true
                    result[ngay][gio] = true;
                }
            }
            //buổi chiều work
            if (ngay != 6 && ngay != 0 && ngay != 13 && ngay != 7) { // khác t7, chủ nhât
                for (int gio = 13; gio < 17; gio++) { // 13-17h trưa true
                    result[ngay][gio] = true;
                }
            } else { // thứ 7, chủ nhật
                for (int gio = 13; gio < 17; gio++) { // 13-17h trưa false
                    result[ngay][gio] = false;
                }
            }

        }
        return result;
    }


    // check phone
    public boolean checkPhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.trim().length() != 10 || !phone.startsWith("0"))
            return false;
        return true;
    }

    // check email
    public boolean checkEmail(String email) {
        if (StringUtils.isBlank(email))
            return true;
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(regexPattern)
                .matcher(email)
                .matches();
    }

    // check password
    public boolean checkPassword(String password) {
        try {
            if (StringUtils.isBlank(password) || password.length() < 6 || password.length() > 50)
                return false;
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // generate new otp
    public String genOTP() {
        return "123456";
    }

    // generate new login token
    public String genLoginToken(int id, String name) {
        String token = "";
        try {
            SessionData session = new SessionData();
            session.setId(id);
            session.setName(name);
            token = jwtUtil.createJWT(session, 1000000000); // Generate login token never expire
        } catch (Exception ex) {
            token = "";
            LogUtil.printDebug("", ex);
        }
        return token;
    }

    // generate otp
    public String generateOTP() {
        SecureRandom random = new SecureRandom();
        int rand = random.nextInt(1000000);
        String result = String.format("%06d", rand);
        return result;
    }

    public String getLastTwoNumberOfYear() {
        return DateTimeUtils.getNow("YY");
    }

    public String getYYMMDD(Date date) {
        return DateTimeUtils.toString(date, "yyMMdd");
    }

    public String getYear(Date date) {
        return DateTimeUtils.toString(date, "yyyy");
    }

    public String getMonth(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String d = simpleDateFormat.format(date);
        return d.split("-")[1];
    }

    public String convertDateToString(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    public Date convertStringToDate(String date, String format) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            return simpleDateFormat.parse(date);
        } catch (Exception e) {
            return null;
        }

    }

    public String getDay(Date date) {
        return convertDateToString(date, "dd");
    }

    public String convertCityIndexToCode(int cityIndex) {
        return String.format("%03d", cityIndex);
    }

    public boolean validBirthday(String birthday) {
        try {
            java.util.Date now = new java.util.Date();
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date tmp = format.parse(birthday + " 00:00:00");
            if (!tmp.before(now)) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public List<String> convertStringToArray(String data) {
        try {
            List<String> rs = JsonUtils.DeSerialize(data, new TypeToken<List<String>>() {
            }.getType());

            if (rs == null) {
                rs = new ArrayList<>();
            }
            return rs;
        } catch (Exception ex) {
        }
        return new ArrayList<>();
    }

    public JSONArray convertStringToArrayObject(String data) {
        try {
            return JsonUtils.DeSerialize(data, JSONArray.class);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    public int getTotalPage(int total) {
        int a = total / ConfigInfo.PAGE_SIZE;
        int b = total % ConfigInfo.PAGE_SIZE;

        if (b > 0) {
            return a + 1;
        }

        return a;
    }

    public int getTotalPageBySize(int total, int size) {
        int a = total / size;
        int b = total % size;

        if (b > 0) {
            return a + 1;
        }

        return a;
    }

    public int getOffset(int page) {
        if (page <= 0) {
            return 0;
        }

        return (page - 1) * ConfigInfo.PAGE_SIZE;
    }

    public String md5(String password) {
        return DigestUtils.md5Hex(password);
    }

    public String priceFormat(double data) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);
        return numberFormat.format(data);
    }

    public String numberFormat(double data) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);
        return numberFormat.format(data);
    }

    public Double roundPrice(double price) {
        DecimalFormat decimalFormat = new DecimalFormat("#####.##");
        return ConvertUtils.toDouble(decimalFormat.format(price));
    }

    public static Date getDateCancelAfterHour(long milisecond, int hourAfter) {
        try {
            Date create_time = getCreateDateOrderCancel(milisecond);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(create_time.getTime());
            int date = cal.get(Calendar.DAY_OF_WEEK); // chủ nhật 1 = 1
            int hour = cal.get(Calendar.HOUR_OF_DAY);

            //check
            int ngay_check = 0;
            int gio_check = 0;
            //logic
            int result_hour = 0;
            boolean stop = false;
            boolean sum = false;
            int count_hour = 0;
            int count_date = 0;
            for (int ngay = 0; ngay < 14; ngay++) {
                ngay_check = ngay;
                if (sum == true) {
                    count_date++;
                }
                for (int gio = 0; gio < 24; gio++) {
                    gio_check = gio;
                    if (date == ngay + 1 && gio == hour + 1) {
                        sum = true;
                    }
                    if (sum == true) {
                        if (arrayWeekly[ngay][gio] == true) {
                            count_hour++;
                            if (count_hour == hourAfter) {
                                result_hour = gio;
                                stop = true;
                                break;
                            }
                        }
                    }
                }
                if (stop == true) {
                    break;
                }
            }

            cal.set(Calendar.DATE, cal.get(Calendar.DATE) + count_date);
            cal.set(Calendar.HOUR_OF_DAY, result_hour);

            // check 8h sáng => chuyển về 17h hôm trước    OR 13h trưa = chuyển về 12h trưa
            if (cal.get(Calendar.HOUR_OF_DAY) == 8 || cal.get(Calendar.HOUR_OF_DAY) == 13) {
                if (cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0) {
                    stop = false;
                    count_date = -1;
                    for (int ngay = ngay_check; ngay >= 0; ngay--) {
                        count_date++;
                        if (ngay == ngay_check) {
                            for (int gio = gio_check - 1; gio >= 0; gio--) {
                                if (arrayWeekly[ngay][gio] == true) {
                                    stop = true;
                                    break;
                                }
                            }
                        } else {
                            for (int gio = 23; gio >= 0; gio--) {
                                if (arrayWeekly[ngay][gio] == true) {
                                    stop = true;
                                    break;
                                }
                            }
                        }

                        if (stop == true) {
                            break;
                        }
                    }
                    //set lại ngày giờ
                    cal.set(Calendar.DATE, cal.get(Calendar.DATE) - count_date);
                    if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                        cal.set(Calendar.HOUR_OF_DAY, 12);
                    } else {
                        if (cal.get(Calendar.HOUR_OF_DAY) == 13) {
                            cal.set(Calendar.HOUR_OF_DAY, 12);
                        } else if (cal.get(Calendar.HOUR_OF_DAY) == 8) {
                            cal.set(Calendar.HOUR_OF_DAY, 17);
                        }
                    }
                }
            }
            return cal.getTime();
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    private static Date getCreateDateOrderCancel(long milisecond) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milisecond);
        int date = cal.get(Calendar.DAY_OF_WEEK);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (date == Calendar.SATURDAY) { // thứ 7
            if (hour < 8) {
                cal.set(Calendar.HOUR_OF_DAY, 8);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }
            if (hour >= 12) {
                cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);
                cal.set(Calendar.HOUR_OF_DAY, 8);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }
        } else if (date == Calendar.SUNDAY) { // chủ nhật
            cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
            cal.set(Calendar.HOUR_OF_DAY, 8);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        } else { // trong tuần
            if (cal.get(Calendar.HOUR_OF_DAY) < 8) {
                cal.set(Calendar.HOUR_OF_DAY, 8);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            } else if (cal.get(Calendar.HOUR_OF_DAY) == 12) {
                cal.set(Calendar.HOUR_OF_DAY, 13);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            } else if (cal.get(Calendar.HOUR_OF_DAY) >= 17) {
                cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
                cal.set(Calendar.HOUR_OF_DAY, 8);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }
        }

        return cal.getTime();
    }

    public static boolean isHenGiao(Date createDate, Date deliveryDate, int day) {
        if (deliveryDate == null) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        Date dateCreate = DateTimeUtils.getDateTime(
                DateTimeUtils.toString(createDate, "yyyy-MM-dd"),
                "yyyy-MM-dd");
        Date dateDelivery = DateTimeUtils.getDateTime(
                DateTimeUtils.toString(deliveryDate, "yyyy-MM-dd"),
                "yyyy-MM-dd");

        int xDay = ConvertUtils.toInt(TimeUnit.MILLISECONDS.toDays(dateDelivery.getTime() - dateCreate.getTime()));
        if (xDay < 2) {
            return false;
        }
        if (xDay > 7) {
            return true;
        }
        int n = xDay;
        for (int i = 0; i < n; i++) {
            calendar.setTime(createDate);
            calendar.add(Calendar.DATE, i + 1);
            Date date1 = DateTimeUtils.getDateTime(
                    DateTimeUtils.toString(calendar.getTime(), "yyyy-MM-dd"),
                    "yyyy-MM-dd");
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY ||
                    dayOfWeek == Calendar.SUNDAY) {
                xDay--;
            }
        }

        if (xDay < 2) {
            return false;
        }

        return true;
    }

    private static int getDateOfWeekOfAfter(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, day);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public int getDateOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public String createJWT(String data) {
        try {
            return jwtUtil.createJWTLink(data);
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return "";
    }

    public String decodeJWT(String data) {
        try {
            Claims claims = jwtUtil.decodeJWT(data);
            if (claims != null && claims.get("data") != null) {
                return claims.get("data").toString();
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        }
        return null;
    }

    public Date getTimeLock(Date date, int timeLock) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MILLISECOND, timeLock);
        return calendar.getTime();
    }

    public Date getDateAfterDay(Date date, int payment_duration_app) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, payment_duration_app);
        return calendar.getTime();
    }

    public Double convertProductPrice(
            double price_original_current,
            double price_new,
            int is_auto,
            String price_setting_type,
            String price_data_type,
            double price_setting_value) {
        PriceSettingType priceSettingType = PriceSettingType.from(price_setting_type);
        switch (priceSettingType) {
            case INCREASE: {
                if (price_original_current == -1) {
                    return -1.0;
                } else if (is_auto == 0) {
                    return price_new;
                } else {
                    if (PriceDataType.PERCENT.getCode().equals(price_data_type)) {
                        double price = price_original_current * 1.0F + price_original_current * 1.0F * price_setting_value / 100;
                        return this.roundPrice(price);
                    } else if (PriceDataType.MONEY.getCode().equals(price_data_type)) {
                        return ConvertUtils.toDouble(price_original_current + price_setting_value);
                    }
                }
                return price_new;
            }
            case DECREASE: {
                if (price_original_current == -1) {
                    return -1.0;
                } else if (PriceDataType.PERCENT.getCode().equals(price_data_type)) {
                    double price = Math.max(0, price_original_current * 1.0F - price_original_current * 1.0F * price_setting_value / 100);
                    return this.roundPrice(price);
                } else if (PriceDataType.MONEY.getCode().equals(price_data_type)) {
                    return ConvertUtils.toDouble(Math.max(0, price_original_current - price_setting_value));
                }
            }
            case CONSTANT: {
                return ConvertUtils.toDouble(price_setting_value);
            }
            case CONTACT: {
                return -1.0;
            }
            default:
                return null;
        }
    }

    public String getAgencyOrderDeptCode(
            String code,
            int order_data_index,
            int total,
            boolean is_hunt_sale,
            boolean has_order_normal) {
        try {
            if (total == 0) {
                return code;
            }
            if (is_hunt_sale) {
                if (has_order_normal) {
                    return code + "-S" + String.format("%02d", order_data_index + 1) + "/" + String.format("%02d", total);
                } else {
                    return code + "-S" + String.format("%02d", order_data_index) + "/" + String.format("%02d", total);
                }
            } else {
                return code + "-T01" + "/" + String.format("%02d", total);
            }
        } catch (Exception e) {

        }
        return code;
    }

    public String getAgencyOrderDeptCodeV2(String code, int order_data_index, int total, boolean is_hunt_sale) {
        try {
            if (is_hunt_sale) {
                return code + "-S" + String.format("%02d", order_data_index + 1) + "/" + String.format("%02d", total);
            } else {
                return code + "-T01" + "/" + String.format("%02d", total);
            }
        } catch (Exception e) {

        }
        return code;
    }

    public boolean isOrderNormal(int promo_id) {
        if (promo_id == 0) {
            return true;
        }
        return false;
    }

    public double getMoneyDiscount(int offer_value, long rank_value) {
        double percent = ConvertUtils.toDouble(
                new DecimalFormat("#0.00").format(
                        offer_value * 1.0F / 100));
        return this.roundPrice(rank_value * percent);
    }

    public static int minDay(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        int minDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        return minDay;
    }

    public static int maxDay(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        return maxDay;
    }

    public Date getMissionPeriodEndTime(Date startDate, MissionPeriodType mp,
                                        String time_tuan_data, String time_thang_data) {
        switch (mp) {
            case TUAN:
                return this.getMissionPeriodEndTimeOfTuan(
                        time_tuan_data, startDate, time_thang_data
                );
            case THANG:
                return getMissionPeriodEndTimeOfThang(
                        time_tuan_data, startDate, time_thang_data);
            case QUY:
                return this.getMissionPeriodEndTimeOfQuy(
                        time_tuan_data, startDate, time_thang_data);
        }
        return null;
    }

    public Date getMissionPeriodEndTimeOfTuan(String time_tuan_data, Date startDate, String time_thang_data) {
        try {
            int day_circle = 7;
            String[] time = time_tuan_data.split("-");
            String[] time_thang = time_thang_data.split("-");
            int hour_config = ConvertUtils.toInt(time[0].split(":")[0]);
            int minute_config = ConvertUtils.toInt(time[0].split(":")[1]);
            int day_of_week_config = ConvertUtils.toInt(time[1]);
            int month = ConvertUtils.toInt(this.getMonth(startDate));
            int year = ConvertUtils.toInt(this.getYear(startDate));
            int day = ConvertUtils.toInt(this.getDay(startDate));
            int day_of_week = getDateOfWeek(startDate);
            int last_day_of_month = this.getLastDayOfMonth(startDate);
            if (ConvertUtils.toInt(time_thang[1]) != 0) {
                last_day_of_month = ConvertUtils.toInt(time_thang[1]);
            }

            if (day_of_week == day_of_week_config) {
                int hour = ConvertUtils.toInt(this.getHour(startDate));
                int minute = ConvertUtils.toInt(this.getMinute(startDate));
                if (hour > hour_config || (hour == hour_config && minute >= minute_config)) {
                    if (day + day_circle > last_day_of_month) {
                        Date end_date = DateTimeUtils.getDateTime(
                                year + "-" + String.format("%02d", month) + "-" + last_day_of_month + " 23:59:59",
                                "yyyy-MM-dd HH:mm:ss");
                        return end_date;
                    } else {
                        Date end_date = DateTimeUtils.getDateTime(
                                year + "-" + String.format("%02d", month) + "-" + (day + day_circle) + " " + time[0] + ":00",
                                "yyyy-MM-dd HH:mm:ss");
                        return end_date;
                    }
                } else {
                    return DateTimeUtils.getDateTime(
                            year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day) + " " + time[0] + ":00",
                            "yyyy-MM-dd HH:mm:ss");
                }
            } else if (day_of_week != day_of_week_config) {
                Date end_date = this.getNextDateOfDayOfWeek(startDate, day_circle, ConvertUtils.toInt(time[1]));
                if (ConvertUtils.toInt(getMonth(end_date)) != month) {
                    end_date = DateTimeUtils.getDateTime(
                            year + "-" + String.format("%02d", month) + "-" + last_day_of_month + " 23:59:59",
                            "yyyy-MM-dd HH:mm:ss");
                    return end_date;
                }
                return DateTimeUtils.getDateTime(
                        year + "-" + String.format("%02d", month) + "-" + String.format("%02d", ConvertUtils.toInt(getDay(end_date))) + " " + time[0] + ":00",
                        "yyyy-MM-dd HH:mm:ss");
            }
        } catch (Exception e) {
            LogUtil.printDebug(Module.MISSION.name(), e);
        }
        return null;
    }

    public Date getMissionPeriodEndTimeOfThang(String time_tuan_data, Date startDate, String time_thang_data) {
        try {
            int day_circle = 7;
            String[] time = time_tuan_data.split("-");
            String[] time_thang = time_thang_data.split("-");
            int day_of_week_config = ConvertUtils.toInt(time[1]);
            int month = ConvertUtils.toInt(this.getMonth(startDate));
            int year = ConvertUtils.toInt(this.getYear(startDate));
            int day = ConvertUtils.toInt(this.getDay(startDate));
            int day_of_week = getDateOfWeek(startDate);
            int last_day_of_month = this.getLastDayOfMonth(startDate);
            if (ConvertUtils.toInt(time_thang[1]) != 0) {
                last_day_of_month = ConvertUtils.toInt(time_thang[1]);
            }

            Date end_date = DateTimeUtils.getDateTime(
                    year + "-" + String.format("%02d", month) + "-" + String.format("%02d", last_day_of_month) + " 23:59:59",
                    "yyyy-MM-dd HH:mm:ss");
            return end_date;
        } catch (Exception e) {
            LogUtil.printDebug(Module.MISSION.name(), e);
        }
        return null;
    }

    public Date getMissionPeriodEndTLTimeOfThang(String time_tuan_data, Date startDate, String time_thang_data) {
        try {
            int day_circle = 7;
            String[] time = time_tuan_data.split("-");
            String[] time_thang = time_thang_data.split("-");
            int day_of_week_config = ConvertUtils.toInt(time[1]);
            int month = ConvertUtils.toInt(this.getMonth(startDate));
            int year = ConvertUtils.toInt(this.getYear(startDate));
            int day = ConvertUtils.toInt(this.getDay(startDate));
            int day_of_week = getDateOfWeek(startDate);
            int last_day_of_month = this.getLastDayOfMonth(startDate);
            if (ConvertUtils.toInt(time_thang[1]) != 0) {
                last_day_of_month = ConvertUtils.toInt(time_thang[1]);
            }

            Date end_date = DateTimeUtils.getDateTime(
                    year + "-" + String.format("%02d", month) + "-" + String.format("%02d", last_day_of_month) + " " + time_thang[0] + ":00",
                    "yyyy-MM-dd HH:mm:ss");
            return end_date;
        } catch (Exception e) {
            LogUtil.printDebug(Module.MISSION.name(), e);
        }
        return null;
    }

    public int getLastDayOfMonth(Date date) {
        date = DateTimeUtils.getDateTime(
                DateTimeUtils.toString(date, "yyyy-MM") + "-01 00:00:00",
                "yyyy-MM-dd HH:mm:ss"
        );
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DATE, -1);
        return ConvertUtils.toInt(DateTimeUtils.toString(cal.getTime(), "dd"));
    }

    public Date getMissionPeriodEndTimeOfQuy(String time_tuan_data, Date startDate, String time_thang_data) {
        try {
            int day_circle = 7;

            String[] time = time_tuan_data.split("-");
            String[] time_thang = time_thang_data.split("-");
            String[] time_quy = time_thang_data.split("-");
            int day_of_week_config = ConvertUtils.toInt(time[1]);
            int month = ConvertUtils.toInt(this.getMonth(startDate));
            int year = ConvertUtils.toInt(this.getYear(startDate));
            int day = ConvertUtils.toInt(this.getDay(startDate));
            int day_of_week = getDateOfWeek(startDate);
            int last_day_of_month = this.getLastDayOfMonth(startDate);
            if (ConvertUtils.toInt(time_thang[1]) != 0) {
                last_day_of_month = ConvertUtils.toInt(time_thang[1]);
            }

            Date end_date = DateTimeUtils.getDateTime(
                    year + "-" + String.format("%02d", month) + "-" + last_day_of_month + " 23:59:59",
                    "yyyy-MM-dd HH:mm:ss");
            return end_date;
        } catch (Exception e) {
            LogUtil.printDebug(Module.MISSION.name(), e);
        }
        return null;
    }

    private Date getNextDateOfDayOfWeek(Date startDate, int day_circle, int day_of_week_config) {
        for (int i = 0; i < day_circle; i++) {
            Date nextDate = this.getDateAfterDay(startDate, i + 1);
            if (getDateOfWeek(nextDate) == day_of_week_config) {
                return nextDate;
            }
        }
        return null;
    }

    public String getHour(Date date) {
        return DateTimeUtils.toString(date, "HH");
    }

    public String getMinute(Date date) {
        return DateTimeUtils.toString(date, "mm");
    }

    public Date getDateAfterHour(Date date, int payment_duration_app) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, payment_duration_app);
        return calendar.getTime();
    }

    public Date getDateAfterMinute(Date date, int payment_duration_app) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, payment_duration_app);
        return calendar.getTime();
    }
}