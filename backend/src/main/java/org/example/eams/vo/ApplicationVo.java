package org.example.eams.vo;

import org.example.eams.enums.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationVo(
        Long id,
        Long assetId,
        String assetNo,
        String assetName,
        String assetCategory,
        Long applicantId,
        String applicantName,
        String reason,
        ApplicationStatus status,
        Long auditorId,
        String auditorName,
        String auditComment,
        LocalDateTime auditedAt,
        LocalDateTime createdAt
) {
}
