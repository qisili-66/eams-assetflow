package org.example.eams.dto.asset;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetSaveRequest(
        @NotBlank(message = "资产编号不能为空")
        @Size(max = 50, message = "资产编号不能超过50个字符")
        String assetNo,

        @NotBlank(message = "资产名称不能为空")
        @Size(max = 100, message = "资产名称不能超过100个字符")
        String name,

        @NotBlank(message = "资产分类不能为空")
        @Size(max = 30, message = "资产分类不能超过30个字符")
        String category,

        @NotNull(message = "资产价格不能为空")
        @DecimalMin(value = "0.00", message = "资产价格不能小于0")
        BigDecimal price,

        @NotNull(message = "购入日期不能为空")
        LocalDate purchaseDate,

        @Size(max = 500, message = "图片地址不能超过500个字符")
        String imageUrl,

        @Size(max = 500, message = "备注不能超过500个字符")
        String remark
) {
}
