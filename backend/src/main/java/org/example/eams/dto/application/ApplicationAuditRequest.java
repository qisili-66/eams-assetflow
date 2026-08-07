package org.example.eams.dto.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.eams.enums.ApplicationStatus;

public record ApplicationAuditRequest(
        @NotNull(message = "审核结果不能为空") ApplicationStatus decision,
        @Size(max = 500, message = "审核意见不能超过500字") String comment
) {
}
