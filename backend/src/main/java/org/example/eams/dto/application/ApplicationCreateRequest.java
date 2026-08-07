package org.example.eams.dto.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationCreateRequest(
        @NotNull(message = "资产不能为空") Long assetId,
        @NotBlank(message = "申请原因不能为空") @Size(max = 500, message = "申请原因不能超过500字") String reason
) {
}
