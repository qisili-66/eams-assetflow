package org.example.eams.vo;

import org.example.eams.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public record UserPageVo(
        Long id,
        String username,
        String nickname,
        Long departmentId,
        String departmentName,
        UserStatus status,
        List<String> roleCodes,
        LocalDateTime createdAt
) {
}
