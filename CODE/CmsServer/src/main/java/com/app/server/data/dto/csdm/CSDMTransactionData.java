package com.app.server.data.dto.csdm;

import com.app.server.data.dto.cttl.CTTLProduct;
import com.app.server.data.dto.program.product.ProgramProduct;
import com.app.server.enums.CTTLTransactionType;
import com.app.server.enums.TransactionCTTLStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CSDMTransactionData {
    private long dtt;
    private long tt;
    private long createdTime;
    private List<Integer> ltExcludeProductId;
    private int transactionId;
    private int order_id;
    private String type;
    private List<CTTLProduct> ltTLProduct;
    private int status;
    private int source;
    private String code;
    private int transactionSource;
    private int staffId;
    private int transaction_type;
    private long transaction_value;
    private long thanh_toan;
    private long payment_date;
    private long payment_value;
    private long decreaseOrderPrice;
    private Map<Integer, Long> mpDecreaseProductPrice;
    private long updateTotalValue;
    private int accumulate_type;
    private long totalValue;

    public long sumDTTOfProduct() {
        return ltTLProduct.stream().reduce(
                0L,
                (total, object) -> total + object.getProductDtt(), Long::sum
        );
    }

    public Long sumGiaTriThamGia(boolean is_cttl_product_quantity) {
        if (status == TransactionCTTLStatus.THOA.getId()) {
            if (is_cttl_product_quantity) {
                return ltTLProduct.stream().reduce(
                        0L,
                        (total, object) -> total + object.getProductQuantity(), Long::sum
                );
            } else {
                if (isDonHang(transaction_type)) {
                    return ltTLProduct.stream().reduce(
                            0L,
                            (total, object) -> total + object.getProductDtt(), Long::sum
                    );
                } else {
                    return dtt;
                }
            }
        }
        return 0L;
    }

    public Long sumDTTThamGia(boolean is_cttl_product_quantity) {
        if (isDonHang(transaction_type)) {
            return ltTLProduct.stream().reduce(
                    0L,
                    (total, object) -> total + object.getProductDtt(), Long::sum
            );
        } else {
            return dtt;
        }
    }

    public Long sumGiaTri() {
        if (isDonHang(transaction_type)) {
            return ltTLProduct.stream().reduce(
                    0L,
                    (total, object) -> total + object.getProductDtt(), Long::sum
            );
        } else {
            return dtt;
        }
    }

    public Integer sumSanPhamTichLuy() {
        return ltTLProduct.stream().reduce(
                0,
                (total, object) -> total + object.getProductQuantity(), Integer::sum
        );
    }

    public Long sumDoanhThuThuanSanPhamTichLuy(
            boolean is_cttl_product,
            Map<Integer, ProgramProduct> mpProductInPromo
    ) {
        if (is_cttl_product) {
            return ltTLProduct.stream().reduce(
                    0L,
                    (total, object) ->
                            total + (
                                    (
                                            (is_cttl_product == false ||
                                                    mpProductInPromo.containsKey(object.getProductId())) &&
                                                    !ltExcludeProductId.contains(object.getProductId())
                                    ) ? object.getProductQuantity() : 0
                            ),
                    Long::sum
            );
        }
        return 0L;
    }

    public boolean isHopLe(boolean is_cttl_product_quantity, long payment_date_data) {
        if (sumGiaTriThamGia(is_cttl_product_quantity) == 0 ||
                payment_date_data < payment_date ||
                transaction_value > payment_value) {
            return false;
        }
        return true;
    }

    public Long sumGiaTriThanhToanHopLe(
            boolean is_cttl_product_quantity,
            long payment_date_data
    ) {
        if (isHopLe(is_cttl_product_quantity, payment_date_data)) {
            if (type.equals(CTTLTransactionType.DON_HANG.getKey())) {
                return sumGiaTriThamGia(is_cttl_product_quantity);
            } else if (type.equals(CTTLTransactionType.TANG_CONG_NO.getKey())) {
                return sumGiaTriThamGia(is_cttl_product_quantity);
            } else if (type.equals(CTTLTransactionType.DIEU_CHINH_DTT.getKey())) {
                return sumGiaTriThamGia(is_cttl_product_quantity);
            } else if (type.equals(CTTLTransactionType.HBTL.getKey())) {
                return sumGiaTriThamGia(is_cttl_product_quantity);
            } else if (type.equals(CTTLTransactionType.GIAM_CONG_NO.getKey())) {
                return sumGiaTriThamGia(is_cttl_product_quantity);
            } else {
                return sumGiaTriThamGia(is_cttl_product_quantity);
            }
        } else {
            return 0L;
        }
    }

    public Long sumTongTienThanhToanHopLe(
            boolean is_cttl_product_quantity,
            long payment_date_data
    ) {
        if (isHopLe(is_cttl_product_quantity, payment_date_data)) {
            if (type.equals(CTTLTransactionType.DON_HANG.getKey())) {
                return tt;
            } else if (type.equals(CTTLTransactionType.TANG_CONG_NO.getKey())) {
                return tt;
            } else if (type.equals(CTTLTransactionType.DIEU_CHINH_DTT.getKey())) {
                return tt;
            } else if (type.equals(CTTLTransactionType.HBTL.getKey())) {
                return tt;
            } else if (type.equals(CTTLTransactionType.GIAM_CONG_NO.getKey())) {
                return tt;
            } else {
                return tt;
            }
        } else {
            return 0L;
        }
    }

    public Long sumGiaTriThanhToan() {
        if (type.equals(CTTLTransactionType.DON_HANG.getKey())) {
            return tt;
        } else if (type.equals(CTTLTransactionType.TANG_CONG_NO.getKey())) {
            return tt;
        } else if (type.equals(CTTLTransactionType.DIEU_CHINH_DTT.getKey())) {
            return tt;
        } else if (type.equals(CTTLTransactionType.HBTL.getKey())) {
            return tt;
        } else if (type.equals(CTTLTransactionType.GIAM_CONG_NO.getKey())) {
            return tt;
        } else {
            return tt;
        }
    }

    public Long sumGiaTriTichLuy(
            boolean is_cttl_product,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product_quantity
    ) {
        if (status == TransactionCTTLStatus.THOA.getId()) {
            if (is_cttl_product_quantity) {
                return ltTLProduct.stream().reduce(
                        0L,
                        (total, object) ->
                                total + (
                                        (
                                                (is_cttl_product == false ||
                                                        mpProductInPromo.containsKey(object.getProductId())) &&
                                                        !ltExcludeProductId.contains(object.getProductId())
                                        ) ? object.getProductQuantity() : 0
                                ),
                        Long::sum
                );
            } else {
                if (isDonHang(transaction_type)) {
                    return ltTLProduct.stream().reduce(
                            0L,
                            (total, object) ->
                                    total + (
                                            (
                                                    (is_cttl_product == false ||
                                                            mpProductInPromo.containsKey(object.getProductId())) &&
                                                            !ltExcludeProductId.contains(object.getProductId())
                                            ) ? object.getProductDtt() : 0
                                    ),
                            Long::sum
                    );
                } else {
                    return dtt;
                }
            }
        }
        return 0L;
    }

    public Long sumDTTTichLuy(
            boolean is_cttl_product,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product_quantity
    ) {
        if (status == TransactionCTTLStatus.THOA.getId()) {
            if (isDonHang(transaction_type)) {
                return ltTLProduct.stream().reduce(
                        0L,
                        (total, object) ->
                                total + (
                                        (
                                                (is_cttl_product == false ||
                                                        mpProductInPromo.containsKey(object.getProductId())) &&
                                                        !ltExcludeProductId.contains(object.getProductId())
                                        ) ? object.getProductDtt() : 0
                                ),
                        Long::sum
                );
            } else {
                return dtt;
            }
        }
        return 0L;
    }

    public Long sumGiaTriTichLuyThanhToan(
            boolean is_cttl_product,
            Map<Integer, ProgramProduct> mpProductInPromo,
            boolean is_cttl_product_quantity,
            long payment_date_data
    ) {
        if (status == TransactionCTTLStatus.THOA.getId()) {
            if (isHopLe(is_cttl_product_quantity, payment_date_data)) {
                if (is_cttl_product_quantity) {
                    return ltTLProduct.stream().reduce(
                            0L,
                            (total, object) ->
                                    total + (
                                            (
                                                    (is_cttl_product == false ||
                                                            mpProductInPromo.containsKey(object.getProductId())) &&
                                                            !ltExcludeProductId.contains(object.getProductId())
                                            ) ? object.getProductQuantity() : 0
                                    ),
                            Long::sum
                    );
                } else {
                    if (isDonHang(transaction_type)) {
                        return ltTLProduct.stream().reduce(
                                0L,
                                (total, object) ->
                                        total + (
                                                (
                                                        (is_cttl_product == false ||
                                                                mpProductInPromo.containsKey(object.getProductId())) &&
                                                                !ltExcludeProductId.contains(object.getProductId())
                                                ) ? object.getProductDtt() : 0
                                        ),
                                Long::sum
                        );
                    } else {
                        return dtt;
                    }
                }
            }
        }
        return 0L;
    }

    public boolean isDonHang(int transaction_type) {
        if (transaction_type == CTTLTransactionType.DON_HANG.getId() ||
                transaction_type == CTTLTransactionType.HBTL.getId()) {
            return true;
        }
        return false;
    }

    public void finishPayment() {
        thanh_toan = transaction_value;
        tt = thanh_toan;
    }
}