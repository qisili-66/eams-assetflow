package org.example.eams.vo;

import org.example.eams.enums.AssetStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssetVo(
        Long id,
        String assetNo,
        String name,
        String category,
        BigDecimal price,
        LocalDate purchaseDate,
        AssetStatus status,
        String imageUrl,
        String remark,
        String currentUserName,
        String scrapReason,
        LocalDateTime scrappedAt,
        LocalDateTime createdAt
) {
}
