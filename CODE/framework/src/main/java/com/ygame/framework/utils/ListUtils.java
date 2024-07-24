/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author huynxt
 */
public class ListUtils {
    
    public static List<String> pagingListString(List<String> list, int offset, int count) {
        List<String> result = new ArrayList<>();
        if (list != null) {
            int lsCount = list.size();
            if (offset > lsCount) {
                return result;
            }
            int fromIndex = offset < 0 ? 0 : offset;
            int toIndex = offset + count;
            toIndex = toIndex > lsCount ? lsCount : toIndex;
            result = list.subList(fromIndex, toIndex);
        }
        return result;
    }
    
    public static List<Long> pagingListLong(List<Long> list, int offset, int count) {
        List<Long> result = new ArrayList<>();
        if (list != null) {
            int lsCount = list.size();
            if (offset > lsCount) {
                return result;
            }
            int fromIndex = offset < 0 ? 0 : offset;
            int toIndex = offset + count;
            toIndex = toIndex > lsCount ? lsCount : toIndex;
            result = list.subList(fromIndex, toIndex);
        }
        return result;
    }
}
