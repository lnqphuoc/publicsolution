package com.app.server.service;

import com.app.server.config.ConfigInfo;
import com.app.server.constants.ResponseMessage;
import com.app.server.data.SessionData;
import com.app.server.data.dto.product.Category;
import com.app.server.data.dto.product.ProductCache;
import com.app.server.data.dto.product.ProductGroup;
import com.app.server.data.dto.warehouse.WarehouseBasicData;
import com.app.server.data.entity.AgencyEntity;
import com.app.server.data.entity.ProductSmallUnitEntity;
import com.app.server.data.extra.TypeFilter;
import com.app.server.data.request.FilterListRequest;
import com.app.server.data.request.FilterRequest;
import com.app.server.data.request.SortByRequest;
import com.app.server.data.request.export.ExportRequest;
import com.app.server.data.request.promo.PromoTimeRequest;
import com.app.server.database.LogDB;
import com.app.server.enums.*;
import com.app.server.response.ClientResponse;
import com.app.server.utils.AppUtils;
import com.app.server.utils.CheckValueUtils;
import com.app.server.utils.ExcelExporter;
import com.app.server.utils.JsonUtils;
import com.google.common.reflect.TypeToken;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.ConvertUtils;
import com.ygame.framework.utils.DateTimeUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ExportService extends BaseService {
    private LogDB logDB;

    @Autowired
    public void setLogDB(LogDB logDB) {
        this.logDB = logDB;
    }

    public ResponseEntity<?> exportInventory(
            SessionData sessionData,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            String fileName = date + ".csv";
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + "-" + date + ".zip");

//            OutputStream outputStream= response.getOutputStream();

            ZipOutputStream outputStream = new ZipOutputStream(response.getOutputStream());
            outputStream.putNextEntry(new ZipEntry(fileName));
            this.addFilterAgencyData(
                    sessionData,
                    JsonUtils.DeSerialize(
                            JsonUtils.Serialize(request),
                            FilterListRequest.class)
            );

            String query = this.filterUtils.getQuery(FunctionList.LIST_AGENCY, request.getFilters(), request.getSorts());
            List<JSONObject> records = this.agencyDB.filter(
                    query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE, 0);

            int total_rank = this.agencyDB.getTotalRank();
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.AVATAR.getImageUrl());
                int agency_id = ConvertUtils.toInt(js.get("id"));

                int status = ConvertUtils.toInt(js.get("status"));
                if (status == AgencyStatus.WAITING_APPROVE.getValue()) {
                    js.put("membership_id", 0);
                }
                js.put("total_rank_value", total_rank);
                js.put("dept_info", this.getDeptInfo(agency_id));
            }
            int total = this.agencyDB.getTotalAgency(query);

            List<JSONObject> listField = new ArrayList<>();

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);

            //Count total
            //Limit config cvs
            records.forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();
                    headerKey.forEach(k -> {
                        dataRecord.add(d.get(k) == null ? "" : d.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();


            outputStream.closeEntry();

            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }

    }

    public ResponseEntity<?> download(String data,
                                      HttpServletResponse response) {
        try {
            ExportRequest request = JsonUtils.DeSerialize(
                    this.appUtils.decodeJWT(data),
                    ExportRequest.class
            );
            if (request == null) {
                return ResponseEntity.status(400).body(null);
            }

            ExportType exportType = ExportType.from(request.getType());
            switch (exportType) {
                case REPORT_INVENTORY:
                    return this.downloadReportInventory(exportType.getName(), request, response);
                case REPORT_AGENCY:
                    return this.downloadReportAgency(exportType.getName(), request, response);
                case REPORT_PRODUCT_PRICE_TIMER:
                    return this.downloadReportProductPriceTimer(exportType.getName(), request, response);
                case EXPORT_ORDER:
                    return this.downloadReportOrder(exportType.getName(), request, response);
                case REPORT_ORDER_TEMP:
                    return this.downloadReportOrderTemp(exportType.getName(), request, response);
                case REPORT_TK_CTTL:
                    return this.downloadReportTKCTTL(exportType.getName(), request, response);
                case REPORT_AGENCY_ACCESS_APP:
                    return this.downloadExportAgencyAccessApp(exportType.getName(), request, response);
                case REPORT_WAREHOUSE_EXPORT_HISTORY:
                    return this.downloadReportWarehouseExportHistory(exportType.getName(), request, response);
                case REPORT_WAREHOUSE_IMPORT_HISTORY:
                    return this.downloadReportWarehouseImportHistory(exportType.getName(), request, response);
                case REPORT_PRODUCT_VISIBILITY:
                    return this.downloadProductVisibility(exportType.getName(), request, response);
                case REPORT_PRODUCT:
                    return this.downloadProduct(exportType.getName(), request, response);
                case REPORT_PRODUCT_GROUP:
                    return this.downloadProductGroup(exportType.getName(), request, response);
                case REPORT_CATEGORY:
                    return this.downloadCategory(exportType.getName(), request, response);
                case REPORT_CATALOG:
                    return this.downloadCatalog(exportType.getName(), request, response);
                case EXPORT_ORDER_CONFIRM_PRODUCT:
                    return this.downloadOrderConfirmProduct(exportType.getName(), request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(400).body(null);
    }

    private ResponseEntity<?> downloadReportInventory(String file_name, ExportRequest request, HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();

//            ZipOutputStream outputStream = new ZipOutputStream(response.getOutputStream());
//            outputStream.putNextEntry(new ZipEntry(file_name + ".xlsx"));

            List<JSONObject> records;
            if (request.getFilters().isEmpty()) {
                String query = this.filterUtils.getQuery(
                        FunctionList.LIST_WAREHOUSE_REPORT,
                        request.getFilters(),
                        request.getSorts());
                records = this.agencyDB.filter(
                        query,
                        this.appUtils.getOffset(request.getPage()),
                        ConfigInfo.PAGE_SIZE, 0);
            } else {
                records = this.filterWarehouseReport(
                        JsonUtils.DeSerialize(
                                JsonUtils.Serialize(request),
                                FilterListRequest.class)
                );
            }
            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"product_code\":\"Mã sản phẩm\"}," +
                    "{\"full_name\":\"Tên sản phẩm\"}," +
                    "{\"don_vi_tinh\":\"Đơn vị tính\"}," +
                    "{\"product_status_name\":\"Trạng thái\"}," +
                    "{\"quantity_start_today\":\"Tồn đầu kỳ\"}," +
                    "{\"quantity_import_today\":\"Nhập trong kỳ\"}," +
                    "{\"quantity_export_today\":\"Xuất trong kỳ\"}," +
                    "{\"quantity_reality\":\"Tồn thực tế\"}," +
                    "{\"quantity_waiting_ship_today\":\"Chờ giao\"}," +
                    "{\"quantity_waiting_approve_today\":\"Chờ xác nhận\"}," +
                    "{\"quantity_availability\":\"Tồn khả dụng\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);

            //Count total
            //Limit config cvs
            records.forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();
                    headerKey.forEach(k -> {
                        dataRecord.add(d.get(k) == null ? "" : d.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    public ClientResponse getLink(SessionData sessionData, ExportRequest request) {
        try {
            ClientResponse crValidate = request.validate();
            if (crValidate.failed()) {
                return crValidate;
            }

            request.setTime(DateTimeUtils.getMilisecondsNow());
            String link = this.appUtils.createJWT(
                    JsonUtils.Serialize(request)
            );
            JSONObject data = new JSONObject();
            data.put("link", link);
            return ClientResponse.success(data);
        } catch (Exception ex) {
            LogUtil.printDebug("EXPORT", ex);
        }
        return ClientResponse.fail(ResponseStatus.FAIL, ResponseMessage.FAIL);
    }

    private ResponseEntity<?> downloadReportAgency(String file_name, ExportRequest request, HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();

//            ZipOutputStream outputStream = new ZipOutputStream(response.getOutputStream());
//            outputStream.putNextEntry(new ZipEntry(file_name + ".xlsx"));
            FilterListRequest filterListRequest = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(request),
                    FilterListRequest.class);
            this.addFilterAgencyData(
                    request.getSessionData(),
                    filterListRequest
            );

            String query = this.filterUtils.getQuery(
                    FunctionList.LIST_AGENCY,
                    filterListRequest.getFilters(),
                    filterListRequest.getSorts());
            List<JSONObject> records = this.agencyDB.filter(
                    query,
                    this.appUtils.getOffset(filterListRequest.getPage()),
                    ConfigInfo.PAGE_SIZE, 0);


            int total = this.warehouseDB.getTotal(query);

            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"ma_dai_ly\":\"Mã đại lý\"}," +
                    "{\"ten_dai_li\":\"Tên đại lý\"}," +
                    "{\"trang_thai\":\"Trạng thái\"}," +
                    "{\"cap_bac\":\"Cấp bậc\"}," +
                    "{\"dtt\":\"DTT\"}," +
                    "{\"tien_thu\":\"Tiền thu\"}," +
                    "{\"a_coin\":\"A-coin\"}," +
                    "{\"hmn\":\"HMN\"}," +
                    "{\"hmgd\":\"HMGD\"}," +
                    "{\"hmkd\":\"HMKD\"}," +
                    "{\"cnck\":\"CNCK\"}," +
                    "{\"nqh\":\"NQH\"}," +
                    "{\"han_no\":\"Hạn nợ\"}," +
                    "{\"hang_khach_hang\":\"Hạng khách hàng\"}," +
                    "{\"phong_kinh_doanh\":\"Phòng kinh doanh\"}," +
                    "{\"khu_vuc_kinh_doanh\":\"Khu vực kinh doanh\"}," +
                    "{\"ngay_duyet\":\"Ngày đăng ký\"}," +
                    "{\"ngay_dang_ky\":\"Ngày duyệt\"}," +
                    "{\"dia_chi\":\"Địa chỉ\"}," +
                    "{\"tinh_thanh\":\"Tỉnh thành\"}," +
                    "{\"quan_huyen\":\"Quận huyện\"}," +
                    "{\"phuong_xa\":\"Phường xã\"}," +
                    "{\"nguoi_dai_dien\":\"Người đại diện\"}," +
                    "{\"gioi_tinh\":\"Giới tính\"}," +
                    "{\"ngay_sinh\":\"Ngày sinh\"}," +
                    "{\"email\":\"Email\"}," +
                    "{\"nganh_hang_chu_luc\":\"Ngành hàng chủ lực\"}," +
                    "{\"loai_hinh_kinh_doanh\":\"Loại hình kinh doanh\"}," +
                    "{\"so_dien_thoai\":\"Số điện thoại\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            int total_rank = this.agencyDB.getTotalRank();
            for (JSONObject js : records) {
                js.put("image_url", ImagePath.AVATAR.getImageUrl());
                int agency_id = ConvertUtils.toInt(js.get("id"));
                int status = ConvertUtils.toInt(js.get("status"));
                if (status == AgencyStatus.WAITING_APPROVE.getValue()) {
                    js.put("membership_id", 0);
                }
                js.put("total_rank_value", total_rank);

                js.put("ma_dai_ly", js.get("code"));
                js.put("ten_dai_li", js.get("shop_name"));
                js.put("trang_thai", AgencyStatus.getNameFrom(
                        ConvertUtils.toInt(js.get("status"))
                ));
                MembershipType membershipType = MembershipType.from(
                        ConvertUtils.toInt(js.get("membership_id")));
                js.put("cap_bac", membershipType == null ? "-" : membershipType.getValue());

                js.put("hang_khach_hang",
                        (membershipType == null || membershipType.getKey() == MembershipType.THANH_VIEN.getKey()) ? "-" :
                                (js.get("rank_value").toString() + "/" + total_rank));
                js.put("phong_kinh_doanh", ConvertUtils.toInt(js.get("business_department_id")) == 0 ? "-" :
                        this.dataManager.getProductManager().
                                getMpBusinessDepartment().get(
                                        ConvertUtils.toInt(js.get("business_department_id"))).getName());
                js.put("khu_vuc_kinh_doanh", this.dataManager.getProductManager().getMpRegion().get(
                        ConvertUtils.toInt(js.get("region_id"))
                ).getName());
                js.put("ngay_dang_ky", js.get("created_date"));
                js.put("ngay_duyet", js.get("approved_date"));

                //"{\"dia_chi\":\"Địa chỉ\"}," +
                js.put("dia_chi", ConvertUtils.toString(js.get("address")));
//                        "{\"tinh_thanh\":\"Tỉnh thành\"}," +
                js.put("tinh_thanh", this.dataManager.getProductManager().getCityNameById(
                        ConvertUtils.toInt(js.get("city_id"))));
//                        "{\"quan_huyen\":\"Quận huyện\"}," +
                js.put("quan_huyen", this.dataManager.getProductManager().getDistrictNameById(
                        ConvertUtils.toInt(js.get("district_id"))));
//                        "{\"phuong_xa\":\"Phường xã\"}," +
                js.put("phuong_xa", this.dataManager.getProductManager().getWardNameById(
                        ConvertUtils.toInt(js.get("ward_id"))));
//                        "{\"nguoi_dai_dien\":\"Người đại diện\"}," +
                js.put("nguoi_dai_dien", js.get("full_name"));
//                        "{\"gioi_tinh\":\"Giới tính\"}," +
                js.put("gioi_tinh", ConvertUtils.toInt(js.get("gender")) == 0 ? "-" :
                        ConvertUtils.toInt(js.get("gender")) == 1 ? "Nam" : "Nữ");
//                        "{\"ngay_sinh\":\"Ngày sinh\"}," +
                js.put("ngay_sinh", js.get("birthday"));
//                        "{\"email\":\"Email\"}," +
                js.put("email", js.get("email"));
//                        "{\"nganh_hang_chu_luc\":\"Ngành hàng chủ lực\"}," +
//               1 : Nông nghiệp, 2 - Máy và thiết bị khác, 3-Xây dựng
                js.put("nganh_hang_chu_luc", ConvertUtils.toInt(js.get("mainstay_industry_id")) == 1 ? "Nông nghiệp" :
                        ConvertUtils.toInt(js.get("mainstay_industry_id")) == 2 ? "Máy và thiết bị khác" :
                                ConvertUtils.toInt(js.get("mainstay_industry_id")) == 3 ? "Xây dựng" : "-");

//                loại hình kinh doanh: 0-chưa xác định, 1-Sỉ, 2-Lẻ, 3: cả hai
//                "{\"loai_hinh_kinh_doanh\":\"Loại hình kinh doanh\"}" +
                js.put("loai_hinh_kinh_doanh", ConvertUtils.toInt(js.get("business_type")) == 1 ? "Sỉ" :
                        ConvertUtils.toInt(js.get("business_type")) == 2 ? "Lẻ" :
                                ConvertUtils.toInt(js.get("business_type")) == 3 ? "Cả hai" : "Chưa xác định");
//                so_dien_thoai
                js.put("so_dien_thoai", ConvertUtils.toString(js.get("phone")));
                JSONObject jsDeptInfo = this.getDeptInfo(agency_id);
                if (jsDeptInfo != null) {
                    js.put("dtt", jsDeptInfo.get("total_dtt_cycle"));
                    js.put("tien_thu", jsDeptInfo.get("total_tt_cycle"));
                    js.put("a_coin", js.get("current_point"));
                    js.put("hmn", jsDeptInfo.get("dept_limit"));
                    js.put("hmgd", jsDeptInfo.get("hmgd"));
                    js.put("hmkd", jsDeptInfo.get("hmkd"));
                    js.put("cnck", jsDeptInfo.get("cno"));
                    js.put("nqh", jsDeptInfo.get("nqh"));
                    js.put("han_no", jsDeptInfo.get("dept_cycle"));
                }
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);

            //Count total
            //Limit config cvs
            records.forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();
                    headerKey.forEach(k -> {
                        dataRecord.add(
                                d.get(k) == null ? "" :
                                        d.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    private ResponseEntity<?> downloadReportProductPriceTimer(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();

            String query = this.filterUtils.getQuery(
                    FunctionList.REPORT_PRODUCT_PRICE_TIMER_DETAIL,
                    request.getFilters(),
                    request.getSorts());
            List<JSONObject> records = this.productDB.filter(
                    query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE, 0);

            int total = this.productDB.getTotal(query);

            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"ma_san_pham\":\"Mã sản phẩm\"}," +
                    "{\"ten_san_pham\":\"Tên sản phẩm\"}," +
                    "{\"gia\":\"Giá\"}," +
                    "{\"dvt\":\"Đơn vị tính\"}," +
                    "{\"ghi_chu\":\"Ghi chú\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            for (JSONObject js : records) {
                js.put("ma_san_pham", js.get("code"));
                js.put("ten_san_pham", js.get("full_name"));
                js.put("gia", js.get("price"));

                ProductSmallUnitEntity productSmallUnitEntity =
                        this.dataManager.getProductManager().getMpProductSmallUnit().get(
                                ConvertUtils.toInt(js.get("product_small_unit_id"))
                        );
                js.put("dvt", productSmallUnitEntity == null ? null : productSmallUnitEntity.getName());
                js.put("ghi_chu", js.get("note"));
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);

            //Count total
            //Limit config cvs
            records.forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();
                    headerKey.forEach(k -> {
                        dataRecord.add(
                                d.get(k) == null ? "" :
                                        d.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    private ResponseEntity<?> downloadReportOrder(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/xlsx");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".xlsx");

            OutputStream outputStream = response.getOutputStream();

            for (FilterRequest filterRequest : request.getFilters()) {
                if (filterRequest.getKey().equals("membership_id")) {
                    filterRequest.setKey("t_agency.membership_id");
                } else if (filterRequest.getKey().equals("status")) {
                    filterRequest.setKey("t.status");
                }
            }

            FilterListRequest filterListRequest = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(request),
                    FilterListRequest.class
            );

            this.addFilterOrderData(
                    request.getSessionData(),
                    filterListRequest
            );

            String query = this.filterUtils.getQuery(
                    FunctionList.REPORT_ORDER,
                    filterListRequest.getFilters(),
                    filterListRequest.getSorts());
            List<JSONObject> records = this.productDB.filter(
                    query,
                    this.appUtils.getOffset(filterListRequest.getPage()),
                    ConfigInfo.PAGE_SIZE, 0);

            int total = this.productDB.getTotal(query);

            String fields = "[" +
                    "{\"stt\":\"#\"}," +
                    "{\"po\":\"PO\"}," +
                    "{\"ten_dai_ly\":\"Customer\"}," +
                    "{\"tong_tien\":\"Amount\"}," +
                    "{\"request_date\":\"Delivery request\"}," +
                    "{\"note\":\"Note\"}," +
                    "{\"trang_thai\":\"Status\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            for (JSONObject js : records) {
//                        "{\"oc\":\"OC\"}," +
                js.put("po", js.get("code"));
//                        "{\"ten_dai_ly\":\"Customer\"}," +
                js.put("ten_dai_ly", js.get("agency_shop_name"));
//                        "{\"tong_tien\":\"Amount\"}," +
                js.put("tong_tien", js.get("total_end_price"));
//                        "{\"request_date\":\"Delivery request\"}," +
                js.put("request_date",
                        js.get("request_delivery_date") == null ? "" : DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(js.get("request_delivery_date").toString(), "yyyy-MM-dd HH:mm:ss"), "dd/MM/yyyy"));
//                        "{\"note\":\"Note\"}," +
                js.put("note", js.get("note_internal"));
//                        "{\"trang_thai\":\"Trạng thái\"}" +
                js.put("trang_thai",
                        js.get("status_name")
                );
            }
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("PO");

            exporter.writeHeaderLine(workbook, sheet, headerValue);

            AtomicInteger stt = new AtomicInteger(1);

            //Count total
            //Limit config cvs
            records.forEach(js -> {
                try {
                    List<Object> dataLines = new ArrayList<>();
                    int rowCount = stt.get();
                    dataLines.add(ConvertUtils.toString(rowCount));
                    //                        "{\"oc\":\"OC\"}," +
                    dataLines.add(ConvertUtils.toString(js.get("po")));
//                        "{\"ten_dai_ly\":\"Customer\"}," +
                    dataLines.add(ConvertUtils.toString(js.get("ten_dai_ly")));
//                        "{\"tong_tien\":\"Amount\"}," +
                    dataLines.add(ConvertUtils.toDouble(js.get("tong_tien")));
//                        "{\"request_date\":\"Delivery request\"}," +
                    dataLines.add(ConvertUtils.toString("request_date"));
//                        "{\"note\":\"Note\"}," +
                    dataLines.add(ConvertUtils.toString(js.get("note_internal")));
//                        "{\"trang_thai\":\"Trạng thái\"}" +
                    dataLines.add(ConvertUtils.toString(
                            js.get("trang_thai")
                    ));
                    exporter.writeDataLines(workbook, sheet, dataLines, rowCount);
                    stt.getAndIncrement();
                } catch (Exception e) {
                    LogUtil.printDebug("", e);
                }
            });
            workbook.write(outputStream);
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            LogUtil.printDebug("", e);
            return ResponseEntity.status(400).body(null);
        }
    }

    private ResponseEntity<?> downloadReportOrderTemp(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();

            String query = this.filterUtils.getQuery(
                    FunctionList.REPORT_ORDER_TEMP,
                    request.getFilters(),
                    request.getSorts());
            List<JSONObject> records = this.productDB.filter(
                    query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE, 0);

            int total = this.productDB.getTotal(query);

            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"ma_don_hang\":\"Mã đơn hàng\"}," +
                    "{\"ma_dai_ly\":\"Mã đại lý\"}," +
                    "{\"ten_dai_ly\":\"Tên đại lý\"}," +
                    "{\"tong_tien\":\"Tổng tiền\"}," +
                    "{\"ngay_tao_don\":\"Ngày tạo đơn\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            for (JSONObject js : records) {
                js.put("ma_don_hang", "TEMP" + ConvertUtils.toString(js.get("id")));
                js.put("ma_dai_ly", js.get("agency_code"));
                js.put("ten_dai_ly", js.get("agency_shop_name"));
                js.put("tong_tien", js.get("total_end_price"));
                js.put("ngay_tao_don", js.get("created_date"));
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);

            //Count total
            //Limit config cvs
            if (!headerKey.isEmpty()) {
                records.forEach(d -> {
                    try {
                        List<String> dataRecord = new ArrayList<>();
                        dataRecord.add(String.valueOf(stt.get()));
                        stt.getAndIncrement();
                        headerKey.forEach(k -> {
                            dataRecord.add(
                                    d.get(k) == null ? "" :
                                            d.get(k).toString());
                        });
                        csvPrinter.printRecord(dataRecord);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    private ResponseEntity<?> downloadReportTKCTTL(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();

            JSONObject jsCTTLStatistic = this.logDB.getOne(
                    "SELECT * FROM statistic_tl_program WHERE id = " + request.getId()
            );
            if (jsCTTLStatistic == null) {
                return ResponseEntity.status(200).body(null);
            }

            JSONObject jsCTTL = this.promoDB.getPromoJs(ConvertUtils.toInt(jsCTTLStatistic.get("program_id")));
            if (jsCTTL == null) {
                return ResponseEntity.status(200).body(null);
            }

            String query = "SELECT * FROM statistic_tl_program_detail" +
                    " WHERE statistic_tl_program_id = " + request.getId();

            List<JSONObject> records = this.logDB.filter(
                    query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE, 0);
            List<String> agencyIdList = records.stream().map(e -> e.get("agency_id").toString()
            ).collect(Collectors.toList());

            List<JSONObject> agencyList = this.agencyDB.getListAgencyInAgency(JsonUtils.Serialize(agencyIdList));
            Map<Integer, JSONObject> mpAgency = new ConcurrentHashMap<>();
            agencyList.forEach(
                    agency -> {
                        mpAgency.put(ConvertUtils.toInt(agency.get("id")), agency);
                    }
            );

            List<String> offer_type = JsonUtils.DeSerialize(
                    jsCTTL.get("offer_info").toString(),
                    new TypeToken<List<String>>() {
                    }.getType());
            PromoOfferType promoOfferType = PromoOfferType.from(offer_type.get(0));

            ProductCache jsGiftInfo = null;
            JSONObject promo_offer_bonus
                    = this.promoDB.getOnePromoOfferBonus(ConvertUtils.toInt(jsCTTLStatistic.get("program_id")));
            if (promo_offer_bonus != null &&
                    ConvertUtils.toInt(promo_offer_bonus.get("product_id")) != 0) {
                jsGiftInfo = this.dataManager.getProductManager().getProductBasicData(
                        ConvertUtils.toInt(promo_offer_bonus.get("product_id"))
                );
            }

            /**
             * Mã đại lý
             * Tên đại lý
             * Ngày XN tham gia
             * SL XN ban đầu
             * SL đang hiển thị
             * SL/DTT tích lũy
             * Thanh toán hợp lệ
             * Hạn mức được hưởng
             * Giá trị ưu đãi được hưởng
             * Thanh toán còn thiếu
             * Phòng KD
             **/
            List<JSONObject> listField = new ArrayList<>();
            String exportFields = JsonUtils.Serialize(request.getFields());
            for (CTTLColumnExportType cttlColumnExportType : CTTLColumnExportType.values()) {
                if (cttlColumnExportType.getLevel() == 1 &&
                        exportFields.contains(cttlColumnExportType.getKey())) {
                    JSONObject field = new JSONObject();
                    if (cttlColumnExportType == CTTLColumnExportType.SL_DAI_LY_DIEU_CHINH) {
                        field.put(cttlColumnExportType.getKey(), cttlColumnExportType.getLabel()
                                + "(x: Đại lý không điều chỉnh số lượng)");
                    } else {
                        field.put(cttlColumnExportType.getKey(), cttlColumnExportType.getLabel());
                    }
                    listField.add(field);
                }
            }

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (!headerKey.isEmpty()) {
                if (headerKey.get(0).equals("stt")) {
                    headerKey.remove(0);
                } else {
                    headerValue.add(0, "STT");
                }
            }

            for (JSONObject js : records) {
                JSONObject jsAgency = mpAgency.get(ConvertUtils.toInt(js.get("agency_id")));
                if (jsAgency != null) {
                    js.put(CTTLColumnExportType.MA_DAI_LY.getKey(), jsAgency.get("code"));
                    js.put(CTTLColumnExportType.TEN_DAI_LY.getKey(), jsAgency.get("shop_name"));
                    js.put(CTTLColumnExportType.PHONG_KD.getKey(), this.dataManager.getProductManager().getBusinessDepartmentName(ConvertUtils.toInt(jsAgency.get("business_department_id"))));
                }
                /**
                 "{\"ngay_xn_tham_gia\":\"Ngày XN tham gia\"}," +
                 "{\"sl_xn_ban_dau\":\"SL XN ban đầu\"}," +
                 "{\"sl_dang_hien_thi\":\"SL đang hiển thị\"}," +
                 "{\"sl_dtt_tich_luy\":\"SL/DTT tích lũy\"}," +
                 "{\"thanh_toan_hop_le\":\"Thanh toán hợp lệ\"}," +
                 "{\"han_muc_duoc_huong\":\"Hạn mức được hưởng\"}," +
                 "{\"gia_tri_uu_dai_duoc_huong\":\"Giá trị ưu đãi được hưởng\"}," +
                 "{\"thanh_toan_con_thieu\":\"Thanh toán còn thiếu\"}," +
                 **/
                js.put(CTTLColumnExportType.NGAY_XN_THAM_GIA.getKey(), "-");
                js.put(CTTLColumnExportType.SL_XN_BAN_DAU.getKey(), ConvertUtils.toString(js.get("confirm_join_quantity")));
                js.put(CTTLColumnExportType.SL_DANG_HIEN_THI.getKey(), ConvertUtils.toString(js.get("show_join_quantity")));
                int update_join_quantity = ConvertUtils.toInt(js.get("update_join_quantity"));
                js.put(CTTLColumnExportType.SL_DAI_LY_DIEU_CHINH.getKey(),
                        update_join_quantity < 0 ? "x" : ConvertUtils.toString(update_join_quantity)
                );
                js.put(CTTLColumnExportType.SL_DTT_TICH_LUY.getKey(), ConvertUtils.toString(js.get("gtth")));
                js.put(CTTLColumnExportType.THANH_TOAN_HOP_LE.getKey(), ConvertUtils.toString(js.get("gttt")));
                List<Integer> reward_limit_level_list =
                        JsonUtils.DeSerialize(js.get("reward_limit_level").toString(), new TypeToken<List<Integer>>() {
                        }.getType());
                Map<Integer, Integer> mpLevel = new HashMap<>();
                String han_muc_duoc_huong = "";
                for (Integer reward_limit_level : reward_limit_level_list) {
                    Integer valueLevel = mpLevel.get(reward_limit_level);
                    if (valueLevel == null) {
                        mpLevel.put(reward_limit_level, 1);
                    } else {
                        mpLevel.put(reward_limit_level, valueLevel + 1);
                    }
                }
                for (Map.Entry<Integer, Integer> entry : mpLevel.entrySet()) {
                    if (!han_muc_duoc_huong.isEmpty()) {
                        han_muc_duoc_huong += ";";
                    }
                    han_muc_duoc_huong += "Hạn mức: " + entry.getKey() + " x " + entry.getValue();
                }

                js.put(CTTLColumnExportType.HAN_MUC_DUOC_HUONG.getKey(), han_muc_duoc_huong);
                js.put(CTTLColumnExportType.GIA_TRI_UU_DAI_DUOC_HUONG.getKey(), promoOfferType == PromoOfferType.GIFT_OFFER ?
                        (ConvertUtils.toString(js.get("reward_gift_quantity")) + "x " + jsGiftInfo.getFull_name()) :
                        ConvertUtils.toString(js.get("reward_money")));
                js.put(CTTLColumnExportType.THANH_TOAN_CON_THIEU.getKey(), "-");
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');

            List<String> headerValue1 = new ArrayList<>();
            CSVPrinter csvPrinterR1 = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue1.toArray(new String[0])));
            List<String> columnR1List = new ArrayList<>();
            if (exportFields.contains(CTTLColumnExportType.NGAY_XUAT.getKey())) {
                columnR1List.add("Ngày xuất: " + this.appUtils.convertDateToString(new Date(), "dd-MM-yyyy"));
                csvPrinterR1.printRecord(columnR1List);
            }

            List<String> columnR2List = new ArrayList<>();
            if (exportFields.contains(CTTLColumnExportType.MA_CT.getKey())) {
                columnR2List.add("Mã CT: " + ConvertUtils.toString(jsCTTL.get("code")));
            }
            columnR2List.add("");
            PromoTimeRequest order_date = JsonUtils.DeSerialize(jsCTTL.get("order_date_data").toString(),
                    PromoTimeRequest.class);
            PromoTimeRequest payment_date = JsonUtils.DeSerialize(jsCTTL.get("payment_date_data").toString(),
                    PromoTimeRequest.class);
            if (exportFields.contains(CTTLColumnExportType.THOI_GIAN_BAT_DAU_DAT_HANG.getKey())) {
                columnR2List.add("Thời gian bắt đầu đặt hàng: " + this.appUtils.convertDateToString(
                        new Date(order_date.getStart_date_millisecond()),
                        "HH:mm dd-MM-yyyy"));
            }
            columnR2List.add("");
            if (exportFields.contains(CTTLColumnExportType.THOI_HAN_THANH_TOAN.getKey())) {
                columnR2List.add("Thời hạn thanh toán: " + this.appUtils.convertDateToString(
                        new Date(payment_date.getEnd_date_millisecond()),
                        "HH:mm dd-MM-yyyy"));
            }
            csvPrinterR1.printRecord(columnR2List);

            List<String> columnR3List = new ArrayList<>();
            if (exportFields.contains(CTTLColumnExportType.TEN_CT.getKey())) {
                columnR3List.add("Tên CT: " + ConvertUtils.toString(jsCTTL.get("name")));
            }
            columnR3List.add("");
            if (exportFields.contains(CTTLColumnExportType.THOI_GIAN_KET_THUC_DAT_HANG.getKey())) {
                columnR3List.add("Thời gian kết thúc đặt hàng: " + this.appUtils.convertDateToString(
                        new Date(order_date.getEnd_date_millisecond()),
                        "HH:mm dd-MM-yyyy"));
            }
            columnR3List.add("");
            PromoTimeRequest reward_date = JsonUtils.DeSerialize(jsCTTL.get("reward_date_data").toString(),
                    PromoTimeRequest.class);
            if (exportFields.contains(CTTLColumnExportType.THOI_GIAN_KET_THUC_TRA_THUONG.getKey())) {
                columnR3List.add("Thời gian kết thúc trả thưởng: " + this.appUtils.convertDateToString(
                        new Date(reward_date.getEnd_date_millisecond()),
                        "HH:mm dd-MM-yyyy"));
            }
            csvPrinterR1.printRecord(columnR3List);
            csvPrinterR1.printRecord(new ArrayList<>());

            /** Điều kiện tham gia:
             Hình thức ưu đãi:
             Loại ưu đãi:
             **/
            if (exportFields.contains(CTTLColumnExportType.DIEU_KIEN_THAM_GIA.getKey())) {
                csvPrinterR1.printRecord(
                        Arrays.asList("Điều kiện tham gia: " + PromoConditionCTTLType.from(
                                ConvertUtils.toString(jsCTTL.get("condition_type"))
                        ).getLabel()));
            }
            if (exportFields.contains(CTTLColumnExportType.HINH_THUC_UU_DAI.getKey())) {
                csvPrinterR1.printRecord(
                        Arrays.asList("Hình thức ưu đãi: " + PromoFormOfRewardType.from(ConvertUtils.toString(jsCTTL.get("form_of_reward"))
                        ).getLabel()));
            }
            if (exportFields.contains(CTTLColumnExportType.LOAI_UU_DAI.getKey())) {
                csvPrinterR1.printRecord(
                        Arrays.asList("Loại ưu đãi: " + promoOfferType.getLabel()));
            }
            csvPrinterR1.printRecord(new ArrayList<>());
            csvPrinterR1.printRecord(new ArrayList<>());

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);


            //Count total
            //Limit config cvs
            if (!headerKey.isEmpty()) {
                records.forEach(d -> {
                    try {
                        List<String> dataRecord = new ArrayList<>();
                        dataRecord.add(String.valueOf(stt.get()));
                        stt.getAndIncrement();
                        headerKey.forEach(k -> {
                            dataRecord.add(
                                    d.get(k) == null ? "" :
                                            d.get(k).toString());
                        });
                        csvPrinter.printRecord(dataRecord);
                    } catch (Exception e) {
                        LogUtil.printDebug("", e);
                    }
                });
            }
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            LogUtil.printDebug("", e);
            return ResponseEntity.status(400).body(null);
        }
    }

    private ResponseEntity<?> downloadExportAgencyAccessApp(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();


            String query = this.filterUtils.getQuery(
                    FunctionList.FILTER_AGENCY_ACCESS_APP_REPORT,
                    request.getFilters(),
                    request.getSorts());
            List<JSONObject> records = this.logDB.filter(
                    query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE, 0);

            Map<Integer, AgencyEntity> mpAgency = new HashMap<>();

            if (!records.isEmpty()) {
                this.agencyDB.loadAllAgency(mpAgency);
            }

            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"code\":\"Mã đại lý\"}," +
                    "{\"shop_name\":\"Tên đại lý\"}," +
                    "{\"quantity\":\"Số lần\"}," +
                    "{\"created_date\":\"Ngày phát sinh\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            for (JSONObject js : records) {
                int agency_id = ConvertUtils.toInt(js.get("agency_id"));
                AgencyEntity agencyEntity = mpAgency.get(agency_id);
                if (agencyEntity != null) {
                    js.put("code", ConvertUtils.toString(agencyEntity.getCode()));
                    js.put("shop_name", ConvertUtils.toString(agencyEntity.getShop_name()));
                }
                js.put("created_date", js.get("created_date"));
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);

            //Count total
            //Limit config cvs
            records.forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();
                    headerKey.forEach(k -> {
                        dataRecord.add(
                                d.get(k) == null ? "" :
                                        d.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    private ResponseEntity<?> downloadReportWarehouseExportHistory(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();


            String query = this.filterUtils.getQuery(
                    FunctionList.LIST_WAREHOUSE_EXPORT_HISTORY,
                    request.getFilters(),
                    request.getSorts());
            List<JSONObject> records = this.agencyDB.filter(
                    query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE, 0);

            Map<Integer, AgencyEntity> mpAgency = new HashMap<>();

            if (!records.isEmpty()) {
                this.agencyDB.loadAllAgency(mpAgency);
            }

            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"ma_phieu\":\"Mã phiếu\"}," +
                    "{\"ma_san_pham\":\"Mã sản phẩm\"}," +
                    "{\"ten_san_pham\":\"Tên sản phẩm\"}," +
                    "{\"don_vi_tinh\":\"Đơn vị tính\"}," +
                    "{\"so_luong_xuat\":\"Số lượng xuất\"}," +
                    "{\"thoi_gian_xuat\":\"Thời gian xuất\"}," +
                    "{\"loai_phieu_xuat\":\"Loại phiếu xuất\"}," +
                    "{\"kho_xuat\":\"Kho xuất\"}," +
                    "{\"ma_xuat_den\":\"Mã xuất đến\"}," +
                    "{\"ten_xuat_den\":\"Xuất đến\"}," +
                    "{\"dia_chi_den\":\"Địa chỉ đến\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            for (JSONObject js : records) {
                js.put("ma_phieu", js.get("code"));

                ProductCache productCache = this.dataManager.getProductManager().getMpProduct().get(
                        ConvertUtils.toInt(js.get("product_id")));
                if (productCache != null) {
                    js.put("ma_san_pham",
                            productCache.getCode());
                    js.put("ten_san_pham",
                            productCache.getFull_name());
                    js.put("don_vi_tinh",
                            this.dataManager.getProductManager().getSmallUnitName(
                                    productCache.getProduct_small_unit_id()));
                }

                js.put("so_luong_xuat", js.get("product_quantity"));
                //"{\"thoi_gian_nhap\":\"Thời gian nhập\"}," +
                js.put("thoi_gian_xuat", DateTimeUtils.toString(AppUtils.convertJsonToDate(js.get("confirmed_date")),
                        "HH:mm:ss dd-MM-yyyy"));
                //"{\"loai_phieu_xuat\":\"Loại phiếu xuất\"}," +
                //"{\"kho_xuat\":\"Kho xuất\"}," +
                WarehouseExportBillType loai_phieu_xuat = WarehouseExportBillType.XUAT_BAN.from(
                        ConvertUtils.toInt(js.get("warehouse_export_bill_type_id")));
                if (loai_phieu_xuat != null) {
                    js.put("loai_phieu_xuat", loai_phieu_xuat.getLabel());
                    if (loai_phieu_xuat.getValue() == WarehouseExportBillType.XUAT_BAN.getValue()) {
                        //"{\"ma_xuat_den\":\"Mã đại lý/ Mã kho đến\"}," +
                        AgencyEntity agencyEntity = mpAgency.get(ConvertUtils.toInt(js.get("agency_id")));
                        if (agencyEntity != null) {
                            js.put("ma_xuat_den", agencyEntity.getCode());
                            //"{\"ten_xuat_den\":\"Xuất đến\"}," +
                            js.put("ten_xuat_den", agencyEntity.getShop_name());
                        }
                    } else {
                        WarehouseBasicData warehouseBasicData = this.dataManager.getWarehouseManager().getMpWarehouse().get(
                                ConvertUtils.toInt(js.get("target_warehouse_id"))
                        );
                        if (warehouseBasicData != null) {
                            js.put("ma_xuat_den", warehouseBasicData.getCode());
                            //"{\"ten_xuat_den\":\"Xuất đến\"}," +
                            js.put("ten_xuat_den", warehouseBasicData.getName());
                        }
                    }
                }
                //"{\"kho_xuat\":\"Kho xuất\"}," +
                js.put("kho_xuat", this.dataManager.getWarehouseManager().getMpWarehouse().get(
                        ConvertUtils.toInt(js
                                .get("warehouse_id"))).getName()
                );
                //"{\"dia_chi_den\":\"Địa chỉ đến\"}" +
                js.put("dia_chi_den", "");
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);

            //Count total
            //Limit config cvs
            records.forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();
                    headerKey.forEach(k -> {
                        dataRecord.add(
                                d.get(k) == null ? "" :
                                        d.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            LogUtil.printDebug("", e);
            return ResponseEntity.status(400).body(null);
        }
    }

    private ResponseEntity<?> downloadReportWarehouseImportHistory(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();


            String query = this.filterUtils.getQuery(
                    FunctionList.LIST_WAREHOUSE_IMPORT_HISTORY,
                    request.getFilters(),
                    request.getSorts());
            List<JSONObject> records = this.agencyDB.filter(
                    query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE, 0);

            Map<Integer, AgencyEntity> mpAgency = new HashMap<>();

            if (!records.isEmpty()) {
                this.agencyDB.loadAllAgency(mpAgency);
            }

            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"ma_phieu\":\"Mã phiếu\"}," +
                    "{\"ma_san_pham\":\"Mã sản phẩm\"}," +
                    "{\"ten_san_pham\":\"Tên sản phẩm\"}," +
                    "{\"don_vi_tinh\":\"Đơn vị tính\"}," +
                    "{\"so_luong_nhap\":\"Số lượng nhập\"}," +
                    "{\"thoi_gian_nhap\":\"Thời gian nhập\"}," +
                    "{\"kho_nhap\":\"Kho nhập\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            for (JSONObject js : records) {
                js.put("ma_phieu", js.get("code"));

                ProductCache productCache = this.dataManager.getProductManager().getMpProduct().get(
                        ConvertUtils.toInt(js.get("product_id")));
                if (productCache != null) {
                    js.put("ma_san_pham",
                            productCache.getCode());
                    js.put("ten_san_pham",
                            productCache.getFull_name());
                    js.put("don_vi_tinh",
                            this.dataManager.getProductManager().getSmallUnitName(
                                    productCache.getProduct_small_unit_id()));
                }

                js.put("so_luong_nhap", js.get("product_quantity"));
                //"{\"thoi_gian_nhap\":\"Thời gian nhập\"}," +
                js.put("thoi_gian_nhap", DateTimeUtils.toString(AppUtils.convertJsonToDate(js.get("confirmed_date")),
                        "HH:mm:ss dd-MM-yyyy"));
                //"{\"kho_nhap\":\"Kho nhập\"}," +
                WarehouseBasicData warehouseBasicData = this.dataManager.getWarehouseManager().getMpWarehouse().get(
                        ConvertUtils.toInt(js
                                .get("warehouse_id")));
                if (warehouseBasicData != null) {
                    js.put("kho_nhap", warehouseBasicData.getName());
                }
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);

            //Count total
            //Limit config cvs
            records.forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();
                    headerKey.forEach(k -> {
                        dataRecord.add(
                                d.get(k) == null ? "" :
                                        d.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    public List<JSONObject> filterWarehouseReport(FilterListRequest request) {
        try {
            String query = this.getQueryReport(
                    FunctionList.LIST_WAREHOUSE_REPORT,
                    request.getFilters(),
                    request.getSorts());
            JSONObject data = new JSONObject();
            List<JSONObject> records = this.agencyDB.filter(query,
                    this.appUtils.getOffset(request.getPage()),
                    ConfigInfo.PAGE_SIZE, 0);

            String start_date = "";
            String end_date = "";

            for (int i = 0; i < request.getFilters().size(); i++) {
                FilterRequest filter = request.getFilters().get(i);
                if (filter.getType().equals(TypeFilter.DATE)) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    if (filter.getValue1() != null) {
                        start_date = dateFormat.format(new Date(filter.getValue1()));
                    }
                    if (filter.getValue2() != null) {
                        end_date = dateFormat.format(new Date(filter.getValue2()));
                    }
                }
            }

            Date today = DateTimeUtils.getDateTime(DateTimeUtils.getNow(), "yyyy-MM-dd");


            if (start_date.isEmpty() && end_date.isEmpty()) {
                start_date = DateTimeUtils.getNow("yyyy-MM-dd");
                end_date = start_date;
            } else if (start_date.isEmpty() && !end_date.isEmpty()) {
                if (DateTimeUtils.getDateTime(end_date, "yyyy-MM-dd").after(today)) {
                    end_date = DateTimeUtils.getNow("yyyy-MM-dd");
                    start_date = end_date;
                } else {
                    start_date = end_date;
                }
            } else if (!start_date.isEmpty() && end_date.isEmpty()) {
                if (DateTimeUtils.getDateTime(start_date, "yyyy-MM-dd").after(DateTimeUtils.getNow())) {
                    start_date = DateTimeUtils.getNow("yyyy-MM-dd");
                    end_date = start_date;
                } else {
                    end_date = DateTimeUtils.getNow("yyyy-MM-dd");
                }
            } else {
                if (DateTimeUtils.getDateTime(start_date, "yyyy-MM-dd").after(DateTimeUtils.getDateTime(end_date, "yyyy-MM-dd"))) {
                    return new ArrayList<>();
                } else if (DateTimeUtils.getDateTime(start_date, "yyyy-MM-dd").after(DateTimeUtils.getNow())) {
                    start_date = DateTimeUtils.getNow("yyyy-MM-dd");
                    end_date = start_date;
                } else if (DateTimeUtils.getDateTime(end_date, "yyyy-MM-dd").after(DateTimeUtils.getNow())) {
                    end_date = DateTimeUtils.getNow("yyyy-MM-dd");
                }
            }

            Date sDate = DateTimeUtils.getDateTime(start_date, "yyyy-MM-dd");
            Date eDate = DateTimeUtils.getDateTime(end_date, "yyyy-MM-dd");

            LogUtil.printDebug("sDate" + sDate);
            LogUtil.printDebug("eDate" + eDate);

            if (sDate.equals(eDate)) {
                if (eDate.equals(today)) {
                    for (JSONObject js : records) {
                        int product_id = ConvertUtils.toInt(js.get("product_id"));
                        ProductCache productCache = this.dataManager.getProductManager().getProductBasicData(product_id);

                        JSONObject rsToday = this.warehouseDB.getWarehouseInfoByProduct(product_id);

                        /**
                         * tồn đầu ngày
                         */
                        js.put("quantity_start_today",
                                rsToday.get("quantity_start_today"));

                        /**
                         * nhập trong ngày
                         */
                        js.put("quantity_import_today",
                                rsToday.get("quantity_import_today"));

                        /**
                         * xuất trong ngày
                         */
                        js.put("quantity_export_today",
                                rsToday.get("quantity_export_today"));

                        /**
                         * tồn cuối ngày
                         */
                        js.put("quantity_end_today",
                                rsToday.get("quantity_end_today"));

                        /**
                         * hiện tại
                         */
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_ship_today", rsToday.get("quantity_waiting_ship_today"));
                        js.put("quantity_reality", this.getQuantityReality(rsToday));
                        js.put("quantity_availability", this.getQuantityAvailability(rsToday));

                        js.put("product_code", productCache == null ? "" : productCache.getCode());
                        js.put("full_name", productCache == null ? "" : productCache.getFull_name());
                        js.put("don_vi_tinh", productCache == null ? "" :
                                this.dataManager.getProductManager().getSmallUnitName(
                                        productCache.getProduct_small_unit_id()
                                )
                        );
                        js.put("product_status_name", productCache == null ? "" :
                                (productCache.getStatus() == 1 ? "Hiện" : "Ẩn"));
                    }
                } else {
                    for (JSONObject js : records) {
                        int product_id = ConvertUtils.toInt(js.get("product_id"));
                        ProductCache productCache =
                                this.dataManager.getProductManager().getProductBasicData(product_id);

                        JSONObject rsToday = this.warehouseDB.getWarehouseInfoByProduct(product_id);
                        JSONObject rsDate = this.warehouseDB.getWarehouseInfoDateByProduct(product_id, start_date);

                        /**
                         * tồn đầu ngày
                         */
                        js.put("quantity_start_today",
                                rsDate == null ? 0 : rsDate.get("quantity_start_today"));

                        /**
                         * nhập trong ngày
                         */
                        js.put("quantity_import_today",
                                rsDate == null ? 0 : rsDate.get("quantity_import_today"));

                        /**
                         * xuất trong ngày
                         */
                        js.put("quantity_export_today",
                                rsDate == null ? 0 : rsDate.get("quantity_export_today"));

                        /**
                         * tồn cuối ngày
                         */
                        js.put("quantity_end_today",
                                rsDate == null ? 0 : rsDate.get("quantity_end_today"));

                        /**
                         * hiện tại
                         */
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_ship_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_reality", this.getQuantityReality(rsToday));
                        js.put("quantity_availability", this.getQuantityAvailability(rsToday));

                        js.put("product_code", productCache == null ? "" : productCache.getCode());
                        js.put("full_name", productCache == null ? "" : productCache.getFull_name());
                        js.put("don_vi_tinh", productCache == null ? "" :
                                this.dataManager.getProductManager().getSmallUnitName(
                                        productCache.getProduct_small_unit_id()
                                )
                        );
                        js.put("product_status_name", productCache == null ? "" :
                                (productCache.getStatus() == 1 ? "Hiện" : "Ẩn"));
                    }
                }
            } else {
                if (eDate.equals(today)) {
                    for (JSONObject js : records) {
                        int product_id = ConvertUtils.toInt(js.get("product_id"));
                        ProductCache productCache =
                                this.dataManager.getProductManager().getProductBasicData(product_id);

                        JSONObject rsToday = this.warehouseDB.getWarehouseInfoByProduct(product_id);
                        JSONObject rsStartDate = this.warehouseDB.getWarehouseInfoDateByProduct(product_id, start_date);
                        /**
                         * tồn đầu ngày
                         */
                        js.put("quantity_start_today",
                                rsStartDate == null ? 0 : rsStartDate.get("quantity_start_today"));

                        /**
                         * nhập trong ngày
                         */
                        js.put("quantity_import_today",
                                ConvertUtils.toInt(rsToday.get("quantity_import_today"))
                                        + this.warehouseDB.getQuantityImportByDate(product_id, start_date, end_date));

                        /**
                         * xuất trong ngày
                         */
                        js.put("quantity_export_today",
                                ConvertUtils.toInt(rsToday.get("quantity_export_today"))
                                        + this.warehouseDB.getQuantityExportByDate(product_id, start_date, end_date));

                        /**
                         * tồn cuối kỳ
                         */
                        js.put("quantity_end_today",
                                rsToday.get("quantity_end_today"));

                        /**
                         * hiện tại
                         */
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_ship_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_reality", this.getQuantityReality(rsToday));
                        js.put("quantity_availability", this.getQuantityAvailability(rsToday));

                        js.put("product_code", productCache == null ? "" : productCache.getCode());
                        js.put("full_name", productCache == null ? "" : productCache.getFull_name());
                        js.put("don_vi_tinh", productCache == null ? "" :
                                this.dataManager.getProductManager().getSmallUnitName(
                                        productCache.getProduct_small_unit_id()
                                )
                        );
                        js.put("product_status_name", productCache == null ? "" :
                                (productCache.getStatus() == 1 ? "Hiện" : "Ẩn"));
                    }
                } else {
                    for (JSONObject js : records) {
                        int product_id = ConvertUtils.toInt(js.get("product_id"));
                        ProductCache productCache =
                                this.dataManager.getProductManager().getProductBasicData(product_id);

                        JSONObject rsToday = this.warehouseDB.getWarehouseInfoByProduct(product_id);
                        JSONObject rsStartDate = this.warehouseDB.getWarehouseInfoDateByProduct(product_id, start_date);
                        /**
                         * tồn đầu ngày
                         */
                        js.put("quantity_start_today",
                                rsStartDate == null ? 0 : rsStartDate.get("quantity_start_today"));

                        /**
                         * nhập trong ngày
                         */
                        js.put("quantity_import_today",
                                this.warehouseDB.getQuantityImportByDate(product_id,
                                        start_date,
                                        end_date));

                        /**
                         * xuất trong ngày
                         */
                        js.put("quantity_export_today",
                                this.warehouseDB.getQuantityExportByDate(product_id, start_date, end_date));

                        /**
                         * tồn cuối kỳ
                         */
                        js.put("quantity_end_today",
                                rsToday.get("quantity_end_today"));

                        /**
                         * hiện tại
                         */
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_ship_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_waiting_approve_today", rsToday.get("quantity_waiting_approve_today"));
                        js.put("quantity_reality", this.getQuantityReality(rsToday));
                        js.put("quantity_availability", this.getQuantityAvailability(rsToday));

                        js.put("product_code", productCache == null ? "" : productCache.getCode());
                        js.put("full_name", productCache == null ? "" : productCache.getFull_name());
                        js.put("don_vi_tinh", productCache == null ? "" :
                                this.dataManager.getProductManager().getSmallUnitName(
                                        productCache.getProduct_small_unit_id()
                                )
                        );
                        js.put("product_status_name", productCache == null ? "" :
                                (productCache.getStatus() == 1 ? "Hiện" : "Ẩn"));
                    }
                }
            }


            return records;
        } catch (Exception ex) {
            LogUtil.printDebug(Module.WAREHOUSE.name(), ex);
        }
        return new ArrayList<>();
    }

    private int getQuantityReality(JSONObject rsToday) {
        if (rsToday != null) {
            return
                    ConvertUtils.toInt(rsToday.get("quantity_start_today"))
                            + ConvertUtils.toInt(rsToday.get("quantity_import_today"))
                            - ConvertUtils.toInt(rsToday.get("quantity_export_today"))
                    ;
        }
        return 0;
    }

    private int getQuantityAvailability(JSONObject rsToday) {
        if (rsToday != null) {
            return
                    ConvertUtils.toInt(rsToday.get("quantity_start_today"))
                            + ConvertUtils.toInt(rsToday.get("quantity_import_today"))
                            - ConvertUtils.toInt(rsToday.get("quantity_export_today"))
                            - ConvertUtils.toInt(rsToday.get("quantity_waiting_approve_today"))
                            - ConvertUtils.toInt(rsToday.get("quantity_waiting_ship_today"))
                    ;
        }
        return 0;
    }

    private String getQueryReport(FunctionList functionList, List<FilterRequest> filterRequestList, List<SortByRequest> sorts) {
        StringBuilder query = new StringBuilder(functionList.getSql());

        List<FilterRequest> filters = new ArrayList<>();
        for (FilterRequest filterRequest : filterRequestList) {
            if (!filterRequest.getType().equals(TypeFilter.DATE)) {
                filters.add(filterRequest);
            }
        }

        if (filters.size() != 0) {
            if (!query.toString().toUpperCase(Locale.ROOT).contains("WHERE")) {
                query.append(" WHERE ");
            } else {
                query.append(" AND ");
            }
            for (int i = 0; i < filters.size(); i++) {
                FilterRequest filter = filters.get(i);
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
                            searchQuery += stringKey + " LIKE '%" + filter.getValue() + "%' ";
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
                            String time1 = dateFormat.format(new Date(filter.getValue1()));
                            query.append(filter.getKey()).append(">='").append(time1).append("'");
                        } else if (filter.getValue1() == null && filter.getValue2() != null) {
                            String time2 = dateFormat.format(new Date(filter.getValue2()));
                            query.append(filter.getKey()).append("<='").append(time2).append("'");
                        } else {
                            String time1 = dateFormat.format(new Date(filter.getValue1()));
                            String time2 = dateFormat.format(new Date(filter.getValue2()));
                            query.append(filter.getKey()).append(">='").append(time1).append("' AND ").append(filter.getKey()).append("<='").append(time2).append("'");
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
                }
                if (i < filters.size() - 1) {
                    query.append(" AND ");
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

    private ResponseEntity<?> downloadProductVisibility(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();

            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"ma_san_pham\":\"Mã sản phẩm\"}," +
                    "{\"ten_san_pham\":\"Tên sản phẩm\"}," +
                    "{\"trang_thai\":\"Trạng thái\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);


            //Count total
            //Limit config cvs
            dataManager.getProductManager().getMpProduct().values().forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();

                    JSONObject record = new JSONObject();
                    record.put("ma_san_pham", d.getCode());
                    record.put("ten_san_pham", d.getFull_name());
                    record.put("trang_thai", this.getProductVisibilityByAgency(
                            request.getId(),
                            d.getId()
                    ));


                    headerKey.forEach(k -> {
                        dataRecord.add(
                                record.get(k) == null ? "" :
                                        record.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    /**
     * Xuất sản phẩm
     *
     * @param file_name
     * @param request
     * @param response
     * @return
     */
    private ResponseEntity<?> downloadProduct(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();

            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"id_san_pham\":\"Id sản phẩm\"}," +
                    "{\"ma_san_pham\":\"Mã sản phẩm\"}," +
                    "{\"ten_san_pham\":\"Tên sản phẩm\"}," +
                    "{\"ten_rut_gon\":\"Tên rút gọn\"}," +
                    "{\"id_nhom_hang\":\"Id nhóm hàng\"}," +
                    "{\"ma_nhom_hang\":\"Mã nhóm hàng\"}," +
                    "{\"ten_nhom_hang\":\"Tên nhóm hàng\"}," +
                    "{\"id_don_vi_nho\":\"Id đơn vị nhỏ\"}," +
                    "{\"ten_don_vi_nho\":\"Tên đơn vị nhỏ\"}," +
                    "{\"id_don_vi_lon\":\"Id đơn vị lớn\"}," +
                    "{\"ten_don_vi_lon\":\"Tên đơn vị lớn\"}," +
                    "{\"quay_cach\":\"Quy cách\"}," +
                    "{\"dac_diem_1\":\"Đặc điểm 1\"}," +
                    "{\"id_dac_diem_2\":\"Id Đặc điểm 2\"}," +
                    "{\"ten_dac_diem_2\":\"Tên Đặc điểm 2\"}," +
                    "{\"id_thuong_hieu\":\"Id thương hiệu\"}," +
                    "{\"ten_thuong_hieu\":\"Tên thương hiệu\"}," +
                    "{\"id_pltth\":\"ID PLTTH\"}," +
                    "{\"ten_pltth\":\"Tên PLTTH\"}," +
                    "{\"trang_thai\":\"Trạng thái: 0-Ẩn, 1-Hiện\"}," +
                    "{\"loai_san_pham\":\"Loại sản phẩm: 1-Máy móc, 2-Phụ tùng\"}," +
                    "{\"hien_thi_tren_app\":\"Trạng thái: 0-Không, 1-Có\"}," +
                    "{\"gia_ban\":\"Giá bán\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);


            //Count total
            //Limit config cvs
            String query = this.filterUtils.getQueryV2(FunctionList.EXPORT_PRODUCT, request.getFilters(), request.getSorts());
            List<JSONObject> products = this.productDB.filterProduct(query, 0, 0, 0);
            products.forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();

                    JSONObject record = new JSONObject();
//                            "{\"id_san_pham\":\"Id sản phẩm\"}," +
                    record.put("id_san_pham", d.get("id"));
//                            "{\"ma_san_pham\":\"Mã sản phẩm\"}," +
                    record.put("ma_san_pham", d.get("code"));
//                            "{\"ten_san_pham\":\"Tên sản phẩm\"}," +
                    record.put("ten_san_pham", d.get("full_name"));
//                            "{\"ten_rut_gon\":\"Tên rút gọn\"}," +
                    record.put("ten_rut_gon", d.get("short_name"));
//                            "{\"id_nhom_hang\":\"Id nhóm hàng\"}," +
                    record.put("id_nhom_hang", d.get("product_group_id"));
//                            "{\"ma_nhom_hang\":\"Mã nhóm hàng\"}," +
                    record.put("ma_nhom_hang", d.get("product_group_code"));
//                            "{\"ten_nhom_hang\":\"Tên nhóm hàng\"}," +
                    record.put("ten_nhom_hang", d.get("product_group_name"));
//                            "{\"id_don_vi_nho\":\"Id đơn vị nhỏ\"}," +
                    record.put("id_don_vi_nho", d.get("id_don_vi_nho"));
//                            "{\"ten_don_vi_nho\":\"Tên đơn vị nhỏ\"}," +
                    record.put("ten_don_vi_nho", d.get("ten_don_vi_nho"));
//                            "{\"id_don_vi_lon\":\"Id đơn vị lớn\"}," +
                    record.put("id_don_vi_lon", d.get("id_don_vi_lon"));
//                            "{\"ten_don_vi_lon\":\"Tên đơn vị lớn\"}," +
                    record.put("ten_don_vi_lon", d.get("ten_don_vi_lon"));
//                            "{\"quay_cach\":\"Quy cách\"}," +
                    record.put("quay_cach", d.get("specification"));
//                            "{\"dac_diem_1\":\"Đặc điểm 1\"}," +
                    record.put("dac_diem_1", d.get("characteristic"));
//                            "{\"id_dac_diem_2\":\"Id Đặc điểm 2\"}," +
                    record.put("id_dac_diem_2", d.get("id_dac_diem_2"));
//                            "{\"ten_dac_diem_2\":\"Tên Đặc điểm 2\"}," +
                    record.put("ten_dac_diem_2", d.get("ten_dac_diem_2"));
//                            "{\"id_thuong_hieu\":\"Id thương hiệu\"}," +
                    record.put("id_thuong_hieu", d.get("id_thuong_hieu"));
//                            "{\"ten_thuong_hieu\":\"Tên thương hiệu\"}," +
                    record.put("ten_thuong_hieu", d.get("ten_thuong_hieu"));
//                            "{\"id_pltth\":\"ID PLTTH\"}," +
                    record.put("id_pltth", d.get("pltth_id"));
//                            "{\"ten_pltth\":\"Tên PLTTH\"}," +
                    record.put("ten_pltth", d.get("pltth_name"));
//                            "{\"trang_thai\":\"Trạng thái\"}" +
                    record.put("trang_thai", ConvertUtils.toString(d.get("status")));
                    record.put("loai_san_pham", d.get("item_type"));
                    record.put("hien_thi_tren_app", d.get("app_active"));
                    record.put("gia_ban", ConvertUtils.toInt(d.get("price")) <= 0 ? "Giá liên hệ" : d.get("price"));

                    headerKey.forEach(k -> {
                        dataRecord.add(
                                record.get(k) == null ? "" :
                                        record.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    /**
     * Xuất nhóm hàng
     *
     * @param file_name
     * @param request
     * @param response
     * @return
     */
    private ResponseEntity<?> downloadProductGroup(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();

            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"id_nhom_hang\":\"Id nhóm hàng\"}," +
                    "{\"ma_nhom_hang\":\"Mã nhóm hàng\"}," +
                    "{\"ten_nhom_hang\":\"Tên nhóm hàng\"}," +
                    "{\"so_san_pham\":\"Số sản phẩm\"}," +
                    "{\"id_pltth\":\"ID PLTTH\"}," +
                    "{\"ten_pltth\":\"Tên PLTTH\"}," +
                    "{\"plsp\":\"PLSP\"}," +
                    "{\"mat_hang\":\"Mặt hàng\"}," +
                    "{\"trang_thai\":\"Trạng thái: 0-Ẩn, 1-Hiện\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);


            //Count total
            //Limit config cvs
            String query = this.filterUtils.getQueryV2(FunctionList.EXPORT_PRODUCT_GROUP, request.getFilters(), request.getSorts());
            List<JSONObject> products = this.productDB.filterProduct(query, 0, 0, 0);
            products.forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();

                    JSONObject record = new JSONObject();
                    record.put("id_nhom_hang", d.get("id"));
                    record.put("ma_nhom_hang", d.get("code"));
                    record.put("ten_nhom_hang", d.get("name"));
                    record.put("id_pltth", d.get("pltth_id"));
                    record.put("ten_pltth", d.get("pltth_name"));
                    record.put("trang_thai", ConvertUtils.toString(d.get("status")));

                    ProductGroup productGroup = this.dataManager.getProductManager().getMpProductGroup().get(
                            ConvertUtils.toInt(d.get("id"))
                    );

                    Category plsp = this.dataManager.getProductManager().getCategoryById(
                            ConvertUtils.toInt(d.get("pltth_id"))
                    );
                    record.put("plsp", plsp.getName());
                    Category mat_hang = this.dataManager.getProductManager().getCategoryById(
                            plsp.getParent_id()
                    );
                    record.put("mat_hang", mat_hang.getName());
                    record.put("so_san_pham", ConvertUtils.toInt(d.get("total")));
                    headerKey.forEach(k -> {
                        dataRecord.add(
                                record.get(k) == null ? "" :
                                        record.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    /**
     * Xuất danh mục
     *
     * @param file_name
     * @param request
     * @param response
     * @return
     */
    private ResponseEntity<?> downloadCategory(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();

            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"id\":\"Id\"}," +
                    "{\"ma\":\"Mã\"}," +
                    "{\"ten\":\"Tên\"}," +
                    "{\"cap\":\"Cấp: 1-Ngành hàng, 2-Mặt hàng, 3-PLSP, 4-PLTTH\"}," +
                    "{\"id_cap_cha\":\"Id cấp cha\"}," +
                    "{\"trang_thai\":\"Trạng thái: 0-Ẩn, 1-Hiện\"}," +
                    "{\"uu_tien_hien_thi\":\"Ưu tiên hiển thị\"}," +
                    "{\"uu_tien_hien_thi_cap_cha\":\"Ưu tiên hiển thị của cấp cha\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);


            //Count total
            //Limit config cvs
            String query = this.filterUtils.getQueryV2(FunctionList.EXPORT_CATEGORY, request.getFilters(), request.getSorts());
            List<JSONObject> products = this.productDB.filterProduct(query, 0, 0, 0);
            products.forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();

                    JSONObject record = new JSONObject();
                    record.put("id", d.get("id"));
                    record.put("ma", d.get("code"));
                    record.put("ten", d.get("name"));
                    record.put("id_cap_cha", d.get("parent_id"));
                    record.put("cap", d.get("category_level"));
                    record.put("trang_thai", ConvertUtils.toString(d.get("status")));
                    record.put("uu_tien_hien_thi", d.get("priority"));
                    record.put("uu_tien_hien_thi_cap_cha", d.get("parent_priority"));

                    headerKey.forEach(k -> {
                        dataRecord.add(
                                record.get(k) == null ? "" :
                                        record.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    /**
     * Xuất Catalog
     *
     * @param file_name
     * @param request
     * @param response
     * @return
     */
    private ResponseEntity<?> downloadCatalog(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".csv");

            OutputStream outputStream = response.getOutputStream();

            String fields = "[" +
                    "{\"stt\":\"STT\"}," +
                    "{\"id\":\"Id\"}," +
                    "{\"ten\":\"Tên\"}," +
                    "{\"uu_tien\":\"Ưu tiên\"}," +
                    "{\"trang_thai\":\"Trạng thái: 0-Ẩn, 1-Hiện\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            //Export CSV
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write('\ufeff');
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerValue.toArray(new String[0])));
            AtomicInteger stt = new AtomicInteger(1);


            //Count total
            //Limit config cvs
            String query = this.filterUtils.getQueryV2(FunctionList.EXPORT_CATALOG, request.getFilters(), request.getSorts());
            List<JSONObject> products = this.productDB.filterProduct(query, 0, 0, 0);
            products.forEach(d -> {
                try {
                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(String.valueOf(stt.get()));
                    stt.getAndIncrement();

                    JSONObject record = new JSONObject();
                    record.put("id", d.get("id"));
                    record.put("ten", d.get("name"));
                    record.put("uu_tien", d.get("priority"));
                    record.put("trang_thai", ConvertUtils.toString(d.get("status")));

                    headerKey.forEach(k -> {
                        dataRecord.add(
                                record.get(k) == null ? "" :
                                        record.get(k).toString());
                    });
                    csvPrinter.printRecord(dataRecord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
            csvPrinter.close();
            //outputStream.closeEntry();
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    private ResponseEntity<?> downloadOrderConfirmProduct(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/xlsx");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".xlsx");

            OutputStream outputStream = response.getOutputStream();

            for (FilterRequest filterRequest : request.getFilters()) {
                if (filterRequest.getKey().equals("membership_id")) {
                    filterRequest.setKey("t_agency.membership_id");
                } else if (filterRequest.getKey().equals("status")) {
                    filterRequest.setKey("t.status");
                }
            }

            FilterListRequest filterListRequest = JsonUtils.DeSerialize(
                    JsonUtils.Serialize(request),
                    FilterListRequest.class
            );

            this.addFilterOrderData(
                    request.getSessionData(),
                    filterListRequest
            );

            String query = this.filterUtils.getQuery(
                    FunctionList.LIST_ORDER_CONFIRMATION_PRODUCT,
                    filterListRequest.getFilters(),
                    filterListRequest.getSorts());
            List<JSONObject> records = this.productDB.filter(
                    query,
                    this.appUtils.getOffset(filterListRequest.getPage()),
                    ConfigInfo.PAGE_SIZE, 0);

            int total = this.productDB.getTotal(query);

            String fields = "[" +
                    "{\"stt\":\"Index\"}," +
                    "{\"oc\":\"OC\"}," +
                    "{\"ten_dai_ly\":\"Customer\"}," +
                    "{\"ngay_tao_don\":\"OC Date\"}," +
                    "{\"sku\":\"Product\"}," +
                    "{\"quantity\":\"Quantity\"}," +
                    "{\"price\":\"Price\"}," +
                    "{\"tong_tien\":\"Amount\"}," +
                    "{\"request_date\":\"Delivery request\"}," +
                    "{\"plan_date\":\"Delivery confirm\"}," +
                    "{\"note\":\"Note\"}," +
                    "{\"trang_thai\":\"Status\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }
            if (headerKey.get(0).equals("stt")) {
                headerKey.remove(0);
            } else {
                headerValue.add(0, "STT");
            }

            for (JSONObject js : records) {
//                        "{\"oc\":\"OC\"}," +
                js.put("oc", js.get("doc_no"));
//                        "{\"ten_dai_ly\":\"Customer\"}," +
                js.put("ten_dai_ly", js.get("shop_name"));
//                        "{\"ngay_tao_don\":\"OC Date\"}," +
                js.put("ngay_tao_don", DateTimeUtils.toString(
                        DateTimeUtils.getDateTime(js.get("created_date").toString(), "yyyy-MM-dd HH:mm:ss"), "dd/MM/yyyy"));
//                        "{\"sku\":\"Product\"}," +
                JSONObject jsProductInfo = JsonUtils.DeSerialize(js.get("product_info").toString(), JSONObject.class);
                js.put("sku", jsProductInfo == null ? "" : jsProductInfo.get("full_name"));
//                        "{\"quantity\":\"Quantity\"}," +
                js.put("quantity", js.get("product_total_quantity"));
//                        "{\"price\":\"Price\"}," +
                js.put("price", js.get("product_price"));
//                        "{\"tong_tien\":\"Amount\"}," +
                js.put("tong_tien", js.get("product_total_end_price"));
//                        "{\"request_date\":\"Delivery request\"}," +
                js.put("request_date",
                        DateTimeUtils.toString(
                                DateTimeUtils.getDateTime(js.get("request_delivery_date").toString(), "yyyy-MM-dd HH:mm:ss"), "dd/MM/yyyy"));
//                        "{\"plan_date\":\"Delivery confirm\"}," +
                js.put("plan_date", DateTimeUtils.toString(
                        DateTimeUtils.getDateTime(js.get("plan_delivery_date").toString(), "yyyy-MM-dd HH:mm:ss"), "dd/MM/yyyy"));
//                        "{\"note\":\"Note\"}," +
                js.put("note", js.get("note_internal"));
//                        "{\"trang_thai\":\"Trạng thái\"}" +
                js.put("trang_thai",
                        js.get("status_name")
                );
            }
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("OC");

            exporter.writeHeaderLine(workbook, sheet, headerValue);

            AtomicInteger stt = new AtomicInteger(1);

            //Count total
            //Limit config cvs
            records.forEach(js -> {
                try {
                    List<Object> dataLines = new ArrayList<>();
                    int rowCount = stt.get();
                    dataLines.add(ConvertUtils.toString(rowCount));
                    //                        "{\"oc\":\"OC\"}," +
                    dataLines.add(ConvertUtils.toString(js.get("doc_no")));
//                        "{\"ten_dai_ly\":\"Customer\"}," +
                    dataLines.add(ConvertUtils.toString(js.get("shop_name")));
//                        "{\"ngay_tao_don\":\"OC Date\"}," +
                    dataLines.add(ConvertUtils.toString(DateTimeUtils.toString(
                            DateTimeUtils.getDateTime(js.get("created_date").toString(), "yyyy-MM-dd HH:mm:ss"), "dd/MM/yyyy")));
//                        "{\"sku\":\"Product\"}," +
                    JSONObject jsProductInfo = JsonUtils.DeSerialize(js.get("product_info").toString(), JSONObject.class);
                    dataLines.add(ConvertUtils.toString(jsProductInfo == null ? "" : jsProductInfo.get("full_name")));
//                        "{\"quantity\":\"Quantity\"}," +
                    dataLines.add(ConvertUtils.toInt(js.get("product_total_quantity")));
//                        "{\"price\":\"Price\"}," +
                    dataLines.add(ConvertUtils.toDouble(js.get("product_price")));
//                        "{\"tong_tien\":\"Amount\"}," +
                    dataLines.add(ConvertUtils.toDouble(js.get("product_total_end_price")));
//                        "{\"request_date\":\"Delivery request\"}," +
                    dataLines.add(ConvertUtils.toString(
                            DateTimeUtils.toString(
                                    DateTimeUtils.getDateTime(js.get("request_delivery_date").toString(), "yyyy-MM-dd HH:mm:ss"), "dd/MM/yyyy")));
//                        "{\"plan_date\":\"Delivery confirm\"}," +
                    dataLines.add(ConvertUtils.toString(DateTimeUtils.toString(
                            DateTimeUtils.getDateTime(js.get("plan_delivery_date").toString(), "yyyy-MM-dd HH:mm:ss"), "dd/MM/yyyy")));
//                        "{\"note\":\"Note\"}," +
                    dataLines.add(ConvertUtils.toString(js.get("note_internal")));
//                        "{\"trang_thai\":\"Trạng thái\"}" +
                    dataLines.add(ConvertUtils.toString(
                            js.get("status_name")
                    ));
                    exporter.writeDataLines(workbook, sheet, dataLines, rowCount);
                    stt.getAndIncrement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            workbook.write(outputStream);
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }

    private ExcelExporter exporter;

    @Autowired
    public void setExporter(ExcelExporter exporter) {
        this.exporter = exporter;
    }

    private ResponseEntity<?> downloadProductCategory(
            String file_name,
            ExportRequest request,
            HttpServletResponse response) {
        try {
            String date = ConvertUtils.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    + "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    "-" + ConvertUtils.toString(Calendar.getInstance().get(Calendar.YEAR));
            response.setContentType("application/xlsx");
            response.setHeader("Content-Disposition", "attachment; filename=" + file_name + "-" + date + ".xlsx");

            OutputStream outputStream = response.getOutputStream();
            XSSFWorkbook workbook = new XSSFWorkbook();

//            Hiển thị trên App
            this.createSheetProductShowInApp(outputStream, request, workbook);
            workbook.write(outputStream);
            outputStream.close();
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.printDebug("EXPORT", e);
            return ResponseEntity.status(400).body(null);
        }
    }

    private void createSheetProductShowInApp(OutputStream outputStream, ExportRequest request, XSSFWorkbook workbook) {
        try {
            String fields = "[" +
                    "{\"id\":\"Id\"}," +
                    "{\"ten\":\"Tên\"}" +
                    "]";
            List<JSONObject> listField = (List<JSONObject>) CheckValueUtils.parseJSON(fields);

            //Set title
            List<String> headerKey = new ArrayList<>();
            List<String> headerValue = new ArrayList<>();
            for (Object o : listField) {
                JSONObject object = (JSONObject) o;
                object.keySet().forEach(k -> {
                    headerKey.add(k.toString());
                    headerValue.add(object.get(k).toString());
                });
            }

            XSSFSheet sheet = workbook.createSheet("Hiển thị trên APP");

            exporter.writeHeaderLine(workbook, sheet, headerValue);

            AtomicInteger stt = new AtomicInteger(1);

            //Count total
            //Limit config cvs
            Arrays.stream(VisibilityType.values()).forEach(d -> {
                try {
                    List<Object> dataLines = new ArrayList<>();
                    int rowCount = stt.get();
                    stt.getAndIncrement();

                    dataLines.add(ConvertUtils.toString(d.getId()));
                    dataLines.add(ConvertUtils.toString(d.getLabel()));
                    exporter.writeDataLines(workbook, sheet, dataLines, rowCount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            LogUtil.printDebug("EXPORT", e);
        }
    }
}