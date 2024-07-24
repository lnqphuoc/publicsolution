package com.app.server.data.dto.program;

import com.app.server.data.dto.program.limit.ProgramLimit;
import com.app.server.data.dto.program.product.OfferProduct;
import com.app.server.data.dto.program.product.ProgramOrderProduct;
import com.app.server.data.dto.program.product.ProgramProduct;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class SortUtil {

    public Comparator<ProgramOrderProduct> getProgramOrderProductComparator() {
        return Comparator.comparing(ProgramOrderProduct::getProductPrice).reversed();
    }

    public Comparator<OfferProduct> getOfferProductComparator() {
        Comparator<OfferProduct> compareByProductCategoryParentPriority = Comparator.comparing(OfferProduct::getProductCategoryParentPriority);
        Comparator<OfferProduct> compareByProductCategoryPriority = Comparator.comparing(OfferProduct::getProductCategoryPriority);
        Comparator<OfferProduct> compareByProductGroupSortData = Comparator.comparing(OfferProduct::getProductGroupSortData);
        Comparator<OfferProduct> compareByProductSortData = Comparator.comparing(OfferProduct::getProductSortData);
        return compareByProductCategoryParentPriority.thenComparing(compareByProductCategoryPriority).thenComparing(compareByProductGroupSortData).thenComparing(compareByProductSortData);
    }

    /**
     * Sắp xếp hạn mức chương trình
     */
    public void sortProgramLimit(List<ProgramLimit> ltProgramLimit) {
        if (ltProgramLimit.isEmpty())
            return;
        ltProgramLimit.sort(Comparator.comparingInt(ProgramLimit::getLevel).reversed());
    }

    /**
     * Sắp xếp chương trình
     */
    public void sortProgram(List<Program> ltProgram) {
        if (ltProgram.isEmpty())
            return;
        Comparator<Program> compareByPriority = Comparator.comparing(Program::getPriority);
        ltProgram.sort(compareByPriority);
    }

    /**
     * Tạo bộ so sánh để trừ sản phẩm trong giỏ hàng săn sale
     */
    public Comparator<ProgramProduct> getProgramProductComparator() {
        return Comparator.comparing(ProgramProduct::getProductPrice);
    }
}