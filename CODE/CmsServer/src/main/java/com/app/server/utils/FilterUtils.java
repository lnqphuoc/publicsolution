package com.app.server.utils;

import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.FilterListByIdRequest;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.SortByRequest;
import com.app.server.data.request.promo.PromoTimeRequest;
import com.app.server.enums.FunctionList;
import com.google.common.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class FilterUtils {
    public String getQuery(FunctionList functionList, List<FilterRequest> filters, List<SortByRequest> sorts) {
        StringBuilder query = new StringBuilder(functionList.getSql());
        if (filters.size() != 0) {
            if (!query.toString().toUpperCase(Locale.ROOT).contains("WHERE")) {
                query.append(" WHERE 1 = 1");
            }

            for (int i = 0; i < filters.size(); i++) {
                FilterRequest filter = filters.get(i);
                if (!functionList.getFilterBy().isEmpty()
                        && !functionList.getFilterBy().equals("[]")
                        && !functionList.getFilterBy().contains(filter.getKey())) {
                    continue;
                }

                query.append(" AND ");

                String value = "";

                if (CheckValueUtils.isNumberic(ConvertUtils.toString(filter.getValue()))) {
                    value = "=" + ConvertUtils.toString(filter.getValue()) + " ";
                } else {
                    value = "  LIKE '" + ConvertUtils.toString(filter.getValue()) + "%' ";
                }
                if (filter.getType().equals(TypeFilter.SELECTBOX)
                        || filter.getType().equals(TypeFilter.CAS)) {
                    query.append(filter.getKey()).append(value);
                } else if (filter.getType().equals(TypeFilter.SEARCH)) {
                    String searchKey = functionList.getSearchDefault();
                    if (!filter.getKey().isEmpty()) {
                        searchKey = filter.getKey();
                    }

                    if (!searchKey.isEmpty() && !filter.getValue().isEmpty()) {
                        String[] stringKeys = searchKey.split(",");
                        String searchQuery = "";
                        for (String stringKey : stringKeys) {
                            if (!searchQuery.isEmpty()) {
                                searchQuery += " OR ";
                            }
                            searchQuery += stringKey + " LIKE '%" + filter.getValue().trim() + "%' ";
                        }
                        searchQuery = " ( " + searchQuery + " ) ";
                        query.append(searchQuery);
                    } else {
                        continue;
                    }
                } else if (filter.getType().equals(TypeFilter.DATE)) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    if (filter.getValue1() != null || filter.getValue2() != null) {
                        if (filter.getValue1() != null && filter.getValue2() == null) {
                            String time1 = dateFormat.format(new Date(filter.getValue1())) + " 00:00:00";
                            query.append(filter.getKey()).append(" >= '").append(time1).append("'");
                        } else if (filter.getValue1() == null && filter.getValue2() != null) {
                            String time2 = dateFormat.format(new Date(filter.getValue2())) + " 23:59:59";
                            query.append(filter.getKey()).append(" <= '").append(time2).append("'");
                        } else {
                            String time1 = dateFormat.format(new Date(filter.getValue1())) + " 00:00:00";
                            String time2 = dateFormat.format(new Date(filter.getValue2())) + " 23:59:59";
                            query.append(filter.getKey()).append(" >= '").append(time1).append("' AND ").append(filter.getKey()).append("<='").append(time2).append("'");
                        }
                    } else {
                        continue;
                    }
                } else if (filter.getType().equals(TypeFilter.FROM_TO)) {
                    if (filter.getValue1() != null || filter.getValue2() != null) {
                        if (filter.getValue1() != null && filter.getValue2() == null) {
                            query.append(filter.getKey()).append(">='").append(filter.getValue1()).append("'");
                        } else if (filter.getValue1() == null && filter.getValue2() != null) {
                            query.append(filter.getKey()).append("<='").append(filter.getValue2()).append("'");
                        } else {
                            query.append(filter.getKey()).append(">='").append(filter.getValue1()).append("' AND ").append(filter.getKey()).append("<='").append(filter.getValue2()).append("'");
                        }
                    } else {
                        continue;
                    }
                } else if (filter.getType().equals(TypeFilter.LIKE)) {
                    query.append("'" + filter.getValue()).append("' LIKE ").append(filter.getKey()).append("");
                } else if (filter.getType().equals(TypeFilter.NOT_LIKE)) {
                    query.append("'" + filter.getValue()).append("' NOT LIKE ").append(filter.getKey()).append("");
                } else if (filter.getType().equals(TypeFilter.SQL)) {
                    query.append(filter.getValue());
                }
            }
        }

        if (sorts != null && !sorts.isEmpty()) {
            if (!query.toString().toUpperCase(Locale.ROOT).contains("ORDER BY")) {
                query.append(" ORDER BY");
            }

            for (int i = 0; i < sorts.size(); i++) {
                query.append(" " + sorts.get(i).getKey() + " " + sorts.get(i).getType());
                if (i < sorts.size() - 1) {
                    query.append(",");
                }
            }
        } else {
            if (functionList.getSortDefault().isEmpty()) {
                query.append(" ORDER BY id DESC");
            } else {
                query.append(" ORDER BY " + functionList.getSortDefault());

            }
        }
//        LogUtil.printDebug(query.toString());
        return query.toString();
    }

    public String getValueByKey(List<FilterRequest> filters, String key) {
        for (FilterRequest request : filters) {
            if (request.getKey().equals(key)) {
                return request.getValue();
            }
        }
        return "";
    }

    public String getValueByType(List<FilterRequest> filters, String type) {
        for (FilterRequest request : filters) {
            if (request.getType().equals(type)) {
                return request.getValue();
            }
        }
        return "";
    }

    public PromoTimeRequest getValueByTime(List<FilterRequest> filters, String key) {
        for (FilterRequest request : filters) {
            if (request.getKey().equals(key)) {
                PromoTimeRequest promoTimeRequest = new PromoTimeRequest();
                promoTimeRequest.setStart_date_millisecond(
                        request.getValue1() != null ? request.getValue1() : 0
                );
                promoTimeRequest.setEnd_date_millisecond(
                        request.getValue2() != null ? request.getValue2() : 0
                );
                return promoTimeRequest;
            }
        }
        return null;
    }

    public String getQueryV2(FunctionList functionList, List<FilterRequest> filters, List<SortByRequest> sorts) {
        StringBuilder query = new StringBuilder(functionList.getSql());
        if (filters.size() != 0) {
            if (!query.toString().toUpperCase(Locale.ROOT).contains("WHERE")) {
                query.append(" WHERE 1 = 1");
            }

            for (int i = 0; i < filters.size(); i++) {
                FilterRequest filter = filters.get(i);
                if (!functionList.getFilterBy().isEmpty()
                        && !functionList.getFilterBy().equals("[]")
                        && !functionList.getFilterBy().contains(filter.getKey())) {
                    continue;
                }

                query.append(" AND ");

                String value = "";

                if (CheckValueUtils.isNumberic(ConvertUtils.toString(filter.getValue()))) {
                    value = "=" + ConvertUtils.toString(filter.getValue()) + " ";
                } else {
                    value = "  LIKE '" + ConvertUtils.toString(filter.getValue()) + "%' ";
                }
                if (filter.getType().equals(TypeFilter.SELECTBOX)
                        || filter.getType().equals(TypeFilter.CAS)) {
                    query.append(filter.getKey()).append(value);
                } else if (filter.getType().equals(TypeFilter.SEARCH)) {
                    String searchKey = functionList.getSearchDefault();
                    if (!filter.getKey().isEmpty()) {
                        searchKey = filter.getKey();
                    }

                    if (!searchKey.isEmpty() && !filter.getValue().isEmpty()) {
                        String[] stringKeys = searchKey.split(",");
                        String searchQuery = "";
                        for (String stringKey : stringKeys) {
                            if (!searchQuery.isEmpty()) {
                                searchQuery += " OR ";
                            }
                            searchQuery += stringKey + " LIKE '%" + filter.getValue().trim() + "%' ";
                        }
                        searchQuery = " ( " + searchQuery + " ) ";
                        query.append(searchQuery);
                    } else {
                        continue;
                    }
                } else if (filter.getType().equals(TypeFilter.DATE)) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    if (filter.getValue1() != null || filter.getValue2() != null) {
                        if (filter.getValue1() != null && filter.getValue2() == null) {
                            String time1 = dateFormat.format(new Date(filter.getValue1())) + " 00:00:00";
                            query.append(filter.getKey()).append(" >= '").append(time1).append("'");
                        } else if (filter.getValue1() == null && filter.getValue2() != null) {
                            String time2 = dateFormat.format(new Date(filter.getValue2())) + " 23:59:59";
                            query.append(filter.getKey()).append(" <= '").append(time2).append("'");
                        } else {
                            String time1 = dateFormat.format(new Date(filter.getValue1())) + " 00:00:00";
                            String time2 = dateFormat.format(new Date(filter.getValue2())) + " 23:59:59";
                            query.append(filter.getKey()).append(" >= '").append(time1).append("' AND ").append(filter.getKey()).append("<='").append(time2).append("'");
                        }
                    } else {
                        continue;
                    }
                } else if (filter.getType().equals(TypeFilter.FROM_TO)) {
                    if (filter.getValue1() != null || filter.getValue2() != null) {
                        if (filter.getValue1() != null && filter.getValue2() == null) {
                            query.append(filter.getKey()).append(">='").append(filter.getValue1()).append("'");
                        } else if (filter.getValue1() == null && filter.getValue2() != null) {
                            query.append(filter.getKey()).append("<='").append(filter.getValue2()).append("'");
                        } else {
                            query.append(filter.getKey()).append(">='").append(filter.getValue1()).append("' AND ").append(filter.getKey()).append("<='").append(filter.getValue2()).append("'");
                        }
                    } else {
                        continue;
                    }
                } else if (filter.getType().equals(TypeFilter.LIKE)) {
                    query.append("'" + filter.getValue()).append("' LIKE ").append(filter.getKey()).append("");
                } else if (filter.getType().equals(TypeFilter.NOT_LIKE)) {
                    query.append("'" + filter.getValue()).append("' NOT LIKE ").append(filter.getKey()).append("");
                } else if (filter.getType().equals(TypeFilter.SQL)) {
                    query.append(filter.getValue());
                }
            }
        }

        if (sorts != null && !sorts.isEmpty()) {
            if (!query.toString().toUpperCase(Locale.ROOT).contains("ORDER BY")) {
                query.append(" ORDER BY");
            }

            for (int i = 0; i < sorts.size(); i++) {
                query.append(" " + sorts.get(i).getKey() + " " + sorts.get(i).getType());
                if (i < sorts.size() - 1) {
                    query.append(",");
                }
            }

            query.append(", id ASC");
        } else {
            if (functionList.getSortDefault().isEmpty()) {
                query.append(" ORDER BY id ASC");
            } else {
                query.append(" ORDER BY " + functionList.getSortDefault());
            }
        }
//        LogUtil.printDebug(query.toString());
        return query.toString();
    }

    public void parseFilter(FunctionList functionList, FilterListRequest request) {
        try {
            if (functionList.getFilterBy().isEmpty() || functionList.getFilterBy().equals("[]")) {
                return;
            }

            Map<String, String> mpFilter = new HashMap<>();
            String[] filterList = functionList.getFilterBy().split(",");

            Arrays.stream(filterList).forEach(
                    x -> {
                        String[] strings = x.split(":");
                        mpFilter.put(strings[0], strings[1]);
                    }
            );

            for (FilterRequest filterRequest : request.getFilters()) {
                String key = mpFilter.get(filterRequest.getKey());
                if (key != null) {
                    filterRequest.setKey(key);
                }
            }
        } catch (Exception e) {
            LogUtil.printDebug("", e);
        }
    }
}