package org.example.eams.vo;

import java.util.List;

public record CurrentUserVo(
        Long id,
        String username,
        String nickname,
        Long departmentId,
        String departmentName,
        List<String> roles,
        List<String> permissions
) {
}
