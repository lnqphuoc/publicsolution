package com.app.server.utils;

public class SortUtils {
    public static String convertProductCodeToProductSortData(String productCode) {
        String data = "";
        for (int i = 0; i < productCode.length(); i++) {
            int number = convertCharToNumber(productCode.charAt(i));
            data += number;
        }
        return data;
    }

    // convert char to number value
    private static int convertCharToNumber(char c) {
        c = Character.toLowerCase(c);
        String template = "0123456789abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < template.length(); i++) {
            char tmp = template.charAt(i);
            if (c == tmp)
                return (i + 11);
        }
        return 10;
    }
}