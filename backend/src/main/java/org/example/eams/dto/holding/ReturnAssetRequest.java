package org.example.eams.dto.holding;

import jakarta.validation.constraints.Size;

public record ReturnAssetRequest(
        @Size(max = 500, message = "归还备注不能超过500字") String remark
) {
}
