package org.example.eams.dto.user;

import jakarta.validation.constraints.NotNull;
import org.example.eams.enums.UserStatus;

public record UserStatusRequest(
        @NotNull(message = "用户状态不能为空")
        UserStatus status
) {
}
